package hu.blackbelt.judo.tatami.jsl.jsl2ui.etl;

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2UiEtlEngine;
import org.slf4j.Logger;

import java.net.URI;

/**
 * ETL implementation of {@link Jsl2UiEtlEngine}: delegates to {@link Jsl2Ui#executeJsl2UiTransformation}.
 * Found via META-INF/services (plain Java) or registered by {@code Jsl2UiEtlEngineComponent} (OSGi, with the bundle's script root).
 * See change: split-etl-artifacts.
 */
public class Jsl2UiEtlEngineImpl implements Jsl2UiEtlEngine {

    private final URI scriptRoot;

    /** Scripts resolved next to this class (jar / target/classes). */
    public Jsl2UiEtlEngineImpl() {
        this(Jsl2Ui.calculateJsl2UiTransformationScriptURI());
    }

    public Jsl2UiEtlEngineImpl(URI scriptRoot) {
        this.scriptRoot = scriptRoot;
    }

    @Override
    public void execute(JslDslModel jslModel, UiModel uiModel, Logger log, boolean createTrace, boolean parallel) throws Exception {
        Jsl2Ui.executeJsl2UiTransformation(Jsl2Ui.Jsl2UiParameter.jsl2UiParameter()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .log(log)
                .scriptUri(scriptRoot)
                .createTrace(createTrace)
                .parallel(parallel));
    }
}
