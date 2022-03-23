package org.judo.tatami.workflow;

import static hu.blackbelt.judo.tatami.workflow.DefaultWorkflowSave.saveModels;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.judo.tatami.workflow.PsmTestModel.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.blackbelt.judo.meta.psm.runtime.PsmModel.PsmValidationException;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReport;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkStatus;
import hu.blackbelt.judo.tatami.workflow.DefaultWorkflowSetupParameters;
import hu.blackbelt.judo.tatami.workflow.PsmDefaultWorkflow;

public class PsmDefaultWorkflowTest {
	
	PsmDefaultWorkflow defaultWorkflow;

    public static final File TARGET_TEST_CLASSES = new File("target/test-classes/psm");

    public static final String TARGET_CLASSES = "target/test-classes/psm";

    public static final List<String> DIALECT_LIST = new ArrayList<>(Arrays.asList("hsqldb", "oracle"));

	private WorkReport workReport;

    private File measureModel;

	@BeforeEach
	void setUp() throws IOException, PsmValidationException {

		createPsmModelelAndSave();

		measureModel = new File(TARGET_CLASSES, MODEL_NAME + "-measure.model");
		measureModel.delete();

		defaultWorkflow = new PsmDefaultWorkflow(DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters()
				.psmModelSourceURI(new File(FILE_LOCATION).toURI())
				.modelName(MODEL_NAME)
				.dialectList(DIALECT_LIST));
		workReport = defaultWorkflow.startDefaultWorkflow();
		saveModels(defaultWorkflow.getTransformationContext(), TARGET_TEST_CLASSES, DIALECT_LIST);
	}

	@Test
	void testDefaultWorkflow() {

		assertThat(workReport.getStatus(), equalTo(WorkStatus.COMPLETED));

		assertTrue(measureModel.exists());
	}
}
