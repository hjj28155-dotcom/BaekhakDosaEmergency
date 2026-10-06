from pathlib import Path
p=Path("build407/FREEDOM_V402_BUILD_THIS/app/src/freedom/assets/index.html")
s=p.read_text(encoding="utf-8")
marker="</body>"
js=r'''
<script id="allPerpectSuccessFlowFix">
(function(){
  'use strict';
  const FLOW_KEY='ALL_PERPECT_SUCCESS_FLOW_V1';

  function summary(){
    try{
      if(window.AndroidHost&&typeof AndroidHost.getSpecialApprovalSummary==='function'){
        const raw=AndroidHost.getSpecialApprovalSummary();
        return typeof raw==='string'?JSON.parse(raw||'{}'):(raw||{});
      }
    }catch(e){}
    return {};
  }
  function remove(id){const e=document.getElementById(id);if(e)e.remove();}
  function gate(){
    let g=document.getElementById('startupVideoGate');
    if(!g){g=document.createElement('div');g.id='startupVideoGate';document.body.appendChild(g);}
    g.style.cssText='position:fixed;inset:0;z-index:2147483645;background:#00152a;color:#fff;display:flex;align-items:center;justify-content:center;font-family:system-ui,-apple-system,Noto Sans KR,sans-serif';
    document.body.style.overflow='hidden';
    return g;
  }
  function scroll(){
    remove('approvalHardLock'); remove('installGuideOverlay');
    const g=gate();
    g.innerHTML='<div style="position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:12px;background:#00152a"><img src="welcome_scroll_final.png" alt="항행의자유 환영 두루마리" style="display:block;max-width:100%;max-height:86vh;object-fit:contain;border-radius:14px"><button id="apsScrollNext" style="margin-top:10px;min-width:240px;border:1px solid #e2c66d;background:linear-gradient(#8d6b1c,#493407);color:#fff3b0;border-radius:15px;padding:13px 22px;font-size:17px;font-weight:900">두루마리 클릭 · 설치동영상</button></div>';
    document.getElementById('apsScrollNext').onclick=installVideo;
  }
  function installVideo(){
    const g=gate();
    g.innerHTML='<div style="position:absolute;inset:0;background:#000;display:flex;flex-direction:column"><div style="height:54px;display:flex;align-items:center;justify-content:center;color:#ffe07a;font-weight:900">설치동영상</div><video id="apsInstallVideo" controls playsinline preload="auto" style="flex:1;width:100%;object-fit:contain;background:#000" src="install_guide_final.mp4"></video><div id="apsInstallStatus" style="padding:10px;text-align:center">설치동영상을 끝까지 시청해 주세요.</div></div>';
    const v=document.getElementById('apsInstallVideo'); try{v.load();const q=v.play();if(q&&q.catch)q.catch(()=>{});}catch(e){}
    v.addEventListener('ended',approval,{once:true});
  }
  function approval(){
    remove('startupVideoGate'); document.body.style.overflow='';
    try{if(window.showApprovalHardLock)window.showApprovalHardLock();}catch(e){}
    setTimeout(waitForRequest,300);
  }
  function waitForRequest(){
    const q=summary();
    if(q.approved===true){noticeVideo();return;}
    if(q.has_request===true||q.status==='pending'){
      noticeVideo();return;
    }
    setTimeout(waitForRequest,800);
  }
  function noticeVideo(){
    remove('approvalHardLock');
    const g=gate();
    const src='freedom_onboarding_postscroll_20260928.mp4';
    g.innerHTML='<div style="position:absolute;inset:0;background:#000;display:flex;flex-direction:column"><div style="height:54px;display:flex;align-items:center;justify-content:center;color:#ffe07a;font-weight:900">안내동영상</div><video id="apsNoticeVideo" controls playsinline preload="auto" style="flex:1;width:100%;object-fit:contain;background:#000" src="'+src+'"></video><div style="padding:10px;text-align:center">안내동영상 종료 후 승인대기 화면으로 이동합니다.</div></div>';
    const v=document.getElementById('apsNoticeVideo');try{v.load();const z=v.play();if(z&&z.catch)z.catch(()=>{});}catch(e){}
    v.addEventListener('ended',pending,{once:true});
  }
  function pending(){
    const g=gate();
    g.innerHTML='<div style="text-align:center;padding:28px"><div style="font-size:25px;color:#ffe58a;font-weight:900">승인대기</div><div style="margin-top:14px;line-height:1.7">승인요청이 서버에 접수되었습니다.<br>항해사앱에서 승인을 기다리고 있습니다.</div><div id="apsPendingState" style="margin-top:18px;color:#8fe8ff">승인 상태 확인 중…</div></div>';
    poll();
  }
  function poll(){
    const q=summary();
    if(q.approved===true){finish();return;}
    try{if(window.AndroidHost&&typeof AndroidHost.checkSpecialApproval==='function')AndroidHost.checkSpecialApproval();}catch(e){}
    setTimeout(poll,1500);
  }
  function finish(){
    remove('startupVideoGate');remove('approvalHardLock');
    document.body.style.overflow='';
    try{localStorage.setItem(FLOW_KEY,'1')}catch(e){}
    try{if(window.unlockApprovedApp)window.unlockApprovedApp();}catch(e){}
  }
  function boot(){
    const q=summary();
    let done=false;try{done=localStorage.getItem(FLOW_KEY)==='1'}catch(e){}
    if(q.approved===true&&done){remove('startupVideoGate');remove('approvalHardLock');document.body.style.overflow='';try{if(window.unlockApprovedApp)window.unlockApprovedApp();}catch(e){};return;}
    scroll();
  }
  window.__ALL_PERPECT_SUCCESS_FLOW__={boot,scroll,installVideo,approval,noticeVideo,pending,finish};
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',()=>setTimeout(boot,80),{once:true});else setTimeout(boot,80);
})();
</script>
'''
if "allPerpectSuccessFlowFix" not in s:
    s=s.replace(marker,js+"\n"+marker)
p.write_text(s,encoding="utf-8")
print("ALL_PERPECT_SUCCESS flow patch applied")
