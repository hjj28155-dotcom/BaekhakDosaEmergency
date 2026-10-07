package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/** 업데이트 버튼: 원격 최신 versionCode를 먼저 확인하고, 필요한 경우에만 APK를 내려받는다. */
public class OverlayUpdateActivity extends Activity {
    private static final String UPDATE_MANIFEST_URL =
            "https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/moveicon-test-update.json";
    private TextView status;
    private ProgressBar progress;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER_HORIZONTAL);box.setPadding(36,72,36,36);box.setBackgroundColor(Color.rgb(5,31,48));
        TextView t=new TextView(this);t.setText("⚓ 이동아이콘 업데이트");t.setTextColor(Color.WHITE);t.setTextSize(24);t.setGravity(Gravity.CENTER);box.addView(t,new LinearLayout.LayoutParams(-1,-2));
        status=new TextView(this);status.setText("최신판을 확인하고 있습니다…");status.setTextColor(Color.WHITE);status.setTextSize(16);status.setPadding(0,42,0,20);status.setGravity(Gravity.CENTER);box.addView(status,new LinearLayout.LayoutParams(-1,-2));
        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);progress.setProgress(0);box.addView(progress,new LinearLayout.LayoutParams(-1,22));
        setContentView(box);
        checkLatest();
    }

    private long installedVersionCode(){
        try{
            PackageInfo p=getPackageManager().getPackageInfo(getPackageName(),0);
            return Build.VERSION.SDK_INT>=28?p.getLongVersionCode():p.versionCode;
        }catch(Exception e){return -1;}
    }

    private String installedVersionName(){
        try{
            PackageInfo p=getPackageManager().getPackageInfo(getPackageName(),0);
            return p.versionName==null?"":p.versionName;
        }catch(Exception e){return "";}
    }

    private void checkLatest(){
        new Thread(()->{
            HttpURLConnection conn=null;
            try{
                URL u=new URL(UPDATE_MANIFEST_URL+"?t="+System.currentTimeMillis());
                conn=(HttpURLConnection)u.openConnection();
                conn.setConnectTimeout(12000);conn.setReadTimeout(15000);conn.setUseCaches(false);conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("Cache-Control","no-cache");conn.connect();
                int code=conn.getResponseCode();if(code<200||code>=300)throw new Exception("HTTP "+code);
                StringBuilder sb=new StringBuilder();
                try(BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"))){String line;while((line=br.readLine())!=null)sb.append(line);}
                JSONObject j=new JSONObject(sb.toString());
                long latest=j.getLong("versionCode");
                String latestName=j.optString("versionName","최신판");
                String apkUrl=j.getString("apkUrl");
                long current=installedVersionCode();
                String currentName=installedVersionName();
                if(current>=latest){
                    runOnUiThread(()->{
                        progress.setProgress(100);progress.setVisibility(View.GONE);
                        status.setText("✅ 최신 버전입니다.\n현재 "+currentName);
                    });
                }else{
                    runOnUiThread(()->status.setText("새 버전 "+latestName+"을 다운로드합니다…"));
                    startDownload(apkUrl, latestName);
                }
            }catch(Exception e){
                final String m=e.getMessage()==null?"확인 실패":e.getMessage();
                runOnUiThread(()->{progress.setVisibility(View.GONE);status.setText("❌ 업데이트 확인 실패 ("+m+")\n잠시 후 다시 눌러 주세요.");});
            }finally{if(conn!=null)conn.disconnect();}
        },"OverlayUpdateCheck").start();
    }

    private void startDownload(String apkUrl,String latestName){
        new Thread(()->{
            HttpURLConnection conn=null; Uri outUri=null;
            try{
                conn=(HttpURLConnection)new URL(apkUrl).openConnection();conn.setConnectTimeout(15000);conn.setReadTimeout(30000);conn.setInstanceFollowRedirects(true);conn.connect();
                int code=conn.getResponseCode();if(code<200||code>=300)throw new Exception("HTTP "+code);
                long total=Build.VERSION.SDK_INT>=24?conn.getContentLengthLong():conn.getContentLength();
                if(Build.VERSION.SDK_INT<29)throw new Exception("Android 10 이상에서 직접 업데이트를 지원합니다.");
                String safe=(latestName==null?"최신":latestName).replaceAll("[^0-9A-Za-z가-힣._-]+","_");
                ContentValues cv=new ContentValues();cv.put(MediaStore.Downloads.DISPLAY_NAME,"항행의자유_이동아이콘_"+safe+".apk");cv.put(MediaStore.Downloads.MIME_TYPE,"application/vnd.android.package-archive");cv.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);cv.put(MediaStore.Downloads.IS_PENDING,1);
                outUri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,cv);if(outUri==null)throw new Exception("다운로드 저장소 생성 실패");
                try(InputStream in=conn.getInputStream();OutputStream out=getContentResolver().openOutputStream(outUri,"w")){
                    if(out==null)throw new Exception("다운로드 파일 열기 실패");byte[] buf=new byte[32768];long done=0;int n,last=-1;
                    while((n=in.read(buf))!=-1){out.write(buf,0,n);done+=n;int pct=total>0?(int)Math.min(100,(done*100)/total):0;if(pct!=last){last=pct;final int fp=pct;final long fd=done;runOnUiThread(()->{progress.setVisibility(View.VISIBLE);progress.setProgress(fp);status.setText("⬇ 업데이트 "+fp+"% · "+String.format(Locale.KOREA,"%.1fMB",fd/1048576.0));});}}out.flush();
                }
                ContentValues doneCv=new ContentValues();doneCv.put(MediaStore.Downloads.IS_PENDING,0);getContentResolver().update(outUri,doneCv,null,null);
                final Uri u=outUri;runOnUiThread(()->{progress.setProgress(100);status.setText("✅ 다운로드 완료 · 설치 화면을 엽니다.");try{Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(u,"application/vnd.android.package-archive");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}catch(Exception e){status.setText("다운로드 완료 · 다운로드 폴더의 APK를 눌러 설치해 주세요.");}});
            }catch(Exception e){final String m=e.getMessage()==null?"업데이트 실패":e.getMessage();if(outUri!=null)try{getContentResolver().delete(outUri,null,null);}catch(Exception ignored){}runOnUiThread(()->{progress.setVisibility(View.GONE);status.setText("❌ "+m+"\n업데이트를 다시 눌러 주세요.");});}
            finally{if(conn!=null)conn.disconnect();}
        },"OverlayDirectUpdater").start();
    }

    @Override protected void onDestroy(){ resumeOverlay(); super.onDestroy(); }
    private void resumeOverlay(){try{Intent s=new Intent(this,OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME);if(Build.VERSION.SDK_INT>=26)startForegroundService(s);else startService(s);}catch(Exception ignored){}}
}
