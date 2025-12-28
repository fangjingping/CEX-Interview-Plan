package com.cex.exchange.demo.resilience;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceRegistry 核心类。
 */
public class ServiceRegistry {
    private final Map<String, Map<String, ServiceInstance>> instances = new ConcurrentHashMap<>();

    public void register(ServiceInstance instance) {
        instances
                .computeIfAbsent(instance.serviceName(), key -> new ConcurrentHashMap<>())
                .put(instance.instanceId(), instance);
    }

    public void unregister(ServiceInstance instance) {
        Map<String, ServiceInstance> serviceInstances = instances.get(instance.serviceName());
        if (serviceInstances != null) {
            serviceInstances.remove(instance.instanceId());
        }
    }

    public List<ServiceInstance> discover(String serviceName) {
        Map<String, ServiceInstance> serviceInstances = instances.get(serviceName);
        if (serviceInstances == null) {
            return List.of();
        }
        return new ArrayList<>(serviceInstances.values());
    }
}
