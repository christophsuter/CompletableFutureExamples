package ch.noser.com.edays.track;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutExampleTest {

    @Test
    public void testCalculateScoreOfWebsiteWithTimeout() throws InterruptedException {
        CompletableFuture<Integer> scoreCF = calculateScoreOfWebsite("www.google.ch");
        timeout(scoreCF, 202)
                .whenComplete(ValidationTasks::log);

        Thread.sleep(250);
    }


    private CompletableFuture<Integer> calculateScoreOfWebsite(String url) {
        return CompletableFuture.supplyAsync(() -> url)
                .thenApplyAsync(ValidationTasks::download)
                .thenApplyAsync(ValidationTasks::parse)
                .thenApplyAsync(ValidationTasks::validate)
                .thenApplyAsync(ValidationTasks::calculateScore);
    }

    private static <A> CompletableFuture<A> timeout(CompletableFuture<A> cf, int wait) {
        Executors.newScheduledThreadPool(1).schedule(() -> {
            cf.completeExceptionally(new TimeoutException("You shall not pass"));
        }, wait, TimeUnit.MILLISECONDS);
        return cf;
    }
}
