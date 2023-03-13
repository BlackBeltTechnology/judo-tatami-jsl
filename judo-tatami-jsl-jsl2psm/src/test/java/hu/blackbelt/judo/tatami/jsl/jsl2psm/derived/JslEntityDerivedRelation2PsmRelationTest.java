package hu.blackbelt.judo.tatami.jsl.jsl2psm.derived;

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

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslEntityDerivedRelation2PsmRelationTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/derived/derivedRelation";

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
    void testDerivedRelationDeclarationModel() throws Exception {
        testName = "TestDerivedRelationModel";

        jslModel = JslParser.getModelFromFiles(
                "DerivedRelationModel",
                List.of(new File("src/test/resources/derived/TestDerivedRelationModel.jsl"))
        );

        transform();

        
        final Set<NavigationProperty> navigationProperties = psmModelWrapper.getStreamOfPsmDerivedNavigationProperty().collect(Collectors.toSet());
        assertEquals(2, navigationProperties.size());

        assertNavigationProperty("_Lead", "keyCustomers");
        assertTrue(assertNavigationProperty("_Lead", "keyCustomers").isCollection());
        assertEquals("self.customers!filter(c | c.iskey)", assertNavigationProperty("_Lead", "keyCustomers").getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("Lead", "keyCustomers");
        assertTrue(assertMappedTransferObjectRelation("Lead", "keyCustomers").isCollection());
        assertEquals(assertNavigationProperty("_Lead", "keyCustomers"), assertMappedTransferObjectRelation("Lead", "keyCustomers").getBinding());
        
        assertNavigationProperty("_Lead", "keyCustomer");
        assertFalse(assertNavigationProperty("_Lead", "keyCustomer").isCollection());
        assertEquals("self.customers!filter(c | c.iskey)!any()", assertNavigationProperty("_Lead", "keyCustomer").getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("Lead", "keyCustomer");
        assertFalse(assertMappedTransferObjectRelation("Lead", "keyCustomer").isCollection());
        assertEquals(assertNavigationProperty("_Lead", "keyCustomer"), assertMappedTransferObjectRelation("Lead", "keyCustomer").getBinding());

        
        assertAllNavigationProperty("_LeadExtended", "keyCustomers");
        assertTrue(assertAllNavigationProperty("_LeadExtended", "keyCustomers").isCollection());
        assertEquals("self.customers!filter(c | c.iskey)", assertAllNavigationProperty("_LeadExtended", "keyCustomers").getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("LeadExtended", "keyCustomers");
        assertTrue(assertMappedTransferObjectRelation("LeadExtended", "keyCustomers").isCollection());
        assertEquals(assertAllNavigationProperty("_LeadExtended", "keyCustomers"), assertMappedTransferObjectRelation("LeadExtended", "keyCustomers").getBinding());
        
        assertAllNavigationProperty("_LeadExtended", "keyCustomer");
        assertFalse(assertAllNavigationProperty("_LeadExtended", "keyCustomer").isCollection());
        assertEquals("self.customers!filter(c | c.iskey)!any()", assertAllNavigationProperty("_LeadExtended", "keyCustomer").getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("LeadExtended", "keyCustomer");
        assertFalse(assertMappedTransferObjectRelation("LeadExtended", "keyCustomer").isCollection());
        assertEquals(assertAllNavigationProperty("_LeadExtended", "keyCustomer"), assertMappedTransferObjectRelation("LeadExtended", "keyCustomer").getBinding());

    }
}
