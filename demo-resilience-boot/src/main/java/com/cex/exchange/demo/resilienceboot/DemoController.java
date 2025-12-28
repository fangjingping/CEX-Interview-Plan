package com.cex.exchange.demo.resilienceboot;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * DemoController 核心类。
 */
@RestController
@RequestMapping("/api")
public class DemoController {
    private final AtomicInteger counter = new AtomicInteger();

    @GetMapping("/price")
    @RateLimiter(name = "priceLimiter")
    public String price() {
        return "price:" + counter.incrementAndGet();
    }

    @GetMapping("/risk")
    @CircuitBreaker(name = "riskBreaker", fallbackMethod = "riskFallback")
    public String risk(@RequestParam(defaultValue = "false") boolean fail) {
        if (fail) {
            throw new IllegalStateException("forced failure");
        }
        return "ok";
    }

    public String riskFallback(boolean fail, Throwable ex) {
        return "fallback";
    }
}
