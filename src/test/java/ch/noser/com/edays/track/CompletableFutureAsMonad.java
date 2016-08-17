package ch.noser.com.edays.track;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * Created by Christoph on 15.08.2016.
 */
public class CompletableFutureAsMonad {

    public static final int THREE_SECONDS = 3000;
    public static final int FIVE_HUNDRED_MS = 500;


    @Test
    public void testException() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> "10String")
                .thenApply(s -> Integer.valueOf(s))
                .whenComplete((result, exec) -> genericCallback(result, exec))
                .thenAccept((info) -> System.out.println("Generated: " + info));

        Thread.sleep(100);
    }


    @Test
    public void testExceptionHandling() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> "10String")
                .thenApply(s -> Integer.valueOf(s))
                .exceptionally((t) -> handleException(t))
                .thenAccept((info) -> System.out.println("Generated: " + info));

        Thread.sleep(100);
    }

    //Original value would be nice
    private Integer handleException(Throwable t) {
        System.out.println("Exception Handled" + t);
        return 10;
    }

    private void genericCallback(Object result, Throwable exec) {
        if (result != null) {
            System.out.println("Result: " + result);
        } else {
            System.out.println("Exception: " + exec);
        }
    }

    @Test
    public void testThenCompose() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> "String")
                .thenCompose(input -> doIt(input))
                .thenAccept((input) -> System.out.println("testThenCompose" + input));

        Thread.sleep(100);
    }

    private CompletionStage<Integer> doIt(String input) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("doIt!");
            return 12;

        });
    }


    @Test
    public void ThreadExampleTest() {
        new Thread(() -> {
            //Do some calculations....
            String result = "Calculated Result";
            System.out.println("Result: " + result);
        }).start();
    }


    @Test
    public void FutureExampleTest() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> {
            //Do some calculations....
            String result = "Calculated Result";
            return result;
        });

        System.out.println("Result: " + future.get());
    }

    @Test
    public void FutureExampleTestWithDelay() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> {
            //Do some calculations....
            Thread.sleep(THREE_SECONDS);

            String result = "Calculated Result";
            return result;
        });


        System.out.println("Waiting");
        while (!future.isDone()) {
            Thread.sleep(FIVE_HUNDRED_MS);
            System.out.println(".");
        }
        System.out.println("Result: " + future.get());
    }

    @Test
    public void testCompletableFuture() throws InterruptedException {
        CompletableFuture
                .supplyAsync(() -> {
                    //Do some calculations....
                    String result = "Calculated Result";
                    return result;
                })
                .thenAccept((info) -> System.out.println("Result: " + info));

        Thread.sleep(100);


    }



}