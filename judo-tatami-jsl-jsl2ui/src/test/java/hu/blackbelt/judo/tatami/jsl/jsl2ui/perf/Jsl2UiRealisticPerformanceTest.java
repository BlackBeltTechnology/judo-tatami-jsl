package hu.blackbelt.judo.tatami.jsl.jsl2ui.perf;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiZetaTransformation;
import hu.blackbelt.judo.tatami.test.util.ModelComparator;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.Jsl2UiParameter.jsl2UiParameter;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Realistic performance test using generated JSL models with UI declarations.
 *
 * Generates a 20-entity model with views, rows, and frontends,
 * runs warmup, then a measured run comparing ETL and ZETA.
 *
 * Run with: mvn test -Pperformance -pl judo-tatami-jsl-jsl2ui -Dtest=Jsl2UiRealisticPerformanceTest
 */
@Tag("performance")
public class Jsl2UiRealisticPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiRealisticPerformanceTest.class);

    private static final int ENTITY_COUNT = 20;

    @Test
    void generatorProducesValidModel() throws Exception {
        for (int size : new int[]{5, 20, 50}) {
            String jsl = RealisticJslUiModelGenerator.generate(size);
            JslDslModel model = JslParser.getModelFromStrings(
                    RealisticJslUiModelGenerator.getModelName(), List.of(jsl));
            assertTrue(model.isValid(), "Generated model with " + size + " entities is not valid");
            log.info("Generator validation: {} entities -> valid model, JSL length: {} chars",
                    size, jsl.length());
        }
    }

    @Test
    void realisticPerformanceComparison() throws Exception {
        String jsl = RealisticJslUiModelGenerator.generate(ENTITY_COUNT);

        log.info("");
        log.info("================================================================");
        log.info("JSL2UI REALISTIC PERFORMANCE TEST ({} entities)", ENTITY_COUNT);
        log.info("================================================================");
        log.info("Generated JSL: {} chars", jsl.length());

        // --- Warmup run ---
        log.info("Warmup: running ETL + ZETA on fresh models...");
        {
            JslDslModel warmupJsl = JslParser.getModelFromStrings(
                    RealisticJslUiModelGenerator.getModelName(), List.of(jsl));
            UiModel warmupUi = buildUiModel().name(warmupJsl.getName()).build();
            Jsl2Ui.executeJsl2UiTransformation(jsl2UiParameter()
                    .jslModel(warmupJsl)
                    .uiModel(warmupUi)
                    .createTrace(false)
                    .parallel(true));
        }
        {
            JslDslModel warmupJsl = JslParser.getModelFromStrings(
                    RealisticJslUiModelGenerator.getModelName(), List.of(jsl));
            UiModel warmupUi = buildUiModel().name(warmupJsl.getName()).build();
            Jsl2UiZetaTransformation.builder()
                    .jslModel(warmupJsl)
                    .uiModel(warmupUi)
                    .defaultModelName(warmupJsl.getName())
                    .build()
                    .execute();
        }
        log.info("Warmup complete.");

        // --- Measured run: ETL ---
        JslDslModel etlJsl = JslParser.getModelFromStrings(
                RealisticJslUiModelGenerator.getModelName(), List.of(jsl));
        UiModel etlUi = buildUiModel().name(etlJsl.getName()).build();
        long etlStart = System.currentTimeMillis();
        Jsl2Ui.executeJsl2UiTransformation(jsl2UiParameter()
                .jslModel(etlJsl)
                .uiModel(etlUi)
                .createTrace(false)
                .parallel(true));
        long etlTime = System.currentTimeMillis() - etlStart;

        // --- Measured run: ZETA ---
        JslDslModel zetaJsl = JslParser.getModelFromStrings(
                RealisticJslUiModelGenerator.getModelName(), List.of(jsl));
        UiModel zetaUi = buildUiModel().name(zetaJsl.getName()).build();
        long zetaStart = System.currentTimeMillis();
        Jsl2UiZetaTransformation.builder()
                .jslModel(zetaJsl)
                .uiModel(zetaUi)
                .defaultModelName(zetaJsl.getName())
                .build()
                .execute();
        long zetaTime = System.currentTimeMillis() - zetaStart;

        // --- Count elements ---
        int etlElements = countElements(etlUi);
        int zetaElements = countElements(zetaUi);

        // --- Report ---
        log.info("");
        log.info("================================================================");
        log.info("RESULTS ({} entities)", ENTITY_COUNT);
        log.info("================================================================");
        log.info("ETL:          {}ms, {} elements", etlTime, etlElements);
        log.info("ZETA:         {}ms, {} elements", zetaTime, zetaElements);
        if (etlTime > 0) {
            log.info("ETL throughput:  {} elements/sec", (long) etlElements * 1000 / etlTime);
        }
        if (zetaTime > 0) {
            log.info("ZETA throughput: {} elements/sec", (long) zetaElements * 1000 / zetaTime);
        }
        if (zetaTime > 0 && etlTime > 0) {
            double speedup = (double) etlTime / zetaTime;
            log.info("Speedup:      {}x (ZETA vs ETL)", String.format("%.2f", speedup));
        }
        log.info("================================================================");

        // --- Model comparison ---
        ModelComparator.ComparisonMode mode = ModelComparator.getConfiguredMode();
        log.info("Comparison mode: {}", mode);

        if (etlElements == 0 && zetaElements == 0) {
            log.info("Both models empty - EQUIVALENT");
            return;
        }

        EObject etlRoot = etlUi.getResourceSet().getResources().get(0).getContents().get(0);
        EObject zetaRoot = zetaUi.getResourceSet().getResources().get(0).getContents().get(0);

        ModelComparator.ComparisonResult result = ModelComparator.compare(etlRoot, zetaRoot, mode);

        if (result.isEquivalent()) {
            log.info("EQUIVALENT: ETL and ZETA models match");
        } else {
            log.warn("{} differences found:\n{}", result.getDifferenceCount(), result.getSummary());
            fail("ETL and ZETA UI models differ:\n" + result.getDetailedReport());
        }
    }

    private int countElements(UiModel model) {
        int count = 0;
        for (var resource : model.getResourceSet().getResources()) {
            var iter = resource.getAllContents();
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
        }
        return count;
    }
}
