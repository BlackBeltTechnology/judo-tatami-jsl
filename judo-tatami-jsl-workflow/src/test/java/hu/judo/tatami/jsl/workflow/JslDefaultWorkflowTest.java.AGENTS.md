# `JslDefaultWorkflowTest.java`

End-to-end test of `JslDefaultWorkflow` over the `test` JSL fixture with dialect list
`hsqldb`.

**Key exports**

- `JslDefaultWorkflowTest` — JUnit 5 test class.
- `TARGET_TEST_CLASSES = new File("target/test-classes/jsl")`,
  `TARGET_CLASSES = "target/test-classes/jsl"` — public output locations shared with the
  workflow save step.
- `setUp()` (`@BeforeEach`), `testDefaultWorkflow()`, `testTransformationContextLoad()`,
  `testTransformationContextPartialLoad()`.

**What setUp does**

1. Calls `JslTestModel.createJslModelAndSave()` to write `test-jsl.model`.
2. Deletes `target/test-classes/jsl/test-psm.model` so a stale PSM cannot make the
   assertion pass.
3. Builds `JslDefaultWorkflow` from `DefaultWorkflowSetupParameters` with
   `jslModelSourceURI` = `FILE_LOCATION`, `dialectList` = `hsqldb`, `modelName` = `test`.
4. Runs `startDefaultWorkflow()`, keeps the `WorkReport`, then calls
   `DefaultWorkflowSave.saveModels(ctx, TARGET_TEST_CLASSES, ImmutableList.of("hsqldb"))`.

**What the tests assert**

- `testDefaultWorkflow` — `workReport.getStatus()` equals `WorkStatus.COMPLETED` and the
  deleted `test-psm.model` exists again, proving the JSL→PSM leg actually ran.
- `testTransformationContextLoad` — rebuilds the workflow, pre-seeds it through
  `WorkflowHelper.loadJslModel` / `loadPsmModel` from the saved `test-jsl.model` /
  `test-psm.model`, then restarts the workflow to check a pre-populated transformation
  context is accepted.
- `testTransformationContextPartialLoad` — same seeding without restarting; exercises
  loading alone.

**Contracts a caller can violate**

- Both reload tests read the files `setUp` saved; skipping or reordering the save step
  makes `WorkflowHelper.load*` point at absent URIs.
- Deleting `psmModel` in `setUp` is load-bearing — without it `testDefaultWorkflow` would
  pass on a leftover artifact from a previous run.
- Tests write into the shared `target/test-classes/jsl` directory under the fixed model
  name `test`; they are not safe to run concurrently against each other.
