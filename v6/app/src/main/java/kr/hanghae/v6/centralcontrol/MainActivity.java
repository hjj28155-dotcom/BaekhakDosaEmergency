package kr.hanghae.v6.centralcontrol;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.content.Intent;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import android.webkit.JavascriptInterface;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.URLUtil;
import android.widget.Toast;
import android.content.Context;
import android.view.ViewGroup;

public final class MainActivity extends Activity {
    private static final String HOME = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/v6-dashboard";
    private static final int PICKER = 611;
    private WebView web;
    private boolean fallbackShown=false;
    private static final String LOCAL_UI_CACHE="v6_screen_cache.html";
    private static final int MAX_UI_BYTES=1024*1024;
    private static final Pattern UI_VERSION=Pattern.compile("CURRENT_VERSION_CODE\\s*=\\s*(\\d+)");
    private volatile int displayedUiVersion=0;
    private android.webkit.ValueCallback<Uri[]> uploadCallback;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web, new ViewGroup.LayoutParams(-1,-1));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setLoadsImagesAutomatically(true);
        s.setMediaPlaybackRequiresUserGesture(true);
        CookieManager.getInstance().setAcceptCookie(true);
        web.addJavascriptInterface(new NativeUpdateBridge(),"V6NativeUpdater");
        web.addJavascriptInterface(new NativeExitBridge(),"V6NativeApp");
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("https".equalsIgnoreCase(uri.getScheme()) && "jyqkohpeowkhuaohlezc.supabase.co".equalsIgnoreCase(uri.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW,uri)); }
                catch(Exception e){ Toast.makeText(MainActivity.this,"연결을 열 수 없습니다.",Toast.LENGTH_LONG).show(); }
                return true;
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame() && !fallbackShown) loadPackagedV6Home();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, android.webkit.ValueCallback<Uri[]> callback, FileChooserParams params) {
                if(uploadCallback!=null) uploadCallback.onReceiveValue(null);
                uploadCallback=callback;
                try {
                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(i,PICKER);
                    return true;
                }
                catch(Exception e){uploadCallback=null;callback.onReceiveValue(null);return false;}
            }
        });
        web.setDownloadListener((url,userAgent,disposition,mimetype,length)->{
            try {
                Uri u=Uri.parse(url);
                if (!"https".equalsIgnoreCase(u.getScheme())) throw new IllegalArgumentException("HTTPS required");
                DownloadManager.Request r=new DownloadManager.Request(u);
                r.setMimeType(mimetype);
                r.addRequestHeader("User-Agent",userAgent);
                String cookie=CookieManager.getInstance().getCookie(url);
                if(cookie!=null)r.addRequestHeader("Cookie",cookie);
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url,disposition,mimetype));
                ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(r);
                Toast.makeText(MainActivity.this,"다운로드 폴더에 저장 중",Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
                catch(Exception ignored){Toast.makeText(MainActivity.this,"파일 다운로드 실패",Toast.LENGTH_LONG).show();}
            }
        });
        loadPackagedV6Home();
    }
    // UI updates are HTTPS-only and may never install or replace APK code.
    // Save only reviewed HTML with an increasing version number, never execute network requests on UI thread.
    private static int htmlVersion(String html) {
        if (html == null || html.length() > MAX_UI_BYTES || !html.trim().toLowerCase(java.util.Locale.ROOT).startsWith("<!doctype html>")) return 0;
        if (!html.contains("id=\"phone\"") || !html.contains("window.V6=V6") || !html.contains("항행의자유 V6")) return 0;
        Matcher m = UI_VERSION.matcher(html);
        if(!m.find())return 0;
        try {return Integer.parseInt(m.group(1));}catch(Exception e){return 0;}
    }
    private static byte[] readBounded(InputStream stream) throws Exception {
        try(ByteArrayOutputStream bytes=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[8192];
            int n,total=0;
            while((n=stream.read(buffer))!=-1){
                total+=n;
                if(total>MAX_UI_BYTES)throw new IllegalArgumentException("UI document exceeds 1MB");
                bytes.write(buffer,0,n);
            }
            return bytes.toByteArray();
        }
    }
    private void loadDocument(String html,int version) {
        if(web==null)return;
        if(version<displayedUiVersion)return; // Never replace a newer UI with an older cached document.
        displayedUiVersion=version;
        web.loadDataWithBaseURL(HOME,html,"text/html","UTF-8",HOME);
    }
    private void loadPackagedV6Home() {
        try(InputStream in=getAssets().open("v6_fallback.html")) {
            String local=new String(readBounded(in),StandardCharsets.UTF_8);
            int localVersion=htmlVersion(local);
            if(localVersion==0)throw new IllegalStateException("Invalid bundled V6 home");
            File cache=new File(getFilesDir(),LOCAL_UI_CACHE);
            if(cache.isFile() && cache.length()>0 && cache.length()<=MAX_UI_BYTES) {
                try(InputStream cached=new java.io.FileInputStream(cache)){
                    String remoteCached=new String(readBounded(cached),StandardCharsets.UTF_8);
                    int cacheVersion=htmlVersion(remoteCached);
                    if(cacheVersion>localVersion){local=remoteCached;localVersion=cacheVersion;}
                }catch(Exception ignored){}
            }
            fallbackShown=true;
            loadDocument(local,localVersion);
            fetchRemoteUi(false); // Future server-published screen changes update the installed app automatically.
        }catch(Exception e){
            Toast.makeText(this,"V6 화면을 불러오지 못했습니다.",Toast.LENGTH_LONG).show();
        }
    }
    private final class NativeExitBridge {
        @JavascriptInterface public void exitApp() {
            runOnUiThread(() -> {
                if(!isFinishing())finish();
            });
        }
    }
    private final class NativeUpdateBridge {
        @JavascriptInterface public void refreshScreen() {
            runOnUiThread(() -> fetchRemoteUi(true));
        }
    }
    private void fetchRemoteUi(boolean userRequested) {
        new Thread(() -> {
            HttpURLConnection connection=null;
            try {
                URL url=new URL(HOME);
                connection=(HttpURLConnection)url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                connection.setRequestProperty("Accept","text/html");
                connection.setRequestProperty("Cache-Control","no-cache");
                if(connection.getResponseCode()!=200)throw new IllegalStateException("HTTP "+connection.getResponseCode());
                String type=connection.getHeaderField("Content-Type");
                if(type==null||!type.toLowerCase(java.util.Locale.ROOT).contains("text/html"))throw new IllegalStateException("서버가 HTML을 반환하지 않았습니다");
                String html;
                try(InputStream data=connection.getInputStream()){
                    html=new String(readBounded(data),StandardCharsets.UTF_8);
                }
                int version=htmlVersion(html);
                if(version<=0)throw new IllegalStateException("화면 문서 검증 실패");
                if(version<=displayedUiVersion){
                    if(userRequested)runOnUiThread(()->Toast.makeText(this,"최신 V6 화면입니다.",Toast.LENGTH_SHORT).show());
                    return;
                }
                File output=new File(getFilesDir(),LOCAL_UI_CACHE);
                File temp=new File(getFilesDir(),LOCAL_UI_CACHE+".tmp");
                try(FileOutputStream stream=new FileOutputStream(temp)){
                    stream.write(html.getBytes(StandardCharsets.UTF_8));
                    stream.flush();
                }
                if(!temp.renameTo(output)){
                    try(FileOutputStream stream=new FileOutputStream(output)){stream.write(html.getBytes(StandardCharsets.UTF_8));}
                    temp.delete();
                }
                runOnUiThread(()->{
                    if(web!=null&&version>displayedUiVersion){
                        loadDocument(html,version);
                        Toast.makeText(this,"V6 화면 업데이트 완료 · "+version,Toast.LENGTH_SHORT).show();
                    }
                });
            }catch(Exception e) {
                if(userRequested)runOnUiThread(()->Toast.makeText(this,"화면 업데이트 실패: "+e.getMessage(),Toast.LENGTH_LONG).show());
            }finally{if(connection!=null)connection.disconnect();}
        },"V6ScreenUpdater").start();
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=PICKER||uploadCallback==null)return;
        uploadCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data));
        uploadCallback=null;
    }
    @Override public void onBackPressed() {
        if(web==null){super.onBackPressed();return;}
        web.evaluateJavascript("Boolean(window.V6 && window.V6.canGoBack && window.V6.canGoBack())", result -> {
            if("true".equals(result)){
                if(web!=null)web.evaluateJavascript("window.V6.back()",null);
            } else if(web!=null && web.canGoBack()){
                web.goBack();
            } else {
                MainActivity.super.onBackPressed();
            }
        });
    }
    @Override protected void onDestroy(){
        if(uploadCallback!=null){uploadCallback.onReceiveValue(null);uploadCallback=null;}
        if(web!=null){web.destroy();web=null;}
        super.onDestroy();
    }
}
