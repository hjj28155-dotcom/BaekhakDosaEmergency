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
    // v1.0.10 adaptive UI release trigger

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
         .setContentText("MP3 · FM · AM · YouTube · AVI · MP4")
         .setOngoing(true).setOnlyAlertOnce(true);
        Intent show=new Intent(this,OverlayService.class).setAction(ACTION_SHOW);
        b.setContentIntent(PendingIntent.getService(this,NOTICE,show,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        return b.build();
    }

    private int dp(int v){return Math.max(1,Math.round(v*getResources().getDisplayMetrics().density));}
    private GradientDrawable bg(int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(1),stroke);return d;
    }

    private float cappedSp(float sp){
        float fs=getResources().getConfiguration().fontScale;
        if(fs<=0f)fs=1f;
        float capped=Math.min(fs,1.20f);
        return sp/capped;
    }

    private Button btn(String text,int color){
        Button b=new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP,cappedSp(11f));
        b.setAllCaps(false);
        b.setSingleLine(true);
        b.setEllipsize(null);
        b.setIncludeFontPadding(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(7),0,dp(7),0);
        b.setMinHeight(dp(34));
        b.setMinimumHeight(dp(34));
        b.setMinWidth(dp(54));
        b.setMinimumWidth(dp(54));
        b.setBackground(bg(color,0x66FFD35A,13));

        int minW;
        if("🚢 항행의자유".equals(text)) minW=118;
        else if("🧭 항해사앱".equals(text)) minW=104;
        else if("아이콘 숨김".equals(text)) minW=96;
        else if("업데이트".equals(text)) minW=82;
        else if("유튜브".equals(text)) minW=72;
        else if("■ 종료".equals(text)) minW=72;
        else if("🎵 MP3".equals(text)) minW=72;
        else if("📻 FM".equals(text)||"📡 AM".equals(text)) minW=64;
        else minW=64;

        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(36));
        p.setMargins(dp(4),dp(1),dp(4),dp(1));
        b.setLayoutParams(p);
        b.setMinWidth(dp(minW));
        b.setMinimumWidth(dp(minW));
        return b;
    }

    private TextView station(String text){
        TextView v=new TextView(this);v.setText(text);v.setTextColor(Color.WHITE);v.setTextSize(12f);v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(12),dp(9),dp(12),dp(9));v.setBackground(bg(0xEE08233E,0x6678BEFF,10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(3),dp(2),dp(3),dp(2));v.setLayoutParams(p);return v;
    }

    private void showOverlay(){
        if(root!=null||wm==null)return;
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(0,0,0,0);root.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.VERTICAL);bar.setGravity(Gravity.CENTER_HORIZONTAL);
        anchor=new TextView(this);anchor.setText("⚓");anchor.setTextSize(19f);anchor.setTextColor(0xFFFFD96A);anchor.setGravity(Gravity.CENTER);
        anchor.setBackground(bg(0xEE08233E,0xCCF3C954,14));anchor.setLayoutParams(new LinearLayout.LayoutParams(dp(38),dp(38)));bar.addView(anchor);

        menu=new LinearLayout(this);menu.setOrientation(LinearLayout.VERTICAL);menu.setGravity(Gravity.CENTER_HORIZONTAL);menu.setPadding(dp(3),dp(3),dp(3),dp(3));menu.setBackground(bg(0xF20A1D30,0xCCF3C954,12));menu.setVisibility(View.VISIBLE);
        Button freedom=btn("🚢 항행의자유",0xEE0C7AA8),
               navigator=btn("🧭 항해사앱",0xEE8A5B18),
               analysis=btn("📊 분석방 V4",0xEE17639A), chatgpt=btn("🤖 ChatGPT",0xEE168A5B),
               mp3=btn("🎵 MP3",0xEE1068C8), fm=btn("📻 FM",0xEE5B34D6), am=btn("📡 AM",0xEE6D2DB7),
               yt=btn("유튜브",0xEEDB1F28), avi=btn("AVI",0xEE8A4D1E), mp4=btn("MP4",0xEE1F7A5B),
               myfiles=btn("내 파일",0xEE455A64), bithumb=btn("빗썸",0xEE455A64), yesfile=btn("예스파일",0xEE455A64),
               kakao=btn("카카오톡",0xEE455A64), nh=btn("NH올원뱅크",0xEE455A64),
               hide=btn("아이콘 숨김",0xEE455A64), stop=btn("■ 종료",0xEED52D46), update=btn("업데이트",0xEE087F5B);
        menu.addView(freedom);
        // Public MASTER: only the 10 core functions live in the normal menu.
        menu.addView(mp3);menu.addView(fm);menu.addView(am);menu.addView(yt);menu.addView(avi);menu.addView(mp4);
        if(BuildConfig.NAVIGATOR_EDITION){
            menu.addView(myfiles);menu.addView(bithumb);menu.addView(yesfile);menu.addView(kakao);menu.addView(nh);
        }
        menu.addView(hide);menu.addView(stop);menu.addView(update);

        ScrollView menuScroll=new ScrollView(this);
        menuScroll.setVerticalScrollBarEnabled(true);
        menuScroll.setFillViewport(false);
        menuScroll.setVisibility(View.GONE);
        menuScroll.addView(menu,new ScrollView.LayoutParams(-1,-2));
        int screenW=getResources().getDisplayMetrics().widthPixels;
        int menuW=dp(170);
        if(screenW<dp(250))menuW=Math.max(dp(150),screenW-dp(70));
        int menuH=dp(310);
        bar.addView(menuScroll,new LinearLayout.LayoutParams(menuW,menuH));
        root.addView(bar,new LinearLayout.LayoutParams(-2,-2));

        now=new TextView(this);now.setTextColor(0xFFFFE58A);now.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP,12f);now.setPadding(dp(8),dp(4),dp(8),dp(5));now.setMaxLines(2);now.setVisibility(View.GONE);
        root.addView(now,new LinearLayout.LayoutParams(-2,-2));

        stationPanel=new LinearLayout(this);stationPanel.setOrientation(LinearLayout.VERTICAL);stationPanel.setVisibility(View.GONE);
        root.addView(stationPanel,new LinearLayout.LayoutParams(-1,-2));

        freedom.setOnClickListener(v->openFreedomApp());
        if(BuildConfig.NAVIGATOR_EDITION) navigator.setOnClickListener(v->openNavigatorApp());
        analysis.setOnClickListener(v->openAnalysisV4());
        chatgpt.setOnClickListener(v->openChatGPT());
        mp3.setOnClickListener(v->{showMp3Controls();toggleLocalMp3();});
        fm.setOnClickListener(v->showStations(true));
        am.setOnClickListener(v->showStations(false));
        yt.setOnClickListener(v->openYoutube());
        avi.setOnClickListener(v->showDownloadVideos("avi"));
        mp4.setOnClickListener(v->showDownloadVideos("mp4"));
        if(BuildConfig.NAVIGATOR_EDITION){
            myfiles.setOnClickListener(v->openExternalApp("com.sec.android.app.myfiles","내 파일","content://com.sec.android.app.myfiles.FileProvider/",""));
            bithumb.setOnClickListener(v->openExternalApp("com.btckorea.bithumb","빗썸","https://www.bithumb.com/","빗썸"));
            yesfile.setOnClickListener(v->openExternalApp("com.mnt.aos.yesfile.shortcuts","예스파일","https://www.yesfile.com/","예스파일"));
            kakao.setOnClickListener(v->openExternalApp("com.kakao.talk","카카오톡","https://www.kakaocorp.com/page/service/service/KakaoTalk","카카오톡"));
            nh.setOnClickListener(v->openExternalApp("com.nonghyup.nhallonebank","NH올원뱅크","https://www.nhbank.com/","NH올원뱅크"));
        }
        hide.setOnClickListener(v->hideOverlayOnly());
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
                case MotionEvent.ACTION_DOWN:
                    down[0]=e.getRawX();down[1]=e.getRawY();pos[0]=lp.x;pos[1]=lp.y;moved[0]=false;
                    anchor.postDelayed(()->{
                        if(!moved[0]){
                            Intent add=new Intent(OverlayService.this,MainActivity.class);
                            add.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            add.putExtra("open_extra_features",true);
                            try{startActivity(add);toast("추가기능");}catch(Exception ex){toast("추가기능을 열 수 없습니다.");}
                            moved[0]=true;
                        }
                    },650);
                    return true;
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
        if(menu!=null && menu.getParent() instanceof View) ((View)menu.getParent()).setVisibility(expanded?View.VISIBLE:View.GONE);
        if(expanded){
            if(now!=null)now.setVisibility(View.GONE);
        }else{
            collapseStations();
        }
    }
    private void collapseMenu(){expanded=false;if(menu!=null && menu.getParent() instanceof View)((View)menu.getParent()).setVisibility(View.GONE);}
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
        String[] proj;
        String selection;
        String[] args;
        String sort=MediaStore.Audio.Media.DISPLAY_NAME+" COLLATE NOCASE ASC";
        if(Build.VERSION.SDK_INT>=29){
            proj=new String[]{MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.RELATIVE_PATH};
            selection=MediaStore.Audio.Media.DURATION+">? AND "+MediaStore.Audio.Media.RELATIVE_PATH+" LIKE ?";
            args=new String[]{"10000",Environment.DIRECTORY_DOWNLOADS+"/이동아이콘/MP3/%"};
        }else{
            proj=new String[]{MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME};
            selection=MediaStore.Audio.Media.DURATION+">?";
            args=new String[]{"10000"};
        }

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
        if(localTrackIds.isEmpty() && Build.VERSION.SDK_INT>=29){
            try(Cursor c=getContentResolver().query(
                    base,
                    new String[]{MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME},
                    MediaStore.Audio.Media.DURATION+">?",
                    new String[]{"10000"},
                    sort)){
                if(c!=null){
                    int idCol=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int nameCol=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                    while(c.moveToNext()){
                        localTrackIds.add(c.getLong(idCol));
                        String n=c.getString(nameCol);
                        localTrackNames.add(n==null?"MP3":n);
                    }
                }
            }catch(Exception ignored){}
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

    private final ArrayList<Uri> videoUris=new ArrayList<>();
    private final ArrayList<String> videoNames=new ArrayList<>();
    private final ArrayList<String> videoMimes=new ArrayList<>();
    private int videoIndex=0;
    private String videoExt="mp4";

    private void showDownloadVideos(String ext){
        if(stationPanel==null)return;
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO)!=android.content.pm.PackageManager.PERMISSION_GRANTED){toast("AVI · MP4 사용을 위해 동영상 권한을 허용해 주세요.");openApp();return;}
        videoUris.clear();videoNames.clear();videoMimes.clear();videoExt=ext;
        Uri base=MediaStore.Files.getContentUri("external");
        String[] projection={MediaStore.Files.FileColumns._ID,MediaStore.Files.FileColumns.DISPLAY_NAME,MediaStore.Files.FileColumns.MIME_TYPE};
        String selection;String[] args;
        if(Build.VERSION.SDK_INT>=29){selection=MediaStore.Files.FileColumns.DISPLAY_NAME+" LIKE ?";args=new String[]{"%."+ext};}
        else{selection=MediaStore.Files.FileColumns.DISPLAY_NAME+" LIKE ?";args=new String[]{"%."+ext};}
        try(Cursor cur=getContentResolver().query(base,projection,selection,args,MediaStore.Files.FileColumns.DISPLAY_NAME+" COLLATE NOCASE ASC")){
            if(cur!=null){int id=cur.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID),nm=cur.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME),mm=cur.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                while(cur.moveToNext()){videoUris.add(Uri.withAppendedPath(base,String.valueOf(cur.getLong(id))));String n=cur.getString(nm);videoNames.add(n==null?ext.toUpperCase(Locale.KOREA):n);String m=mm>=0?cur.getString(mm):null;videoMimes.add(m==null||m.isEmpty()?("mp4".equals(ext)?"video/mp4":"video/*"):m);}}
        }catch(Exception e){toast(ext.toUpperCase(Locale.KOREA)+" 목록을 불러오지 못했습니다.");return;}
        videoIndex=0;renderVideoBar();
    }

    private void renderVideoBar(){
        stationPanel.removeAllViews();stationPanel.setVisibility(View.VISIBLE);
        if(videoUris.isEmpty()){TextView e=station(videoExt.toUpperCase(Locale.KOREA)+" 파일이 없습니다.");e.setTextColor(0xFFFFB8B8);stationPanel.addView(e);return;}
        TextView current=station("🎬 "+videoExt.toUpperCase(Locale.KOREA)+" · "+videoNames.get(videoIndex));current.setTextColor(0xFFFFD96A);stationPanel.addView(current);
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER);
        Button prev=new Button(this);prev.setText("◀ 이전");prev.setTextSize(10f);prev.setAllCaps(false);
        Button play=new Button(this);play.setText("▶ 재생 / ■ 중지");play.setTextSize(10f);play.setAllCaps(false);
        Button next=new Button(this);next.setText("다음 ▶");next.setTextSize(10f);next.setAllCaps(false);
        prev.setOnClickListener(v->{videoIndex=(videoIndex-1+videoUris.size())%videoUris.size();renderVideoBar();});
        next.setOnClickListener(v->{videoIndex=(videoIndex+1)%videoUris.size();renderVideoBar();});
        play.setOnClickListener(v->openVideoFile(videoUris.get(videoIndex),videoMimes.get(videoIndex)));
        row.addView(prev,new LinearLayout.LayoutParams(dp(88),dp(42)));row.addView(play,new LinearLayout.LayoutParams(dp(100),dp(42)));row.addView(next,new LinearLayout.LayoutParams(dp(88),dp(42)));stationPanel.addView(row);
    }

    private void openVideoFile(Uri uri,String mime){
        try{
            Intent i=new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri,mime);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            setNow("🎬 동영상 재생 앱을 열었습니다.");
        }catch(Exception e){
            try{
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(uri,"video/*");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }catch(Exception x){
                toast("이 동영상을 재생할 앱이 없습니다.");
            }
        }
    }

    private String[][] activeStations=null;
    private int stationIndex=0;
    private boolean stationFm=true;

    private void showStations(boolean fm){
        stationFm=fm;
        activeStations=fm?new String[][]{
            {"KBS 클래식FM","https://radio.bsod.kr/stream?stn=kbs&ch=1fm"},
            {"MBC FM4U","https://radio.bsod.kr/stream?stn=mbc&ch=fm4u"},
            {"SBS 파워FM","https://radio.bsod.kr/stream?stn=sbs&ch=powerfm"},
            {"KBS 해피FM","https://radio.bsod.kr/stream?stn=kbs&ch=2radio"}
        }:new String[][]{
            {"KBS 1라디오","https://radio.bsod.kr/stream?stn=kbs&ch=1radio"},
            {"MBC 표준FM","https://radio.bsod.kr/stream?stn=mbc&ch=sfm"},
            {"SBS 러브FM","https://radio.bsod.kr/stream?stn=sbs&ch=lovefm"}
        };
        stationIndex=Math.max(0,Math.min(stationIndex,activeStations.length-1));
        renderStationBar();
    }

    private void renderStationBar(){
        if(stationPanel==null||activeStations==null||activeStations.length==0)return;
        stationPanel.removeAllViews();stationPanel.setVisibility(View.VISIBLE);
        TextView current=station((stationFm?"📻 FM · ":"📡 AM · ")+activeStations[stationIndex][0]);
        current.setTextColor(0xFFFFD96A);stationPanel.addView(current);
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER);
        Button prev=new Button(this);prev.setText("◀ 이전채널");prev.setTextSize(10f);prev.setAllCaps(false);
        Button play=new Button(this);play.setText("▶ 재생 / ■ 중지");play.setTextSize(10f);play.setAllCaps(false);
        Button next=new Button(this);next.setText("다음채널 ▶");next.setTextSize(10f);next.setAllCaps(false);
        prev.setOnClickListener(v->{stationIndex=(stationIndex-1+activeStations.length)%activeStations.length;renderStationBar();});
        next.setOnClickListener(v->{stationIndex=(stationIndex+1)%activeStations.length;renderStationBar();});
        play.setOnClickListener(v->{if(player!=null){stopMedia();renderStationBar();}else playUrl(activeStations[stationIndex][0],activeStations[stationIndex][1]);});
        row.addView(prev,new LinearLayout.LayoutParams(dp(88),dp(42)));
        row.addView(play,new LinearLayout.LayoutParams(dp(100),dp(42)));
        row.addView(next,new LinearLayout.LayoutParams(dp(88),dp(42)));
        stationPanel.addView(row);
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

    private void openFreedomApp(){
        collapseStations();
        collapseMenu();
        // 확정 시작점: 두루마리부터 시작하는 항행의자유 온보딩.
        try{
            Intent web=new Intent(Intent.ACTION_VIEW,Uri.parse("https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/?entry=moveicon-onboarding"));
            web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(web);
            setNow("🚢 항행의자유 신규설치 안내를 엽니다.");
        }catch(Exception e){
            toast("항행의자유 신규설치 안내를 열 수 없습니다.");
        }
    }

    private void openNavigatorApp(){
        collapseStations();
        collapseMenu();

        if(launchPackage("com.baekhak.dosa.v3","🧭 항해사앱 실행")) return;
        if(launchByLabel("항해사","🧭 항해사앱 실행")) return;

        try{
            Intent web=new Intent(Intent.ACTION_VIEW,Uri.parse("https://drive.google.com/file/d/12tYTGlkKPraVz1PEeqOszrGmvTp9Juue/view?usp=drivesdk"));
            web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(web);
            setNow("🧭 구글드라이브의 항해사앱 최신본을 엽니다.");
        }catch(Exception e){
            toast("항해사앱을 열 수 없습니다.");
        }
    }

    private void openAnalysisV4(){
        collapseStations();
        collapseMenu();
        try{
            Intent i=new Intent();
            i.setClassName("com.navigator.analysis.v4","com.navigator.analysis.v4.MainActivity");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            setNow("📊 분석방 V4 실행");
            return;
        }catch(Exception ignored){}
        if(launchPackage("com.navigator.analysis.v4","📊 분석방 V4 실행")) return;
        toast("분석방 V4를 실행할 수 없습니다.");
    }

    private void openChatGPT(){
        collapseStations();
        collapseMenu();
        if(launchPackage("com.openai.chatgpt","🤖 ChatGPT 실행")) return;
        toast("ChatGPT 앱이 설치되어 있지 않거나 실행할 수 없습니다.");
    }

    private boolean launchPackage(String pkg,String status){
        try{
            Intent i=getPackageManager().getLaunchIntentForPackage(pkg);
            if(i==null)return false;
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            setNow(status);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    private boolean launchByLabel(String keyword,String status){
        try{
            android.content.pm.PackageManager pm=getPackageManager();
            Intent q=new Intent(Intent.ACTION_MAIN);
            q.addCategory(Intent.CATEGORY_LAUNCHER);
            java.util.List<android.content.pm.ResolveInfo> list=pm.queryIntentActivities(q,0);
            if(list==null)return false;

            for(android.content.pm.ResolveInfo ri:list){
                if(ri==null || ri.activityInfo==null || ri.activityInfo.applicationInfo==null)continue;
                String pkg=ri.activityInfo.packageName;
                if(getPackageName().equals(pkg))continue;

                CharSequence appLabel=ri.activityInfo.applicationInfo.loadLabel(pm);
                CharSequence activityLabel=ri.loadLabel(pm);
                String a=appLabel==null?"":appLabel.toString();
                String b=activityLabel==null?"":activityLabel.toString();

                if(a.contains(keyword)||b.contains(keyword)){
                    Intent i=pm.getLaunchIntentForPackage(pkg);
                    if(i==null){
                        i=new Intent(Intent.ACTION_MAIN);
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        i.setClassName(pkg,ri.activityInfo.name);
                    }
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    setNow(status);
                    return true;
                }
            }
        }catch(Exception ignored){}
        return false;
    }

    private void hideOverlayOnly(){
        collapseStations();
        collapseMenu();
        removeOverlay();
        try{
            NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if(nm!=null)nm.notify(NOTICE,notification());
        }catch(Exception ignored){}
        toast("이동아이콘을 숨겼습니다. 알림을 누르면 다시 나타납니다.");
    }

    private void openYoutube(){
        collapseStations();
        try{Intent y=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com"));y.setPackage("com.google.android.youtube");y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(y);}
        catch(Exception e){try{Intent y=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com"));y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(y);}catch(Exception x){toast("YouTube를 열 수 없습니다.");}}
    }
    private void openExternalApp(String pkg,String label,String fallbackUrl,String keyword){
        collapseStations();
        collapseMenu();
        if(pkg!=null && pkg.length()>0 && launchPackage(pkg,"✅ "+label+" 실행")) return;
        String k=(keyword==null||keyword.length()==0)?label:keyword;
        if(launchByLabel(k,"✅ "+label+" 실행")) return;
        if(fallbackUrl!=null && fallbackUrl.startsWith("http")){
            try{
                Intent web=new Intent(Intent.ACTION_VIEW,Uri.parse(fallbackUrl));
                web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(web);
                setNow("🌐 "+label+" 페이지를 엽니다.");
                return;
            }catch(Exception ignored){}
        }
        toast(label+"을(를) 열 수 없습니다.");
    }

    private void openUpdater(){
        // Installed users update inside the app: download progress -> Android installer.
        openUpdaterLegacy();
    }

    private void openUpdaterLegacy(){
        if(updateBusy){
            setNow("⬇ 업데이트 다운로드 진행 중…");
            return;
        }
        updateBusy=true;
        setNow("⬇ 업데이트 다운로드 시작 · 0%");

        final String apkUrl=BuildConfig.NAVIGATOR_EDITION
                ?"https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/HANGHAE_NAVIGATOR_MINI-debug.apk?v=1.0.69"
                :"https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/HANGHAE_MINI_MP3_FM_AM_YOUTUBE-debug.apk?v=1.0.69";

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
