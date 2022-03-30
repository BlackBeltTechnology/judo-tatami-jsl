package hu.judo.tatami.jsl.workflow;

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
						.modelName(MODEL_NAME)
		);
		workReport = defaultWorkflow.startDefaultWorkflow();
		saveModels(defaultWorkflow.getTransformationContext(), TARGET_TEST_CLASSES);
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
						.modelName(MODEL_NAME)
		);

		WorkflowHelper workflowHelper = new WorkflowHelper(defaultWorkflow);

		workflowHelper.loadJslModel(MODEL_NAME, null, new File(TARGET_CLASSES, MODEL_NAME + "-jsl.model").toURI());

		workflowHelper.loadPsmModel(MODEL_NAME, null, new File(TARGET_CLASSES, MODEL_NAME + "-psm.model").toURI());
	}
}
