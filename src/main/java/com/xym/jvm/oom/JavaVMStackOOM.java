package com.xym.jvm.oom;

/**
 * 描述类作用
 *
 * @author xym
 * @create 2018-07-20 11:40
 */
public class JavaVMStackOOM {

    private void dontStop() {
        while (true) {

        }
    }

    public void stackLeakByThread() {
        while (true) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dontStop();
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        new JavaVMStackOOM().stackLeakByThread();
    }
}
