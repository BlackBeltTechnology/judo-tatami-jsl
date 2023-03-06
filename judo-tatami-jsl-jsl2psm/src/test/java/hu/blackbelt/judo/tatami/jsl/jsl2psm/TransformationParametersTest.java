package hu.blackbelt.judo.tatami.jsl.jsl2psm;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.Jsl2PsmParameterBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        return new BufferedSlf4jLogger(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    // TODO: Unmapped Transfer Field declaration reads / maps
    
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
    	} else if (testName.equals("TestDefaultDefaultNamePrefix")) {
            return parameters.defaultDefaultNamePrefix("_pre_");
        } else if (testName.equals("TestDefaultDefaultNameMidfix")) {
            return parameters.defaultDefaultNameMidfix("_Default_Mid_");
        } else if (testName.equals("TestDefaultDefaultNamePostfix")) {
            return parameters.defaultDefaultNamePostfix("_post");
    	} else if (testName.equals("TestDefaultReadsNamePrefix")) {
            return parameters.defaultReadsNamePrefix("_pre_");
        } else if (testName.equals("TestDefaultReadsNameMidfix")) {
            return parameters.defaultReadsNameMidfix("_Default_Mid_");
        } else if (testName.equals("TestDefaultReadsNamePostfix")) {
            return parameters.defaultReadsNamePostfix("_post");
        }
    	return parameters;
    }
    
    @Test
    void testNoDefaultTransferObject() throws Exception {
        testName = "TestNoDefaultTransferObject";

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
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

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
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

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
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

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
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

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
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

    @Test
    void testDefaultDefaultNamePrefix() throws Exception {
        testName = "TestDefaultDefaultNamePrefix";

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
                        "\n" +
                        "type string String(min-size = 0, max-size = 32);\n" +
                        "\n" +
                        "entity T {\n" +
                        "\tfield String strField = \"hello\";" +
                        "}\n"
                )
        );

        transform();

        assertEntityType("_T");
        assertEquals(1, getMappedTransferObjectTypes().size());
        final TransferAttribute strField = assertMappedTransferObjectAttribute("T", "strField");
        assertEquals("_pre_strField_Default_T", strField.getDefaultValue().getName());
    }

    @Test
    void testDefaultDefaultNameMidfix() throws Exception {
        testName = "TestDefaultDefaultNameMidfix";

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
                        "\n" +
                        "type string String(min-size = 0, max-size = 32);\n" +
                        "\n" +
                        "entity T {\n" +
                        "\tfield String strField = \"hello\";" +
                        "}\n"
                )
        );

        transform();

        assertEntityType("_T");
        assertEquals(1, getMappedTransferObjectTypes().size());
        final TransferAttribute strField = assertMappedTransferObjectAttribute("T", "strField");
        assertEquals("_strField_Default_Mid_T", strField.getDefaultValue().getName());
    }

    @Test
    void testDefaultDefaultNamePostfix() throws Exception {
        testName = "TestDefaultDefaultNamePostfix";

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
                        "\n" +
                        "type string String(min-size = 0, max-size = 32);\n" +
                        "\n" +
                        "entity T {\n" +
                        "\tfield String strField = \"hello\";" +
                        "}\n"
                )
        );

        transform();

        assertEntityType("_T");
        assertEquals(1, getMappedTransferObjectTypes().size());
        final TransferAttribute strField = assertMappedTransferObjectAttribute("T", "strField");
        assertEquals("_strField_Default_T_post", strField.getDefaultValue().getName());
    }

    @Test
    void testDefaultReadsNamePrefix() throws Exception {
        testName = "TestDefaultReadsNamePrefix";

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
                        "\n" +
                        "type string String(min-size = 0, max-size = 32);\n" +
                        "\n" +
						"entity E {\n" +
                        "    field String strField;\n" +
                        "}\n" +
                        "transfer T maps E as e {\n" +
                        "    field String strField reads e.strField;\n" +
                        "}\n"
                
                )
        );

        transform();

        assertMappedTransferObjectAttribute("T", "strField");
        assertDataProperty("_E", "_pre_strField_T_Reads");
    }

    @Test
    void testDefaultReadsNameMidfix() throws Exception {
        testName = "TestDefaultReadsNameMidfix";

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
                        "\n" +
                        "type string String(min-size = 0, max-size = 32);\n" +
                        "\n" +
						"entity E {\n" +
                        "    field String strField;\n" +
                        "}\n" +
                        "transfer T maps E as e {\n" +
                        "    field String strField reads e.strField;\n" +
                        "}\n"
                )
        );

        transform();

        assertMappedTransferObjectAttribute("T", "strField");
        assertDataProperty("_E", "_strField_Default_Mid_T_Reads");
    }

    @Test
    void testDefaultReadsNamePostfix() throws Exception {
        testName = "TestDefaultReadsNamePostfix";

        jslModel = JslParser.getModelFromStrings(
                "Test",
                List.of("model Test;\n" +
                        "\n" +
                        "type string String(min-size = 0, max-size = 32);\n" +
                        "\n" +
						"entity E {\n" +
                        "    field String strField;\n" +
                        "}\n" +
                        "transfer T maps E as e {\n" +
                        "    field String strField reads e.strField;\n" +
                        "}\n"
                )
        );

        transform();

        assertMappedTransferObjectAttribute("T", "strField");
        assertDataProperty("_E", "_strField_T_post");
    }

}
