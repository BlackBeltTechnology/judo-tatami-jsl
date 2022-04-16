package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.TimeType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslTimeTypeDeclaration2PsmTimeTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/type/time";

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
    void testDeclaration() throws Exception {
        testName = "TestDeclaration";

        jslModel = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type time MyTime\n"
                )
        );

        transform();

        assertTimeType("MyTime");
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = parser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type time MyTime\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield MyTime arrivalTime\n" +
                        "}"
                )
        );

        transform();

        assertTimeType("MyTime");
        assertEquals(assertTimeType("MyTime"), assertAttribute("_Person", "arrivalTime").getDataType());
        assertEquals(assertTimeType("MyTime"), assertMappedTransferObjectAttribute("Person", "arrivalTime").getDataType());

        assertFalse(assertAttribute("_Person", "arrivalTime").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Person", "arrivalTime").isRequired());

    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = parser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type time MyTime\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield required MyTime arrivalTime\n" +
                        "}"
                )
        );

        transform();

        assertTimeType("MyTime");
        assertEquals(assertTimeType("MyTime"), assertAttribute("_Person", "arrivalTime").getDataType());
        assertEquals(assertTimeType("MyTime"), assertMappedTransferObjectAttribute("Person", "arrivalTime").getDataType());

        assertTrue(assertAttribute("_Person", "arrivalTime").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("Person", "arrivalTime").isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = parser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type time MyTime\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield MyTime arrivalTime\n" +
                        "}\n" +
                        "\n" +
                        "entity StudentPerson extends Person {\n" +
                        "}"
                )
        );

        transform();

        assertTimeType("MyTime");
        assertEquals(assertTimeType("MyTime"), assertAttribute("_Person", "arrivalTime").getDataType());
        assertEquals(assertTimeType("MyTime"), assertMappedTransferObjectAttribute("Person", "arrivalTime").getDataType());

        assertEquals(assertTimeType("MyTime"), assertAttribute("_StudentPerson", "arrivalTime").getDataType());
        assertEquals(assertTimeType("MyTime"), assertMappedTransferObjectAttribute("StudentPerson", "arrivalTime").getDataType());
    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = parser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type time MyTime\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tidentifier MyTime arrivalTime\n" +
                        "}"
                )
        );

        transform();

        assertTimeType("MyTime");
        assertEquals(assertTimeType("MyTime"), assertAttribute("_Person", "arrivalTime").getDataType());
        assertEquals(assertTimeType("MyTime"), assertMappedTransferObjectAttribute("Person", "arrivalTime").getDataType());
        assertTrue(assertAttribute("_Person", "arrivalTime").isIdentifier());
    }
}
