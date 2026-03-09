## Why

The judo-tatami-jsl project now supports dual transformation (ETL and ZETA) for jsl2psm, but there are no performance comparison tests to measure and validate that ZETA transformations produce equivalent output in comparable time. The judo-tatami-base project already has comprehensive performance test infrastructure (AbstractExternalModelTest, ModelComparator, RealisticPsmModelGenerator) that should be leveraged. Performance tests are needed to catch regressions and validate structural equivalence between ETL and ZETA outputs using STRICT comparison mode.

## What Changes

- Add `@Tag("performance")` discovery comparison tests for jsl2psm that auto-discover `.jsl` test models and run both ETL and ZETA transformations, comparing outputs with ModelComparator in STRICT mode
- Add `@Tag("performance")` discovery comparison tests for jsl2ui (ETL-only for now, since no ZETA implementation exists)
- Add a realistic JSL model generator that creates synthetic JSL text with RackInspect-like characteristics (70+ entities, transfers, relations, queries, actions)
- Add a realistic performance test using the synthetic model generator with warmup + single measurement
- Add Maven `performance` profile to parent pom.xml (excludedGroups override pattern from judo-tatami-base)
- Add `excludedGroups=performance` to default surefire config so performance tests are skipped in normal builds
- Add `external-model-tests.properties` for both modules to configure discoverable model paths
- Measure transformation time only (not JSL parsing), report throughput and speedup/slowdown ratios

## Capabilities

### New Capabilities
- `perf-discovery-tests`: Discovery-based comparison tests that find .jsl files and run ETL vs ZETA (jsl2psm) or ETL-only (jsl2ui) transformations, comparing output models with ModelComparator STRICT mode
- `perf-realistic-model`: Synthetic JSL model generator producing RackInspect-scale models and performance test using it with warmup + single measured run
- `perf-maven-profile`: Maven configuration to exclude performance tests from normal builds and run them on demand via -Pperformance

### Modified Capabilities

## Impact

- **pom.xml (parent)**: Add `excludedGroups=performance` to surefire config, add `performance` profile
- **judo-tatami-jsl-jsl2psm**: New test classes in `perf/` package, new `external-model-tests.properties`
- **judo-tatami-jsl-jsl2ui**: New test class in `perf/` package, new `external-model-tests.properties`
- **Dependencies**: Uses existing `judo-tatami-test-utils` (already a test dependency) — ModelComparator, AbstractExternalModelTest, ExternalModelConfig
- **No production code changes**
