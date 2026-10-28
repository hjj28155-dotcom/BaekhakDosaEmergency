const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const src=fs.readFileSync('build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/freedom_onboarding_r14.js','utf8');
new vm.Script(src);
const elements=new Map(),timers=[],listeners={},intervals=[];let checks=0,opened=0,now=1000000;
let doc;
class Element{
 constructor(){this.id='';this.style={display:'block'};this.children=new Map();this.events={};this.textContent=''}
 setAttribute(k,v){this[k]=v}
 set innerHTML(s){this.html=s;for(const m of s.matchAll(/id="([^"]+)"/g)){const e=new Element();e.id=m[1];elements.set(e.id,e);this.children.set(e.id,e)}}
 querySelector(s){return this.children.get(s.slice(1))}
 addEventListener(k,fn){this.events[k]=fn}
 focus(){doc.activeElement=this}
 remove(){elements.delete(this.id);for(const id of this.children.keys())elements.delete(id)}
}
doc={hidden:false,readyState:'complete',activeElement:null,body:{style:{},appendChild(e){elements.set(e.id,e)}},documentElement:{style:{}},createElement:()=>new Element(),getElementById:id=>elements.get(id)||null,addEventListener(k,fn){listeners[k]=fn}};
const stored=new Map([['freedom_v439_guide_video_finished','1']]);
const bridge={getSpecialApprovalSummary:()=>JSON.stringify({approved:true}),checkLatestFreedomVersion(){checks++},openOfficialUpdateLink(){opened++}};
const win={AndroidHost:bridge,getComputedStyle:e=>e.style,addEventListener:(k,fn)=>listeners[k]=fn,setInterval:fn=>intervals.push(fn),unlockApprovedApp(){}};
const context={window:win,document:doc,Date:{now:()=>now},localStorage:{getItem:k=>stored.get(k)||null,setItem:(k,v)=>stored.set(k,v)},setTimeout(fn,ms){timers.push({fn,ms})},alert(){throw Error('unexpected alert')},D:{rounds:{keep:'unchanged'}}};
vm.runInNewContext(src,context);
function popup(){return elements.get('freedomHomeUpdatePopup')}
function status(code,installed=40050){win.onFreedomVersionStatus(JSON.stringify({status:code>installed?'update':'latest',installed_code:installed,latest_code:code,installed_name:'4.0.50',latest_name:'4.0.51'}))}
function tick(){intervals.forEach(fn=>fn())}
for(const t of [...timers])if(t.ms===1200)t.fn();
assert.equal(checks,1,'automatic check on approved Home');
win.onFreedomVersionStatus('{bad');assert.equal(popup(),undefined);
status(40050);assert.equal(popup(),undefined,'same version stays silent');status(40049);assert.equal(popup(),undefined,'older release stays silent');
win.onFreedomVersionStatus('null');assert.equal(popup(),undefined);
win.onFreedomVersionStatus({status:'update',installed_code:40050,latest_code:40050.5});assert.equal(popup(),undefined);
// A result received during analysis must wait until the Home is visible.
const modal=new Element();modal.id='modal';elements.set('modal',modal);
status(40051);assert.equal(popup(),undefined,'no popup on analysis screen');
modal.style.display='none';tick();assert.ok(popup(),'deferred popup appears on Home');
assert.match(elements.get('freedomHomeUpdateVersion').textContent,/4.0.50 → 새 버전 4.0.51/);
elements.get('freedomHomeUpdateLater').onclick();assert.equal(popup(),undefined);
status(40051);tick();assert.equal(popup(),undefined,'same release dismissed for current app session');
status(40052);assert.ok(popup(),'newer version can notify');elements.get('freedomHomeUpdateNow').onclick();assert.equal(opened,1,'uses existing official update link');assert.equal(popup(),undefined);
// Resume/focus retries are throttled and hidden app never shows popup.
assert.equal(checks,1);listeners.focus();assert.equal(checks,1);
now+=300001;listeners.focus();assert.equal(checks,2);
doc.hidden=true;status(40053);assert.equal(popup(),undefined);doc.hidden=false;listeners.visibilitychange();assert.ok(popup());
const event={key:'Escape',preventDefault(){}};popup().events.keydown(event);assert.equal(popup(),undefined);
win.onFreedomVersionStatus({status:'error'});tick();assert.equal(popup(),undefined,'network failure stays silent');
assert.equal(context.D.rounds.keep,'unchanged');assert.deepEqual([...stored.keys()],['freedom_v439_guide_video_finished'],'no approval/DB storage writes');
assert.match(src,/window.updatePage=function/);assert.match(src,/if\(dl\)dl.onclick=openFreedomUpdate/);
console.log('PASS: Home startup/resume checking, official newer-only popup, modal defer, Later/session dedupe, newer release, existing update route, error/old/latest silence, focus throttle, background defer and data preservation.');
