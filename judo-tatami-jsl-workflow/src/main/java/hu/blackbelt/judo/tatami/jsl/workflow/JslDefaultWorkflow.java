package hu.blackbelt.judo.tatami.jsl.workflow;

import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JslDefaultWorkflow extends AbstractTatamiPipelineWorkflow {

	public JslDefaultWorkflow(DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder builder) {
		super(builder);
	}

	public JslDefaultWorkflow(DefaultWorkflowSetupParameters params) {
		super(params);
	}

	@Override
	public void loadModels(WorkflowHelper workflowHelper, WorkflowMetrics metrics, TransformationContext transformationContext, DefaultWorkflowSetupParameters parameters) {
		workflowHelper.loadJslModel(parameters.getModelName(), parameters.getJslModel(), parameters.getJslModelSourceURI());
	}
}
