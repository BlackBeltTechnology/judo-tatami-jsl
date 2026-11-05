# AGENTS.md — `judo-tatami-jsl-jsl2ui-etl/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2ui`

Base test fixture and workflow execution tests for the ETL JSL-to-UI transformation (moved from core; `new Jsl2UiWork(ctx)` one-arg ctor). See change: split-etl-artifacts.

| File | Purpose |
| --- | --- |
| `AbstractTest.java` | Base test fixture providing `transform(TransformationMode)`, UI validation via `UiValidator`, trace mapping, and page container/definition assertions. Subclasses must assign `jslModel` before calling `transform`. |
| `Jsl2UiWorkTest.java` | Parameterized test executing JSL-to-UI transformation inside a `WorkFlowEngine` sequential flow across ETL and ZETA modes, validating output UI model resources. |
