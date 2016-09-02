package ch.noser.com.edays.track.lambda;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Created by christoph.suter on 27.08.2016.
 */
public class LambdaRecap {

    @Test
    public void testSupplierExamples() {

        //Class implementing interface Supplier
        Supplier<Integer> integerSupplier = new SupplierClass();
        CompletableFuture<Integer> aCF1 = CompletableFuture.supplyAsync(integerSupplier);

        //Lambda Expression
        CompletableFuture<Integer> aCF2 = CompletableFuture.supplyAsync(() -> aSupplier());
        CompletableFuture<Integer> aCF3 = CompletableFuture.supplyAsync(() -> 1);
        CompletableFuture<Integer> aCF4 = CompletableFuture.supplyAsync(() -> {
            return aSupplier();
        });
        Supplier<Integer> aSupplierLambdaStyle = () -> aSupplier();
        CompletableFuture<Integer> aCF5 = CompletableFuture.supplyAsync(aSupplierLambdaStyle);

        //Method References
        CompletableFuture<Integer> aCF6 = CompletableFuture.supplyAsync(this::aSupplier);
        CompletableFuture<Integer> aCF7 = CompletableFuture.supplyAsync(LambdaRecap::aStaticSupplier);
    }


    public Integer aSupplier() {
        return 1;
    }

    public static Integer aStaticSupplier() {
        return 1;
    }

    public Integer aFunction(String str) {
        return Integer.valueOf(str);
    }

    public void aConsumer(Integer integer) {
        System.out.println("Consumed: " + integer);
    }
}
