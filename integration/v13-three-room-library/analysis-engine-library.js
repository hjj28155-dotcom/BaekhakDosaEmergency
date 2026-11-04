/* V13 app analysis-room engine library: safe staging registry.
 * Captain's latest instruction: only 선장 원문방 / 신서방 / 항해사방.
 * All originals and old reports remain untouched. Nothing here installs engines.
 */
(function(root){
  "use strict";
  const ROOM_KEYS=Object.freeze(["captain","sinseo","navigator"]);
  const LABELS=Object.freeze({captain:"선장 원문방",sinseo:"신서방",navigator:"항해사방"});
  const ENGINE_LIBRARY=Object.freeze({
    captain:Object.freeze({room:"captain",label:LABELS.captain,source:"V12 captain original (verification required)",status:"UNBOUND"}),
    sinseo:Object.freeze({room:"sinseo",label:LABELS.sinseo,source:"신서방 source not yet authenticated",status:"UNBOUND"}),
    navigator:Object.freeze({room:"navigator",label:LABELS.navigator,source:"V3 R33 / Navigator V1 research candidates (not authenticated official)",status:"UNBOUND"})
  });
  const validSet=a=>Array.isArray(a)&&a.length===6&&new Set(a).size===6&&a.every(n=>Number.isInteger(n)&&n>=1&&n<=45);
  const validFive=a=>Array.isArray(a)&&a.length===5&&a.every(validSet)&&new Set(a.map(s=>[...s].sort((a,b)=>a-b).join(","))).size===5;
  function bind(implementations,attestations){
    if(!implementations||!attestations)throw Error("ENGINE_LIBRARY_BINDINGS_REQUIRED");
    const functions=ROOM_KEYS.map(k=>implementations[k]);
    if(functions.every(f=>typeof f==="function")&&new Set(functions).size!==3)throw Error("SHARED_ENGINE_NOT_INDEPENDENT");
    async function analyze({round,raw}={}){
      if(!Number.isInteger(round)||round<1||round>9999)throw Error("INVALID_ROUND");
      if(!Array.isArray(raw)||raw.length!==6||raw.some(s=>typeof s!=="string"||!s.trim()||s.length>4000))throw Error("SIX_RAW_REQUIRED");
      const outputs={};
      for(const room of ROOM_KEYS){
        const engine=implementations[room],proof=attestations[room];
        if(typeof engine!=="function"||proof?.verified!==true||typeof proof.digest!=="string"||!proof.digest)throw Error("ENGINE_NOT_VERIFIED:"+room);
        const x=await engine({round,raw:[...raw]});
        if(!x||x.round!==round||x.sourceDigest!==proof.digest||!validFive(x.sets))throw Error("ENGINE_RESPONSE_INVALID:"+room);
        outputs[room]=Object.freeze({room,label:LABELS[room],sets:Object.freeze(x.sets.map(s=>Object.freeze([...s]))),sourceDigest:proof.digest});
      }
      // 3 authenticated five-set engines produce 15 sets. Captain's previous 20-set
      // target cannot be declared satisfied until an authorized allocation is known.
      return Object.freeze({round,rooms:ROOM_KEYS,setsByRoom:Object.freeze(outputs),verifiedSetCount:15,
        targetSetCount:20,targetAllocationResolved:false,officialEligible:false,
        mode:"STAGING_ONLY",recordsWritten:false});
    }
    return Object.freeze({analyze});
  }
  root.V13AnalysisEngineLibrary=Object.freeze({rooms:ROOM_KEYS,labels:LABELS,registry:ENGINE_LIBRARY,bind});
})(globalThis);
