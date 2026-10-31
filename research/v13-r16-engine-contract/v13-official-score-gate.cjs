'use strict';
// V13 R16 stage 2 — RESEARCH ONLY. External trusted BEFORE lock and official draw
// verifier callbacks are REQUIRED. Local records alone never certify results.
const {createHash}=require('node:crypto');
const {inspectThirty}=require('./v13-independent-engine-contract.cjs');
const digest=x=>createHash('sha256').update(JSON.stringify(x)).digest('hex');
const validSix=x=>Array.isArray(x)&&x.length===6&&new Set(x).size===6&&x.every(n=>Number.isInteger(n)&&n>=1&&n<=45);
const stop=reason=>({ready:false,reason,official_eligible:false});
function scoreVerified({reports,round,rawFingerprint,draw,cutoffMs,verifyBeforeLock,verifyOfficialDraw}={}){
 const check=inspectThirty(reports,{round,rawFingerprint});
 if(!check.ok)return stop('INVALID_BEFORE_REPORTS:'+check.reason);
 if(!Number.isInteger(round)||!Number.isFinite(cutoffMs)||cutoffMs<=0)return stop('INVALID_TARGET_TIME');
 if(typeof verifyBeforeLock!=='function'||typeof verifyOfficialDraw!=='function')return stop('TRUSTED_VERIFIER_NOT_CONNECTED');
 if(!draw||!validSix(draw.winning)||!Number.isInteger(draw.bonus)||draw.bonus<1||draw.bonus>45||draw.winning.includes(draw.bonus))return stop('INVALID_DRAW');
 const drawHash=digest({round,winning:draw.winning.slice().sort((a,b)=>a-b),bonus:draw.bonus});
 const proofDraw=verifyOfficialDraw({round,drawHash});
 if(!proofDraw||proofDraw.verified!==true||proofDraw.drawHash!==drawHash)return stop('OFFICIAL_DRAW_NOT_ATTESTED');
 for(const r of reports){
  if(r.officialLocked===true||r.resultAfter||r.grade||r.winningNumbers||r.bonus)return stop('BEFORE_CONTAMINATION');
  const reportHash=digest({round,roomKey:r.roomKey,rawFingerprint,combinations:r.combinations.map(x=>x.slice().sort((a,b)=>a-b))});
  const proof=verifyBeforeLock({round,roomKey:r.roomKey,reportHash,cutoffMs});
  if(!proof||proof.verified!==true||proof.reportHash!==reportHash||!Number.isFinite(proof.lockedAtMs)||proof.lockedAtMs>=cutoffMs||proof.lockedAtMs<=0)return stop('BEFORE_LOCK_NOT_ATTESTED:'+r.roomKey);
 }
 const set=new Set(draw.winning);
 const rows=reports.flatMap(r=>r.combinations.map(numbers=>{
  const matches=numbers.filter(n=>set.has(n)).length;
  const grade=matches===6?1:matches===5&&numbers.includes(draw.bonus)?2:matches===5?3:matches===4?4:matches===3?5:0;
  return {roomKey:r.roomKey,numbers:numbers.slice(),matches,grade};
 }));
 const winners=rows.filter(x=>x.grade>0);
 return {ready:true,official_eligible:true,round,total:rows.length,rows,winners,best:winners.length?Math.min(...winners.map(x=>x.grade)):0,drawHash,warning:'Trusted callbacks must be independently implemented and verified before any production use.'};
}
module.exports={scoreVerified};
