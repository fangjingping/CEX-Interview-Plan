package com.cex.exchange.demo.profiling;

/**
 * ProfilingPlayground 核心类。
 */
public class ProfilingPlayground {
    private final CpuIntensiveTask cpuTask = new CpuIntensiveTask();
    private final LockContentionTask contentionTask = new LockContentionTask();
    private final AllocationTask allocationTask = new AllocationTask();

    public long cpuLoop(int iterations) {
        return cpuTask.compute(42L, iterations);
    }

    public long contendedIncrement(int threads, int iterations) {
        return contentionTask.run(threads, iterations);
    }

    public int allocateObjects(int count) {
        return allocationTask.allocate(count);
    }
}
