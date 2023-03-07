package org.example;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    private static ExecutorService executorService;

    static YourThreadFactory yourThreadFactory = new YourThreadFactory();

    private static ExecutorService createExecutorService() {
        return Executors.newSingleThreadExecutor(yourThreadFactory);
    }

    public static void main(String[] args) throws IOException {
        executorService = createExecutorService();


        for (int index = 0; index < 10; index++) {
            testExecutionException(index);
        }

        for (int index = 0; index < 10; index++) {
            testTimeoutExceptionWithInterruptableTask(index);
        }

        // This is going to leak threads
        for (int index = 0; index < 10; index++) {
            testTimeoutExceptionWithNotInterruptableTask(index);
        }

        System.out.println("\n\nThread Dump");
        Thread.getAllStackTraces().keySet()
                .forEach((t) -> System.out.println(t.getName() + " Is Daemon " + t.isDaemon() + " Is Alive " + t.isAlive()));
        System.in.read();
    }


    static void testExecutionException(int index) {
        Future<?> future = executorService.submit((Runnable) () -> {
            throw new RuntimeException("throwing from testExecutionException");
        });
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException exception) {
            System.out.println("dException is not expected in testExecutionException " + index);
        } catch (ExecutionException exception) {
            System.out.println("ExecutionException occurred in testExecutionException " + index);
        }
    }

    static void testTimeoutExceptionWithInterruptableTask(int index) {
        Future<?> future = executorService.submit(() -> {
            try {
                Thread.sleep(10_000);
                System.out.println("executing testTimeoutExceptionWithInterruptableTask " + index);
            } catch (Exception exception) {
                System.out.println("Exception in testTimeoutExceptionWithInterruptableTask " + index + " " + exception
                );
            }
        });
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException exception) {
            System.out.println("Exception is not expected in testTimeoutExceptionWithInterruptableTask " + index);
        } catch (TimeoutException e) {
            System.out.println("Timeout occurred in testTimeoutExceptionWithInterruptableTask " + index);
            future.cancel(true);
            executorService.shutdownNow();

            executorService = createExecutorService();
        }
    }

    static void testTimeoutExceptionWithNotInterruptableTask(int index) {
        Future<?> future = executorService.submit(() -> {
            try {
                System.in.read();
                System.out.println("executing testTimeoutExceptionWithNotInterruptableTask " + index);
            } catch (Exception exception) {
                System.out.println("Exception in testTimeoutExceptionWithNotInterruptableTask " + index + " " + exception
                );
            }
        });
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException exception) {
            System.out.println("Exception is not expected in testTimeoutExceptionWithNotInterruptableTask " + index);
        } catch (TimeoutException exception) {
            System.out.println("Timeout occurred in testTimeoutExceptionWithNotInterruptableTask " + index);
            future.cancel(true);
            executorService.shutdownNow();

            executorService = createExecutorService();
        }
    }
}