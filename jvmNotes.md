## jvm知识点总结

### 1.不推荐使用`finalize()`函数中释放资源
* 因为`finalize()`函数有可能发生引用外泄，在无意中复活对象。
* `finalize()`是被系统调用的，调用时间是不明确的，因此不是一个好的资源释放方案，推荐使用`try-catch-finally`语句中进行资源的释放。

### 2、强引用的特点：
* 强引用可以直接访问目标对象
* 强引用所指向的对象在任何时候都不会被系统回收，虚拟机宁愿抛出OOM异常，也不会回收强引用所指向的对象
* 强引用可能导致内存泄漏

### 3、年轻代串行回收器特点：
* 使用复制算法（Copying）
* 使用单线程进行垃圾回收
* 它是独占式垃圾回收（Stop-The-World）
* 没有线程切换的开销，久经考验，处理高效
* 适合单CPU选用
* 虚拟机Client模式下默认的垃圾回收期
* 参数配置（-XX:+UseSerialGC）

### 4、老年代串行回收器

* 使用标记压缩算法（Mark-Compact）
* 启动老年代串行回收器参数：
    * -XX:+UseSerialGC:新生代、老年代都使用串行回收器
    * -XX:+UseParNewGC:新生代使用ParNew回收器，老年代使用串行回收器
    * -XX:+UseParallelGC:新生代使用ParallelGC回收器，老年代使用串行回收器

### 5、新生代ParNew回收器
* 串行回收器的多线程版本
* 使用复制算法
* 启用ParNew回收器使用参数：
    * `-XX:+UseParNewGC`:新生代使用ParNew回收器，老年代使用串行回收器
    * `-XX:+UseConcMarkSweepGC`:新生代使用ParNew回收器,老年代使用CMS
* 使用`-XX:ParallelGCThreads`参数指定回收器工作线程数量，一般最好和CPU数量相当，避免过多的线程数，影响垃圾收集性能。在默认情况下，当CPU数量小于8个时，`ParallelGCThreads`的值等于CPU数量。当CPU数量大于8个时，`ParallelGCThreads`的值等于`3+((5*CPU_Count)/8)`.

### 6、新生代ParallelGC回收器
* 复制算法收集器
* 注重系统吞吐量
* 启用ParallelGC回收器:
    * `-XX:+UseParallelGC`:新生代使用`ParallelGC`回收器，老年代使用串行回收器
    * `-XX:+UseParallelOldGC`:新生代使用`ParallelGC`回收器，老年代使用`ParallelOldGC`回收器

* ParallelGC回收器提供了两个重要的参数用于控制系统的吞吐量：
    * -XX:MaxGCPauseMillis:设置最大垃圾收集停顿时间。大于0的整数
    * -XX:GCTimeRatio:设置吞吐量大小，值为1到100之间的整数。
    * 支持自适应GC调节策略，使用`-XX+UseAdaptiveSizePolicy`
    * 参数`-XX:MaxGCPauseMillis`和`-XX:GCTimeRatio`是相互矛盾的，通常如果减少一次收集的最大停顿时间，就会同时减小系统吞吐量，增加系统吞吐量又可能会同时增加一次垃圾回收的最大停顿。
    
### 7、老年代ParallelOldGC回收器
* 采用标记压缩算法
* 老年代ParallelOldGC回收器是一个多线程并发收集器
* 关注系统吞吐量，并且和ParallelGC新生代回收器搭配使用
* 参数`-XX:ParallelGCThreads`用来设置垃圾回收时的线程数量

### 8、CMS回收器
* 采用标记清除算法，同时也是一个使用多线程并行回收的垃圾回收器。
* CMS主要关注系统停顿时间
* 步骤多包括：初始标记（独占）、并发标记（可以和用户线程一起执行）、预清理（可以和用户线程一起执行）、重新标记（独占）、并发清理（可以和用户线程一起执行）、并发重置（可以和用户线程一起执行）。
* 主要参数：
    * 启用CMS收集器`-XX:+UseConcMarkSweepGC`
    * 设置并发线程数量`-XX:ConcGCThreads`或`-XX:ParallelCMSThreads`参数手工指定。
    * 回收阈值`-XX:CMSInitiatingOccupancyFraction`来指定，默认是68。即当老年代的空间使用率达到68%时，会执行一次CMS回收，如果应用程序的内存使用率增长很快，在CMS的执行过程中，已经出现了内存不足的情况，此时CMS回收失败，虚拟机将启动老年代串行收集器进行垃圾回收。
    * 内存压缩整理`-XX:+UseCMSCompactAtFullCollection`，使CMS在垃圾收集完成后，进行一次内存碎片整理，内存碎片整理不是并发的。`-XX:CMSFullGCsBeforeCompaction`参数可以用于设定多少次CMS回收后，进行一次内存压缩。
    * 如果需要回收Perm区，配置`-XX:+CMSClassUnloadingEnabled`,如果条件允许，那么系统会使用CMS的机制回收Perm区的Class数据。

### 9、G1（GarBage-First）回收器

* ***并行性***：G1在回收期间，可以由多个GC线程同时工作，有效利用多核计算力。
* ***并发性***：G1拥有与应用程序交替执行的能力，部分工作可以和应用程序同时执行，因此一般来说，不会在整个回收期间完全阻塞应用程序
* ***分代GC***：G1依然是一个分代收集器，但是和之前回收器不同，它同时兼顾年轻代和老年代。对比其他回收器，它们或者工作在年轻代，或者工作在老年代。
* ***空间整理***：G1在回收过程中，会进行适当的对象移动。不像CMS，只是简单地标记清理对象，在若干次GC后，CMS必须进行一次碎片整理。而G1不同，它每次回收都会有效地复制对象，减少空间碎片。
* ***可预见性***：由于分区的原因，G1可以只选取部分区域进行内存回收，这样缩小了回收的范围，因此对于全局停顿也能得到较好的控制。



G1的收集过程有4个阶段：

* 新生代GC

  * 新生代GC的主要工作是回收`eden/survivor`一旦`eden`区被占满，新生代GC就会启动。新生代GC只处理`eden`和`survivor`区，回收后，所有`eden`区都应该被清空，而`survivor`区会被收集一部分数据，但是应该至少仍然存在一个`survivor`区，类比其它的新生代回收器，这点似乎并没有太大变化，另一个重要的变化是老年代的区域增多，因为部分`survivor`或者`eden`区的对象可能会晋升到老年代。

* 并发标记周期

* 混合收集

* 如果需要，可能会进行Full GC

* 重要参数：

  * `-XX:+UseG1GC`，启用G1回收器
  * `-XX:MaxGCPauseMillis`指定目标最大停顿时间
  * `-XX:ParallelGCThreads`设置并行回收时，GC的工作线程数量
  * `-XX:InitiatingHeapOccupancyPercent`，参数可以指定当整个堆使用率达到多少时，触发并行标记周期的执行。默认值为45，即当整个堆占用率达到45%时，执行并发标记周期。`InitiatingHeapOccupancyPercent`一旦设置，始终都不会被G1收集器修改，这意味着G1收集器不会试图改变这个值，来满足`MaxGCPauseMillis`的目标。如果`InitiatingHeapOccupancyPercent`值设置过大，会导致并发周期迟迟得不到启动，那么引起Full GC的可能性也大大增加，反之，一个过小的`InitiatingHeapOccupancyPercent`值，会使得并发周期非常频繁，大量GC线程抢占CPU，会导致应用程序的性能有所下降。

  




















