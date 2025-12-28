# 仓库指南

## 项目结构与模块组织
- `matching-engine/src/main/java/com/cex/exchange/model`: 领域对象（订单、成交、枚举）。
- `matching-engine/src/main/java/com/cex/exchange/book`: 订单簿与价格档位结构。
- `matching-engine/src/main/java/com/cex/exchange/engine`: 撮合逻辑与核心流程。
- `matching-engine/src/main/java/com/cex/exchange/service`: 简单入口服务层。
- `matching-engine/src/test/java/com/cex/exchange/engine`: 撮合行为的 JUnit 测试。
- `demo-system-design`: 事件日志 + 快照恢复演示。
- `demo-java-concurrency`: 有界队列 + 背压演示。
- `demo-middleware`: 幂等消费演示。
- `demo-risk`: 保证金比例 + 强平演示。
- `wallet-custody`: 钱包与托管演示。
- `account-ledger`: 双录账本演示。
- `settlement-recon`: 清结算与对账演示。
- `gateway-auth`: 网关鉴权演示。
- `market-data`: 行情与数据分发演示。
- `demo-resilience`: Spring Cloud 治理演示。
- `docs/`: 面试材料与手册（`handbooks/`）。

## 构建、测试与开发命令
- `mvn test`: 从根目录运行全模块测试。
- `mvn -pl matching-engine test`: 仅运行撮合模块测试。
- `mvn -pl demo-risk test`: 运行指定演示模块测试。
- `mvn -pl wallet-custody test`: 运行钱包与托管演示测试。
- `mvn -pl demo-resilience test`: 运行 Spring Cloud 治理演示测试。
- `scripts/install-hooks.sh`: 安装本仓库预提交钩子（敏感信息扫描）。
- 新增文档与任务请放在 `docs/` 下，并在 `README.md` 中补充索引。

## 编码风格与命名约定
- Java 17，4 空格缩进，同一行大括号风格。
- 包名遵循 `com.cex.exchange.*`；demo 使用 `com.cex.exchange.demo.*`。
- 价格与数量使用 `BigDecimal`，避免浮点误差。
- 类名使用名词（`OrderBook`, `MatchingEngine`），测试类以 `Test` 结尾。

## 测试规范
- 测试框架：JUnit Jupiter (JUnit 5)，通过 Maven Surefire 执行。
- 测试放在 `src/test/java`，包路径与主代码保持一致。
- 测试方法名描述行为（如 `matchesLimitOrderPartially`），断言关键状态。
- 未设置覆盖率门槛，优先覆盖撮合边界与 FIFO 规则。

## Commit 与 PR 规范
- 当前工作区无 Git 历史，未发现既有提交规范。
- 建议提交风格：动词开头、可加范围（如 `engine: handle market orders`）。
- PR 需包含：简要说明、测试结果（`mvn test`）、行为变更的文档更新。

## 架构与面试备注
- `docs/ARCHITECTURE.md` 汇总系统设计要点，核心流程变更时需同步更新。
