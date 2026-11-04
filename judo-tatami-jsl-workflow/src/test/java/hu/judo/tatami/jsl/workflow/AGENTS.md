# AGENTS.md — `judo-tatami-jsl-workflow/src/test/java/hu/judo/tatami/jsl/workflow`

Test sources for the JSL default workflow. Two fixture builders emit a JSL and a PSM
model under `target/test-classes/`; the test drives `JslDefaultWorkflow` over the JSL
fixture and then re-feeds the saved models back through `WorkflowHelper`.

| File | Purpose |
| --- | --- |
| `JslDefaultWorkflowTest.java` | Runs `JslDefaultWorkflow` end-to-end on the `test` JSL fixture with dialect `hsqldb`, asserts `WorkStatus.COMPLETED` plus regenerated `test-psm.model`, and reloads saved models via `WorkflowHelper`. → see `JslDefaultWorkflowTest.java.AGENTS.md` |
| `JslTestModel.java` | Builds and persists the shared JSL fixture. Exports `MODEL_NAME = "test"`, `FILE_LOCATION = target/test-classes/jsl/test-jsl.model`, `createJslModelAndSave()` which assembles a `ModelDeclaration` named `test` holding `EntityDeclaration` `A` and `B` and calls `saveJslDslModel()`. Throws `JslDslModel.JslDslValidationException` when the built model fails validation. |
| `PsmTestModel.java` | Builds and saves a standalone PSM fixture (`Model` `M`: inheritance chains, all primitive type kinds, measured type, containment, bidirectional association) via `createPsmModelelAndSave()`. → see `PsmTestModel.java.AGENTS.md` |
