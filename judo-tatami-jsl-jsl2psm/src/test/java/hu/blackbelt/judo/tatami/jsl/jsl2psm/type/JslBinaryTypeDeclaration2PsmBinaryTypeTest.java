package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.BinaryType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        jslModel = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type binary Picture mime-types m\"image/png\", m\"image/*\" max-file-size 1024\n"
                )
        );

        transform();

        final Set<BinaryType> psmBinaries = psmModelWrapper.getStreamOfPsmTypeBinaryType().collect(Collectors.toSet());
        assertEquals(1, psmBinaries.size());

        final Optional<BinaryType> binary = psmBinaries.stream().filter(n -> n.getName().equals("Picture")).findFirst();
        assertTrue(binary.isPresent());
        assertEquals(binary.get().getName(), "Picture");
        assertEquals(binary.get().getMimeTypes().size(), 2);
        assertEquals(binary.get().getMimeTypes(), Arrays.asList("m\"image/png\"", "m\"image/*\""));
        assertEquals(binary.get().getMaxFileSize(), 1024);
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = parser.getModelFromStrings(
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

        final Optional<BinaryType> psmTypePicture = psmModelWrapper.getStreamOfPsmTypeBinaryType().filter(n -> n.getName().equals("Picture")).findFirst();
        assertTrue(psmTypePicture.isPresent());
        assertTrue(psmTypePicture.get().getMimeTypes().isEmpty());
        assertEquals(psmTypePicture.get().getMaxFileSize(), 0);
        final Optional<EntityType> psmEntityUser = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("User")).findFirst();
        assertTrue(psmEntityUser.isPresent());
        final Optional<Attribute> psmUserPictureAttribute = psmEntityUser.get().getAllAttributes().stream().filter(a -> a.getName().equals("profilePicture")).findFirst();
        assertTrue(psmUserPictureAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = parser.getModelFromStrings(
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

        final Optional<EntityType> psmEntityUser = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("User")).findFirst();
        assertTrue(psmEntityUser.isPresent());
        final Optional<Attribute> psmUserPictureAttribute = psmEntityUser.get().getAllAttributes().stream().filter(a -> a.getName().equals("profilePicture")).findFirst();
        assertTrue(psmUserPictureAttribute.isPresent());
        assertTrue(psmUserPictureAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = parser.getModelFromStrings(
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

        final Optional<EntityType> psmAdminUser = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("AdminUser")).findFirst();
        assertTrue(psmAdminUser.isPresent());
        final Optional<Attribute> psmAdminUserProfilePictureAttribute = psmAdminUser.get().getAllAttributes().stream().filter(a -> a.getName().equals("profilePicture")).findFirst();
        assertTrue(psmAdminUserProfilePictureAttribute.isPresent());
    }
    
    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

    	jslModel = parser.getModelFromStrings(
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

        final Optional<EntityType> psmEmail = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("User")).findFirst();
        assertTrue(psmEmail.isPresent());
        final Optional<Attribute> psmUserPictureAttribute = psmEmail.get().getAllAttributes().stream().filter(a -> a.getName().equals("profilePicture")).findFirst();
        assertTrue(psmUserPictureAttribute.isPresent());
        assertTrue(psmUserPictureAttribute.get().isIdentifier());
    }
}
