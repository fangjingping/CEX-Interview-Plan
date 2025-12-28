# 演示：合约交易链路集成

串联撮合 → Outbox → 消息分发 → 账本 → 行情的最小闭环。

## 演示内容
- 撮合成交生成事件并写入 Outbox。
- Outbox 发布到分区日志，模拟消息总线。
- 幂等消费写账本并更新行情。

## 运行
```bash
mvn -pl demo-trade-pipeline test
```
