/* Distribution-only routing. No admin authentication or auto-approval. */
(function () {
  'use strict';
  function showOwnRequest() { return window.specialApprovalPage(); }
  window.approvalPage = showOwnRequest;
  window.captainApprovalPage = showOwnRequest;
  window.loadCaptainApprovalList = showOwnRequest;
  window.refreshCaptainApprovalBadge = function () { return false; };
  window.captainApprovalApi = async function () {
    throw new Error('DISTRIBUTION_HAS_NO_ADMIN_PERMISSION');
  };
  window.captainApprovalDecide = function () {
    window.alert('이 앱은 승인요청용입니다. 다른 사람의 승인·거절은 항해사앱에서 처리합니다.');
    return false;
  };
})();
