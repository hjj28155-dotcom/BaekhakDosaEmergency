package com.baekhak.centralcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import org.json.JSONObject;

public class OverlayControlService extends Service {
    private static final String CHANNEL = "v3_overlay_control";
    private static final int NOTICE = 43140;
    public static final String ACTION_SHOW = "com.baekhak.centralcontrol.overlay.SHOW";
    public static final String ACTION_HIDE = "com.baekhak.centralcontrol.overlay.HIDE";
    public static final String ACTION_SUSPEND = "com.baekhak.centralcontrol.overlay.SUSPEND";
    public static final String ACTION_RESUME = "com.baekhak.centralcontrol.overlay.RESUME";
    public static final String ACTION_REFRESH = "com.baekhak.centralcontrol.overlay.REFRESH";
    private static final String PREFS = "overlay_control";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_X = "last_x";
    private static final String KEY_Y = "last_y";

    private WindowManager wm;
    private LinearLayout root;
    private LinearLayout channelPanel;
    private WindowManager.LayoutParams channelLp;
    private LinearLayout buttonGroup;
    private TextView anchorView;
    private LinearLayout topMediaBar;
    private TextView currentTrackView;
    private LinearLayout playlistPanel;
    private WindowManager.LayoutParams topMediaLp;
    private WindowManager.LayoutParams overlayLp;
    private boolean expanded = false;
    private boolean suspended = false;
    private String currentMediaMode = "MP3";
    private String currentRadioName = "";
    private String currentRadioUrl = "";
    private int currentRadioIndex = 0;
    private boolean radioPlaying = false;
    private static final String[][] FM_STATIONS = {{"KBS 클래식FM","https://radio.bsod.kr/stream?stn=kbs&ch=1fm"},{"MBC FM4U","https://radio.bsod.kr/stream?stn=mbc&ch=fm4u"},{"SBS 파워FM","https://radio.bsod.kr/stream?stn=sbs&ch=powerfm"},{"KBS 해피FM","https://radio.bsod.kr/stream?stn=kbs&ch=2radio"}};
    private static final String[][] AM_STATIONS = {{"KBS 1라디오 · 뉴스/시사","https://radio.bsod.kr/stream?stn=kbs&ch=1radio"},{"MBC 표준FM · 뉴스/교양","https://radio.bsod.kr/stream?stn=mbc&ch=sfm"},{"SBS 러브FM · 뉴스/생활","https://radio.bsod.kr/stream?stn=sbs&ch=lovefm"}};
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable musicStateTicker = new Runnable() {
        @Override public void run() {
            updateCurrentTrackLabel();
            handler.postDelayed(this, 850);
        }
    };

