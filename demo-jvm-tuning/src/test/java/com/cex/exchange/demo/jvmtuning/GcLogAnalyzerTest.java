package com.cex.exchange.demo.jvmtuning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * GcLogAnalyzerTest 单元测试。
 */
class GcLogAnalyzerTest {

    @Test
    void parsesPauseStats() {
        List<String> lines = List.of(
                "[info][gc] Pause Young (Normal) (G1 Evacuation Pause) 5.0ms",
                "[info][gc] Pause Young (Normal) (G1 Evacuation Pause) 12.5ms",
                "[info][gc] Pause Full (System.gc()) 120.0ms"
        );

        GcLogAnalyzer analyzer = new GcLogAnalyzer();
        GcPauseStats stats = analyzer.analyze(lines);

        assertEquals(3, stats.getCount());
        assertEquals(120.0, stats.getMaxMillis(), 0.001);
        assertEquals(120.0, stats.getP95Millis(), 0.001);
        assertEquals(120.0, stats.getP99Millis(), 0.001);
    }

    @Test
    void returnsZeroWhenNoPause() {
        GcLogAnalyzer analyzer = new GcLogAnalyzer();
        GcPauseStats stats = analyzer.analyze(List.of("[info][gc] concurrent cycle"));

        assertEquals(0, stats.getCount());
        assertEquals(0.0, stats.getMaxMillis(), 0.001);
    }
}
