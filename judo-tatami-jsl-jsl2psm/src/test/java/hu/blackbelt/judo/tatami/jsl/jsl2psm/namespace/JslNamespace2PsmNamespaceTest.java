package hu.blackbelt.judo.tatami.jsl.jsl2psm.namespace;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.TestUtils.allPsm;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslNamespace2PsmNamespaceTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/namespace";

    @Override
    protected String getTargetTestClasses() {
        return TARGET_TEST_CLASSES;
    }

    @Override
    protected String getTest() {
        return "JslNamespace2PsmNamespaceTest";
    }

    @Override
    protected Log createLog() {
        return new Slf4jLog(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testCreateModel() throws Exception {
        testName = "CreateModel";

        jslModel = parser.getModelFromStrings(
                "TestModel",
                List.of("model TestModel"));

        transform();

        final Optional<Model> lookupPSMModel = psmModelWrapper.getStreamOfPsmNamespaceModel()
                .findAny();

        assertTrue(lookupPSMModel.isPresent());
        assertThat(lookupPSMModel.get().getName(), IsEqual.equalTo(jslModel.getName()));
    }
}
