package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JslEntityDefaultValue2PsmPrimitiveAccessorTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/entity";

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
    void testPrimitiveDefaultValue() throws Exception {
        testName = "TestPrimitiveDefaultValue";

        jslModel = parser.getModelFromFiles(
                "TestDefaultExpressionModel",
                List.of(new File("src/test/resources/entity/TestDefaultExpressionModel.jsl"))
        );

        transform();

//        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(1, getEntityTypes().size());

        final Set<String> psmEntityTypeNames = getEntityTypes().stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("_SalesPerson");
        assertThat(psmEntityTypeNames, IsEqual.equalTo(jslEntityTypeDeclarationNames));

        assertEquals("self.value > 1", assertDataProperty("_SalesPerson", "_t1_default_SalesPerson").getGetterExpression().getExpression());
        assertEquals("1 + 2", assertDataProperty("_SalesPerson", "_value_default_SalesPerson").getGetterExpression().getExpression());
        assertEquals("\"\" + self.value + \"test\"", assertDataProperty("_SalesPerson", "_stringConcat_default_SalesPerson").getGetterExpression().getExpression());
    }
}
