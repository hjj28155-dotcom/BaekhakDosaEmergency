// Execute the real original engine functions in Node VM (not mocked engine outputs).
// This is a headless JavaScript contract test, NOT a device/WebView installation test.
'use strict';
const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const root='build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/';
const source=fs.readFileSync(root+'engine_v2.html','utf8');
const scripts=[...source.matchAll(/<script(?:\s[^>]*)?>([\s\S]*?)<\/script>/gi)].map(x=>x[1]);
assert.ok(scripts.length>10&&scripts[2].includes('function v13Run()'),'real V1.3 engine found');
const nodes=new Map();
function el(id){
 if(!nodes.has(id))nodes.set(id,{id,value:'',textContent:'',innerHTML:'',style:{},classList:{add(){},remove(){},toggle(){}},
  addEventListener(){},appendChild(){},setAttribute(){},removeAttribute(){},click(){}});
 return nodes.get(id);
}
const window={};
const document={getElementById:el,querySelector(){return null},querySelectorAll(){return []},
 createElement(){return el('new')},readyState:'loading',addEventListener(){},body:el('body')};
const ctx={window,document,localStorage:{getItem(){return null},setItem(){}},
 alert(){},URL:{createObjectURL(){return ''},revokeObjectURL(){}},Blob:function(){},
 setTimeout(){},console};vm.createContext(ctx);
for(const js of scripts.slice(0,4))vm.runInContext(js,ctx,{timeout:12000});
assert.equal(typeof vm.runInContext('v13Run',ctx),'function');
vm.runInNewContext(fs.readFileSync(root+'raw_bank.js','utf8'),{window});
vm.runInNewContext(fs.readFileSync(root+'db1_v1247_patch.js','utf8'),{window});
assert.equal(window.DB1_UPDATE_1247.status,'PASS');
const app=fs.readFileSync(root+'index.html','utf8');
const start=app.indexOf('function v4043TaskMatchesEngineReply('),end=app.indexOf('function v4043AcceptEngineResult(',start);
assert.ok(start>0&&end>start);
const verifier=app.slice(start,end);
vm.runInContext(verifier,ctx);
let previous=null;
for(const n of [1242,1244,1246,1247]){
 const r=window.V3_RAW_BANK[String(n)];
 assert.ok(r,'DB-1 round missing '+n);
 const raw=String(r.text||r.lines&&r.lines.join('\n')||'').trim();
 const lines=raw.split(/\r?\n/).map(x=>x.trim()).filter(Boolean);
 assert.equal(lines.length,6,'DB-1 six lines '+n);
 el('round').value=String(n);el('raw').value=raw;
 vm.runInContext('v13Run()',ctx,{timeout:20000});
 const engine=vm.runInContext('LAST',ctx);
 assert.equal(engine.round,n);
 assert.deepEqual(Array.from(engine.lines),lines);
 const rank=Array.from(engine.rank,x=>Number(x.n));
 assert.ok(rank.length>=8,'real engine candidate universe >=8 for '+n);
 assert.equal(new Set(rank).size,rank.length,'real engine candidates are distinct');
 const task={round:n,raw},response={ok:true,round:n,decoded:engine.dec,rank:engine.rank};
 assert.equal(ctx.v4043TaskMatchesEngineReply(response,task),true,'fresh response accepted for '+n);
 if(previous){
  assert.equal(ctx.v4043TaskMatchesEngineReply(previous.response,task),false,'older round must not satisfy new source');
 }
 const changed={round:n,raw:lines.map((x,i)=>i===0?x+' 임의추가문장':x).join('\n')};
 assert.equal(ctx.v4043TaskMatchesEngineReply(response,changed),false,'same-round changed original must invalidate previous rank');
 previous={task,response};
 console.log('PASS actual V1.3 original engine',n,'rank',rank.length,'final5',engine.pack&&engine.pack.final5&&engine.pack.final5.length);
}
assert.equal(ctx.v4043TaskMatchesEngineReply({ok:true,round:1247,decoded:previous.response.decoded,rank:[{n:1}]},previous.task),false,'incomplete rank rejected');
assert.equal(ctx.v4043TaskMatchesEngineReply({ok:true,round:1247,rank:previous.response.rank},previous.task),false,'missing six-sentence evidence rejected');
console.log('PASS: genuine DB-1 original engine 4 rounds, exact decoded raw matching, cross-round and same-round stale-data rejection.');
