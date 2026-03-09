package hu.blackbelt.judo.tatami.jsl.jsl2psm.perf;

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
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmZetaTransformation;
import hu.blackbelt.judo.tatami.test.util.ModelComparator;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Realistic performance test using generated JSL models.
 *
 * Generates a 70-entity model matching RackInspect characteristics,
 * runs warmup (1 ETL + 1 ZETA on fresh models), then a single measured
 * run (1 ETL + 1 ZETA on fresh models), compares outputs with
 * ModelComparator in STRICT mode, and reports timing/throughput/speedup.
 *
 * Run with: mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm -Dtest=Jsl2PsmRealisticPerformanceTest
 */
@Tag("performance")
public class Jsl2PsmRealisticPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2PsmRealisticPerformanceTest.class);

    private static final int ENTITY_COUNT = 70;

    @Test
    void generatorProducesValidModel() throws Exception {
        for (int size : new int[]{20, 70, 100}) {
            String jsl = RealisticJslModelGenerator.generate(size);
            JslDslModel model = JslParser.getModelFromStrings(
                    RealisticJslModelGenerator.getModelName(), List.of(jsl));
            assertTrue(model.isValid(), "Generated model with " + size + " entities is not valid");
            log.info("Generator validation: {} entities -> valid model, JSL length: {} chars",
                    size, jsl.length());
        }
    }

    @Test
    void realisticPerformanceComparison() throws Exception {
        String jsl = RealisticJslModelGenerator.generate(ENTITY_COUNT);

        log.info("");
        log.info("================================================================");
        log.info("REALISTIC PERFORMANCE TEST ({} entities)", ENTITY_COUNT);
        log.info("================================================================");
        log.info("Generated JSL: {} chars", jsl.length());

        // --- Warmup run ---
        log.info("Warmup: running ETL + ZETA on fresh models...");
        {
            JslDslModel warmupJsl = JslParser.getModelFromStrings(
                    RealisticJslModelGenerator.getModelName(), List.of(jsl));
            PsmModel warmupPsm = buildPsmModel().build();
            Jsl2Psm.executeJsl2PsmTransformation(Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter()
                    .jslModel(warmupJsl)
                    .psmModel(warmupPsm)
                    .createTrace(false)
                    .parallel(true)
                    .useCache(true)
                    .generateBehaviours(true));
        }
        {
            JslDslModel warmupJsl = JslParser.getModelFromStrings(
                    RealisticJslModelGenerator.getModelName(), List.of(jsl));
            PsmModel warmupPsm = buildPsmModel().build();
            Jsl2PsmZetaTransformation.builder()
                    .jslModel(warmupJsl)
                    .psmModel(warmupPsm)
                    .defaultModelName(warmupJsl.getName())
                    .generateBehaviours(true)
                    .build()
                    .execute();
        }
        log.info("Warmup complete.");

        // --- Measured run: ETL ---
        JslDslModel etlJsl = JslParser.getModelFromStrings(
                RealisticJslModelGenerator.getModelName(), List.of(jsl));
        PsmModel etlPsm = buildPsmModel().build();
        long etlStart = System.currentTimeMillis();
        Jsl2Psm.executeJsl2PsmTransformation(Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter()
                .jslModel(etlJsl)
                .psmModel(etlPsm)
                .createTrace(false)
                .parallel(true)
                .useCache(true)
                .generateBehaviours(true));
        long etlTime = System.currentTimeMillis() - etlStart;

        // --- Measured run: ZETA ---
        JslDslModel zetaJsl = JslParser.getModelFromStrings(
                RealisticJslModelGenerator.getModelName(), List.of(jsl));
        PsmModel zetaPsm = buildPsmModel().build();
        long zetaStart = System.currentTimeMillis();
        Jsl2PsmZetaTransformation.builder()
                .jslModel(zetaJsl)
                .psmModel(zetaPsm)
                .defaultModelName(zetaJsl.getName())
                .generateBehaviours(true)
                .build()
                .execute();
        long zetaTime = System.currentTimeMillis() - zetaStart;

        // --- Count elements ---
        int etlElements = countElements(etlPsm);
        int zetaElements = countElements(zetaPsm);

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

        EObject etlRoot = etlPsm.getResourceSet().getResources().get(0).getContents().get(0);
        EObject zetaRoot = zetaPsm.getResourceSet().getResources().get(0).getContents().get(0);

        ModelComparator.ComparisonResult result = ModelComparator.compare(etlRoot, zetaRoot, mode);

        if (result.isEquivalent()) {
            log.info("EQUIVALENT: ETL and ZETA models match");
        } else {
            log.warn("{} differences found:\n{}", result.getDifferenceCount(), result.getSummary());
            fail("ETL and ZETA models differ:\n" + result.getDetailedReport());
        }
    }

    private int countElements(PsmModel model) {
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
