package hu.blackbelt.judo.tatami.jsl.workflow;

import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;

public class DefaultWorkflow extends AbstractTatamiPipelineWorkflow {

	public DefaultWorkflow(DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder builder) {
		super(builder);
	}

	public DefaultWorkflow(DefaultWorkflowSetupParameters params) {
		super(params);
	}

	@Override
	public void loadModels(WorkflowHelper workflowHelper, WorkflowMetrics metrics, TransformationContext transformationContext, DefaultWorkflowSetupParameters parameters) {
		workflowHelper.loadJslModel(parameters.getModelName(), parameters.getJslModel(), parameters.getJslModelSourceURI());
	}
}
