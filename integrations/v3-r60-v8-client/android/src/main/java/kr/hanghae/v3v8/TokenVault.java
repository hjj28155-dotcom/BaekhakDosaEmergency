package kr.hanghae.v3v8;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Provision from trusted native enrollment only. Never expose a setter/getter to JS. */
public final class TokenVault {
    private static final String ALIAS = "v3_v8_device_token_v1";
    private static synchronized SecretKey key() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        if (ks.containsAlias(ALIAS)) return (SecretKey) ks.getKey(ALIAS, null);
        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        kg.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return kg.generateKey();
    }
    public static void provision(Context context, String deviceToken) throws Exception {
        if (deviceToken == null || !deviceToken.matches("[a-f0-9]{64}")) throw new IllegalArgumentException("INVALID_DEVICE_TOKEN");
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE, key());
        byte[] ciphertext = c.doFinal(deviceToken.getBytes(StandardCharsets.UTF_8));
        if (!context.getSharedPreferences("v3_v8_auth", 0).edit()
            .putString("iv", Base64.encodeToString(c.getIV(), Base64.NO_WRAP))
            .putString("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP)).commit())
            throw new IllegalStateException("TOKEN_SAVE_FAILED");
    }
    static String read(Context context) throws Exception {
        var p = context.getSharedPreferences("v3_v8_auth", 0);
        String iv = p.getString("iv", null), data = p.getString("ciphertext", null);
        if (iv == null || data == null) throw new IllegalStateException("DEVICE_ENROLLMENT_REQUIRED");
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)));
        String token = new String(c.doFinal(Base64.decode(data, Base64.NO_WRAP)), StandardCharsets.UTF_8);
        if (!token.matches("[a-f0-9]{64}")) throw new IllegalStateException("TOKEN_INVALID");
        return token;
    }
}
