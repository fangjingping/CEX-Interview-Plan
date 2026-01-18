# Demo Walkthrough

This walkthrough runs the end-to-end perpetual flow integration test and explains the expected outcome.

## Run
```bash
mvn -pl demo-trade-pipeline -am test
```

## Expected
- Risk checks freeze margin for two makers and one taker; insufficient balance is rejected.
- Matching engine produces two trades for the same symbol.
- Idempotent consumption ignores duplicates; ledger entry count equals trade count.
- Ledger remains balanced (total debit equals total credit).
- Positions update (taker long 2, each maker short 1), then liquidation closes taker to flat.
- Market data book has two bid levels (101 then 100).
- Persistence log preserves trade order; recovery applies events after the snapshot.
