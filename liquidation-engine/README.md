# liquidation-engine

Minimal liquidation flow demo: task enqueue -> single-thread worker -> position close.

## Design highlights
- Priority: higher `priority` runs first; ties break by earlier timestamp.
- Backlog protection: bounded queue (default 1024). Enqueue drops when full and increments `droppedCount`.
- Idempotency: `userId|symbol` is unique across queued/in-flight tasks; replays after close return `NO_POSITION`.
- Execution: `LiquidationService` closes the position using `PositionService.closePosition` and emits a liquidation trade.
- Threading: scheduler is thread-safe, worker is intended to run on a single thread.

## Run tests
```bash
mvn -pl liquidation-engine test
```
