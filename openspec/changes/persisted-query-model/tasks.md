# Tasks

Module-level tasks of the master change `persisted-query-model` (superproject `openspec/changes/persisted-query-model`)
for `judo-tatami-jsl` (rank 9): the workflows produce the query artifact.

## 1. Workflow stage, save and parameters (4.3)

- [x] 1.1 `judo-tatami-jsl`: the same as 4.1 for `JslDefaultWorkflow`, its `DefaultWorkflowSave` copy and
      `judo-tatami-jsl-workflow-maven-plugin`. Verify: `JslDefaultWorkflowTest` asserts the query file; the
      maven-plugin-test output contains it.

## 2. Documentation (4.4)

- [x] 2.1 Documentation: spec deltas `workflow`, `workflow-maven-plugin` (judo-tatami-jsl), plus `AGENTS.md` rows.
      Verify: `openspec validate` in this store.

## 3. Folded tests

- [x] 3.1 Add unit test: JSL workflow writes the query file — copy harness glue from runtime/judo-tatami-jsl/judo-tatami-jsl-workflow/src/test/java/hu/judo/tatami/jsl/workflow/JslDefaultWorkflowTest.java; Triple: JSL fixture model of `JslDefaultWorkflowTest`, default params / workflow run + save / the save dir holds `<name>-query.model` (test-plan #TP-2)
