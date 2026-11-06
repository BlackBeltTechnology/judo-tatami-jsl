# judo-tatami-jsl-jsl2ui-etl/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/etl/osgi
OSGi wiring of the ETL add-on.
| File | Purpose |
|---|---|
| `Jsl2UiEtlEngineComponent.java` | DS `@Component(immediate = true)`; `activate(BundleContext)` resolves bundle entry `/tatami/jsl2ui/transformations/ui/jslToUi.etl` to `.`, builds `Jsl2UiEtlEngineImpl(scriptRoot)`, calls `EtlEngineRegistry.register(Jsl2UiEtlEngine.class, engine)`; `deactivate()` unregisters. Makes the engine visible to core bundle in Karaf. See change: split-etl-artifacts. |
