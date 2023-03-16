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
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;

import org.hamcrest.core.IsEqual;
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
        assertEquals(1, assertUnmappedTransferObject("Unmapped").getAttributes().size());
        
        TransferAttribute transientAttribute = assertUnmappedTransferObjectAttribute("Unmapped", "transient");
        assertFalse(transientAttribute.isRequired());
        assertEquals(assertStringType("String"), transientAttribute.getDataType());
        StaticData transientDefault = assertStaticData("_transient_Default_Unmapped");
        assertEquals("\"Test\"", transientDefault.getGetterExpression().getExpression());
        assertEquals(assertUnmappedTransferObjectAttribute("Unmapped", "transient").getDefaultValue(), transientDefault);

        
        assertMappedTransferObject("Mapped");
        assertEquals(3, assertMappedTransferObject("Mapped").getAttributes().size());

        TransferAttribute unmappedAttribute = assertMappedTransferObjectAttribute("Mapped", "unmappedAttribute");
        assertFalse(unmappedAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), unmappedAttribute.getDataType());
        StaticData unmappedAttributeDefault = assertStaticData("_unmappedAttribute_Default_Mapped");
        assertEquals("1", unmappedAttributeDefault.getGetterExpression().getExpression());
        assertEquals(unmappedAttribute.getDefaultValue(), unmappedAttributeDefault);

        
        TransferAttribute mappedAttribute = assertMappedTransferObjectAttribute("Mapped", "mappedAttribute");
        assertFalse(mappedAttribute.isRequired());
        assertEquals(assertNumericType("Integer"), mappedAttribute.getDataType());
        assertEquals(assertAttribute("_Entity", "attribute"), mappedAttribute.getBinding());
        DataProperty mappedAttributeDefault = assertDataProperty("_Entity", "_mappedAttribute_Default_Mapped");
        assertEquals("2", mappedAttributeDefault.getGetterExpression().getExpression());
        assertEquals(mappedAttributeDefault, mappedAttribute.getDefaultValue());


        TransferAttribute mappedIdentifier = assertMappedTransferObjectAttribute("Mapped", "mappedIdentifier");
        assertFalse(mappedIdentifier.isRequired());
        assertEquals(assertNumericType("Integer"), mappedIdentifier.getDataType());
        assertEquals(assertAttribute("_Entity", "identifier"), mappedIdentifier.getBinding());
        DataProperty mappedIdentifierDefault = assertDataProperty("_Entity", "_mappedIdentifier_Default_Mapped");
        assertEquals("3", mappedIdentifierDefault.getGetterExpression().getExpression());
        assertEquals(mappedIdentifierDefault, mappedIdentifier.getDefaultValue());

        assertEquals(4, assertMappedTransferObject("Mapped").getRelations().size());

        TransferObjectRelation mappedAssociation = assertMappedTransferObjectRelation("Mapped", "mappedAssociation");
        assertFalse(mappedAssociation.isRequired());
        assertThat(mappedAssociation.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "association")));
        assertThat(mappedAssociation.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociation.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(mappedAssociation.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));
//        DataProperty mappedIdentifierDefault = assertDataProperty("_Entity", "_mappedIdentifier_Default_Mapped");
//        assertEquals(mappedIdentifierDefault, assertMappedTransferObjectAttribute("Mapped", "_mappedIdentifier_Default_Mapped").getBinding());
//        assertEquals("3", mappedIdentifierDefault.getGetterExpression().getExpression());


        TransferObjectRelation mappedAssociationOpposite = assertMappedTransferObjectRelation("Mapped", "mappedAssociationOpposite");
        assertFalse(mappedAssociationOpposite.isRequired());
        assertThat(mappedAssociationOpposite.getBinding(), IsEqual.equalTo(assertRelation("_Entity", "entityRelatedOpposite")));
        assertThat(mappedAssociationOpposite.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(mappedAssociationOpposite.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(mappedAssociationOpposite.getTarget(), IsEqual.equalTo(assertMappedTransferObject("MappedRelated")));

        
        
        
    }
    
}
