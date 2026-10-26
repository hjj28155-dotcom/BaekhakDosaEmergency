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
console.log('PASS: DB-1 six-line fallback, strict 1247 check, current auto20 route, historical 20-combo research isolation, locked purchase preservation');
