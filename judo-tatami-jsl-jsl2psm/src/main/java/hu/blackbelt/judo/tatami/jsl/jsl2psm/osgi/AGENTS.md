# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/osgi`

OSGi service components tracking JSL DSL models and managing their lifecycle transformations into PSM models. Epsilon-free; ZETA by default.

| File | Purpose |
| --- | --- |
| `Jsl2PsmTransformationJslModelTracker.java` | OSGi service component tracking active `JslDslModel` instances and registering transformed `PsmModel` OSGi services. Exports `install(JslDslModel)`, `uninstall(JslDslModel)`, `getModelClass()`. Ignores duplicate model registrations if model name already exists in internal cache. |
| `Jsl2PsmTransformationService.java` | OSGi service building `PsmModel` via `Jsl2PsmWork` (ZETA default; ETL needs the `-etl` bundle). → see `Jsl2PsmTransformationService.java.AGENTS.md`. See change: split-etl-artifacts. |
