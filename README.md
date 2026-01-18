# CEX 交易所面试准备项目（Java）

这个仓库提供一个可运行的、可扩展的撮合引擎原型，用来帮助 Java 资深开发进行 CEX 交易所面试准备。重点覆盖撮合、订单簿、时间优先、价格优先、市场单与限价单处理等核心题。

## 目标

- 用清晰的最小实现覆盖撮合核心流程
- 提供可讨论的架构切面：一致性、吞吐、延迟、容错、扩展性
- 通过任务清单引导你逐步完善：风控、资金、撮合队列、撮合撮合日志、撮合快照

## 结构

- `matching-engine`: 撮合引擎模块
- `demo-system-design`: 一致性与回放演示
- `demo-java-concurrency`: Java 并发与背压演示
- `demo-middleware`: 幂等与中间件演示
- `demo-risk`: 合约风险与强平演示
- `position-ledger`: 仓位与保证金台账演示
- `liquidation-engine`: 强平队列与订单生成演示
- `pricing`: 指数价/标记价/资金费率演示
- `persistence`: 事件日志与快照持久化演示
- `wallet-custody`: 钱包与托管演示
- `account-ledger`: 双录账本演示
- `settlement-recon`: 清结算与对账演示
- `gateway-auth`: 网关鉴权演示
- `market-data`: 行情与数据分发演示
- `demo-resilience`: Spring Cloud 治理演示
- `demo-low-latency`: 低延迟环形队列演示
- `demo-jvm-tuning`: GC 日志分析演示
- `demo-profiling`: 性能剖析演示
- `demo-trade-pipeline`: 合约交易链路集成演示
- `demo-resilience-boot`: Boot 3 + Spring Cloud 治理示例
- `docs/ARCHITECTURE.md`: 可用于面试系统设计的结构化说明
- `docs/DERIVATIVES-INTERVIEW-OUTLINE.md`: 合约方向 CTO 级面试大纲与题库框架
- `docs/handbooks/`: 四大方向与全业务扩展手册
- `docs/PRODUCTION-READINESS.md`: 生产级落地清单
- `docs/COMPETENCY-MATRIX.md`: 能力矩阵与红线
- `docs/DERIVATIVES-FORMULAS.md`: 合约公式速查
- `docs/SCENARIO-PLAYBOOK.md`: 场景演练手册
- `docs/ENGINEERING-DRILLS.md`: 工程实操演练
- `docs/INTERVIEW-RUBRICS.md`: 面试评分标准
- `docs/INTEGRATION-FLOWS.md`: Java 与中间件交互链路说明
- `docs/DEMO-WALKTHROUGH.md`: 端到端演示运行说明
- `docs/INDEX.md`: 文档索引目录
- `docs/CONTRIBUTING.md`: Contribution guide
- `docs/CODE_OF_CONDUCT.md`: Code of conduct
- `docs/SECURITY.md`: Security policy
- `docs/THIRD_PARTY_NOTICES.md`: Third-party notices
- `docs/THIRD_PARTY_LICENSES.md`: Third-party license report
- `docs/handbooks/kafka-ops.md`: Kafka 运维手册
- `docs/handbooks/redis-ops.md`: Redis 运维手册
- `docs/handbooks/es-ops.md`: Elasticsearch 运维手册
- `docs/handbooks/mysql-ops.md`: MySQL 运维手册
- `docs/scenarios/`: 故障复盘模板
- `docs/TASKS.md`: 练习任务清单
- `docs/INTERVIEW-QUESTIONS.md`: 面试问答方向
- `AGENTS.md`: 仓库贡献指南与协作规则

## 快速开始

```bash
mvn test
```

## 建议练习路线

1. 先跑测试，理解撮合行为与边界条件
2. 在 `docs/TASKS.md` 中挑 3-5 个任务实现
3. 结合 `docs/ARCHITECTURE.md` 做系统设计演练
4. 用 `docs/INTERVIEW-QUESTIONS.md` 自测并补齐薄弱点

## 本次补充与回顾

