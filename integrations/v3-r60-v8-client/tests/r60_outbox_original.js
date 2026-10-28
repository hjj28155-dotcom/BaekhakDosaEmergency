/* V3 R30: Four rooms x five, new namespace from round one, central handoff LAB.
 * Does not delete, overwrite or read the old V3 localStorage records.
 * Does not automatically send to Supabase/Drive; authenticated receiver required.
 * R30 is a source-only staging module; do not claim Android deployment.
 */
(function(root){'use strict';
const EPOCH='V3_RESTART_R53_20261028';
const SCHEMA='V3_R30_FOUR_ROOM_20_COMBINATIONS_V1';
const DB_NAME='shipmate_v3_r53_fresh20_epoch1';
const DB_VERSION=1;
const ROOM_IDS=Object.freeze(['DOSA_RAW5','NAVIGATOR_RAW5','AUTO1_CAPTAIN_RAW5','AUTO2_INDEPENDENT5']);
const SOURCE_TYPES=Object.freeze(['DOSA_ENGINE','NAVIGATOR_DIRECT_LOCKED','NAVIGATOR_APP_RESEARCH','CAPTAIN_REPLACEMENT_RAW_ANALYSIS','CAPTAIN_MASTER_V2_U10_LOCAL_VERIFIED','CAPTAIN_MASTER_V2_R46_EVIDENCE_PROMOTION_LOCAL','CAPTAIN_MASTER_V2_R59_DERIVED_RESEARCH','INDEPENDENT_AUTO']);
const TARGET=Object.freeze({server:'V7_CENTRAL_CONTROL',room_key:'extra_navigator',folder_key:'result_before',display:'항해사방/결과전'});
const goodHash=s=>typeof s==='string'&&/^[0-9a-f]{64}$/.test(s);
const plain=x=>x!==null&&typeof x==='object'&&!Array.isArray(x);
const reject=reason=>({ok:false,reason,official:false});
const roundOk=r=>Number.isInteger(r)&&r>=1&&r<=9999;
const safeString=s=>typeof s==='string'&&s.length>0&&s.length<=160;
function validate(snapshot){
 if(!plain(snapshot)||snapshot.schema!==SCHEMA||snapshot.epoch!==EPOCH||!roundOk(snapshot.round))return reject('SCHEMA_EPOCH_ROUND');
 if(!Array.isArray(snapshot.raw6)||snapshot.raw6.length!==6||snapshot.raw6.some(s=>typeof s!=='string'||s.trim().length<6||s.length>5000))return reject('RAW6_REQUIRED');
 if(!goodHash(snapshot.raw6Sha256)||!goodHash(snapshot.rulesSha256))return reject('SOURCE_HASH_REQUIRED');
 if(snapshot.winningNumbersUsed!==false||snapshot.bonusNumbersUsed!==false||snapshot.targetResultDerivedEvidenceUsed!==false||snapshot.targetResultKnownToGenerator!==false)return reject('TARGET_RESULT_MUST_BE_BLIND');
 if(!['HISTORICAL_BLIND_PRACTICE','PROSPECTIVE_PRE_DRAW'].includes(snapshot.analysisMode))return reject('ANALYSIS_MODE_REQUIRED');
 if(snapshot.analysisMode==='PROSPECTIVE_PRE_DRAW'&&snapshot.preDrawExternalLockCertified!==true)return reject('EXTERNAL_PRE_DRAW_LOCK_REQUIRED');
 if(snapshot.analysisMode==='HISTORICAL_BLIND_PRACTICE'&&snapshot.preDrawExternalLockCertified!==false)return reject('HISTORICAL_CANNOT_CLAIM_PRE_DRAW');
 if(!plain(snapshot.blindAudit)||snapshot.blindAudit.targetRound!==snapshot.round||snapshot.blindAudit.targetResultAndDescendantsExcluded!==true||snapshot.blindAudit.otherRoundSourcesAllowed!==true)return reject('BLIND_AUDIT_MISSING');
 if(!Array.isArray(snapshot.groups)||snapshot.groups.length!==4||snapshot.totalCombinations!==20)return reject('FOUR_GROUPS_20_REQUIRED');
 if(!safeString(snapshot.lockedAt)||!Number.isFinite(Date.parse(snapshot.lockedAt)))return reject('LOCK_TIME_REQUIRED');
 if(snapshot.winningNumbers!==undefined||snapshot.bonusNumber!==undefined||snapshot.hitCounts!==undefined||snapshot.grade!==undefined)return reject('RESULT_FIELD_FORBIDDEN');
 const seen=new Set(),engineHashes=[];
 for(let i=0;i<4;i++){
  const g=snapshot.groups[i],role=ROOM_IDS[i];
  if(!plain(g)||g.room!==role||!Array.isArray(g.games)||g.games.length!==5||!goodHash(g.engineSha256)||!goodHash(g.evidenceSha256))return reject('GROUP_SHAPE_OR_PROVENANCE');
  if(!safeString(g.classification)||!SOURCE_TYPES.includes(g.classification))return reject('CLASSIFICATION');
  if(i===0&&g.classification!=='DOSA_ENGINE')return reject('DOSA_ROLE_MISMATCH');
  if(i===1&&!['NAVIGATOR_DIRECT_LOCKED','NAVIGATOR_APP_RESEARCH'].includes(g.classification))return reject('NAVIGATOR_PROVENANCE_MISMATCH');
  if(i===1&&g.classification==='NAVIGATOR_DIRECT_LOCKED'&&(!goodHash(g.signedArtifactSha256)||g.humanDirectConfirmed!==true))return reject('NAVIGATOR_DIRECT_PROOF_REQUIRED');
  if(i===1&&g.classification==='NAVIGATOR_APP_RESEARCH'&&g.humanDirectConfirmed!==false)return reject('AUTOMATED_NOT_HUMAN');
  if(i===2&&!['CAPTAIN_REPLACEMENT_RAW_ANALYSIS','CAPTAIN_MASTER_V2_U10_LOCAL_VERIFIED','CAPTAIN_MASTER_V2_R46_EVIDENCE_PROMOTION_LOCAL','CAPTAIN_MASTER_V2_R59_DERIVED_RESEARCH'].includes(g.classification))return reject('AUTO1_MUST_ANALYZE_RAW6');
  if(i===2&&['CAPTAIN_MASTER_V2_U10_LOCAL_VERIFIED','CAPTAIN_MASTER_V2_R46_EVIDENCE_PROMOTION_LOCAL','CAPTAIN_MASTER_V2_R59_DERIVED_RESEARCH'].includes(g.classification)&&(g.captainPackGate!=='PASS'||g.localU10Validated!==true||!Array.isArray(g.captainRankedUniverse)||g.captainRankedUniverse.length<7||g.captainRankedUniverse.length>10))return reject('CAPTAIN_U10_PROOF_REQUIRED');
  if(i===2&&g.classification==='CAPTAIN_MASTER_V2_R46_EVIDENCE_PROMOTION_LOCAL'&&(g.captainEvidenceExtension!==true||!Array.isArray(g.captainSupplementNumbers)||(g.captainSupplementNumbers.length<1&&g.captainTrimmedNumbers.length<1)||(g.captainOriginalCandidateK>=7&&g.captainOriginalCandidateK<=10)))return reject('CAPTAIN_EVIDENCE_EXTENSION_PROOF_REQUIRED');
  if(i===2&&g.classification==='CAPTAIN_MASTER_V2_R59_DERIVED_RESEARCH'&&(!g.captainFallbackResearch||!plain(g.captainFallbackAudit)||g.captainFallbackAudit.applied!==true||!Array.isArray(g.captainFallbackAudit.added)||g.captainFallbackAudit.added.length<1||snapshot.analysisMode!=='HISTORICAL_BLIND_PRACTICE'))return reject('CAPTAIN_R59_RESEARCH_PROVENANCE_REQUIRED');
  if(i===3&&g.classification!=='INDEPENDENT_AUTO')return reject('AUTO2_MUST_BE_INDEPENDENT');
  engineHashes.push(g.engineSha256);
  for(const nums of g.games){
   if(!Array.isArray(nums)||nums.length!==6||nums.some(n=>!Number.isInteger(n)||n<1||n>45)||new Set(nums).size!==6)return reject('INVALID_SIX_NUMBER_GAME');
   const sig=nums.slice().sort((a,b)=>a-b).join('-');if(seen.has(sig))return reject('DUPLICATE_ACROSS_FOUR_ROOMS');seen.add(sig);
  }
 }
 if(engineHashes[2]===engineHashes[3])return reject('AUTO1_AUTO2_NOT_SEPARATE_ENGINES');
 return {ok:true,reason:'FOUR_ROOMS_20_BLIND_STRUCTURE_PASS',official:false};
}
function envelope(snapshot){
 const v=validate(snapshot);if(!v.ok)throw Error(v.reason);
 return {schema:'V7_V3_NAVIGATOR_20_RESULT_BEFORE_HANDOFF_V1',destination:TARGET,
  originApp:'V3_NAVIGATOR',epoch:EPOCH,round:snapshot.round,lockedAt:snapshot.lockedAt,
  analysisMode:snapshot.analysisMode,preDrawExternalLockCertified:snapshot.preDrawExternalLockCertified,
  raw6Sha256:snapshot.raw6Sha256,rulesSha256:snapshot.rulesSha256,
  blindAudit:snapshot.blindAudit,groups:snapshot.groups,totalCombinations:20,
  requiresServerAuthentication:true,requiresServerReceipt:true,submissionStatus:'PENDING_UNSENT'};
}
function openDB(idb){return new Promise((resolve,reject)=>{
 if(!idb||typeof idb.open!=='function')return reject(Error('INDEXEDDB_UNAVAILABLE'));
 let req;try{req=idb.open(DB_NAME,DB_VERSION)}catch(e){return reject(Error('DB_OPEN_EXCEPTION'))}
 req.onupgradeneeded=()=>{const db=req.result;
  for(const name of ['analyses','roundLocks','centralOutbox'])if(!db.objectStoreNames.contains(name))db.createObjectStore(name,{keyPath:'round'});
 };
 req.onsuccess=()=>resolve(req.result);req.onerror=()=>reject(Error('DB_OPEN_FAILED'));req.onblocked=()=>reject(Error('DB_OPEN_BLOCKED'));
});}
const close=db=>{try{db.close()}catch(_){}};
function transact(db,snapshot,out){return new Promise((resolve,reject)=>{
 let tx;
 try{tx=db.transaction(['analyses','roundLocks','centralOutbox'],'readwrite')}catch(e){return reject(Error('TRANSACTION_UNAVAILABLE'))}
 let ended=false;const fail=()=>{if(!ended){ended=true;reject(Error('ATOMIC_ROLLBACK_OR_DUPLICATE'))}};
 tx.onabort=fail;tx.onerror=()=>{};tx.oncomplete=()=>{if(!ended){ended=true;resolve({created:true,round:snapshot.round,centralStatus:'PENDING_UNSENT',epoch:EPOCH})}};
 const recordId=EPOCH+':'+snapshot.round+':'+snapshot.raw6Sha256.slice(0,16);
 try{
  tx.objectStore('analyses').add({round:snapshot.round,recordId,epoch:EPOCH,payload:snapshot});
  tx.objectStore('roundLocks').add({round:snapshot.round,recordId,epoch:EPOCH,locked:true});
  tx.objectStore('centralOutbox').add({round:snapshot.round,recordId,epoch:EPOCH,status:'PENDING_UNSENT',payload:out});
 }catch(_){try{tx.abort()}catch(__){}fail()}
});}
function read(db,round){return new Promise((resolve,reject)=>{
 let tx,a,b,c;
 try{tx=db.transaction(['analyses','roundLocks','centralOutbox'],'readonly');a=tx.objectStore('analyses').get(round);b=tx.objectStore('roundLocks').get(round);c=tx.objectStore('centralOutbox').get(round)}catch(_){return reject(Error('READ_TRANSACTION_FAILED'))}
 let first,lock,out;a.onsuccess=()=>{first=a.result};b.onsuccess=()=>{lock=b.result};c.onsuccess=()=>{out=c.result};
 tx.onabort=()=>reject(Error('READ_ABORTED'));
 tx.oncomplete=()=>{
  if(!first&&!lock&&!out)return resolve(null);
  if(!first||!lock||!out||first.recordId!==lock.recordId||first.recordId!==out.recordId||lock.locked!==true)return reject(Error('PARTIAL_RECORD_CORRUPTION'));
  resolve({round,locked:true,centralStatus:out.status,payload:first.payload,outbox:out.payload,epoch:EPOCH});
 };
});}
function laboratoryAdapter(indexedDB){return Object.freeze({scope:'NEW_EPOCH_LAB_NOT_PRODUCTION',epoch:EPOCH,database:DB_NAME,
 async create(snapshot){let db;const v=validate(snapshot);if(!v.ok)return {created:false,reason:v.reason};
  try{db=await openDB(indexedDB);const snap=JSON.parse(JSON.stringify(snapshot));return await transact(db,snap,envelope(snap));}
  catch(e){return {created:false,reason:String(e&&e.message||'DB_FAILED')}}finally{if(db)close(db)}},
 async read(round){if(!roundOk(round))return null;let db;try{db=await openDB(indexedDB);return await read(db,round)}catch(e){throw e}finally{if(db)close(db)}},
 /* The envelope is queued, not sent. No unauthenticated caller may claim ACK. */
 async pending(round){const r=await this.read(round);return r&&r.centralStatus==='PENDING_UNSENT'?r.outbox:null}
 });}
root.NAV_V3_R30_FRESH20=Object.freeze({version:'V3_R30_FOUR_ROOM_RESET_SYNC_CONTRACT_V1',epoch:EPOCH,database:DB_NAME,
 rooms:ROOM_IDS,destination:TARGET,validate,envelope,laboratoryAdapter,
 freshOperationalRound:1,oldDatabaseDeleted:false,oldDatabaseWritten:false,
 productionDataResetOnDevice:false,appHomeAltered:false,
 centralNetworkIntegrated:false,centralAuthenticatedReceiverTested:false,
 humanDirectSamplesRegistered:0,officialRegistrationEnabled:false,automaticTransferEnabled:false,
 previous25CombinationContractRetiredFromNewEpoch:true});
})(typeof window!=='undefined'?window:globalThis);
