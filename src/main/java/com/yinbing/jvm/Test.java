package com.yinbing.jvm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class Test {

    public static int a = 0;

    public static Object object = new Object();

    public static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < 10 ; i++) {
            new Thread(()->{
                try {
                    countDownLatch.await();
                    for (int j = 0; j < 1000; j++) {
                        synchronized (object){

                        }
                        lock.lock();
                            a++;
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }

            }).start();
        }

        Thread.sleep(1000);

        countDownLatch.countDown();

        Thread.sleep(1000);

        System.out.println(a);
    }

}
