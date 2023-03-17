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
        assertEquals(5, assertMappedTransferObject("AutoMapped").getAttributes().size());

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

        
        assertEquals(8, assertMappedTransferObject("AutoMapped").getRelations().size());

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
        
    }
    
}
