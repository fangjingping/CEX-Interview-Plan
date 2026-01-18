# Elasticsearch Ops Handbook

## Deployment Topology and Components
- Master-eligible nodes: manage cluster state.
- Data nodes: store shards and serve search.
- Ingest nodes: pipelines for enrichment.
- Coordinating nodes: query routing and fan-out.

Example topology (3 masters, 6 data nodes):
```
Clients --> Coordinating nodes --> Data nodes (shards)
            |-> Master nodes (cluster state)
Ingest nodes sit in front of data nodes if used
```

## Key Parameters and Tuning (with rationale)
- `number_of_shards`: size for parallelism; too many hurts overhead.
- `number_of_replicas`: improves read scale and HA; costs write TPS.
- `refresh_interval`: larger reduces write amplification; smaller reduces search latency.
- `translog.durability=request|async`: async improves throughput with risk.
- `indices.memory.index_buffer_size`: controls indexing buffer pressure.
- `cluster.routing.allocation.awareness`: keep replicas across AZs.
- `cluster.routing.allocation.disk.watermark.*`: prevent disk full incidents.
- `thread_pool.write.queue_size`: avoid rejecting writes too early.
- `search.max_buckets`: protect against heavy aggregations.

## Common Failure Cases (at least 5)
- Write rejections: thread pool queue full.
- Cluster red/yellow: unassigned shards or failed nodes.
- Slow queries: heavy aggregations, cold cache, or large results.
- Hot shard: uneven routing causes single shard overload.
- Long GC or heap pressure: leads to timeouts and node drops.
- Disk watermark triggered: shards stop allocating.
- Mapping explosion: too many fields, slow indexing.

## Troubleshooting Runbook
Metrics:
- Cluster health, node heap usage, GC time.
- `thread_pool.write` and `thread_pool.search` queue and rejected counts.
- Indexing and search latency (p50/p99).
- Disk usage per node and per shard.

Logs:
- Elasticsearch logs for shard allocation, GC, and circuit breakers.

Commands:
```
# Cluster status
curl -s http://<host>:9200/_cluster/health?pretty

# Nodes and shards
curl -s http://<host>:9200/_cat/nodes?v
curl -s http://<host>:9200/_cat/shards?v

# Allocation explain
curl -s http://<host>:9200/_cluster/allocation/explain?pretty

# Thread pools
curl -s http://<host>:9200/_cat/thread_pool?v

# Node stats
curl -s http://<host>:9200/_nodes/stats?pretty
```

## Exchange Tradeoffs (ordering, idempotency, latency, throughput)
- Ordering: ES is eventually consistent; do not use for strict ordering.
- Idempotency: use deterministic document IDs for upserts.
- Latency: `refresh_interval` tunes search freshness vs write cost.
- Throughput: replicas improve reads but reduce indexing throughput.
- Consistency: async translog can lose recent writes on failure.
