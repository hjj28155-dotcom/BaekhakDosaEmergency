/* Source integration module; requires a same-signer Android update and V3V8Native.
 * Never creates/deletes the R60 database or changes analysis/lock/payload bytes.
 */
(function (root) {
  'use strict';
  const DATABASE = 'shipmate_v3_r53_fresh20_epoch1';
  const EPOCH = 'V3_RESTART_R53_20261028';
  const UUID = /^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i;
  const roundOK = n => Number.isInteger(n) && n >= 1 && n <= 9999;
  const fail = reason => { throw Error(reason); };
  const json = x => JSON.stringify(x);
  async function digest(s) {
    const hash = await root.crypto.subtle.digest('SHA-256', new TextEncoder().encode(s));
    return Array.from(new Uint8Array(hash), b => b.toString(16).padStart(2, '0')).join('');
  }
  function openExisting() {
    return new Promise((resolve, reject) => {
      const r = root.indexedDB.open(DATABASE); // Do not request a version upgrade.
      r.onupgradeneeded = () => { r.transaction.abort(); reject(Error('R60_DATABASE_MISSING')); };
      r.onerror = () => reject(Error('R60_DATABASE_UNAVAILABLE'));
      r.onblocked = () => reject(Error('R60_DATABASE_BLOCKED'));
      r.onsuccess = () => {
        if (!['analyses', 'roundLocks', 'centralOutbox'].every(n => r.result.objectStoreNames.contains(n))) {
          r.result.close(); reject(Error('R60_STORES_MISSING'));
        } else resolve(r.result);
      };
    });
  }
  function checked(a, l, o) {
    if (!a || !l || !o || a.recordId !== l.recordId || a.recordId !== o.recordId ||
        a.round !== l.round || a.round !== o.round || !roundOK(o.round) ||
        a.epoch !== EPOCH || l.epoch !== EPOCH || o.epoch !== EPOCH || l.locked !== true)
      fail('FIRST_LOCK_INTEGRITY');
    const contract = root.NAV_V3_R30_FRESH20;
    if (!contract || !contract.validate(a.payload).ok ||
        json(contract.envelope(a.payload)) !== json(o.payload)) fail('ORIGINAL_PAYLOAD_MISMATCH');
    if (!['PENDING_UNSENT', 'SENT_VERIFIED'].includes(o.status)) fail('OUTBOX_STATUS_INVALID');
    return o;
  }
  function readRound(db, round) {
    return new Promise((resolve, reject) => {
      const tx = db.transaction(['analyses', 'roundLocks', 'centralOutbox'], 'readonly');
      const a = tx.objectStore('analyses').get(round), l = tx.objectStore('roundLocks').get(round),
        o = tx.objectStore('centralOutbox').get(round);
      tx.onabort = () => reject(Error('READ_ABORTED'));
      tx.oncomplete = () => {
        try { resolve(checked(a.result, l.result, o.result)); } catch (e) { reject(e); }
      };
    });
  }
  function validReceipt(r, o, sha) {
    if (!r || r.ok !== true || r.server_verified !== true ||
        r.status !== 'ACCEPTED_RESEARCH_ARCHIVE' || r.origin_app !== 'V3_NAVIGATOR' ||
        r.epoch !== EPOCH || r.round !== o.round || r.payload_sha256 !== sha ||
        !UUID.test(r.receipt_id || '') || !UUID.test(r.asset_id || '') ||
        r.archive !== 'V8/V3/result_before' || r.official_predraw_certified !== false ||
        r.analysis_mode !== o.payload.analysisMode || typeof r.accepted_at !== 'string' ||
        !Number.isFinite(Date.parse(r.accepted_at)) || Date.parse(r.accepted_at) > Date.now() + 300000)
      fail('SERVER_RECEIPT_MISMATCH');
  }
  function mark(db, original, sha, receipt) {
    return new Promise((resolve, reject) => {
      // Hashing completed before opening this transaction; no async gap inside IDB.
      const tx = db.transaction(['analyses', 'roundLocks', 'centralOutbox'], 'readwrite');
      let a, l, o, error;
      const requests = ['analyses', 'roundLocks', 'centralOutbox'].map(n => tx.objectStore(n).get(original.round));
      let remaining = 3;
      requests.forEach((r, i) => { r.onsuccess = () => {
        if (i === 0) a = r.result; else if (i === 1) l = r.result; else o = r.result;
        if (--remaining) return;
        try {
          checked(a, l, o);
          if (json(o.payload) !== json(original.payload) || o.recordId !== original.recordId)
            fail('OUTBOX_CHANGED_DURING_TRANSFER');
          validReceipt(receipt, o, sha);
          if (o.status === 'SENT_VERIFIED') {
            if (!o.v8Receipt || o.v8Receipt.receipt_id !== receipt.receipt_id ||
                o.v8Receipt.asset_id !== receipt.asset_id || o.v8PayloadSha256 !== sha)
              fail('IMMUTABLE_RECEIPT_CONFLICT');
          } else {
            tx.objectStore('centralOutbox').put(Object.assign({}, o, {
              status: 'SENT_VERIFIED', v8Receipt: receipt, v8PayloadSha256: sha
            }));
          }
        } catch (e) { error = e; tx.abort(); }
      }; });
      tx.onabort = () => reject(error || Error('ACK_TRANSACTION_ABORTED'));
      tx.oncomplete = () => resolve({ round: original.round, status: 'SENT_VERIFIED', receipt_id: receipt.receipt_id });
    });
  }
  const inFlight = new Map();
  async function syncOne(round) {
    if (!roundOK(round)) fail('INVALID_ROUND');
    const native = root.V3V8Native;
    if (!native || typeof native.enqueue !== 'function' || typeof native.poll !== 'function')
      fail('SAME_SIGNER_NATIVE_UPDATE_REQUIRED');
    const db = await openExisting();
    try {
      const o = await readRound(db, round), bytes = json(o.payload), sha = await digest(bytes);
      const queued = JSON.parse(native.enqueue(bytes, sha));
      if (!queued.ok) fail(queued.error || 'NATIVE_QUEUE_REJECTED');
      // No caller-supplied receipt API: poll only the native durable worker result.
      const result = JSON.parse(native.poll(round, sha));
      if (result.state !== 'VERIFIED') return { round, status: o.status, nativeState: result.state };
      validReceipt(result.receipt, o, sha);
      return await mark(db, o, sha, result.receipt);
    } finally { db.close(); }
  }
  function syncRound(round) {
    if (!inFlight.has(round)) {
      const p = syncOne(round).finally(() => inFlight.delete(round));
      inFlight.set(round, p);
    }
    return inFlight.get(round);
  }
  async function syncPending() {
    const db = await openExisting();
    let rows;
    try {
      rows = await new Promise((resolve, reject) => {
        const tx = db.transaction('centralOutbox', 'readonly');
        const req = tx.objectStore('centralOutbox').getAll();
        tx.oncomplete = () => resolve(req.result);
        tx.onabort = () => reject(Error('OUTBOX_LIST_ABORTED'));
      });
    } finally { db.close(); }
    const results = [];
    for (const row of rows.filter(x => x.status === 'PENDING_UNSENT')) {
      try { results.push(await syncRound(row.round)); }
      catch (e) { results.push({ round: row.round, status: 'PENDING_UNSENT', error: e.message }); }
    }
    return results;
  }
  root.V3_R60_V8_TRANSFER = Object.freeze({ version: '0.2.0-source', syncRound, syncPending });
})(typeof window !== 'undefined' ? window : globalThis);
