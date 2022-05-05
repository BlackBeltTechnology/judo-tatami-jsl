package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.derived.PrimitiveAccessor;
import hu.blackbelt.judo.meta.psm.type.FlatPrimitiveType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

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
    void testDefaultValues() throws Exception {
        testName = "TestDefaultValues";

        jslModel = parser.getModelFromFiles(
                "TestDefaultExpressionModel",
                List.of(new File("src/test/resources/entity/TestDefaultExpressionModel.jsl"))
        );

        transform();

        assertDefault("LiteralEntity", "booleanLiteral", "true", () -> assertBooleanType("Boolean"));
        assertDefault("LiteralEntity", "intLiteral", "1", () -> assertNumericType("Integer"));
        assertDefault("LiteralEntity", "decimalLiteral", "1.23", () -> assertNumericType("Decimal"));
        assertDefault("LiteralEntity", "escapedStringLiteral", "\"escaped\nstring\"", () -> assertStringType("String"));
        assertDefault("LiteralEntity", "rawStringLiteral", "\"raw\\\\nstring\"", () -> assertStringType("String"));
        assertDefault("LiteralEntity", "dateLiteral", "`2020-02-18`", () -> assertDateType("Date"));
        assertDefault("LiteralEntity", "timeStampLiteral", "`2020-02-18T10:11:12Z`", () -> assertTimestampType("Timestamp"));
        assertDefault("LiteralEntity", "timeLiteral", "`23:59:59`", () -> assertTimeType("Time"));

        assertDefault("LiteralEntityWithIdentifiers", "idBooleanLiteral", "true", () -> assertBooleanType("Boolean"));
        assertDefault("LiteralEntityWithIdentifiers", "idIntLiteral", "1", () -> assertNumericType("Integer"));
        assertDefault("LiteralEntityWithIdentifiers", "idDecimalLiteral", "1.23", () -> assertNumericType("Decimal"));
        assertDefault("LiteralEntityWithIdentifiers", "idEscapedStringLiteral", "\"escaped\nstring\"", () -> assertStringType("String"));
        assertDefault("LiteralEntityWithIdentifiers", "idRawStringLiteral", "\"raw\\\\nstring\"", () -> assertStringType("String"));
        assertDefault("LiteralEntityWithIdentifiers", "idDateLiteral", "`2020-02-18`", () -> assertDateType("Date"));
        assertDefault("LiteralEntityWithIdentifiers", "idTimeStampLiteral", "`2020-02-18T10:11:12Z`", () -> assertTimestampType("Timestamp"));
        assertDefault("LiteralEntityWithIdentifiers", "idTimeLiteral", "`23:59:59`", () -> assertTimeType("Time"));

        assertDefault("ComplexDefaultsEntity", "parenthesizedField", "((1 + 2) * 3) / 4", () -> assertNumericType("Decimal"));
        assertDefault("ComplexDefaultsEntity", "ternaryField", "1 < 2 ? \"yes\" : \"no\"", () -> assertStringType("String"));
        assertDefault("ComplexDefaultsEntity", "unaryField", "not true", () -> assertBooleanType("Boolean"));
        assertDefault("ComplexDefaultsEntity", "binaryField", "1 + 2", () -> assertNumericType("Integer"));

        final PrimitiveAccessor withDefaultErrorField = assertUnmappedTransferObjectAttribute("ErrorWithDefaults", "withDefault").getDefaultValue();
        assertEquals(assertStringType("String"), withDefaultErrorField.getDataType());
        assertEquals("\"Hello!\"", withDefaultErrorField.getGetterExpression().getExpression());
    }

    private void assertDefault(String toName, String attrName, String defaultValue, Supplier<? extends FlatPrimitiveType> call) {
        assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName);
        assertEquals(call.get(), assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName).getDataType());
        final PrimitiveAccessor literal = assertMappedTransferObjectAttribute(toName, attrName).getDefaultValue();
        assertEquals(call.get(), literal.getDataType());
        assertEquals(defaultValue, literal.getGetterExpression().getExpression());
    }

}
