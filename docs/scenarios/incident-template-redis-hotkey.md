# Incident Template - Redis Hot Key or Cache Stampede

## Summary
- What happened:
- Start time (UTC):
- End time (UTC):
- Severity:

## Impact
- Affected services:
- Error rate / latency:
- Backend load impact:

## Detection and Alerts
- Alert name:
- Trigger condition:
- Detection time:

## Timeline (UTC)
- T0:
- T+5m:
- T+15m:
- T+30m:
- Resolution:

## Symptoms
- High CPU on single Redis node
- Elevated latency for a subset of keys
- Backend load spike (cache miss storm)

## Root Cause
- Hot key pattern:
- Why TTL or sharding did not help:

## Contributing Factors
- Key design (hash tags, shard distribution):
- TTL strategy:
- Client retry behavior:

## Immediate Mitigation
- Apply local cache or request coalescing
- Add rate limit or circuit breaker
- Hot key replication or split key

## Recovery Steps
- Validate cache hit ratio
- Reduce backend pressure
- Normalize latency and CPU

## Verification
- Hit ratio back to baseline
- Latency p99 normal
- No elevated evictions

## Communications
- Internal update time:
- External update time:
- Customer-facing summary:

## Metrics / Logs / Commands Checklist
Metrics:
- keyspace_hits/keyspace_misses
- instantaneous_ops_per_sec
- used_memory, evicted_keys
- blocked_clients

Logs:
- Redis server log
- Client error logs

Commands:
```
redis-cli INFO
redis-cli SLOWLOG GET 10
redis-cli LATENCY DOCTOR
redis-cli CLIENT LIST
```

## Action Items
- Short term fixes:
- Long term fixes:
- Ownership and due dates:
