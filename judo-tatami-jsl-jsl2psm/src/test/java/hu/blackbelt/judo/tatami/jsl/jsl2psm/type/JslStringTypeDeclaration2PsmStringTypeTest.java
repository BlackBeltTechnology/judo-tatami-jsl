package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

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
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslStringTypeDeclaration2PsmStringTypeTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/type/string";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/string";
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

    @Test
    void testDeclaration() throws Exception {
        testName = "TestDeclaration";

        jslModel = JslParser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel;\n" +
                        "\n" +
                        "type string Name(min-size = 0, max-size = 32, regex =\"/^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/g\");\n"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(32, assertStringType("Name").getMaxLength());
        assertEquals("/^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$/g", assertStringType("Name").getRegExp());
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel;\n" +
                        "\n" +
                        "type string Name(min-size = 0, max-size = 32);\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Name name;\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());

        assertFalse(assertAttribute("_Person", "name").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Person", "name").isRequired());

    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel;\n" +
                        "\n" +
                        "type string Name(min-size = 0, max-size = 32);\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield required Name name;\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());

        assertTrue(assertAttribute("_Person", "name").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("Person", "name").isRequired());

    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel;\n" +
                        "\n" +
                        "type string Name(min-size = 0, max-size = 32);\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Name name;\n" +
                        "}\n" +
                        "\n" +
                        "entity StudentPerson extends Person {\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());

        assertEquals(assertStringType("Name"), assertAllAttribute("_StudentPerson", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("StudentPerson", "name").getDataType());

    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel;\n" +
                        "\n" +
                        "type string Name(min-size = 0, max-size = 32);\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tidentifier Name name;\n" +
                        "}"
                )
        );

        transform();

        assertStringType("Name");
        assertEquals(assertStringType("Name"), assertAttribute("_Person", "name").getDataType());
        assertEquals(assertStringType("Name"), assertMappedTransferObjectAttribute("Person", "name").getDataType());
        assertTrue(assertAttribute("_Person", "name").isIdentifier());
    }
}
