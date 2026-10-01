package com.openai.hanghae.mini;

import android.app.*;
import android.content.*;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OverlayService extends Service {
    public static final String ACTION_SHOW="com.navigator.freedom.miniicon.SHOW";
    public static final String ACTION_HIDE="com.navigator.freedom.miniicon.HIDE";
    private static final String CHANNEL="hanghae_mini_overlay";
    private static final int NOTICE=7411;

    private WindowManager wm;
    private LinearLayout root, menu, stationPanel;
    private WindowManager.LayoutParams lp;
    private TextView anchor, now;
    private boolean expanded=false;
    private MediaPlayer player;
    private String currentName="";
    private boolean localMode=false;
    private final ArrayList<Long> localTrackIds=new ArrayList<>();
    private final ArrayList<String> localTrackNames=new ArrayList<>();
    private int localTrackIndex=-1;

    private volatile boolean updateBusy=false;
    private final Handler updateHandler=new Handler(Looper.getMainLooper());

    @Override public void onCreate(){
        super.onCreate();
        createChannel();
        startForeground(NOTICE,notification());
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
    }

    @Override public int onStartCommand(Intent intent,int flags,int startId){
        String a=intent==null?ACTION_SHOW:intent.getAction();
        if(ACTION_HIDE.equals(a)){
            getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("enabled",false).apply();
            removeOverlay();
            stopMedia();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("enabled",true).apply();
        if(Build.VERSION.SDK_INT<23 || Settings.canDrawOverlays(this)) showOverlay();
        return START_STICKY;
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel(CHANNEL,"항행의자유 이동아이콘",NotificationManager.IMPORTANCE_LOW);
            c.setSound(null,null);
            NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if(nm!=null)nm.createNotificationChannel(c);
        }
    }

    private Notification notification(){
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_media_play)
         .setContentTitle("항행의자유 이동아이콘")
         .setContentText("MP3 · FM · AM · YouTube")
         .setOngoing(true).setOnlyAlertOnce(true);
        Intent open=new Intent(this,MainActivity.class);
        b.setContentIntent(PendingIntent.getActivity(this,NOTICE,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        return b.build();
    }

    private int dp(int v){return Math.max(1,Math.round(v*getResources().getDisplayMetrics().density));}
    private GradientDrawable bg(int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(1),stroke);return d;
    }

    private Button btn(String text,int color){
        Button b=new Button(this);b.setText(text);b.setTextColor(Color.WHITE);b.setTextSize(9.5f);b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);b.setPadding(dp(3),0,dp(3),0);b.setMinHeight(0);b.setMinimumHeight(0);b.setMinWidth(0);b.setMinimumWidth(0);
        b.setBackground(bg(color,0x66FFD35A,13));
        int w="🎵 MP3".equals(text)?52:("📻 FM".equals(text)||"📡 AM".equals(text)?47:("업데이트".equals(text)?62:55));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(w),dp(36));p.setMargins(dp(1),dp(2),dp(1),dp(2));b.setLayoutParams(p);return b;
    }

    private TextView station(String text){
        TextView v=new TextView(this);v.setText(text);v.setTextColor(Color.WHITE);v.setTextSize(12f);v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(12),dp(9),dp(12),dp(9));v.setBackground(bg(0xEE08233E,0x6678BEFF,10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(3),dp(2),dp(3),dp(2));v.setLayoutParams(p);return v;
    }

    private void showOverlay(){
        if(root!=null||wm==null)return;
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(5),dp(4),dp(5),dp(4));root.setBackground(bg(0xF20A1D30,0xCCF3C954,18));

        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER_VERTICAL);
        anchor=new TextView(this);anchor.setText("⚓");anchor.setTextSize(19f);anchor.setTextColor(0xFFFFD96A);anchor.setGravity(Gravity.CENTER);
        anchor.setBackground(bg(0xEE08233E,0xCCF3C954,14));anchor.setLayoutParams(new LinearLayout.LayoutParams(dp(39),dp(39)));bar.addView(anchor);

        menu=new LinearLayout(this);menu.setOrientation(LinearLayout.HORIZONTAL);menu.setGravity(Gravity.CENTER_VERTICAL);menu.setVisibility(View.GONE);
        Button mp3=btn("🎵 MP3",0xEE1068C8), fm=btn("📻 FM",0xEE5B34D6), am=btn("📡 AM",0xEE6D2DB7),
               yt=btn("유튜브",0xEEDB1F28), stop=btn("■ 종료",0xEED52D46), update=btn("업데이트",0xEE087F5B);
        menu.addView(mp3);menu.addView(fm);menu.addView(am);menu.addView(yt);menu.addView(stop);menu.addView(update);bar.addView(menu);
        root.addView(bar,new LinearLayout.LayoutParams(-2,dp(46)));

        now=new TextView(this);now.setTextColor(0xFFFFE58A);now.setTextSize(10.5f);now.setPadding(dp(8),dp(2),dp(8),dp(3));now.setVisibility(View.GONE);
        root.addView(now,new LinearLayout.LayoutParams(-2,-2));

        stationPanel=new LinearLayout(this);stationPanel.setOrientation(LinearLayout.VERTICAL);stationPanel.setVisibility(View.GONE);
        root.addView(stationPanel,new LinearLayout.LayoutParams(-1,-2));

        mp3.setOnClickListener(v->{showMp3Controls();toggleLocalMp3();});
        fm.setOnClickListener(v->showStations(true));
        am.setOnClickListener(v->showStations(false));
        yt.setOnClickListener(v->openYoutube());
        stop.setOnClickListener(v->{stopMedia();collapseStations();collapseMenu();toast("음악 · 라디오를 종료했습니다.");});
        update.setOnClickListener(v->{collapseStations();collapseMenu();openUpdater();});

        if(Build.VERSION.SDK_INT>=26)lp=new WindowManager.LayoutParams(-2,-2,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        else lp=new WindowManager.LayoutParams(-2,-2,WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.START;lp.x=dp(8);lp.y=dp(12);

        final float[] down=new float[2];final int[] pos=new int[2];final boolean[] moved=new boolean[1];
        anchor.setOnTouchListener((v,e)->{
            switch(e.getActionMasked()){
                case MotionEvent.ACTION_DOWN:down[0]=e.getRawX();down[1]=e.getRawY();pos[0]=lp.x;pos[1]=lp.y;moved[0]=false;return true;
                case MotionEvent.ACTION_MOVE:
                    int dx=(int)(e.getRawX()-down[0]),dy=(int)(e.getRawY()-down[1]);
                    if(Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5))moved[0]=true;
                    if(moved[0]){lp.x=Math.max(0,pos[0]+dx);lp.y=Math.max(0,pos[1]+dy);try{wm.updateViewLayout(root,lp);}catch(Exception ignored){}}
                    return true;
                case MotionEvent.ACTION_UP:if(!moved[0])toggleMenu();return true;
            }return false;
        });

        try{wm.addView(root,lp);}catch(Exception e){root=null;}
    }

    private void toggleMenu(){
        expanded=!expanded;
        if(menu!=null)menu.setVisibility(expanded?View.VISIBLE:View.GONE);
        if(expanded){
            try{
                String ver=getPackageManager().getPackageInfo(getPackageName(),0).versionName;
                setNow("항행의자유 이동아이콘 v"+ver+" · 업데이트 버튼 준비");
            }catch(Exception ignored){}
        }else{
            collapseStations();
        }
    }
    private void collapseMenu(){expanded=false;if(menu!=null)menu.setVisibility(View.GONE);}
    private void collapseStations(){if(stationPanel!=null){stationPanel.removeAllViews();stationPanel.setVisibility(View.GONE);}}

    private void toggleLocalMp3(){
        if(player!=null&&localMode){
            try{
                if(player.isPlaying()){
                    player.pause();
                    setNow("🎵 일시정지 · "+currentName);
                }else{
                    player.start();
                    setNow("🎵 "+currentName);
                }
            }catch(Exception e){
                startAllMp3();
            }
            return;
        }
        startAllMp3();
    }

    private void showMp3Controls(){
        if(stationPanel==null)return;
        stationPanel.removeAllViews();
        stationPanel.setVisibility(View.VISIBLE);

        TextView title=station("🎵 MP3 전체곡 연속재생");
        title.setTextColor(0xFFFFD96A);
        stationPanel.addView(title);

        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        Button prev=new Button(this);
        prev.setText("◀ 이전곡");
        prev.setTextSize(11f);
        prev.setAllCaps(false);
        prev.setOnClickListener(v->previousLocalTrack());

        Button pause=new Button(this);
        pause.setText("▶ / ❚❚");
        pause.setTextSize(11f);
        pause.setAllCaps(false);
        pause.setOnClickListener(v->toggleLocalMp3());

        Button next=new Button(this);
        next.setText("다음곡 ▶");
        next.setTextSize(11f);
        next.setAllCaps(false);
        next.setOnClickListener(v->nextLocalTrack());

        row.addView(prev,new LinearLayout.LayoutParams(dp(82),dp(42)));
        row.addView(pause,new LinearLayout.LayoutParams(dp(82),dp(42)));
        row.addView(next,new LinearLayout.LayoutParams(dp(82),dp(42)));
        stationPanel.addView(row);
    }

    private boolean loadLocalTracks(){
        localTrackIds.clear();
        localTrackNames.clear();

        Uri base=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] proj={MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME};
        String selection=MediaStore.Audio.Media.DURATION+">?";
        String[] args={"10000"};
        String sort=MediaStore.Audio.Media.DISPLAY_NAME+" COLLATE NOCASE ASC";

        try(Cursor c=getContentResolver().query(base,proj,selection,args,sort)){
            if(c!=null){
                int idCol=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int nameCol=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                while(c.moveToNext()){
                    localTrackIds.add(c.getLong(idCol));
                    String n=c.getString(nameCol);
                    localTrackNames.add(n==null?"MP3":n);
                }
            }
        }catch(SecurityException e){
            toast("음악 및 오디오 권한을 허용해 주세요.");
            openApp();
            return false;
        }catch(Exception e){
            toast("MP3 목록을 불러오지 못했습니다.");
            return false;
        }
        return !localTrackIds.isEmpty();
    }

    private void startAllMp3(){
        if(localTrackIds.isEmpty() && !loadLocalTracks()){
            toast("휴대폰에서 MP3 파일을 찾지 못했습니다.");
            return;
        }
        if(localTrackIndex<0 || localTrackIndex>=localTrackIds.size()) localTrackIndex=0;
        playLocalTrack(localTrackIndex);
    }

    private void playLocalTrack(int index){
        if(localTrackIds.isEmpty()){
            if(!loadLocalTracks()){
                toast("휴대폰에서 MP3 파일을 찾지 못했습니다.");
                return;
            }
        }
        if(index<0) index=localTrackIds.size()-1;
        if(index>=localTrackIds.size()) index=0;
        localTrackIndex=index;

        Uri uri=Uri.withAppendedPath(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                String.valueOf(localTrackIds.get(localTrackIndex)));
        String name=localTrackNames.get(localTrackIndex);

        releasePlayerOnly();
        currentName=name;
        localMode=true;

        MediaPlayer mp=new MediaPlayer();
        player=mp;
        try{
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mp.setDataSource(this,uri);
            mp.setOnPreparedListener(p->{
                try{
                    p.start();
                    setNow("🎵 "+(localTrackIndex+1)+"/"+localTrackIds.size()+" · "+currentName);
                }catch(Exception ignored){}
            });
            mp.setOnCompletionListener(p->nextLocalTrack());
            mp.setOnErrorListener((p,w,e)->{
                toast("이 곡을 재생하지 못해 다음 곡으로 넘어갑니다.");
                nextLocalTrack();
                return true;
            });
            mp.prepareAsync();
            setNow("MP3 준비 중 · "+name);
        }catch(Exception e){
            nextLocalTrack();
        }
    }

    private void nextLocalTrack(){
        if(localTrackIds.isEmpty()){
            startAllMp3();
            return;
        }
        playLocalTrack((localTrackIndex+1)%localTrackIds.size());
    }

    private void previousLocalTrack(){
        if(localTrackIds.isEmpty()){
            startAllMp3();
            return;
        }
        int n=localTrackIndex-1;
        if(n<0)n=localTrackIds.size()-1;
        playLocalTrack(n);
    }

    private void showStations(boolean fm){
        if(stationPanel==null)return;stationPanel.removeAllViews();stationPanel.setVisibility(View.VISIBLE);
        String[][] list=fm?new String[][]{
            {"KBS 클래식FM","https://radio.bsod.kr/stream?stn=kbs&ch=1fm"},
            {"MBC FM4U","https://radio.bsod.kr/stream?stn=mbc&ch=fm4u"},
            {"SBS 파워FM","https://radio.bsod.kr/stream?stn=sbs&ch=powerfm"},
            {"KBS 해피FM","https://radio.bsod.kr/stream?stn=kbs&ch=2radio"}
        }:new String[][]{
            {"KBS 1라디오","https://radio.bsod.kr/stream?stn=kbs&ch=1radio"},
            {"MBC 표준FM","https://radio.bsod.kr/stream?stn=mbc&ch=sfm"},
            {"SBS 러브FM","https://radio.bsod.kr/stream?stn=sbs&ch=lovefm"}
        };
        TextView title=station("▼ "+(fm?"FM":"AM")+" 인터넷 채널 선택");title.setTextColor(0xFFFFD96A);title.setOnClickListener(v->collapseStations());stationPanel.addView(title);
        for(String[] s:list){TextView item=station("▶ "+s[0]);item.setOnClickListener(v->{playUrl(s[0],s[1]);collapseStations();});stationPanel.addView(item);}
    }

    private void playUrl(String name,String url){playUri(name,Uri.parse(url),false);}
    private void playUri(String name,Uri uri,boolean local){
        stopMedia();currentName=name==null?"재생 중":name;localMode=local;
        MediaPlayer mp=new MediaPlayer();player=mp;
        try{
            mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mp.setDataSource(this,uri);
            mp.setOnPreparedListener(p->{try{p.start();setNow((local?"🎵 ":"📻 ")+currentName);}catch(Exception ignored){}});
            mp.setOnErrorListener((p,w,e)->{toast("재생 연결에 실패했습니다.");stopMedia();return true;});
            mp.prepareAsync();
            setNow("연결 중 · "+currentName);
        }catch(Exception e){stopMedia();toast("재생을 시작하지 못했습니다.");}
    }

    private void releasePlayerOnly(){
        MediaPlayer p=player;
        player=null;
        if(p!=null){
            try{p.setOnCompletionListener(null);}catch(Exception ignored){}
            try{p.setOnErrorListener(null);}catch(Exception ignored){}
            try{p.stop();}catch(Exception ignored){}
            try{p.release();}catch(Exception ignored){}
        }
    }

    private void stopMedia(){
        releasePlayerOnly();
        currentName="";
        localMode=false;
        localTrackIndex=-1;
        if(now!=null)now.setVisibility(View.GONE);
    }
    private void setNow(String s){if(now!=null){now.setText(s);now.setVisibility(View.VISIBLE);}}

    private void openYoutube(){
        collapseStations();
        try{Intent y=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com"));y.setPackage("com.google.android.youtube");y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(y);}
        catch(Exception e){try{Intent y=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com"));y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(y);}catch(Exception x){toast("YouTube를 열 수 없습니다.");}}
    }
    private void openUpdater(){
        if(updateBusy){
            setNow("⬇ 업데이트 다운로드 진행 중…");
            return;
        }
        updateBusy=true;
        setNow("⬇ 업데이트 다운로드 시작 · 0%");

        final String apkUrl="https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/HANGHAE_MINI_MP3_FM_AM_YOUTUBE-debug.apk?v=20261002-0105";

        new Thread(() -> {
            HttpURLConnection conn=null;
            Uri outUri=null;
            try{
                URL u=new URL(apkUrl);
                conn=(HttpURLConnection)u.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent","HanghaeFreedomUpdater/1.0");
                conn.connect();

                int code=conn.getResponseCode();
                if(code<200 || code>=300){
                    final int ec=code;
                    updateHandler.post(() -> setNow("❌ 업데이트 서버 오류 HTTP "+ec));
                    return;
                }

                long total=Build.VERSION.SDK_INT>=24?conn.getContentLengthLong():conn.getContentLength();
                String fileName="항행의자유_이동아이콘_업데이트_"+System.currentTimeMillis()+".apk";

                if(Build.VERSION.SDK_INT>=29){
                    ContentValues cv=new ContentValues();
                    cv.put(MediaStore.Downloads.DISPLAY_NAME,fileName);
                    cv.put(MediaStore.Downloads.MIME_TYPE,"application/vnd.android.package-archive");
                    cv.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);
                    cv.put(MediaStore.Downloads.IS_PENDING,1);
                    outUri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,cv);
                    if(outUri==null)throw new Exception("Downloads 저장소 생성 실패");
                }else{
                    updateHandler.post(() -> setNow("❌ Android 10 이상에서 업데이트를 지원합니다."));
                    return;
                }

                try(InputStream in=conn.getInputStream();
                    OutputStream out=getContentResolver().openOutputStream(outUri,"w")){
                    if(out==null)throw new Exception("다운로드 파일 열기 실패");
                    byte[] buf=new byte[32768];
                    long done=0;
                    int n;
                    int last=-1;
                    while((n=in.read(buf))!=-1){
                        out.write(buf,0,n);
                        done+=n;
                        int pct=total>0?(int)Math.min(100,(done*100)/total):0;
                        if(pct!=last){
                            last=pct;
                            final int fp=pct;
                            final long fd=done;
                            updateHandler.post(() -> setNow("⬇ 업데이트 "+fp+"% · "+String.format(java.util.Locale.KOREA,"%.1fMB",fd/1048576.0)));
                        }
                    }
                    out.flush();
                }

                if(Build.VERSION.SDK_INT>=29){
                    ContentValues doneCv=new ContentValues();
                    doneCv.put(MediaStore.Downloads.IS_PENDING,0);
                    getContentResolver().update(outUri,doneCv,null,null);
                }

                final Uri installUri=outUri;
                updateHandler.post(() -> {
                    setNow("✅ 다운로드 100% · 설치 화면 여는 중");
                    try{
                        Intent install=new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(installUri,"application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(install);
                    }catch(Exception e){
                        setNow("✅ 다운로드 완료 · 내 파일 > 다운로드에서 APK를 눌러 설치하세요.");
                    }
                });

            }catch(Exception e){
                final String msg=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage();
                if(outUri!=null){
                    try{getContentResolver().delete(outUri,null,null);}catch(Exception ignored){}
                }
                updateHandler.post(() -> {
                    setNow("❌ 업데이트 실패 · "+msg+" · 브라우저 다운로드로 전환");
                    try{
                        Intent browser=new Intent(Intent.ACTION_VIEW,Uri.parse(apkUrl));
                        browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(browser);
                    }catch(Exception ignored){}
                });
            }finally{
                if(conn!=null)conn.disconnect();
                updateBusy=false;
            }
        },"HanghaeUpdater").start();
    }

    private void openApp(){try{Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception ignored){}}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    private void removeOverlay(){if(root!=null&&wm!=null){try{wm.removeView(root);}catch(Exception ignored){}}root=null;menu=null;stationPanel=null;anchor=null;now=null;}
    @Override public void onDestroy(){
        removeOverlay();
        stopMedia();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
