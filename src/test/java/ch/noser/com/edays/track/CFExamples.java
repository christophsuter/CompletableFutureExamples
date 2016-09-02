package ch.noser.com.edays.track;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Christoph on 15.08.2016.
 */
public class CFExamples {

    private static final int DELAY = 100;

    @Test
    public void testCompletableFuture() {
        CompletableFuture.supplyAsync(() -> "www.google.ch")
                .thenApplyAsync(ValidationTasks::download)
                .thenApplyAsync(ValidationTasks::parse)
                .thenApplyAsync(ValidationTasks::validate)
                .thenApplyAsync(ValidationTasks::calculateScore)
                .whenComplete(ValidationTasks::log)
                .join();
    }

    @Test
    public void testCreateCF() {
        CompletableFuture<String> cfSupplier = CompletableFuture.supplyAsync(() -> "cfSupplier"); //Might take longer
        CompletableFuture<Void> cfRunnable = CompletableFuture.runAsync(() -> { /* Do something */ }); //Might take longer
        CompletableFuture<String> cfCompleted = CompletableFuture.completedFuture("cfCompleted"); //Already Completed

        cfSupplier.whenComplete(ValidationTasks::log).join();
        cfRunnable.whenComplete(ValidationTasks::log).join();
        cfCompleted.whenComplete(ValidationTasks::log).join();
    }


    @Test
    public void testNextStageFuture() {
        Executor executor = Executors.newFixedThreadPool(100);

        //CompletableFuture with Functions
        CompletableFuture.supplyAsync(() -> "10")
                .thenApply(Integer::valueOf)
                .thenApplyAsync(Integer::valueOf)
                .thenApplyAsync(i -> i * i, executor)
                .whenComplete(ValidationTasks::log);

        //CompletableFuture with Consumers
        CompletableFuture.supplyAsync(() -> "10")
                .thenAccept(str -> System.out.println("Accepted String: " + str))
                .thenAcceptAsync(aVoid -> {/* Do some calculation */ })
                .thenAcceptAsync(aVoid -> {/* Do some calculation */ }, executor)
                .whenComplete(ValidationTasks::log);

        //CompletableFuture with Runnables
        CompletableFuture.supplyAsync(() -> "10")
                .thenRun(() -> {/* Do some calculation */ })
                .thenRunAsync(() -> {/* Do some calculation */ })
                .thenRunAsync(() -> {/* Do some calculation */ }, executor)
                .whenComplete(ValidationTasks::log);
    }


    @Test
    public void testCombine() {
        int size = 5;

        CompletableFuture<Double> circleArea = CompletableFuture
                .supplyAsync(() -> size / 2.0)
                .thenApply((r) -> r * r * Math.PI);

        CompletableFuture<Double> squareArea = CompletableFuture
                .supplyAsync(() -> size)
                .thenApply((r) -> 1.0 * r * r);

        //Combine with a function
        circleArea.thenCombine(squareArea, (circle, square) -> Math.abs(circle - square))
                .thenAccept(diff -> System.out.println("Difference between circle and square: " + diff));

        //Combine with a Consumers
        circleArea.thenAcceptBothAsync(squareArea, (circle, square) -> {
            Double diff = Math.abs(circle - square);
            System.out.println("Difference between circle and square: " + diff);
        });

        //Combine with a Runnable
        circleArea.runAfterBothAsync(squareArea, () -> {
            System.out.println("Both calculation finished");
        });
    }


    @Test
    public void testEither() throws InterruptedException {
        int size = 5;

        CompletableFuture<Double> circleArea = CompletableFuture
                .supplyAsync(() -> size / 2.0)
                .thenApplyAsync(CFExamples::randomDelay)
                .thenApplyAsync((r) -> r * r * Math.PI);

        CompletableFuture<Double> squareArea = CompletableFuture
                .supplyAsync(() -> size)
                .thenApplyAsync(CFExamples::randomDelay)
                .thenApplyAsync((r) -> 1.0 * r * r);

        //Either with a function
        circleArea.applyToEitherAsync(squareArea, area -> area * area)
                .thenAccept(area -> System.out.println("Area calculated: " + area));


        //Eiterh with a Consumers
        circleArea.acceptEitherAsync(squareArea, area -> {
            System.out.println("Area calculated: " + area);
        });

        //Either with a Runnable
        circleArea.runAfterEitherAsync(squareArea, () -> {
            System.out.println("Either is finished!");
        });

        Thread.sleep(DELAY);
    }


    @Test
    public void testReuseComputation() {
        int size = 5;

        CompletableFuture<Double> squareArea = CompletableFuture
                .supplyAsync(() -> size)
                .thenApply((r) -> r * r * 1.0);

        squareArea.thenApply(area -> area + 1)
                .thenAccept(area -> System.out.println("Area big: " + area));

        squareArea.thenAccept(area -> System.out.println("Area small: " + area));
    }


