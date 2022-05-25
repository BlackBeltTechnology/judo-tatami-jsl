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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslTimestampTypeDeclaration2PsmTimestampTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/timestamp";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/timestamp";
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
                        "type timestamp Timestamp\n"
                )
        );

        transform();

        assertTimestampType("Timestamp");        
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type timestamp Timestamp\n" +
                        "\n" +
                        "entity Email {\n" +
                        "\tfield Timestamp receivedAt\n" +
                        "}"
                )
        );

        transform();
        
        assertTimestampType("Timestamp");
        assertEquals(assertTimestampType("Timestamp"), assertAttribute("_Email", "receivedAt").getDataType());
        assertEquals(assertTimestampType("Timestamp"), assertMappedTransferObjectAttribute("Email", "receivedAt").getDataType());

        assertFalse(assertAttribute("_Email", "receivedAt").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Email", "receivedAt").isRequired());

    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type timestamp Timestamp\n" +
                        "\n" +
                        "entity Email {\n" +
                        "\tfield required Timestamp receivedAt\n" +
                        "}"
                )
        );

        transform();

        assertTimestampType("Timestamp");
        assertEquals(assertTimestampType("Timestamp"), assertAttribute("_Email", "receivedAt").getDataType());
        assertEquals(assertTimestampType("Timestamp"), assertMappedTransferObjectAttribute("Email", "receivedAt").getDataType());

        assertTrue(assertAttribute("_Email", "receivedAt").isRequired());        
        assertTrue(assertMappedTransferObjectAttribute("Email", "receivedAt").isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type timestamp Timestamp\n" +
                        "\n" +
                        "entity Email {\n" +
                        "\tfield Timestamp receivedAt\n" +
                        "}\n" +
                        "\n" +
                        "entity ImportantEmail extends Email {\n" +
                        "}"
                )
        );

        transform();

        assertTimestampType("Timestamp");
        assertEquals(assertTimestampType("Timestamp"), assertAttribute("_Email", "receivedAt").getDataType());
        assertEquals(assertTimestampType("Timestamp"), assertMappedTransferObjectAttribute("Email", "receivedAt").getDataType());        
        assertEquals(assertTimestampType("Timestamp"), assertAttribute("_ImportantEmail", "receivedAt").getDataType());
        assertEquals(assertTimestampType("Timestamp"), assertMappedTransferObjectAttribute("ImportantEmail", "receivedAt").getDataType());
    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

    	jslModel = JslParser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type timestamp Timestamp\n" +
                        "\n" +
                        "entity Email {\n" +
                        "\tidentifier Timestamp receivedAt\n" +
                        "}"
                )
        );

        transform();

        assertTimestampType("Timestamp");
        assertEquals(assertTimestampType("Timestamp"), assertAttribute("_Email", "receivedAt").getDataType());
        assertEquals(assertTimestampType("Timestamp"), assertMappedTransferObjectAttribute("Email", "receivedAt").getDataType());        
        assertTrue(assertAttribute("_Email", "receivedAt").isIdentifier());
    }
}
