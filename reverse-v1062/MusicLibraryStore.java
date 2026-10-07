package com.baekhak.centralcontrol;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import com.baekhak.centralcontrol.MusicLibraryFiles;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class MusicLibraryStore {
    private static volatile List<MusicLibraryFiles.Track> cached = Collections.emptyList();
    private static volatile boolean importing = false;
    private static volatile String progress = "";
    private final Context context;
    private final MusicLibraryFiles files;

    public MusicLibraryStore(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.context = applicationContext;
        this.files = new MusicLibraryFiles(applicationContext.getFilesDir());
    }

    public List<MusicLibraryFiles.Track> read() throws IOException {
        List<MusicLibraryFiles.Track> listMigrateLegacy = this.files.migrateLegacy(this.context.getSharedPreferences("v3_music_preferences", 0).getString("music_name", ""));
        cached = Collections.unmodifiableList(new ArrayList(listMigrateLegacy));
        return listMigrateLegacy;
    }

    public File file(MusicLibraryFiles.Track track) throws IOException {
        return this.files.file(track);
    }

    public static String progress() {
        return progress;
    }

    public static boolean importing() {
        return importing;
    }

    public String asJson() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        try {
            for (MusicLibraryFiles.Track track : importing ? cached : read()) {
                JSONObject jSONObject2 = new JSONObject();
                jSONObject2.put("id", track.id);
                jSONObject2.put(RadioPlaybackService.EXTRA_NAME, track.name);
                jSONObject2.put("size", track.size);
                jSONObject2.put("available", file(track).isFile());
                jSONArray.put(jSONObject2);
            }
            boolean z = true;
            jSONObject.put("ok", true);
            jSONObject.put("items", jSONArray);
            String string = this.context.getSharedPreferences("v3_music_preferences", 0).getString("music_library_folder_uri", "");
            if (string == null || string.isEmpty()) {
                z = false;
            }
            jSONObject.put("folderConnected", z);
            jSONObject.put("importing", importing);
            jSONObject.put("progress", progress);
        } catch (Exception e) {
            try {
                jSONObject.put("ok", false);
                jSONObject.put("error", e.getMessage());
            } catch (Exception unused) {
            }
        }
        return jSONObject.toString();
    }

    public int appendUris(List<Uri> list, int i) throws IOException {
        int size;
        String type;
        synchronized (MusicLibraryStore.class) {
            importing = true;
            progress = "음악을 보관하는 중입니다. 기존 목록은 유지됩니다.";
            try {
                try {
                    read();
                    ArrayList arrayList = new ArrayList();
                    HashSet hashSet = new HashSet();
                    for (final Uri uri : list) {
                        if (uri != null && hashSet.add(uri.toString())) {
                            final String strName = name(uri);
                            try {
                                type = this.context.getContentResolver().getType(uri);
                            } catch (Exception unused) {
                                type = null;
                            }
                            if (audio(strName, type)) {
                                try {
                                    this.context.getContentResolver().takePersistableUriPermission(uri, 1);
                                } catch (Exception unused2) {
                                }
                                arrayList.add(new MusicLibraryFiles.Source() { // from class: com.baekhak.centralcontrol.MusicLibraryStore.1
                                    @Override // com.baekhak.centralcontrol.MusicLibraryFiles.Source
                                    public String name() {
                                        return strName;
                                    }

                                    @Override // com.baekhak.centralcontrol.MusicLibraryFiles.Source
                                    public InputStream open() throws IOException {
                                        try {
                                            return "file".equals(uri.getScheme()) ? new FileInputStream(new File(uri.getPath())) : MusicLibraryStore.this.context.getContentResolver().openInputStream(uri);
                                        } catch (SecurityException e) {
                                            throw new IOException("음악 읽기 권한이 없습니다: " + strName, e);
                                        }
                                    }
                                });
                            }
                        }
                    }
                    List<MusicLibraryFiles.Track> listAppend = this.files.append(arrayList, new MusicLibraryFiles.Progress() { // from class: com.baekhak.centralcontrol.MusicLibraryStore$$ExternalSyntheticLambda0
                        @Override // com.baekhak.centralcontrol.MusicLibraryFiles.Progress
                        public final void changed(int i2, int i3) {
                            MusicLibraryStore.progress = "음악 보관 중 " + i2 + " / " + i3 + "곡";
                        }
                    });
                    cached = Collections.unmodifiableList(new ArrayList(listAppend));
                    progress = "저장된 음악 " + listAppend.size() + "곡 · 기존 선택 목록 유지";
                    size = listAppend.size();
                } catch (IOException e) {
                    progress = "저장 실패: " + e.getMessage() + " · 기존 목록 유지";
                    throw e;
                }
            } finally {
                importing = false;
            }
        }
        return size;
    }

    public int appendFolder(Uri uri) throws IOException {
        try {
            this.context.getContentResolver().takePersistableUriPermission(uri, 1);
        } catch (Exception unused) {
        }
        ArrayList arrayList = new ArrayList();
        collect(uri, DocumentsContract.getTreeDocumentId(uri), arrayList, 0);
        int iAppendUris = appendUris(arrayList, 1);
        this.context.getSharedPreferences("v3_music_preferences", 0).edit().putString("music_library_folder_uri", uri.toString()).commit();
        return iAppendUris;
    }

    private void collect(Uri uri, String str, List<Uri> list, int i) throws IOException {
        if (i > 12) {
            throw new IOException("음악 폴더가 너무 깊습니다. 음악이 들어 있는 하위 폴더를 선택하세요.");
        }
        Uri uriBuildChildDocumentsUriUsingTree = DocumentsContract.buildChildDocumentsUriUsingTree(uri, str);
        String[] strArr = {"document_id", "_display_name", "mime_type"};
        ArrayList<String[]> arrayList = new ArrayList();
        try {
            Cursor cursorQuery = this.context.getContentResolver().query(uriBuildChildDocumentsUriUsingTree, strArr, null, null, null);
            try {
                if (cursorQuery == null) {
                    throw new IOException("음악 폴더를 읽지 못했습니다.");
                }
                while (cursorQuery.moveToNext()) {
                    arrayList.add(new String[]{cursorQuery.getString(0), cursorQuery.getString(1), cursorQuery.getString(2)});
                }
                if (cursorQuery != null) {
                    cursorQuery.close();
                }
                Collections.sort(arrayList, new Comparator() { // from class: com.baekhak.centralcontrol.MusicLibraryStore$$ExternalSyntheticLambda1
                    @Override // java.util.Comparator
                    public final int compare(Object obj, Object obj2) {
                        return String.valueOf(((String[]) obj)[1]).compareToIgnoreCase(String.valueOf(((String[]) obj2)[1]));
                    }
                });
                for (String[] strArr2 : arrayList) {
                    if ("vnd.android.document/directory".equals(strArr2[2])) {
                        collect(uri, strArr2[0], list, i + 1);
                    } else if (audio(strArr2[1], strArr2[2])) {
                        list.add(DocumentsContract.buildDocumentUriUsingTree(uri, strArr2[0]));
                    }
                    if (list.size() > 10000) {
                        throw new IOException("한 번에 10,000곡 이하의 음악 폴더를 선택하세요.");
                    }
                }
            } finally {
            }
        } catch (RuntimeException e) {
            throw new IOException("음악 폴더 읽기 실패. 기존 목록은 유지됩니다.", e);
        }
    }

    private String name(Uri uri) {
        try {
            Cursor cursorQuery = this.context.getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.moveToFirst()) {
                        String string = cursorQuery.getString(0);
                        if (string != null) {
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return string;
                        }
                    }
                } finally {
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        } catch (Exception unused) {
        }
        String lastPathSegment = uri.getLastPathSegment();
        return lastPathSegment == null ? "선택한 음악" : lastPathSegment;
    }

    private static boolean audio(String str, String str2) {
        return (str2 != null && str2.startsWith("audio/")) || (str == null ? "" : str.toLowerCase(Locale.ROOT)).matches(".*\\.(mp3|m4a|aac|wav|ogg|flac|opus)$");
    }
}
