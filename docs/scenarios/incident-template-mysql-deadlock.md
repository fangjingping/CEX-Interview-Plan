# Incident Template - MySQL Deadlock or Hot Row

## Summary
- What happened:
- Start time (UTC):
- End time (UTC):
- Severity:

## Impact
- Affected tables:
- Error rate / retry rate:
- Latency impact:

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
- Deadlock errors or lock wait timeouts
- Slow queries
- Threads_running spike

## Root Cause
- Primary cause (query pattern, missing index, hot row):

## Contributing Factors
- Transaction size and duration:
- Isolation level:
- Index coverage:

## Immediate Mitigation
- Kill long transactions
- Add index or change query order
- Reduce concurrency or batch size

## Recovery Steps
- Drain backlog
- Validate application retries
- Confirm replication health

## Verification
- Deadlocks return to baseline
- Lock waits normal
- Query latency normal

## Communications
- Internal update time:
- External update time:
- Customer-facing summary:

## Metrics / Logs / Commands Checklist
Metrics:
- Innodb_row_lock_waits / Innodb_row_lock_time
- Threads_running
- Replication lag

Logs:
- Error log
- Slow query log

Commands:
```
SHOW ENGINE INNODB STATUS\G
SHOW FULL PROCESSLIST;
SHOW STATUS LIKE 'Innodb_row_lock%';
```

## Action Items
- Short term fixes:
- Long term fixes:
- Ownership and due dates:
