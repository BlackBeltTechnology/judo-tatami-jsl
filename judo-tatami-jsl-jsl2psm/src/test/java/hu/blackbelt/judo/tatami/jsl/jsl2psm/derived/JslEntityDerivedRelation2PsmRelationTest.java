package hu.blackbelt.judo.tatami.jsl.jsl2psm.derived;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        return new Slf4jLog(log);
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

        jslModel = parser.getModelFromFiles(
                "DerivedRelationModel",
                List.of(new File("src/test/resources/derived/TestDerivedRelationModel.jsl"))
        );

        transform();

        
        final Set<NavigationProperty> navigationProperties = psmModelWrapper.getStreamOfPsmDerivedNavigationProperty().collect(Collectors.toSet());
        assertEquals(2, navigationProperties.size());
        final Optional<NavigationProperty> keyCustomers = navigationProperties.stream().filter(n -> n.getName().equals("keyCustomers")).findFirst();
        assertTrue(keyCustomers.isPresent());
        assertTrue(keyCustomers.get().isCollection());
        assertEquals("self.customers!filter(c|c.isKey)", keyCustomers.get().getGetterExpression().getExpression());

        final Optional<NavigationProperty> keyCustomer = navigationProperties.stream().filter(n -> n.getName().equals("keyCustomer")).findFirst();
        assertTrue(keyCustomer.isPresent());
        assertTrue(keyCustomer.get().isCollection());
        assertEquals("self.customers!filter(c|c.isKey)!any()", keyCustomer.get().getGetterExpression().getExpression());

    }
}
