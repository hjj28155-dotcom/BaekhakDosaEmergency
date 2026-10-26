# V4.0.39 post-approval guide fix, research only.
# Do not distribute a changed APK without the original V4.0.39 signing key.
from pathlib import Path
import zipfile,hashlib
BAD_NAME="assets/freedom_onboarding_postscroll_20260928.mp4"
GOOD_NAME="assets/freedom_onboarding_latest.mp4"
CODE_NAME="assets/freedom_onboarding_r14.js"
BAD_SHA256="68801858fe936c7b7eba64fa76833fdaf5e9fdf5d9cf6c338d8b9d001689174e"
GOOD_SHA256="a1649302edae23b8dd2fdc2f21a1bc678630519f8918ee29efa39d5994e86fd5"
SOURCE_APK_SHA256="4661b2d2f59ac462ae48aa85c4b9457d0e5adf7a77680aaac31a440af6be9b83"
def sha256(data): return hashlib.sha256(data).hexdigest()
def prepare_verified_replacement(source_apk,guide_mp4):
    source=Path(source_apk).read_bytes()
    assert sha256(source)==SOURCE_APK_SHA256, "NOT_VERIFIED_V439_APK"
    video=Path(guide_mp4).read_bytes()
    assert sha256(video)==GOOD_SHA256, "WRONG_GUIDE_SOURCE"
    with zipfile.ZipFile(source_apk) as z:
        old=z.read(BAD_NAME)
        assert sha256(old)==BAD_SHA256, "UNEXPECTED_EMBEDDED_GUIDE"
        js=z.read(CODE_NAME).decode("utf-8")
    old_entry="const INTRO_GUIDE='freedom_onboarding_postscroll_20260928.mp4';"
    new_entry="const INTRO_GUIDE='freedom_onboarding_latest.mp4';"
    assert js.count(old_entry)==1, "UNEXPECTED_ONBOARDING_CODE"
    js=js.replace(old_entry,new_entry)
    assert "else {mark(DONE);goHome()}" in js, "GUIDE_TO_HOME_MISSING"
    assert "if(!pre&&!approvedOnce&&!isApproved(approvalState()))" in js, "APPROVAL_GUARD_MISSING"
    return {CODE_NAME:js.encode("utf-8"),GOOD_NAME:video}
# The replacement assets are verified but do not create an installable APK here.
# Production steps require exact original signing identity and an increased versionCode.
# Never delete original approvals/data or advise users to uninstall.
