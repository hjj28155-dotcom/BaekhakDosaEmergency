'use strict';
const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path'),vm=require('node:vm');
const html=fs.readFileSync(path.join(__dirname,'v13-ui-r19.html'),'utf8');
function has(x){assert.ok(html.includes(x),'missing: '+x);}
test('r19 HTML retains V13 real DB1 and app update hook',()=>{has('const RAW=JSON.parse');has('window.V13_DB1=Object.freeze');has('window.V13UpdateResult=function');has('window.V13CheckHomeUpdates=');});
test('home one-tap calls real R18 four room pipeline',()=>{has("v13R19HomeRun.textContent='⚓ 원문분석 실행'");has("runOriginalRawAnalysis();");has('async function runConnectedPredraw(');has('const tasks=PREDRAW_KEYS.map(');has("['captain','dosa','freedom','navigator']");});
test('Freedom V3 original top overlay and back',()=>{has("v13R19FreedomFrame.src='v12_v3_master/index.html'");has("v13R19FreedomOverlay.hidden=false");has("v13R19FreedomOverlay.hidden=true");has("if(!v13R19FreedomOverlay.hidden)");});
test('score keeps original official draw and fanfare',()=>{has('function scoreAfterDraw(');has("record.verifiedOfficial!==true");has('function playLocalFanfare()');has('🎺 축하 빵빠레 재생');has('이번에는 낙선입니다');has('scheduleAutoDraw(round)');});
test('original user private chat and V1 popup remain',()=>{has("id=\"homeOriginalChat\"");has("data-top=\"update\"");has("V13Native.checkForUpdates(true)");has('v13ShowPublishedNotice');});
test('all inline JavaScript modules parse with Node vm',()=>{
const tags=[...html.matchAll(/<script([^>]*)>([\s\S]*?)<\/script>/gi)].filter(m=>!(/(?:application\/json|json|raw-data)/i.test(m[1])));
assert.ok(tags.length>=1);
for(let i=0;i<tags.length;i++) new vm.Script(tags[i][2],{filename:'inline-'+i+'.js'});
});
test('no accidental certified result synthesis in extension',()=>{
const block=html.slice(html.indexOf('/* R19 feature-only extension'),html.indexOf('window.V13_REMOTE_R19'));
assert.ok(!block.includes('importBeforeReport('));
assert.ok(!block.includes('localStorage.setItem('));
assert.ok(!block.includes('registerEngine('));
});
