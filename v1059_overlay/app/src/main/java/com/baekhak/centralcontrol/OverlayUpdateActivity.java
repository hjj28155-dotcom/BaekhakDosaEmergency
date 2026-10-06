package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** V1051: dedicated updater landing screen. Never opens the picker on entry. */
public class OverlayUpdateActivity extends Activity {
    private static final int PICK_APK=7417;
    private boolean pickerOpen=false;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        showLandingScreen();
    }

    private void showLandingScreen(){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(36,72,36,36);
        box.setBackgroundColor(Color.rgb(5,31,48));

        TextView t=new TextView(this);
        t.setText("⚓ 이동아이콘 업데이트\nV1.0.51 (1051)");
        t.setTextColor(Color.WHITE); t.setTextSize(24); t.setGravity(Gravity.CENTER);
        box.addView(t,new LinearLayout.LayoutParams(-1,-2));

        TextView d=new TextView(this);
        d.setText("업데이트 파일을 이미 받아 둔 경우에만 아래 버튼을 누르세요.\n\n이 화면에 들어왔다고 해서 파일 선택기가 자동으로 열리지 않습니다.");
        d.setTextColor(Color.WHITE); d.setTextSize(16); d.setPadding(0,48,0,48); d.setGravity(Gravity.CENTER);
        box.addView(d,new LinearLayout.LayoutParams(-1,-2));

        Button pick=new Button(this);
        pick.setText("APK 업데이트 파일 선택");
        pick.setOnClickListener(v->pickApk());
        box.addView(pick,new LinearLayout.LayoutParams(-1,-2));

        Button close=new Button(this);
        close.setText("닫기 · 이동아이콘으로 돌아가기");
        close.setOnClickListener(v->finish());
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.topMargin=24;
        box.addView(close,cp);
        setContentView(box);
    }

    private void pickApk(){
        if(pickerOpen)return;
        try{
            pickerOpen=true;
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/vnd.android.package-archive");
            startActivityForResult(i,PICK_APK);
        }catch(Exception e){
            pickerOpen=false;
            Toast.makeText(this,"APK 선택 화면을 열 수 없습니다.",Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);
        if(req!=PICK_APK)return;
        pickerOpen=false;
        if(res!=RESULT_OK||data==null||data.getData()==null){ showLandingScreen(); return; }
        Uri u=data.getData();
        try{
            Intent i=new Intent(Intent.ACTION_VIEW)
                .setDataAndType(u,"application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        }catch(Exception e){
            Toast.makeText(this,"APK 설치 화면을 열 수 없습니다.",Toast.LENGTH_LONG).show();
            showLandingScreen();
        }
    }

    @Override protected void onDestroy(){ resumeOverlay(); super.onDestroy(); }
    private void resumeOverlay(){
        try{
            Intent s=new Intent(this,OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME);
            if(Build.VERSION.SDK_INT>=26)startForegroundService(s);else startService(s);
        }catch(Exception ignored){}
    }
}