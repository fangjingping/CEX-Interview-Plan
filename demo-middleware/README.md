# 演示：中间件与数据层

演示 Kafka 分区顺序、Outbox/CDC、Redis 缓存 TTL、ES 刷新语义与消息幂等的基础实现思路。

## 演示内容
- 消息幂等处理与去重。
- 分区顺序与偏移读取。
- 缓存 TTL、读取穿透保护与简化搜索索引。
- Outbox 发布与刷新语义（ES）。

## 运行
```bash
mvn -pl demo-middleware test
```
