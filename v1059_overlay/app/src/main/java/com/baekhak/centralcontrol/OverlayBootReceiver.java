package com.baekhak.centralcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class OverlayBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        try {
            boolean enabled = context.getSharedPreferences("overlay_control", Context.MODE_PRIVATE).getBoolean("enabled", true);
            if (!enabled) return;
            Intent service = new Intent(context, OverlayControlService.class).setAction(OverlayControlService.ACTION_SHOW);
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service); else context.startService(service);
        } catch (Exception ignored) {}
    }
}