package hu.blackbelt.judo.tatami.jsl.jsl2ui.dual;

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
import hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiZetaTransformation;
import hu.blackbelt.judo.tatami.test.util.ModelComparator;
import hu.blackbelt.judo.tatami.test.util.ModelComparator.ComparisonMode;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.Jsl2UiParameter.jsl2UiParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.executeJsl2UiTransformation;
import static org.junit.jupiter.api.Assertions.*;

/**
 * External model dual transformation test with STRICT comparison and performance measurement.
 *
 * Loads all JSL models from {@code external-model-tests.properties}, runs both ETL and ZETA
 * transformations, compares results in STRICT mode, and reports performance (timing + speedup).
 */
public class Jsl2UiExternalDualComparisonTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiExternalDualComparisonTest.class);

    private static final String PROPERTIES_FILE = "external-model-tests.properties";

    private final List<TestResult> results = new CopyOnWriteArrayList<>();

    private record TestResult(
            String modelName,
            long etlTimeMs,
            long zetaTimeMs,
            double speedup,
            int etlElements,
            int zetaElements,
            String status
    ) {}

    @TestFactory
    Collection<DynamicTest> compareExternalModelsStrictWithPerformance() {
        List<ModelConfig> configs = loadModelConfigs();
        Assumptions.assumeTrue(!configs.isEmpty(),
                "No models found in " + PROPERTIES_FILE);

        results.clear();
        List<DynamicTest> tests = configs.stream()
                .map(config -> DynamicTest.dynamicTest(config.name, () -> testModel(config)))
                .collect(Collectors.toList());

        tests.add(DynamicTest.dynamicTest("== Summary ==", this::printSummary));
        return tests;
    }

    private void testModel(ModelConfig config) throws Exception {
        List<File> jslFiles = resolveJslFiles(config);

        JslDslModel jslModel;
        try {
            jslModel = JslParser.getModelFromFiles(jslFiles);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "JSL parse failed for " + config.name + ": " + e.getMessage());
            return;
        }
        assertTrue(jslModel.isValid(), "JSL model is not valid: " + config.name);

        // ETL (timed)
        JslDslModel etlSource = JslParser.getModelFromFiles(jslFiles);
        long etlStart = System.currentTimeMillis();
        UiModel etlUi = executeEtl(etlSource);
        long etlTime = System.currentTimeMillis() - etlStart;
        int etlElements = countElements(etlUi);

        // ZETA (timed)
        JslDslModel zetaSource = JslParser.getModelFromFiles(jslFiles);
        long zetaStart = System.currentTimeMillis();
        UiModel zetaUi = executeZeta(zetaSource);
        long zetaTime = System.currentTimeMillis() - zetaStart;
        int zetaElements = countElements(zetaUi);

        double speedup = zetaTime > 0 ? (double) etlTime / zetaTime : 0;

        // Handle empty models
        if (etlElements == 0 && zetaElements == 0) {
            log.info("[{}] Both empty — EQUIVALENT (ETL={}ms, ZETA={}ms)", config.name, etlTime, zetaTime);
            results.add(new TestResult(config.name, etlTime, zetaTime, speedup, 0, 0, "EQUIVALENT"));
            return;
        }

        // STRICT comparison
        EObject etlRoot = etlUi.getResourceSet().getResources().get(0).getContents().get(0);
        EObject zetaRoot = zetaUi.getResourceSet().getResources().get(0).getContents().get(0);

        ModelComparator.ComparisonResult result = ModelComparator.compare(etlRoot, zetaRoot, ComparisonMode.STRICT);

        if (result.isEquivalent()) {
            log.info("[{}] EQUIVALENT — ETL={}ms ({}el), ZETA={}ms ({}el), speedup={}", config.name,
                    etlTime, etlElements, zetaTime, zetaElements, String.format("%.2fx", speedup));
            results.add(new TestResult(config.name, etlTime, zetaTime, speedup, etlElements, zetaElements, "EQUIVALENT"));
        } else {
            log.error("[{}] {} differences — ETL={}ms ({}el), ZETA={}ms ({}el)",
                    config.name, result.getDifferenceCount(), etlTime, etlElements, zetaTime, zetaElements);
            results.add(new TestResult(config.name, etlTime, zetaTime, speedup, etlElements, zetaElements,
                    "DIFF(" + result.getDifferenceCount() + ")"));
            String report = result.getDifferenceList().stream().map(d -> "  " + d.describe()).collect(Collectors.joining("\n"));
            fail("STRICT comparison failed for " + config.name + ":\n"
                    + result.getDifferenceCount() + " difference(s):\n" + report);
        }
    }

    // --- Transformation helpers ---

    private UiModel executeEtl(JslDslModel jslModel) throws Exception {
        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
        executeJsl2UiTransformation(jsl2UiParameter()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .createTrace(false));
        return uiModel;
    }

    private UiModel executeZeta(JslDslModel jslModel) {
        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
        Jsl2UiZetaTransformation.builder()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .defaultModelName(jslModel.getName())
                .build()
                .execute();
        return uiModel;
    }

    // --- Model loading ---

    private List<ModelConfig> loadModelConfigs() {
        List<ModelConfig> configs = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) {
                log.info("No {} found in classpath", PROPERTIES_FILE);
                return configs;
            }
            Properties props = new Properties();
            props.load(is);
            for (String name : props.stringPropertyNames()) {
                String value = props.getProperty(name);
                configs.add(ModelConfig.parse(name, value));
            }
        } catch (IOException e) {
            log.warn("Failed to load {}: {}", PROPERTIES_FILE, e.getMessage());
        }
        configs.sort(Comparator.comparing(c -> c.name));
        return configs;
    }

    private List<File> resolveJslFiles(ModelConfig config) {
        List<File> jslFiles = new ArrayList<>();
        File primaryFile = new File(config.path);
        if (!primaryFile.isAbsolute()) {
            primaryFile = new File(System.getProperty("user.dir"), config.path);
        }
        Assumptions.assumeTrue(primaryFile.exists(), "JSL file not found: " + primaryFile);
        jslFiles.add(primaryFile);

        if (config.companions != null) {
            for (String companion : config.companions.split(",")) {
                File compFile = new File(companion.trim());
                if (!compFile.isAbsolute()) {
                    compFile = new File(System.getProperty("user.dir"), companion.trim());
                }
                if (compFile.exists()) {
                    jslFiles.add(compFile);
                }
            }
        }
        return jslFiles;
    }

    // --- Element counting ---

    private int countElements(UiModel model) {
        int count = 0;
        for (Resource resource : model.getResourceSet().getResources()) {
            var iter = resource.getAllContents();
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
        }
        return count;
    }

    // --- Summary ---

    private void printSummary() {
        if (results.isEmpty()) {
            log.info("No results to summarize");
            return;
        }

        int passed = (int) results.stream().filter(r -> "EQUIVALENT".equals(r.status())).count();
        int failed = results.size() - passed;
        long totalEtl = results.stream().mapToLong(TestResult::etlTimeMs).sum();
        long totalZeta = results.stream().mapToLong(TestResult::zetaTimeMs).sum();
        double avgSpeedup = totalZeta > 0 ? (double) totalEtl / totalZeta : 0;

        log.info("");
        log.info("================================================================");
        log.info("JSL2UI STRICT DUAL COMPARISON ({} models, {} passed, {} failed)",
                results.size(), passed, failed);
        log.info("================================================================");
        log.info(String.format("%-35s | %7s | %7s | %7s | %6s | %6s | %s",
                "Model", "ETL ms", "ZETA ms", "Speedup", "ETL#", "ZETA#", "Status"));
        log.info("------------------------------------+---------+---------+---------+--------+--------+-----------");

        for (TestResult r : results) {
            String speedupStr = r.speedup() >= 1.0
                    ? String.format("%.1fx", r.speedup())
                    : String.format("%.1fx", r.speedup());
            log.info(String.format("%-35s | %7d | %7d | %7s | %6d | %6d | %s",
                    truncate(r.modelName(), 35), r.etlTimeMs(), r.zetaTimeMs(),
                    speedupStr, r.etlElements(), r.zetaElements(), r.status()));
        }

        log.info("------------------------------------+---------+---------+---------+--------+--------+-----------");
        log.info(String.format("%-35s | %7d | %7d | %7s | %6s | %6s | %d/%d PASS",
                "TOTAL", totalEtl, totalZeta,
                String.format("%.1fx", avgSpeedup), "", "", passed, results.size()));
        log.info("================================================================");
    }

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 2) + "..";
    }

    static class ModelConfig {
        final String name;
        final String path;
        final String companions;

        ModelConfig(String name, String path, String companions) {
            this.name = name;
            this.path = path;
            this.companions = companions;
        }

        static ModelConfig parse(String name, String value) {
            String[] parts = value.split(";");
            String path = parts[0].trim();
            String companions = null;
            for (int i = 1; i < parts.length; i++) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length == 2 && "companions".equals(kv[0].trim())) {
                    companions = kv[1].trim();
                }
            }
            return new ModelConfig(name, path, companions);
        }
    }
}
