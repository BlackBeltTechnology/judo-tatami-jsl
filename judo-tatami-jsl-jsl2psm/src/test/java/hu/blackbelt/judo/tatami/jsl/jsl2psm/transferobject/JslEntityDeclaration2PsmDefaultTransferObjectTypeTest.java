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
public class JslEntityDeclaration2PsmDefaultTransferObjectTypeTest extends AbstractTest {
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
        testName = "TestCreateDefaultTransferObjectType";


        jslModel = JslParser.getModelFromFiles(
                "DefaultTransferObjectTypeModel",
                List.of(new File("src/test/resources/transferobject/TestCreateDefaultTransferObjectTypeModel.jsl"))
        );

        transform();

        assertTrue(assertMappedTransferObject("Person").isAbstract());
        assertMappedTransferObject("PersonWithAge");

        assertMappedTransferObject("Named");
        assertEquals(1, assertMappedTransferObject("Named").getAttributes().size());

        assertMappedTransferObject("Customer");
        assertMappedTransferObject("Lead");
        assertMappedTransferObject("SalesPerson");
        assertEquals(4, assertMappedTransferObject("SalesPerson").getAttributes().size());


        assertMappedTransferObjectAttribute("SalesPerson", "id");
        assertFalse(assertMappedTransferObjectAttribute("SalesPerson", "id").isRequired());
        assertEquals(assertNumericType("Identifier"), assertMappedTransferObjectAttribute("SalesPerson", "id").getDataType());

        assertMappedTransferObjectAttribute("SalesPerson", "name");
        assertFalse(assertMappedTransferObjectAttribute("SalesPerson", "name").isRequired());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("SalesPerson", "name").getDataType());

        assertMappedTransferObjectAttribute("SalesPerson", "birthName");
        assertFalse(assertMappedTransferObjectAttribute("SalesPerson", "birthName").isRequired());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("SalesPerson", "birthName").getDataType());

        assertMappedTransferObjectAttribute("SalesPerson", "age");
        assertFalse(assertMappedTransferObjectAttribute("SalesPerson", "age").isRequired());
        assertEquals(assertNumericType("Age"), assertMappedTransferObjectAttribute("SalesPerson", "age").getDataType());

    }

}
