package com.baekhak.centralcontrol;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.*;
import org.json.*;

/** Android adapters for the crash-safe private audio library. Both app roles use this code. */
public final class MusicLibraryStore {
    private static volatile boolean importing=false;
    private static volatile String progress="";
    private static volatile List<MusicLibraryFiles.Track> cached=Collections.emptyList();
    private final Context context;
    private final MusicLibraryFiles files;
    public MusicLibraryStore(Context context) {
        this.context=context.getApplicationContext();files=new MusicLibraryFiles(this.context.getFilesDir());
    }
    public List<MusicLibraryFiles.Track> read() throws IOException {
        List<MusicLibraryFiles.Track> found=files.migrateLegacy(context.getSharedPreferences("v3_music_preferences",0).getString("music_name",""));
        cached=Collections.unmodifiableList(new ArrayList<>(found));return found;
    }
    public File file(MusicLibraryFiles.Track t) throws IOException {return files.file(t);}
    public static String progress(){return progress;}
    public static boolean importing(){return importing;}
    public String asJson() {
        JSONObject result=new JSONObject();JSONArray rows=new JSONArray();
        try {
            for(MusicLibraryFiles.Track t:(importing?cached:read())) {
                JSONObject row=new JSONObject(); row.put("id",t.id);row.put("name",t.name);row.put("size",t.size);
                row.put("available",file(t).isFile());rows.put(row);
            }
            result.put("ok",true);result.put("items",rows);
            String tree=context.getSharedPreferences("v3_music_preferences",0).getString("music_library_folder_uri","");
            result.put("folderConnected",tree!=null&&!tree.isEmpty());
            result.put("importing",importing);result.put("progress",progress);
        }catch(Exception e){try{result.put("ok",false);result.put("error",e.getMessage());}catch(Exception ignored){}}
        return result.toString();
    }
    public int appendUris(List<Uri> uris,int flags) throws IOException {
        // Serialize whole imports, not individual files; no old library is cleared beforehand.
        synchronized(MusicLibraryStore.class) {
            importing=true;progress="음악을 보관하는 중입니다. 기존 목록은 유지됩니다.";
            try {
                read(); // One-time, non-destructive migration of the old copied playlist.
                List<MusicLibraryFiles.Source> sources=new ArrayList<>();Set<String> seen=new HashSet<>();
                for(Uri uri:uris) {
                    if(uri==null||!seen.add(uri.toString()))continue;
                    String name=name(uri),mime=null;
                    try{mime=context.getContentResolver().getType(uri);}catch(Exception ignored){}
                    if(!audio(name,mime))continue;
                    try{context.getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
                    final Uri sourceUri=uri;final String sourceName=name;
                    sources.add(new MusicLibraryFiles.Source(){
                        public String name(){return sourceName;}
                        public InputStream open() throws IOException {
                            try{return "file".equals(sourceUri.getScheme())?new FileInputStream(new File(sourceUri.getPath())):context.getContentResolver().openInputStream(sourceUri);}
                            catch(SecurityException e){throw new IOException("음악 읽기 권한이 없습니다: "+sourceName,e);}
                        }
                    });
                }
                List<MusicLibraryFiles.Track> saved=files.append(sources,(done,total)->progress="음악 보관 중 "+done+" / "+total+"곡");
                cached=Collections.unmodifiableList(new ArrayList<>(saved));
                progress="저장된 음악 "+saved.size()+"곡 · 기존 선택 목록 유지";
                return saved.size();
            }catch(IOException e){progress="저장 실패: "+e.getMessage()+" · 기존 목록 유지";throw e;}
            finally{importing=false;}
        }
    }
    public int appendFolder(Uri tree) throws IOException {
        try{context.getContentResolver().takePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
        List<Uri> uris=new ArrayList<>(); collect(tree,DocumentsContract.getTreeDocumentId(tree),uris,0);
        int count=appendUris(uris,Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Remember only a successfully imported folder. Cancel/failure retains the prior folder.
        context.getSharedPreferences("v3_music_preferences",0).edit().putString("music_library_folder_uri",tree.toString()).commit();
        return count;
    }
    private void collect(Uri tree,String parent,List<Uri> uris,int depth) throws IOException {
        if(depth>12)throw new IOException("음악 폴더가 너무 깊습니다. 음악이 들어 있는 하위 폴더를 선택하세요.");
        Uri child=DocumentsContract.buildChildDocumentsUriUsingTree(tree,parent);
        String[] cols={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE};
        List<String[]> entries=new ArrayList<>();
        try(Cursor c=context.getContentResolver().query(child,cols,null,null,null)) {
            if(c==null)throw new IOException("음악 폴더를 읽지 못했습니다.");
            while(c.moveToNext())entries.add(new String[]{c.getString(0),c.getString(1),c.getString(2)});
        }catch(RuntimeException e){throw new IOException("음악 폴더 읽기 실패. 기존 목록은 유지됩니다.",e);}
        Collections.sort(entries,(a,b)->String.valueOf(a[1]).compareToIgnoreCase(String.valueOf(b[1])));
        for(String[] entry:entries) {
            if(DocumentsContract.Document.MIME_TYPE_DIR.equals(entry[2]))collect(tree,entry[0],uris,depth+1);
            else if(audio(entry[1],entry[2]))uris.add(DocumentsContract.buildDocumentUriUsingTree(tree,entry[0]));
            if(uris.size()>10000)throw new IOException("한 번에 10,000곡 이하의 음악 폴더를 선택하세요.");
        }
    }
    private String name(Uri uri) {
        try(Cursor c=context.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) {
            if(c!=null&&c.moveToFirst()) {String s=c.getString(0);if(s!=null)return s;}
        }catch(Exception ignored){}
        String p=uri.getLastPathSegment();return p==null?"선택한 음악":p;
    }
    private static boolean audio(String name,String mime) {
        String n=name==null?"":name.toLowerCase(Locale.ROOT);
        return mime!=null&&mime.startsWith("audio/")||n.matches(".*\\.(mp3|m4a|aac|wav|ogg|flac|opus)$");
    }
}