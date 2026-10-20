package kr.hanghae.moveicon.fresh;
import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.util.*;

/** Playback is fullscreen only when the user explicitly opens an MP4 file. */
public class VideoPlayerActivity extends Activity {
 private VideoView video;
 private LinearLayout controls;
 private TextView title;
 private SeekBar seek;
 private HandlerTicker ticker;
 private final ArrayList<File> playlist=new ArrayList<>();
 private int index=0,repeat=0;
 private float speed=1f;
 private boolean fullscreen=false;
 private android.media.MediaPlayer current;
 private String folderPath;
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private Button button(String label,Runnable click){
  Button b=new Button(this);b.setText(label);b.setTextColor(0xffffdda0);b.setAllCaps(false);
  b.setTextSize(12);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff133451));
  b.setOnClickListener(v->click.run());return b;
 }
 private void message(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);
  folderPath=getIntent().getStringExtra("folder");
  String chosen=getIntent().getStringExtra("file");
  if(folderPath==null||chosen==null){finish();return;}
  File folder=new File(folderPath);
  File[] list=folder.listFiles(f->f.isFile()&&(f.getName().toLowerCase(Locale.ROOT).endsWith(".mp4")||
   f.getName().toLowerCase(Locale.ROOT).endsWith(".mkv")||f.getName().toLowerCase(Locale.ROOT).endsWith(".webm")));
  if(list!=null){Arrays.sort(list,(a,c)->a.getName().compareToIgnoreCase(c.getName()));playlist.addAll(Arrays.asList(list));}
  for(int i=0;i<playlist.size();i++)if(playlist.get(i).getAbsolutePath().equals(chosen))index=i;
  if(playlist.isEmpty()){finish();return;}
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  root.setBackgroundColor(0xff07182d);
  title=new TextView(this);title.setTextColor(0xffffdb9a);title.setTextSize(17);title.setPadding(dp(12),dp(12),dp(12),dp(8));
  root.addView(title);
  video=new VideoView(this);root.addView(video,new LinearLayout.LayoutParams(-1,0,1f));
  seek=new SeekBar(this);seek.setMax(1000);root.addView(seek);
  seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
   public void onStartTrackingTouch(SeekBar s){}
   public void onStopTrackingTouch(SeekBar s){if(video.getDuration()>0)video.seekTo((int)((long)video.getDuration()*s.getProgress()/1000));}
   public void onProgressChanged(SeekBar s,int v,boolean user){}
  });
  controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);root.addView(controls);
  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);controls.addView(row);
  add(row,"⏮ 이전",()->skip(-1));add(row,"⏯ 재생/정지",()->{if(video.isPlaying())video.pause();else video.start();});
  add(row,"다음 ⏭",()->skip(1));add(row,"⛶ 전체",this::toggleFullscreen);
  LinearLayout row2=new LinearLayout(this);row2.setGravity(Gravity.CENTER);controls.addView(row2);
  add(row2,"🔁 반복",()->{repeat=(repeat+1)%3;message(new String[]{"반복 해제","한 영상 반복","전체 반복"}[repeat]);});
  add(row2,"속도",()->new AlertDialog.Builder(this).setTitle("재생 속도").setItems(
   new String[]{"0.5x","0.75x","1.0x","1.25x","1.5x","2.0x"},(d,w)->{
    speed=new float[]{.5f,.75f,1f,1.25f,1.5f,2f}[w];applySpeed();}).show());
  add(row2,"화면비",()->new AlertDialog.Builder(this).setTitle("화면 비율").setItems(
   new String[]{"원본 비율","화면 채우기"},(d,w)->{
    video.setLayoutParams(new LinearLayout.LayoutParams(-1,0,1f));
    video.requestLayout();
    if(w==1)message("화면 채우기는 기기별 지원에 따라 달라집니다.");
   }).show());
  add(row2,"⋮ 더보기",this::more);
  setContentView(root);
  video.setOnPreparedListener(mp->{current=mp;applySpeed();});
  video.setOnCompletionListener(mp->{if(repeat==1)play();else if(index<playlist.size()-1)skip(1);else if(repeat==2){index=0;play();}});
  ticker=new HandlerTicker();ticker.start();
  play();
 }
 private void add(LinearLayout row,String text,Runnable action){
  Button b=button(text,action);row.addView(b,new LinearLayout.LayoutParams(0,dp(47),1f));
 }
 private void play(){
  File f=playlist.get(index);title.setText("🎬 "+f.getName()+"   "+(index+1)+"/"+playlist.size());
  video.setVideoPath(f.getAbsolutePath());video.start();
 }
 private void skip(int delta){
  int next=index+delta;
  if(next<0||next>=playlist.size()){if(repeat==2)next=(next+playlist.size())%playlist.size();else return;}
  index=next;play();
 }
 private void applySpeed(){
  if(current!=null&&android.os.Build.VERSION.SDK_INT>=23)try{
   current.setPlaybackParams(current.getPlaybackParams().setSpeed(speed));
  }catch(Exception e){message("이 영상은 속도 변경을 지원하지 않습니다.");}
 }
 private void toggleFullscreen(){
  fullscreen=!fullscreen;
  setRequestedOrientation(fullscreen?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  if(fullscreen)getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  else getWindow().getDecorView().setSystemUiVisibility(0);
 }
 private void more(){
  new AlertDialog.Builder(this).setTitle("MP4 재생 메뉴").setItems(new String[]{
   "즐겨찾기 추가 / 해제","파일 정보","앱 보관함에서 삭제","보관함으로 돌아가기"
  },(d,n)->{
   File f=playlist.get(index);
   if(n==0){
    android.content.SharedPreferences pref=getSharedPreferences("favorites",MODE_PRIVATE);
    boolean old=pref.getBoolean(f.getAbsolutePath(),false);
    pref.edit().putBoolean(f.getAbsolutePath(),!old).apply();
    message(old?"즐겨찾기 해제":"즐겨찾기에 추가");
   }else if(n==1){
    new AlertDialog.Builder(this).setTitle(f.getName()).setMessage(
     "파일 크기: "+(f.length()/1024/1024)+" MB\n저장 위치: 앱 내부 MP4 보관함\n길이: "+(video.getDuration()/1000)+"초").setPositiveButton("확인",null).show();
   }else if(n==2){
    new AlertDialog.Builder(this).setTitle("파일 삭제 확인")
     .setMessage("앱 내부에 복사된 이 영상만 삭제합니다. 휴대전화의 원본 파일은 삭제하지 않습니다.")
     .setNegativeButton("취소",null).setPositiveButton("삭제",(dialog,which)->{
      video.stopPlayback();
      if(f.delete()){playlist.remove(index);if(playlist.isEmpty())finish();else{index=Math.min(index,playlist.size()-1);play();}}
      else message("파일 삭제 실패");
     }).show();
   }else finish();
  }).show();
 }
 private class HandlerTicker implements Runnable {
  final android.os.Handler h=new android.os.Handler(android.os.Looper.getMainLooper());
  void start(){h.postDelayed(this,500);}
  void stop(){h.removeCallbacks(this);}
  @Override public void run(){
   if(video!=null&&video.getDuration()>0)seek.setProgress((int)(1000L*video.getCurrentPosition()/video.getDuration()));
   h.postDelayed(this,500);
  }
 }
 @Override public void onBackPressed(){if(fullscreen)toggleFullscreen();else super.onBackPressed();}
 @Override protected void onDestroy(){if(ticker!=null)ticker.stop();if(video!=null)video.stopPlayback();super.onDestroy();}
}
