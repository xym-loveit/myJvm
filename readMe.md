## java堆为对象分配空间的方式：

### 1、指针碰撞（serial、parNew带有Compact过程收集器）
使用过的内存和空闲的内存划分界限（通过指针划分）

### 2、空闲列表（CMS基于Mark-Sweep算法收集器）
使用过的内存和空闲内存相互交错


## 解决对象分配内存并发问题

### 1、分配时进行同步处理（CAS失败重试）

### 2、分配时按照不同的线程划分在不同空间。

即每个线程在java堆中预先分配一小块内存，称为本地线程分配缓冲（Thread Local Allocation Buffer，TLAB），只有TLAB用完并分配新的TLAB时，才需要同步锁定。虚拟机是否使用TLAB，可以采用-XX:+/-UseTLAB参数来设定。

## 对象在HotSpot虚拟机中的存储布局可以分为3块区域：对象头（Header）、实例数据（instance data）对齐填充（padding）

对象头：存储自身运行时数据，如哈希码（hashcode）、GC分代年龄、锁状态标志、线程持有的锁、偏向线程ID、偏向时间戳等；另一部分为类型指针，即为对象指向它的类元数据的指针。

实例数据：真正存储的实例数据。

对齐填充：由于HotSpot vm的自动内存管理系统要求对象起始地址必须是8字节的整数倍，换句话说，就是对象的大小必须是8字节的整数倍。而对象头部分正好是8字节的倍数，因此对象实例数据部分没有对齐时，就需要对齐填充来补全。

## 虚拟机访问对象的方式：

### 1、使用句柄 

### 2、直接指针（Hotspot虚拟机默认模式）

## 堆内存异常实例: 
```
VM Args: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError

```

分析方法：
1、内存溢出：是否有必要增大堆内存参数（-Xms/-Xmx）
2、内存泄漏（通过工具分析对象到GC Roots的引用链）

常用分析工具：Memory Analyzer (MAT)（http://www.eclipse.org/mat/）

## 栈内存异常实例：

方法递归：
```
VM Args: -Xss128k

```

创建线程：
```
VM Args: -Xss2m

```

## 运行时常量池异常

```
VM Args: -XX:PermSize=10m -XX:MaxPermSize=10m

```

## 方法区异常

对应`JavaMethodAreaOOM`示例
```
VM Args: -XX:PermSize=10m -XX:MaxPermSize=10m

```

## 直接内存溢出异常

```
VM Args: -Xmx20m -XX:MaxDirectMemorySize=10m

```

## 垃圾回收算法：
即通过什么断定对象是垃圾？

### 1、引用计数算法（jvm未采用该算法）
给对象添加一个计数器，每当有一个地方引用它时，计数器值就加1；当引用失效时，计数器值就减1；任何时刻计数器为0的对象就是不可能再被使用的，即视该对象为垃圾。

### 2、可达性分析算法
通过一系列的称为“GC Roots”的对象作为起始点，从这些节点开始向下搜索，搜索所走过的路径被称为应用链，当一个对象到GC Roots没有任何引用链相连时，则证明此对象是不可用的。

### 3、哪些可以作为`GC Roots`对象呢？
- 虚拟机栈（栈帧中的本地变量表）中引用的对象
- 方法区中常量引用的对象
- 方法区中类静态属性引用的对象
- 本地方法栈中JNI（即一般说的Native方法）引用的对象

### java引用类型

- 强引用（`Strong Reference`）

强引用就是指在程序代码中普遍存在的，类似“Object obj=new Object()”这类引用，只要强引用还存在，垃圾收集器永远不会回收掉被引用的对象。

- 软引用（`Soft Reference`）

软引用是用来描述一些还有用但并非必须的对象。对于软引用关联着的对象，在系统将要发生内存溢出异常前，将会把这些对象列进回收范围之中进行第二次回收。如果这次回收还没有足够的内存，才会抛出内存溢出异常。jdk1.2之后，提供了SoftReference类实现软引用。

- 弱引用（`Weak Reference`）

弱引用也是用来描述非必须对象的，但是它的强度比软引用更弱一些，被弱引用关联的对象只能生存到下一次垃圾收集发生之前。当垃圾收集器工作时，无论当前内存是否足够，都会回收掉只被弱引用关联的对象。在jdk1.2之后，提供了WeakReference类来实现弱应用。

- 虚引用（`Phantom Reference`）

虚引用也称为幽灵引用或者幻影引用，它是最弱的一种引用关系。一个对象是否有虚引用的存在，完全不会对其生存时间构成影响，也无法通过虚引用来取得一个对象实例。为一个对象设置虚引用关联的唯一目的就是能在这个对象被收集器回收时收到一个系统通知。在jdk1.2之后，提供了PhantomReference类来实现虚引用。

### 对象在死亡之前会执行一次（仅仅一次）`finalize`方法
在finalize方法中可以进行自我拯救

### 方法区中垃圾收集针对无用类收集条件：

- 该类的所有实例都已经被回收，也就是java堆中不存在该类的任何实例
- 加载该类的ClassLoader已经被回收
- 该类对应的java.lang.Class对象没有在任何地方被引用，无法在任何地方通过发射访问该类的方法

