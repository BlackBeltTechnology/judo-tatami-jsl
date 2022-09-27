package hu.judo.tatami.jsl.workflow;

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

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.EntityDeclarationBuilder;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.ModelDeclarationBuilder;
import org.eclipse.emf.common.util.URI;

import java.io.IOException;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static java.util.Arrays.asList;

public class JslTestModel {

    public static final String MODEL_NAME = "test";

    public static final String FILE_LOCATION = "target/test-classes/jsl/" + MODEL_NAME + "-jsl.model";


    public static void createJslModelAndSave() throws JslDslModel.JslDslValidationException, IOException {
        JslDslModel jslModel = buildJslDslModel().uri(URI.createURI(FILE_LOCATION)).name(MODEL_NAME).build();

        final EntityDeclaration entityA = EntityDeclarationBuilder.create().withName("A").build();
        final EntityDeclaration entityB = EntityDeclarationBuilder.create().withName("B").build();

        final ModelDeclaration model = ModelDeclarationBuilder.create()
                .withName(MODEL_NAME)
                .withDeclarations(asList(entityA, entityB))
                .build();

        jslModel.addContent(model);
        jslModel.saveJslDslModel();
    }

}
