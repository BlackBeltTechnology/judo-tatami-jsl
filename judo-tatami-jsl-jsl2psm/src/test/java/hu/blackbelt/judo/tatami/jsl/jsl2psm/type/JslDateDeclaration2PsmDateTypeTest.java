package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.DateType;
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

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.TestUtils.allPsm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslDateDeclaration2PsmDateTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/date";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/date";
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
                        "type date Date\n"
                )
        );

        transform();

        final Set<DateType> psmDates = psmModelWrapper.getStreamOfPsmTypeDateType().collect(Collectors.toSet());
        assertEquals(1, psmDates.size());

        final Optional<DateType> date = psmDates.stream().filter(n -> n.getName().equals("Date")).findFirst();
        assertTrue(date.isPresent());
        assertEquals(date.get().getName(), "Date");
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = parser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type date Date\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Date birthDate\n" +
                        "}"
                )
        );

        transform();

        final Optional<DateType> psmTypeDate = psmModelWrapper.getStreamOfPsmTypeDateType().filter(n -> n.getName().equals("Date")).findFirst();
        assertTrue(psmTypeDate.isPresent());
        final Optional<EntityType> psmEntityPatient = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Patient")).findFirst();
        assertTrue(psmEntityPatient.isPresent());
        final Optional<Attribute> psmPatientBirthDateAttribute = psmEntityPatient.get().getAllAttributes().stream().filter(a -> a.getName().equals("birthDate")).findFirst();
        assertTrue(psmPatientBirthDateAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = parser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type date Date\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield required Date birthDate\n" +
                        "}"
                )
        );

        transform();

        final Optional<EntityType> psmEntityPatient = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Patient")).findFirst();
        assertTrue(psmEntityPatient.isPresent());
        final Optional<Attribute> psmPatientBirthDateAttribute = psmEntityPatient.get().getAllAttributes().stream().filter(a -> a.getName().equals("birthDate")).findFirst();
        assertTrue(psmPatientBirthDateAttribute.isPresent());
        assertTrue(psmPatientBirthDateAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = parser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type date Date\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Date birthDate\n" +
                        "}\n" +
                        "\n" +
                        "entity SurgentPatient extends Patient {\n" +
                        "}"
                )
        );

        transform();

        final Optional<EntityType> psmSurgentPatient = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("SurgentPatient")).findFirst();
        assertTrue(psmSurgentPatient.isPresent());
        final Optional<Attribute> psmSurgentPatientBirthDateAttribute = psmSurgentPatient.get().getAllAttributes().stream().filter(a -> a.getName().equals("birthDate")).findFirst();
        assertTrue(psmSurgentPatientBirthDateAttribute.isPresent());
    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        jslModel = parser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type date Date\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tidentifier Date birthDate\n" +
                        "}"
                )
        );

        transform();

        final Optional<EntityType> psmEmail = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Patient")).findFirst();
        assertTrue(psmEmail.isPresent());
        final Optional<Attribute> psmPatientBirthDateAttribute = psmEmail.get().getAllAttributes().stream().filter(a -> a.getName().equals("birthDate")).findFirst();
        assertTrue(psmPatientBirthDateAttribute.isPresent());
        assertTrue(psmPatientBirthDateAttribute.get().isIdentifier());
    }
}
