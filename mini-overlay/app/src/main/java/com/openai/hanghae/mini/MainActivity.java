package com.openai.hanghae.mini;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
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
    private static final String UPDATE_URL="https://raw.githubusercontent.com/hjj28155-dotcom/BaekhakDosaEmergency/main/%ED%95%AD%ED%96%89%EC%9D%98%EC%9E%90%EC%9C%A0_%EC%9D%B4%EB%8F%99%EC%95%84%EC%9D%B4%EC%BD%98_MP3_FM_AM_%EC%9C%A0%ED%8A%9C%EB%B8%8C.apk";
    private boolean pendingStart=false;
    private long updateDownloadId=-1L;
    private BroadcastReceiver updateReceiver;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(buildUi());
        if(getIntent()!=null && getIntent().getBooleanExtra("direct_update",false)){
            startDirectUpdate();
        }
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

        Button update=button("⬇ 업데이트");
        update.setOnClickListener(v->startDirectUpdate());
        root.addView(update);

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

    private void startDirectUpdate(){
        try{
            DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            if(dm==null){Toast.makeText(this,"업데이트를 시작하지 못했습니다.",Toast.LENGTH_SHORT).show();return;}
            DownloadManager.Request req=new DownloadManager.Request(Uri.parse(UPDATE_URL));
            req.setTitle("항행의자유 이동아이콘 업데이트");
            req.setDescription("최신 APK를 내려받는 중입니다.");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setMimeType("application/vnd.android.package-archive");
            updateDownloadId=dm.enqueue(req);

            if(updateReceiver!=null){
                try{unregisterReceiver(updateReceiver);}catch(Exception ignored){}
            }
            updateReceiver=new BroadcastReceiver(){
                @Override public void onReceive(Context context, Intent intent){
                    long id=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1L);
                    if(id!=updateDownloadId)return;
                    try{
                        Uri uri=dm.getUriForDownloadedFile(id);
                        if(uri==null){Toast.makeText(MainActivity.this,"업데이트 파일을 열지 못했습니다.",Toast.LENGTH_SHORT).show();return;}
                        Intent install=new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(uri,"application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(install);
                    }catch(Exception e){
                        Toast.makeText(MainActivity.this,"설치 화면을 열지 못했습니다.",Toast.LENGTH_SHORT).show();
                    }
                }
            };
            IntentFilter filter=new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            if(Build.VERSION.SDK_INT>=33) registerReceiver(updateReceiver,filter,Context.RECEIVER_NOT_EXPORTED);
            else registerReceiver(updateReceiver,filter);
            Toast.makeText(this,"업데이트 파일을 내려받습니다.",Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Toast.makeText(this,"업데이트를 시작하지 못했습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onDestroy(){
        if(updateReceiver!=null){
            try{unregisterReceiver(updateReceiver);}catch(Exception ignored){}
            updateReceiver=null;
        }
        super.onDestroy();
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
