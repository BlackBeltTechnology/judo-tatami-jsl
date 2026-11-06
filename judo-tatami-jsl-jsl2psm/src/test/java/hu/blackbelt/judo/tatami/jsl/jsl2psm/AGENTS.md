# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm`

Core (Epsilon-free) JSL-to-PSM tests: Zeta-mode work test and ETL-add-on-missing contract. ETL, dual and perf tests live in `judo-tatami-jsl-jsl2psm-etl`.

| File | Purpose |
| --- | --- |
| `Jsl2PsmWorkEtlMissingTest.java` | ETL mode without `-etl` add-on on classpath throws `EtlEngineMissingException` naming `hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2psm-etl`; ZETA mode runs without add-on and the target `PsmModel` is present. See change: split-etl-artifacts. |
| `Jsl2PsmWorkTest.java` | Zeta-mode `Jsl2PsmWork` in a `SequentialFlow`; one-arg ctor `new Jsl2PsmWork(ctx)`. Verifies workflow status and PSM model creation from programmatically built `JslDslModel`. Stays in core; other tests moved to `-etl`. See change: split-etl-artifacts. |
| `Jsl2PsmZetaImportedModelSingletonTest.java` | Regression: `Main` imports `Externals` (mapped transfer) via `JslParser.getModelFromStrings`; ZETA run; asserts every `TransferAttribute.dataType` lies in the PSM resource and each `QueryCustomizer*Type` exists once. Fails without `getSingletonModelDeclaration`. See change: split-etl-artifacts. |
