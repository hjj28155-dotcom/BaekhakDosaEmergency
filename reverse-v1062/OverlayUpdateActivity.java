package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/* loaded from: classes.dex */
public class OverlayUpdateActivity extends Activity {
    private static final int PICK_APK = 7417;
    private boolean pickerOpen = false;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        showLandingScreen();
    }

    private void showLandingScreen() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(1);
        linearLayout.setPadding(36, 72, 36, 36);
        linearLayout.setBackgroundColor(Color.rgb(5, 31, 48));
        TextView textView = new TextView(this);
        textView.setText("⚓ 이동아이콘 업데이트\nV1.0.51 (1051)");
        textView.setTextColor(-1);
        textView.setTextSize(24.0f);
        textView.setGravity(17);
        linearLayout.addView(textView, new LinearLayout.LayoutParams(-1, -2));
        TextView textView2 = new TextView(this);
        textView2.setText("업데이트 파일을 이미 받아 둔 경우에만 아래 버튼을 누르세요.\n\n이 화면에 들어왔다고 해서 파일 선택기가 자동으로 열리지 않습니다.");
        textView2.setTextColor(-1);
        textView2.setTextSize(16.0f);
        textView2.setPadding(0, 48, 0, 48);
        textView2.setGravity(17);
        linearLayout.addView(textView2, new LinearLayout.LayoutParams(-1, -2));
        Button button = new Button(this);
        button.setText("APK 업데이트 파일 선택");
        button.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayUpdateActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m128x6924497e(view);
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-1, -2));
        Button button2 = new Button(this);
        button2.setText("닫기 · 이동아이콘으로 돌아가기");
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayUpdateActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m129x5acdef9d(view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.topMargin = 24;
        linearLayout.addView(button2, layoutParams);
        setContentView(linearLayout);
    }

    /* renamed from: lambda$showLandingScreen$0$com-baekhak-centralcontrol-OverlayUpdateActivity, reason: not valid java name */
    /* synthetic */ void m128x6924497e(View view) {
        pickApk();
    }

    /* renamed from: lambda$showLandingScreen$1$com-baekhak-centralcontrol-OverlayUpdateActivity, reason: not valid java name */
    /* synthetic */ void m129x5acdef9d(View view) {
        finish();
    }

    private void pickApk() {
        if (this.pickerOpen) {
            return;
        }
        try {
            this.pickerOpen = true;
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.setType("application/vnd.android.package-archive");
            startActivityForResult(intent, PICK_APK);
        } catch (Exception unused) {
            this.pickerOpen = false;
            Toast.makeText(this, "APK 선택 화면을 열 수 없습니다.", 1).show();
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != PICK_APK) {
            return;
        }
        this.pickerOpen = false;
        if (i2 != -1 || intent == null || intent.getData() == null) {
            showLandingScreen();
            return;
        }
        try {
            startActivity(new Intent("android.intent.action.VIEW").setDataAndType(intent.getData(), "application/vnd.android.package-archive").addFlags(1));
        } catch (Exception unused) {
            Toast.makeText(this, "APK 설치 화면을 열 수 없습니다.", 1).show();
            showLandingScreen();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        resumeOverlay();
        super.onDestroy();
    }

    private void resumeOverlay() {
        try {
            Intent action = new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_RESUME);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(action);
            } else {
                startService(action);
            }
        } catch (Exception unused) {
        }
    }
}
