// V4.0.46 emergency full-corpus, 1248 primary and result linking; no repository/original mutation.
const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const base='build405/FREEDOM_V402_BUILD_THIS/app/';
const html=fs.readFileSync(base+'src/freedom/assets/index.html','utf8');
const gradle=fs.readFileSync(base+'build.gradle','utf8');
assert.match(gradle,/applicationId 'com[.]navigator[.]freedom[.]v3final'/);
assert.match(gradle,/versionCode 40046/);
assert.match(gradle,/signingConfig signingConfigs[.]distribution/);
assert.match(html,/const V4046_VISIBLE=\[1244,1245,1246,1247,1248\]/);
assert.match(html,/function rawPage[(][)]\{return v4044ReviewRound[(]1248,true[)]\}/);
assert.match(html,/v4046PrimaryRaw[(]target[)]/);
assert.match(html,/function v4046WinningForRound/);
assert.match(html,/reference[.]scoreRaw[(]raw[)]/);
assert.match(html,/if[(]V4046_VISIBLE[.]includes[(]Number[(]cur[(][)][)][)][)]return v4044ReviewRound/);
assert.match(html,/id="v4044SmallScreenModalTitleFix"/);
assert.equal((html.match(/src="freedom_reference_v4043.js"/g)||[]).length,1);
const inline=[...html.matchAll(/<script([^>]*)>([\s\S]*?)<\/script>/gi)].filter(x=>!/\bsrc\s*=/.test(x[1]));
assert.equal(inline.length,6);inline.forEach((x,i)=>assert.doesNotThrow(()=>new Function(x[2]),'inline JS '+i));
function range(a,b){const i=html.indexOf(a),j=html.indexOf(b,i+a.length);assert.ok(i>=0&&j>i,'missing source anchors '+a);return html.slice(i,j)}
const gameFunctions=range('function v123Seeded(seedStr){','function v123Seed1243Result(){');
const groupFunctions=range('function v330GroupsResult(games,result,bonus){','function v330Print(){');
const newest=range('function v4044ValidSets(games,count){','function v330OpenRound(n){');
const corpus={calls:0};
const makeRaw=n=>Array.from({length:6},(_,i)=>'제'+n+'회 자료 '+(i+1)+'번째 원문').join('\n');
const notes=[],queue=[],saved={count:0};
let selection=1248;
const D={rounds:{'1247':{buy:{locked:true,combos:['1,2,3,4,5,6']},analysis:{source:'ORIGINAL_LOCKED'}}}};
const root={
 window:{DB1_UPDATE_1247:{status:'PASS'},V3_RAW_BANK_META:{count:1247},FREEDOM_REF_V4043:{
  scoreRaw(raw){corpus.calls++;const n=Number((raw.match(/제(\d+)회/)||[])[1])||1248;
   const scores={};for(let k=1;k<=45;k++)scores[k]=1+((k*13+n*7)%15);
   return {status:'OK',scores}}
 }},
 D,
 document:{getElementById(id){return id==='v4046Progress'?{set textContent(v){notes.push(v)}}:id==='v4046RawInput'?{value:makeRaw(1248)}:null}},
 V4046_VISIBLE:[1244,1245,1246,1247,1248],
 V330_RECENT:[1243,1244,1245,1246,1247,1248],
 V330_WIN:{1246:{nums:[16,22,23,24,27,28],bonus:6},1247:{nums:[8,14,21,22,42,45],bonus:24}},
 setRound(n){selection=n;return true},
 cur(){return String(selection)},
 R(){return D.rounds[String(selection)]||(D.rounds[String(selection)]={})},
 rawBank:()=>null,
 v4043Db1Text:n=>n>=1&&n<=1247?makeRaw(n):'',
 modal:(title,body)=>{notes.push({title,body})},
 esc:s=>String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;'),
 v4045MaybeRefreshWinning:()=>{},
 v4044RawCard:()=>'<div>raw unused by emergency UI</div>',
 v4044AutoRows:()=>'<div>locked original 5</div>',
 save:()=>{saved.count++},
 alert:msg=>{throw Error('unexpected alert '+msg)},
 setTimeout:fn=>{queue.push(fn)},
};
vm.createContext(root);
vm.runInContext(gameFunctions+'\n'+groupFunctions+'\n'+newest,root,{timeout:10000});
function drain(){let limit=500;while(queue.length&&limit-->0)queue.shift()();assert.ok(limit>0,'queued task loop must finish')}
function copyData(x){return JSON.parse(JSON.stringify(x))}
const lockedBefore=copyData(D.rounds['1247']);
assert.equal(root.v4046WinningForRound(1248,{}),null,'unannounced 1248 must not have fabricated draw result');
root.v4045AutoAnalyze20(1248,false);drain();
let draft=D.rounds['1248'].analysis;
assert.equal(draft.corpusRoundCount,1247);
assert.equal(draft.corpusLastRound,1247);
assert.equal(draft.primaryWeight,0,'absent 1248 raw must use whole 1-1247 history');
assert.equal(draft.historyWeight,1);
assert.equal(draft.winningNumbersUsed,false);
assert.equal(draft.auto20.length,20);
assert.equal(draft.groups.length,4);
assert.ok(draft.groups.every(g=>g.rows.length===5));
assert.equal(new Set(draft.auto20.map(x=>x.join(','))).size,20);
assert.equal(corpus.calls,1247,'all 1247 historical raws analyzed once');
assert.deepEqual(copyData(D.rounds['1247']),lockedBefore,'locked 1247 original unchanged');

