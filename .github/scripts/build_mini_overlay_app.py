from pathlib import Path
import re, shutil

ROOT=Path('miniicon/FREEDOM_V402_BUILD_THIS')
java=ROOT/'app/src/main/java/com/baekhak/centralcontrol'
res=ROOT/'app/src/freedom/res'
assets=ROOT/'app/src/freedom/assets'

# Remove large app assets; the mini icon app does not use WebView/onboarding assets.
if assets.exists():
    shutil.rmtree(assets)

# Minimal launcher activity: requests overlay/audio permission and starts the floating control.
main = r'''package com.baekhak.centralcontrol;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQ_AUDIO = 77;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        showUi();
        ensurePermissionsAndStart();
    }

    @Override protected void onResume() {
        super.onResume();
        if (canOverlay()) startOverlay();
    }

    private void showUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(36), dp(22), dp(28));
        root.setBackgroundColor(Color.rgb(4, 24, 42));

        TextView title = new TextView(this);
        title.setText("항행의자유 이동아이콘");
        title.setTextColor(Color.rgb(255, 225, 128));
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("\n⚓ 이동아이콘 전용\nMP3 · FM · AM · YouTube\n\n처음 한 번 '다른 앱 위에 표시'와\n'음악 및 오디오' 권한을 허용해 주세요.");
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        Button start = button("⚓ 이동아이콘 시작");
        start.setOnClickListener(v -> ensurePermissionsAndStart());
        root.addView(start);

        Button stop = button("이동아이콘 숨기기");
        stop.setOnClickListener(v -> {
            Intent i = new Intent(this, OverlayControlService.class).setAction(OverlayControlService.ACTION_HIDE);
            try { startService(i); } catch (Exception ignored) {}
        });
        root.addView(stop);

        setContentView(root);
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(17);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        lp.setMargins(0, dp(14), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private boolean canOverlay() {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
    }

    private void ensurePermissionsAndStart() {
        if (!canOverlay()) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(i);
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQ_AUDIO);
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 33 &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_AUDIO);
            return;
        }
        startOverlay();
    }

    private void startOverlay() {
        Intent i = new Intent(this, OverlayControlService.class).setAction(OverlayControlService.ACTION_SHOW);
        try {
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        } catch (Exception ignored) {}
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_AUDIO) startOverlay();
    }
}
'''
(java/'MainActivity.java').write_text(main, encoding='utf-8')

# Modify overlay MP3 button so it plays device music directly instead of opening the full app library.
p=java/'OverlayControlService.java'
s=p.read_text(encoding='utf-8')
old='''        music.setOnClickListener(v -> {
            stopRadio();
            collapseStations();
            try {
                Intent open = getPackageManager().getLaunchIntentForPackage(getPackageName());
                if (open == null) {
                    toast("항행의자유 앱에서 음악목록을 열 수 없습니다.");
                    return;
                }
                open.putExtra("v3_open_saved_music_library", true);
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(open);
            } catch (Exception e) {
                toast("저장된 음악목록을 열지 못했습니다.");
            }
        });'''
new='''        music.setOnClickListener(v -> {
            stopRadio();
            collapseStations();
            if (!hasAudioPermission()) {
                requestAudioPermissionInApp();
                return;
            }
            try {
                Intent i = new Intent(this, MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_PLAY);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
                toast("휴대폰 MP3 재생을 시작합니다.");
            } catch (Exception e) {
                toast("MP3 재생을 시작하지 못했습니다.");
            }
        });'''
s=s.replace(old,new)
s=s.replace("항행의자유 앱을 열어 음악 권한을 허용해 주세요.","이동아이콘 앱을 열어 음악 권한을 허용해 주세요.")
s=s.replace("항행의자유 앱을 열어 음악 및 오디오 권한을 허용해 주세요.","이동아이콘 앱을 열어 음악 및 오디오 권한을 허용해 주세요.")
s=s.replace("항행의자유 상단 미니바","항행의자유 이동아이콘")
s=s.replace("항행의자유 미니바 사용 중","항행의자유 이동아이콘 사용 중")
p.write_text(s,encoding='utf-8')

# Minimal manifest.
manifest = r'''<manifest xmlns:android="http://schemas.android.com/apk/res/android">
 <uses-permission android:name="android.permission.INTERNET"/>
 <uses-permission android:name="android.permission.WAKE_LOCK"/>
 <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
 <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
 <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
 <uses-permission android:name="android.permission.READ_MEDIA_AUDIO"/>
 <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32"/>
 <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
 <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
 <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE"/>

 <queries>
   <package android:name="com.google.android.youtube"/>
 </queries>

 <application
   android:theme="@style/AppTheme"
   android:label="@string/app_name"
   android:icon="@drawable/v3_ship_icon"
   android:usesCleartextTraffic="true"
   android:allowBackup="false">

   <activity android:name=".MainActivity" android:exported="true">
     <intent-filter>
       <action android:name="android.intent.action.MAIN"/>
       <category android:name="android.intent.category.LAUNCHER"/>
     </intent-filter>
   </activity>

   <service android:name=".MusicPlaybackService" android:exported="false"
     android:stopWithTask="false" android:foregroundServiceType="mediaPlayback"/>

   <service android:name=".RadioPlaybackService" android:exported="false"
     android:stopWithTask="false" android:foregroundServiceType="mediaPlayback"/>

   <service android:name=".OverlayControlService" android:exported="false"
     android:stopWithTask="false" android:foregroundServiceType="specialUse">
     <property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
       android:value="Persistent user-requested media control overlay"/>
   </service>

   <receiver android:name=".OverlayBootReceiver" android:enabled="true" android:exported="true">
     <intent-filter>
       <action android:name="android.intent.action.BOOT_COMPLETED"/>
       <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED"/>
       <action android:name="android.intent.action.MY_PACKAGE_REPLACED"/>
     </intent-filter>
   </receiver>
 </application>
</manifest>
'''
(ROOT/'app/src/main/AndroidManifest.xml').write_text(manifest, encoding='utf-8')

# Debug/release overlay manifest no longer needed.
fm=ROOT/'app/src/freedom/AndroidManifest.xml'
if fm.exists(): fm.write_text('<manifest xmlns:android="http://schemas.android.com/apk/res/android"/>',encoding='utf-8')

# Rename app.
(res/'values/strings.xml').write_text('<resources><string name="app_name">항행의자유 이동아이콘</string></resources>',encoding='utf-8')

# Build config: separate package, keep only required resources, omit large assets.
g=ROOT/'app/build.gradle'
t=g.read_text(encoding='utf-8')
t=t.replace("applicationId 'com.navigator.freedom.v3final'","applicationId 'com.navigator.freedom.miniicon'")
t=re.sub(r'versionCode\s+\d+','versionCode 10001',t)
t=re.sub(r'versionName\s+"[^"]+"','versionName "1.0.1-MINI-ICON"',t)
t=t.replace("assets.srcDirs = ['src/main/assets', 'src/freedom/assets']","assets.srcDirs = []")
t=re.sub(r'outputFileName\s*=\s*"[^"]+"','outputFileName = "HANGHAE_MINI_MP3_FM_AM_YOUTUBE-debug.apk"',t)
g.write_text(t,encoding='utf-8')
