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
function runTimers(){let count=0;while(timers.size){const [id,t]=[...timers.entries()].sort((a,b)=>a[1].ms-b[1].ms)[0];timers.delete(id);t.fn();assert.ok(++count<150)}}
function output(){return calls.filter(c=>c[0]!=='stop')}
function announce(sets,w,source){ctx.D.rounds[round]={draw:w,...(source==='research'?{analysisResearch:{auto20:sets}}:{analysis:{auto20:sets}})};return ctx.v4049Announce(round,false)}
for(const [g,rank] of [[[1,2,3,4,5,6],1],[[1,2,3,4,5,7],2],[[1,2,3,4,5,8],3],[[1,2,3,4,8,9],4],[[1,2,3,8,9,10],5]]){
 const tickets=games(g),before=JSON.stringify(tickets);
 assert.equal(ctx.v4049ScoreSummary(tickets,winning).bestRank,rank,'prize rank preserved in visual table');
 assert.equal(announce(tickets,winning,'saved'),false,'captain requested no winning voice or fanfare');
 runTimers();assert.equal(output().length,0);
 assert.equal(JSON.stringify(tickets),before,'original twenty tickets immutable');
}
for(const [g,source] of [[[1,2,8,9,10,11],'saved'],[[1,8,9,10,11,12],'saved'],[[8,9,10,11,12,13],'research']]){
 const before=output().length;
 assert.equal(announce(games(g),winning,source),true,'losing result must announce');
 runTimers();
 assert.equal(output().length,before+1);
 const last=output().at(-1);assert.equal(last[0],'lose','ONLY speak consolation');
 assert.equal(last[1],`아쉽습니다. 제${round}회는 낙첨입니다. 다음 회차에는 더 좋은 결과를 기대하겠습니다.`,'saved and research must use EXACT SAME SPOKEN TEXT');
 if(source==='research')assert.match(nodes.comfortBottom.textContent,/실제 구매 성적 아님/,'research provenance remains visual');
 assert.equal(ctx.v4049Announce(round,false),false,'same loss must not repeat automatically');runTimers();assert.equal(output().length,before+1);
 assert.equal(ctx.v4049Announce(round,true),true,'explicit losing message replay');runTimers();assert.equal(output().length,before+2);
}
const count=output().length;
assert.equal(announce([],winning,'saved'),false,'no stored tickets means silence');
assert.equal(announce(games([8,9,10,11,12,13]),null,'saved'),false,'unannounced draw silent');
assert.equal(announce(games([1,2,3,4,5,7]),winning,'research'),false,'research with 3+ matches must not be called a loss');
runTimers();assert.equal(output().length,count);
for(const [n,source,combo,sounds] of [
 [1244,'saved',[8,9,10,11,12,13],true],
 [1245,'research',[8,9,10,11,12,13],true],
 [1246,'saved',[1,2,3,4,5,7],false],
 [1247,'research',[8,9,10,11,12,13],true],
 [1248,'saved',[1,2,3,4,5,6],false]
]){
 round=n;const prior=output().length;
 assert.equal(announce(games(combo),winning,source),sounds,'each round follows only-loss policy');
 runTimers();assert.equal(output().length,prior+(sounds?1:0));
 if(sounds)assert.equal(output().at(-1)[1],`아쉽습니다. 제${n}회는 낙첨입니다. 다음 회차에는 더 좋은 결과를 기대하겠습니다.`,'all five rounds use identical template');
}
// Home cancellation must abort queued losing speech and not modify tickets.
const prior=output().length;
ctx.v4049Later(()=>ctx.v4054LossVoice(round,{wins:0,highestHits:0},false),120);
ctx.stopScoreAudio();runTimers();assert.equal(output().length,prior);
// Native unavailable: short browser consolation fallback.
const spoken=[];ctx.window.BaekhakNative=null;
ctx.window.speechSynthesis={cancel(){},speak:u=>spoken.push(u.text)};
ctx.v4054LossVoice(round,{wins:0,highestHits:1},false);runTimers();
assert.match(spoken.at(-1),/낙첨입니다/);
ctx.v4054LossVoice(round,{wins:0,highestHits:1},true);ctx.stopScoreAudio();
const unchanged=spoken.length;runTimers();assert.equal(spoken.length,unchanged);
assert.match(html,/function closeM\(\)\{stopScoreAudio\(\)/);
assert.match(html,/function modal\(title,html\)\{v4052PrepareModal\(title,html\)/);
assert.match(html,/if\(win&&display20&&v4049ScoreSummary\(display20,win\)[.]wins===0\)v4049Later/);
assert.match(html,/function v4054LossVoice/);
assert.match(html,/if\(outcome[.]wins>0\)return false/);
assert.doesNotMatch(html,/if\(researchOnly\)v4053ResearchVoice/);
const java=fs.readFileSync(base+'src/main/java/com/baekhak/centralcontrol/MainActivity.java','utf8');
assert.match(java,/scoreVoiceHandler.removeCallbacksAndMessages\(null\);\s*pendingScoreSpeech = null;/);
console.log('PASS: V4.0.55 uniform concise 낙첨 voice for zero wins, saved/research provenance, consecutive rounds, 1~5 visual ranks, winning silence, repeat control, browser/native TTS, Home cancel, originals preserved.');
// V4.0.52 regression: same-score redraw preserves a losing TTS queued or playing.
ctx.window.BaekhakNative=native;
ctx.window.innerHeight=768;ctx.window.addEventListener=()=>{};
ctx.document.documentElement={style:{setProperty(){}}};
vm.runInContext(range('function v4052SyncViewport(){','function modal(title,html){'),ctx);
const scoreHtml='<div class="v4047Select">same verified draw and tickets</div>';
nodes.modal.style.display='block';
ctx.v4052PrepareModal('결과',scoreHtml);
ctx.v4054LossVoice(round,{wins:0,highestHits:2},false);
const stops=calls.filter(c=>c[0]==='stop').length;
ctx.v4052PrepareModal('결과',scoreHtml);
assert.equal(calls.filter(c=>c[0]==='stop').length,stops,'identical draw callback redraw must preserve consolation speech');
ctx.v4052PrepareModal('원문','<textarea>source</textarea>');
assert.equal(calls.filter(c=>c[0]==='stop').length,stops+1,'leaving results stops speech');
ctx.document.querySelectorAll=()=>[];ctx.document.body={style:{}};
vm.runInContext(html.match(/function closeM\(\)\{[^\n]+/)[0],ctx);
ctx.D.rounds[round]={draw:winning,analysis:{auto20:games([8,9,10,11,12,13])}};
ctx.v4049Announce(round,true);ctx.closeM();nodes.modal.style.display='block';
assert.equal(ctx.v4049Announce(round,false),true,'home revisit may replay missing prior losing message');
console.log('PASS: replay after Home and same score redraw preserves native consolation, navigation cancels.');
