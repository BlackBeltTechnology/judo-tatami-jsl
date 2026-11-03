# judo-tatami-jsl-jsl2ui-etl — module agent doctrine

ETL add-on for the JSL→UI stage. Epsilon reaches a classpath only through this artifact; core `judo-tatami-jsl-jsl2ui` is Epsilon-free and runs Zeta by default.

## Contents

- `src/main/java/.../jsl2ui/etl/Jsl2Ui.java` — ETL entry point, moved from core; package `hu.blackbelt.judo.tatami.jsl.jsl2ui.etl`.
- `src/main/java/.../jsl2ui/etl/Jsl2UiEtlEngineImpl.java` — implements core `Jsl2UiEtlEngine` by delegating to `Jsl2Ui`.
- `src/main/java/.../jsl2ui/etl/osgi/Jsl2UiEtlEngineComponent.java` — DS component; registers the engine in `EtlEngineRegistry` on activate (script root = bundle entry `/tatami/jsl2ui/transformations/ui/jslToUi.etl` resolved to `.`), unregisters on deactivate.
- `src/main/resources/META-INF/services/hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2UiEtlEngine` — `ServiceLoader` registration.
- `src/main/epsilon/**` — ETL/EOL scripts, moved from core; pom copies them to `target/classes/tatami/jsl2ui` and sets the `Jsl2Ui-Transformation-ScriptRoot` bundle header.
- `src/test/java/**`, `src/test/resources/**` — ETL, dual-engine (ETL vs Zeta) and perf tests + fixtures, moved from core.

## Discovery

Plain Java and Spring Boot: `ServiceLoader` via `META-INF/services`. Karaf: DS component (`ServiceLoader` does not cross bundle class loaders). Core `Jsl2UiWork` resolves the engine only in `ETL`/`DUAL` mode via `EtlEngineRegistry.require(Jsl2UiEtlEngine.class, Jsl2UiEtlEngine.ARTIFACT_COORDINATES)`; a missing add-on throws `EtlEngineMissingException` naming `hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2ui-etl`. The workflow default is ZETA. This add-on alone does NOT make a run ETL: every downstream stage Work also reads `judo.transformation.mode`. ETL/DUAL users add the aggregate `judo-tatami-jsl-workflow-etl` (type `pom`; Maven plugin: as plugin dependency), not this artifact alone.

## Invariants

- Core never imports `.etl` classes; the dependency points `-etl` → core only.
- Package stays `.etl` (sub-package of core) to avoid an OSGi split package.
- Tests drive `etl.Jsl2Ui`; `new Jsl2UiWork(ctx)` is one-arg.

Doc discipline: see repo root `../AGENTS.md`. See change: split-etl-artifacts.
