# judo-tatami-jsl-workflow-etl — module agent doctrine

Dependency-only aggregate (`<packaging>pom</packaging>`, no code, no sources). Pulls every ETL (Epsilon) add-on the JSL workflow needs in `ETL`/`DUAL` mode. Core modules stay Epsilon-free; Epsilon reaches a classpath only through this artifact (or the individual add-ons).

## Contents

Ten dependencies, versions managed in parent `judo-tatami-jsl-parent` (`${judo-tatami-base-version}` for base add-ons):

- `judo-tatami-jsl-jsl2psm-etl`, `judo-tatami-jsl-jsl2ui-etl` — JSL front end (this repo).
- `judo-tatami-psm2asm-etl`, `judo-tatami-psm2measure-etl`, `judo-tatami-asm2rdbms-etl`, `judo-tatami-asm2keycloak-etl`, `judo-tatami-rdbms2liquibase-etl` — downstream stages (`judo-tatami-base`).
- `judo-tatami-psm-validation-etl`, `judo-tatami-asm-validation-etl`, `judo-tatami-expression-asm-validation-etl` — validations.

## Use

- Maven: declare with `<type>pom</type>` as `<dependency>` of `judo-tatami-jsl-workflow-maven-plugin` when `transformationMode` is `ETL`/`DUAL`.
- Runtime: put on classpath of `JSLTransformationModelLoader` (judo-runtime-core-jsl) when `-Djudo.transformation.mode=ETL`; test-scoped there.
- Karaf: not used; install feature `judo-platform-etl` instead.

## Invariants

- The two jsl add-ons alone are NOT enough: every downstream stage Work reads `judo.transformation.mode` and throws `EtlEngineMissingException` without its add-on.
- Adding a new `-etl` stage add-on to the pipeline means adding it here too.
- Not an OSGi bundle; `pom` packaging only.

Doc discipline: see repo root `../AGENTS.md`. See change: split-etl-artifacts.
