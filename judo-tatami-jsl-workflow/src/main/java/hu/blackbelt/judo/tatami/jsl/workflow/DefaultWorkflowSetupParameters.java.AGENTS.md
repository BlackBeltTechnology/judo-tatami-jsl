# `DefaultWorkflowSetupParameters.java`

Configuration parameter bean for Tatami workflow execution. Exports `DefaultWorkflowSetupParameters` builder with flags for parallel execution, dialect lists, model validation, caching, and transformation skip toggles.

- `transformationMode` defaults to `TransformationMode.DEFAULT` (ZETA; was ETL).
- ETL/DUAL need the aggregate `judo-tatami-jsl-workflow-etl` (type `pom`, all 10 add-ons) on the classpath.
- `judo-tatami-jsl-jsl2psm-etl` + `-jsl2ui-etl` alone are NOT enough: every downstream stage Work (psm2asm, psm2measure, asm2rdbms, asm2keycloak, rdbms2liquibase, validations) also reads `judo.transformation.mode` and needs its `-etl` add-on, else `EtlEngineMissingException`.
- Runtime callers (`JSLTransformationModelLoader`) pass `TransformationMode.fromSystemProperty()`; Maven callers use `DefaultWorkflowMojo` param `transformationMode`.

- `ignoreAsm2Query` defaults to `false`: the `asm2query` stage runs unless opted out.

See change: split-etl-artifacts, persisted-query-model.
