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
public class JslDateDeclaration2PsmDateTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/date";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/date";
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
                        "type date Date;\n"
                )
        );

        transform();

        assertDateType("Date");
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel;\n" +
                        "\n" +
                        "type date Date;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Date birthDate;\n" +
                        "}"
                )
        );

        transform();

        assertDateType("Date");
        assertEquals(assertDateType("Date"), assertAttribute("_Patient", "birthDate").getDataType());
        assertEquals(assertDateType("Date"), assertMappedTransferObjectAttribute("Patient", "birthDate").getDataType());

        assertFalse(assertAttribute("_Patient", "birthDate").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Patient", "birthDate").isRequired());

    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel;\n" +
                        "\n" +
                        "type date Date;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield required Date birthDate;\n" +
                        "}"
                )
        );

        transform();

        assertDateType("Date");
        assertEquals(assertDateType("Date"), assertAttribute("_Patient", "birthDate").getDataType());
        assertEquals(assertDateType("Date"), assertMappedTransferObjectAttribute("Patient", "birthDate").getDataType());

        assertTrue(assertAttribute("_Patient", "birthDate").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("Patient", "birthDate").isRequired());

    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel;\n" +
                        "\n" +
                        "type date Date;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Date birthDate;\n" +
                        "}\n" +
                        "\n" +
                        "entity SurgentPatient extends Patient {\n" +
                        "}"
                )
        );

        transform();

        assertDateType("Date");
        assertEquals(assertDateType("Date"), assertAttribute("_Patient", "birthDate").getDataType());
        assertEquals(assertDateType("Date"), assertAllAttribute("_SurgentPatient", "birthDate").getDataType());
        assertEquals(assertDateType("Date"), assertMappedTransferObjectAttribute("Patient", "birthDate").getDataType());
        assertEquals(assertDateType("Date"), assertMappedTransferObjectAttribute("SurgentPatient", "birthDate").getDataType());

    }

    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel;\n" +
                        "\n" +
                        "type date Date;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tidentifier Date birthDate;\n" +
                        "}"
                )
        );

        transform();

        assertDateType("Date");
        assertEquals(assertDateType("Date"), assertAttribute("_Patient", "birthDate").getDataType());
        assertEquals(assertDateType("Date"), assertMappedTransferObjectAttribute("Patient", "birthDate").getDataType());
        assertTrue(assertAttribute("_Patient", "birthDate").isIdentifier());

    }
}
