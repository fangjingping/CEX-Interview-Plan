package com.cex.exchange.demo.gateway;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuthService 核心类。
 */
public class AuthService {
    private final long windowMillis;
    private final Set<String> usedNonces = ConcurrentHashMap.newKeySet();

    public AuthService(long windowMillis) {
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be > 0");
        }
        this.windowMillis = windowMillis;
    }

    public boolean verify(ApiKey apiKey, SignedRequest request, long nowMillis) {
        Objects.requireNonNull(apiKey, "apiKey");
        Objects.requireNonNull(request, "request");
        if (!apiKey.keyId().equals(request.apiKeyId())) {
            return false;
        }
        long diff = Math.abs(nowMillis - request.timestamp());
        if (diff > windowMillis) {
            return false;
        }
        String expected = SignatureUtil.sign(apiKey.secret(), request.timestamp(), request.nonce(), request.payload());
        if (!expected.equals(request.signature())) {
            return false;
        }
        String nonceKey = apiKey.keyId() + ":" + request.nonce();
        return usedNonces.add(nonceKey);
    }
}
