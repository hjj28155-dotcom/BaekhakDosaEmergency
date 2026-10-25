// Crew meeting v1.0.5 read-only V7 archive bridge. Standalone from existing crew-meeting.
// The ONLY credential is an already-registered crew room bearer token; no passwords
// or V7 device tokens appear in this source. V3 handset files stay local.
import { createClient } from "npm:@supabase/supabase-js@2";
const CLIENT_ORIGIN="https://hjj28155-dotcom.github.io";
const CAPTAIN_ROOM_ID="96774ce6-c34f-4505-bbcc-c62e07d3ef3a";
const db=createClient(Deno.env.get("SUPABASE_URL")||"",Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")||"",{
  auth:{persistSession:false,autoRefreshToken:false}
});
const SCOPES={
  "v7-source":{room:"all",type:"V7_SOURCE"},
  "v7-purchase":{room:"captain",type:"V7_PURCHASE_5"}
};
function reply(req,data,status=200){
  const origin=req.headers.get("origin")||"";
  return new Response(JSON.stringify(data),{
    status,
    headers:{
      "Content-Type":"application/json; charset=utf-8",
      "Cache-Control":"no-store, private",
      "Vary":"Origin",
      "X-Content-Type-Options":"nosniff",
      "Access-Control-Allow-Origin":origin===CLIENT_ORIGIN?origin:CLIENT_ORIGIN,
      "Access-Control-Allow-Headers":"authorization, content-type",
      "Access-Control-Allow-Methods":"GET, OPTIONS"
    }
  });
}
async function authorize(req){
  const value=req.headers.get("authorization")||"";
  const hit=/^Bearer ([0-9a-f]{48,64})$/.exec(value);
  if(!hit)return false;
  const digest=await crypto.subtle.digest("SHA-256",new TextEncoder().encode(hit[1]));
  const hash=[...new Uint8Array(digest)].map(x=>x.toString(16).padStart(2,"0")).join("");
  const {data,error}=await db.from("crew_meeting_rooms").select("id").eq("id",CAPTAIN_ROOM_ID).eq("access_hash",hash).maybeSingle();
  return !error&&!!data;
}
function limitedDoc(doc,detail=false){
  const safe={
    id:doc.id,
    origin:"V7_SERVER",
    room_key:doc.room_key,
    document_type:doc.document_type,
    round_no:doc.round_no,
    filename:doc.filename||null,
    created_at:doc.created_at,
    source_sha256:doc.source_sha256||null,
    received_before_draw:doc.received_before_draw===true,
    stage:doc.stage||null,
    engine:doc.engine||null
  };
  if(detail){
    safe.source_text=doc.document_type==="V7_SOURCE"?String(doc.source_text||"").slice(0,9000):null;
    safe.combinations=doc.document_type==="V7_PURCHASE_5"&&Array.isArray(doc.combinations)?doc.combinations.slice(0,50):[];
  }
  return safe;
}
Deno.serve(async(req)=>{
  if(req.method==="OPTIONS")return reply(req,{ok:true},200);
  if(req.method!=="GET")return reply(req,{error:"READ_ONLY"},405);
  if(!Deno.env.get("SUPABASE_SERVICE_ROLE_KEY"))return reply(req,{error:"SERVICE_UNAVAILABLE"},503);
  const allowed=await authorize(req);
  if(!allowed)return reply(req,{error:"UNAUTHORIZED"},401);
  const url=new URL(req.url);
  const action=url.pathname.split("/").filter(Boolean).pop();
  if(action==="list"){
    const kind=url.searchParams.get("kind")||"v7-purchase";
    const scope=SCOPES[kind];
    if(!scope)return reply(req,{error:"UNKNOWN_REPORT_KIND"},400);
    const exactRound=url.searchParams.get("round");
    if(exactRound!==null&&!/^[1-9][0-9]{0,3}$/.test(exactRound))return reply(req,{error:"INVALID_ROUND"},400);
    let query=db.from("v7_documents").select("id,room_key,document_type,round_no,filename,created_at,source_sha256,received_before_draw,stage,engine")
      .eq("room_key",scope.room).eq("document_type",scope.type).order("round_no",{ascending:false}).limit(60);
    if(exactRound!==null)query=query.eq("round_no",Number(exactRound));
    const {data,error}=await query;
    if(error){console.error("CREW_REPORT_LIST_FAILED");return reply(req,{error:"REPORT_LIST_FAILED"},500);}
    return reply(req,{ok:true,kind,origin:"V7_SERVER",read_only:true,source_scope:scope,items:(data||[]).map(x=>limitedDoc(x))});
  }
  if(action==="detail"){
    const kind=url.searchParams.get("kind")||"";
    const scope=SCOPES[kind];
    const id=url.searchParams.get("id")||"";
    if(!scope||!/^[0-9a-f]{8}-[0-9a-f-]{27,36}$/i.test(id))return reply(req,{error:"INVALID_REPORT_QUERY"},400);
    const {data,error}=await db.from("v7_documents")
      .select("id,room_key,document_type,round_no,filename,created_at,source_sha256,received_before_draw,stage,engine,source_text,combinations")
      .eq("room_key",scope.room).eq("document_type",scope.type).eq("id",id).maybeSingle();
    if(error)return reply(req,{error:"REPORT_READ_FAILED"},500);
    if(!data)return reply(req,{error:"REPORT_NOT_FOUND"},404);
    return reply(req,{ok:true,kind,read_only:true,report:limitedDoc(data,true)});
  }
  return reply(req,{error:"NOT_FOUND"},404);
});
