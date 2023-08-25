package hu.blackbelt.judo.tatami.jsl.jsl2psm.importmodel;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.namespace.*;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslMultipleJslModelImportTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/importmodel";

    @Override
    protected String getTargetTestClasses() {
        return TARGET_TEST_CLASSES;
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
    void testImportModel() throws Exception {
        testName = "TestImportModelTest";

        jslModel = JslParser.getModelFromStrings("ns2::c", ImmutableList.of(
                "model ns1::a;\n"
                + "\n"
                + "type string String min-size:0 max-size:32;",

                "model ns2::b;\n"
                + "\n"
                + "import ns1::a as modela;\n"
                + "\n"
                + "entity B {\n"
                + "    field modela::String f1;\n"
                + "}",

                "model ns2::c;\n"
                + "\n"
                + "import ns1::a;\n"
                + "\n"
                + "entity C {\n"
                + "    field String f1;\n"
                + "}"


                ));

        transform();

        final Set<EntityType> psmEntityTypes = psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
        assertEquals(2, psmEntityTypes.size());

        assertThat(psmEntityTypes.stream().map(NamedElement::getName).collect(Collectors.toSet()),
                IsEqual.equalTo(ImmutableSet.of("_B", "_C")));

        final Set<Model> models = psmModelWrapper.getStreamOfPsmNamespaceModel().collect(Collectors.toSet());
        assertEquals(1, models.size());

        Model psmModel = models.iterator().next();

        assertThat(psmModel.getPackages().stream().map(NamedElement::getName).
                collect(Collectors.toSet()),
                IsEqual.equalTo(ImmutableSet.of("ns1", "ns2")));

        Package ns1 = psmModel.getPackages().stream().filter(p -> p.getName().equals("ns1")).findFirst().get();
        final Set<String> ns1PackageNames = ns1.getPackages().stream().map(NamedElement::getName).collect(Collectors.toSet());
        assertThat(ns1PackageNames,
                IsEqual.equalTo(ImmutableSet.of("a")));

        Package a = ns1.getPackages().stream().filter(p -> p.getName().equals("a")).findFirst().get();
        assertTrue(a.getElements().stream().filter(p -> p.getName().equals("String")).map(e -> (StringType) e).findFirst().isPresent());


        Package ns2 = psmModel.getPackages().stream().filter(p -> p.getName().equals("ns2")).findFirst().get();
        assertThat(ns2.getPackages().stream().map(NamedElement::getName).collect(Collectors.toSet()),
                IsEqual.equalTo(ImmutableSet.of("b", "c")));

        Package b = ns2.getPackages().stream().filter(p -> p.getName().equals("b")).findFirst().get();
        assertTrue(b.getElements().stream().filter(p -> p.getName().equals("_B")).map(e -> (EntityType) e).findFirst().isPresent());

        Package c = ns2.getPackages().stream().filter(p -> p.getName().equals("c")).findFirst().get();
        assertTrue(c.getElements().stream().filter(p -> p.getName().equals("_C")).map(e -> (EntityType) e).findFirst().isPresent());


    }


}
