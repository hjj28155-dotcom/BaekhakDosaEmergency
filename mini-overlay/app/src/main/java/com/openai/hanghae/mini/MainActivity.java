package com.openai.hanghae.mini;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_AUDIO=77;
    private static final String UPDATE_URL="https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/HANGHAE_NAVIGATOR_MINI-debug.apk?v=1.0.19-20261004";

    private boolean pendingStart=false;
    private long updateDownloadId=-1L;
    private BroadcastReceiver updateReceiver;

    private TextView updateStatus;
    private ProgressBar updateProgress;
    private Button updateButton;
    private final Handler progressHandler=new Handler(Looper.getMainLooper());

    private final Runnable progressPoller=new Runnable(){
        @Override public void run(){
            if(updateDownloadId<0L) return;
            queryDownloadProgress();
            progressHandler.postDelayed(this,500);
        }
    };

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

        updateButton=button("⬇ 업데이트");
        updateButton.setOnClickListener(v->startDirectUpdate());
        root.addView(updateButton);

        updateStatus=new TextView(this);
        updateStatus.setText("업데이트 버튼을 누르면 진행률이 바로 표시됩니다.");
        updateStatus.setTextColor(Color.rgb(180,220,240));
        updateStatus.setTextSize(15);
        updateStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusLp=new LinearLayout.LayoutParams(-1,-2);
        statusLp.setMargins(0,dp(12),0,0);
        root.addView(updateStatus,statusLp);

        updateProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        updateProgress.setMax(100);
        updateProgress.setProgress(0);
        updateProgress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressLp=new LinearLayout.LayoutParams(-1,dp(18));
        progressLp.setMargins(0,dp(8),0,0);
        root.addView(updateProgress,progressLp);

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
        if(Build.VERSION.SDK_INT>=33 &&
           (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO)!=PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO)!=PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_VIDEO},REQ_AUDIO);
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
            if(dm==null){
                setUpdateState("업데이트를 시작하지 못했습니다.",0,false);
                return;
            }

            if(updateDownloadId>=0L){
                setUpdateState("이미 업데이트 파일을 내려받는 중입니다.",updateProgress==null?0:updateProgress.getProgress(),true);
                return;
            }

            String fileName="항행의자유_이동아이콘_업데이트_"+System.currentTimeMillis()+".apk";
            DownloadManager.Request req=new DownloadManager.Request(Uri.parse(UPDATE_URL));
            req.setTitle("항행의자유 이동아이콘 업데이트");
            req.setDescription("최신 APK를 내려받는 중입니다.");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setMimeType("application/vnd.android.package-archive");
            req.setAllowedOverMetered(true);
            req.setAllowedOverRoaming(true);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);

            if(updateReceiver!=null){
                try{unregisterReceiver(updateReceiver);}catch(Exception ignored){}
            }

            updateReceiver=new BroadcastReceiver(){
                @Override public void onReceive(Context context, Intent intent){
                    long id=intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1L);
                    if(id!=updateDownloadId)return;
                    progressHandler.removeCallbacks(progressPoller);
                    queryDownloadProgress();
                    try{
                        Uri uri=dm.getUriForDownloadedFile(id);
                        if(uri==null){
                            setUpdateState("다운로드는 끝났지만 설치 파일을 열지 못했습니다. 다운로드 폴더를 확인해 주세요.",100,false);
                            updateDownloadId=-1L;
                            return;
                        }
                        setUpdateState("✅ 다운로드 완료 · 다운로드 폴더에 저장됨 · 설치 화면을 여는 중",100,true);
                        Intent install=new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(uri,"application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(install);
                    }catch(Exception e){
                        setUpdateState("✅ 다운로드 완료 · 다운로드 폴더에서 APK를 눌러 설치해 주세요.",100,true);
                    }finally{
                        updateDownloadId=-1L;
                    }
                }
            };

            IntentFilter filter=new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            if(Build.VERSION.SDK_INT>=33) registerReceiver(updateReceiver,filter,Context.RECEIVER_NOT_EXPORTED);
            else registerReceiver(updateReceiver,filter);

            updateDownloadId=dm.enqueue(req);
            if(updateButton!=null) updateButton.setEnabled(false);
            setUpdateState("⬇ 다운로드 시작 · 0%",0,true);
            progressHandler.removeCallbacks(progressPoller);
            progressHandler.post(progressPoller);

        }catch(Exception e){
            updateDownloadId=-1L;
            if(updateButton!=null) updateButton.setEnabled(true);
            setUpdateState("업데이트를 시작하지 못했습니다.",0,false);
        }
    }

    private void queryDownloadProgress(){
        if(updateDownloadId<0L)return;
        DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        if(dm==null)return;

        DownloadManager.Query q=new DownloadManager.Query().setFilterById(updateDownloadId);
        try(Cursor c=dm.query(q)){
            if(c==null || !c.moveToFirst())return;
            int status=c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            long done=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long total=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

            int percent=(total>0L)?(int)Math.min(100L,(done*100L)/total):0;
            if(status==DownloadManager.STATUS_RUNNING){
                setUpdateState("⬇ 다운로드 "+percent+"% · "+mb(done)+" / "+(total>0?mb(total):"--"),percent,true);
            }else if(status==DownloadManager.STATUS_PENDING){
                setUpdateState("업데이트 다운로드 준비 중…",percent,true);
            }else if(status==DownloadManager.STATUS_PAUSED){
                setUpdateState("다운로드 일시 대기 · "+percent+"%",percent,true);
            }else if(status==DownloadManager.STATUS_SUCCESSFUL){
                setUpdateState("✅ 다운로드 100% · 설치 준비 중",100,true);
            }else if(status==DownloadManager.STATUS_FAILED){
                int reason=c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
                progressHandler.removeCallbacks(progressPoller);
                updateDownloadId=-1L;
                if(updateButton!=null) updateButton.setEnabled(true);
                setUpdateState("❌ 다운로드 실패("+reason+") · 다시 업데이트를 눌러 주세요.",percent,false);
            }
        }catch(Exception ignored){}
    }

    private String mb(long bytes){
        return String.format(Locale.KOREA,"%.1fMB",bytes/1048576.0);
    }

    private void setUpdateState(String text,int percent,boolean showBar){
        if(updateStatus!=null)updateStatus.setText(text);
        if(updateProgress!=null){
            updateProgress.setVisibility(showBar?View.VISIBLE:View.GONE);
            updateProgress.setProgress(Math.max(0,Math.min(100,percent)));
        }
        if(updateButton!=null && updateDownloadId<0L)updateButton.setEnabled(true);
    }

    @Override protected void onDestroy(){
        progressHandler.removeCallbacks(progressPoller);
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
            else Toast.makeText(this,"MP3 · AVI · MP4 사용을 위해 음악/동영상 권한이 필요합니다.",Toast.LENGTH_SHORT).show();
        }
    }
}
