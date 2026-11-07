# judo-tatami-jsl-jsl2ui-etl/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/etl
ETL entry point of the JSL→UI stage plus its engine adapter. Sub-package `.etl` keeps core and add-on split-package free.
| File | Purpose |
|---|---|
| `Jsl2Ui.java` | Runs the ETL JSL→UI transformation (Epsilon `EtlExecutionContext`). Exports `Jsl2UiParameter` (builder `jsl2UiParameter`), `executeJsl2UiTransformation(...)`, `calculateJsl2UiTransformationScriptURI()`, `SCRIPT_ROOT_TATAMI_JSL_2_UI`. Package `.etl` (moved from core). See change: split-etl-artifacts. |
| `Jsl2UiEtlEngineImpl.java` | Implements core `Jsl2UiEtlEngine`. Ctor `()` resolves scripts via `Jsl2Ui.calculateJsl2UiTransformationScriptURI()`; ctor `(URI scriptRoot)` for OSGi. `execute(...)` builds `Jsl2UiParameter` and calls `Jsl2Ui.executeJsl2UiTransformation`; `log` may be `null`. Loaded by `ServiceLoader` or `Jsl2UiEtlEngineComponent`. See change: split-etl-artifacts. |
