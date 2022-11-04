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
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.support.JslDslModelResourceSupport;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JslExpressionToJqlExpressionTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/derived/primitiveAccessor";

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
    void testDerivedExpressionModel() throws Exception {
        testName = "TestDerivedExpressionModel";

        jslModel = JslParser.getModelFromFiles(
                "TestDerivedExpressionModel",
                List.of(new File("src/test/resources/derived/TestDerivedExpressionModel.jsl"), new File("src/test/resources/derived/TestDerivedExpressionModelInherited.jsl"))
        );

        jslModelWrapper = JslDslModelResourceSupport.jslDslModelResourceSupportBuilder().resourceSet(jslModel.getResourceSet()).build();

        assertEquals("\"apple\"!length()",  jqlDerived("SalesPerson", "strLength", "", ""));

        assertEquals("self.lead.salesPerson!filter(d | d.lead.closed == false)!filter(d | d.lead.closed == false).stringLiteral!substring(1, 1)!length()",  jqlDerived("SalesPerson", "strLengthDerived3", "", ""));

        
        assertEquals("self.leads!count()",  jqlDerived("SalesPerson", "value", "", ""));

        assertEquals("self.leads!count() > 1", jqlDerived("SalesPerson", "t1", "", ""));

        assertEquals("self.leads!filter(lead | lead.value > 10 and lead.value < 100)", jqlDerived("SalesPerson", "leadsOver10", "", ""));
        
        assertEquals("self.leads!filter(lead | lead.value > 20 and lead.value < 50)", jqlDerived("SalesPerson", "leadsOver20", "", ""));
        
        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(lead | lead.value > 10 and lead.value < 100)", 
        		jqlDerived("SalesPerson", "leadsOver10Static", "", ""));
        
        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(lead | lead.value > 20 and lead.value < 50)", 
        		jqlDerived("SalesPerson", "leadsOver20Static", "", ""));
        
        assertEquals("self.leads!filter(lead | lead.value > (input.minLeadsBetween!isDefined() ? input.minLeadsBetween : 1) "
        		+ "and lead.value < (input.maxLeadsBetween!isDefined() ? input.maxLeadsBetween : 50))", 
        		jqlEntityQuery("SalesPerson", "leadsBetween", "", ""));
        
        assertEquals("self.leads!filter(lead | lead.value > (input.minLeadsOverMin!isDefined() ? input.minLeadsOverMin : 5) and lead.value < 100)", 
        		jqlEntityQuery("SalesPerson", "leadsOverWithMin", "", ""));

        assertEquals("self.leads!filter(lead | lead.value > input.minLeadsOverMin and lead.value < 100)", 
        		jqlEntityQuery("SalesPerson", "leadsOverWithMinWithoutDefault", "", ""));

        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(lead | lead.value > (input.minLeadsOverMin!isDefined() ? input.minLeadsOverMin : 5) "
        		+ "and lead.value < 100)", jqlEntityQuery("SalesPerson", "leadsOverWithMinStatic", "", ""));

        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(lead | lead.value > (input.minLeadsBetween!isDefined() ? input.minLeadsBetween : 1) "
        		+ "and lead.value < (input.maxLeadsBetween!isDefined() ? input.maxLeadsBetween : 50))", jqlStaticQuery("staticLeadsBetween", "", ""));
        
        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(lead | lead.value > (input.minLeadsOverMin!isDefined() ? input.minLeadsOverMin : 5) "
        		+ "and lead.value < 100)", jqlStaticQuery("staticLeadsOverWithMin", "", ""));

        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(lead | lead.value > (input.minLeadsBetween!isDefined() ? input.minLeadsBetween : 1) "
        		+ "and lead.value < (input.maxLeadsBetween!isDefined() ? input.maxLeadsBetween : 50)).salesPerson.leads"
        		+ "!filter(lead | lead.value > (input.minLeadsBetween!isDefined() ? input.minLeadsBetween : 1) "
        		+ "and lead.value < (input.maxLeadsBetween!isDefined() ? input.maxLeadsBetween : 50))", jqlStaticQuery("staticLeadsBetweenAndSalesPersonLeads", "", ""));
		
        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModelImport::EntityNamePrefix_LeadInherited_EntityNamePostfix!filter(lead | "
        		+ "lead.value > (input.minLeadsBetween!isDefined() ? input.minLeadsBetween : 1) and lead.value < (input.maxLeadsBetween!isDefined() ? input.maxLeadsBetween : 50))",
        		jqlStaticQuery("staticInheritedLeadsBetween", "EntityNamePrefix_", "_EntityNamePostfix"));
        
        assertEquals("self", jqlDerived("SalesPerson", "selfDerived", "", ""));
        assertEquals("TestDerivedExpressionModel::TestDerivedExpressionModel::Customer!any()", jqlDerived("SalesPerson", "anyCustomer", "", ""));
        assertEquals("\"\" + self.value + \"test\"", jqlDerived("SalesPerson", "stringConcat", "", ""));
        assertEquals("self.leads!count() > 0 ? self.leads!filter(lead | lead.closed)!count() / self.leads!count() : 0", jqlDerived("SalesPerson", "complex", "", ""));
        assertEquals("((1 + 2) * 3) / 4", jqlDerived("SalesPerson", "arithmetic", "", ""));

        assertEquals("`12:12:11.11`", jqlDerived("SalesPerson", "timeLiteral", "", ""));
        assertEquals("`2020-12-01T12:12:11.11Z`", jqlDerived("SalesPerson", "timestampLiteral", "", ""));
        assertEquals("`2020-12-01`", jqlDerived("SalesPerson", "dateLiteral", "", ""));
        assertEquals("\"String\nString2\"", jqlDerived("SalesPerson", "stringLiteral", "", ""));
        assertEquals("\"Raw\\n\"", jqlDerived("SalesPerson", "stringRawLiteral", "", ""));
        assertEquals("100.12", jqlDerived("SalesPerson", "decimalLiteral", "", ""));
        assertEquals("10 * 10 * 10", jqlDerived("SalesPerson", "powerOfTen", "", ""));

        assertEquals("(self.value!isDefined() ? self.value!abs()!abs() : 10!abs()!abs())", jqlDerived("Lead", "orElseValue", "", ""));
        assertEquals("(self.value!isDefined() ? (self.value!floor()!isDefined() ? self.value!floor()!abs() : 5!abs()) : (9!floor()!isDefined() ? 9!floor()!abs() : 5!abs()))", jqlDerived("Lead", "orElseValue2", "", ""));
        assertEquals("(self.value!isDefined() ? self.value!abs() : (9!isDefined() ? 9 : 5)!abs())", jqlDerived("Lead", "orElseValue3", "", ""));
        assertEquals("(TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(l | l.value < 1000)!any()!isDefined() ? TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!filter(l | l.value < 1000)!any() : TestDerivedExpressionModel::TestDerivedExpressionModel::Lead!any())", jqlDerived("Lead", "orElseValue4", "", ""));
        assertEquals("(self.value!isDefined() ? self.value : 10)", jqlDerived("Lead", "orElseValue5", "", ""));
}

    private String jqlDerived(String entity, String field, String entityNamePrefix, String entityNamePostfix) {
        return JslExpressionToJqlExpression.getJqlForDerived(
                jslModelWrapper.getStreamOfJsldslEntityDerivedDeclaration()
                        .filter(d -> d.getName().equals(field) &&
                                ((EntityDeclaration) d.eContainer()).getName().equals(entity))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Could not find entity: " + entity + " and field: " + field)), entityNamePrefix, entityNamePostfix
                        );
    }

    private String jqlEntityQuery(String entity, String field, String entityNamePrefix, String entityNamePostfix) {
        return JslExpressionToJqlExpression.getJqlForEntityQuery(
                jslModelWrapper.getStreamOfJsldslEntityQueryDeclaration()
                        .filter(d -> d.getName().equals(field) &&
                                ((EntityDeclaration) d.eContainer()).getName().equals(entity))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Could not find entity: " + entity + " and field: " + field)), entityNamePrefix, entityNamePostfix
                        );
    }

    private String jqlStaticQuery(String query, String entityNamePrefix, String entityNamePostfix) {
        return JslExpressionToJqlExpression.getJqlForStaticQuery(
                jslModelWrapper.getStreamOfJsldslQueryDeclaration()
                        .filter(d -> d.getName().equals(query))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Could not find query: " + query)), entityNamePrefix, entityNamePostfix
                        );
    }

}
