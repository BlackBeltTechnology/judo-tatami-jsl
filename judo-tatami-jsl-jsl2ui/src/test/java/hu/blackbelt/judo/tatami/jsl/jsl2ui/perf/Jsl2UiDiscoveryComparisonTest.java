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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.Jsl2UiParameter.jsl2UiParameter;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Discovery-based comparison test for JSL2UI transformation.
 *
 * Discovers .jsl model files from:
 * 1. external-model-tests.properties in classpath
 * 2. System property judo.test.discovery.basedir for ad-hoc directories
 *
 * For each model, runs both ETL and ZETA transformations, measures
 * transformation time only (not parsing), and compares output UI models
 * with ModelComparator in STRICT mode by default.
 *
 * Note: Only models with UIFrontendDeclaration produce UI output.
 * Models without frontend declarations will produce empty UI models
 * (which should still be equivalent between ETL and ZETA).
 *
 * Run with: mvn test -Pperformance -pl judo-tatami-jsl-jsl2ui -Dtest=Jsl2UiDiscoveryComparisonTest
 */
@Tag("performance")
public class Jsl2UiDiscoveryComparisonTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiDiscoveryComparisonTest.class);

    private static final String PROPERTIES_FILE = "external-model-tests.properties";
    private static final String BASEDIR_PROPERTY = "judo.test.discovery.basedir";
    private static final String TARGET_TEST_CLASSES = "target/test-classes/perf";

    private final List<TestResult> results = new ArrayList<>();

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

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

        List<DynamicTest> tests = models.values().stream()
                .map(config -> DynamicTest.dynamicTest(config.name, () -> testModel(config)))
                .collect(Collectors.toList());

        tests.add(DynamicTest.dynamicTest("== Summary ==", this::printSummary));
        return tests;
    }

    private void testModel(ModelConfig config) throws Exception {
        // Resolve files
        List<File> jslFiles = new ArrayList<>();
        File primaryFile = new File(config.path);
        if (!primaryFile.isAbsolute()) {
            primaryFile = new File(System.getProperty("user.dir"), config.path);
        }
        Assumptions.assumeTrue(primaryFile.exists(), "JSL file not found: " + primaryFile);
        jslFiles.add(primaryFile);

        // Add companion files
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

        log.info("");
        log.info("================================================================");
        log.info("Testing: {} ({})", config.name, primaryFile.getName());
        log.info("================================================================");

        // Parse JSL model once (shared, not timed)
        JslDslModel jslModel;
        try {
            jslModel = JslParser.getModelFromFiles(jslFiles);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "JSL parse failed for " + config.name + ": " + e.getMessage());
            return;
        }
        assertTrue(jslModel.isValid(), "JSL model is not valid: " + config.name);

        // ETL transformation (timed)
        UiModel etlUi = buildUiModel().name(jslModel.getName()).build();
        long etlStart = System.currentTimeMillis();
        Jsl2Ui.executeJsl2UiTransformation(jsl2UiParameter()
                .jslModel(jslModel)
                .uiModel(etlUi)
                .createTrace(false)
                .parallel(true));
        long etlTime = System.currentTimeMillis() - etlStart;

        // Re-parse for ZETA (fresh model to avoid state issues)
        JslDslModel jslModelZeta = JslParser.getModelFromFiles(jslFiles);

        // ZETA transformation (timed)
        UiModel zetaUi = buildUiModel().name(jslModelZeta.getName()).build();
        long zetaStart = System.currentTimeMillis();
        Jsl2UiZetaTransformation.builder()
                .jslModel(jslModelZeta)
                .uiModel(zetaUi)
                .defaultModelName(jslModelZeta.getName())
                .build()
                .execute();
        long zetaTime = System.currentTimeMillis() - zetaStart;

        // Count elements
        int etlElements = countElements(etlUi);
        int zetaElements = countElements(zetaUi);

        log.info("ETL:  {}ms, {} elements", etlTime, etlElements);
        log.info("ZETA: {}ms, {} elements", zetaTime, zetaElements);

        // Model comparison
        ModelComparator.ComparisonMode mode = ModelComparator.getConfiguredMode();
        log.info("Comparison mode: {}", mode);

        // Handle empty models (no frontend declarations)
        if (etlElements == 0 && zetaElements == 0) {
            log.info("Both models empty (no frontend declarations) - EQUIVALENT");
            results.add(new TestResult(config.name, etlTime, zetaTime, etlElements, zetaElements, "EQUIVALENT (empty)"));
            return;
        }

        EObject etlRoot = etlUi.getResourceSet().getResources().get(0).getContents().get(0);
        EObject zetaRoot = zetaUi.getResourceSet().getResources().get(0).getContents().get(0);

        ModelComparator.ComparisonResult result = ModelComparator.compare(etlRoot, zetaRoot, mode);

        String compStatus;
        if (result.isEquivalent()) {
            log.info("EQUIVALENT: ETL and ZETA models match");
            compStatus = "EQUIVALENT";
        } else {
            compStatus = "DIFF (" + result.getDifferenceCount() + " diffs)";
            log.warn("{} differences found:\n{}", result.getDifferenceCount(), result.getSummary());
        }

        results.add(new TestResult(config.name, etlTime, zetaTime, etlElements, zetaElements, compStatus));
    }

    private void printSummary() {
        if (results.isEmpty()) {
            log.info("No results to summarize");
            return;
        }

        log.info("");
        log.info("================================================================");
        log.info("JSL2UI PERFORMANCE SUMMARY");
        log.info("================================================================");
        log.info("");
        log.info(String.format("%-25s %8s %8s %8s %8s %s",
                "Model", "ETL(ms)", "ZETA(ms)", "ETL#", "ZETA#", "Status"));
        log.info(String.format("%-25s %8s %8s %8s %8s %s",
                "-------------------------", "--------", "--------", "--------", "--------", "----------"));

        for (TestResult r : results) {
            log.info(String.format("%-25s %8d %8d %8d %8d %s",
                    r.name.length() > 25 ? r.name.substring(0, 25) : r.name,
                    r.etlTime, r.zetaTime, r.etlElements, r.zetaElements, r.status));
        }
        log.info("================================================================");
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

    record TestResult(String name, long etlTime, long zetaTime, int etlElements, int zetaElements, String status) {}
}
