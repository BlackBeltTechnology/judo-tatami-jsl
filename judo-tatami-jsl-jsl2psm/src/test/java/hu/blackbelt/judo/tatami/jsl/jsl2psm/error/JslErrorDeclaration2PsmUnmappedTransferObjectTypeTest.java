package hu.blackbelt.judo.tatami.jsl.jsl2psm.error;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.type.FlatPrimitiveType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslErrorDeclaration2PsmUnmappedTransferObjectTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/error";

    @Override
    protected String getTargetTestClasses() {
        return TARGET_TEST_CLASSES;
    }

    @Override
    protected String getTest() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected Log createLog() {
        return new BufferedSlf4jLogger(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testCreateErrorType() throws Exception {
        testName = "ErrorTypeCreateModel";

        jslModel = JslParser.getModelFromFiles(
                "ErrorTypeCreateModel",
                List.of(new File("src/test/resources/error/ErrorTestModel.jsl"))
        );

        transform();

        assertEquals(3, getUnmappedTransferObjectTypes().size());

        assertUnmappedTransferObject("MyError");
        assertUnmappedTransferObject("OtherError");
        assertUnmappedTransferObject("YetAnotherError");

        assertUnmappedTransferObjectAttribute("OtherError", "code");
        assertTrue(assertUnmappedTransferObjectAttribute("MyError", "code").isRequired());
        assertEquals(assertNumericType("Integer"), assertUnmappedTransferObjectAttribute("MyError", "code").getDataType());

        assertErrorData("MyError", "code", true, () -> assertNumericType("Integer"));
        assertErrorData("MyError", "msg", false, () -> assertStringType("String"));

        assertErrorData("OtherError", "code", true, () -> assertNumericType("Integer"));
        assertErrorData("OtherError", "msg", false, () -> assertStringType("String"));
        assertErrorData("OtherError", "other", false, () -> assertStringType("String"));

        assertErrorData("YetAnotherError", "code", true, () -> assertNumericType("Integer"));
        assertErrorData("YetAnotherError", "msg", false, () -> assertStringType("String"));
        assertErrorData("YetAnotherError", "other", false, () -> assertStringType("String"));
        assertErrorData("YetAnotherError", "someField", false, () -> assertStringType("String"));
    }

    private void assertErrorData(String toName, String attrName, boolean isRequired, Supplier<? extends FlatPrimitiveType> call) {
        assertUnmappedTransferObjectAttribute(toName, attrName);
        assertEquals(isRequired, assertUnmappedTransferObjectAttribute(toName, attrName).isRequired());
        assertEquals(call.get(), assertUnmappedTransferObjectAttribute(toName, attrName).getDataType());
    }
}
