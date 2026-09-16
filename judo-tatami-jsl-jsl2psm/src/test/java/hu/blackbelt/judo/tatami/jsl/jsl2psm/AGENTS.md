# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm`

Integration test suites and shared test fixtures for JSL-to-PSM transformations.

| File | Purpose |
| --- | --- |
| `AbstractTest.java` | Shared base test fixture for JSL-to-PSM transformation tests. Exports `transform()`, `generateBehaviours()`, model accessor helpers (`getEntityTypes()`, `getTransferObjectTypes()`), and comprehensive assertions (`assertEntityType()`, `assertDataProperty()`, `assertAttribute()`, `assertRelation()`). Manages temporary model lifecycles and validation. |
| `Jsl2PsmWorkTest.java` | Workflow engine integration test executing `Jsl2PsmWork` within a `SequentialFlow`. Verifies workflow status reporting and PSM model creation from programmatically constructed `JslDslModel`. |
| `TransformationParametersTest.java` | Test suite validating transformation customization parameters (entity/transfer object prefixes and postfixes, custom type generation, default transfer object omission). Overrides `addTransformationParameters()` per test scenario. |
