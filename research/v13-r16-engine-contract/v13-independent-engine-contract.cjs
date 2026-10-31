'use strict';
// V13 R16 candidate: untrusted report shape checks only, NOT a deployed engine.
const ROOM_COUNTS=Object.freeze({captain:5,dosa:5,freedom:15,navigator:5});
const FREEDOM_GROUPS=Object.freeze(['RAW5','AUTO_A5','AUTO_B5']);
const isSet=a=>Array.isArray(a)&&a.length===6&&new Set(a).size===6&&a.every(n=>Number.isInteger(n)&&n>=1&&n<=45);
const signature=a=>a.slice().sort((x,y)=>x-y).join(',');
function inspectReport(report,{round,rawFingerprint}={}) {
 if(!report||typeof report!=='object'||Array.isArray(report))return {ok:false,reason:'INVALID_REPORT'};
 const key=report.roomKey,expected=ROOM_COUNTS[key];
 if(!expected)return {ok:false,reason:'UNKNOWN_ROOM'};
 if(report.schema!=='V13_PREDRAW_ROOM_REPORT_V1'||report.phase!=='BEFORE'||report.round!==round)return {ok:false,reason:'WRONG_PHASE_OR_ROUND'};
 if(!rawFingerprint||report.rawFingerprint!==rawFingerprint)return {ok:false,reason:'RAW_FINGERPRINT_MISMATCH'};
 const sets=report.combinations;
 if(!Array.isArray(sets)||sets.length!==expected||!sets.every(isSet))return {ok:false,reason:'ROOM_COUNT_OR_NUMBERS_INVALID'};
 const signs=sets.map(signature);
 if(new Set(signs).size!==expected)return {ok:false,reason:'ROOM_INTERNAL_DUPLICATES'};
 if(key==='freedom') {
  const groups=report.groups;
  if(!Array.isArray(groups)||groups.length!==3)return {ok:false,reason:'FREEDOM_GROUPS_MISSING'};
  for(let i=0;i<3;i++){
   const g=groups[i],part=sets.slice(i*5,i*5+5);
   if(!g||g.id!==FREEDOM_GROUPS[i]||!Array.isArray(g.combinations)||g.combinations.length!==5||!g.combinations.every(isSet)||g.combinations.some((x,j)=>signature(x)!==signature(part[j])))return {ok:false,reason:'FREEDOM_GROUP_MISMATCH'};
  }
 }
 if(report.winningNumbers||report.bonus||report.grade||report.resultAfter)return {ok:false,reason:'RESULT_AFTER_CONTAMINATION'};
 return {ok:true,reason:'FORMAT_ONLY_NOT_OFFICIAL',count:expected,room:key,official_eligible:false};
}
function inspectThirty(reports,{round,rawFingerprint}={}){
 const rooms=Object.keys(ROOM_COUNTS);
 if(!Array.isArray(reports)||reports.length!==rooms.length)return {ok:false,reason:'FOUR_ROOMS_REQUIRED',official_eligible:false};
 const seen=new Set(),checked=new Set();
 for(const r of reports){const report=inspectReport(r,{round,rawFingerprint});if(!report.ok)return report;
  if(checked.has(r.roomKey))return {ok:false,reason:'DUPLICATE_ROOM',official_eligible:false};checked.add(r.roomKey);
  for(const set of r.combinations){const sig=signature(set);if(seen.has(sig))return {ok:false,reason:'CROSS_ROOM_DUPLICATE',official_eligible:false};seen.add(sig);}}
 if(checked.size!==4||seen.size!==30)return {ok:false,reason:'THIRTY_INCOMPLETE',official_eligible:false};
 // An external trusted verifier and independently archived pre-draw lock are still mandatory.
 return {ok:true,reason:'30_FORMAT_VALID_PROVENANCE_NOT_ATTESTED',count:30,official_eligible:false};
}
module.exports={ROOM_COUNTS,FREEDOM_GROUPS,inspectReport,inspectThirty};
