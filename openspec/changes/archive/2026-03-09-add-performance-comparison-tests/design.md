## Context

The judo-tatami-jsl project transforms JSL (Judo Specification Language) models to PSM and UI models. The jsl2psm module now has dual transformation support (ETL via Epsilon and ZETA via Java annotations), with 163 parametrized tests passing for both modes. However, there are no performance comparison tests to validate structural equivalence and measure timing differences.

The judo-tatami-base project has established infrastructure for this:
- `AbstractExternalModelTest` — base class with model discovery, result recording, summary reporting
- `ModelComparator` — STRICT/STRUCTURAL/LENIENT comparison modes
- `ExternalModelConfig` — model path + parameters record
- `RealisticPsmModelGenerator` — synthetic model generator (RackInspect-like characteristics)
- Pattern: `DiscoveryComparisonTest` with `@TestFactory` + `@Tag("performance")`
- Pattern: `RealisticPerformanceTest` with warmup + single measurement

RackInspect is the reference real-world model (72 entities, 837 TOs, 19MB ASM output) but it's ESM-based, not JSL. For JSL performance tests we need synthetic JSL text generation.

## Goals / Non-Goals

**Goals:**
- Performance comparison tests for jsl2psm (ETL vs ZETA) with STRICT model comparison
- Performance comparison tests for jsl2ui (ETL-only timing, no ZETA yet)
- Discovery mechanism to auto-find `.jsl` test files
- Synthetic JSL model generator with RackInspect-scale characteristics
- Maven `performance` profile following judo-tatami-base pattern
- Measure transformation time only (exclude JSL parsing)

**Non-Goals:**
- Implementing ZETA transformation for jsl2ui (separate change)
- JMH microbenchmarks or statistical analysis (warmup + single run is sufficient)
- Cross-project changes to judo-tatami-base (use existing test-utils as-is)
- Modifying existing 163 functional tests

## Decisions

### 1. JSL model generator placement: local in jsl2psm test sources

**Decision:** Place `RealisticJslModelGenerator` in `jsl2psm/src/test/java/.../perf/` rather than in `judo-tatami-test-utils`.

**Rationale:** Avoids cross-project changes. The generator produces JSL text strings — it's specific to JSL, not reusable for ESM-based modules. The existing tests already use `JslParser.getModelFromStrings()` for inline JSL.

**Alternative considered:** Adding to `judo-tatami-test-utils` (like `RealisticPsmModelGenerator`). Rejected because it would require a tatami-base release cycle and the JSL generator is only useful in this project.

### 2. Model discovery: scan test resources + external properties

**Decision:** Three sources for model discovery:
1. Built-in: scan `src/test/resources/**/*.jsl` (19 files for jsl2psm, 1 for jsl2ui)
2. Properties: `external-model-tests.properties` with explicit paths
3. System property: `-Djudo.test.discovery.basedir` for ad-hoc directories

**Rationale:** Matches judo-tatami-base pattern exactly. Built-in scan ensures all existing test models are covered without configuration.

### 3. Measurement approach: warmup + single run

**Decision:** Single warmup iteration (ETL + ZETA) then single measured run for each, using fresh models for each execution. Report wall-clock milliseconds.

**Rationale:** Matches `RealisticPerformanceTest` pattern in tatami-base. Single run is sufficient for regression detection. Statistical analysis not needed.

### 4. Comparison mode: STRICT by default

**Decision:** Use `ModelComparator.ComparisonMode.STRICT` as the default comparison mode. Configurable via system property `judo.test.comparison.mode`.

**Rationale:** User requirement. STRICT validates all attributes and references match exactly (order-independent), ensuring ETL and ZETA produce identical PSM/UI models.

### 5. Test structure: @TestFactory dynamic tests

**Decision:** Use `@TestFactory` returning `Collection<DynamicTest>` with one test per discovered `.jsl` file, plus a summary test at the end.

**Rationale:** Matches `Psm2AsmDiscoveryComparisonTest` pattern. Dynamic tests give clear per-model reporting in test runners.

### 6. Exclude parsing from measurement

**Decision:** Parse JSL model once before measurement, then time only the `transform()` call.

**Rationale:** JSL parsing is identical for ETL and ZETA — it produces the same `JslDslModel`. Only the transformation differs.

## Risks / Trade-offs

- **[Risk] STRICT comparison may be too strict for initial Zeta port** → Mitigation: comparison mode is configurable via `-Djudo.test.comparison.mode=STRUCTURAL`. Tests log differences but can be configured to tolerate known gaps.
- **[Risk] Synthetic JSL model may not parse correctly** → Mitigation: validate generated JSL with `JslParser` in the generator itself. Use simple, well-tested JSL patterns.
- **[Risk] XMI ID collisions in Zeta already exist (logged as ERROR)** → Mitigation: XMI ID comparison is off by default (`judo.test.comparison.xmiIds=false`). STRICT mode compares structure, not XMI IDs.
- **[Trade-off] Generator in test sources is not reusable across projects** → Acceptable: only this project needs JSL generation.