- 新增四大方向独立手册，并链接到合约面试大纲。
- 引入多模块 Maven 结构，支持各方向演示模块独立演示与测试。
- 扩展 CTO 题库与评估要点，覆盖系统设计、Java 并发/JVM、中间件、业务风控。
- 增加仓库贡献指南，明确结构、构建测试与命名约定。
- 补充代码注释并将英文描述统一为中文表述（保留技术术语）。
- 扩展全业务手册与题库，并新增钱包、账本、清结算、网关与行情演示模块。
- 更新任务清单与面试问题，补齐钱包、账户、清结算、行情与合规方向。
- 新增生产级落地清单，明确一致性、容灾、性能与安全要求。
- 扩展 Java/JVM 与中间件覆盖，新增 Spring Cloud 治理演示模块。
- 补充 Kafka/Redis/ES 的技术点与演示，增强中间件实战覆盖。
- 补齐合约链路内的 Java/JVM、治理与合规细节，并按 Boot 3 体系调整相关文档。
- 新增能力矩阵、公式速查、场景演练、工程实操与评分标准。
- 增强中间件治理实现细节（Outbox/CDC、TTL、ES 刷新语义）。
- 增加预提交敏感信息扫描钩子与安装脚本。
- 补充 Java 与中间件交互链路说明，并修正幂等、并发与时钟依赖实现。
- 新增低延迟/JVM 调优/性能剖析演示模块，补齐 tail latency 与 safepoint/Code Cache 内容。
- 新增合约链路集成演示与 Boot 3 治理示例，补齐端到端闭环与可运行样例。
- 增强幂等去重与熔断半开治理，补齐过期清理与空闲策略演示。

## 行为记录（Agent Log）

- 2025-12-27 | Codex | 新增 `docs/DERIVATIVES-INTERVIEW-OUTLINE.md`，加入 CTO 面试大纲与答题框架。
- 2025-12-27 | Codex | 新增 `AGENTS.md`，明确协作规则与仓库约定。
- 2025-12-27 | Codex | 扩展 CTO 题库，增加系统设计、Java/JVM、中间件、风控四条深挖路线。
- 2025-12-27 | Codex | 调整为多模块 Maven 结构，新增四个 demo 模块。
- 2025-12-27 | Codex | 新增 `docs/handbooks/` 四大方向独立手册并在大纲中索引。
- 2025-12-27 | Codex | 补充代码注释并将文档说明统一为中文表述。
- 2025-12-27 | Codex | 扩展全业务手册与题库，新增钱包、账本、清结算、网关与行情演示模块。
- 2025-12-27 | Codex | 更新任务与面试问题清单，补齐钱包、账户、清结算、行情与合规方向。
- 2025-12-27 | Codex | 新增 `docs/PRODUCTION-READINESS.md`，整理生产级落地清单。
- 2025-12-27 | Codex | 扩展 Java/JVM 与中间件覆盖，新增 `demo-resilience` 治理演示模块与相关题库。
- 2025-12-27 | Codex | 增加 Kafka/Redis/ES 演示代码与中间件扩展手册内容。
- 2025-12-27 | Codex | 补齐合约链路内 Java/JVM、治理与合规细节并按 Boot 3 体系更新文档。
- 2025-12-27 | Codex | 新增能力矩阵、公式速查、场景演练、工程实操与评分标准文档。
- 2025-12-27 | Codex | 增强中间件治理实现细节，补充 Outbox/CDC、TTL 与 ES 刷新语义演示。
- 2025-12-27 | Codex | 新增预提交敏感信息扫描钩子与安装脚本。
- 2025-12-27 | Codex | 修正撮合并发控制、账本线程安全与幂等窗口，并补充 Java 与中间件交互链路说明。
- 2025-12-27 | Codex | 新增低延迟/JVM 调优/性能剖析演示模块，并补齐尾延迟诊断内容。
- 2025-12-27 | Codex | 新增合约链路集成演示与 Boot 3 治理示例，并补齐链路闭环与治理说明。
- 2025-12-27 | Codex | 增强幂等去重过期清理、熔断半开控制与低延迟空闲策略演示。
