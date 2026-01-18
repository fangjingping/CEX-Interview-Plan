# BASELINE (Run 0)

> 注意：当前工作区已包含新增模块与代码变更；以下清单与测试结果基于当前状态采集（非全新干净基线）。如需重置基线，请明确指示。

## 1) 模块清单
- matching-engine
- demo-system-design
- demo-java-concurrency
- demo-middleware
- demo-risk
- position-ledger
- liquidation-engine
- pricing
- persistence
- wallet-custody
- account-ledger
- settlement-recon
- gateway-auth
- market-data
- demo-resilience
- demo-low-latency
- demo-jvm-tuning
- demo-profiling
- demo-trade-pipeline
- demo-resilience-boot

## 2) docs 目录结构
- docs/ARCHITECTURE.md
- docs/COMPETENCY-MATRIX.md
- docs/DERIVATIVES-FORMULAS.md
- docs/DERIVATIVES-INTERVIEW-OUTLINE.md
- docs/ENGINEERING-DRILLS.md
- docs/INTEGRATION-FLOWS.md
- docs/INTERVIEW-QUESTIONS.md
- docs/INTERVIEW-RUBRICS.md
- docs/PRODUCTION-READINESS.md
- docs/SCENARIO-PLAYBOOK.md
- docs/TASKS.md
- docs/handbooks/account-permission.md
- docs/handbooks/business-risk.md
- docs/handbooks/compliance-ops.md
- docs/handbooks/java-concurrency-jvm.md
- docs/handbooks/market-data.md
- docs/handbooks/middleware-data.md
- docs/handbooks/settlement-reconciliation.md
- docs/handbooks/spring-cloud-platform.md
- docs/handbooks/system-design-consistency.md
- docs/handbooks/wallet-custody.md

## 3) 基线测试
- 命令：`mvn test`
- 结果：失败（未全绿）
- 失败用例/模块：`demo-resilience-boot`（POM 解析阶段失败，未进入具体用例）
- 失败信息摘要：
  - `Non-resolvable import POM: org.springframework.cloud:spring-cloud-dependencies:2023.0.2`
  - `Failed to create parent directories for ... ~/.m2/... .pom.lastUpdated`
  - `dependencies.dependency.version missing for spring-cloud-starter-circuitbreaker-resilience4j`
- 最可能原因：
  - 沙盒限制导致无法写入 `~/.m2`，依赖缓存与下载失败。
  - 网络访问受限，无法从 Maven Central 拉取 BOM。
  - BOM 未解析导致依赖版本缺失。

## 4) 后续变更策略
- 模块命名/包名：模块名使用现有 kebab-case；包名统一 `com.cex.exchange.*`，demo 模块使用 `com.cex.exchange.demo.*`。
- 事件模型：每类事件包含 `eventId`、`aggregateKey`（如 symbol/userId）、`timestamp`、`version`（可选）；事件用于幂等去重与回放。
- 错误码/异常：每模块定义 `ErrorCode` 枚举 + 对应 `Exception`；不使用通用 `RuntimeException`。
- 幂等约定：请求级 `requestId`/`idempotencyKey`；事件级 `eventId`；账本 `entryId`；仓位 `tradeId`。
- Integration Test 放置：优先放在 `demo-trade-pipeline/src/test/java/...`，命名建议 `*IntegrationTest`；如规模扩大再新增 `demo-e2e` 模块。
