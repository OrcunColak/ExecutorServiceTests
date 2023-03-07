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
            testInterruptedException(index);
        }

//        for (int index = 0; index < 10; index++) {
//            testExecutionException(index);
//        }
//        for (int index = 0; index < 10; index++) {
//            testTimeoutExceptionWithNotInterruptedTask(index);
//        }

//        for (int index = 0; index < 10; index++) {
//            testTimeoutExceptionWithInterruptedTask(index);
//        }
        System.out.println("\n\nThread Dump");
        Thread.getAllStackTraces().keySet()
                .forEach((t) -> System.out.println(t.getName() + " Is Daemon " + t.isDaemon() + " Is Alive " + t.isAlive()));
        System.in.read();
    }

    static void testInterruptedException(int index) {
        Future<?> future = executorService.submit((Runnable) () -> {
            Thread.currentThread().interrupt();
        });
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        } catch (ExecutionException exception) {
            System.out.println("ExecutionException occurred in testInterruptedException " + index);
        } catch (TimeoutException exception) {
            System.out.println("Timeout occurred in testInterruptedException " + index);
            future.cancel(true);
        }
    }

    static void testExecutionException(int index) {
        Future<?> future = executorService.submit((Runnable) () -> {
            throw new RuntimeException("throwing from testExecutionException");
        });
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        } catch (ExecutionException exception) {
            System.out.println("ExecutionException occurred in testExecutionException " + index);
        } catch (TimeoutException exception) {
            System.out.println("Timeout occurred in testExecutionException " + index);
            future.cancel(true);
        }
    }

    static void testTimeoutExceptionWithInterruptedTask(int index) {
        Future<?> future = executorService.submit(() -> {
            try {
                Thread.sleep(10_000);
                System.out.println("executing testTimeoutExceptionWithInterruptedTask " + index);
            } catch (Exception exception) {
                System.out.println("Exception in testTimeoutExceptionWithInterruptedTask " + index + " " + exception
                );
            }
        });
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        } catch (ExecutionException exception) {
            throw new RuntimeException(exception);
        } catch (TimeoutException e) {
            System.out.println("Timeout occurred in testTimeoutExceptionWithInterruptedTask " + index);
            future.cancel(true);
            executorService.shutdownNow();

            executorService = createExecutorService();
        }
    }

    static void testTimeoutExceptionWithNotInterruptedTask(int index) {
        Future<?> future = executorService.submit(() -> {
            try {
                System.in.read();
                System.out.println("executing testTimeoutExceptionWithNotInterruptedTask " + index);
            } catch (Exception exception) {
                System.out.println("Exception in testTimeoutExceptionWithNotInterruptedTask " + index + " " + exception
                );
            }
        });
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        } catch (ExecutionException exception) {
            throw new RuntimeException(exception);
        } catch (TimeoutException exception) {
            System.out.println("Timeout occurred in testTimeoutExceptionWithNotInterruptedTask " + index);
            future.cancel(true);
            executorService.shutdownNow();

            executorService = createExecutorService();
        }
    }
}