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
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
        testName = "TestCreateUnmappedTransferObjectType";

        
        jslModel = JslParser.getModelFromFiles(
                "UnmappedTransferObjectTypeModel",
                List.of(new File("src/test/resources/transferobject/TestCreateUnmappedTransferObjectTypeModel.jsl"))
        );

        transform();

        assertUnmappedTransferObject("Unmapped");
        assertEquals(3, assertUnmappedTransferObject("Unmapped").getAttributes().size());
        
        assertUnmappedTransferObjectAttribute("Unmapped", "transient");
        assertFalse(assertUnmappedTransferObjectAttribute("Unmapped", "transient").isRequired());
        assertEquals(assertStringType("String"), assertUnmappedTransferObjectAttribute("Unmapped", "transient").getDataType());

        assertUnmappedTransferObjectAttribute("Unmapped", "required");
        assertTrue(assertUnmappedTransferObjectAttribute("Unmapped", "required").isRequired());
        assertEquals(assertStringType("String"), assertUnmappedTransferObjectAttribute("Unmapped", "required").getDataType());

        assertUnmappedTransferObjectAttribute("Unmapped", "derived");
        assertFalse(assertUnmappedTransferObjectAttribute("Unmapped", "derived").isRequired());
        assertEquals(assertNumericType("Integer"), assertUnmappedTransferObjectAttribute("Unmapped", "derived").getDataType());

        StaticData derived = assertStaticData("_derived_Unmapped_Reads");
        assertEquals("UnmappedTransferObjectTypeModel::UnmappedTransferObjectTypeModel::_Entity!any().attribute", derived.getGetterExpression().getExpression());

        
        
    }
    
}
