package com.baekhak.centralcontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import com.baekhak.centralcontrol.MusicLibraryFiles;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MusicPlaybackService extends Service {
    public static final String ACTION_APP_EXIT = "com.baekhak.centralcontrol.music.APP_EXIT";
    public static final String ACTION_DEFAULT = "com.baekhak.centralcontrol.music.DEFAULT";
    public static final String ACTION_LIBRARY = "com.baekhak.centralcontrol.music.LIBRARY";
    public static final String ACTION_NEXT = "com.baekhak.centralcontrol.music.NEXT";
    public static final String ACTION_PAUSE = "com.baekhak.centralcontrol.music.PAUSE";
    public static final String ACTION_PLAY = "com.baekhak.centralcontrol.music.PLAY";
    public static final String ACTION_PREV = "com.baekhak.centralcontrol.music.PREV";
    public static final String ACTION_RELOAD = "com.baekhak.centralcontrol.music.RELOAD";
    public static final String ACTION_SELECT = "com.baekhak.centralcontrol.music.SELECT";
    public static final String ACTION_STOP = "com.baekhak.centralcontrol.music.STOP";
    public static final String ACTION_VOLUME = "com.baekhak.centralcontrol.music.VOLUME";
    private static final String CHANNEL = "v3_music_playback";
    public static final String EXTRA_TRACK = "track_id";
    public static final String EXTRA_VOLUME = "volume";
    private static final int NOTICE = 3407;
    private static final String PREFIX = "com.baekhak.centralcontrol.music.";
    private static final String PREFS = "v3_music_preferences";
    private static volatile MusicPlaybackService active;
    private AudioManager audio;
    private AudioFocusRequest focusRequest;
    private MediaPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<MusicLibraryFiles.Track> tracks = new ArrayList();
    private final List<Uri> phoneUris = new ArrayList();
    private final List<String> phoneNames = new ArrayList();
    private final List<String> phoneIds = new ArrayList();
    private boolean phoneMode = false;
    private final Set<String> failed = new HashSet();
    private boolean prepared = false;
    private boolean seeking = false;
    private boolean wantPlay = false;
    private boolean focusHeld = false;
    private boolean resumeOnFocus = false;
    private int index = 0;
    private int pendingPosition = 0;
    private int duration = 0;
    private String trackId = "";
    private String trackName = "";
    private String status = "ready";
    private String error = "";
    private final Runnable checkpoint = new Runnable() { // from class: com.baekhak.centralcontrol.MusicPlaybackService.1
        @Override // java.lang.Runnable
        public void run() {
            MusicPlaybackService.this.saveCheckpoint(false);
            MusicPlaybackService.this.handler.postDelayed(this, 2000L);
        }
    };
    private final BroadcastReceiver noisy = new BroadcastReceiver() { // from class: com.baekhak.centralcontrol.MusicPlaybackService.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) throws IllegalStateException {
            MusicPlaybackService.this.pause(true);
        }
    };
    private final AudioManager.OnAudioFocusChangeListener focusChange = new AudioManager.OnAudioFocusChangeListener() { // from class: com.baekhak.centralcontrol.MusicPlaybackService$$ExternalSyntheticLambda16
        @Override // android.media.AudioManager.OnAudioFocusChangeListener
        public final void onAudioFocusChange(int i) {
            this.f$0.m89lambda$new$1$combaekhakcentralcontrolMusicPlaybackService(i);
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* renamed from: lambda$new$1$com-baekhak-centralcontrol-MusicPlaybackService, reason: not valid java name */
    /* synthetic */ void m89lambda$new$1$combaekhakcentralcontrolMusicPlaybackService(final int i) {
        this.handler.post(new Runnable() { // from class: com.baekhak.centralcontrol.MusicPlaybackService$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() throws IllegalStateException {
                this.f$0.m88lambda$new$0$combaekhakcentralcontrolMusicPlaybackService(i);
            }
        });
    }

    /* renamed from: lambda$new$0$com-baekhak-centralcontrol-MusicPlaybackService, reason: not valid java name */
    /* synthetic */ void m88lambda$new$0$combaekhakcentralcontrolMusicPlaybackService(int i) throws IllegalStateException {
        if (i == 1) {
            this.focusHeld = true;
            applyVolume(readVolume());
            if (this.resumeOnFocus) {
                this.resumeOnFocus = false;
                this.wantPlay = true;
                startIfReady();
                return;
            }
            return;
        }
        if (i != -3) {
            boolean z = i == -2;
            this.resumeOnFocus = z && this.wantPlay;
            this.focusHeld = false;
            pause(!z);
            return;
        }
        try {
            if (this.player != null) {
                float volume = (readVolume() / 100.0f) * 0.2f;
                this.player.setVolume(volume, volume);
            }
        } catch (Exception unused) {
        }
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS, 0);
    }

    private int readVolume() {
        return Math.max(0, Math.min(100, prefs().getInt("music_volume", 42)));
    }

    private boolean enabled() {
        return prefs().getBoolean("music_enabled", false);
    }

    private boolean playing() {
        MediaPlayer mediaPlayer;
        try {
            if (!this.prepared || this.seeking || (mediaPlayer = this.player) == null) {
                return false;
            }
            return mediaPlayer.isPlaying();
        } catch (Exception unused) {
            return false;
        }
    }

    private int position() {
        MediaPlayer mediaPlayer;
        try {
            if (this.prepared && !this.seeking && (mediaPlayer = this.player) != null) {
                return mediaPlayer.getCurrentPosition();
            }
        } catch (Exception unused) {
        }
        return this.pendingPosition;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        active = this;
        this.audio = (AudioManager) getSystemService("audio");
        if (Build.VERSION.SDK_INT >= 26) {
            ((NotificationManager) getSystemService("notification")).createNotificationChannel(MainActivity$$ExternalSyntheticApiModelOutline0.m(CHANNEL, "V3 배경음악", 2));
        }
        IntentFilter intentFilter = new IntentFilter("android.media.AUDIO_BECOMING_NOISY");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(this.noisy, intentFilter, 4);
        } else {
            registerReceiver(this.noisy, intentFilter);
        }
        this.handler.postDelayed(this.checkpoint, 2000L);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) throws IllegalStateException, Resources.NotFoundException, IOException, SecurityException, IllegalArgumentException {
        String action = intent == null ? ACTION_PLAY : intent.getAction();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTICE, notification(), 2);
        } else {
            startForeground(NOTICE, notification());
        }
        if (ACTION_APP_EXIT.equals(action)) {
            saveCheckpoint(true);
            if (!playing()) {
                return enabled() ? 1 : 2;
            }
            this.wantPlay = true;
            this.status = "playing";
            notifyState();
            return 1;
        }
        if (ACTION_STOP.equals(action)) {
            saveCheckpoint(true);
            prefs().edit().putBoolean("music_enabled", false).commit();
            this.wantPlay = false;
            release();
            abandonFocus();
            stopForeground(true);
            stopSelf();
            return 2;
        }
        if (ACTION_PAUSE.equals(action)) {
            pause(true);
            return 2;
        }
        if (ACTION_VOLUME.equals(action)) {
            applyVolume(intent.getIntExtra(EXTRA_VOLUME, readVolume()));
            notifyState();
            return 2;
        }
        if (intent == null && !enabled()) {
            stopForeground(true);
            stopSelf();
            return 2;
        }
        this.failed.clear();
        this.error = "";
        if (ACTION_RELOAD.equals(action)) {
            saveCheckpoint(true);
            String string = this.trackId;
            boolean zEnabled = enabled();
            release();
            if (!loadTracks()) {
                return 2;
            }
            if (string.isEmpty()) {
                string = prefs().getString("music_track_id", "");
            }
            choose(string);
            this.wantPlay = zEnabled;
            openCurrent(savedPosition());
            return 2;
        }
        if (ACTION_DEFAULT.equals(action)) {
            saveCheckpoint(true);
            prefs().edit().putBoolean("music_use_default", true).putBoolean("music_enabled", true).commit();
            this.tracks.clear();
            this.phoneUris.clear();
            this.phoneNames.clear();
            this.phoneIds.clear();
            this.phoneMode = false;
            this.trackId = "default";
            this.trackName = "기본 배경음악";
            this.wantPlay = true;
            openCurrent(0);
            return 2;
        }
        if (ACTION_LIBRARY.equals(action)) {
            saveCheckpoint(true);
            prefs().edit().putBoolean("music_use_default", false).putBoolean("music_enabled", true).commit();
            if (!loadTracks()) {
                return 2;
            }
            choose(prefs().getString("music_library_last_id", ""));
            this.wantPlay = true;
            openCurrent(savedPosition());
            return 2;
        }
        if (ACTION_SELECT.equals(action)) {
            String stringExtra = intent.getStringExtra(EXTRA_TRACK);
            saveCheckpoint(true);
            prefs().edit().putBoolean("music_use_default", false).commit();
            if (!loadTracks()) {
                return 2;
            }
            if (!contains(stringExtra)) {
                this.error = "선택한 곡이 저장목록에 없습니다. 목록을 새로 열어 주세요.";
                notifyState();
                return 2;
            }
            choose(stringExtra);
            prefs().edit().putBoolean("music_enabled", true).commit();
            this.wantPlay = true;
            openCurrent(savedPosition());
            return 2;
        }
        if (ACTION_NEXT.equals(action) || ACTION_PREV.equals(action)) {
            saveCheckpoint(true);
            String str = this.trackId;
            if (!loadTracks()) {
                return 2;
            }
            choose(str);
            if (trackCount() > 0) {
                this.index = (this.index + (ACTION_NEXT.equals(action) ? 1 : trackCount() - 1)) % trackCount();
                setTrack();
            }
            prefs().edit().putBoolean("music_enabled", true).commit();
            this.wantPlay = true;
            openCurrent(0);
            return 2;
        }
        prefs().edit().putBoolean("music_enabled", true).commit();
        this.wantPlay = true;
        if (this.player != null) {
            startIfReady();
        } else {
            if (!loadTracks()) {
                return 2;
            }
            choose(prefs().getString("music_track_id", ""));
            openCurrent(savedPosition());
        }
        return 2;
    }

    private boolean loadTracks() {
        this.tracks.clear();
        this.phoneUris.clear();
        this.phoneNames.clear();
        this.phoneIds.clear();
        this.phoneMode = false;
        if (prefs().getBoolean("music_use_default", false)) {
            return true;
        }
        try {
            this.tracks.addAll(new MusicLibraryStore(this).read());
            if (!this.tracks.isEmpty()) {
                return true;
            }
            fail("저장된 음악이 없습니다. MP3 불러오기에서 필요한 곡을 직접 선택해 주세요.");
            return false;
        } catch (Exception unused) {
            fail("저장된 음악목록을 읽지 못했습니다.");
            return false;
        }
    }

    private int trackCount() {
        return (this.phoneMode ? this.phoneUris : this.tracks).size();
    }

    private boolean contains(String str) {
        if (this.phoneMode) {
            Iterator<String> it = this.phoneIds.iterator();
            while (it.hasNext()) {
                if (it.next().equals(str)) {
                    return true;
                }
            }
            return false;
        }
        Iterator<MusicLibraryFiles.Track> it2 = this.tracks.iterator();
        while (it2.hasNext()) {
            if (it2.next().id.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private void choose(String str) {
        int i = 0;
        if (this.phoneMode) {
            this.index = Math.max(0, Math.min(prefs().getInt("music_index", 0), this.phoneUris.size() - 1));
            while (true) {
                if (i >= this.phoneIds.size()) {
                    break;
                }
                if (this.phoneIds.get(i).equals(str)) {
                    this.index = i;
                    break;
                }
                i++;
            }
            setTrack();
            return;
        }
        if (this.tracks.isEmpty()) {
            this.index = 0;
            this.trackId = "default";
            this.trackName = "기본 배경음악";
            return;
        }
        this.index = Math.max(0, Math.min(prefs().getInt("music_index", 0), this.tracks.size() - 1));
        while (true) {
            if (i >= this.tracks.size()) {
                break;
            }
            if (this.tracks.get(i).id.equals(str)) {
                this.index = i;
                break;
            }
            i++;
        }
        setTrack();
    }

    private void setTrack() {
        if (this.phoneMode) {
            this.trackId = this.phoneIds.get(this.index);
            this.trackName = this.phoneNames.get(this.index);
        } else {
            MusicLibraryFiles.Track track = this.tracks.get(this.index);
            this.trackId = track.id;
            this.trackName = track.name;
        }
    }

    private int savedPosition() {
        return Math.max(0, prefs().getInt("music_pos_" + this.trackId, 0));
    }

    private void openCurrent(int i) throws IllegalStateException, Resources.NotFoundException, IOException, SecurityException, IllegalArgumentException {
        release();
        this.pendingPosition = Math.max(0, i);
        this.duration = 0;
        this.status = "preparing";
        if (this.trackId.isEmpty()) {
            choose(prefs().getString("music_track_id", ""));
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        this.player = mediaPlayer;
        try {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(2).build());
            mediaPlayer.setWakeMode(getApplicationContext(), 1);
            mediaPlayer.setVolume(readVolume() / 100.0f, readVolume() / 100.0f);
            if (this.phoneMode) {
                mediaPlayer.setDataSource(this, this.phoneUris.get(this.index));
            } else if (this.tracks.isEmpty()) {
                AssetFileDescriptor assetFileDescriptorOpenRawResourceFd = getResources().openRawResourceFd(R.raw.home_joyful_loop);
                if (assetFileDescriptorOpenRawResourceFd == null) {
                    throw new IllegalStateException("기본 음악 없음");
                }
                try {
                    mediaPlayer.setDataSource(assetFileDescriptorOpenRawResourceFd.getFileDescriptor(), assetFileDescriptorOpenRawResourceFd.getStartOffset(), assetFileDescriptorOpenRawResourceFd.getLength());
                    assetFileDescriptorOpenRawResourceFd.close();
                } catch (Throwable th) {
                    assetFileDescriptorOpenRawResourceFd.close();
                    throw th;
                }
            } else {
                mediaPlayer.setDataSource(new MusicLibraryStore(this).file(this.tracks.get(this.index)).getAbsolutePath());
            }
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.baekhak.centralcontrol.MusicPlaybackService$$ExternalSyntheticLambda11
                @Override // android.media.MediaPlayer.OnPreparedListener
                public final void onPrepared(MediaPlayer mediaPlayer2) throws IllegalStateException {
                    this.f$0.m90xf160aaca(mediaPlayer2);
                }
            });
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() { // from class: com.baekhak.centralcontrol.MusicPlaybackService$$ExternalSyntheticLambda12
                @Override // android.media.MediaPlayer.OnSeekCompleteListener
                public final void onSeekComplete(MediaPlayer mediaPlayer2) throws IllegalStateException {
                    this.f$0.m91xf0ea44cb(mediaPlayer2);
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // from class: com.baekhak.centralcontrol.MusicPlaybackService$$ExternalSyntheticLambda13
                @Override // android.media.MediaPlayer.OnCompletionListener
                public final void onCompletion(MediaPlayer mediaPlayer2) {
                    this.f$0.m92xf073decc(mediaPlayer2);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() { // from class: com.baekhak.centralcontrol.MusicPlaybackService$$ExternalSyntheticLambda14
                @Override // android.media.MediaPlayer.OnErrorListener
                public final boolean onError(MediaPlayer mediaPlayer2, int i2, int i3) {
                    return this.f$0.m93xeffd78cd(mediaPlayer2, i2, i3);
                }
            });
            mediaPlayer.prepareAsync();
            notifyState();
        } catch (Exception unused) {
            skipUnreadable();
        }
    }

    /* renamed from: lambda$openCurrent$2$com-baekhak-centralcontrol-MusicPlaybackService, reason: not valid java name */
    /* synthetic */ void m90xf160aaca(MediaPlayer mediaPlayer) throws IllegalStateException {
        if (this.player != mediaPlayer) {
            return;
        }
        this.prepared = true;
        int duration = mediaPlayer.getDuration();
        this.duration = duration;
        int iMax = Math.max(0, Math.min(this.pendingPosition, Math.max(0, duration - 1)));
        this.pendingPosition = iMax;
        if (iMax > 0) {
            this.seeking = true;
            mediaPlayer.seekTo(iMax);
        } else {
            saveCheckpoint(false);
            startIfReady();
        }
    }

    /* renamed from: lambda$openCurrent$3$com-baekhak-centralcontrol-MusicPlaybackService, reason: not valid java name */
    /* synthetic */ void m91xf0ea44cb(MediaPlayer mediaPlayer) throws IllegalStateException {
        if (this.player != mediaPlayer) {
            return;
        }
        this.seeking = false;
        this.pendingPosition = 0;
        startIfReady();
    }

    /* renamed from: lambda$openCurrent$4$com-baekhak-centralcontrol-MusicPlaybackService, reason: not valid java name */
    /* synthetic */ void m92xf073decc(MediaPlayer mediaPlayer) {
        if (this.player != mediaPlayer) {
            return;
        }
        this.pendingPosition = 0;
        prefs().edit().putInt("music_pos_" + this.trackId, 0).commit();
        advance(false);
    }

    /* renamed from: lambda$openCurrent$5$com-baekhak-centralcontrol-MusicPlaybackService, reason: not valid java name */
    /* synthetic */ boolean m93xeffd78cd(MediaPlayer mediaPlayer, int i, int i2) {
        if (this.player != mediaPlayer) {
            return true;
        }
        skipUnreadable();
        return true;
    }

    private void startIfReady() throws IllegalStateException {
        if (!this.prepared || this.seeking || this.player == null) {
            return;
        }
        if (!this.wantPlay) {
            this.status = "paused";
            saveCheckpoint(false);
            notifyState();
        } else {
            if (!requestFocus()) {
                this.status = "paused";
                this.error = "다른 앱이 오디오를 사용 중입니다. 재생 버튼을 다시 눌러 주세요.";
                notifyState();
                return;
            }
            try {
                this.player.start();
                this.status = "playing";
                this.error = "";
                saveCheckpoint(false);
                notifyState();
            } catch (Exception unused) {
                skipUnreadable();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pause(boolean z) throws IllegalStateException {
        saveCheckpoint(true);
        this.wantPlay = false;
        try {
            if (playing()) {
                this.player.pause();
            }
        } catch (Exception unused) {
        }
        if (z) {
            prefs().edit().putBoolean("music_enabled", false).commit();
            this.resumeOnFocus = false;
            abandonFocus();
        }
        this.status = "paused";
        saveCheckpoint(true);
        notifyState();
    }

    private void advance(boolean z) {
        release();
        if (trackCount() > 0) {
            this.index = (this.index + 1) % trackCount();
            setTrack();
        }
        if (!z) {
            this.failed.clear();
        }
        this.handler.post(new Runnable() { // from class: com.baekhak.centralcontrol.MusicPlaybackService$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() throws IllegalStateException, Resources.NotFoundException, IOException, SecurityException, IllegalArgumentException {
                this.f$0.m87lambda$advance$6$combaekhakcentralcontrolMusicPlaybackService();
            }
        });
    }

    /* renamed from: lambda$advance$6$com-baekhak-centralcontrol-MusicPlaybackService, reason: not valid java name */
    /* synthetic */ void m87lambda$advance$6$combaekhakcentralcontrolMusicPlaybackService() throws IllegalStateException, Resources.NotFoundException, IOException, SecurityException, IllegalArgumentException {
        openCurrent(0);
    }

    private void skipUnreadable() {
        if (!this.failed.add(this.trackId) || this.failed.size() >= Math.max(1, trackCount())) {
            fail("재생 가능한 곡을 찾지 못했습니다. 저장목록은 보존했습니다.");
            return;
        }
        this.error = "읽을 수 없는 곡을 건너뜁니다: " + this.trackName;
        advance(true);
    }

    private void fail(String str) {
        this.error = str;
        this.status = "error";
        this.wantPlay = false;
        release();
        abandonFocus();
        notifyState();
    }

    private boolean requestFocus() {
        int iRequestAudioFocus;
        if (this.focusHeld) {
            return true;
        }
        if (this.audio == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            if (this.focusRequest == null) {
                this.focusRequest = MainActivity$$ExternalSyntheticApiModelOutline0.m(1).setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(2).build()).setOnAudioFocusChangeListener(this.focusChange, this.handler).build();
            }
            iRequestAudioFocus = this.audio.requestAudioFocus(this.focusRequest);
        } else {
            iRequestAudioFocus = this.audio.requestAudioFocus(this.focusChange, 3, 1);
        }
        boolean z = iRequestAudioFocus == 1;
        this.focusHeld = z;
        return z;
    }

    private void abandonFocus() {
        AudioFocusRequest audioFocusRequest;
        try {
            if (this.audio != null) {
                if (Build.VERSION.SDK_INT < 26 || (audioFocusRequest = this.focusRequest) == null) {
                    this.audio.abandonAudioFocus(this.focusChange);
                } else {
                    this.audio.abandonAudioFocusRequest(audioFocusRequest);
                }
            }
        } catch (Exception unused) {
        }
        this.focusHeld = false;
    }

    private void applyVolume(int i) {
        int iMax = Math.max(0, Math.min(100, i));
        prefs().edit().putInt("music_volume", iMax).apply();
        try {
            MediaPlayer mediaPlayer = this.player;
            if (mediaPlayer != null) {
                float f = iMax / 100.0f;
                mediaPlayer.setVolume(f, f);
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveCheckpoint(boolean z) {
        if (this.trackId.isEmpty()) {
            return;
        }
        int iPosition = position();
        this.pendingPosition = iPosition;
        SharedPreferences.Editor editorPutInt = prefs().edit().putString("music_track_id", this.trackId).putString("music_current_name", this.trackName).putInt("music_index", this.index).putInt("music_position_ms", iPosition).putInt("music_duration_ms", this.duration).putInt("music_pos_" + this.trackId, iPosition);
        if (!"default".equals(this.trackId)) {
            editorPutInt.putString("music_library_last_id", this.trackId);
        }
        if (z) {
            editorPutInt.commit();
        } else {
            editorPutInt.apply();
        }
    }

    private void release() {
        MediaPlayer mediaPlayer = this.player;
        this.player = null;
        this.prepared = false;
        this.seeking = false;
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setOnPreparedListener(null);
                mediaPlayer.setOnSeekCompleteListener(null);
                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.setOnErrorListener(null);
                mediaPlayer.release();
            } catch (Exception unused) {
            }
        }
    }

    private PendingIntent action(String str, int i) {
        return PendingIntent.getService(this, i, new Intent(this, (Class<?>) MusicPlaybackService.class).setAction(str), 201326592);
    }

    private Notification notification() {
        Notification.Builder builderM = Build.VERSION.SDK_INT >= 26 ? MainActivity$$ExternalSyntheticApiModelOutline0.m(this, CHANNEL) : new Notification.Builder(this);
        Notification.Builder contentTitle = builderM.setSmallIcon(R.drawable.v3_ship_icon).setContentTitle("V3 배경음악");
        StringBuilder sb = new StringBuilder();
        sb.append(playing() ? "재생 중 · " : "일시정지 · ");
        sb.append(this.trackName.isEmpty() ? "저장된 음악" : this.trackName);
        contentTitle.setContentText(sb.toString()).setOnlyAlertOnce(true).setOngoing(playing()).setCategory("transport");
        Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launchIntentForPackage != null) {
            builderM.setContentIntent(PendingIntent.getActivity(this, 3400, launchIntentForPackage, 201326592));
        }
        builderM.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_previous, "이전", action(ACTION_PREV, 3404)).build());
        builderM.addAction(new Notification.Action.Builder(playing() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play, playing() ? "일시정지" : "재생", action(playing() ? ACTION_PAUSE : ACTION_PLAY, 3401)).build());
        builderM.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_next, "다음", action(ACTION_NEXT, 3405)).build());
        builderM.addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "종료", action(ACTION_STOP, 3403)).build());
        return builderM.build();
    }

    private void notifyState() {
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        if (notificationManager != null) {
            notificationManager.notify(NOTICE, notification());
        }
    }

    public static boolean isRunning() {
        return active != null;
    }

    public static String playlistSnapshot(Context context, int i) throws JSONException {
        JSONArray jSONArray = new JSONArray();
        int iMax = Math.max(1, Math.min(20, i));
        try {
            for (MusicLibraryFiles.Track track : new MusicLibraryStore(context).read()) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", track.id);
                jSONObject.put(RadioPlaybackService.EXTRA_NAME, track.name);
                jSONArray.put(jSONObject);
                if (jSONArray.length() >= iMax) {
                    break;
                }
            }
        } catch (Exception unused) {
        }
        return jSONArray.toString();
    }

    public static String snapshot(Context context) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        MusicPlaybackService musicPlaybackService = active;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS, 0);
        String str = "";
        try {
            jSONObject.put(RadioPlaybackService.EXTRA_NAME, musicPlaybackService == null ? sharedPreferences.getString("music_current_name", "") : musicPlaybackService.trackName);
            jSONObject.put("id", musicPlaybackService == null ? sharedPreferences.getString("music_track_id", "") : musicPlaybackService.trackId);
            jSONObject.put("position", musicPlaybackService == null ? sharedPreferences.getInt("music_position_ms", 0) : musicPlaybackService.position());
            jSONObject.put("duration", musicPlaybackService == null ? sharedPreferences.getInt("music_duration_ms", 0) : musicPlaybackService.duration);
            jSONObject.put("status", musicPlaybackService == null ? "ready" : musicPlaybackService.status);
            jSONObject.put("playing", musicPlaybackService != null && musicPlaybackService.playing());
            jSONObject.put("enabled", sharedPreferences.getBoolean("music_enabled", false));
            jSONObject.put(EXTRA_VOLUME, sharedPreferences.getInt("music_volume", 42));
            if (musicPlaybackService != null) {
                str = musicPlaybackService.error;
            }
            jSONObject.put("error", str);
            jSONObject.put("importing", MusicLibraryStore.importing());
            jSONObject.put("progress", MusicLibraryStore.progress());
        } catch (Exception unused) {
        }
        return jSONObject.toString();
    }

    @Override // android.app.Service
    public void onTaskRemoved(Intent intent) {
        saveCheckpoint(true);
        if (playing()) {
            this.wantPlay = true;
            notifyState();
        }
        super.onTaskRemoved(intent);
    }

    @Override // android.app.Service
    public void onDestroy() {
        saveCheckpoint(true);
        this.handler.removeCallbacksAndMessages(null);
        release();
        abandonFocus();
        try {
            unregisterReceiver(this.noisy);
        } catch (Exception unused) {
        }
        if (active == this) {
            active = null;
        }
        super.onDestroy();
    }
}
