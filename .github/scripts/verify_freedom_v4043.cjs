const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const p='build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/';
const html=fs.readFileSync(p+'index.html','utf8');
const patch=fs.readFileSync(p+'db1_v1247_patch.js','utf8');
const script=(html.match(/<script>\s*const APP_BASE_VERSION[\s\S]*?<\/script>/i)||[])[0];
assert(script,'app main JS exists');
new vm.Script(script.replace(/^<script>/i,'').replace(/<\/script>$/i,''));
assert(patch.includes("meta.count=1247"),'DB-1 update requires 1247 verified records');
assert(html.includes('let raw=manual||db1'),'manual text may be absent; DB-1 fallback must drive the engine');
assert(html.includes("status.status!=='PASS'"),'refuse incomplete DB-1');
assert(html.includes("V330_AFTER='purchase';runAnalysisFromHome()"),'current 1247 auto analysis route');
assert(html.includes("a.historicalResultExcluded=true"),'past research cannot masquerade as first-locked historical result');
assert(html.includes('R().analysisResearch=a'),'separate historical research data');
assert(html.includes("if(!b.locked)"),'official purchased combinations remain protected if locked');
const extract=(start,end)=>{const a=html.indexOf(start),b=html.indexOf(end,a+start.length);assert(a>=0&&b>a,start);return html.slice(a,b)};
const db=extract('function v4043Db1Text(round){','let V4043_ANALYSIS_INPUT=null;');
const bank={'1247':{text:'첫째\n둘째\n셋째\n넷째\n다섯째\n여섯째'}};
const ctx={window:{V3_RAW_BANK:bank,DB1_UPDATE_1247:{status:'PASS'},V3_RAW_BANK_META:{count:1247}}};
vm.createContext(ctx);vm.runInContext(db,ctx);
assert.equal(ctx.v4043Db1Text(1247),bank[1247].text,'embedded DB-1 alone supplies six lines');
ctx.window.DB1_UPDATE_1247={status:'HOLD'};
assert.equal(ctx.v4043Db1Text(1247),'','invalid DB-1 rejected');
ctx.window.DB1_UPDATE_1247={status:'PASS'};
assert.equal(ctx.v4043Db1Text(1246),'','missing record is not fabricated');
const main=extract('function storeAnalysisResult(m,pool,original,fallback,error){','function candidateBalls(nums)');
const twenty=Array.from({length:20},(_,i)=>[1+i%20,21,22,23,24,25]);
const bought={buy:{locked:true,combos:['1 2 3 4 5 6'],lockedAt:'BEFORE'}};
let rec=bought,researchPages=0;
const context={
 R(){return rec},cur(){return '1246'},cleanGames(){return []},v123Auto20FromUniverse(){return twenty},gamesToRows(){return []},
 V330_WIN:{1246:{nums:[1,2,3,4,5,6]}},V330_AFTER:'research',V123_AFTER_ANALYSIS:'',
 V4043_ANALYSIS_INPUT:{source:'내장 DB-1 1~1247'},save(){},v4043HistoricalResearchPage(){researchPages++},
 console,Date,modal(){throw Error('no modal expected')}
};
vm.createContext(context);vm.runInContext(main,context);
context.storeAnalysisResult({engine:'original engine',rank:[]},[1,2,3,4,5,6,7,8,9,10],[],false,'');
assert.equal(researchPages,1,'show research output');
assert.equal(rec.analysisResearch.auto20.length,20,'20 generated combos available');
assert.equal(rec.analysis,undefined,'historic original analysis must remain absent');
assert.deepEqual(rec.buy,{locked:true,combos:['1 2 3 4 5 6'],lockedAt:'BEFORE'},'past locked tickets not rewritten');
assert.equal(rec.analysisResearch.researchOnly,true);

