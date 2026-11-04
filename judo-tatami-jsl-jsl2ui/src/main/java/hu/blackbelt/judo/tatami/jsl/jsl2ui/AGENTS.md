# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui`

Epsilon-free Java orchestration, ETL engine SPI and workflow integration for JSL-to-UI (Zeta runs here; ETL lives in `judo-tatami-jsl-jsl2ui-etl`).

| File | Purpose |
| --- | --- |
| `Jsl2UiEtlEngine.java` | SPI interface for optional ETL add-on `judo-tatami-jsl-jsl2ui-etl`. Exports `ARTIFACT_COORDINATES` (`hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2ui-etl`), `void execute(JslDslModel, UiModel, Logger, createTrace, parallel)`; `log` may be `null`. Resolved by `Jsl2UiWork` via `EtlEngineRegistry`. See change: split-etl-artifacts. |
| `Jsl2UiTransformationTrace.java` | Captures bidirectional model mapping trace between `JslDslModel` and `UiModel`. Implements `TransformationTrace`; exports `fromModelsAndTrace()`, `save()`, and EMF resource URI handler methods. |
| `Jsl2UiWork.java` | Work step for JSL→UI; Zeta in-module, ETL via `Jsl2UiEtlEngine` add-on. → see `Jsl2UiWork.java.AGENTS.md`. See change: split-etl-artifacts. |