    @Test
    public void testCallback() {
        int size = 5;

        CompletableFuture.supplyAsync(() -> size)
                .thenApply((r) -> r * r)
                .whenComplete(ValidationTasks::log)
                //Further calculations
                .thenApply(Math::sqrt)
                .whenComplete(ValidationTasks::log);


        CompletableFuture.supplyAsync(() -> size)
                .thenApply((r) -> r * r)
                .handle(CFExamples::handleInteger);
    }

    //Delay randomly
    private static <U> U randomDelay(U value) {
        try {
            Thread.sleep((long) (Math.random() * DELAY));
        } catch (InterruptedException ex) {
        }
        return value;
    }

    @Test
    public void testExceptionDependent() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> "String")
                .thenApply(CFExamples::exceptionTask)
                .thenAccept(str -> System.out.println("Calculated string: " + str))
                .thenRun(() -> {
                    System.out.println("Run Task");
                })
                .whenComplete(ValidationTasks::log);
    }

    private static String exceptionTask(String s) {
        throw new RuntimeException("BAM");
    }


    @Test
    public void testCombinedException() throws InterruptedException {
        CompletableFuture<String> aFailureFast = CompletableFuture.supplyAsync(() -> "A (fast)")
                .thenApplyAsync(CFExamples::fixDelay)
                .thenApplyAsync(CFExamples::throwException);

        CompletableFuture<String> aFailureSlow = CompletableFuture.supplyAsync(() -> "A (slow)")
                .thenApplyAsync(CFExamples::fixDelay)
                .thenApplyAsync(CFExamples::fixDelay)
                .thenApplyAsync(CFExamples::fixDelay)
                .thenApplyAsync(CFExamples::throwException);


        CompletableFuture<String> bFailure = CompletableFuture.supplyAsync(() -> "B")
                .thenApplyAsync(CFExamples::throwException);

        CompletableFuture<String> bSuccess = CompletableFuture.supplyAsync(() -> "B")
                .thenApplyAsync(CFExamples::fixDelay)
                .thenApplyAsync(CFExamples::fixDelay);


        aFailureFast.thenCombine(bFailure, (a, b) -> a + b)
                .whenCompleteAsync((value, exception) -> logValueOrException(value, exception, "Exception in A (fast), Exception in B, Combined"));

        aFailureFast.applyToEither(bFailure, a -> a)
                .whenCompleteAsync((value, exception) -> logValueOrException(value, exception, "Exception in A (fast), Exception in B, Either"));

        aFailureFast.thenCombine(bSuccess, (a, b) -> a + b)
                .whenCompleteAsync((value, exception) -> logValueOrException(value, exception, "Exception in A (fast), Success in B, Combined"));

        aFailureFast.applyToEither(bSuccess, a -> a)
                .whenCompleteAsync((value, exception) -> logValueOrException(value, exception, "Exception in A (fast), Success in B, Either"));

        aFailureSlow.applyToEither(bSuccess, a -> a)
                .whenCompleteAsync((value, exception) -> logValueOrException(value, exception, "Exception in A (slow), Success in B, Either"));


        Thread.sleep(DELAY * 5);
    }

    private static <U> U fixDelay(U value) {
        try {
            Thread.sleep((long) (DELAY));
        } catch (InterruptedException ex) {
        }
        return value;
    }


    @Test
    public void testThenCompose() {
        CompletableFuture.supplyAsync(() -> "google")
                .thenApplyAsync(a -> "www." + a)
                .thenApplyAsync(a -> a + ".ch")
                .thenCompose(input -> cfForCalculateScore(input))
                .whenComplete(ValidationTasks::log)
                .join();
    }

    private CompletionStage<Integer> cfForCalculateScore(String url) {
        return CompletableFuture.supplyAsync(() -> url)
                .whenComplete((v, t) -> System.out.println("Download from " + v))
                .thenApplyAsync(ValidationTasks::download)
                .thenApplyAsync(ValidationTasks::parse)
                .thenApplyAsync(ValidationTasks::calculateScore);
    }

    private static Integer handleInteger(Integer input, Throwable exception) {
        if (input != null) {
            return input;
        } else {
            System.out.println("ex: " + ExceptionUtils.getStackTrace(exception));
            return 123;
        }
    }

    private static <U> U throwException(U s) {
        throw new RuntimeException("Exception - " + s);
    }

    private void logValueOrException(String value, Throwable exception, String description) {
        String msg = exception == null ? null : exception.getMessage();
        System.out.println("Case: " + description + ", value: " + value + ", Exception: " + msg);
    }

}