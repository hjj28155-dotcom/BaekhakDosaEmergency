// V8 standalone V3 result-before receiver. Does NOT write any V7 table.
// Deploy with verify_jwt=false: authorization is the existing per-device 64-hex bearer.
// No public write/read access to the receipt table; all DB calls run server-side.
import { createClient } from "npm:@supabase/supabase-js@2.57.4";
const db = createClient(Deno.env.get("SUPABASE_URL") || "", Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "", {
  auth: { persistSession: false, autoRefreshToken: false }
});
const H = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization,content-type",
  "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
  "Content-Type": "application/json; charset=utf-8",
  "Cache-Control": "no-store",
  "X-Content-Type-Options": "nosniff"
};
const reply = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status, headers: H });
const HEX64 = /^[a-f0-9]{64}$/;
const UUID = /^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i;
const EPOCH = "V3_RESTART_R53_20261028";
const SCHEMA = "V7_V3_NAVIGATOR_20_RESULT_BEFORE_HANDOFF_V1"; // Legacy R60 payload marker, not a V7 destination.
const ROOM = "extra_navigator";
const ROOM_IDS = ["DOSA_RAW5", "NAVIGATOR_RAW5", "AUTO1_CAPTAIN_RAW5", "AUTO2_INDEPENDENT5"];
const CLASSIFICATIONS = new Set([
  "DOSA_ENGINE", "NAVIGATOR_DIRECT_LOCKED", "NAVIGATOR_APP_RESEARCH",
  "CAPTAIN_REPLACEMENT_RAW_ANALYSIS", "CAPTAIN_MASTER_V2_U10_LOCAL_VERIFIED",
  "CAPTAIN_MASTER_V2_R46_EVIDENCE_PROMOTION_LOCAL", "CAPTAIN_MASTER_V2_R59_DERIVED_RESEARCH",
  "INDEPENDENT_AUTO"
]);
const FORBIDDEN = new Set(["winningNumbers", "bonusNumber", "hitCounts", "grade", "resultAfter", "winningNumbersUsed", "bonusNumbersUsed", "actualDraw", "drawResult"]);
const MAX_FILE = 300000;
const good = (s: unknown) => typeof s === "string" && HEX64.test(s);
const obj = (x: unknown): x is Record<string, any> => x !== null && typeof x === "object" && !Array.isArray(x);
const validRound = (x: unknown) => Number.isInteger(x) && Number(x) >= 1 && Number(x) <= 9999;
const hex = (a: Uint8Array) => Array.from(a, x => x.toString(16).padStart(2, "0")).join("");
async function digest(data: Uint8Array | string) {
  const bytes = typeof data === "string" ? new TextEncoder().encode(data) : data;
  return hex(new Uint8Array(await crypto.subtle.digest("SHA-256", bytes)));
}
async function authenticate(req: Request) {
  const value = req.headers.get("Authorization") || "";
  if (!/^Bearer [a-f0-9]{64}$/.test(value)) return null;
  const tokenHash = await digest(value.slice(7));
  const { data, error } = await db.from("v4_sync_devices").select("id")
    .eq("token_hash", tokenHash).is("revoked_at", null).maybeSingle();
  return error ? null : data;
}
function hasForbiddenFields(x: unknown, depth = 0): boolean {
  if (depth > 18) return true;
  if (Array.isArray(x)) return x.some(v => hasForbiddenFields(v, depth + 1));
  if (!obj(x)) return false;
  return Object.entries(x).some(([k, v]) => FORBIDDEN.has(k) || hasForbiddenFields(v, depth + 1));
}
function validatePayload(x: unknown, round: number) {
  if (!obj(x) || x.schema !== SCHEMA || x.originApp !== "V3_NAVIGATOR" ||
      x.epoch !== EPOCH || x.round !== round) return "SCHEMA_EPOCH_ROUND";
  if (!obj(x.destination) || x.destination.room_key !== ROOM ||
      x.destination.folder_key !== "result_before") return "DESTINATION_INVALID";
  if (!good(x.raw6Sha256) || !good(x.rulesSha256)) return "SOURCE_HASH_REQUIRED";
  if (!["HISTORICAL_BLIND_PRACTICE", "PROSPECTIVE_PRE_DRAW"].includes(x.analysisMode)) return "ANALYSIS_MODE_REQUIRED";
  if (x.analysisMode === "HISTORICAL_BLIND_PRACTICE" && x.preDrawExternalLockCertified !== false) return "HISTORICAL_NOT_PREDRAW";
  if (x.analysisMode === "PROSPECTIVE_PRE_DRAW" && x.preDrawExternalLockCertified !== true) return "PREDRAW_LOCK_FLAG_REQUIRED";
  if (typeof x.lockedAt !== "string" || !Number.isFinite(Date.parse(x.lockedAt))) return "LOCKED_AT_REQUIRED";
  if (Date.parse(x.lockedAt) > Date.now() + 300000) return "FUTURE_LOCK_INVALID";
  if (!obj(x.blindAudit) || x.blindAudit.targetRound !== round ||
      x.blindAudit.targetResultAndDescendantsExcluded !== true ||
      x.blindAudit.otherRoundSourcesAllowed !== true) return "BLIND_AUDIT_INVALID";
  if (x.totalCombinations !== 20 || !Array.isArray(x.groups) || x.groups.length !== 4) return "FOUR_GROUPS_REQUIRED";
  if (x.requiresServerAuthentication !== true || x.requiresServerReceipt !== true ||
      x.submissionStatus !== "PENDING_UNSENT") return "ORIGINAL_UNSENT_CONTRACT_REQUIRED";
  if (hasForbiddenFields(x)) return "RESULT_AFTER_FIELDS_FORBIDDEN";
  const seen = new Set<string>();
  const engines: string[] = [];
  for (let i = 0; i < 4; i++) {
    const g = x.groups[i];
    if (!obj(g) || g.room !== ROOM_IDS[i] || !Array.isArray(g.games) ||
        g.games.length !== 5 || !good(g.engineSha256) || !good(g.evidenceSha256) ||
        !CLASSIFICATIONS.has(g.classification)) return "GROUP_OR_EVIDENCE_INVALID";
    if (i === 0 && g.classification !== "DOSA_ENGINE") return "DOSA_CLASSIFICATION";
    if (i === 1 && !["NAVIGATOR_DIRECT_LOCKED", "NAVIGATOR_APP_RESEARCH"].includes(g.classification)) return "NAVIGATOR_CLASSIFICATION";
    if (i === 1 && g.classification === "NAVIGATOR_DIRECT_LOCKED" &&
        (!good(g.signedArtifactSha256) || g.humanDirectConfirmed !== true)) return "DIRECT_EVIDENCE_REQUIRED";
    if (i === 1 && g.classification === "NAVIGATOR_APP_RESEARCH" &&
        g.humanDirectConfirmed !== false) return "AUTOMATED_PROVENANCE_REQUIRED";
    if (i === 2 && ![
      "CAPTAIN_REPLACEMENT_RAW_ANALYSIS", "CAPTAIN_MASTER_V2_U10_LOCAL_VERIFIED",
      "CAPTAIN_MASTER_V2_R46_EVIDENCE_PROMOTION_LOCAL", "CAPTAIN_MASTER_V2_R59_DERIVED_RESEARCH"
    ].includes(g.classification)) return "CAPTAIN_CLASSIFICATION";
    if (i === 2 && g.classification.startsWith("CAPTAIN_MASTER_") &&
        (g.captainPackGate !== "PASS" || g.localU10Validated !== true ||
         !Array.isArray(g.captainRankedUniverse) || g.captainRankedUniverse.length < 7 ||
         g.captainRankedUniverse.length > 10)) return "CAPTAIN_U10_PROOF_REQUIRED";
    if (i === 2 && g.classification === "CAPTAIN_MASTER_V2_R59_DERIVED_RESEARCH" &&
        (!g.captainFallbackResearch || !obj(g.captainFallbackAudit) ||
         g.captainFallbackAudit.applied !== true || !Array.isArray(g.captainFallbackAudit.added) ||
         g.captainFallbackAudit.added.length < 1 || x.analysisMode !== "HISTORICAL_BLIND_PRACTICE"))
      return "CAPTAIN_R59_PROOF_REQUIRED";
    if (i === 3 && g.classification !== "INDEPENDENT_AUTO") return "INDEPENDENT_CLASSIFICATION";
    engines.push(g.engineSha256);
    for (const nums of g.games) {
      if (!Array.isArray(nums) || nums.length !== 6 ||
          nums.some((n: unknown) => !Number.isInteger(n) || Number(n) < 1 || Number(n) > 45) ||
          new Set(nums).size !== 6) return "INVALID_SIX_NUMBER_GAME";
      const signature = nums.slice().sort((a: number, b: number) => a - b).join("-");
      if (seen.has(signature)) return "DUPLICATE_ACROSS_ROOMS";
      seen.add(signature);
    }
  }
  if (engines[2] === engines[3]) return "AUTO1_AUTO2_NOT_INDEPENDENT";
  return null;
}
type Asset = { asset_id: string; room_key: string; round_no: number; filename: string;
  bytes: number; sha256: string; source_device: string };
