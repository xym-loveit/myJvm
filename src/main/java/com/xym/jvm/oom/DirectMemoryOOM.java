package com.xym.jvm.oom;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 本机内存溢出
 *
 * @author xym
 * @create 2018-07-20 14:06
 */
public class DirectMemoryOOM {

    private static final int _1MB = 2 << 19;

    public static void main(String[] args) {
        Field field = Unsafe.class.getDeclaredFields()[0];
        field.setAccessible(true);
        try {
            Unsafe unsafe = (Unsafe) field.get(null);
            while (true) {
                unsafe.allocateMemory(_1MB);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
