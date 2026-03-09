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
import hu.blackbelt.judo.tatami.core.TransformationMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslBinaryTypeDeclaration2PsmBinaryTypeTest extends AbstractTest {
    static final String TARGET_TEST_CLASSES = "target/test-classes/type/binary";

    @Override
    protected String getTargetTestClasses() {
        return "target/test-classes/type/binary";
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

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"})
    void testDeclaration(TransformationMode mode) throws Exception {

        jslModel = JslParser.getModelFromStrings(
                List.of("model DeclarationModel;\n" +
                        "\n" +
                        "type binary Picture mime-type:[\"image/png\", \"image/*\"] max-file-size:1024 KiB;\n"
                )
        );

        transform(mode);

        assertBinaryType("Picture");
        assertEquals(assertBinaryType("Picture").getName(), "Picture");
        assertEquals(assertBinaryType("Picture").getMimeTypes().size(), 2);
        assertEquals(assertBinaryType("Picture").getMimeTypes(), Arrays.asList("image/png", "image/*"));
        assertEquals(assertBinaryType("Picture").getMaxFileSize(), 1048576);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"})
    void testEntityMemberRequired(TransformationMode mode) throws Exception {

        jslModel = JslParser.getModelFromStrings(
                List.of("model EntityMemberRequiredModel;\n" +
                        "\n" +
                        "type binary Picture mime-type:[\"image/png\", \"image/*\"] max-file-size:1024 KiB;\n" +
                        "\n" +
                        "entity User {\n" +
                        "\tfield Picture profilePicture required;\n" +
                        "}"
                )
        );

        transform(mode);

        assertBinaryType("Picture");
        assertEquals(assertBinaryType("Picture").getMimeTypes(), Arrays.asList("image/png", "image/*"));
        assertEquals(assertBinaryType("Picture").getMaxFileSize(), 1048576);
        assertEquals(assertBinaryType("Picture"), assertAttribute("_User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("User", "profilePicture").getDataType());
        assertTrue(assertAttribute("_User", "profilePicture").isRequired());
        assertTrue(assertMappedTransferObjectAttribute("User", "profilePicture").isRequired());

    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"})
    void testEntityMemberInheritance(TransformationMode mode) throws Exception {

        jslModel = JslParser.getModelFromStrings(
                List.of("model EntityMemberInheritanceModel;\n" +
                        "\n" +
                        "type binary Picture mime-type:[\"image/png\", \"image/*\"] max-file-size:1024 KiB;\n" +
                        "\n" +
                        "entity User {\n" +
                        "\tfield Picture profilePicture;\n" +
                        "}\n" +
                        "\n" +
                        "entity AdminUser extends User {\n" +
                        "}"
                )
        );

        transform(mode);

        assertBinaryType("Picture");
        assertEquals(assertBinaryType("Picture"), assertAttribute("_User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertAllAttribute("_AdminUser", "profilePicture").getDataType());

        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("AdminUser", "profilePicture").getDataType());

    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"})
    void testEntityMemberIdentifier(TransformationMode mode) throws Exception {

        jslModel = JslParser.getModelFromStrings(
                List.of("model EntityMemberIdentifierModel;\n" +
                        "\n" +
                        "type binary Picture mime-type : [\"image/png\", \"image/*\"] max-file-size : 1024 KiB;\n" +
                        "\n" +
                        "entity User {\n" +
                        "\tidentifier Picture profilePicture;\n" +
                        "}"
                )
        );

        transform(mode);

        assertBinaryType("Picture");
        assertEquals(assertBinaryType("Picture"), assertAttribute("_User", "profilePicture").getDataType());
        assertEquals(assertBinaryType("Picture"), assertMappedTransferObjectAttribute("User", "profilePicture").getDataType());

        assertTrue(assertAttribute("_User", "profilePicture").isIdentifier());

    }
}
