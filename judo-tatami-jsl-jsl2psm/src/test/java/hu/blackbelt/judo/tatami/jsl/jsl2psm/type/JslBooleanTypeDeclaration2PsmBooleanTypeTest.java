package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.BooleanType;
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
public class JslBooleanTypeDeclaration2PsmBooleanTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/boolean";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/boolean";
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

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type boolean Boolean\n"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<BooleanType> psmBooleans = psmModelWrapper.getStreamOfPsmTypeBooleanType().collect(Collectors.toSet());
        assertEquals(1, psmBooleans.size());

        final Optional<BooleanType> bool = psmBooleans.stream().filter(n -> n.getName().equals("Boolean")).findFirst();
        assertTrue(bool.isPresent());
        assertEquals(bool.get().getName(), "Boolean");
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type boolean Vaccinated\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Vaccinated vaccinated\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<BooleanType> psmTypeVaccinated = psmModelWrapper.getStreamOfPsmTypeBooleanType().filter(n -> n.getName().equals("Vaccinated")).findFirst();
        assertTrue(psmTypeVaccinated.isPresent());
        final Optional<EntityType> psmEntityPatient = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Patient")).findFirst();
        assertTrue(psmEntityPatient.isPresent());
        final Optional<Attribute> psmPatientVaccinatedAttribute = psmEntityPatient.get().getAllAttributes().stream().filter(a -> a.getName().equals("vaccinated")).findFirst();
        assertTrue(psmPatientVaccinatedAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type boolean Vaccinated\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield required Vaccinated vaccinated\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmEntityPatient = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Patient")).findFirst();
        assertTrue(psmEntityPatient.isPresent());
        final Optional<Attribute> psmPatientVaccinatedAttribute = psmEntityPatient.get().getAllAttributes().stream().filter(a -> a.getName().equals("vaccinated")).findFirst();
        assertTrue(psmPatientVaccinatedAttribute.isPresent());
        assertTrue(psmPatientVaccinatedAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type boolean Vaccinated\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Vaccinated vaccinated\n" +
                        "}\n" +
                        "\n" +
                        "entity SurgentPatient extends Patient {\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmSurgentPatient = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("SurgentPatient")).findFirst();
        assertTrue(psmSurgentPatient.isPresent());
        final Optional<Attribute> psmSurgentPatientVaccinatedAttribute = psmSurgentPatient.get().getAllAttributes().stream().filter(a -> a.getName().equals("vaccinated")).findFirst();
        assertTrue(psmSurgentPatientVaccinatedAttribute.isPresent());
    }
    
    @Test
    void testEntityMemberIdentifier() throws Exception {
        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel\n" +
                        "\n" +
                        "type boolean Vaccinated\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tidentifier Vaccinated vaccinated\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmEmail = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Patient")).findFirst();
        assertTrue(psmEmail.isPresent());
        final Optional<Attribute> psmPatientVaccinatedAttribute = psmEmail.get().getAllAttributes().stream().filter(a -> a.getName().equals("vaccinated")).findFirst();
        assertTrue(psmPatientVaccinatedAttribute.isPresent());
        assertTrue(psmPatientVaccinatedAttribute.get().isIdentifier());
    }
}
