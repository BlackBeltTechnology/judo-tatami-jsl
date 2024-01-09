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

import org.slf4j.Logger;
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
    protected Logger createLog() {
        return new BufferedSlf4jLogger(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testCreateMappedTransferObjectType() throws Exception {
        testName = "TestCreateMappedTransferObjectType";


        jslModel = JslParser.getModelFromFiles(
                "MappedTransferObjectTypeModel",
                List.of(new File("src/test/resources/transferobject/TestCreateMappedTransferObjectTypeModel.jsl"))
        );

        transform();


        assertMappedTransferObject("Mapped");
        assertEquals(7, assertMappedTransferObject("Mapped").getAttributes().size());

        TransferAttribute unmappedAttribute = assertMappedTransferObjectAttribute("Mapped", "unmappedAttribute");
        assertFalse(unmappedAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), unmappedAttribute.getDataType());

        TransferAttribute mappedAttribute = assertMappedTransferObjectAttribute("Mapped", "mappedAttribute");
        assertFalse(mappedAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), mappedAttribute.getDataType());
        assertEquals(assertAttribute("_Entity", "attribute"), mappedAttribute.getBinding());

        TransferAttribute derivedAttribute = assertMappedTransferObjectAttribute("Mapped", "derivedAttribute");
        assertFalse(derivedAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), derivedAttribute.getDataType());
        assertEquals(assertDataProperty("_Entity", "_derivedAttribute_Reads_Mapped"), derivedAttribute.getBinding());

        TransferAttribute derivedAttributeStatic = assertMappedTransferObjectAttribute("Mapped", "derivedAttributeStatic");
        assertFalse(derivedAttributeStatic.isRequired());
        assertEquals(assertNumericType("Integer"), derivedAttributeStatic.getDataType());
        DataProperty derivedAttributeStaticProperty = assertDataProperty("_Entity", "_derivedAttributeStatic_Reads_Mapped");
        assertEquals(derivedAttributeStaticProperty, derivedAttributeStatic.getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_Entity!any().attribute", derivedAttributeStaticProperty.getGetterExpression().getExpression());

        TransferAttribute mappedAttributeDerived = assertMappedTransferObjectAttribute("Mapped", "mappedAttributeDerived");
        assertFalse(mappedAttributeDerived.isRequired());
        assertEquals(assertNumericType("Integer"), mappedAttributeDerived.getDataType());
        DataProperty mappedAttributeDerivedProperty = assertDataProperty("_Entity", "_mappedAttributeDerived_Reads_Mapped");
        assertEquals(mappedAttributeDerivedProperty, mappedAttributeDerived.getBinding());

        TransferAttribute mappedIdentifier = assertMappedTransferObjectAttribute("Mapped", "mappedIdentifier");
        assertFalse(mappedIdentifier.isRequired());
        assertEquals(assertNumericType("Integer"), mappedIdentifier.getDataType());
        assertEquals(assertAttribute("_Entity", "id"), mappedIdentifier.getBinding());

        
        TransferAttribute derivedIdentifier = assertMappedTransferObjectAttribute("Mapped", "derivedIdentifier");
        assertFalse(derivedIdentifier.isRequired());
        assertEquals(assertNumericType("Integer"), derivedIdentifier.getDataType());
        assertEquals(assertDataProperty("_Entity", "_derivedIdentifier_Reads_Mapped"), derivedIdentifier.getBinding());

        assertEquals(13, assertMappedTransferObject("Mapped").getRelations().size());

        
        TransferObjectRelation unmappedContainment = assertMappedTransferObjectRelation("Mapped", "unmappedContainment");
        assertFalse(unmappedContainment.isRequired());
        assertThat(unmappedContainment.getBinding(), IsNull.nullValue());
        assertThat(unmappedContainment.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(unmappedContainment.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(unmappedContainment.getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));


        TransferObjectRelation unmappedContainmentRequired = assertMappedTransferObjectRelation("Mapped", "unmappedContainmentRequired");
        assertTrue(unmappedContainmentRequired.isRequired());
        assertThat(unmappedContainmentRequired.getBinding(), IsNull.nullValue());
        assertThat(unmappedContainmentRequired.getCardinality().getLower(), IsEqual.equalTo(1));
        assertThat(unmappedContainmentRequired.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(unmappedContainmentRequired.getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));


        TransferObjectRelation unmappedContainmentCollection = assertMappedTransferObjectRelation("Mapped", "unmappedContainmentCollection");
        assertFalse(unmappedContainmentCollection.isRequired());
        assertThat(unmappedContainmentCollection.getBinding(), IsNull.nullValue());
        assertThat(unmappedContainmentCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(unmappedContainmentCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(unmappedContainmentCollection.getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));

        
        TransferObjectRelation mappedAssociation = assertMappedTransferObjectRelation("Mapped", "mappedAssociation");
        assertFalse(mappedAssociation.isRequired());
        assertThat(mappedAssociation.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "association")));
        assertThat(mappedAssociation.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociation.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(mappedAssociation.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        TransferObjectRelation mappedAssociationOpposite = assertMappedTransferObjectRelation("Mapped", "mappedAssociationOpposite");
        assertFalse(mappedAssociationOpposite.isRequired());
        assertThat(mappedAssociationOpposite.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "entityRelatedOpposite")));
        assertThat(mappedAssociationOpposite.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociationOpposite.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(mappedAssociationOpposite.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        TransferObjectRelation derivedContainment = assertMappedTransferObjectRelation("Mapped", "derivedContainment");
        assertEquals(assertNavigationProperty("_Entity", "_derivedContainment_Reads_Mapped"), derivedContainment.getBinding());
        assertFalse(derivedContainment.isRequired());
        assertThat(derivedContainment.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(derivedContainment.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(derivedContainment.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        
        TransferObjectRelation derivedContainmentCollection = assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollection");
        assertEquals(assertNavigationProperty("_Entity", "_derivedContainmentCollection_Reads_Mapped"), derivedContainmentCollection.getBinding());
        assertFalse(derivedContainmentCollection.isRequired());
        assertThat(derivedContainmentCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(derivedContainmentCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(derivedContainmentCollection.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        TransferObjectRelation derivedAssociation = assertMappedTransferObjectRelation("Mapped", "derivedAssociation");
        assertEquals(assertNavigationProperty("_Entity", "_derivedAssociation_Reads_Mapped"), derivedAssociation.getBinding());
        assertFalse(derivedAssociation.isRequired());
        assertThat(derivedAssociation.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(derivedAssociation.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(derivedAssociation.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        TransferObjectRelation derivedContainmentStatic = assertMappedTransferObjectRelation("Mapped", "derivedContainmentStatic");
        assertFalse(derivedContainmentStatic.isRequired());
        assertThat(derivedContainmentStatic.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(derivedContainmentStatic.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(derivedContainmentStatic.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentStaticProperty = assertNavigationProperty("_Entity", "_derivedContainmentStatic_Reads_Mapped");
        assertEquals(derivedContainmentStaticProperty, derivedContainmentStatic.getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_EntityRelated!any()", derivedContainmentStaticProperty.getGetterExpression().getExpression());

        TransferObjectRelation derivedContainmentCollectionStatic = assertMappedTransferObjectRelation("Mapped", "derivedContainmentCollectionStatic");
        assertFalse(derivedContainmentCollectionStatic.isRequired());
        assertThat(derivedContainmentCollectionStatic.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(derivedContainmentCollectionStatic.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(derivedContainmentCollectionStatic.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty derivedContainmentCollectionStaticProperty = assertNavigationProperty("_Entity", "_derivedContainmentCollectionStatic_Reads_Mapped");
        assertEquals(derivedContainmentCollectionStaticProperty, derivedContainmentCollectionStatic.getBinding());
        assertEquals("MappedTransferObjectTypeModel::MappedTransferObjectTypeModel::_EntityRelated", derivedContainmentCollectionStaticProperty.getGetterExpression().getExpression());

        TransferObjectRelation mappedContainmentDerived = assertMappedTransferObjectRelation("Mapped", "mappedContainmentDerived");
        assertFalse(mappedContainmentDerived.isRequired());
        assertThat(mappedContainmentDerived.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedContainmentDerived.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(mappedContainmentDerived.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty mappedContainmentDerivedProperty = assertNavigationProperty("_Entity", "_mappedContainmentDerived_Reads_Mapped");
        assertEquals(mappedContainmentDerivedProperty, mappedContainmentDerived.getBinding());
        assertEquals("self.containmentDerived", mappedContainmentDerivedProperty.getGetterExpression().getExpression());
        
        TransferObjectRelation mappedContainmentCollectionDerived = assertMappedTransferObjectRelation("Mapped", "mappedContainmentCollectionDerived");
        assertFalse(mappedContainmentCollectionDerived.isRequired());
        assertThat(mappedContainmentCollectionDerived.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedContainmentCollectionDerived.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(mappedContainmentCollectionDerived.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
        NavigationProperty mappedContainmentCollectionDerivedProperty = assertNavigationProperty("_Entity", "_mappedContainmentCollectionDerived_Reads_Mapped");
        assertEquals(mappedContainmentCollectionDerivedProperty, mappedContainmentCollectionDerived.getBinding());
        assertEquals("self.containmentCollectionDerived", mappedContainmentCollectionDerivedProperty.getGetterExpression().getExpression());

    }

}
