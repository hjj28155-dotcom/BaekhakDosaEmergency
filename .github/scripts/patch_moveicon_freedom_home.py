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

        // 확정 순서 시작: 이동아이콘 -> 항행의자유 홈.
        // 홈 이후 공지사항 -> 설치 및 업데이트 설명서 ->
        // 설치 설명서 전체화면 -> 설치동영상은 항행의자유 앱 안에서 진행한다.
        if(launchPackage("com.navigator.freedom.v3final","🚢 항행의자유 실행")) return;
        if(launchPackage("com.navigator.freedom","🚢 항행의자유 실행")) return;
        if(launchByLabel("항행의자유","🚢 항행의자유 실행")) return;

        toast("항행의자유 앱을 찾을 수 없습니다.");
    }
'''
s = s[:start] + new_method + s[end:]
p.write_text(s, encoding="utf-8")
print("Patched Freedom button: move icon -> Freedom app home")
