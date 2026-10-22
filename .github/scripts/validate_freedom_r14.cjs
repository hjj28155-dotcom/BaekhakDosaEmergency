const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const path=require('node:path');
const p='build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/';
const html=fs.readFileSync(p+'index.html','utf8'),r14=fs.readFileSync(p+'freedom_onboarding_r14.js','utf8');
assert.equal(html.split('src="freedom_onboarding_r14.js"').length-1,1,'R14 imported exactly once');
assert.ok(r14.includes('https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/update.html'),'app update route must open Freedom-only official download');
assert.ok(!r14.includes('moveicon.html'),'Freedom app must not navigate to MoveIcon distribution page');
assert.ok(r14.includes('h.openOfficialUpdateLink()'),'in-app update should use dedicated native browser handler');
assert.ok(!html.includes('id="r12AuthoritativeOnboardingMain"'),'old R12 authority removed');
assert.ok(!html.includes('id="r13AuthoritativeOnboarding"'),'old R13 authority removed');
assert.ok(!html.includes('window.__onboardingSequenceActive=true;\n  // FIX36'),'old early onboarding removed');
assert.ok(!html.includes('window.location.href="https://'),'no website redirect during onboard');
assert.match(html,/1244:\{nums:\[1,13,18,26,34,38\],bonus:25\}/);
assert.match(html,/v340ResponsiveResultRepair/);
assert.ok(fs.statSync(p+'install_guide_final.mp4').size>100000,'pre-approval install video asset exists');
assert.ok(fs.statSync(p+'freedom_onboarding_postscroll_20260928.mp4').size>100000,'approved guide video exists');
assert.ok(fs.statSync(p+'welcome_scroll_final.png').size>5000,'scroll image exists');
new vm.Script(r14,{filename:'freedom_onboarding_r14.js'});
for(const m of html.matchAll(/<script(?:\s[^>]*)?>([\s\S]*?)<\/script>/g)){
 if(m[1].trim())new vm.Script(m[1],{filename:'index_inline_script.js'});
}
const contextDB={window:{}};contextDB.window.window=contextDB.window;
vm.runInNewContext(fs.readFileSync(p+'raw_bank.js','utf8'),contextDB,{timeout:30000});
const original1244=contextDB.window.V3_RAW_BANK['1244'].text;
vm.runInNewContext(fs.readFileSync(p+'db1_v1247_patch.js','utf8'),contextDB,{timeout:30000});
assert.equal(contextDB.window.DB1_UPDATE_1247.status,'PASS');
assert.equal(contextDB.window.V3_RAW_BANK_META.count,1247);
assert.equal(contextDB.window.V3_RAW_BANK['1244'].text,original1244);
for(let n=1;n<=1247;n++)assert.equal(contextDB.window.V3_RAW_BANK[String(n)].text.replaceAll('\r','').split('\n').filter(Boolean).length,6,'DB1 '+n);
function createHarness(startState,storage=new Map()){
 const elements=new Map();
 class E {
  constructor(id){this.id=id;this.style={};this.handlers={};this.children=new Map();this.onclick=null;this.value='';this._html='';this.src='';}
  set innerHTML(value){
   this._html=value;
   for(const match of value.matchAll(/id="([^"]+)"/g)){
    const child=new E(match[1]);this.children.set(match[1],child);elements.set(match[1],child);
   }
  }
  get innerHTML(){return this._html}
  querySelector(sel){return this.children.get(sel.replace('#',''))||null}
  addEventListener(type,fn){this.handlers[type]=fn}
  dispatch(type){assert.ok(this.handlers[type],'missing event '+type+' on '+this.id);this.handlers[type]()}
  remove(){elements.delete(this.id);for(const [k] of this.children)elements.delete(k)}
  play(){return Promise.resolve()}
  pause(){}
  load(){}
 }
 const body={style:{},appendChild(el){elements.set(el.id,el)}};
 const document={readyState:'complete',body,documentElement:{style:{}},createElement(){return new E('')},getElementById(id){return elements.get(id)||null}};
 const logs={approvedRuntime:0,submitted:[],urlOpen:[],state:startState};
 const bridge={
  getSpecialApprovalSummary(){return JSON.stringify(logs.state)},
  requestSpecialApproval(round,name,ref){logs.submitted.push({round,name,ref});logs.state={status:'pending',has_request:true}},
  checkSpecialApproval(){},
  openRadioPage(url){logs.urlOpen.push(url)},
  startApprovedRuntime(){logs.approvedRuntime++}
 };
 const win={AndroidHost:bridge,localStorage:{getItem(k){return storage.get(k)||null},setItem(k,v){storage.set(k,v)}},document,unlockApprovedApp(){logs.approvedRuntime++},__onboardingSequenceActive:false};
 const ctx={window:win,document,localStorage:win.localStorage,AndroidHost:bridge,console,Promise,D:{},save(){},setTimeout(cb){cb()}};
 ctx.window.window=win;
 vm.runInNewContext(r14,ctx,{timeout:30000,filename:'freedom_onboarding_r14.js'});
 return {win,document,logs,storage,get(id){return document.getElementById(id)}};
}
// New installation: 5 steps, no external link navigation, no premature home
let t=createHarness({});
assert.ok(t.get('freedomR14Start'),'stage1 scroll');
t.get('freedomR14Start').onclick();
assert.equal(t.get('freedomR14Video').src,'install_guide_final.mp4','stage2 install movie');
assert.equal(t.logs.approvedRuntime,0);
t.get('freedomR14Video').dispatch('ended');
assert.ok(t.get('freedomR14Request'),'stage3 approval request');
t.get('freedomR14Name').value='Test Name';t.get('freedomR14Ref').value='Test Ref';
t.get('freedomR14Request').onclick();
assert.equal(t.logs.submitted.length,1);
assert.equal(t.logs.submitted[0].round,1244);
assert.ok(t.get('freedomR14Request'),'pending remains on request screen');
t.win.onSpecialApprovalState({status:'pending',has_request:true});
assert.ok(t.get('freedomR14Request'),'pending cannot bypass');
assert.equal(t.logs.approvedRuntime,0);
t.logs.state={status:'approved',approved:true};
t.win.onSpecialApprovalState({status:'approved',approved:true});
assert.equal(t.get('freedomR14Video').src,'freedom_onboarding_postscroll_20260928.mp4','stage4 approved guide');
assert.equal(t.logs.approvedRuntime,0,'guide must finish before home');
t.get('freedomR14Video').dispatch('ended');
assert.equal(t.get('freedomR14FullScreen'),null,'stage5 gate removed');
assert.equal(t.logs.approvedRuntime,1,'home unlocked once');
assert.equal(t.logs.urlOpen.length,0,'no external link during onboarding');
// Already approved on next launch returns straight home
const again=createHarness({approved:true,status:'approved'},t.storage);
assert.equal(again.get('freedomR14FullScreen'),null);
assert.equal(again.logs.approvedRuntime,1);
// Approved first launch with guide not complete must show guide
const firstApproved=createHarness({approved:true,status:'approved'});
assert.equal(firstApproved.get('freedomR14Video').src,'freedom_onboarding_postscroll_20260928.mp4');
assert.equal(firstApproved.logs.approvedRuntime,0);
// Pending user returns to approval, not movie loop
const pending=createHarness({status:'pending',has_request:true});
assert.ok(pending.get('freedomR14Check'));
assert.equal(pending.get('freedomR14Request').disabled,true,'existing approval application cannot be resent');
assert.equal(pending.logs.approvedRuntime,0);
assert.equal(pending.logs.submitted.length,0,'no automatic duplicate approval');
pending.get('freedomR14Request').onclick();
assert.equal(pending.logs.submitted.length,0,'duplicate approval guarded even when handler invoked');
// Captain phone already approved: no approval request screen on upgrade or relaunch.
const captain=createHarness({approved:true,status:'approved'});
assert.equal(captain.get('freedomR14Request'),null,'captain must not see request button');
assert.equal(captain.get('freedomR14Video').src,'freedom_onboarding_postscroll_20260928.mp4');
assert.equal(captain.logs.submitted.length,0);
captain.get('freedomR14Video').dispatch('ended');
assert.equal(captain.logs.approvedRuntime,1);
console.log('PASS: scroll -> installation video -> approval -> guide -> home, captain approved bypasses re-request');
