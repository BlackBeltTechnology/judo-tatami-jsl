package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
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

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type numeric MyNumber precision 12 scale 5\n"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<NumericType> psmNumerics = allPsm(psmModel, NumericType.class).collect(Collectors.toSet());
        assertEquals(1, psmNumerics.size());

        final Optional<NumericType> myNumber = psmNumerics.stream().filter(n -> n.getName().equals("MyNumber")).findFirst();
        assertTrue(myNumber.isPresent());
        assertEquals(myNumber.get().getName(), "MyNumber");
        assertEquals(myNumber.get().getPrecision(), 12);
        assertEquals(myNumber.get().getScale(), 5);
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<NumericType> psmTypeHeight = allPsm(psmModel, NumericType.class).filter(n -> n.getName().equals("Height")).findFirst();
        assertTrue(psmTypeHeight.isPresent());
        final Optional<EntityType> psmEntityPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonHeightAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("height")).findFirst();
        assertTrue(psmPersonHeightAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmEntityPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonHeightAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("height")).findFirst();
        assertTrue(psmPersonHeightAttribute.isPresent());
        assertTrue(psmPersonHeightAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmStudentPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("StudentPerson")).findFirst();
        assertTrue(psmStudentPerson.isPresent());
        final Optional<Attribute> psmStudentHeightAttribute = psmStudentPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("height")).findFirst();
        assertTrue(psmStudentHeightAttribute.isPresent());
    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmPerson.isPresent());
        final Optional<Attribute> psmPersonHeightAttribute = psmPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("height")).findFirst();
        assertTrue(psmPersonHeightAttribute.isPresent());
        assertTrue(psmPersonHeightAttribute.get().isIdentifier());
    }
}
