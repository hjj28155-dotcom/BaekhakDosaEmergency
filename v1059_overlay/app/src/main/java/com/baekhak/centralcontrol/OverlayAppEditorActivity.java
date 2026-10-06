package com.baekhak.centralcontrol;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;

public class OverlayAppEditorActivity extends Activity {
    private static final String PREFS="overlay_control", KEY="custom_apps";
    private LinearLayout list; private final ArrayList<String> selected=new ArrayList<>();
    @Override public void onCreate(Bundle b){super.onCreate(b); load(); build();}
    private void load(){String s=getSharedPreferences(PREFS,0).getString(KEY,""); if(s!=null&&!s.isEmpty())for(String p:s.split("\\|"))if(!p.isEmpty())selected.add(p);}
    private void save(){StringBuilder s=new StringBuilder();for(String p:selected){if(s.length()>0)s.append('|');s.append(p);}getSharedPreferences(PREFS,0).edit().putString(KEY,s.toString()).apply(); refreshOverlay();}
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(18,18,18,18);root.setBackgroundColor(Color.rgb(8,27,45));
        TextView title=new TextView(this);title.setText("⚓ 이동아이콘 편집\n원하는 앱을 추가·제거하고 순서를 바꿀 수 있습니다.");title.setTextColor(Color.WHITE);title.setTextSize(19);title.setPadding(8,8,8,16);root.addView(title);
        ScrollView sv=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        Button close=new Button(this);close.setText("완료");close.setOnClickListener(v->finish());root.addView(close);setContentView(root);render();
    }
    private void render(){list.removeAllViews();
        TextView chosen=new TextView(this);chosen.setText("현재 추가된 앱 (▲▼로 순서 변경)");chosen.setTextColor(0xFFFFD96A);chosen.setTextSize(16);list.addView(chosen);
        for(int i=0;i<selected.size();i++){final int ix=i;String pkg=selected.get(i);String label=label(pkg);LinearLayout row=row();TextView n=name(label);row.addView(n,new LinearLayout.LayoutParams(0,-2,1));Button up=small("▲"),dn=small("▼"),rm=small("제거");up.setOnClickListener(v->{if(ix>0){Collections.swap(selected,ix,ix-1);save();render();}});dn.setOnClickListener(v->{if(ix<selected.size()-1){Collections.swap(selected,ix,ix+1);save();render();}});rm.setOnClickListener(v->{selected.remove(ix);save();render();});row.addView(up);row.addView(dn);row.addView(rm);list.addView(row);}
        TextView all=new TextView(this);all.setText("\n＋ 설치된 앱 추가");all.setTextColor(0xFF8ED6FF);all.setTextSize(16);list.addView(all);
        Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> apps=getPackageManager().queryIntentActivities(q,0);Collections.sort(apps,new ResolveInfo.DisplayNameComparator(getPackageManager()));HashSet<String> seen=new HashSet<>();
        for(ResolveInfo r:apps){String pkg=r.activityInfo.packageName;if(pkg.equals(getPackageName())||selected.contains(pkg)||!seen.add(pkg))continue;LinearLayout row=row();TextView n=name(String.valueOf(r.loadLabel(getPackageManager())));row.addView(n,new LinearLayout.LayoutParams(0,-2,1));Button add=small("추가");add.setOnClickListener(v->{selected.add(pkg);save();render();});row.addView(add);list.addView(row);}
    }
    private LinearLayout row(){LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(2,3,2,3);return r;}
    private TextView name(String s){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(14);v.setPadding(8,8,8,8);return v;}
    private Button small(String s){Button b=new Button(this);b.setText(s);b.setTextSize(11);b.setMinWidth(0);b.setMinimumWidth(0);return b;}
    private String label(String pkg){try{ApplicationInfo a=getPackageManager().getApplicationInfo(pkg,0);return String.valueOf(getPackageManager().getApplicationLabel(a));}catch(Exception e){return pkg;}}
    private void refreshOverlay(){try{Intent i=new Intent(this,OverlayControlService.class).setAction(OverlayControlService.ACTION_REFRESH);if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}catch(Exception ignored){}}
}