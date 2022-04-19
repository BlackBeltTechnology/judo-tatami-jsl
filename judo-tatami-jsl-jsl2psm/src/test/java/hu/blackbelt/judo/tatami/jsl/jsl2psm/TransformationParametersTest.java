package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.Jsl2PsmParameterBuilder;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.epsilon.ecl.parse.Ecl_EolParserRules.returnStatement_return;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
public class TransformationParametersTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/parameters";

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

    
    @Override
    public Jsl2PsmParameterBuilder addTransformationParameters(String testName, Jsl2PsmParameterBuilder parameters) {

    	if (testName.equals("TestNoDefaultTransferObject")) {
    		return parameters.generateDefaultTransferObject(false);
    	} else if (testName.equals("TestEntityPrefix")) {
    		return parameters.entityNamePrefix("_Entity_");
    	} else if (testName.equals("TestEntityPostfix")) {
    		return parameters.entityNamePrefix("").entityNamePostfix("_Entity");
    	} else if (testName.equals("TestTransferObjectPrefix")) {
    		return parameters.defaultTransferObjectNamePrefix("_DefaultTransferObject");
    	} else if (testName.equals("TestTransferObjectPostfix")) {
    		return parameters.defaultTransferObjectNamePostfix("_DefaultTransferObject");
    	}
    	
    	return parameters;
    }
    
    @Test
    void testNoDefaultTransferObject() throws Exception {
        testName = "TestNoDefaultTransferObject";

        jslModel = parser.getModelFromStrings(
                "Test",
                List.of("model Test\n" +
                        "\n" +
                        "entity T {\n" +
                        "}\n"
                		)
        );

        transform();

        assertEntityType("_T");
        assertEquals(0, getMappedTransferObjectTypes().size());
    }

    @Test
    void testEntityPrefix() throws Exception {
        testName = "TestEntityPrefix";

        jslModel = parser.getModelFromStrings(
                "Test",
                List.of("model Test\n" +
                        "\n" +
                        "entity T {\n" +
                        "}\n"
                		)
        );

        transform();

        assertEntityType("_Entity_T");
        assertEquals(1, getMappedTransferObjectTypes().size());
        assertMappedTransferObject("T");
    }

    @Test
    void testEntityPostfix() throws Exception {
        testName = "TestEntityPostfix";

        jslModel = parser.getModelFromStrings(
                "Test",
                List.of("model Test\n" +
                        "\n" +
                        "entity T {\n" +
                        "}\n"
                		)
        );

        transform();

        assertEntityType("T_Entity");
        assertEquals(1, getMappedTransferObjectTypes().size());
        assertMappedTransferObject("T");
    }
    
    
    @Test
    void testTransferObjectPrefix() throws Exception {
        testName = "TestTransferObjectPrefix";

        jslModel = parser.getModelFromStrings(
                "Test",
                List.of("model Test\n" +
                        "\n" +
                        "entity T {\n" +
                        "}\n"
                		)
        );

        transform();

        assertEntityType("_T");
        assertEquals(1, getMappedTransferObjectTypes().size());
        assertMappedTransferObject("_DefaultTransferObjectT");
    }

    @Test
    void testTransferObjectPostfix() throws Exception {
        testName = "TestTransferObjectPostfix";

        jslModel = parser.getModelFromStrings(
                "Test",
                List.of("model Test\n" +
                        "\n" +
                        "entity T {\n" +
                        "}\n"
                		)
        );

        transform();

        assertEntityType("_T");
        assertEquals(1, getMappedTransferObjectTypes().size());
        assertMappedTransferObject("T_DefaultTransferObject");
    }


}
