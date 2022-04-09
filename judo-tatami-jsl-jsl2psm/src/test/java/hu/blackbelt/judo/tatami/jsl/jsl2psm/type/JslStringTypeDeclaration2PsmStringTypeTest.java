package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.TestUtils.allPsm;
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

        jslModel = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type string Name max-length 32 regex \"/^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/g\"\n"
                )
        );

        transform();

        final Set<StringType> psmStrings = psmModelWrapper.getStreamOfPsmTypeStringType().collect(Collectors.toSet());
        assertEquals(1, psmStrings.size());

        final Optional<StringType> myName = psmStrings.stream().filter(n -> n.getName().equals("Name")).findFirst();
        assertTrue(myName.isPresent());
        assertEquals(myName.get().getName(), "Name");
        assertEquals(myName.get().getMaxLength(), 32);
        String regExp = "/^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/g";
        assertEquals(myName.get().getRegExp(), regExp);
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = parser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type string Name max-length 32\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Name name\n" +
                        "}"
                )
        );

        transform();

        final Optional<StringType> psmTypeName = psmModelWrapper.getStreamOfPsmTypeStringType().filter(n -> n.getName().equals("Name")).findFirst();
        assertTrue(psmTypeName.isPresent());
        final Optional<EntityType> psmEntityPerson = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonNameAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("name")).findFirst();
        assertTrue(psmPersonNameAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = parser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type string Name max-length 32\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield required Name name\n" +
                        "}"
                )
        );

        transform();

        final Optional<EntityType> psmEntityPerson = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonNameAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("name")).findFirst();
        assertTrue(psmPersonNameAttribute.isPresent());
        assertTrue(psmPersonNameAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = parser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type string Name max-length 32\n" +
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

        final Optional<EntityType> psmStudentPerson = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("StudentPerson")).findFirst();
        assertTrue(psmStudentPerson.isPresent());
        final Optional<Attribute> psmStudentNameAttribute = psmStudentPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("name")).findFirst();
        assertTrue(psmStudentNameAttribute.isPresent());
    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = parser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type string Name max-length 32\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tidentifier Name name\n" +
                        "}"
                )
        );

        transform();

        final Optional<EntityType> psmPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmPerson.isPresent());
        final Optional<Attribute> psmPersonNameAttribute = psmPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("name")).findFirst();
        assertTrue(psmPersonNameAttribute.isPresent());
        assertTrue(psmPersonNameAttribute.get().isIdentifier());
    }
}