## 垃圾收集算法

### 标记-清除算法（Mark-Sweep）

- 标记和清除效率不高
- 容易产生垃圾碎片

### 复制算法（Copying）
- 内存分为2块，一个用于存放对象，一块用于垃圾收集时置换空间
- 内存缩小为原来一半，代价有些高

* 基于复制算法演变为了：将堆内存分为三块，`Eden(8)`、`Survivor1(1)`、`Survivor2(1)`,且`Eden:Survivor=8/1`
也就是说每次新生代中可用内存空间为整个新生代容量的90%（80%+10%），只有10%的内存会被“浪费”。但没有办法保证每次回收都只有不多于10%的对象存活，当Survivor空间不够用时，需要依赖其他内存（老年代）进行分配担保。

### 标记整理算法（Mark Compact）
标记过程与“标记-清除”算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向一端移动，然后直接清理掉端边界以外的内存。

### 分代收集算法
根据对象存活周期不同将内存划分为几块。一般是把java堆分为新生代和老年代，这样就可以根据各个年代的特点采用最适当的收集算法。在新生代中，每次垃圾收集时都发现有大批对象死去，只有少量存活，那就选用复制算法；而老年代中因为对象存活率高、没有额外空间对它进行分配担保，就必须使用“标记-清理”或者“标记-整理”算法来回收。

## 垃圾收集器

### Serial收集器（Client模式下推荐）
停顿所有线程，单线程收集，无线程切换开销，一般会选择运行在Client模式下

### ParNew（Serial的多线程版本）
Server模式下虚拟机中首选新生代收集器，除了Serial收集器外，目前只有它能和CMS收集器配合使用。

### Parallel Scavenge收集器（适用于新生代）
（关注吞吐量，适合后台运算，不需要与用户交互的任务）

### Serial Old收集器（Serial老年代版本）

### Parallel Old收集器（Parallel Scavenge收集器的老年代版本）

### CMS（Concurrent Mark Sweep）收集器（互联网和B/S系统服务端，重视服务响应速度）

收集器收集过程比较复杂，包含4个步骤：

- 初始标记（CMS initial mark）
- 并发标记（CMS concurrent mark）
- 重新标记（CMS remark）
- 并发清除（CMS concurrent sweep）

### G1收集器

### 垃圾收集器参数总结

垃圾收集相关的常用参数



| 参数                           | 描述                                                         |
| ------------------------------ | :----------------------------------------------------------- |
| UseSerialGC                    | 虚拟机运行在 Client模式下的默认值,打开此开关后,使用 Serial+Serial Old的收集器组合进行内存回收 |
| UseParNewGC                    | 打开此开关后,使用 ParNew+ Serial Old的收集器组合进行内存回收 |
| UseConcMarkSweepGC             | 打开此开关后,使用 ParNew+CMS+ SerialOld的收集器组合进行内存回收，Serial Old收集器将作为CMS收集器出现 Oncurrent Mode Failure失败后的后备收集器使用 |
| UseParallelGC                  | 虚拟机运行在 Server模式下的默认值,打开此开关后,使用 ParallelScavenge+ Serial Old( PS Marksweep)的收集器组合进行内存回收 |
| UseParallelOldGC               | 打开此开关后,使用 Parallel Scavenge+ Parallel Old的收集器组合进行内存回收 |
| SurvivorRatio                  | 新生代中Eden区域与 Survivor区域的容量比值,默认为8,代表Eden: Survivor=8:1 |
| PretenureSizeThreshold         | 直接晋升到老年代的对象大小,设置这个参数后,大于这个参数的对象，将直接在老年代分配 |
| MaxTenuringThreshold           | 晋升到老年代的对象年龄。每个对象在坚持过一次 Minor GC之后,年龄就增加1,当超过这个参数值时就进人老年代 |
| UseAdaptiveSizePolicy          | 动态调整Java堆中各个区域的大小以及进入老年代的年龄           |
| HandlePromotionFailure         | 是否允许分配担保失败,即老年代的剩余空间不足以应付新生代的整个Eden和 Survivor区的所有对象都存活的极端情况 |
| ParallelGCThreads              | 设置并行GC时进行内存回收的线程数                             |
| GCTimeRatio                    | GC时间占总时间的比率,默认值为99,即允许1%的GC时间。仅在使用 Parallel Scavenge收集器时生效 |
| MaxGCPauseMillis               | 设置GC的最大停顿时间。仅在使用 Parallel Scavenge收集器时生效 |
| CMSInitiatingOccupancyFraction | 设置CMS收集器在老年代空间被使用多少后触发垃圾收集。默认值为68%,仅在使用CMS收集器时生效 |
| UseCMSCompactatFullCollection  | 设置CMS收集器在完成垃圾收集后是否要进行一次内存碎片整理。仅在使用CMS收集器时生效 |
| CMSFullGCsBeforeCompaction     | 设置CMS收集器在进行若干次垃圾收集后再启动一次内存碎片整理仅在使用CMS收集集器时生效 |















