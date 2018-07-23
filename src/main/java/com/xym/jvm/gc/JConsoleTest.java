package com.xym.jvm.gc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * VM Args: -Xms=100m -Xmx=100m -XX:+UseSerialGC
 *
 * @author xym
 * @create 2018-07-23 1:02
 */
public class JConsoleTest {

    static class OOMObject {
        //64K
        public byte[] bytes = new byte[64 * 1024];
    }

    public static void fillHeap(int num) throws InterruptedException {
        List<OOMObject> objects = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            //稍微延迟一下，让监控曲线更加明显
            TimeUnit.MILLISECONDS.sleep(500);
            objects.add(new OOMObject());
        }
        System.out.println("--over!");
        //System.gc();
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(1000 * 5);
        fillHeap(1000);
        System.gc();
        Thread.sleep(1000 * 50);
    }

}
