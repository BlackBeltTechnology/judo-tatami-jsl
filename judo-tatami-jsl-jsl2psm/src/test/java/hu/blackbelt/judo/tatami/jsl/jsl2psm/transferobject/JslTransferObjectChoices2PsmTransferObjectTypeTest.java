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
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.service.*;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslTransferObjectChoices2PsmTransferObjectTypeTest extends AbstractTest {
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
    void testTransferObjectChicesModel() throws Exception {

        
        jslModel = JslParser.getModelFromFiles(
                "TransferObjectChoicesModel",
                List.of(new File("src/test/resources/transferobject/TestTransferObjectChoicesModel.jsl"))
        );

        transform();
        
        assertUnmappedTransferObject("Unmapped");
        assertEquals(4, assertUnmappedTransferObject("Unmapped").getRelations().size());

        TransferObjectRelation unmappedTransientRelationWithDefinedChoices = assertUnmappedTransferObjectRelation("Unmapped", "unmappedTransientRelationWithDefinedChoices");      
        TransferObjectRelation unmappedTransientRelationWithDefinedChoicesRange = assertUnmappedTransferObjectRelation("Unmapped", "_unmappedTransientRelationWithDefinedChoices_RelationRange_Unmapped");        
        StaticNavigation unmappedTransientRelationWithDefinedChoicesStaticNavigation = assertStaticNavigation("_unmappedTransientRelationWithDefinedChoices_RelationRange_Unmapped");
        assertEquals("TransferObjectChoicesModel::TransferObjectChoicesModel::_EntityRelated", unmappedTransientRelationWithDefinedChoicesStaticNavigation.getGetterExpression().getExpression());
        assertEquals(unmappedTransientRelationWithDefinedChoicesStaticNavigation, unmappedTransientRelationWithDefinedChoices.getRange());
        assertEquals(unmappedTransientRelationWithDefinedChoicesStaticNavigation, unmappedTransientRelationWithDefinedChoicesRange.getBinding());

        TransferObjectRelation unmappedTransientRelationCollectionWithDefinedChoices = assertUnmappedTransferObjectRelation("Unmapped", "unmappedTransientRelationCollectionWithDefinedChoices");
        TransferObjectRelation unmappedTransientRelationCollectionWithDefinedChoicesRange = assertUnmappedTransferObjectRelation("Unmapped", "_unmappedTransientRelationCollectionWithDefinedChoices_RelationRange_Unmapped");
        StaticNavigation unmappedTransientRelationCollectionWithDefinedChoicesStaticNavigation = assertStaticNavigation("_unmappedTransientRelationCollectionWithDefinedChoices_RelationRange_Unmapped");
        assertEquals("TransferObjectChoicesModel::TransferObjectChoicesModel::_EntityRelated", unmappedTransientRelationWithDefinedChoicesStaticNavigation.getGetterExpression().getExpression());
        assertEquals(unmappedTransientRelationCollectionWithDefinedChoicesStaticNavigation, unmappedTransientRelationCollectionWithDefinedChoices.getRange());
        assertEquals(unmappedTransientRelationCollectionWithDefinedChoicesStaticNavigation, unmappedTransientRelationCollectionWithDefinedChoicesRange.getBinding());
        
        TransferObjectRelation mappedTransientRelationWithDefinedChoices = assertMappedTransferObjectRelation("Mapped", "mappedTransientRelationWithDefinedChoices");      
        TransferObjectRelation mappedTransientRelationWithDefinedChoicesRange = assertMappedTransferObjectRelation("Mapped", "_mappedTransientRelationWithDefinedChoices_RelationRange_Mapped");        
        NavigationProperty mappedTransientRelationWithDefinedChoicesNavigationProperty = assertNavigationProperty("_Entity", "_mappedTransientRelationWithDefinedChoices_RelationRange_Mapped");
        assertEquals("TransferObjectChoicesModel::TransferObjectChoicesModel::_EntityRelated", mappedTransientRelationWithDefinedChoicesNavigationProperty.getGetterExpression().getExpression());
        assertEquals(mappedTransientRelationWithDefinedChoicesNavigationProperty, mappedTransientRelationWithDefinedChoices.getRange());
        assertEquals(mappedTransientRelationWithDefinedChoicesNavigationProperty, mappedTransientRelationWithDefinedChoicesRange.getBinding());

        TransferObjectRelation mappedTransientRelationCollectionWithDefinedChoices = assertMappedTransferObjectRelation("Mapped", "mappedTransientRelationCollectionWithDefinedChoices");
        TransferObjectRelation mappedTransientRelationCollectionWithDefinedChoicesRange = assertMappedTransferObjectRelation("Mapped", "_mappedTransientRelationCollectionWithDefinedChoices_RelationRange_Mapped");        
        NavigationProperty mappedTransientRelationCollectionWithDefinedChoicesNavigationProperty = assertNavigationProperty("_Entity", "_mappedTransientRelationCollectionWithDefinedChoices_RelationRange_Mapped");
        assertEquals("TransferObjectChoicesModel::TransferObjectChoicesModel::_EntityRelated", mappedTransientRelationWithDefinedChoicesNavigationProperty.getGetterExpression().getExpression());
        assertEquals(mappedTransientRelationCollectionWithDefinedChoicesNavigationProperty, mappedTransientRelationCollectionWithDefinedChoices.getRange());
        assertEquals(mappedTransientRelationCollectionWithDefinedChoicesNavigationProperty, mappedTransientRelationCollectionWithDefinedChoicesRange.getBinding());

        TransferObjectRelation mappedAssociationWithDefinedChoices = assertMappedTransferObjectRelation("Mapped", "mappedAssociationWithDefinedChoices");
        TransferObjectRelation mappedAssociationWithDefinedChoicesRange = assertMappedTransferObjectRelation("Mapped", "_mappedAssociationWithDefinedChoices_RelationRange_Mapped");        
        NavigationProperty mappedAssociationWithDefinedChoicesNavigationProperty = assertNavigationProperty("_Entity", "_mappedAssociationWithDefinedChoices_RelationRange_Mapped");
        assertEquals("TransferObjectChoicesModel::TransferObjectChoicesModel::_EntityRelated", mappedAssociationWithDefinedChoicesNavigationProperty.getGetterExpression().getExpression());
        assertEquals(mappedAssociationWithDefinedChoicesNavigationProperty, mappedAssociationWithDefinedChoices.getRange());
        assertEquals(mappedAssociationWithDefinedChoicesNavigationProperty, mappedAssociationWithDefinedChoicesRange.getBinding());
        
        TransferObjectRelation mappedAssociationCollectionWithDefinedChoices = assertMappedTransferObjectRelation("Mapped", "mappedAssociationCollectionWithDefinedChoices");
        TransferObjectRelation mappedAssociationCollectionWithDefinedChoicesRange = assertMappedTransferObjectRelation("Mapped", "_mappedAssociationCollectionWithDefinedChoices_RelationRange_Mapped");        
        NavigationProperty mappedAssociationCollectionWithDefinedChoicesNavigationProperty = assertNavigationProperty("_Entity", "_mappedAssociationCollectionWithDefinedChoices_RelationRange_Mapped");
        assertEquals("TransferObjectChoicesModel::TransferObjectChoicesModel::_EntityRelated", mappedAssociationCollectionWithDefinedChoicesNavigationProperty.getGetterExpression().getExpression());
        assertEquals(mappedAssociationCollectionWithDefinedChoicesNavigationProperty, mappedAssociationCollectionWithDefinedChoices.getRange());
        assertEquals(mappedAssociationCollectionWithDefinedChoicesNavigationProperty, mappedAssociationCollectionWithDefinedChoicesRange.getBinding());

    }
    
}
