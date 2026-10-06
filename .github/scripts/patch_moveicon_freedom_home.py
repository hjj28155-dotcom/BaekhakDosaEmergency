from pathlib import Path

p = Path("mini-overlay/app/src/main/java/com/openai/hanghae/mini/OverlayService.java")
s = p.read_text(encoding="utf-8")

old_listener = 'freedom.setOnClickListener(v->{collapseStations();collapseMenu();toast("항행의자유는 현재 준비 중입니다.");});'
new_listener = 'freedom.setOnClickListener(v->openFreedomApp());'
if old_listener not in s and new_listener not in s:
    raise SystemExit("Freedom listener target not found")
s = s.replace(old_listener, new_listener)

start = s.index("    private void openFreedomApp(){")
end = s.index("\n    private void openNavigatorApp(){", start)
new_method = '''    private void openFreedomApp(){
        collapseStations();
        collapseMenu();

        // 확정 순서 시작:
        // 이동아이콘 '항행의자유' -> 신규설치 온보딩의 두루마리 화면.
        // 이후 두루마리 클릭 -> 설치동영상 -> 승인요청 -> 서버접수 ->
        // 안내동영상 -> 승인대기 -> 항해사앱 승인 -> 승인완료 확인 -> 홈.
        try{
            Intent web=new Intent(Intent.ACTION_VIEW,Uri.parse("https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/?entry=moveicon-onboarding"));
            web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(web);
            setNow("🚢 항행의자유 신규설치 안내를 엽니다.");
        }catch(Exception e){
            toast("항행의자유 신규설치 안내를 열 수 없습니다.");
        }
    }
'''
s = s[:start] + new_method + s[end:]
p.write_text(s, encoding="utf-8")
print("Patched Freedom button: move icon -> Freedom app home")
