// Crew meeting shared-event bridge v1: shared work messages, NOT original ChatGPT session API.
// The only web write identity is CAPTAIN. Original-thread connector reports must be
// explicitly written by that thread using the connected Supabase tool; no auto-poll
// or direct original-thread message access exists. Never label user-declared roles verified.
import {createClient} from "npm:@supabase/supabase-js@2";
const supabase=createClient(Deno.env.get("SUPABASE_URL")||"",Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")||"",{
 auth:{persistSession:false,autoRefreshToken:false}
});
const ORIGIN="https://hjj28155-dotcom.github.io";
const ROLES=new Set(["captain","navigator","controller","linkofficer"]);
function headers(req:Request){
 const o=req.headers.get("Origin")||"";
 return {
  "Access-Control-Allow-Origin":o===ORIGIN?o:ORIGIN,
  "Access-Control-Allow-Headers":"authorization,content-type",
  "Access-Control-Allow-Methods":"GET,POST,OPTIONS",
  "Cache-Control":"no-store, private",
  "Vary":"Origin",
  "X-Content-Type-Options":"nosniff"
 };
}
function response(req:Request,body:unknown,status=200){
 return new Response(JSON.stringify(body),{status,headers:{...headers(req),"Content-Type":"application/json; charset=utf-8"}});
}
async function getRoom(req:Request){
 const match=/^Bearer ([0-9a-f]{48,64})$/.exec(req.headers.get("authorization")||"");
 if(!match)return null;
 const digest=new Uint8Array(await crypto.subtle.digest("SHA-256",new TextEncoder().encode(match[1])));
 const hash=Array.from(digest).map(b=>b.toString(16).padStart(2,"0")).join("");
 const {data,error}=await supabase.from("crew_meeting_rooms").select("id").eq("access_hash",hash).maybeSingle();
 return error?null:data;
}
Deno.serve(async(req:Request)=>{
 if(req.method==="OPTIONS")return new Response(null,{status:204,headers:headers(req)});
 if(!Deno.env.get("SUPABASE_SERVICE_ROLE_KEY"))return response(req,{error:"SERVICE_UNAVAILABLE"},503);
 if(req.method!=="GET"&&req.method!=="POST")return response(req,{error:"METHOD_NOT_ALLOWED"},405);
 const room=await getRoom(req);
 if(!room)return response(req,{error:"UNAUTHORIZED"},401);
 const action=new URL(req.url).pathname.split("/").filter(Boolean).pop();
 if(req.method==="GET"&&action==="events"){
  const {data,error}=await supabase.from("crew_meeting_bridge_events")
   .select("id,author_role,body,source_kind,provenance,created_at")
   .eq("room_id",room.id).order("created_at",{ascending:false}).limit(100);
  if(error){console.error("CREW_BRIDGE_READ_FAILED");return response(req,{error:"EVENT_LIST_FAILED"},500);}
  return response(req,{ok:true,bridge_kind:"DECLARED_ROLE_SHARED_LOG_NOT_ORIGINAL_SESSION_API",
   original_chat_automatic:false,read_only_history:true,items:(data||[]).filter(e=>ROLES.has(e.author_role)).reverse()});
 }
 if(req.method==="POST"&&action==="captain"){
  let payload:any;
  try{
   const length=Number(req.headers.get("content-length")||0);
   if(length>6000)return response(req,{error:"BODY_TOO_LARGE"},413);
   payload=await req.json();
  }catch{return response(req,{error:"INVALID_JSON"},400);}
  if(!payload||typeof payload.body!=="string")return response(req,{error:"INVALID_TEXT"},400);
  const body=payload.body.trim();
  if(body.length<1||body.length>1200)return response(req,{error:"INVALID_LENGTH"},400);
  // 6-second cooldown prevents accidental rapid/repeated taps; no actor impersonation.
  const since=new Date(Date.now()-6000).toISOString();
  const recent=await supabase.from("crew_meeting_bridge_events")
   .select("id",{head:true,count:"exact"}).eq("room_id",room.id).eq("author_role","captain").gte("created_at",since);
  if(recent.error)return response(req,{error:"RATE_CHECK_FAILED"},500);
  if((recent.count||0)>0)return response(req,{error:"WAIT_SIX_SECONDS"},429);
  const saved=await supabase.from("crew_meeting_bridge_events").insert({
   room_id:room.id,author_role:"captain",body,source_kind:"captain_app",
   provenance:"DECLARED_NOT_SESSION_VERIFIED"
  }).select("id,created_at").single();
  if(saved.error){console.error("CREW_BRIDGE_SAVE_FAILED");return response(req,{error:"SAVE_FAILED"},500);}
  return response(req,{ok:true,saved:true,id:saved.data.id,created_at:saved.data.created_at,
   auto_sent_to_original_chats:false,source_kind:"captain_app"});
 }
 return response(req,{error:"NOT_FOUND"},404);
});
