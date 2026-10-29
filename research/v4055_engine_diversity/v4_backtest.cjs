/* Retrospective research ONLY. Input: V4.0.55 APK extracted assets (never published here).
 * Usage: V4_ASSET_DIR=/path/to/assets node v4_backtest.cjs
 * Requires nearby public_draws_1148_1247.csv and v4_diversity_candidate.cjs.
 * Inputs MAY include retrospectively edited raw text or later reference dictionaries:
 * NEVER call results pre-draw evidence. NO write to app localStorage, Drive or DB.
 */
'use strict';
const assert=require('node:assert/strict'),fs=require('fs'),vm=require('vm'),path=require('path');
const div=require('./v4_diversity_candidate.cjs');
const root=process.env.V4_ASSET_DIR||path.join(__dirname,'apk_assets');
const context={window:{}};vm.createContext(context);
for(const file of ['raw_bank.js','db1_v1247_patch.js','freedom_reference_v4043.js']){
 vm.runInContext(fs.readFileSync(path.join(root,file),'utf8'),context,{timeout:15000});
}
const html=fs.readFileSync(path.join(root,'index.html'),'utf8');
const start=html.indexOf('function v123Seeded(seedStr){');
const end=html.indexOf('function v123Seed1243Result(){',start);
assert(start>=0&&end>start);
vm.runInContext(html.slice(start,end),context);
const bank=context.window.V3_RAW_BANK,ref=context.window.FREEDOM_REF_V4043;
const csv=fs.readFileSync(path.join(__dirname,'public_draws_1148_1247.csv'),'utf8').trim().split(/\r?\n/).slice(1).map(x=>x.split(',').map(Number));
assert.equal(csv.length,100);
const winners=Object.fromEntries(csv.map(row=>[row[0],row.slice(1,7)]));
const rawScores=Array.from({length:1248},()=>new Float64Array(46));
for(let round=1;round<=1247;round++){
 const raw=bank[String(round)]?.text;
 assert(raw&&raw.split('\n').length===6);
 const parsed=ref.scoreRaw(raw);
 assert.equal(parsed.status,'OK');
 for(let n=1;n<=45;n++)rawScores[round][n]=Number(parsed.scores[n]||0);
}
const prefix=Array.from({length:1248},()=>new Float64Array(46));
for(let r=1;r<=1247;r++)for(let n=1;n<=45;n++)prefix[r][n]=prefix[r-1][n]+rawScores[r][n];
function ranking(round,useCurrent){
 const hist=prefix[round-1],m=Math.max(...hist.slice(1)),cur=rawScores[round],cmax=Math.max(...cur.slice(1));
 const weight=useCurrent&&cmax?0.7:0;
 return Array.from({length:45},(_,i)=>({n:i+1,score:(1-weight)*(hist[i+1]/m)+weight*(cur[i+1]/(cmax||1))}))
  .sort((a,b)=>b.score-a.score||a.n-b.n);
}
const rows=[];
for(let round=1148;round<=1247;round++)for(const currentRaw of [false,true]){
 const ranked=ranking(round,currentRaw);
 const old=context.v123Auto20FromUniverse(ranked.slice(0,12).map(x=>x.n),round,20);
 for(const poolSize of [12,30,36,45]){
  const games=poolSize===12?old:div.build({ranking:ranked,round,poolSize,seedSuffix:currentRaw?'primary':'history'}).games;
  assert(div.checkGames(games));
  const hits=games.map(g=>g.filter(n=>winners[round].includes(n)).length);
  rows.push({round,currentRaw,poolSize,winningTickets:hits.filter(h=>h>=3).length,bestHit:Math.max(...hits)});
 }
}
const summary=[false,true].flatMap(currentRaw=>[12,30,36,45].map(poolSize=>{
 const sel=rows.filter(x=>x.currentRaw===currentRaw&&x.poolSize===poolSize);
 return {currentRaw,poolSize,rounds:sel.length,drawsAtLeastOneWin:sel.filter(x=>x.winningTickets>0).length,totalWinningTickets:sel.reduce((s,x)=>s+x.winningTickets,0)};
}));
console.log(JSON.stringify({warning:'RETROSPECTIVE ONLY; not verified pre-draw locked, no profit or forecasting claim',source:'V4.0.55 APK assets; unverified historical source chronology',summary},null,2));
