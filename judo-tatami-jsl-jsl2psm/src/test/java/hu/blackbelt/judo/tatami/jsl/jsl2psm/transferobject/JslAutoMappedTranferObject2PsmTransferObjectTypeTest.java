package hu.blackbelt.judo.tatami.jsl.jsl2psm.transferobject;

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

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
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
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslAutoMappedTranferObject2PsmTransferObjectTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/transferobject";

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
    void testCreateAutoMappedTransferObjectType() throws Exception {
        testName = "TestCreateAutoMappedTransferObjectType";


        jslModel = JslParser.getModelFromFiles(
                "AutoMappedTransferObjectTypeModel",
                List.of(new File("src/test/resources/transferobject/TestCreateAutoMappedTransferObjectTypeModel.jsl"))
        );

        transform();

        assertMappedTransferObject("AutoMapped");
        assertEquals(7, assertMappedTransferObject("AutoMapped").getAttributes().size());

        TransferAttribute nameAttribute = assertMappedTransferObjectAttribute("AutoMapped", "name");
        assertFalse(nameAttribute.isRequired());
        assertEquals(assertStringType("String"), nameAttribute.getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "name"), nameAttribute.getBinding());

        TransferAttribute attribute = assertMappedTransferObjectAttribute("AutoMapped", "attribute");
        assertFalse(attribute.isRequired());
        assertEquals(assertNumericType("Integer"), attribute.getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "attribute"), attribute.getBinding());

        TransferAttribute identifier = assertMappedTransferObjectAttribute("AutoMapped", "identifier");
        assertFalse(identifier.isRequired());
        assertEquals(assertNumericType("Integer"), identifier.getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "identifier"), identifier.getBinding());
        
        TransferAttribute attribute2 = assertMappedTransferObjectAttribute("AutoMapped", "attribute2");
        assertFalse(attribute2.isRequired());
        assertEquals(assertNumericType("Integer"), attribute2.getDataType());
        assertEquals(assertAttribute("_Entity", "attribute2"), attribute2.getBinding());

        TransferAttribute attributeDerived = assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived");
        assertFalse(attributeDerived.isRequired());
        assertEquals(assertNumericType("Integer"), attributeDerived.getDataType());
        DataProperty attributeDerivedProperty = assertDataProperty("_EntityAncestor", "attributeDerived");
        assertEquals(attributeDerivedProperty, attributeDerived.getBinding());
        assertEquals("self.attribute", attributeDerivedProperty.getGetterExpression().getExpression());

        TransferAttribute attributeDerived2 = assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived2");
        assertFalse(attributeDerived2.isRequired());
        assertEquals(assertNumericType("Integer"), attributeDerived2.getDataType());
        DataProperty attributeDerived2Property = assertDataProperty("_Entity", "attributeDerived2");
        assertEquals(attributeDerived2Property, attributeDerived2.getBinding());
        assertEquals("self.attribute", attributeDerived2Property.getGetterExpression().getExpression());

        assertEquals(16, assertMappedTransferObject("AutoMapped").getRelations().size());

        TransferObjectRelation containment = assertMappedTransferObjectRelation("AutoMapped", "containment");
        assertFalse(containment.isRequired());
        assertThat(containment.getBinding(), IsEqual.equalTo(assertRelation("_EntityAncestor", "containment")));
        assertThat(containment.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(containment.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(containment.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation containmentCollection = assertMappedTransferObjectRelation("AutoMapped", "containmentCollection");
        assertFalse(containmentCollection.isRequired());
        assertThat(containmentCollection.getBinding(), IsEqual.equalTo(assertRelation("_EntityAncestor", "containmentCollection")));
        assertThat(containmentCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(containmentCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(containmentCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        
        TransferObjectRelation containment2 = assertMappedTransferObjectRelation("AutoMapped", "containment2");
        assertFalse(containment2.isRequired());
        assertThat(containment2.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "containment2")));
        assertThat(containment2.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(containment2.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(containment2.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation containmentCollection2 = assertMappedTransferObjectRelation("AutoMapped", "containmentCollection2");
        assertFalse(containmentCollection2.isRequired());
        assertThat(containmentCollection2.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "containmentCollection2")));
        assertThat(containmentCollection2.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(containmentCollection2.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(containmentCollection2.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        
        TransferObjectRelation association = assertMappedTransferObjectRelation("AutoMapped", "association");
        assertFalse(association.isRequired());
        assertThat(association.getBinding(), IsEqual.equalTo(assertRelation("_EntityAncestor", "association")));
        assertThat(association.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(association.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(association.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation associationCollection = assertMappedTransferObjectRelation("AutoMapped", "associationCollection");
        assertFalse(associationCollection.isRequired());
        assertThat(associationCollection.getBinding(), IsEqual.equalTo(assertRelation("_EntityAncestor", "associationCollection")));
        assertThat(associationCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(associationCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(associationCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        
        TransferObjectRelation association2 = assertMappedTransferObjectRelation("AutoMapped", "association2");
        assertFalse(association2.isRequired());
        assertThat(association2.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "association2")));
        assertThat(association2.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(association2.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(association2.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation associationCollection2 = assertMappedTransferObjectRelation("AutoMapped", "associationCollection2");
        assertFalse(associationCollection2.isRequired());
        assertThat(associationCollection2.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "associationCollection2")));
        assertThat(associationCollection2.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(associationCollection2.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(associationCollection2.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation twoWayAssociation = assertMappedTransferObjectRelation("AutoMapped", "twoWayAssociation");
        assertFalse(twoWayAssociation.isRequired());
        assertThat(twoWayAssociation.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "twoWayAssociation")));
        assertThat(twoWayAssociation.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociation.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(twoWayAssociation.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation twoWayAssociationCollection = assertMappedTransferObjectRelation("AutoMapped", "twoWayAssociationCollection");
        assertFalse(twoWayAssociationCollection.isRequired());
        assertThat(twoWayAssociationCollection.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "twoWayAssociationCollection")));
        assertThat(twoWayAssociationCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociationCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(twoWayAssociationCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation twoWayAssociationOpposite = assertMappedTransferObjectRelation("AutoMapped", "twoWayAssociationOpposite");
        assertFalse(twoWayAssociationOpposite.isRequired());
        assertThat(twoWayAssociationOpposite.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "twoWayAssociationOpposite")));
        assertThat(twoWayAssociationOpposite.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociationOpposite.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(twoWayAssociationOpposite.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        TransferObjectRelation twoWayAssociationCollectionOpposite = assertMappedTransferObjectRelation("AutoMapped", "twoWayAssociationCollectionOpposite");
        assertFalse(twoWayAssociationCollectionOpposite.isRequired());
        assertThat(twoWayAssociationCollectionOpposite.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "twoWayAssociationCollectionOpposite")));
        assertThat(twoWayAssociationCollectionOpposite.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociationCollectionOpposite.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(twoWayAssociationCollectionOpposite.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        
        TransferObjectRelation containmentDerived = assertMappedTransferObjectRelation("AutoMapped", "containmentDerived");
        assertFalse(containmentDerived.isRequired());
        assertThat(containmentDerived.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(containmentDerived.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(containmentDerived.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentDerivedProperty = assertNavigationProperty("_EntityAncestor", "containmentDerived");
        assertEquals(containmentDerivedProperty, containmentDerived.getBinding());
        assertEquals("self.containment", containmentDerivedProperty.getGetterExpression().getExpression());

        TransferObjectRelation containmentDerived2 = assertMappedTransferObjectRelation("AutoMapped", "containmentDerived2");
        assertFalse(containmentDerived2.isRequired());
        assertThat(containmentDerived2.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(containmentDerived2.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(containmentDerived2.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentDerived2Property = assertNavigationProperty("_Entity", "containmentDerived2");
        assertEquals(containmentDerived2Property, containmentDerived2.getBinding());
        assertEquals("self.containment", containmentDerived2Property.getGetterExpression().getExpression());

        TransferObjectRelation containmentCollectionDerived = assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived");
        assertFalse(containmentCollectionDerived.isRequired());
        assertThat(containmentCollectionDerived.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentCollectionDerivedProperty = assertNavigationProperty("_EntityAncestor", "containmentCollectionDerived");
        assertEquals(containmentCollectionDerivedProperty, containmentCollectionDerived.getBinding());
        assertEquals("self.containmentCollection", containmentCollectionDerivedProperty.getGetterExpression().getExpression());

        TransferObjectRelation containmentCollectionDerived2 = assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2");
        assertFalse(containmentCollectionDerived2.isRequired());
        assertThat(containmentCollectionDerived2.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(containmentCollectionDerived2.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentCollectionDerived2Property = assertNavigationProperty("_Entity", "containmentCollectionDerived2");
        assertEquals(containmentCollectionDerived2Property, containmentCollectionDerived2.getBinding());
        assertEquals("self.containmentCollection", containmentCollectionDerived2Property.getGetterExpression().getExpression());

        assertMappedTransferObject("AutoMappedRelated");
        assertEquals(4, assertMappedTransferObject("AutoMappedRelated").getRelations().size());
        assertEquals(1, assertMappedTransferObject("AutoMappedRelated").getAttributes().size());

        TransferAttribute integerAttribute = assertMappedTransferObjectAttribute("AutoMappedRelated", "attribute");
        assertFalse(integerAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), integerAttribute.getDataType());
        assertEquals(assertAttribute("_EntityRelated", "attribute"), integerAttribute.getBinding());

        TransferObjectRelation twoWayAssociationRelated = assertMappedTransferObjectRelation("AutoMappedRelated", "twoWayAssociationRelated");
        assertFalse(twoWayAssociationRelated.isRequired());
        assertThat(twoWayAssociationRelated.getBinding(), IsEqual.equalTo(assertRelation("_EntityRelated", "twoWayAssociationRelated")));
        assertThat(twoWayAssociationRelated.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociationRelated.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(twoWayAssociationRelated.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMapped")));

        TransferObjectRelation twoWayAssociationRelatedCollection = assertMappedTransferObjectRelation("AutoMappedRelated", "twoWayAssociationRelatedCollection");
        assertFalse(twoWayAssociationRelatedCollection.isRequired());
        assertThat(twoWayAssociationRelatedCollection.getBinding(), IsEqual.equalTo(assertRelation("_EntityRelated", "twoWayAssociationRelatedCollection")));
        assertThat(twoWayAssociationRelatedCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociationRelatedCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(twoWayAssociationRelatedCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMapped")));

        TransferObjectRelation twoWayAssociationInjector = assertMappedTransferObjectRelation("AutoMappedRelated", "twoWayAssociationInjector");
        assertFalse(twoWayAssociationInjector.isRequired());
        assertThat(twoWayAssociationInjector.getBinding(), IsEqual.equalTo(assertRelation("_EntityRelated", "twoWayAssociationInjector")));
        assertThat(twoWayAssociationInjector.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociationInjector.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(twoWayAssociationInjector.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMapped")));

        TransferObjectRelation twoWayAssociationCollectionInjector = assertMappedTransferObjectRelation("AutoMappedRelated", "twoWayAssociationCollectionInjector");
        assertFalse(twoWayAssociationCollectionInjector.isRequired());
        assertThat(twoWayAssociationCollectionInjector.getBinding(), IsEqual.equalTo(assertRelation("_EntityRelated", "twoWayAssociationCollectionInjector")));
        assertThat(twoWayAssociationCollectionInjector.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(twoWayAssociationCollectionInjector.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(twoWayAssociationCollectionInjector.getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMapped")));

    }

}
