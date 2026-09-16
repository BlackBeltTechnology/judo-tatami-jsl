# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui`

Java orchestration and workflow integration executing JSL-to-UI transformations using Epsilon ETL and Zeta engines.

| File | Purpose |
| --- | --- |
| `Jsl2Ui.java` | Runs JSL-to-UI ETL transformation pipeline via Epsilon `ExecutionContext`. Exports `executeJsl2UiTransformation(Jsl2UiParameter)` and script URI resolver `calculateJsl2UiTransformationScriptURI()`. |
| `Jsl2UiTransformationTrace.java` | Captures bidirectional model mapping trace between `JslDslModel` and `UiModel`. Implements `TransformationTrace`; exports `fromModelsAndTrace()`, `save()`, and EMF resource URI handler methods. |
| `Jsl2UiWork.java` | Bridges JSL-to-UI transformation into Tatami workflow engine as an `AbstractTransformationWork` task; switches execution between Epsilon ETL and `Jsl2UiZetaTransformation` based on `TransformationMode`. |
