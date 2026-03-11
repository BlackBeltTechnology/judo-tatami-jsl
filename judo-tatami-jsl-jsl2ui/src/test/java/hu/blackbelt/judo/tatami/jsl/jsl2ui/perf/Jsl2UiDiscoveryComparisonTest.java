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
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.Jsl2UiParameter.jsl2UiParameter;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance comparison test for JSL2UI transformation.
 *
 * <p>ETL runs once per model (baseline timing + reference model).
 * ZETA runs N times per model (averaged timing + comparison model).
 * Comparison uses STRICT mode by default (includes EAnnotations).
 *
 * <p>Model sources:
 * <ul>
 *   <li>{@code external-model-tests.properties} in classpath</li>
 *   <li>System property {@code judo.test.discovery.basedir} for ad-hoc directories</li>
 * </ul>
 *
 * <p>Run with:
 * <pre>
 * mvn test -Pperformance -pl judo-tatami-jsl-jsl2ui \
 *     -Dtest=Jsl2UiDiscoveryComparisonTest
 * </pre>
 */
@Tag("performance")
public class Jsl2UiDiscoveryComparisonTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiDiscoveryComparisonTest.class);

    private static final String PROPERTIES_FILE = "external-model-tests.properties";
    private static final String BASEDIR_PROPERTY = "judo.test.discovery.basedir";

    private static final int DEFAULT_ZETA_ITERATIONS = 3;
    private static final boolean DEFAULT_WARMUP = true;
    private static final String DEFAULT_COMPARISON_MODE = "STRICT";

    private final List<TestResult> results = new CopyOnWriteArrayList<>();

    private record TestResult(
            String modelName,
            long etlTimeMs,
            long zetaAvgTimeMs,
            double speedup,
            int etlOutputCount,
            int zetaOutputCount,
            String comparisonResult,
            int differenceCount
    ) {}

    @TestFactory
    Collection<DynamicTest> compareEtlAndZetaForJslModels() {
        Map<String, ModelConfig> models = new LinkedHashMap<>();

        // 1. Load from properties file
        loadModelConfigs().forEach(config -> models.put(config.name, config));

        // 2. Auto-discovery via system property
        String baseDirValue = System.getProperty(BASEDIR_PROPERTY);
        if (baseDirValue != null && !baseDirValue.isEmpty()) {
            Path baseDir = Paths.get(baseDirValue).toAbsolutePath().normalize();
            if (baseDir.toFile().isDirectory()) {
                log.info("Discovering JSL models in: {}", baseDir);
                discoverJslFiles(baseDir).forEach(config -> models.put(config.name, config));
            }
        }

        Assumptions.assumeTrue(!models.isEmpty(),
                "No models found (configure " + PROPERTIES_FILE + " or set '" + BASEDIR_PROPERTY + "')");

        int zetaIterations = getZetaIterations();
        boolean warmup = isWarmupEnabled();
        String compMode = getComparisonMode();
        log.info("Performance test config: zetaIterations={}, warmup={}, comparisonMode={}", zetaIterations, warmup, compMode);

        results.clear();
        List<DynamicTest> tests = models.values().stream()
                .map(config -> DynamicTest.dynamicTest(config.name, () -> testModel(config)))
                .collect(Collectors.toList());
        tests.add(DynamicTest.dynamicTest("== Summary ==", () -> {
            printSummary();
            writeJsonResults(Paths.get("target"));
        }));
        return tests;
    }

    private void testModel(ModelConfig config) throws Exception {
        // Resolve files
        List<File> jslFiles = resolveJslFiles(config);

        log.info("");
        log.info("================================================================");
        log.info("Testing: {} ({})", config.name, jslFiles.get(0).getName());
        log.info("================================================================");

        // Parse JSL model (not timed)
        JslDslModel jslModel;
        try {
            jslModel = JslParser.getModelFromFiles(jslFiles);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "JSL parse failed for " + config.name + ": " + e.getMessage());
            return;
        }
        assertTrue(jslModel.isValid(), "JSL model is not valid: " + config.name);

        int zetaIterations = getZetaIterations();
        boolean warmup = isWarmupEnabled();

        // Warmup phase
        if (warmup) {
            log.info("--- Warmup ---");
            JslDslModel warmupModel = JslParser.getModelFromFiles(jslFiles);
            runEtl(warmupModel);
            JslDslModel warmupModelZeta = JslParser.getModelFromFiles(jslFiles);
            runZeta(warmupModelZeta);
            log.info("Warmup complete");
        }

        // ETL: 1 run
        JslDslModel etlSource = JslParser.getModelFromFiles(jslFiles);
        long etlStart = System.currentTimeMillis();
        UiModel etlUi = runEtl(etlSource);
        long etlTime = System.currentTimeMillis() - etlStart;
        int etlElements = countElements(etlUi);

        // ZETA: N runs
        long zetaTotalTime = 0;
        int zetaElements = 0;
        UiModel zetaUi = null;
        for (int i = 0; i < zetaIterations; i++) {
            JslDslModel zetaSource = JslParser.getModelFromFiles(jslFiles);
            long zetaStart = System.currentTimeMillis();
            zetaUi = runZeta(zetaSource);
            zetaTotalTime += System.currentTimeMillis() - zetaStart;
            zetaElements = countElements(zetaUi);
        }
        long zetaAvgTime = zetaTotalTime / zetaIterations;

        // Log timing
        double speedup = zetaAvgTime > 0 ? (double) etlTime / zetaAvgTime : 0;
        log.info("ETL:  {}ms, {} elements", etlTime, etlElements);
        log.info("ZETA: {}ms avg ({} iterations), {} elements", zetaAvgTime, zetaIterations, zetaElements);
        log.info("Speedup: {}", String.format("%.2fx", speedup));

        // Handle empty models (no frontend declarations)
        if (etlElements == 0 && zetaElements == 0) {
            log.info("Both models empty (no frontend declarations) - EQUIVALENT");
            results.add(new TestResult(config.name, etlTime, zetaAvgTime, speedup, 0, 0, "EQUIVALENT", 0));
            return;
        }

        // Model comparison
        ModelComparator.ComparisonMode mode = ModelComparator.ComparisonMode.valueOf(getComparisonMode());
        log.info("Comparison mode: {}", mode);

        EObject etlRoot = etlUi.getResourceSet().getResources().get(0).getContents().get(0);
        EObject zetaRoot = zetaUi.getResourceSet().getResources().get(0).getContents().get(0);

        ModelComparator.ComparisonResult result = ModelComparator.compare(etlRoot, zetaRoot, mode);

        if (result.isEquivalent()) {
            log.info("EQUIVALENT: ETL and ZETA models match ({})", mode);
            results.add(new TestResult(config.name, etlTime, zetaAvgTime, speedup, etlElements, zetaElements, "EQUIVALENT", 0));
        } else {
            results.add(new TestResult(config.name, etlTime, zetaAvgTime, speedup, etlElements, zetaElements,
                    "DIFF", result.getDifferenceCount()));
            log.warn("{} differences found:\n{}", result.getDifferenceCount(), result.getSummary());
        }
    }

    // --- Transformation helpers ---

    private UiModel runEtl(JslDslModel jslModel) throws Exception {
        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
        Jsl2Ui.executeJsl2UiTransformation(jsl2UiParameter()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .createTrace(false));
        return uiModel;
    }

    private UiModel runZeta(JslDslModel jslModel) {
        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
        Jsl2UiZetaTransformation.builder()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .defaultModelName(jslModel.getName())
                .build()
                .execute();
        return uiModel;
    }

    // --- Configuration ---

    private static int getZetaIterations() {
        String val = System.getProperty("judo.test.zeta.iterations");
        return (val != null && !val.isEmpty()) ? Integer.parseInt(val) : DEFAULT_ZETA_ITERATIONS;
    }

    private static boolean isWarmupEnabled() {
        String val = System.getProperty("judo.test.warmup");
        return (val != null && !val.isEmpty()) ? Boolean.parseBoolean(val) : DEFAULT_WARMUP;
    }

    private static String getComparisonMode() {
        String mode = System.getProperty("judo.test.comparison.mode");
        return (mode != null && !mode.isEmpty()) ? mode : DEFAULT_COMPARISON_MODE;
    }

    // --- Model loading ---

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
        return configs;
    }

    private List<ModelConfig> discoverJslFiles(Path baseDir) {
        List<ModelConfig> configs = new ArrayList<>();
        try {
            Files.walk(baseDir)
                    .filter(p -> p.toString().endsWith(".jsl"))
                    .filter(p -> !p.toString().contains("/target/"))
                    .forEach(p -> {
                        String name = p.getFileName().toString().replace(".jsl", "");
                        configs.add(new ModelConfig(name, p.toString(), null));
                    });
        } catch (IOException e) {
            log.warn("Failed to discover models in {}: {}", baseDir, e.getMessage());
        }
        return configs;
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

    // --- Summary and reporting ---

    private void printSummary() {
        if (results.isEmpty()) {
            log.info("No results to summarize");
            return;
        }

        int passed = (int) results.stream().filter(r -> "EQUIVALENT".equals(r.comparisonResult())).count();
        int failed = (int) results.stream().filter(r -> "DIFF".equals(r.comparisonResult())).count();

        log.info("");
        log.info("================================================================");
        log.info("JSL2UI PERFORMANCE SUMMARY ({} models, {} passed, {} failed)",
                results.size(), passed, failed);
        log.info("================================================================");
        log.info(String.format("%-28s | %8s | %8s | %8s | %6s | %6s | %s",
                "Model", "ETL(ms)", "ZETA(ms)", "Speedup", "ETL#", "ZETA#", "Status"));
        log.info("-----------------------------+----------+----------+----------+--------+--------+-----------");

        long totalEtl = 0;
        long totalZeta = 0;
        for (TestResult r : results) {
            totalEtl += r.etlTimeMs();
            totalZeta += r.zetaAvgTimeMs();
            String speedupStr = r.speedup() >= 1.0
                    ? String.format("%.2fx", r.speedup())
                    : String.format("%.2fx SLOW", r.speedup());
            log.info(String.format("%-28s | %8d | %8d | %8s | %6d | %6d | %s",
                    truncate(r.modelName(), 28), r.etlTimeMs(), r.zetaAvgTimeMs(),
                    speedupStr, r.etlOutputCount(), r.zetaOutputCount(), r.comparisonResult()));
        }

        double avgSpeedup = totalZeta > 0 ? (double) totalEtl / totalZeta : 0;
        log.info("-----------------------------+----------+----------+----------+--------+--------+-----------");
        log.info(String.format("%-28s | %8d | %8d | %8s | %6s | %6s | %d/%d PASS",
                "TOTAL", totalEtl, totalZeta,
                String.format("%.2fx", avgSpeedup), "", "", passed, results.size()));
        log.info("================================================================");
    }

    private void writeJsonResults(Path targetDir) {
        if (results.isEmpty()) return;

        try {
            Files.createDirectories(targetDir);
            Path jsonFile = targetDir.resolve("comparison-results.json");

            int passed = (int) results.stream().filter(r -> "EQUIVALENT".equals(r.comparisonResult())).count();
            int failed = (int) results.stream().filter(r -> "DIFF".equals(r.comparisonResult())).count();
            long totalEtl = results.stream().mapToLong(TestResult::etlTimeMs).sum();
            long totalZeta = results.stream().mapToLong(TestResult::zetaAvgTimeMs).sum();
            double avgSpeedup = totalZeta > 0 ? (double) totalEtl / totalZeta : 0;

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"module\": \"jsl2ui\",\n");
            sb.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
            sb.append("  \"comparisonMode\": \"").append(getComparisonMode()).append("\",\n");
            sb.append("  \"zetaIterations\": ").append(getZetaIterations()).append(",\n");
            sb.append("  \"warmup\": ").append(isWarmupEnabled()).append(",\n");
            sb.append("  \"results\": [\n");

            for (int i = 0; i < results.size(); i++) {
                TestResult r = results.get(i);
                sb.append("    {\n");
                sb.append("      \"model\": \"").append(escapeJson(r.modelName())).append("\",\n");
                sb.append("      \"etlTimeMs\": ").append(r.etlTimeMs()).append(",\n");
                sb.append("      \"zetaAvgTimeMs\": ").append(r.zetaAvgTimeMs()).append(",\n");
                sb.append("      \"speedup\": ").append(String.format("%.2f", r.speedup())).append(",\n");
                sb.append("      \"etlOutputCount\": ").append(r.etlOutputCount()).append(",\n");
                sb.append("      \"zetaOutputCount\": ").append(r.zetaOutputCount()).append(",\n");
                sb.append("      \"comparisonResult\": \"").append(r.comparisonResult()).append("\",\n");
                sb.append("      \"differenceCount\": ").append(r.differenceCount()).append("\n");
                sb.append("    }").append(i < results.size() - 1 ? "," : "").append("\n");
            }

            sb.append("  ],\n");
            sb.append("  \"summary\": {\n");
            sb.append("    \"totalModels\": ").append(results.size()).append(",\n");
            sb.append("    \"passed\": ").append(passed).append(",\n");
            sb.append("    \"failed\": ").append(failed).append(",\n");
            sb.append("    \"totalEtlTimeMs\": ").append(totalEtl).append(",\n");
            sb.append("    \"totalZetaTimeMs\": ").append(totalZeta).append(",\n");
            sb.append("    \"avgSpeedup\": ").append(String.format("%.2f", avgSpeedup)).append("\n");
            sb.append("  }\n");
            sb.append("}\n");

            Files.writeString(jsonFile, sb.toString(), StandardCharsets.UTF_8);
            log.info("Results written to: {}", jsonFile);
        } catch (IOException e) {
            log.warn("Failed to write JSON results: {}", e.getMessage());
        }
    }

    // --- Utilities ---

    private static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 2) + "..";
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
