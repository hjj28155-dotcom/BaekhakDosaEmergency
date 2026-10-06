package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.ComponentName;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.view.View;
import android.view.Gravity;
import android.view.TextureView;
import android.view.Surface;
import android.graphics.SurfaceTexture;
import android.graphics.Color;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.media.MediaPlayer;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.UUID;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {
    private WebView webView;
    private String pendingData, pendingName, pendingMime;

    private static final int UPDATE_PICKER = 4303;
    private static final int SAVE_PICKER = 4304;
    private static final int TEXT_PICKER = 4305;
    private static final int LEGACY_BACKUP_PICKER = 4306;
    private static final int MUSIC_PICKER = 4307;
    private static final int APK_UPDATE_PICKER = 4308;
    private static final int MUSIC_FOLDER_PICKER = 4309;
    private static final int MUSIC_MULTI_PICKER = 4311;
    public static final String EXTRA_OPEN_SAVED_MUSIC = "v3_open_saved_music_library";
    public static final String EXTRA_OPEN_UPDATE = "v3_open_update";
    private static final int AUDIO_LIBRARY_PERMISSION = 4312;
    private static final int AUTO_AUDIO_PERMISSION = 4313;

    private static final String UPDATE_FILE = "shipmate_v3_update.html";
    private static final String BASE_URL = "file:///android_asset/";
    private static final int CONTENT_BUILD = BuildConfig.VERSION_CODE;
    private static final String UPDATE_STATS_URL = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/app-update-heartbeat";
    private static final String UPDATE_STATS_APP_KEY = "bxuiC99t7w3AG6PtIe-b3ui3xiIqRsRh-OKw0LU26qk";
    private static final String UPDATE_TRACK_PREFS = "v3_update_tracking";
    private static final String SPECIAL_APPROVAL_PREFS = "v3_round_special_approval";
    private static final String APPROVAL_REQUEST_URL = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/approval-request";
    private static final String APPROVAL_STATUS_URL = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/approval-status";
    private static final int SPECIAL_APPROVAL_START_ROUND = 1243;

    private static final String MUSIC_PREFS = "v3_music_preferences";
    private static final String MUSIC_NAME = "music_name";
    private static final String MUSIC_ENABLED = "music_enabled";
    private static final String MUSIC_VOLUME = "music_volume";
    private static final String MUSIC_FILE = "v3_user_background_audio";
    private static final String MUSIC_DIR = "v3_music_playlist";
    private static final String MUSIC_TREE_URI = "music_tree_uri";
    private String pendingMusicLibraryMode = "multi";

    // V2 성적 화면의 축하음악 + 한국어 음성안내 계승
    private TextToSpeech scoreTts;
    private boolean scoreTtsReady = false;
    private String pendingScoreSpeech = null;
    private int pendingScoreRank = 0;
    private MediaPlayer scoreFanfarePlayer;
    private MediaPlayer launchSeaPlayer;
    private TextToSpeech launchTts;
    private FrameLayout rootLayout;
    private TextureView launchVideo;
    private MediaPlayer launchVideoPlayer;
    private MediaPlayer radioPlayer;
    private boolean radioPlaying = false;
    private String radioName = "";
    private String radioUrl = "";
    private Surface launchVideoSurface;
    private boolean launchVideoPrepared = false;
    private boolean launchVideoActive = false;
    private boolean launchTtsReady = false;
    private boolean launchSpeechRequested = false;
    private boolean launchIntroFinished = false;
    private boolean overlayUpdateScreenActive = false;
    private static final String LAUNCH_WELCOME_TEXT = "우리 항행의자유앱에 승선한 지인들께 알려드립니다. 희망의 배에 승선하신 것을 진심으로 환영합니다. 부디 즐거운 항해가 되시기를 기원합니다.";
    private final Handler scoreHandler = new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prepareBuildMigration();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        startAlwaysOverlayExperiment();

        rootLayout = new FrameLayout(this);
        setContentView(rootLayout);

        webView = new WebView(this);
        webView.setVisibility(View.VISIBLE);
        rootLayout.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        AndroidHostBridge nativeBridge = new AndroidHostBridge();
        webView.addJavascriptInterface(nativeBridge, "AndroidHost");
        webView.addJavascriptInterface(nativeBridge, "BaekhakNative");
        webView.setWebViewClient(new WebViewClient());
        configureAppWebView(webView);

        initScoreTts();
        // FIX9: saved playback intent is restored in onResume; never force music OFF.
        loadInstalledApp();
        handleSavedMusicIntent(getIntent());
        handleOverlayUpdateIntent(getIntent());
        if (getIntent() != null && getIntent().getBooleanExtra("v3_play_install_guide", false)) {
            getIntent().removeExtra("v3_play_install_guide");
            webView.postDelayed(() -> webView.evaluateJavascript("if(window.playInstallGuide){window.playInstallGuide();}", null), 1200);
        }
        requestMusicNotificationPermission();
        handleOverlayAudioPermissionRequest(getIntent());
        requestAutoAudioPermissionOnce();
        // V1059: 자동 전체 음악검색 금지. 직접 선택한 곡만 내부보관합니다.
        reportStoredNameUpdateCheckinAsync();
    }

    /** Flavor-specific connections; the distribution implementation contains no admin code. */
    protected void configureAppWebView(WebView view) { }

    // STAGE4.47: 기본 인트로는 MP4 원본에서 만든 animated WebP를 WebView에서 재생합니다.
    // 아래 네이티브 영상 루틴은 호출하지 않는 비상용 코드로만 남겨 둡니다.
    private void startNativeVoyageIntro() {
        launchIntroFinished = false;
        launchVideoActive = true;
        launchVideoPrepared = false;
        try {
            if (webView != null) webView.setVisibility(View.INVISIBLE);
            if (rootLayout != null) rootLayout.setBackgroundColor(Color.BLACK);
            releaseNativeVoyageMedia();
            if (launchVideo != null && rootLayout != null) {
                try { rootLayout.removeView(launchVideo); } catch (Exception ignored) {}
            }

            launchVideo = new TextureView(this);
            launchVideo.setOpaque(true);
            launchVideo.setBackgroundColor(Color.BLACK);
            FrameLayout.LayoutParams vp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            vp.gravity = Gravity.CENTER;
            rootLayout.addView(launchVideo, vp);

            launchVideo.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    prepareNativeVoyagePlayer(surfaceTexture);
                }
                @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {}
                @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    try { if (launchVideoSurface != null) launchVideoSurface.release(); } catch (Exception ignored) {}
                    launchVideoSurface = null;
                    return true;
                }
                @Override public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
            });

            if (launchVideo.isAvailable() && launchVideo.getSurfaceTexture() != null) {
                prepareNativeVoyagePlayer(launchVideo.getSurfaceTexture());
            }

            scoreHandler.postDelayed(() -> {
                if (!launchVideoActive || launchVideoPrepared) return;
                try {
                    if (launchVideo != null && launchVideo.isAvailable() && launchVideo.getSurfaceTexture() != null) {
                        prepareNativeVoyagePlayer(launchVideo.getSurfaceTexture());
                    }
                } catch (Exception ignored) {}
            }, 1800);
            // 영상 완료 콜백이 누락되는 기기에서도 홈으로 반드시 복귀.
            scoreHandler.postDelayed(() -> {
                if (launchVideoActive) finishNativeVoyageVideo();
            }, 23000);
        } catch (Exception e) {
            finishNativeVoyageVideo();
        }
    }

    private void prepareNativeVoyagePlayer(SurfaceTexture surfaceTexture) {
        // STAGE4.53: 시작 동영상 삭제. 네이티브 영상 재생 비활성.
        finishNativeVoyageVideo();
    }

    private void releaseNativeVoyageMedia() {
        try {
            if (launchVideoPlayer != null) {
                try { if (launchVideoPlayer.isPlaying()) launchVideoPlayer.stop(); } catch (Exception ignored) {}
                try { launchVideoPlayer.reset(); } catch (Exception ignored) {}
                try { launchVideoPlayer.release(); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        launchVideoPlayer = null;
        try { if (launchVideoSurface != null) launchVideoSurface.release(); } catch (Exception ignored) {}
        launchVideoSurface = null;
    }

    private void finishNativeVoyageVideo() {
        if (!launchVideoActive) return;
        launchVideoActive = false;
        launchIntroFinished = true;
        launchVideoPrepared = false;
        releaseNativeVoyageMedia();
        try { if (launchTts != null) launchTts.stop(); } catch (Exception ignored) {}
        try { if (launchVideo != null && rootLayout != null) rootLayout.removeView(launchVideo); } catch (Exception ignored) {}
        launchVideo = null;
        if (webView != null) webView.setVisibility(View.VISIBLE);
    }


    private boolean isOverlayControlEnabled() {
        try { return getSharedPreferences("overlay_control", MODE_PRIVATE).getBoolean("enabled", true); }
        catch (Exception e) { return true; }
    }

    private void setOverlayControlEnabled(boolean enabled) {
        try { getSharedPreferences("overlay_control", MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply(); }
        catch (Exception ignored) {}
        try {
            Intent service = new Intent(this, OverlayControlService.class)
                    .setAction(enabled ? OverlayControlService.ACTION_SHOW : OverlayControlService.ACTION_HIDE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(service);
            else startService(service);
        } catch (Exception ignored) {}
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            try {
                Intent permission = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(permission);
            } catch (Exception ignored) {}
        }
    }

    // V3.1.04 항해사 전용 실험: 상시 음악/FM/AM 오버레이. 기존 onResume은 건드리지 않습니다.
    private void startAlwaysOverlayExperiment() {
        if (!isOverlayControlEnabled()) return;
        try {
            Intent service = new Intent(this, OverlayControlService.class).setAction(OverlayControlService.ACTION_SHOW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(service);
            else startService(service);
        } catch (Exception ignored) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            try {
                Intent permission = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(permission);
                Toast.makeText(this, "상단 음악·FM·AM 미니바를 위해 '다른 앱 위에 표시'를 허용해 주세요.", Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
        }
    }

    private void closeAppScreen() {
        stopBackgroundMusicForAppExit();
        try {
            launchVideoActive = false;
            launchIntroFinished = true;
            launchVideoPrepared = false;
            releaseNativeVoyageMedia();
            if (launchVideo != null && rootLayout != null) {
                try { rootLayout.removeView(launchVideo); } catch (Exception ignored) {}
            }
            launchVideo = null;
            try { if (launchTts != null) launchTts.stop(); } catch (Exception ignored) {}
            try { if (webView != null) { webView.setVisibility(View.INVISIBLE); webView.stopLoading(); webView.loadUrl("about:blank"); } } catch (Exception ignored) {}
        } catch (Exception ignored) {}
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask();
            else finish();
        } catch (Exception e) {
            try { finish(); } catch (Exception ignored) {}
        }
    }

    // STAGE4.40: 앱 실행 시 우리 배가 다가오는 동안 바다소리를 재생합니다.
    private void startLaunchSeaSound() {
        try {
            if (launchSeaPlayer != null) { launchSeaPlayer.release(); launchSeaPlayer = null; }
            launchSeaPlayer = MediaPlayer.create(this, R.raw.sea_intro);
            if (launchSeaPlayer != null) {
                launchSeaPlayer.setLooping(true);
                launchSeaPlayer.setVolume(0.42f, 0.42f);
                launchSeaPlayer.start();
            }
        } catch (Exception ignored) {}
    }

    private void finishLaunchIntroNative() {
        if (launchIntroFinished) return;
        launchIntroFinished = true;
        try {
            if (launchSeaPlayer != null) {
                if (launchSeaPlayer.isPlaying()) launchSeaPlayer.stop();
                launchSeaPlayer.release();
                launchSeaPlayer = null;
            }
        } catch (Exception ignored) { launchSeaPlayer = null; }
        try { if (launchTts != null) launchTts.stop(); } catch (Exception ignored) {}
    }

    // STAGE4.42: 20초 항해 인트로 동안 한국어 승무원 환영 멘트를 안내합니다.
    private void initLaunchTts() {
        try {
            launchTts = new TextToSpeech(getApplicationContext(), status -> {
                if (status != TextToSpeech.SUCCESS || launchTts == null) return;
                try {
                    int lang = launchTts.setLanguage(Locale.KOREA);
                    launchTtsReady = lang != TextToSpeech.LANG_MISSING_DATA && lang != TextToSpeech.LANG_NOT_SUPPORTED;
                    launchTts.setSpeechRate(0.86f);
                    launchTts.setPitch(1.18f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        launchTts.setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build());
                        // 기기에 여성으로 표시된 한국어 음성이 있으면 우선 사용합니다.
                        try {
                            android.speech.tts.Voice fallback = null;
                            for (android.speech.tts.Voice v : launchTts.getVoices()) {
                                if (v == null || v.getLocale() == null) continue;
                                String language = v.getLocale().getLanguage();
                                if (!"ko".equalsIgnoreCase(language)) continue;
                                if (fallback == null) fallback = v;
                                String name = String.valueOf(v.getName()).toLowerCase(java.util.Locale.ROOT);
                                String features = String.valueOf(v.getFeatures()).toLowerCase(java.util.Locale.ROOT);
                                if (name.contains("female") || name.contains("woman") || features.contains("female")) {
                                    launchTts.setVoice(v);
                                    fallback = null;
                                    break;
                                }
                            }
                            if (fallback != null) launchTts.setVoice(fallback);
                        } catch (Exception ignored) {}
                    }
                    if (launchTtsReady && launchSpeechRequested && !launchIntroFinished) {
                        speakLaunchWelcomeNow();
                    }
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {
            launchTts = null;
            launchTtsReady = false;
        }
    }

    private void speakLaunchWelcome() {
        if (launchIntroFinished) return;
        launchSpeechRequested = true;
        if (launchTtsReady) speakLaunchWelcomeNow();
    }

    private void speakLaunchWelcomeNow() {
        if (launchIntroFinished || launchTts == null) return;
        launchSpeechRequested = false;
        try {
            if (launchSeaPlayer != null) launchSeaPlayer.setVolume(0.26f, 0.26f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                android.os.Bundle ttsParams = new android.os.Bundle();
                ttsParams.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
                launchTts.speak(LAUNCH_WELCOME_TEXT, TextToSpeech.QUEUE_FLUSH, ttsParams, "V3_LAUNCH_WELCOME");
            } else {
                launchTts.speak(LAUNCH_WELCOME_TEXT, TextToSpeech.QUEUE_FLUSH, null);
            }
        } catch (Exception ignored) {}
    }

    // FIX9 preserves the previous song, position and explicit pause/stop choice.


    private String getOrCreateAnonymousInstallId() {
        SharedPreferences sp = getSharedPreferences(UPDATE_TRACK_PREFS, MODE_PRIVATE);
        String id = sp.getString("anonymous_install_id", "");
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
            sp.edit().putString("anonymous_install_id", id).apply();
        }
        return id;
    }

    private String currentVersionName() {
        try {
            android.content.pm.PackageInfo p = getPackageManager().getPackageInfo(getPackageName(), 0);
            return p.versionName == null ? "V3" : p.versionName;
        } catch (Exception e) { return "V3"; }
    }

    private String postUpdateApi(JSONObject payload) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(UPDATE_STATS_URL).openConnection();
        c.setConnectTimeout(7000); c.setReadTimeout(7000);
        c.setRequestMethod("POST"); c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        c.setRequestProperty("x-app-key", UPDATE_STATS_APP_KEY);
        try (OutputStream out = c.getOutputStream()) {
            out.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
        int code = c.getResponseCode();
        InputStream in = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        String body = in == null ? "" : new String(readAll(in), StandardCharsets.UTF_8);
        c.disconnect();
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code);
        return body;
    }

    // 같은 버전은 설치기기당 한 번만 서버에 보고합니다.
    private void reportUpdateCheckinAsync() {
        final long versionCode = getInstalledVersionCode();
        final SharedPreferences sp = getSharedPreferences(UPDATE_TRACK_PREFS, MODE_PRIVATE);
        if (sp.getLong("reported_version_code", -1L) == versionCode) return;
        new Thread(() -> {
            try {
                JSONObject p = new JSONObject();
                p.put("action", "checkin");
                p.put("install_id", getOrCreateAnonymousInstallId());
                p.put("version_name", currentVersionName());
                p.put("version_code", versionCode);
                String body = postUpdateApi(p);
                JSONObject r = new JSONObject(body);
                if (r.optBoolean("ok", false)) sp.edit().putLong("reported_version_code", versionCode).apply();
            } catch (Exception ignored) {}
        }, "V3-UpdateCheckin").start();
    }

    private void requestUpdateStatsAsync() {
        new Thread(() -> {
            String body;
            try {
                JSONObject p = new JSONObject(); p.put("action", "stats");
                body = postUpdateApi(p);
            } catch (Exception e) {
                try { JSONObject x = new JSONObject(); x.put("ok", false); x.put("error", "network"); body = x.toString(); }
                catch (Exception ignored) { body = "{\"ok\":false}"; }
            }
            final String result = body;
            if (webView != null) webView.post(() -> webView.evaluateJavascript("onUpdateStats(" + JSONObject.quote(result) + ");", null));
        }, "V3-UpdateStats").start();
    }
    private String postUpdateAdminApi(JSONObject payload) throws Exception {
        throw new Exception("admin_stats_disabled_in_distribution");
    }

    private String cleanUpdateDisplayName(String raw) {
        String n = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (n.isEmpty() || n.length() > 40) return "";
        if (n.matches(".*\\d.*") || n.contains("@")) return "";
        return n;
    }

    private String getStoredUpdateDisplayName() {
        return getSharedPreferences(UPDATE_TRACK_PREFS, MODE_PRIVATE).getString("update_display_name", "");
    }

    private void reportStoredNameUpdateCheckinAsync() {
        final String name = cleanUpdateDisplayName(getStoredUpdateDisplayName());
        if (name.isEmpty()) return;
        reportNamedUpdateCheckinAsync(name);
    }

    private void reportNamedUpdateCheckinAsync(final String rawName) {
        final String name = cleanUpdateDisplayName(rawName);
        if (name.isEmpty()) return;
        final long versionCode = getInstalledVersionCode();
        final SharedPreferences sp = getSharedPreferences(UPDATE_TRACK_PREFS, MODE_PRIVATE);
        String lastName = sp.getString("reported_named_name", "");
        long lastCode = sp.getLong("reported_named_version_code", -1L);
        if (lastCode == versionCode && name.equals(lastName)) return;
        new Thread(() -> {
            try {
                JSONObject p = new JSONObject();
                p.put("action", "named_checkin");
                p.put("display_name", name);
                p.put("version_name", currentVersionName());
                p.put("version_code", versionCode);
                JSONObject r = new JSONObject(postUpdateApi(p));
                if (r.optBoolean("ok", false)) {
                    sp.edit()
                      .putString("update_display_name", name)
                      .putString("reported_named_name", name)
                      .putLong("reported_named_version_code", versionCode)
                      .apply();
                }
            } catch (Exception ignored) {}
        }, "V3-NamedUpdateCheckin").start();
    }

    private void setUpdateDisplayNameAndReportAsync(final String rawName) {
        final String name = cleanUpdateDisplayName(rawName);
        if (name.isEmpty()) {
            if (webView != null) webView.post(() -> webView.evaluateJavascript(
                "if(window.onUpdateNameSaved){onUpdateNameSaved(false,'이름만 입력해 주세요. 숫자·전화번호·이메일은 저장하지 않습니다.');}", null));
            return;
        }
        getSharedPreferences(UPDATE_TRACK_PREFS, MODE_PRIVATE).edit().putString("update_display_name", name).apply();
        reportNamedUpdateCheckinAsync(name);
        if (webView != null) webView.postDelayed(() -> webView.evaluateJavascript(
            "if(window.onUpdateNameSaved){onUpdateNameSaved(true," + JSONObject.quote(name) + ");}", null), 250);
    }

    private void requestNamedUpdateStatsAsync() {
        final String result = "{\"ok\":false,\"error\":\"admin_view_disabled_in_distribution\"}";
        if (webView != null) webView.post(() -> webView.evaluateJavascript(
            "if(window.onNamedUpdateStats){onNamedUpdateStats(" + JSONObject.quote(result) + ");}", null));
    }

    private SharedPreferences specialApprovalPrefs() {
        return getSharedPreferences(SPECIAL_APPROVAL_PREFS, MODE_PRIVATE);
    }

    private String getOrCreateApprovalDeviceId() {
        SharedPreferences sp = specialApprovalPrefs();
        String id = sp.getString("device_id", "");
        if (id == null || !id.matches("\\d{6,20}")) {
            long base = System.currentTimeMillis();
            String tail = String.valueOf(Math.abs(UUID.randomUUID().hashCode()));
            if (tail.length() > 5) tail = tail.substring(tail.length() - 5);
            id = String.valueOf(base) + tail;
            if (id.length() > 20) id = id.substring(id.length() - 20);
            sp.edit().putString("device_id", id).apply();
        }
        return id;
    }

    private String postJson(String url, JSONObject payload) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(7000); c.setReadTimeout(7000);
        c.setRequestMethod("POST"); c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        try (OutputStream out = c.getOutputStream()) {
            out.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
        int code = c.getResponseCode();
        InputStream in = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        String body = in == null ? "" : new String(readAll(in), StandardCharsets.UTF_8);
        c.disconnect();
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code + " " + body);
        return body;
    }

    private boolean isSpecialApprovedLocal() {
        return specialApprovalPrefs().getBoolean("approved", false);
    }

    private String specialApprovalSummaryJson() {
        try {
            SharedPreferences sp = specialApprovalPrefs();
            JSONObject j = new JSONObject();
            boolean approved = sp.getBoolean("approved", false);
            boolean hasRequest = !sp.getString("request_token", "").isEmpty();
            j.put("approved", approved);
            j.put("requested_round", sp.getInt("requested_round", SPECIAL_APPROVAL_START_ROUND));
            j.put("approval_code", sp.getString("approval_code", ""));
            j.put("has_request", hasRequest);
            j.put("applicant_name", sp.getString("applicant_name", ""));
            j.put("referrer_name", sp.getString("referrer_name", ""));
            j.put("status", approved ? "approved" : (hasRequest ? "pending" : "not_requested"));
            return j.toString();
        } catch (Exception e) { return "{\"approved\":false}"; }
    }

    private void sendSpecialApprovalState(String json) {
        final String payload = json == null ? "{}" : json;
        if (webView != null) webView.post(() -> webView.evaluateJavascript(
            "if(window.onSpecialApprovalState){onSpecialApprovalState(" + JSONObject.quote(payload) + ");}", null));
    }

    private void requestSpecialApprovalAsync(final int requestedRound, final String applicantRaw, final String referrerRaw) {
        if (requestedRound < SPECIAL_APPROVAL_START_ROUND) {
            sendSpecialApprovalState("{\"ok\":true,\"status\":\"approved\",\"round_policy_free\":true}");
            return;
        }
        final String applicant = applicantRaw == null ? "" : applicantRaw.trim();
        final String referrer = referrerRaw == null ? "" : referrerRaw.trim();
        if (applicant.isEmpty() || referrer.isEmpty()) { sendSpecialApprovalState("{\"ok\":false,\"error\":\"identity_required\"}"); return; }
        specialApprovalPrefs().edit().putString("applicant_name", applicant).putString("referrer_name", referrer).apply();
        new Thread(() -> {
            try {
                JSONObject p = new JSONObject();
                p.put("device_id", getOrCreateApprovalDeviceId());
                p.put("device_label", applicant + "||" + referrer);
                p.put("referrer_name", referrer);
                p.put("purpose_ack", true);
                p.put("requested_round", requestedRound);
                String body = postJson(APPROVAL_REQUEST_URL, p);
                JSONObject r = new JSONObject(body);
                SharedPreferences.Editor ed = specialApprovalPrefs().edit();
                if (r.has("request_token")) ed.putString("request_token", r.optString("request_token", ""));
                if (r.has("approval_code")) ed.putString("approval_code", r.optString("approval_code", ""));
                ed.putInt("requested_round", requestedRound);
                if ("approved".equalsIgnoreCase(r.optString("status", ""))) ed.putBoolean("approved", true);
                ed.apply();
                JSONObject merged = new JSONObject(body);
                String cachedCode = specialApprovalPrefs().getString("approval_code", "");
                if (!cachedCode.isEmpty() && !merged.has("approval_code")) merged.put("approval_code", cachedCode);
                if (!merged.has("has_request")) merged.put("has_request", !specialApprovalPrefs().getString("request_token", "").isEmpty());
                sendSpecialApprovalState(merged.toString());
            } catch (Exception e) {
                sendSpecialApprovalState("{\"ok\":false,\"error\":\"network\"}");
            }
        }, "V3-SpecialApprovalRequest").start();
    }

    private void checkSpecialApprovalAsync() {
        if (isSpecialApprovedLocal()) {
            try {
                JSONObject merged = new JSONObject(specialApprovalSummaryJson());
                merged.put("ok", true);
                merged.put("status", "approved");
                merged.put("special_approval", true);
                sendSpecialApprovalState(merged.toString());
            } catch (Exception e) {
                sendSpecialApprovalState(specialApprovalSummaryJson());
            }
            return;
        }
        final SharedPreferences sp = specialApprovalPrefs();
        final String token = sp.getString("request_token", "");
        if (token == null || token.isEmpty()) {
            sendSpecialApprovalState("{\"ok\":true,\"status\":\"not_requested\"}");
            return;
        }
        new Thread(() -> {
            try {
                JSONObject p = new JSONObject();
                p.put("request_token", token);
                p.put("device_id", getOrCreateApprovalDeviceId());
                String body = postJson(APPROVAL_STATUS_URL, p);
                JSONObject r = new JSONObject(body);
                if ("approved".equalsIgnoreCase(r.optString("status", ""))) {
                    sp.edit().putBoolean("approved", true).apply();
                }
                JSONObject merged = new JSONObject(body);
                String cachedCode = sp.getString("approval_code", "");
                if (!cachedCode.isEmpty() && !merged.has("approval_code")) merged.put("approval_code", cachedCode);
                if (!merged.has("has_request")) merged.put("has_request", !sp.getString("request_token", "").isEmpty());
                sendSpecialApprovalState(merged.toString());
            } catch (Exception e) {
                sendSpecialApprovalState("{\"ok\":false,\"error\":\"network\"}");
            }
        }, "V3-SpecialApprovalStatus").start();
    }

    // 새 빌드를 처음 설치/업데이트했을 때 이전 HTML 업데이트 잔재가
    // 새 내장 화면을 덮어쓰지 않도록 딱 한 번 정리합니다.
    private long getInstalledVersionCode() {
        try {
            android.content.pm.PackageInfo p = getPackageManager().getPackageInfo(getPackageName(), 0);
            return Build.VERSION.SDK_INT >= 28 ? p.getLongVersionCode() : p.versionCode;
        } catch (Exception e) {
            return CONTENT_BUILD;
        }
    }

    private void prepareBuildMigration() {
        // STAGE4.35부터는 APK에 내장된 화면만 공식 실행본으로 사용합니다.
        // 과거 HTML 내부 업데이트 파일은 보관하고 읽지 않아 구버전 화면이
        // 새 Java/브리지 기능을 덮어쓰는 현상을 원천 차단합니다.
        try {
            File f = new File(getFilesDir(), UPDATE_FILE);
            if (f.exists()) {
                File backup = new File(getFilesDir(), UPDATE_FILE + ".before_integrated");
                if (!backup.exists()) f.renameTo(backup);
            }
        } catch (Exception ignored) {}
        SharedPreferences sp = getSharedPreferences("v3_native_state", MODE_PRIVATE);
        sp.edit()
          .putLong("native_version_code", getInstalledVersionCode())
          .putInt("content_build", CONTENT_BUILD)
          .apply();
    }

    private void loadInstalledApp() {
        // 공식 화면은 항상 현재 APK의 assets/index.html에서 로드합니다.
        webView.loadUrl(BASE_URL + "index.html");
    }

    private byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    private String readUri(Uri uri) throws Exception {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) throw new Exception("open failed");
            return new String(readAll(in), StandardCharsets.UTF_8);
        }
    }

    private String displayName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) {
                    String n = c.getString(i);
                    if (n != null && !n.trim().isEmpty()) return n;
                }
            }
        } catch (Exception ignored) {}
        return "파일";
    }

    private boolean installUpdate(String html) {
        if (html == null || html.length() < 300 || !html.contains("SHIPMATE_V3_UPDATE")) return false;
        File tmp = new File(getFilesDir(), UPDATE_FILE + ".tmp");
        File dst = new File(getFilesDir(), UPDATE_FILE);
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(html.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception e) { return false; }
        if (dst.exists() && !dst.delete()) { tmp.delete(); return false; }
        if (!tmp.renameTo(dst)) { tmp.delete(); return false; }
        runOnUiThread(() -> webView.loadDataWithBaseURL(BASE_URL, html, "text/html", "UTF-8", null));
        return true;
    }

    private void alert(String message) {
        final String q = JSONObject.quote(message == null ? "" : message);
        webView.post(() -> webView.evaluateJavascript("alert(" + q + ");", null));
    }

    private void createSave(String name, String mime, String data) {
        pendingName = name;
        pendingMime = mime;
        pendingData = data;
        runOnUiThread(() -> {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(mime);
            i.putExtra(Intent.EXTRA_TITLE, name);
            try { startActivityForResult(i, SAVE_PICKER); }
            catch (Exception e) { pendingData = null; alert("저장창을 열 수 없습니다."); }
        });
    }

    // STAGE4.36: 전체 백업은 파일 선택창을 띄우지 않고
    // 내 파일 > 다운로드 > 항행의자유 폴더에 자동 저장합니다.
    private void saveBackupDirect(String data) {
        if (data == null || data.isEmpty()) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            createSave("항행의자유_V3_전체백업.json", "application/json", data);
            return;
        }
        new Thread(() -> {
            Uri uri = null;
            try {
                String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(new Date());
                String fileName = "항행의자유_V3_전체백업_" + stamp + ".json";
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/항행의자유");
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
                uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("MediaStore insert failed");
                try (OutputStream out = getContentResolver().openOutputStream(uri, "w")) {
                    if (out == null) throw new Exception("openOutputStream failed");
                    out.write(data.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                ContentValues done = new ContentValues();
                done.put(MediaStore.MediaColumns.IS_PENDING, 0);
                getContentResolver().update(uri, done, null, null);
                alert("전체 백업 완료\n내 파일 > 다운로드 > 항행의자유\n" + fileName);
            } catch (Exception e) {
                try { if (uri != null) getContentResolver().delete(uri, null, null); } catch (Exception ignored) {}
                alert("자동 백업 저장 실패. 저장 위치 선택창으로 전환합니다.");
                runOnUiThread(() -> createSave("항행의자유_V3_전체백업.json", "application/json", data));
            }
        }, "V3-Backup").start();
    }


    // STAGE4.37: 자동 백업을 동기식으로 처리하고 JS에 결과를 되돌려줍니다.
    // 성공 여부를 화면 모달과 Toast에서 모두 확인할 수 있게 합니다.
    private String saveBackupDirectSync(String data) {
        if (data == null || data.isEmpty()) return "ERR|백업 데이터가 비어 있습니다.";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "ERR|Android 10 미만에서는 자동 저장을 사용할 수 없습니다.";
        Uri uri = null;
        try {
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(new Date());
            String fileName = "항행의자유_V3_전체백업_" + stamp + ".json";
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/항행의자유");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("MediaStore insert failed");
            try (OutputStream out = getContentResolver().openOutputStream(uri, "w")) {
                if (out == null) throw new Exception("openOutputStream failed");
                out.write(data.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            ContentValues done = new ContentValues();
            done.put(MediaStore.MediaColumns.IS_PENDING, 0);
            getContentResolver().update(uri, done, null, null);
            final String toastName = fileName;
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "전체 백업 완료: " + toastName, Toast.LENGTH_LONG).show());
            return "OK|" + fileName;
        } catch (Exception e) {
            try { if (uri != null) getContentResolver().delete(uri, null, null); } catch (Exception ignored) {}
            final String msg = e.getMessage() == null ? "자동 저장 실패" : e.getMessage();
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "전체 백업 실패: " + msg, Toast.LENGTH_LONG).show());
            return "ERR|" + msg;
        }
    }

    private void sendLegacyBackupToJs(final String name, final String text) {
        if (text == null) return;
        webView.post(() -> {
            webView.evaluateJavascript("legacyImportStart(" + JSONObject.quote(name == null ? "backup.json" : name) + ");", null);
            final int chunkSize = 12000;
            for (int i = 0; i < text.length(); i += chunkSize) {
                String chunk = text.substring(i, Math.min(text.length(), i + chunkSize));
                webView.evaluateJavascript("legacyImportChunk(" + JSONObject.quote(chunk) + ");", null);
            }
            webView.evaluateJavascript("legacyImportFinish();", null);
        });
    }

    // ---------------- Background music ----------------
    private SharedPreferences musicPrefs() {
        return getSharedPreferences(MUSIC_PREFS, MODE_PRIVATE);
    }

    private boolean musicEnabled() {
        return musicPrefs().getBoolean(MUSIC_ENABLED, false);
    }

    private int musicVolume() {
        int v = musicPrefs().getInt(MUSIC_VOLUME, 42);
        return Math.max(0, Math.min(100, v));
    }

    private String musicName() {
        try { String n=new JSONObject(MusicPlaybackService.snapshot(this)).optString("name", "");
            if(!n.isEmpty())return n; } catch(Exception ignored){}
        return musicPrefs().getString(MUSIC_NAME, "");
    }

    private void sendMusicCommand(String action) {
        try {
            Intent i = new Intent(this, MusicPlaybackService.class);
            i.setAction(action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
            else startService(i);
        } catch (Exception ignored) {}
    }

    private void requestMusicNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 4310);
                }
            } catch (Exception ignored) {}
        }
    }

    private void stopBackgroundMusic() {
        musicPrefs().edit().putBoolean(MUSIC_ENABLED, false).apply();
        sendMusicCommand(MusicPlaybackService.ACTION_STOP);
    }

    private void stopBackgroundMusicForAppExit() {
        // 앱 종료 시에는 선택한 곡/재생목록을 삭제하거나 OFF로 바꾸지 않습니다.
        sendMusicCommand(MusicPlaybackService.ACTION_APP_EXIT);
    }

    private void startBackgroundMusic() {
        if (!musicEnabled()) return;
        sendMusicCommand(MusicPlaybackService.ACTION_PLAY);
    }

    private void pauseBackgroundMusic() {
        sendMusicCommand(MusicPlaybackService.ACTION_PAUSE);
    }

    private void applyMusicVolume(int percent) {
        int p = Math.max(0, Math.min(100, percent));
        musicPrefs().edit().putInt(MUSIC_VOLUME, p).apply();
        try {
            Intent i = new Intent(this, MusicPlaybackService.class);
            i.setAction(MusicPlaybackService.ACTION_VOLUME);
            i.putExtra(MusicPlaybackService.EXTRA_VOLUME, p);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i);
            else startService(i);
        } catch (Exception ignored) {}
    }

    private boolean toggleBackgroundMusic() {
        boolean enabled = !musicEnabled();
        musicPrefs().edit().putBoolean(MUSIC_ENABLED, enabled).apply();
        if (enabled) startBackgroundMusic(); else pauseBackgroundMusic();
        return enabled;
    }

    private boolean isBlockedPickerPackage(String pkg) {
        if (pkg == null) return false;
        String p = pkg.toLowerCase(java.util.Locale.US);
        return p.contains("androidide") || p.contains("m4coding") || p.contains("aide") || p.contains("codeassist");
    }

    private boolean tryExplicitDocumentsUi(Intent base, int requestCode) {
        // ACTION_OPEN_DOCUMENT / ACTION_OPEN_DOCUMENT_TREE 의 실제 시스템 구현은
        // DocumentsUI의 PickActivity 입니다. 이 Activity를 명시적으로 호출하면
        // AndroidIDE/A-IDE가 파일선택 Intent를 가로채지 못합니다.
        String[] packages = new String[]{"com.google.android.documentsui", "com.android.documentsui"};
        for (String pkg : packages) {
            try {
                Intent x = new Intent(base);
                x.setComponent(new ComponentName(pkg, "com.android.documentsui.picker.PickActivity"));
                startActivityForResult(x, requestCode);
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private boolean tryResolvedSafePicker(Intent base, int requestCode) {
        try {
            List<ResolveInfo> list = getPackageManager().queryIntentActivities(base, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            if (list != null) {
                // Samsung 내 파일 / 시스템 DocumentsUI 우선, 코딩 IDE는 제외합니다.
                String[] preferred = new String[]{
                        "com.sec.android.app.myfiles",
                        "com.google.android.documentsui",
                        "com.android.documentsui"
                };
                for (String want : preferred) {
                    for (ResolveInfo ri : list) {
                        if (ri == null || ri.activityInfo == null) continue;
                        String pkg = ri.activityInfo.packageName;
                        if (!want.equals(pkg) || isBlockedPickerPackage(pkg)) continue;
                        Intent x = new Intent(base);
                        x.setComponent(new ComponentName(pkg, ri.activityInfo.name));
                        startActivityForResult(x, requestCode);
                        return true;
                    }
                }
                for (ResolveInfo ri : list) {
                    if (ri == null || ri.activityInfo == null) continue;
                    String pkg = ri.activityInfo.packageName;
                    if (isBlockedPickerPackage(pkg)) continue;
                    Intent x = new Intent(base);
                    x.setComponent(new ComponentName(pkg, ri.activityInfo.name));
                    startActivityForResult(x, requestCode);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean startPreferredFilesPicker(Intent base, int requestCode) {
        // 1) 시스템 DocumentsUI를 명시적으로 호출
        if (tryExplicitDocumentsUi(base, requestCode)) return true;
        // 2) 현재 기기에 등록된 안전한 파일선택기 중 A-IDE를 제외하고 선택