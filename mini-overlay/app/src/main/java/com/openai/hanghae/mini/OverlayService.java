package com.openai.hanghae.mini;

import android.app.*;
import android.content.*;
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
        int w="🎵 MP3".equals(text)?52:("📻 FM".equals(text)||"📡 AM".equals(text)?47:("✕".equals(text)?40:55));
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
               yt=btn("유튜브",0xEEDB1F28), stop=btn("■ 종료",0xEED52D46), close=btn("✕",0xEE4A5568);
        menu.addView(mp3);menu.addView(fm);menu.addView(am);menu.addView(yt);menu.addView(stop);menu.addView(close);bar.addView(menu);
        root.addView(bar,new LinearLayout.LayoutParams(-2,dp(46)));

        now=new TextView(this);now.setTextColor(0xFFFFE58A);now.setTextSize(10.5f);now.setPadding(dp(8),dp(2),dp(8),dp(3));now.setVisibility(View.GONE);
        root.addView(now,new LinearLayout.LayoutParams(-2,-2));

        stationPanel=new LinearLayout(this);stationPanel.setOrientation(LinearLayout.VERTICAL);stationPanel.setVisibility(View.GONE);
        root.addView(stationPanel,new LinearLayout.LayoutParams(-1,-2));

        mp3.setOnClickListener(v->{collapseStations();toggleLocalMp3();});
        fm.setOnClickListener(v->showStations(true));
        am.setOnClickListener(v->showStations(false));
        yt.setOnClickListener(v->openYoutube());
        stop.setOnClickListener(v->{stopMedia();collapseStations();collapseMenu();toast("음악 · 라디오를 종료했습니다.");});
        close.setOnClickListener(v->{collapseStations();collapseMenu();});

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

    private void toggleMenu(){expanded=!expanded;if(menu!=null)menu.setVisibility(expanded?View.VISIBLE:View.GONE);if(!expanded)collapseStations();}
    private void collapseMenu(){expanded=false;if(menu!=null)menu.setVisibility(View.GONE);}
    private void collapseStations(){if(stationPanel!=null){stationPanel.removeAllViews();stationPanel.setVisibility(View.GONE);}}

    private void toggleLocalMp3(){
        if(player!=null&&localMode){
            try{
                if(player.isPlaying()){player.pause();setNow("🎵 MP3 일시정지");}
                else{player.start();setNow("🎵 "+currentName);}
            }catch(Exception e){startLatestMp3();}
            return;
        }
        startLatestMp3();
    }

    private void startLatestMp3(){
        Uri base=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] proj={MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME};
        try(Cursor c=getContentResolver().query(base,proj,MediaStore.Audio.Media.DURATION+">?",new String[]{"10000"},MediaStore.Audio.Media.DATE_MODIFIED+" DESC")){
            if(c!=null&&c.moveToFirst()){
                long id=c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String name=c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                Uri uri=Uri.withAppendedPath(base,String.valueOf(id));
                playUri(name,uri,true);
            }else toast("휴대폰에서 MP3 파일을 찾지 못했습니다.");
        }catch(SecurityException e){toast("음악 및 오디오 권한을 허용해 주세요.");openApp();}
        catch(Exception e){toast("MP3를 재생하지 못했습니다.");}
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

    private void stopMedia(){MediaPlayer p=player;player=null;currentName="";localMode=false;if(p!=null){try{p.stop();}catch(Exception ignored){}try{p.release();}catch(Exception ignored){}}if(now!=null)now.setVisibility(View.GONE);}
    private void setNow(String s){if(now!=null){now.setText(s);now.setVisibility(View.VISIBLE);}}

    private void openYoutube(){
        collapseStations();
        try{Intent y=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com"));y.setPackage("com.google.android.youtube");y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(y);}
        catch(Exception e){try{Intent y=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com"));y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(y);}catch(Exception x){toast("YouTube를 열 수 없습니다.");}}
    }
    private void openApp(){try{Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception ignored){}}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    private void removeOverlay(){if(root!=null&&wm!=null){try{wm.removeView(root);}catch(Exception ignored){}}root=null;menu=null;stationPanel=null;anchor=null;now=null;}
    @Override public void onDestroy(){removeOverlay();stopMedia();super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
