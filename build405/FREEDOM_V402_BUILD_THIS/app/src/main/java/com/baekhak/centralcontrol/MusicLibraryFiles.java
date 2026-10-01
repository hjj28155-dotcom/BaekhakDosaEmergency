package com.baekhak.centralcontrol;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

/** Local audio library. Append-only imports; never deletes a user's original or old library.
 * A complete new manifest is fsynced before the previous manifest is replaced.
 * This class has no Android dependencies so failure/restart cases can be executed on a JVM.
 */
public final class MusicLibraryFiles {
    private static final Object LOCK = new Object();
    private static final int MAGIC = 0x4d555339, SCHEMA = 1, MAX_TRACKS = 10000;
    public static final class Track {
        public final String id, name, path, hash;
        public final long size;
        public Track(String id, String name, String path, long size, String hash) {
            this.id=id; this.name=name; this.path=path; this.size=size; this.hash=hash;
        }
    }
    public interface Source { String name(); InputStream open() throws IOException; }
    public interface Progress { void changed(int done, int total); }
    private final File filesRoot, home;
    public MusicLibraryFiles(File filesRoot) {
        this.filesRoot=filesRoot; this.home=new File(filesRoot,"music_library_v9");
    }
    public File file(Track t) throws IOException {
        File f=new File(filesRoot,t.path).getCanonicalFile();
        String root=filesRoot.getCanonicalPath()+File.separator;
        if (!f.getPath().startsWith(root)) throw new IOException("음악 파일 경로가 올바르지 않습니다.");
        return f;
    }
    public List<Track> read() throws IOException {
        synchronized(LOCK) {
            File live=new File(home,"index.bin"), bak=new File(home,"index.bak");
            if(live.exists()) {
                try { return readIndex(live); }
                catch(IOException e) { if(bak.exists()) return readIndex(bak); throw e; }
            }
            if(bak.exists()) return readIndex(bak);
            return new ArrayList<>();
        }
    }
    private List<Track> readIndex(File f) throws IOException {
        try(DataInputStream in=new DataInputStream(new BufferedInputStream(new FileInputStream(f)))) {
            if(in.readInt()!=MAGIC || in.readInt()!=SCHEMA) throw new IOException("음악목록을 읽지 못했습니다. 기존 파일은 보존됩니다.");
            int n=in.readInt(); if(n<0 || n>MAX_TRACKS) throw new IOException("음악목록 크기 오류");
            List<Track> result=new ArrayList<>(); Set<String> ids=new HashSet<>();
            for(int i=0;i<n;i++) {
                Track t=new Track(in.readUTF(),in.readUTF(),in.readUTF(),in.readLong(),in.readUTF());
                file(t); // Reject path traversal without dropping inaccessible/missing entries.
                if(t.id.isEmpty() || !ids.add(t.id) || t.size<0) throw new IOException("음악목록 내용 오류");
                result.add(t);
            }
            if(in.read()!=-1) throw new IOException("음악목록 끝부분 오류");
            return result;
        }
    }
    private void writeIndex(List<Track> list) throws IOException {
        if(list.size()>MAX_TRACKS) throw new IOException("저장 한도 10,000곡을 넘었습니다. 기존 목록은 보존됩니다.");
        if(!home.isDirectory() && !home.mkdirs()) throw new IOException("음악 보관폴더를 만들지 못했습니다.");
        File live=new File(home,"index.bin"), bak=new File(home,"index.bak"), temp=new File(home,"index.new");
        try(FileOutputStream raw=new FileOutputStream(temp); DataOutputStream out=new DataOutputStream(raw)) {
            out.writeInt(MAGIC);out.writeInt(SCHEMA);out.writeInt(list.size());
            for(Track t:list) {out.writeUTF(t.id);out.writeUTF(t.name);out.writeUTF(t.path);out.writeLong(t.size);out.writeUTF(t.hash);}
            out.flush();raw.getFD().sync();
        }
        readIndex(temp); // Validation before touching the previous index.
        if(live.exists()) {
            if(bak.exists() && !bak.delete()) throw new IOException("이전 음악목록 백업을 교체하지 못했습니다.");
            if(!live.renameTo(bak)) throw new IOException("기존 음악목록을 보존하지 못했습니다.");
        }
        if(!temp.renameTo(live)) {
            if(!live.exists() && bak.exists()) bak.renameTo(live);
            throw new IOException("새 목록 저장 실패. 기존 음악목록은 보존됩니다.");
        }
        // Keep the last committed backup; it can recover an interrupted later write.
    }
    public List<Track> migrateLegacy(String singleName) throws IOException {
        synchronized(LOCK) {
            if(new File(home,"index.bin").exists() || new File(home,"index.bak").exists()) return read();
            List<Track> list=new ArrayList<>();
            File dir=new File(filesRoot,"v3_music_playlist"); File[] ff=dir.listFiles();
            if(ff!=null) {
                Arrays.sort(ff,new Comparator<File>() { public int compare(File a,File b){return a.getName().compareTo(b.getName());} });
                for(File f:ff) if(f.isFile() && f.length()>0) {
                    String name=f.getName().replaceFirst("^\\d{3,}_", "");
                    list.add(new Track("legacy-"+digest(f.getName().getBytes("UTF-8")),name,"v3_music_playlist/"+f.getName(),f.length(),""));
                }
            }
            File single=new File(filesRoot,"v3_user_background_audio");
            if(list.isEmpty() && single.isFile() && single.length()>0) list.add(new Track("legacy-single",singleName==null||singleName.isEmpty()?"기존 선택 음악":singleName,single.getName(),single.length(),""));
            if(!list.isEmpty()) writeIndex(list);
            return list; // Never moves or deletes old data.
        }
    }
    public List<Track> append(List<Source> sources, Progress progress) throws IOException {
        synchronized(LOCK) {
            if(sources==null || sources.isEmpty()) throw new IOException("선택한 음악이 없습니다. 기존 목록은 유지됩니다.");
            List<Track> old=read(), merged=new ArrayList<>(old);
            File batch=new File(home,"imports/"+UUID.randomUUID());
            if(!batch.mkdirs()) throw new IOException("음악 저장공간을 준비하지 못했습니다.");
            boolean committed=false;
            try {
                Set<String> hashes=new HashSet<>(); for(Track t:old) if(!t.hash.isEmpty()) hashes.add(t.hash);
                int done=0;
                for(Source source:sources) {
                    String name=source.name(); if(name==null || name.trim().isEmpty()) name="선택한 음악";
                    if(name.length()>500) name=name.substring(0,500);
                    File dest=new File(batch,UUID.randomUUID()+".audio"); MessageDigest hash=sha(); long size=0;
                    try(InputStream in=source.open(); FileOutputStream out=new FileOutputStream(dest)) {
                        if(in==null) throw new IOException("읽을 수 없는 음악: "+name);
                        byte[] buffer=new byte[65536];int n;
                        while((n=in.read(buffer))!=-1) {if(n==0)continue;out.write(buffer,0,n);hash.update(buffer,0,n);size+=n;}
                        out.flush();out.getFD().sync();
                    }
                    if(size<=0) throw new IOException("비어 있는 음악: "+name);
                    String h=hex(hash.digest());
                    // Legacy entries did not have a digest. Compare only equal-size files once needed.
                    for(Track t:old) if(t.hash.isEmpty() && t.size==size && file(t).isFile() && hashFile(file(t)).equals(h)) hashes.add(h);
                    if(hashes.add(h)) {
                        String relative="music_library_v9/imports/"+batch.getName()+"/"+dest.getName();
                        merged.add(new Track(h,name,relative,size,h));
                    } else if(!dest.delete()) dest.deleteOnExit();
                    done++; if(progress!=null)progress.changed(done,sources.size());
                }
                writeIndex(merged); committed=true; return merged;
            } finally {
                if(!committed) deleteBatch(batch); // Only the new, uncommitted copies.
            }
        }
    }
    private static void deleteBatch(File batch) {File[] f=batch.listFiles();if(f!=null)for(File x:f)x.delete();batch.delete();}
    private static MessageDigest sha() throws IOException {try{return MessageDigest.getInstance("SHA-256");}catch(Exception e){throw new IOException(e);}}
    private static String hex(byte[] a){StringBuilder b=new StringBuilder();for(byte x:a)b.append(String.format(Locale.ROOT,"%02x",x&255));return b.toString();}
    private static String digest(byte[] a) throws IOException {return hex(sha().digest(a));}
    private static String hashFile(File f) throws IOException {MessageDigest d=sha();try(InputStream i=new FileInputStream(f)){byte[] b=new byte[65536];int n;while((n=i.read(b))!=-1)if(n>0)d.update(b,0,n);}return hex(d.digest());}
}
