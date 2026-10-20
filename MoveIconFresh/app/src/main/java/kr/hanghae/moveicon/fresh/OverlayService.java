package kr.hanghae.moveicon.fresh;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Small independently movable circular anchor.
 * Touch it to expand a compact floating radial menu; touch again to collapse.
 * Other applications stay visible. The menu is never a fullscreen Activity.
 */
public class OverlayService extends Service {
 private WindowManager wm;
 private TextView bubble;
 private FrameLayout radial;
 private WindowManager.LayoutParams anchorParams,radialParams;
 private android.content.SharedPreferences prefs;
 private int screenW,screenH;
 private boolean expanded=false;
 private int dp(float value){return (int)(getResources().getDisplayMetrics().density*value+.5f);}
 private GradientDrawable disc(int fill,int stroke){
  GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);
  d.setColor(fill);d.setStroke(dp(2),stroke);return d;
 }
 private int overlayType(){
  return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
 }
 private WindowManager.LayoutParams window(int w,int h){
  WindowManager.LayoutParams p=new WindowManager.LayoutParams(w,h,overlayType(),
   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
   PixelFormat.TRANSLUCENT);
  p.gravity=Gravity.TOP|Gravity.LEFT;
  return p;
 }
 private TextView circle(String text,int textSize,int fill){
  TextView view=new TextView(this);
  view.setText(text);view.setTextSize(textSize);view.setTextColor(0xffffdda0);
  view.setGravity(Gravity.CENTER);
  view.setBackground(disc(fill,0xffe2b75b));view.setElevation(dp(5));
  return view;
 }
 @Override public void onCreate(){
  super.onCreate();
  if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;}
  final String channel="moveicon-fresh";
  NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26)
   manager.createNotificationChannel(new NotificationChannel(channel,"동그란 이동아이콘",NotificationManager.IMPORTANCE_LOW));
  Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,channel):new Notification.Builder(this);
  Notification notice=builder.setSmallIcon(android.R.drawable.ic_menu_compass)
   .setContentTitle("항행의자유 이동아이콘").setContentText("원형 닻을 누르면 메뉴가 열립니다")
   .setOngoing(true).build();
  startForeground(1984,notice);
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  prefs=getSharedPreferences("position",MODE_PRIVATE);
  android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
  screenW=dm.widthPixels;screenH=dm.heightPixels;
  int size=dp(62);
  anchorParams=window(size,size);
  anchorParams.x=clamp(prefs.getInt("x",screenW-dp(86)),0,screenW-size);
  anchorParams.y=clamp(prefs.getInt("y",dp(270)),0,screenH-size-dp(60));
  bubble=circle("⚓",35,0xff08243e);
  bubble.setOnTouchListener(new View.OnTouchListener(){
   float downX,downY;int initialX,initialY;boolean moving;
   @Override public boolean onTouch(View v,MotionEvent e){
    if(e.getActionMasked()==MotionEvent.ACTION_DOWN){
     downX=e.getRawX();downY=e.getRawY();initialX=anchorParams.x;initialY=anchorParams.y;
     moving=false;return true;
    }
    if(e.getActionMasked()==MotionEvent.ACTION_MOVE){
     float dx=e.getRawX()-downX,dy=e.getRawY()-downY;
     if(Math.abs(dx)>dp(7)||Math.abs(dy)>dp(7))moving=true;
     if(moving){
      anchorParams.x=clamp(initialX+(int)dx,0,screenW-size);
      anchorParams.y=clamp(initialY+(int)dy,0,screenH-size-dp(60));
      try{wm.updateViewLayout(bubble,anchorParams);}catch(Exception ignored){}
      if(expanded){removeRadial();expanded=false;}
     }return true;
    }
    if(e.getActionMasked()==MotionEvent.ACTION_UP){
     prefs.edit().putInt("x",anchorParams.x).putInt("y",anchorParams.y).apply();
     if(!moving)toggleRadial();return true;
    }
    return e.getActionMasked()==MotionEvent.ACTION_CANCEL;
   }
  });
  wm.addView(bubble,anchorParams);
 }
 private int clamp(int n,int lo,int hi){return Math.max(lo,Math.min(n,Math.max(lo,hi)));}
 private void toggleRadial(){
  if(expanded){removeRadial();expanded=false;}
  else{showRadial();expanded=true;}
 }
 private void removeRadial(){
  if(radial!=null){try{wm.removeView(radial);}catch(Exception ignored){} radial=null;}
 }
 private void compact(String section){
  removeRadial();expanded=false;
  Intent intent=new Intent(this,MainActivity.class);
  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  if(section!=null)intent.putExtra("open",section);
  startActivity(intent);
 }
 private void external(String url){
  removeRadial();expanded=false;
  try{
   Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   startActivity(intent);
  }catch(Exception ex){Toast.makeText(this,"연결할 수 없습니다.",Toast.LENGTH_LONG).show();}
 }
 private void showRadial(){
  removeRadial();
  final int panel=dp(310),item=dp(76);
  radial=new FrameLayout(this);
  // The panel has no opaque fullscreen scrim and occupies only 310dp.
  radialParams=window(panel,panel);
  radialParams.x=clamp(anchorParams.x+dp(31)-panel/2,0,screenW-panel);
  radialParams.y=clamp(anchorParams.y+dp(31)-panel/2,0,screenH-panel-dp(40));
  String[] texts={"🌐\n항행의자유","♫\nMP3","▣\nMP4","📻\nFM","📡\nAM","▶\n유튜브","▣\n실시간TV","⚙\n설정"};
  Runnable[] actions={
   ()->external("https://hjj28155-dotcom.github.io/BaekhakDosaEmergency/"),
   ()->compact("MP3"),()->compact("MP4"),
   ()->external("https://radio.kbs.co.kr/"),
   ()->external("https://www.mbc.co.kr/"),
   ()->external("https://www.youtube.com/"),
   ()->external("https://www.kbs.co.kr/"),
   ()->compact("settings")
  };
  double radius=108;
  for(int n=0;n<8;n++){
   final int index=n;
   double angle=(n*45-90)*Math.PI/180.0;
   int cx=panel/2+(int)(dp((float)radius)*Math.cos(angle));
   int cy=panel/2+(int)(dp((float)radius)*Math.sin(angle));
   FrameLayout.LayoutParams pos=new FrameLayout.LayoutParams(item,item);
   pos.leftMargin=clamp(cx-item/2,0,panel-item);
   pos.topMargin=clamp(cy-item/2,0,panel-item);
   TextView v=circle(texts[n],12,0xff0b2a49);
   v.setOnClickListener(w->actions[index].run());
   radial.addView(v,pos);
  }
  int middle=dp(65);
  TextView close=circle("⚓",32,0xff061d36);
  FrameLayout.LayoutParams center=new FrameLayout.LayoutParams(middle,middle);
  center.leftMargin=(panel-middle)/2;center.topMargin=(panel-middle)/2;
  close.setOnClickListener(v->toggleRadial());
  radial.addView(close,center);
  try{wm.addView(radial,radialParams);}catch(Exception e){radial=null;expanded=false;}
 }
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(intent!=null&&"STOP".equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
  return START_STICKY;
 }
 @Override public void onDestroy(){
  removeRadial();
  if(wm!=null&&bubble!=null){try{wm.removeView(bubble);}catch(Exception ignored){}}
  super.onDestroy();
 }
 @Override public IBinder onBind(Intent intent){return null;}
}
