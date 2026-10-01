from pathlib import Path
import re

ROOT=Path('build405/FREEDOM_V402_BUILD_THIS')
p=ROOT/'app/src/freedom/assets/index.html'
s=p.read_text(encoding='utf-8')

# Force the corrected onboarding flow once for V4.0.6.
s=s.replace("const INTRO_CAMPAIGN_KEY='freedom_intro_campaign_V333_FULL_PLAY';",
            "const INTRO_CAMPAIGN_KEY='freedom_intro_campaign_V406_SCROLL_APPROVAL';")

# Keep the welcome scroll visible behind the approval request window.
s=s.replace(
'#approvalHardLock{position:fixed;inset:0;z-index:2147483646;background:#061827;color:#fff;display:flex;align-items:center;justify-content:center;padding:18px;font-family:system-ui,-apple-system,"Noto Sans KR",sans-serif}',
'#approvalHardLock{position:fixed;inset:0;z-index:2147483646;background:rgba(0,8,18,.38);color:#fff;display:flex;align-items:flex-end;justify-content:center;padding:18px 14px 26px;font-family:system-ui,-apple-system,"Noto Sans KR",sans-serif}'
)
s=s.replace(
'#approvalHardLock .box{width:min(94vw,520px);background:#0b2a42;border:2px solid #d8b950;border-radius:18px;padding:20px 16px;text-align:center;box-shadow:0 16px 60px rgba(0,0,0,.65)}',
'#approvalHardLock .box{width:min(94vw,520px);background:rgba(7,35,57,.96);border:2px solid #d8b950;border-radius:18px;padding:17px 15px;text-align:center;box-shadow:0 16px 60px rgba(0,0,0,.72)}'
)

# First launch: show scroll first, then overlay approval request when approval is not yet granted.
pat=re.compile(r"  function startFirstRunSequence\(\)\{.*?\n  \}\n\n  if\(document\.readyState==='loading'\)\{", re.S)
rep="""  function startFirstRunSequence(){
    hideHomeScroll();
    try{
      let q={};
      if(window.AndroidHost&&AndroidHost.getSpecialApprovalSummary){
        const raw=AndroidHost.getSpecialApprovalSummary();
        q=typeof raw==='string'?JSON.parse(raw||'{}'):(raw||{});
      }
      if(q.approved===true && hasSeenIntro()){
        window.__onboardingSequenceActive=false;
        const old=document.getElementById('startupVideoGate');
        if(old)old.remove();
        document.body.style.overflow='';
        if(window.unlockApprovedApp)window.unlockApprovedApp();
        return;
      }

      // Always show the welcome scroll. If not approved, show approval window over it.
      beginApprovedOnboardingSequence();
      if(q.approved!==true){
        setTimeout(function(){
          try{if(window.showApprovalHardLock)window.showApprovalHardLock();}catch(e){}
        },0);
      }
    }catch(e){
      beginApprovedOnboardingSequence();
      setTimeout(function(){
        try{if(window.showApprovalHardLock)window.showApprovalHardLock();}catch(x){}
      },0);
    }
  }

  if(document.readyState==='loading'){"""
s,n=pat.subn(rep,s,count=1)
assert n==1, 'startFirstRunSequence not found'

# Approval modal must be allowed to mount while the scroll stage is active.
s=s.replace("   if(window.__onboardingSequenceActive===true && !window.__approvalThenIntro) return;\n\n   var q=readApproval();",
            "   var q=readApproval();")

# If already approved, do not unlock through the approval mount; just leave the scroll stage visible.
s=s.replace("""   var q=readApproval();
   if(q.approved===true){
     window.__onboardingSequenceActive=false;
     unlockApp();
     return;
   }""","""   var q=readApproval();
   if(q.approved===true){
     hideLockOnly();
     return;
   }""")

# Approval request should wait for captain approval and not launch a video while pending.
s=s.replace("window.__approvalThenIntro=true;","window.__approvalThenIntro=false;")
s=s.replace(
"승인요청 후 항해사 승인이 완료되어야 다음 단계로 진행됩니다. 승인완료 전에는 앱 기능을 사용할 수 없습니다.",
"환영 두루마리 화면과 승인요청 창이 함께 표시됩니다. 항해사 승인완료 후 설치동영상으로 진행합니다."
)

# On approval, remove only the approval window and keep the scroll visible.
approved_old="""   if(q.status==='approved'||q.approved===true){
     statusText('항해사 승인완료.');
     window.__approvalThenIntro=false;
     hideLockOnly();
     try{
       if(window.__hasSeenFreedomIntro && window.__hasSeenFreedomIntro()){window.__onboardingSequenceActive=false;unlockApp();}
       else if(window.beginApprovedOnboardingSequence){window.beginApprovedOnboardingSequence();}
       else unlockApp();
     }catch(e){unlockApp();}
     return;
   }"""
approved_new="""   if(q.status==='approved'||q.approved===true){
     statusText('항해사 승인완료.');
     window.__approvalThenIntro=false;
     hideLockOnly();
     window.__onboardingSequenceActive=true;
     return;
   }"""
s=s.replace(approved_old,approved_new)

# Boot must not suppress the approval overlay because the scroll is active.
s=s.replace("""   // 첫 설치 안내 순서 중에는 승인화면을 먼저 띄우지 않는다.
   if(window.__onboardingSequenceActive===true) return;

   mount();""","""   mount();""")

s=s.replace('<div class="simpleVersion">V4.0.5 FINAL</div>',
            '<div class="simpleVersion">V4.0.6 FINAL</div>')

p.write_text(s,encoding='utf-8')

g=ROOT/'app/build.gradle'
t=g.read_text(encoding='utf-8')
t=re.sub(r'versionCode\s+\d+','versionCode 30406',t)
t=re.sub(r'versionName\s+"[^"]+"','versionName "4.0.6-SCROLL-APPROVAL"',t)
t=re.sub(r'outputFileName\s*=\s*"[^"]+"','outputFileName = "FREEDOM_V3_FINAL_406-debug.apk"',t)
g.write_text(t,encoding='utf-8')
