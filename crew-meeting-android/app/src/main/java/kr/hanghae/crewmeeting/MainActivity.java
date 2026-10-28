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
import android.webkit.WebResourceResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import android.webkit.ValueCallback;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.Toast;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MainActivity extends Activity {
    private static final String ROOM_URL =
        "https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/crew-meeting.html?app_version=130";
    private static final int FILE_CHOOSER_CODE = 502;
    private static final int PICK_V3_APP = 503;
    private static final int PICK_V7_APP = 504;
    private static final int SAVE_V4_PNG_CODE = 505;
    private static final String LINK_PREFS = "crew_meeting_installed_app_links_v1";
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private volatile byte[] pendingV4Png;

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
        web.addJavascriptInterface(new V4PngSaver(), "CrewPngSaver");
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
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                Uri uri=req.getUrl();
                if(!"https".equals(uri.getScheme()) || !"hjj28155-dotcom.github.io".equals(uri.getHost()))return null;
                String p=uri.getPath();
                String name=null,mime=null;
                if("/BaekhakDosaEmergency/crew-db1-1-1247.json".equals(p)){
                    name="db1_rounds_1_1247.json";mime="application/json";
                }else if("/BaekhakDosaEmergency/crew-captain-engine-r46.html".equals(p)){
                    name="captain_engine_r46.html";mime="text/html";
                }else if("/BaekhakDosaEmergency/crew-liaison-blind-r1.mjs".equals(p)){
                    name="liaison-blind-engine.mjs";mime="text/javascript";
                }else if("/BaekhakDosaEmergency/crew-liaison-baseline-r1.mjs".equals(p)){
                    name="v7-engine100-baseline.mjs";mime="text/javascript";
                }else if("/BaekhakDosaEmergency/crew-cumulative-after.css".equals(p)){
                    name="cumulative_after_only.css";mime="text/css";
                }else if("/BaekhakDosaEmergency/crew-cumulative-after.js".equals(p)){
                    name="cumulative_after_only.js";mime="text/javascript";
                }else if("/BaekhakDosaEmergency/crew-v4-png-report.js".equals(p)){
                    name="v4_png_report.js";mime="text/javascript";
                }else if("/BaekhakDosaEmergency/crew-official-draws.json".equals(p)){
                    name="official_draws_verified_17.json";mime="application/json";
                }
                if(name==null)return null;
                try{
                    return new WebResourceResponse(mime,"UTF-8",getAssets().open(name));
                }catch(java.io.IOException ex){
                    return new WebResourceResponse("text/plain","UTF-8",
                        new java.io.ByteArrayInputStream("BUNDLED_ASSET_MISSING".getBytes(StandardCharsets.UTF_8)));
                }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                if(!req.isForMainFrame())return false; // allow only WebView subresources and bundled R46 iframe
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
                    || !html.contains("id=\"freedomSecondList\"")
                    || !html.contains("id=\"freedomResetActive\"")
                    || !html.contains("id=\"freedomPngPreview\"")
                    || !html.contains("id=\"freedomSavePng\"")
                    || !html.contains("crew-v4-png-report.js"))
                throw new java.io.IOException("Required captain dashboard modules missing");
            // The six-line original DB-1 is an APK asset, not a remote website or a material-room cache.
            // Fail closed if the snapshot is incomplete; never silently substitute remote data.
            try(InputStream dbStream=getAssets().open("db1_rounds_1_1247.json")){
                ByteArrayOutputStream dbBytes=new ByteArrayOutputStream();
                byte[] dbBuf=new byte[8192];int dbLen;
                while((dbLen=dbStream.read(dbBuf))!=-1){
                    if(dbBytes.size()+dbLen>2000000)throw new java.io.IOException("DB1_ASSET_TOO_LARGE");
                    dbBytes.write(dbBuf,0,dbLen);
                }
                JSONObject snapshot=new JSONObject(new String(dbBytes.toByteArray(),StandardCharsets.UTF_8));
                JSONArray rows=snapshot.getJSONArray("rows");
                if(rows.length()!=1247 || snapshot.getInt("count")!=1247)throw new java.io.IOException("DB1_COUNT_MISMATCH");
                for(int i=0;i<1247;i++){
                    JSONObject item=rows.getJSONObject(i);
                    String raw=item.getString("source_text");
                    if(item.getInt("round_no")!=i+1 || raw.split("\\n",-1).length!=6
                       || !item.getString("source_sha256").matches("[a-f0-9]{64}"))
                        throw new java.io.IOException("DB1_ROUND_INVALID_"+(i+1));
                    try{
                        byte[] digest=MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
                        StringBuilder hex=new StringBuilder(64);
                        for(byte b:digest)hex.append(String.format(java.util.Locale.ROOT,"%02x",b & 255));
                        if(!hex.toString().equals(item.getString("source_sha256")))
                            throw new java.io.IOException("DB1_SHA256_INVALID_"+(i+1));
                    }catch(NoSuchAlgorithmException ex){throw new java.io.IOException("SHA256_UNAVAILABLE",ex);}
                }
            }catch(org.json.JSONException ex){throw new java.io.IOException("DB1_PARSE_INVALID",ex);}
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


    /**
     * Save only an in-app V4 report PNG. No arbitrary paths, URLs, filenames,
     * HTML, or other MIME types are accepted. The source research is unchanged.
     */
    public final class V4PngSaver {
        @JavascriptInterface public String savePng(String dataUrl, String fileName) {
            if (dataUrl == null || !dataUrl.startsWith("data:image/png;base64,")
                || dataUrl.length() > 4000000
                || fileName == null || !fileName.matches("HANGHAE_V4_R(?:[0-9]{1,4}|UNKNOWN)_R46_R1\\.png"))
                return "INVALID_PNG_REQUEST";
            byte[] bytes;
            try { bytes=android.util.Base64.decode(dataUrl.substring(22),android.util.Base64.DEFAULT); }
            catch(Exception ex){return "INVALID_BASE64";}
            if(bytes.length<100 || bytes.length>3000000
               || (bytes[0]&255)!=137 || bytes[1]!=80 || bytes[2]!=78 || bytes[3]!=71
               || bytes[4]!=13 || bytes[5]!=10 || bytes[6]!=26 || bytes[7]!=10)
                return "INVALID_PNG_BYTES";
            if(android.os.Build.VERSION.SDK_INT<29){
                pendingV4Png=bytes;
                runOnUiThread(() -> {
                    Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_TITLE,fileName);
                    try{startActivityForResult(intent,SAVE_V4_PNG_CODE);}
                    catch(Exception ex){
                        pendingV4Png=null;
                        Toast.makeText(MainActivity.this,"PNG 저장 위치를 선택할 수 없습니다.",Toast.LENGTH_LONG).show();
                    }
                });
                return "PICKER";
            }
            Uri out=null;
            try{
                android.content.ContentValues values=new android.content.ContentValues();
                values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,fileName);
                values.put(android.provider.MediaStore.Images.Media.MIME_TYPE,"image/png");
                values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_PICTURES+"/항행의자유");
                values.put(android.provider.MediaStore.Images.Media.IS_PENDING,1);
                out=getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                if(out==null)return "STORAGE_UNAVAILABLE";
                try(java.io.OutputStream stream=getContentResolver().openOutputStream(out)){
                    if(stream==null)throw new java.io.IOException("NO_OUTPUT_STREAM");
                    stream.write(bytes);
                }
                android.content.ContentValues complete=new android.content.ContentValues();
                complete.put(android.provider.MediaStore.Images.Media.IS_PENDING,0);
                getContentResolver().update(out,complete,null,null);
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "V4 PNG 저장 완료 · 사진/Pictures/항행의자유",Toast.LENGTH_LONG).show());
                return "SAVED";
            }catch(Exception ex){
                if(out!=null)try{getContentResolver().delete(out,null,null);}catch(Exception ignored){}
                return "STORAGE_WRITE_FAILED";
            }
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
              || "/BaekhakDosaEmergency/hanghae-distribution-approval-room.html".equals(uri.getPath())
              || "/BaekhakDosaEmergency/crew-captain-engine-r46.html".equals(uri.getPath()))) return false;
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
        if(reqCode==SAVE_V4_PNG_CODE){
            byte[] bytes=pendingV4Png;pendingV4Png=null;
            if(resultCode==RESULT_OK && data!=null && data.getData()!=null && bytes!=null){
                try(java.io.OutputStream stream=getContentResolver().openOutputStream(data.getData())){
                    if(stream==null)throw new java.io.IOException("NO_OUTPUT_STREAM");
                    stream.write(bytes);
                    Toast.makeText(this,"V4 PNG 저장 완료",Toast.LENGTH_LONG).show();
                }catch(Exception ex){
                    Toast.makeText(this,"V4 PNG 저장 실패",Toast.LENGTH_LONG).show();
                }
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
        pendingV4Png=null;
        if(fileCallback!=null){fileCallback.onReceiveValue(null);fileCallback=null;}
        if(web!=null){web.destroy();web=null;}
        super.onDestroy();
    }
}
