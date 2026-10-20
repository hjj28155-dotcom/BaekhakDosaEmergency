package kr.hanghae.moveicon.fresh;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {
  private static final int AUDIO=401, VIDEO=402;
  private LinearLayout content;
  private int dp(float n){ return (int)(getResources().getDisplayMetrics().density*n+.5f); }
  private void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
  private GradientDrawable bg(int color,int stroke,int radius){
    GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));
    d.setStroke(dp(1),stroke);return d;
  }
  private TextView label(String s,int size,int color,boolean bold){
    TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
    if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
    t.setGravity(Gravity.CENTER);return t;
  }
  private Button button(String s,Runnable action){
    Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextSize(16);
    b.setTextColor(Color.WHITE);b.setBackground(bg(0xff102c49,0xff977646,16));
    b.setOnClickListener(v->action.run());return b;
  }
  private void open(String url){
    try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
    catch(Exception e){toast("연결 가능한 앱이 없습니다.");}
  }
  private void select(int type){
    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
    i.addCategory(Intent.CATEGORY_OPENABLE);
    i.setType(type==AUDIO?"audio/*":"video/*");
    startActivityForResult(i,type);
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(result!=RESULT_OK||data==null||data.getData()==null)return;
    if(request!=AUDIO&&request!=VIDEO)return;
    Uri uri=data.getData();
    String directory=request==AUDIO?"MP3":"MP4";
    File dir=new File(getExternalFilesDir(null),directory);
    if(!dir.exists()&&!dir.mkdirs()){toast("보관 폴더를 만들지 못했습니다.");return;}
    String filename=System.currentTimeMillis()+(request==AUDIO?".mp3":".mp4");
    File dest=new File(dir,filename);
    try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(dest)){
      if(in==null)throw new Exception("파일을 읽을 수 없습니다.");
      byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
      toast(directory+" 보관 완료: "+filename);
    }catch(Exception ex){dest.delete();toast("저장 실패: "+ex.getMessage());}
  }
  private void showMedia(String kind){
    File dir=new File(getExternalFilesDir(null),kind);File[] files=dir.listFiles();
    content.removeAllViews();
    title(kind+" 보관함");
    Button add=button("+ "+kind+" 파일 추가",()->select(kind.equals("MP3")?AUDIO:VIDEO));
    content.addView(add,new LinearLayout.LayoutParams(-1,dp(57)));
    if(files==null||files.length==0){content.addView(label("보관한 파일이 없습니다.",16,0xffc5d9ee,false));return;}
    for(File f:files){
      Button play=button("▶ "+f.getName(),()->{
        try {
          // App-owned file plays in the built-in player screen.
          showPlayer(f,kind.equals("MP4"));
        }catch(Exception ex){toast("재생 실패: "+ex.getMessage());}
      });
      LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(55));p.topMargin=dp(8);
      content.addView(play,p);
    }
    Button back=button("← 홈",this::home);content.addView(back,new LinearLayout.LayoutParams(-1,dp(54)));
  }
  private android.media.MediaPlayer audioPlayer;
  private void showPlayer(File file,boolean video){
    content.removeAllViews();title(file.getName());
    if(video){
      android.widget.VideoView vv=new android.widget.VideoView(this);
      content.addView(vv,new LinearLayout.LayoutParams(-1,dp(270)));
      vv.setVideoPath(file.getAbsolutePath());
      vv.setMediaController(new android.widget.MediaController(this));
      vv.start();
    }else{
      try{
        if(audioPlayer!=null){audioPlayer.release();audioPlayer=null;}
        audioPlayer=new android.media.MediaPlayer();
        audioPlayer.setDataSource(file.getAbsolutePath());audioPlayer.prepare();audioPlayer.start();
        content.addView(label("♪ 재생 중",22,0xffffd789,true),new LinearLayout.LayoutParams(-1,dp(140)));
        Button pause=button("재생 / 일시정지",()->{if(audioPlayer!=null){if(audioPlayer.isPlaying())audioPlayer.pause();else audioPlayer.start();}});
        content.addView(pause,new LinearLayout.LayoutParams(-1,dp(55)));
      }catch(Exception ex){toast("오디오 재생 실패");}
    }
    Button back=button("← 보관함",()->{if(audioPlayer!=null){audioPlayer.release();audioPlayer=null;}showMedia(video?"MP4":"MP3");});
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(55));p.topMargin=dp(18);content.addView(back,p);
  }
  private void title(String t){
    TextView h=label("⚓ "+t,25,0xffffd789,true);h.setPadding(0,dp(12),0,dp(18));
    content.addView(h,new LinearLayout.LayoutParams(-1,dp(85)));
  }
  private void launchOverlay(){
    if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){
      toast("다른 앱 위에 표시 권한을 허용하세요.");
      startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
      return;
    }
    if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
      requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},50);
    try{
      Intent intent=new Intent(this,OverlayService.class);
      if(Build.VERSION.SDK_INT>=26)startForegroundService(intent);else startService(intent);
      toast("닻 이동아이콘을 표시했습니다.");
    }catch(Exception e){toast("닻 표시 실패: "+e.getMessage());}
  }
  private void home(){
    if(audioPlayer!=null){audioPlayer.release();audioPlayer=null;}
    content.removeAllViews();title("항행의자유 이동아이콘");
    content.addView(label("별빛이 머무는 밤바다 · V1.0.84",15,0xffd3e4f5,false),
      new LinearLayout.LayoutParams(-1,dp(46)));
    String[] names={"항행의자유","MP3","MP4","FM","AM","유튜브","실시간TV","설정"};
    for(int i=0;i<4;i++){
      LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
      for(int j=0;j<2;j++){
        final int index=i*2+j;
        Button b=button(names[index],()->{
          switch(index){
            case 0:open("https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/");break;
            case 1:showMedia("MP3");break;
            case 2:showMedia("MP4");break;
            case 3:open("https://radio.kbs.co.kr/");break;
            case 4:open("https://www.mbc.co.kr/");break;
            case 5:open("https://www.youtube.com/");break;
            case 6:open("https://www.kbs.co.kr/");break;
            case 7:settingsPage();break;
          }
        });
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(72),1);p.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(b,p);
      }
      content.addView(row,new LinearLayout.LayoutParams(-1,dp(80)));
    }
    Button custom=button("＋ 아이콘 추가 · 이동 · 편집 · 삭제",()->ShortcutBoard.show(this,content,this::home));
    LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(56));cp.topMargin=dp(12);content.addView(custom,cp);
    Button anchor=button("⚓ 화면 위 닻 아이콘 켜기",this::launchOverlay);
    LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(-1,dp(56));a.topMargin=dp(15);content.addView(anchor,a);
    Button update=button("업데이트",()->open("https://github.com/hjj28155-dotcom/BaekhakDosaEmergency/actions"));
    LinearLayout.LayoutParams u=new LinearLayout.LayoutParams(-1,dp(55));u.topMargin=dp(9);content.addView(update,u);
    Button end=button("종료",this::finish);LinearLayout.LayoutParams e=new LinearLayout.LayoutParams(-1,dp(55));e.topMargin=dp(8);content.addView(end,e);
  }
  private void settingsPage(){
    content.removeAllViews();title("설정");
    Button floatButton=button("닻 이동아이콘 켜기",this::launchOverlay);
    content.addView(floatButton,new LinearLayout.LayoutParams(-1,dp(60)));
    Button stop=button("닻 이동아이콘 끄기",()->{stopService(new Intent(this,OverlayService.class));toast("닻 아이콘을 닫았습니다.");});
    content.addView(stop,new LinearLayout.LayoutParams(-1,dp(60)));
    Button back=button("← 홈",this::home);content.addView(back,new LinearLayout.LayoutParams(-1,dp(60)));
  }
  @Override protected void onCreate(Bundle state){
    super.onCreate(state);
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
    root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff031329,0xff0a3456,0xff02101e}));
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
    content=new LinearLayout(this);content.setPadding(dp(18),dp(20),dp(18),dp(30));
    content.setOrientation(LinearLayout.VERTICAL);scroll.addView(content);setContentView(root);home();
  }
  @Override protected void onDestroy(){if(audioPlayer!=null){audioPlayer.release();audioPlayer=null;}super.onDestroy();}
}
