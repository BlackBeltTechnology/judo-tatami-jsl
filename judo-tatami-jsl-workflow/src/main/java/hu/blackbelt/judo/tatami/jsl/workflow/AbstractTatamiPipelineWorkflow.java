package hu.blackbelt.judo.tatami.jsl.workflow;

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
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.blackbelt.judo.tatami.asm2keycloak.Asm2KeycloakWork.Asm2KeycloakWorkParameter.asm2KeycloakWorkParameter;
import static hu.blackbelt.judo.tatami.asm2rdbms.Asm2RdbmsWork.Asm2RdbmsWorkParameter.asm2RdbmsWorkParameter;
import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.ParallelFlow.Builder.aNewParallelFlow;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;
import static hu.blackbelt.judo.tatami.psm2asm.Psm2AsmWork.Psm2AsmWorkParameter.psm2AsmWorkParameter;
import static hu.blackbelt.judo.tatami.psm2measure.Psm2MeasureWork.Psm2MeasureWorkParameter.psm2MeasureWorkParameter;


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

    @SuppressWarnings("unchecked")
    public WorkReport startDefaultWorkflow() {
        TransformationContext.TransformationContextVerifier verifier = transformationContext.transformationContextVerifier;
        WorkflowHelper workflowHelper = new WorkflowHelper(transformationContext, metrics);

        transformationContext.put(Jsl2PsmWork.Jsl2PsmWorkParameter.jsl2PsmWorkParameter()
                        .generateBehaviours(parameters.getGenerateBehaviours())
                .createTrace(!parameters.getIgnoreJsl2PsmTrace()).build());

        transformationContext.put(psm2AsmWorkParameter()
                .createTrace(!parameters.getIgnorePsm2AsmTrace())
                .useCache(parameters.getUseCache())
                .parallel(parameters.getRunInParallel())
                .build());
        transformationContext.put(psm2MeasureWorkParameter()
                .createTrace(!parameters.getIgnorePsm2MeasureTrace())
                .useCache(parameters.getUseCache())
                .parallel(parameters.getRunInParallel())
                .build());
        transformationContext.put(asm2RdbmsWorkParameter()
                .createTrace(!parameters.getIgnoreAsm2RdbmsTrace())
                .useCache(parameters.getUseCache())
                .parallel(parameters.getRunInParallel())
                .createSimpleName(parameters.getRdbmsCreateSimpleName())
                .nameSize(parameters.getRdbmsNameSize())
                .shortNameSize(parameters.getRdbmsShortNameSize())
                .tablePrefix(parameters.getRdbmsTablePrefix())
                .columnPrefix(parameters.getRdbmsColumnPrefix())
                .foreignKeyPrefix(parameters.getRdbmsForeignKeyPrefix())
                .inverseForeignKeyPrefix(parameters.getRdbmsInverseForeignKeyPrefix())
                .junctionTablePrefix(parameters.getRdbmsJunctionTablePrefix())
                .build());
        transformationContext.put(asm2KeycloakWorkParameter()
                .createTrace(!parameters.getIgnoreAsm2KeycloakTrace())
                .useCache(parameters.getUseCache())
                .parallel(parameters.getRunInParallel())
                .build());

        loadModels(workflowHelper, metrics, transformationContext, parameters);

//        Optional<Work> validateJslWork = parameters.getValidateModels() && verifier.verifyClassPresent(JslDslModel.class) ?
//                Optional.of(workflowHelper.createJslValidateWork()) :
//                Optional.empty();

        Optional<Work> validatePsmWork = parameters.getValidateModels() && verifier.verifyClassPresent(PsmModel.class) ?
                Optional.of(workflowHelper.createPsmValidateWork()) :
                Optional.empty();

        Optional<Work> createPsmWork = parameters.getIgnoreJsl2Psm() || workflowHelper.jsl2PsmOutputPredicate().get() ?
                Optional.empty() :
                Optional.of(workflowHelper.createJsl2PsmWork());

        Optional<Work> createUiWork = parameters.getIgnoreJsl2Ui() || workflowHelper.jsl2UiOutputPredicate().get() ?
                Optional.empty() :
                Optional.of(workflowHelper.createJsl2UiWork());

        Optional<Work> createAsmWork = parameters.getIgnorePsm2Asm() || workflowHelper.psm2AsmOutputPredicate().get() ?
                Optional.empty() :
                Optional.of(workflowHelper.createPsm2AsmWork());

        Optional<Work> createMeasureWork = parameters.getIgnorePsm2Measure() || workflowHelper.psm2MeasureOutputPredicate().get() ?
                Optional.empty() :
                Optional.of(workflowHelper.createPsm2MeasureWork());

        Optional<Work> createExpressionWork = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2Expression() || workflowHelper.asm2ExpressionOutputPredicate().get() ?
                Optional.empty() :
                Optional.of(workflowHelper.createAsm2ExpressionWork(parameters.getValidateModels()));

        Stream<Optional<Work>> createRdbmsWorks = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2Rdbms() ?
                Stream.empty() :
                parameters.getDialectList()
                        .stream()
                        .filter(dialect -> !workflowHelper.asm2RdbmsOutputPredicate(dialect).get())
                        .map(dialect -> Optional.of(workflowHelper.createAsm2RdbmsWork(dialect, parameters.getIgnoreRdbms2Liquibase()))
                        );

        @SuppressWarnings("unused")
        Stream<Optional<Work>> createLiquibaseWorks = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2Rdbms() || parameters.getIgnoreRdbms2Liquibase() ?
                Stream.empty() :
                parameters.getDialectList()
                        .stream()
                        .filter(dialect -> !workflowHelper.rdbms2LiquibaseOutputPredicate(dialect).get())
                        .map(dialect -> Optional.of(workflowHelper.createRdbms2LiquibaseWork(dialect))
                        );

        Optional<Work> createKeycloakWork = parameters.getIgnorePsm2Asm() || parameters.getIgnoreAsm2Keycloak() || workflowHelper.asm2KeycloakOutputPredicate().get() ?
                Optional.empty() :
                Optional.of(workflowHelper.createAsm2KeycloakWork());


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
                                            .execute(Stream.of(createPsmWork, createUiWork))
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
                                                    Stream.of(createExpressionWork, createKeycloakWork),
                                                    createRdbmsWorks
                                            ))
                                            .build()),
                            Optional.of(
                                    aNewParallelFlow()
                                            .named("Parallel RDBMS Transformations")
                                            .execute(createLiquibaseWorks)
                                            .build())

                            ).build();
        } else {
            workflow = aNewSequentialFlow()
                    .named("Run all transformations sequentially")
                    .execute(
                            Stream.concat(
                                Stream.of(
                                        /*validateJslWork,*/
                                        validatePsmWork,
                                        createPsmWork,
                                        createMeasureWork,
                                        createAsmWork,
                                        createExpressionWork),
                                Stream.concat(createRdbmsWorks, createLiquibaseWorks))
                            ).build();
        }

        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        WorkReport workReport = workFlowEngine.run(workflow);

        if (workReport.getStatus() == WorkStatus.FAILED) {
            throw new IllegalStateException("Transformation failed", workReport.getError());
        }

        if (parameters.getEnableMetrics()) {
            int maxRowWidth = metrics.getExecutionTimes().entrySet().stream().map(k -> k.getKey().length()).max(Comparator.comparing(k -> k)).orElse(0);
            log.info("Workflow summary: {}", metrics.getExecutionTimes().entrySet().stream()
                    .map(e -> "\n  - " + e.getKey() + " ".repeat(maxRowWidth - e.getKey().length()) + " " + ((double) e.getValue()) / 1000.0f + " s")
                    .collect(Collectors.joining()));
        }

        return workReport;
    }
}
