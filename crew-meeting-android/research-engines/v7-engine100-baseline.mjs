// V7 independent research engine E100-R1. No official DB write, AI-provider inference, or win-rate claim.
// Original V3/DOSA source validation must be completed before any official integration.
export const ENGINE_VERSION='V7_ENGINE_E100_R1_RESEARCH_20261025';
export const ROOM_MAP=Object.freeze({ai1:'analysis',ai2:'helmsman',ai3:'freedom',ai4:'chief',ai5:'extra_ai5_room'});
export const ENGINE_PROFILES=Object.freeze({
 ai1:{role:'선장 원문 해석',v3:0.85,dosa:0.15,salt:'CAPTAIN'},
 ai2:{role:'도사 해독 근거',v3:0.15,dosa:0.85,salt:'DOSA'},
 ai3:{role:'독립 교차 분석',v3:0.50,dosa:0.50,salt:'CROSS'},
 ai4:{role:'코드·데이터 검증',v3:0.65,dosa:0.35,salt:'INTEGRITY'},
 ai5:{role:'조합·보고서 점검',v3:0.35,dosa:0.65,salt:'OPERATIONS'},
 central:{role:'V7 중앙 결합 연구',v3:0.50,dosa:0.50,salt:'CENTRAL'}
});
const fail=(id)=>{throw Error(id)};
export function validSet(a){return Array.isArray(a)&&a.length===6&&new Set(a).size===6&&a.every(n=>Number.isInteger(n)&&n>=1&&n<=45)}
export function verifyFive(sets){return Array.isArray(sets)&&sets.length===5&&sets.every(validSet)&&new Set(sets.map(a=>[...a].sort((x,y)=>x-y).join(','))).size===5;}
export function drawMillis(round){if(!Number.isInteger(round)||round<1||round>9999)fail('INVALID_ROUND');return Date.UTC(2002,11,7,11,35)+(round-1)*604800000}
export function blockOf(round){drawMillis(round);const start=Math.floor((round-1)/100)*100+1;return {start,end:start+99,sequence:Math.floor((round-1)/100)+1};}
export function validatedRaw(raw){
 if(!Array.isArray(raw)||raw.length!==6||raw.some(s=>typeof s!=='string'||!s.trim()||s.length>4000))fail('SIX_RAW_SENTENCES_REQUIRED');
 if(raw.some(s=>/(?:당첨\s*번호|보너스\s*번호|이번\s*회차\s*정답|winning.?numbers?|answer\s*[:=])/i.test(s)))fail('TARGET_RESULT_LEAK');
 return raw.map(s=>s.replace(/\r/g,'').trim());
}
function seedOf(input){let x=2166136261;for(let i=0;i<input.length;i++){x^=input.charCodeAt(i);x=Math.imul(x,16777619)}return x>>>0;}
function rng(seed){let state=seed||0x9e3779b9;return ()=>{state^=state<<13;state^=state>>>17;state^=state<<5;return (state>>>0)/4294967296;};}
function shuffle(items,seed){let a=[...items],random=rng(seed);for(let i=a.length-1;i>0;i--){let j=Math.floor(random()*(i+1));[a[i],a[j]]=[a[j],a[i]]}return a;}
export function sanitizeEvidence(e,{targetRound,cutoffMs,verifiedSourceDigests=[]}={}){
 if(!e)return {usable:false,reason:'MISSING_ENGINE_EVIDENCE',scores:new Map()};
 if(!Array.isArray(e.ranked)||e.ranked.length<6)return {usable:false,reason:'INVALID_ENGINE_RANK',scores:new Map()};
 if(!Number.isFinite(cutoffMs)||!Number.isInteger(targetRound))fail('MISSING_CUTOFF');
 if(!Number.isFinite(e.archivedAt)||e.archivedAt>=cutoffMs)return {usable:false,reason:'NOT_ARCHIVED_BEFORE_TARGET',scores:new Map()};
 if(e.sourceRound!=null&&(!Number.isInteger(e.sourceRound)||e.sourceRound>=targetRound))return {usable:false,reason:'FUTURE_OR_TARGET_SOURCE',scores:new Map()};
 if(typeof e.sourceDigest!=='string'||!verifiedSourceDigests.includes(e.sourceDigest))return {usable:false,reason:'UNVERIFIED_SOURCE',scores:new Map()};
 const values=new Map();
 for(const x of e.ranked){let n=typeof x==='number'?x:x?.n,score=typeof x==='number'?e.ranked.length-e.ranked.indexOf(x):x?.score;if(!Number.isInteger(n)||n<1||n>45||!Number.isFinite(score)||score<0)return {usable:false,reason:'INVALID_ENGINE_RANK',scores:new Map()};if(!values.has(n))values.set(n,score);}
 if(values.size<6)return {usable:false,reason:'INSUFFICIENT_ENGINE_RANK',scores:new Map()};
 const maximum=Math.max(...values.values());if(maximum<=0)return {usable:false,reason:'EMPTY_ENGINE_SCORES',scores:new Map()};
 return {usable:true,reason:'VERIFIED_INPUT_SOURCE',scores:new Map([...values].map(([n,s])=>[n,s/maximum]))};
}
function composeScores(a,b,profile,round,raw){
 const seed=seedOf(ENGINE_VERSION+'|'+profile.salt+'|'+round+'|'+raw.join('|')),rand=rng(seed);const weighted=[];
 for(let n=1;n<=45;n++){const random=rand();const v=a.usable?(a.scores.get(n)||0):0,d=b.usable?(b.scores.get(n)||0):0;
 const evidence=a.usable||b.usable;let score=evidence?(v*profile.v3+d*profile.dosa)+random*0.001:random;
 weighted.push({n,score,tie:random});}
 return weighted.sort((x,y)=>y.score-x.score||x.n-y.n).map(x=>x.n);
}
function selectFive(order,raw,round,profile){
 const top=order.slice(0,30);if(new Set(top).size!==30)fail('RANK_NOT_UNIQUE');
 const mixed=shuffle(top,seedOf('sets|'+ENGINE_VERSION+'|'+profile.salt+'|'+round+'|'+raw.join('|')));
 const sets=Array.from({length:5},(_,i)=>mixed.slice(i*6,i*6+6).sort((a,b)=>a-b));
 if(!verifyFive(sets))fail('FIVE_SET_POSTCONDITION_FAILED');return sets;
}
export function analyzeAll({round,raw,v3,dosa,verifiedSourceDigests=[],asOfMs}={}){
 const lines=validatedRaw(raw),drawCutoff=drawMillis(round);
 if(!Number.isFinite(asOfMs))fail('ANALYSIS_TIMESTAMP_REQUIRED');
 const cutoff=Math.min(drawCutoff,asOfMs);
 const srcA=sanitizeEvidence(v3,{targetRound:round,cutoffMs:cutoff,verifiedSourceDigests});
 const srcB=sanitizeEvidence(dosa,{targetRound:round,cutoffMs:cutoff,verifiedSourceDigests});
 const isPreDraw=asOfMs<drawCutoff;
 const setsByRoom={};
 for(const [room,profile] of Object.entries(ENGINE_PROFILES)){
  let ranking=composeScores(srcA,srcB,profile,round,lines),sets=selectFive(ranking,lines,round,profile);
  setsByRoom[room]={room,storage_room:ROOM_MAP[room]||'captain',role:profile.role,sets,unique_numbers:new Set(sets.flat()).size,
   classification:srcA.usable&&srcB.usable?'BOTH_SOURCES_VERIFIED_RESEARCH':'UNVERIFIED_OR_PARTIAL_FALLBACK',
   official_eligible:false,source_status:{v3:srcA.reason,dosa:srcB.reason},candidates:ranking.slice(0,30)};
 }
 return {ok:true,version:ENGINE_VERSION,mode:'RESEARCH_ONLY',round,block:blockOf(round),as_of:asOfMs,pre_draw_claim_untrusted:isPreDraw,
  official_eligible:false,winning_numbers_used:false,bonus_used:false,raw_line_count:6,setsByRoom};
}
