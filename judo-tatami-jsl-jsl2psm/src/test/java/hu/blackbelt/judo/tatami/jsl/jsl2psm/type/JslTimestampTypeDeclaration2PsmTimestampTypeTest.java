package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.type.TimestampType;
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

        jslModel = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type timestamp Timestamp\n"
                )
        );

        transform();

        final Set<TimestampType> psmTimestamps = psmModelWrapper.getStreamOfPsmTypeTimestampType().collect(Collectors.toSet());
        assertEquals(1, psmTimestamps.size());

        final Optional<TimestampType> timestamp = psmTimestamps.stream().filter(n -> n.getName().equals("Timestamp")).findFirst();
        assertTrue(timestamp.isPresent());
        assertEquals(timestamp.get().getName(), "Timestamp");
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = parser.getModelFromStrings(
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

        final Optional<TimestampType> psmTypeReceivedAt = psmModelWrapper.getStreamOfPsmTypeTimestampType().filter(n -> n.getName().equals("Timestamp")).findFirst();
        assertTrue(psmTypeReceivedAt.isPresent());
        final Optional<EntityType> psmEntityEmail = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Email")).findFirst();
        assertTrue(psmEntityEmail.isPresent());
        final Optional<Attribute> psmEmailReceivedAtAttribute = psmEntityEmail.get().getAllAttributes().stream().filter(a -> a.getName().equals("receivedAt")).findFirst();
        assertTrue(psmEmailReceivedAtAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = parser.getModelFromStrings(
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

        final Optional<EntityType> psmEntityEmail = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Email")).findFirst();
        assertTrue(psmEntityEmail.isPresent());
        final Optional<Attribute> psmEmailReceivedAtAttribute = psmEntityEmail.get().getAllAttributes().stream().filter(a -> a.getName().equals("receivedAt")).findFirst();
        assertTrue(psmEmailReceivedAtAttribute.isPresent());
        assertTrue(psmEmailReceivedAtAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = parser.getModelFromStrings(
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

        final Optional<EntityType> psmImportantEmail = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("ImportantEmail")).findFirst();
        assertTrue(psmImportantEmail.isPresent());
        final Optional<Attribute> psmImportantEmailReceivedAtAttribute = psmImportantEmail.get().getAllAttributes().stream().filter(a -> a.getName().equals("receivedAt")).findFirst();
        assertTrue(psmImportantEmailReceivedAtAttribute.isPresent());
    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

    	jslModel = parser.getModelFromStrings(
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

        final Optional<EntityType> psmEmail = psmModelWrapper.getStreamOfPsmDataEntityType().filter(e -> e.getName().equals("Email")).findFirst();
        assertTrue(psmEmail.isPresent());
        final Optional<Attribute> psmImportantEmailReceivedAtAttribute = psmEmail.get().getAllAttributes().stream().filter(a -> a.getName().equals("receivedAt")).findFirst();
        assertTrue(psmImportantEmailReceivedAtAttribute.isPresent());
        assertTrue(psmImportantEmailReceivedAtAttribute.get().isIdentifier());
    }
}
