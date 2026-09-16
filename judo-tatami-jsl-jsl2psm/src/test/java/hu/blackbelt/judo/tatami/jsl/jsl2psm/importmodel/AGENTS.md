# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/importmodel`

Integration tests for multi-model JSL imports and cross-model namespace resolution.

| File | Purpose |
| --- | --- |
| `JslMultipleJslModelImportTest.java` | Parameterized test across ETL and ZETA validating cross-namespace model imports (`ns1::a`, `ns1::b`, `ns2::c`) and element resolution into PSM packages and entity types. |
