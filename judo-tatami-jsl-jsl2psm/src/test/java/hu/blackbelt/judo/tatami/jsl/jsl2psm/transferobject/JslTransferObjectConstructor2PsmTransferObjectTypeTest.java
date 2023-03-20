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
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
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
import static org.mockito.ArgumentMatchers.isNull;

@Slf4j
public class JslTransferObjectConstructor2PsmTransferObjectTypeTest extends AbstractTest {
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
    void testTransferObjectConstructorModel() throws Exception {
        testName = "TestTransferObjectConstructor";

        
        jslModel = JslParser.getModelFromFiles(
                "TransferObjectConstructorModel",
                List.of(new File("src/test/resources/transferobject/TestTransferObjectConstructorModel.jsl"))
        );

        transform();

        
        assertUnmappedTransferObject("Unmapped");
        assertEquals(2, assertUnmappedTransferObject("Unmapped").getAttributes().size());
        
        TransferAttribute transientAttribute = assertUnmappedTransferObjectAttribute("Unmapped", "transient");
        assertFalse(transientAttribute.isRequired());
        assertEquals(assertStringType("String"), transientAttribute.getDataType());
        StaticData transientDefault = assertStaticData("_transient_Default_Unmapped");
        assertEquals("\"Test\"", transientDefault.getGetterExpression().getExpression());
        assertEquals(transientAttribute.getDefaultValue(), transientDefault);
        TransferAttribute transientDefaultAttribute = assertUnmappedTransferObjectAttribute("Unmapped", "_transient_Default_Unmapped");
        assertEquals(transientDefaultAttribute.getBinding(), transientDefault);

        
        assertMappedTransferObject("Mapped");
        assertEquals(20, assertMappedTransferObject("Mapped").getAttributes().size());

        TransferAttribute unmappedAttribute = assertMappedTransferObjectAttribute("Mapped", "unmappedAttribute");
        assertFalse(unmappedAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), unmappedAttribute.getDataType());
        DataProperty unmappedAttributeDefault = assertDataProperty("_Entity", "_unmappedAttribute_Default_Mapped");
        assertEquals("1", unmappedAttributeDefault.getGetterExpression().getExpression());
        assertEquals(unmappedAttribute.getDefaultValue(), unmappedAttributeDefault);
        TransferAttribute unmappedAttributeDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_unmappedAttribute_Default_Mapped");
        assertEquals(unmappedAttributeDefaultAttribute.getBinding(), unmappedAttributeDefault);

        
        TransferAttribute mappedAttribute = assertMappedTransferObjectAttribute("Mapped", "mappedAttribute");
        assertFalse(mappedAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), mappedAttribute.getDataType());
        assertEquals(assertAttribute("_Entity", "attribute"), mappedAttribute.getBinding());
        DataProperty mappedAttributeDefault = assertDataProperty("_Entity", "_mappedAttribute_Default_Mapped");
        assertEquals("2", mappedAttributeDefault.getGetterExpression().getExpression());
        assertEquals(mappedAttributeDefault, mappedAttribute.getDefaultValue());
        TransferAttribute mappedAttributeDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedAttribute_Default_Mapped");
        assertEquals(mappedAttributeDefaultAttribute.getBinding(), mappedAttributeDefault);

        TransferAttribute mappedIdentifier = assertMappedTransferObjectAttribute("Mapped", "mappedIdentifier");
        assertFalse(mappedIdentifier.isRequired());
        assertEquals(assertNumericType("Integer"), mappedIdentifier.getDataType());
        assertEquals(assertAttribute("_Entity", "identifier"), mappedIdentifier.getBinding());
        DataProperty mappedIdentifierDefault = assertDataProperty("_Entity", "_mappedIdentifier_Default_Mapped");
        assertEquals("3", mappedIdentifierDefault.getGetterExpression().getExpression());
        assertEquals(mappedIdentifierDefault, mappedIdentifier.getDefaultValue());
        TransferAttribute mappedIdentifierDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedIdentifier_Default_Mapped");
        assertEquals(mappedIdentifierDefaultAttribute.getBinding(), mappedIdentifierDefault);

        TransferAttribute mappedEnum = assertMappedTransferObjectAttribute("Mapped", "mappedEnum");
        assertFalse(mappedEnum.isRequired());
        assertEquals(assertEnumerationType("MyEnum"), mappedEnum.getDataType());
        assertEquals(assertAttribute("_Entity", "enum"), mappedEnum.getBinding());
        DataProperty mappedEnumDefault = assertDataProperty("_Entity", "_mappedEnum_Default_Mapped");
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::MyEnum#Crazy", mappedEnumDefault.getGetterExpression().getExpression());
        assertEquals(mappedEnumDefault, mappedEnum.getDefaultValue());
        TransferAttribute mappedEnumDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedEnum_Default_Mapped");
        assertEquals(mappedEnumDefaultAttribute.getBinding(), mappedEnumDefault);

        TransferAttribute mappedAttributeEntityDefault = assertMappedTransferObjectAttribute("Mapped", "mappedAttributeEntityDefault");
        assertFalse(mappedAttributeEntityDefault.isRequired());
        assertEquals(assertNumericType("Integer"), mappedAttributeEntityDefault.getDataType());
        assertEquals(assertAttribute("_Entity", "attribute"), mappedAttributeEntityDefault.getBinding());
        DataProperty mappedAttributeEntityDefaultDefault = assertDataProperty("_Entity", "_attribute_Default_Entity");
        assertEquals("1", mappedAttributeEntityDefaultDefault.getGetterExpression().getExpression());
        assertEquals(mappedAttributeEntityDefaultDefault, mappedAttributeEntityDefault.getDefaultValue());
        TransferAttribute mappedAttributeEntityDefaultDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedAttributeEntityDefault_Default_Mapped");
        assertEquals(mappedAttributeEntityDefaultDefaultAttribute.getBinding(), mappedAttributeEntityDefaultDefault);

        TransferAttribute mappedIdentifierEntityDefault = assertMappedTransferObjectAttribute("Mapped", "mappedIdentifierEntityDefault");
        assertFalse(mappedIdentifierEntityDefault.isRequired());
        assertEquals(assertNumericType("Integer"), mappedIdentifierEntityDefault.getDataType());
        assertEquals(assertAttribute("_Entity", "identifier"), mappedIdentifierEntityDefault.getBinding());
        DataProperty mappedIdentifierEntityDefaultDefault = assertDataProperty("_Entity", "_identifier_Default_Entity");
        assertEquals("1", mappedIdentifierEntityDefaultDefault.getGetterExpression().getExpression());
        assertEquals(mappedIdentifierEntityDefaultDefault, mappedIdentifierEntityDefault.getDefaultValue());
        TransferAttribute mappedIdentifierEntityDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedIdentifierEntityDefault_Default_Mapped");
        assertEquals(mappedIdentifierEntityDefaultAttribute.getBinding(), mappedIdentifierEntityDefaultDefault);

        TransferAttribute mappedEnumEntityDefault = assertMappedTransferObjectAttribute("Mapped", "mappedEnumEntityDefault");
        assertFalse(mappedEnumEntityDefault.isRequired());
        assertEquals(assertEnumerationType("MyEnum"), mappedEnumEntityDefault.getDataType());
        assertEquals(assertAttribute("_Entity", "enum"), mappedEnumEntityDefault.getBinding());
        DataProperty mappedEnumEntityDefaultDefault = assertDataProperty("_Entity", "_enum_Default_Entity");
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::MyEnum#Bombastic", mappedEnumEntityDefaultDefault.getGetterExpression().getExpression());
        assertEquals(mappedEnumEntityDefaultDefault, mappedEnumEntityDefault.getDefaultValue());
        TransferAttribute mappedEnumEntityDefaultDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedEnumEntityDefault_Default_Mapped");
        assertEquals(mappedEnumEntityDefaultDefaultAttribute.getBinding(), mappedEnumEntityDefaultDefault);        

        TransferAttribute mappedEnumAncestorEntityDefault = assertMappedTransferObjectAttribute("Mapped", "mappedEnumAncestorEntityDefault");
        assertFalse(mappedEnumAncestorEntityDefault.isRequired());
        assertEquals(assertEnumerationType("MyEnum"), mappedEnumAncestorEntityDefault.getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "enumAncestor"), mappedEnumAncestorEntityDefault.getBinding());
        DataProperty mappedEnumAncestorEntityDefaultDefault = assertDataProperty("_EntityAncestor", "_enumAncestor_Default_EntityAncestor");
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::MyEnum#Atomic", mappedEnumAncestorEntityDefaultDefault.getGetterExpression().getExpression());
        assertEquals(mappedEnumAncestorEntityDefaultDefault, mappedEnumAncestorEntityDefault.getDefaultValue());
        TransferAttribute mappedEnumAncestorEntityDefaultDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedEnumAncestorEntityDefault_Default_Mapped");
        assertEquals(mappedEnumAncestorEntityDefaultDefaultAttribute.getBinding(), mappedEnumAncestorEntityDefaultDefault);        

        TransferAttribute mappedAttributeAncestorEntityDefault = assertMappedTransferObjectAttribute("Mapped", "mappedAttributeAncestorEntityDefault");
        assertFalse(mappedAttributeAncestorEntityDefault.isRequired());
        assertEquals(assertNumericType("Integer"), mappedAttributeAncestorEntityDefault.getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "attributeAncestor"), mappedAttributeAncestorEntityDefault.getBinding());
        DataProperty mappedAttributeAncestorEntityDefaultDefault = assertDataProperty("_EntityAncestor", "_attributeAncestor_Default_EntityAncestor");
        assertEquals("(-1)", mappedAttributeAncestorEntityDefaultDefault.getGetterExpression().getExpression());
        assertEquals(mappedAttributeAncestorEntityDefaultDefault, mappedAttributeAncestorEntityDefault.getDefaultValue());
        TransferAttribute mappedAttributeAncestorEntityDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedAttributeAncestorEntityDefault_Default_Mapped");
        assertEquals(mappedAttributeAncestorEntityDefaultAttribute.getBinding(), mappedAttributeAncestorEntityDefaultDefault);

        TransferAttribute mappedIdentifierAncestorEntityDefault = assertMappedTransferObjectAttribute("Mapped", "mappedIdentifierAncestorEntityDefault");
        assertFalse(mappedIdentifierAncestorEntityDefault.isRequired());
        assertEquals(assertNumericType("Integer"), mappedIdentifierAncestorEntityDefault.getDataType());
        assertEquals(assertAttribute("_EntityAncestor", "identifierAncestor"), mappedIdentifierAncestorEntityDefault.getBinding());
        DataProperty mappedIdentifierAncestorEntityDefaultDefault = assertDataProperty("_EntityAncestor", "_identifierAncestor_Default_EntityAncestor");
        assertEquals("(-1)", mappedIdentifierAncestorEntityDefaultDefault.getGetterExpression().getExpression());
        assertEquals(mappedIdentifierAncestorEntityDefaultDefault, mappedIdentifierAncestorEntityDefault.getDefaultValue());
        TransferAttribute mappedIdentifierAncestorEntityDefaultAttribute = assertMappedTransferObjectAttribute("Mapped", "_mappedIdentifierAncestorEntityDefault_Default_Mapped");
        assertEquals(mappedIdentifierAncestorEntityDefaultAttribute.getBinding(), mappedIdentifierAncestorEntityDefaultDefault);

        
        assertEquals(12, assertMappedTransferObject("Mapped").getRelations().size());

        TransferObjectRelation mappedAssociation = assertMappedTransferObjectRelation("Mapped", "mappedAssociation");
        assertFalse(mappedAssociation.isRequired());
        assertThat(mappedAssociation.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "association")));
        assertThat(mappedAssociation.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociation.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(mappedAssociation.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty mappedAssociationDefault = assertNavigationProperty("_Entity", "_mappedAssociation_Default_Mapped");
        assertEquals(mappedAssociationDefault, mappedAssociation.getDefaultValue());
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::_EntityRelated!any()", mappedAssociationDefault.getGetterExpression().getExpression());
        TransferObjectRelation mappedAssociationDefaultAttribute = assertMappedTransferObjectRelation("Mapped", "_mappedAssociation_Default_Mapped");
        assertEquals(mappedAssociationDefaultAttribute.getBinding(), mappedAssociationDefault);

        TransferObjectRelation mappedAssociationCollection = assertMappedTransferObjectRelation("Mapped", "mappedAssociationCollection");
        assertFalse(mappedAssociationCollection.isRequired());
        assertThat(mappedAssociationCollection.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "associationCollection")));
        assertThat(mappedAssociationCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociationCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(mappedAssociationCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty mappedAssociationCollectionDefault = assertNavigationProperty("_Entity", "_mappedAssociationCollection_Default_Mapped");
        assertEquals(mappedAssociationCollectionDefault, mappedAssociationCollection.getDefaultValue());
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::_EntityRelated", mappedAssociationCollectionDefault.getGetterExpression().getExpression());
        TransferObjectRelation mappedAssociationCollectionDefaultAttribute = assertMappedTransferObjectRelation("Mapped", "_mappedAssociationCollection_Default_Mapped");
        assertEquals(mappedAssociationCollectionDefaultAttribute.getBinding(), mappedAssociationCollectionDefault);
        
        TransferObjectRelation mappedAssociationOpposite = assertMappedTransferObjectRelation("Mapped", "mappedAssociationOpposite");
        assertFalse(mappedAssociationOpposite.isRequired());
        assertThat(mappedAssociationOpposite.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "entityRelatedOpposite")));
        assertThat(mappedAssociationOpposite.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociationOpposite.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(mappedAssociationOpposite.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty mappedAssociationOppositeDefault = assertNavigationProperty("_Entity", "_mappedAssociationOpposite_Default_Mapped");
        assertEquals(mappedAssociationOppositeDefault, mappedAssociationOpposite.getDefaultValue());
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::_EntityRelated!any()", mappedAssociationOppositeDefault.getGetterExpression().getExpression());
        TransferObjectRelation mappedAssociationOppositeDefaultAttribute = assertMappedTransferObjectRelation("Mapped", "_mappedAssociationOpposite_Default_Mapped");
        assertEquals(mappedAssociationOppositeDefaultAttribute.getBinding(), mappedAssociationOppositeDefault);

        TransferObjectRelation mappedAssociationOppositeCollection = assertMappedTransferObjectRelation("Mapped", "mappedAssociationOppositeCollection");
        assertFalse(mappedAssociationOppositeCollection.isRequired());
        assertThat(mappedAssociationOppositeCollection.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "entityRelatedOppositeCollection")));
        assertThat(mappedAssociationOppositeCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociationOppositeCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(mappedAssociationOppositeCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty mappedAssociationOppositeCollectionDefault = assertNavigationProperty("_Entity", "_mappedAssociationOppositeCollection_Default_Mapped");
        assertEquals(mappedAssociationOppositeCollectionDefault, mappedAssociationOppositeCollection.getDefaultValue());
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::_EntityRelated", mappedAssociationOppositeCollectionDefault.getGetterExpression().getExpression());
        TransferObjectRelation mappedAssociationOppositeCollectionDefaultAttribute = assertMappedTransferObjectRelation("Mapped", "_mappedAssociationOppositeCollection_Default_Mapped");
        assertEquals(mappedAssociationOppositeCollectionDefaultAttribute.getBinding(),mappedAssociationOppositeCollectionDefault);

        TransferObjectRelation transientRelation = assertMappedTransferObjectRelation("Mapped", "transientRelation");
        assertFalse(transientRelation.isRequired());
        assertThat(transientRelation.getBinding(), IsNull.nullValue());
        assertThat(transientRelation.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(transientRelation.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(transientRelation.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty transientRelationDefault = assertNavigationProperty("_Entity", "_transientRelation_Default_Mapped");
        assertEquals(transientRelationDefault, transientRelation.getDefaultValue());
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::_EntityRelated!any()", transientRelationDefault.getGetterExpression().getExpression());
        TransferObjectRelation transientRelationDefaultAttribute = assertMappedTransferObjectRelation("Mapped", "_transientRelation_Default_Mapped");
        assertEquals(transientRelationDefaultAttribute.getBinding(),transientRelationDefault);

        TransferObjectRelation transientRelationCollection = assertMappedTransferObjectRelation("Mapped", "transientRelationCollection");
        assertFalse(transientRelationCollection.isRequired());
        assertThat(transientRelationCollection.getBinding(), IsNull.nullValue());
        assertThat(transientRelationCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(transientRelationCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(transientRelationCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty transientRelationCollectionDefault = assertNavigationProperty("_Entity", "_transientRelationCollection_Default_Mapped");
        assertEquals(transientRelationCollectionDefault, transientRelationCollection.getDefaultValue());
        assertEquals("TransferObjectConstructorModel::TransferObjectConstructorModel::_EntityRelated", transientRelationCollectionDefault.getGetterExpression().getExpression());
        TransferObjectRelation transientRelationCollectionDefaultAttribute = assertMappedTransferObjectRelation("Mapped", "_transientRelationCollection_Default_Mapped");
        assertEquals(transientRelationCollectionDefaultAttribute.getBinding(),transientRelationCollectionDefault);

    }
    
}
