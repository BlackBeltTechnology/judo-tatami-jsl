package hu.blackbelt.judo.tatami.jsl.jsl2psm.functions;

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
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslInstanceFunctionTranslationTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/functions";

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
    void testInstanceFunctionTest() throws Exception {
        testName = "TestInstanceFunctionModel";

        jslModel = JslParser.getModelFromFiles(
                "TestInstanceFunctionModel",
                List.of(new File("src/test/resources/function/TestInstanceFunctionModel.jsl"), new File("src/test/resources/function/ImportedTestInstanceFunctionModel.jsl"))
        );

        transform();

        final Set<NavigationProperty> navigationProperties = psmModelWrapper.getStreamOfPsmDerivedNavigationProperty().collect(Collectors.toSet());
        assertEquals(3, navigationProperties.size());

        assertNavigationProperty("_C", "allCfromAPlainName");
        assertFalse(assertNavigationProperty("_C", "allCfromAPlainName").isCollection());
        assertEquals(assertEntityType("_A"), assertNavigationProperty("_C", "allCfromAPlainName").getTarget());

        assertFalse(assertMappedTransferObjectRelation("C", "allCfromAPlainName").isCollection());
        assertEquals(assertMappedTransferObject("A"), assertMappedTransferObjectRelation("C", "allCfromAPlainName").getTarget());
        assertEquals(assertNavigationProperty("_C", "allCfromAPlainName"), assertMappedTransferObjectRelation("C", "allCfromAPlainName").getBinding());

        assertEquals("self!container(TestInstanceFunctionModel::TestInstanceFunctionModel::_A)",
                assertNavigationProperty("_C", "allCfromAPlainName").getGetterExpression().getExpression());


        assertNavigationProperty("_C", "allCfromAFqName");
        assertFalse(assertNavigationProperty("_C", "allCfromAFqName").isCollection());
        assertEquals(assertEntityType("_A"), assertNavigationProperty("_C", "allCfromAFqName").getTarget());

        assertFalse(assertMappedTransferObjectRelation("C", "allCfromAFqName").isCollection());
        assertEquals(assertMappedTransferObject("A"), assertMappedTransferObjectRelation("C", "allCfromAFqName").getTarget());
        assertEquals(assertNavigationProperty("_C", "allCfromAFqName"), assertMappedTransferObjectRelation("C", "allCfromAFqName").getBinding());

        assertEquals("self!container(TestInstanceFunctionModel::TestInstanceFunctionModel::_A)",
                assertNavigationProperty("_C", "allCfromAFqName").getGetterExpression().getExpression());


        assertNavigationProperty("_C", "allCfromAImport");
        assertFalse(assertNavigationProperty("_C", "allCfromAImport").isCollection());
        assertEquals(assertEntityType("_I"), assertNavigationProperty("_C", "allCfromAImport").getTarget());

        assertFalse(assertMappedTransferObjectRelation("C", "allCfromAImport").isCollection());
        assertEquals(assertMappedTransferObject("I"), assertMappedTransferObjectRelation("C", "allCfromAImport").getTarget());
        assertEquals(assertNavigationProperty("_C", "allCfromAImport"), assertMappedTransferObjectRelation("C", "allCfromAImport").getBinding());

        assertEquals("self!container(TestInstanceFunctionModel::ImportedTestInstanceFunctionModel::_I)",
                assertNavigationProperty("_C", "allCfromAImport").getGetterExpression().getExpression());

    }
}
