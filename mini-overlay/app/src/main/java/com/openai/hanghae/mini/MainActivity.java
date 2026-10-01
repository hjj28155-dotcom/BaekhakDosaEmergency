package com.openai.hanghae.mini;

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
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_AUDIO=77;
    private boolean pendingStart=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(buildUi());
    }

    @Override protected void onResume(){
        super.onResume();
        if(pendingStart && canOverlay()){
            pendingStart=false;
            ensureAudioPermissionAndStart();
        }
    }

    private LinearLayout buildUi(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22),dp(36),dp(22),dp(28));
        root.setBackgroundColor(Color.rgb(4,24,42));

        TextView title=new TextView(this);
        title.setText("항행의자유 이동아이콘");
        title.setTextColor(Color.rgb(255,225,128));
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,-2));

        TextView info=new TextView(this);
        info.setText("\n⚓ 이동아이콘 전용\nMP3 · FM · AM · YouTube\n\n원할 때만 켜고 끌 수 있습니다.\n꺼둔 상태는 앱 재실행·재부팅 후에도 유지됩니다.");
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        root.addView(info,new LinearLayout.LayoutParams(-1,-2));

        Button start=button("⚓ 이동아이콘 켜기");
        start.setOnClickListener(v->startRequested());
        root.addView(start);

        Button hide=button("⏹ 이동아이콘 끄기");
        hide.setOnClickListener(v->hideOverlay());
        root.addView(hide);
        return root;
    }

    private Button button(String label){
        Button b=new Button(this);
        b.setText(label);
        b.setTextSize(17);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(54));
        lp.setMargins(0,dp(14),0,0);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
    private boolean canOverlay(){ return Build.VERSION.SDK_INT<23 || Settings.canDrawOverlays(this); }

    private void startRequested(){
        if(!canOverlay()){
            pendingStart=true;
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
            return;
        }
        ensureAudioPermissionAndStart();
    }

    private void ensureAudioPermissionAndStart(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO},REQ_AUDIO);
            return;
        }
        if(Build.VERSION.SDK_INT>=23 && Build.VERSION.SDK_INT<33 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQ_AUDIO);
            return;
        }
        startOverlay();
    }

    private void startOverlay(){
        getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("enabled",true).apply();
        Intent i=new Intent(this,OverlayService.class).setAction(OverlayService.ACTION_SHOW);
        try{
            if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
            Toast.makeText(this,"이동아이콘을 켰습니다.",Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Toast.makeText(this,"이동아이콘을 시작하지 못했습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    private void hideOverlay(){
        pendingStart=false;
        getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("enabled",false).apply();
        Intent i=new Intent(this,OverlayService.class).setAction(OverlayService.ACTION_HIDE);
        try{
            if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
        }catch(Exception ignored){}
        try{ stopService(new Intent(this,OverlayService.class)); }catch(Exception ignored){}
        Toast.makeText(this,"이동아이콘을 껐습니다.",Toast.LENGTH_SHORT).show();
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQ_AUDIO){
            boolean granted=grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED;
            if(granted) startOverlay();
            else Toast.makeText(this,"MP3 사용을 위해 음악 권한이 필요합니다.",Toast.LENGTH_SHORT).show();
        }
    }
}
