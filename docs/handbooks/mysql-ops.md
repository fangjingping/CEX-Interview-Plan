# MySQL Ops Handbook

## Deployment Topology and Components
- Primary-replica: one primary for writes, replicas for reads.
- Semi-sync (optional): reduce data loss on failover.
- Proxy layer (optional): ProxySQL or HAProxy for routing.

Example topology:
```
App --> Proxy --> Primary
                 |-> Replica 1
                 |-> Replica 2
```

## Key Parameters and Tuning (with rationale)
- `innodb_buffer_pool_size`: main cache; 60-75 percent of RAM.
- `innodb_log_file_size`: larger reduces checkpoint pressure.
- `innodb_flush_log_at_trx_commit`: 1 for durability, 2 for throughput.
- `sync_binlog`: 1 for durability, 0 for throughput.
- `innodb_io_capacity`: align with disk IO ability.
- `innodb_flush_method=O_DIRECT`: reduce double buffering.
- `transaction_isolation=READ-COMMITTED|REPEATABLE-READ`: tradeoff between consistency and locks.
- `innodb_lock_wait_timeout`: avoid long stalls.
- `max_connections`: protect CPU and memory.
- `binlog_format=ROW`: safer replication and auditing.

## Common Failure Cases (at least 5)
- Deadlocks under concurrent updates.
- Hot row or hot index causing lock waits.
- Replication lag or broken replication.
- Disk full or slow IO causes stalls.
- Long transactions block purge and increase undo.
- Slow queries due to missing indexes.
- Metadata lock blocks DDL or DML.

## Troubleshooting Runbook
Metrics:
- `Threads_running`, `Threads_connected`.
- `Innodb_row_lock_time`, `Innodb_row_lock_waits`.
- Buffer pool hit ratio, redo log usage.
- Replica lag (Seconds_Behind_Master or replica status).

Logs:
- Error log, slow query log, and binlog.

Commands:
```
SHOW ENGINE INNODB STATUS\G
SHOW FULL PROCESSLIST;
SHOW STATUS LIKE 'Threads_running';
SHOW STATUS LIKE 'Innodb_row_lock%';
SHOW VARIABLES LIKE 'innodb%';
EXPLAIN <query>;

# Replication
SHOW REPLICA STATUS\G
```

## Exchange Tradeoffs (ordering, idempotency, latency, throughput)
- Ordering: primary is authoritative; replicas are async and can be stale.
- Idempotency: required for retryable writes to avoid duplicate rows.
- Latency: strict durability (`innodb_flush_log_at_trx_commit=1`) increases latency.
- Throughput: read replicas scale reads but add replication lag risk.
- Consistency: higher isolation reduces anomalies but increases lock contention.
