package com.demo.gateway.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 打印当前系统的线程信息
 * 确认客户端是否能重用
 * @author lw
 */
public class ThreadInfo implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadInfo.class);

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
                logger.info("当前线程:" + currentAmount);

                Thread[] lstThreads = new Thread[currentAmount];
                group.enumerate(lstThreads);
                for (Thread thread: lstThreads) {
                    logger.info("Thead name: " + thread.getName());
                }
            }
        }
    }

    public void start () {
        logger.info("Starting print thread info ");
        if (t == null) {
            t = new Thread(this, "thread info");
            t.start ();
        }
    }
}
