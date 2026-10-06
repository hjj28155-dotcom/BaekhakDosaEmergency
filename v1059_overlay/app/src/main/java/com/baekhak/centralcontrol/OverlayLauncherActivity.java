package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class OverlayLauncherActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getSharedPreferences("overlay_control", MODE_PRIVATE).edit().putBoolean("enabled", true).apply();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            try {
                Intent p = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(p);
                Toast.makeText(this, "이동아이콘 표시 권한을 허용한 뒤 항행의자유를 다시 눌러 주세요.", Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
            finish();
            return;
        }
        startOverlay();
        finishAndRemoveTask();
    }
    @Override protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            startOverlay();
            finishAndRemoveTask();
        }
    }
    private void startOverlay() {
        try {
            Intent s = new Intent(this, OverlayControlService.class).setAction(OverlayControlService.ACTION_SHOW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(s); else startService(s);
        } catch (Exception ignored) {}
    }
}