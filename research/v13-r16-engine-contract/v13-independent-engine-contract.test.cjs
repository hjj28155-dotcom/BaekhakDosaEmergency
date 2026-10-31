const test=require('node:test'),assert=require('node:assert/strict');
const {ROOM_COUNTS,inspectReport,inspectThirty}=require('./v13-independent-engine-contract.cjs');
const round=1250,rawFingerprint='TEST-SIX-RAW-SHA';
function six(seed){return Array.from({length:6},(_,i)=>(seed+i-1)%45+1)}
function make(room,offset){const sets=Array.from({length:ROOM_COUNTS[room]},(_,i)=>six(offset+i*6));return {schema:'V13_PREDRAW_ROOM_REPORT_V1',phase:'BEFORE',round,roomKey:room,rawFingerprint,sourceId:'UNVERIFIED_TEST_ONLY',combinations:sets,groups:room==='freedom'?['RAW5','AUTO_A5','AUTO_B5'].map((id,i)=>({id,combinations:sets.slice(i*5,i*5+5)})):undefined}}
const c=make('captain',1),d=make('dosa',3),f=make('freedom',5),n=make('navigator',7);
test('5+5+15+5',()=>assert.deepEqual(Object.values(ROOM_COUNTS),[5,5,15,5]));
test('freedom 15 is format-only',()=>{const x=inspectReport(f,{round,rawFingerprint});assert.equal(x.ok,true);assert.equal(x.official_eligible,false)});
test('freedom 5 rejected',()=>{const x=make('freedom',9);x.combinations=x.combinations.slice(0,5);assert.equal(inspectReport(x,{round,rawFingerprint}).ok,false)});
test('freedom group order rejected',()=>{const x=make('freedom',9);[x.groups[0],x.groups[1]]=[x.groups[1],x.groups[0]];assert.equal(inspectReport(x,{round,rawFingerprint}).reason,'FREEDOM_GROUP_MISMATCH')});
test('wrong round rejected',()=>assert.equal(inspectReport(c,{round:1249,rawFingerprint}).reason,'WRONG_PHASE_OR_ROUND'));
test('wrong fingerprint rejected',()=>assert.equal(inspectReport(c,{round,rawFingerprint:'wrong'}).reason,'RAW_FINGERPRINT_MISMATCH'));
test('after result contamination rejected',()=>{const x=make('captain',8);x.bonus=2;assert.equal(inspectReport(x,{round,rawFingerprint}).reason,'RESULT_AFTER_CONTAMINATION')});
test('cross room duplicates rejected',()=>assert.equal(inspectThirty([c,d,f,n],{round,rawFingerprint}).reason,'CROSS_ROOM_DUPLICATE'));
test('missing room rejected',()=>assert.equal(inspectThirty([c,d,f],{round,rawFingerprint}).reason,'FOUR_ROOMS_REQUIRED'));
test('30 distinct sets remain unverified',()=>{
 let i=0;const reports=['captain','dosa','freedom','navigator'].map(room=>{
 const r=make(room,1);r.combinations=Array.from({length:ROOM_COUNTS[room]},()=>six(++i));
 if(room==='freedom')r.groups=['RAW5','AUTO_A5','AUTO_B5'].map((id,j)=>({id,combinations:r.combinations.slice(j*5,j*5+5)}));
 return r;});
 const x=inspectThirty(reports,{round,rawFingerprint});assert.equal(x.ok,true);assert.equal(x.count,30);assert.equal(x.official_eligible,false);
});
