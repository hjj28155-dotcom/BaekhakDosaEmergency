import test from 'node:test';
import assert from 'node:assert/strict';
import {resolveV15Engine,V15_ENGINE_POLICY} from '../src/engine-routing.mjs';
test('freedom always routes to existing V3 engine, never integrated',()=>{
  assert.equal(V15_ENGINE_POLICY.roomRoutes.freedom,'legacy_v3');
  assert.equal(resolveV15Engine('freedom',{integratedReady:true,legacyV3Ready:true}).mode,'LEGACY_V3');
});
test('unverified integrated engine cannot be marked ready for any of three rooms',()=>{
  for(const room of ['captain','dosa','navigator'])
    assert.deepEqual(resolveV15Engine(room,{integratedReady:true}),{mode:'INTEGRATED_PENDING_VERIFICATION',enabled:false,officialEligible:false});
});
test('missing legacy engine does not silently fall back to integrated',()=>{
  assert.equal(resolveV15Engine('freedom',{integratedReady:true}).enabled,false);
});
test('official write is protected',()=>{assert.equal(V15_ENGINE_POLICY.protection.neverAutowriteOfficialResults,true)});
test('unknown room rejected',()=>assert.throws(()=>resolveV15Engine('other'),/UNKNOWN_ROOM/));
