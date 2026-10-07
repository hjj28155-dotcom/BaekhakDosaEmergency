package com.baekhak.centralcontrol;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/* loaded from: classes.dex */
public final class MusicLibraryFiles {
    private static final Object LOCK = new Object();
    private static final int MAGIC = 1297437497;
    private static final int MAX_TRACKS = 10000;
    private static final int SCHEMA = 1;
    private final File filesRoot;
    private final File home;

    public interface Progress {
        void changed(int i, int i2);
    }

    public interface Source {
        String name();

        InputStream open() throws IOException;
    }

    public static final class Track {
        public final String hash;
        public final String id;
        public final String name;
        public final String path;
        public final long size;

        public Track(String str, String str2, String str3, long j, String str4) {
            this.id = str;
            this.name = str2;
            this.path = str3;
            this.size = j;
            this.hash = str4;
        }
    }

    public MusicLibraryFiles(File file) {
        this.filesRoot = file;
        this.home = new File(file, "music_library_v9");
    }

    public File file(Track track) throws IOException {
        File canonicalFile = new File(this.filesRoot, track.path).getCanonicalFile();
        if (canonicalFile.getPath().startsWith(this.filesRoot.getCanonicalPath() + File.separator)) {
            return canonicalFile;
        }
        throw new IOException("음악 파일 경로가 올바르지 않습니다.");
    }

    public List<Track> read() throws IOException {
        synchronized (LOCK) {
            File file = new File(this.home, "index.bin");
            File file2 = new File(this.home, "index.bak");
            if (file.exists()) {
                try {
                    return readIndex(file);
                } catch (IOException e) {
                    if (file2.exists()) {
                        return readIndex(file2);
                    }
                    throw e;
                }
            }
            if (file2.exists()) {
                return readIndex(file2);
            }
            return new ArrayList();
        }
    }

