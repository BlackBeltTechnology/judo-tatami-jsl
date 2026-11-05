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

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.EntityDeclarationBuilder;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.ModelDeclarationBuilder;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.core.TransformationMode;
import hu.blackbelt.judo.tatami.core.etl.EtlEngineMissingException;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core artifact without the judo-tatami-jsl-jsl2psm-etl add-on: ETL fails fast naming the artifact, ZETA runs.
 * See change: split-etl-artifacts.
 */
class Jsl2PsmWorkEtlMissingTest {

    private TransformationContext context(TransformationMode mode) {
        JslDslModel jslModel = JslDslModel.buildJslDslModel().uri(URI.createURI("urn:test-jsl")).name("test").build();
        ModelDeclaration model = ModelDeclarationBuilder.create()
                .withName("test")
                .withDeclarations(asList(EntityDeclarationBuilder.create().withName("A").build()))
                .build();
        jslModel.addContent(model);
        TransformationContext context = new TransformationContext("test");
        context.put(jslModel);
        context.put(Jsl2PsmWork.Jsl2PsmWorkParameter.jsl2PsmWorkParameter().transformationMode(mode).build());
        return context;
    }

    @Test
    void etlModeWithoutAddOnFailsNamingTheArtifact() {
        EtlEngineMissingException e = assertThrows(EtlEngineMissingException.class,
                () -> new Jsl2PsmWork(context(TransformationMode.ETL)).execute());
        assertEquals(Jsl2PsmEtlEngine.class, e.getEngineType());
        assertTrue(e.getMessage().contains("hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2psm-etl"), e.getMessage());
    }

    @Test
    void zetaModeRunsWithoutAddOn() {
        TransformationContext context = context(TransformationMode.ZETA);
        assertDoesNotThrow(() -> new Jsl2PsmWork(context).execute());
        assertTrue(context.getByClass(PsmModel.class).isPresent());
    }
}
