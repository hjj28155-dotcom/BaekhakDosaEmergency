from pathlib import Path
import re

ROOT=Path('build407/FREEDOM_V402_BUILD_THIS')
java=ROOT/'app/src/main/java/com/baekhak/centralcontrol/MainActivity.java'
s=java.read_text(encoding='utf-8')

# Main app no longer exposes or starts the floating movement icon.
s=re.sub(
r'''    private boolean isOverlayControlEnabled\(\) \{.*?^    \}''',
'''    private boolean isOverlayControlEnabled() {
        return false;
    }''',
s,count=1,flags=re.S|re.M)

s=re.sub(
r'''    private void setOverlayControlEnabled\(boolean enabled\) \{.*?^    \}''',
'''    private void setOverlayControlEnabled(boolean enabled) {
        // V4.0.7: floating movement icon removed from the main app.
    }''',
s,count=1,flags=re.S|re.M)

s=re.sub(
r'''    private void startAlwaysOverlayExperiment\(\) \{.*?^    \}''',
'''    private void startAlwaysOverlayExperiment() {
        // V4.0.7: floating movement icon removed from the main app.
    }''',
s,count=1,flags=re.S|re.M)

# Do not start/stop overlay service on launch.
s=s.replace('''        // HARDLOCK: 승인완료 전에는 이동아이콘/음악을 시작하지 않습니다.
        if (isSpecialApprovedLocal()) {
            startAlwaysOverlayExperiment();
        } else {
            try { stopService(new Intent(this, OverlayControlService.class)); } catch (Exception ignored) {}
            try {
                Intent stopMusic = new Intent(this, MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_STOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(stopMusic); else startService(stopMusic);
            } catch (Exception ignored) {}
        }''',
'''        // V4.0.7: floating movement icon removed from the main app.
        if (!isSpecialApprovedLocal()) {
            try {
                Intent stopMusic = new Intent(this, MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_STOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(stopMusic); else startService(stopMusic);
            } catch (Exception ignored) {}
        }''')

# Remove any direct late calls.
s=s.replace('try { startAlwaysOverlayExperiment(); } catch (Exception ignored) {}','')

java.write_text(s,encoding='utf-8')

# Remove overlay service/boot receiver/permissions from manifest.
m=ROOT/'app/src/main/AndroidManifest.xml'
t=m.read_text(encoding='utf-8')
t=re.sub(r'\s*<uses-permission android:name="android\.permission\.SYSTEM_ALERT_WINDOW"\s*/>','',t)
t=re.sub(r'\s*<uses-permission android:name="android\.permission\.FOREGROUND_SERVICE_SPECIAL_USE"\s*/>','',t)
t=re.sub(r'\s*<receiver android:name="\.OverlayBootReceiver".*?</receiver>','',t,flags=re.S)
t=re.sub(r'\s*<service\s+android:name="\.OverlayControlService".*?</service>','',t,flags=re.S)
m.write_text(t,encoding='utf-8')

# Force UI setting off if the old control is still present in HTML.
html=ROOT/'app/src/freedom/assets/index.html'
h=html.read_text(encoding='utf-8')
h=re.sub(r'function readOverlayEnabled\(\)\{.*?\n\}', 'function readOverlayEnabled(){ return false; }', h, count=1, flags=re.S)
h=re.sub(r'function saveOverlayEnabled\(enabled\)\{.*?\n\}', 'function saveOverlayEnabled(enabled){ return false; }', h, count=1, flags=re.S)
h=h.replace('<div class="simpleVersion">V4.0.6 FINAL</div>','<div class="simpleVersion">V4.0.7 FINAL</div>')
h=h.replace('이동아이콘','')
html.write_text(h,encoding='utf-8')

# Version bump.
g=ROOT/'app/build.gradle'
b=g.read_text(encoding='utf-8')
b=re.sub(r'versionCode\s+\d+','versionCode 30407',b)
b=re.sub(r'versionName\s+"[^"]+"','versionName "4.0.7-NO-FLOATING-ICON"',b)
b=re.sub(r'outputFileName\s*=\s*"[^"]+"','outputFileName = "FREEDOM_V3_FINAL_407-debug.apk"',b)
g.write_text(b,encoding='utf-8')
