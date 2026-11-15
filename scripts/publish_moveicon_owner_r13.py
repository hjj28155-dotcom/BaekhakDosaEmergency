#!/usr/bin/env python3
"""Only run after verifying the publicly downloadable exact R13 APK."""
import json
from pathlib import Path
from html import escape

url = "https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/releases/download/moveicon-owner-r13-v1.1.12-645-final/MoveIcon_OWNER_R13_1.1.12_645_FINAL_SIGNED.apk"
sha = "b85756a5911047d204e0b0e9b2b9bf2e538d6595e32d416fa29f666512fd164b"
cert = "2716bfb38ed089c8a039d73be92db5000ddaf85b70f0e5d8689bd537da303b3e"
manifest = Path("moveicon-owner-release.json")
page = Path("moveicon-download.html")
old = json.loads(manifest.read_text())
assert old["channel"] == "owner" and old["package"] == "com.navigator.freedom.moveicon.owner"
assert old["ownerCertificateSha256"] == cert
assert (old["versionCode"],old["sha256"]) in [
    (1100012,"cdf437358e72a049a12f046a7b8fa459641479898e6d74e6e28ea79d14f87ce2"),
    (1100013,sha)], "unexpected or newer release: preserve it"
s = page.read_text()
if old["versionCode"] == 1100013:
    assert 'id="moveicon-owner-r13"' in s and url in s
    raise SystemExit(0)
start = s.index('<section id="moveicon-owner-r12"')
end = s.index("</section>",start)+len("</section>")
section = s[start:end]
old_primary=escape(old["apkUrl"],quote=True)
assert section.count(old_primary)==1, "unexpected primary link"
section=section.replace(old_primary,url).replace(old["fallbackUrl"],url)
section=section.replace('moveicon-owner-r12','moveicon-owner-r13')
section=section.replace('R12','R13').replace('1.1.11','1.1.12')
section=section.replace('MoveIcon_OWNER_R13_1.1.12_SIGNED.apk','MoveIcon_OWNER_R13_1.1.12_645_FINAL_SIGNED.apk')
section=section.replace('자동 버전 확인판','645 FINAL 내장판')
section=section.replace('48.76 MB','148.79 MB').replace('2026-11-15','2026-11-16')
section=section.replace('645 앱은 별도 설치 앱입니다. 현재 645 설치 링크는 차단 상태입니다.','645 FINAL R678이 내장되어 있으며 별도 645 앱 없이 실행할 수 있도록 제작했습니다. 실제 Android 기기 검증은 진행 전입니다.')
section=section.replace('구글 드라이브에서 APK를 다운로드한 뒤','공식 GitHub에서 APK를 다운로드한 뒤')
section=section.replace(' Google 드라이브의 파일 검사 안내가 뜨면 내용을 확인하고 다운로드를 계속하세요.','')
section=section.replace('드라이브 다운로드 오류가 발생할 경우','공식 APK 직접 다운로드')
section=section.replace('드라이브와 SHA-256이 동일한 정식 서명 파일입니다.','검증된 정식 OWNER 서명 파일입니다.')
assert 'R12' not in section and '1.1.11' not in section
assert 'data-latest="1.1.12"' in section and url in section
updated=s[:start]+section+s[end:]
j=dict(old,versionName="1.1.12-OWNER",versionCode=1100013,release="R13",apkUrl=url,sha256=sha,sizeBytes=148787966,ownerCertificateSha256=cert,deviceTested=False,fallbackUrl=url)
page.write_text(updated,encoding="utf-8")
manifest.write_text(json.dumps(j,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print("OWNER R13 metadata and download page ready; other channels unchanged.")
