package hu.blackbelt.judo.tatami.jsl.jsl2psm.derived;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        Optional<ModelDeclaration> model = parser.getModelFromFiles(
                "TestDerivedExpressionModel",
                List.of(new File("src/test/resources/derived/TestDerivedExpressionModel.jsl"))
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();


        assertEquals("self.leads!count()",  jql("SalesPerson", "value"));
        assertEquals("self.leads!count()>1", jql("SalesPerson", "t1"));

        assertEquals("self.leads!filter(lead|lead.value>limit)", jql("SalesPerson", "leadsOver"));
        assertEquals("self.leads(limit=10)", jql("SalesPerson", "leadsOver10"));
        assertEquals("self", jql("SalesPerson", "selfDerived"));
        assertEquals("Customer!any()", jql("SalesPerson", "anyCustomer"));
        assertEquals("\"\"+self.value+\"test\"", jql("SalesPerson", "stringConcat"));
        assertEquals("self.leads!count()>0?self.leads!filter(lead|lead.closed)!count()/self.leads!count():0", jql("SalesPerson", "complex"));
        assertEquals("((1+2)*3)/4", jql("SalesPerson", "arithmetic"));
    }

    private String jql(String entity, String field) {
        return JslExpressionToJqlExpression.getJql(
                jslModelWrapper.getStreamOfJsldslEntityDerivedDeclaration()
                        .filter(d -> d.getName().equals(field) &&
                                ((EntityDeclaration) d.eContainer()).getName().equals(entity))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Could not find entity: " + entity + " and field: " + field))
                        .getExpression());
    }

}
