# `Jsl2UiTransformationService.java`

OSGi DS service. `install(JslDslModel)` builds `UiModel` (URI `ui:<name>.model`, unchanged), puts jslModel, uiModel and `Logger.class`→log into a `TransformationContext`, runs `new Jsl2UiWork(context).execute()`. Mode ZETA by default; ETL/DUAL need the `judo-tatami-jsl-jsl2ui-etl` bundle (its DS component registers the engine in `EtlEngineRegistry`).
No `TransformationTrace` service is registered (UI has no trace in this path); `uninstall` unregisters if present, else logs an error.
Exports `activate(BundleContext)`, `install`, `uninstall`. No Epsilon imports; no bundle-entry script lookup (moved to `Jsl2UiEtlEngineComponent`).

See change: split-etl-artifacts.
