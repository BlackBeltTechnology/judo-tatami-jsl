package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslNumericTypeDeclaration2PsmNumericTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/type/numeric";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/numeric";
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
    void testDeclaration() throws Exception {
        testName = "TestDeclaration";

        jslModel = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type numeric MyNumber precision 12 scale 5\n"
                )
        );

        transform();

        assertNumericType("MyNumber");        
        assertEquals(12, assertNumericType("MyNumber").getPrecision());
        assertEquals(5, assertNumericType("MyNumber").getScale());
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = parser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type numeric Height precision 3 scale 0\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Height height\n" +
                        "}"
                )
        );

        transform();

        assertNumericType("Height");
        assertEquals(assertNumericType("Height"), assertAttribute("_Person", "height").getDataType());
        assertEquals(assertNumericType("Height"), assertMappedTransferObjectAttribute("Person", "height").getDataType());

        assertFalse(assertAttribute("_Person", "height").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Person", "height").isRequired());

    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = parser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type numeric Height precision 3 scale 0\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield required Height height\n" +
                        "}"
                )
        );

        transform();

        assertNumericType("Height");
        assertEquals(assertNumericType("Height"), assertAttribute("_Person", "height").getDataType());
        assertEquals(assertNumericType("Height"), assertMappedTransferObjectAttribute("Person", "height").getDataType());

        assertTrue(assertAttribute("_Person", "height").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("Person", "height").isRequired());

    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = parser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type numeric Height precision 3 scale 0\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Height height\n" +
                        "}\n" +
                        "\n" +
                        "entity StudentPerson extends Person {\n" +
                        "}"
                )
        );

        transform();

        assertNumericType("Height");
        assertEquals(assertNumericType("Height"), assertAttribute("_Person", "height").getDataType());
        assertEquals(assertNumericType("Height"), assertMappedTransferObjectAttribute("Person", "height").getDataType());

        assertEquals(assertNumericType("Height"), assertAttribute("_StudentPerson", "height").getDataType());
        assertEquals(assertNumericType("Height"), assertMappedTransferObjectAttribute("StudentPerson", "height").getDataType());

    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = parser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type numeric Height precision 3 scale 0\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tidentifier Height height\n" +
                        "}"
                )
        );

        transform();

        assertNumericType("Height");
        assertEquals(assertNumericType("Height"), assertAttribute("_Person", "height").getDataType());
        assertEquals(assertNumericType("Height"), assertMappedTransferObjectAttribute("Person", "height").getDataType());

        assertTrue(assertAttribute("_Person", "height").isIdentifier());
    }
}
