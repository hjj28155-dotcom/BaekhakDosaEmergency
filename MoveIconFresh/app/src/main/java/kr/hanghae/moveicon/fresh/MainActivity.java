package kr.hanghae.moveicon.fresh;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.database.Cursor;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** Compact utility dialog. The main menu lives in OverlayService, never as a fullscreen activity. */
public class MainActivity extends Activity {
 public static final int MP3_FOLDER=401,MP4_FOLDER=402;
 private LinearLayout body;
 private boolean launchAnchorOnResume=false;
 private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
 private GradientDrawable panel(){GradientDrawable d=new GradientDrawable();d.setColor(0xff081e37);d.setCornerRadius(dp(20));d.setStroke(dp(2),0xffc49e59);return d;}
 private Button add(String label,Runnable action){
  Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);
  b.setTextColor(0xffffdb9b);b.setBackground(panel());b.setOnClickListener(v->action.run());
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(49));lp.bottomMargin=dp(7);body.addView(b,lp);return b;
 }
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 private void folder(int request){
  // Pick individual media files instead of a storage root, which Android 11+ blocks.
  Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT);
  pick.addCategory(Intent.CATEGORY_OPENABLE);
  pick.setType(request==MP3_FOLDER?"audio/*":"video/*");
  pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
  pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  startActivityForResult(pick,request);
 }
 private void overlay(){
  if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){
   toast("다른 앱 위에 표시 권한을 허용하십시오.");
   startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));
   return;
  }
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
   requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},50);
  Intent i=new Intent(this,OverlayService.class);
  if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);
  finish();
 }
 private void shortcutEdit(){
  body.removeAllViews();
  ShortcutBoard.show(this,body,this::showCompact);
  Window w=getWindow();w.setLayout(dp(340),dp(560));
 }
 private void showCompact(){
  body.removeAllViews();
  TextView title=new TextView(this);title.setText("⚓ 이동아이콘");title.setTextColor(0xffffd58a);
  title.setTextSize(22);title.setGravity(Gravity.CENTER);
  body.addView(title,new LinearLayout.LayoutParams(-1,dp(52)));
  add("⚓ 동그란 이동아이콘 표시",this::overlay);
  add("🎵 내 음악 폴더에서 MP3 가져오기",()->folder(MP3_FOLDER));
  add("🎬 내 뮤비 폴더에서 MP4 가져오기",()->folder(MP4_FOLDER));
  add("📁 앱 보관함 MP3 / MP4",()->{showLibrary("MP3");});
  add("＋ 아이콘 추가 · 이동 · 편집",this::shortcutEdit);
  add("닫기",this::finish);
  getWindow().setLayout(dp(340),WindowManager.LayoutParams.WRAP_CONTENT);
 }
 private void showLibrary(String type){
  body.removeAllViews();
  TextView heading=new TextView(this);heading.setText("앱 보관함 / "+type);heading.setTextSize(18);
  heading.setTextColor(Color.WHITE);body.addView(heading);
  File dir=new File(getFilesDir(),"보관함/"+type);
  browseLibrary(dir,type,true);

  add("MP3 보기",()->showLibrary("MP3"));
  add("MP4 보기",()->showLibrary("MP4"));
  add("← 돌아가기",this::showCompact);
  getWindow().setLayout(dp(340),WindowManager.LayoutParams.WRAP_CONTENT);
 }
 private void browseLibrary(File folder,String type,boolean root){
  File[] files=folder.listFiles();
  if(files!=null){
   java.util.Arrays.sort(files,(a,b)->{
    if(a.isDirectory()!=b.isDirectory())return a.isDirectory()?-1:1;
    return a.getName().compareToIgnoreCase(b.getName());
   });
   for(File file:files){
    if(file.isDirectory())add("📁 "+file.getName(),()->{
     body.removeAllViews();
     TextView title=new TextView(this);title.setText(type+" / "+file.getName());
     title.setTextColor(Color.WHITE);title.setTextSize(18);body.addView(title);
     browseLibrary(file,type,false);
     add("← 상위 폴더",()->showLibrary(type));
    });
    else if(file.isFile())add("▶ "+file.getName(),()->playFile(file,type));
   }
  }
 }
 private android.media.MediaPlayer player;
 private void playFile(File f,String type){
  if(type.equals("MP3")){
   try{if(player!=null)player.release();player=new android.media.MediaPlayer();
    player.setDataSource(f.getAbsolutePath());player.prepare();player.start();toast("재생: "+f.getName());
   }catch(Exception e){toast("재생 실패: "+e.getMessage());}
  } else {
   Intent intent=new Intent(this,VideoPlayerActivity.class);
   intent.putExtra("file",f.getAbsolutePath());
   intent.putExtra("folder",f.getParent());
   startActivity(intent);
  }
 }
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  Window w=getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
  WindowManager.LayoutParams attrs=w.getAttributes();attrs.dimAmount=.16f;w.setAttributes(attrs);
  android.widget.ScrollView scroll=new android.widget.ScrollView(this);
  scroll.setBackground(new NightSeaDrawable());
  body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(15),dp(15),dp(15),dp(15));
  scroll.addView(body);setContentView(scroll);
  String op=getIntent().getStringExtra("open");
  if("MP3".equals(op))showLibrary("MP3");
  else if("MP4".equals(op))showLibrary("MP4");
  else if("settings".equals(op))showCompact();
  else if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){showCompact();launchAnchorOnResume=true;overlay();}
  else {launchAnchorOnResume=true;overlay();}

 }
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(result!=RESULT_OK||data==null)return;
  if(request!=MP3_FOLDER&&request!=MP4_FOLDER)return;
  String group=request==MP3_FOLDER?"MP3":"MP4";
  java.util.ArrayList<Uri> selected=new java.util.ArrayList<>();
  if(data.getClipData()!=null)for(int i=0;i<data.getClipData().getItemCount();i++)
   selected.add(data.getClipData().getItemAt(i).getUri());
  else if(data.getData()!=null)selected.add(data.getData());
  if(selected.isEmpty())return;
  int count=0;
  File destDir=new File(getFilesDir(),"보관함/"+group);
  if(!destDir.exists()&&!destDir.mkdirs()){toast("앱 보관함 생성 실패");return;}
  for(Uri uri:selected){
   File output=null;
   try{
    String name="media_"+System.currentTimeMillis()+"_"+count+(group.equals("MP3")?".mp3":".mp4");
    try(Cursor c=getContentResolver().query(uri,new String[]{android.provider.OpenableColumns.DISPLAY_NAME},null,null,null)){
     if(c!=null&&c.moveToFirst()&&c.getString(0)!=null)name=c.getString(0);
    }
    name=name.replace('/','_').replace((char)92,'_');
    String lower=name.toLowerCase(java.util.Locale.ROOT);
    if(group.equals("MP3")&&!(lower.endsWith(".mp3")||lower.endsWith(".m4a")||lower.endsWith(".wav")||lower.endsWith(".flac")))continue;
    if(group.equals("MP4")&&!(lower.endsWith(".mp4")||lower.endsWith(".mkv")||lower.endsWith(".webm")))continue;
    output=new File(destDir,name);
    if(output.exists())output=new File(destDir,System.currentTimeMillis()+"_"+count+"_"+name);
    try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(output)){
     if(in==null)throw new Exception("파일을 읽을 수 없습니다");
     byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
    }
    count++;
   }catch(Exception ex){if(output!=null)output.delete();toast("일부 파일 가져오기 실패: "+ex.getMessage());}
  }
  toast(count+"개 파일을 앱 내부 "+group+" 보관함에 복사했습니다.");
  showLibrary(group);
 }
 private int copyFolder(Uri tree,String group)throws Exception{
  Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree));
  return copyChildren(tree,children,new File(getFilesDir(),"보관함/"+group),group,0);
 }
 private int copyChildren(Uri tree,Uri children,File folder,String group,int depth)throws Exception{
  if(depth>8)throw new Exception("하위 폴더 단계가 너무 깊습니다.");
  if(!folder.exists()&&!folder.mkdirs())throw new Exception("앱 보관함 폴더 생성 실패");
  int total=0;String[] cols={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE};
  try(Cursor c=getContentResolver().query(children,cols,null,null,null)){
   if(c==null)throw new Exception("폴더를 읽을 수 없습니다.");
   while(c.moveToNext()){
    String id=c.getString(0),name=c.getString(1),mime=c.getString(2);
    if(name==null)continue;
    name=name.replace('/', '_');
    Uri doc=DocumentsContract.buildDocumentUriUsingTree(tree,id);
    if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)){
      Uri sub=DocumentsContract.buildChildDocumentsUriUsingTree(tree,id);
      total+=copyChildren(tree,sub,new File(folder,name),group,depth+1);
    }else{
      String lower=name.toLowerCase(java.util.Locale.ROOT);
      if(!(group.equals("MP3")?(lower.endsWith(".mp3")||lower.endsWith(".m4a")||lower.endsWith(".wav")||lower.endsWith(".flac")):
        (lower.endsWith(".mp4")||lower.endsWith(".mkv")||lower.endsWith(".webm"))))continue;
      File dest=new File(folder,name);
      if(dest.exists()){String stem=name.contains(".")?name.substring(0,name.lastIndexOf('.')):name;
       String ext=name.contains(".")?name.substring(name.lastIndexOf('.')):"";
       dest=new File(folder,stem+"_"+System.currentTimeMillis()+ext);
      }
      try(InputStream in=getContentResolver().openInputStream(doc);FileOutputStream out=new FileOutputStream(dest)){
       if(in==null)throw new Exception("파일을 열 수 없습니다");
       byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
       total++;
      }catch(Exception e){dest.delete();throw e;}
    }
   }
  }
  return total;
 }
 @Override protected void onResume(){
  super.onResume();
  if(launchAnchorOnResume&&Build.VERSION.SDK_INT>=23&&Settings.canDrawOverlays(this)) {
   launchAnchorOnResume=false;
   overlay();
  }
 }
 @Override protected void onDestroy(){if(player!=null){player.release();player=null;}super.onDestroy();}
}
