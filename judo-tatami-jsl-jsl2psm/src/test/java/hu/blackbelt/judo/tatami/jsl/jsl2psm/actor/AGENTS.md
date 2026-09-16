# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/actor`

Dual-engine (ETL and Zeta) integration tests verifying actor and anonymous actor transformations to PSM operations and behaviours.

| File | Purpose |
| --- | --- |
| `JslModel2PsmActorTest.java` | Parameterized test suite across ETL and ZETA transformation modes verifying actor declarations map to PSM `BoundTransferOperation`, `UnboundOperation`, and `TransferOperationBehaviour`. Loads `ActorTestModel.jsl`. |
| `JslModel2PsmAnonymousActorTest.java` | Parameterized test suite across ETL and ZETA transformation modes verifying anonymous actor definitions map to PSM operations and behaviours without explicit role naming. Loads `AnonymousActorTestModel.jsl`. |
