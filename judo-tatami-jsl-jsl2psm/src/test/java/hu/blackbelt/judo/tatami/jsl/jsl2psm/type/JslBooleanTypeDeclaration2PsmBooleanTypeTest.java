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

import org.slf4j.Logger;
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
public class JslBooleanTypeDeclaration2PsmBooleanTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/boolean";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/boolean";
    }

    @Override
    protected String getTest() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected Logger createLog() {
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
                        "type boolean Boolean;\n"
                )
        );

        transform();
        assertBooleanType("Boolean");
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel;\n" +
                        "\n" +
                        "type boolean Vaccinated;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Vaccinated vaccinated;\n" +
                        "}"
                )
        );

        transform();

        assertBooleanType("Vaccinated");
        assertEquals(assertBooleanType("Vaccinated"), assertAttribute("_Patient", "vaccinated").getDataType());
        assertEquals(assertBooleanType("Vaccinated"), assertMappedTransferObjectAttribute("Patient", "vaccinated").getDataType());

        assertFalse(assertAttribute("_Patient", "vaccinated").isRequired());
        assertFalse(assertMappedTransferObjectAttribute("Patient", "vaccinated").isRequired());

    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel;\n" +
                        "\n" +
                        "type boolean Vaccinated;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield required Vaccinated vaccinated;\n" +
                        "}"
                )
        );

        transform();

        assertBooleanType("Vaccinated");
        assertEquals(assertBooleanType("Vaccinated"), assertAttribute("_Patient", "vaccinated").getDataType());
        assertEquals(assertBooleanType("Vaccinated"), assertMappedTransferObjectAttribute("Patient", "vaccinated").getDataType());

        assertTrue(assertAttribute("_Patient", "vaccinated").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("Patient", "vaccinated").isRequired());

    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel;\n" +
                        "\n" +
                        "type boolean Vaccinated;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tfield Vaccinated vaccinated;\n" +
                        "}\n" +
                        "\n" +
                        "entity SurgentPatient extends Patient {\n" +
                        "}"
                )
        );

        transform();

        assertBooleanType("Vaccinated");
        assertEquals(assertBooleanType("Vaccinated"), assertAttribute("_Patient", "vaccinated").getDataType());
        assertEquals(assertBooleanType("Vaccinated"), assertMappedTransferObjectAttribute("Patient", "vaccinated").getDataType());

        assertEquals(assertBooleanType("Vaccinated"), assertAllAttribute("_SurgentPatient", "vaccinated").getDataType());
        assertEquals(assertBooleanType("Vaccinated"), assertMappedTransferObjectAttribute("SurgentPatient", "vaccinated").getDataType());

    }


    @Test
    void testEntityMemberIdentifier() throws Exception {
        testName = "TestEntityMemberIdentifier";

        jslModel = JslParser.getModelFromStrings(
                "EntityMemberIdentifierModel",
                List.of("model EntityMemberIdentifierModel;\n" +
                        "\n" +
                        "type boolean Vaccinated;\n" +
                        "\n" +
                        "entity Patient {\n" +
                        "\tidentifier Vaccinated vaccinated;\n" +
                        "}"
                )
        );

        transform();

        assertBooleanType("Vaccinated");
        assertEquals(assertBooleanType("Vaccinated"), assertAttribute("_Patient", "vaccinated").getDataType());
        assertEquals(assertBooleanType("Vaccinated"), assertMappedTransferObjectAttribute("Patient", "vaccinated").getDataType());

        assertTrue(assertAttribute("_Patient", "vaccinated").isIdentifier());
    }
}
