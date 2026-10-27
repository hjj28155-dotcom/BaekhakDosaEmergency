import {createClient} from 'npm:@supabase/supabase-js@2.57.4';

// Captain's independent central dashboard: READ ONLY auto-routing of existing source records.
// This function does NOT create/alter result-before originals, grades, locks or app data.
const URL=Deno.env.get('SUPABASE_URL')||'';
const SECRET=Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')||'';
const db=createClient(URL,SECRET,{auth:{persistSession:false,autoRefreshToken:false}});
const ROOM='96774ce6-c34f-4505-bbcc-c62e07d3ef3a';
const ORIGIN='https://hjj28155-dotcom.github.io';
const MAX_ROUND=1247;
const ROOMS=[
 {id:'v3-1',app:'v3',name:'V3 제1방'},
 {id:'v3-2',app:'v3',name:'V3 제2방'},
 {id:'v3-3',app:'v3',name:'V3 제3방'},
 {id:'v3-4',app:'v3',name:'V3 제4방'},
 {id:'v7',app:'v7',name:'V7 통제관방'},
 {id:'v4',app:'v4',name:'V4 항해의자유방'}
] as const;
// 15+5 are the V3 APP configuration across its four rooms; not a claim of 20 per room.
const COMPOSITION={
 v3:{rooms:4,original:15,automatic:5,configuredTotal:20},
 v7:{rooms:1,original:5,automatic:0,configuredTotal:5},
 v4:{rooms:1,original:10,automatic:0,configuredTotal:10}
} as const;
function send(req:Request,payload:unknown,status=200){
 const origin=req.headers.get('Origin')||'';
 return new Response(JSON.stringify(payload),{status,headers:{
  'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store, private',
  'X-Content-Type-Options':'nosniff','Vary':'Origin',
  'Access-Control-Allow-Origin':origin===ORIGIN?origin:ORIGIN,
  'Access-Control-Allow-Methods':'GET, OPTIONS',
  'Access-Control-Allow-Headers':'authorization,content-type',
 }});
}
async function captain(req:Request){
 const m=/^Bearer ([0-9a-f]{48,64})$/.exec(req.headers.get('Authorization')||'');
 if(!m)return false;
 const buf=await crypto.subtle.digest('SHA-256',new TextEncoder().encode(m[1]));
 const hash=Array.from(new Uint8Array(buf),b=>b.toString(16).padStart(2,'0')).join('');
 const {data,error}=await db.from('crew_meeting_rooms').select('id')
   .eq('id',ROOM).eq('access_hash',hash).maybeSingle();
 return !error&&!!data;
}
function validRound(x:unknown){return Number.isInteger(x)&&Number(x)>=1&&Number(x)<=MAX_ROUND;}
function beforeMarker(name:string){
 return /(?:결과[\s_-]*전|result[\s_-]*before|before[\s_-]*result|pre[\s_-]*draw)/i.test(name);
}
function validSha(s:unknown){return typeof s==='string'&&/^[a-f0-9]{64}$/i.test(s);}
function cutoff(round:number){return Date.UTC(2002,11,7,11,35)+(round-1)*604800000;}
// Do not label a past reproduction as a pre-draw forecast even if filename says RESULT-BEFORE.
function classifyPreDraw(round:number,created_at:unknown,recorded:boolean,sha:unknown){
 return recorded===true&&validSha(sha)&&typeof created_at==='string'&&
  Number.isFinite(Date.parse(created_at))&&Date.parse(created_at)<cutoff(round)
  ? 'PRE_DRAW_METADATA_ONLY' : 'RETROSPECTIVE_OR_UNVERIFIED';
}
type Entry={room_id:string;app:string;round_no:number;record_id:string;source:string;
 origin_room:string;filename:string;source_sha256:string|null;created_at:string|null;
 verification:string;official_eligible:false;record_type:'RESULT-BEFORE';};