const original20=copyData(draft.auto20);
root.v4046SaveRaw1248();drain();
draft=D.rounds['1248'].analysis;
assert.equal(draft.primaryWeight,0.7,'provided 1248 raw must have dominant weight');
assert.ok(Math.abs(draft.historyWeight-0.3)<1e-9);
assert.equal(draft.corpusRoundCount,1247);
assert.equal(corpus.calls,2495,'new primary + full 1247 historical raws all reanalyzed');
assert.equal(D.rounds['1248'].analysisVersions.length,1,'previous generated set preserved on change');
assert.deepEqual(copyData(D.rounds['1248'].analysisVersions[0].auto20),original20);
assert.equal(D.rounds['1248'].raw.text,makeRaw(1248));

root.v4044ReviewRound(1248,false);
const view=notes.filter(x=>x&&x.title==='제1248회 · 자동 20조합 및 당첨연동').at(-1);
assert.ok(view,'1248 emergency combo screen');
assert.ok(view.body.includes('1248회 원문 등록/수정'));
for(const n of [1244,1245,1246,1247,1248])assert.match(view.body,new RegExp('v330OpenRound\\('+n+'\\)'));
assert.ok(!view.body.includes('v330OpenRound(1243)'),'only current plus four previous visible');
assert.equal((view.body.match(/class="r5group"/g)||[]).length,4);
assert.equal((view.body.match(/class="r5row purchase"/g)||[]).length,20);
assert.doesNotMatch(view.body,/제1248회 자동 연동 당첨번호/,'unannounced result cannot appear');
assert.deepEqual(copyData(D.rounds['1247']),lockedBefore);

const expected=[8,14,21,22,42,45];
root.v4044ReviewRound(1247,false);
const after=notes.filter(x=>x&&x.title==='제1247회 · 자동 20조합 및 당첨연동').at(-1);
assert.ok(after.body.includes('제1247회 자동 연동 당첨번호'));
assert.equal((after.body.match(/class="r5group"/g)||[]).length,0,'locked record without verified 20 tickets must not fabricate');
const testGames=Array.from({length:20},(_,i)=>[1,2,3,4,5,6]);
const grades=root.v330GroupsResult(testGames,[1,2,3,4,5,7],6);
assert.match(grades,/2등/,'bonus 5+bonus grade must be recognised');
assert.equal((grades.match(/class="r5group"/g)||[]).length,4);
assert.equal((grades.match(/class="r5row"/g)||[]).length,20);

const researchBefore=copyData(D.rounds['1246']||{});
root.v4045AutoAnalyze20(1246,false);drain();
assert.equal(D.rounds['1246'].analysisResearch.researchOnly,true);
assert.equal(D.rounds['1246'].analysisResearch.historicalResultExcluded,true);
assert.deepEqual(copyData(D.rounds['1247']),lockedBefore);
assert.deepEqual(researchBefore,{});
console.log('PASS: V4.0.46 complete 1247-history analysis, optional dominant 1248 primary, 4x5 groups, result pending and graded, 5 visible rounds, future wins not fabricated, locked original preserved, historical research segregated, six JS syntax checks.');
