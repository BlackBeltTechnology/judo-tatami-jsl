package hu.blackbelt.judo.tatami.jsl.jsl2ui;

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

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.ActorDeclarationBuilder;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.EntityDeclarationBuilder;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.ModelDeclarationBuilder;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngine;
import hu.blackbelt.judo.tatami.core.workflow.flow.WorkFlow;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReport;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.SaveArguments.uiSaveArgumentsBuilder;
import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.calculateJsl2UiTransformationScriptURI;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class Jsl2UiWorkTest {
    public static final String MODEL_NAME = "test";
    public static final String TARGET_TEST_CLASSES = "target/test-classes";
    public static final String JSL_FILE_LOCATION = TARGET_TEST_CLASSES + "/jsl/" + MODEL_NAME + "-jsl.model";
    public static final String UI_FILE_LOCATION = TARGET_TEST_CLASSES + "/jsl/" + MODEL_NAME + "-ui.model";

    Jsl2UiWork jsl2UiWork;
    TransformationContext transformationContext;

    @BeforeEach
    void setUp() throws JslDslModel.JslDslValidationException, IOException {
        JslDslModel jslModel = buildJslDslModel().uri(URI.createURI(JSL_FILE_LOCATION)).name(MODEL_NAME).build();

        final ActorDeclaration actorDeclaration = ActorDeclarationBuilder.create()
                .withHuman(true)
                .withName("WorkActor")
                .build();

        final ModelDeclaration model = ModelDeclarationBuilder.create()
                .withName(MODEL_NAME)
                .withDeclarations(asList(actorDeclaration))
                .build();

        jslModel.addContent(model);
        jslModel.saveJslDslModel();

        transformationContext = new TransformationContext(MODEL_NAME);
        transformationContext.put(jslModel);

        jsl2UiWork = new Jsl2UiWork(transformationContext, calculateJsl2UiTransformationScriptURI());
        jslModel.saveJslDslModel(jslDslSaveArgumentsBuilder().file(new File(JSL_FILE_LOCATION)).build());
    }

    @Test
    void testSimpleWorkflow() throws IOException, UiModel.UiValidationException {
        WorkFlow workflow = aNewSequentialFlow().execute(jsl2UiWork).build();

        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        WorkReport workReport = workFlowEngine.run(workflow);

        log.info("Workflow completed with status {}", workReport.getStatus(), workReport.getError());

        assertThat(workReport.getStatus(), equalTo(WorkStatus.COMPLETED));

        Optional<UiModel> uiModel = transformationContext.getByClass(UiModel.class);
        assertTrue(uiModel.isPresent());
        uiModel.get().saveUiModel(uiSaveArgumentsBuilder().file(new File(UI_FILE_LOCATION)));
    }

}
