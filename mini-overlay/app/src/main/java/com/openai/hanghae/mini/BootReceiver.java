package com.openai.hanghae.mini;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent){
        try{
            boolean enabled=context.getSharedPreferences("overlay_control",Context.MODE_PRIVATE).getBoolean("enabled",true);
            if(!enabled) return;
            Intent s=new Intent(context,OverlayService.class).setAction(OverlayService.ACTION_SHOW);
            if(Build.VERSION.SDK_INT>=26) context.startForegroundService(s); else context.startService(s);
        }catch(Exception ignored){}
    }
}
