package com.baekhak.centralcontrol;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public final class VideoLibraryStore {
    public static final class Item {
        public final String name,path,hash;
        Item(String n,String p,String h){name=n;path=p;hash=h;}
    }
    private final Context context;
    private final File root;

    public VideoLibraryStore(Context c){
        context=c.getApplicationContext();
        root=new File(context.getFilesDir(),"video_library_v1");
    }

    public synchronized List<Item> read(){
        ArrayList<Item> out=new ArrayList<>();
        File[] ff=root.listFiles();
        if(ff==null)return out;
        Arrays.sort(ff,(a,b)->a.getName().compareToIgnoreCase(b.getName()));
        for(File f:ff){
            if(!f.isFile()||f.length()<=0)continue;
            String n=f.getName();
            int k=n.indexOf("__");
            String display=k>=0?n.substring(k+2):n;
            out.add(new Item(display,f.getAbsolutePath(),k>=0?n.substring(0,k):""));
        }
        return out;
    }

    public synchronized int append(List<Uri> uris) throws IOException{
        if(!root.isDirectory()&&!root.mkdirs())throw new IOException("영상 보관폴더 생성 실패");
        Set<String> existing=new HashSet<>();
        for(Item x:read())if(!x.hash.isEmpty())existing.add(x.hash);
        int added=0;
        for(Uri u:uris){
            if(u==null)continue;
            String name=name(u);
            File tmp=File.createTempFile("import_",".video",root);
            MessageDigest d=sha();
            long size=0;
            try(InputStream in=context.getContentResolver().openInputStream(u);
                FileOutputStream os=new FileOutputStream(tmp)){
                if(in==null)throw new IOException("영상을 읽지 못했습니다: "+name);
                byte[] b=new byte[65536];
                int n;
                while((n=in.read(b))!=-1){
                    if(n>0){os.write(b,0,n);d.update(b,0,n);size+=n;}
                }
                os.flush(); os.getFD().sync();
            }
            if(size<=0){tmp.delete();continue;}
            String h=hex(d.digest());
            if(existing.add(h)){
                String safe=name.replaceAll("[\\\\/:*?\"<>|]","_");
                if(safe.length()>180)safe=safe.substring(safe.length()-180);
                File dst=new File(root,h+"__"+safe);
                if(!tmp.renameTo(dst)){copy(tmp,dst);tmp.delete();}
                added++;
            }else tmp.delete();
        }
        return added;
    }

    private String name(Uri u){
        try(Cursor c=context.getContentResolver().query(u,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){
            if(c!=null&&c.moveToFirst()){
                String s=c.getString(0);
                if(s!=null&&!s.isEmpty())return s;
            }
        }catch(Exception ignored){}
        return "video_"+System.currentTimeMillis()+".mp4";
    }

    private static void copy(File a,File b)throws IOException{
        try(InputStream i=new FileInputStream(a);FileOutputStream o=new FileOutputStream(b)){
            byte[] x=new byte[65536];int n;
            while((n=i.read(x))!=-1)if(n>0)o.write(x,0,n);
            o.flush();o.getFD().sync();
        }
    }
    private static MessageDigest sha()throws IOException{
        try{return MessageDigest.getInstance("SHA-256");}
        catch(Exception e){throw new IOException(e);}
    }
    private static String hex(byte[] a){
        StringBuilder s=new StringBuilder();
        for(byte x:a)s.append(String.format(Locale.ROOT,"%02x",x&255));
        return s.toString();
    }
}
