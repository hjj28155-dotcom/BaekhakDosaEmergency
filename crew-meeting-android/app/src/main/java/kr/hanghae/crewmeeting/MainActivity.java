package kr.hanghae.crewmeeting;

import android.app.Activity;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.view.WindowManager;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.DownloadListener;
import android.webkit.WebResourceRequest;
import android.webkit.ValueCallback;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.Toast;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class MainActivity extends Activity {
    private static final String ROOM_URL =
        "https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/crew-meeting.html?app_version=118";
    private static final int FILE_CHOOSER_CODE = 502;
    private static final int PICK_V3_APP = 503;
    private static final int PICK_V7_APP = 504;
    private static final String LINK_PREFS = "crew_meeting_installed_app_links_v1";
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // R13: allow the fullscreen activity to use the short display-cutout edge.
        // Some modern phones otherwise leave a tall unused black strip above the header.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            WindowManager.LayoutParams windowLayout = getWindow().getAttributes();
            windowLayout.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(windowLayout);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        web = new WebView(this);
        web.setVerticalScrollBarEnabled(false);
        setContentView(web);
        // Only two fixed roles can be opened. The web layer never supplies package names.
        web.addJavascriptInterface(new InstalledAppLauncher(), "CrewInstalledApps");
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return route(req.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return route(Uri.parse(url));
            }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                Toast.makeText(MainActivity.this, "보안 연결 오류: 연결을 차단했습니다.", Toast.LENGTH_LONG).show();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view,
                    ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    Intent chooser = params.createIntent();
                    startActivityForResult(chooser, FILE_CHOOSER_CODE);
                    return true;
                } catch (Exception e) {
                    fileCallback.onReceiveValue(null);
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "파일 선택기를 열 수 없습니다.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
        web.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (url == null || !url.startsWith("https://")) {
                Toast.makeText(this,"지원하지 않는 다운로드 주소입니다.",Toast.LENGTH_LONG).show();
                return;
            }
            Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.addCategory(Intent.CATEGORY_BROWSABLE);
            try { startActivity(view); }
            catch (Exception e) { Toast.makeText(this,"다운로드용 브라우저를 열 수 없습니다.",Toast.LENGTH_LONG).show(); }
        });
        loadCaptainDashboard();
    }

    /**
     * The captain-only research dashboard is bundled into this APK, so the existing
     * public main-branch Pages HTML cannot silently replace the candidate build.
     * The trusted base URL preserves the original Pages paths and approval room.
     * This does NOT install/update V3, V7 or the public V4 application.
     */
    private void loadCaptainDashboard() {
        try (InputStream input = getAssets().open("captain_home_v114.html");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                if (output.size() + length > 1048576)
                    throw new java.io.IOException("Invalid embedded dashboard size");
                output.write(buffer, 0, length);
            }
            String html = new String(output.toByteArray(), StandardCharsets.UTF_8);
            if (!html.contains("id=\"beforeRoomGrid\"")
                    || !html.contains("id=\"homeApprovalBtn\"")
                    || !html.contains("id=\"appCommandDeck\"")
                    || !html.contains("id=\"homeV3RoomsPanel\"")
                    || !html.contains("data-v3-room-card=\"v3-4\"")
                    || !html.contains("id=\"freedomAnalysisPanel\"")
                    || !html.contains("id=\"freedomFirstFive\"")
                    || !html.contains("id=\"freedomSecondFive\"")
                    || !html.contains("id=\"freedomGoCumulativeDb\"")
                    || !html.contains("id=\"freedomRunRound\"")
                    || !html.contains("id=\"freedomFirstList\"")
                    || !html.contains("id=\"freedomSecondList\""))
                throw new java.io.IOException("Required captain dashboard modules missing");
            web.loadDataWithBaseURL(ROOM_URL, html, "text/html", "UTF-8", ROOM_URL);
        } catch (java.io.IOException e) {
            Toast.makeText(this, "중앙통제실 홈화면을 검증할 수 없습니다. 기존 설치본을 보존하세요.",
                Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Manual, per-device launch registration. This does NOT embed/clone the V3/V7
     * engine or connect their ChatGPT conversations. Only native launcher activities
     * explicitly chosen by the captain are stored.
     */
    public final class InstalledAppLauncher {
        @JavascriptInterface public void openInstalled(String role, boolean chooseAgain) {
            if (!"v3".equals(role) && !"v7".equals(role)) return;
            runOnUiThread(() -> openSelectedApp(role, chooseAgain));
        }
    }

    private void openSelectedApp(String role, boolean chooseAgain) {
        SharedPreferences prefs = getSharedPreferences(LINK_PREFS, MODE_PRIVATE);
        String registered = prefs.getString(role, null);
        if (chooseAgain || registered == null) {
            chooseInstalledApp(role);
            return;
        }
        ComponentName target = ComponentName.unflattenFromString(registered);
        if (target == null) {
            Toast.makeText(this, "앱 선택 기록을 확인할 수 없습니다. 다시 선택해 주세요.", Toast.LENGTH_LONG).show();
            chooseInstalledApp(role);
            return;
        }
        try {
            Intent launch = new Intent(Intent.ACTION_MAIN);
            launch.addCategory(Intent.CATEGORY_LAUNCHER);
            launch.setComponent(target);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "선택한 앱을 열 수 없습니다. 현재 설치된 V3/V7 앱을 다시 선택해 주세요.", Toast.LENGTH_LONG).show();
            chooseInstalledApp(role);
        }
    }

    private void chooseInstalledApp(String role) {
        int requestCode = "v3".equals(role) ? PICK_V3_APP : PICK_V7_APP;
        Intent launchers = new Intent(Intent.ACTION_MAIN);
        launchers.addCategory(Intent.CATEGORY_LAUNCHER);
        Intent picker = new Intent(Intent.ACTION_PICK_ACTIVITY);
        picker.putExtra(Intent.EXTRA_INTENT, launchers);
        picker.putExtra(Intent.EXTRA_TITLE, "v3".equals(role) ? "설치된 V3 항해사앱 선택" : "설치된 V7 중앙통제실 선택");
        try {
            startActivityForResult(picker, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "설치 앱 선택창을 열 수 없습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean route(Uri uri) {
        if (uri == null) return true;
        if ("https".equalsIgnoreCase(uri.getScheme())
            && "hjj28155-dotcom.github.io".equalsIgnoreCase(uri.getHost())
            && ("/BaekhakDosaEmergency/crew-meeting.html".equals(uri.getPath())
              || "/BaekhakDosaEmergency/hanghae-distribution-approval-room.html".equals(uri.getPath()))) return false;
        if (!"https".equalsIgnoreCase(uri.getScheme())) return true;
        try {
            Intent external = new Intent(Intent.ACTION_VIEW, uri);
            external.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(external);
        } catch (Exception e) {
            Toast.makeText(this, "외부 링크를 열 수 없습니다.", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override protected void onActivityResult(int reqCode,int resultCode,Intent data) {
        super.onActivityResult(reqCode,resultCode,data);
        if (reqCode == PICK_V3_APP || reqCode == PICK_V7_APP) {
            if (resultCode == RESULT_OK && data != null && data.getComponent() != null) {
                String role = reqCode == PICK_V3_APP ? "v3" : "v7";
                ComponentName component = data.getComponent();
                if (!getPackageName().equals(component.getPackageName())) {
                    getSharedPreferences(LINK_PREFS, MODE_PRIVATE).edit()
                        .putString(role, component.flattenToString()).apply();
                    openSelectedApp(role, false);
                } else {
                    Toast.makeText(this, "공동회의 자신은 선택할 수 없습니다.", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_OK) {
                Toast.makeText(this, "선택한 앱의 실행정보를 확인할 수 없습니다.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if(reqCode==FILE_CHOOSER_CODE && fileCallback!=null) {
            Uri[] result=WebChromeClient.FileChooserParams.parseResult(resultCode,data);
            fileCallback.onReceiveValue(result);
            fileCallback=null;
        }
    }

    @Override public void onBackPressed() {
        if(web!=null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if(fileCallback!=null){fileCallback.onReceiveValue(null);fileCallback=null;}
        if(web!=null){web.destroy();web=null;}
        super.onDestroy();
    }
}
