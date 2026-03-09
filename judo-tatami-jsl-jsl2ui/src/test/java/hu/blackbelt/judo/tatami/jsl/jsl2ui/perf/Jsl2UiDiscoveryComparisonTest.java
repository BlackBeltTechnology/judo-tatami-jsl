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
 * Discovery-based timing test for JSL2UI transformation.
 *
 * Discovers .jsl model files from:
 * 1. external-model-tests.properties in classpath
 * 2. System property judo.test.discovery.basedir for ad-hoc directories
 *
 * For each model, runs ETL transformation and measures timing.
 * No ZETA comparison (jsl2ui has no ZETA implementation yet).
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
    Collection<DynamicTest> timeEtlForJslModels() {
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
        // Resolve file
        File jslFile = new File(config.path);
        if (!jslFile.isAbsolute()) {
            jslFile = new File(System.getProperty("user.dir"), config.path);
        }
        Assumptions.assumeTrue(jslFile.exists(), "JSL file not found: " + jslFile);

        log.info("");
        log.info("================================================================");
        log.info("Testing: {} ({})", config.name, jslFile.getName());
        log.info("================================================================");

        // Parse JSL model (not timed)
        JslDslModel jslModel = JslParser.getModelFromFiles(List.of(jslFile));
        assertTrue(jslModel.isValid(), "JSL model is not valid: " + config.name);

        // ETL transformation (timed)
        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
        long etlStart = System.currentTimeMillis();
        Jsl2Ui.executeJsl2UiTransformation(jsl2UiParameter()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .createTrace(false)
                .parallel(true));
        long etlTime = System.currentTimeMillis() - etlStart;

        // Count elements
        int elements = countElements(uiModel);

        log.info("ETL: {}ms, {} elements", etlTime, elements);

        results.add(new TestResult(config.name, etlTime, elements));
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
        log.info(String.format("%-25s %8s %8s",
                "Model", "ETL(ms)", "Elements"));
        log.info(String.format("%-25s %8s %8s",
                "-------------------------", "--------", "--------"));

        for (TestResult r : results) {
            log.info(String.format("%-25s %8d %8d",
                    r.name.length() > 25 ? r.name.substring(0, 25) : r.name,
                    r.etlTime, r.elements));
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
                configs.add(new ModelConfig(name, value.trim()));
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
                    .forEach(p -> {
                        String name = p.getFileName().toString().replace(".jsl", "");
                        configs.add(new ModelConfig(name, p.toString()));
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

        ModelConfig(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }

    record TestResult(String name, long etlTime, int elements) {}
}
