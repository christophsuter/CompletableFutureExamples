package ch.noser.com.edays.track;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * Created by christoph.suter on 19.08.2016.
 */
public class HistoryThreadTest {

    public static final int THREE_SECONDS = 3000;
    public static final int FIVE_HUNDRED_MS = 500;

    @Test
    public void ThreadExampleTest() throws InterruptedException {
        new Thread(() -> {
            //Do some calculations....
            String result = "Calculated Result";
            System.out.println("Result: " + result);
        }).start();

        Thread.sleep(100);
    }

    @Test
    public void FutureExampleTest() throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> {
            //Do some calculations....
            String result = "Calculated Result";
            return result;
        });

        System.out.println("Result: " + future.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void FutureExampleTestWithDelay() throws ExecutionException, InterruptedException, TimeoutException {
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
        System.out.println("Result: " + future.get(10, TimeUnit.SECONDS));
    }

}
