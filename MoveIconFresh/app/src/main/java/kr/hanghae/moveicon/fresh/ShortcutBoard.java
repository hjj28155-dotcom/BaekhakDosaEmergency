package kr.hanghae.moveicon.fresh;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.InputType;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Private, user-configurable launcher tiles. No other apps or their files are modified. */
public final class ShortcutBoard {
 private static final String PREFS="moveicon_fresh_shortcuts";
 private final Activity a;
 private final LinearLayout host;
 private final Runnable goHome;
 private final SharedPreferences store;
 private final ArrayList<Item> items=new ArrayList<>();
 private FrameLayout surface;
 private int width;
 private int height;
 private static final class Item {
  long id;String name,url,pkg;int x,y;
 }
 private int dp(float v){return (int)(a.getResources().getDisplayMetrics().density*v+.5f);}
 private ShortcutBoard(Activity a,LinearLayout host,Runnable goHome){
  this.a=a;this.host=host;this.goHome=goHome;
  store=a.getSharedPreferences(PREFS,Activity.MODE_PRIVATE);
  width=a.getResources().getDisplayMetrics().widthPixels-dp(56);
  height=dp(440);
  try{
   JSONArray rows=new JSONArray(store.getString("items","[]"));
   for(int i=0;i<rows.length();i++){
    JSONObject j=rows.getJSONObject(i);Item item=new Item();
    item.id=j.optLong("id",System.currentTimeMillis()+i);
    item.name=j.optString("name","바로가기");
    item.url=j.optString("url","");item.pkg=j.optString("pkg","");
    item.x=j.optInt("x",(i%2)*(width/2));item.y=j.optInt("y",(i/2)*dp(78));
    items.add(item);
   }
  }catch(Exception ignored){}
 }
 public static void show(Activity a,LinearLayout host,Runnable goHome){
  new ShortcutBoard(a,host,goHome).render();
 }
 private void save(){
  JSONArray out=new JSONArray();
  try{
   for(Item i:items){JSONObject j=new JSONObject();
    j.put("id",i.id);j.put("name",i.name);j.put("url",i.url);
    j.put("pkg",i.pkg);j.put("x",i.x);j.put("y",i.y);out.put(j);
   }
  }catch(Exception ignored){}
  store.edit().putString("items",out.toString()).apply();
 }
 private GradientDrawable shape(){
  GradientDrawable d=new GradientDrawable();d.setColor(0xff0a2b49);d.setCornerRadius(dp(13));d.setStroke(dp(2),0xffdcb463);return d;
 }
 private Button button(String text,Runnable run){
  Button b=new Button(a);b.setAllCaps(false);b.setText(text);b.setTextSize(16);
  b.setTextColor(0xffffdc93);b.setBackground(shape());b.setOnClickListener(v->run.run());return b;
 }
 private void render(){
  host.removeAllViews();
  TextView heading=new TextView(a);heading.setText("⚓ 나만의 이동아이콘");
  heading.setTextSize(24);heading.setTextColor(0xffffd58a);heading.setGravity(Gravity.CENTER);
  heading.setPadding(0,dp(12),0,dp(10));
  host.addView(heading,new LinearLayout.LayoutParams(-1,dp(70)));
  TextView hint=new TextView(a);
  hint.setText("아이콘을 길게 누른 채 원하는 자리로 이동\n짧게 누르면 실행 · 편집은 아래 버튼 이용");
  hint.setTextColor(Color.WHITE);hint.setTextSize(14);hint.setGravity(Gravity.CENTER);
  host.addView(hint,new LinearLayout.LayoutParams(-1,dp(58)));
  Button plus=button("＋ 아이콘 추가",()->edit(null));
  host.addView(plus,new LinearLayout.LayoutParams(-1,dp(55)));
  surface=new FrameLayout(a);
  LinearLayout.LayoutParams s=new LinearLayout.LayoutParams(-1,height);s.topMargin=dp(12);
  surface.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x661e4a78,0x66081931}));
  host.addView(surface,s);
  for(Item item:new ArrayList<>(items))drawItem(item);
  Button edit=button("아이콘 편집 · 삭제",this::pickEdit);
  LinearLayout.LayoutParams e=new LinearLayout.LayoutParams(-1,dp(55));e.topMargin=dp(12);host.addView(edit,e);
  Button home=button("← 홈",goHome);
  LinearLayout.LayoutParams h=new LinearLayout.LayoutParams(-1,dp(55));h.topMargin=dp(10);host.addView(home,h);
 }
 private void drawItem(Item i){
  final int w=Math.min(width/2-dp(7),dp(175)), h=dp(68);
  TextView tile=new TextView(a);tile.setText("✦  "+i.name);tile.setTextSize(16);
  tile.setTextColor(0xffffdc93);tile.setGravity(Gravity.CENTER);tile.setBackground(shape());
  tile.setPadding(dp(5),0,dp(5),0);
  FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(w,h);
  p.leftMargin=Math.max(0,Math.min(i.x,width-w));p.topMargin=Math.max(0,Math.min(i.y,height-h));
  surface.addView(tile,p);
  tile.setOnTouchListener(new View.OnTouchListener(){
   float downX,downY;int originX,originY;boolean drag;
   final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
   Runnable arm;
   @Override public boolean onTouch(View v,MotionEvent event){
    switch(event.getActionMasked()){
     case MotionEvent.ACTION_DOWN:
      downX=event.getRawX();downY=event.getRawY();
      originX=i.x;originY=i.y;drag=false;
      arm=()->{drag=true;tile.setAlpha(.83f);};handler.postDelayed(arm,450);return true;
     case MotionEvent.ACTION_MOVE:
      int dx=(int)(event.getRawX()-downX),dy=(int)(event.getRawY()-downY);
      if(drag){
       i.x=Math.max(0,Math.min(width-w,originX+dx));
       i.y=Math.max(0,Math.min(height-h,originY+dy));
       FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)tile.getLayoutParams();
       lp.leftMargin=i.x;lp.topMargin=i.y;tile.setLayoutParams(lp);
      }else if(Math.abs(dx)>dp(15)||Math.abs(dy)>dp(15)){handler.removeCallbacks(arm);}
      return true;
     case MotionEvent.ACTION_CANCEL:
     case MotionEvent.ACTION_UP:
      handler.removeCallbacks(arm);tile.setAlpha(1f);
      if(drag)save();
      else if(event.getActionMasked()==MotionEvent.ACTION_UP)launch(i);
      return true;
    }
    return true;
   }
  });
 }
 private void launch(Item i){
  try{
   if(!i.pkg.isEmpty()){
    Intent in=a.getPackageManager().getLaunchIntentForPackage(i.pkg);
    if(in==null)throw new Exception("앱이 설치되지 않았습니다.");
    a.startActivity(in);
   }else if(!i.url.isEmpty()){
    Uri uri=Uri.parse(i.url);
    String scheme=uri.getScheme();
    if(!"https".equalsIgnoreCase(scheme)&&!"http".equalsIgnoreCase(scheme))
     throw new Exception("웹 주소는 https:// 또는 http:// 로 시작해야 합니다.");
    a.startActivity(new Intent(Intent.ACTION_VIEW,uri));
   }else edit(i);
  }catch(Exception ex){new AlertDialog.Builder(a).setMessage("열기 실패: "+ex.getMessage()).setPositiveButton("확인",null).show();}
 }
 private void pickEdit(){
  if(items.isEmpty()){edit(null);return;}
  String[] names=new String[items.size()];
  for(int n=0;n<items.size();n++)names[n]=items.get(n).name;
  new AlertDialog.Builder(a).setTitle("편집할 아이콘 선택").setItems(names,(d,index)->edit(items.get(index)))
   .setNegativeButton("취소",null).show();
 }
 private void edit(Item old){
  final Item item=old==null?new Item():old;
  if(old==null){item.id=System.currentTimeMillis();item.name="새 아이콘";
   int n=items.size();item.x=(n%2)*(width/2);item.y=Math.min(height-dp(68),(n/2)*dp(78));}
  final String[] draftPkg={item.pkg};
  LinearLayout form=new LinearLayout(a);form.setPadding(dp(20),dp(8),dp(20),0);
  form.setOrientation(LinearLayout.VERTICAL);
  EditText name=new EditText(a);name.setSingleLine(true);name.setHint("아이콘 이름");name.setText(item.name);form.addView(name);
  EditText url=new EditText(a);url.setSingleLine(true);url.setHint("https:// 연결 주소 (선택)");url.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
  url.setText(item.url);form.addView(url);
  TextView pkg=new TextView(a);pkg.setTextColor(Color.DKGRAY);
  pkg.setText(item.pkg.isEmpty()?"연결 앱: 선택 안 됨":"연결 앱: "+item.pkg);
  form.addView(pkg);
  Button choose=new Button(a);choose.setText("휴대전화 앱 선택");
  choose.setOnClickListener(v->chooseInstalledApp(draftPkg,pkg));
  form.addView(choose);
  Button unlink=new Button(a);unlink.setText("앱 연결 해제 · 웹 주소 사용");
  unlink.setOnClickListener(v->{draftPkg[0]="";pkg.setText("연결 앱: 선택 안 됨");});
  form.addView(unlink);
  AlertDialog.Builder b=new AlertDialog.Builder(a).setTitle(old==null?"아이콘 추가":"아이콘 편집")
   .setView(form).setPositiveButton("저장",(d,w)->{
    String t=name.getText().toString().trim(),u=url.getText().toString().trim();
    if(t.isEmpty()){notifyUser("이름이 비어 있어 저장하지 않았습니다.");return;}
    if(!u.isEmpty()&&!u.startsWith("https://")&&!u.startsWith("http://")){
     notifyUser("http:// 또는 https:// 주소만 사용합니다.");return;
    }
    item.name=t;item.url=u;item.pkg=draftPkg[0];if(old==null)items.add(item);save();render();
   }).setNegativeButton("취소",null);
  if(old!=null)b.setNeutralButton("삭제",(d,w)->new AlertDialog.Builder(a)
   .setTitle("아이콘 삭제").setMessage("이 바로가기만 삭제합니다. 원래 앱이나 파일은 삭제하지 않습니다.")
   .setPositiveButton("삭제",(dd,ww)->{items.remove(old);save();render();})
   .setNegativeButton("취소",null).show());
  b.show();
 }
 private void chooseInstalledApp(String[] selectedPkg,TextView chosen){
  PackageManager pm=a.getPackageManager();
  Intent intent=new Intent(Intent.ACTION_MAIN);intent.addCategory(Intent.CATEGORY_LAUNCHER);
  List<ResolveInfo> apps=pm.queryIntentActivities(intent,0);
  Collections.sort(apps,Comparator.comparing(x->String.valueOf(x.loadLabel(pm)),String.CASE_INSENSITIVE_ORDER));
  if(apps.isEmpty()){notifyUser("선택할 앱이 없습니다.");return;}
  String[] labels=new String[apps.size()];
  for(int i=0;i<apps.size();i++)labels[i]=String.valueOf(apps.get(i).loadLabel(pm));
  new AlertDialog.Builder(a).setTitle("연결할 앱 선택").setItems(labels,(d,which)->{
   selectedPkg[0]=apps.get(which).activityInfo.packageName;
   chosen.setText("연결 앱: "+labels[which]);
  }).setNegativeButton("취소",null).show();
 }
 private void notifyUser(String s){android.widget.Toast.makeText(a,s,android.widget.Toast.LENGTH_LONG).show();}
}
