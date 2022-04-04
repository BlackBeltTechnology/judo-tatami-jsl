package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.Relation;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslEntityRelationDeclaration2PsmRelationTest extends AbstractTest  {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/entity-relation";

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
    void testEntityUnidirectionalCompositionRelationType() throws Exception {
        testName = "TestEntityUnidirectionalCompositionRelationType";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityUnidirectionalCompositionRelationTypeModel",
                List.of("model EntityUnidirectionalCompositionRelationTypeModel\n" +
                        "\n" +
                        "entity SalesPerson {\n" +
                        "\tfield Lead[] leads\n" +
                        "}\n" +
                        "\n" +
                        "entity Lead {\n" +
                        "\tfield required Customer customer\n" +
                        "}\n" +
                        "\n" +
                        "entity Customer {\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(3, psmEntityTypes.size());

        final Set<Relation> psmRelations = psmModelWrapper.getStreamOfPsmDataRelation().collect(Collectors.toSet());
        assertEquals(2, psmRelations.size());

        final Optional<EntityType> psmEntityCustomer = psmEntityTypes.stream().filter(e -> e.getName().equals("Customer")).findAny();
        assertTrue(psmEntityCustomer.isPresent());

        final Optional<EntityType> psmEntityLead = psmEntityTypes.stream().filter(e -> e.getName().equals("Lead")).findAny();
        assertTrue(psmEntityLead.isPresent());

        final Optional<Relation> leadCustomerRelation = psmEntityLead.get().getRelations().stream().filter(r -> r.getName().equals("customer")).findFirst();
        assertTrue(leadCustomerRelation.isPresent());
        assertTrue(leadCustomerRelation.get().isRequired());
        assertEquals(psmEntityCustomer.get(), leadCustomerRelation.get().getTarget());

        final Optional<EntityType> psmEntitySalesPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("SalesPerson")).findAny();
        assertTrue(psmEntitySalesPerson.isPresent());

        final Optional<Relation> salesPersonLeadsRelation = psmEntitySalesPerson.get().getRelations().stream().filter(r -> r.getName().equals("leads")).findFirst();
        assertTrue(salesPersonLeadsRelation.isPresent());
        assertFalse(salesPersonLeadsRelation.get().isRequired());
        assertEquals(0, salesPersonLeadsRelation.get().getCardinality().getLower());
        assertEquals(-1, salesPersonLeadsRelation.get().getCardinality().getUpper());
        assertEquals(psmEntityLead.get(), salesPersonLeadsRelation.get().getTarget());
    }

    @Test
    void testEntityUnidirectionalCompositionInheritedRelationType() throws Exception {
        testName = "TestEntityUnidirectionalCompositionInheritedRelationType";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityUnidirectionalCompositionInheritedRelationTypeModel",
                List.of("model EntityUnidirectionalCompositionInheritedRelationTypeModel\n" +
                        "\n" +
                        "entity SalesPerson {\n" +
                        "\tfield Lead[] leads\n" +
                        "\tfield Customer represents\n" +
                        "}\n" +
                        "entity Lead {\n" +
                        "\tfield required Customer customer\n" +
                        "}\n" +
                        "\n" +
                        "entity SuperSalesPerson extends SalesPerson {\n" +
                        "}\n" +
                        "entity LazySalesPerson extends SalesPerson {\n" +
                        "}\n" +
                        "\n" +
                        "entity Customer {\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(5, psmEntityTypes.size());

        final Set<Relation> psmRelations = psmModelWrapper.getStreamOfPsmDataRelation().collect(Collectors.toSet());
        assertEquals(3, psmRelations.size());

        final Optional<EntityType> psmEntityCustomer = psmEntityTypes.stream().filter(e -> e.getName().equals("Customer")).findAny();
        assertTrue(psmEntityCustomer.isPresent());

        final Optional<EntityType> psmEntityLead = psmEntityTypes.stream().filter(e -> e.getName().equals("Lead")).findAny();
        assertTrue(psmEntityLead.isPresent());

        final Optional<EntityType> psmEntitySuperSalesPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("SuperSalesPerson")).findAny();
        assertTrue(psmEntitySuperSalesPerson.isPresent());

        final Optional<Relation> superSalesPersonLeadsRelation = psmEntitySuperSalesPerson.get().getRelations().stream().filter(r -> r.getName().equals("leads")).findFirst();
        assertTrue(superSalesPersonLeadsRelation.isPresent());
        assertFalse(superSalesPersonLeadsRelation.get().isRequired());
        assertEquals(0, superSalesPersonLeadsRelation.get().getCardinality().getLower());
        assertEquals(-1, superSalesPersonLeadsRelation.get().getCardinality().getUpper());
        assertEquals(psmEntityLead.get(), superSalesPersonLeadsRelation.get().getTarget());

        final Optional<EntityType> psmEntityLazySalesPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("LazySalesPerson")).findAny();
        assertTrue(psmEntityLazySalesPerson.isPresent());

        final Optional<Relation> lazySalesPersonLeadsRelation = psmEntityLazySalesPerson.get().getRelations().stream().filter(r -> r.getName().equals("leads")).findFirst();
        assertTrue(lazySalesPersonLeadsRelation.isPresent());
        assertFalse(lazySalesPersonLeadsRelation.get().isRequired());
        assertEquals(0, lazySalesPersonLeadsRelation.get().getCardinality().getLower());
        assertEquals(-1, lazySalesPersonLeadsRelation.get().getCardinality().getUpper());
        assertEquals(psmEntityLead.get(), lazySalesPersonLeadsRelation.get().getTarget());
    }
}
