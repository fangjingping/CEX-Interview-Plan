package com.cex.exchange.demo.profiling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ProfilingPlaygroundTest 单元测试。
 */
class ProfilingPlaygroundTest {

    @Test
    void runsCpuLoop() {
        ProfilingPlayground playground = new ProfilingPlayground();
        long expected = 42L;
        for (int i = 0; i < 10; i++) {
            expected = expected * 31 + i;
        }
        assertEquals(expected, playground.cpuLoop(10));
    }

    @Test
    void runsContentionTask() {
        ProfilingPlayground playground = new ProfilingPlayground();
        assertEquals(100, playground.contendedIncrement(2, 50));
    }

    @Test
    void runsAllocationTask() {
        ProfilingPlayground playground = new ProfilingPlayground();
        assertEquals(10, playground.allocateObjects(10));
    }
}
