# Incident Template - ES Write Jitter or Slow Queries

## Summary
- What happened:
- Start time (UTC):
- End time (UTC):
- Severity:

## Impact
- Affected indices:
- Indexing throughput drop:
- Search latency impact:

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
- Write rejections or high queue depth
- Search latency spikes
- Cluster health yellow/red

## Root Cause
- Primary cause (hot shard, GC, disk watermark, mapping explosion):

## Contributing Factors
- Shard count and routing strategy:
- Refresh interval:
- Query patterns or aggregations:

## Immediate Mitigation
- Throttle indexing
- Increase refresh interval
- Reroute or split hot shard

## Recovery Steps
- Clear queues and normalize latency
- Rebalance shards
- Validate cluster health

## Verification
- Indexing and search latency stable
- No write rejections
- Cluster green

## Communications
- Internal update time:
- External update time:
- Customer-facing summary:

## Metrics / Logs / Commands Checklist
Metrics:
- thread_pool.write/search queue and rejected
- JVM heap usage and GC time
- Disk usage and watermark status

Logs:
- Elasticsearch node logs

Commands:
```
curl -s http://<host>:9200/_cluster/health?pretty
curl -s http://<host>:9200/_cat/shards?v
curl -s http://<host>:9200/_cat/thread_pool?v
```

## Action Items
- Short term fixes:
- Long term fixes:
- Ownership and due dates:
