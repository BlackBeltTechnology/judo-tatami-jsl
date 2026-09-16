# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/operation`

Integration tests for JSL action declarations and CRUD behaviour generation mapped to PSM operations.

| File | Purpose |
| --- | --- |
| `JslAction2PsmOperationTest.java` | Parameterized test across ETL and ZETA validating transformation of JSL actions into bound/unbound transfer operations, operation parameters, and return types. Loads `ActionTestModel.jsl`. |
| `JslModel2PsmCrudBehaviourTest.java` | Parameterized test across ETL and ZETA verifying generation of PSM CRUD operation behaviours (`CREATE`, `UPDATE`, `DELETE`, `LIST`, `GET`) on transfer objects. Loads `CrudBehaviourTestModel.jsl`. |
