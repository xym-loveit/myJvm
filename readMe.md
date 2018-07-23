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



## 认识GC日志

每一种收集器的日志形式都是由它们自身的实现所决定的,换而言之,每个收集器的日志格式都可以不一样。但虚拟机设计者为了方便用户阅读,将各个收集器的日志都维持一定的共性,例如以下两段典型的GC日志:

```
0.139: [GC [PSYoungGen: 6041K->4728K(37376K)] 6041K->4728K(122368K), 0.0160554 secs] [Times: user=0.03 sys=0.00, real=0.02 secs] 
0.155: [Full GC [PSYoungGen: 4728K->0K(37376K)] [ParOldGen: 0K->4628K(84992K)] 4728K->4628K(122368K) [PSPermGen: 3076K->3075K(21504K)], 0.0238742 secs] [Times: user=0.03 sys=0.03, real=0.02 secs]  

```

1. 前面的数字（0.139、0.155）代表GC发生的时间，这个数字的含义是从Java虚拟机启动以来经过的秒数。
2. GC日志开头的“GC和Full GC”说明了这次垃圾收集的停顿类型，而不是用来区分新生代还是老年代GC的，如果有“Full”，说明这次GC是发生了“Stop-The-World”的
3. [PSYoungGen、“DefNew”、"Tenured"、“Perm”]表示GC发生的区域，这里显示的区域名称与使用的GC收集器是密切相关的，例如当前所使用的“Parallel Scavenge收集器，那配套新生代为”PSYoungGen“，老年代和永久代同理，名称也是由收集器决定的。
4. 方括号内部“6041K->4728K(37376K)”含义是“GC前该内存区域已使用容量-->GC后该内存区域已使用容量（该内存区域总容量）”。而在方括号之外的"6041K->4728K(122368K)"表示GC前Java堆已使用容量-->GC后Java堆已使用容量（Java堆总容量）
5. 再往后“0.0160554 secs”表示该内存区域GC所占用的时间，单位是秒。



## 对象分配测试示例

### 1、对象优先在Eden区分配

```java
/**
 * 描述类作用
 * <p>
 * VM Args:-XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails
 * <p>
 * -XX:+UseSerialGC：使用串行带线程收集器
 * <p>
 * -verbose:gc：在输出设备上显示虚拟机垃圾收集信息
 * <p>
 * -Xms20M -Xmx20M :控制java堆内存大小为20M
 * <p>
 * -Xmn10M：分配10兆给新生代,剩余10M老年代
 * <p>
 * -XX:+PrintGCDetails：详细输出GC日志
 * <p>
 * <p>
 * 此时java堆内存（新生代[8兆eden，1兆S0区，1兆S1区]）10M+（老年代）10M
 * <p>
 * 因此新生代可用空间为8+1=9M
 *
 * @author xym
 */
public class TestMain {
    //1兆
    private static final int _1MB = 2 << 19;

    public static void main(String[] args) {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[2 * _1MB];
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        //出现一次minor GC
        allocation4 = new byte[4 * _1MB];
    }
}

```

执行后结果为：

```

[GC[DefNew: 7506K->532K(9216K), 0.0064196 secs] 7506K->6677K(19456K), 0.0064722 secs] [Times: user=0.00 sys=0.02, real=0.01 secs] 
Heap
 def new generation   total 9216K, used 4958K [0x00000000f9a00000, 0x00000000fa400000, 0x00000000fa400000)
  eden space 8192K,  54% used [0x00000000f9a00000, 0x00000000f9e526c8, 0x00000000fa200000)
  from space 1024K,  52% used [0x00000000fa300000, 0x00000000fa3853d0, 0x00000000fa400000)
  to   space 1024K,   0% used [0x00000000fa200000, 0x00000000fa200000, 0x00000000fa300000)
 tenured generation   total 10240K, used 6144K [0x00000000fa400000, 0x00000000fae00000, 0x00000000fae00000)
   the space 10240K,  60% used [0x00000000fa400000, 0x00000000faa00030, 0x00000000faa00200, 0x00000000fae00000)
 compacting perm gen  total 21248K, used 2991K [0x00000000fae00000, 0x00000000fc2c0000, 0x0000000100000000)
   the space 21248K,  14% used [0x00000000fae00000, 0x00000000fb0ebdc0, 0x00000000fb0ebe00, 0x00000000fc2c0000)

-----------------------------------------------------------------------------------------
1、DefNew表示采用的串行收集器生效，7506K->532K(9216K)表明年轻代回收的效果很给力，7506K->6677K(19456K)，堆内存回收效果最终还是不理想，说明垃圾没怎么被回收，还是存再堆内存的老年代中。

2、eden space 8192K（8M）, from space 1024K（1M）,to   space 1024K（1M）,刚好和我们分析的一致。

3、tenured generation the space 10240K，60% used说明老年代空间大小为10M，且被占用了60%

4、当为allocation4分配4m内存空间时，此刻年轻代eden存放了allocation1，allocation2，allocation3共消耗6M内存此时，eden剩余空间=（8-6=2M），s0=s1=1M，这时他们都容不下4M，此时引发GC，GC发生后，因s区容纳不下6M，3个对象都会转移至老年代。此时老年代被占用6兆，也即是60%，eden区腾出来的空间容纳当前4M的allocation4

```

