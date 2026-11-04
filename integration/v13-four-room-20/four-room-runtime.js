/* Four-room / twenty-combination candidate integration.
 * Staging only: never mutates legacy records, locks, signed APK or release manifests.
 * Room swap: remove dosa and freedom; navigator is the 항해사방.
 */
(function(root){
 "use strict";
 const ROOMS=Object.freeze(["captain","navigator","cross","diversify"]);
 const LABELS=Object.freeze({captain:"선장 원문",navigator:"항해사방",cross:"교차 분석",diversify:"다양화"});
 const validSet=a=>Array.isArray(a)&&a.length===6&&new Set(a).size===6&&a.every(n=>Number.isInteger(n)&&n>=1&&n<=45);
 const validFive=a=>Array.isArray(a)&&a.length===5&&a.every(validSet)&&new Set(a.map(s=>[...s].sort((x,y)=>x-y).join(",")).size===5;
 const validRaw=a=>Array.isArray(a)&&a.length===6&&a.every(s=>typeof s==="string"&&s.trim().length>0&&s.length<=4000);
 function install({implementations,attestation}={}){
  if(!implementations||!attestation)throw Error("ENGINE_BINDINGS_REQUIRED");
  async function analyze({round,raw}={}){
   if(!Number.isInteger(round)||round<1||round>9999)throw Error("INVALID_ROUND");
   if(!validRaw(raw))throw Error("SIX_RAW_REQUIRED");
   const results={}; const all=[];
   for(const room of ROOMS){
    const engine=implementations[room],proof=attestation[room];
    if(typeof engine!=="function"||proof?.verified!==true||typeof proof.digest!=="string"||!proof.digest)throw Error("ORIGINAL_ENGINE_NOT_VERIFIED:"+room);
    const output=await engine({round,raw:[...raw]});
    if(!output||output.round!==round||output.sourceDigest!==proof.digest||!validFive(output.sets))throw Error("ENGINE_RESPONSE_INVALID:"+room);
    const sets=output.sets.map(s=>Object.freeze([...s]));
    results[room]=Object.freeze({room,label:LABELS[room],round,sets:Object.freeze(sets),engineDigest:proof.digest});
    for(const set of sets)all.push({room,combination:[...set]});
   }
   if(all.length!==20)throw Error("TWENTY_SETS_REQUIRED");
   // Do not silently invent, repair, or silently replace any combination.
   return Object.freeze({round,rooms:ROOMS,setsByRoom:Object.freeze(results),totalCombinations:20,
    officialEligible:false,mode:"STAGING_VERIFICATION_ONLY",recordsWritten:false});
  }
  return Object.freeze({analyze});
 }
 root.V13FourRoomTwenty=Object.freeze({install,rooms:ROOMS,labels:LABELS,validFive});
})(globalThis);
