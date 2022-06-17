package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
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
public class JslStringTypeDeclaration2PsmStringTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/type/string";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/string";
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

        jslModel = JslParser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type string Name(max-length = 32, regex =\"/^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/g\")\n"
                )
        );

        transform();

        assertStringType("Name");        
        assertEquals(32, assertStringType("Name").getMaxLength());
        assertEquals("/^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/g", assertStringType("Name").getRegExp());
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type string Name(max-length = 32)\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Name name\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());

        assertFalse(assertAttribute("_Person", "name").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Person", "name").isRequired());

    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type string Name(max-length = 32)\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield required Name name\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());

        assertTrue(assertAttribute("_Person", "name").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("Person", "name").isRequired());

    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type string Name(max-length = 32)\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Name name\n" +
                        "}\n" +
                        "\n" +
                        "entity StudentPerson extends Person {\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());

        assertEquals(assertStringType("Name"), assertAttribute("_StudentPerson", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("StudentPerson", "name").getDataType());

    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type string Name(max-length = 32)\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tidentifier Name name\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());        
        assertTrue(assertAttribute("_Person", "name").isIdentifier());
    }
}
