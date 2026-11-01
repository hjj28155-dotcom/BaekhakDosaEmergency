/* V13 R8.29 engine runtime integration — isolated, not production enabled.
 * Never replace the freedom room engine; do not write DB or lock records here.
 * The caller MUST bind the original V12 Captain V10 engine functions.
 */
(function(root){
  "use strict";
  const OWN={captain:"captain",dosa:"dosa",navigator:"navigator"};
  const validSet=a=>Array.isArray(a)&&a.length===6&&a.every(n=>Number.isInteger(n)&&n>=1&&n<=45)&&new Set(a).size===6;
  const validFive=a=>Array.isArray(a)&&a.length===5&&a.every(validSet)&&new Set(a.map(s=>[...s].sort((x,y)=>x-y).join(","))).size===5;
  const validRaw=a=>Array.isArray(a)&&a.length===6&&a.every(s=>typeof s==="string"&&s.trim().length>0);
  function install({captainV10,dosaV10,navigatorV10,freedomExisting,attestation}={}){
    if(typeof freedomExisting!=="function")throw Error("FREEDOM_EXISTING_REQUIRED");
    const implementations={captain:captainV10,dosa:dosaV10,navigator:navigatorV10};
    const stamps=Object.freeze({...attestation});
    async function run(room,{round,raw}={}){
      if(!Number.isInteger(round)||round<1||round>9999)throw Error("INVALID_ROUND");
      if(!validRaw(raw))throw Error("SIX_RAW_REQUIRED");
      if(room==="freedom")return freedomExisting({round,raw});
      if(!Object.hasOwn(OWN,room))throw Error("INVALID_ROOM");
      if(typeof implementations[room]!=="function"||stamps[room]?.verified!==true||!stamps[room]?.digest)throw Error("V12_V10_ORIGINAL_NOT_BOUND");
      const out=await implementations[room]({round,raw});
      if(!out||out.round!==round||out.sourceDigest!==stamps[room].digest||!validFive(out.sets))throw Error("ORIGINAL_ENGINE_RESPONSE_INVALID");
      return Object.freeze({room,round,sets:out.sets.map(a=>Object.freeze([...a])),engineDigest:stamps[room].digest,verified:true});
    }
    return Object.freeze({run});
  }
  root.V13ThreeRoomEngine=Object.freeze({install});
})(globalThis);
