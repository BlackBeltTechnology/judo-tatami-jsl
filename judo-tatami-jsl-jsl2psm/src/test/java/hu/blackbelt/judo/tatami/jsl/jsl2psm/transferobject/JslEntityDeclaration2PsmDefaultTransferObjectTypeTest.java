package hu.blackbelt.judo.tatami.jsl.jsl2psm.transferobject;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
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

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "DefaultTransferObjectTypeModel",
                List.of("model DefaultTransferObjectTypeModel\n" +
                        "\n" +
                        "type string Name max-length 32\n" +
                        "\n" +
                        "entity Test {\n" +
                        "\tfield Name test\n" +
                        "}\n" +
                        "entity abstract Person {\n" +
                        "\tfield Name name\n" +
                        "}\n" +
                        "entity SalesPerson extends Person {\n" +
                        "}\n"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<StringType> psmTypeName = psmModelWrapper.getStreamOfPsmTypeStringType().filter(n -> n.getName().equals("Name")).findFirst();
        assertTrue(psmTypeName.isPresent());

        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(3, psmEntityTypes.size());

        final Set<MappedTransferObjectType> psmTOTypes = psmModelWrapper.getStreamOfPsmServiceMappedTransferObjectType().collect(Collectors.toSet());
        assertEquals(3, psmTOTypes.size());

        final Set<String> psmTOTypeNames = psmTOTypes.stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("Test", "Person", "SalesPerson");
        assertThat(psmTOTypeNames, IsEqual.equalTo(jslEntityTypeDeclarationNames.stream().map(n -> n + "DefaultTransferObject").collect(Collectors.toSet())));

        final Optional<MappedTransferObjectType> psmDefaultTOPerson = psmTOTypes.stream().filter(e -> e.getName().equals("PersonDefaultTransferObject")).findAny();
        assertTrue(psmDefaultTOPerson.isPresent());
        assertTrue(psmDefaultTOPerson.get().isAbstract());

        final Optional<MappedTransferObjectType> psmDefaultTOTest = psmTOTypes.stream().filter(e -> e.getName().equals("TestDefaultTransferObject")).findAny();
        assertTrue(psmDefaultTOTest.isPresent());

        final List<TransferAttribute> psmTestDefaultTOAttributes = psmDefaultTOTest.get().getAttributes();
        assertEquals(1, psmTestDefaultTOAttributes.size());

        final TransferAttribute psmDefaultTOAttributeTest = psmTestDefaultTOAttributes.get(0);
        assertEquals("test", psmDefaultTOAttributeTest.getName());
        assertFalse(psmDefaultTOAttributeTest.isRequired());
        assertEquals(psmTypeName.get(), psmDefaultTOAttributeTest.getDataType());

//        final Optional<MappedTransferObjectType> psmDefaultTOSalesPerson = psmTOTypes.stream().filter(e -> e.getName().equals("SalesPersonDefaultTransferObject")).findAny();
//        assertTrue(psmDefaultTOSalesPerson.isPresent());
//
//        final List<TransferAttribute> psmSalesPersonAttributes = psmDefaultTOSalesPerson.get().getAttributes();
//        assertEquals(1, psmSalesPersonAttributes.size());
    }
}
