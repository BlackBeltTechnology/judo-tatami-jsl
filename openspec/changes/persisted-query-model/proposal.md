## Why

The runtimes rebuild the logical query model from JQL on every boot. The multi-module change
`openspec/changes/persisted-query-model` (judo-ng superproject) persists it as `<name>-query.model` next to the ASM
model. For this module that means the JSL workflow (`JslDefaultWorkflow`) and its Maven plugin must produce the
artifact in the same run and output directory as the other model files, so that models built from JSL sources are
bootable without the query build.

## What Changes

- `AbstractTatamiPipelineWorkflow` / `WorkflowHelper` gain an `asm2query` stage (`createAsm2QueryWork()`), placed
  **after** the parallel ASM transformation block, never concurrently with `asm2expression`, `asm2rdbms` or
  `asm2keycloak`.
- The stage needs the run's `ExpressionModel`. Without one it is skipped with a WARN and no artifact is written. It
  is also skipped when `ignorePsm2Asm` is set or when a `QueryModel` is already in the context (output predicate).
- `DefaultWorkflowSetupParameters` gets `ignoreAsm2Query` (default `false`).
- `DefaultWorkflowSave` writes `<name>-query.model` last, only when a `QueryModel` is in context and the expression
  model was saved; a save that writes the ASM without a `QueryModel` deletes a stale query file.
- `DefaultWorkflowMojo` (goal `default-model-workflow`) gets an `ignoreAsm2Query` parameter (default `false`) mapped
  onto `DefaultWorkflowSetupParameters`.
- `judo-tatami-jsl-workflow` depends on the new `judo-tatami-asm2query` module (judo-tatami-base).
- Documentation: spec deltas, the plugin parameter reference page, `AGENTS.md` rows.
- No BREAKING change in this module: the new parameter and stage are additive; set `ignoreAsm2Query=true` to opt
  out.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `workflow`: asm2query stage, skip rules, `ignoreAsm2Query`, query file save order and stale delete.
- `workflow-maven-plugin`: `ignoreAsm2Query` parameter.

## Impact

- `judo-tatami-jsl-workflow`: `AbstractTatamiPipelineWorkflow`, `WorkflowHelper`, `DefaultWorkflowSave`,
  `DefaultWorkflowSetupParameters`, `pom.xml` (dependency on `judo-tatami-asm2query`); test
  `JslDefaultWorkflowTest`.
- `judo-tatami-jsl-workflow-maven-plugin`: `DefaultWorkflowMojo`; `judo-tatami-jsl-workflow-maven-plugin-test`
  produces the artifact as a side effect; `docs/pages/judo-tatami-jsl-workflow-maven-plugin.md`.
- Depends on `judo-tatami-base` (rank 7, new `judo-tatami-asm2query`) being released and the dependency bump
  applied. This module is rank 9; consumers at rank 10 (`judo-runtime-core-jsl`, `judo-spring-maven-plugin`) pick
  the artifact up afterwards.
- Cost: one extra query build per model build (about 4 s on a large model, sequential).
- Rollback: `ignoreAsm2Query=true` (or the stage removed); delete the file. Runtimes then build as before. No
  database or schema impact.
