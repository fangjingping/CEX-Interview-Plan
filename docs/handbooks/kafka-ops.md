# Kafka Ops Handbook

## Deployment Topology and Components
- Brokers: 3-5 nodes per cluster, spread across AZs.
- Controller quorum: KRaft (preferred) or ZooKeeper (legacy).
- Producers and consumers: scale independently; use dedicated client pools.
- Optional: Schema Registry, Kafka Connect, MirrorMaker2, and monitoring stack.

Example topology (single region, multi-AZ):
```
Producers --> Brokers (AZ-A/B/C) --> Local disk
Consumers --> Brokers (AZ-A/B/C)
Controller quorum (KRaft) on brokers or dedicated nodes
```

## Key Parameters and Tuning (with rationale)
- `replication.factor=3`: tolerate one broker loss without data loss.
- `min.insync.replicas=2`: require quorum for writes; protects durability.
- `acks=all`: ensure writes are committed to ISR before success.
- `unclean.leader.election.enable=false`: avoid data loss on leader failover.
- `enable.idempotence=true`: avoid duplicate writes on retries.
- `num.partitions`: scale throughput; keep order per key per partition.
- `compression.type=zstd` or `snappy`: reduce network and disk IO.
- `batch.size` and `linger.ms`: larger batches improve throughput at cost of latency.
- `max.in.flight.requests.per.connection=1-5`: keep ordering on retries.
- `message.max.bytes` and `replica.fetch.max.bytes`: align for large messages.
- `log.retention.hours` or `log.retention.bytes`: control storage growth.
- `log.segment.bytes`: smaller segments speed up recovery and compaction.

## Common Failure Cases (at least 5)
- Rebalance storms: frequent group rebalances due to slow consumers or short timeouts.
- Under-replicated partitions: ISR shrinks from disk or network issues.
- Controller flaps: leadership changes cause cluster-wide instability.
- Disk full or slow disk: broker throttling, log dir offline.
- Producer timeouts: request handler saturation or broker overload.
- Consumer lag growth: insufficient partitions or slow processing.
- Corrupt log segment: broker fails to start or partition offline.

## Troubleshooting Runbook
Metrics:
- `UnderReplicatedPartitions`, `OfflinePartitions`, `ActiveControllerCount`.
- `RequestHandlerAvgIdlePercent`, `NetworkProcessorAvgIdlePercent`.
- `ReplicaFetcherThreadMaxLag`, `BytesInPerSec`, `BytesOutPerSec`.
- Consumer lag per group and partition.

Logs:
- broker `server.log` and controller logs.
- client logs (producer retries, consumer rebalances).

Commands:
```
# Topics and partitions
kafka-topics.sh --bootstrap-server <host:port> --describe --topic <topic>

# Consumer lag
kafka-consumer-groups.sh --bootstrap-server <host:port> --describe --group <group>

# Broker config
kafka-configs.sh --bootstrap-server <host:port> --describe --entity-type brokers --entity-name <id>

# Log directories
kafka-log-dirs.sh --bootstrap-server <host:port> --describe

# KRaft quorum (if used)
kafka-metadata-quorum.sh --bootstrap-server <host:port> describe
```

## Exchange Tradeoffs (ordering, idempotency, latency, throughput)
- Ordering: strict order only within a partition; use key per symbol.
- Idempotency: producer idempotence + consumer dedupe for at-least-once safety.
- Latency: small batches reduce latency but increase broker overhead.
- Throughput: more partitions increase parallelism but hurt ordering and rebalance cost.
- Durability: `acks=all` and `min.insync.replicas` increase safety but reduce peak TPS.
