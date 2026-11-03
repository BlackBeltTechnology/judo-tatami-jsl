# AGENTS.md — `judo-tatami-jsl-jsl2psm-etl/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm`

Integration test suites and shared test fixtures for the ETL JSL-to-PSM transformation (drive `etl.Jsl2Psm`; moved from core). See change: split-etl-artifacts.

| File | Purpose |
| --- | --- |
| `AbstractTest.java` | Shared base test fixture for JSL-to-PSM transformation tests. Exports `transform()`, `generateBehaviours()`, model accessor helpers (`getEntityTypes()`, `getTransferObjectTypes()`), and comprehensive assertions (`assertEntityType()`, `assertDataProperty()`, `assertAttribute()`, `assertRelation()`). Manages temporary model lifecycles and validation. |
| `TransformationParametersTest.java` | Test suite validating transformation customization parameters (entity/transfer object prefixes and postfixes, custom type generation, default transfer object omission). Overrides `addTransformationParameters()` per test scenario. |
