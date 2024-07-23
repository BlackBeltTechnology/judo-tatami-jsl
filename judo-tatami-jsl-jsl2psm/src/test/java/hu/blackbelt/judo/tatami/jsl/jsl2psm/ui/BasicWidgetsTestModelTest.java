package hu.blackbelt.judo.tatami.jsl.jsl2psm.ui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.BufferedSlf4jLogger;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.ui.BasicWidgetsTestModelTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicWidgetsTestModelTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/ui";

    @Override
    protected String getTargetTestClasses() {
        return TARGET_TEST_CLASSES;
    }

    @Override
    protected String getTest() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected Logger createLog() {
        return log; //new BufferedSlf4jLogger(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testBasicWidgetsTestModel() throws Exception {
        testName = "BasicWidgetsTestModel";


        jslModel = JslParser.getModelFromFiles(
                "BasicWidgetsTestModel",
                List.of(new File("src/test/resources/ui/BasicWidgetsTestModel.jsl"))
        );

        transform();
        assertTrue(true);
    }

}
