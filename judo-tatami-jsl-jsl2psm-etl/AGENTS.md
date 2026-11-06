# judo-tatami-jsl-jsl2psm-etl — module agent doctrine

ETL add-on for the JSL→PSM stage. Epsilon reaches a classpath only through this artifact; core `judo-tatami-jsl-jsl2psm` is Epsilon-free and runs Zeta by default.

## Contents

- `src/main/java/.../jsl2psm/etl/Jsl2Psm.java` — ETL entry point, moved from core; package `hu.blackbelt.judo.tatami.jsl.jsl2psm.etl`.
- `src/main/java/.../jsl2psm/etl/Jsl2PsmEtlEngineImpl.java` — implements core `Jsl2PsmEtlEngine` by delegating to `Jsl2Psm`.
- `src/main/java/.../jsl2psm/etl/osgi/Jsl2PsmEtlEngineComponent.java` — DS component; registers the engine in `EtlEngineRegistry` on activate (script root = bundle entry `/tatami/jsl2psm/transformations/psm/jslToPsm.etl` resolved to `.`), unregisters on deactivate.
- `src/main/resources/META-INF/services/hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmEtlEngine` — `ServiceLoader` registration.
- `src/main/epsilon/**` — ETL/EOL scripts, moved from core; pom copies them to `target/classes/tatami/jsl2psm` and sets the `Jsl2Psm-Transformation-ScriptRoot` bundle header.
- `src/test/java/**`, `src/test/resources/**` — ETL, dual-engine (ETL vs Zeta) and perf tests + fixtures, moved from core; `Jsl2PsmWorkTest` (Zeta) stays in core.

## Discovery

Plain Java and Spring Boot: `ServiceLoader` via `META-INF/services`. Karaf: DS component (`ServiceLoader` does not cross bundle class loaders). Core `Jsl2PsmWork` resolves the engine only in `ETL`/`DUAL` mode via `EtlEngineRegistry.require(Jsl2PsmEtlEngine.class, Jsl2PsmEtlEngine.ARTIFACT_COORDINATES)`; a missing add-on throws `EtlEngineMissingException` naming `hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2psm-etl`. The workflow default is ZETA. This add-on alone does NOT make a run ETL: every downstream stage Work also reads `judo.transformation.mode`. ETL/DUAL users add the aggregate `judo-tatami-jsl-workflow-etl` (type `pom`; Maven plugin: as plugin dependency), not this artifact alone.

## Invariants

- Core never imports `.etl` classes; the dependency points `-etl` → core only.
- Package stays `.etl` (sub-package of core) to avoid an OSGi split package.
- Tests drive `etl.Jsl2Psm`; `new Jsl2PsmWork(ctx)` is one-arg.

Doc discipline: see repo root `../AGENTS.md`. See change: split-etl-artifacts.
