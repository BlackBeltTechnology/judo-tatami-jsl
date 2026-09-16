# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/dual`

Differential equivalence test suites comparing ETL and Zeta transformation engine outputs.

| File | Purpose |
| --- | --- |
| `Jsl2PsmDualTransformationTest.java` | Dual-engine regression suite executing identical JSL models through both ETL (`executeEtl`) and Zeta (`executeZeta`) transformations. Asserts structural equivalence of generated PSM models using `StructuralModelComparator`. |
