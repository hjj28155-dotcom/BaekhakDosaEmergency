package com.baekhak.centralcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.baekhak.centralcontrol.MusicLibraryFiles;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    private static final int APK_UPDATE_PICKER = 4308;
    private static final String APPROVAL_REQUEST_URL = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/approval-request";
    private static final String APPROVAL_STATUS_URL = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/approval-status";
    private static final int AUDIO_LIBRARY_PERMISSION = 4312;
    private static final int AUTO_AUDIO_PERMISSION = 4313;
    private static final String BASE_URL = "file:///android_asset/";
    private static final int CONTENT_BUILD = 1062;
    public static final String EXTRA_OPEN_SAVED_MUSIC = "v3_open_saved_music_library";
    public static final String EXTRA_OPEN_UPDATE = "v3_open_update";
    private static final String LAUNCH_WELCOME_TEXT = "우리 항행의자유앱에 승선한 지인들께 알려드립니다. 희망의 배에 승선하신 것을 진심으로 환영합니다. 부디 즐거운 항해가 되시기를 기원합니다.";
    private static final int LEGACY_BACKUP_PICKER = 4306;
    private static final String MUSIC_DIR = "v3_music_playlist";
    private static final String MUSIC_ENABLED = "music_enabled";
    private static final String MUSIC_FILE = "v3_user_background_audio";
    private static final int MUSIC_FOLDER_PICKER = 4309;
    private static final int MUSIC_MULTI_PICKER = 4311;
    private static final String MUSIC_NAME = "music_name";
    private static final int MUSIC_PICKER = 4307;
    private static final String MUSIC_PREFS = "v3_music_preferences";
    private static final String MUSIC_TREE_URI = "music_tree_uri";
    private static final String MUSIC_VOLUME = "music_volume";
    private static final int SAVE_PICKER = 4304;
    private static final String SPECIAL_APPROVAL_PREFS = "v3_round_special_approval";
    private static final int SPECIAL_APPROVAL_START_ROUND = 1243;
    private static final int TEXT_PICKER = 4305;
    private static final String UPDATE_FILE = "shipmate_v3_update.html";
    private static final int UPDATE_PICKER = 4303;
    private static final String UPDATE_STATS_APP_KEY = "bxuiC99t7w3AG6PtIe-b3ui3xiIqRsRh-OKw0LU26qk";
    private static final String UPDATE_STATS_URL = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/app-update-heartbeat";
    private static final String UPDATE_TRACK_PREFS = "v3_update_tracking";
    private MediaPlayer launchSeaPlayer;
    private TextToSpeech launchTts;
    private TextureView launchVideo;
    private MediaPlayer launchVideoPlayer;
    private Surface launchVideoSurface;
    private String pendingData;
    private String pendingMime;
    private String pendingName;
    private MediaPlayer radioPlayer;
    private FrameLayout rootLayout;
    private MediaPlayer scoreFanfarePlayer;
    private TextToSpeech scoreTts;
    private WebView webView;
    private String pendingMusicLibraryMode = "multi";
    private boolean scoreTtsReady = false;
    private String pendingScoreSpeech = null;
    private int pendingScoreRank = 0;
    private boolean radioPlaying = false;
    private String radioName = "";
    private String radioUrl = "";
    private boolean launchVideoPrepared = false;
    private boolean launchVideoActive = false;
    private boolean launchTtsReady = false;
    private boolean launchSpeechRequested = false;
    private boolean launchIntroFinished = false;
    private boolean overlayUpdateScreenActive = false;
    private final Handler scoreHandler = new Handler(Looper.getMainLooper());

    protected void configureAppWebView(WebView webView) {
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) throws PackageManager.NameNotFoundException {
        super.onCreate(bundle);
        prepareBuildMigration();
        setVolumeControlStream(3);
        startAlwaysOverlayExperiment();
        FrameLayout frameLayout = new FrameLayout(this);
        this.rootLayout = frameLayout;
        setContentView(frameLayout);
        WebView webView = new WebView(this);
        this.webView = webView;
        webView.setVisibility(0);
        this.rootLayout.addView(this.webView, new FrameLayout.LayoutParams(-1, -1));
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setAllowFileAccess(true);
        this.webView.getSettings().setAllowContentAccess(true);
        AndroidHostBridge androidHostBridge = new AndroidHostBridge();
        this.webView.addJavascriptInterface(androidHostBridge, "AndroidHost");
        this.webView.addJavascriptInterface(androidHostBridge, "BaekhakNative");
        this.webView.setWebViewClient(new WebViewClient());
        configureAppWebView(this.webView);
        initScoreTts();
        loadInstalledApp();
        handleSavedMusicIntent(getIntent());
        handleOverlayUpdateIntent(getIntent());
        if (getIntent() != null && getIntent().getBooleanExtra("v3_play_install_guide", false)) {
            getIntent().removeExtra("v3_play_install_guide");
            this.webView.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m36lambda$onCreate$0$combaekhakcentralcontrolMainActivity();
                }
            }, 1200L);
        }
        requestMusicNotificationPermission();
        handleOverlayAudioPermissionRequest(getIntent());
        requestAutoAudioPermissionOnce();
        reportStoredNameUpdateCheckinAsync();
    }

    /* renamed from: lambda$onCreate$0$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m36lambda$onCreate$0$combaekhakcentralcontrolMainActivity() {
        this.webView.evaluateJavascript("if(window.playInstallGuide){window.playInstallGuide();}", null);
    }

    private void startNativeVoyageIntro() throws IllegalStateException {
        FrameLayout frameLayout;
        this.launchIntroFinished = false;
        this.launchVideoActive = true;
        this.launchVideoPrepared = false;
        try {
            WebView webView = this.webView;
            if (webView != null) {
                webView.setVisibility(4);
            }
            FrameLayout frameLayout2 = this.rootLayout;
            if (frameLayout2 != null) {
                frameLayout2.setBackgroundColor(-16777216);
            }
            releaseNativeVoyageMedia();
            TextureView textureView = this.launchVideo;
            if (textureView != null && (frameLayout = this.rootLayout) != null) {
                try {
                    frameLayout.removeView(textureView);
                } catch (Exception unused) {
                }
            }
            TextureView textureView2 = new TextureView(this);
            this.launchVideo = textureView2;
            textureView2.setOpaque(true);
            this.launchVideo.setBackgroundColor(-16777216);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
            layoutParams.gravity = 17;
            this.rootLayout.addView(this.launchVideo, layoutParams);
            this.launchVideo.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() { // from class: com.baekhak.centralcontrol.MainActivity.1
                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) throws IllegalStateException {
                    MainActivity.this.prepareNativeVoyagePlayer(surfaceTexture);
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    try {
                        if (MainActivity.this.launchVideoSurface != null) {
                            MainActivity.this.launchVideoSurface.release();
                        }
                    } catch (Exception unused2) {
                    }
                    MainActivity.this.launchVideoSurface = null;
                    return true;
                }
            });
            if (this.launchVideo.isAvailable() && this.launchVideo.getSurfaceTexture() != null) {
                prepareNativeVoyagePlayer(this.launchVideo.getSurfaceTexture());
            }
            this.scoreHandler.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda18
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m71x9bd8867b();
                }
            }, 1800L);
            this.scoreHandler.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda19
                @Override // java.lang.Runnable
                public final void run() throws IllegalStateException {
                    this.f$0.m72x2878b17c();
                }
            }, 23000L);
        } catch (Exception unused2) {
            finishNativeVoyageVideo();
        }
    }

    /* renamed from: lambda$startNativeVoyageIntro$1$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m71x9bd8867b() {
        if (!this.launchVideoActive || this.launchVideoPrepared) {
            return;
        }
        try {
            TextureView textureView = this.launchVideo;
            if (textureView == null || !textureView.isAvailable() || this.launchVideo.getSurfaceTexture() == null) {
                return;
            }
            prepareNativeVoyagePlayer(this.launchVideo.getSurfaceTexture());
        } catch (Exception unused) {
        }
    }

    /* renamed from: lambda$startNativeVoyageIntro$2$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m72x2878b17c() throws IllegalStateException {
        if (this.launchVideoActive) {
            finishNativeVoyageVideo();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void prepareNativeVoyagePlayer(SurfaceTexture surfaceTexture) throws IllegalStateException {
        finishNativeVoyageVideo();
    }

    private void releaseNativeVoyageMedia() throws IllegalStateException {
        try {
            MediaPlayer mediaPlayer = this.launchVideoPlayer;
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        this.launchVideoPlayer.stop();
                    }
                } catch (Exception unused) {
                }
                try {
                    this.launchVideoPlayer.reset();
                } catch (Exception unused2) {
                }
                this.launchVideoPlayer.release();
            }
        } catch (Exception unused3) {
        }
        this.launchVideoPlayer = null;
        try {
            Surface surface = this.launchVideoSurface;
            if (surface != null) {
                surface.release();
            }
        } catch (Exception unused4) {
        }
        this.launchVideoSurface = null;
    }

    private void finishNativeVoyageVideo() throws IllegalStateException {
        FrameLayout frameLayout;
        if (this.launchVideoActive) {
            this.launchVideoActive = false;
            this.launchIntroFinished = true;
            this.launchVideoPrepared = false;
            releaseNativeVoyageMedia();
            try {
                TextToSpeech textToSpeech = this.launchTts;
                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
            } catch (Exception unused) {
            }
            try {
                TextureView textureView = this.launchVideo;
                if (textureView != null && (frameLayout = this.rootLayout) != null) {
                    frameLayout.removeView(textureView);
                }
            } catch (Exception unused2) {
            }
            this.launchVideo = null;
            WebView webView = this.webView;
            if (webView != null) {
                webView.setVisibility(0);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isOverlayControlEnabled() {
        try {
            return getSharedPreferences("overlay_control", 0).getBoolean("enabled", true);
        } catch (Exception unused) {
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setOverlayControlEnabled(boolean z) {
        try {
            getSharedPreferences("overlay_control", 0).edit().putBoolean("enabled", z).apply();
        } catch (Exception unused) {
        }
        try {
            Intent action = new Intent(this, (Class<?>) OverlayControlService.class).setAction(z ? OverlayControlService.ACTION_SHOW : OverlayControlService.ACTION_HIDE);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(action);
            } else {
                startService(action);
            }
        } catch (Exception unused2) {
        }
        if (!z || Settings.canDrawOverlays(this)) {
            return;
        }
        try {
            startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())));
        } catch (Exception unused3) {
        }
    }

    private void startAlwaysOverlayExperiment() {
        if (isOverlayControlEnabled()) {
            try {
                Intent action = new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_SHOW);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(action);
                } else {
                    startService(action);
                }
            } catch (Exception unused) {
            }
            if (Settings.canDrawOverlays(this)) {
                return;
            }
            try {
                startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())));
                Toast.makeText(this, "상단 음악·FM·AM 미니바를 위해 '다른 앱 위에 표시'를 허용해 주세요.", 1).show();
            } catch (Exception unused2) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeAppScreen() {
        FrameLayout frameLayout;
        stopBackgroundMusicForAppExit();
        try {
            this.launchVideoActive = false;
            this.launchIntroFinished = true;
            this.launchVideoPrepared = false;
            releaseNativeVoyageMedia();
            TextureView textureView = this.launchVideo;
            if (textureView != null && (frameLayout = this.rootLayout) != null) {
                try {
                    frameLayout.removeView(textureView);
                } catch (Exception unused) {
                }
            }
            this.launchVideo = null;
            try {
                TextToSpeech textToSpeech = this.launchTts;
                if (textToSpeech != null) {
                    textToSpeech.stop();
                }
            } catch (Exception unused2) {
            }
            WebView webView = this.webView;
            if (webView != null) {
                webView.setVisibility(4);
                this.webView.stopLoading();
                this.webView.loadUrl("about:blank");
            }
        } catch (Exception unused3) {
        }
        try {
            try {
                finishAndRemoveTask();
            } catch (Exception unused4) {
            }
        } catch (Exception unused5) {
            finish();
        }
    }

    private void startLaunchSeaSound() throws IllegalStateException {
        try {
            MediaPlayer mediaPlayer = this.launchSeaPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.release();
                this.launchSeaPlayer = null;
            }
            MediaPlayer mediaPlayerCreate = MediaPlayer.create(this, R.raw.sea_intro);
            this.launchSeaPlayer = mediaPlayerCreate;
            if (mediaPlayerCreate != null) {
                mediaPlayerCreate.setLooping(true);
                this.launchSeaPlayer.setVolume(0.42f, 0.42f);
                this.launchSeaPlayer.start();
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishLaunchIntroNative() throws IllegalStateException {
        if (this.launchIntroFinished) {
            return;
        }
        this.launchIntroFinished = true;
        try {
            MediaPlayer mediaPlayer = this.launchSeaPlayer;
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    this.launchSeaPlayer.stop();
                }
                this.launchSeaPlayer.release();
                this.launchSeaPlayer = null;
            }
        } catch (Exception unused) {
            this.launchSeaPlayer = null;
        }
        try {
            TextToSpeech textToSpeech = this.launchTts;
            if (textToSpeech != null) {
                textToSpeech.stop();
            }
        } catch (Exception unused2) {
        }
    }

    private void initLaunchTts() {
        try {
            this.launchTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda23
                @Override // android.speech.tts.TextToSpeech.OnInitListener
                public final void onInit(int i) {
                    this.f$0.m30lambda$initLaunchTts$3$combaekhakcentralcontrolMainActivity(i);
                }
            });
        } catch (Exception unused) {
            this.launchTts = null;
            this.launchTtsReady = false;
        }
    }

    /* renamed from: lambda$initLaunchTts$3$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m30lambda$initLaunchTts$3$combaekhakcentralcontrolMainActivity(int i) {
        TextToSpeech textToSpeech;
        if (i != 0 || (textToSpeech = this.launchTts) == null) {
            return;
        }
        try {
            int language = textToSpeech.setLanguage(Locale.KOREA);
            this.launchTtsReady = (language == -1 || language == -2) ? false : true;
            this.launchTts.setSpeechRate(0.86f);
            this.launchTts.setPitch(1.18f);
            this.launchTts.setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(1).build());
            try {
                Voice voice = null;
                Voice voice2 = null;
                for (Voice voice3 : this.launchTts.getVoices()) {
                    if (voice3 != null && voice3.getLocale() != null && "ko".equalsIgnoreCase(voice3.getLocale().getLanguage())) {
                        if (voice2 == null) {
                            voice2 = voice3;
                        }
                        String lowerCase = String.valueOf(voice3.getName()).toLowerCase(Locale.ROOT);
                        String lowerCase2 = String.valueOf(voice3.getFeatures()).toLowerCase(Locale.ROOT);
                        if (lowerCase.contains("female") || lowerCase.contains("woman") || lowerCase2.contains("female")) {
                            this.launchTts.setVoice(voice3);
                            break;
                        }
                    }
                }
                voice = voice2;
                if (voice != null) {
                    this.launchTts.setVoice(voice);
                }
            } catch (Exception unused) {
            }
            if (this.launchTtsReady && this.launchSpeechRequested && !this.launchIntroFinished) {
                speakLaunchWelcomeNow();
            }
        } catch (Exception unused2) {
        }
    }

    private void speakLaunchWelcome() {
        if (this.launchIntroFinished) {
            return;
        }
        this.launchSpeechRequested = true;
        if (this.launchTtsReady) {
            speakLaunchWelcomeNow();
        }
    }

    private void speakLaunchWelcomeNow() {
        if (this.launchIntroFinished || this.launchTts == null) {
            return;
        }
        this.launchSpeechRequested = false;
        try {
            MediaPlayer mediaPlayer = this.launchSeaPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0.26f, 0.26f);
            }
            Bundle bundle = new Bundle();
            bundle.putFloat(MusicPlaybackService.EXTRA_VOLUME, 1.0f);
            this.launchTts.speak(LAUNCH_WELCOME_TEXT, 0, bundle, "V3_LAUNCH_WELCOME");
        } catch (Exception unused) {
        }
    }

    private String getOrCreateAnonymousInstallId() {
        SharedPreferences sharedPreferences = getSharedPreferences(UPDATE_TRACK_PREFS, 0);
        String string = sharedPreferences.getString("anonymous_install_id", "");
        if (string != null && !string.trim().isEmpty()) {
            return string;
        }
        String string2 = UUID.randomUUID().toString();
        sharedPreferences.edit().putString("anonymous_install_id", string2).apply();
        return string2;
    }

    private String currentVersionName() throws PackageManager.NameNotFoundException {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName == null ? "V3" : packageInfo.versionName;
        } catch (Exception unused) {
            return "V3";
        }
    }

    private String postUpdateApi(JSONObject jSONObject) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(UPDATE_STATS_URL).openConnection();
        httpURLConnection.setConnectTimeout(7000);
        httpURLConnection.setReadTimeout(7000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        httpURLConnection.setRequestProperty("x-app-key", UPDATE_STATS_APP_KEY);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        try {
            outputStream.write(jSONObject.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            if (outputStream != null) {
                outputStream.close();
            }
            int responseCode = httpURLConnection.getResponseCode();
            InputStream errorStream = (responseCode < 200 || responseCode >= 300) ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
            String str = errorStream == null ? "" : new String(readAll(errorStream), StandardCharsets.UTF_8);
            httpURLConnection.disconnect();
            if (responseCode >= 200 && responseCode < 300) {
                return str;
            }
            throw new Exception("HTTP " + responseCode);
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    private void reportUpdateCheckinAsync() throws PackageManager.NameNotFoundException {
        final long installedVersionCode = getInstalledVersionCode();
        final SharedPreferences sharedPreferences = getSharedPreferences(UPDATE_TRACK_PREFS, 0);
        if (sharedPreferences.getLong("reported_version_code", -1L) == installedVersionCode) {
            return;
        }
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m48xb451bb96(installedVersionCode, sharedPreferences);
            }
        }, "V3-UpdateCheckin").start();
    }

    /* renamed from: lambda$reportUpdateCheckinAsync$4$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m48xb451bb96(long j, SharedPreferences sharedPreferences) throws JSONException {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("action", "checkin");
            jSONObject.put("install_id", getOrCreateAnonymousInstallId());
            jSONObject.put("version_name", currentVersionName());
            jSONObject.put("version_code", j);
            if (new JSONObject(postUpdateApi(jSONObject)).optBoolean("ok", false)) {
                sharedPreferences.edit().putLong("reported_version_code", j).apply();
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestUpdateStatsAsync() {
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda21
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m53xe0521c27();
            }
        }, "V3-UpdateStats").start();
    }

    /* renamed from: lambda$requestUpdateStatsAsync$6$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m53xe0521c27() throws JSONException {
        final String string;
        try {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("action", "stats");
                string = postUpdateApi(jSONObject);
            } catch (Exception unused) {
                string = "{\"ok\":false}";
            }
        } catch (Exception unused2) {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("ok", false);
            jSONObject2.put("error", "network");
            string = jSONObject2.toString();
        }
        WebView webView = this.webView;
        if (webView != null) {
            webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda45
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m52x53b1f126(string);
                }
            });
        }
    }

    /* renamed from: lambda$requestUpdateStatsAsync$5$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m52x53b1f126(String str) {
        this.webView.evaluateJavascript("onUpdateStats(" + JSONObject.quote(str) + ");", null);
    }

    private String postUpdateAdminApi(JSONObject jSONObject) throws Exception {
        throw new Exception("admin_stats_disabled_in_distribution");
    }

    private String cleanUpdateDisplayName(String str) {
        String strReplaceAll = str == null ? "" : str.trim().replaceAll("\\s+", " ");
        return (strReplaceAll.isEmpty() || strReplaceAll.length() > 40 || strReplaceAll.matches(".*\\d.*") || strReplaceAll.contains("@")) ? "" : strReplaceAll;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getStoredUpdateDisplayName() {
        return getSharedPreferences(UPDATE_TRACK_PREFS, 0).getString("update_display_name", "");
    }

    private void reportStoredNameUpdateCheckinAsync() throws PackageManager.NameNotFoundException {
        String strCleanUpdateDisplayName = cleanUpdateDisplayName(getStoredUpdateDisplayName());
        if (strCleanUpdateDisplayName.isEmpty()) {
            return;
        }
        reportNamedUpdateCheckinAsync(strCleanUpdateDisplayName);
    }

    private void reportNamedUpdateCheckinAsync(String str) throws PackageManager.NameNotFoundException {
        final String strCleanUpdateDisplayName = cleanUpdateDisplayName(str);
        if (strCleanUpdateDisplayName.isEmpty()) {
            return;
        }
        final long installedVersionCode = getInstalledVersionCode();
        final SharedPreferences sharedPreferences = getSharedPreferences(UPDATE_TRACK_PREFS, 0);
        String string = sharedPreferences.getString("reported_named_name", "");
        if (sharedPreferences.getLong("reported_named_version_code", -1L) == installedVersionCode && strCleanUpdateDisplayName.equals(string)) {
            return;
        }
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda32
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m47x2477c470(strCleanUpdateDisplayName, installedVersionCode, sharedPreferences);
            }
        }, "V3-NamedUpdateCheckin").start();
    }

    /* renamed from: lambda$reportNamedUpdateCheckinAsync$7$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m47x2477c470(String str, long j, SharedPreferences sharedPreferences) throws JSONException {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("action", "named_checkin");
            jSONObject.put("display_name", str);
            jSONObject.put("version_name", currentVersionName());
            jSONObject.put("version_code", j);
            if (new JSONObject(postUpdateApi(jSONObject)).optBoolean("ok", false)) {
                sharedPreferences.edit().putString("update_display_name", str).putString("reported_named_name", str).putLong("reported_named_version_code", j).apply();
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setUpdateDisplayNameAndReportAsync(String str) throws PackageManager.NameNotFoundException {
        final String strCleanUpdateDisplayName = cleanUpdateDisplayName(str);
        if (strCleanUpdateDisplayName.isEmpty()) {
            WebView webView = this.webView;
            if (webView != null) {
                webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda59
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m63x9ef61621();
                    }
                });
                return;
            }
            return;
        }
        getSharedPreferences(UPDATE_TRACK_PREFS, 0).edit().putString("update_display_name", strCleanUpdateDisplayName).apply();
        reportNamedUpdateCheckinAsync(strCleanUpdateDisplayName);
        WebView webView2 = this.webView;
        if (webView2 != null) {
            webView2.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda60
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m64x2b964122(strCleanUpdateDisplayName);
                }
            }, 250L);
        }
    }

    /* renamed from: lambda$setUpdateDisplayNameAndReportAsync$8$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m63x9ef61621() {
        this.webView.evaluateJavascript("if(window.onUpdateNameSaved){onUpdateNameSaved(false,'이름만 입력해 주세요. 숫자·전화번호·이메일은 저장하지 않습니다.');}", null);
    }

    /* renamed from: lambda$setUpdateDisplayNameAndReportAsync$9$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m64x2b964122(String str) {
        this.webView.evaluateJavascript("if(window.onUpdateNameSaved){onUpdateNameSaved(true," + JSONObject.quote(str) + ");}", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestNamedUpdateStatsAsync() {
        WebView webView = this.webView;
        if (webView != null) {
            webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda24
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m50xfe79dd93();
                }
            });
        }
    }

    /* renamed from: lambda$requestNamedUpdateStatsAsync$10$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m50xfe79dd93() {
        this.webView.evaluateJavascript("if(window.onNamedUpdateStats){onNamedUpdateStats(" + JSONObject.quote("{\"ok\":false,\"error\":\"admin_view_disabled_in_distribution\"}") + ");}", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public SharedPreferences specialApprovalPrefs() {
        return getSharedPreferences(SPECIAL_APPROVAL_PREFS, 0);
    }

    private String getOrCreateApprovalDeviceId() {
        SharedPreferences sharedPreferencesSpecialApprovalPrefs = specialApprovalPrefs();
        String string = sharedPreferencesSpecialApprovalPrefs.getString("device_id", "");
        if (string == null || !string.matches("\\d{6,20}")) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            String strValueOf = String.valueOf(Math.abs(UUID.randomUUID().hashCode()));
            if (strValueOf.length() > 5) {
                strValueOf = strValueOf.substring(strValueOf.length() - 5);
            }
            string = String.valueOf(jCurrentTimeMillis) + strValueOf;
            if (string.length() > 20) {
                string = string.substring(string.length() - 20);
            }
            sharedPreferencesSpecialApprovalPrefs.edit().putString("device_id", string).apply();
        }
        return string;
    }

    private String postJson(String str, JSONObject jSONObject) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
        httpURLConnection.setConnectTimeout(7000);
        httpURLConnection.setReadTimeout(7000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        OutputStream outputStream = httpURLConnection.getOutputStream();
        try {
            outputStream.write(jSONObject.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            if (outputStream != null) {
                outputStream.close();
            }
            int responseCode = httpURLConnection.getResponseCode();
            InputStream errorStream = (responseCode < 200 || responseCode >= 300) ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
            String str2 = errorStream == null ? "" : new String(readAll(errorStream), StandardCharsets.UTF_8);
            httpURLConnection.disconnect();
            if (responseCode >= 200 && responseCode < 300) {
                return str2;
            }
            throw new Exception("HTTP " + responseCode + " " + str2);
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isSpecialApprovedLocal() {
        return specialApprovalPrefs().getBoolean("approved", false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String specialApprovalSummaryJson() throws JSONException {
        String str = "approved";
        try {
            SharedPreferences sharedPreferencesSpecialApprovalPrefs = specialApprovalPrefs();
            JSONObject jSONObject = new JSONObject();
            boolean z = sharedPreferencesSpecialApprovalPrefs.getBoolean("approved", false);
            boolean zIsEmpty = sharedPreferencesSpecialApprovalPrefs.getString("request_token", "").isEmpty();
            jSONObject.put("approved", z);
            jSONObject.put("requested_round", sharedPreferencesSpecialApprovalPrefs.getInt("requested_round", SPECIAL_APPROVAL_START_ROUND));
            jSONObject.put("approval_code", sharedPreferencesSpecialApprovalPrefs.getString("approval_code", ""));
            jSONObject.put("has_request", !zIsEmpty);
            jSONObject.put("applicant_name", sharedPreferencesSpecialApprovalPrefs.getString("applicant_name", ""));
            jSONObject.put("referrer_name", sharedPreferencesSpecialApprovalPrefs.getString("referrer_name", ""));
            if (!z) {
                str = !zIsEmpty ? "pending" : "not_requested";
            }
            jSONObject.put("status", str);
            return jSONObject.toString();
        } catch (Exception unused) {
            return "{\"approved\":false}";
        }
    }

    private void sendSpecialApprovalState(final String str) {
        if (str == null) {
            str = "{}";
        }
        WebView webView = this.webView;
        if (webView != null) {
            webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m62xaddc147(str);
                }
            });
        }
    }

    /* renamed from: lambda$sendSpecialApprovalState$11$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m62xaddc147(String str) {
        this.webView.evaluateJavascript("if(window.onSpecialApprovalState){onSpecialApprovalState(" + JSONObject.quote(str) + ");}", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestSpecialApprovalAsync(final int i, String str, String str2) {
        if (i < SPECIAL_APPROVAL_START_ROUND) {
            sendSpecialApprovalState("{\"ok\":true,\"status\":\"approved\",\"round_policy_free\":true}");
            return;
        }
        final String strTrim = "";
        final String strTrim2 = str == null ? "" : str.trim();
        if (str2 != null) {
            strTrim = str2.trim();
        }
        if (strTrim2.isEmpty() || strTrim.isEmpty()) {
            sendSpecialApprovalState("{\"ok\":false,\"error\":\"identity_required\"}");
        } else {
            specialApprovalPrefs().edit().putString("applicant_name", strTrim2).putString("referrer_name", strTrim).apply();
            new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda55
                @Override // java.lang.Runnable
                public final void run() throws JSONException {
                    this.f$0.m51xefff1f70(strTrim2, strTrim, i);
                }
            }, "V3-SpecialApprovalRequest").start();
        }
    }

    /* renamed from: lambda$requestSpecialApprovalAsync$12$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m51xefff1f70(String str, String str2, int i) throws JSONException {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("device_id", getOrCreateApprovalDeviceId());
            jSONObject.put("device_label", str + "||" + str2);
            jSONObject.put("referrer_name", str2);
            jSONObject.put("purpose_ack", true);
            jSONObject.put("requested_round", i);
            String strPostJson = postJson(APPROVAL_REQUEST_URL, jSONObject);
            JSONObject jSONObject2 = new JSONObject(strPostJson);
            SharedPreferences.Editor editorEdit = specialApprovalPrefs().edit();
            if (jSONObject2.has("request_token")) {
                editorEdit.putString("request_token", jSONObject2.optString("request_token", ""));
            }
            if (jSONObject2.has("approval_code")) {
                editorEdit.putString("approval_code", jSONObject2.optString("approval_code", ""));
            }
            editorEdit.putInt("requested_round", i);
            if ("approved".equalsIgnoreCase(jSONObject2.optString("status", ""))) {
                editorEdit.putBoolean("approved", true);
            }
            editorEdit.apply();
            JSONObject jSONObject3 = new JSONObject(strPostJson);
            String string = specialApprovalPrefs().getString("approval_code", "");
            if (!string.isEmpty() && !jSONObject3.has("approval_code")) {
                jSONObject3.put("approval_code", string);
            }
            if (!jSONObject3.has("has_request")) {
                jSONObject3.put("has_request", !specialApprovalPrefs().getString("request_token", "").isEmpty());
            }
            sendSpecialApprovalState(jSONObject3.toString());
        } catch (Exception unused) {
            sendSpecialApprovalState("{\"ok\":false,\"error\":\"network\"}");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkSpecialApprovalAsync() throws JSONException {
        if (isSpecialApprovedLocal()) {
            try {
                JSONObject jSONObject = new JSONObject(specialApprovalSummaryJson());
                jSONObject.put("ok", true);
                jSONObject.put("status", "approved");
                jSONObject.put("special_approval", true);
                sendSpecialApprovalState(jSONObject.toString());
                return;
            } catch (Exception unused) {
                sendSpecialApprovalState(specialApprovalSummaryJson());
                return;
            }
        }
        final SharedPreferences sharedPreferencesSpecialApprovalPrefs = specialApprovalPrefs();
        final String string = sharedPreferencesSpecialApprovalPrefs.getString("request_token", "");
        if (string == null || string.isEmpty()) {
            sendSpecialApprovalState("{\"ok\":true,\"status\":\"not_requested\"}");
        } else {
            new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda22
                @Override // java.lang.Runnable
                public final void run() throws JSONException {
                    this.f$0.m18x869a818(string, sharedPreferencesSpecialApprovalPrefs);
                }
            }, "V3-SpecialApprovalStatus").start();
        }
    }

    /* renamed from: lambda$checkSpecialApprovalAsync$13$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m18x869a818(String str, SharedPreferences sharedPreferences) throws JSONException {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("request_token", str);
            jSONObject.put("device_id", getOrCreateApprovalDeviceId());
            String strPostJson = postJson(APPROVAL_STATUS_URL, jSONObject);
            if ("approved".equalsIgnoreCase(new JSONObject(strPostJson).optString("status", ""))) {
                sharedPreferences.edit().putBoolean("approved", true).apply();
            }
            JSONObject jSONObject2 = new JSONObject(strPostJson);
            String string = sharedPreferences.getString("approval_code", "");
            if (!string.isEmpty() && !jSONObject2.has("approval_code")) {
                jSONObject2.put("approval_code", string);
            }
            if (!jSONObject2.has("has_request")) {
                jSONObject2.put("has_request", !sharedPreferences.getString("request_token", "").isEmpty());
            }
            sendSpecialApprovalState(jSONObject2.toString());
        } catch (Exception unused) {
            sendSpecialApprovalState("{\"ok\":false,\"error\":\"network\"}");
        }
    }

    private long getInstalledVersionCode() throws PackageManager.NameNotFoundException {
        try {
            return Build.VERSION.SDK_INT >= 28 ? getPackageManager().getPackageInfo(getPackageName(), 0).getLongVersionCode() : r0.versionCode;
        } catch (Exception unused) {
            return 1062L;
        }
    }

    private void prepareBuildMigration() {
        try {
            File file = new File(getFilesDir(), UPDATE_FILE);
            if (file.exists()) {
                File file2 = new File(getFilesDir(), "shipmate_v3_update.html.before_integrated");
                if (!file2.exists()) {
                    file.renameTo(file2);
                }
            }
        } catch (Exception unused) {
        }
        getSharedPreferences("v3_native_state", 0).edit().putLong("native_version_code", getInstalledVersionCode()).putInt("content_build", 1062).apply();
    }

    private void loadInstalledApp() {
        this.webView.loadUrl("file:///android_asset/index.html");
    }

    private byte[] readAll(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        while (true) {
            int i = inputStream.read(bArr);
            if (i <= 0) {
                return byteArrayOutputStream.toByteArray();
            }
            byteArrayOutputStream.write(bArr, 0, i);
        }
    }

    private String readUri(Uri uri) throws Exception {
        InputStream inputStreamOpenInputStream = getContentResolver().openInputStream(uri);
        try {
            if (inputStreamOpenInputStream == null) {
                throw new Exception("open failed");
            }
            String str = new String(readAll(inputStreamOpenInputStream), StandardCharsets.UTF_8);
            if (inputStreamOpenInputStream != null) {
                inputStreamOpenInputStream.close();
            }
            return str;
        } catch (Throwable th) {
            if (inputStreamOpenInputStream != null) {
                try {
                    inputStreamOpenInputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    private String displayName(Uri uri) {
        int columnIndex;
        String string;
        try {
            Cursor cursorQuery = getContentResolver().query(uri, null, null, null, null);
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.moveToFirst() && (columnIndex = cursorQuery.getColumnIndex("_display_name")) >= 0 && (string = cursorQuery.getString(columnIndex)) != null) {
                        if (!string.trim().isEmpty()) {
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return string;
                        }
                    }
                } finally {
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
                return "파일";
            }
            return "파일";
        } catch (Exception unused) {
            return "파일";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean installUpdate(final String str) throws IOException {
        if (str != null && str.length() >= 300 && str.contains("SHIPMATE_V3_UPDATE")) {
            File file = new File(getFilesDir(), "shipmate_v3_update.html.tmp");
            File file2 = new File(getFilesDir(), UPDATE_FILE);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                try {
                    fileOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    if (file2.exists() && !file2.delete()) {
                        file.delete();
                        return false;
                    }
                    if (file.renameTo(file2)) {
                        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda11
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.m32lambda$installUpdate$14$combaekhakcentralcontrolMainActivity(str);
                            }
                        });
                        return true;
                    }
                    file.delete();
                    return false;
                } finally {
                }
            } catch (Exception unused) {
            }
        }
        return false;
    }

    /* renamed from: lambda$installUpdate$14$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m32lambda$installUpdate$14$combaekhakcentralcontrolMainActivity(String str) {
        this.webView.loadDataWithBaseURL(BASE_URL, str, "text/html", "UTF-8", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void alert(String str) {
        if (str == null) {
            str = "";
        }
        final String strQuote = JSONObject.quote(str);
        this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda44
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m17lambda$alert$15$combaekhakcentralcontrolMainActivity(strQuote);
            }
        });
    }

    /* renamed from: lambda$alert$15$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m17lambda$alert$15$combaekhakcentralcontrolMainActivity(String str) {
        this.webView.evaluateJavascript("alert(" + str + ");", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createSave(final String str, final String str2, String str3) {
        this.pendingName = str;
        this.pendingMime = str2;
        this.pendingData = str3;
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m22lambda$createSave$16$combaekhakcentralcontrolMainActivity(str2, str);
            }
        });
    }

    /* renamed from: lambda$createSave$16$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m22lambda$createSave$16$combaekhakcentralcontrolMainActivity(String str, String str2) {
        Intent intent = new Intent("android.intent.action.CREATE_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType(str);
        intent.putExtra("android.intent.extra.TITLE", str2);
        try {
            startActivityForResult(intent, SAVE_PICKER);
        } catch (Exception unused) {
            this.pendingData = null;
            alert("저장창을 열 수 없습니다.");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveBackupDirect(final String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        if (Build.VERSION.SDK_INT < 29) {
            createSave("항행의자유_V3_전체백업.json", "application/json", str);
        } else {
            new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda46
                @Override // java.lang.Runnable
                public final void run() throws Exception {
                    this.f$0.m55x3da34983(str);
                }
            }, "V3-Backup").start();
        }
    }

    /* renamed from: lambda$saveBackupDirect$18$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m55x3da34983(final String str) throws Exception {
        Uri uriInsert;
        try {
            String str2 = "항행의자유_V3_전체백업_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(new Date()) + ".json";
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", str2);
            contentValues.put("mime_type", "application/json");
            contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS + "/항행의자유");
            contentValues.put("is_pending", (Integer) 1);
            uriInsert = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            try {
                if (uriInsert == null) {
                    throw new Exception("MediaStore insert failed");
                }
                OutputStream outputStreamOpenOutputStream = getContentResolver().openOutputStream(uriInsert, "w");
                try {
                    if (outputStreamOpenOutputStream == null) {
                        throw new Exception("openOutputStream failed");
                    }
                    outputStreamOpenOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
                    outputStreamOpenOutputStream.flush();
                    if (outputStreamOpenOutputStream != null) {
                        outputStreamOpenOutputStream.close();
                    }
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put("is_pending", (Integer) 0);
                    getContentResolver().update(uriInsert, contentValues2, null, null);
                    alert("전체 백업 완료\n내 파일 > 다운로드 > 항행의자유\n" + str2);
                } finally {
                }
            } catch (Exception unused) {
                if (uriInsert != null) {
                    try {
                        getContentResolver().delete(uriInsert, null, null);
                    } catch (Exception unused2) {
                    }
                }
                alert("자동 백업 저장 실패. 저장 위치 선택창으로 전환합니다.");
                runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda53
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m54xb1031e82(str);
                    }
                });
            }
        } catch (Exception unused3) {
            uriInsert = null;
        }
    }

    /* renamed from: lambda$saveBackupDirect$17$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m54xb1031e82(String str) {
        createSave("항행의자유_V3_전체백업.json", "application/json", str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String saveBackupDirectSync(String str) throws Exception {
        Uri uriInsert;
        if (str == null || str.isEmpty()) {
            return "ERR|백업 데이터가 비어 있습니다.";
        }
        if (Build.VERSION.SDK_INT < 29) {
            return "ERR|Android 10 미만에서는 자동 저장을 사용할 수 없습니다.";
        }
        try {
            final String str2 = "항행의자유_V3_전체백업_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(new Date()) + ".json";
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", str2);
            contentValues.put("mime_type", "application/json");
            contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS + "/항행의자유");
            contentValues.put("is_pending", (Integer) 1);
            uriInsert = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            try {
                if (uriInsert == null) {
                    throw new Exception("MediaStore insert failed");
                }
                OutputStream outputStreamOpenOutputStream = getContentResolver().openOutputStream(uriInsert, "w");
                try {
                    if (outputStreamOpenOutputStream == null) {
                        throw new Exception("openOutputStream failed");
                    }
                    outputStreamOpenOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
                    outputStreamOpenOutputStream.flush();
                    if (outputStreamOpenOutputStream != null) {
                        outputStreamOpenOutputStream.close();
                    }
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put("is_pending", (Integer) 0);
                    getContentResolver().update(uriInsert, contentValues2, null, null);
                    runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda5
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m56x6a4b0969(str2);
                        }
                    });
                    return "OK|" + str2;
                } finally {
                }
            } catch (Exception e) {
                e = e;
                if (uriInsert != null) {
                    try {
                        getContentResolver().delete(uriInsert, null, null);
                    } catch (Exception unused) {
                    }
                }
                final String message = e.getMessage() == null ? "자동 저장 실패" : e.getMessage();
                runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda6
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m57x800ebb7f(message);
                    }
                });
                return "ERR|" + message;
            }
        } catch (Exception e2) {
            e = e2;
            uriInsert = null;
        }
    }

    /* renamed from: lambda$saveBackupDirectSync$19$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m56x6a4b0969(String str) {
        Toast.makeText(this, "전체 백업 완료: " + str, 1).show();
    }

    /* renamed from: lambda$saveBackupDirectSync$20$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m57x800ebb7f(String str) {
        Toast.makeText(this, "전체 백업 실패: " + str, 1).show();
    }

    private void sendLegacyBackupToJs(final String str, final String str2) {
        if (str2 == null) {
            return;
        }
        this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda39
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m61xcf15a4cc(str, str2);
            }
        });
    }

    /* renamed from: lambda$sendLegacyBackupToJs$21$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m61xcf15a4cc(String str, String str2) {
        WebView webView = this.webView;
        StringBuilder sb = new StringBuilder("legacyImportStart(");
        if (str == null) {
            str = "backup.json";
        }
        sb.append(JSONObject.quote(str));
        sb.append(");");
        webView.evaluateJavascript(sb.toString(), null);
        int i = 0;
        while (i < str2.length()) {
            int i2 = i + 12000;
            String strSubstring = str2.substring(i, Math.min(str2.length(), i2));
            this.webView.evaluateJavascript("legacyImportChunk(" + JSONObject.quote(strSubstring) + ");", null);
            i = i2;
        }
        this.webView.evaluateJavascript("legacyImportFinish();", null);
    }

    private SharedPreferences musicPrefs() {
        return getSharedPreferences(MUSIC_PREFS, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean musicEnabled() {
        return musicPrefs().getBoolean(MUSIC_ENABLED, false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int musicVolume() {
        return Math.max(0, Math.min(100, musicPrefs().getInt(MUSIC_VOLUME, 42)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String musicName() {
        try {
            String strOptString = new JSONObject(MusicPlaybackService.snapshot(this)).optString(RadioPlaybackService.EXTRA_NAME, "");
            if (!strOptString.isEmpty()) {
                return strOptString;
            }
        } catch (Exception unused) {
        }
        return musicPrefs().getString(MUSIC_NAME, "");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendMusicCommand(String str) {
        try {
            Intent intent = new Intent(this, (Class<?>) MusicPlaybackService.class);
            intent.setAction(str);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Exception unused) {
        }
    }

    private void requestMusicNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
                    requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 4310);
                }
            } catch (Exception unused) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopBackgroundMusic() {
        musicPrefs().edit().putBoolean(MUSIC_ENABLED, false).apply();
        sendMusicCommand(MusicPlaybackService.ACTION_STOP);
    }

    private void stopBackgroundMusicForAppExit() {
        sendMusicCommand(MusicPlaybackService.ACTION_APP_EXIT);
    }

    private void startBackgroundMusic() {
        if (musicEnabled()) {
            sendMusicCommand(MusicPlaybackService.ACTION_PLAY);
        }
    }

    private void pauseBackgroundMusic() {
        sendMusicCommand(MusicPlaybackService.ACTION_PAUSE);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyMusicVolume(int i) {
        int iMax = Math.max(0, Math.min(100, i));
        musicPrefs().edit().putInt(MUSIC_VOLUME, iMax).apply();
        try {
            Intent intent = new Intent(this, (Class<?>) MusicPlaybackService.class);
            intent.setAction(MusicPlaybackService.ACTION_VOLUME);
            intent.putExtra(MusicPlaybackService.EXTRA_VOLUME, iMax);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean toggleBackgroundMusic() {
        boolean zMusicEnabled = musicEnabled();
        boolean z = !zMusicEnabled;
        musicPrefs().edit().putBoolean(MUSIC_ENABLED, z).apply();
        if (zMusicEnabled) {
            pauseBackgroundMusic();
        } else {
            startBackgroundMusic();
        }
        return z;
    }

    private boolean isBlockedPickerPackage(String str) {
        if (str == null) {
            return false;
        }
        String lowerCase = str.toLowerCase(Locale.US);
        return lowerCase.contains("androidide") || lowerCase.contains("m4coding") || lowerCase.contains("aide") || lowerCase.contains("codeassist");
    }

    private boolean tryExplicitDocumentsUi(Intent intent, int i) {
        String[] strArr = {"com.google.android.documentsui", "com.android.documentsui"};
        for (int i2 = 0; i2 < 2; i2++) {
            String str = strArr[i2];
            try {
                Intent intent2 = new Intent(intent);
                intent2.setComponent(new ComponentName(str, "com.android.documentsui.picker.PickActivity"));
                startActivityForResult(intent2, i);
                return true;
            } catch (Exception unused) {
            }
        }
        return false;
    }

    private boolean tryResolvedSafePicker(Intent intent, int i) {
        try {
            List<ResolveInfo> listQueryIntentActivities = getPackageManager().queryIntentActivities(intent, 65536);
            if (listQueryIntentActivities != null) {
                String[] strArr = {"com.sec.android.app.myfiles", "com.google.android.documentsui", "com.android.documentsui"};
                for (int i2 = 0; i2 < 3; i2++) {
                    String str = strArr[i2];
                    for (ResolveInfo resolveInfo : listQueryIntentActivities) {
                        if (resolveInfo != null && resolveInfo.activityInfo != null) {
                            String str2 = resolveInfo.activityInfo.packageName;
                            if (str.equals(str2) && !isBlockedPickerPackage(str2)) {
                                Intent intent2 = new Intent(intent);
                                intent2.setComponent(new ComponentName(str2, resolveInfo.activityInfo.name));
                                startActivityForResult(intent2, i);
                                return true;
                            }
                        }
                    }
                }
                for (ResolveInfo resolveInfo2 : listQueryIntentActivities) {
                    if (resolveInfo2 != null && resolveInfo2.activityInfo != null) {
                        String str3 = resolveInfo2.activityInfo.packageName;
                        if (!isBlockedPickerPackage(str3)) {
                            Intent intent3 = new Intent(intent);
                            intent3.setComponent(new ComponentName(str3, resolveInfo2.activityInfo.name));
                            startActivityForResult(intent3, i);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception unused) {
        }
        return false;
    }

    private boolean startPreferredFilesPicker(Intent intent, int i) {
        if (tryExplicitDocumentsUi(intent, i) || tryResolvedSafePicker(intent, i)) {
            return true;
        }
        try {
            startActivityForResult(new Intent(intent), i);
            return true;
        } catch (Exception unused) {
            Toast.makeText(this, "시스템 파일 선택 화면을 열지 못했습니다.", 1).show();
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openSavedMusicLibraryChooser() {
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda56
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m40x118a479f();
            }
        });
    }

    /* renamed from: lambda$openSavedMusicLibraryChooser$25$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m40x118a479f() {
        String strOptString = "";
        try {
            final List<MusicLibraryFiles.Track> list = new MusicLibraryStore(this).read();
            if (list != null && !list.isEmpty()) {
                try {
                    strOptString = new JSONObject(MusicPlaybackService.snapshot(this)).optString("id", "");
                } catch (Exception unused) {
                }
                CharSequence[] charSequenceArr = new CharSequence[list.size()];
                int i = -1;
                for (int i2 = 0; i2 < list.size(); i2++) {
                    MusicLibraryFiles.Track track = list.get(i2);
                    charSequenceArr[i2] = track.name;
                    if (track.id.equals(strOptString)) {
                        i = i2;
                    }
                }
                final AlertDialog alertDialogCreate = new AlertDialog.Builder(this).setTitle("저장된 MP3 · " + list.size() + "곡").setSingleChoiceItems(charSequenceArr, i, (DialogInterface.OnClickListener) null).setNegativeButton("닫기", (DialogInterface.OnClickListener) null).setNeutralButton("음악폴더 다시 불러오기", (DialogInterface.OnClickListener) null).setPositiveButton("선택곡 재생", (DialogInterface.OnClickListener) null).create();
                alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda42
                    @Override // android.content.DialogInterface.OnShowListener
                    public final void onShow(DialogInterface dialogInterface) {
                        this.f$0.m39x84ea1c9e(alertDialogCreate, list, dialogInterface);
                    }
                });
                alertDialogCreate.show();
                return;
            }
            chooseMultipleMusicFiles();
        } catch (Exception unused2) {
            Toast.makeText(this, "저장된 음악목록을 열지 못했습니다.", 1).show();
        }
    }

    /* renamed from: lambda$openSavedMusicLibraryChooser$24$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m39x84ea1c9e(final AlertDialog alertDialog, final List list, DialogInterface dialogInterface) {
        alertDialog.getButton(-3).setText("MP3 추가 불러오기");
        alertDialog.getButton(-3).setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda49
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m37x6ba9c69c(alertDialog, view);
            }
        });
        alertDialog.getButton(-1).setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda50
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m38xf849f19d(alertDialog, list, view);
            }
        });
    }

    /* renamed from: lambda$openSavedMusicLibraryChooser$22$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m37x6ba9c69c(AlertDialog alertDialog, View view) {
        alertDialog.dismiss();
        chooseMultipleMusicFiles();
    }

    /* renamed from: lambda$openSavedMusicLibraryChooser$23$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m38xf849f19d(AlertDialog alertDialog, List list, View view) {
        int checkedItemPosition = alertDialog.getListView().getCheckedItemPosition();
        if (checkedItemPosition < 0 || checkedItemPosition >= list.size()) {
            Toast.makeText(this, "재생할 곡을 선택해 주세요.", 0).show();
            return;
        }
        MusicLibraryFiles.Track track = (MusicLibraryFiles.Track) list.get(checkedItemPosition);
        try {
            musicPrefs().edit().putBoolean("music_use_default", false).putBoolean(MUSIC_ENABLED, true).apply();
            Intent intentPutExtra = new Intent(this, (Class<?>) MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_SELECT).putExtra(MusicPlaybackService.EXTRA_TRACK, track.id);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentPutExtra);
            } else {
                startService(intentPutExtra);
            }
            Toast.makeText(this, "재생: " + track.name, 0).show();
            alertDialog.dismiss();
        } catch (Exception unused) {
            Toast.makeText(this, "선택한 음악을 재생하지 못했습니다.", 1).show();
        }
    }

    private void handleSavedMusicIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("v3_force_local_music_folder", false)) {
            intent.removeExtra("v3_force_local_music_folder");
            openSavedMusicLibraryChooser();
        } else {
            if (intent == null || !intent.getBooleanExtra(EXTRA_OPEN_SAVED_MUSIC, false)) {
                return;
            }
            intent.removeExtra(EXTRA_OPEN_SAVED_MUSIC);
            openSavedMusicLibraryChooser();
        }
    }

    private void handleOverlayUpdateIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_OPEN_UPDATE, false)) {
            return;
        }
        intent.removeExtra(EXTRA_OPEN_UPDATE);
        this.overlayUpdateScreenActive = true;
        suspendOverlayForUpdate();
        WebView webView = this.webView;
        if (webView != null) {
            webView.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda37
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m28xd194fa15();
                }
            }, 450L);
        }
    }

    /* renamed from: lambda$handleOverlayUpdateIntent$26$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m28xd194fa15() {
        this.webView.evaluateJavascript("if(window.updatePage){window.updatePage();setTimeout(function(){var m=document.getElementById('modal');if(m){m.classList.add('updateFullScreen');m.style.position='fixed';m.style.left='0';m.style.top='0';m.style.right='0';m.style.bottom='0';m.style.width='100vw';m.style.height='100dvh';m.style.margin='0';m.style.zIndex='2147483000';m.style.display='block';}},30);}else if(window.openM){window.openM('update');}", null);
    }

    private void suspendOverlayForUpdate() {
        try {
            Intent action = new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_SUSPEND);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(action);
            } else {
                startService(action);
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void chooseMusicFile() {
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda27
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m21x2a67aa39();
            }
        });
    }

    /* renamed from: lambda$chooseMusicFile$27$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m21x2a67aa39() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("audio/*");
        intent.addFlags(65);
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/document/primary%3AMusic"));
            } catch (Exception unused) {
            }
        }
        startPreferredFilesPicker(intent, MUSIC_PICKER);
    }

    private void chooseMusicFolder() {
        chooseMultipleMusicFiles();
    }

    private void autoLoadPhoneMusic(boolean z) {
        chooseMultipleMusicFiles();
    }

    private void refreshOurMusicFolder() {
        chooseMultipleMusicFiles();
    }

    private void importMusicFolderAsync(final Uri uri) {
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m29xf210f9fc(uri);
            }
        }, "V3-MusicFolderImport").start();
    }

    /* renamed from: lambda$importMusicFolderAsync$28$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m29xf210f9fc(Uri uri) {
        try {
            musicImportResult(true, saveMusicFolder(uri), "");
        } catch (Exception e) {
            musicImportResult(false, -1, e.getMessage());
        }
    }

    private void musicImportResult(final boolean z, final int i, final String str) {
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda26
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m33x8994bede(z, i, str);
            }
        });
    }

    /* renamed from: lambda$musicImportResult$29$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m33x8994bede(boolean z, int i, String str) throws JSONException {
        StringBuilder sb;
        String str2;
        if (z) {
            sb = new StringBuilder("우리 음악폴더 ");
            sb.append(i);
            str2 = "곡 저장 완료";
        } else {
            sb = new StringBuilder();
            sb.append(String.valueOf(str));
            str2 = " · 기존 목록은 유지됩니다.";
        }
        sb.append(str2);
        Toast.makeText(this, sb.toString(), 1).show();
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("ok", z);
            jSONObject.put("count", i);
            jSONObject.put("error", str);
            WebView webView = this.webView;
            if (webView != null) {
                webView.evaluateJavascript("if(window.onMusicLibraryChanged)onMusicLibraryChanged(" + jSONObject.toString() + ");", null);
            }
        } catch (Exception unused) {
        }
        if (!z || i <= 0) {
            return;
        }
        this.scoreHandler.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda38
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.openSavedMusicLibraryChooser();
            }
        }, 250L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void chooseMultipleMusicFiles() {
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda47
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m20x81e8ea88();
            }
        });
    }

    /* renamed from: lambda$chooseMultipleMusicFiles$30$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m20x81e8ea88() {
        try {
            try {
                Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA_MULTIPLE");
                intent.setPackage("com.sec.android.app.myfiles");
                intent.putExtra("CONTENT_TYPE", "audio/*");
                intent.addFlags(1);
                intent.putExtra("FOLDERPATH", "/storage/emulated/0/Music");
                intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                intent.addCategory("android.intent.category.DEFAULT");
                startActivityForResult(intent, MUSIC_MULTI_PICKER);
            } catch (Exception unused) {
                Intent intent2 = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                intent2.setPackage("com.sec.android.app.myfiles");
                intent2.putExtra("CONTENT_TYPE", "audio/*");
                intent2.addFlags(1);
                intent2.putExtra("FOLDERPATH", "/storage/emulated/0/Music");
                intent2.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                intent2.addCategory("android.intent.category.DEFAULT");
                startActivityForResult(intent2, MUSIC_MULTI_PICKER);
            }
        } catch (Exception unused2) {
            String[] strArr = {"com.google.android.documentsui", "com.android.documentsui"};
            for (int i = 0; i < 2; i++) {
                String str = strArr[i];
                try {
                    Intent intent3 = new Intent("android.intent.action.OPEN_DOCUMENT");
                    intent3.setPackage(str);
                    intent3.addCategory("android.intent.category.OPENABLE");
                    intent3.setType("audio/*");
                    intent3.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                    intent3.addFlags(65);
                    if (Build.VERSION.SDK_INT >= 26) {
                        try {
                            intent3.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/document/primary%3AMusic"));
                        } catch (Exception unused3) {
                        }
                    }
                    startActivityForResult(intent3, MUSIC_MULTI_PICKER);
                    return;
                } catch (Exception unused4) {
                }
            }
            try {
                Intent intent4 = new Intent("android.intent.action.OPEN_DOCUMENT");
                intent4.addCategory("android.intent.category.OPENABLE");
                intent4.setType("audio/*");
                intent4.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                intent4.addFlags(65);
                if (Build.VERSION.SDK_INT >= 26) {
                    intent4.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/document/primary%3AMusic"));
                }
                startActivityForResult(intent4, MUSIC_MULTI_PICKER);
            } catch (Exception unused5) {
                Toast.makeText(this, "음악 선택 화면을 열지 못했습니다. 우리 음악폴더 연결을 사용해 주세요.", 1).show();
            }
        }
    }

    private int saveMultipleMusicFiles(Intent intent) throws Exception {
        ArrayList<Uri> arrayList = new ArrayList<>();
        if (intent.getClipData() != null) {
            for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                Uri uri = intent.getClipData().getItemAt(i).getUri();
                if (uri != null) {
                    arrayList.add(uri);
                }
            }
        }
        if (intent.getData() != null) {
            arrayList.add(intent.getData());
        }
        if (intent.getExtras() != null) {
            String[] strArr = {"SELECTED_FILES", "selectedItems", "selected_files", "filePaths", "uris"};
            for (int i2 = 0; i2 < 5; i2++) {
                Object obj = intent.getExtras().get(strArr[i2]);
                if (obj instanceof Collection) {
                    Iterator it = ((Collection) obj).iterator();
                    while (it.hasNext()) {
                        addMusicResultUri(arrayList, it.next());
                    }
                } else if (obj instanceof Object[]) {
                    for (Object obj2 : (Object[]) obj) {
                        addMusicResultUri(arrayList, obj2);
                    }
                }
            }
        }
        int iAppendUris = new MusicLibraryStore(this).appendUris(arrayList, intent.getFlags());
        musicLibraryChanged(iAppendUris);
        return iAppendUris;
    }

    private void addMusicResultUri(ArrayList<Uri> arrayList, Object obj) {
        Uri uriFromFile;
        if (obj instanceof Uri) {
            arrayList.add((Uri) obj);
            return;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            if (str.startsWith("/")) {
                uriFromFile = Uri.fromFile(new File(str));
            } else if (!str.startsWith("content://") && !str.startsWith("file://")) {
                return;
            } else {
                uriFromFile = Uri.parse(str);
            }
            arrayList.add(uriFromFile);
        }
    }

    private void musicLibraryChanged(int i) {
        musicPrefs().edit().putString(MUSIC_NAME, "우리 음악폴더 · " + i + "곡").apply();
        if (musicEnabled() || MusicPlaybackService.isRunning()) {
            sendMusicCommand(MusicPlaybackService.ACTION_RELOAD);
        }
    }

    private boolean looksLikeAudio(String str, String str2) {
        if (str2 != null && str2.toLowerCase().startsWith("audio/")) {
            return true;
        }
        String lowerCase = str == null ? "" : str.toLowerCase();
        return lowerCase.endsWith(".mp3") || lowerCase.endsWith(".m4a") || lowerCase.endsWith(".aac") || lowerCase.endsWith(".wav") || lowerCase.endsWith(".ogg") || lowerCase.endsWith(".flac");
    }

    private int saveMusicFolder(Uri uri) throws Exception {
        int iAppendFolder = new MusicLibraryStore(this).appendFolder(uri);
        musicLibraryChanged(iAppendFolder);
        return iAppendFolder;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int musicPlaylistCount() {
        try {
            return new MusicLibraryStore(this).read().size();
        } catch (Exception unused) {
            return -1;
        }
    }

    private void saveSelectedMusic(Uri uri, String str) throws Exception {
        ArrayList arrayList = new ArrayList();
        arrayList.add(uri);
        musicLibraryChanged(new MusicLibraryStore(this).appendUris(arrayList, 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearSelectedMusic() {
        sendMusicCommand(MusicPlaybackService.ACTION_DEFAULT);
    }

    @Override // android.app.Activity
    protected void onResume() throws JSONException {
        String string;
        super.onResume();
        if (this.overlayUpdateScreenActive) {
            suspendOverlayForUpdate();
        }
        if (musicEnabled()) {
            startBackgroundMusic();
        }
        handleSavedMusicIntent(getIntent());
        handleOverlayUpdateIntent(getIntent());
        if (isSpecialApprovedLocal() || (string = specialApprovalPrefs().getString("request_token", "")) == null || string.isEmpty()) {
            return;
        }
        checkSpecialApprovalAsync();
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOverlayAudioPermissionRequest(intent);
        handleSavedMusicIntent(intent);
        handleOverlayUpdateIntent(intent);
    }

    private void handleOverlayAudioPermissionRequest(Intent intent) {
        if (intent == null || !intent.getBooleanExtra("v3_request_audio_permission", false)) {
            return;
        }
        intent.removeExtra("v3_request_audio_permission");
        if (hasAudioLibraryPermission()) {
            Toast.makeText(this, "음악 권한이 이미 허용되어 있습니다. 이동아이콘의 MP3를 눌러 재생해 주세요.", 0).show();
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{"android.permission.READ_MEDIA_AUDIO"}, AUTO_AUDIO_PERMISSION);
            } else {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, AUTO_AUDIO_PERMISSION);
            }
        } catch (Exception unused) {
            Toast.makeText(this, "설정에서 음악 및 오디오 권한을 허용해 주세요.", 1).show();
        }
    }

    private void requestAutoAudioPermissionOnce() {
        if (hasAudioLibraryPermission()) {
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences(MUSIC_PREFS, 0);
        if (sharedPreferences.getBoolean("auto_audio_permission_asked", false)) {
            return;
        }
        sharedPreferences.edit().putBoolean("auto_audio_permission_asked", true).apply();
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{"android.permission.READ_MEDIA_AUDIO"}, AUTO_AUDIO_PERMISSION);
            } else {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, AUTO_AUDIO_PERMISSION);
            }
        } catch (Exception unused) {
        }
    }

    private boolean hasAudioLibraryPermission() {
        return Build.VERSION.SDK_INT >= 33 ? checkSelfPermission("android.permission.READ_MEDIA_AUDIO") == 0 : checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0;
    }

    private void requestDeviceMusicLibrary(String str) {
        this.pendingMusicLibraryMode = (str == null || str.trim().isEmpty()) ? "multi" : str.trim();
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda43
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m49x36f93821();
            }
        });
    }

    /* renamed from: lambda$requestDeviceMusicLibrary$31$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m49x36f93821() throws JSONException {
        Toast.makeText(this, "휴대폰 음악목록을 불러오는 중입니다.", 0).show();
        if (hasAudioLibraryPermission()) {
            showNativeMusicMultiPicker();
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{"android.permission.READ_MEDIA_AUDIO"}, AUDIO_LIBRARY_PERMISSION);
            } else {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, AUDIO_LIBRARY_PERMISSION);
            }
        } catch (Exception unused) {
            deliverDeviceMusicLibraryError("음악 및 오디오 권한을 요청하지 못했습니다.");
        }
    }

    private void showNativeMusicMultiPicker() {
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda54
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m68xccfd2e51();
            }
        }, "V3-NativeMusicPicker").start();
    }

    /* renamed from: lambda$showNativeMusicMultiPicker$36$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m68xccfd2e51() throws JSONException {
        String string;
        try {
            final ArrayList arrayList = new ArrayList();
            final ArrayList arrayList2 = new ArrayList();
            Cursor cursorQuery = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_display_name", "artist", "duration"}, Build.VERSION.SDK_INT >= 29 ? "is_music!=0" : null, null, "_display_name COLLATE NOCASE ASC");
            if (cursorQuery != null) {
                try {
                    int columnIndex = cursorQuery.getColumnIndex("_id");
                    int columnIndex2 = cursorQuery.getColumnIndex("_display_name");
                    int columnIndex3 = cursorQuery.getColumnIndex("artist");
                    while (cursorQuery.moveToNext()) {
                        long j = columnIndex >= 0 ? cursorQuery.getLong(columnIndex) : -1L;
                        if (j >= 0) {
                            if (columnIndex2 >= 0) {
                                string = cursorQuery.getString(columnIndex2);
                            } else {
                                string = "track_" + j;
                            }
                            String string2 = columnIndex3 >= 0 ? cursorQuery.getString(columnIndex3) : "";
                            if (string == null || string.trim().isEmpty()) {
                                string = "음악 " + (arrayList.size() + 1);
                            }
                            arrayList.add(Long.valueOf(j));
                            if (string2 == null || string2.trim().isEmpty() || "<unknown>".equalsIgnoreCase(string2.trim())) {
                                arrayList2.add(string);
                            } else {
                                arrayList2.add(string + "  ·  " + string2);
                            }
                        }
                    }
                } finally {
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda34
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m67x405d0350(arrayList, arrayList2);
                }
            });
        } catch (Exception unused) {
            deliverDeviceMusicLibraryError("휴대폰 음악목록을 읽지 못했습니다.");
        }
    }

    /* renamed from: lambda$showNativeMusicMultiPicker$35$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m67x405d0350(final ArrayList arrayList, ArrayList arrayList2) {
        if (arrayList.isEmpty()) {
            Toast.makeText(this, "휴대폰에서 재생 가능한 음악을 찾지 못했습니다.", 1).show();
            return;
        }
        final boolean[] zArr = new boolean[arrayList.size()];
        final AlertDialog alertDialogCreate = new AlertDialog.Builder(this).setTitle("연속 재생 음악 선택").setMultiChoiceItems((CharSequence[]) arrayList2.toArray(new CharSequence[0]), zArr, new DialogInterface.OnMultiChoiceClickListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda28
            @Override // android.content.DialogInterface.OnMultiChoiceClickListener
            public final void onClick(DialogInterface dialogInterface, int i, boolean z) {
                MainActivity.lambda$showNativeMusicMultiPicker$32(zArr, dialogInterface, i, z);
            }
        }).setNegativeButton("취소", (DialogInterface.OnClickListener) null).setPositiveButton("선택 음악 재생", (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda29
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                this.f$0.m66xb3bcd84f(alertDialogCreate, zArr, arrayList, dialogInterface);
            }
        });
        alertDialogCreate.show();
    }

    static /* synthetic */ void lambda$showNativeMusicMultiPicker$32(boolean[] zArr, DialogInterface dialogInterface, int i, boolean z) {
        zArr[i] = z;
    }

    /* renamed from: lambda$showNativeMusicMultiPicker$34$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m66xb3bcd84f(final AlertDialog alertDialog, final boolean[] zArr, final ArrayList arrayList, DialogInterface dialogInterface) {
        alertDialog.getButton(-1).setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda14
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m65x271cad4e(zArr, arrayList, alertDialog, view);
            }
        });
    }

    /* renamed from: lambda$showNativeMusicMultiPicker$33$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m65x271cad4e(boolean[] zArr, ArrayList arrayList, AlertDialog alertDialog, View view) {
        JSONArray jSONArray = new JSONArray();
        for (int i = 0; i < zArr.length; i++) {
            if (zArr[i]) {
                jSONArray.put(arrayList.get(i));
            }
        }
        if (jSONArray.length() == 0) {
            Toast.makeText(this, "음악을 한 곡 이상 선택해 주세요.", 0).show();
            return;
        }
        alertDialog.dismiss();
        Toast.makeText(this, "선택한 음악을 재생목록에 저장합니다.", 0).show();
        saveDeviceMusicSelection(jSONArray.toString());
    }

    private JSONArray readDeviceMusicLibrary() throws Exception {
        String string;
        JSONArray jSONArray = new JSONArray();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ArrayList arrayList = new ArrayList();
        arrayList.add("_id");
        arrayList.add("_display_name");
        arrayList.add("artist");
        arrayList.add("duration");
        if (Build.VERSION.SDK_INT >= 29) {
            arrayList.add("relative_path");
        }
        Cursor cursorQuery = getContentResolver().query(uri, (String[]) arrayList.toArray(new String[0]), null, null, "_display_name COLLATE NOCASE ASC");
        if (cursorQuery == null) {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            return jSONArray;
        }
        try {
            int columnIndex = cursorQuery.getColumnIndex("_id");
            int columnIndex2 = cursorQuery.getColumnIndex("_display_name");
            int columnIndex3 = cursorQuery.getColumnIndex("artist");
            int columnIndex4 = cursorQuery.getColumnIndex("duration");
            int columnIndex5 = Build.VERSION.SDK_INT >= 29 ? cursorQuery.getColumnIndex("relative_path") : -1;
            while (cursorQuery.moveToNext()) {
                long j = columnIndex >= 0 ? cursorQuery.getLong(columnIndex) : -1L;
                if (j >= 0) {
                    if (columnIndex2 >= 0) {
                        string = cursorQuery.getString(columnIndex2);
                    } else {
                        string = "track_" + j;
                    }
                    String str = "";
                    String string2 = columnIndex3 >= 0 ? cursorQuery.getString(columnIndex3) : "";
                    long j2 = columnIndex4 >= 0 ? cursorQuery.getLong(columnIndex4) : 0L;
                    String string3 = columnIndex5 >= 0 ? cursorQuery.getString(columnIndex5) : "Music/";
                    if (string == null || string.trim().isEmpty()) {
                        string = "track_" + j;
                    }
                    int i = columnIndex;
                    if (looksLikeAudio(string, "audio/*")) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("id", j);
                        jSONObject.put(RadioPlaybackService.EXTRA_NAME, string);
                        if (string2 != null) {
                            str = string2;
                        }
                        jSONObject.put("artist", str);
                        jSONObject.put("duration", j2);
                        jSONObject.put("path", (string3 == null || string3.trim().isEmpty()) ? "Music/" : string3);
                        jSONArray.put(jSONObject);
                    }
                    columnIndex = i;
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            return jSONArray;
        } finally {
        }
    }

    private void deliverDeviceMusicLibrary(final String str) {
        if (str == null || str.trim().isEmpty()) {
            str = "multi";
        }
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m24xb38a7652(str);
            }
        }, "V3-MusicLibrary").start();
    }

    /* renamed from: lambda$deliverDeviceMusicLibrary$38$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m24xb38a7652(String str) throws JSONException {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("ok", true);
            jSONObject.put("mode", str);
            jSONObject.put("items", readDeviceMusicLibrary());
            final String string = jSONObject.toString();
            this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda57
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m23x26ea4b51(string);
                }
            });
        } catch (Exception unused) {
            deliverDeviceMusicLibraryError("휴대폰 음악 목록을 읽지 못했습니다.");
        }
    }

    /* renamed from: lambda$deliverDeviceMusicLibrary$37$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m23x26ea4b51(String str) {
        this.webView.evaluateJavascript("onDeviceMusicLibrary(" + str + ");", null);
    }

    private void deliverDeviceMusicLibraryError(String str) throws JSONException {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("ok", false);
            jSONObject.put("mode", this.pendingMusicLibraryMode);
            if (str == null) {
                str = "음악 목록 오류";
            }
            jSONObject.put("error", str);
            final String string = jSONObject.toString();
            this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda16
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m25x35d0e653(string);
                }
            });
        } catch (Exception unused) {
        }
    }

    /* renamed from: lambda$deliverDeviceMusicLibraryError$39$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m25x35d0e653(String str) {
        this.webView.evaluateJavascript("onDeviceMusicLibrary(" + str + ");", null);
    }

    private int copyMediaIdsToPlaylist(JSONArray jSONArray) throws Exception {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            long jOptLong = jSONArray.optLong(i, -1L);
            if (jOptLong >= 0) {
                arrayList.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, jOptLong));
            }
        }
        int iAppendUris = new MusicLibraryStore(this).appendUris(arrayList, 1);
        musicLibraryChanged(iAppendUris);
        return iAppendUris;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveDeviceMusicSelection(final String str) {
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda40
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m60xf6a00508(str);
            }
        }, "V3-MusicSave").start();
    }

    /* renamed from: lambda$saveDeviceMusicSelection$42$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m60xf6a00508(String str) throws JSONException {
        try {
            if (str == null) {
                str = "[]";
            }
            int iCopyMediaIdsToPlaylist = copyMediaIdsToPlaylist(new JSONArray(str));
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("ok", true);
            jSONObject.put("count", iCopyMediaIdsToPlaylist);
            final String string = jSONObject.toString();
            this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda30
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m58xdd5faf06(string);
                }
            });
        } catch (Exception e) {
            try {
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("ok", false);
                jSONObject2.put("error", e.getMessage() == null ? "음악 저장 실패" : e.getMessage());
                final String string2 = jSONObject2.toString();
                this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda31
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m59x69ffda07(string2);
                    }
                });
            } catch (Exception unused) {
            }
        }
    }

    /* renamed from: lambda$saveDeviceMusicSelection$40$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m58xdd5faf06(String str) {
        this.webView.evaluateJavascript("onDeviceMusicPlaylistSaved(" + str + ");", null);
    }

    /* renamed from: lambda$saveDeviceMusicSelection$41$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m59x69ffda07(String str) {
        this.webView.evaluateJavascript("onDeviceMusicPlaylistSaved(" + str + ");", null);
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) throws JSONException {
        String str;
        super.onRequestPermissionsResult(i, strArr, iArr);
        boolean z = false;
        if (i != AUTO_AUDIO_PERMISSION) {
            if (i == AUDIO_LIBRARY_PERMISSION) {
                if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
                    showNativeMusicMultiPicker();
                    return;
                } else {
                    deliverDeviceMusicLibraryError("음악 및 오디오 권한이 필요합니다. 설정에서 권한을 허용해 주세요.");
                    return;
                }
            }
            return;
        }
        if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
            z = true;
        }
        if (z) {
            str = "음악 및 오디오 권한이 허용되었습니다. 필요한 MP3를 직접 선택해 주세요.";
        } else {
            str = "MP3 파일 접근 권한을 허용해 주세요.";
        }
        Toast.makeText(this, str, 1).show();
        if (z) {
            chooseMultipleMusicFiles();
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
        if (this.overlayUpdateScreenActive) {
            suspendOverlayForUpdate();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() throws IllegalStateException {
        stopScoreFanfare();
        try {
            TextToSpeech textToSpeech = this.scoreTts;
            if (textToSpeech != null) {
                textToSpeech.stop();
                this.scoreTts.shutdown();
                this.scoreTts = null;
            }
        } catch (Exception unused) {
        }
        try {
            MediaPlayer mediaPlayer = this.launchSeaPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.release();
                this.launchSeaPlayer = null;
            }
        } catch (Exception unused2) {
        }
        try {
            releaseNativeVoyageMedia();
            TextureView textureView = this.launchVideo;
            if (textureView != null) {
                FrameLayout frameLayout = this.rootLayout;
                if (frameLayout != null) {
                    frameLayout.removeView(textureView);
                }
                this.launchVideo = null;
            }
        } catch (Exception unused3) {
        }
        try {
            TextToSpeech textToSpeech2 = this.launchTts;
            if (textToSpeech2 != null) {
                textToSpeech2.stop();
                this.launchTts.shutdown();
                this.launchTts = null;
            }
        } catch (Exception unused4) {
        }
        try {
            stopRadioDirectInternal();
        } catch (Exception unused5) {
        }
        super.onDestroy();
    }

    private void initScoreTts() {
        try {
            this.scoreTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda61
                @Override // android.speech.tts.TextToSpeech.OnInitListener
                public final void onInit(int i) {
                    this.f$0.m31lambda$initScoreTts$43$combaekhakcentralcontrolMainActivity(i);
                }
            });
        } catch (Exception unused) {
            this.scoreTtsReady = false;
        }
    }

    /* renamed from: lambda$initScoreTts$43$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m31lambda$initScoreTts$43$combaekhakcentralcontrolMainActivity(int i) {
        String str;
        if (i == 0) {
            try {
                int language = this.scoreTts.setLanguage(Locale.KOREAN);
                this.scoreTtsReady = (language == -1 || language == -2) ? false : true;
                this.scoreTts.setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(1).build());
                if (!this.scoreTtsReady || (str = this.pendingScoreSpeech) == null || str.trim().isEmpty()) {
                    return;
                }
                String str2 = this.pendingScoreSpeech;
                int i2 = this.pendingScoreRank;
                this.pendingScoreSpeech = null;
                this.pendingScoreRank = 0;
                m41xf0b0daf1(str2, i2);
            } catch (Exception unused) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopScoreFanfare() throws IllegalStateException {
        try {
            MediaPlayer mediaPlayer = this.scoreFanfarePlayer;
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                } catch (Exception unused) {
                }
                try {
                    this.scoreFanfarePlayer.release();
                } catch (Exception unused2) {
                }
                this.scoreFanfarePlayer = null;
            }
        } catch (Exception unused3) {
        }
    }

    private void playScoreFanfare() {
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda33
            @Override // java.lang.Runnable
            public final void run() throws IllegalStateException {
                this.f$0.m46xd095a342();
            }
        });
    }

    /* renamed from: lambda$playScoreFanfare$45$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m46xd095a342() throws IllegalStateException {
        try {
            stopScoreFanfare();
            MediaPlayer mediaPlayerCreate = MediaPlayer.create(this, R.raw.v3_congrats_fanfare);
            this.scoreFanfarePlayer = mediaPlayerCreate;
            if (mediaPlayerCreate != null) {
                mediaPlayerCreate.setVolume(0.82f, 0.82f);
                this.scoreFanfarePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda15
                    @Override // android.media.MediaPlayer.OnCompletionListener
                    public final void onCompletion(MediaPlayer mediaPlayer) {
                        this.f$0.m45x43f57841(mediaPlayer);
                    }
                });
                this.scoreFanfarePlayer.start();
            }
        } catch (Exception unused) {
        }
    }

    /* renamed from: lambda$playScoreFanfare$44$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m45x43f57841(MediaPlayer mediaPlayer) {
        try {
            mediaPlayer.release();
        } catch (Exception unused) {
        }
        if (this.scoreFanfarePlayer == mediaPlayer) {
            this.scoreFanfarePlayer = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: speakScoreNow, reason: merged with bridge method [inline-methods] */
    public void m41xf0b0daf1(final String str, final int i) {
        if (str == null || str.trim().isEmpty()) {
            return;
        }
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda48
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m70lambda$speakScoreNow$46$combaekhakcentralcontrolMainActivity(str, i);
            }
        });
    }

    /* renamed from: lambda$speakScoreNow$46$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m70lambda$speakScoreNow$46$combaekhakcentralcontrolMainActivity(String str, int i) {
        try {
            TextToSpeech textToSpeech = this.scoreTts;
            if (textToSpeech == null || !this.scoreTtsReady) {
                this.pendingScoreSpeech = str;
                this.pendingScoreRank = i;
            } else {
                textToSpeech.setSpeechRate((i <= 0 || i > 2) ? 0.96f : 0.9f);
                this.scoreTts.setPitch(i > 0 ? 1.03f : 1.0f);
                this.scoreTts.speak(str, 0, null, "v3_score_voice");
            }
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void playCelebrationWithVoice(final String str, final int i) {
        playScoreFanfare();
        this.scoreHandler.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m41xf0b0daf1(str, i);
            }
        }, 650L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void speakConsolationVoice(final String str) throws IllegalStateException {
        stopScoreFanfare();
        this.scoreHandler.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda36
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m69x9dc34e45(str);
            }
        }, 180L);
    }

    /* renamed from: lambda$speakConsolationVoice$48$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m69x9dc34e45(String str) {
        m41xf0b0daf1(str, 0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDownloadInitialUri(Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload"));
            } catch (Exception unused) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void chooseApkUpdateFile() {
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda41
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m19x57d10899();
            }
        });
    }

    /* renamed from: lambda$chooseApkUpdateFile$49$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m19x57d10899() {
        try {
            try {
                Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                intent.setPackage("com.sec.android.app.myfiles");
                intent.putExtra("CONTENT_TYPE", "*/*");
                intent.putExtra("FOLDERPATH", "/storage/emulated/0/Download");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(1);
                startActivityForResult(intent, APK_UPDATE_PICKER);
                Toast.makeText(this, "내 파일에서 업데이트 APK를 선택해 주세요.", 0).show();
            } catch (Exception unused) {
                Toast.makeText(this, "Samsung 내 파일 앱을 열지 못했습니다.", 1).show();
            }
        } catch (Exception unused2) {
            Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.myfiles");
            if (launchIntentForPackage != null) {
                launchIntentForPackage.addFlags(268435456);
                startActivity(launchIntentForPackage);
                Toast.makeText(this, "내 파일의 Download 폴더에서 업데이트 APK를 눌러 설치해 주세요.", 1).show();
                return;
            }
            Toast.makeText(this, "Samsung 내 파일 앱을 열지 못했습니다.", 1).show();
        }
    }

    private void launchApkInstaller(Uri uri) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(268435457);
            startActivity(intent);
        } catch (Exception unused) {
            Toast.makeText(this, "APK 설치 화면을 열지 못했습니다.", 1).show();
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, final Intent intent) throws IOException {
        super.onActivityResult(i, i2, intent);
        if (i == APK_UPDATE_PICKER) {
            if (i2 != -1 || intent == null || intent.getData() == null) {
                return;
            }
            Uri data = intent.getData();
            String strDisplayName = displayName(data);
            String type = getContentResolver().getType(data);
            if ((strDisplayName == null || !strDisplayName.toLowerCase().endsWith(".apk")) && !"application/vnd.android.package-archive".equals(type) && !"application/octet-stream".equals(type)) {
                Toast.makeText(this, "APK 파일을 선택해 주세요.", 1).show();
                return;
            } else {
                Toast.makeText(this, "업데이트 APK 선택 완료", 0).show();
                launchApkInstaller(data);
                return;
            }
        }
        if (i == UPDATE_PICKER) {
            if (i2 != -1 || intent == null || intent.getData() == null) {
                return;
            }
            try {
                alert(installUpdate(readUri(intent.getData())) ? "V3 업데이트 적용 완료" : "업데이트 파일이 올바르지 않습니다.");
                return;
            } catch (Exception unused) {
                alert("업데이트 적용 실패");
                return;
            }
        }
        if (i == TEXT_PICKER) {
            if (i2 != -1 || intent == null || intent.getData() == null) {
                return;
            }
            try {
                Uri data2 = intent.getData();
                final String str = "receiveText(" + JSONObject.quote(displayName(data2)) + "," + JSONObject.quote(readUri(data2)) + ");";
                this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda51
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m34xdeb744b4(str);
                    }
                });
                return;
            } catch (Exception unused2) {
                alert("원문 TXT 불러오기 실패");
                return;
            }
        }
        if (i == LEGACY_BACKUP_PICKER) {
            if (i2 != -1 || intent == null || intent.getData() == null) {
                return;
            }
            try {
                Uri data3 = intent.getData();
                sendLegacyBackupToJs(displayName(data3), readUri(data3));
                return;
            } catch (Exception unused3) {
                alert("백업 JSON을 읽지 못했습니다.");
                return;
            }
        }
        if (i == MUSIC_PICKER || i == MUSIC_MULTI_PICKER) {
            if (i2 != -1 || intent == null) {
                return;
            }
            new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda52
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m35x6b576fb5(intent);
                }
            }, "V3-MusicImport").start();
            return;
        }
        if (i == MUSIC_FOLDER_PICKER) {
            if (i2 != -1 || intent == null || intent.getData() == null) {
                return;
            }
            Uri data4 = intent.getData();
            try {
                getContentResolver().takePersistableUriPermission(data4, (intent.getFlags() & 3) | 1);
            } catch (Exception unused4) {
            }
            musicPrefs().edit().putString("music_library_folder_uri", data4.toString()).putString(MUSIC_TREE_URI, data4.toString()).apply();
            importMusicFolderAsync(data4);
            return;
        }
        if (i == SAVE_PICKER) {
            if (i2 == -1 && intent != null && intent.getData() != null && this.pendingData != null) {
                try {
                    OutputStream outputStreamOpenOutputStream = getContentResolver().openOutputStream(intent.getData());
                    try {
                        if (outputStreamOpenOutputStream == null) {
                            throw new Exception("save failed");
                        }
                        outputStreamOpenOutputStream.write(this.pendingData.getBytes(StandardCharsets.UTF_8));
                        outputStreamOpenOutputStream.flush();
                        alert("파일 저장 완료");
                        if (outputStreamOpenOutputStream != null) {
                            outputStreamOpenOutputStream.close();
                        }
                    } finally {
                    }
                } catch (Exception unused5) {
                    alert("파일 저장 실패");
                }
            }
            this.pendingData = null;
            this.pendingName = null;
            this.pendingMime = null;
        }
    }

    /* renamed from: lambda$onActivityResult$50$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m34xdeb744b4(String str) {
        this.webView.evaluateJavascript(str, null);
    }

    /* renamed from: lambda$onActivityResult$51$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m35x6b576fb5(Intent intent) {
        try {
            musicImportResult(true, saveMultipleMusicFiles(intent), "");
        } catch (Exception e) {
            musicImportResult(false, -1, e.getMessage());
        }
    }

    private String httpGet(String str) throws Exception {
        Throwable th;
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            try {
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(12000);
                httpURLConnection.setReadTimeout(12000);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty("Accept", "application/json, text/plain, */*");
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36");
                httpURLConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                httpURLConnection.setRequestProperty("Referer", "https://www.dhlottery.co.kr/lt645/result");
                int responseCode = httpURLConnection.getResponseCode();
                InputStream errorStream = (responseCode < 200 || responseCode >= 300) ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
                if (errorStream == null) {
                    throw new Exception("HTTP " + responseCode);
                }
                String str2 = new String(readAll(errorStream), StandardCharsets.UTF_8);
                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception("HTTP " + responseCode);
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                return str2;
            } catch (Throwable th2) {
                th = th2;
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            httpURLConnection = null;
        }
    }

    private JSONObject parseWinningNewApi(int i, String str) throws Exception {
        JSONObject jSONObjectOptJSONObject = new JSONObject(str).optJSONObject("data");
        JSONObject jSONObjectOptJSONObject2 = null;
        JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject == null ? null : jSONObjectOptJSONObject.optJSONArray("list");
        if (jSONArrayOptJSONArray == null || jSONArrayOptJSONArray.length() == 0) {
            throw new Exception("당첨결과 없음");
        }
        int i2 = 0;
        while (true) {
            if (i2 >= jSONArrayOptJSONArray.length()) {
                break;
            }
            JSONObject jSONObjectOptJSONObject3 = jSONArrayOptJSONArray.optJSONObject(i2);
            if (jSONObjectOptJSONObject3 != null && jSONObjectOptJSONObject3.optInt("ltEpsd", -1) == i) {
                jSONObjectOptJSONObject2 = jSONObjectOptJSONObject3;
                break;
            }
            i2++;
        }
        if (jSONObjectOptJSONObject2 == null) {
            jSONObjectOptJSONObject2 = jSONArrayOptJSONArray.optJSONObject(0);
        }
        if (jSONObjectOptJSONObject2 == null || jSONObjectOptJSONObject2.optInt("ltEpsd", -1) != i) {
            throw new Exception("회차 결과 없음");
        }
        JSONArray jSONArray = new JSONArray();
        for (int i3 = 1; i3 <= 6; i3++) {
            int iOptInt = jSONObjectOptJSONObject2.optInt("tm" + i3 + "WnNo", 0);
            if (iOptInt < 1 || iOptInt > 45) {
                throw new Exception("당첨번호 형식 오류");
            }
            jSONArray.put(iOptInt);
        }
        int iOptInt2 = jSONObjectOptJSONObject2.optInt("bnsWnNo", 0);
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("ok", true);
        jSONObject.put("round", i);
        jSONObject.put("numbers", jSONArray);
        jSONObject.put("bonus", iOptInt2);
        jSONObject.put("date", jSONObjectOptJSONObject2.optString("ltRflYmd", ""));
        jSONObject.put("source", "동행복권");
        return jSONObject;
    }

    private JSONObject parseWinningLegacyApi(int i, String str) throws Exception {
        JSONObject jSONObject = new JSONObject(str);
        if (!"success".equalsIgnoreCase(jSONObject.optString("returnValue", ""))) {
            throw new Exception("결과 없음");
        }
        JSONArray jSONArray = new JSONArray();
        for (int i2 = 1; i2 <= 6; i2++) {
            int iOptInt = jSONObject.optInt("drwtNo" + i2, 0);
            if (iOptInt < 1 || iOptInt > 45) {
                throw new Exception("당첨번호 형식 오류");
            }
            jSONArray.put(iOptInt);
        }
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("ok", true);
        jSONObject2.put("round", i);
        jSONObject2.put("numbers", jSONArray);
        jSONObject2.put("bonus", jSONObject.optInt("bnusNo", 0));
        jSONObject2.put("date", jSONObject.optString("drwNoDate", ""));
        jSONObject2.put("source", "동행복권");
        return jSONObject2;
    }

    private void deliverWinningResult(JSONObject jSONObject) {
        final String string = jSONObject == null ? "{}" : jSONObject.toString();
        this.webView.post(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda35
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m26x4608e739(string);
            }
        });
    }

    /* renamed from: lambda$deliverWinningResult$52$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m26x4608e739(String str) {
        this.webView.evaluateJavascript("(function(d){try{if(window.__v3WinningResult)window.__v3WinningResult(d);}catch(e){}try{var f=document.getElementById('engineFrame');if(f&&f.contentWindow&&f.contentWindow.__baekhakWinningResult)f.contentWindow.__baekhakWinningResult(d);}catch(e){}})(" + str + ");", null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fetchWinningNumbersAsync(final int i) throws JSONException {
        if (i < 1 || i > 9999) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("ok", false);
                jSONObject.put("round", i);
                jSONObject.put("error", "회차 번호가 올바르지 않습니다.");
                deliverWinningResult(jSONObject);
                return;
            } catch (Exception unused) {
                return;
            }
        }
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda58
            @Override // java.lang.Runnable
            public final void run() throws JSONException {
                this.f$0.m27x81b0327a(i);
            }
        }, "V3-WinningLookup").start();
    }

    /* renamed from: lambda$fetchWinningNumbersAsync$53$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m27x81b0327a(int i) throws JSONException {
        String message;
        JSONObject winningLegacyApi;
        message = "당첨번호를 조회하지 못했습니다.";
        try {
            winningLegacyApi = parseWinningNewApi(i, httpGet("https://www.dhlottery.co.kr/lt645/selectPstLt645Info.do?srchLtEpsd=" + i + "&_=" + System.currentTimeMillis()));
        } catch (Exception e) {
            message = e.getMessage() != null ? e.getMessage() : "당첨번호를 조회하지 못했습니다.";
            try {
                winningLegacyApi = parseWinningLegacyApi(i, httpGet("https://www.dhlottery.co.kr/common.do?method=getLottoNumber&drwNo=" + i));
            } catch (Exception e2) {
                if (e2.getMessage() != null) {
                    message = e2.getMessage();
                }
                winningLegacyApi = null;
            }
        }
        if (winningLegacyApi == null) {
            try {
                JSONObject jSONObject = new JSONObject();
                try {
                    jSONObject.put("ok", false);
                    jSONObject.put("round", i);
                    jSONObject.put("error", "제" + i + "회 당첨번호가 아직 발표되지 않았거나 네트워크 조회에 실패했습니다. (" + message + ")");
                } catch (Exception unused) {
                }
                winningLegacyApi = jSONObject;
            } catch (Exception unused2) {
            }
        }
        deliverWinningResult(winningLegacyApi);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String handleMusicAction(String str) {
        String lowerCase = str == null ? "" : str.trim().toLowerCase(Locale.ROOT);
        try {
            if ("folder".equals(lowerCase)) {
                chooseMusicFolder();
                return "필요한 MP3를 직접 선택합니다.";
            }
            if ("refreshfolder".equals(lowerCase)) {
                refreshOurMusicFolder();
                return "필요한 MP3를 추가로 직접 선택합니다.";
            }
            if ("library".equals(lowerCase)) {
                sendMusicCommand(MusicPlaybackService.ACTION_LIBRARY);
                return "저장된 음악을 이어 재생합니다.";
            }
            if ("multi".equals(lowerCase)) {
                chooseMultipleMusicFiles();
                return "내 파일에서 연속곡을 선택합니다.";
            }
            if ("single".equals(lowerCase)) {
                chooseMusicFile();
                return "한 곡 선택 화면을 엽니다.";
            }
            if ("prev".equals(lowerCase)) {
                sendMusicCommand(MusicPlaybackService.ACTION_PREV);
                return "이전 곡으로 이동했습니다.";
            }
            if ("play".equals(lowerCase)) {
                musicPrefs().edit().putBoolean(MUSIC_ENABLED, true).apply();
                sendMusicCommand(MusicPlaybackService.ACTION_PLAY);
                return "선택한 음악 재생을 시작했습니다.";
            }
            if ("pause".equals(lowerCase)) {
                musicPrefs().edit().putBoolean(MUSIC_ENABLED, false).apply();
                sendMusicCommand(MusicPlaybackService.ACTION_PAUSE);
                return "일시정지했습니다.";
            }
            if ("next".equals(lowerCase)) {
                sendMusicCommand(MusicPlaybackService.ACTION_NEXT);
                return "다음 곡으로 이동했습니다.";
            }
            if ("toggle".equals(lowerCase)) {
                return toggleBackgroundMusic() ? "재생을 시작했습니다." : "일시정지했습니다.";
            }
            if ("stop".equals(lowerCase)) {
                stopBackgroundMusic();
                return "음악을 종료했습니다.";
            }
            if (!"reset".equals(lowerCase)) {
                return "알 수 없는 음악 명령입니다.";
            }
            clearSelectedMusic();
            return "기본 음악으로 전환했습니다. 우리 음악폴더의 곡은 보존됩니다.";
        } catch (Exception unused) {
            return "음악 기능 처리 중 오류가 발생했습니다.";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopRadioDirectInternal() throws IllegalStateException {
        this.radioPlaying = false;
        this.radioName = "";
        this.radioUrl = "";
        try {
            MediaPlayer mediaPlayer = this.radioPlayer;
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.setOnPreparedListener(null);
                } catch (Exception unused) {
                }
                try {
                    this.radioPlayer.setOnErrorListener(null);
                } catch (Exception unused2) {
                }
                try {
                    this.radioPlayer.stop();
                } catch (Exception unused3) {
                }
                try {
                    this.radioPlayer.reset();
                } catch (Exception unused4) {
                }
                this.radioPlayer.release();
            }
        } catch (Exception unused5) {
        }
        this.radioPlayer = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopRadioDirect() {
        runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda25
            @Override // java.lang.Runnable
            public final void run() throws IllegalStateException {
                this.f$0.stopRadioDirectInternal();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void playRadioDirect(String str, String str2) {
        final String strTrim = str == null ? "" : str.trim();
        final String strTrim2 = str2 == null ? "FM 라디오" : str2.trim();
        if (strTrim.startsWith("https://") || strTrim.startsWith("http://")) {
            runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda17
                @Override // java.lang.Runnable
                public final void run() throws IllegalStateException, IOException, SecurityException, IllegalArgumentException {
                    this.f$0.m44x4ca83fcf(strTrim, strTrim2);
                }
            });
        }
    }

    /* renamed from: lambda$playRadioDirect$56$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m44x4ca83fcf(String str, String str2) throws IllegalStateException, IOException, SecurityException, IllegalArgumentException {
        stopRadioDirectInternal();
        try {
            stopBackgroundMusic();
        } catch (Exception unused) {
        }
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            this.radioPlayer = mediaPlayer;
            this.radioUrl = str;
            if (str2.isEmpty()) {
                str2 = "FM 라디오";
            }
            this.radioName = str2;
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(2).build());
            mediaPlayer.setDataSource(str);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda12
                @Override // android.media.MediaPlayer.OnPreparedListener
                public final void onPrepared(MediaPlayer mediaPlayer2) throws IllegalStateException {
                    this.f$0.m42x3367e9cd(mediaPlayer2);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() { // from class: com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticLambda13
                @Override // android.media.MediaPlayer.OnErrorListener
                public final boolean onError(MediaPlayer mediaPlayer2, int i, int i2) {
                    return this.f$0.m43xc00814ce(mediaPlayer2, i, i2);
                }
            });
            mediaPlayer.prepareAsync();
        } catch (Exception unused2) {
            stopRadioDirectInternal();
        }
    }

    /* renamed from: lambda$playRadioDirect$54$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ void m42x3367e9cd(MediaPlayer mediaPlayer) throws IllegalStateException {
        if (this.radioPlayer != mediaPlayer) {
            return;
        }
        try {
            mediaPlayer.start();
            this.radioPlaying = true;
        } catch (Exception unused) {
            stopRadioDirectInternal();
        }
    }

    /* renamed from: lambda$playRadioDirect$55$com-baekhak-centralcontrol-MainActivity, reason: not valid java name */
    /* synthetic */ boolean m43xc00814ce(MediaPlayer mediaPlayer, int i, int i2) throws IllegalStateException {
        if (this.radioPlayer != mediaPlayer) {
            return true;
        }
        stopRadioDirectInternal();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:10:0x0015  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.String radioStateJson() throws org.json.JSONException {
        /*
            r4 = this;
            org.json.JSONObject r0 = new org.json.JSONObject
            r0.<init>()
            boolean r1 = r4.radioPlaying     // Catch: java.lang.Exception -> L32
            if (r1 == 0) goto L15
            android.media.MediaPlayer r2 = r4.radioPlayer     // Catch: java.lang.Exception -> L16
            if (r2 == 0) goto L15
            boolean r1 = r2.isPlaying()     // Catch: java.lang.Exception -> L16
            if (r1 == 0) goto L15
            r1 = 1
            goto L16
        L15:
            r1 = 0
        L16:
            java.lang.String r2 = "playing"
            r0.put(r2, r1)     // Catch: java.lang.Exception -> L32
            java.lang.String r1 = "name"
            java.lang.String r2 = r4.radioName     // Catch: java.lang.Exception -> L32
            java.lang.String r3 = ""
            if (r2 != 0) goto L24
            r2 = r3
        L24:
            r0.put(r1, r2)     // Catch: java.lang.Exception -> L32
            java.lang.String r1 = "url"
            java.lang.String r2 = r4.radioUrl     // Catch: java.lang.Exception -> L32
            if (r2 != 0) goto L2e
            goto L2f
        L2e:
            r3 = r2
        L2f:
            r0.put(r1, r3)     // Catch: java.lang.Exception -> L32
        L32:
            java.lang.String r0 = r0.toString()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.baekhak.centralcontrol.MainActivity.radioStateJson():java.lang.String");
    }

    public final class AndroidHostBridge {
        public AndroidHostBridge() {
        }

        /* renamed from: lambda$launchIntroDone$0$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m82xaa5f7f63() throws IllegalStateException {
            MainActivity.this.finishLaunchIntroNative();
        }

        @JavascriptInterface
        public void launchIntroDone() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda10
                @Override // java.lang.Runnable
                public final void run() throws IllegalStateException {
                    this.f$0.m82xaa5f7f63();
                }
            });
        }

        @JavascriptInterface
        public void requestUpdateStats() {
            MainActivity.this.requestUpdateStatsAsync();
        }

        @JavascriptInterface
        public void requestNamedUpdateStats() {
            MainActivity.this.requestNamedUpdateStatsAsync();
        }

        @JavascriptInterface
        public String getUpdateDisplayName() {
            return MainActivity.this.getStoredUpdateDisplayName();
        }

        @JavascriptInterface
        public void setUpdateDisplayNameAndReport(String str) throws PackageManager.NameNotFoundException {
            MainActivity.this.setUpdateDisplayNameAndReportAsync(str);
        }

        @JavascriptInterface
        public boolean isSpecialApproved() {
            return MainActivity.this.isSpecialApprovedLocal();
        }

        @JavascriptInterface
        public String getSpecialApprovalSummary() {
            return MainActivity.this.specialApprovalSummaryJson();
        }

        @JavascriptInterface
        public void requestSpecialApproval(int i, String str, String str2) {
            MainActivity.this.requestSpecialApprovalAsync(i, str, str2);
        }

        @JavascriptInterface
        public String getApprovalApplicantName() {
            return MainActivity.this.specialApprovalPrefs().getString("applicant_name", "");
        }

        @JavascriptInterface
        public String getApprovalReferrerName() {
            return MainActivity.this.specialApprovalPrefs().getString("referrer_name", "");
        }

        @JavascriptInterface
        public void checkSpecialApproval() throws JSONException {
            MainActivity.this.checkSpecialApprovalAsync();
        }

        @JavascriptInterface
        public String musicAction(String str) {
            return MainActivity.this.handleMusicAction(str);
        }

        @JavascriptInterface
        public void playRadioDirect(String str, String str2) {
            MainActivity.this.playRadioDirect(str, str2);
        }

        @JavascriptInterface
        public void stopRadioDirect() {
            MainActivity.this.stopRadioDirect();
        }

        @JavascriptInterface
        public String getRadioDirectState() {
            return MainActivity.this.radioStateJson();
        }

        @JavascriptInterface
        public boolean isOverlayIconEnabled() {
            return MainActivity.this.isOverlayControlEnabled();
        }

        /* renamed from: lambda$setOverlayIconEnabled$1$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m85x8cf4d9e3(boolean z) {
            MainActivity.this.setOverlayControlEnabled(z);
        }

        @JavascriptInterface
        public void setOverlayIconEnabled(final boolean z) {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda8
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m85x8cf4d9e3(z);
                }
            });
        }

        @JavascriptInterface
        public void openRadioPage(String str) {
            final String strTrim = str == null ? "" : str.trim();
            if (strTrim.startsWith("https://") || strTrim.startsWith("http://")) {
                MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda4
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m83x1d853c8a(strTrim);
                    }
                });
            }
        }

        /* renamed from: lambda$openRadioPage$2$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m83x1d853c8a(String str) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(str));
                intent.addFlags(268435456);
                MainActivity.this.startActivity(intent);
            } catch (Exception unused) {
                Toast.makeText(MainActivity.this, "라디오 페이지를 열지 못했습니다.", 0).show();
            }
        }

        @JavascriptInterface
        public void playCelebrationWithVoice(String str, int i) {
            MainActivity.this.playCelebrationWithVoice(str, i);
        }

        @JavascriptInterface
        public void speakCelebration(String str, int i) {
            MainActivity.this.m41xf0b0daf1(str, i);
        }

        @JavascriptInterface
        public void speakConsolation(String str) throws IllegalStateException {
            MainActivity.this.speakConsolationVoice(str);
        }

        @JavascriptInterface
        public void stopScoreVoice() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() throws IllegalStateException {
                    this.f$0.m86xc4819d47();
                }
            });
        }

        /* renamed from: lambda$stopScoreVoice$3$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m86xc4819d47() throws IllegalStateException {
            try {
                if (MainActivity.this.scoreTts != null) {
                    MainActivity.this.scoreTts.stop();
                }
            } catch (Exception unused) {
            }
            MainActivity.this.stopScoreFanfare();
        }

        @JavascriptInterface
        public void exitApp() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda7
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m80x5ba837ef();
                }
            });
        }

        /* renamed from: lambda$exitApp$4$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m80x5ba837ef() {
            MainActivity.this.closeAppScreen();
        }

        @JavascriptInterface
        public void chooseUpdate() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m78xcaef6767();
                }
            });
        }

        /* renamed from: lambda$chooseUpdate$5$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m78xcaef6767() {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("*/*");
            intent.putExtra("android.intent.extra.MIME_TYPES", new String[]{"text/html", "text/plain", "application/octet-stream"});
            MainActivity.this.setDownloadInitialUri(intent);
            try {
                MainActivity.this.startActivityForResult(intent, MainActivity.UPDATE_PICKER);
            } catch (Exception unused) {
                MainActivity.this.alert("HTML 업데이트 파일 선택기를 열 수 없습니다.");
            }
        }

        @JavascriptInterface
        public void chooseApkUpdate() {
            MainActivity.this.chooseApkUpdateFile();
        }

        @JavascriptInterface
        public String getNativeVersion() throws PackageManager.NameNotFoundException {
            try {
                PackageInfo packageInfo = MainActivity.this.getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0);
                return (packageInfo.versionName == null ? "V3 native" : packageInfo.versionName) + " (" + (Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode() : packageInfo.versionCode) + ")";
            } catch (Exception unused) {
                return "V3 native";
            }
        }

        @JavascriptInterface
        public void chooseText() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda9
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m77x8cd48f8c();
                }
            });
        }

        /* renamed from: lambda$chooseText$6$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m77x8cd48f8c() {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("text/plain");
            try {
                MainActivity.this.startActivityForResult(intent, MainActivity.TEXT_PICKER);
            } catch (Exception unused) {
                MainActivity.this.alert("TXT 선택기를 열 수 없습니다.");
            }
        }

        @JavascriptInterface
        public void chooseLegacyBackup() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m76x218f5d0b();
                }
            });
        }

        /* renamed from: lambda$chooseLegacyBackup$7$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m76x218f5d0b() {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("*/*");
            intent.putExtra("android.intent.extra.MIME_TYPES", new String[]{"application/json", "text/plain", "application/octet-stream"});
            try {
                MainActivity.this.startActivityForResult(intent, MainActivity.LEGACY_BACKUP_PICKER);
            } catch (Exception unused) {
                MainActivity.this.alert("백업 JSON 선택기를 열 수 없습니다.");
            }
        }

        @JavascriptInterface
        public boolean applyUpdateHtml(String str) {
            return MainActivity.this.installUpdate(str);
        }

        @JavascriptInterface
        public String backupJsonV37(String str) {
            return MainActivity.this.saveBackupDirectSync(str);
        }

        @JavascriptInterface
        public void backupJson(String str) {
            if (str == null || str.isEmpty()) {
                return;
            }
            MainActivity.this.saveBackupDirect(str);
        }

        @JavascriptInterface
        public void saveText(String str, String str2) {
            if (str2 != null) {
                MainActivity mainActivity = MainActivity.this;
                if (str == null) {
                    str = "항행의자유_V3_기록.txt";
                }
                mainActivity.createSave(str, "text/plain", str2);
            }
        }

        @JavascriptInterface
        public void copyText(final String str) {
            if (str == null) {
                return;
            }
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m79x3eb22dac(str);
                }
            });
        }

        /* renamed from: lambda$copyText$8$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m79x3eb22dac(String str) {
            try {
                ClipboardManager clipboardManager = (ClipboardManager) MainActivity.this.getSystemService("clipboard");
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Final5", str));
                }
            } catch (Exception unused) {
            }
        }

        @JavascriptInterface
        public boolean clearInstalledUpdate() {
            File file = new File(MainActivity.this.getFilesDir(), MainActivity.UPDATE_FILE);
            return !file.exists() || file.delete();
        }

        @JavascriptInterface
        public void fetchWinningNumbers(int i) throws JSONException {
            MainActivity.this.fetchWinningNumbersAsync(i);
        }

        @JavascriptInterface
        public String getMusicState() {
            return MusicPlaybackService.snapshot(MainActivity.this);
        }

        @JavascriptInterface
        public String getSavedMusicLibrary() {
            return new MusicLibraryStore(MainActivity.this).asJson();
        }

        @JavascriptInterface
        public void playSavedMusic(final String str) {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m84x11930c9d(str);
                }
            });
        }

        /* renamed from: lambda$playSavedMusic$9$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m84x11930c9d(String str) {
            try {
                Intent intentPutExtra = new Intent(MainActivity.this, (Class<?>) MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_SELECT).putExtra(MusicPlaybackService.EXTRA_TRACK, str);
                if (Build.VERSION.SDK_INT >= 26) {
                    MainActivity.this.startForegroundService(intentPutExtra);
                } else {
                    MainActivity.this.startService(intentPutExtra);
                }
            } catch (Exception unused) {
                Toast.makeText(MainActivity.this, "음악 재생을 시작하지 못했습니다.", 1).show();
            }
        }

        @JavascriptInterface
        public void openOurMusicFolder() {
            chooseMusicFolder();
        }

        @JavascriptInterface
        public void openDeviceMusicLibrary(String str) {
            MainActivity.this.chooseMultipleMusicFiles();
        }

        @JavascriptInterface
        public void saveDeviceMusicSelection(String str) {
            MainActivity.this.saveDeviceMusicSelection(str);
        }

        @JavascriptInterface
        public void chooseMusic() {
            MainActivity.this.chooseMusicFile();
        }

        @JavascriptInterface
        public void chooseMusicFolder() {
            chooseMusicFolder();
        }

        @JavascriptInterface
        public void chooseMultipleMusic() {
            MainActivity.this.chooseMultipleMusicFiles();
        }

        @JavascriptInterface
        public void musicNext() {
            MainActivity.this.sendMusicCommand(MusicPlaybackService.ACTION_NEXT);
        }

        @JavascriptInterface
        public void musicPrev() {
            MainActivity.this.sendMusicCommand(MusicPlaybackService.ACTION_PREV);
        }

        @JavascriptInterface
        public void stopMusic() {
            MainActivity.this.stopBackgroundMusic();
        }

        @JavascriptInterface
        public int getMusicPlaylistCount() {
            return MainActivity.this.musicPlaylistCount();
        }

        @JavascriptInterface
        public String getMusicName() {
            return MainActivity.this.musicName();
        }

        @JavascriptInterface
        public boolean isMusicEnabled() {
            return MainActivity.this.musicEnabled();
        }

        @JavascriptInterface
        public int getMusicVolume() {
            return MainActivity.this.musicVolume();
        }

        @JavascriptInterface
        public boolean toggleMusic() {
            return MainActivity.this.toggleBackgroundMusic();
        }

        @JavascriptInterface
        public void setMusicVolume(int i) {
            MainActivity.this.applyMusicVolume(i);
        }

        @JavascriptInterface
        public void clearMusic() {
            MainActivity.this.clearSelectedMusic();
        }

        @JavascriptInterface
        public void finishOverlayUpdateScreen() {
            MainActivity.this.runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.MainActivity$AndroidHostBridge$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m81x3dc686e7();
                }
            });
        }

        /* renamed from: lambda$finishOverlayUpdateScreen$10$com-baekhak-centralcontrol-MainActivity$AndroidHostBridge, reason: not valid java name */
        /* synthetic */ void m81x3dc686e7() {
            MainActivity.this.overlayUpdateScreenActive = false;
            MainActivity.this.restoreOverlayAfterUpdate();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void restoreOverlayAfterUpdate() {
        try {
            Intent action = new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(action);
            } else {
                startService(action);
            }
        } catch (Exception unused) {
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        this.overlayUpdateScreenActive = false;
        restoreOverlayAfterUpdate();
        if (this.launchVideoActive) {
            closeAppScreen();
            return;
        }
        WebView webView = this.webView;
        if (webView == null || !webView.canGoBack()) {
            closeAppScreen();
        } else {
            this.webView.goBack();
        }
    }
}
