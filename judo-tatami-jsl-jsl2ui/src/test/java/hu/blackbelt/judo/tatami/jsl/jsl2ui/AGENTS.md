# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2ui`

Core (Epsilon-free) JSL-to-UI tests: ETL-add-on-missing contract only. ETL, dual, perf tests and `AbstractTest` live in `judo-tatami-jsl-jsl2ui-etl`.

| File | Purpose |
| --- | --- |
| `Jsl2UiWorkEtlMissingTest.java` | ETL mode without `-etl` add-on on classpath throws `EtlEngineMissingException` naming `hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2ui-etl`; ZETA mode runs without add-on and the target `UiModel` is present. See change: split-etl-artifacts. |
