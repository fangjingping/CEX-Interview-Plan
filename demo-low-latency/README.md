# 演示：低延迟环形队列

演示 SPSC 环形队列与忙等消费，强调预分配与背压策略。

## 演示内容
- 预分配对象槽位，避免热路径分配。
- SPSC 顺序保证与容量背压。
- 忙等消费的尾延迟影响。
- 空闲策略（Spin/Backoff）对 CPU 与尾延迟的权衡。

## 运行
```bash
mvn -pl demo-low-latency test
```
