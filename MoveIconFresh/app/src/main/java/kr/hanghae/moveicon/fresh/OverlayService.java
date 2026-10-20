package kr.hanghae.moveicon.fresh;
import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.*;
import android.widget.TextView;

public class OverlayService extends Service {
 private WindowManager wm;private TextView bubble;private WindowManager.LayoutParams params;
 @Override public void onCreate(){
  super.onCreate();
  if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return;}
  String channel="moveicon-fresh";
  if(Build.VERSION.SDK_INT>=26){
   NotificationChannel nc=new NotificationChannel(channel,"이동아이콘",NotificationManager.IMPORTANCE_LOW);
   ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(nc);
  }
  Notification.Builder nb=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,channel):new Notification.Builder(this);
  Notification notification=nb.setSmallIcon(android.R.drawable.ic_menu_compass).setContentTitle("항행의자유 이동아이콘")
    .setContentText("화면 위 닻 아이콘 표시 중").setOngoing(true).build();
  startForeground(1984,notification);
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  bubble=new TextView(this);bubble.setText("⚓");bubble.setTextSize(35);bubble.setGravity(Gravity.CENTER);
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
  bg.setColor(0xff082c4c);bg.setCornerRadius(100);bg.setStroke(3,0xffffd789);bubble.setBackground(bg);
  int size=(int)(64*getResources().getDisplayMetrics().density);
  params=new WindowManager.LayoutParams(size,size,
    Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
  params.gravity=Gravity.TOP|Gravity.START;
  android.content.SharedPreferences pref=getSharedPreferences("position",MODE_PRIVATE);
  params.x=pref.getInt("x",10);params.y=pref.getInt("y",220);
  bubble.setOnTouchListener(new View.OnTouchListener(){
   int startX,startY;float downX,downY;boolean moved;
   @Override public boolean onTouch(View v,android.view.MotionEvent e){
    switch(e.getAction()){
     case MotionEvent.ACTION_DOWN: startX=params.x;startY=params.y;downX=e.getRawX();downY=e.getRawY();moved=false;return true;
     case MotionEvent.ACTION_MOVE:
      int x=(int)(e.getRawX()-downX),y=(int)(e.getRawY()-downY);
      if(Math.abs(x)>8||Math.abs(y)>8)moved=true;
      params.x=Math.max(0,startX+x);params.y=Math.max(0,startY+y);
      try{wm.updateViewLayout(bubble,params);}catch(Exception ignored){}
      return true;
     case MotionEvent.ACTION_UP:
      pref.edit().putInt("x",params.x).putInt("y",params.y).apply();
      if(!moved){
       Intent open=new Intent(OverlayService.this,MainActivity.class);
       open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
       startActivity(open);
      }
      return true;
    }
    return false;
   }
  });
  wm.addView(bubble,params);
 }
 @Override public int onStartCommand(Intent intent,int flags,int startId){return START_NOT_STICKY;}
 @Override public void onDestroy(){if(wm!=null&&bubble!=null){try{wm.removeView(bubble);}catch(Exception ignored){}}super.onDestroy();}
 @Override public IBinder onBind(Intent intent){return null;}
}
