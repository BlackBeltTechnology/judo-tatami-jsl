package hu.blackbelt.judo.tatami.jsl.jsl2psm.error;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.Relation;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
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

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslErrorDeclaration2PsmUnmappedTransferObjectTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/error";

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
    void testCreateErrorType() throws Exception {
        testName = "TestCreateErrorType";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "ErrorTypeCreateModel",
                List.of("model ErrorTypeCreateModel\n" +
                        "\n" +
                        "type numeric Integer precision 9  scale 0\n" +
                        "type string String max-length 128\n" +
                        "\n" +
                        "error MyError {\n" +
                        "\tfield required Integer code\n" +
                        "\tfield String msg\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<UnmappedTransferObjectType> psmUnmappedTOTypes = psmModelWrapper.getStreamOfPsmServiceUnmappedTransferObjectType().collect(Collectors.toSet());
        assertEquals(1, psmUnmappedTOTypes.size());

        final Optional<UnmappedTransferObjectType> psmUnmappedTOMyError = psmUnmappedTOTypes.stream().filter(e -> e.getName().equals("MyError")).findAny();
        assertTrue(psmUnmappedTOMyError.isPresent());

        final Optional<NumericType> psmTypeInteger = psmModelWrapper.getStreamOfPsmTypeNumericType().filter(n -> n.getName().equals("Integer")).findFirst();
        assertTrue(psmTypeInteger.isPresent());

        final Optional<TransferAttribute> myErrorCode = psmUnmappedTOMyError.get().getAttributes().stream().filter(r -> r.getName().equals("code")).findFirst();
        assertTrue(myErrorCode.isPresent());
        assertTrue(myErrorCode.get().isRequired());
        assertEquals(psmTypeInteger.get(), myErrorCode.get().getDataType());

        final Optional<StringType> psmTypeString = psmModelWrapper.getStreamOfPsmTypeStringType().filter(n -> n.getName().equals("String")).findFirst();
        assertTrue(psmTypeString.isPresent());

        final Optional<TransferAttribute> myMsg = psmUnmappedTOMyError.get().getAttributes().stream().filter(r -> r.getName().equals("msg")).findFirst();
        assertTrue(myMsg.isPresent());
        assertFalse(myMsg.get().isRequired());
        assertEquals(psmTypeString.get(), myMsg.get().getDataType());
    }
}