async function verifyAsset(assetId: string, deviceId: string, round: number, expectedHash: string) {
  const { data: a, error } = await db.from("v6_room_assets")
    .select("asset_id,room_key,round_no,filename,bytes,sha256,source_device")
    .eq("asset_id", assetId).is("deleted_at", null).maybeSingle();
  if (error || !a) return { error: "ASSET_NOT_FOUND", status: 404 };
  const asset = a as Asset;
  if (asset.source_device !== deviceId || asset.room_key !== ROOM || asset.round_no !== round)
    return { error: "ASSET_DEVICE_ROOM_ROUND_MISMATCH", status: 403 };
  if (!/^V3_.*_RESULT_BEFORE\.json$/i.test(asset.filename) ||
      asset.bytes < 100 || asset.bytes > MAX_FILE) return { error: "V3_JSON_FILE_REQUIRED", status: 422 };
  if (asset.sha256 !== expectedHash) return { error: "ASSET_SHA256_MISMATCH", status: 409 };
  const { data: chunks, error: ce } = await db.from("v4_library_chunks")
    .select("bucket_name,storage_key,bytes,sha256,chunk_index")
    .eq("asset_id", assetId).order("chunk_index").limit(2);
  if (ce || !chunks || chunks.length !== 1 || chunks[0].chunk_index !== 0 ||
      chunks[0].bucket_name !== "v4-library" || chunks[0].bytes !== asset.bytes ||
      chunks[0].sha256 !== expectedHash) return { error: "ORIGINAL_CHUNK_INTEGRITY", status: 409 };
  const key = String(chunks[0].storage_key);
  const { data: blob, error: de } = await db.storage.from("v4-library").download(key);
  if (de || !blob) return { error: "ORIGINAL_BYTES_MISSING", status: 409 };
  const bytes = new Uint8Array(await blob.arrayBuffer());
  if (bytes.byteLength !== asset.bytes || await digest(bytes) !== expectedHash)
    return { error: "ORIGINAL_BYTES_SHA256_MISMATCH", status: 409 };
  let json: unknown;
  try { json = JSON.parse(new TextDecoder("utf-8", { fatal: true }).decode(bytes)); }
  catch { return { error: "INVALID_UTF8_JSON", status: 422 }; }
  const invalid = validatePayload(json, round);
  if (invalid) return { error: invalid, status: 422 };
  return { asset, storage_key: key, payload: json as Record<string, any> };
}
function receiptBody(row: Record<string, any>, duplicate: boolean) {
  return { ok: true, status: "ACCEPTED_RESEARCH_ARCHIVE", receipt_id: row.id,
    origin_app: row.origin_app, epoch: row.epoch, round: row.round_no,
    asset_id: row.asset_id, payload_sha256: row.payload_sha256,
    accepted_at: row.accepted_at, analysis_mode: row.analysis_mode,
    official_predraw_certified: false, archive: "V8/V3/result_before",
    duplicate, server_verified: true };
}
async function main(req: Request): Promise<Response> {
  if (req.method === "OPTIONS") return new Response(null, { status: 204, headers: H });
  const url = new URL(req.url);
  const action = url.pathname.split("/").filter(Boolean).at(-1) || "";
  if (req.method === "GET" && action === "health")
    return reply({ ok: true, service: "v8-v3-resultbefore-receive", version: "1",
      original_upload_service: "v6-room-library-sync", v7_dependency: false,
      v3_native_auto_sender_bundled: false, official_predraw_certified: false });
  const device = await authenticate(req);
  if (!device) return reply({ ok: false, error: "DEVICE_AUTH_REQUIRED" }, 401);
  if (req.method === "POST" && (action === "receive" || action === "v8-v3-resultbefore-receive")) {
    const raw = await req.text();
    if (raw.length > 4096) return reply({ error: "REQUEST_TOO_LARGE" }, 413);
    let body: any;
    try { body = JSON.parse(raw); } catch { return reply({ error: "BAD_JSON" }, 400); }
    const assetId = String(body?.asset_id || "");
    const hash = String(body?.payload_sha256 || "");
    const round = body?.round;
    if (!UUID.test(assetId) || !good(hash) || !validRound(round) ||
        body?.origin_app !== "V3_NAVIGATOR" || body?.epoch !== EPOCH)
      return reply({ error: "INVALID_RECEIPT_REQUEST" }, 400);
    const verified = await verifyAsset(assetId, device.id, round, hash);
    if ("error" in verified) return reply({ error: verified.error }, verified.status);
    const { data: existing, error: lookupError } = await db.from("v8_v3_resultbefore_receipts")
      .select("*").eq("origin_app", "V3_NAVIGATOR").eq("epoch", EPOCH)
      .eq("round_no", round).maybeSingle();
    if (lookupError) return reply({ error: "RECEIPT_LOOKUP_FAILED" }, 500);
    if (existing) {
      if (existing.asset_id !== assetId || existing.payload_sha256 !== hash ||
          existing.source_device !== device.id) return reply({ error: "IMMUTABLE_ROUND_CONFLICT" }, 409);
      return reply(receiptBody(existing, true));
    }
    const payload = verified.payload as Record<string, any>;
    const row = { origin_app: "V3_NAVIGATOR", epoch: EPOCH, round_no: round,
      asset_id: assetId, source_device: device.id, payload_sha256: hash,
      storage_key: verified.storage_key, analysis_mode: payload.analysisMode,
      locked_at: new Date(payload.lockedAt).toISOString(),
      official_predraw_certified: false, status: "ACCEPTED_RESEARCH_ARCHIVE" };
    const { data: inserted, error: ie } = await db.from("v8_v3_resultbefore_receipts")
      .insert(row).select("*").single();
    if (ie || !inserted) {
      if (ie?.code === "23505") {
        const { data: concurrent } = await db.from("v8_v3_resultbefore_receipts")
          .select("*").eq("origin_app", "V3_NAVIGATOR").eq("epoch", EPOCH)
          .eq("round_no", round).maybeSingle();
        if (concurrent && concurrent.asset_id === assetId &&
            concurrent.payload_sha256 === hash && concurrent.source_device === device.id)
          return reply(receiptBody(concurrent, true));
        return reply({ error: "IMMUTABLE_ROUND_CONFLICT" }, 409);
      }
      return reply({ error: "RECEIPT_INSERT_FAILED" }, 500);
    }
    return reply(receiptBody(inserted, false), 201);
  }
  if (req.method === "GET" && action === "receipt") {
    const round = Number(url.searchParams.get("round"));
    const epoch = url.searchParams.get("epoch");
    if (!validRound(round) || epoch !== EPOCH) return reply({ error: "INVALID_RECEIPT_QUERY" }, 400);
    const { data: row, error } = await db.from("v8_v3_resultbefore_receipts")
      .select("*").eq("origin_app", "V3_NAVIGATOR").eq("epoch", EPOCH)
      .eq("round_no", round).maybeSingle();
    if (error) return reply({ error: "RECEIPT_LOOKUP_FAILED" }, 500);
    if (!row || row.source_device !== device.id) return reply({ error: "RECEIPT_NOT_FOUND" }, 404);
    const verified = await verifyAsset(row.asset_id, device.id, round, row.payload_sha256);
    if ("error" in verified || verified.storage_key !== row.storage_key)
      return reply({ error: "RECEIPT_ORIGINAL_INTEGRITY_FAILED" }, 409);
    return reply(receiptBody(row, true));
  }
  return reply({ error: "NOT_FOUND" }, 404);
}
Deno.serve(req => main(req).catch(() => reply({ error: "INTERNAL_ERROR" }, 500)));
