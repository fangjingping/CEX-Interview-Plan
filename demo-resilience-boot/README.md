# 演示：Spring Boot 3 + Spring Cloud 治理

提供可运行的 Boot 3 示例，展示熔断与限流配置的落地方式。

## 运行
```bash
mvn -pl demo-resilience-boot spring-boot:run
```

## 端点
- `GET /api/price`: 限流演示
- `GET /api/risk?fail=true`: 熔断演示
- `GET /actuator/health`: 健康检查
