package com.baekhak.centralcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

public class RadioPlaybackService extends Service {
    public static final String ACTION_PLAY = "com.baekhak.centralcontrol.radio.PLAY";
    public static final String ACTION_STOP = "com.baekhak.centralcontrol.radio.STOP";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_URL = "url";
    private static final String CHANNEL = "v3_radio_playback";
    private static final int NOTICE = 43141;

    private MediaPlayer player;
    private String currentName = "인터넷 라디오";

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CHANNEL, "항행의자유 라디오", NotificationManager.IMPORTANCE_LOW);
            c.setSound(null, null);
            NotificationManager n = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (n != null) n.createNotificationChannel(c);
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? "" : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            release();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (ACTION_PLAY.equals(action)) {
            String name = intent.getStringExtra(EXTRA_NAME);
            String url = intent.getStringExtra(EXTRA_URL);
            if (name != null && !name.trim().isEmpty()) currentName = name.trim();
            startForeground(NOTICE, notification("연결 중…"));
            play(url);
        }
        return START_NOT_STICKY;
    }

    private void play(String url) {
        release();
        if (url == null || url.trim().isEmpty()) {
            stopForeground(true); stopSelf(); return;
        }
        final MediaPlayer mp = new MediaPlayer();
        player = mp;
        try {
            mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mp.setDataSource(url.trim());
            mp.setOnPreparedListener(done -> {
                if (player != done) return;
                try { done.start(); } catch (Exception ignored) {}
                update("재생 중");
            });
            mp.setOnErrorListener((bad, what, extra) -> {
                if (player == bad) {
                    update("연결 실패 · 다른 채널을 선택해 주세요");
                    release();
                }
                return true;
            });
            mp.prepareAsync();
        } catch (Exception e) {
            update("연결 실패 · 다른 채널을 선택해 주세요");
            release();
        }
    }

    private PendingIntent stopAction() {
        Intent i = new Intent(this, RadioPlaybackService.class).setAction(ACTION_STOP);
        return PendingIntent.getService(this, NOTICE + 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private Notification notification(String state) {
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        b.setSmallIcon(R.drawable.v3_ship_icon)
                .setContentTitle(currentName)
                .setContentText(state)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "종료", stopAction()).build());
        Intent open = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (open != null) b.setContentIntent(PendingIntent.getActivity(this, NOTICE, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        return b.build();
    }

    private void update(String state) {
        NotificationManager n = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (n != null) n.notify(NOTICE, notification(state));
    }

    private void release() {
        MediaPlayer old = player;
        player = null;
        if (old != null) {
            try { old.setOnPreparedListener(null); old.setOnErrorListener(null); old.release(); } catch (Exception ignored) {}
        }
    }

    @Override public void onDestroy() { release(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}