    private final Runnable permissionWatcher = new Runnable() {
        @Override public void run() {
            if (suspended) { removeOverlay(); return; }
            if (!getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_ENABLED, true)) {
                removeOverlay();
                stopSelf();
                return;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(OverlayControlService.this)) {
                showOverlay();
            } else {
                removeOverlay();
                handler.postDelayed(this, 700);
            }
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(NOTICE, serviceNotification());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        currentMediaMode = getSharedPreferences(PREFS, MODE_PRIVATE).getString("media_mode", "MP3");
        if (getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_ENABLED, true)) handler.post(permissionWatcher);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_SHOW : intent.getAction();
        if (ACTION_SUSPEND.equals(action)) {
            suspended = true;
            removeOverlay();
            return START_STICKY;
        }
        if (ACTION_RESUME.equals(action)) {
            suspended = false;
            handler.removeCallbacks(permissionWatcher);
            handler.post(permissionWatcher);
            return START_STICKY;
        }
        if (ACTION_REFRESH.equals(action)) {
            removeOverlay();
            handler.removeCallbacks(permissionWatcher);
            handler.post(permissionWatcher);
            return START_STICKY;
        }
        if (ACTION_HIDE.equals(action)) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply();
            handler.removeCallbacksAndMessages(null);
            removeOverlay();
            try { stopForeground(true); } catch (Exception ignored) {}
            stopSelf();
            return START_NOT_STICKY;
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, true).apply();
        handler.removeCallbacks(permissionWatcher);
        handler.post(permissionWatcher);
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CHANNEL, "항행의자유 상단 미니바", NotificationManager.IMPORTANCE_LOW);
            c.setDescription("MP3·FM·AM·유튜브 상시 제어 이동아이콘");
            c.setSound(null, null);
            NotificationManager n = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (n != null) n.createNotificationChannel(c);
        }
    }

    private Notification serviceNotification() {
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        b.setSmallIcon(R.drawable.v3_ship_icon)
                .setContentTitle("항행의자유 미니바 사용 중")
                .setContentText("MP3 · FM · AM · 유튜브를 이동아이콘에서 바로 사용합니다.")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_SERVICE);
        Intent open = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (open != null) b.setContentIntent(PendingIntent.getActivity(this, NOTICE, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        return b.build();
    }

    private int dp(int value) {
        return Math.max(1, Math.round(value * getResources().getDisplayMetrics().density));
    }

    private GradientDrawable bg(int color, int strokeColor, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        d.setStroke(dp(1), strokeColor);
        return d;
    }

    private Button button(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(11.5f);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(8), 0, dp(8), 0);
        b.setMinHeight(0); b.setMinimumHeight(0); b.setMinWidth(0); b.setMinimumWidth(0);
        b.setBackground(bg(color, 0x66FFD35A, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(112), dp(38));
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        b.setLayoutParams(lp);
        return b;
    }

    private TextView stationButton(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(12f);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(12), dp(9), dp(12), dp(9));
        v.setBackground(bg(0xEE08233E, 0x6678BEFF, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(3), dp(2), dp(3), dp(2));
        v.setLayoutParams(lp);
        return v;
    }

    private void showOverlay() {
        if (root != null || wm == null) return;

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.TOP);
        root.setPadding(dp(4), dp(4), dp(4), dp(4));
        root.setBackground(bg(0xF20A1D30, 0xCCF3C954, 14));

        LinearLayout mainColumn = new LinearLayout(this);
        mainColumn.setOrientation(LinearLayout.VERTICAL);
        mainColumn.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(mainColumn);

        TextView anchor = new TextView(this);
        anchorView = anchor;
        anchor.setText("⚓"); anchor.setTextSize(22f); anchor.setTextColor(0xFFFFD96A);
        anchor.setGravity(Gravity.CENTER); anchor.setBackground(bg(0xEE08233E, 0xCCF3C954, 12));
        anchor.setLayoutParams(new LinearLayout.LayoutParams(dp(112), dp(42)));
        mainColumn.addView(anchor);

        buttonGroup = new LinearLayout(this);
        buttonGroup.setOrientation(LinearLayout.VERTICAL);
        buttonGroup.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonGroup.setVisibility(View.GONE);

        Button freedom = button("항행의자유", 0xEE263238);
        Button navigator = button("항해사앱", 0xEE263238);
        Button analysis = button("분석방 V4", 0xEE263238);
        Button chatgpt = button("ChatGPT", 0xEE263238);
        Button music = button("🎵 MP3", 0xEE1068C8);
        Button fm = button("📻 FM", 0xEE5B34D6);
        Button am = button("📡 AM", 0xEE6D2DB7);
        Button youtube = button("유튜브", 0xEEDB1F28);
        Button avi = button("AVI", 0xEE37474F);
        Button mp4 = button("MP4", 0xEE37474F);
        Button hideIcon = button("아이콘 숨김", 0xEE4A5568);
        Button stop = button("■ 종료", 0xEED52D46);
        Button update = button("업데이트", 0xEE1E88E5);
        Button[] all = {freedom,navigator,analysis,chatgpt,music,fm,am,youtube,avi,mp4};
        for (Button b : all) buttonGroup.addView(b);
        addCustomAppButtons(buttonGroup);
        buttonGroup.addView(hideIcon); buttonGroup.addView(stop); buttonGroup.addView(update);
        mainColumn.addView(buttonGroup);

        // V1040: media list is a separate fixed horizontal bar at the phone top.
        createTopMediaBar();
        handler.removeCallbacks(musicStateTicker); handler.post(musicStateTicker);

        channelPanel = new LinearLayout(this);
        channelPanel.setOrientation(LinearLayout.VERTICAL);
        channelPanel.setPadding(dp(4), dp(2), dp(4), dp(2));
        channelPanel.setMinimumWidth(dp(155));
        channelPanel.setVisibility(View.GONE);

        freedom.setOnClickListener(v -> {
            try {
                Intent open = new Intent(this, MainActivity.class);
                open.putExtra("v3_play_install_guide", true);
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(open);
            } catch (Exception e) { toast("항행의자유를 열 수 없습니다."); }
        });
        navigator.setOnClickListener(v -> launchNavigatorApp());
        analysis.setOnClickListener(v -> launchExplicitOrPackage("com.navigator.analysis.v4", "com.navigator.analysis.v4.MainActivity", "분석방 V4를 실행할 수 없습니다."));
        chatgpt.setOnClickListener(v -> launchPackageOrUrl("com.openai.chatgpt", "https://chatgpt.com"));
        music.setOnClickListener(v -> safeMediaClick(() -> {
            stopVideoSafely(); stopRadioSafely(); collapseStations();
            setMediaMode("MP3", "");
            sendMusicAction(MusicPlaybackService.ACTION_PLAY);
        }, "MP3를 실행하지 못했습니다."));
        fm.setOnClickListener(v -> safeMediaClick(() -> { stopVideoSafely(); switchRadioMode("FM"); }, "FM을 실행하지 못했습니다."));
        am.setOnClickListener(v -> safeMediaClick(() -> { stopVideoSafely(); switchRadioMode("AM"); }, "AM을 실행하지 못했습니다."));
        youtube.setOnClickListener(v -> launchPackageOrUrl("com.google.android.youtube", "https://www.youtube.com"));
        avi.setOnClickListener(v -> safeMediaClick(() -> { stopAllAudioForMediaSwitch(); setMediaMode("AVI", "Movies 자동검색"); openVideo("video/x-msvideo"); }, "AVI를 실행하지 못했습니다."));
        mp4.setOnClickListener(v -> safeMediaClick(() -> { stopAllAudioForMediaSwitch(); setMediaMode("MP4", "Movies 자동검색"); openVideo("video/mp4"); }, "MP4를 실행하지 못했습니다."));
        hideIcon.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply();
            handler.removeCallbacksAndMessages(null); removeOverlay(); stopSelf();
        });
        update.setOnClickListener(v -> {
            try {
                suspended = true;
                removeOverlay();
                Intent open = new Intent(this, OverlayUpdateActivity.class);
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(open);
            } catch (Exception e) { suspended=false; toast("업데이트 화면을 열 수 없습니다."); }
        });
        stop.setOnClickListener(v -> {
            stopRadio();
            try {
                Intent i = new Intent(this, MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_STOP);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
            } catch (Exception ignored) {}
            collapseStations(); expanded = false;
            if (buttonGroup != null) buttonGroup.setVisibility(View.GONE);
            toast("음악 · 라디오를 종료했습니다.");
        });

        if (Build.VERSION.SDK_INT >= 26) overlayLp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        else overlayLp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        overlayLp.gravity = Gravity.TOP | Gravity.START;

        if (Build.VERSION.SDK_INT >= 26) channelLp = new WindowManager.LayoutParams(
                dp(180), WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        else channelLp = new WindowManager.LayoutParams(
                dp(180), WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        channelLp.gravity = Gravity.TOP | Gravity.START;

        android.content.SharedPreferences posPrefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        overlayLp.x = posPrefs.getInt(KEY_X, dp(8));
        overlayLp.y = posPrefs.getInt(KEY_Y, dp(12));

        final float[] downRaw = new float[2]; final int[] downPos = new int[2]; final int[] downTopY = new int[1]; final boolean[] moved = new boolean[1]; final long[] downTime=new long[1];
        anchor.setOnLongClickListener(v -> {
            try { Intent e=new Intent(this, OverlayAppEditorActivity.class); e.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(e); return true; }
            catch(Exception ex){ toast("아이콘 편집을 열 수 없습니다."); return true; }
        });
        anchor.setOnTouchListener((v, event) -> {
            if (overlayLp == null || wm == null) return false;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRaw[0]=event.getRawX(); downRaw[1]=event.getRawY(); downPos[0]=overlayLp.x; downPos[1]=overlayLp.y;
                    downTopY[0] = topMediaLp == null ? 0 : topMediaLp.y;
                    moved[0]=false; downTime[0]=System.currentTimeMillis(); return true;
                case MotionEvent.ACTION_MOVE:
                    int dx=(int)(event.getRawX()-downRaw[0]), dy=(int)(event.getRawY()-downRaw[1]);
                    if (Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5)) moved[0]=true;
                    if (moved[0]) {
                        overlayLp.x=Math.max(0,downPos[0]+dx); overlayLp.y=Math.max(0,downPos[1]+dy);
                        try { wm.updateViewLayout(root,overlayLp); } catch(Exception ignored){}
                        if (topMediaLp != null && topMediaBar != null) {
                            topMediaLp.y = Math.max(0, downTopY[0] + dy);
                            try { wm.updateViewLayout(topMediaBar, topMediaLp); } catch(Exception ignored){}
                        }
                        updateSidePanelPosition();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (moved[0]) {
                        android.content.SharedPreferences.Editor ed=getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_X, overlayLp.x).putInt(KEY_Y, overlayLp.y);
                        if (topMediaLp != null) ed.putInt("top_bar_y", topMediaLp.y);
                        ed.apply();
                    } else if(System.currentTimeMillis()-downTime[0] >= 650){ try{Intent e=new Intent(this,OverlayAppEditorActivity.class);e.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(e);}catch(Exception ex){toast("아이콘 편집을 열 수 없습니다.");} } else toggleExpanded();
                    return true;
            }
            return false;
        });
        try { wm.addView(root, overlayLp); } catch (Exception e) { root=null; channelPanel=null; buttonGroup=null; anchorView=null; overlayLp=null; }
    }


    private void createTopMediaBar() {
        if (wm == null || topMediaBar != null) return;
        topMediaBar = new LinearLayout(this);
        topMediaBar.setOrientation(LinearLayout.VERTICAL);
        topMediaBar.setGravity(Gravity.CENTER_VERTICAL);
        topMediaBar.setPadding(dp(7), dp(4), dp(7), dp(4));
        topMediaBar.setBackground(bg(0xF20A1D30, 0xCCF3C954, 10));

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        TextView prev = topControl("⏮ 이전곡");
        TextView play = topControl("▶ 재생/일시정지");
        TextView next = topControl("다음곡 ⏭");
        prev.setTag("prev_control"); play.setTag("play_control"); next.setTag("next_control");
        controls.addView(prev); controls.addView(play); controls.addView(next);
        topMediaBar.addView(controls, new LinearLayout.LayoutParams(-1, dp(38)));

        currentTrackView = new TextView(this);
        currentTrackView.setTextColor(0xFFFFE58A);
        currentTrackView.setTextSize(13.5f);
        currentTrackView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        currentTrackView.setSingleLine(true);
        currentTrackView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        currentTrackView.setMarqueeRepeatLimit(-1);
        currentTrackView.setSelected(true);
        currentTrackView.setGravity(Gravity.CENTER_VERTICAL);
        currentTrackView.setPadding(dp(9), 0, dp(9), 0);
        topMediaBar.addView(currentTrackView, new LinearLayout.LayoutParams(-1, dp(34)));

        playlistPanel = new LinearLayout(this); // kept for compatibility; V1045 title-only second row.
        playlistPanel.setVisibility(View.GONE);
        topMediaBar.addView(playlistPanel, new LinearLayout.LayoutParams(1,1));

        prev.setOnClickListener(v -> mediaPrevious());
        next.setOnClickListener(v -> mediaNext());
        play.setOnClickListener(v -> mediaPlayPause());

        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        topMediaLp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, dp(80), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        topMediaLp.gravity = Gravity.TOP | Gravity.START;
        topMediaLp.x = 0;
        topMediaLp.y = getSharedPreferences(PREFS, MODE_PRIVATE).getInt("top_bar_y", 0);
        final float[] topDown = new float[2]; final int[] topStartY = new int[1]; final int[] iconStartY = new int[1]; final boolean[] moved = new boolean[1];
        topMediaBar.setOnTouchListener((v, e) -> {
            if (topMediaLp == null || wm == null) return false;
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    topDown[0]=e.getRawX(); topDown[1]=e.getRawY(); topStartY[0]=topMediaLp.y; iconStartY[0]=overlayLp==null?0:overlayLp.y; moved[0]=false; return false;
                case MotionEvent.ACTION_MOVE:
                    int dy=Math.round(e.getRawY()-topDown[1]);
                    if(Math.abs(dy)>dp(7)){ moved[0]=true; topMediaLp.y=Math.max(0,topStartY[0]+dy); try{wm.updateViewLayout(topMediaBar,topMediaLp);}catch(Exception ignored){}
                        if(overlayLp!=null&&root!=null){overlayLp.y=Math.max(0,iconStartY[0]+dy);try{wm.updateViewLayout(root,overlayLp);}catch(Exception ignored){} updateSidePanelPosition();} return true; }
                    return false;
                case MotionEvent.ACTION_UP:
                    if(moved[0]){android.content.SharedPreferences.Editor ed=getSharedPreferences(PREFS,MODE_PRIVATE).edit().putInt("top_bar_y",topMediaLp.y);if(overlayLp!=null)ed.putInt(KEY_X,overlayLp.x).putInt(KEY_Y,overlayLp.y);ed.apply();return true;} return false;
            } return false;
        });
        updateControlLabels();
        try { wm.addView(topMediaBar, topMediaLp); }
        catch (Exception e) { topMediaBar=null; currentTrackView=null; playlistPanel=null; topMediaLp=null; }
    }

    private TextView topControl(String text) {
        TextView v=new TextView(this); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(12f); v.setGravity(Gravity.CENTER);
        v.setBackground(bg(0xEE163955,0x8878BEFF,8)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(34),1f); lp.setMargins(dp(2),dp(2),dp(2),dp(2)); v.setLayoutParams(lp); return v;
    }

    private void sendMusicAction(String action) {
        currentMediaMode="MP3";
        try { Intent i=new Intent(this,MusicPlaybackService.class).setAction(action); if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i); }
        catch(Exception e){toast("MP3 조작을 실행하지 못했습니다.");}
    }

    private void setMediaMode(String mode, String title) {
        currentMediaMode=mode;
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("media_mode",mode).putString("media_title",title==null?"":title).apply();
        if(currentTrackView!=null) currentTrackView.setText(mode+" · "+(title==null||title.isEmpty()?"자동 연결":title));
        updateControlLabels();
    }

    private void updateControlLabels() {
        if (topMediaBar == null) return;
        View controlsView = topMediaBar.getChildAt(0);
        if (!(controlsView instanceof LinearLayout)) return;
        LinearLayout c=(LinearLayout)controlsView;
        if(c.getChildCount()<3)return;
        TextView prev=(TextView)c.getChildAt(0), play=(TextView)c.getChildAt(1), next=(TextView)c.getChildAt(2);
        if("FM".equals(currentMediaMode)||"AM".equals(currentMediaMode)){ prev.setText("⏮ 이전채널"); next.setText("다음채널 ⏭"); }
        else if("MP4".equals(currentMediaMode)||"AVI".equals(currentMediaMode)){ prev.setText("⏮ 이전영상"); next.setText("다음영상 ⏭"); }
        else { prev.setText("⏮ 이전곡"); next.setText("다음곡 ⏭"); }
        boolean active=false;
        if("FM".equals(currentMediaMode)||"AM".equals(currentMediaMode)) active=radioPlaying;
        else if("MP3".equals(currentMediaMode)) { try{active=new JSONObject(MusicPlaybackService.snapshot(this)).optBoolean("playing",false);}catch(Exception ignored){} }
        else if("MP4".equals(currentMediaMode)||"AVI".equals(currentMediaMode)) active=getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean("video_playing",false);
        play.setText(active?"■ 중지":"▶ 재생");
    }

    private void switchRadioMode(String mode){
        stopMusicForMediaSwitch(); stopRadioSafely(); collapseStations();
        String[][] list="AM".equals(mode)?AM_STATIONS:FM_STATIONS;
        int saved=getSharedPreferences(PREFS,MODE_PRIVATE).getInt("radio_index_"+mode,0);
        currentRadioIndex=Math.max(0,Math.min(saved,list.length-1));
        playRadioAs(mode,list[currentRadioIndex][0],list[currentRadioIndex][1]);
    }
    private void radioStep(int delta){
        String mode=("AM".equals(currentMediaMode)?"AM":"FM"); String[][] list="AM".equals(mode)?AM_STATIONS:FM_STATIONS;
        currentRadioIndex=(currentRadioIndex+delta+list.length)%list.length;
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putInt("radio_index_"+mode,currentRadioIndex).apply();
        stopRadioSafely(); playRadioAs(mode,list[currentRadioIndex][0],list[currentRadioIndex][1]);
    }
    private void mediaPrevious(){
        if("MP3".equals(currentMediaMode)){sendMusicAction(MusicPlaybackService.ACTION_PREV);return;}
        if("FM".equals(currentMediaMode)||"AM".equals(currentMediaMode)){radioStep(-1);return;}
        if("MP4".equals(currentMediaMode)||"AVI".equals(currentMediaMode)){openVideoStep(currentMediaMode,-1);return;}
    }
    private void mediaNext(){
        if("MP3".equals(currentMediaMode)){sendMusicAction(MusicPlaybackService.ACTION_NEXT);return;}
        if("FM".equals(currentMediaMode)||"AM".equals(currentMediaMode)){radioStep(1);return;}
        if("MP4".equals(currentMediaMode)||"AVI".equals(currentMediaMode)){openVideoStep(currentMediaMode,1);return;}
    }
    private void mediaPlayPause(){
        if("FM".equals(currentMediaMode)||"AM".equals(currentMediaMode)){
            if(radioPlaying){ stopRadioSafely(); setMediaMode(currentMediaMode, currentRadioName+" · 정지"); }
            else { String mode=currentMediaMode; String[][] list="AM".equals(mode)?AM_STATIONS:FM_STATIONS; if(currentRadioName==null||currentRadioName.isEmpty()){int ix=Math.max(0,Math.min(currentRadioIndex,list.length-1)); currentRadioName=list[ix][0];currentRadioUrl=list[ix][1];} playRadioAs(mode,currentRadioName,currentRadioUrl); }
            updateControlLabels(); return;
        }
        if("MP4".equals(currentMediaMode)||"AVI".equals(currentMediaMode)){
            boolean playing=getSharedPreferences(PREFS,MODE_PRIVATE).getBoolean("video_playing",false);
            if(playing){ try{Intent i=new Intent(this,LocalMediaPickerActivity.class);i.putExtra(LocalMediaPickerActivity.EXTRA_MIME,"AVI".equals(currentMediaMode)?"video/x-msvideo":"video/mp4");i.putExtra("control","stop");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);}catch(Exception ignored){} getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean("video_playing",false).apply(); }
            else openVideoStep(currentMediaMode,0);
            updateControlLabels(); return;
        }
        try{JSONObject st=new JSONObject(MusicPlaybackService.snapshot(this)); if(st.optBoolean("playing",false)){ sendMusicAction(MusicPlaybackService.ACTION_STOP); if(currentTrackView!=null){currentTrackView.setSelected(false);currentTrackView.setText("MP3 · 정지");} } else sendMusicAction(MusicPlaybackService.ACTION_PLAY);}catch(Exception e){sendMusicAction(MusicPlaybackService.ACTION_PLAY);} updateControlLabels();
    }

    private void stopVideoSafely(){
        try{
            Intent i=new Intent(this,LocalMediaPickerActivity.class);
            i.putExtra("control","stop");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }catch(Exception ignored){}
        getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean("video_playing",false).apply();
    }
    private void openVideoStep(String mode,int delta){
        try{Intent i=new Intent(this,LocalMediaPickerActivity.class); i.putExtra(LocalMediaPickerActivity.EXTRA_MIME,"AVI".equals(mode)?"video/x-msvideo":"video/mp4"); i.putExtra("step",delta); i.putExtra("control", delta==0?"toggle":"step"); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP); startActivity(i);}catch(Exception e){toast("동영상을 열 수 없습니다.");}
    }

    private void addCustomAppButtons(LinearLayout group){
        String raw=getSharedPreferences(PREFS,MODE_PRIVATE).getString("custom_apps","");
        if(raw==null||raw.trim().isEmpty())return;
        for(String pkg:raw.split("\\|")){ pkg=pkg.trim(); if(pkg.isEmpty())continue; try{ android.content.pm.ApplicationInfo ai=getPackageManager().getApplicationInfo(pkg,0); String label=String.valueOf(getPackageManager().getApplicationLabel(ai)); Button b=button(label,0xEE455A64); final String fp=pkg; b.setOnClickListener(v->launchPackage(fp,"앱을 실행할 수 없습니다.")); group.addView(b);}catch(Exception ignored){} }
    }

    private void launchUrl(String url, String fail) {
        try { Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse(url)); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }
        catch(Exception e){ toast(fail); }
    }

    private void launchExplicitOrPackage(String pkg, String cls, String fail) {
        try {
            Intent i = new Intent();
            i.setClassName(pkg, cls);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return;
        } catch(Exception ignored) {}
        launchPackage(pkg, fail);
    }

    private void launchNavigatorApp() {
        // 10/4 실제 이동아이콘 APK에서 확인된 항해사앱 패키지를 최우선으로 사용.
        String[] candidates = {
                "com.baekhak.dosa.v3",
                "com.baekhak.centralcontrol",
                "com.baekhak.controlcenter.v2",
                "com.openai.hanghae.mini"
        };
        for (String pkg : candidates) {
            try {
                if (getPackageName().equals(pkg)) continue;
                Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(i);
                    return;
                }
            } catch (Exception ignored) {}
        }
        toast("항해사앱을 찾을 수 없습니다.");
    }

    private void launchPackage(String pkg, String fail) {
        try { Intent i=getPackageManager().getLaunchIntentForPackage(pkg); if(i==null){toast(fail);return;} i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }
        catch(Exception e){ toast(fail); }
    }

    private void launchPackageOrUrl(String pkg, String url) {
        try { Intent i=getPackageManager().getLaunchIntentForPackage(pkg); if(i!=null){i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);return;} } catch(Exception ignored){}
        try { Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse(url)); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); } catch(Exception e){toast("열 수 없습니다.");}
    }

    private void openVideo(String mime) {
        try {
            Intent i = new Intent(this, LocalMediaPickerActivity.class);
            i.putExtra(LocalMediaPickerActivity.EXTRA_MIME, mime);
            i.putExtra("control","open");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        } catch(Exception e){ toast("휴대폰 동영상을 자동검색할 수 없습니다."); }
    }

    private void toggleExpanded() {
        expanded = !expanded;
        if (anchorView != null) anchorView.setVisibility(View.VISIBLE);
        if (buttonGroup != null) buttonGroup.setVisibility(expanded ? View.VISIBLE : View.GONE);
        if (!expanded) collapseStations();
        if (root != null && wm != null && overlayLp != null) {
            try { wm.updateViewLayout(root, overlayLp); } catch (Exception ignored) {}
        }
    }

    private void collapseStations() {
        if (channelPanel != null && wm != null) {
            try { wm.removeView(channelPanel); } catch (Exception ignored) {}
            channelPanel.removeAllViews();
            channelPanel.setVisibility(View.GONE);
        }
    }

    private void updateSidePanelPosition() {
        if (channelPanel == null || channelLp == null || overlayLp == null || wm == null || channelPanel.getVisibility() != View.VISIBLE) return;
        channelLp.x = overlayLp.x + dp(118);
        channelLp.y = overlayLp.y + dp(44);
        try { wm.updateViewLayout(channelPanel, channelLp); } catch (Exception ignored) {}
    }

    private void showSidePanel() {
        if (channelPanel == null || channelLp == null || overlayLp == null || wm == null) return;
        channelLp.x = overlayLp.x + dp(118);
        channelLp.y = overlayLp.y + dp(44);
        channelPanel.setVisibility(View.VISIBLE);
        try {
            if (channelPanel.getParent() == null) wm.addView(channelPanel, channelLp);
            else wm.updateViewLayout(channelPanel, channelLp);
        } catch (Exception ignored) {}
    }


    private void showFmStations() {
        showStations(new String[][]{
                {"KBS 클래식FM", "https://radio.bsod.kr/stream?stn=kbs&ch=1fm"},
                {"MBC FM4U", "https://radio.bsod.kr/stream?stn=mbc&ch=fm4u"},
                {"SBS 파워FM", "https://radio.bsod.kr/stream?stn=sbs&ch=powerfm"},
                {"KBS 해피FM", "https://radio.bsod.kr/stream?stn=kbs&ch=2radio"}
        }, "FM");
    }

    private void showAmStations() {
        showStations(new String[][]{
                {"KBS 1라디오 · 뉴스/시사", "https://radio.bsod.kr/stream?stn=kbs&ch=1radio"},
                {"MBC 표준FM · 뉴스/교양", "https://radio.bsod.kr/stream?stn=mbc&ch=sfm"},
                {"SBS 러브FM · 뉴스/생활", "https://radio.bsod.kr/stream?stn=sbs&ch=lovefm"}
        }, "AM");
    }

    private void showVideoSideMenu(String label, String mime) {
        if (channelPanel == null) return;
        collapseStations();
        channelPanel.removeAllViews();
        TextView title = stationButton("◀ " + label + " 파일");
        title.setTextColor(0xFFFFD96A);
        title.setOnClickListener(v -> collapseStations());
        channelPanel.addView(title);
        TextView choose = stationButton("▶ " + label + " 파일 선택");
        choose.setOnClickListener(v -> { collapseStations(); openVideo(mime); });
        channelPanel.addView(choose);
        showSidePanel();
    }

    private void showStations(String[][] stations, String group) {
        if (channelPanel == null) return;
        collapseStations();
        channelPanel.removeAllViews();
        TextView title = stationButton("◀ " + group + " 인터넷 채널 선택");
        title.setTextColor(0xFFFFD96A);
        title.setOnClickListener(v -> collapseStations());
        channelPanel.addView(title);
        for (String[] s : stations) {
            TextView item = stationButton("▶ " + s[0]);
            item.setOnClickListener(v -> {
                playRadio(s[0], s[1]);
                collapseStations();
            });
            channelPanel.addView(item);
        }
        showSidePanel();
    }


    private boolean hasAudioPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            return checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestAudioPermissionInApp() {
        try {
            Intent open = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (open == null) {
                toast("항행의자유 앱을 열어 음악 권한을 허용해 주세요.");
                return;
            }
            open.putExtra("v3_request_audio_permission", true);
            open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(open);
            toast("음악 재생을 위해 권한을 한 번 허용해 주세요.");
        } catch (Exception e) {
            toast("항행의자유 앱을 열어 음악 및 오디오 권한을 허용해 주세요.");
        }
    }


    private void updateCurrentTrackLabel() {
        final TextView view=currentTrackView; if(view==null)return;
        try {
            String mode=currentMediaMode;
            if(mode==null||mode.isEmpty()) mode=getSharedPreferences(PREFS,MODE_PRIVATE).getString("media_mode","MP3");
            if("MP3".equals(mode)) {
                JSONObject state=new JSONObject(MusicPlaybackService.snapshot(this));
                String name=state.optString("name","").trim(); boolean playing=state.optBoolean("playing",false);
                // Display the MediaStore/folder filename exactly as supplied by the music library.
                if(playing){ view.setSelected(true); view.setEllipsize(TextUtils.TruncateAt.MARQUEE); view.setText("▶ "+name); }
                else { view.setSelected(false); view.setEllipsize(TextUtils.TruncateAt.END); view.setText(name.isEmpty()?"MP3 · 정지":"MP3 · 정지 · "+name); }
            } else {
                String title=getSharedPreferences(PREFS,MODE_PRIVATE).getString("media_title","");
                view.setText(mode+" · "+(title.isEmpty()?"자동 연결":title));
            }
        } catch(Exception e){view.setText(currentMediaMode+" · 준비");}
    }

    private TextView topSong(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(11f);
        v.setSingleLine(true);
        v.setEllipsize(TextUtils.TruncateAt.END);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(7), 0, dp(7), 0);
        v.setBackground(bg(0xDD12304B, 0x5578BEFF, 7));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(108), dp(38));
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        v.setLayoutParams(lp);
        return v;
    }

    private void playRadio(String name, String url) {
        playRadioAs(currentMediaMode.equals("AM") ? "AM" : "FM", name, url);
    }

    private void playRadioAs(String mode, String name, String url) {
        currentRadioName=name; currentRadioUrl=url; radioPlaying=true;
        setMediaMode(mode, name);
        try {
            Intent pause = new Intent(this, MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_STOP);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(pause); else startService(pause);
        } catch (Exception ignored) {}
        try {
            Intent i = new Intent(this, RadioPlaybackService.class)
                    .setAction(RadioPlaybackService.ACTION_PLAY)
                    .putExtra(RadioPlaybackService.EXTRA_NAME, name)
                    .putExtra(RadioPlaybackService.EXTRA_URL, url);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
            toast(name + " 연결 중…");
        } catch (Exception e) { toast("라디오를 시작하지 못했습니다."); }
    }

    // V1050: STOP actions must never launch a new foreground service.
    // Android 12+ can kill the app when a foreground service is started only to stop immediately.
    private void stopRadioSafely() {
        try { stopService(new Intent(this, RadioPlaybackService.class)); } catch (Throwable ignored) {}
        currentRadioUrl = ""; radioPlaying=false;
    }

    private void stopRadio() { stopRadioSafely(); }

    private void stopMusicForMediaSwitch() {
        try { stopService(new Intent(this, MusicPlaybackService.class)); } catch (Throwable ignored) {}
    }

    private void stopAllAudioForMediaSwitch() {
        stopMusicForMediaSwitch();
        stopRadioSafely();
        collapseStations();
    }

    private void safeMediaClick(Runnable action, String failMessage) {
        try { action.run(); }
        catch (Throwable t) {
            try { toast(failMessage); } catch (Throwable ignored) {}
        }
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    private void removeOverlay() {
        if (root != null && wm != null) {
            try { wm.removeView(root); } catch (Exception ignored) {}
        }
        if (channelPanel != null && wm != null) { try { wm.removeView(channelPanel); } catch (Exception ignored) {} }
        if (topMediaBar != null && wm != null) { try { wm.removeView(topMediaBar); } catch (Exception ignored) {} }
        handler.removeCallbacks(musicStateTicker);
        root = null;
        channelPanel = null;
        channelLp = null;
        topMediaBar = null;
        topMediaLp = null;
        anchorView = null;
        currentTrackView = null;
        playlistPanel = null;
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        removeOverlay();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}