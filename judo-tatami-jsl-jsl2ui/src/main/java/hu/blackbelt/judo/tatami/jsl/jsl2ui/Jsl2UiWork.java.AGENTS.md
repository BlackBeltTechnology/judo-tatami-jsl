# `Jsl2UiWork.java`

`AbstractTransformationWork` for the JSL→UI step. Exports `Jsl2UiWork`, `Jsl2UiWorkParameter` (`createTrace`, `parallel`, `transformationMode` default `TransformationMode.fromSystemProperty()`), `execute`, `main`.
Only ctor: `Jsl2UiWork(TransformationContext)`; the script-URI ctor and field are removed.
ETL branch (`mode.isEtl()`, also DUAL): `EtlEngineRegistry.require(Jsl2UiEtlEngine.class, Jsl2UiEtlEngine.ARTIFACT_COORDINATES).execute(...)`; add-on absent → `EtlEngineMissingException` naming `hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2ui-etl`.
Zeta branch (`mode.isZeta()`): `Jsl2UiZetaTransformation` in this artifact, unchanged. DUAL runs ETL then Zeta. Core is Epsilon-free.
Covered by `Jsl2UiWorkEtlMissingTest` here; ETL/DUAL tests live in `judo-tatami-jsl-jsl2ui-etl`.

See change: split-etl-artifacts.
