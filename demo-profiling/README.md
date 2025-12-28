# 演示：性能剖析与热点定位

演示 CPU 热点、锁竞争与分配热点，用于 async-profiler/JFR 诊断演练。

## 演示内容
- CPU 密集路径与热点方法。
- 锁竞争导致的尾延迟。
- 分配热点与内存抖动。

## 运行
```bash
mvn -pl demo-profiling test
```