// Integration regression: current six sentences + three historical DB-1 texts each
// travel through the original V3_ANALYZE engine protocol before rank merging.
const historyHelpers=extract('function v4043Db1Text(round){','function runAnalysisFromHome(){');
const lines=v=>[v+'사과','둘째하늘','셋째바다','넷째강물','다섯째별','여섯째바람'].join('\n');
const historyBank={
 '1247':{text:lines('현재')},
 '1246':{text:lines('지난')},
 '1245':{text:lines('그전')},
 '620':{text:lines('중간')},
 '610':{text:lines('중간대')},
 '400':{text:lines('옛날')}
};
const posted=[],modals=[];let combined=null;
const hctx={
 window:{V3_RAW_BANK:historyBank,DB1_UPDATE_1247:{status:'PASS'},V3_RAW_BANK_META:{count:1247}},
 document:{getElementById(id){assert.equal(id,'engineFrame');return {contentWindow:{postMessage(payload){posted.push(payload)}}}}},
 modal(title,msg){modals.push(title)},
 setTimeout(fn){fn()},handleEngineResult(m){combined=m},console
};
vm.createContext(hctx);vm.runInContext(historyHelpers,hctx);
vm.runInContext("v4043BeginAnalysis(1247,window.V3_RAW_BANK['1247'].text)",hctx);
assert.equal(posted[0].round,1247);
assert.equal(posted[0].raw,historyBank['1247'].text);
let processed=0;
while(processed<posted.length&&processed<8){
 const call=posted[processed++];
 const first=call.round===1247?[1,2,3,4,5,6,7,8,9,10]:[3,4,5,6,7,8,9,10,11,12];
 hctx.v4043AcceptEngineResult({ok:true,round:call.round,rank:first.map(n=>({n}))});
}
assert.equal(processed,4,'three historical calls plus current call');
assert(combined&&combined.db1Evidence,'combined engine response must include provenance');
assert.equal(combined.db1Evidence.historySearched,5);
assert.equal(combined.db1Evidence.historyEngineAnalysed.length,3);
assert.equal(combined.db1Evidence.comparedWithoutWinningNumbers,true);
assert.deepEqual(Array.from(combined.rank.slice(0,6),x=>Number(x.n)),[1,2,3,4,5,6],
 'current raw has first-six candidate priority');
assert.equal(modals.length,0,'successful co-analysis must not display a false error');

// Real effect regression: divergent historical DB-1 engine ranks must
// contribute an actual purchased candidate, not only a provenance label.
const primaryRanks=Array.from({length:10},(_,i)=>({n:i+1,score:10-i}));
const historicalRanks=[11,12,13,14,15,16,17,18,19,20].map((n,i)=>({n,score:10-i}));
const enriched=hctx.v4043MergedRanks(primaryRanks,[historicalRanks,historicalRanks,historicalRanks]);
assert.deepEqual(Array.from(enriched.slice(0,6),x=>x.n),[1,2,3,4,5,6],'six current leaders may not be displaced');
assert(enriched.slice(6).some(x=>x.n>10),'distinct historical DB-1 scores must occupy research tail');
assert(enriched.filter(x=>x.source==='DB1_HISTORY_CANDIDATE').length>=1,'source must identify history-only candidates');
const autoCode=extract('function v123Seeded(seedStr){','function v123Seed1243Result(){');
vm.runInContext(autoCode,hctx);
const baseGames=hctx.v123Auto20FromUniverse(primaryRanks.map(x=>x.n),1247,20);
const combinedGames=hctx.v123Auto20FromUniverse(enriched.map(x=>x.n),1247,20);
assert.equal(combinedGames.length,20);
assert.notDeepEqual(combinedGames.map(x=>x.join(',')),baseGames.map(x=>x.join(',')),
 'different historical DB-1 evidence must actually change the auto-20 purchase candidates');



// User-supplied current original takes precedence, but the validated DB-1 remains required.
const launchAnalysis=extract('function runAnalysisFromHome(){','function runAnalysisFromRaw(){');
const sends=[];
const roundData={raw:{text:'사용자 제공 현재 회차 원문'}};
const bctx={
 window:{V3_RAW_BANK:historyBank,DB1_UPDATE_1247:{status:'PASS'},V3_RAW_BANK_META:{count:1247}},
 R(){return roundData},cur(){return '1247'},V330_WIN:{},modal(){throw Error('valid DB-1 must not show error')},
 sendAnalysisToEngine(round,text){sends.push({round,text})},setTimeout(){},console
};
vm.createContext(bctx);vm.runInContext(historyHelpers+launchAnalysis,bctx);
bctx.runAnalysisFromHome();
assert.equal(sends[0].text,roundData.raw.text,'current real original must be primary');
assert.equal(vm.runInContext('V4043_ANALYSIS_INPUT.source',bctx),'사용자 저장 원문');
roundData.raw.text='';
bctx.runAnalysisFromHome();
assert.equal(sends[1].text,historyBank['1247'].text,'no current original must use 1247 DB-1');
assert.equal(vm.runInContext('V4043_ANALYSIS_INPUT.source',bctx),'내장 DB-1 1~1247');

console.log('PASS: DB-1 six-line fallback, strict 1247 check, current auto20 route, historical 20-combo research isolation, locked purchase preservation');
