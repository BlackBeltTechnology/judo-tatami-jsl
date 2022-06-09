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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslBinaryTypeDeclaration2PsmBinaryTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/binary";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/binary";
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
                        "type binary Picture(mime-types = m\"image/png\", m\"image/*\", max-file-size = 1024)\n"
                )
        );

        transform();

        assertBinaryType("Picture");
        assertEquals(assertBinaryType("Picture").getName(), "Picture");
        assertEquals(assertBinaryType("Picture").getMimeTypes().size(), 2);
        assertEquals(assertBinaryType("Picture").getMimeTypes(), Arrays.asList("m\"image/png\"", "m\"image/*\""));
        assertEquals(assertBinaryType("Picture").getMaxFileSize(), 1024);
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type binary Picture" +
                        "\n" +
                        "entity User {\n" +
                        "\tfield Picture profilePicture\n" +
                        "}"
                )
        );

        transform();

        assertTrue(assertBinaryType("Picture").getMimeTypes().isEmpty());       
        assertBinaryType("Picture");        
        assertEquals(assertBinaryType("Picture").getMaxFileSize(), 0);
        assertEquals(assertBinaryType("Picture"), assertAttribute("_User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("User", "profilePicture").getDataType());        
        assertFalse(assertAttribute("_User", "profilePicture").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("User", "profilePicture").isRequired());
        
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type binary Picture\n" +
                        "\n" +
                        "entity User {\n" +
                        "\tfield required Picture profilePicture\n" +
                        "}"
                )
        );

        transform();

        assertBinaryType("Picture");        
        assertEquals(assertBinaryType("Picture").getMaxFileSize(), 0);
        assertEquals(assertBinaryType("Picture"), assertAttribute("_User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("User", "profilePicture").getDataType());
        assertTrue(assertAttribute("_User", "profilePicture").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("User", "profilePicture").isRequired());

    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type binary Picture\n" +
                        "\n" +
                        "entity User {\n" +
                        "\tfield Picture profilePicture\n" +
                        "}\n" +
                        "\n" +
                        "entity AdminUser extends User {\n" +
                        "}"
                )
        );

        transform();

        assertBinaryType("Picture");        
        assertEquals(assertBinaryType("Picture"), assertAttribute("_User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertAttribute("_AdminUser", "profilePicture").getDataType());
        
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("AdminUser", "profilePicture").getDataType());
        
    }
    
    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

    	jslModel = JslParser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type binary Picture\n" +
                        "\n" +
                        "entity User {\n" +
                        "\tidentifier Picture profilePicture\n" +
                        "}"
                )
        );

        transform();

        assertBinaryType("Picture");        
        assertEquals(assertBinaryType("Picture"), assertAttribute("_User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("User", "profilePicture").getDataType());        

        assertTrue(assertAttribute("_User", "profilePicture").isIdentifier());

    }
}
