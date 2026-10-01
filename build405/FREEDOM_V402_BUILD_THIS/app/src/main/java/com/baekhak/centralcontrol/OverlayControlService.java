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
    private static final String PREFS = "overlay_control";
    private static final String KEY_ENABLED = "enabled";

    private WindowManager wm;
    private LinearLayout root;
    private LinearLayout channelPanel;
    private LinearLayout buttonGroup;
    private TextView anchorView;
    private TextView currentTrackView;
    private WindowManager.LayoutParams overlayLp;
    private boolean expanded = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable musicStateTicker = new Runnable() {
        @Override public void run() {
            updateCurrentTrackLabel();
            handler.postDelayed(this, 850);
        }
    };

    private final Runnable permissionWatcher = new Runnable() {
        @Override public void run() {
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
        if (getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_ENABLED, true)) handler.post(permissionWatcher);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_SHOW : intent.getAction();
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
        b.setTextSize(9.3f);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4), 0, dp(4), 0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setBackground(bg(color, 0x66FFD35A, 13));

        // 이동바 전체가 휴대폰 화면 안에 들어오도록 기능버튼만 조금 줄임.
        int bw;
        if ("🎵 MP3".equals(text)) bw = 52;
        else if ("📻 FM".equals(text) || "📡 AM".equals(text)) bw = 47;
        else if ("유튜브".equals(text)) bw = 55;
        else if ("■ 종료".equals(text)) bw = 55;
        else if ("✕".equals(text)) bw = 40;
        else bw = 50;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(bw), dp(36));
        lp.setMargins(dp(1), dp(2), dp(1), dp(2));
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
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(5), dp(4), dp(5), dp(4));
        root.setBackground(bg(0xF20A1D30, 0xCCF3C954, 18));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        TextView anchor = new TextView(this);
        anchorView = anchor;
        anchor.setText("⚓");
        anchor.setTextSize(19f);
        anchor.setTextColor(0xFFFFD96A);
        anchor.setGravity(Gravity.CENTER);
        anchor.setBackground(bg(0xEE08233E, 0xCCF3C954, 14));
        anchor.setLayoutParams(new LinearLayout.LayoutParams(dp(39), dp(39)));
        bar.addView(anchor);

        buttonGroup = new LinearLayout(this);
        buttonGroup.setOrientation(LinearLayout.HORIZONTAL);
        buttonGroup.setGravity(Gravity.CENTER_VERTICAL);
        buttonGroup.setVisibility(View.GONE);

        Button music = button("🎵 MP3", 0xEE1068C8);
        Button fm = button("📻 FM", 0xEE5B34D6);
        Button am = button("📡 AM", 0xEE6D2DB7);
        Button youtube = button("유튜브", 0xEEDB1F28);
        Button stop = button("■ 종료", 0xEED52D46);
        Button hideIcon = button("✕", 0xEE4A5568);
        buttonGroup.addView(music); buttonGroup.addView(fm); buttonGroup.addView(am); buttonGroup.addView(youtube); buttonGroup.addView(stop); buttonGroup.addView(hideIcon);
        bar.addView(buttonGroup);
        root.addView(bar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(46)));

        currentTrackView = new TextView(this);
        currentTrackView.setTextColor(0xFFFFE58A);
        currentTrackView.setTextSize(10.5f);
        currentTrackView.setSingleLine(true);
        currentTrackView.setEllipsize(TextUtils.TruncateAt.END);
        currentTrackView.setMaxWidth(dp(260));
        currentTrackView.setPadding(dp(8), dp(2), dp(8), dp(3));
        currentTrackView.setGravity(Gravity.CENTER_VERTICAL);
        currentTrackView.setVisibility(View.GONE);
        root.addView(currentTrackView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        handler.removeCallbacks(musicStateTicker);
        handler.post(musicStateTicker);

        channelPanel = new LinearLayout(this);
        channelPanel.setOrientation(LinearLayout.VERTICAL);
        channelPanel.setVisibility(View.GONE);
        root.addView(channelPanel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        music.setOnClickListener(v -> {
            stopRadio();
            collapseStations();
            try {
                Intent open = getPackageManager().getLaunchIntentForPackage(getPackageName());
                if (open == null) {
                    toast("항행의자유 앱에서 음악목록을 열 수 없습니다.");
                    return;
                }
                open.putExtra("v3_open_saved_music_library", true);
                open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(open);
            } catch (Exception e) {
                toast("저장된 음악목록을 열지 못했습니다.");
            }
        });
        fm.setOnClickListener(v -> showFmStations());
        am.setOnClickListener(v -> showAmStations());
        youtube.setOnClickListener(v -> {
            collapseStations();
            try {
                Intent y = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
                y.setPackage("com.google.android.youtube");
                y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(y);
            } catch (Exception first) {
                try {
                    Intent y = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
                    y.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(y);
                } catch (Exception ignored) { toast("YouTube를 열 수 없습니다."); }
            }
        });
        stop.setOnClickListener(v -> {
            stopRadio();
            try {
                Intent i = new Intent(this, MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_STOP);
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
            } catch (Exception ignored) {}
            collapseStations();

            // A안: 종료를 눌러도 이동아이콘은 사라지지 않고 작은 ⚓만 남김.
            expanded = false;
            if (buttonGroup != null) buttonGroup.setVisibility(View.GONE);
            if (anchorView != null) anchorView.setVisibility(View.VISIBLE);
            if (root != null && wm != null && overlayLp != null) {
                try { wm.updateViewLayout(root, overlayLp); } catch (Exception ignored) {}
            }
            toast("음악 · 라디오를 종료했습니다.");
        });
        hideIcon.setOnClickListener(v -> {
            collapseStations();

            // A안: X는 펼쳐진 메뉴만 접고 ⚓ 아이콘은 항상 남김.
            expanded = false;
            if (buttonGroup != null) buttonGroup.setVisibility(View.GONE);
            if (anchorView != null) anchorView.setVisibility(View.VISIBLE);
            if (root != null && wm != null && overlayLp != null) {
                try { wm.updateViewLayout(root, overlayLp); } catch (Exception ignored) {}
            }
        });

        if (Build.VERSION.SDK_INT >= 26) {
            overlayLp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
        } else {
            overlayLp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
        }
        overlayLp.gravity = Gravity.TOP | Gravity.START;
        overlayLp.x = dp(8);
        overlayLp.y = dp(12);

        final float[] downRaw = new float[2];
        final int[] downPos = new int[2];
        final boolean[] moved = new boolean[1];
        anchor.setOnTouchListener((v, event) -> {
            if (overlayLp == null || wm == null) return false;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRaw[0] = event.getRawX(); downRaw[1] = event.getRawY();
                    downPos[0] = overlayLp.x; downPos[1] = overlayLp.y;
                    moved[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int)(event.getRawX() - downRaw[0]);
                    int dy = (int)(event.getRawY() - downRaw[1]);
                    if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) moved[0] = true;
                    if (moved[0]) {
                        overlayLp.x = Math.max(0, downPos[0] + dx);
                        overlayLp.y = Math.max(0, downPos[1] + dy);
                        try { wm.updateViewLayout(root, overlayLp); } catch (Exception ignored) {}
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!moved[0]) toggleExpanded();
                    return true;
            }
            return false;
        });

        try { wm.addView(root, overlayLp); } catch (Exception e) { root = null; channelPanel = null; buttonGroup = null; anchorView = null; overlayLp = null; }
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
        if (channelPanel != null) {
            channelPanel.removeAllViews();
            channelPanel.setVisibility(View.GONE);
        }
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

    private void showStations(String[][] stations, String group) {
        if (channelPanel == null) return;
        channelPanel.removeAllViews();
        channelPanel.setVisibility(View.VISIBLE);
        TextView title = stationButton("▼ " + group + " 인터넷 채널 선택");
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
        TextView view = currentTrackView;
        if (view == null) return;
        try {
            JSONObject state = new JSONObject(MusicPlaybackService.snapshot(this));
            String name = state.optString("name", "").trim();
            boolean playing = state.optBoolean("playing", false);
            boolean enabled = state.optBoolean("enabled", false);
            if (!name.isEmpty() && (playing || enabled)) {
                view.setText("🎵 현재재생: " + name);
                view.setVisibility(View.VISIBLE);
            } else {
                view.setText("");
                view.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            view.setVisibility(View.GONE);
        }
    }

    private void playRadio(String name, String url) {
        try {
            Intent pause = new Intent(this, MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_PAUSE);
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

    private void stopRadio() {
        try {
            Intent i = new Intent(this, RadioPlaybackService.class).setAction(RadioPlaybackService.ACTION_STOP);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        } catch (Exception ignored) {}
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    private void removeOverlay() {
        if (root != null && wm != null) {
            try { wm.removeView(root); } catch (Exception ignored) {}
        }
        handler.removeCallbacks(musicStateTicker);
        root = null;
        channelPanel = null;
        anchorView = null;
        currentTrackView = null;
    }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        removeOverlay();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
