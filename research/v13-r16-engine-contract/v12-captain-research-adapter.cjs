/* V13 R16 — legacy V12 captain research adapter. Not a live deployed engine.
 * Requires verified bytes from V12 R9 source:
 * engine_v10.html sha256 b2ff0097896f246f5ca885afb07dcf8841b494ceef1a91497901681504c208bd
 * v10_isolated_captain_bridge.js sha256 b2fbcf6fac2716eb411084ef825b409b6205eca51eade91281ff270ab9d68240
 * Caller MUST independently verify asset digests; this adapter does not do so.
 * Never use as official, never fabricate a fallback, never auto-write a lock.
 */
(function (root, factory) {
  const api = factory();
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  else root.V13_V12_CAPTAIN_RESEARCH_ADAPTER = Object.freeze(api);
})(typeof window !== 'undefined' ? window : globalThis, function () {
  'use strict';
  const BRIDGE_VERSION = 'V10_ISOLATED_R60_SNAPSHOT_BRIDGE_20261029';
  const SOURCE_ID = 'V12_R9_V10_ISOLATED_R60_RESEARCH_ONLY';
  const isSix = a => Array.isArray(a) && a.length === 6 &&
    new Set(a).size === 6 && a.every(n => Number.isInteger(n) && n >= 1 && n <= 45);
  const canonical = a => [...a].sort((a,b) => a-b).join(',');
  function makeAdapter(bridge, {assetsVerified=false}={}) {
    if (!assetsVerified) throw Error('CAPTAIN_SOURCE_HASH_NOT_VERIFIED');
    if (!bridge || bridge.version !== BRIDGE_VERSION || typeof bridge.request !== 'function') throw Error('V12_V10_BRIDGE_MISSING');
    return async function captainLegacyResearch(input) {
      if (!input || input.phase !== 'BEFORE' || !Number.isInteger(input.round) || input.round < 1 || input.round > 1247) throw Error('CAPTAIN_LEGACY_ROUND_UNSUPPORTED');
      if (!Array.isArray(input.raw) || input.raw.length !== 6 || input.raw.some(s => typeof s !== 'string' || !s.trim())) throw Error('CAPTAIN_RAW6_INVALID');
      if (typeof input.rawFingerprint !== 'string' || !input.rawFingerprint) throw Error('CAPTAIN_RAW_FINGERPRINT_REQUIRED');
      const response = await bridge.request(input.round, [...input.raw]);
      if (!response || response.packGate !== 'PASS' || response.packLocked !== true || response.localU10Validated !== true) throw Error('CAPTAIN_LEGACY_GATE_NOT_VERIFIED');
      if (!Array.isArray(response.rankedUniverse) || response.rankedUniverse.length < 7 || response.rankedUniverse.length > 10) throw Error('CAPTAIN_LEGACY_RANK_INVALID');
      const sets = response.games;
      if (!Array.isArray(sets) || sets.length !== 5 || !sets.every(isSix) || new Set(sets.map(canonical)).size !== 5) throw Error('CAPTAIN_LEGACY_FINAL5_INVALID');
      if (response.fallbackResearch === true) throw Error('CAPTAIN_LEGACY_DERIVED_FALLBACK_REJECTED');
      if (sets.some(g => g.some(n => !response.rankedUniverse.includes(n)))) throw Error('CAPTAIN_LEGACY_RANK_MISMATCH');
      return Object.freeze({
        schema:'V13_PREDRAW_ROOM_REPORT_V1', phase:'BEFORE', round:input.round,
        roomKey:'captain', rawFingerprint:input.rawFingerprint,
        sourceId:SOURCE_ID, combinations:sets.map(g => [...g].sort((a,b)=>a-b)),
        researchOnly:true, officialLocked:false, officialEligible:false
      });
    };
  }
  // Registers only for controlled research sessions with independently hash-verified assets.
  function registerResearch(root, {assetsVerified=false}={}) {
    if (!root?.V13_RESULT_BEFORE || !root.V10_ISOLATED_CAPTAIN) throw Error('CAPTAIN_RESEARCH_RUNTIME_MISSING');
    const handler=makeAdapter(root.V10_ISOLATED_CAPTAIN,{assetsVerified});
    root.V13_RESULT_BEFORE.registerEngine('captain',handler);
    return Object.freeze({registered:true,mode:'RESEARCH_ONLY',sourceId:SOURCE_ID});
  }
  return {BRIDGE_VERSION,SOURCE_ID,makeAdapter,registerResearch};
});
