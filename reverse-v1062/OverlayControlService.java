package com.baekhak.centralcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class OverlayControlService extends Service {
    public static final String ACTION_HIDE = "com.baekhak.centralcontrol.overlay.HIDE";
    public static final String ACTION_REFRESH = "com.baekhak.centralcontrol.overlay.REFRESH";
    public static final String ACTION_RESUME = "com.baekhak.centralcontrol.overlay.RESUME";
    public static final String ACTION_SHOW = "com.baekhak.centralcontrol.overlay.SHOW";
    public static final String ACTION_SUSPEND = "com.baekhak.centralcontrol.overlay.SUSPEND";
    private static final String CHANNEL = "v3_overlay_control";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_X = "last_x";
    private static final String KEY_Y = "last_y";
    private static final int NOTICE = 43140;
    private static final String PREFS = "overlay_control";
    private TextView anchorView;
    private LinearLayout buttonGroup;
    private WindowManager.LayoutParams channelLp;
    private LinearLayout channelPanel;
    private TextView currentTrackView;
    private WindowManager.LayoutParams overlayLp;
    private LinearLayout playlistPanel;
    private LinearLayout root;
    private LinearLayout topMediaBar;
    private WindowManager.LayoutParams topMediaLp;
    private WindowManager wm;
    private static final String[][] FM_STATIONS = {new String[]{"KBS 클래식FM", "https://radio.bsod.kr/stream?stn=kbs&ch=1fm"}, new String[]{"MBC FM4U", "https://radio.bsod.kr/stream?stn=mbc&ch=fm4u"}, new String[]{"SBS 파워FM", "https://radio.bsod.kr/stream?stn=sbs&ch=powerfm"}, new String[]{"KBS 해피FM", "https://radio.bsod.kr/stream?stn=kbs&ch=2radio"}};
    private static final String[][] AM_STATIONS = {new String[]{"KBS 1라디오 · 뉴스/시사", "https://radio.bsod.kr/stream?stn=kbs&ch=1radio"}, new String[]{"MBC 표준FM · 뉴스/교양", "https://radio.bsod.kr/stream?stn=mbc&ch=sfm"}, new String[]{"SBS 러브FM · 뉴스/생활", "https://radio.bsod.kr/stream?stn=sbs&ch=lovefm"}};
    private boolean expanded = false;
    private boolean suspended = false;
    private String currentMediaMode = "MP3";
    private String currentRadioName = "";
    private String currentRadioUrl = "";
    private int currentRadioIndex = 0;
    private boolean radioPlaying = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable musicStateTicker = new Runnable() { // from class: com.baekhak.centralcontrol.OverlayControlService.1
        @Override // java.lang.Runnable
        public void run() {
            OverlayControlService.this.updateCurrentTrackLabel();
            OverlayControlService.this.handler.postDelayed(this, 850L);
        }
    };
    private final Runnable permissionWatcher = new Runnable() { // from class: com.baekhak.centralcontrol.OverlayControlService.2
        @Override // java.lang.Runnable
        public void run() throws PackageManager.NameNotFoundException {
            if (OverlayControlService.this.suspended) {
                OverlayControlService.this.removeOverlay();
                return;
            }
            if (!OverlayControlService.this.getSharedPreferences(OverlayControlService.PREFS, 0).getBoolean(OverlayControlService.KEY_ENABLED, true)) {
                OverlayControlService.this.removeOverlay();
                OverlayControlService.this.stopSelf();
            } else if (Settings.canDrawOverlays(OverlayControlService.this)) {
                OverlayControlService.this.showOverlay();
            } else {
                OverlayControlService.this.removeOverlay();
                OverlayControlService.this.handler.postDelayed(this, 700L);
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(NOTICE, serviceNotification());
        this.wm = (WindowManager) getSystemService("window");
        this.currentMediaMode = getSharedPreferences(PREFS, 0).getString("media_mode", "MP3");
        if (getSharedPreferences(PREFS, 0).getBoolean(KEY_ENABLED, true)) {
            this.handler.post(this.permissionWatcher);
        }
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        String action = intent == null ? ACTION_SHOW : intent.getAction();
        if (ACTION_SUSPEND.equals(action)) {
            this.suspended = true;
            removeOverlay();
            return 1;
        }
        if (ACTION_RESUME.equals(action)) {
            this.suspended = false;
            this.handler.removeCallbacks(this.permissionWatcher);
            this.handler.post(this.permissionWatcher);
            return 1;
        }
        if (ACTION_REFRESH.equals(action)) {
            removeOverlay();
            this.handler.removeCallbacks(this.permissionWatcher);
            this.handler.post(this.permissionWatcher);
            return 1;
        }
        if (ACTION_HIDE.equals(action)) {
            getSharedPreferences(PREFS, 0).edit().putBoolean(KEY_ENABLED, false).apply();
            this.handler.removeCallbacksAndMessages(null);
            removeOverlay();
            try {
                stopForeground(true);
            } catch (Exception unused) {
            }
            stopSelf();
            return 2;
        }
        getSharedPreferences(PREFS, 0).edit().putBoolean(KEY_ENABLED, true).apply();
        this.handler.removeCallbacks(this.permissionWatcher);
        this.handler.post(this.permissionWatcher);
        return 1;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannelM = MainActivity$$ExternalSyntheticApiModelOutline0.m(CHANNEL, "항행의자유 상단 미니바", 2);
            notificationChannelM.setDescription("MP3·FM·AM·유튜브 상시 제어 이동아이콘");
            notificationChannelM.setSound(null, null);
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannelM);
            }
        }
    }

    private Notification serviceNotification() {
        Notification.Builder builderM = Build.VERSION.SDK_INT >= 26 ? MainActivity$$ExternalSyntheticApiModelOutline0.m(this, CHANNEL) : new Notification.Builder(this);
        builderM.setSmallIcon(R.drawable.v3_ship_icon).setContentTitle("항행의자유 미니바 사용 중").setContentText("MP3 · FM · AM · 유튜브를 이동아이콘에서 바로 사용합니다.").setOngoing(true).setOnlyAlertOnce(true).setCategory("service");
        Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launchIntentForPackage != null) {
            builderM.setContentIntent(PendingIntent.getActivity(this, NOTICE, launchIntentForPackage, 201326592));
        }
        return builderM.build();
    }

    private int dp(int i) {
        return Math.max(1, Math.round(i * getResources().getDisplayMetrics().density));
    }

    private GradientDrawable bg(int i, int i2, int i3) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(i3));
        gradientDrawable.setStroke(dp(1), i2);
        return gradientDrawable;
    }

    private Button button(String str, int i) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextColor(-1);
        button.setTextSize(11.5f);
        button.setAllCaps(false);
        button.setGravity(17);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setBackground(bg(i, 1728041818, 10));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(112), dp(38));
        layoutParams.setMargins(dp(2), dp(2), dp(2), dp(2));
        button.setLayoutParams(layoutParams);
        return button;
    }

    private TextView stationButton(String str) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(-1);
        textView.setTextSize(12.0f);
        textView.setGravity(16);
        textView.setPadding(dp(12), dp(9), dp(12), dp(9));
        textView.setBackground(bg(-301456578, 1719189247, 10));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(dp(3), dp(2), dp(3), dp(2));
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showOverlay() throws PackageManager.NameNotFoundException {
        if (this.root != null || this.wm == null) {
            return;
        }
        LinearLayout linearLayout = new LinearLayout(this);
        this.root = linearLayout;
        linearLayout.setOrientation(0);
        this.root.setGravity(48);
        this.root.setPadding(dp(4), dp(4), dp(4), dp(4));
        this.root.setBackground(bg(-234218192, -856438444, 14));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setGravity(1);
        this.root.addView(linearLayout2);
        TextView textView = new TextView(this);
        this.anchorView = textView;
        textView.setText("⚓");
        textView.setTextSize(22.0f);
        textView.setTextColor(-9878);
        textView.setGravity(17);
        textView.setBackground(bg(-301456578, -856438444, 12));
        textView.setLayoutParams(new LinearLayout.LayoutParams(dp(112), dp(42)));
        linearLayout2.addView(textView);
        LinearLayout linearLayout3 = new LinearLayout(this);
        this.buttonGroup = linearLayout3;
        linearLayout3.setOrientation(1);
        this.buttonGroup.setGravity(1);
        this.buttonGroup.setVisibility(8);
        Button button = button("항행의자유", -299486664);
        Button button2 = button("항해사앱", -299486664);
        Button button3 = button("분석방 V4", -299486664);
        Button button4 = button("ChatGPT", -299486664);
        Button button5 = button("🎵 MP3", -300914488);
        Button button6 = button("📻 FM", -296012586);
        Button button7 = button("📡 AM", -294834761);
        Button button8 = button("유튜브", -287629528);
        Button button9 = button("AVI", -298367153);
        Button button10 = button("MP4", -298367153);
        Button button11 = button("아이콘 숨김", -297118360);
        Button button12 = button("■ 종료", -288019130);
        Button button13 = button("업데이트", -299988763);
        Button[] buttonArr = {button, button2, button3, button4, button5, button6, button7, button8, button9, button10};
        int i = 0;
        for (int i2 = 10; i < i2; i2 = 10) {
            this.buttonGroup.addView(buttonArr[i]);
            i++;
            buttonArr = buttonArr;
        }
        addCustomAppButtons(this.buttonGroup);
        this.buttonGroup.addView(button11);
        this.buttonGroup.addView(button12);
        this.buttonGroup.addView(button13);
        linearLayout2.addView(this.buttonGroup);
        createTopMediaBar();
        this.handler.removeCallbacks(this.musicStateTicker);
        this.handler.post(this.musicStateTicker);
        LinearLayout linearLayout4 = new LinearLayout(this);
        this.channelPanel = linearLayout4;
        linearLayout4.setOrientation(1);
        this.channelPanel.setPadding(dp(4), dp(2), dp(4), dp(2));
        this.channelPanel.setMinimumWidth(dp(155));
        this.channelPanel.setVisibility(8);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m104x6b944491(view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda10
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m105x5d3deab0(view);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m116x4ee790cf(view);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda12
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m117x409136ee(view);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda13
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m119x23e4832c(view);
            }
        });
        button6.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda14
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m121x737cf6a(view);
            }
        });
        button7.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda15
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m123xea8b1ba8(view);
            }
        });
        button8.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda16
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m106x99a877e6(view);
            }
        });
        button9.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda17
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m108x7cfbc424(view);
            }
        });
        button10.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda18
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m110x604f1062(view);
            }
        });
        button11.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m111x51f8b681(view);
            }
        });
        button13.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m112x43a25ca0(view);
            }
        });
        button12.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda7
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m113x354c02bf(view);
            }
        });
        if (Build.VERSION.SDK_INT >= 26) {
            this.overlayLp = new WindowManager.LayoutParams(-2, -2, 2038, 264, -3);
        } else {
            this.overlayLp = new WindowManager.LayoutParams(-2, -2, 2002, 264, -3);
        }
        this.overlayLp.gravity = 8388659;
        if (Build.VERSION.SDK_INT >= 26) {
            this.channelLp = new WindowManager.LayoutParams(dp(180), -2, 2038, 264, -3);
        } else {
            this.channelLp = new WindowManager.LayoutParams(dp(180), -2, 2002, 264, -3);
        }
        this.channelLp.gravity = 8388659;
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS, 0);
        this.overlayLp.x = sharedPreferences.getInt(KEY_X, dp(8));
        this.overlayLp.y = sharedPreferences.getInt(KEY_Y, dp(12));
        final float[] fArr = new float[2];
        final int[] iArr = new int[2];
        final int[] iArr2 = new int[1];
        final boolean[] zArr = new boolean[1];
        final long[] jArr = new long[1];
        textView.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda8
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return this.f$0.m114x26f5a8de(view);
            }
        });
        textView.setOnTouchListener(new View.OnTouchListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda9
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.m115x189f4efd(fArr, iArr, iArr2, zArr, jArr, view, motionEvent);
            }
        });
        try {
            this.wm.addView(this.root, this.overlayLp);
        } catch (Exception unused) {
            this.root = null;
            this.channelPanel = null;
            this.buttonGroup = null;
            this.anchorView = null;
            this.overlayLp = null;
        }
    }

    /* renamed from: lambda$showOverlay$0$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m104x6b944491(View view) {
        try {
            Intent intent = new Intent(this, (Class<?>) MainActivity.class);
            intent.putExtra("v3_play_install_guide", true);
            intent.addFlags(872415232);
            startActivity(intent);
        } catch (Exception unused) {
            toast("항행의자유를 열 수 없습니다.");
        }
    }

    /* renamed from: lambda$showOverlay$1$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m105x5d3deab0(View view) {
        launchNavigatorApp();
    }

    /* renamed from: lambda$showOverlay$2$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m116x4ee790cf(View view) {
        launchExplicitOrPackage("com.navigator.analysis.v4", "com.navigator.analysis.v4.MainActivity", "분석방 V4를 실행할 수 없습니다.");
    }

    /* renamed from: lambda$showOverlay$3$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m117x409136ee(View view) {
        launchPackageOrUrl("com.openai.chatgpt", "https://chatgpt.com");
    }

    /* renamed from: lambda$showOverlay$5$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m119x23e4832c(View view) {
        safeMediaClick(new Runnable() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda30
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m118x323add0d();
            }
        }, "MP3 불러오기를 열지 못했습니다.");
    }

    /* renamed from: lambda$showOverlay$4$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m118x323add0d() {
        stopVideoSafely();
        stopRadioSafely();
        collapseStations();
        setMediaMode("MP3", "");
        Intent intent = new Intent(this, (Class<?>) MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_OPEN_SAVED_MUSIC, true);
        intent.addFlags(872415232);
        startActivity(intent);
    }

    /* renamed from: lambda$showOverlay$6$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m120x158e294b() {
        stopVideoSafely();
        switchRadioMode("FM");
    }

    /* renamed from: lambda$showOverlay$7$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m121x737cf6a(View view) {
        safeMediaClick(new Runnable() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m120x158e294b();
            }
        }, "FM을 실행하지 못했습니다.");
    }

    /* renamed from: lambda$showOverlay$8$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m122xf8e17589() {
        stopVideoSafely();
        switchRadioMode("AM");
    }

    /* renamed from: lambda$showOverlay$9$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m123xea8b1ba8(View view) {
        safeMediaClick(new Runnable() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda19
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m122xf8e17589();
            }
        }, "AM을 실행하지 못했습니다.");
    }

    /* renamed from: lambda$showOverlay$10$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m106x99a877e6(View view) {
        launchPackageOrUrl("com.google.android.youtube", "https://www.youtube.com");
    }

    /* renamed from: lambda$showOverlay$11$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m107x8b521e05() {
        stopAllAudioForMediaSwitch();
        setMediaMode("AVI", "Movies 자동검색");
        openVideo("video/x-msvideo");
    }

    /* renamed from: lambda$showOverlay$12$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m108x7cfbc424(View view) {
        safeMediaClick(new Runnable() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda28
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m107x8b521e05();
            }
        }, "AVI를 실행하지 못했습니다.");
    }

    /* renamed from: lambda$showOverlay$13$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m109x6ea56a43() {
        stopAllAudioForMediaSwitch();
        setMediaMode("MP4", "Movies 자동검색");
        openVideo("video/mp4");
    }

    /* renamed from: lambda$showOverlay$14$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m110x604f1062(View view) {
        safeMediaClick(new Runnable() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda21
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m109x6ea56a43();
            }
        }, "MP4를 실행하지 못했습니다.");
    }

    /* renamed from: lambda$showOverlay$15$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m111x51f8b681(View view) {
        getSharedPreferences(PREFS, 0).edit().putBoolean(KEY_ENABLED, false).apply();
        this.handler.removeCallbacksAndMessages(null);
        removeOverlay();
        stopSelf();
    }

    /* renamed from: lambda$showOverlay$16$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m112x43a25ca0(View view) {
        try {
            this.suspended = true;
            removeOverlay();
            Intent intent = new Intent(this, (Class<?>) OverlayUpdateActivity.class);
            intent.addFlags(268435456);
            startActivity(intent);
        } catch (Exception unused) {
            this.suspended = false;
            toast("업데이트 화면을 열 수 없습니다.");
        }
    }

    /* renamed from: lambda$showOverlay$17$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m113x354c02bf(View view) {
        stopRadio();
        try {
            Intent action = new Intent(this, (Class<?>) MusicPlaybackService.class).setAction(MusicPlaybackService.ACTION_STOP);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(action);
            } else {
                startService(action);
            }
        } catch (Exception unused) {
        }
        collapseStations();
        this.expanded = false;
        LinearLayout linearLayout = this.buttonGroup;
        if (linearLayout != null) {
            linearLayout.setVisibility(8);
        }
        toast("음악 · 라디오를 종료했습니다.");
    }

    /* renamed from: lambda$showOverlay$18$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ boolean m114x26f5a8de(View view) {
        try {
            Intent intent = new Intent(this, (Class<?>) OverlayAppEditorActivity.class);
            intent.addFlags(268435456);
            startActivity(intent);
            return true;
        } catch (Exception unused) {
            toast("아이콘 편집을 열 수 없습니다.");
            return true;
        }
    }

    /* renamed from: lambda$showOverlay$19$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ boolean m115x189f4efd(float[] fArr, int[] iArr, int[] iArr2, boolean[] zArr, long[] jArr, View view, MotionEvent motionEvent) {
        if (this.overlayLp == null || this.wm == null) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            fArr[0] = motionEvent.getRawX();
            fArr[1] = motionEvent.getRawY();
            iArr[0] = this.overlayLp.x;
            iArr[1] = this.overlayLp.y;
            WindowManager.LayoutParams layoutParams = this.topMediaLp;
            iArr2[0] = layoutParams == null ? 0 : layoutParams.y;
            zArr[0] = false;
            jArr[0] = System.currentTimeMillis();
            return true;
        }
        if (actionMasked == 1) {
            if (zArr[0]) {
                SharedPreferences.Editor editorPutInt = getSharedPreferences(PREFS, 0).edit().putInt(KEY_X, this.overlayLp.x).putInt(KEY_Y, this.overlayLp.y);
                WindowManager.LayoutParams layoutParams2 = this.topMediaLp;
                if (layoutParams2 != null) {
                    editorPutInt.putInt("top_bar_y", layoutParams2.y);
                }
                editorPutInt.apply();
            } else if (System.currentTimeMillis() - jArr[0] >= 650) {
                try {
                    Intent intent = new Intent(this, (Class<?>) OverlayAppEditorActivity.class);
                    intent.addFlags(268435456);
                    startActivity(intent);
                } catch (Exception unused) {
                    toast("아이콘 편집을 열 수 없습니다.");
                }
            } else {
                toggleExpanded();
            }
            return true;
        }
        if (actionMasked != 2) {
            return false;
        }
        int rawX = (int) (motionEvent.getRawX() - fArr[0]);
        int rawY = (int) (motionEvent.getRawY() - fArr[1]);
        if (Math.abs(rawX) > dp(5) || Math.abs(rawY) > dp(5)) {
            zArr[0] = true;
        }
        if (zArr[0]) {
            this.overlayLp.x = Math.max(0, iArr[0] + rawX);
            this.overlayLp.y = Math.max(0, iArr[1] + rawY);
            try {
                this.wm.updateViewLayout(this.root, this.overlayLp);
            } catch (Exception unused2) {
            }
            WindowManager.LayoutParams layoutParams3 = this.topMediaLp;
            if (layoutParams3 != null && this.topMediaBar != null) {
                layoutParams3.y = Math.max(0, iArr2[0] + rawY);
                try {
                    this.wm.updateViewLayout(this.topMediaBar, this.topMediaLp);
                } catch (Exception unused3) {
                }
            }
            updateSidePanelPosition();
        }
        return true;
    }

    private void createTopMediaBar() {
        if (this.wm == null || this.topMediaBar != null) {
            return;
        }
        LinearLayout linearLayout = new LinearLayout(this);
        this.topMediaBar = linearLayout;
        linearLayout.setOrientation(1);
        this.topMediaBar.setGravity(16);
        this.topMediaBar.setPadding(dp(7), dp(4), dp(7), dp(4));
        this.topMediaBar.setBackground(bg(-234218192, -856438444, 10));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(17);
        TextView textView = topControl("⏮ 이전곡");
        TextView textView2 = topControl("▶ 재생/일시정지");
        TextView textView3 = topControl("다음곡 ⏭");
        textView.setTag("prev_control");
        textView2.setTag("play_control");
        textView3.setTag("next_control");
        linearLayout2.addView(textView);
        linearLayout2.addView(textView2);
        linearLayout2.addView(textView3);
        this.topMediaBar.addView(linearLayout2, new LinearLayout.LayoutParams(-1, dp(38)));
        TextView textView4 = new TextView(this);
        this.currentTrackView = textView4;
        textView4.setTextColor(-6774);
        this.currentTrackView.setTextSize(13.5f);
        this.currentTrackView.setTypeface(Typeface.DEFAULT_BOLD);
        this.currentTrackView.setSingleLine(true);
        this.currentTrackView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.currentTrackView.setMarqueeRepeatLimit(-1);
        this.currentTrackView.setSelected(true);
        this.currentTrackView.setGravity(16);
        this.currentTrackView.setPadding(dp(9), 0, dp(9), 0);
        this.topMediaBar.addView(this.currentTrackView, new LinearLayout.LayoutParams(-1, dp(34)));
        LinearLayout linearLayout3 = new LinearLayout(this);
        this.playlistPanel = linearLayout3;
        linearLayout3.setVisibility(8);
        this.topMediaBar.addView(this.playlistPanel, new LinearLayout.LayoutParams(1, 1));
        textView.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda24
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m100x94fcafbc(view);
            }
        });
        textView3.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda25
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m101x86a655db(view);
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda26
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m102x784ffbfa(view);
            }
        });
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, dp(80), Build.VERSION.SDK_INT >= 26 ? 2038 : 2002, 264, -3);
        this.topMediaLp = layoutParams;
        layoutParams.gravity = 8388659;
        this.topMediaLp.x = 0;
        this.topMediaLp.y = getSharedPreferences(PREFS, 0).getInt("top_bar_y", 0);
        final float[] fArr = new float[2];
        final int[] iArr = new int[1];
        final int[] iArr2 = new int[1];
        final boolean[] zArr = new boolean[1];
        this.topMediaBar.setOnTouchListener(new View.OnTouchListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda27
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return this.f$0.m103x69f9a219(fArr, iArr, iArr2, zArr, view, motionEvent);
            }
        });
        updateControlLabels();
        try {
            this.wm.addView(this.topMediaBar, this.topMediaLp);
        } catch (Exception unused) {
            this.topMediaBar = null;
            this.currentTrackView = null;
            this.playlistPanel = null;
            this.topMediaLp = null;
        }
    }

    /* renamed from: lambda$createTopMediaBar$20$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m100x94fcafbc(View view) {
        mediaPrevious();
    }

    /* renamed from: lambda$createTopMediaBar$21$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m101x86a655db(View view) {
        mediaNext();
    }

    /* renamed from: lambda$createTopMediaBar$22$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m102x784ffbfa(View view) {
        mediaPlayPause();
    }

    /* renamed from: lambda$createTopMediaBar$23$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ boolean m103x69f9a219(float[] fArr, int[] iArr, int[] iArr2, boolean[] zArr, View view, MotionEvent motionEvent) {
        if (this.topMediaLp != null && this.wm != null) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != 0) {
                if (actionMasked == 1) {
                    if (!zArr[0]) {
                        return false;
                    }
                    SharedPreferences.Editor editorPutInt = getSharedPreferences(PREFS, 0).edit().putInt("top_bar_y", this.topMediaLp.y);
                    WindowManager.LayoutParams layoutParams = this.overlayLp;
                    if (layoutParams != null) {
                        editorPutInt.putInt(KEY_X, layoutParams.x).putInt(KEY_Y, this.overlayLp.y);
                    }
                    editorPutInt.apply();
                    return true;
                }
                if (actionMasked != 2) {
                    return false;
                }
                int iRound = Math.round(motionEvent.getRawY() - fArr[1]);
                if (Math.abs(iRound) <= dp(7)) {
                    return false;
                }
                zArr[0] = true;
                this.topMediaLp.y = Math.max(0, iArr[0] + iRound);
                try {
                    this.wm.updateViewLayout(this.topMediaBar, this.topMediaLp);
                } catch (Exception unused) {
                }
                WindowManager.LayoutParams layoutParams2 = this.overlayLp;
                if (layoutParams2 != null && this.root != null) {
                    layoutParams2.y = Math.max(0, iArr2[0] + iRound);
                    try {
                        this.wm.updateViewLayout(this.root, this.overlayLp);
                    } catch (Exception unused2) {
                    }
                    updateSidePanelPosition();
                }
                return true;
            }
            fArr[0] = motionEvent.getRawX();
            fArr[1] = motionEvent.getRawY();
            iArr[0] = this.topMediaLp.y;
            WindowManager.LayoutParams layoutParams3 = this.overlayLp;
            iArr2[0] = layoutParams3 == null ? 0 : layoutParams3.y;
            zArr[0] = false;
        }
        return false;
    }

    private TextView topControl(String str) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(-1);
        textView.setTextSize(12.0f);
        textView.setGravity(17);
        textView.setBackground(bg(-300533419, -2005352705, 8));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(34), 1.0f);
        layoutParams.setMargins(dp(2), dp(2), dp(2), dp(2));
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private void sendMusicAction(String str) {
        this.currentMediaMode = "MP3";
        try {
            Intent action = new Intent(this, (Class<?>) MusicPlaybackService.class).setAction(str);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(action);
            } else {
                startService(action);
            }
        } catch (Exception unused) {
            toast("MP3 조작을 실행하지 못했습니다.");
        }
    }

    private void setMediaMode(String str, String str2) {
        this.currentMediaMode = str;
        getSharedPreferences(PREFS, 0).edit().putString("media_mode", str).putString("media_title", str2 == null ? "" : str2).apply();
        TextView textView = this.currentTrackView;
        if (textView != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" · ");
            if (str2 == null || str2.isEmpty()) {
                str2 = "자동 연결";
            }
            sb.append(str2);
            textView.setText(sb.toString());
        }
        updateControlLabels();
    }

    private void updateControlLabels() {
        LinearLayout linearLayout = this.topMediaBar;
        if (linearLayout == null) {
            return;
        }
        boolean zOptBoolean = false;
        View childAt = linearLayout.getChildAt(0);
        if (childAt instanceof LinearLayout) {
            LinearLayout linearLayout2 = (LinearLayout) childAt;
            if (linearLayout2.getChildCount() < 3) {
                return;
            }
            TextView textView = (TextView) linearLayout2.getChildAt(0);
            TextView textView2 = (TextView) linearLayout2.getChildAt(1);
            TextView textView3 = (TextView) linearLayout2.getChildAt(2);
            if ("FM".equals(this.currentMediaMode) || "AM".equals(this.currentMediaMode)) {
                textView.setText("⏮ 이전채널");
                textView3.setText("다음채널 ⏭");
            } else if ("MP4".equals(this.currentMediaMode) || "AVI".equals(this.currentMediaMode)) {
                textView.setText("⏮ 이전영상");
                textView3.setText("다음영상 ⏭");
            } else {
                textView.setText("⏮ 이전곡");
                textView3.setText("다음곡 ⏭");
            }
            if ("FM".equals(this.currentMediaMode) || "AM".equals(this.currentMediaMode)) {
                zOptBoolean = this.radioPlaying;
            } else if ("MP3".equals(this.currentMediaMode)) {
                try {
                    zOptBoolean = new JSONObject(MusicPlaybackService.snapshot(this)).optBoolean("playing", false);
                } catch (Exception unused) {
                }
            } else if ("MP4".equals(this.currentMediaMode) || "AVI".equals(this.currentMediaMode)) {
                zOptBoolean = getSharedPreferences(PREFS, 0).getBoolean("video_playing", false);
            }
            textView2.setText(zOptBoolean ? "■ 중지" : "▶ 재생");
        }
    }

    private void switchRadioMode(String str) {
        stopMusicForMediaSwitch();
        stopRadioSafely();
        collapseStations();
        String[][] strArr = "AM".equals(str) ? AM_STATIONS : FM_STATIONS;
        int iMax = Math.max(0, Math.min(getSharedPreferences(PREFS, 0).getInt("radio_index_" + str, 0), strArr.length - 1));
        this.currentRadioIndex = iMax;
        String[] strArr2 = strArr[iMax];
        playRadioAs(str, strArr2[0], strArr2[1]);
    }

    private void radioStep(int i) {
        String str = "AM".equals(this.currentMediaMode) ? "AM" : "FM";
        String[][] strArr = "AM".equals(str) ? AM_STATIONS : FM_STATIONS;
        this.currentRadioIndex = ((this.currentRadioIndex + i) + strArr.length) % strArr.length;
        getSharedPreferences(PREFS, 0).edit().putInt("radio_index_".concat(str), this.currentRadioIndex).apply();
        stopRadioSafely();
        String[] strArr2 = strArr[this.currentRadioIndex];
        playRadioAs(str, strArr2[0], strArr2[1]);
    }

    private void mediaPrevious() {
        if ("MP3".equals(this.currentMediaMode)) {
            sendMusicAction(MusicPlaybackService.ACTION_PREV);
            return;
        }
        if ("FM".equals(this.currentMediaMode) || "AM".equals(this.currentMediaMode)) {
            radioStep(-1);
        } else if ("MP4".equals(this.currentMediaMode) || "AVI".equals(this.currentMediaMode)) {
            openVideoStep(this.currentMediaMode, -1);
        }
    }

    private void mediaNext() {
        if ("MP3".equals(this.currentMediaMode)) {
            sendMusicAction(MusicPlaybackService.ACTION_NEXT);
            return;
        }
        if ("FM".equals(this.currentMediaMode) || "AM".equals(this.currentMediaMode)) {
            radioStep(1);
        } else if ("MP4".equals(this.currentMediaMode) || "AVI".equals(this.currentMediaMode)) {
            openVideoStep(this.currentMediaMode, 1);
        }
    }

    private void mediaPlayPause() {
        if ("FM".equals(this.currentMediaMode) || "AM".equals(this.currentMediaMode)) {
            if (this.radioPlaying) {
                stopRadioSafely();
                setMediaMode(this.currentMediaMode, this.currentRadioName + " · 정지");
            } else {
                String str = this.currentMediaMode;
                String[][] strArr = "AM".equals(str) ? AM_STATIONS : FM_STATIONS;
                String str2 = this.currentRadioName;
                if (str2 == null || str2.isEmpty()) {
                    String[] strArr2 = strArr[Math.max(0, Math.min(this.currentRadioIndex, strArr.length - 1))];
                    this.currentRadioName = strArr2[0];
                    this.currentRadioUrl = strArr2[1];
                }
                playRadioAs(str, this.currentRadioName, this.currentRadioUrl);
            }
            updateControlLabels();
            return;
        }
        if ("MP4".equals(this.currentMediaMode) || "AVI".equals(this.currentMediaMode)) {
            if (getSharedPreferences(PREFS, 0).getBoolean("video_playing", false)) {
                try {
                    Intent intent = new Intent(this, (Class<?>) LocalMediaPickerActivity.class);
                    intent.putExtra(LocalMediaPickerActivity.EXTRA_MIME, "AVI".equals(this.currentMediaMode) ? "video/x-msvideo" : "video/mp4");
                    intent.putExtra("control", "stop");
                    intent.addFlags(805306368);
                    startActivity(intent);
                } catch (Exception unused) {
                }
                getSharedPreferences(PREFS, 0).edit().putBoolean("video_playing", false).apply();
            } else {
                openVideoStep(this.currentMediaMode, 0);
            }
            updateControlLabels();
            return;
        }
        try {
            if (new JSONObject(MusicPlaybackService.snapshot(this)).optBoolean("playing", false)) {
                sendMusicAction(MusicPlaybackService.ACTION_STOP);
                TextView textView = this.currentTrackView;
                if (textView != null) {
                    textView.setSelected(false);
                    this.currentTrackView.setText("MP3 · 정지");
                }
            } else {
                sendMusicAction(MusicPlaybackService.ACTION_PLAY);
            }
        } catch (Exception unused2) {
            sendMusicAction(MusicPlaybackService.ACTION_PLAY);
        }
        updateControlLabels();
    }

    private void stopVideoSafely() {
        try {
            Intent intent = new Intent(this, (Class<?>) LocalMediaPickerActivity.class);
            intent.putExtra("control", "stop");
            intent.addFlags(872415232);
            startActivity(intent);
        } catch (Exception unused) {
        }
        getSharedPreferences(PREFS, 0).edit().putBoolean("video_playing", false).apply();
    }

    private void openVideoStep(String str, int i) {
        try {
            Intent intent = new Intent(this, (Class<?>) LocalMediaPickerActivity.class);
            intent.putExtra(LocalMediaPickerActivity.EXTRA_MIME, "AVI".equals(str) ? "video/x-msvideo" : "video/mp4");
            intent.putExtra("step", i);
            intent.putExtra("control", i == 0 ? "toggle" : "step");
            intent.addFlags(805306368);
            startActivity(intent);
        } catch (Exception unused) {
            toast("동영상을 열 수 없습니다.");
        }
    }

    private void addCustomAppButtons(LinearLayout linearLayout) throws PackageManager.NameNotFoundException {
        String string = getSharedPreferences(PREFS, 0).getString("custom_apps", "");
        if (string == null || string.trim().isEmpty()) {
            return;
        }
        for (String str : string.split("\\|")) {
            final String strTrim = str.trim();
            if (!strTrim.isEmpty()) {
                try {
                    Button button = button(String.valueOf(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(strTrim, 0))), -297444764);
                    button.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda29
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            this.f$0.m99xdd00ce42(strTrim, view);
                        }
                    });
                    linearLayout.addView(button);
                } catch (Exception unused) {
                }
            }
        }
    }

    /* renamed from: lambda$addCustomAppButtons$24$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m99xdd00ce42(String str, View view) {
        launchPackage(str, "앱을 실행할 수 없습니다.");
    }

    private void launchUrl(String str, String str2) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(str));
            intent.addFlags(268435456);
            startActivity(intent);
        } catch (Exception unused) {
            toast(str2);
        }
    }

    private void launchExplicitOrPackage(String str, String str2, String str3) {
        try {
            Intent intent = new Intent();
            intent.setClassName(str, str2);
            intent.addFlags(268435456);
            startActivity(intent);
        } catch (Exception unused) {
            launchPackage(str, str3);
        }
    }

    private void launchNavigatorApp() {
        Intent launchIntentForPackage;
        String[] strArr = {"com.baekhak.dosa.v3", "com.baekhak.centralcontrol", "com.baekhak.controlcenter.v2", "com.openai.hanghae.mini"};
        for (int i = 0; i < 4; i++) {
            String str = strArr[i];
            try {
                if (!getPackageName().equals(str) && (launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(str)) != null) {
                    launchIntentForPackage.addFlags(270532608);
                    startActivity(launchIntentForPackage);
                    return;
                }
            } catch (Exception unused) {
            }
        }
        toast("항해사앱을 찾을 수 없습니다.");
    }

    private void launchPackage(String str, String str2) {
        try {
            Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(str);
            if (launchIntentForPackage == null) {
                toast(str2);
            } else {
                launchIntentForPackage.addFlags(268435456);
                startActivity(launchIntentForPackage);
            }
        } catch (Exception unused) {
            toast(str2);
        }
    }

    private void launchPackageOrUrl(String str, String str2) {
        try {
            Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(str);
            if (launchIntentForPackage != null) {
                launchIntentForPackage.addFlags(268435456);
                startActivity(launchIntentForPackage);
                return;
            }
        } catch (Exception unused) {
        }
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(str2));
            intent.addFlags(268435456);
            startActivity(intent);
        } catch (Exception unused2) {
            toast("열 수 없습니다.");
        }
    }

    private void openVideo(String str) {
        try {
            Intent intent = new Intent(this, (Class<?>) LocalMediaPickerActivity.class);
            intent.putExtra(LocalMediaPickerActivity.EXTRA_MIME, str);
            intent.putExtra("control", "open");
            intent.addFlags(805306368);
            startActivity(intent);
        } catch (Exception unused) {
            toast("휴대폰 동영상을 자동검색할 수 없습니다.");
        }
    }

    private void toggleExpanded() {
        WindowManager windowManager;
        WindowManager.LayoutParams layoutParams;
        this.expanded = !this.expanded;
        TextView textView = this.anchorView;
        if (textView != null) {
            textView.setVisibility(0);
        }
        LinearLayout linearLayout = this.buttonGroup;
        if (linearLayout != null) {
            linearLayout.setVisibility(this.expanded ? 0 : 8);
        }
        if (!this.expanded) {
            collapseStations();
        }
        LinearLayout linearLayout2 = this.root;
        if (linearLayout2 == null || (windowManager = this.wm) == null || (layoutParams = this.overlayLp) == null) {
            return;
        }
        try {
            windowManager.updateViewLayout(linearLayout2, layoutParams);
        } catch (Exception unused) {
        }
    }

    private void collapseStations() {
        WindowManager windowManager;
        LinearLayout linearLayout = this.channelPanel;
        if (linearLayout == null || (windowManager = this.wm) == null) {
            return;
        }
        try {
            windowManager.removeView(linearLayout);
        } catch (Exception unused) {
        }
        this.channelPanel.removeAllViews();
        this.channelPanel.setVisibility(8);
    }

    private void updateSidePanelPosition() {
        LinearLayout linearLayout = this.channelPanel;
        if (linearLayout == null || this.channelLp == null || this.overlayLp == null || this.wm == null || linearLayout.getVisibility() != 0) {
            return;
        }
        this.channelLp.x = this.overlayLp.x + dp(118);
        this.channelLp.y = this.overlayLp.y + dp(44);
        try {
            this.wm.updateViewLayout(this.channelPanel, this.channelLp);
        } catch (Exception unused) {
        }
    }

    private void showSidePanel() {
        WindowManager.LayoutParams layoutParams;
        WindowManager.LayoutParams layoutParams2;
        if (this.channelPanel == null || (layoutParams = this.channelLp) == null || (layoutParams2 = this.overlayLp) == null || this.wm == null) {
            return;
        }
        layoutParams.x = layoutParams2.x + dp(118);
        this.channelLp.y = this.overlayLp.y + dp(44);
        this.channelPanel.setVisibility(0);
        try {
            if (this.channelPanel.getParent() == null) {
                this.wm.addView(this.channelPanel, this.channelLp);
            } else {
                this.wm.updateViewLayout(this.channelPanel, this.channelLp);
            }
        } catch (Exception unused) {
        }
    }

    private void showFmStations() {
        showStations(new String[][]{new String[]{"KBS 클래식FM", "https://radio.bsod.kr/stream?stn=kbs&ch=1fm"}, new String[]{"MBC FM4U", "https://radio.bsod.kr/stream?stn=mbc&ch=fm4u"}, new String[]{"SBS 파워FM", "https://radio.bsod.kr/stream?stn=sbs&ch=powerfm"}, new String[]{"KBS 해피FM", "https://radio.bsod.kr/stream?stn=kbs&ch=2radio"}}, "FM");
    }

    private void showAmStations() {
        showStations(new String[][]{new String[]{"KBS 1라디오 · 뉴스/시사", "https://radio.bsod.kr/stream?stn=kbs&ch=1radio"}, new String[]{"MBC 표준FM · 뉴스/교양", "https://radio.bsod.kr/stream?stn=mbc&ch=sfm"}, new String[]{"SBS 러브FM · 뉴스/생활", "https://radio.bsod.kr/stream?stn=sbs&ch=lovefm"}}, "AM");
    }

    private void showVideoSideMenu(String str, final String str2) {
        if (this.channelPanel == null) {
            return;
        }
        collapseStations();
        this.channelPanel.removeAllViews();
        TextView textViewStationButton = stationButton("◀ " + str + " 파일");
        textViewStationButton.setTextColor(-9878);
        textViewStationButton.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda31
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m126xe6e15de3(view);
            }
        });
        this.channelPanel.addView(textViewStationButton);
        TextView textViewStationButton2 = stationButton("▶ " + str + " 파일 선택");
        textViewStationButton2.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m127xd88b0402(str2, view);
            }
        });
        this.channelPanel.addView(textViewStationButton2);
        showSidePanel();
    }

    /* renamed from: lambda$showVideoSideMenu$25$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m126xe6e15de3(View view) {
        collapseStations();
    }

    /* renamed from: lambda$showVideoSideMenu$26$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m127xd88b0402(String str, View view) {
        collapseStations();
        openVideo(str);
    }

    private void showStations(String[][] strArr, String str) {
        if (this.channelPanel == null) {
            return;
        }
        collapseStations();
        this.channelPanel.removeAllViews();
        TextView textViewStationButton = stationButton("◀ " + str + " 인터넷 채널 선택");
        textViewStationButton.setTextColor(-9878);
        textViewStationButton.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda22
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m124x7c48d163(view);
            }
        });
        this.channelPanel.addView(textViewStationButton);
        for (final String[] strArr2 : strArr) {
            TextView textViewStationButton2 = stationButton("▶ " + strArr2[0]);
            textViewStationButton2.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayControlService$$ExternalSyntheticLambda23
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.m125x6df27782(strArr2, view);
                }
            });
            this.channelPanel.addView(textViewStationButton2);
        }
        showSidePanel();
    }

    /* renamed from: lambda$showStations$27$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m124x7c48d163(View view) {
        collapseStations();
    }

    /* renamed from: lambda$showStations$28$com-baekhak-centralcontrol-OverlayControlService, reason: not valid java name */
    /* synthetic */ void m125x6df27782(String[] strArr, View view) {
        playRadio(strArr[0], strArr[1]);
        collapseStations();
    }

    private boolean hasAudioPermission() {
        return Build.VERSION.SDK_INT >= 33 ? checkSelfPermission("android.permission.READ_MEDIA_AUDIO") == 0 : checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0;
    }

    private void requestAudioPermissionInApp() {
        try {
            Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (launchIntentForPackage == null) {
                toast("항행의자유 앱을 열어 음악 권한을 허용해 주세요.");
                return;
            }
            launchIntentForPackage.putExtra("v3_request_audio_permission", true);
            launchIntentForPackage.addFlags(872415232);
            startActivity(launchIntentForPackage);
            toast("음악 재생을 위해 권한을 한 번 허용해 주세요.");
        } catch (Exception unused) {
            toast("항행의자유 앱을 열어 음악 및 오디오 권한을 허용해 주세요.");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateCurrentTrackLabel() {
        String str;
        TextView textView = this.currentTrackView;
        if (textView == null) {
            return;
        }
        try {
            String string = this.currentMediaMode;
            if (string == null || string.isEmpty()) {
                string = getSharedPreferences(PREFS, 0).getString("media_mode", "MP3");
            }
            if ("MP3".equals(string)) {
                JSONObject jSONObject = new JSONObject(MusicPlaybackService.snapshot(this));
                String strTrim = jSONObject.optString(RadioPlaybackService.EXTRA_NAME, "").trim();
                if (jSONObject.optBoolean("playing", false)) {
                    textView.setSelected(true);
                    textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    textView.setText("▶ " + strTrim);
                    return;
                }
                textView.setSelected(false);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                if (strTrim.isEmpty()) {
                    str = "MP3 · 정지";
                } else {
                    str = "MP3 · 정지 · " + strTrim;
                }
                textView.setText(str);
                return;
            }
            String string2 = getSharedPreferences(PREFS, 0).getString("media_title", "");
            StringBuilder sb = new StringBuilder();
            sb.append(string);
            sb.append(" · ");
            if (string2.isEmpty()) {
                string2 = "자동 연결";
            }
            sb.append(string2);
            textView.setText(sb.toString());
        } catch (Exception unused) {
            textView.setText(this.currentMediaMode + " · 준비");
        }
    }

    private TextView topSong(String str) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(-1);
        textView.setTextSize(11.0f);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity(16);
        textView.setPadding(dp(7), 0, dp(7), 0);
        textView.setBackground(bg(-586010549, 1433976575, 7));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(108), dp(38));
        layoutParams.setMargins(dp(2), dp(2), dp(2), dp(2));
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private void playRadio(String str, String str2) {
        playRadioAs(this.currentMediaMode.equals("AM") ? "AM" : "FM", str, str2);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Can't find top splitter block for handler:B:15:0x0024
        	at jadx.core.utils.BlockUtils.getTopSplitterForHandler(BlockUtils.java:1178)
        	at jadx.core.dex.visitors.regions.maker.ExcHandlersRegionMaker.collectHandlerRegions(ExcHandlersRegionMaker.java:53)
        	at jadx.core.dex.visitors.regions.maker.ExcHandlersRegionMaker.process(ExcHandlersRegionMaker.java:38)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:27)
        */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v2, types: [android.content.Intent] */
    private void playRadioAs(java.lang.String r3, java.lang.String r4, java.lang.String r5) {
        /*
            r2 = this;
            r2.currentRadioName = r4
            r2.currentRadioUrl = r5
            r0 = 1
            r2.radioPlaying = r0
            r2.setMediaMode(r3, r4)
            r3 = 26
            android.content.Intent r0 = new android.content.Intent     // Catch: java.lang.Exception -> L24
            java.lang.Class<com.baekhak.centralcontrol.MusicPlaybackService> r1 = com.baekhak.centralcontrol.MusicPlaybackService.class
            r0.<init>(r2, r1)     // Catch: java.lang.Exception -> L24
            java.lang.String r1 = "com.baekhak.centralcontrol.music.STOP"
            android.content.Intent r0 = r0.setAction(r1)     // Catch: java.lang.Exception -> L24
            int r1 = android.os.Build.VERSION.SDK_INT     // Catch: java.lang.Exception -> L24
            if (r1 < r3) goto L21
            com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticApiModelOutline0.m(r2, r0)     // Catch: java.lang.Exception -> L24
            goto L24
        L21:
            r2.startService(r0)     // Catch: java.lang.Exception -> L24
        L24:
            android.content.Intent r0 = new android.content.Intent     // Catch: java.lang.Exception -> L5d
            java.lang.Class<com.baekhak.centralcontrol.RadioPlaybackService> r1 = com.baekhak.centralcontrol.RadioPlaybackService.class
            r0.<init>(r2, r1)     // Catch: java.lang.Exception -> L5d
            java.lang.String r1 = "com.baekhak.centralcontrol.radio.PLAY"
            android.content.Intent r0 = r0.setAction(r1)     // Catch: java.lang.Exception -> L5d
            java.lang.String r1 = "name"
            android.content.Intent r0 = r0.putExtra(r1, r4)     // Catch: java.lang.Exception -> L5d
            java.lang.String r1 = "url"
            android.content.Intent r5 = r0.putExtra(r1, r5)     // Catch: java.lang.Exception -> L5d
            int r0 = android.os.Build.VERSION.SDK_INT     // Catch: java.lang.Exception -> L5d
            if (r0 < r3) goto L45
            com.baekhak.centralcontrol.MainActivity$$ExternalSyntheticApiModelOutline0.m(r2, r5)     // Catch: java.lang.Exception -> L5d
            goto L48
        L45:
            r2.startService(r5)     // Catch: java.lang.Exception -> L5d
        L48:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L5d
            r3.<init>()     // Catch: java.lang.Exception -> L5d
            r3.append(r4)     // Catch: java.lang.Exception -> L5d
            java.lang.String r4 = " 연결 중…"
            r3.append(r4)     // Catch: java.lang.Exception -> L5d
            java.lang.String r3 = r3.toString()     // Catch: java.lang.Exception -> L5d
            r2.toast(r3)     // Catch: java.lang.Exception -> L5d
            goto L62
        L5d:
            java.lang.String r3 = "라디오를 시작하지 못했습니다."
            r2.toast(r3)
        L62:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.baekhak.centralcontrol.OverlayControlService.playRadioAs(java.lang.String, java.lang.String, java.lang.String):void");
    }

    private void stopRadioSafely() {
        try {
            stopService(new Intent(this, (Class<?>) RadioPlaybackService.class));
        } catch (Throwable unused) {
        }
        this.currentRadioUrl = "";
        this.radioPlaying = false;
    }

    private void stopRadio() {
        stopRadioSafely();
    }

    private void stopMusicForMediaSwitch() {
        try {
            stopService(new Intent(this, (Class<?>) MusicPlaybackService.class));
        } catch (Throwable unused) {
        }
    }

    private void stopAllAudioForMediaSwitch() {
        stopMusicForMediaSwitch();
        stopRadioSafely();
        collapseStations();
    }

    private void safeMediaClick(Runnable runnable, String str) {
        try {
            try {
                runnable.run();
            } catch (Throwable unused) {
                toast(str);
            }
        } catch (Throwable unused2) {
        }
    }

    private void toast(String str) {
        Toast.makeText(this, str, 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeOverlay() {
        WindowManager windowManager;
        WindowManager windowManager2;
        WindowManager windowManager3;
        LinearLayout linearLayout = this.root;
        if (linearLayout != null && (windowManager3 = this.wm) != null) {
            try {
                windowManager3.removeView(linearLayout);
            } catch (Exception unused) {
            }
        }
        LinearLayout linearLayout2 = this.channelPanel;
        if (linearLayout2 != null && (windowManager2 = this.wm) != null) {
            try {
                windowManager2.removeView(linearLayout2);
            } catch (Exception unused2) {
            }
        }
        LinearLayout linearLayout3 = this.topMediaBar;
        if (linearLayout3 != null && (windowManager = this.wm) != null) {
            try {
                windowManager.removeView(linearLayout3);
            } catch (Exception unused3) {
            }
        }
        this.handler.removeCallbacks(this.musicStateTicker);
        this.root = null;
        this.channelPanel = null;
        this.channelLp = null;
        this.topMediaBar = null;
        this.topMediaLp = null;
        this.anchorView = null;
        this.currentTrackView = null;
        this.playlistPanel = null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.handler.removeCallbacksAndMessages(null);
        removeOverlay();
        super.onDestroy();
    }
}
