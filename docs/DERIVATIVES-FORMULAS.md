# 合约关键公式速查（简化版）

## 说明
以下为常见公式的简化表达，实际交易所会结合费率、资金费率、风险限额阶梯与模式（逐仓/全仓）做调整。

## 基本定义
- 名义价值：`Notional = |Q| * MarkPrice`
- 未实现盈亏（多头）：`UPnL = (MarkPrice - EntryPrice) * Q`
- 未实现盈亏（空头）：`UPnL = (EntryPrice - MarkPrice) * |Q|`
- 账户权益：`Equity = WalletBalance + UPnL - FeesAccrued`
- 维持保证金：`MM = Notional * MMR + Buffer`
- 保证金率：`MR = Equity / Notional`

## 强平条件（概念）
当 `Equity <= MM` 时触发强平，具体强平价通过“Equity = MM”求解 MarkPrice。

示意（多头 Q > 0）：
```
Wallet + (Mark - Entry) * Q - Fees = Mark * Q * MMR + Buffer
Mark = (Entry * Q - Wallet + Fees + Buffer) / (Q * (1 - MMR))
```
空头同理，使用 |Q| 并注意方向符号。

## 资金费率（概念）
- 资金费支付：`FundingPayment = Notional * FundingRate * SideSign`
- SideSign：多头在 FundingRate > 0 时支付，空头在 FundingRate < 0 时支付。
- 结算周期影响权益与保证金率，需纳入强平判定。

## 风险限额阶梯
- MMR 与 Buffer 通常随名义价值分段递增。
- 实际计算中需先确定所在阶梯，再计算 MM 与强平条件。