### 2、大对象直接在老年代分配

```
**
 * <p>
 * VM Args:-XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -     
 *  XX:SurvivorRatio=8 -XX:PretenureSizeThreshold=3145728
 * <p>
 * -XX:+UseSerialGC：使用串行带线程收集器
 * <p>
 * -verbose:gc：在输出设备上显示虚拟机垃圾收集信息
 * <p>
 * -Xms20M -Xmx20M :控制java堆内存大小为20M
 * <p>
 * -Xmn10M：分配10兆给新生代
 * <p>
 * -XX:+PrintGCDetails：详细输出GC日志
 * <p>
 * -XX:SurvivorRatio=8：Eden区和Survivor区域的容量比值为8:1
 *<p>
 *  -XX:PretenureSizeThreshold=3145728：大于3M的对象将直接在老年代分配
 * <p>
 * 此时java堆内存（新生代[8兆eden，1兆S0区，1兆S1区]）10M+（老年代）10M
 * <p>
 * 新生代可用空间为8+1=9M
 *
 * @author xym
 * @create 2018-07-21 18:20
 */
public static void main(String[] args) {
        //testAllocation();
        byte[] allocation = new byte[4 * _1MB];
}
```

执行结果：

```
Heap
 def new generation   total 9216K, used 1526K [0x00000000f9a00000, 0x00000000fa400000, 0x00000000fa400000)
  eden space 8192K,  18% used [0x00000000f9a00000, 0x00000000f9b7d9c8, 0x00000000fa200000)
  from space 1024K,   0% used [0x00000000fa200000, 0x00000000fa200000, 0x00000000fa300000)
  to   space 1024K,   0% used [0x00000000fa300000, 0x00000000fa300000, 0x00000000fa400000)
 tenured generation   total 10240K, used 4096K [0x00000000fa400000, 0x00000000fae00000, 0x00000000fae00000)
   the space 10240K,  40% used [0x00000000fa400000, 0x00000000fa800010, 0x00000000fa800200, 0x00000000fae00000)
 compacting perm gen  total 21248K, used 2946K [0x00000000fae00000, 0x00000000fc2c0000, 0x0000000100000000)
   the space 21248K,  13% used [0x00000000fae00000, 0x00000000fb0e0e18, 0x00000000fb0e1000, 0x00000000fc2c0000)
   
1、tenured generation   total 10240K, used 4096K，40% used，表明4M的对象直接分配在老年代中
```

### 3、长期存活的对象将进入老年代

```
//-XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1
//MaxTenuringThreshold:晋升到老年代的对象年龄,当超过指定年龄后对象会进入老年代

public static void main(String[] args) {
        //testAllocation();
        //testPretenureSizeThreshold();
        byte[] allocation1, allocation2, allocation3;
        allocation1 = new byte[_1MB / 4];
        allocation2 = new byte[4 * _1MB];
        allocation3 = new byte[4 * _1MB];
        allocation3 = null;
        allocation3 = new byte[4 * _1MB];
 }
```

执行结果

```
[GC[DefNew: 5878K->783K(9216K), 0.0058853 secs] 5878K->4879K(19456K), 0.0059248 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[GC[DefNew: 4879K->0K(9216K), 0.0023356 secs] 8975K->4879K(19456K), 0.0023621 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 def new generation   total 9216K, used 4346K [0x00000000f9a00000, 0x00000000fa400000, 0x00000000fa400000)
  eden space 8192K,  53% used [0x00000000f9a00000, 0x00000000f9e3ea30, 0x00000000fa200000)
  from space 1024K,   0% used [0x00000000fa200000, 0x00000000fa200000, 0x00000000fa300000)
  to   space 1024K,   0% used [0x00000000fa300000, 0x00000000fa300000, 0x00000000fa400000)
 tenured generation   total 10240K, used 4879K [0x00000000fa400000, 0x00000000fae00000, 0x00000000fae00000)
   the space 10240K,  47% used [0x00000000fa400000, 0x00000000fa8c3c50, 0x00000000fa8c3e00, 0x00000000fae00000)
 compacting perm gen  total 21248K, used 2937K [0x00000000fae00000, 0x00000000fc2c0000, 0x0000000100000000)
   the space 21248K,  13% used [0x00000000fae00000, 0x00000000fb0de748, 0x00000000fb0de800, 0x00000000fc2c0000)
   
```

