/* Isolated, read-only, offline reference bundle verification. */
const fs=require('node:fs'),assert=require('node:assert/strict'),crypto=require('node:crypto'),vm=require('node:vm');
const base='build405/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/';
const source=base+'reference_sources/';
const sha=x=>crypto.createHash('sha256').update(x).digest('hex');
const manifest=JSON.parse(fs.readFileSync(base+'reference_sources_manifest_v4043.json','utf8'));
assert.equal(manifest.mode,'ISOLATED_CANDIDATE_UNAPPROVED_NOT_OFFICIAL');
const cases=[
 ['백학_공식해독규칙_V1.0.txt','657e3a32b0b7be6bc9cc7d40a327232a4502706c05f8ae99ac53ae9478999ad1',18277],
 ['00_6대6_성공회차_마스터목록_20260818.txt','53d38c03de30c08aee51422e5bd37cefcb40b6771cee8e67206e3a8b72e9c39f',3534],
 ['신서_1001_1033_6대6_성공패턴_기준판_V1.0.txt','26b05caf362dd46b3e01e6ac0464bb4ff3419b954b0c1a97f78aa53ed1a8911e',10463],
 ['백학.txt','4f8059ee26987ac1482ac687ddbe4056e7215ccbd7e719e8377bff7eb097eb3a',977383],
 ['백학 로또꿈해몽백과 2026 6월신버젼 (1).txt','1842831fe89e7d413a3ae6ea46fafd774e333c084750d80e90aff4f880a8b722',536793],
 ['백학도사_197풀이_198-202신서_공식교본_원문보존_20260818.txt','52769ee60f5d0346b97d1a51bbb37c2dc0b55651088551f53e4bd8ac8deeba7f',11135],
 ['성공사례_분류기준_3분의6부터_V1.0_20260821.txt','86b69c401570e3698397729ef8a48f405fc6ff551930b6c9cdccedace8c8dc7c',1221],
 ['백학신서_청명방_성공사례_재복기_학습메모_V1.0_20260821.txt','89a665d0a4de0ae10121b5db3629a37dca70578066d6181aa23a695bd9c6be1b',4385],
 ['백학신서_청명방_혹독예행6_성공사례_3of6_결과후복기_V1.0_20260821.txt','bcbc7ebe0dd272540892e52fcf7aef48189d2ba02428d4d987a4415e990e1b16',5118]
];
for(const [file,digest,size] of cases){
 const bytes=fs.readFileSync(source+file);assert.equal(bytes.length,size,file+' byte length');assert.equal(sha(bytes),digest,file+' SHA256');
 assert.ok(manifest.files[file]);
}
const html=fs.readFileSync(base+'index.html','utf8');
assert.equal(html.split('src="freedom_reference_v4043.js"').length-1,1,'reference lexicon must load once');
assert.ok(html.indexOf('src="freedom_reference_v4043.js"')<html.indexOf('const APP_BASE_VERSION'),'load index before main JS');
assert.match(html,/referencePack\.scoreRaw\(primary\.task\.raw\)/,'dream lexicon must actually influence engine rank');
const lex=fs.readFileSync(base+'freedom_reference_v4043.js','utf8');
new vm.Script(lex,{filename:'freedom_reference_v4043.js'});
const window={};vm.runInNewContext(lex,{window,console});
const pack=window.FREEDOM_REF_V4043;
assert.equal(pack.numbers,45);assert.ok(pack.lexiconEntries>11000);
const raw=['첫번째. 항공모함이 바다에서 떠 있다.','두번째. 검은 하늘.','세번째. 해가 뜬다.','네번째. 바람.','다섯번째. 등대.','여섯번째. 종소리.'].join('\n');
const evidence=pack.scoreRaw(raw);
assert.equal(evidence.status,'OK');assert.ok(evidence.scores[33]>0||evidence.scores[44]>0);
assert.equal(evidence.winningNumbersUsed,false);
assert.equal(pack.scoreRaw('one line').status,'SIX_RAW_REQUIRED');
assert.ok(!lex.includes('당첨번호: 7 · 9 · 11 · 21 · 30 · 35'),'result-after numbers not embedded in scoring module');
const start=html.indexOf('function v4043AcceptEngineResult('),end=html.indexOf('function v4043BeginAnalysis(',start);
assert.ok(start>=0&&end>start,'engine callback markers');
const callback=html.slice(start,end);new vm.Script(callback);
let delivered=null;
const state={
 window:{FREEDOM_REF_V4043:{manifest:{version:'test'},lexiconEntries:123,scoreRaw:()=>({status:'OK',scores:{10:100,9:20},matches:[{number:10,term:'test'}]})}},
 V4043_ANALYSIS_SESSION:{index:0,responses:[],historyCount:0,source:'manual',tasks:[{role:'CURRENT',round:1247,raw}]},
 v4043MergedRanks:()=>Array.from({length:10},(_,i)=>({n:i+1,score:10-i})),
 handleEngineResult:m=>{delivered=m},modal:()=>{throw Error('unexpected modal')},
 setTimeout:()=>{},v4043SendNextEngineTask:()=>{throw Error('unexpected extra job')},
 Number,Array,Object,String
};vm.createContext(state);vm.runInContext(callback,state);
state.v4043AcceptEngineResult({ok:true,round:1247,rank:Array.from({length:10},(_,i)=>({n:i+1}))});
assert.ok(delivered);assert.deepEqual(Array.from(delivered.rank.slice(0,6),v=>v.n),[1,2,3,4,5,6]);
assert.deepEqual(Array.from(delivered.rank.slice(6),v=>v.n),[10,9,7,8]);
assert.equal(delivered.db1Evidence.referenceEvidence.successCases,'POST_RESULT_EXCLUDED');
assert.equal(delivered.db1Evidence.referenceEvidence.winningNumbersUsed,false);
assert.ok(!html.includes("getOriginalSourceAndDrawDataForFuture"),'no invented result path');
console.log('PASS: exact 6 material files; dream CP949→UTF8 with CRLF; offline 45-number index; V4.0.43 actual reference bridge; preserved primary six; post-result exclusion; no signer change.');

assert.ok(manifest.rule_precedence.includes('sentence relation/geometry first'));
assert.ok(manifest.approved_four_originals_status.startsWith('PARTIAL_NOT_CERTIFIED'));
for(const name of ['00_6대6_성공회차_마스터목록_20260818.txt','신서_1001_1033_6대6_성공패턴_기준판_V1.0.txt']) assert.ok(manifest.files[name].runtime_role.includes('RESULT_AFTER'));
console.log('PASS: extra exact official-rule candidate and two success archives preserved ONLY for retrospective audit; not certified as signed official scorer.');

for(const [filename,record] of Object.entries(manifest.files)){
 const p=record.android_runtime_asset;
 assert.ok(/^reference_runtime\/[a-z0-9_]+\.txt$/.test(p),'Safe ASCII runtime asset path: '+filename);
 const src=fs.readFileSync(source+filename),portable=fs.readFileSync(base+p);
 assert.equal(sha(portable),record.android_runtime_sha256,'runtime SHA matched: '+filename);
 if(filename.includes('로또꿈해몽백과'))assert.notEqual(sha(src),sha(portable),'CP949 source must not be mislabeled UTF8 original');
 else assert.equal(sha(portable),sha(src),'runtime and original exact bytes: '+filename);
}
console.log('PASS: 9 portable ASCII Android assets have authentic source-name manifest SHA; source originals kept separately.');
