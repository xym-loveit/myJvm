package com.xym.jvm.oom;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述类作用
 *
 * @author xym
 * @create 2018-07-20 11:47
 */
public class RuntimeConstantPoolOOM {
    public static void main(String[] args) {
        //使用list保持着常量池引用，避免Full GC回收常量池行为
        List<Object> list = new ArrayList<>();
        int i = 0;
        try {
            while (true) {
                list.add(String.valueOf(i++).intern());
            }
        } catch (Throwable e) {
            System.out.println("perm size=" + i);
            throw e;
        }
    }
}
