/* V30 V1-style user-driven update decision logic. No downloads/install from this module. */
(function(root) {
  "use strict";
  function isSha256(s) { return typeof s === "string" && /^[a-f\d]{64}$/i.test(s); }
  function httpsUrl(s) { try { const u = new URL(s); return u.protocol === "https:" && !!u.hostname && !u.username && !u.password; } catch (_) { return false; } }
  function positiveInteger(n) { return Number.isSafeInteger(n) && n > 0; }
  function decide(local, remote) {
    // Caller MUST fetch remote manifest from authenticated release endpoint, enforce HTTPS
    // with Android network stack and validate actual APK signature/hash before install.
    if (!local || !remote || !positiveInteger(local.versionCode) ||
      typeof local.packageName !== "string" || !local.packageName ||
      !positiveInteger(remote.versionCode) || remote.packageName !== local.packageName) {
      return {showPopup:false,kind:"ERROR",reason:"INVALID_MANIFEST_OR_PACKAGE"};
    }
    const release = remote.versionCode > local.versionCode;
    const newNotice = typeof remote.noticeId === "string" && remote.noticeId.length > 0 &&
      remote.noticeId !== (local.lastSeenNoticeId || "");
    if (release && (!httpsUrl(remote.apkUrl) || !isSha256(remote.apkSha256))) {
      return {showPopup:false,kind:"ERROR",reason:"UNVERIFIED_DOWNLOAD_METADATA"};
    }
    if (release) {
      return {showPopup:true,kind:"UPDATE",title:"새 업데이트가 있습니다",
        message:typeof remote.message==="string"?remote.message:"",
        buttonLabel:"업데이트 다운로드",apkUrl:remote.apkUrl,apkSha256:remote.apkSha256,
        targetVersionCode:remote.versionCode,requiresUserInstall:true};
    }
    if (newNotice) {
      return {showPopup:true,kind:"NOTICE",title:"공지사항",message:typeof remote.message==="string"?remote.message:"",
        buttonLabel:"확인",noticeId:remote.noticeId,requiresUserInstall:false};
    }
    return {showPopup:false,kind:"NONE"};
  }
  root.V30V1Update = {decide};
})(typeof window !== "undefined" ? window : globalThis);
