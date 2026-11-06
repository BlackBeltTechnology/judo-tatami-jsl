package hu.blackbelt.judo.tatami.jsl.jsl2ui;

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import org.slf4j.Logger;

/**
 * ETL (Epsilon) JSL to UI engine, contributed by the optional {@code judo-tatami-jsl-jsl2ui-etl} add-on.
 * Jsl2UiWork looks it up via {@link hu.blackbelt.judo.tatami.core.etl.EtlEngineRegistry} in ETL/DUAL mode only.
 * See change: split-etl-artifacts.
 */
public interface Jsl2UiEtlEngine {

    /** Maven coordinates of the add-on implementing this engine (named in EtlEngineMissingException). */
    String ARTIFACT_COORDINATES = "hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2ui-etl";

    /** Runs the ETL transformation into {@code uiModel}. {@code log} may be {@code null}. */
    void execute(JslDslModel jslModel, UiModel uiModel, Logger log, boolean createTrace, boolean parallel) throws Exception;
}
