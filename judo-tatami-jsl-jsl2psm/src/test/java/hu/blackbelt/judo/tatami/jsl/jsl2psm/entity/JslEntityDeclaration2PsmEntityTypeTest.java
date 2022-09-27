package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

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

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEntityDeclaration2PsmEntityTypeTest extends AbstractTest {
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
        return new BufferedSlf4jLogger(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testCreateEntityType() throws Exception {
        testName = "TestCreateEntityType";

        jslModel = JslParser.getModelFromStrings(
                "EntityTypeCreateModel",
                List.of("model EntityTypeCreateModel;\n" +
                        "\n" +
                        "entity Test {\n" +
                        "}\n" +
                        "entity abstract Person {\n" +
                        "}\n" +
                        "entity SalesPerson extends Person, Test {\n" +
                        "}\n"
                )
        );

        transform();

//        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(3, getEntityTypes().size());

        final Set<String> psmEntityTypeNames = getEntityTypes().stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("_Test", "_Person", "_SalesPerson");
        assertThat(psmEntityTypeNames, IsEqual.equalTo(jslEntityTypeDeclarationNames));

        assertTrue(assertEntityType("_Person").isAbstract());
        assertEntityType("_SalesPerson");
                
        final Set<String> psmEntityType3SuperTypeNames = assertEntityType("_SalesPerson").getSuperEntityTypes().stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityType3SuperTypeNames = ImmutableSet.of("_Person", "_Test");
        assertThat(psmEntityType3SuperTypeNames, IsEqual.equalTo(jslEntityType3SuperTypeNames));
    }

    @Test
    void testEntityLocalName() throws Exception {
        testName = "TestEntityLocaleName";

        jslModel = JslParser.getModelFromStrings(
                "First::Second::EntityLocaleNameModel",
                List.of("model First::Second::EntityLocaleNameModel;\n" +
                        "\n" +
                        "entity Test {\n" +
                        "}"
                )
        );

        transform();

        final Set<hu.blackbelt.judo.meta.psm.namespace.Package> psmPackageTypes = psmModelWrapper.getStreamOfPsmNamespacePackage().collect(Collectors.toSet());
        assertEquals(3, psmPackageTypes.size());

        final Set<String> psmEntityPackageNames = psmPackageTypes.stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslPackageNames = ImmutableSet.of("First", "Second", "EntityLocaleNameModel");
        assertThat(psmEntityPackageNames, IsEqual.equalTo(jslPackageNames));

        assertEquals(1, getEntityTypes().size());

        final Set<String> psmEntityTypeNames = getEntityTypes().stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("_Test");
        assertThat(psmEntityTypeNames, IsEqual.equalTo(jslEntityTypeDeclarationNames));
    }
}
