package com.gateway.Util;


/**
 * @author lw
 */
public class ThreadInfo implements Runnable {

    private Thread t;

    @Override
    public void run() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        int currentAmount = group.activeCount();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentAmount != group.activeCount()) {
                currentAmount = Thread.activeCount();
                System.out.println("当前线程:" + currentAmount);

                Thread[] lstThreads = new Thread[currentAmount];
                group.enumerate(lstThreads);
                for (Thread thread: lstThreads) {
                    System.out.println("Thead name: " + thread.getName());
                }
            }
        }
    }

    public void start () {
        System.out.println("Starting print thread info ");
        if (t == null) {
            t = new Thread (this, "thread info");
            t.start ();
        }
    }
}
