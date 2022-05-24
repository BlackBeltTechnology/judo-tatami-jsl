package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEnumDeclaration2PsmEnumerationTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/enumeration";

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

        jslModel = JslParser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "enum LeadStatus {\n" +
                        "\tOPPORTUNITY = 0\n" +
                        "\tLEAD = 1\n" +
                        "\tPROJECT = 2\n" +
                        "}"
                )
        );

        transform();

        assertEnumerationType("LeadStatus");

        assertEquals(
        		ImmutableSet.of("OPPORTUNITY", "LEAD", "PROJECT"), 
        		assertEnumerationType("LeadStatus").getMembers().stream().map(m -> m.getName()).collect(Collectors.toSet())
		);

        assertEquals(0, assertEnumerationMember("LeadStatus", "OPPORTUNITY").getOrdinal());
        assertEquals(1, assertEnumerationMember("LeadStatus", "LEAD").getOrdinal());
        assertEquals(2, assertEnumerationMember("LeadStatus", "PROJECT").getOrdinal());
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "enum LeadStatus {\n" +
                        "\tOPPORTUNITY = 0\n" +
                        "\tLEAD = 1\n" +
                        "\tPROJECT = 2\n" +
                        "}\n" +
                        "\n" +
                        "entity Lead {\n" +
                        "\tfield LeadStatus status\n" +
                        "}"
                )
        );

        transform();
        
        assertEnumerationType("LeadStatus");
        assertEquals(assertEnumerationType("LeadStatus"), assertAttribute("_Lead", "status").getDataType());
        assertEquals(assertEnumerationType("LeadStatus"), assertMappedTransferObjectAttribute("Lead", "status").getDataType());

        assertFalse(assertAttribute("_Lead", "status").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Lead", "status").isRequired());

    
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "enum LeadStatus {\n" +
                        "\tOPPORTUNITY = 0\n" +
                        "\tLEAD = 1\n" +
                        "\tPROJECT = 2\n" +
                        "}\n" +
                        "\n" +
                        "entity Lead {\n" +
                        "\tfield required LeadStatus status\n" +
                        "}"
                )
        );

        transform();

        assertEnumerationType("LeadStatus");
        assertEquals(assertEnumerationType("LeadStatus"), assertAttribute("_Lead", "status").getDataType());
        assertEquals(assertEnumerationType("LeadStatus"), assertMappedTransferObjectAttribute("Lead", "status").getDataType());
        assertTrue(assertAttribute("_Lead", "status").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("Lead", "status").isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "enum LeadStatus {\n" +
                        "\tOPPORTUNITY = 0\n" +
                        "\tLEAD = 1\n" +
                        "\tPROJECT = 2\n" +
                        "}\n" +
                        "\n" +
                        "entity Lead {\n" +
                        "\tfield LeadStatus status\n" +
                        "}\n" +
                        "\n" +
                        "entity LeadManager extends Lead {\n" +
                        "}"
                )
        );

        transform();
        
        assertEnumerationType("LeadStatus");
        assertEquals(assertEnumerationType("LeadStatus"), assertAttribute("_Lead", "status").getDataType());
        assertEquals(assertEnumerationType("LeadStatus"), assertMappedTransferObjectAttribute("Lead", "status").getDataType());
        assertEquals(assertEnumerationType("LeadStatus"), assertAttribute("_LeadManager", "status").getDataType());
        assertEquals(assertEnumerationType("LeadStatus"), assertMappedTransferObjectAttribute("LeadManager", "status").getDataType());

    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "enum LeadStatus {\n" +
                        "\tOPPORTUNITY = 0\n" +
                        "\tLEAD = 1\n" +
                        "\tPROJECT = 2\n" +
                        "}\n" +
                        "\n" +
                        "entity Lead {\n" +
                        "\tidentifier LeadStatus status\n" +
                        "}\n" +
                        "\n" +
                        "entity LeadManager extends Lead {\n" +
                        "}"
                )
        );

        transform();

        assertEnumerationType("LeadStatus");
        assertEquals(assertEnumerationType("LeadStatus"), assertAttribute("_Lead", "status").getDataType());
        assertEquals(assertEnumerationType("LeadStatus"), assertMappedTransferObjectAttribute("Lead", "status").getDataType());
        assertTrue(assertAttribute("_Lead", "status").isIdentifier());
    }
}
