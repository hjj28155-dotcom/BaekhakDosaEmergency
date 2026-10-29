/* READ-ONLY V4 combination diversification research. No localStorage, no network, no results. */
'use strict';
function seedHash(text){let h=2166136261>>>0;for(const c of String(text)){h^=c.charCodeAt(0);h=Math.imul(h,16777619)>>>0;}return h>>>0}
function rngFor(text){let seed=seedHash(text)||1;return()=>{seed=(Math.imul(1664525,seed)+1013904223)>>>0;return seed/4294967296}}
function shuffled(vals,rng){const x=[...vals];for(let i=x.length-1;i>0;i--){const j=Math.floor(rng()*(i+1));[x[i],x[j]]=[x[j],x[i]];}return x}
function checkGames(games){return games.length===20 && games.every(g=>g.length===6&&g.every(n=>Number.isInteger(n)&&n>=1&&n<=45)&&new Set(g).size===6) && new Set(games.map(g=>g.join(',')).size===20}
function scoreGames(group,prior){
 let penalty=0;const seen=new Set(prior.map(g=>g.join(',')));
 for(const game of group){
  const key=game.join(',');if(seen.has(key))penalty+=500;seen.add(key);
  for(const old of prior){const overlap=game.filter(n=>old.includes(n)).length;penalty+=overlap>=4?Math.pow(overlap-3,2)*12:0;}
 }
 return penalty
}
function build({ranking,round,poolSize=30,seedSuffix=''}) {
 if(!Number.isInteger(round)||round<1)throw Error('round invalid');
 if(!Number.isInteger(poolSize)||poolSize<30||poolSize>45)throw Error('poolSize must be 30..45');
 const order=[...new Set((ranking||[]).map(n=>typeof n==='number'?n:Number(n.n)).filter(n=>Number.isInteger(n)&&n>=1&&n<=45))];
 if(order.length<poolSize)throw Error('Ranking missing candidates: no fallback fabrication');
 const pool=order.slice(0,poolSize),exposure=Object.fromEntries(pool.map(n=>[n,0]));
 const games=[],groups=[];
 for(let g=0;g<4;g++){
  const chosen=pool.map((n,i)=>({n,exposure:exposure[n],rank:i})).sort((a,b)=>a.exposure-b.exposure||a.rank-b.rank).slice(0,30).map(x=>x.n);
  const rng=rngFor('V4-RESEARCH-DIVERSITY-v1|'+round+'|'+poolSize+'|group'+g+'|'+seedSuffix);
  let best=null,bestPenalty=Infinity;
  for(let t=0;t<240;t++){
   const order=shuffled(chosen,rng),candidate=Array.from({length:5},(_,i)=>order.slice(i*6,i*6+6).sort((a,b)=>a-b));
   const penalty=scoreGames(candidate,games);
   if(penalty<bestPenalty){best=candidate;bestPenalty=penalty;if(penalty===0)break}
  }
  if(!best||bestPenalty>=500)throw Error('No unique grouping produced');
  for(const game of best)games.push(game);
  groups.push(best);chosen.forEach(n=>exposure[n]++);
 }
 if(!checkGames(games)||groups.some(g=>new Set(g.flat()).size!==30))throw Error('Invalid partition');
 return {round,poolSize,source:'RESEARCH_ONLY_NO_SAVE_NO_WIN_RESULT',pool,games,groups,exposure,uniqueNumbers:new Set(games.flat()).size}
}
module.exports={build,checkGames};
