package kr.hanghae.v3v8;

import android.content.Context;
import android.webkit.JavascriptInterface;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

/** Bind ONLY to a trusted local-assets WebView. Host must block remote navigation. */
public final class V3V8Native {
    static final String EPOCH = "V3_RESTART_R53_20261028";
    private final Context context;
    public V3V8Native(Context context) { this.context = context.getApplicationContext(); }
    static String sha(byte[] bytes) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder result = new StringBuilder(); for (byte b : hash) result.append(String.format("%02x", b & 255));
        return result.toString();
    }
    static void schedule(Context context, int round) {
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(TransferWorker.class)
            .setInputData(new Data.Builder().putInt("round", round).build())
            .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork("v3-v8-round-" + round, ExistingWorkPolicy.KEEP, work);
    }
    @JavascriptInterface public String enqueue(String payload, String hash) {
        try (Outbox db = new Outbox(context)) {
            if (payload == null || payload.length() > 300000) throw new IllegalArgumentException();
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 100 || bytes.length > 300000 || hash == null || !sha(bytes).equals(hash)) throw new IllegalArgumentException();
            JSONObject p = new JSONObject(payload); int round = p.getInt("round");
            if (round < 1 || round > 9999 || !EPOCH.equals(p.getString("epoch")) ||
                !"V3_NAVIGATOR".equals(p.getString("originApp")) ||
                !"V7_V3_NAVIGATOR_20_RESULT_BEFORE_HANDOFF_V1".equals(p.getString("schema")) ||
                !"PENDING_UNSENT".equals(p.getString("submissionStatus"))) throw new IllegalArgumentException();
            db.enqueue(round, payload, hash);
            String state = db.row(round).state;
            if (!"VERIFIED".equals(state) && !"HOLD".equals(state)) schedule(context, round);
            return new JSONObject().put("ok", true).toString();
        } catch (Exception e) { return "{\"ok\":false,\"error\":\"NATIVE_QUEUE_REJECTED\"}"; }
    }
    @JavascriptInterface public String poll(int round, String hash) {
        try (Outbox db = new Outbox(context)) {
            Outbox.Row row = db.row(round);
            if (row == null || !row.sha.equals(hash)) return "{\"state\":\"MISSING_OR_HASH_MISMATCH\"}";
            JSONObject result = new JSONObject().put("state", row.state);
            if ("VERIFIED".equals(row.state)) result.put("receipt", new JSONObject(row.receipt));
            return result.toString();
        } catch (Exception e) { return "{\"state\":\"NATIVE_READ_FAILED\"}"; }
    }
}
