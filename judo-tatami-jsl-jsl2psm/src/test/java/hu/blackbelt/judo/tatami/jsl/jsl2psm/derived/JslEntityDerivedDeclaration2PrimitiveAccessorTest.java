package hu.blackbelt.judo.tatami.jsl.jsl2psm.derived;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEntityDerivedDeclaration2PrimitiveAccessorTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/derived/primitiveAccessor";

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
    void testPrimitiveDerivedDeclarationModel() throws Exception {
        testName = "TestPrimitiveDerivedDeclarationModel";

        jslModel = parser.getModelFromStrings(
                "PrimitiveDerivedDeclarationModel",
                List.of("model PrimitiveDerivedDeclarationModel\n" +
                        "\n" +
                        "type numeric Integer precision 9 scale 0\n" +
                        "entity Lead {\n" +
                        "  field Integer value\n" +
                        "}\n" +
                        "entity Test {\n" +
                        "  relation Lead[] leads\n" +
                        "  derived Integer value = self.leads!count()\n" +
                        "}\n" +
                        "entity TestExtended extends Test {\n" +
                        "}\n"
                		)
        );

        transform();

        assertDataProperty("_Test", "value");
        assertTrue(assertDataProperty("_Test", "value").isPrimitive());
        assertEquals("self.leads!count()", assertDataProperty("_Test", "value").getGetterExpression().getExpression());
        
        assertMappedTransferObjectAttribute("Test", "value");
        assertEquals(assertDataProperty("_Test", "value"), assertMappedTransferObjectAttribute("Test", "value").getBinding());

        assertMappedTransferObjectAttribute("TestExtended", "value");
        assertEquals(assertDataProperty("_TestExtended", "value"), assertMappedTransferObjectAttribute("Test", "value").getBinding());

    }
}
