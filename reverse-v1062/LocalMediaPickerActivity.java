package com.baekhak.centralcontrol;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.baekhak.centralcontrol.VideoLibraryStore;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* loaded from: classes.dex */
public class LocalMediaPickerActivity extends Activity {
    public static final String EXTRA_MIME = "mime";
    private static final int REQ_VIDEO = 7311;
    private static final int REQ_VIDEO_PICK = 7312;
    private Uri currentUri;
    private LinearLayout fullControls;
    private Button fullscreen;
    private Button importVideo;
    private Button playPause;
    private MediaPlayer preparedPlayer;
    private SeekBar seek;
    private TextView title;
    private VideoView video;
    private String wantedMime = "video/mp4";
    private String mode = "MP4";
    private int pendingStep = 0;
    private String pendingControl = "open";
    private boolean full = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hideControls = new Runnable() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda10
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.m10lambda$new$0$combaekhakcentralcontrolLocalMediaPickerActivity();
        }
    };
    private final Runnable seekUpdater = new Runnable() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity.1
        @Override // java.lang.Runnable
        public void run() {
            try {
                if (LocalMediaPickerActivity.this.video != null && LocalMediaPickerActivity.this.seek != null && LocalMediaPickerActivity.this.currentUri != null) {
                    int duration = LocalMediaPickerActivity.this.video.getDuration();
                    int currentPosition = LocalMediaPickerActivity.this.video.getCurrentPosition();
                    if (duration > 0) {
                        LocalMediaPickerActivity.this.seek.setMax(duration);
                        if (!LocalMediaPickerActivity.this.seek.isPressed()) {
                            LocalMediaPickerActivity.this.seek.setProgress(currentPosition);
                        }
                    }
                    LocalMediaPickerActivity.this.updatePlayPauseLabel();
                }
            } catch (Exception unused) {
            }
            LocalMediaPickerActivity.this.handler.postDelayed(this, 500L);
        }
    };

    /* renamed from: lambda$new$0$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m10lambda$new$0$combaekhakcentralcontrolLocalMediaPickerActivity() {
        LinearLayout linearLayout;
        if (!this.full || (linearLayout = this.fullControls) == null) {
            return;
        }
        linearLayout.setVisibility(8);
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        buildUi();
        configurePip();
        handleIntent(getIntent(), true);
        this.handler.post(this.seekUpdater);
    }

    private void configurePip() {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                setPictureInPictureParams(pipBuilder().build());
            } catch (Exception unused) {
            }
        }
    }

    private Rational currentPipRatio() {
        int videoWidth;
        int videoHeight;
        MediaPlayer mediaPlayer;
        int i = 16;
        int i2 = 9;
        try {
            mediaPlayer = this.preparedPlayer;
        } catch (Exception unused) {
        }
        if (mediaPlayer != null && mediaPlayer.getVideoWidth() > 0 && this.preparedPlayer.getVideoHeight() > 0) {
            videoWidth = this.preparedPlayer.getVideoWidth();
            try {
                videoHeight = this.preparedPlayer.getVideoHeight();
            } catch (Exception unused2) {
            }
            if (videoWidth > 0 && videoHeight > 0) {
                i = videoWidth;
                i2 = videoHeight;
            }
            return new Rational(i, i2);
        }
        videoWidth = 16;
        videoHeight = 9;
        if (videoWidth > 0) {
            i = videoWidth;
            i2 = videoHeight;
        }
        return new Rational(i, i2);
    }

    private RemoteAction pipExpandAction() {
        if (Build.VERSION.SDK_INT < 26) {
            return null;
        }
        try {
            Intent intent = new Intent(this, (Class<?>) LocalMediaPickerActivity.class);
            intent.putExtra(EXTRA_MIME, this.wantedMime);
            intent.putExtra("control", "pip_expand");
            intent.addFlags(603979776);
            PendingIntent activity = PendingIntent.getActivity(this, 9062, intent, 201326592);
            Icon iconCreateWithResource = Icon.createWithResource(this, android.R.drawable.ic_menu_view);
            MainActivity$$ExternalSyntheticApiModelOutline0.m74m();
            return MainActivity$$ExternalSyntheticApiModelOutline0.m(iconCreateWithResource, "전체화면", "전체화면으로 돌아가기", activity);
        } catch (Exception unused) {
            return null;
        }
    }

    private PictureInPictureParams.Builder pipBuilder() {
        RemoteAction remoteActionPipExpandAction;
        PictureInPictureParams.Builder aspectRatio = MainActivity$$ExternalSyntheticApiModelOutline0.m().setAspectRatio(currentPipRatio());
        if (Build.VERSION.SDK_INT >= 26 && (remoteActionPipExpandAction = pipExpandAction()) != null) {
            aspectRatio.setActions(Collections.singletonList(remoteActionPipExpandAction));
        }
        if (Build.VERSION.SDK_INT >= 31) {
            aspectRatio.setAutoEnterEnabled(true);
        }
        return aspectRatio;
    }

    private void updatePipParams() {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                setPictureInPictureParams(pipBuilder().build());
            } catch (Exception unused) {
            }
        }
    }

    private void setPipChrome(boolean z) {
        TextView textView = this.title;
        if (textView != null) {
            textView.setVisibility(z ? 8 : 0);
        }
        Button button = this.fullscreen;
        if (button != null) {
            button.setVisibility(z ? 8 : 0);
        }
        Button button2 = this.importVideo;
        if (button2 != null) {
            button2.setVisibility(z ? 8 : 0);
        }
        LinearLayout linearLayout = this.fullControls;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
    }

    @Override // android.app.Activity
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT < 26 || isInPictureInPictureMode() || this.currentUri == null) {
            return;
        }
        try {
            VideoView videoView = this.video;
            if (videoView == null || !videoView.isPlaying()) {
                return;
            }
            updatePipParams();
            enterPictureInPictureMode(pipBuilder().build());
        } catch (Exception unused) {
        }
    }

    @Override // android.app.Activity
    public void onPictureInPictureModeChanged(boolean z, Configuration configuration) {
        super.onPictureInPictureModeChanged(z, configuration);
        setPipChrome(z);
        if (z || this.full) {
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(0);
        setPipChrome(false);
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent, false);
    }

    private Button ctl(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(13.0f);
        button.setTextColor(-1);
        button.setBackgroundColor(-871222710);
        button.setPadding(8, 4, 8, 4);
        return button;
    }

    private void buildUi() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(-16777216);
        VideoView videoView = new VideoView(this);
        this.video = videoView;
        frameLayout.addView(videoView, new FrameLayout.LayoutParams(-1, -1));
        TextView textView = new TextView(this);
        this.title = textView;
        textView.setTextColor(-1);
        this.title.setTextSize(15.0f);
        this.title.setGravity(17);
        this.title.setBackgroundColor(-1728053248);
        this.title.setPadding(12, 10, 12, 10);
        frameLayout.addView(this.title, new FrameLayout.LayoutParams(-1, -2, 80));
        Button buttonCtl = ctl("⛶ 전체화면");
        this.fullscreen = buttonCtl;
        buttonCtl.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda15
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m0xc255ebfd(view);
            }
        });
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 53);
        layoutParams.setMargins(8, 8, 8, 8);
        frameLayout.addView(this.fullscreen, layoutParams);
        Button buttonCtl2 = ctl("＋ 불러오기");
        this.importVideo = buttonCtl2;
        buttonCtl2.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda16
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m2x4f909d7e(view);
            }
        });
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2, 51);
        layoutParams2.setMargins(8, 8, 8, 8);
        frameLayout.addView(this.importVideo, layoutParams2);
        LinearLayout linearLayout = new LinearLayout(this);
        this.fullControls = linearLayout;
        linearLayout.setOrientation(1);
        this.fullControls.setPadding(8, 6, 8, 8);
        this.fullControls.setBackgroundColor(-1342177280);
        this.fullControls.setVisibility(8);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setGravity(17);
        linearLayout2.setOrientation(0);
        Button buttonCtl3 = ctl("⏪ 10초");
        this.playPause = ctl("▶ 재생");
        Button buttonCtl4 = ctl("10초 ⏩");
        Button buttonCtl5 = ctl("■ 종료");
        Button buttonCtl6 = ctl("↩ 원래화면");
        linearLayout2.addView(buttonCtl3, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout2.addView(this.playPause, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout2.addView(buttonCtl4, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout2.addView(buttonCtl5, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayout2.addView(buttonCtl6, new LinearLayout.LayoutParams(0, -2, 1.0f));
        this.seek = new SeekBar(this);
        this.fullControls.addView(linearLayout2, new LinearLayout.LayoutParams(-1, -2));
        this.fullControls.addView(this.seek, new LinearLayout.LayoutParams(-1, -2));
        frameLayout.addView(this.fullControls, new FrameLayout.LayoutParams(-1, -2, 80));
        buttonCtl3.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda17
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m3xdccb4eff(view);
            }
        });
        buttonCtl4.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda18
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m4x6a060080(view);
            }
        });
        this.playPause.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda19
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m5xf740b201(view);
            }
        });
        buttonCtl5.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda20
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m6x847b6382(view);
            }
        });
        buttonCtl6.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda21
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m7x11b61503(view);
            }
        });
        this.seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity.2
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (!z || LocalMediaPickerActivity.this.video == null) {
                    return;
                }
                try {
                    LocalMediaPickerActivity.this.video.seekTo(i);
                } catch (Exception unused) {
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
                LocalMediaPickerActivity.this.handler.removeCallbacks(LocalMediaPickerActivity.this.hideControls);
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
                LocalMediaPickerActivity.this.showControls();
            }
        });
        this.video.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda22
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m8x9ef0c684(view);
            }
        });
        setContentView(frameLayout);
        this.video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda23
            @Override // android.media.MediaPlayer.OnCompletionListener
            public final void onCompletion(MediaPlayer mediaPlayer) {
                this.f$0.m9x2c2b7805(mediaPlayer);
            }
        });
        this.video.setOnErrorListener(new MediaPlayer.OnErrorListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda24
            @Override // android.media.MediaPlayer.OnErrorListener
            public final boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                return this.f$0.m1x739bbe49(mediaPlayer, i, i2);
            }
        });
    }

    /* renamed from: lambda$buildUi$1$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m0xc255ebfd(View view) {
        setFullscreen(!this.full);
        showControls();
    }

    /* renamed from: lambda$buildUi$2$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m2x4f909d7e(View view) {
        openVideoPicker();
    }

    /* renamed from: lambda$buildUi$3$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m3xdccb4eff(View view) {
        seekBy(-10000);
        showControls();
    }

    /* renamed from: lambda$buildUi$4$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m4x6a060080(View view) {
        seekBy(10000);
        showControls();
    }

    /* renamed from: lambda$buildUi$5$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m5xf740b201(View view) {
        toggle();
        showControls();
    }

    /* renamed from: lambda$buildUi$6$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m6x847b6382(View view) {
        stopAndClose();
    }

    /* renamed from: lambda$buildUi$7$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m7x11b61503(View view) {
        setFullscreen(false);
    }

    /* renamed from: lambda$buildUi$8$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m8x9ef0c684(View view) {
        if (this.full) {
            if (this.fullControls.getVisibility() != 0) {
                showControls();
            } else {
                this.fullControls.setVisibility(8);
                this.handler.removeCallbacks(this.hideControls);
            }
        }
    }

    /* renamed from: lambda$buildUi$10$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ boolean m1x739bbe49(MediaPlayer mediaPlayer, int i, int i2) {
        Toast.makeText(this, "이 영상을 재생할 수 없습니다.", 1).show();
        return true;
    }

    /* renamed from: lambda$buildUi$9$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m9x2c2b7805(MediaPlayer mediaPlayer) {
        selectAndPlayPrivateAware(1);
    }

    private void handleIntent(Intent intent, boolean z) {
        if (intent != null && intent.getStringExtra(EXTRA_MIME) != null) {
            this.wantedMime = intent.getStringExtra(EXTRA_MIME);
        }
        this.mode = "video/x-msvideo".equals(this.wantedMime) ? "AVI" : "MP4";
        this.pendingStep = intent == null ? 0 : intent.getIntExtra("step", 0);
        String stringExtra = intent == null ? "open" : intent.getStringExtra("control");
        this.pendingControl = stringExtra;
        if (stringExtra == null) {
            this.pendingControl = "open";
        }
        if ("stop".equals(this.pendingControl)) {
            stopAndClose();
            return;
        }
        if ("pip_expand".equals(this.pendingControl) && this.currentUri != null) {
            try {
                moveTaskToBack(false);
            } catch (Exception unused) {
            }
            setFullscreen(true);
        } else if (z || !"toggle".equals(this.pendingControl) || this.currentUri == null) {
            selectAndPlayPrivateAware(this.pendingStep);
        } else {
            toggle();
        }
    }

    private boolean needsPermission() {
        return checkSelfPermission(videoPermission()) != 0;
    }

    private String videoPermission() {
        return Build.VERSION.SDK_INT >= 33 ? "android.permission.READ_MEDIA_VIDEO" : "android.permission.READ_EXTERNAL_STORAGE";
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == REQ_VIDEO && iArr.length > 0 && iArr[0] == 0) {
            selectAndPlayPrivateAware(this.pendingStep);
        } else {
            Toast.makeText(this, "동영상 권한을 허용해 주세요.", 1).show();
            finish();
        }
    }

    private void toggle() {
        VideoView videoView = this.video;
        if (videoView == null || this.currentUri == null) {
            return;
        }
        try {
            if (videoView.isPlaying()) {
                this.video.pause();
            } else {
                this.video.start();
            }
        } catch (Exception unused) {
        }
        updateVideoPlayingPref();
        updatePlayPauseLabel();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePlayPauseLabel() {
        VideoView videoView;
        Button button = this.playPause;
        if (button == null || (videoView = this.video) == null) {
            return;
        }
        try {
            button.setText(videoView.isPlaying() ? "⏸ 일시정지" : "▶ 재생");
        } catch (Exception unused) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:8:0x000d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void updateVideoPlayingPref() {
        /*
            r3 = this;
            r0 = 0
            android.widget.VideoView r1 = r3.video     // Catch: java.lang.Exception -> Ld
            if (r1 == 0) goto Ld
            boolean r1 = r1.isPlaying()     // Catch: java.lang.Exception -> Ld
            if (r1 == 0) goto Ld
            r1 = 1
            goto Le
        Ld:
            r1 = r0
        Le:
            java.lang.String r2 = "overlay_control"
            android.content.SharedPreferences r0 = r3.getSharedPreferences(r2, r0)
            android.content.SharedPreferences$Editor r0 = r0.edit()
            java.lang.String r2 = "video_playing"
            android.content.SharedPreferences$Editor r0 = r0.putBoolean(r2, r1)
            r0.apply()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.baekhak.centralcontrol.LocalMediaPickerActivity.updateVideoPlayingPref():void");
    }

    private void seekBy(int i) {
        VideoView videoView = this.video;
        if (videoView == null || this.currentUri == null) {
            return;
        }
        try {
            int duration = videoView.getDuration();
            int iMax = Math.max(0, this.video.getCurrentPosition() + i);
            if (duration > 0) {
                iMax = Math.min(duration, iMax);
            }
            this.video.seekTo(iMax);
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showControls() {
        LinearLayout linearLayout;
        if (!this.full || (linearLayout = this.fullControls) == null) {
            return;
        }
        linearLayout.setVisibility(0);
        updatePlayPauseLabel();
        this.handler.removeCallbacks(this.hideControls);
        this.handler.postDelayed(this.hideControls, 4000L);
    }

    private void applyScaling() {
        MediaPlayer mediaPlayer = this.preparedPlayer;
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.setVideoScalingMode(1);
        } catch (Exception unused) {
        }
    }

    private void stopAndClose() {
        try {
            VideoView videoView = this.video;
            if (videoView != null) {
                videoView.stopPlayback();
            }
        } catch (Exception unused) {
        }
        getSharedPreferences("overlay_control", 0).edit().putBoolean("video_playing", false).apply();
        finishAndRemoveTask();
    }

    private void setFullscreen(boolean z) {
        VideoView videoView = this.video;
        final int currentPosition = videoView == null ? 0 : videoView.getCurrentPosition();
        VideoView videoView2 = this.video;
        final boolean z2 = videoView2 != null && videoView2.isPlaying();
        this.full = z;
        if (z) {
            try {
                startService(new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_SUSPEND));
            } catch (Exception unused) {
            }
            setRequestedOrientation(6);
            getWindow().getDecorView().setSystemUiVisibility(5894);
            TextView textView = this.title;
            if (textView != null) {
                textView.setVisibility(8);
            }
            Button button = this.fullscreen;
            if (button != null) {
                button.setVisibility(8);
            }
            Button button2 = this.importVideo;
            if (button2 != null) {
                button2.setVisibility(8);
            }
            showControls();
        } else {
            this.handler.removeCallbacks(this.hideControls);
            LinearLayout linearLayout = this.fullControls;
            if (linearLayout != null) {
                linearLayout.setVisibility(8);
            }
            setRequestedOrientation(-1);
            getWindow().getDecorView().setSystemUiVisibility(0);
            TextView textView2 = this.title;
            if (textView2 != null) {
                textView2.setVisibility(0);
            }
            Button button3 = this.fullscreen;
            if (button3 != null) {
                button3.setText("⛶ 전체화면");
                this.fullscreen.setVisibility(0);
            }
            Button button4 = this.importVideo;
            if (button4 != null) {
                button4.setVisibility(0);
            }
            try {
                startService(new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME));
            } catch (Exception unused2) {
            }
        }
        applyScaling();
        VideoView videoView3 = this.video;
        if (videoView3 == null || currentPosition <= 0) {
            return;
        }
        videoView3.postDelayed(new Runnable() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda25
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m15x3d67078f(currentPosition, z2);
            }
        }, 250L);
    }

    /* renamed from: lambda$setFullscreen$11$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m15x3d67078f(int i, boolean z) {
        try {
            this.video.seekTo(i);
            if (z) {
                this.video.start();
            }
            updateVideoPlayingPref();
            updatePlayPauseLabel();
        } catch (Exception unused) {
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (this.full) {
            setFullscreen(false);
        } else {
            stopAndClose();
        }
    }

    private List<VideoLibraryStore.Item> savedForMode() {
        List<VideoLibraryStore.Item> list = new VideoLibraryStore(this).read();
        ArrayList arrayList = new ArrayList();
        for (VideoLibraryStore.Item item : list) {
            String lowerCase = item.name == null ? "" : item.name.toLowerCase(Locale.ROOT);
            if (("MP4".equals(this.mode) && lowerCase.endsWith(".mp4")) || ("AVI".equals(this.mode) && lowerCase.endsWith(".avi"))) {
                arrayList.add(item);
            }
        }
        return arrayList;
    }

    private void selectAndPlayPrivateAware(int i) {
        List<VideoLibraryStore.Item> listSavedForMode = savedForMode();
        if (listSavedForMode.isEmpty()) {
            openVideoPicker();
        } else {
            playPrivate(listSavedForMode, i);
        }
    }

    private void openVideoPicker() {
        try {
            try {
                try {
                    Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA_MULTIPLE");
                    intent.setPackage("com.sec.android.app.myfiles");
                    intent.putExtra("CONTENT_TYPE", "video/*");
                    intent.putExtra("FOLDERPATH", "/storage/emulated/0/Movies");
                    intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                    intent.addFlags(1);
                    intent.addCategory("android.intent.category.DEFAULT");
                    startActivityForResult(intent, REQ_VIDEO_PICK);
                } catch (Exception unused) {
                    Intent intent2 = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                    intent2.setPackage("com.sec.android.app.myfiles");
                    intent2.putExtra("CONTENT_TYPE", "video/*");
                    intent2.putExtra("FOLDERPATH", "/storage/emulated/0/Movies");
                    intent2.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                    intent2.addFlags(1);
                    intent2.addCategory("android.intent.category.DEFAULT");
                    startActivityForResult(intent2, REQ_VIDEO_PICK);
                }
            } catch (Exception unused2) {
                Intent intent3 = new Intent("android.intent.action.OPEN_DOCUMENT");
                intent3.addCategory("android.intent.category.OPENABLE");
                intent3.setType("video/*");
                intent3.putExtra("android.intent.extra.MIME_TYPES", new String[]{"video/mp4", "video/x-msvideo", "video/avi", "video/*"});
                intent3.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
                intent3.addFlags(65);
                if (Build.VERSION.SDK_INT >= 26) {
                    try {
                        intent3.putExtra("android.provider.extra.INITIAL_URI", Uri.parse("content://com.android.externalstorage.documents/document/primary%3AMovies"));
                    } catch (Exception unused3) {
                    }
                }
                startActivityForResult(intent3, REQ_VIDEO_PICK);
            }
        } catch (Exception unused4) {
            Toast.makeText(this, "Movies 영상 선택 화면을 열지 못했습니다.", 1).show();
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != REQ_VIDEO_PICK) {
            return;
        }
        if (i2 != -1 || intent == null) {
            if (this.currentUri == null && savedForMode().isEmpty()) {
                Toast.makeText(this, "불러올 영상을 선택하지 않았습니다.", 0).show();
                return;
            }
            return;
        }
        final ArrayList arrayList = new ArrayList();
        ClipData clipData = intent.getClipData();
        if (clipData != null) {
            for (int i3 = 0; i3 < clipData.getItemCount(); i3++) {
                Uri uri = clipData.getItemAt(i3).getUri();
                if (uri != null) {
                    arrayList.add(uri);
                }
            }
        }
        if (intent.getData() != null) {
            arrayList.add(intent.getData());
        }
        if (arrayList.isEmpty()) {
            Toast.makeText(this, "선택한 영상이 없습니다.", 0).show();
            return;
        }
        try {
            int flags = intent.getFlags();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                try {
                    getContentResolver().takePersistableUriPermission((Uri) it.next(), flags & 1);
                } catch (Exception unused) {
                }
            }
        } catch (Exception unused2) {
        }
        Toast.makeText(this, "선택한 " + this.mode + " " + arrayList.size() + "개를 앱 내부에 보관하는 중입니다.", 0).show();
        new Thread(new Runnable() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m13x8d485baa(arrayList);
            }
        }, "Manual-Video-PrivateCopy").start();
    }

    /* renamed from: lambda$onActivityResult$14$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m13x8d485baa(ArrayList arrayList) {
        try {
            final int iAppend = new VideoLibraryStore(this).append(arrayList);
            runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda11
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m11x72d2f8a8(iAppend);
                }
            });
        } catch (Exception unused) {
            runOnUiThread(new Runnable() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda12
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m12xdaa29();
                }
            });
        }
    }

    /* renamed from: lambda$onActivityResult$12$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m11x72d2f8a8(int i) {
        List<VideoLibraryStore.Item> listSavedForMode = savedForMode();
        Toast.makeText(this, this.mode + " 내부보관 완료 · 새로 " + i + "개", 1).show();
        if (listSavedForMode.isEmpty()) {
            return;
        }
        playPrivate(listSavedForMode, 0);
    }

    /* renamed from: lambda$onActivityResult$13$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m12xdaa29() {
        Toast.makeText(this, "영상 내부보관에 실패했습니다. 원본은 변경되지 않았습니다.", 1).show();
    }

    private void playPrivate(List<VideoLibraryStore.Item> list, int i) {
        if (list == null || list.isEmpty()) {
            openVideoPicker();
            return;
        }
        String str = "video_private_index_" + this.mode;
        int size = getSharedPreferences("overlay_control", 0).getInt(str, 0);
        if (i != 0) {
            size = ((size + i) + list.size()) % list.size();
        }
        if (size < 0 || size >= list.size()) {
            size = 0;
        }
        VideoLibraryStore.Item item = list.get(size);
        this.currentUri = Uri.fromFile(new File(item.path));
        getSharedPreferences("overlay_control", 0).edit().putInt(str, size).putString("media_mode", this.mode).putString("media_title", item.name).apply();
        this.title.setText(this.mode + " · " + item.name);
        this.video.stopPlayback();
        this.preparedPlayer = null;
        this.video.setVideoPath(item.path);
        this.video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.baekhak.centralcontrol.LocalMediaPickerActivity$$ExternalSyntheticLambda13
            @Override // android.media.MediaPlayer.OnPreparedListener
            public final void onPrepared(MediaPlayer mediaPlayer) {
                this.f$0.m14xdfbaa2a1(mediaPlayer);
            }
        });
    }

    /* renamed from: lambda$playPrivate$15$com-baekhak-centralcontrol-LocalMediaPickerActivity, reason: not valid java name */
    /* synthetic */ void m14xdfbaa2a1(MediaPlayer mediaPlayer) {
        this.preparedPlayer = mediaPlayer;
        mediaPlayer.setLooping(false);
        applyScaling();
        updatePipParams();
        this.video.start();
        updateVideoPlayingPref();
        updatePlayPauseLabel();
        if (this.full) {
            showControls();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        this.handler.removeCallbacksAndMessages(null);
        VideoView videoView = this.video;
        if (videoView != null) {
            videoView.stopPlayback();
        }
        getSharedPreferences("overlay_control", 0).edit().putBoolean("video_playing", false).apply();
        try {
            startService(new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME));
        } catch (Exception unused) {
        }
        super.onDestroy();
    }
}
