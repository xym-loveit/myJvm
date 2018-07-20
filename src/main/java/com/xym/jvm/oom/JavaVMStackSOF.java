package com.xym.jvm.oom;

/**
 * 栈内存溢出示例
 * <p>
 * -Xss128k
 *
 * @author xym
 * @create 2018-07-20 11:33
 */
public class JavaVMStackSOF {

    private int stackLength = 1;

    public void stackLeak() {
        stackLength++;
        stackLeak();
    }

    public static void main(String[] args) {
        JavaVMStackSOF javaVMStackSOF = new JavaVMStackSOF();
        try {
            javaVMStackSOF.stackLeak();
        } catch (Throwable e) {
            System.out.println("stack length:" + javaVMStackSOF.stackLength);
            throw e;
        }
    }

}
