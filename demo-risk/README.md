# 演示：业务风控与强平

演示保证金比例评估、预冻结与强平判断。

## 演示内容
- 基于标记价的保证金比例计算。
- 风险状态分类（OK / MARGIN_CALL / LIQUIDATE）。
- 风控预冻结、成交消耗与释放。

## 运行
```bash
mvn -pl demo-risk test
```
