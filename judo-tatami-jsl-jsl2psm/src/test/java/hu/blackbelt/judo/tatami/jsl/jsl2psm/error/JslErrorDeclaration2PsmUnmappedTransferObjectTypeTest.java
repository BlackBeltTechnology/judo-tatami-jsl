package hu.blackbelt.judo.tatami.jsl.jsl2psm.error;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
        return new Slf4jLog(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testCreateErrorType() throws Exception {
        testName = "TestCreateErrorType";

        jslModel = parser.getModelFromStrings(
                "ErrorTypeCreateModel",
                List.of("model ErrorTypeCreateModel\n" +
                        "\n" +
                        "type numeric Integer precision 9  scale 0\n" +
                        "type string String max-length 128\n" +
                        "\n" +
                        "error MyError {\n" +
                        "\tfield required Integer code\n" +
                        "\tfield String msg\n" +
                        "}"
                )
        );

        transform();

        assertEquals(1, getUnmappedTransferObjectTypes().size());

        assertUnmappedTransferObject("MyError");

        assertUnmappedTransferObjectAttribute("MyError", "code");
        assertTrue(assertUnmappedTransferObjectAttribute("MyError", "code").isRequired());
        assertEquals(assertNumericType("Integer"), assertUnmappedTransferObjectAttribute("MyError", "code").getDataType());

        assertUnmappedTransferObjectAttribute("MyError", "msg");
        assertFalse(assertUnmappedTransferObjectAttribute("MyError", "msg").isRequired());
        assertEquals(assertStringType("String"), assertUnmappedTransferObjectAttribute("MyError", "msg").getDataType());

    }
}
