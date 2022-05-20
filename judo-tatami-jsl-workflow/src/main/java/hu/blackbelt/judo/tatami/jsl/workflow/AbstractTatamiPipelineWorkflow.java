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

import static hu.blackbelt.judo.tatami.asm2rdbms.Asm2RdbmsWork.Asm2RdbmsWorkParameter.asm2RdbmsWorkParameter;
import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.ParallelFlow.Builder.aNewParallelFlow;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;
import static hu.blackbelt.judo.tatami.psm2asm.Psm2AsmWork.Psm2AsmWorkParameter.psm2AsmWorkParameter;


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

		transformationContext.put(psm2AsmWorkParameter().createTrace(!parameters.getIgnorePsm2AsmTrace()).build());
		transformationContext.put(asm2RdbmsWorkParameter().createTrace(!parameters.getIgnoreAsm2Rdbms())
				.modelVersion(modelVersion).build());

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

		Optional<Work> createAsmWork = parameters.getIgnorePsm2Asm() || workflowHelper.psm2AsmOutputPredicate().get() ?
				Optional.empty() :
				Optional.of(workflowHelper.createPsm2AsmWork());

		Optional<Work> createMeasureWork = parameters.getIgnorePsm2Measure() || workflowHelper.psm2MeasureOutputPredicate().get() ?
				Optional.empty() :
				Optional.of(workflowHelper.createPsm2MeasureWork());

		Optional<Work> createExpressionWork = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2Expression() || workflowHelper.asm2ExpressionOutputPredicate().get() ?
				Optional.empty() :
				Optional.of(workflowHelper.createAsm2ExpressionWork(parameters.getValidateModels()));

		Optional<Work> createSDKWork = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2sdk() || workflowHelper.asm2SDKPredicate().get() ?
				Optional.empty() :
				Optional.of(workflowHelper.createAsm2SDKWork());

		Stream<Optional<Work>> createRdbmsWorks = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2Rdbms() ?
				Stream.empty() :
				parameters.getDialectList()
						.stream()
						.filter(dialect -> !workflowHelper.asm2RdbmsOutputPredicate(dialect).get())
						.map(dialect -> Optional.of(workflowHelper.createAsm2RdbmsWork(dialect, parameters.getIgnoreRdbms2Liquibase()))
						);

		Stream<Optional<Work>> createLiquibaseWorks = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2Rdbms() || parameters.getIgnoreRdbms2Liquibase() ?
				Stream.empty() :
				parameters.getDialectList()
						.stream()
						.filter(dialect -> !workflowHelper.rdbms2LiquibaseOutputPredicate(dialect).get())
						.map(dialect -> Optional.of(workflowHelper.createRdbms2LiquibaseWork(dialect))
						);

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
											.build()),

							Optional.of(
									aNewParallelFlow()
											.named("Parallel PSM Transformations")
											.execute(Stream.of(createAsmWork, createMeasureWork))
											.build()),
							Optional.of(
									aNewParallelFlow()
											.named("Parallel ASM Transformations")
											.execute(Stream.concat(
													Stream.of(createExpressionWork, createSDKWork),
													createRdbmsWorks
											))
											.build())

							).build();
		} else {
			workflow = aNewSequentialFlow()
					.named("Run all transformations sequentially")
					.execute(
							Stream.of(
									/*validateJslWork,*/
									validatePsmWork,
									createPsmWork,
									createMeasureWork,
									createAsmWork,
									createExpressionWork,
									createSDKWork
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
