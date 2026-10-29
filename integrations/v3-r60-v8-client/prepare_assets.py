#!/usr/bin/env python3
"""Prepare reviewed asset overrides from the exact R60 APK. Never emits an APK."""
import argparse
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile

R60_SHA = '87ccc77af2a1bd2e5a3b19f31ef84e5e21eccffda717daf2e183c726f9f58026'
ROOT = Path(__file__).resolve().parent

def prepare(apk, destination):
    if hashlib.sha256(apk.read_bytes()).hexdigest() != R60_SHA:
        raise ValueError('R60_ORIGINAL_SHA256_MISMATCH')
    if destination.exists():
        raise ValueError('OUTPUT_MUST_BE_NEW_DIRECTORY')
    with ZipFile(apk) as archive:
        index = archive.read('assets/index.html').decode('utf-8')
        draw = archive.read('assets/engines/nav_v3_r39_official_draw_readonly.js').decode('utf-8')
        old = "locked.centralStatus!=='PENDING_UNSENT'"
        if index.count(old) != 1:
            raise ValueError('SCHEDULE_PRECONDITION_CHANGED')
        # A fast ACK must not make the hourly analysis counter stop after round one.
        index = index.replace(old, "!['PENDING_UNSENT','SENT_VERIFIED'].includes(locked.centralStatus)")
        loader = '''<script src="engines/v3_r60_v8_transfer_adapter.js"></script>
<script id="V3_V8_NATIVE_RECONCILE">
(function(){
  if (!window.V3V8Native) return;
  let busy=false;
  async function reconcile(){
    if(busy)return;busy=true;
    try {
      const rows=await window.V3_R60_V8_TRANSFER.syncPending();
      const status=document.querySelector('#central .warning strong');
      if(status)status.textContent='V8 전송 대기열 확인: '+rows.length+'건 · 접수 여부는 서버 영수증으로 확인';
    } catch(e) { /* No original DB, or unavailable storage: never create/reset it here. */ }
    finally {busy=false;}
  }
  setInterval(reconcile,15000);
  window.addEventListener('online',reconcile);
  document.addEventListener('visibilitychange',()=>{if(!document.hidden)reconcile();});
  reconcile();
})();
</script>'''
        if index.count('</body>') != 1:
            raise ValueError('BODY_PRECONDITION_CHANGED')
        index = index.replace('</body>', loader + '\n</body>')
        old_url = 'https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/v7-confirmed-draw-readonly'
        new_url = 'https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/v8-v3-resultbefore-receive/draw'
        if draw.count(old_url) != 1:
            raise ValueError('DRAW_URL_PRECONDITION_CHANGED')
        draw = draw.replace(old_url, new_url).replace("source:'V7_DB_CONFIRMED_READ_ONLY'", "source:'V8_MIGRATED_REFERENCE_READ_ONLY'")
        files = {
            'assets/index.html': index.encode(),
            'assets/engines/nav_v3_r39_official_draw_readonly.js': draw.encode(),
            'assets/engines/v3_r60_v8_transfer_adapter.js': (ROOT / 'v3_r60_v8_transfer_adapter.js').read_bytes(),
        }
        destination.mkdir(parents=True)
        for name, data in files.items():
            target = destination / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
        audit = {
            'base_apk_sha256': R60_SHA,
            'base_package': 'com.baekhak.dosa.f6', 'base_version_code': 30193,
            'required_certificate_sha256': 'eedcfd3730f650e55defee1b0c971b77117b241211d73eb02a8fd9982245a48c',
            'output': 'ASSET_OVERRIDES_ONLY_NOT_AN_APK',
            'native_host_integrated': False, 'same_signer_apk_built': False,
            'device_authenticated': False, 'live_upload_verified': False,
            'engine_original_sha256': hashlib.sha256(archive.read('assets/engine_v2.html')).hexdigest(),
            'overrides': {name: hashlib.sha256(data).hexdigest() for name, data in files.items()},
        }
        (destination / 'SOURCE_PREPARATION_AUDIT.json').write_text(json.dumps(audit, indent=2) + '\n')
        return audit

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('r60_apk', type=Path)
    parser.add_argument('output_new_directory', type=Path)
    args = parser.parse_args()
    print(json.dumps(prepare(args.r60_apk, args.output_new_directory), indent=2))
