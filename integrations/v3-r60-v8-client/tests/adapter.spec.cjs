'use strict';
const {chromium} = require('playwright');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const assert = require('node:assert/strict');
(async () => {
  const server = http.createServer((req, res) => { res.setHeader('Content-Type', 'text/html'); res.end('<!doctype html><title>V3 V8 isolated test</title>'); });
  await new Promise(r => server.listen(0, '127.0.0.1', r));
  let browser;
  try {
    browser = await chromium.launch({headless:true, args:['--no-sandbox']});
    const page = await browser.newPage();
    await page.goto('http://127.0.0.1:' + server.address().port);
    await page.addScriptTag({path:path.join(__dirname, 'r60_outbox_original.js')});
    await page.addScriptTag({path:path.join(__dirname, '../v3_r60_v8_transfer_adapter.js')});
    const results = await page.evaluate(async () => {
      const tests = [];
      const check = (condition, name) => { if (!condition) throw Error(name); tests.push(name); };
      const rejects = async (p, name) => { let failed=false; try { await p; } catch { failed=true; } check(failed,name); };
      window.V3V8Native={enqueue:()=>'{"ok":true}',poll:()=>'{"state":"QUEUED"}'};
      await rejects(V3_R60_V8_TRANSFER.syncRound(1), 'missing database rejected without creation');
      const s = {
        schema:'V3_R30_FOUR_ROOM_20_COMBINATIONS_V1', epoch:NAV_V3_R30_FRESH20.epoch,round:1,
        raw6:Array.from({length:6},(_,i)=>'원문 근거 문장 '+i),raw6Sha256:'a'.repeat(64),rulesSha256:'b'.repeat(64),
        winningNumbersUsed:false,bonusNumbersUsed:false,targetResultDerivedEvidenceUsed:false,targetResultKnownToGenerator:false,
        analysisMode:'HISTORICAL_BLIND_PRACTICE',preDrawExternalLockCertified:false,
        blindAudit:{targetRound:1,targetResultAndDescendantsExcluded:true,otherRoundSourcesAllowed:true},
        totalCombinations:20,lockedAt:new Date().toISOString(),groups:[]
      };
      const roles=['DOSA_RAW5','NAVIGATOR_RAW5','AUTO1_CAPTAIN_RAW5','AUTO2_INDEPENDENT5'];
      const classes=['DOSA_ENGINE','NAVIGATOR_APP_RESEARCH','CAPTAIN_REPLACEMENT_RAW_ANALYSIS','INDEPENDENT_AUTO'];
      for(let i=0;i<4;i++)s.groups.push({room:roles[i],classification:classes[i],humanDirectConfirmed:false,
        engineSha256:String(i+1).repeat(64),evidenceSha256:'e'.repeat(64),
        games:Array.from({length:5},(_,j)=>[1,2,3,4,5,6+i*5+j])});
      const store=NAV_V3_R30_FRESH20.laboratoryAdapter(indexedDB);
      check((await store.create(s)).created,'R60 original creates valid locked fixture');
      const before=JSON.stringify(await store.read(1));
      check((await V3_R60_V8_TRANSFER.syncRound(1)).status==='PENDING_UNSENT','queued is not verified');
      check(JSON.stringify(await store.read(1))===before,'pending transfer leaves original intact');
      let hash, payload;
      const receipt={ok:true,server_verified:true,status:'ACCEPTED_RESEARCH_ARCHIVE',origin_app:'V3_NAVIGATOR',
        epoch:s.epoch,round:1,receipt_id:'11111111-1111-4111-8111-111111111111',asset_id:'22222222-2222-4222-8222-222222222222',
        archive:'V8/V3/result_before',official_predraw_certified:false,analysis_mode:s.analysisMode,accepted_at:new Date().toISOString()};
      V3V8Native.enqueue=(p,h)=>{payload=p;hash=h;return '{"ok":true}';};
      V3V8Native.poll=()=>JSON.stringify({state:'VERIFIED',receipt:{...receipt,payload_sha256:'0'.repeat(64)}});
      await rejects(V3_R60_V8_TRANSFER.syncRound(1),'wrong receipt hash rejected');
      check(JSON.stringify(await store.read(1))===before,'bad ACK leaves first lock and pending payload intact');
      for (const patch of [{round:2},{epoch:'OTHER'},{archive:'V7/result_before'},
          {official_predraw_certified:true},{server_verified:false},{origin_app:'V7'},
          {accepted_at:'invalid'},{receipt_id:'not-a-uuid'},{analysis_mode:'PROSPECTIVE_PRE_DRAW'}]) {
        V3V8Native.poll=()=>JSON.stringify({state:'VERIFIED',receipt:{...receipt,payload_sha256:hash,...patch}});
        await rejects(V3_R60_V8_TRANSFER.syncRound(1),'receipt mismatch rejected '+JSON.stringify(patch));
      }
      V3V8Native.enqueue=()=>'{"ok":false}';
      await rejects(V3_R60_V8_TRANSFER.syncRound(1),'native durable queue failure rejected');
      V3V8Native.enqueue=(p,h)=>{payload=p;hash=h;return '{"ok":true}';};
      V3V8Native.poll=()=>JSON.stringify({state:'VERIFIED',receipt:{...receipt,payload_sha256:hash}});
      const good=await V3_R60_V8_TRANSFER.syncRound(1);
      check(good.status==='SENT_VERIFIED','valid native server GET receipt accepted');
      const after=await store.read(1), b=JSON.parse(before);
      check(JSON.stringify(after.payload)===JSON.stringify(b.payload)&&JSON.stringify(after.outbox)===JSON.stringify(b.outbox),
        'analysis and first report payload preserved byte-for-byte');
      check(payload===JSON.stringify(b.outbox),'uploaded JSON is exact original envelope');
      check((await V3_R60_V8_TRANSFER.syncRound(1)).receipt_id===good.receipt_id,'duplicate ACK idempotent');
      V3V8Native.poll=()=>JSON.stringify({state:'VERIFIED',receipt:{...receipt,payload_sha256:hash,receipt_id:'33333333-3333-4333-8333-333333333333'}});
      await rejects(V3_R60_V8_TRANSFER.syncRound(1),'conflicting second receipt rejected');
      check(!(await store.create(s)).created,'first lock cannot be overwritten');
      check((await V3_R60_V8_TRANSFER.syncPending()).length===0,'verified rows excluded from pending scan');
      // Concurrent tampering is detected in the final atomic transaction.
      const second=structuredClone(s);second.round=2;second.blindAudit.targetRound=2;
      check((await store.create(second)).created,'second isolated fixture created');
      let payload2;
      V3V8Native.enqueue=(p,h)=>{payload2=p;hash=h;return '{"ok":true}';};
      V3V8Native.poll=()=>'{"state":"RETRY"}';
      check((await V3_R60_V8_TRANSFER.syncRound(2)).nativeState==='RETRY','network retry remains pending');
      check((await store.read(2)).centralStatus==='PENDING_UNSENT','retry does not mark sent');
      // Delete native capability: browser alone must not claim transfer completion.
      delete window.V3V8Native;
      await rejects(V3_R60_V8_TRANSFER.syncRound(2),'native capability required');
      return tests;
    });
    assert(results.length>=20);
    console.log(JSON.stringify({passed:results.length,tests:results,scope:'Chromium real IndexedDB + mocked native bridge; no device or live upload'},null,2));
  } finally { if(browser)await browser.close(); await new Promise(r=>server.close(r)); }
})().catch(e=>{console.error(e);process.exitCode=1;});
