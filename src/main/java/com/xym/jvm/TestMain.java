package com.xym.jvm;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 描述类作用
 *
 * @author xym
 * @create 2018-07-20 0:54
 */
public class TestMain {

    public static void main(String[] args) {
        List<SoftReference> arryList = new ArrayList<>();

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            SoftReference<Reference> softReference = new SoftReference<>(new Reference());
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            arryList.add(softReference);
        }

    }

    static class Reference {
        //1m
        private byte[] bytes = new byte[2 << 19];

        @Override
        protected void finalize() throws Throwable {
            System.out.println("will be gc!" + this);
        }
    }
}
