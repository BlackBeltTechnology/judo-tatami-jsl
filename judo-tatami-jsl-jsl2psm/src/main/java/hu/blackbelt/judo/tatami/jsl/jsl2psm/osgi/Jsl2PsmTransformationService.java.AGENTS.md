# `Jsl2PsmTransformationService.java`

OSGi DS service. `install(JslDslModel)` builds `PsmModel` (URI `psm:<name>.model`, unchanged), puts jslModel, psmModel and `Logger.class`→log into a `TransformationContext`, runs `new Jsl2PsmWork(context).execute()`. Mode ZETA by default; ETL/DUAL need the `judo-tatami-jsl-jsl2psm-etl` bundle (its DS component registers the engine in `EtlEngineRegistry`).
Registers a `TransformationTrace` OSGi service only if a `Jsl2PsmTransformationTrace` is in the context (ETL ran); the Zeta path registers none. `uninstall` unregisters it if present, else logs an error.
Exports `activate(BundleContext)`, `install`, `uninstall`. No Epsilon imports; no bundle-entry script lookup (moved to `Jsl2PsmEtlEngineComponent`).

See change: split-etl-artifacts.
