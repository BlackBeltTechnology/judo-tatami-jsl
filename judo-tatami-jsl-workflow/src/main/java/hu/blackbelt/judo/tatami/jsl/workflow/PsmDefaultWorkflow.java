package hu.blackbelt.judo.tatami.jsl.workflow;

import hu.blackbelt.judo.tatami.core.workflow.work.*;

public class PsmDefaultWorkflow extends AbstractTatamiPipelineWorkflow {

	public PsmDefaultWorkflow(DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder builder) {
		super(builder);
	}

	public PsmDefaultWorkflow(DefaultWorkflowSetupParameters params) {
		super(params);
	}

	@Override
	public void loadModels(WorkflowHelper workflowHelper, WorkflowMetrics metrics, TransformationContext transformationContext, DefaultWorkflowSetupParameters parameters) {
		workflowHelper.loadPsmModel(parameters.getModelName(), parameters.getPsmModel(), parameters.getPsmModelSourceURI());
	}
}
