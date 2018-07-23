package com.xym.jvm.gc;

/**
 * 描述类作用
 * <p>
 * VM Args:-XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1
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
 * <p>
 * 此时java堆内存（新生代[8兆eden，1兆S0区，1兆S1区]）10M+（老年代）10M
 * <p>
 * 新生代可用空间为8+1=9M
 *
 * @author xym
 * @create 2018-07-21 18:20
 */
public class TestMain {
    //1兆
    private static final int _1MB = 2 << 19;

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

    //大对象直接在老年代分配
    //VM Args:-XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 -XX:PretenureSizeThreshold=3145728
    private static void testPretenureSizeThreshold() {
        byte[] allocation = new byte[4 * _1MB];
    }


    //对象优先在Eden区分配
    //VM Args:-XX:+UseSerialGC -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails
    private static void testAllocation() {
        byte[] allocation1, allocation2, allocation3, allocation4;
        allocation1 = new byte[2 * _1MB];
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        //出现一次minor GC
        allocation4 = new byte[4 * _1MB];
    }
}




