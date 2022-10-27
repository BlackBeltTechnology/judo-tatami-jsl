package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.data.*;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
        return new BufferedSlf4jLogger(log);
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

        jslModel = JslParser.getModelFromStrings(
                "EntityUnidirectionalCompositionRelationTypeModel",
                List.of("model EntityUnidirectionalCompositionRelationTypeModel;\n" +
                        "\n" +
                        "entity SalesPerson {\n" +
                        "\tfield Lead[] leads;\n" +
                        "}\n" +
                        "\n" +
                        "entity Lead {\n" +
                        "\tfield required Customer customer;\n" +
                        "}\n" +
                        "\n" +
                        "entity Customer {\n" +
                        "}"
                )
        );

        transform();

        assertEquals(3, getEntityTypes().size());

        assertEntityType("_Customer");
        assertEntityType("_Lead");
        assertEntityType("_SalesPerson");

        assertMappedTransferObject("Customer");
        assertMappedTransferObject("Lead");
        assertMappedTransferObject("SalesPerson");

        assertTrue(assertRelation("_Lead", "customer").isRequired());
        assertEquals(1, assertRelation("_Lead", "customer").getCardinality().getLower());
        assertEquals(1, assertRelation("_Lead", "customer").getCardinality().getUpper());
        assertEquals(assertEntityType("_Customer"), assertRelation("_Lead", "customer").getTarget());

        assertTrue(assertMappedTransferObjectRelation("Lead", "customer").isRequired());
        assertEquals(1, assertMappedTransferObjectRelation("Lead", "customer").getCardinality().getLower());
        assertEquals(1, assertMappedTransferObjectRelation("Lead", "customer").getCardinality().getUpper());
        assertEquals(assertMappedTransferObject("Customer"), assertMappedTransferObjectRelation("Lead", "customer").getTarget());
      
        assertFalse(assertRelation("_SalesPerson", "leads").isRequired());
        assertEquals(0, assertRelation("_SalesPerson", "leads").getCardinality().getLower());
        assertEquals(-1, assertRelation("_SalesPerson", "leads").getCardinality().getUpper());
        assertEquals(assertEntityType("_Lead"), assertRelation("_SalesPerson", "leads").getTarget());        

        assertFalse(assertMappedTransferObjectRelation("SalesPerson", "leads").isRequired());
        assertEquals(0, assertMappedTransferObjectRelation("SalesPerson", "leads").getCardinality().getLower());
        assertEquals(-1, assertMappedTransferObjectRelation("SalesPerson", "leads").getCardinality().getUpper());
        assertEquals(assertMappedTransferObject("Lead"), assertMappedTransferObjectRelation("SalesPerson", "leads").getTarget());        

    }

    @Test
    void testEntityUnidirectionalCompositionInheritedRelationType() throws Exception {
        testName = "TestEntityUnidirectionalCompositionInheritedRelationType";

        jslModel = JslParser.getModelFromStrings(
                "EntityUnidirectionalCompositionInheritedRelationTypeModel",
                List.of("model EntityUnidirectionalCompositionInheritedRelationTypeModel;\n" +
                        "\n" +
                        "entity SalesPerson {\n" +
                        "\tfield Lead[] leads;\n" +
                        "\tfield Customer represents;\n" +
                        "}\n" +
                        "entity Lead {\n" +
                        "\tfield required Customer customer;\n" +
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

        assertEquals(5, getEntityTypes().size());
      
        assertEntityType("_Customer");
        assertEntityType("_Lead");
        assertEntityType("_SuperSalesPerson");
        assertEntityType("_LazySalesPerson");

        assertFalse(assertRelation("_SuperSalesPerson", "leads").isRequired());
        assertEquals(0, assertRelation("_SuperSalesPerson", "leads").getCardinality().getLower());
        assertEquals(-1, assertRelation("_SuperSalesPerson", "leads").getCardinality().getUpper());
        assertEquals(assertEntityType("_Lead"), assertRelation("_SuperSalesPerson", "leads").getTarget());

        assertFalse(assertMappedTransferObjectRelation("SuperSalesPerson", "leads").isRequired());
        assertEquals(0, assertMappedTransferObjectRelation("SuperSalesPerson", "leads").getCardinality().getLower());
        assertEquals(-1, assertMappedTransferObjectRelation("SuperSalesPerson", "leads").getCardinality().getUpper());
        assertEquals(assertMappedTransferObject("Lead"), assertMappedTransferObjectRelation("SuperSalesPerson", "leads").getTarget());
        
        assertFalse(assertRelation("_LazySalesPerson", "leads").isRequired());
        assertEquals(0, assertRelation("_LazySalesPerson", "leads").getCardinality().getLower());
        assertEquals(-1, assertRelation("_LazySalesPerson", "leads").getCardinality().getUpper());
        assertEquals(assertEntityType("_Lead"), assertRelation("_SuperSalesPerson", "leads").getTarget());

        assertFalse(assertMappedTransferObjectRelation("LazySalesPerson", "leads").isRequired());
        assertEquals(0, assertMappedTransferObjectRelation("LazySalesPerson", "leads").getCardinality().getLower());
        assertEquals(-1, assertMappedTransferObjectRelation("LazySalesPerson", "leads").getCardinality().getUpper());
        assertEquals(assertMappedTransferObject("Lead"), assertMappedTransferObjectRelation("SuperSalesPerson", "leads").getTarget());

    }
    
    @Test
    void testEntityAsssociationRelation() throws Exception {
        testName = "AssociationRelationTest";

        jslModel = JslParser.getModelFromFiles(
                "AssociationRelationTestModel",
                List.of(new File("src/test/resources/entity/AssociationRelationTestModel.jsl"))
        );

        transform();

        EntityType lead = assertEntityType("_Lead");
        EntityType customer = assertEntityType("_Customer");
        
        MappedTransferObjectType leadTo = assertMappedTransferObject("Lead");
        MappedTransferObjectType customerTo = assertMappedTransferObject("Customer");
 
        assertThat(leadTo.getEntityType(), IsEqual.equalTo(lead));
        assertThat(customerTo.getEntityType(), IsEqual.equalTo(customer));
        
        Collection<Relation> leadRelations = lead.getRelations();
        Set<String> leadRelationNames = leadRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(leadRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("customer", "customers", 
        				"customer1", "customers1", 
        				"customer2", "customers2", 
        				"injectedCustomer1", "injectedCustomers1", 
        				"injectedCustomer2", "injectedCustomers2")));

        Collection<TransferObjectRelation> leadToRelations = leadTo.getRelations();
        Set<String> leadToRelationNames = leadToRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(leadToRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("customer", "customers", 
        				"customer1", "customers1", 
        				"customer2", "customers2", 
        				"injectedCustomer1", "injectedCustomers1", 
        				"injectedCustomer2", "injectedCustomers2")));

        Collection<TransferObjectRelation> leadChildToRelations = leadTo.getRelations();
        Set<String> leadChildToRelationNames = leadChildToRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(leadChildToRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("customer", "customers", 
        				"customer1", "customers1", 
        				"customer2", "customers2", 
        				"injectedCustomer1", "injectedCustomers1", 
        				"injectedCustomer2", "injectedCustomers2")));

        Collection<TransferObjectRelation> leadGrandChildToRelations = leadTo.getRelations();
        Set<String> leadGrandChildToRelationNames = leadGrandChildToRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(leadGrandChildToRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("customer", "customers", 
        				"customer1", "customers1", 
        				"customer2", "customers2", 
        				"injectedCustomer1", "injectedCustomers1", 
        				"injectedCustomer2", "injectedCustomers2")));

        
        assertThat(assertRelation("_Lead", "customer").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "customer").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customer")).getTarget(), IsEqual.equalTo(customer));

        assertThat(assertMappedTransferObjectRelation("Lead", "customer").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "customer")));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer").getTarget(), IsEqual.equalTo(customerTo));

        assertThat(assertRelation("_Lead", "customers").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "customers").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customers")).getTarget(), IsEqual.equalTo(customer));

        assertThat(assertMappedTransferObjectRelation("Lead", "customers").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "customers")));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers").getTarget(), IsEqual.equalTo(customerTo));

        
        assertThat(assertRelation("_Lead", "customer1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "customer1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customer1")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customer1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "customer1")).getPartner().getName(), IsEqual.equalTo("leads1"));

        assertThat(assertMappedTransferObjectRelation("Lead", "customer1").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "customer1")));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer1").getTarget(), IsEqual.equalTo(customerTo));
 
        
        assertThat(assertRelation("_Lead", "customers1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "customers1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customers1")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customers1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "customers1")).getPartner().getName(), IsEqual.equalTo("leads2"));

        assertThat(assertMappedTransferObjectRelation("Lead", "customers1").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "customers1")));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers1").getTarget(), IsEqual.equalTo(customerTo));

        
        assertThat(assertRelation("_Lead", "customer2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "customer2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customer2")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customer2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "customer2")).getPartner().getName(), IsEqual.equalTo("leads3"));

        assertThat(assertMappedTransferObjectRelation("Lead", "customer2").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "customer2")));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Lead", "customer2").getTarget(), IsEqual.equalTo(customerTo));

        
        assertThat(assertRelation("_Lead", "customers2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "customers2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customers2")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "customers2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "customers2")).getPartner().getName(), IsEqual.equalTo("leads4"));

        assertThat(assertMappedTransferObjectRelation("Lead", "customers2").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "customers2")));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Lead", "customers2").getTarget(), IsEqual.equalTo(customerTo));

        
        assertThat(assertRelation("_Lead", "injectedCustomer1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "injectedCustomer1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomer1")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomer1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomer1")).getPartner().getName(), IsEqual.equalTo("leads5"));

        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer1").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "injectedCustomer1")));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer1").getTarget(), IsEqual.equalTo(customerTo));

        
        assertThat(assertRelation("_Lead", "injectedCustomers1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "injectedCustomers1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomers1")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomers1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomers1")).getPartner().getName(), IsEqual.equalTo("leads6"));

        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers1").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "injectedCustomers1")));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers1").getTarget(), IsEqual.equalTo(customerTo));

        
        assertThat(assertRelation("_Lead", "injectedCustomer2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "injectedCustomer2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomer2")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomer2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomer2")).getPartner().getName(), IsEqual.equalTo("lead1"));

        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer2").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "injectedCustomer2")));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomer2").getTarget(), IsEqual.equalTo(customerTo));

        
        assertThat(assertRelation("_Lead", "injectedCustomers2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Lead", "injectedCustomers2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomers2")).getTarget(), IsEqual.equalTo(customer));
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomers2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Lead", "injectedCustomers2")).getPartner().getName(), IsEqual.equalTo("lead2"));

        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers2").getBinding(), IsEqual.equalTo(assertRelation("_Lead", "injectedCustomers2")));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Lead", "injectedCustomers2").getTarget(), IsEqual.equalTo(customerTo));

        
        Collection<Relation> customerRelations = customer.getRelations();
        Set<String> customerRelationNames = customerRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(customerRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("lead", "leads", 
        				"leads1", "leads2", "leads3", "leads4", "leads5", "leads6", 
        				"lead1", "lead2")));

        Collection<TransferObjectRelation> customerToRelations = customerTo.getRelations();
        Set<String> customerToRelationNames = customerToRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(customerToRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("lead", "leads", 
        				"leads1", "leads2", "leads3", "leads4", "leads5", "leads6", 
        				"lead1", "lead2")));

        Collection<TransferObjectRelation> customerChildToRelations = customerTo.getRelations();
        Set<String> customerChildToRelationNames = customerChildToRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(customerChildToRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("lead", "leads", 
        				"leads1", "leads2", "leads3", "leads4", "leads5", "leads6", 
        				"lead1", "lead2")));


        Collection<TransferObjectRelation> customerGrandChildToRelations = customerTo.getRelations();
        Set<String> customerGrandChildToRelationNames = customerGrandChildToRelations.stream().map(r -> r.getName()).collect(Collectors.toSet());
        assertThat(customerGrandChildToRelationNames, 
        		IsEqual.equalTo(ImmutableSet.of("lead", "leads", 
        				"leads1", "leads2", "leads3", "leads4", "leads5", "leads6", 
        				"lead1", "lead2")));


        assertThat(assertRelation("_Customer", "lead").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "lead").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "lead")).getTarget(), IsEqual.equalTo(lead));

        assertThat(assertMappedTransferObjectRelation("Customer", "lead").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "lead")));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead").getTarget(), IsEqual.equalTo(leadTo));

        
        assertThat(assertRelation("_Customer", "leads").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "leads").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads")).getTarget(), IsEqual.equalTo(lead));

        assertThat(assertMappedTransferObjectRelation("Customer", "leads").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "leads")));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads").getTarget(), IsEqual.equalTo(leadTo));

        
        assertThat(assertRelation("_Customer", "leads1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "leads1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads1")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads1")).getPartner().getName(), IsEqual.equalTo("customer1"));

        assertThat(assertMappedTransferObjectRelation("Customer", "leads1").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "leads1")));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads1").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads1").getTarget(), IsEqual.equalTo(leadTo));

        assertThat(assertRelation("_Customer", "leads2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "leads2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads2")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads2")).getPartner().getName(), IsEqual.equalTo("customers1"));

        assertThat(assertMappedTransferObjectRelation("Customer", "leads2").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "leads2")));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads2").getTarget(), IsEqual.equalTo(leadTo));


        assertThat(assertRelation("_Customer", "leads3").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "leads3").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads3")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads3")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads3")).getPartner().getName(), IsEqual.equalTo("customer2"));

        assertThat(assertMappedTransferObjectRelation("Customer", "leads3").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "leads3")));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads3").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads3").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads3").getTarget(), IsEqual.equalTo(leadTo));

        assertThat(assertRelation("_Customer", "leads4").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "leads4").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads4")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads4")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads4")).getPartner().getName(), IsEqual.equalTo("customers2"));

        assertThat(assertMappedTransferObjectRelation("Customer", "leads4").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "leads4")));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads4").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads4").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads4").getTarget(), IsEqual.equalTo(leadTo));

        assertThat(assertRelation("_Customer", "leads5").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "leads5").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads5")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads5")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads5")).getPartner().getName(), IsEqual.equalTo("injectedCustomer1"));

        assertThat(assertMappedTransferObjectRelation("Customer", "leads5").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "leads5")));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads5").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads5").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads5").getTarget(), IsEqual.equalTo(leadTo));

        assertThat(assertRelation("_Customer", "leads6").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "leads6").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads6")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads6")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "leads6")).getPartner().getName(), IsEqual.equalTo("injectedCustomers1"));

        assertThat(assertMappedTransferObjectRelation("Customer", "leads6").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "leads6")));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads6").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads6").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Customer", "leads6").getTarget(), IsEqual.equalTo(leadTo));

        assertThat(assertRelation("_Customer", "lead1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "lead1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "lead1")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "lead1")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "lead1")).getPartner().getName(), IsEqual.equalTo("injectedCustomer2"));

        assertThat(assertMappedTransferObjectRelation("Customer", "lead1").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "lead1")));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead1").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead1").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead1").getTarget(), IsEqual.equalTo(leadTo));

        assertThat(assertRelation("_Customer", "lead2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertRelation("_Customer", "lead2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(((AssociationEnd) assertRelation("_Customer", "lead2")).getTarget(), IsEqual.equalTo(lead));
        assertThat(((AssociationEnd) assertRelation("_Customer", "lead2")).getPartner(), IsNull.notNullValue());
        assertThat(((AssociationEnd) assertRelation("_Customer", "lead2")).getPartner().getName(), IsEqual.equalTo("injectedCustomers2"));

        assertThat(assertMappedTransferObjectRelation("Customer", "lead2").getBinding(), IsEqual.equalTo(assertRelation("_Customer", "lead2")));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Customer", "lead2").getTarget(), IsEqual.equalTo(leadTo));

    }
    
    
}