async function loadV7(round:number|null){
 let q=db.from('v7_documents').select('id,round_no,room_key,document_type,stage,filename,source_sha256,created_at,received_before_draw')
 .eq('document_type','V7_PURCHASE_5').eq('stage','RESULT-BEFORE')
 .gte('round_no',1).lte('round_no',MAX_ROUND)
 .order('round_no',{ascending:false}).limit(80);
 if(round!==null)q=q.eq('round_no',round);
 const {data,error}=await q;if(error)throw Error('V7_READ_FAILED');
 return (data||[]).filter(x=>validRound(x.round_no)).map((x):Entry=>({
  room_id:'v7',app:'v7',round_no:x.round_no,record_id:String(x.id),
  source:'v7_documents',origin_room:String(x.room_key||''),filename:String(x.filename||'V7_PURCHASE_5'),
  source_sha256:validSha(x.source_sha256)?x.source_sha256:null,
  created_at:x.created_at||null,
  verification:classifyPreDraw(x.round_no,x.created_at,x.received_before_draw,x.source_sha256),
  official_eligible:false,record_type:'RESULT-BEFORE'
 }));
}
async function loadV4(round:number|null){
 let q=db.from('v4_sync_files')
   .select('id,room_key,round_no,filename,sha256,created_at')
   .eq('room_key','freedom').is('deleted_at',null)
   .gte('round_no',1).lte('round_no',MAX_ROUND)
   .order('created_at',{ascending:false}).limit(80);
 if(round!==null)q=q.eq('round_no',round);
 const {data,error}=await q;if(error)throw Error('V4_READ_FAILED');
 // Filename is only a candidate label, NOT proof the report was created before the draw.
 return (data||[]).filter(x=>validRound(x.round_no)&&beforeMarker(String(x.filename||'')))
 .map((x):Entry=>({
  room_id:'v4',app:'v4',round_no:x.round_no,record_id:String(x.id),
  source:'v4_sync_files',origin_room:'freedom',filename:String(x.filename||''),
  source_sha256:validSha(x.sha256)?x.sha256:null,
  created_at:x.created_at||null,
  verification:'FILENAME_ONLY_UNVERIFIED',official_eligible:false,record_type:'RESULT-BEFORE'
 }));
}
Deno.serve(async req=>{
 if(req.method==='OPTIONS')return send(req,{ok:true});
 if(req.method!=='GET')return send(req,{error:'READ_ONLY'},405);
 if(!URL||!SECRET)return send(req,{error:'NOT_CONFIGURED'},503);
 try{if(!await captain(req))return send(req,{error:'CAPTAIN_AUTH_REQUIRED'},401);}
 catch{return send(req,{error:'CAPTAIN_AUTH_UNAVAILABLE'},503);}
 const u=new URL(req.url);
 const room=u.searchParams.get('room')||'all',raw=u.searchParams.get('round');
 if(room!=='all'&&!ROOMS.some(r=>r.id===room))return send(req,{error:'INVALID_ROOM'},400);
 if(raw!==null&&!/^[1-9][0-9]{0,3}$/.test(raw))return send(req,{error:'INVALID_ROUND'},400);
 const round=raw===null?null:Number(raw);
 if(round!==null&&!validRound(round))return send(req,{error:'ROUND_OUT_OF_RANGE'},400);
 // Only pull from source stores. V3 four rooms have NO verified server sender yet:
 // do not misattribute v6_room_assets or other app's results as V3.
 const needsV7=room==='all'||room==='v7',needsV4=room==='all'||room==='v4';
 const loaded=await Promise.allSettled([
  needsV7?loadV7(round):Promise.resolve([]),
  needsV4?loadV4(round):Promise.resolve([])
 ]);
 const issues=[] as string[],rows=[] as Entry[];
 if(needsV7){if(loaded[0].status==='fulfilled')rows.push(...loaded[0].value);else issues.push('V7_SOURCE_READ_FAILED');}
 if(needsV4){if(loaded[1].status==='fulfilled')rows.push(...loaded[1].value);else issues.push('V4_SOURCE_READ_FAILED');}
 // Duplicate metadata is collapsed only by immutable source ID; no source data is altered.
 const seen=new Set<string>();
 const entries=rows.filter(x=>{
  const key=x.source+':'+x.record_id;if(seen.has(key))return false;seen.add(key);return true;
 }).sort((a,b)=>b.round_no-a.round_no||a.room_id.localeCompare(b.room_id));
 const roomStates=ROOMS.filter(r=>room==='all'||r.id===room).map(r=>{
  const found=entries.filter(x=>x.room_id===r.id);
  const linked=r.app==='v7'?loaded[0].status==='fulfilled':r.app==='v4'?loaded[1].status==='fulfilled':false;
  return {...r,source_readable:linked,source_submission_connected:r.app==='v7'?true:r.app==='v4'?true:false,
   actual_app_auto_push_verified:false,received_candidates:found.length,
   official_before_count:0,state:r.app==='v3'?'SOURCE_CONNECTOR_REQUIRED':
     !linked?'SOURCE_READ_FAILED':found.length?'UNVERIFIED_SOURCE_RECORDS':'NO_REPORTS_RECEIVED'};
 });
 return send(req,{ok:issues.length===0,read_only:true,stage:'RESULT-BEFORE',
  round,scope:room,range:[1,MAX_ROUND],research_restart_round:1,
  legacy_v3_analyses_captain_reported:350,
  composition:COMPOSITION,source_rooms:roomStates,items:entries,errors:issues,
  official_before_count:0,raw_predictions_included:false,
  note:'원본 자동 송신은 앱마다 따로 검증해야 합니다. 과거 재분석은 추첨 전 공식 예측으로 간주하지 않습니다.'
 },issues.length?503:200);
});
