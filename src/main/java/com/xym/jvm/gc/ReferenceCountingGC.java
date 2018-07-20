package com.xym.jvm.gc;

/**
 * 引用计数算法的缺陷
 * <p>
 * 循环引用会导致计数器算法无法通知GC收集器回收，在此证明jvm不是通过引用计数器算法来判断对象是否存活的
 *
 * @author xym
 * @create 2018-07-20 14:18
 */
public class ReferenceCountingGC {

    public Object instance = null;

    private static final int _1MB = 2 << 19;

    /**
     * 该成员属性的唯一意义就是占用点内存，以便能在GC日志中看清楚是否被回收过
     */
    private byte[] bytes = new byte[2 * _1MB];

    public static void main(String[] args) {

        ReferenceCountingGC countingGCA = new ReferenceCountingGC();
        ReferenceCountingGC countingGCB = new ReferenceCountingGC();
        countingGCA.instance = countingGCB;
        countingGCB.instance = countingGCA;

        /**
         * 假设在这里发生了gc，a和b是否能被回收?
         */
        System.gc();
    }
}
