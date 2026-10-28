/* V439: Web ①두루마리 ②설치동영상 ③설치링크, App ④승인요청 ⑤승인확인 ⑥안내동영상 ⑦홈화면. */
(function(){
'use strict';
const SCROLL='scroll',INSTALL='installVideo',APPROVAL='approval',GUIDE='guideVideo',HOME='home';
const DONE='freedom_v439_guide_video_finished';
const INSTALL_DONE='freedom_r14_install_video_finished';
const INTRO_INSTALL='install_guide_final.mp4';
const INTRO_GUIDE='freedom_onboarding_postscroll_20260928.mp4';
const HUB='https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/hanghae-freedom-distribution.html';
const GATE='freedomR14FullScreen';
const UPDATE_DRIVE_FOLDER='https://drive.google.com/drive/folders/1VI4a3RZfBdNxfs6unkZzEIaeI4a4bnMY';
let stage='init',approvedOnce=false,approvalCheckRequested=false;
const native=()=>{try{return window.AndroidHost||window.BaekhakNative||null}catch(e){return null}};
function approvalState(){
 try{
  const h=native();
  if(h&&typeof h.getSpecialApprovalSummary==='function'){
   const q=h.getSpecialApprovalSummary();
   const obj=typeof q==='string'?JSON.parse(q||'{}'):q;
   return obj&&typeof obj==='object'?obj:{};
  }
 }catch(e){}
 return {};
}
function isApproved(q){
 if(!!q&&(q.approved===true||q.status==='approved'))return true;
 try{const h=native();if(h&&typeof h.isSpecialApproved==='function')return h.isSpecialApproved()===true}catch(e){}
 return false;
}
function marked(k){try{return localStorage.getItem(k)==='1'}catch(e){return false}}
function mark(k){try{localStorage.setItem(k,'1')}catch(e){}}
function clean(){
 [GATE,'startupVideoGate','approvalHardLock','installGuideOverlay','r12Gate','r13Gate'].forEach(id=>{
  const e=document.getElementById(id);if(e)e.remove();
 });
 document.body.style.overflow='';
 document.documentElement.style.overflowY='auto';
}
function panel(){
 clean();
 const o=document.createElement('section');
 o.id=GATE;
 o.style.cssText='position:fixed;inset:0;z-index:2147483647;overflow-y:auto;background:#00152a;color:white;font-family:system-ui,-apple-system,Noto Sans KR,sans-serif';
 document.body.appendChild(o);
 document.body.style.overflow='hidden';
 return o;
}
function goHome(){
 if(stage===HOME)return;
 if(!approvedOnce&&!isApproved(approvalState())){showApproval();return}
 if(!marked(DONE)){showGuide();return}
 stage=HOME;clean();window.__onboardingSequenceActive=false;
 try{if(window.unlockApprovedApp)window.unlockApprovedApp();
 else if(native()&&native().startApprovedRuntime)native().startApprovedRuntime()}catch(e){}
 try{if(typeof refreshHomeUI==='function')refreshHomeUI()}catch(e){}
 setTimeout(checkHomeUpdate,1200);
}
function showVideo(type){
 if(stage===HOME)return;
 const pre=type===INSTALL;
 if(stage===(pre?INSTALL:GUIDE))return;
 if(!pre&&!approvedOnce&&!isApproved(approvalState())){showApproval();return}
 stage=pre?INSTALL:GUIDE;
 if(!pre)approvedOnce=true;
 const g=panel();
 const videoSrc=pre?INTRO_INSTALL:INTRO_GUIDE;
 const heading=pre?'설치동영상':'승인완료 · 안내동영상';
 const message=pre?'최초 미승인 사용자만 승인요청 화면으로 이동합니다.':'안내동영상이 끝나면 홈화면으로 자동 이동합니다.';
 g.innerHTML='<div style="position:absolute;inset:0;display:flex;flex-direction:column;background:linear-gradient(180deg,#07233e,#0c5675 48%,#02192f)"><div style="padding:12px;text-align:center;color:#ffe49e;font-size:17px;font-weight:900">'+heading+'</div><video id="freedomR14Video" controls playsinline webkit-playsinline preload="metadata" style="display:block;flex:1;min-height:0;width:100%;object-fit:contain;background:#000"></video><button id="freedomR14Play" type="button" style="margin:10px;padding:14px;border:1px solid #d9bc65;border-radius:12px;background:#75551b;color:#fff;font-weight:900">▶ 동영상 재생</button><div id="freedomR14Notice" style="text-align:center;padding:0 12px 14px;color:#d1e5f4;font-size:13px">'+message+'</div></div>';
 const v=g.querySelector('#freedomR14Video');
 const messageBox=g.querySelector('#freedomR14Notice');
 v.src=videoSrc;
 const play=()=>{try{let p=v.play();if(p&&p.catch)p.catch(()=>{messageBox.textContent='동영상 재생 버튼을 눌러주세요.'})}catch(e){messageBox.textContent='재생 버튼을 눌러 다시 시도해 주세요.'}};
 g.querySelector('#freedomR14Play').onclick=play;
 v.addEventListener('ended',()=>{
  if(pre){mark(INSTALL_DONE);showApproval()}
  else {mark(DONE);goHome()}
 },{once:true});
 v.addEventListener('error',()=>{messageBox.textContent='동영상 파일 재생에 실패했습니다. 인터넷 연결 및 파일을 확인해 주세요.'});
 play();
}
function showScroll(){
 if(stage===HOME||stage===GUIDE)return;
 stage=SCROLL;const g=panel();
 g.innerHTML='<div style="max-width:720px;margin:0 auto;min-height:100%;padding:16px;display:flex;flex-direction:column;align-items:center;justify-content:center"><img src="welcome_scroll_final.png" alt="환영 두루마리" style="max-width:100%;max-height:77vh;object-fit:contain;border-radius:14px"><button id="freedomR14Start" style="width:100%;padding:18px;margin-top:14px;background:#735514;color:white;border:1px solid #e6c366;border-radius:12px;font-size:17px;font-weight:900">다음 · 설치동영상</button></div>';
 g.querySelector('#freedomR14Start').onclick=()=>showVideo(INSTALL);
}
function showApproval(){
 if(stage===HOME||stage===GUIDE)return;
 const q=approvalState();
 if(isApproved(q)){approvedOnce=true;showVideo(GUIDE);return}
 stage=APPROVAL;
 approvalCheckRequested=false;
 const pending=q.status==='pending'||q.has_request===true;
 const g=panel();
 g.innerHTML='<div style="max-width:620px;margin:8vh auto;padding:20px"><div style="border:1px solid #d9bc65;border-radius:18px;background:#082b45;padding:20px"><h2 style="text-align:center;color:#ffe29a">⚓ 항행의자유 설치 승인</h2><p style="line-height:1.6">설치승인을 받으면 안내동영상이 나오고, 동영상이 끝나면 홈화면으로 이동합니다.</p><input id="freedomR14Name" placeholder="본인 실명" style="width:100%;padding:13px;margin:8px 0;color:white;background:#051b2c;border:1px solid #6599b7;border-radius:10px"><input id="freedomR14Ref" placeholder="누구의 지인인지" style="width:100%;padding:13px;margin:8px 0;color:white;background:#051b2c;border:1px solid #6599b7;border-radius:10px"><div style="display:flex;gap:8px"><button id="freedomR14Request" style="flex:1;padding:14px;border-radius:11px;background:#765713;color:#fff;border:1px solid #d6b75e">승인요청</button><button id="freedomR14Check" style="flex:1;padding:14px;border-radius:11px;background:#115175;color:#fff;border:1px solid #84bdda">승인 확인</button></div><p id="freedomR14Status" style="text-align:center;color:#91ecc9">'+(pending?'승인대기 · 승인완료 후 승인 확인을 눌러주세요.':'승인요청을 진행해 주세요.')+'</p></div></div>';
 const message=g.querySelector('#freedomR14Status');
 const requestBtn=g.querySelector('#freedomR14Request');
 // 기존 승인요청이 있으면 중복 신청을 차단하고 기존 건만 조회.
 if(pending){
  requestBtn.disabled=true;
  requestBtn.textContent='기존 승인 확인 중';
  requestBtn.style.opacity='0.55';
  g.querySelector('#freedomR14Name').style.display='none';
  g.querySelector('#freedomR14Ref').style.display='none';
  message.textContent='기존 승인기록을 확인 중입니다. 새 승인요청은 하지 마세요.';
 }
 requestBtn.onclick=()=>{
  const n=g.querySelector('#freedomR14Name').value.trim(),ref=g.querySelector('#freedomR14Ref').value.trim();
  const existing=approvalState();
  if(isApproved(existing)){approvedOnce=true;showVideo(GUIDE);return}
  if(existing.has_request===true||existing.status==='pending'){
   message.textContent='이미 승인요청이 접수되어 있습니다. 승인 확인을 눌러주세요.';return
  }
  if(!n||!ref){message.textContent='실명과 지인 정보를 모두 입력해 주세요.';return}
  const h=native();
  if(!h||typeof h.requestSpecialApproval!=='function'){message.textContent='승인요청 기능을 연결하지 못했습니다.';return}
  message.textContent='승인요청을 보내는 중입니다.';
  try{h.requestSpecialApproval(1244,n,ref)}catch(e){message.textContent='승인 서버 연결을 확인해 주세요.'}
 };
 g.querySelector('#freedomR14Check').onclick=()=>{
  const q=approvalState();
  if(isApproved(q)){approvedOnce=true;showVideo(GUIDE);return}
  approvalCheckRequested=true;
  const h=native();
  message.textContent='승인 상태를 확인하는 중입니다.';
  try{if(h&&h.checkSpecialApproval)h.checkSpecialApproval()}catch(e){message.textContent='승인 상태 확인에 실패했습니다.'}
 };
}
function boot(){
 if(stage!== 'init')return;
 const q=approvalState();
 if(isApproved(q)){
  approvedOnce=true;
  if(marked(DONE))goHome();else showVideo(GUIDE);
 }else if(q.has_request===true||q.status==='pending'){
  showApproval();
  try{const h=native();if(h&&h.checkSpecialApproval)h.checkSpecialApproval()}catch(e){}
 }else {
  // ①두루마리 ②설치동영상 ③APK 설치는 공식 웹 설치 안내 1~3단계에서 이미 수행됨.
  // Android 앱 실행은 반드시 ④승인요청·⑤승인확인부터 시작한다.
  showApproval();
 }
}
window.startFreedomIntroVideo=function(){};
window.beginApprovedOnboardingSequence=function(){};
window.showApprovalHardLock=showApproval;
window.showFinalScroll=function(){if(stage==='init')boot();};
window.onSpecialApprovalState=function(payload){
 let q={};
 try{q=typeof payload==='string'?JSON.parse(payload||'{}'):(payload||{})}catch(e){}
 if((isApproved(q)&&q.round_policy_free!==true)||isApproved(approvalState())){
  // 서버 또는 보존된 기기 승인 완료가 확인되면 추가 승인요청·수동 확인 없이 진행한다.
  approvedOnce=true;
  try{SPECIAL_APPROVED=true;D.specialApprovalUnlocked=true;save()}catch(e){}
  if(marked(DONE))goHome();else showVideo(GUIDE);
  return;
 }
 if(stage===APPROVAL){
  const msg=document.getElementById('freedomR14Status');
  if(!msg)return;
  if(q.status==='rejected')msg.textContent='승인이 거절되었습니다. 항해사에게 확인해 주세요.';
  else if(q.error||q.ok===false)msg.textContent='승인 서버 연결 또는 상태 확인 오류입니다.';
  else if(q.status==='pending'||q.has_request===true)msg.textContent='승인대기 · 승인완료 후 승인 확인을 눌러주세요.';
  else msg.textContent='승인완료 전입니다. 승인 확인을 눌러주세요.';
 }
};
function checkFreedomInstalledVersion(){
 const msg=document.getElementById('freedomVersionMessage');
 const details=document.getElementById('freedomVersionDetails');
 const dl=document.getElementById('freedomVersionDownload');
 if(!msg||!details||!dl)return;
 msg.textContent='현재 설치된 APK 버전을 확인하고 있습니다…';
 details.textContent='';
 dl.hidden=false;dl.textContent='드라이브 업데이트 파일 열기';
 try{
  const h=native();
  if(!h||typeof h.checkLatestFreedomVersion!=='function'){
   msg.textContent='앱 버전 확인 기능을 이용할 수 없습니다.';
   details.textContent='최신 여부를 확인할 수 없으므로 최신이라고 표시하지 않습니다.';
   return;
  }
  h.checkLatestFreedomVersion();
 }catch(e){
  msg.textContent='버전 확인에 실패했습니다. 인터넷 연결을 확인한 뒤 다시 시도하세요.';
 }
}
// V4.0.50: home notification uses the same verified official release callback.
let homeUpdatePending=null,homeUpdateBusy=false,homeUpdateLastCheck=0,verifiedDriveUpdateUrl='';
const homeUpdateShown=new Set();
function validHomeUpdate(q){
 if(!q||typeof q!=='object')return false;
 const installed=Number(q.installed_code),latest=Number(q.latest_code);
 return !!q&&q.status==='update'&&Number.isSafeInteger(installed)&&installed>0&&
 Number.isSafeInteger(latest)&&latest>=40041&&latest<=200000&&latest>installed;
}
function homeIsVisible(){
 if(stage!==HOME||window.__onboardingSequenceActive||document.hidden)return false;
 for(const id of ['modal','freedomR14FullScreen','approvalHardLock','fanfareOverlay','comfortOverlay','installGuideOverlay']){
  const e=document.getElementById(id);
  if(e&&window.getComputedStyle(e).display!=='none')return false;
 }
 return true;
}
function openFreedomUpdate(){
 try{const h=native();if(h&&h.openOfficialUpdateLink){h.openOfficialUpdateLink();return}const url=verifiedDriveUpdateUrl||UPDATE_DRIVE_FOLDER;if(h&&h.openRadioPage){h.openRadioPage(url);return}window.open(url,'_blank')}
 catch(e){alert('공식 업데이트 링크를 열지 못했습니다.')}
}
function showHomeUpdate(){
 const q=homeUpdatePending;
 if(!q||!validHomeUpdate(q)||homeUpdateShown.has(Number(q.latest_code))||!homeIsVisible()||document.getElementById('freedomHomeUpdatePopup'))return;
 const o=document.createElement('section');o.id='freedomHomeUpdatePopup';
 o.setAttribute('role','dialog');o.setAttribute('aria-modal','true');o.setAttribute('aria-labelledby','freedomHomeUpdateTitle');
 o.style.cssText='position:fixed;inset:0;z-index:2147483647;display:flex;align-items:center;justify-content:center;padding:18px;background:rgba(0,12,25,.78);font-family:system-ui,sans-serif';
 o.innerHTML='<div style="width:100%;max-width:390px;padding:22px;border:1px solid #e4bf58;border-radius:18px;background:#082c47;color:white;box-shadow:0 12px 45px #0008"><h2 id="freedomHomeUpdateTitle" style="margin:0 0 14px;color:#ffe28a;font-size:21px">새 업데이트가 있습니다</h2><p id="freedomHomeUpdateVersion" style="line-height:1.6"></p><p style="line-height:1.6;font-size:14px">기존 앱을 삭제하지 않고 업데이트하세요.</p><div style="display:flex;gap:9px;margin-top:18px"><button type="button" id="freedomHomeUpdateLater" style="flex:1;padding:13px;border:1px solid #7ca4bb;border-radius:10px;background:#143d57;color:white">나중에</button><button type="button" id="freedomHomeUpdateNow" style="flex:1;padding:13px;border:1px solid #edcf77;border-radius:10px;background:#795b1a;color:white;font-weight:800">업데이트</button></div></div>';
 document.body.appendChild(o);
 o.querySelector('#freedomHomeUpdateVersion').textContent='현재 '+String(q.installed_name||q.installed_code)+' → 새 버전 '+String(q.latest_name||q.latest_code);
 homeUpdateShown.add(Number(q.latest_code));
 const previous=document.activeElement;
 function close(){o.remove();if(previous&&typeof previous.focus==='function')previous.focus()}
 const later=o.querySelector('#freedomHomeUpdateLater'),now=o.querySelector('#freedomHomeUpdateNow');
 later.onclick=close;now.onclick=()=>{close();openFreedomUpdate()};
 o.addEventListener('keydown',e=>{if(e.key==='Escape'){e.preventDefault();close()}else if(e.key==='Tab'){e.preventDefault();(document.activeElement===later?now:later).focus()}});
 if(typeof now.focus==='function')now.focus();
}
function checkHomeUpdate(){
 showHomeUpdate();
 if(stage!==HOME||document.hidden||homeUpdateBusy)return;
 const h=native();if(!h||typeof h.checkLatestFreedomVersion!=='function')return;
 const now=Date.now();if(homeUpdateLastCheck&&now-homeUpdateLastCheck<300000)return;
 homeUpdateLastCheck=now;homeUpdateBusy=true;
 try{h.checkLatestFreedomVersion();setTimeout(()=>{homeUpdateBusy=false},30000)}catch(e){homeUpdateBusy=false}
}
window.onFreedomVersionStatus=function(payload){
 let q={};try{q=typeof payload==='string'?JSON.parse(payload||'{}'):(payload||{})}catch(e){}
 homeUpdateBusy=false;
 if(!q||typeof q!=='object')q={};
 verifiedDriveUpdateUrl=(Number(q.latest_code)>=Number(q.installed_code)&&/^https:\/\/drive[.]google[.]com\/file\/d\/[A-Za-z0-9_-]+(?:\/|$)/.test(String(q.apk_url||'')))?String(q.apk_url):'';
 homeUpdatePending=validHomeUpdate(q)?q:null;
 showHomeUpdate();
 const msg=document.getElementById('freedomVersionMessage');
 const details=document.getElementById('freedomVersionDetails');
 const dl=document.getElementById('freedomVersionDownload');
 if(!msg||!details||!dl)return;
 dl.hidden=false;dl.textContent='드라이브 업데이트 파일 열기';
 const installed=Number(q.installed_code)||0,latest=Number(q.latest_code)||0;
 if(q.status==='latest'&&installed>=latest&&latest>=40041){
  msg.textContent='✅ 최신 APK가 설치되어 있습니다. 업데이트할 필요가 없습니다.';
  details.textContent='설치된 버전: '+String(q.installed_name||'')+' / 버전코드 '+installed+' · 공식 배포: '+latest;
 }else if(q.status==='update'&&installed>0&&latest>installed){
  msg.textContent='⬇️ 새로운 항행의자유 APK 업데이트가 있습니다.';
  details.textContent='설치된 버전코드 '+installed+' → 최신 '+latest+'. 기존 앱은 삭제하지 말고 업데이트 설치하세요.';
  dl.hidden=false;
 }else{
  msg.textContent='버전 확인이 되지 않았습니다.';
  details.textContent='인터넷 연결을 확인하고 다시 확인하세요. 확인 전에는 최신이라고 단정할 수 없습니다.';
 }
};
window.updatePage=function(){
 modal('항행의자유 업데이트','<div class="panel goldline"><h3>설치 및 업데이트 버전 확인</h3><p id="freedomVersionMessage" role="status">현재 설치 버전 확인 중…</p><p class="small" id="freedomVersionDetails"></p><button class="b green" id="freedomVersionRetry" style="width:100%">버전 다시 확인</button><button class="b gold" id="freedomVersionDownload" style="width:100%">드라이브 업데이트 파일 열기</button></div>');
 const retry=document.getElementById('freedomVersionRetry');
 const dl=document.getElementById('freedomVersionDownload');
 if(retry)retry.onclick=checkFreedomInstalledVersion;
 if(dl)dl.onclick=openFreedomUpdate;
 checkFreedomInstalledVersion();
};
if(window.addEventListener)window.addEventListener('focus',checkHomeUpdate);
if(document.addEventListener)document.addEventListener('visibilitychange',checkHomeUpdate);
if(window.setInterval)window.setInterval(checkHomeUpdate,15000);
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',boot,{once:true});
else boot();
})();

