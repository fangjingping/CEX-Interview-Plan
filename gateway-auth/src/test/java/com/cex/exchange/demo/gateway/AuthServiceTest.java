package com.cex.exchange.demo.gateway;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AuthServiceTest 单元测试。
 */
class AuthServiceTest {

    @Test
    void verifiesSignatureAndRejectsReplay() {
        ApiKey apiKey = new ApiKey("K1", "secret");
        long now = 10_000L;
        String nonce = "N1";
        String payload = "{\"symbol\":\"BTC-USDT\"}";
        String signature = SignatureUtil.sign(apiKey.secret(), now, nonce, payload);
        SignedRequest request = new SignedRequest(apiKey.keyId(), now, nonce, payload, signature);

        AuthService authService = new AuthService(30_000L);
        assertTrue(authService.verify(apiKey, request, now));
        assertFalse(authService.verify(apiKey, request, now));
    }

    @Test
    void rejectsOutOfWindowTimestamp() {
        ApiKey apiKey = new ApiKey("K1", "secret");
        long now = 100_000L;
        String signature = SignatureUtil.sign(apiKey.secret(), now - 60_000L, "N2", "payload");
        SignedRequest request = new SignedRequest(apiKey.keyId(), now - 60_000L, "N2", "payload", signature);

        AuthService authService = new AuthService(30_000L);
        assertFalse(authService.verify(apiKey, request, now));
    }
}
