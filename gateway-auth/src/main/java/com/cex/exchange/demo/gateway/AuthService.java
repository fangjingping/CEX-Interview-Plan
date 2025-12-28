package com.cex.exchange.demo.gateway;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AuthService 核心类。
 */
public class AuthService {
    private final long windowMillis;
    private final long nonceTtlMillis;
    private final ConcurrentHashMap<String, Long> nonceExpiresAt = new ConcurrentHashMap<>();

    public AuthService(long windowMillis) {
        this(windowMillis, windowMillis);
    }

    public AuthService(long windowMillis, long nonceTtlMillis) {
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("windowMillis must be > 0");
        }
        if (nonceTtlMillis <= 0) {
            throw new IllegalArgumentException("nonceTtlMillis must be > 0");
        }
        this.windowMillis = windowMillis;
        this.nonceTtlMillis = nonceTtlMillis;
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
        return registerNonce(nonceKey, nowMillis);
    }

    private boolean registerNonce(String nonceKey, long nowMillis) {
        AtomicBoolean accepted = new AtomicBoolean(false);
        long nextExpiresAt = nowMillis + nonceTtlMillis;
        nonceExpiresAt.compute(nonceKey, (key, existing) -> {
            if (existing == null || existing <= nowMillis) {
                accepted.set(true);
                return nextExpiresAt;
            }
            return existing;
        });
        return accepted.get();
    }
}
