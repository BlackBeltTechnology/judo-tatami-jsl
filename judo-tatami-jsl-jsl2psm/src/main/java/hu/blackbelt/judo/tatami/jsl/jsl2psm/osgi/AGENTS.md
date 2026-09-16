# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/osgi`

OSGi service components tracking JSL DSL models and managing their lifecycle transformations into PSM models.

| File | Purpose |
| --- | --- |
| `Jsl2PsmTransformationJslModelTracker.java` | OSGi service component tracking active `JslDslModel` instances and registering transformed `PsmModel` OSGi services. Exports `install(JslDslModel)`, `uninstall(JslDslModel)`, `getModelClass()`. Ignores duplicate model registrations if model name already exists in internal cache. |
| `Jsl2PsmTransformationService.java` | OSGi service coordinating ETL execution from `JslDslModel` to `PsmModel` via `Jsl2Psm`. Exports `activate(BundleContext)`, `install(JslDslModel)`, `uninstall(JslDslModel)`. Locates `/tatami/jsl2psm/transformations/psm/jslToPsm.etl` bundle resource and registers resulting `TransformationTrace` services in OSGi registry. |
