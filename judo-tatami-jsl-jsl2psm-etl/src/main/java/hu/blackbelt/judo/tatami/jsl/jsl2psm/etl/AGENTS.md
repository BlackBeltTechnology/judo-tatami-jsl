# judo-tatami-jsl-jsl2psm-etl/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/etl
ETL entry point of the JSL→PSM stage plus its engine adapter. Sub-package `.etl` keeps core and add-on split-package free.
| File | Purpose |
|---|---|
| `Jsl2Psm.java` | Runs the ETL JSL→PSM transformation (Epsilon `EtlExecutionContext`). Exports `Jsl2PsmParameter` (builder `jsl2PsmParameter`), `executeJsl2PsmTransformation(...)`, `calculateJsl2PsmTransformationScriptURI()`, `SCRIPT_ROOT_TATAMI_JSL_2_PSM`. Requires non-null `jslModel`/`psmModel`; trace via `EtlTransformationTraceExtractor` (`judo-tatami-util-etl`). Package `.etl` (moved from core). See change: split-etl-artifacts. |
| `Jsl2PsmEtlEngineImpl.java` | Implements core `Jsl2PsmEtlEngine`. Ctor `()` resolves scripts via `Jsl2Psm.calculateJsl2PsmTransformationScriptURI()`; ctor `(URI scriptRoot)` for OSGi. `execute(...)` builds `Jsl2PsmParameter` and calls `Jsl2Psm.executeJsl2PsmTransformation`; `log` may be `null`. Loaded by `ServiceLoader` or `Jsl2PsmEtlEngineComponent`. See change: split-etl-artifacts. |
