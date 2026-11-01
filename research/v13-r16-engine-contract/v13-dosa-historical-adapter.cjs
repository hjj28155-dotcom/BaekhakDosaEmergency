/* R16: historical DOSA 1..100 only. No official BEFORE and no persistent write. */
'use strict';
const VALID_ENGINE_SHA256='90f6295b756968aa81f8f8e642113d74812c247863ff289f41773561e317c72e';
const SOURCE_ID='DOSA_V2_4_RC_ORIGINAL_HISTORICAL_1_100';
const canonical=(a)=>a.slice().sort((x,y)=>x-y).join(',');
const validSix=(a)=>Array.isArray(a)&&a.length===6&&new Set(a).size===6&&a.every(n=>Number.isInteger(n)&&n>=1&&n<=45);
function createHistoricalDosa(engine, {sourceSha256, officialLockStatus}={}) {
  if(sourceSha256!==VALID_ENGINE_SHA256)throw Error('DOSA_ORIGINAL_ASSET_HASH_MISMATCH');
  if(officialLockStatus!=='DEVELOPMENT_BASELINE_NOT_50PCT_PASSED')throw Error('DOSA_LOCK_CONTRACT_UNVERIFIED');
  if(engine?.name!=='DOSA V2.4-RC'||engine?.scope!=='1-100 validation only'||engine?.gate?.allow101Plus!==false||typeof engine.generateFinal5!=='function')throw Error('DOSA_UNEXPECTED_ENGINE');
  return function analyze({round,raw,rawFingerprint,phase}={}){
    if(!Number.isInteger(round)||round<1||round>100)throw Error('DOSA_SCOPE_1_TO_100_ONLY');
    if(phase!=='HISTORICAL_RESEARCH')throw Error('DOSA_NOT_AN_OFFICIAL_BEFORE_ENGINE');
    if(!Array.isArray(raw)||raw.length!==6||raw.some(v=>typeof v!=='string'||!v.trim()))throw Error('DOSA_RAW6_REQUIRED');
    if(typeof rawFingerprint!=='string'||!/^[a-f0-9]{64}$/i.test(rawFingerprint))throw Error('DOSA_RAW_FINGERPRINT_REQUIRED');
    const sets=engine.generateFinal5(raw);
    if(!Array.isArray(sets)||sets.length!==5||!sets.every(validSix)||new Set(sets.map(canonical)).size!==5)throw Error('DOSA_INVALID_FINAL5');
    return Object.freeze({schema:'V13_HISTORICAL_ENGINE_PREVIEW_V1',roomKey:'dosa',round,phase:'HISTORICAL_RESEARCH',sourceId:SOURCE_ID,
      rawFingerprint,combinations:sets.map(x=>Object.freeze([...x])),researchOnly:true,officialEligible:false,officialLocked:false,scoreEligible:false,persistPermission:false});
  };
}
module.exports={createHistoricalDosa,SOURCE_ID,VALID_ENGINE_SHA256};
