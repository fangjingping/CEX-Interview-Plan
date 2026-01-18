# Redis Ops Handbook

## Deployment Topology and Components
- Standalone: simplest, single node, no HA.
- Primary-replica: one primary, multiple replicas, optional read scaling.
- Sentinel: monitors primary, performs failover.
- Cluster: sharded slots with replicas per shard.

Example topology (cluster with replicas):
```
Clients --> Proxy or SDK --> Redis Cluster
Shard 1: primary + replica
Shard 2: primary + replica
Shard 3: primary + replica
```

## Key Parameters and Tuning (with rationale)
- `maxmemory`: bound memory usage; prevent OOM.
- `maxmemory-policy`: choose eviction (allkeys-lru/volatile-ttl) for cache.
- `appendonly yes`: durability for write-heavy data; tradeoff latency.
- `appendfsync everysec`: balance durability and throughput.
- `save` rules: snapshots for recovery; avoid heavy sync during peak.
- `repl-backlog-size`: keep backlog for partial resync.
- `repl-diskless-sync yes`: faster replica sync for large datasets.
- `client-output-buffer-limit`: protect memory from slow clients.
- `io-threads`: improve read throughput on multi-core.
- `tcp-keepalive`: detect dead clients faster.

## Common Failure Cases (at least 5)
- Hot key: single key saturates CPU and network.
- Cache stampede: many misses trigger backend overload.
- Eviction storm: memory pressure causes sudden cache loss.
- Replica lag: replicas fall behind, stale reads.
- Split brain: network partition causes dual primaries.
- AOF fsync stalls: write latency spikes.
- Big keys: blocking operations and long GC pauses.

## Troubleshooting Runbook
Metrics:
- `used_memory`, `maxmemory`, `mem_fragmentation_ratio`.
- `keyspace_hits`, `keyspace_misses`, `evicted_keys`.
- `blocked_clients`, `connected_clients`, `instantaneous_ops_per_sec`.
- `instantaneous_input_kbps`, `instantaneous_output_kbps`.

Logs:
- Redis server log for failover, loading, and persistence errors.
- Slowlog for long-running commands.

Commands:
```
redis-cli INFO
redis-cli INFO memory
redis-cli SLOWLOG GET 10
redis-cli LATENCY DOCTOR
redis-cli CLIENT LIST
redis-cli MEMORY STATS

# Cluster
redis-cli CLUSTER INFO
redis-cli CLUSTER NODES
```

## Exchange Tradeoffs (ordering, idempotency, latency, throughput)
- Ordering: Redis is single-threaded per node; cross-shard ordering is not guaranteed.
- Idempotency: good for short-lived tokens and de-dup keys with TTL.
- Latency: in-memory reads are fast; persistence adds jitter.
- Throughput: sharding scales reads/writes but increases operational complexity.
- Consistency: replicas are async; avoid using replicas for strict balance reads.
