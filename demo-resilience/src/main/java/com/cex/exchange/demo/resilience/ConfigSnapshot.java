package com.cex.exchange.demo.resilience;

import java.util.Map;

/**
 * ConfigSnapshot 记录类型。
 */
public record ConfigSnapshot(long version, Map<String, String> values) {
}
