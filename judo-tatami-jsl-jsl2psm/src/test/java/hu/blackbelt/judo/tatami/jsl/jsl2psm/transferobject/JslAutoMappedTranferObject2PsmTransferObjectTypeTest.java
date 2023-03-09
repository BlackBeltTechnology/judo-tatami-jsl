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
        assertEquals(3, assertMappedTransferObject("AutoMapped").getAttributes().size());


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

        /*
        assertMappedTransferObjectAttribute("AutoMapped", "derivedAttribute");
        assertFalse(assertMappedTransferObjectAttribute("AutoMapped", "derivedAttribute").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("AutoMapped", "derivedAttribute").getDataType());
        DataProperty derivedAttribute = assertDataProperty("_Entity", "_derivedAttribute_Reads_Mapped");
        assertEquals(derivedAttribute, assertMappedTransferObjectAttribute("AutoMapped", "derivedAttribute").getBinding());
        assertEquals("self.attribute", derivedAttribute.getGetterExpression().getExpression());

        assertMappedTransferObjectAttribute("AutoMapped", "derivedAttributeStatic");
        assertFalse(assertMappedTransferObjectAttribute("AutoMapped", "derivedAttributeStatic").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("AutoMapped", "derivedAttributeStatic").getDataType());
        DataProperty derivedAttributeStatic = assertDataProperty("_Entity", "_derivedAttributeStatic_Reads_Mapped");
        assertEquals(derivedAttributeStatic, assertMappedTransferObjectAttribute("AutoMapped", "derivedAttributeStatic").getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_Entity!any().attribute", derivedAttributeStatic.getGetterExpression().getExpression());
		*/
        
        assertEquals(4, assertMappedTransferObject("AutoMapped").getRelations().size());

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

        /*
        
// TODO: JNG-4603 Fix association        
//        assertMappedTransferObjectRelation("AutoMapped", "mappedAssociation");
//        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociation").isRequired());
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociation").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "association")));
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociation").getCardinality().getLower(), IsEqual.equalTo(0));
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociation").getCardinality().getUpper(), IsEqual.equalTo(1));
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociation").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));


//        assertMappedTransferObjectRelation("AutoMapped", "mappedAssociationOppostite");
//        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociationOppostite").isRequired());
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociationOppostite").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "entityRelatedOpposite")));
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociationOppostite").getCardinality().getLower(), IsEqual.equalTo(0));
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociationOppostite").getCardinality().getUpper(), IsEqual.equalTo(1));
//        assertThat(assertMappedTransferObjectRelation("AutoMapped", "mappedAssociationOppostite").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        /*
        assertMappedTransferObjectRelation("AutoMapped", "derivedContainment");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "derivedContainment").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainment").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainment").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainment").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainment = assertNavigationProperty("_Entity", "_derivedContainment_Reads_Mapped");
        assertEquals(derivedContainment, assertMappedTransferObjectRelation("AutoMapped", "derivedContainment").getBinding());
        assertEquals("self.containment", derivedContainment.getGetterExpression().getExpression());
        
        assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollection");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollection").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollection").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollection").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollection").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentCollection = assertNavigationProperty("_Entity", "_derivedContainmentCollection_Reads_Mapped");
        assertEquals(derivedContainmentCollection, assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollection").getBinding());
        assertEquals("self.containmentCollection", derivedContainmentCollection.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("AutoMapped", "derivedAssociation");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "derivedAssociation").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedAssociation").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedAssociation").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedAssociation").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedAssociation = assertNavigationProperty("_Entity", "_derivedAssociation_Reads_Mapped");
        assertEquals(derivedAssociation, assertMappedTransferObjectRelation("AutoMapped", "derivedAssociation").getBinding());
        assertEquals("self.association", derivedAssociation.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentStatic");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentStatic").isRequired());
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentStatic").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentStatic").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentStatic").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentStatic").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentStatic = assertNavigationProperty("_Entity", "_derivedContainmentStatic_Reads_Mapped");
        assertEquals(derivedContainmentStatic, assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentStatic").getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_EntityRelated!any()", derivedContainmentStatic.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollectionStatic");
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollectionStatic").isRequired());
        assertFalse(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollectionStatic").isRequired());
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollectionStatic").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollectionStatic").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollectionStatic").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentCollectionStatic = assertNavigationProperty("_Entity", "_derivedContainmentCollectionStatic_Reads_Mapped");
        assertEquals(derivedContainmentCollectionStatic, assertMappedTransferObjectRelation("AutoMapped", "derivedContainmentCollectionStatic").getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_EntityRelated", derivedContainmentCollectionStatic.getGetterExpression().getExpression());
        */
        
    }
    
}
