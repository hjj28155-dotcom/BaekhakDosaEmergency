package kr.hanghae.v3v8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONObject;

/** Separate durable copy. Never opens or modifies the WebView's original IndexedDB. */
final class Outbox extends SQLiteOpenHelper {
    Outbox(Context c) { super(c, "v3_v8_transfer.sqlite", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE reports (round_no INTEGER PRIMARY KEY, payload TEXT NOT NULL, sha TEXT NOT NULL, state TEXT NOT NULL, asset TEXT, receipt TEXT)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("EXPLICIT_MIGRATION_REQUIRED");
    }
    synchronized void enqueue(int round, String payload, String sha) throws Exception {
        SQLiteDatabase db = getWritableDatabase(); db.beginTransaction();
        try {
            Row old = row(round);
            if (old != null) {
                if (!old.sha.equals(sha) || !old.payload.equals(payload)) throw new IllegalStateException("IMMUTABLE_ROUND_CONFLICT");
            } else {
                ContentValues v = new ContentValues(); v.put("round_no", round); v.put("payload", payload);
                v.put("sha", sha); v.put("state", "QUEUED"); db.insertOrThrow("reports", null, v);
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
    Row row(int round) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT payload,sha,state,asset,receipt FROM reports WHERE round_no=?", new String[]{String.valueOf(round)})) {
            if (!c.moveToFirst()) return null;
            return new Row(round, c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4));
        }
    }
    void state(int round, String state) {
        ContentValues v = new ContentValues(); v.put("state", state);
        getWritableDatabase().update("reports", v, "round_no=? AND state!='VERIFIED'", new String[]{String.valueOf(round)});
    }
    synchronized void checkpoint(Row row, String asset) throws Exception {
        Row current = row(row.round);
        if (current == null || !current.sha.equals(row.sha) || (current.asset != null && !current.asset.equals(asset)))
            throw new IllegalStateException("ASSET_CHECKPOINT_CONFLICT");
        ContentValues v = new ContentValues(); v.put("asset", asset);
        getWritableDatabase().update("reports", v, "round_no=? AND sha=?", new String[]{String.valueOf(row.round), row.sha});
    }
    synchronized void verified(Row row, JSONObject receipt) throws Exception {
        Row current = row(row.round);
        if (current == null || !current.sha.equals(row.sha) || !receipt.getString("asset_id").equals(current.asset))
            throw new IllegalStateException("VERIFY_CHECKPOINT_CONFLICT");
        if ("VERIFIED".equals(current.state)) {
            if (!new JSONObject(current.receipt).getString("receipt_id").equals(receipt.getString("receipt_id")))
                throw new IllegalStateException("RECEIPT_CONFLICT");
            return;
        }
        ContentValues v = new ContentValues(); v.put("state", "VERIFIED"); v.put("receipt", receipt.toString());
        getWritableDatabase().update("reports", v, "round_no=? AND sha=?", new String[]{String.valueOf(row.round), row.sha});
    }
    static final class Row {
        final int round; final String payload, sha, state, asset, receipt;
        Row(int round, String payload, String sha, String state, String asset, String receipt) {
            this.round=round; this.payload=payload; this.sha=sha; this.state=state; this.asset=asset; this.receipt=receipt;
        }
    }
}
