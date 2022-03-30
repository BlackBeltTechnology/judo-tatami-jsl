package hu.blackbelt.judo.tatami.jsl.workflow;

import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngine;
import hu.blackbelt.judo.tatami.core.workflow.flow.WorkFlow;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.core.workflow.work.Work;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReport;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkStatus;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmWork;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.ParallelFlow.Builder.aNewParallelFlow;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;


@Slf4j
public abstract class AbstractTatamiPipelineWorkflow {

	@Getter
	protected final TransformationContext transformationContext;

	@Getter
	protected final DefaultWorkflowSetupParameters parameters;

	@Getter
	protected final WorkflowMetrics metrics;

	public AbstractTatamiPipelineWorkflow(DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder builder) {
		this(builder.build());
	}

	public AbstractTatamiPipelineWorkflow(DefaultWorkflowSetupParameters params) {
		this.parameters = params;
		this.transformationContext = new TransformationContext(this.parameters.getModelName());
		this.metrics = parameters.getEnableMetrics() ? new DefaultWorkflowMetricsCollector() : null;
	}

	abstract public void loadModels(WorkflowHelper workflowFactory, WorkflowMetrics metrics, TransformationContext transformationContext, DefaultWorkflowSetupParameters parameters);

	public WorkReport startDefaultWorkflow() {
		TransformationContext.TransformationContextVerifier verifier = transformationContext.transformationContextVerifier;
		WorkflowHelper workflowHelper = new WorkflowHelper(transformationContext, metrics);

		String modelVersion = parameters.getModelVersion();
		if (modelVersion == null || modelVersion.trim().equals("")) {
			modelVersion = "1.0.0." + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_SNAPSHOT";
		} else if (modelVersion.endsWith("-SNAPSHOT")) {
			modelVersion = modelVersion.replace("-SNAPSHOT", "." +
					new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_SNAPSHOT");
		}

		transformationContext.put(Jsl2PsmWork.Jsl2PsmWorkParameter.jsl2PsmWorkParameter().createTrace(!parameters.getIgnoreJsl2PsmTrace()).build());

		loadModels(workflowHelper, metrics, transformationContext, parameters);

//		Optional<Work> validateJslWork = parameters.getValidateModels() && verifier.verifyClassPresent(JslDslModel.class) ?
//				Optional.of(workflowHelper.createJslValidateWork()) :
//				Optional.empty();

		Optional<Work> validatePsmWork = parameters.getValidateModels() && verifier.verifyClassPresent(PsmModel.class) ?
				Optional.of(workflowHelper.createPsmValidateWork()) :
				Optional.empty();

		Optional<Work> createPsmWork = parameters.getIgnoreJsl2Psm() || workflowHelper.jsl2PsmOutputPredicate().get() ?
				Optional.empty() :
				Optional.of(workflowHelper.createJsl2PsmWork());

		WorkFlow workflow;

		if (parameters.getRunInParallel()) {
			workflow = aNewSequentialFlow()
					.named("Validate JSL and execute JSL, PSM transformations")
					.execute(
							Optional.of(
									aNewParallelFlow()
											.named("Parallel Validations")
											.execute(Stream.of(/*validateJslWork, */validatePsmWork))
											.build()),
							Optional.of(
									aNewParallelFlow()
											.named("Parallel JSL Transformations")
											.execute(Stream.of(createPsmWork))
											.build())

					).build();
		} else {
			workflow = aNewSequentialFlow()
					.named("Run all transformations sequentially")
					.execute(
							Stream.of(
									/*validateJslWork,*/
									validatePsmWork,
									createPsmWork
							)
					).build();
		}

		WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
		WorkReport workReport = workFlowEngine.run(workflow);

		if (workReport.getStatus() == WorkStatus.FAILED) {
			throw new IllegalStateException("Transformation failed", workReport.getError());
		}

		if (parameters.getEnableMetrics()) {
			log.info("Workflow summary: {}", metrics.getExecutionTimes().entrySet().stream()
					.map(e -> "\n  - " + e.getKey() + ": " + e.getValue()).collect(Collectors.joining()));
		}

		return workReport;
	}
}
