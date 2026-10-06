from pathlib import Path
p=Path("build405/FREEDOM_V402_BUILD_THIS/app/src/main/java/com/baekhak/centralcontrol/MainActivity.java")
s=p.read_text(encoding="utf-8")
s=s.replace("private static final int MUSIC_MULTI_PICKER = 4311;","private static final int MUSIC_MULTI_PICKER = 4311;\n    private static final int VIDEO_MULTI_PICKER = 4314;\n    private static final String VIDEO_DIR = \"v3_video_library\";\n    private static final String VIDEO_PREFS = \"v3_video_preferences\";\n    private static final String VIDEO_VOLUME = \"video_volume\";")
anchor='    private String httpGet(String urlText) throws Exception {'
insert=r'''
    private void chooseMultipleVideoFiles() {
        runOnUiThread(() -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("video/*");
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/mp4","video/*"});
            try {
                if (!tryExplicitDocumentsUi(i, VIDEO_MULTI_PICKER) && !tryResolvedSafePicker(i, VIDEO_MULTI_PICKER))
                    startActivityForResult(i, VIDEO_MULTI_PICKER);
            } catch (Exception e) { alert("MP4 불러오기를 열 수 없습니다."); }
        });
    }

    private File videoLibraryDir() {
        File d = new File(getFilesDir(), VIDEO_DIR);
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private int saveSelectedVideos(Intent data) throws Exception {
        ArrayList<Uri> uris = new ArrayList<>();
        if (data.getClipData() != null) {
            for (int x=0;x<data.getClipData().getItemCount();x++) uris.add(data.getClipData().getItemAt(x).getUri());
        } else if (data.getData()!=null) uris.add(data.getData());
        if (uris.isEmpty()) throw new Exception("선택한 MP4가 없습니다.");
        int saved=0;
        for (Uri uri:uris) {
            String name=displayName(uri);
            if(name==null||name.trim().isEmpty()) name="video_"+System.currentTimeMillis()+".mp4";
            name=name.replaceAll("[\\/:*?\"<>|]","_");
            if(!name.toLowerCase(Locale.ROOT).endsWith(".mp4")) name += ".mp4";
            File outFile=new File(videoLibraryDir(),UUID.randomUUID()+"_"+name);
            try(InputStream in=getContentResolver().openInputStream(uri); FileOutputStream out=new FileOutputStream(outFile)){
                if(in==null)throw new Exception("MP4를 읽을 수 없습니다.");
                byte[] buf=new byte[65536];int n;long total=0;
                while((n=in.read(buf))!=-1){if(n>0){out.write(buf,0,n);total+=n;}}
                out.flush();out.getFD().sync();
                if(total<=0){outFile.delete();throw new Exception("빈 MP4 파일입니다.");}
            }
            saved++;
        }
        return saved;
    }

    private String savedVideoLibraryJson() {
        JSONArray a=new JSONArray();
        try {
            File[] fs=videoLibraryDir().listFiles();
            if(fs!=null){
                java.util.Arrays.sort(fs,(x,y)->Long.compare(y.lastModified(),x.lastModified()));
                for(File f:fs) if(f.isFile()&&f.length()>0){
                    JSONObject j=new JSONObject();
                    j.put("name",f.getName().replaceFirst("^[^_]+_",""));
                    j.put("path",f.getAbsolutePath());
                    j.put("size",f.length());
                    a.put(j);
                }
            }
        } catch(Exception ignored){}
        return a.toString();
    }

    private int videoVolume() { return getSharedPreferences(VIDEO_PREFS,MODE_PRIVATE).getInt(VIDEO_VOLUME,70); }
    private void setVideoVolume(int percent) {
        int p=Math.max(0,Math.min(100,percent));
        getSharedPreferences(VIDEO_PREFS,MODE_PRIVATE).edit().putInt(VIDEO_VOLUME,p).apply();
    }

    private void playSavedVideo(String path) {
        runOnUiThread(()->{
            try{
                File f=new File(path==null?"":path);
                if(!f.isFile() || !f.getCanonicalPath().startsWith(videoLibraryDir().getCanonicalPath())) throw new Exception("not found");
                // No AndroidX dependency: play the private app-folder MP4 inside this Activity.
                MediaPlayer mp=new MediaPlayer();
                TextureView tv=new TextureView(this);
                tv.setBackgroundColor(Color.BLACK);
                FrameLayout.LayoutParams vp=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
                vp.gravity=Gravity.CENTER;
                rootLayout.addView(tv,vp);
                webView.setVisibility(View.INVISIBLE);
                tv.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
                    @Override public void onSurfaceTextureAvailable(SurfaceTexture st,int w,int h){
                        try{
                            Surface sf=new Surface(st);
                            mp.setSurface(sf);
                            mp.setDataSource(f.getAbsolutePath());
                            float vol=Math.max(0f,Math.min(1f,videoVolume()/100f));
                            mp.setVolume(vol,vol);
                            mp.setOnPreparedListener(x->x.start());
                            mp.setOnCompletionListener(x->{try{x.release();}catch(Exception ignored){} try{rootLayout.removeView(tv);}catch(Exception ignored){} webView.setVisibility(View.VISIBLE);});
                            mp.setOnErrorListener((x,what,extra)->{try{x.release();}catch(Exception ignored){} try{rootLayout.removeView(tv);}catch(Exception ignored){} webView.setVisibility(View.VISIBLE);return true;});
                            mp.prepareAsync();
                        }catch(Exception e){try{rootLayout.removeView(tv);}catch(Exception ignored){} webView.setVisibility(View.VISIBLE);}
                    }
                    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture st,int w,int h){}
                    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture st){try{mp.release();}catch(Exception ignored){} webView.setVisibility(View.VISIBLE);return true;}
                    @Override public void onSurfaceTextureUpdated(SurfaceTexture st){}
                });
            }catch(Exception e){Toast.makeText(this,"저장된 MP4를 열지 못했습니다.",Toast.LENGTH_LONG).show();}
        });
    }

'''
if anchor not in s: raise SystemExit("anchor missing")
s=s.replace(anchor,insert+anchor)
activity_anchor='        if (requestCode == SAVE_PICKER) {'
activity_insert=r'''        if(requestCode==VIDEO_MULTI_PICKER) {
            if(resultCode==RESULT_OK && data!=null) {
                final Intent picked=data;
                new Thread(()->{
                    try{
                        int count=saveSelectedVideos(picked);
                        final String js="if(window.videoImportResult)videoImportResult(true,"+count+",'');";
                        webView.post(()->webView.evaluateJavascript(js,null));
                    }catch(Exception e){
                        final String js="if(window.videoImportResult)videoImportResult(false,-1,"+JSONObject.quote(String.valueOf(e.getMessage()))+");";
                        webView.post(()->webView.evaluateJavascript(js,null));
                    }
                },"V3-VideoImport").start();
            }
            return;
        }

'''
s=s.replace(activity_anchor,activity_insert+activity_anchor)
bridge='        @JavascriptInterface public void openOurMusicFolder() { chooseMusicFolder(); }'
bridge_insert=r'''        @JavascriptInterface public void chooseMultipleVideos() { chooseMultipleVideoFiles(); }
        @JavascriptInterface public String getSavedVideoLibrary() { return savedVideoLibraryJson(); }
        @JavascriptInterface public void playSavedVideo(String path) { MainActivity.this.playSavedVideo(path); }
        @JavascriptInterface public int getVideoVolume() { return MainActivity.this.videoVolume(); }
        @JavascriptInterface public void setVideoVolume(int percent) { MainActivity.this.setVideoVolume(percent); }
'''
s=s.replace(bridge,bridge_insert+bridge)
p.write_text(s,encoding="utf-8")
print("MP4 native import/app-folder bridge added")
