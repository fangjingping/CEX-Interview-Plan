package com.cex.exchange.demo.resilience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ServiceRegistryTest 单元测试。
 */
class ServiceRegistryTest {

    @Test
    void registersAndUnregistersInstances() {
        ServiceRegistry registry = new ServiceRegistry();
        ServiceInstance instance = new ServiceInstance("risk", "i1", "127.0.0.1", 8080);

        registry.register(instance);
        assertEquals(1, registry.discover("risk").size());

        registry.unregister(instance);
        assertEquals(0, registry.discover("risk").size());
    }
}
