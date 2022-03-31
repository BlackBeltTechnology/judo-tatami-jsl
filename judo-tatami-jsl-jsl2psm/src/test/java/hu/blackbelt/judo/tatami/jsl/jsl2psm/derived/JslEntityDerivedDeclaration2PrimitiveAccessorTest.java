package hu.blackbelt.judo.tatami.jsl.jsl2psm.derived;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.derived.DataExpressionType;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.TimestampType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.TestUtils.allPsm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEntityDerivedDeclaration2PrimitiveAccessorTest extends AbstractTest {
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
    void testPrimitiveDerivedDeclarationModel() throws Exception {
        testName = "TestPrimitiveDerivedDeclarationModel";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "PrimitiveDerivedDeclarationModel",
                List.of("model PrimitiveDerivedDeclarationModel\n" +
                        "\n" +
                        "type numeric Integer precision 9 scale 0\n" +
                        "entity Lead {\n" +
                        "  field Integer value\n" +
                        "}\n" +
                        "entity Test {\n" +
                        "  relation Lead[] leads\n" +
                        "  derived Integer value = self.leads!count()\n" +
                        "}\n"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<DataProperty> dataProperties = psmModelWrapper.getStreamOfPsmDerivedDataProperty().collect(Collectors.toSet());
        assertEquals(1, dataProperties.size());
        final Optional<DataProperty> valueProperty = dataProperties.stream().filter(n -> n.getName().equals("value")).findFirst();
        assertTrue(valueProperty.isPresent());
        assertTrue(valueProperty.get().isPrimitive());
        assertEquals("self.leads!count()", valueProperty.get().getGetterExpression().getExpression());
    }
}
