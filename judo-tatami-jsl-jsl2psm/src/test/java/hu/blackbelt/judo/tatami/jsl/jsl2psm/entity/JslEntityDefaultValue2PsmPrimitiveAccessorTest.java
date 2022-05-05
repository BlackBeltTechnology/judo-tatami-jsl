package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.derived.PrimitiveAccessor;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

        assertBooleanDefault("Boolean", "LiteralEntity", "booleanLiteral", "true");
        assertNumericDefault("Integer", "LiteralEntity", "intLiteral", "1");
        assertNumericDefault("Decimal", "LiteralEntity", "decimalLiteral", "1.23");
        assertStringDefault("String", "LiteralEntity", "escapedStringLiteral", "\"escaped\nstring\"");
        assertStringDefault("String", "LiteralEntity", "rawStringLiteral", "\"raw\\\\nstring\"");
        assertDateDefault("Date", "LiteralEntity", "dateLiteral", "`2020-02-18`");
        assertTimestampDefault("Timestamp", "LiteralEntity", "timeStampLiteral", "`2020-02-18T10:11:12Z`");
        assertTimeDefault("Time", "LiteralEntity", "timeLiteral", "`23:59:59`");

        assertBooleanDefault("Boolean", "LiteralEntityWithIdentifiers", "idBooleanLiteral", "true");
        assertNumericDefault("Integer", "LiteralEntityWithIdentifiers", "idIntLiteral", "1");
        assertNumericDefault("Decimal", "LiteralEntityWithIdentifiers", "idDecimalLiteral", "1.23");
        assertStringDefault("String", "LiteralEntityWithIdentifiers", "idEscapedStringLiteral", "\"escaped\nstring\"");
        assertStringDefault("String", "LiteralEntityWithIdentifiers", "idRawStringLiteral", "\"raw\\\\nstring\"");
        assertDateDefault("Date", "LiteralEntityWithIdentifiers", "idDateLiteral", "`2020-02-18`");
        assertTimestampDefault("Timestamp", "LiteralEntityWithIdentifiers", "idTimeStampLiteral", "`2020-02-18T10:11:12Z`");
        assertTimeDefault("Time", "LiteralEntityWithIdentifiers", "idTimeLiteral", "`23:59:59`");

        assertNumericDefault("Decimal", "ComplexDefaultsEntity", "parenthesizedField", "((1 + 2) * 3) / 4");
        assertStringDefault("String", "ComplexDefaultsEntity", "ternaryField", "1 < 2 ? \"yes\" : \"no\"");
        assertBooleanDefault("Boolean", "ComplexDefaultsEntity", "unaryField", "not true");
        assertNumericDefault("Integer", "ComplexDefaultsEntity", "binaryField", "1 + 2");

        final PrimitiveAccessor withDefaultErrorField = assertUnmappedTransferObjectAttribute("ErrorWithDefaults", "withDefault").getDefaultValue();
        assertEquals(assertStringType("String"), withDefaultErrorField.getDataType());
        assertEquals("\"Hello!\"", withDefaultErrorField.getGetterExpression().getExpression());
    }

    private void assertBooleanDefault(String type, String toName, String attrName, String defaultValue) {
//        assertDataProperty("_LiteralEntity", "_booleanLiteral_default_LiteralEntity");
//        assertEquals(assertBooleanType("Boolean"), assertDataProperty("_LiteralEntity", "_booleanLiteral_default_LiteralEntity").getDataType());
//        final PrimitiveAccessor booleanLiteral = assertMappedTransferObjectAttribute("LiteralEntity", "booleanLiteral").getDefaultValue();
//        assertEquals(assertBooleanType("Boolean"), booleanLiteral.getDataType());
//        assertEquals("true", booleanLiteral.getGetterExpression().getExpression());

        assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName);
        assertEquals(assertBooleanType(type), assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName).getDataType());
        final PrimitiveAccessor literal = assertMappedTransferObjectAttribute(toName, attrName).getDefaultValue();
        assertEquals(assertBooleanType(type), literal.getDataType());
        assertEquals(defaultValue, literal.getGetterExpression().getExpression());
    }

    private void assertNumericDefault(String type, String toName, String attrName, String defaultValue) {
        assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName);
        assertEquals(assertNumericType(type), assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName).getDataType());
        final PrimitiveAccessor literal = assertMappedTransferObjectAttribute(toName, attrName).getDefaultValue();
        assertEquals(assertNumericType(type), literal.getDataType());
        assertEquals(defaultValue, literal.getGetterExpression().getExpression());
    }

    private void assertStringDefault(String type, String toName, String attrName, String defaultValue) {
        assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName);
        assertEquals(assertStringType(type), assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName).getDataType());
        final PrimitiveAccessor literal = assertMappedTransferObjectAttribute(toName, attrName).getDefaultValue();
        assertEquals(assertStringType(type), literal.getDataType());
        assertEquals(defaultValue, literal.getGetterExpression().getExpression());
    }

    private void assertDateDefault(String type, String toName, String attrName, String defaultValue) {
        assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName);
        assertEquals(assertDateType(type), assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName).getDataType());
        final PrimitiveAccessor literal = assertMappedTransferObjectAttribute(toName, attrName).getDefaultValue();
        assertEquals(assertDateType(type), literal.getDataType());
        assertEquals(defaultValue, literal.getGetterExpression().getExpression());
    }

    private void assertTimestampDefault(String type, String toName, String attrName, String defaultValue) {
        assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName);
        assertEquals(assertTimestampType(type), assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName).getDataType());
        final PrimitiveAccessor literal = assertMappedTransferObjectAttribute(toName, attrName).getDefaultValue();
        assertEquals(assertTimestampType(type), literal.getDataType());
        assertEquals(defaultValue, literal.getGetterExpression().getExpression());
    }

    private void assertTimeDefault(String type, String toName, String attrName, String defaultValue) {
        assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName);
        assertEquals(assertTimeType(type), assertDataProperty("_" + toName, "_" + attrName + "_default_" + toName).getDataType());
        final PrimitiveAccessor literal = assertMappedTransferObjectAttribute(toName, attrName).getDefaultValue();
        assertEquals(assertTimeType(type), literal.getDataType());
        assertEquals(defaultValue, literal.getGetterExpression().getExpression());
    }
}
