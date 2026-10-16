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
    private void loadPackagedV6Home() {
        try (InputStream in=getAssets().open("v6_fallback.html");
             ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[8192];
            int n;
            while ((n=in.read(buffer))!=-1) out.write(buffer,0,n);
            fallbackShown=true;
            String html=new String(out.toByteArray(),StandardCharsets.UTF_8);
            web.loadDataWithBaseURL(HOME,html,"text/html","UTF-8",HOME);
            // Bundled HTML is loaded as text/html to prevent a raw-source display.
        } catch(Exception e) {
            Toast.makeText(this,"V6 화면을 불러오지 못했습니다.",Toast.LENGTH_LONG).show();
        }
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=PICKER||uploadCallback==null)return;
        uploadCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data));
        uploadCallback=null;
    }
    @Override public void onBackPressed() {
        if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();
    }
    @Override protected void onDestroy(){
        if(uploadCallback!=null){uploadCallback.onReceiveValue(null);uploadCallback=null;}
        if(web!=null){web.destroy();web=null;}
        super.onDestroy();
    }
}
