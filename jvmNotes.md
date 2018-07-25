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


  



