    private List<Track> readIndex(File file) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        try {
            if (dataInputStream.readInt() != MAGIC || dataInputStream.readInt() != SCHEMA) {
                throw new IOException("음악목록을 읽지 못했습니다. 기존 파일은 보존됩니다.");
            }
            int i = dataInputStream.readInt();
            if (i < 0 || i > MAX_TRACKS) {
                throw new IOException("음악목록 크기 오류");
            }
            ArrayList arrayList = new ArrayList();
            HashSet hashSet = new HashSet();
            for (int i2 = 0; i2 < i; i2 += SCHEMA) {
                Track track = new Track(dataInputStream.readUTF(), dataInputStream.readUTF(), dataInputStream.readUTF(), dataInputStream.readLong(), dataInputStream.readUTF());
                file(track);
                if (track.id.isEmpty() || !hashSet.add(track.id) || track.size < 0) {
                    throw new IOException("음악목록 내용 오류");
                }
                arrayList.add(track);
            }
            if (dataInputStream.read() != -1) {
                throw new IOException("음악목록 끝부분 오류");
            }
            dataInputStream.close();
            return arrayList;
        } catch (Throwable th) {
            try {
                dataInputStream.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private void writeIndex(List<Track> list) throws IOException {
        if (list.size() > MAX_TRACKS) {
            throw new IOException("저장 한도 10,000곡을 넘었습니다. 기존 목록은 보존됩니다.");
        }
        if (!this.home.isDirectory() && !this.home.mkdirs()) {
            throw new IOException("음악 보관폴더를 만들지 못했습니다.");
        }
        File file = new File(this.home, "index.bin");
        File file2 = new File(this.home, "index.bak");
        File file3 = new File(this.home, "index.new");
        FileOutputStream fileOutputStream = new FileOutputStream(file3);
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
            try {
                dataOutputStream.writeInt(MAGIC);
                dataOutputStream.writeInt(SCHEMA);
                dataOutputStream.writeInt(list.size());
                for (Track track : list) {
                    dataOutputStream.writeUTF(track.id);
                    dataOutputStream.writeUTF(track.name);
                    dataOutputStream.writeUTF(track.path);
                    dataOutputStream.writeLong(track.size);
                    dataOutputStream.writeUTF(track.hash);
                }
                dataOutputStream.flush();
                fileOutputStream.getFD().sync();
                dataOutputStream.close();
                fileOutputStream.close();
                readIndex(file3);
                if (file.exists()) {
                    if (file2.exists() && !file2.delete()) {
                        throw new IOException("이전 음악목록 백업을 교체하지 못했습니다.");
                    }
                    if (!file.renameTo(file2)) {
                        throw new IOException("기존 음악목록을 보존하지 못했습니다.");
                    }
                }
                if (file3.renameTo(file)) {
                    return;
                }
                if (!file.exists() && file2.exists()) {
                    file2.renameTo(file);
                }
                throw new IOException("새 목록 저장 실패. 기존 음악목록은 보존됩니다.");
            } finally {
            }
        } catch (Throwable th) {
            try {
                fileOutputStream.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    public List<Track> migrateLegacy(String str) throws IOException {
        synchronized (LOCK) {
            if (!new File(this.home, "index.bin").exists() && !new File(this.home, "index.bak").exists()) {
                ArrayList arrayList = new ArrayList();
                File[] fileArrListFiles = new File(this.filesRoot, "v3_music_playlist").listFiles();
                if (fileArrListFiles != null) {
                    Arrays.sort(fileArrListFiles, new Comparator<File>() { // from class: com.baekhak.centralcontrol.MusicLibraryFiles.1
                        @Override // java.util.Comparator
                        public int compare(File file, File file2) {
                            return file.getName().compareTo(file2.getName());
                        }
                    });
                    int length = fileArrListFiles.length;
                    for (int i = 0; i < length; i += SCHEMA) {
                        File file = fileArrListFiles[i];
                        if (file.isFile() && file.length() > 0) {
                            String strReplaceFirst = file.getName().replaceFirst("^\\d{3,}_", "");
                            arrayList.add(new Track("legacy-" + digest(file.getName().getBytes("UTF-8")), strReplaceFirst, "v3_music_playlist/" + file.getName(), file.length(), ""));
                        }
                    }
                }
                File file2 = new File(this.filesRoot, "v3_user_background_audio");
                if (arrayList.isEmpty() && file2.isFile() && file2.length() > 0) {
                    arrayList.add(new Track("legacy-single", (str == null || str.isEmpty()) ? "기존 선택 음악" : str, file2.getName(), file2.length(), ""));
                }
                if (!arrayList.isEmpty()) {
                    writeIndex(arrayList);
                }
                return arrayList;
            }
            return read();
        }
    }

    public List<Track> append(List<Source> list, Progress progress) throws IOException {
        ArrayList arrayList;
        Iterator<Source> it;
        synchronized (LOCK) {
            if (list != null) {
                if (!list.isEmpty()) {
                    List<Track> list2 = read();
                    arrayList = new ArrayList(list2);
                    File file = new File(this.home, "imports/" + UUID.randomUUID());
                    if (!file.mkdirs()) {
                        throw new IOException("음악 저장공간을 준비하지 못했습니다.");
                    }
                    try {
                        HashSet hashSet = new HashSet();
                        for (Track track : list2) {
                            if (!track.hash.isEmpty()) {
                                hashSet.add(track.hash);
                            }
                        }
                        Iterator<Source> it2 = list.iterator();
                        int i = 0;
                        int i2 = 0;
                        while (it2.hasNext()) {
                            Source next = it2.next();
                            String strName = next.name();
                            if (strName == null || strName.trim().isEmpty()) {
                                strName = "선택한 음악";
                            }
                            if (strName.length() > 500) {
                                strName = strName.substring(i, 500);
                            }
                            String str = strName;
                            File file2 = new File(file, UUID.randomUUID() + ".audio");
                            MessageDigest messageDigestSha = sha();
                            InputStream inputStreamOpen = next.open();
                            try {
                                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                                if (inputStreamOpen == null) {
                                    throw new IOException("읽을 수 없는 음악: " + str);
                                }
                                try {
                                    byte[] bArr = new byte[65536];
                                    long j = 0;
                                    while (true) {
                                        int i3 = inputStreamOpen.read(bArr);
                                        it = it2;
                                        if (i3 == -1) {
                                            break;
                                        }
                                        if (i3 != 0) {
                                            fileOutputStream.write(bArr, 0, i3);
                                            messageDigestSha.update(bArr, 0, i3);
                                            j += i3;
                                        }
                                        it2 = it;
                                    }
                                    fileOutputStream.flush();
                                    fileOutputStream.getFD().sync();
                                    fileOutputStream.close();
                                    if (inputStreamOpen != null) {
                                        inputStreamOpen.close();
                                    }
                                    if (j <= 0) {
                                        throw new IOException("비어 있는 음악: " + str);
                                    }
                                    String strHex = hex(messageDigestSha.digest());
                                    for (Track track2 : list2) {
                                        if (track2.hash.isEmpty() && track2.size == j && file(track2).isFile() && hashFile(file(track2)).equals(strHex)) {
                                            hashSet.add(strHex);
                                        }
                                    }
                                    if (hashSet.add(strHex)) {
                                        arrayList.add(new Track(strHex, str, "music_library_v9/imports/" + file.getName() + "/" + file2.getName(), j, strHex));
                                    } else if (!file2.delete()) {
                                        file2.deleteOnExit();
                                    }
                                    i2 += SCHEMA;
                                    if (progress != null) {
                                        progress.changed(i2, list.size());
                                    }
                                    it2 = it;
                                    i = 0;
                                } catch (Throwable th) {
                                    try {
                                        fileOutputStream.close();
                                        throw th;
                                    } catch (Throwable th2) {
                                        th.addSuppressed(th2);
                                        throw th;
                                    }
                                }
                            } finally {
                            }
                        }
                        writeIndex(arrayList);
                    } catch (Throwable th3) {
                        deleteBatch(file);
                        throw th3;
                    }
                }
            }
            throw new IOException("선택한 음악이 없습니다. 기존 목록은 유지됩니다.");
        }
        return arrayList;
    }

    private static void deleteBatch(File file) {
        File[] fileArrListFiles = file.listFiles();
        if (fileArrListFiles != null) {
            int length = fileArrListFiles.length;
            for (int i = 0; i < length; i += SCHEMA) {
                fileArrListFiles[i].delete();
            }
        }
        file.delete();
    }

    private static MessageDigest sha() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static String hex(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        int length = bArr.length;
        for (int i = 0; i < length; i += SCHEMA) {
            sb.append(String.format(Locale.ROOT, "%02x", Integer.valueOf(bArr[i] & 255)));
        }
        return sb.toString();
    }

    private static String digest(byte[] bArr) throws IOException {
        return hex(sha().digest(bArr));
    }

    private static String hashFile(File file) throws IOException {
        MessageDigest messageDigestSha = sha();
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            byte[] bArr = new byte[65536];
            while (true) {
                int i = fileInputStream.read(bArr);
                if (i == -1) {
                    fileInputStream.close();
                    return hex(messageDigestSha.digest());
                }
                if (i > 0) {
                    messageDigestSha.update(bArr, 0, i);
                }
            }
        } catch (Throwable th) {
            try {
                fileInputStream.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }
}
