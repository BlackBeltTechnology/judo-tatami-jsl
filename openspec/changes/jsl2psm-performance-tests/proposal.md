## Why

The existing `Jsl2PsmDiscoveryComparisonTest` runs ETL and ZETA each once per model with no warmup, no multi-iteration averaging, and no JSON result export. This makes performance measurements unreliable and hard to track over time. We need a proper performance test that runs ETL once (for baseline + comparison model) and ZETA multiple times (for averaged metrics), with STRICT model comparison by default, plus a bash script for easy execution.

## What Changes

- Rewrite `Jsl2PsmDiscoveryComparisonTest` to support configurable warmup and multi-iteration ZETA runs (ETL runs once per model)
- Use STRICT comparison mode by default (compares EAnnotations, not just structure)
- Add JSON result export to `target/comparison-results.json`
- Create `execute-performance-tests.sh` bash script in project root for easy execution with sensible defaults

## Capabilities

### New Capabilities
- `perf-test-runner`: Bash script and enhanced test class for JSL2PSM performance benchmarking with multi-iteration ZETA runs, STRICT comparison, and JSON export

### Modified Capabilities

## Impact

- `judo-tatami-jsl-jsl2psm/src/test/java/.../perf/Jsl2PsmDiscoveryComparisonTest.java` — rewritten
- `execute-performance-tests.sh` — new file in project root
- No API or dependency changes
