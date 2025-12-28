# 演示：JVM 调优与 GC 诊断

演示 GC 日志解析与暂停统计，帮助定位尾延迟来源。

## 演示内容
- 解析 GC 日志中的 pause 事件。
- 输出 P95/P99 与最大停顿。
- 用于复盘线上抖动与调优效果。

## 运行
```bash
mvn -pl demo-jvm-tuning test
```
