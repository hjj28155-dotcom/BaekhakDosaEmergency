// Isolated Freedom V4.0.44 source regression verification (no installation, no original-record writes).
const fs=require('node:fs');
const vm=require('node:vm');
const assert=require('node:assert/strict');
const base='build405/FREEDOM_V402_BUILD_THIS/app/';
const html=fs.readFileSync(base+'src/freedom/assets/index.html','utf8');
const gradle=fs.readFileSync(base+'build.gradle','utf8');

assert.match(gradle,/applicationId 'com[.]navigator[.]freedom[.]v3final'/);
assert.match(gradle,/versionCode 40044/);
assert.match(gradle,/versionName "4[.]0[.]44-RAW-GRADE-RECOVERY"/);
assert.match(gradle,/signingConfig signingConfigs[.]distribution/);
assert.match(html,/id="v4044SmallScreenModalTitleFix"/);
assert.match(html,/white-space:normal!important;/);
assert.match(html,/function v4044ReviewRound[(]round[)]/);
assert.match(html,/1247:\{nums:\[8,14,21,22,42,45\],bonus:24\}/);

const inline=[...html.matchAll(/<script([^>]*)>([\s\S]*?)<\/script>/gi)].filter(m=>!/\bsrc\s*=/.test(m[1]));
assert.equal(inline.length,6,'Six embedded source scripts expected');
for(const [i,s] of inline.entries())assert.doesNotThrow(()=>new Function(s[2]),'embedded script '+i);

const start=html.indexOf('function v4044RawCard(round){');
const end=html.indexOf('function v330OpenRound(n){',start);
assert.ok(start>0&&end>start,'isolated recent-round review functions exist');
let record={raw:{text:'첫째만',source:'기기에 저장된 원문'}};
let bank={text:['첫째','둘째','셋째','넷째','다섯째','여섯째'].join('\n'),source:'내장 DB-1'};
let last={};
const context={
  R:()=>record,
  rawBank:()=>bank,
  esc:s=>String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'),
  V330_RECENT:[1243,1244,1245,1246,1247],
  V330_WIN:{1247:{nums:[8,14,21,22,42,45],bonus:24}},
  setRound:n=>n===1247,
  modal:(title,markup)=>{last={title,markup}},
  rawPage:()=>{},
  closeM:()=>{},
};
vm.createContext(context);
vm.runInContext(html.slice(start,end),context,{timeout:1500});
const {v4044RawCard,v4044ValidSets,v4044AutoRows,v4044ReviewRound}=context;

const raw=v4044RawCard(1247);
assert.match(raw,/첫째[\s\S]*둘째[\s\S]*여섯째/);
assert.match(raw,/기존 입력은 삭제하거나 덮어쓰지 않았습니다/);
assert.equal(record.raw.text,'첫째만','persisted original must not change');

const repeated=Array.from({length:20},()=>[8,14,21,22,42,45]);
const normalized=v4044ValidSets(repeated,20);
assert.ok(normalized&&normalized.length===20,'duplicate saved tickets are not discarded');
assert.equal(v4044ValidSets([[8,14,21,22,42,46]],1),null,'out-of-range game rejected');
assert.equal(v4044ValidSets([[8,8,14,21,22,42]],1),null,'within-game duplicate rejected');
assert.equal(v4044ValidSets([],20),null,'never create missing prior tickets');
const rows=v4044AutoRows(normalized,context.V330_WIN[1247],'저장된 20조합');
assert.equal((rows.match(/class="r5row"/g)||[]).length,20,'all saved tickets shown');
assert.match(rows,/중복 저장 조합 19건 포함/);
assert.equal((rows.match(/1등/g)||[]).length,20);

record={raw:{},analysis:{},buy:{}};
v4044ReviewRound(1247);
assert.match(last.markup,/제1247회 확인된 당첨번호/);
assert.match(last.markup,/저장 조합 확인 대기/);
assert.doesNotMatch(last.markup,/최초잠금 구매 5조합 자동채점/);
const archive=Array.from({length:5},(_,i)=>({game:[8,14,21,22,42,45]}));
record={raw:{},analysis:{},legacy:{groups:[{rows:archive}]},buy:{}};
v4044ReviewRound(1247);
assert.match(last.markup,/기존 저장 분석그룹·이관 조합 비교/);
assert.match(last.markup,/최초잠금 인증과 별개/);
assert.doesNotMatch(last.markup,/최초잠금 구매 5조합 자동채점/);

record={raw:{},analysis:{auto20:repeated},buy:{locked:true,combos:repeated.slice(0,5)}};
v4044ReviewRound(1247);
assert.match(last.markup,/최초잠금 구매 5조합 자동채점/);
assert.match(last.markup,/저장된 자동분석 20조합 비교/);
console.log('PASS: V4.0.44 six JS blocks, original fallback, repeated 20 tickets, invalid tickets, no fabrication, legacy comparison, locked 5 ticket grading, CSS/identity checks');
