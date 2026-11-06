# `Jsl2PsmWork.java`

`AbstractTransformationWork` for the JSL→PSM step. Exports `Jsl2PsmWork`, `Jsl2PsmWorkParameter` (`createTrace`, `parallel`, `generateBehaviours`, `transformationMode` default `TransformationMode.fromSystemProperty()`), `execute`, `main`.
Only ctor: `Jsl2PsmWork(TransformationContext)`; the script-URI ctor and field are removed (script location is the `-etl` add-on's concern).
ETL branch (`mode.isEtl()`, also DUAL): `EtlEngineRegistry.require(Jsl2PsmEtlEngine.class, Jsl2PsmEtlEngine.ARTIFACT_COORDINATES).execute(...)`; add-on absent → `EtlEngineMissingException` naming `hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2psm-etl`. Returned `Jsl2PsmTransformationTrace` goes into the context.
Zeta branch (`mode.isZeta()`): `Jsl2PsmZetaTransformation` in this artifact, unchanged. DUAL runs ETL then Zeta. Core is Epsilon-free.
Covered by `Jsl2PsmWorkTest` (Zeta) and `Jsl2PsmWorkEtlMissingTest` here; ETL/DUAL tests live in `judo-tatami-jsl-jsl2psm-etl`.

See change: split-etl-artifacts.
