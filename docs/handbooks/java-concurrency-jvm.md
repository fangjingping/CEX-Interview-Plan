# Java 并发与 JVM 性能手册（CTO 面试向）

## 目标
体现对 Java 并发语义、线程模型与性能排障的掌控，能给出可落地的优化路径与证据链。

## 考核原则（CTO 视角）
- 能否把“稳定性与低延迟”放在首要位置，而不是只追求吞吐。
- 能否给出可观测证据链（指标/日志/堆栈/JFR）支撑决策。
- 能否在资源约束下做出工程化取舍（CPU/内存/GC/IO）。

## 应用场景（CEX）
- 撮合引擎的单线程确定性 + 多路接入并发。
- 行情扇出与慢消费者治理。
- 风控、清结算的批处理与低延迟混合负载。
- 大促或极端行情下的容量与延迟抖动控制。

## 核心能力清单
- Java 核心：集合/泛型、异常与序列化、I/O 与 NIO。
- JMM：happens-before、volatile、锁与 CAS 的语义差异。
- 并发组件：线程池、阻塞队列、并发容器与背压策略。
- 并发原语：ReentrantLock/ReadWriteLock/StampedLock、Condition、ThreadLocal。
- 异步模型：CompletableFuture/ForkJoin 的适用场景与代价。
- 性能定位：GC 日志、线程栈、火焰图、JFR/async-profiler。
- 内存与对象：逃逸分析、分配热点、false sharing。
- JVM 调优：G1/ZGC、TLAB、Code Cache、类加载与元空间。
- 低延迟策略：对象复用、批处理、NUMA 亲和与线程绑核。
- 诊断闭环：safepoint 日志、JIT 退化、内存碎片与尾延迟治理。

## 低延迟深水区
- JIT 与 Code Cache：编译层级、去优化、Code Cache 满导致的抖动。
- safepoint：触发点与停顿来源，如何基于日志定位。
- GC 选型：ZGC/Shenandoah vs G1 的场景取舍与暂停预算。
- 内存布局：对象头、对齐、伪共享与 cache line 影响。
- 堆外与 DirectBuffer：使用边界、内存回收与泄漏定位。

## 典型面试题与追问
必问
- 线程池如何配置？如何避免任务堆积？
- G1/ZGC 的适用场景与调优思路？
追问
- 如何定位 P99 延迟抖动？
- OOM 的常见根因与修复步骤？
- ThreadLocal 泄漏与类加载器泄漏如何排查？
- safepoint 停顿的常见触发点有哪些？
- Code Cache 满导致编译退化时如何处理？

## 回答结构建议
先识别瓶颈类型（CPU/内存/锁/IO）→ 给观测证据 → 复现实验或压测 → 优化 → 结果验证。

## 常见陷阱
- 只会背参数，不知道如何验证效果。
- 对“锁竞争/排队延迟”的理解停留在概念层。

## 演示关联
- 代码模块：`demo-java-concurrency`, `demo-low-latency`, `demo-jvm-tuning`, `demo-profiling`
- 建议演示：单线程顺序处理、背压与拒绝策略、FIFO 保序、GC 日志分析、性能剖析。
