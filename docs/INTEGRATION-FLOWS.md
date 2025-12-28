# Java 与中间件交互流程（合约链路）

本文聚焦合约链路内 Java 服务与中间件的真实交互路径，强调顺序性、幂等与一致性边界。

## 端到端链路概览
1. Gateway 接入：鉴权（HMAC + nonce）、限流与幂等键校验。
2. 预风控：余额/保证金校验、冻结额度、委托参数合法性。
3. Sequencer：按交易对分片串行化，保证确定性顺序。
4. 撮合引擎：内存订单簿撮合，产出成交/订单事件。
5. Outbox：撮合与持久化写入同事务域，防止消息丢失。
6. 消息总线：Kafka/Pulsar 以 `symbol` 为分区键分发。
7. 账本/仓位：消费成交事件，写账本与仓位，落库并再次 Outbox。
8. 行情分发：订阅成交/盘口事件，扇出到 WebSocket/推送。

## 关键交互点与一致性
- 幂等边界：API 层 `idempotencyKey`；撮合事件 `eventId`；账本 `entryId`；出入金 `txId`。
- 顺序保证：同交易对事件落在同分区；撮合与账本在同分区消费。
- 失败重试：下游消费采用 at-least-once + 幂等去重；重放依赖事件日志/快照。
- 数据查询：核心账本以关系型数据库为准；ES 仅用于检索与分析。

## 中间件落点（Java 服务）
- Kafka：Spring Kafka 生产/消费；使用分区键保证顺序；幂等生产 + 重试策略。
- Redis：读缓存与热点防护；读穿与 TTL 治理；避免强依赖锁语义。
- 数据库：账本追加写与强一致；必要时使用乐观锁与批量写。
- ES：行情与成交检索；控制 refresh/merge 影响写入延迟。

## 代码演示对照
- 撮合与订单簿：`matching-engine`
- 幂等与 Outbox：`demo-middleware`
- 回放与快照：`demo-system-design`
- 风控与强平：`demo-risk`
- 账本与清结算：`account-ledger`, `settlement-recon`
- 鉴权与治理：`gateway-auth`, `demo-resilience`
