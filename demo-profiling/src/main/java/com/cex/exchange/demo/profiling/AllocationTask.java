package com.cex.exchange.demo.profiling;

import java.util.ArrayList;
import java.util.List;

/**
 * AllocationTask 核心类。
 */
public class AllocationTask {
    public int allocate(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        List<byte[]> buffers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            buffers.add(new byte[1024]);
        }
        return buffers.size();
    }
}
