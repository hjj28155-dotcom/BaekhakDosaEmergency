package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.File;
import java.util.*;

public class LocalMediaPickerActivity extends Activity {
    public static final String EXTRA_MIME="mime";
    private static final int REQ_VIDEO_PICK=7312;

    private String mode="MP4";
    private VideoView video;
    private TextView title;
    private Button fullscreen,importVideo,playPause;
    private LinearLayout fullControls;
    private SeekBar seek;
    private Uri currentUri;
    private MediaPlayer preparedPlayer;
    private boolean full=false;
    private final Handler handler=new Handler(Looper.getMainLooper());

    private final Runnable hideControls=()->{
        if(full&&fullControls!=null)fullControls.setVisibility(View.GONE);
    };
    private final Runnable seekUpdater=new Runnable(){
        @Override public void run(){
            try{
                if(video!=null&&seek!=null&&currentUri!=null){
                    int d=video.getDuration(),p=video.getCurrentPosition();
                    if(d>0){
                        seek.setMax(d);
                        if(!seek.isPressed())seek.setProgress(p);
                    }
                    updatePlayPauseLabel();
                }
            }catch(Exception ignored){}
            handler.postDelayed(this,500);
        }
    };

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        buildUi();
        handleIntent(getIntent());
        handler.post(seekUpdater);
    }

    @Override protected void onNewIntent(Intent i){
        super.onNewIntent(i);
        setIntent(i);
        handleIntent(i);
    }

    private Button ctl(String t){
        Button b=new Button(this);
        b.setText(t);b.setTextSize(13);b.setTextColor(Color.WHITE);
        b.setBackgroundColor(0xCC12324A);b.setPadding(8,4,8,4);
        return b;
    }

    private void buildUi(){
        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        video=new VideoView(this);
        root.addView(video,new FrameLayout.LayoutParams(-1,-1));

        title=new TextView(this);
        title.setTextColor(Color.WHITE);title.setTextSize(15);title.setGravity(Gravity.CENTER);
        title.setBackgroundColor(0x99000000);title.setPadding(12,10,12,10);
        root.addView(title,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));

        fullscreen=ctl("⛶ 전체화면");
        fullscreen.setOnClickListener(v->{setFullscreen(true);showControls();});
        FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.RIGHT);
        fp.setMargins(8,8,8,8);root.addView(fullscreen,fp);

        importVideo=ctl("＋ 불러오기");
        importVideo.setOnClickListener(v->openVideoPicker());
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.LEFT);
        ip.setMargins(8,8,8,8);root.addView(importVideo,ip);

        fullControls=new LinearLayout(this);
        fullControls.setOrientation(LinearLayout.VERTICAL);
        fullControls.setPadding(8,6,8,8);
        fullControls.setBackgroundColor(0xB0000000);
        fullControls.setVisibility(View.GONE);

        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);row.setOrientation(LinearLayout.HORIZONTAL);
        Button back10=ctl("⏪ 10초");
        playPause=ctl("▶ 재생");
        Button fwd10=ctl("10초 ⏩");
        Button stop=ctl("■ 종료");
        Button normal=ctl("↩ 원래화면");
        row.addView(back10,new LinearLayout.LayoutParams(0,-2,1));
        row.addView(playPause,new LinearLayout.LayoutParams(0,-2,1));
        row.addView(fwd10,new LinearLayout.LayoutParams(0,-2,1));
        row.addView(stop,new LinearLayout.LayoutParams(0,-2,1));
        row.addView(normal,new LinearLayout.LayoutParams(0,-2,1));

        seek=new SeekBar(this);
        fullControls.addView(row,new LinearLayout.LayoutParams(-1,-2));
        fullControls.addView(seek,new LinearLayout.LayoutParams(-1,-2));

        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);
        root.addView(fullControls,cp);

        back10.setOnClickListener(v->{seekBy(-10000);showControls();});
        fwd10.setOnClickListener(v->{seekBy(10000);showControls();});
        playPause.setOnClickListener(v->{toggle();showControls();});
        stop.setOnClickListener(v->stopAndClose());
        normal.setOnClickListener(v->setFullscreen(false));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean from){if(from&&video!=null)video.seekTo(p);}
            public void onStartTrackingTouch(SeekBar s){handler.removeCallbacks(hideControls);}
            public void onStopTrackingTouch(SeekBar s){showControls();}
        });
        root.setOnClickListener(v->{if(full)showControls();});
        setContentView(root);
    }

    private void handleIntent(Intent i){
        String mime=i==null?null:i.getStringExtra(EXTRA_MIME);
        mode=(mime!=null&&mime.toLowerCase(Locale.ROOT).contains("avi"))?"AVI":"MP4";
        String control=i==null?"open":i.getStringExtra("control");
        if(control==null)control="open";
        int step=i==null?0:i.getIntExtra("step",0);

        if("stop".equals(control)){stopAndClose();return;}
        if("toggle".equals(control)&&currentUri!=null){toggle();return;}
        if("step".equals(control)){selectAndPlayPrivateAware(step==0?1:step);return;}
        selectAndPlayPrivateAware(0);
    }

    private List<VideoLibraryStore.Item> savedForMode(){
        List<VideoLibraryStore.Item> all=new VideoLibraryStore(this).read();
        ArrayList<VideoLibraryStore.Item> out=new ArrayList<>();
        for(VideoLibraryStore.Item x:all){
            String n=x.name==null?"":x.name.toLowerCase(Locale.ROOT);
            if(("MP4".equals(mode)&&n.endsWith(".mp4"))||("AVI".equals(mode)&&n.endsWith(".avi")))out.add(x);
        }
        return out;
    }

    private void selectAndPlayPrivateAware(int step){
        List<VideoLibraryStore.Item> saved=savedForMode();
        if(saved.isEmpty()){openVideoPicker();return;}
        playPrivate(saved,step);
    }

    private void openVideoPicker(){
        try{
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("MP4".equals(mode)?"video/mp4":"video/*");
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                try{
                    i.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                        Uri.parse("content://com.android.externalstorage.documents/document/primary%3AMovies"));
                }catch(Exception ignored){}
            }
            startActivityForResult(i,REQ_VIDEO_PICK);
        }catch(Exception e){
            Toast.makeText(this,"영상 선택 화면을 열지 못했습니다.",Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=REQ_VIDEO_PICK)return;
        if(resultCode!=RESULT_OK||data==null){
            Toast.makeText(this,"불러올 영상을 선택하지 않았습니다.",Toast.LENGTH_SHORT).show();
            return;
        }
        final ArrayList<Uri> picked=new ArrayList<>();
        ClipData cd=data.getClipData();
        if(cd!=null){
            for(int k=0;k<cd.getItemCount();k++){
                Uri u=cd.getItemAt(k).getUri();
                if(u!=null)picked.add(u);
            }
        }
        if(data.getData()!=null)picked.add(data.getData());
        if(picked.isEmpty())return;

        try{
            int flags=data.getFlags()&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            for(Uri u:picked){
                try{getContentResolver().takePersistableUriPermission(u,flags&Intent.FLAG_GRANT_READ_URI_PERMISSION);}
                catch(Exception ignored){}
            }
        }catch(Exception ignored){}

        Toast.makeText(this,"선택한 "+mode+" "+picked.size()+"개 내부보관 중",Toast.LENGTH_SHORT).show();
        new Thread(()->{
            try{
                int added=new VideoLibraryStore(this).append(picked);
                runOnUiThread(()->{
                    Toast.makeText(this,mode+" 내부보관 완료 · "+added+"개",Toast.LENGTH_LONG).show();
                    List<VideoLibraryStore.Item> now=savedForMode();
                    if(!now.isEmpty())playPrivate(now,0);
                });
            }catch(Exception e){
                runOnUiThread(()->Toast.makeText(this,"영상 내부보관에 실패했습니다. 원본은 유지됩니다.",Toast.LENGTH_LONG).show());
            }
        },"Manual-Video-PrivateCopy").start();
    }

    private void playPrivate(List<VideoLibraryStore.Item> list,int step){
        if(list==null||list.isEmpty()){openVideoPicker();return;}
        String key="video_private_index_"+mode;
        int idx=getSharedPreferences("overlay_control",MODE_PRIVATE).getInt(key,0);
        if(step!=0)idx=(idx+step+list.size())%list.size();
        if(idx<0||idx>=list.size())idx=0;
        VideoLibraryStore.Item x=list.get(idx);
        currentUri=Uri.fromFile(new File(x.path));
        getSharedPreferences("overlay_control",MODE_PRIVATE).edit()
            .putInt(key,idx).putString("media_mode",mode).putString("media_title",x.name).apply();
        title.setText(mode+" · "+x.name);
        video.stopPlayback();
        preparedPlayer=null;
        video.setVideoPath(x.path);
        video.setOnPreparedListener(mp->{
            preparedPlayer=mp;
            mp.setLooping(false);
            applyScaling();
            video.start();
            updateVideoPlayingPref();
            updatePlayPauseLabel();
        });
    }

    private void seekBy(int d){
        try{
            int p=Math.max(0,Math.min(video.getDuration(),video.getCurrentPosition()+d));
            video.seekTo(p);
        }catch(Exception ignored){}
    }

    private void toggle(){
        if(video==null||currentUri==null)return;
        try{if(video.isPlaying())video.pause();else video.start();}catch(Exception ignored){}
        updateVideoPlayingPref();updatePlayPauseLabel();
    }

    private void updatePlayPauseLabel(){
        if(playPause==null)return;
        try{playPause.setText(video!=null&&video.isPlaying()?"⏸ 일시정지":"▶ 재생");}
        catch(Exception ignored){}
    }

    private void updateVideoPlayingPref(){
        boolean p=false;try{p=video!=null&&video.isPlaying();}catch(Exception ignored){}
        getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("video_playing",p).apply();
    }

    private void showControls(){
        if(!full||fullControls==null)return;
        fullControls.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideControls);
        handler.postDelayed(hideControls,4000);
    }

    private void applyScaling(){
        if(preparedPlayer==null)return;
        try{
            preparedPlayer.setVideoScalingMode(full?
                MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:
                MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        }catch(Exception ignored){}
    }

    private void setFullscreen(boolean enable){
        if(full==enable)return;
        int pos=video==null?0:video.getCurrentPosition();
        boolean was=video!=null&&video.isPlaying();
        full=enable;
        if(enable){
            try{startService(new Intent(this,OverlayControlService.class).setAction(OverlayControlService.ACTION_SUSPEND));}catch(Exception ignored){}
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            title.setVisibility(View.GONE);fullscreen.setVisibility(View.GONE);importVideo.setVisibility(View.GONE);
            showControls();
        }else{
            handler.removeCallbacks(hideControls);
            fullControls.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            getWindow().getDecorView().setSystemUiVisibility(0);
            title.setVisibility(View.VISIBLE);fullscreen.setVisibility(View.VISIBLE);importVideo.setVisibility(View.VISIBLE);
            try{startService(new Intent(this,OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME));}catch(Exception ignored){}
        }
        applyScaling();
        if(pos>0){
            final int p=pos;
            video.postDelayed(()->{
                try{video.seekTo(p);if(was)video.start();updateVideoPlayingPref();}catch(Exception ignored){}
            },250);
        }
    }

    private void stopAndClose(){
        try{if(video!=null)video.stopPlayback();}catch(Exception ignored){}
        getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("video_playing",false).apply();
        if(Build.VERSION.SDK_INT>=21)finishAndRemoveTask();else finish();
    }

    @Override public void onBackPressed(){
        if(full){setFullscreen(false);return;}
        stopAndClose();
    }

    @Override protected void onDestroy(){
        handler.removeCallbacksAndMessages(null);
        try{if(video!=null)video.stopPlayback();}catch(Exception ignored){}
        getSharedPreferences("overlay_control",MODE_PRIVATE).edit().putBoolean("video_playing",false).apply();
        try{startService(new Intent(this,OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME));}catch(Exception ignored){}
        super.onDestroy();
    }
}
