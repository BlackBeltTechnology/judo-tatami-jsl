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
        assertEquals(6, assertMappedTransferObject("AutoMapped").getAttributes().size());

        assertMappedTransferObjectAttribute("AutoMapped", "attribute");
        assertFalse(assertMappedTransferObjectAttribute("AutoMapped", "attribute").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("AutoMapped", "attribute").getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "attribute"), assertMappedTransferObjectAttribute("AutoMapped", "attribute").getBinding());

        assertMappedTransferObjectAttribute("AutoMapped", "identifier");
        assertFalse(assertMappedTransferObjectAttribute("AutoMapped", "identifier").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("AutoMapped", "identifier").getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "identifier"), assertMappedTransferObjectAttribute("AutoMapped", "identifier").getBinding());
        
        assertMappedTransferObjectAttribute("AutoMapped", "attribute2");
        assertFalse(assertMappedTransferObjectAttribute("AutoMapped", "attribute2").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("AutoMapped", "attribute2").getDataType());
        assertEquals(assertAttribute("_Entity", "attribute2"), assertMappedTransferObjectAttribute("AutoMapped", "attribute2").getBinding());

        assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived");
        assertFalse(assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived").getDataType());
        DataProperty attributeDerived = assertDataProperty("_EntityAncestor", "attributeDerived");
        assertEquals(attributeDerived, assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived").getBinding());
        assertEquals("self.attribute", attributeDerived.getGetterExpression().getExpression());

        assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived2");
        assertFalse(assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived2").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived2").getDataType());
        DataProperty attributeDerived2 = assertDataProperty("_Entity", "attributeDerived2");
        assertEquals(attributeDerived2, assertMappedTransferObjectAttribute("AutoMapped", "attributeDerived2").getBinding());
        assertEquals("self.attribute", attributeDerived2.getGetterExpression().getExpression());

        
        assertEquals(8, assertMappedTransferObject("AutoMapped").getRelations().size());

        assertMappedTransferObjectRelation("AutoMapped", "containment");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containment").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment").getBinding(), IsEqual.equalTo(assertRelation("_EntityAncestor", "containment")));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        assertMappedTransferObjectRelation("AutoMapped", "containmentCollection");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection").getBinding(), IsEqual.equalTo(assertRelation("_EntityAncestor", "containmentCollection")));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        
        assertMappedTransferObjectRelation("AutoMapped", "containment2");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containment2").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment2").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "containment2")));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containment2").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        assertMappedTransferObjectRelation("AutoMapped", "containmentCollection2");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection2").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection2").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "containmentCollection2")));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollection2").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));

        assertMappedTransferObjectRelation("AutoMapped", "containmentDerived");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentDerived = assertNavigationProperty("_EntityAncestor", "containmentDerived");
        assertEquals(containmentDerived, assertMappedTransferObjectRelation("AutoMapped", "containmentDerived").getBinding());
        assertEquals("self.containment", containmentDerived.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("AutoMapped", "containmentDerived2");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived2").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived2").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentDerived2").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentDerived2 = assertNavigationProperty("_Entity", "containmentDerived2");
        assertEquals(containmentDerived2, assertMappedTransferObjectRelation("AutoMapped", "containmentDerived2").getBinding());
        assertEquals("self.containment", containmentDerived2.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentCollectionDerived = assertNavigationProperty("_EntityAncestor", "containmentCollectionDerived");
        assertEquals(containmentCollectionDerived, assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived").getBinding());
        assertEquals("self.containmentCollection", containmentCollectionDerived.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2").getTarget(), IsEqual.equalTo(assertMappedTransferObject("AutoMappedRelated")));
        NavigationProperty containmentCollectionDerived2 = assertNavigationProperty("_Entity", "containmentCollectionDerived2");
        assertEquals(containmentCollectionDerived2, assertMappedTransferObjectRelation("AutoMapped", "containmentCollectionDerived2").getBinding());
        assertEquals("self.containmentCollection", containmentCollectionDerived2.getGetterExpression().getExpression());
        
    }
    
}
