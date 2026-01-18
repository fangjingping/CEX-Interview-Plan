# Incident Template - Kafka Rebalance Thrash

## Summary
- What happened:
- Start time (UTC):
- End time (UTC):
- Severity:

## Impact
- Affected topics/groups:
- Consumer lag peak:
- User impact:
- Revenue or SLA impact:

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
- Frequent group rebalances
- High lag growth
- Consumer errors or timeouts

## Root Cause
- Primary cause:
- Why it caused rebalances:

## Contributing Factors
- Config values (max.poll.interval.ms, session.timeout.ms, heartbeat.interval.ms):
- Consumer processing time:
- Partition count vs consumer count:

## Immediate Mitigation
- Actions taken:
- Rollback or config change:
- Temporary scaling or throttling:

## Recovery Steps
- Stabilize group membership
- Drain lag
- Validate offsets and ordering

## Verification
- Lag back to baseline
- Rebalance rate normal
- Error rate normal

## Communications
- Internal update time:
- External update time:
- Customer-facing summary:

## Metrics / Logs / Commands Checklist
Metrics:
- Consumer lag per partition
- Rebalance rate
- RequestHandlerAvgIdlePercent
- UnderReplicatedPartitions

Logs:
- Consumer logs (rebalance, commit, poll)
- Broker logs for errors

Commands:
```
kafka-consumer-groups.sh --bootstrap-server <host:port> --describe --group <group>
kafka-topics.sh --bootstrap-server <host:port> --describe --topic <topic>
```

## Action Items
- Short term fixes:
- Long term fixes:
- Ownership and due dates:
