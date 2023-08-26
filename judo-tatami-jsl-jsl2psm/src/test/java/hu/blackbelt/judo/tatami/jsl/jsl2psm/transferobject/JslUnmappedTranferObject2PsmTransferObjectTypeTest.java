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
import hu.blackbelt.judo.meta.psm.derived.StaticData;
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
public class JslUnmappedTranferObject2PsmTransferObjectTypeTest extends AbstractTest {
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
    void testCreateUnmappedTransferObjectType() throws Exception {
        testName = "TestCreateUnmappedTransferObjectType";


        jslModel = JslParser.getModelFromFiles(
                "UnmappedTransferObjectTypeModel",
                List.of(new File("src/test/resources/transferobject/TestCreateUnmappedTransferObjectTypeModel.jsl"))
        );

        transform();

        assertUnmappedTransferObject("Unmapped");
        assertEquals(3, assertUnmappedTransferObject("Unmapped").getAttributes().size());
        
        TransferAttribute transientAtttribute = assertUnmappedTransferObjectAttribute("Unmapped", "transient");
        assertFalse(transientAtttribute.isRequired());
        assertEquals(assertStringType("String"), transientAtttribute.getDataType());

        TransferAttribute required = assertUnmappedTransferObjectAttribute("Unmapped", "required");
        assertTrue(required.isRequired());
        assertEquals(assertStringType("String"), required.getDataType());

        TransferAttribute derived = assertUnmappedTransferObjectAttribute("Unmapped", "derived");
        assertFalse(derived.isRequired());
        assertEquals(assertNumericType("Integer"), derived.getDataType());

        StaticData derivedProperty = assertStaticData("_derived_Reads_Unmapped");
        assertEquals(derivedProperty, derived.getBinding());
        assertEquals("UnmappedTransferObjectTypeModel::UnmappedTransferObjectTypeModel::_Entity!any().attribute", derivedProperty.getGetterExpression().getExpression());

        assertEquals(4, assertUnmappedTransferObject("Unmapped").getRelations().size());

        TransferObjectRelation unmappedRelated = assertUnmappedTransferObjectRelation("Unmapped", "unmappedRelated");
        assertThat(unmappedRelated.getBinding(), IsNull.nullValue());
        assertThat(unmappedRelated.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(unmappedRelated.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(unmappedRelated.getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));

        TransferObjectRelation unmappedRelatedRequired = assertUnmappedTransferObjectRelation("Unmapped", "unmappedRelatedRequired");
        assertThat(unmappedRelatedRequired.getBinding(), IsNull.nullValue());
        assertThat(unmappedRelatedRequired.getCardinality().getLower(), IsEqual.equalTo(1));
        assertThat(unmappedRelatedRequired.getCardinality().getUpper(), IsEqual.equalTo(1));
        assertThat(unmappedRelatedRequired.getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));

        TransferObjectRelation unmappedRelatedCollection = assertUnmappedTransferObjectRelation("Unmapped", "unmappedRelatedCollection");
        assertThat(unmappedRelatedCollection.getBinding(), IsNull.nullValue());
        assertThat(unmappedRelatedCollection.getCardinality().getLower(), IsEqual.equalTo(0));
        assertThat(unmappedRelatedCollection.getCardinality().getUpper(), IsEqual.equalTo(-1));
        assertThat(unmappedRelatedCollection.getTarget(), IsEqual.equalTo(assertUnmappedTransferObject("UnmappedRelated")));


    }

}
