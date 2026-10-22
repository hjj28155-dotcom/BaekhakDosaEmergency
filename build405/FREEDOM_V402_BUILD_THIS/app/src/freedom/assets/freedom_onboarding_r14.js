/* V439: Web ①두루마리 ②설치동영상 ③설치링크, App ④승인요청 ⑤승인확인 ⑥안내동영상 ⑦홈화면. */
(function(){
'use strict';
const SCROLL='scroll',INSTALL='installVideo',APPROVAL='approval',GUIDE='guideVideo',HOME='home';
const DONE='freedom_v439_guide_video_finished';
const INSTALL_DONE='freedom_r14_install_video_finished';
const INTRO_INSTALL='install_guide_final.mp4';
const INTRO_GUIDE='freedom_onboarding_postscroll_20260928.mp4';
const HUB='https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/update.html';
const GATE='freedomR14FullScreen';
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
 const message=pre?'설치동영상이 끝나면 승인요청 화면으로 이동합니다.':'안내동영상이 끝나면 홈화면으로 자동 이동합니다.';
 g.innerHTML='<div style="position:absolute;inset:0;display:flex;flex-direction:column;background:#000"><div style="padding:12px;text-align:center;color:#ffe49e;font-size:17px;font-weight:900">'+heading+'</div><video id="freedomR14Video" controls playsinline webkit-playsinline preload="metadata" style="display:block;flex:1;min-height:0;width:100%;object-fit:contain;background:#000"></video><button id="freedomR14Play" type="button" style="margin:10px;padding:14px;border:1px solid #d9bc65;border-radius:12px;background:#75551b;color:#fff;font-weight:900">▶ 동영상 재생</button><div id="freedomR14Notice" style="text-align:center;padding:0 12px 14px;color:#d1e5f4;font-size:13px">'+message+'</div></div>';
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
  // ①두루마리 ②설치동영상 ③APK 설치는 공식 moveicon.html에서 이미 수행됨.
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
 if(isApproved(q)||isApproved(approvalState())){
  // 신규 설치는 승인확인 버튼을 거친 후 영상으로 진행한다.
  if(stage===APPROVAL&&!approvalCheckRequested){
   const msg=document.getElementById('freedomR14Status');
   if(msg)msg.textContent='승인이 완료됐습니다. 승인 확인 버튼을 눌러주세요.';
   return;
  }
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
window.updatePage=function(){
 modal('항행의자유 업데이트','<div class="panel goldline"><h3>공식 설치 및 업데이트</h3><p>항행의자유 전용 APK 다운로드 페이지로 연결합니다.</p><button class="b gold" id="freedomR14Update" style="width:100%">공식 설치 및 업데이트 링크 열기</button></div>');
 const b=document.getElementById('freedomR14Update');
 if(b)b.onclick=()=>{
  try{const h=native();if(h&&h.openOfficialUpdateLink){h.openOfficialUpdateLink();return}if(h&&h.openRadioPage){h.openRadioPage(HUB);return}window.open(HUB,'_blank')}catch(e){alert('업데이트 링크를 열지 못했습니다.')}
 };
};
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',boot,{once:true});
else boot();
})();