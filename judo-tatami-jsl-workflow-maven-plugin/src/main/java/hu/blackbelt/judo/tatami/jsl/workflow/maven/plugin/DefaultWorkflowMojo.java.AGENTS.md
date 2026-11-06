# `DefaultWorkflowMojo.java`

Maven Mojo `default-model-workflow` binding to COMPILE phase; sets up `DefaultWorkflow` parameters (PSM, ASM, RDBMS, Liquibase) and runs transformation workflow.

- `transformationMode` parameter defaults to `"ZETA"` (was `"ETL"`).
- Plugin ships no `-etl` add-on. ETL/DUAL need the aggregate `hu.blackbelt.judo.tatami:judo-tatami-jsl-workflow-etl` (`<type>pom</type>`) as a plugin `<dependency>`.
- `judo-tatami-jsl-jsl2psm-etl` + `-jsl2ui-etl` alone are NOT enough: every downstream stage Work reads `judo.transformation.mode` and needs its `-etl` add-on, else `EtlEngineMissingException`.

- `ignoreAsm2Query` param (property `ignoreAsm2Query`, default `false`) maps to `DefaultWorkflowSetupParameters.ignoreAsm2Query`; default build writes `<name>-query.model` next to the ASM model.

See change: split-etl-artifacts, persisted-query-model.
