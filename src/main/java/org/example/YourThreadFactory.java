package org.example;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class YourThreadFactory implements ThreadFactory {

    AtomicInteger atomicInteger = new AtomicInteger();

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "Jar Executor" + atomicInteger.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
