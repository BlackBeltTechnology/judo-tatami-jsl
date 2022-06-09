package hu.blackbelt.judo.tatami.jsl.jsl2psm.derived;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
    @Disabled
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

        
        assertNavigationProperty("_LeadExtended", "keyCustomers");
        assertTrue(assertNavigationProperty("_LeadExtended", "keyCustomers").isCollection());
        assertEquals("self.customers!filter(c | c.iskey)", assertNavigationProperty("_LeadExtended", "keyCustomers").getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("LeadExtended", "keyCustomers");
        assertTrue(assertMappedTransferObjectRelation("LeadExtended", "keyCustomers").isCollection());
        assertEquals(assertNavigationProperty("_LeadExtended", "keyCustomers"), assertMappedTransferObjectRelation("LeadExtended", "keyCustomers").getBinding());
        
        assertNavigationProperty("_LeadExtended", "keyCustomer");
        assertFalse(assertNavigationProperty("_LeadExtended", "keyCustomer").isCollection());
        assertEquals("self.customers!filter(c | c.iskey)!any()", assertNavigationProperty("_LeadExtended", "keyCustomer").getGetterExpression().getExpression());

        assertMappedTransferObjectRelation("LeadExtended", "keyCustomer");
        assertFalse(assertMappedTransferObjectRelation("LeadExtended", "keyCustomer").isCollection());
        assertEquals(assertNavigationProperty("_LeadExtended", "keyCustomer"), assertMappedTransferObjectRelation("LeadExtended", "keyCustomer").getBinding());

    }
}
