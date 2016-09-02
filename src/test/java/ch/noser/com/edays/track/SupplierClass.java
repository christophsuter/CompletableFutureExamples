package ch.noser.com.edays.track;

import java.util.function.Supplier;

/**
 * Created by christoph.suter on 27.08.2016.
 */
public class SupplierClass implements Supplier<Integer> {

    @Override
    public Integer get() {
        return 1;
    }
}
