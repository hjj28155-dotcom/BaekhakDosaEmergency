// V8 independent read-only draw references, migrated from V7 without modifying source.
// V3 R60 must update its hardcoded V7 URL in a same-signer APK before V7 is retired.
import { createClient } from "npm:@supabase/supabase-js@2.57.4";
const cors={"Access-Control-Allow-Origin":"*","Access-Control-Allow-Methods":"GET, OPTIONS","Access-Control-Allow-Headers":"Content-Type","Content-Type":"application/json; charset=utf-8","X-Content-Type-Options":"nosniff"};
const respond=(payload:unknown,status=200)=>new Response(JSON.stringify(payload),{status,headers:{...cors,"Cache-Control":status===200?"public, max-age=1800":"no-store"}});
Deno.serve(async req=>{
  if(req.method==="OPTIONS")return new Response(null,{status:204,headers:cors});
  if(req.method!=="GET")return respond({error:"GET_ONLY"},405);
  const parsed=new URL(req.url);
  const candidate=parsed.searchParams.get("round_no");
  if(!candidate||!/^[1-9][0-9]{0,3}$/.test(candidate))return respond({error:"INVALID_ROUND"},400);
  const round=Number(candidate);
  if(round<1||round>9999)return respond({error:"INVALID_ROUND"},400);
  const url=Deno.env.get("SUPABASE_URL");
  const role=Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if(!url||!role)return respond({error:"SERVER_NOT_CONFIGURED"},503);
  try{
    const db=createClient(url,role,{auth:{persistSession:false,autoRefreshToken:false}});
    const {data,error}=await db.from("v8_draw_references").select("round_no,numbers,bonus,source_url,confirmed_at,independently_source_verified,migrated_from").eq("round_no",round).maybeSingle();
    if(error)return respond({error:"OFFICIAL_DB_UNAVAILABLE"},503);
    if(!data)return respond({round_no:round,status:"NOT_CONFIRMED"},404);
    const ns=data.numbers;
    if(!Array.isArray(ns)||ns.length!==6||new Set(ns).size!==6||ns.some((n:unknown)=>!Number.isInteger(n)||Number(n)<1||Number(n)>45)||!Number.isInteger(data.bonus)||data.bonus<1||data.bonus>45||ns.includes(data.bonus)||!data.confirmed_at)return respond({error:"OFFICIAL_DATA_INVALID"},503);
    return respond({round_no:round,status:"CONFIRMED",numbers:[...ns].sort((a,b)=>a-b),bonus:data.bonus,source_url:data.source_url||null,confirmed_at:data.confirmed_at,independent_source_verified:false,legacy_reference:true,source_scope:"V8_MIGRATED_REFERENCE"});
  }catch{return respond({error:"READ_FAILED"},503)}
});