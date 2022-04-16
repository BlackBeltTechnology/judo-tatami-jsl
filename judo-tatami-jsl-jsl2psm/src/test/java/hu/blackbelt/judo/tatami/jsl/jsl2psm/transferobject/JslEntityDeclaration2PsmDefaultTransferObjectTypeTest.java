package hu.blackbelt.judo.tatami.jsl.jsl2psm.transferobject;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.StringType;
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
        return new Slf4jLog(log);
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

        
        jslModel = parser.getModelFromFiles(
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