## Sun JDK监控和故障处理命令行工具

| 名称   | 主要作用                                                     |
| ------ | ------------------------------------------------------------ |
| jps    | JVM Process Status Tool,显示指定系统内所有的 Hotspot虚拟机进程 |
| jstat  | JVM Statistics Monitoring Tool,用于收集 Hotspot虚拟机各方面的运行数据 |
| jinfo  | Configuration Info for Java,显示示虚拟机配置信息             |
| jmap   | Memory Map for Java,生成虚拟机的内存转储快照( heapdump文件） |
| jhat   | JVM Heap Dump Browser,用于分析heapdump文件,它会建立一个HTTP/HTML服务器,让用户可以在浏览器上查看分析结果 |
| jstack | Stack Trace for Java,显示虚拟机的线程快照                    |

### 1、jps：虚拟机进程状况工具

`jps [-q] [-mlvV] [<hostid>]`

***jps主要选项***

| 选项 | 作用                                              |
| ---- | ------------------------------------------------- |
| -q   | 只输出 LVMID,省略主类的名称                       |
| -m   | 输出虚拟机进程启动时传递给主类 main（）函数的参数 |
| -l   | 输出主类的全名,如果进程执行的是Jar包,输出Jar路径  |
| -v   | 输出虚拟机进程启动时JVM参数                       |

###2、jstat：虚拟机统计信息监视工具

`jstat -<option> [-t] [-h<lines>] <vmid> [<interval> [<count>]]`

选项代表着用户希望查询的虚拟机信息，只要分为3类：类装载、垃圾收集、运行期编译状况

| 选项              | 作用                                                         |
| ----------------- | ------------------------------------------------------------ |
| -class            | 监视类装载、卸载数量、总空间以及类装载所耗费的时间           |
| -gc               | 监视Java堆状况,包括Eden区、两个 survivor区、老年代、永久代等的容量、已用空间、GC时间合计等信息 |
| -gccapacity       | 监视内容与-gc基本相同,但输出主要关注Java堆各个区域使用到的最大、最小空间 |
| -gcutil           | 监视内容与-gc基本相相同,但输出主要关注已使用空间占总空间的百分比 |
| -gccause          | 与- gcutil功能一样,但是会额外输出导致上一次GC产生的原因      |
| -gcnew            | 监视新生代GC状况                                             |
| -gcnewcapacity    | 监视内容与- gcnew基本相同,输出主要关注使用到的最大、最小空间 |
| -gcold            | 监视老年代GC状况                                             |
| -gcoldcapacity    | 监视内容与- gcold基本相同,输出主要关注使用到的最大、最小空间 |
| -gcpermcapacity   | 输出水久代使用到的最大、最小空间                             |
| -compiler         | 输出JⅡT编编译器编译过的方法、耗时等信息                      |
| -printcompilation | 输出已经被JⅡT编译的方法                                      |

### 3、jinfo：java配置信息工具

`jinfo [option] <pid>`

Jnfo( Configuration Info for Java)的作用是实时地查看和调整虚拟机各项参数。主要显示系统默认参数

### 4、jmap：java内存映像工具

`jmap [option] <pid>`

***jmap工具主要选项***

| 选项           | 作用                                                         |
| -------------- | ------------------------------------------------------------ |
| -dump          | 生成ava堆转储快照。格式为:dump[live,] format=b,file=< filename>,其中live子参数说明是否只dump出存活的对象 |
| -heap          | 显示Java堆详细信息,如使用哪种回收器、参数配置、分代状况等。只在 Linux/ Solaris平台下有效 |
| -histo         | 显示堆中对象统计信息,包括类、实例数量、合计容量              |
| -permstat      | 以 Classloader为统计口径显示永久代内存状态。只在 Linux/Solaris平台下有效 |
| -F             | 当虚拟机进程对-dump选项没有响应时,可使用这个选项强制生成dump快照。只在 Linux/ Solaris平台下有效 |
| -finalizerinfo | 显示在F- Queue中等待 Finalizer线程执行 finalize方法的对象。只在 Linux/ Solaris平台下有效 |

### 5、jstack：java堆栈跟踪工具

`jstack [-l] <pid>`

***jstack工具主要选项***

| 选项 | 作用                                        |
| ---- | ------------------------------------------- |
| -F   | 当正常输出的请求不被响应时,强制输出线程堆栈 |
| -l   | 除堆栈外,显示关于锁的附加信息               |
| -m   | 如果调用到本地方法的话,可以显示C/C++的堆栈  |

## 图形化分析工具

### 1、JConsole：Java监视与管理控制台

### 2、VisualVM：多合一故障处理工具



