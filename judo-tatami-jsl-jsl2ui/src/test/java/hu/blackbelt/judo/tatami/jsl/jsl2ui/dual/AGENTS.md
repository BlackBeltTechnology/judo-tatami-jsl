# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/dual`

Dual-engine comparison and parity validation tests between ETL and ZETA JSL-to-UI engines.

| File | Purpose |
| --- | --- |
| `Jsl2UiDualTransformationTest.java` | Runs unit-level JSL models through both ETL and ZETA transformations, verifying structural and attribute equivalence using `ModelComparator`. |
| `Jsl2UiExternalDualComparisonTest.java` | Dynamic test factory executing dual ETL vs. ZETA transformations on models defined in `external-model-tests.properties`, asserting strict equality and reporting timing/speedup. |
