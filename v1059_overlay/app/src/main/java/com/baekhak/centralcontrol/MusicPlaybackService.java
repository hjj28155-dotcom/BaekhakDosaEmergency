package com.baekhak.centralcontrol;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.*;
import java.util.*;
import org.json.JSONObject;

/** FIX9: persistent track identity + position, async preparation, bounded error handling.
 * App exit preserves resume intent. Explicit pause/stop does not autoplay on the next launch.
 */
public class MusicPlaybackService extends Service {
    private static final String PREFIX="com.baekhak.centralcontrol.music.";
    public static final String ACTION_PLAY=PREFIX+"PLAY", ACTION_PAUSE=PREFIX+"PAUSE",
        ACTION_RELOAD=PREFIX+"RELOAD",ACTION_VOLUME=PREFIX+"VOLUME",ACTION_STOP=PREFIX+"STOP",
        ACTION_APP_EXIT=PREFIX+"APP_EXIT",ACTION_NEXT=PREFIX+"NEXT",ACTION_PREV=PREFIX+"PREV",
        ACTION_SELECT=PREFIX+"SELECT", ACTION_DEFAULT=PREFIX+"DEFAULT", ACTION_LIBRARY=PREFIX+"LIBRARY";
    public static final String EXTRA_VOLUME="volume",EXTRA_TRACK="track_id";
    private static final String PREFS="v3_music_preferences",CHANNEL="v3_music_playback";
    private static final int NOTICE=3407;
    private static volatile MusicPlaybackService active;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<MusicLibraryFiles.Track> tracks=new ArrayList<>();
    private final List<Uri> phoneUris=new ArrayList<>();
    private final List<String> phoneNames=new ArrayList<>();
    private final List<String> phoneIds=new ArrayList<>();
    private boolean phoneMode=false;
    private final Set<String> failed=new HashSet<>();
    private MediaPlayer player;
    private AudioManager audio;
    private AudioFocusRequest focusRequest;
    private boolean prepared=false,seeking=false,wantPlay=false,focusHeld=false,resumeOnFocus=false;
    private int index=0,pendingPosition=0,duration=0;
    private String trackId="",trackName="",status="ready",error="";
    private final Runnable checkpoint=new Runnable(){public void run(){saveCheckpoint(false);handler.postDelayed(this,2000);}};
    private final BroadcastReceiver noisy=new BroadcastReceiver(){public void onReceive(Context c,Intent i){pause(true);}};
    private final AudioManager.OnAudioFocusChangeListener focusChange=change->handler.post(()->{
        if(change==AudioManager.AUDIOFOCUS_GAIN) {
            focusHeld=true;applyVolume(readVolume());
            if(resumeOnFocus){resumeOnFocus=false;wantPlay=true;startIfReady();}
        } else if(change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            try{if(player!=null){float v=readVolume()/100f*0.2f;player.setVolume(v,v);}}catch(Exception ignored){}
        } else {
            boolean transientLoss=change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
            resumeOnFocus=transientLoss&&wantPlay;
            focusHeld=false;pause(!transientLoss);
        }
    });
    private SharedPreferences prefs(){return getSharedPreferences(PREFS,MODE_PRIVATE);}
    private int readVolume(){return Math.max(0,Math.min(100,prefs().getInt("music_volume",42)));}
    private boolean enabled(){return prefs().getBoolean("music_enabled",false);}
    private boolean playing(){try{return prepared&&!seeking&&player!=null&&player.isPlaying();}catch(Exception e){return false;}}
    private int position(){try{if(prepared&&!seeking&&player!=null)return player.getCurrentPosition();}catch(Exception ignored){}return pendingPosition;}
    @Override public void onCreate(){
        super.onCreate();active=this;audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"V3 배경음악",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}
        IntentFilter f=new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        if(Build.VERSION.SDK_INT>=33)registerReceiver(noisy,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(noisy,f);
        handler.postDelayed(checkpoint,2000);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        String a=intent==null?ACTION_PLAY:intent.getAction();
        if(Build.VERSION.SDK_INT>=29)startForeground(NOTICE,notification(),ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        else startForeground(NOTICE,notification());
        if(ACTION_APP_EXIT.equals(a)) {
            saveCheckpoint(true);
            if(playing()) { wantPlay=true; status="playing"; notifyState(); return START_STICKY; }
            return enabled() ? START_STICKY : START_NOT_STICKY;
        }
        if(ACTION_STOP.equals(a)) {
            saveCheckpoint(true);prefs().edit().putBoolean("music_enabled",false).commit();
            wantPlay=false;release();abandonFocus();stopForeground(true);stopSelf();return START_NOT_STICKY;
        }
        if(ACTION_PAUSE.equals(a)){pause(true);return START_NOT_STICKY;}
        if(ACTION_VOLUME.equals(a)){applyVolume(intent.getIntExtra(EXTRA_VOLUME,readVolume()));notifyState();return START_NOT_STICKY;}
        if(intent==null&&!enabled()){stopForeground(true);stopSelf();return START_NOT_STICKY;}
        failed.clear();error="";
        if(ACTION_RELOAD.equals(a)) {
            saveCheckpoint(true);String old=trackId;boolean play=enabled();release();
            if(!loadTracks())return START_NOT_STICKY;
            choose(old.isEmpty()?prefs().getString("music_track_id",""):old);
            wantPlay=play;openCurrent(savedPosition());return START_NOT_STICKY;
        }
        if(ACTION_DEFAULT.equals(a)) {
            saveCheckpoint(true);prefs().edit().putBoolean("music_use_default",true).putBoolean("music_enabled",true).commit();
            tracks.clear();phoneUris.clear();phoneNames.clear();phoneIds.clear();phoneMode=false;trackId="default";trackName="기본 배경음악";wantPlay=true;openCurrent(0);return START_NOT_STICKY;
        }
        if(ACTION_LIBRARY.equals(a)) {
            saveCheckpoint(true);prefs().edit().putBoolean("music_use_default",false).putBoolean("music_enabled",true).commit();
            if(!loadTracks())return START_NOT_STICKY;
            choose(prefs().getString("music_library_last_id",""));wantPlay=true;openCurrent(savedPosition());return START_NOT_STICKY;
        }
        if(ACTION_SELECT.equals(a)) {
            String id=intent.getStringExtra(EXTRA_TRACK); saveCheckpoint(true);
            prefs().edit().putBoolean("music_use_default",false).commit();
            if(!loadTracks())return START_NOT_STICKY;
            if(!contains(id)){error="선택한 곡이 저장목록에 없습니다. 목록을 새로 열어 주세요.";notifyState();return START_NOT_STICKY;}
            choose(id);prefs().edit().putBoolean("music_enabled",true).commit();wantPlay=true;openCurrent(savedPosition());return START_NOT_STICKY;
        }
        if(ACTION_NEXT.equals(a)||ACTION_PREV.equals(a)) {
            saveCheckpoint(true);String old=trackId;
            if(!loadTracks())return START_NOT_STICKY;choose(old);
            if(trackCount()>0){index=(index+(ACTION_NEXT.equals(a)?1:trackCount()-1))%trackCount();setTrack();}
            prefs().edit().putBoolean("music_enabled",true).commit();wantPlay=true;openCurrent(0);return START_NOT_STICKY;
        }
        prefs().edit().putBoolean("music_enabled",true).commit();wantPlay=true;
        if(player==null){if(!loadTracks())return START_NOT_STICKY;choose(prefs().getString("music_track_id",""));openCurrent(savedPosition());}
        else startIfReady();
        return START_NOT_STICKY;
    }
    private boolean loadTracks(){
        tracks.clear(); phoneUris.clear(); phoneNames.clear(); phoneIds.clear(); phoneMode=false;
        if(prefs().getBoolean("music_use_default",false))return true;
        try {
            // V3.3.2: 사용자가 한 번 불러와 앱에 보관한 음악목록을 최우선 사용합니다.
            tracks.addAll(new MusicLibraryStore(this).read());
            if(!tracks.isEmpty()) return true;
            // V1059: 휴대폰 전체 음악 자동검색 금지. 직접 선택해 내부보관한 곡만 재생합니다.
            fail("저장된 음악이 없습니다. MP3 불러오기에서 필요한 곡을 직접 선택해 주세요.");
            return false;
        } catch(Exception e){fail("저장된 음악목록을 읽지 못했습니다.");return false;}
    }
    // V1059: 휴대폰 전체 MP3 자동검색 기능 제거.
    private int trackCount(){return phoneMode?phoneUris.size():tracks.size();}
    private boolean contains(String id){
        if(phoneMode){for(String x:phoneIds)if(x.equals(id))return true;return false;}
        for(MusicLibraryFiles.Track t:tracks)if(t.id.equals(id))return true;return false;
    }
    private void choose(String id){
        if(phoneMode){
            index=Math.max(0,Math.min(prefs().getInt("music_index",0),phoneUris.size()-1));
            for(int i=0;i<phoneIds.size();i++)if(phoneIds.get(i).equals(id)){index=i;break;}
            setTrack();return;
        }
        if(tracks.isEmpty()){index=0;trackId="default";trackName="기본 배경음악";return;}
        index=Math.max(0,Math.min(prefs().getInt("music_index",0),tracks.size()-1));
        for(int i=0;i<tracks.size();i++)if(tracks.get(i).id.equals(id)){index=i;break;}
        setTrack();
    }
    private void setTrack(){
        if(phoneMode){trackId=phoneIds.get(index);trackName=phoneNames.get(index);return;}
        MusicLibraryFiles.Track t=tracks.get(index);trackId=t.id;trackName=t.name;
    }
    private int savedPosition(){return Math.max(0,prefs().getInt("music_pos_"+trackId,0));}
    private void openCurrent(int position){
        release();pendingPosition=Math.max(0,position);duration=0;status="preparing";
        if(trackId.isEmpty())choose(prefs().getString("music_track_id",""));
        final MediaPlayer mp=new MediaPlayer();player=mp;
        try {
            mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mp.setWakeMode(getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK);
            mp.setVolume(readVolume()/100f,readVolume()/100f);
            if(phoneMode){
                mp.setDataSource(this,phoneUris.get(index));
            } else if(tracks.isEmpty()){
                android.content.res.AssetFileDescriptor fd=getResources().openRawResourceFd(R.raw.home_joyful_loop);
                if(fd==null)throw new IllegalStateException("기본 음악 없음");
                try{mp.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}finally{fd.close();}
            } else mp.setDataSource(new MusicLibraryStore(this).file(tracks.get(index)).getAbsolutePath());
            mp.setLooping(false);
            mp.setOnPreparedListener(done->{
                if(player!=done)return;prepared=true;duration=done.getDuration();
                pendingPosition=Math.max(0,Math.min(pendingPosition,Math.max(0,duration-1)));
                if(pendingPosition>0){seeking=true;done.seekTo(pendingPosition);}
                else {saveCheckpoint(false);startIfReady();}
            });
            mp.setOnSeekCompleteListener(done->{if(player!=done)return;seeking=false;pendingPosition=0;startIfReady();});
            mp.setOnCompletionListener(done->{if(player!=done)return;pendingPosition=0;prefs().edit().putInt("music_pos_"+trackId,0).commit();advance(false);});
            mp.setOnErrorListener((bad,what,extra)->{if(player==bad)skipUnreadable();return true;});
            mp.prepareAsync();notifyState();
        }catch(Exception e){skipUnreadable();}
    }
    private void startIfReady(){
        if(!prepared||seeking||player==null)return;
        if(!wantPlay){status="paused";saveCheckpoint(false);notifyState();return;}
        if(!requestFocus()){status="paused";error="다른 앱이 오디오를 사용 중입니다. 재생 버튼을 다시 눌러 주세요.";notifyState();return;}
        try{player.start();status="playing";error="";saveCheckpoint(false);notifyState();}catch(Exception e){skipUnreadable();}
    }
    private void pause(boolean user){
        saveCheckpoint(true);wantPlay=false;
        try{if(playing())player.pause();}catch(Exception ignored){}
        if(user){prefs().edit().putBoolean("music_enabled",false).commit();resumeOnFocus=false;abandonFocus();}
        status="paused";saveCheckpoint(true);notifyState();
    }
    private void advance(boolean afterError){
        release();
        if(trackCount()>0){index=(index+1)%trackCount();setTrack();}
        if(!afterError)failed.clear();
        handler.post(()->openCurrent(0));
    }
    private void skipUnreadable(){
        if(!failed.add(trackId)||failed.size()>=Math.max(1,trackCount())){fail("재생 가능한 곡을 찾지 못했습니다. 저장목록은 보존했습니다.");return;}
        error="읽을 수 없는 곡을 건너뜁니다: "+trackName;advance(true);
    }
    private void fail(String message){error=message;status="error";wantPlay=false;release();abandonFocus();notifyState();}
    private boolean requestFocus(){
        if(focusHeld)return true;if(audio==null)return false;
        int r;
        if(Build.VERSION.SDK_INT>=26){
            if(focusRequest==null)focusRequest=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setOnAudioFocusChangeListener(focusChange,handler).build();
            r=audio.requestAudioFocus(focusRequest);
        } else r=audio.requestAudioFocus(focusChange,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        return focusHeld=r==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
    private void abandonFocus(){try{if(audio!=null){if(Build.VERSION.SDK_INT>=26&&focusRequest!=null)audio.abandonAudioFocusRequest(focusRequest);else audio.abandonAudioFocus(focusChange);}}catch(Exception ignored){}focusHeld=false;}
    private void applyVolume(int p){p=Math.max(0,Math.min(100,p));prefs().edit().putInt("music_volume",p).apply();try{if(player!=null)player.setVolume(p/100f,p/100f);}catch(Exception ignored){}}
    private void saveCheckpoint(boolean sync){
        if(trackId.isEmpty())return;int pos=position();pendingPosition=pos;
        SharedPreferences.Editor e=prefs().edit().putString("music_track_id",trackId).putString("music_current_name",trackName)
            .putInt("music_index",index).putInt("music_position_ms",pos).putInt("music_duration_ms",duration).putInt("music_pos_"+trackId,pos);
        if(!"default".equals(trackId))e.putString("music_library_last_id",trackId);
        if(sync)e.commit();else e.apply();
    }
    private void release(){
        MediaPlayer old=player;player=null;prepared=false;seeking=false;
        if(old!=null){try{old.setOnPreparedListener(null);old.setOnSeekCompleteListener(null);old.setOnCompletionListener(null);old.setOnErrorListener(null);old.release();}catch(Exception ignored){}}
    }
    private PendingIntent action(String a,int code){Intent i=new Intent(this,MusicPlaybackService.class).setAction(a);int flags=PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE;return PendingIntent.getService(this,code,i,flags);}
    private Notification notification(){
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        b.setSmallIcon(R.drawable.v3_ship_icon).setContentTitle("V3 배경음악").setContentText((playing()?"재생 중 · ":"일시정지 · ")+(trackName.isEmpty()?"저장된 음악":trackName)).setOnlyAlertOnce(true).setOngoing(playing()).setCategory(Notification.CATEGORY_TRANSPORT);
        Intent open=getPackageManager().getLaunchIntentForPackage(getPackageName());
        if(open!=null)b.setContentIntent(PendingIntent.getActivity(this,3400,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        b.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_previous,"이전",action(ACTION_PREV,3404)).build());
        b.addAction(new Notification.Action.Builder(playing()?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,playing()?"일시정지":"재생",action(playing()?ACTION_PAUSE:ACTION_PLAY,3401)).build());
        b.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_next,"다음",action(ACTION_NEXT,3405)).build());
        b.addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,"종료",action(ACTION_STOP,3403)).build());
        return b.build();
    }
    private void notifyState(){NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(n!=null)n.notify(NOTICE,notification());}
    public static boolean isRunning(){return active!=null;}

    public static String playlistSnapshot(Context context,int limit){
        org.json.JSONArray a=new org.json.JSONArray();
        int max=Math.max(1,Math.min(20,limit));
        try{
            List<MusicLibraryFiles.Track> saved=new MusicLibraryStore(context).read();
            for(MusicLibraryFiles.Track t:saved){
                JSONObject o=new JSONObject();o.put("id",t.id);o.put("name",t.name);a.put(o);
                if(a.length()>=max)break;
            }
        }catch(Exception ignored){}
        return a.toString();
    }
    public static String snapshot(Context context){
        JSONObject j=new JSONObject();MusicPlaybackService s=active;
        SharedPreferences p=context.getSharedPreferences(PREFS,0);
        try{
            j.put("name",s==null?p.getString("music_current_name",""):s.trackName);
            j.put("id",s==null?p.getString("music_track_id",""):s.trackId);
            j.put("position",s==null?p.getInt("music_position_ms",0):s.position());j.put("duration",s==null?p.getInt("music_duration_ms",0):s.duration);
            j.put("status",s==null?"ready":s.status);j.put("playing",s!=null&&s.playing());j.put("enabled",p.getBoolean("music_enabled",false));
            j.put("volume",p.getInt("music_volume",42));j.put("error",s==null?"":s.error);
            j.put("importing",MusicLibraryStore.importing());j.put("progress",MusicLibraryStore.progress());
        }catch(Exception ignored){}return j.toString();
    }
    @Override public void onTaskRemoved(Intent intent){saveCheckpoint(true);if(playing()){wantPlay=true;notifyState();}super.onTaskRemoved(intent);}
    @Override public void onDestroy(){saveCheckpoint(true);handler.removeCallbacksAndMessages(null);release();abandonFocus();try{unregisterReceiver(noisy);}catch(Exception ignored){}if(active==this)active=null;super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}