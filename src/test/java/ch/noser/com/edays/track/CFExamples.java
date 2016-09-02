package ch.noser.com.edays.track;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Created by Christoph on 15.08.2016.
 */
public class CFExamples {

    private static final int DELAY = 100;

    @Test
    //used
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
    //used
    public void testCreateCF() {
        CompletableFuture<String> cfSupplier = CompletableFuture.supplyAsync(() -> "cfSupplier"); //Might take longer
        CompletableFuture<Void> cfRunnable = CompletableFuture.runAsync(() -> { /* Do something */ }); //Might take longer
        CompletableFuture<String> cfCompleted = CompletableFuture.completedFuture("cfCompleted"); //Already Completed

        cfSupplier.whenComplete(ValidationTasks::log).join();
        cfRunnable.whenComplete(ValidationTasks::log).join();
        cfCompleted.whenComplete(ValidationTasks::log).join();
    }


    @Test
    //used
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
    //used
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
    //used
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
    //used
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
    //used
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
    //used
    private static <U> U randomDelay(U value) {
        try {
            Thread.sleep((long) (Math.random() * DELAY));
        } catch (InterruptedException ex) {
        }
        return value;
    }

    @Test
    //used
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


    //used
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


        Thread.sleep(DELAY*5);
    }

    //used
    private static <U> U fixDelay(U value) {
        try {
            Thread.sleep((long) (DELAY));
        } catch (InterruptedException ex) {
        }
        return value;
    }



    @Test
    //used
    public void testThenCompose() {
        CompletableFuture.supplyAsync(() -> "google")
                .thenApplyAsync(a -> "www." + a)
                .thenApplyAsync(a -> a + ".ch")
                .thenCompose(input -> cfForCalculateScore(input))
                .whenComplete(ValidationTasks::log)
                .join();
    }

    //used
    private CompletionStage<Integer> cfForCalculateScore(String url) {
        return CompletableFuture.supplyAsync(() -> url)
                .whenComplete((v,t) -> System.out.println("Download from " + v))
                .thenApplyAsync(ValidationTasks::download)
                .thenApplyAsync(ValidationTasks::parse)
                .thenApplyAsync(ValidationTasks::calculateScore);
    }

    private void logValueOrException(String value, Throwable exception, String description) {
        String msg = exception == null ? null : exception.getMessage();
        System.out.println("Case: " + description + ", value: " + value + ", Exception: " + msg);
    }

    private static <U> U throwException(U s) {
        throw new RuntimeException("Exception - " + s);
    }


    @Test
    public void testAlwaysException() {
        CompletableFuture<Object> cf = executeErrorCF();
        cf.isCompletedExceptionally();

        CompletableFuture
                .completedFuture("www.google.ch")
                .thenApplyAsync(a -> a)
                .whenComplete(ValidationTasks::log)
                .join();

    }

    private CompletableFuture<Object> executeErrorCF() {
        return CompletableFuture.supplyAsync(() -> "www.google.ch")
                .thenApplyAsync((uri) -> {
                    throw new RuntimeException("Bla");
                })
                .whenComplete(ValidationTasks::log);
    }

    @Test
    public void testWasUndWie() {


        //Timeout --> schwierig
        new Thread(() -> {
            try {
                String website = ValidationTasks.download("www.google.ch");
                List<String> parsedWebsite = ValidationTasks.parse(website);
                Integer score = ValidationTasks.calculateScore(parsedWebsite);
            } catch (RuntimeException ex) {
                //?
            }


        }).run();


        CompletableFuture.supplyAsync(() -> "www.google.ch")
                .thenApplyAsync(ValidationTasks::download)
                .thenApplyAsync(ValidationTasks::parse)
                .thenApplyAsync(ValidationTasks::validate)
                .thenApplyAsync(ValidationTasks::calculateScore)
                .whenComplete(ValidationTasks::log)
                .join();
    }


    @Test
    public void testDupplicate() {
        int size = 5;

        CompletableFuture<Double> squareArea = CompletableFuture
                .supplyAsync(() -> size)
                .thenApply((r) -> r * r * 1.0);

        squareArea.thenApply(area -> area + 1)
                .thenAccept(area -> System.out.println("Area big: " + area));

        squareArea.thenAccept(area -> System.out.println("Area small: " + area));
    }


    @Test
    public void testAttachFurtherOperations() throws ExecutionException, InterruptedException {
        int size = 5;

        CompletableFuture<Double> squareArea = CompletableFuture
                .supplyAsync(() -> size)
                .thenApply((r) -> r * r * 1.0);

        System.out.println("Area: " + squareArea.get());  //Blocking, finish calculation

        squareArea.thenApply(area -> area + 1)
                .thenAccept(area -> System.out.println("Area Big: " + area));
    }


    @Test
    public void testException() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> "10-String")
                .thenApply(s -> Integer.valueOf(s))
                .whenComplete((result, exec) -> genericCallback(result, exec));
    }

    private void genericCallback(Object result, Throwable exec) {
        if (result != null) {
            System.out.println("Result: " + result);
        } else {
            System.out.println("Exception: " + exec);
        }
    }

    private CompletionStage<Integer> doIt(String input) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("doIt!");
            return 12;

        });
    }




    @Test
    public void testCompletableFuture2() throws InterruptedException {
        CompletableFuture
                .supplyAsync(() -> {
                    //Do some calculations....
                    String result = "Calculated Result";
                    return result;
                })
                .thenAccept((info) -> System.out.println("Result: " + info));

        Thread.sleep(100);

    }


    private static <A> CompletableFuture<A> timeout(CompletableFuture<? extends A> cf, int wait) {
        CompletableFuture<A> timeout = new CompletableFuture<>();
        Executors.newScheduledThreadPool(1).schedule(() -> timeout.completeExceptionally(new TimeoutException("bla")), wait, TimeUnit.SECONDS);
        cf.thenAccept(a -> timeout.complete(a));
        return timeout;
    }

    private <A> CompletableFuture<A> timeout2(CompletableFuture<A> cf, int wait) {
        CompletableFuture<A> timer = getATimeoutCF(wait);
        return cf.applyToEither(timer, Function.identity());
    }

    private <A> CompletableFuture<A> timeout3(Object input, Function<Object, A> function, int timeout) {
        CompletableFuture<A> timer = getATimeoutCF(timeout);
        CompletableFuture<A> task = CompletableFuture.supplyAsync(() -> input)
                .thenApply(i -> function.apply(i));

        //CompletableFuture<A> task = new CompletableFuture<>().thenApply(i -> function.apply(i));
        return task.applyToEither(timer, Function.identity());
    }


    private static <A> CompletableFuture<A> getATimeoutCF(int wait) {
        CompletableFuture<A> timer = new CompletableFuture<>();
        Executors.newScheduledThreadPool(1).schedule(() -> timer.completeExceptionally(new TimeoutException("bla")), wait, TimeUnit.SECONDS);
        return timer;
    }


    @Test
    public void thenComposeTest() throws InterruptedException {
        CompletableFuture.supplyAsync(() -> "start")
                .thenCompose(str -> timeout3(str, a -> a + "....", 1))
                .thenCompose(str -> timeout3(str, a -> a + "----", 1))
                .whenComplete((r, e) -> System.out.println("Result: " + r + ", exec: " + e))
                .thenAccept(s -> System.out.println("Result: s"));


        Thread.sleep(100);
    }

    @Test
    public void testHandleException() throws InterruptedException {
        CompletableFuture
                .supplyAsync(() -> "10-NotInteger")
                .thenApply(Integer::valueOf)
                .thenApply(i -> 10 / i)
                .whenComplete((input, exception) -> handleInteger(input, exception))
                .handle((input, exception) -> handleInteger(input, exception))
                .thenAccept((info) -> System.out.println("Result: " + info));

        Thread.sleep(100);

    }

    //used
    private static Integer handleInteger(Integer input, Throwable exception) {
        if (input != null) {
            return input;
        } else {
            System.out.println("ex: " + ExceptionUtils.getStackTrace(exception));
            return 123;
        }

    }

}