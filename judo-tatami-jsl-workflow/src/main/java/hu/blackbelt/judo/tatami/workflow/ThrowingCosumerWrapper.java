package hu.blackbelt.judo.tatami.workflow;

import com.pivovarit.function.ThrowingConsumer;

import java.util.function.Consumer;

public class ThrowingCosumerWrapper {
    public static <T> Consumer<T> executeWrapper(boolean silent, ThrowingConsumer<T, Exception> throwingConsumer) {
        if (silent) {
            return quietConsumerWrapper(throwingConsumer);
        } else {
            return throwingConsumerWrapper(throwingConsumer);
        }
    }

    public static <T> Consumer<T> throwingConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer) {
        return i -> {
            try {
                throwingConsumer.accept(i);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static <T> Consumer<T> quietConsumerWrapper(ThrowingConsumer<T, Exception> throwingConsumer) {
        return i -> {
            try {
                throwingConsumer.accept(i);
            } catch (Exception ex) {
            }
        };
    }
}
