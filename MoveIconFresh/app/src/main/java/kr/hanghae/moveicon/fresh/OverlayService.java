package kr.hanghae.moveicon.fresh;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/** Each floating icon is an independent draggable window, not a fixed menu grid. */
public class OverlayService extends Service {
 private WindowManager wm;
 private android.content.SharedPreferences prefs;
 private final ArrayList<TextView> tiles=new ArrayList<>();
 private final ArrayList<WindowManager.LayoutParams> positions=new ArrayList<>();
 private TextView anchor;
 private WindowManager.LayoutParams anchorPos;
 private boolean open=false;
 private int sw,sh;
 private int dp(float x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
 private int clamp(int x,int low,int high){return Math.max(low,Math.min(x,Math.max(low,high)));}
 private GradientDrawable circle(int fill){
  GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);
  d.setColor(fill);d.setStroke(dp(2),0xffe9c16e);return d;
 }
 private TextView tile(String glyph,String label,int size){
  TextView t=new TextView(this);
  t.setText(glyph+"\n"+label);t.setTextSize(size);
  t.setTextColor(0xffffe3aa);t.setGravity(Gravity.CENTER);
  t.setBackground(circle(0xff092944));t.setElevation(dp(4));return t;
 }
 private WindowManager.LayoutParams params(int size,int x,int y){
  int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
  WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(size),dp(size),type,
   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);
  p.gravity=Gravity.TOP|Gravity.LEFT;p.x=x;p.y=y;return p;
 }
 private void drag(TextView view,WindowManager.LayoutParams p,String key,Runnable click){
  view.setOnTouchListener(new View.OnTouchListener(){
   float downX,downY;int sx,sy;boolean moved;
   @Override public boolean onTouch(View v,MotionEvent e){
    switch(e.getActionMasked()){
     case MotionEvent.ACTION_DOWN:
      downX=e.getRawX();downY=e.getRawY();sx=p.x;sy=p.y;moved=false;return true;
     case MotionEvent.ACTION_MOVE:
      int dx=(int)(e.getRawX()-downX),dy=(int)(e.getRawY()-downY);
      if(Math.abs(dx)>dp(6)||Math.abs(dy)>dp(6))moved=true;
      if(moved){
       p.x=clamp(sx+dx,0,Math.max(0,sw-p.width));
       p.y=clamp(sy+dy,0,Math.max(0,sh-p.height-dp(38)));
       try{wm.updateViewLayout(view,p);}catch(Exception ignored){}
      }
      return true;
     case MotionEvent.ACTION_UP:
      if(moved)prefs.edit().putInt(key+"_x",p.x).putInt(key+"_y",p.y).apply();
      else click.run();
      return true;
     case MotionEvent.ACTION_CANCEL:return true;
    }
    return false;
   }
  });
 }
 @Override public void onCreate(){
  super.onCreate();
  if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;}
  String channel="moveicon-fresh";
  NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(
   new NotificationChannel(channel,"항행의자유 원형 이동아이콘",NotificationManager.IMPORTANCE_LOW));
  Notification.Builder nb=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,channel):new Notification.Builder(this);
  startForeground(1984,nb.setSmallIcon(android.R.drawable.ic_menu_compass)
   .setContentTitle("항행의자유 이동아이콘").setContentText("닻을 눌러 원형 아이콘 펼치기").setOngoing(true).build());
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  prefs=getSharedPreferences("floating_positions_v2",MODE_PRIVATE);
  android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
  sw=dm.widthPixels;sh=dm.heightPixels;
  int size=dp(55);
  anchorPos=params(55,prefs.getInt("anchor_x",sw-dp(76)),prefs.getInt("anchor_y",dp(250)));
  anchorPos.x=clamp(anchorPos.x,0,sw-size);
  anchorPos.y=clamp(anchorPos.y,0,sh-size-dp(38));
  anchor=tile("⚓","",30);
  drag(anchor,anchorPos,"anchor",this::toggle);
  wm.addView(anchor,anchorPos);
  getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("hidden",false).apply();
 }
 private void toggle(){if(open)collapse();else expand();}
 private void expand(){
  if(open)return;
  open=true;
  // Restore the proven narrow two-column vertical launcher.
  // Individual tiles remain independent windows, so no fullscreen dimming layer.
  String[] glyphs={"🌐","♫","▣","📻","📡","▶","▣","⚙","◉","■"};
  String[] labels={"항행의자유","MP3","MP4","FM","AM","유튜브","실시간TV","설정","숨김","전체 중지"};
  int width=dp(72),height=dp(72),gap=dp(7);
  int left=clamp(anchorPos.x-dp(84),0,Math.max(0,sw-(width*2+gap)));
  int top=clamp(anchorPos.y+dp(58),0,Math.max(0,sh-dp(445)));
  for(int n=0;n<labels.length;n++){
   final int index=n;
   int row=n/2,col=n%2;
   int proposedX=left+col*(width+gap);
   int proposedY=top+row*(height+gap);
   String key="menu_"+n;
   int x=clamp(prefs.getInt(key+"_x",proposedX),0,sw-width);
   int y=clamp(prefs.getInt(key+"_y",proposedY),0,sh-height-dp(38));
   WindowManager.LayoutParams p=params(72,x,y);
   TextView v=tile(glyphs[n],labels[n],11);
   drag(v,p,key,()->{
    if(index==8){collapse();try{wm.removeView(anchor);}catch(Exception ignored){}getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("hidden",true).apply();stopSelf();}
    else if(index==9){collapse();stopSelf();}
    else launch(index);
   });
   try{wm.addView(v,p);tiles.add(v);positions.add(p);}catch(Exception ignored){}
  }
 }
 private void collapse(){
  for(TextView t:tiles)try{wm.removeView(t);}catch(Exception ignored){}
  tiles.clear();positions.clear();open=false;
 }
 private void launch(int n){
  collapse();
  switch(n){
   case 1:compact("MP3");break;
   case 2:compact("MP4");break;
   case 7:compact("settings");break;
   case 0:external("https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/");break;
   case 3:external("https://radio.kbs.co.kr/");break;
   case 4:external("https://www.mbc.co.kr/");break;
   case 5:external("https://www.youtube.com/");break;
   case 6:external("https://www.kbs.co.kr/");break;
  }
 }
 private void compact(String section){
  Intent i=new Intent(this,MainActivity.class);
  i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);i.putExtra("open",section);startActivity(i);
 }
 private void external(String url){
  try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
   i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);
  }catch(Exception e){Toast.makeText(this,"연결 실패",Toast.LENGTH_SHORT).show();}
 }
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(intent!=null&&"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
  return START_STICKY;
 }
 @Override public void onDestroy(){
  collapse();
  if(wm!=null&&anchor!=null)try{wm.removeView(anchor);}catch(Exception ignored){}
  super.onDestroy();
 }
 @Override public IBinder onBind(Intent i){return null;}
}
