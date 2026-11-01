// V15 engine routing contract — never changes V13/V3/V7 production routes.
// This module intentionally does NOT claim the V15 integrated engine is installed or verified.
export const V15_ENGINE_POLICY = Object.freeze({
  version: "V15_DEV_20261101",
  officialRelease: false,
  integratedEngineVerified: false,
  integratedEngineId: "UNVERIFIED",
  roomRoutes: Object.freeze({
    captain: "integrated",
    dosa: "integrated",
    navigator: "integrated",
    freedom: "legacy_v3",
  }),
  protection: Object.freeze({
    preservePriorInstalls: true,
    preserveHistoricRecords: true,
    preserveFirstRegistrationLocks: true,
    neverAutowriteOfficialResults: true,
  }),
});
export function resolveV15Engine(room, { integratedReady = false, legacyV3Ready = false } = {}) {
  if (!Object.hasOwn(V15_ENGINE_POLICY.roomRoutes, room)) throw new Error("UNKNOWN_ROOM");
  if (room === "freedom") {
    return legacyV3Ready
      ? { mode: "LEGACY_V3", enabled: true, officialEligible: false }
      : { mode: "LEGACY_V3_UNAVAILABLE", enabled: false, officialEligible: false };
  }
  return integratedReady && V15_ENGINE_POLICY.integratedEngineVerified
    ? { mode: "INTEGRATED", enabled: true, officialEligible: false }
    : { mode: "INTEGRATED_PENDING_VERIFICATION", enabled: false, officialEligible: false };
}
