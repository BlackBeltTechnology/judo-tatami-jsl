package hu.blackbelt.judo.tatami.jsl.jsl2psm.derived;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.support.JslDslModelResourceSupport;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
        return new Slf4jLog(log);
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
                List.of(new File("src/test/resources/derived/TestDerivedExpressionModel.jsl"))
        );

        jslModelWrapper = JslDslModelResourceSupport.jslDslModelResourceSupportBuilder().resourceSet(jslModel.getResourceSet()).build();

//        transform();
/*
        assertEquals("self.leads!count()",  jql("SalesPerson", "value"));
        assertEquals("self.leads!count() > 1", jql("SalesPerson", "t1"));

//        assertEquals("self.leads!filter(lead | lead.value > input.limit!isDefined() ? input.limit : 100)", jql("SalesPerson", "leadsOver"));
        assertEquals("self.leads!filter(lead | lead.value > 100)", jql("SalesPerson", "leadsOver"));
*/              
        assertEquals("self.leads!filter(lead | lead.value > 10)", jql("SalesPerson", "leadsOver10"));
/*
        assertEquals("self", jql("SalesPerson", "selfDerived"));
        assertEquals("Customer!any()", jql("SalesPerson", "anyCustomer"));
        assertEquals("\"\" + self.value + \"test\"", jql("SalesPerson", "stringConcat"));
        assertEquals("self.leads!count() > 0 ? self.leads!filter(lead | lead.closed)!count() / self.leads!count() : 0", jql("SalesPerson", "complex"));
        assertEquals("((1 + 2) * 3) / 4", jql("SalesPerson", "arithmetic"));

        assertEquals("`12:12:11.11`", jql("SalesPerson", "timeLiteral"));
        assertEquals("`2020-12-01T12:12:11.11Z`", jql("SalesPerson", "timestampLiteral"));
        assertEquals("`2020-12-01`", jql("SalesPerson", "dateLiteral"));
        assertEquals("\"String\nString2\"", jql("SalesPerson", "stringLiteral"));
        assertEquals("\"Raw\\\\n\"", jql("SalesPerson", "stringRawLiteral"));
        assertEquals("100.12", jql("SalesPerson", "decimalLiteral"));
*/
    }

    private String jql(String entity, String field) {
        return JslExpressionToJqlExpression.getJql(
                jslModelWrapper.getStreamOfJsldslEntityDerivedDeclaration()
                        .filter(d -> d.getName().equals(field) &&
                                ((EntityDeclaration) d.eContainer()).getName().equals(entity))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Could not find entity: " + entity + " and field: " + field))
                        );
    }

}
