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

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReport;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkStatus;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSetupParameters;
import hu.blackbelt.judo.tatami.jsl.workflow.JslDefaultWorkflow;
import hu.blackbelt.judo.tatami.jsl.workflow.WorkflowHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSave.saveModels;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static hu.judo.tatami.jsl.workflow.JslTestModel.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JslDefaultWorkflowTest {

    JslDefaultWorkflow defaultWorkflow;

    public static final File TARGET_TEST_CLASSES = new File("target/test-classes/jsl");

    public static final String TARGET_CLASSES = "target/test-classes/jsl";

    private WorkReport workReport;

    private File psmModel;

    @BeforeEach
    void setUp() throws IOException, JslDslModel.JslDslValidationException {
        createJslModelAndSave();

        psmModel = new File(TARGET_CLASSES, MODEL_NAME + "-psm.model");
        psmModel.delete();

        defaultWorkflow = new JslDefaultWorkflow(
                DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters()
                        .jslModelSourceURI(new File(FILE_LOCATION).toURI())
                        .dialectList(ImmutableList.of("hsqldb"))
                        .modelName(MODEL_NAME)
        );
        workReport = defaultWorkflow.startDefaultWorkflow();
        saveModels(defaultWorkflow.getTransformationContext(), TARGET_TEST_CLASSES, ImmutableList.of("hsqldb"));
    }

    @Test
    void testDefaultWorkflow() {
        assertThat(workReport.getStatus(), equalTo(WorkStatus.COMPLETED));

        assertTrue(psmModel.exists());
    }

    @Test
    void testTransformationContextLoad() {
        // Test reload
        defaultWorkflow = new JslDefaultWorkflow(
                DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters()
                        .jslModelSourceURI(new File(FILE_LOCATION).toURI())
                        .dialectList(ImmutableList.of("hsqldb"))
                        .modelName(MODEL_NAME)
        );

        WorkflowHelper workflowHelper = new WorkflowHelper(defaultWorkflow);

        workflowHelper.loadJslModel(MODEL_NAME, null, new File(TARGET_CLASSES, MODEL_NAME + "-jsl.model").toURI());

        workflowHelper.loadPsmModel(MODEL_NAME, null, new File(TARGET_CLASSES, MODEL_NAME + "-psm.model").toURI());

        workReport = defaultWorkflow.startDefaultWorkflow();
    }

    @Test
    void testTransformationContextPartialLoad() {
        // Test reload
        defaultWorkflow = new JslDefaultWorkflow(
                DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters()
                        .jslModelSourceURI(new File(FILE_LOCATION).toURI())
                        .dialectList(ImmutableList.of("hsqldb"))
                        .modelName(MODEL_NAME)
        );

        WorkflowHelper workflowHelper = new WorkflowHelper(defaultWorkflow);

        workflowHelper.loadJslModel(MODEL_NAME, null, new File(TARGET_CLASSES, MODEL_NAME + "-jsl.model").toURI());

        workflowHelper.loadPsmModel(MODEL_NAME, null, new File(TARGET_CLASSES, MODEL_NAME + "-psm.model").toURI());
    }
}
