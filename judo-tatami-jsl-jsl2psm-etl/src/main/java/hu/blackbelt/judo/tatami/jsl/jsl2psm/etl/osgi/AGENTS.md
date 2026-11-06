# judo-tatami-jsl-jsl2psm-etl/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/etl/osgi
OSGi wiring of the ETL add-on.
| File | Purpose |
|---|---|
| `Jsl2PsmEtlEngineComponent.java` | DS `@Component(immediate = true)`; `activate(BundleContext)` resolves bundle entry `/tatami/jsl2psm/transformations/psm/jslToPsm.etl` to `.`, builds `Jsl2PsmEtlEngineImpl(scriptRoot)`, calls `EtlEngineRegistry.register(Jsl2PsmEtlEngine.class, engine)`; `deactivate()` unregisters. Makes the engine visible to core bundle in Karaf. See change: split-etl-artifacts. |
