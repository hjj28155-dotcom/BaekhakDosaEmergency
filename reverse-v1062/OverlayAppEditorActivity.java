package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class OverlayAppEditorActivity extends Activity {
    private static final String KEY = "custom_apps";
    private static final String PREFS = "overlay_control";
    private LinearLayout list;
    private final ArrayList<String> selected = new ArrayList<>();

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) throws PackageManager.NameNotFoundException {
        super.onCreate(bundle);
        load();
        build();
    }

    private void load() {
        String string = getSharedPreferences(PREFS, 0).getString(KEY, "");
        if (string == null || string.isEmpty()) {
            return;
        }
        for (String str : string.split("\\|")) {
            if (!str.isEmpty()) {
                this.selected.add(str);
            }
        }
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = this.selected.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(next);
        }
        getSharedPreferences(PREFS, 0).edit().putString(KEY, sb.toString()).apply();
        refreshOverlay();
    }

    private void build() throws PackageManager.NameNotFoundException {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(18, 18, 18, 18);
        linearLayout.setBackgroundColor(Color.rgb(8, 27, 45));
        TextView textView = new TextView(this);
        textView.setText("⚓ 이동아이콘 편집\n원하는 앱을 추가·제거하고 순서를 바꿀 수 있습니다.");
        textView.setTextColor(-1);
        textView.setTextSize(19.0f);
        textView.setPadding(8, 8, 8, 16);
        linearLayout.addView(textView);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout2 = new LinearLayout(this);
        this.list = linearLayout2;
        linearLayout2.setOrientation(1);
        scrollView.addView(this.list);
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        Button button = new Button(this);
        button.setText("완료");
        button.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayAppEditorActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m94xe0c2475f(view);
            }
        });
        linearLayout.addView(button);
        setContentView(linearLayout);
        render();
    }

    /* renamed from: lambda$build$0$com-baekhak-centralcontrol-OverlayAppEditorActivity, reason: not valid java name */
    /* synthetic */ void m94xe0c2475f(View view) {
        finish();
    }

    private void render() throws PackageManager.NameNotFoundException {
        this.list.removeAllViews();
        TextView textView = new TextView(this);
        textView.setText("현재 추가된 앱 (▲▼로 순서 변경)");
        textView.setTextColor(-9878);
        textView.setTextSize(16.0f);
        this.list.addView(textView);
        for (final int i = 0; i < this.selected.size(); i++) {
            String strLabel = label(this.selected.get(i));
            LinearLayout linearLayoutRow = row();
            linearLayoutRow.addView(name(strLabel), new LinearLayout.LayoutParams(0, -2, 1.0f));
            Button buttonSmall = small("▲");
            Button buttonSmall2 = small("▼");
            Button buttonSmall3 = small("제거");
            buttonSmall.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayAppEditorActivity$$ExternalSyntheticLambda2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) throws PackageManager.NameNotFoundException {
                    this.f$0.m95x31c73922(i, view);
                }
            });
            buttonSmall2.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayAppEditorActivity$$ExternalSyntheticLambda3
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) throws PackageManager.NameNotFoundException {
                    this.f$0.m96xbf01eaa3(i, view);
                }
            });
            buttonSmall3.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayAppEditorActivity$$ExternalSyntheticLambda4
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) throws PackageManager.NameNotFoundException {
                    this.f$0.m97x4c3c9c24(i, view);
                }
            });
            linearLayoutRow.addView(buttonSmall);
            linearLayoutRow.addView(buttonSmall2);
            linearLayoutRow.addView(buttonSmall3);
            this.list.addView(linearLayoutRow);
        }
        TextView textView2 = new TextView(this);
        textView2.setText("\n＋ 설치된 앱 추가");
        textView2.setTextColor(-7416065);
        textView2.setTextSize(16.0f);
        this.list.addView(textView2);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> listQueryIntentActivities = getPackageManager().queryIntentActivities(intent, 0);
        Collections.sort(listQueryIntentActivities, new ResolveInfo.DisplayNameComparator(getPackageManager()));
        HashSet hashSet = new HashSet();
        for (ResolveInfo resolveInfo : listQueryIntentActivities) {
            final String str = resolveInfo.activityInfo.packageName;
            if (!str.equals(getPackageName()) && !this.selected.contains(str) && hashSet.add(str)) {
                LinearLayout linearLayoutRow2 = row();
                linearLayoutRow2.addView(name(String.valueOf(resolveInfo.loadLabel(getPackageManager()))), new LinearLayout.LayoutParams(0, -2, 1.0f));
                Button buttonSmall4 = small("추가");
                buttonSmall4.setOnClickListener(new View.OnClickListener() { // from class: com.baekhak.centralcontrol.OverlayAppEditorActivity$$ExternalSyntheticLambda5
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) throws PackageManager.NameNotFoundException {
                        this.f$0.m98xd9774da5(str, view);
                    }
                });
                linearLayoutRow2.addView(buttonSmall4);
                this.list.addView(linearLayoutRow2);
            }
        }
    }

    /* renamed from: lambda$render$1$com-baekhak-centralcontrol-OverlayAppEditorActivity, reason: not valid java name */
    /* synthetic */ void m95x31c73922(int i, View view) throws PackageManager.NameNotFoundException {
        if (i > 0) {
            Collections.swap(this.selected, i, i - 1);
            save();
            render();
        }
    }

    /* renamed from: lambda$render$2$com-baekhak-centralcontrol-OverlayAppEditorActivity, reason: not valid java name */
    /* synthetic */ void m96xbf01eaa3(int i, View view) throws PackageManager.NameNotFoundException {
        if (i < this.selected.size() - 1) {
            Collections.swap(this.selected, i, i + 1);
            save();
            render();
        }
    }

    /* renamed from: lambda$render$3$com-baekhak-centralcontrol-OverlayAppEditorActivity, reason: not valid java name */
    /* synthetic */ void m97x4c3c9c24(int i, View view) throws PackageManager.NameNotFoundException {
        this.selected.remove(i);
        save();
        render();
    }

    /* renamed from: lambda$render$4$com-baekhak-centralcontrol-OverlayAppEditorActivity, reason: not valid java name */
    /* synthetic */ void m98xd9774da5(String str, View view) throws PackageManager.NameNotFoundException {
        this.selected.add(str);
        save();
        render();
    }

    private LinearLayout row() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(16);
        linearLayout.setPadding(2, 3, 2, 3);
        return linearLayout;
    }

    private TextView name(String str) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(-1);
        textView.setTextSize(14.0f);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private Button small(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(11.0f);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        return button;
    }

    private String label(String str) throws PackageManager.NameNotFoundException {
        try {
            return String.valueOf(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(str, 0)));
        } catch (Exception unused) {
            return str;
        }
    }

    private void refreshOverlay() {
        try {
            Intent action = new Intent(this, (Class<?>) OverlayControlService.class).setAction(OverlayControlService.ACTION_REFRESH);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(action);
            } else {
                startService(action);
            }
        } catch (Exception unused) {
        }
    }
}
