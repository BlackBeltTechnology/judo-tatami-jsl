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
public class JslMappedTranferObject2PsmTransferObjectTypeTest extends AbstractTest {
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
    void testCreateDefaultTransferObjectType() throws Exception {
        testName = "TestCreateMappedTransferObjectType";

        
        jslModel = JslParser.getModelFromFiles(
                "MappedTransferObjectTypeModel",
                List.of(new File("src/test/resources/transferobject/TestCreateMappedTransferObjectTypeModel.jsl"))
        );

        transform();

        
        assertMappedTransferObject("Mapped");
        assertEquals(4, assertMappedTransferObject("Mapped").getAttributes().size());

        assertMappedTransferObjectAttribute("Mapped", "unmappedAttribute");
        assertFalse(assertMappedTransferObjectAttribute("Mapped", "unmappedAttribute").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("Mapped", "unmappedAttribute").getDataType());

        assertMappedTransferObjectAttribute("Mapped", "mappedAttribute");
        assertFalse(assertMappedTransferObjectAttribute("Mapped", "mappedAttribute").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("Mapped", "mappedAttribute").getDataType());
        assertEquals(assertAttribute("_Entity", "attribute"), assertMappedTransferObjectAttribute("Mapped", "mappedAttribute").getBinding());

        assertMappedTransferObjectAttribute("Mapped", "derivedAttribute");
        assertFalse(assertMappedTransferObjectAttribute("Mapped", "derivedAttribute").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("Mapped", "derivedAttribute").getDataType());
        DataProperty derivedAttribute = assertDataProperty("_Entity", "_Reads_derivedAttribute_Mapped");
        assertEquals(derivedAttribute, assertMappedTransferObjectAttribute("Mapped", "derivedAttribute").getBinding());
        assertEquals("self.attribute", derivedAttribute.getGetterExpression().getExpression());

        assertMappedTransferObjectAttribute("Mapped", "derivedAttributeStatic");
        assertFalse(assertMappedTransferObjectAttribute("Mapped", "derivedAttributeStatic").isRequired());
        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("Mapped", "derivedAttributeStatic").getDataType());
        DataProperty derivedAttributeStatic = assertDataProperty("_Entity", "_Reads_derivedAttributeStatic_Mapped");
        assertEquals(derivedAttributeStatic, assertMappedTransferObjectAttribute("Mapped", "derivedAttributeStatic").getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_Entity!any().attribute", derivedAttributeStatic.getGetterExpression().getExpression());

        
        assertEquals(10, assertMappedTransferObject("Mapped").getRelations().size());
        assertMappedTransferObjectRelation("Mapped", "unmappedContainment");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "unmappedContainment").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainment").getBinding(), IsNull.nullValue());
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainment").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainment").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainment").getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));


        assertMappedTransferObjectRelation("Mapped", "unmappedContainmentRequired");
        assertTrue(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentRequired").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentRequired").getBinding(), IsNull.nullValue());
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentRequired").getCardinality().getLower(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentRequired").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentRequired").getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));


        assertMappedTransferObjectRelation("Mapped", "unmappedContainmentCollection");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentCollection").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentCollection").getBinding(), IsNull.nullValue());
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentCollection").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentCollection").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "unmappedContainmentCollection").getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));

        
        assertMappedTransferObjectRelation("Mapped", "mappedContainment");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "mappedContainment").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainment").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "containment")));
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainment").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainment").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainment").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));


        assertMappedTransferObjectRelation("Mapped", "mappedContainmentCollection");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "mappedContainmentCollection").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainmentCollection").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "containmentCollection")));
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainmentCollection").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainmentCollection").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedContainmentCollection").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        
// TODO: Fix association        
//        assertMappedTransferObjectRelation("Mapped", "mappedAssociation");
//        assertFalse(assertMappedTransferObjectRelation("Mapped", "mappedAssociation").isRequired());
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociation").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "association")));
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociation").getCardinality().getLower(), IsEqual.equalTo(0));
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociation").getCardinality().getUpper(), IsEqual.equalTo(1));
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociation").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));


//        assertMappedTransferObjectRelation("Mapped", "mappedAssociationOppostite");
//        assertFalse(assertMappedTransferObjectRelation("Mapped", "mappedAssociationOppostite").isRequired());
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociationOppostite").getBinding(), IsEqual.equalTo(assertRelation("_Entity", "entityRelatedOpposite")));
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociationOppostite").getCardinality().getLower(), IsEqual.equalTo(0));
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociationOppostite").getCardinality().getUpper(), IsEqual.equalTo(1));
//        assertThat(assertMappedTransferObjectRelation("Mapped", "mappedAssociationOppostite").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        
        assertMappedTransferObjectRelation("Mapped", "derivedContainment");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "derivedContainment").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainment").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainment").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainment").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainment = assertNavigationProperty("_Entity", "_Reads_derivedContainment_Mapped");
        assertEquals(derivedContainment, assertMappedTransferObjectRelation("Mapped", "derivedContainment").getBinding());
        assertEquals("self.containment", derivedContainment.getGetterExpression().getExpression());
        
        assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollection");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollection").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollection").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollection").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollection").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentCollection = assertNavigationProperty("_Entity", "_Reads_derivedContainmentCollection_Mapped");
        assertEquals(derivedContainmentCollection, assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollection").getBinding());
        assertEquals("self.containmentCollection", derivedContainmentCollection.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("Mapped", "derivedAssociation");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "derivedAssociation").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedAssociation").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedAssociation").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedAssociation").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedAssociation = assertNavigationProperty("_Entity", "_Reads_derivedAssociation_Mapped");
        assertEquals(derivedAssociation, assertMappedTransferObjectRelation("Mapped", "derivedAssociation").getBinding());
        assertEquals("self.association", derivedAssociation.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic").isRequired());
        assertFalse(assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic").getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentStatic = assertNavigationProperty("_Entity", "_Reads_derivedContainmentStatic_Mapped");
        assertEquals(derivedContainmentStatic, assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic").getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_EntityRelated!any()", derivedContainmentStatic.getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic");
        assertFalse(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic").isRequired());
        assertFalse(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic").isRequired());
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic").getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic").getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic").getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentCollectionStatic = assertNavigationProperty("_Entity", "_Reads_derivedContainmentCollectionStatic_Mapped");
        assertEquals(derivedContainmentCollectionStatic, assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic").getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_EntityRelated", derivedContainmentCollectionStatic.getGetterExpression().getExpression());

        
    }
    
}
