## Tasks

- [x] **Task 1: Write failing test for inherited relation default value** — Add a parameterized dual test that transforms `RelationWithDefaultsModel.jsl` using both ETL and ZETA, then asserts STRICT equivalence. Focus on `CollectorWithSingleOptionalDefaultRelationExtended` and `CollectorWithSingleRequiredDefaultRelationExtended` — verify their `TransferObjectRelation.defaultValue` is set. Verify: test fails with 2 value mismatches (defaultValue: NavigationProperty vs null).
- [x] **Task 2: Fix cloneEntityRelation to set defaultValue** — In `Jsl2PsmHelper.cloneEntityRelation()` (~line 1197-1200), replace the empty comment block with `defaultValue` resolution via `ctx.equivalent()`. Verify: test from Task 1 passes.
- [x] **Task 3: Run full external model comparison suite** — Run `Jsl2PsmDiscoveryComparisonTest` with `-Pperformance` and confirm 51/51 PASS (including `itest-RelationWithDefaults`). Verify: 0 failures, 0 diffs.
