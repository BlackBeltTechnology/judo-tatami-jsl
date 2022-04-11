package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.Relation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;

import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

        jslModel = parser.getModelFromStrings(
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

        jslModel = parser.getModelFromStrings(
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

        final Optional<Relation> superSalesPersonLeadsRelation = psmEntitySuperSalesPerson.get().getAllRelations().stream().filter(r -> r.getName().equals("leads")).findFirst();
        assertTrue(superSalesPersonLeadsRelation.isPresent());
        assertFalse(superSalesPersonLeadsRelation.get().isRequired());
        assertEquals(0, superSalesPersonLeadsRelation.get().getCardinality().getLower());
        assertEquals(-1, superSalesPersonLeadsRelation.get().getCardinality().getUpper());
        assertEquals(psmEntityLead.get(), superSalesPersonLeadsRelation.get().getTarget());

        final Optional<EntityType> psmEntityLazySalesPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("LazySalesPerson")).findAny();
        assertTrue(psmEntityLazySalesPerson.isPresent());

        final Optional<Relation> lazySalesPersonLeadsRelation = psmEntityLazySalesPerson.get().getAllRelations().stream().filter(r -> r.getName().equals("leads")).findFirst();
        assertTrue(lazySalesPersonLeadsRelation.isPresent());
        assertFalse(lazySalesPersonLeadsRelation.get().isRequired());
        assertEquals(0, lazySalesPersonLeadsRelation.get().getCardinality().getLower());
        assertEquals(-1, lazySalesPersonLeadsRelation.get().getCardinality().getUpper());
        assertEquals(psmEntityLead.get(), lazySalesPersonLeadsRelation.get().getTarget());
    }
    
    @Test
    void testEntityAsssociationRelation() throws Exception {
        testName = "AssociationRelationTest";

        jslModel = parser.getModelFromFiles(
                "AssociationRelationTestModel",
                List.of(new File("src/test/resources/entity/AssociationRelationTestModel.jsl"))
        );

        transform();

        final Set<EntityType> entities = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());

        
        final Optional<EntityType> lead = entities.stream().filter(e -> e.getName().equals("Lead")).findFirst();
        assertTrue(lead.isPresent());
        
        final Optional<EntityType> customer = entities.stream().filter(e -> e.getName().equals("Customer")).findFirst();
        assertTrue(customer.isPresent());


        Collection<Relation> leadRelations = lead.get().getRelations();
        Set<String> leadRelationNames = leadRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(leadRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("customer", "customers", 
        				"customer1", "customers1", 
        				"customer2", "customers2", 
        				"injectedCustomer1", "injectedCustomers1", 
        				"injectedCustomer2", "injectedCustomers2")));

        assertThat(getRelation("Lead", "customer").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "customer").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Lead", "customer")).getTarget(), IsEqual.equalTo(customer.get()));

        assertThat(getRelation("Lead", "customers").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "customers").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Lead", "customers")).getTarget(), IsEqual.equalTo(customer.get()));

        assertThat(getRelation("Lead", "customer1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "customer1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Lead", "customer1")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "customer1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "customer1")).getPartner().getName(), IsEqual.equalTo("leads1"));

        assertThat(getRelation("Lead", "customers1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "customers1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Lead", "customers1")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "customers1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "customers1")).getPartner().getName(), IsEqual.equalTo("leads2"));

        assertThat(getRelation("Lead", "customer2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "customer2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Lead", "customer2")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "customer2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "customer2")).getPartner().getName(), IsEqual.equalTo("leads3"));
        
        assertThat(getRelation("Lead", "customers2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "customers2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Lead", "customers2")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "customers2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "customers2")).getPartner().getName(), IsEqual.equalTo("leads4"));

        assertThat(getRelation("Lead", "injectedCustomer1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "injectedCustomer1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomer1")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomer1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomer1")).getPartner().getName(), IsEqual.equalTo("leads5"));

        assertThat(getRelation("Lead", "injectedCustomers1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "injectedCustomers1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomers1")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomers1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomers1")).getPartner().getName(), IsEqual.equalTo("leads6"));

        assertThat(getRelation("Lead", "injectedCustomer2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "injectedCustomer2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomer2")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomer2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomer2")).getPartner().getName(), IsEqual.equalTo("lead1"));

        assertThat(getRelation("Lead", "injectedCustomers2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Lead", "injectedCustomers2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomers2")).getTarget(), IsEqual.equalTo(customer.get()));
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomers2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Lead", "injectedCustomers2")).getPartner().getName(), IsEqual.equalTo("lead2"));

        Collection<Relation> customerRelations = customer.get().getRelations();
        Set<String> customerRelationNames = customerRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(customerRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("lead", "leads", 
        				"leads1", "leads2", "leads3", "leads4", "leads5", "leads6", 
        				"lead1", "lead2")));

        assertThat(getRelation("Customer", "lead").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "lead").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Customer", "lead")).getTarget(), IsEqual.equalTo(lead.get()));

        assertThat(getRelation("Customer", "leads").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "leads").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Customer", "leads")).getTarget(), IsEqual.equalTo(lead.get()));

        assertThat(getRelation("Customer", "leads1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "leads1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Customer", "leads1")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "leads1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "leads1")).getPartner().getName(), IsEqual.equalTo("customer1"));

        assertThat(getRelation("Customer", "leads2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "leads2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Customer", "leads2")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "leads2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "leads2")).getPartner().getName(), IsEqual.equalTo("customers1"));

        assertThat(getRelation("Customer", "leads3").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "leads3").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Customer", "leads3")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "leads3")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "leads3")).getPartner().getName(), IsEqual.equalTo("customer2"));

        assertThat(getRelation("Customer", "leads4").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "leads4").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Customer", "leads4")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "leads4")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "leads4")).getPartner().getName(), IsEqual.equalTo("customers2"));

        assertThat(getRelation("Customer", "leads5").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "leads5").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Customer", "leads5")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "leads5")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "leads5")).getPartner().getName(), IsEqual.equalTo("injectedCustomer1"));

        assertThat(getRelation("Customer", "leads6").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "leads6").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) getRelation("Customer", "leads6")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "leads6")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "leads6")).getPartner().getName(), IsEqual.equalTo("injectedCustomers1"));
        
        assertThat(getRelation("Customer", "lead1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "lead1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Customer", "lead1")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "lead1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "lead1")).getPartner().getName(), IsEqual.equalTo("injectedCustomer2"));

        assertThat(getRelation("Customer", "lead2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(getRelation("Customer", "lead2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) getRelation("Customer", "lead2")).getTarget(), IsEqual.equalTo(lead.get()));
        assertThat(((AssociationEnd) getRelation("Customer", "lead2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) getRelation("Customer", "lead2")).getPartner().getName(), IsEqual.equalTo("injectedCustomers2"));

    }
    
    
    private Relation getRelation(String entityName, String relationName) {
        final Set<EntityType> entities = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());

        final Optional<EntityType> entity = entities.stream().filter(e -> e.getName().equals(entityName)).findFirst();
        assertTrue(entity.isPresent());

    	return entity.get().getRelations().stream().filter(r -> r.getName().equals(relationName)).findFirst().get();

    }
}
