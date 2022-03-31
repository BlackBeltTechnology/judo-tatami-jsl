package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.EnumerationMember;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
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

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<EnumerationType> psmEnumeration = psmModelWrapper.getStreamOfPsmTypeEnumerationType().collect(Collectors.toSet());
        assertEquals(1, psmEnumeration.size());

        final Optional<EnumerationType> timestamp = psmEnumeration.stream().filter(n -> n.getName().equals("LeadStatus")).findFirst();
        assertTrue(timestamp.isPresent());
        assertEquals(timestamp.get().getName(), "LeadStatus");

        final List<EnumerationMember> psmTimestampMembers = timestamp.get().getMembers();
        assertEquals(3, psmTimestampMembers.size());

        final Optional<EnumerationMember> opportunity = psmTimestampMembers.stream().filter(m -> m.getName().equals("OPPORTUNITY")).findFirst();
        assertTrue(opportunity.isPresent());
        assertEquals(0, opportunity.get().getOrdinal());

        final Optional<EnumerationMember> lead = psmTimestampMembers.stream().filter(m -> m.getName().equals("LEAD")).findFirst();
        assertTrue(lead.isPresent());
        assertEquals(1, lead.get().getOrdinal());

        final Optional<EnumerationMember> project = psmTimestampMembers.stream().filter(m -> m.getName().equals("PROJECT")).findFirst();
        assertTrue(project.isPresent());
        assertEquals(2, project.get().getOrdinal());
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EnumerationType> psmTypeLeadStatus = psmModelWrapper.getStreamOfPsmTypeEnumerationType().filter(n -> n.getName().equals("LeadStatus")).findFirst();
        assertTrue(psmTypeLeadStatus.isPresent());
        final Optional<EntityType> psmEntityLead = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Lead")).findFirst();
        assertTrue(psmEntityLead.isPresent());
        final Optional<Attribute> psmLeadStatusAttribute = psmEntityLead.get().getAllAttributes().stream().filter(a -> a.getName().equals("status")).findFirst();
        assertTrue(psmLeadStatusAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EnumerationType> psmTypeLeadStatus = psmModelWrapper.getStreamOfPsmTypeEnumerationType().filter(n -> n.getName().equals("LeadStatus")).findFirst();
        assertTrue(psmTypeLeadStatus.isPresent());
        final Optional<EntityType> psmEntityLead = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Lead")).findFirst();
        assertTrue(psmEntityLead.isPresent());
        final Optional<Attribute> psmLeadStatusAttribute = psmEntityLead.get().getAllAttributes().stream().filter(a -> a.getName().equals("status")).findFirst();
        assertTrue(psmLeadStatusAttribute.isPresent());
        assertTrue(psmLeadStatusAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmLeadManager = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("LeadManager")).findFirst();
        assertTrue(psmLeadManager.isPresent());
        final Optional<Attribute> psmLeadManagerStatusAttribute = psmLeadManager.get().getAllAttributes().stream().filter(a -> a.getName().equals("status")).findFirst();
        assertTrue(psmLeadManagerStatusAttribute.isPresent());
    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
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
                        "\tidentifier LeadStatus status\n" +
                        "}\n" +
                        "\n" +
                        "entity LeadManager extends Lead {\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmLeadManager = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("LeadManager")).findFirst();
        assertTrue(psmLeadManager.isPresent());
        final Optional<Attribute> psmLeadManagerStatusAttribute = psmLeadManager.get().getAllAttributes().stream().filter(a -> a.getName().equals("status")).findFirst();
        assertTrue(psmLeadManagerStatusAttribute.isPresent());
        assertTrue(psmLeadManagerStatusAttribute.get().isIdentifier());
    }
}
