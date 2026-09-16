# AGENTS.md — `judo-tatami-jsl-workflow/src/main/java/hu/blackbelt/judo/tatami/jsl/workflow`

Pipeline workflow implementations and configuration for orchestrating JSL/PSM model transformations into ASM, UI, RDBMS, Liquibase, and Keycloak models.

| File | Purpose |
| --- | --- |
| `AbstractTatamiPipelineWorkflow.java` | Base workflow orchestrator defining sequential/parallel execution pipelines across JSL, PSM, ASM, and target models. Exports `startDefaultWorkflow()`, getters for `transformationContext`, `parameters`, and `metrics`. Enforces sequential/parallel model load and transformation stages. |
| `CheckWork.java` | Validation work unit testing boolean state against transformation context. Exports `CheckWork(Supplier<Boolean>)`, `call()`, `getName()`. Returns `FAILED` work report when supplier returns false or throws. |
| `DefaultWorkflow.java` | Pipeline workflow entry point loading JSL models from configured URI or instance. Exports `DefaultWorkflow(DefaultWorkflowSetupParameters)`, overrides `loadModels()`. Delegates model loading to `WorkflowHelper.loadJslModel()`. |
| `DefaultWorkflowMetricsCollector.java` | Thread-safe in-memory metrics tracker for transformation invocations, completions, failures, and execution times. Exports `invokedTransformation()`, `stoppedTransformation()`, `getInvocationCounts()`, `getCompletedCounts()`, `getFailedCounts()`, `getExecutionTimes()`. |
| `DefaultWorkflowSave.java` | Serializes transformed metamodels (ASM, Expression, JSL DSL, Keycloak, Liquibase, Measure, PSM, RDBMS, UI) and traces to target directory. Exports `saveModels()`, `saveFixedLiquibaseModel()`. |
| `DefaultWorkflowSetupParameters.java` | Configuration parameter bean for Tatami workflow execution. Exports `DefaultWorkflowSetupParameters` builder with flags for parallel execution, dialect lists, model validation, caching, and transformation skip toggles. |
| `JslDefaultWorkflow.java` | JSL-origin pipeline workflow implementation. Exports `JslDefaultWorkflow(DefaultWorkflowSetupParameters)`, overrides `loadModels()`. Loads JSL DSL model into transformation context via `WorkflowHelper.loadJslModel()`. |
| `PsmDefaultWorkflow.java` | PSM-origin pipeline workflow implementation bypassing JSL parsing. Exports `PsmDefaultWorkflow(DefaultWorkflowSetupParameters)`, overrides `loadModels()`. Loads pre-existing PSM model directly into transformation context via `WorkflowHelper.loadPsmModel()`. |
| `ThrowingCosumerWrapper.java` | Functional wrapper adapting throwing consumers to standard Java consumers. Exports `executeWrapper()`, `throwingConsumerWrapper()`, `quietConsumerWrapper()`. Re-throws checked exceptions as `RuntimeException` unless silent flag is set. |
| `WorkflowHelper.java` | Transformation factory and model loader instantiating Jsl2Psm, Jsl2Ui, Psm2Asm, Psm2Measure, Asm2Rdbms, Asm2Keycloak, and Rdbms2Liquibase work steps. Exports model loaders (`loadJslModel()`, `loadPsmModel()`, etc.) and work factories (`createJsl2PsmWork()`, `createPsm2AsmWork()`, etc.). |
| `WorkflowMetrics.java` | Metrics collector interface defining transformation telemetry contracts. Exports `getInvocationCounts()`, `getCompletedCounts()`, `getFailedCounts()`, and `getExecutionTimes()`. |
