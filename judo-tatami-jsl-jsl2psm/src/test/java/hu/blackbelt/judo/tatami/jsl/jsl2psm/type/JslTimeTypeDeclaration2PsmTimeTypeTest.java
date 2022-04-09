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

        final Set<TimeType> psmTimes = psmModelWrapper.getStreamOfPsmTypeTimeType().collect(Collectors.toSet());
        assertEquals(1, psmTimes.size());

        final Optional<TimeType> myTime = psmTimes.stream().filter(n -> n.getName().equals("MyTime")).findFirst();
        assertTrue(myTime.isPresent());
        assertEquals(myTime.get().getName(), "MyTime");
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

        final Optional<TimeType> psmTypeTime = psmModelWrapper.getStreamOfPsmTypeTimeType().filter(n -> n.getName().equals("MyTime")).findFirst();
        assertTrue(psmTypeTime.isPresent());
        final Optional<EntityType> psmEntityPerson = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonArrivalTimeAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("arrivalTime")).findFirst();
        assertTrue(psmPersonArrivalTimeAttribute.isPresent());
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

        final Optional<EntityType> psmEntityPerson = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonArrivalTimeAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("arrivalTime")).findFirst();
        assertTrue(psmPersonArrivalTimeAttribute.isPresent());
        assertTrue(psmPersonArrivalTimeAttribute.get().isRequired());
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

        final Optional<EntityType> psmStudentPerson = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("StudentPerson")).findFirst();
        assertTrue(psmStudentPerson.isPresent());
        final Optional<Attribute> psmStudentArrivalTimeAttribute = psmStudentPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("arrivalTime")).findFirst();
        assertTrue(psmStudentArrivalTimeAttribute.isPresent());
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

        final Optional<EntityType> psmPerson = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmPerson.isPresent());
        final Optional<Attribute> psmPersonArrivalTimeAttribute = psmPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("arrivalTime")).findFirst();
        assertTrue(psmPersonArrivalTimeAttribute.isPresent());
        assertTrue(psmPersonArrivalTimeAttribute.get().isIdentifier());
    }
}
