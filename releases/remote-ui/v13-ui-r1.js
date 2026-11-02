/* V13 R8.74 first-party remote presentation bundle revision 1.
   Does not change number analysis, official draw, score calculations or saved ledger. */
(function () {
  'use strict';
  if (!window.V13RemoteUIHooks || typeof window.V13RemoteUIHooks !== 'object')
    throw new Error('V13_REMOTE_HOOK_CONTRACT_MISSING');
  window.V13RemoteUIHooks.afterRender = function (context) {
    if (!context || context.view !== 6 || !context.stage) return;
    const official = context.stage.querySelector('#v13ScoreOfficialOnly');
    if (!official || official.querySelector('.v13RemoteOfficialHint')) return;
    const message = document.createElement('p');
    message.className = 'v13RemoteOfficialHint';
    message.textContent = '공식 당첨번호는 조합 수신과 별도로 조회합니다. 미수신 조합의 등수 계산은 보류합니다.';
    official.appendChild(message);
  };
  let style = document.getElementById('v13RemoteBundleStyle');
  if (!style) {
    style = document.createElement('style');
    style.id = 'v13RemoteBundleStyle';
    document.head.appendChild(style);
  }
  style.textContent = '.v13RemoteOfficialHint{margin:8px 0 2px;padding:7px 9px;border:1px solid #c49a58;border-radius:9px;color:#ffe1a9;font-size:clamp(12px,2.5vw,15px);text-align:center}';
  window.V13RemoteBundleRevision = 1;
})();
