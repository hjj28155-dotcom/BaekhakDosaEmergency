const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const base='build405/FREEDOM_V402_BUILD_THIS/app/';
const html=fs.readFileSync(base+'src/freedom/assets/index.html','utf8');
function range(a,b){let i=html.indexOf(a),j=html.indexOf(b,i+a.length);assert.ok(i>=0&&j>i);return html.slice(i,j)}
const nodes={},timers=new Map(),calls=[];let seq=0,round=1248;
function node(){const classes=new Set();return {textContent:'',style:{display:'block'},children:[],classList:{add:c=>classes.add(c),remove:c=>classes.delete(c),contains:c=>classes.has(c)},querySelectorAll:()=>[],appendChild(e){this.children.push(e)}}}
for(const id of ['modal','fanfareOverlay','fanfareTitle','fanfareText','fanfareBottom','comfortOverlay','comfortTitle','comfortText','comfortBottom'])nodes[id]=node();
nodes.modal.classList.add('v4047Compact');
const native={playCelebrationWithVoice:(msg,r)=>calls.push(['win',msg,r]),speakConsolation:msg=>calls.push(['lose',msg]),stopScoreVoice:()=>calls.push(['stop'])};
const ctx={window:{BaekhakNative:native},document:{getElementById:id=>nodes[id],createElement:()=>node()},cur:()=>round,V4046_VISIBLE:[1244,1245,1246,1247,1248],D:{rounds:{}},v4046WinningForRound:(n,rec)=>rec.draw||null,setTimeout(fn,ms){const id=++seq;timers.set(id,{fn,ms});return id},clearTimeout:id=>timers.delete(id),SpeechSynthesisUtterance:function(t){this.text=t}};
vm.createContext(ctx);
vm.runInContext(range('function v4044ValidSets(games,count){','function v4044AutoRows(')+range('/* V4.0.49: cancel only transient effects','function v4048GoHome(')+range('function calcRank(hit,bonusHit){','function scoredRows(')+range('function scoreVoiceHost(){','\n\n',),ctx);
// The last range above ends after all score-audio functions (one-line declarations).
const winning={nums:[1,2,3,4,5,6],bonus:7};
const games=g=>Array.from({length:20},()=>g.slice());
function runTimers(){let count=0;while(timers.size){const [id,t]=[...timers.entries()].sort((a,b)=>a[1].ms-b[1].ms)[0];timers.delete(id);t.fn();assert.ok(++count<100)}}
function output(){return calls.filter(c=>c[0]!=='stop')}
function announce(sets,w,source){ctx.D.rounds[round]={draw:w,...(source==='research'?{analysisResearch:{auto20:sets}}:{analysis:{auto20:sets}})};return ctx.v4049Announce(round,false)}
for(const [g,rank] of [[[1,2,3,4,5,6],1],[[1,2,3,4,5,7],2],[[1,2,3,4,5,8],3],[[1,2,3,4,8,9],4],[[1,2,3,8,9,10],5]]){
 const sets=games(g),before=JSON.stringify(sets);assert.equal(ctx.v4049ScoreSummary(sets,winning).bestRank,rank);
 announce(sets,winning,'locked');runTimers();const last=output().at(-1);assert.equal(last[0],'win');assert.equal(last[2],rank);assert.match(last[1],new RegExp(rank+'등'));assert.equal(JSON.stringify(sets),before);
 const n=output().length;announce(sets,winning,'locked');runTimers();assert.equal(output().length,n,'revisit must not replay same saved result');
}
for(const g of [[1,2,8,9,10,11],[1,8,9,10,11,12],[8,9,10,11,12,13]]){announce(games(g),winning,'saved');runTimers();assert.equal(output().at(-1)[0],'lose')}
const n=output().length;
for(const [g,w] of [[[],winning],[games([1,2,3,4,5,6]),null]])announce(g,w,'saved');runTimers();assert.equal(output().length,n,'missing draw/tickets stay silent');
assert.equal(announce(games([1,2,3,4,5,7]),winning,'research'),false);runTimers();assert.equal(output().length,n,'research never imitates purchased wins');
// Home cancellation removes queued score effects before they can fire.
const before=output().length;
ctx.v4049Later(()=>ctx.showComfort(round,0),120);ctx.stopScoreAudio();runTimers();assert.equal(output().length,before);
// Browser speech fallback cancellation and actual fanfare oscillators.
let closed=0,started=0,spoken=[];
ctx.window.BaekhakNative=null;
ctx.window.AudioContext=function(){this.currentTime=0;this.destination={};this.close=()=>{closed++};this.createOscillator=()=>({frequency:{},connect(){return this},start(){started++},stop(){}});this.createGain=()=>({gain:{setValueAtTime(){},exponentialRampToValueAtTime(){}},connect(){return this}})};
ctx.window.speechSynthesis={cancel(){},speak:u=>spoken.push(u.text)};
ctx.showFanfare(round,5,20,2);assert.ok(started>0);ctx.stopScoreAudio();runTimers();assert.equal(spoken.length,0);assert.ok(closed>0,'Home stops active fanfare');
ctx.showFanfare(round,5,20,2);runTimers();assert.match(spoken.at(-1),/2등/);
ctx.showComfort(round,2);ctx.stopScoreAudio();let spokenCount=spoken.length;runTimers();assert.equal(spoken.length,spokenCount);
assert.match(html,/function closeM\(\)\{stopScoreAudio\(\)/);
assert.match(html,/function modal\(title,html\)\{v4052PrepareModal\(title,html\)/);
assert.match(html,/source!=='research'\)v4049Later/);
assert.match(fs.readFileSync(base+'src/freedom/assets/v4047_compact.css','utf8'),/z-index:2147483647!important/);
const java=fs.readFileSync(base+'src/main/java/com/baekhak/centralcontrol/MainActivity.java','utf8');
assert.match(java,/scoreVoiceHandler.removeCallbacksAndMessages\(null\);\s*pendingScoreSpeech = null;/);
assert.match(java,/scoreVoiceHandler.postDelayed\(\(\) -> speakScoreNow\(text, rankNo\), 650L\)/);
console.log('PASS: V4.0.49 ranks 1-5 with bonus 2nd; 0-2 hit consolation; native + browser fanfare/voice; research excluded; repeat suppression; pending/missing silence; navigation and audio cancellation; originals unchanged.');
// V4.0.52 regression: a delayed official-result redraw must not cancel speech.
ctx.window.innerHeight=768;ctx.window.addEventListener=()=>{};
ctx.document.documentElement={style:{setProperty(){}}};
vm.runInContext(range('function v4052SyncViewport(){','function modal(title,html){'),ctx);
ctx.window.BaekhakNative=native;
const scoreHtml='<div class="v4047Select">same verified draw and tickets</div>';
ctx.v4052PrepareModal('결과',scoreHtml);
ctx.showFanfare(round,5,20,2);
const stops=calls.filter(c=>c[0]==='stop').length;
ctx.v4052PrepareModal('결과',scoreHtml);
assert.equal(calls.filter(c=>c[0]==='stop').length,stops,'identical native callback redraw preserves fanfare and queued TTS');
ctx.v4052PrepareModal('원문','<textarea>source</textarea>');
assert.equal(calls.filter(c=>c[0]==='stop').length,stops+1,'leaving the result cancels speech');
ctx.document.querySelectorAll=()=>[];ctx.document.body={style:{}};
const close=html.match(/function closeM\(\)\{[^\n]+/)[0];vm.runInContext(close,ctx);
round=1248;ctx.D.rounds[round]={draw:winning,analysis:{auto20:games([1,2,3,4,5,7])}};
ctx.v4049Announce(round,true);ctx.closeM();nodes.modal.style.display='block';
assert.equal(ctx.v4049Announce(round,false),true,'returning from Home automatically announces again');
console.log('PASS: delayed identical draw redraw preserves native audio; navigation cancels; Home re-entry automatically replays.');
