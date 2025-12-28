package com.cex.exchange.demo.gateway;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * SignatureUtil 核心类。
 */
public class SignatureUtil {
    private static final String HMAC_ALGO = "HmacSHA256";

    public static String sign(String secret, long timestamp, String nonce, String payload) {
        String message = timestamp + "\n" + nonce + "\n" + payload;
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] digest = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("signature failure", ex);
        }
    }

    private SignatureUtil() {
    }
}
