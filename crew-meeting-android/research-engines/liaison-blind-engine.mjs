/* 중앙연락관 별도 신서 과거회차 연구 — R1. Official V7 writes: none.
 * The caller MUST validate archived evidence hashes against independently held
 * pre-target snapshots. This pure module never authenticates archival timestamps.
 */
import {analyzeAll,drawMillis,validatedRaw,verifyFive,sanitizeEvidence} from './crew-liaison-baseline-r1.mjs';
export const LIAISON_VERSION='V7_LIAISON_BLIND_R1_RESEARCH_20261026';
export const LIAISON_ROOM='extra_central_liaison';
export const FORBIDDEN_PATH=/(?:RESULT[-_ ]?AFTER|결과후|당첨번호|당첨[_ ]?결과|정답|WINNING|DRAW[_-]?RESULT)/i;
function reject(msg){throw new Error(msg)}
function hex64(s){return typeof s==='string'&&/^[a-f0-9]{64}$/i.test(s)}
function checkOne(label,ev,registry,round,asOfMs){
 if(!ev||!registry||!hex64(registry.digest))reject('VERIFIED_'+label+'_ARCHIVE_REQUIRED');
 if(registry.sourceKind!==label||registry.exactPath==null||FORBIDDEN_PATH.test(registry.exactPath))reject('INVALID_'+label+'_PROVENANCE');
 if(!hex64(ev.sourceDigest)||ev.sourceDigest!==registry.digest)reject('SOURCE_DIGEST_MISMATCH_'+label);
 if(!Number.isInteger(registry.archivedAt)||registry.archivedAt>=asOfMs||registry.archivedAt>=drawMillis(round))reject('ARCHIVE_NOT_BEFORE_TARGET_'+label);
 if(!Number.isInteger(registry.sourceRound)||registry.sourceRound>=round||registry.sourceRound<0)reject('NON_PRIOR_SOURCE_'+label);
 if(ev.sourceRound!==registry.sourceRound||ev.archivedAt!==registry.archivedAt)reject('UNVERIFIED_METADATA_'+label);
 if(registry.verifiedBy!=='INDEPENDENT_ARCHIVE')reject('UNATTESTED_ARCHIVE_'+label);
 const sanitized=sanitizeEvidence(ev,{targetRound:round,cutoffMs:asOfMs,verifiedSourceDigests:[registry.digest]});
 if(!sanitized.usable)reject('INVALID_EVIDENCE_'+label+'_'+sanitized.reason);
 return ev;
}
export function runLiaisonBlind({round,raw,v3,dosa,archiveRegistry,asOfMs}={}){
 if(!Number.isInteger(round)||round<2||round>9999)reject('INVALID_ROUND');
 if(!Number.isFinite(asOfMs)||asOfMs>=drawMillis(round))reject('PROSPECTIVE_ARCHIVE_CUTOFF_REQUIRED');
 const clues=validatedRaw(raw);
 if(clues.some(s=>FORBIDDEN_PATH.test(s)))reject('TARGET_RESULT_IN_CLUE');
 if(!archiveRegistry||Array.isArray(archiveRegistry))reject('MISSING_TRUSTED_ARCHIVE_REGISTRY');
 const a=checkOne('V3',v3,archiveRegistry.v3,round,asOfMs);
 const b=checkOne('DOSA',dosa,archiveRegistry.dosa,round,asOfMs);
 const result=analyzeAll({round,raw:clues,v3:a,dosa:b,
  verifiedSourceDigests:[archiveRegistry.v3.digest,archiveRegistry.dosa.digest],asOfMs});
 const candidate=result.setsByRoom.central;
 if(candidate.classification!=='BOTH_SOURCES_VERIFIED_RESEARCH'||!verifyFive(candidate.sets))reject('RESEARCH_POSTCONDITION_FAILED');
 return Object.freeze({version:LIAISON_VERSION,room:'liaison',storage_room:LIAISON_ROOM,
  mode:'RESEARCH_ONLY',official_eligible:false,registered:false,locked:false,
  round,as_of:asOfMs,pre_draw_attestation:'NOT_INDEPENDENTLY_ESTABLISHED',
  historical_test_grade:'NOT_YET_SCORED',sets:candidate.sets.map(s=>Object.freeze([...s])),
  source_digest_v3:archiveRegistry.v3.digest,source_digest_dosa:archiveRegistry.dosa.digest,
  distinct_numbers:new Set(candidate.sets.flat()).size});
}
export function previewReportStorage({room,category,filename}={}){
 if(typeof room!=='string'||typeof filename!=='string')reject('INVALID_REPORT_ROUTE');
 if(FORBIDDEN_PATH.test(filename)&&category==='결과전보고서')reject('AFTER_RESULT_CANNOT_BE_PRE');
 const allowed={captain:'선장방',extra_central_liaison:'중앙연락관방'};
 if(!(room in allowed))reject('RESEARCH_ROLE_NOT_ALLOWED');
 return Object.freeze({storage_room:room,display_room:allowed[room],private_room_only:true,
  public_aggregate_copy:false,official_write_allowed:false});
}
