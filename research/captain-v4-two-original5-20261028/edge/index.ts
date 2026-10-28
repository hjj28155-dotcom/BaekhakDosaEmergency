import {createClient} from 'npm:@supabase/supabase-js@2.57.4';
const URL=Deno.env.get('SUPABASE_URL')||'',KEY=Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')||'';
const CLIENT=createClient(URL,KEY,{auth:{persistSession:false,autoRefreshToken:false}});
const CAPTAIN_ROOM='96774ce6-c34f-4505-bbcc-c62e07d3ef3a';
const BASE_ORIGIN='https://hjj28155-dotcom.github.io';
const ENGINE_BLOB='ff50617c892bddc16109b62e4f90d59d3c4a6664';
function cors(req:Request){const origin=req.headers.get('origin')||'';return {'Content-Type':'application/json; charset=utf-8','Cache-Control':'private,no-store','Vary':'Origin','Access-Control-Allow-Origin':origin===BASE_ORIGIN?origin:BASE_ORIGIN,'Access-Control-Allow-Headers':'authorization,content-type','Access-Control-Allow-Methods':'GET,POST,OPTIONS','X-Content-Type-Options':'nosniff'};}
function reply(req:Request,x:unknown,status=200){return new Response(JSON.stringify(x),{status,headers:cors(req)});}
function hex(bytes:Uint8Array){return [...bytes].map(b=>b.toString(16).padStart(2,'0')).join('');}
async function sha256(s:string){return hex(new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(s))));}
async function captain(req:Request){
 const header=req.headers.get('authorization')||'',m=/^Bearer ([a-f0-9]{48,64})$/.exec(header);
 if(!m)return false;
 const hash=await sha256(m[1]);
 const {data,error}=await CLIENT.from('crew_meeting_rooms').select('id').eq('id',CAPTAIN_ROOM).eq('access_hash',hash).maybeSingle();
 return !error&&!!data;
}
function roundValid(n:unknown):n is number{return Number.isInteger(n)&&Number(n)>=1&&Number(n)<=1247;}
function packValid(g:unknown):g is number[][] {
 return Array.isArray(g)&&g.length===5&&g.every(x=>Array.isArray(x)&&x.length===6&&
   x.every(n=>Number.isInteger(n)&&n>=1&&n<=45)&&new Set(x).size===6);
}
function normalize(pack:number[][]){return pack.map(g=>g.slice().sort((a,b)=>a-b));}
async function getSource(round:number){
 const {data,error}=await CLIENT.from('v6_round_sources').select('round_no,room_key,source_type,source_text,source_sha256,status').eq('round_no',round).eq('room_key','all').maybeSingle();
 if(error||!data||!data.source_text||typeof data.source_sha256!=='string')throw new Error('SOURCE_NOT_FOUND');
 const raw=String(data.source_text),lines=raw.split(/\r?\n/).map(s=>s.trim()).filter(Boolean);
 if(lines.length!==6||raw.length>20000)throw new Error('SOURCE_SIX_LINE_INVALID');
 if(await sha256(raw)!==data.source_sha256)throw new Error('SOURCE_SHA_MISMATCH');
 return {round_no:round,source_text:raw,source_sha256:data.source_sha256,source_type:data.source_type,status:data.status};
}
Deno.serve(async(req)=>{
 if(req.method==='OPTIONS')return new Response(null,{status:204,headers:cors(req)});
 if(!URL||!KEY)return reply(req,{error:'NOT_CONFIGURED'},503);
 if(req.method!=='GET'&&req.method!=='POST')return reply(req,{error:'METHOD_NOT_ALLOWED'},405);
 try{if(!await captain(req))return reply(req,{error:'CAPTAIN_AUTH_REQUIRED'},401);}
 catch{return reply(req,{error:'CAPTAIN_AUTH_UNAVAILABLE'},503);}
 if(req.method==='GET'){
  const params=new URL(req.url).searchParams,raw=params.get('round')||'1';
  if(!/^[1-9][0-9]{0,3}$/.test(raw)||!roundValid(Number(raw)))return reply(req,{error:'INVALID_ROUND'},400);
  const round=Number(raw);
  try{
   const source=await getSource(round);
   const {data,error}=await CLIENT.from('crew_v4_research_reports').select('id,round_no,source_sha256,engine_source_blob,pack_a,pack_b,report_sha256,created_at,report_class').eq('round_no',round).order('created_at',{ascending:false}).limit(20);
   if(error)throw new Error('REPORT_READ_FAILED');
   return reply(req,{ok:true,read_only:true,engine_blob:ENGINE_BLOB,round,research_only:true,source,reports:data||[],note:'두 묶음 모두 연구 전용. 기존 공식 구매잠금/결과전 원본과 별도.'});
  }catch(e){return reply(req,{error:String(e instanceof Error?e.message:'READ_FAILED')},500);}
 }
 // Body limit and immutable append-only reports: NEVER touch original V4/public records.
 const length=Number(req.headers.get('content-length')||0);
 if(length>20000)return reply(req,{error:'PAYLOAD_TOO_LARGE'},413);
 let body:any;try{body=await req.json();}catch{return reply(req,{error:'INVALID_JSON'},400);}
 const round=Number(body?.round_no),pa=body?.pack_a,pb=body?.pack_b;
 if(!roundValid(round)||!packValid(pa)||!packValid(pb)||body.engine_source_blob!==ENGINE_BLOB||
    typeof body.source_sha256!=='string'||!/^[a-f0-9]{64}$/.test(body.source_sha256))
   return reply(req,{error:'INVALID_TWO_PACK_PAYLOAD'},400);
 const a=normalize(pa),b=normalize(pb),keys=[...a,...b].map(x=>x.join(','));
 if(new Set(keys).size!==10)return reply(req,{error:'DUPLICATE_COMBINATIONS'},409);
 try{
  const source=await getSource(round);
  if(source.source_sha256!==body.source_sha256)return reply(req,{error:'ORIGINAL_SOURCE_CHANGED'},409);
  const digest=await sha256(JSON.stringify({round,source_sha256:source.source_sha256,engine_source_blob:ENGINE_BLOB,pack_a:a,pack_b:b}));
  const {data:found,error:foundErr}=await CLIENT.from('crew_v4_research_reports').select('id,created_at').eq('report_sha256',digest).maybeSingle();
  if(foundErr)throw new Error('DEDUP_CHECK_FAILED');
  if(found)return reply(req,{ok:true,duplicate:true,report_id:found.id,report_sha256:digest,created_at:found.created_at,research_only:true,official_eligible:false});
  const {data,error}=await CLIENT.from('crew_v4_research_reports')
   .insert({round_no:round,source_sha256:source.source_sha256,engine_source_blob:ENGINE_BLOB,pack_a:a,pack_b:b,report_sha256:digest,report_class:'RETROSPECTIVE_RESEARCH',research_only:true})
   .select('id,created_at').single();
  if(error)throw new Error('REPORT_INSERT_FAILED');
  return reply(req,{ok:true,duplicate:false,report_id:data.id,report_sha256:digest,created_at:data.created_at,research_only:true,official_eligible:false},201);
 }catch(e){return reply(req,{error:String(e instanceof Error?e.message:'SAVE_FAILED')},500);}
});