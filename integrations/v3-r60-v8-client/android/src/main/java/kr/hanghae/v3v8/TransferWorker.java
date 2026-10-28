package kr.hanghae.v3v8;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.json.JSONObject;

/** Sends locked reports only. No analysis generation or V7 request paths. */
public final class TransferWorker extends Worker {
    private static final String BASE = "https://jyqkohpeowkhuaohlezc.supabase.co/functions/v1/";
    public TransferWorker(@NonNull Context context, @NonNull WorkerParameters params) { super(context, params); }
    private static final class HttpFailure extends Exception {
        final int code; HttpFailure(int code) { this.code=code; }
    }
    private JSONObject request(String path, String token, byte[] body, String contentType) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(BASE + path).openConnection();
        c.setConnectTimeout(20000); c.setReadTimeout(30000); c.setInstanceFollowRedirects(false);
        c.setRequestProperty("Authorization", "Bearer " + token);
        c.setRequestProperty("Accept", "application/json");
        try {
            if (body != null) {
                c.setRequestMethod("POST"); c.setDoOutput(true); c.setRequestProperty("Content-Type", contentType);
                c.setFixedLengthStreamingMode(body.length);
                try (var out = c.getOutputStream()) { out.write(body); }
            }
            int status = c.getResponseCode();
            if (status < 200 || status >= 300) throw new HttpFailure(status);
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096]; int count;
                while ((count = in.read(buf)) != -1) {
                    if (out.size() + count > 65536) throw new IllegalStateException("RESPONSE_TOO_LARGE");
                    out.write(buf, 0, count);
                }
                return new JSONObject(out.toString("UTF-8"));
            }
        } finally { c.disconnect(); }
    }
    private String upload(Outbox.Row row, String token) throws Exception {
        String boundary = "V3V8_" + UUID.randomUUID().toString().replace("-", "");
        String filename = "V3_" + V3V8Native.EPOCH + "_" + row.round + "_RESULT_BEFORE.json";
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        String fields = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"room_key\"\r\n\r\nextra_navigator\r\n" +
            "--" + boundary + "\r\nContent-Disposition: form-data; name=\"round_no\"\r\n\r\n" + row.round + "\r\n" +
            "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\nContent-Type: application/json\r\n\r\n";
        body.write(fields.getBytes(StandardCharsets.UTF_8));
        body.write(row.payload.getBytes(StandardCharsets.UTF_8)); // Exact original JSON bytes.
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        JSONObject r = request("v6-room-library-sync/upload", token, body.toByteArray(), "multipart/form-data; boundary=" + boundary);
        if (!r.optBoolean("ok") || !row.sha.equals(r.optString("sha256"))) throw new IllegalStateException("UPLOAD_HASH_MISMATCH");
        return uuid(r.getString("asset_id"));
    }
    private static String uuid(String value) {
        if (!value.matches("(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) throw new IllegalStateException("UUID_INVALID");
        return value;
    }
    private void validate(JSONObject r, Outbox.Row row, String asset) throws Exception {
        uuid(r.getString("receipt_id")); uuid(r.getString("asset_id"));
        JSONObject payload = new JSONObject(row.payload);
        if (!r.getBoolean("ok") || !r.getBoolean("server_verified") ||
            !"ACCEPTED_RESEARCH_ARCHIVE".equals(r.getString("status")) ||
            !"V3_NAVIGATOR".equals(r.getString("origin_app")) ||
            !V3V8Native.EPOCH.equals(r.getString("epoch")) || r.getInt("round") != row.round ||
            !row.sha.equals(r.getString("payload_sha256")) || !asset.equals(r.getString("asset_id")) ||
            !"V8/V3/result_before".equals(r.getString("archive")) || r.getBoolean("official_predraw_certified") ||
            !payload.getString("analysisMode").equals(r.getString("analysis_mode")) ||
            Instant.parse(r.getString("accepted_at")).toEpochMilli() > System.currentTimeMillis() + 300000)
            throw new IllegalStateException("RECEIPT_MISMATCH");
    }
    @NonNull @Override public Result doWork() {
        int round = getInputData().getInt("round", -1);
        if (round < 1 || round > 9999) return Result.failure();
        try (Outbox db = new Outbox(getApplicationContext())) {
            Outbox.Row row = db.row(round);
            if (row == null || "VERIFIED".equals(row.state) || "HOLD".equals(row.state)) return Result.success();
            String token;
            try { token = TokenVault.read(getApplicationContext()); }
            catch (Exception e) { db.state(round, "NEEDS_AUTH"); return Result.success(); }
            try {
                if (!row.sha.equals(V3V8Native.sha(row.payload.getBytes(StandardCharsets.UTF_8)))) throw new IllegalStateException("LOCAL_BYTES_CHANGED");
                String asset = row.asset;
                if (asset == null) { asset = upload(row, token); db.checkpoint(row, asset); }
                JSONObject body = new JSONObject().put("asset_id", asset).put("origin_app", "V3_NAVIGATOR")
                    .put("epoch", V3V8Native.EPOCH).put("round", round).put("payload_sha256", row.sha);
                JSONObject accepted = request("v8-v3-resultbefore-receive/receive", token,
                    body.toString().getBytes(StandardCharsets.UTF_8), "application/json");
                validate(accepted, row, asset);
                JSONObject verified = request("v8-v3-resultbefore-receive/receipt?epoch=" + V3V8Native.EPOCH + "&round=" + round, token, null, null);
                validate(verified, row, asset);
                if (!accepted.getString("receipt_id").equals(verified.getString("receipt_id")) ||
                    !accepted.getString("accepted_at").equals(verified.getString("accepted_at"))) throw new IllegalStateException("POST_GET_RECEIPT_CONFLICT");
                db.verified(row, verified); return Result.success();
            } catch (HttpFailure e) {
                if (e.code == 401 || e.code == 403) { db.state(round, "NEEDS_AUTH"); return Result.success(); }
                if (e.code >= 400 && e.code < 500 && e.code != 408 && e.code != 429) { db.state(round, "HOLD"); return Result.success(); }
                db.state(round, "RETRY"); return Result.retry();
            } catch (IllegalStateException | org.json.JSONException | java.time.format.DateTimeParseException e) {
                db.state(round, "HOLD"); return Result.success();
            } catch (Exception e) {
                db.state(round, "RETRY"); return Result.retry();
            }
        } catch (Exception e) { return Result.retry(); }
    }
}
