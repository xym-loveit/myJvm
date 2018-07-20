package com.xym.jvm.oom;

import java.util.ArrayList;
import java.util.List;

/**
 * 虚拟机堆OOM示例
 * <p>
 * -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError
 *
 * @author xym
 * @create 2018-07-20 11:15
 */
public class HeapOOM {

    static class OOMObject {

    }

    public static void main(String[] args) {
        List<OOMObject> objects = new ArrayList<>();
        while (true) {
            objects.add(new OOMObject());
        }
    }
}
