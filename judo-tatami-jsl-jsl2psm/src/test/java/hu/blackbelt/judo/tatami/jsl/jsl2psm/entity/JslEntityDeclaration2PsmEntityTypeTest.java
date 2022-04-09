package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEntityDeclaration2PsmEntityTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/entity";

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
    void testCreateEntityType() throws Exception {
        testName = "TestCreateEntityType";

        jslModel = parser.getModelFromStrings(
                "EntityTypeCreateModel",
                List.of("model EntityTypeCreateModel\n" +
                        "\n" +
                        "entity Test {\n" +
                        "}\n" +
                        "entity abstract Person {\n" +
                        "}\n" +
                        "entity SalesPerson extends Person, Test {\n" +
                        "}\n"
                )
        );

        transform();

        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(3, psmEntityTypes.size());

        final Set<String> psmEntityTypeNames = psmEntityTypes.stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("Test", "Person", "SalesPerson");
        assertThat(psmEntityTypeNames, IsEqual.equalTo(jslEntityTypeDeclarationNames));

        final Optional<EntityType> psmEntityPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("Person")).findAny();
        assertTrue(psmEntityPerson.isPresent());
        assertTrue(psmEntityPerson.get().isAbstract());

        final Optional<EntityType> psmEntitySalesPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("SalesPerson")).findAny();
        assertTrue(psmEntitySalesPerson.isPresent());

        final Set<String> psmEntityType3SuperTypeNames = psmEntitySalesPerson.get().getSuperEntityTypes().stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityType3SuperTypeNames = ImmutableSet.of("Person", "Test");
        assertThat(psmEntityType3SuperTypeNames, IsEqual.equalTo(jslEntityType3SuperTypeNames));
    }

    @Test
    void testEntityLocalName() throws Exception {
        //testName = "TestEntityLocaleName";

        jslModel = parser.getModelFromStrings(
                "First::Second::EntityLocaleNameModel",
                List.of("model First::Second::EntityLocaleNameModel\n" +
                        "\n" +
                        "entity Test {\n" +
                        "}"
                )
        );

        transform();

        final Set<hu.blackbelt.judo.meta.psm.namespace.Package> psmPackageTypes = psmModelWrapper.getStreamOfPsmNamespacePackage().collect(Collectors.toSet());
        assertEquals(3, psmPackageTypes.size());

        final Set<String> psmEntityPackageNames = psmPackageTypes.stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslPackageNames = ImmutableSet.of("First", "Second", "EntityLocaleNameModel");
        assertThat(psmEntityPackageNames, IsEqual.equalTo(jslPackageNames));

        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(1, psmEntityTypes.size());

        final Set<String> psmEntityTypeNames = psmEntityTypes.stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("Test");
        assertThat(psmEntityTypeNames, IsEqual.equalTo(jslEntityTypeDeclarationNames));
    }
}
