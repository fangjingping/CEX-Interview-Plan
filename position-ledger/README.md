# position-ledger

最小仓位生命周期演示：开仓、加仓、减仓、平仓，以及简化风控快照计算。

## 假设与公式（简化版）
- 单向持仓（一个方向净仓）。
- 保证金：`Margin = |Qty| * EntryPrice * IMR`。
- 未实现盈亏：
  - 多头：`UPnL = (Mark - Entry) * Qty`
  - 空头：`UPnL = (Entry - Mark) * Qty`
- 权益：`Equity = Margin + UPnL`
- 已用保证金：`Used = Notional * IMR`，`Notional = Mark * |Qty|`
- 可用保证金：`Available = Equity - Used`
- 强平价（解 Equitiy = MM）：
  - 多头：`Liq = (Entry*Qty - Margin) / (Qty * (1 - MMR))`
  - 空头：`Liq = (Entry*Qty + Margin) / (Qty * (1 + MMR))`
- 破产价（Equity = 0）：
  - 多头：`Bank = Entry - Margin/Qty`
  - 空头：`Bank = Entry + Margin/Qty`

## 精度与舍入
- 价格、数量、金额统一按 8 位小数计算，舍入模式 `HALF_UP`。
- 服务内会对输入进行归一化；若归一化后为 0，则视为非法输入。

## 并发模型
- `PositionService` 可并发调用：使用 `ConcurrentHashMap` + `compute` 保证单 `PositionKey` 原子更新。
- `Position`/`PositionState`/`MarginSnapshot` 均为不可变对象，不暴露可变内部状态。

## 运行测试
```bash
mvn -pl position-ledger test
```
