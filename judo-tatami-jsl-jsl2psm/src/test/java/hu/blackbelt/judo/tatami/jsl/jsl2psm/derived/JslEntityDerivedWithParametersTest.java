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
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEntityDerivedWithParametersTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/derived/parameters";

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
    void testEntityDerivedWithParametersTest() throws Exception {
        testName = "TestEntityDerivedWithParametersTest";

        jslModel = JslParser.getModelFromFiles(
                "TestDerivedWithParameters",
                List.of(new File("src/test/resources/derived/TestDerivedWithParametersModel.jsl"))
        );

        transform();


        final Set<NavigationProperty> navigationProperties = psmModelWrapper.getStreamOfPsmDerivedNavigationProperty().collect(Collectors.toSet());
        assertEquals(7, navigationProperties.size());

        assertNavigationProperty("_SalesPerson", "leadsBetween");
        assertTrue(assertNavigationProperty("_SalesPerson", "leadsBetween").isCollection());
        assertEquals(assertEntityType("_Lead"), assertNavigationProperty("_SalesPerson", "leadsBetween").getTarget());

        assertTrue(assertMappedTransferObjectRelation("SalesPerson", "leadsBetween").isCollection());
        assertEquals(assertMappedTransferObject("Lead"), assertMappedTransferObjectRelation("SalesPerson", "leadsBetween").getTarget());
        assertEquals(assertNavigationProperty("_SalesPerson", "leadsBetween"), assertMappedTransferObjectRelation("SalesPerson", "leadsBetween").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > (input.minLeadsBetween!isDefined() ? input.minLeadsBetween : 1) and lead.value < (input.maxLeadsBetween!isDefined() ? input.maxLeadsBetween : 50))",
                assertNavigationProperty("_SalesPerson", "leadsBetween").getGetterExpression().getExpression());


        assertNavigationProperty("_SalesPerson", "leadsOverWithMin");
        assertTrue(assertNavigationProperty("_SalesPerson", "leadsOverWithMin").isCollection());
        assertEquals(assertEntityType("_Lead"), assertNavigationProperty("_SalesPerson", "leadsOverWithMin").getTarget());

        assertTrue(assertMappedTransferObjectRelation("SalesPerson", "leadsOverWithMin").isCollection());
        assertEquals(assertMappedTransferObject("Lead"), assertMappedTransferObjectRelation("SalesPerson", "leadsOverWithMin").getTarget());
        assertEquals(assertNavigationProperty("_SalesPerson", "leadsOverWithMin"), assertMappedTransferObjectRelation("SalesPerson", "leadsOverWithMin").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > (input.minLeadsOverMin!isDefined() ? input.minLeadsOverMin : 5) and lead.value < 100)",
                assertNavigationProperty("_SalesPerson", "leadsOverWithMin").getGetterExpression().getExpression());


        assertNavigationProperty("_SalesPerson", "leadsOver10");
        assertTrue(assertNavigationProperty("_SalesPerson", "leadsOver10").isCollection());
        assertEquals(assertEntityType("_Lead"), assertNavigationProperty("_SalesPerson", "leadsOver10").getTarget());

        assertTrue(assertMappedTransferObjectRelation("SalesPerson", "leadsOver10").isCollection());
        assertEquals(assertMappedTransferObject("Lead"), assertMappedTransferObjectRelation("SalesPerson", "leadsOver10").getTarget());
        assertEquals(assertNavigationProperty("_SalesPerson", "leadsOver10"), assertMappedTransferObjectRelation("SalesPerson", "leadsOver10").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > 10 and lead.value < 100)",
                assertNavigationProperty("_SalesPerson", "leadsOver10").getGetterExpression().getExpression());


        assertNavigationProperty("_SalesPerson", "leadsOver20");
        assertTrue(assertNavigationProperty("_SalesPerson", "leadsOver20").isCollection());
        assertEquals(assertEntityType("_Lead"), assertNavigationProperty("_SalesPerson", "leadsOver20").getTarget());

        assertTrue(assertMappedTransferObjectRelation("SalesPerson", "leadsOver20").isCollection());
        assertEquals(assertMappedTransferObject("Lead"), assertMappedTransferObjectRelation("SalesPerson", "leadsOver20").getTarget());
        assertEquals(assertNavigationProperty("_SalesPerson", "leadsOver20"), assertMappedTransferObjectRelation("SalesPerson", "leadsOver20").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > 20 and lead.value < 50)",
                assertNavigationProperty("_SalesPerson", "leadsOver20").getGetterExpression().getExpression());


        final Set<DataProperty> dataProperties = psmModelWrapper.getStreamOfPsmDerivedDataProperty().collect(Collectors.toSet());
        assertEquals(7, dataProperties.size());

        assertDataProperty("_SalesPerson", "leadsBetweenCount");
        assertEquals(assertNumericType("Integer"), assertDataProperty("_SalesPerson", "leadsBetweenCount").getDataType());

        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("SalesPerson", "leadsBetweenCount").getDataType());
        assertEquals(assertDataProperty("_SalesPerson", "leadsBetweenCount"), assertMappedTransferObjectAttribute("SalesPerson", "leadsBetweenCount").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > (input.minLeadsBetween!isDefined() ? input.minLeadsBetween : 1) and lead.value < (input.maxLeadsBetween!isDefined() ? input.maxLeadsBetween : 50))!count()",
                assertDataProperty("_SalesPerson", "leadsBetweenCount").getGetterExpression().getExpression());


        assertDataProperty("_SalesPerson", "leadsOverWithMinCount");
        assertEquals(assertNumericType("Integer"), assertDataProperty("_SalesPerson", "leadsOverWithMinCount").getDataType());

        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("SalesPerson", "leadsOverWithMinCount").getDataType());
        assertEquals(assertDataProperty("_SalesPerson", "leadsOverWithMinCount"), assertMappedTransferObjectAttribute("SalesPerson", "leadsOverWithMinCount").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > (input.minLeadsOverMin!isDefined() ? input.minLeadsOverMin : 5) and lead.value < 100)!count()",
                assertDataProperty("_SalesPerson", "leadsOverWithMinCount").getGetterExpression().getExpression());


        assertDataProperty("_SalesPerson", "leadsOver10Count");
        assertEquals(assertNumericType("Integer"), assertDataProperty("_SalesPerson", "leadsOver10Count").getDataType());

        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("SalesPerson", "leadsOver10Count").getDataType());
        assertEquals(assertDataProperty("_SalesPerson", "leadsOver10Count"), assertMappedTransferObjectAttribute("SalesPerson", "leadsOver10Count").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > 10 and lead.value < 100)!count()",
                assertDataProperty("_SalesPerson", "leadsOver10Count").getGetterExpression().getExpression());

        assertDataProperty("_SalesPerson", "leadsOver20Count");
        assertEquals(assertNumericType("Integer"), assertDataProperty("_SalesPerson", "leadsOver20Count").getDataType());

        assertEquals(assertNumericType("Integer"), assertMappedTransferObjectAttribute("SalesPerson", "leadsOver20Count").getDataType());
        assertEquals(assertDataProperty("_SalesPerson", "leadsOver20Count"), assertMappedTransferObjectAttribute("SalesPerson", "leadsOver20Count").getBinding());

        assertEquals("self.leads!filter(lead | lead.value > 20 and lead.value < 50)!count()",
                assertDataProperty("_SalesPerson", "leadsOver20Count").getGetterExpression().getExpression());

        assertEquals("100000",
                assertMappedTransferObjectAttribute("Lead", "value").getDefaultValue().getGetterExpression().getExpression());
    }
}
