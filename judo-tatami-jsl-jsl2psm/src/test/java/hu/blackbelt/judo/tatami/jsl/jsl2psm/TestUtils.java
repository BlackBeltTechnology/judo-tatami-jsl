package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.judo.meta.psm.runtime.PsmModel;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TestUtils {
    public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    public static <T> Stream<T> allPsm(PsmModel psmModel) {
        return asStream((Iterator<T>) psmModel.getResourceSet().getAllContents(), false);
    }

    public static <T> Stream<T> allPsm(PsmModel psmModel, final Class<T> clazz) {
        return allPsm(psmModel).filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
