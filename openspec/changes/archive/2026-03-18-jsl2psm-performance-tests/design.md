## Approach

Rewrite `Jsl2PsmDiscoveryComparisonTest` following the pattern from `Esm2PsmDiscoveryComparisonTest` (judo-tatami-base), adapted for JSL model loading. Add a root-level bash script for easy invocation.

## Test Class Design

### Per-model flow

1. Parse JSL files (not timed)
2. Optional warmup: run ETL once + ZETA once (discarded)
3. ETL: 1 run → timed, produces baseline PSM model
4. ZETA: N runs → each timed, average computed, last run produces comparison PSM model
5. STRICT comparison of ETL vs ZETA PSM models via `ModelComparator`
6. Collect `TestResult` record

### Configuration via system properties

| Property | Default | Description |
|----------|---------|-------------|
| `judo.test.zeta.iterations` | 3 | Number of ZETA runs per model |
| `judo.test.warmup` | true | Enable warmup before measured runs |
| `judo.test.comparison.mode` | STRICT | ModelComparator comparison mode |

### Output

- Console summary table (model, ETL ms, ZETA avg ms, speedup, element counts, status)
- JSON file at `target/comparison-results.json` with full results + summary

### Model sources (unchanged)

1. `external-model-tests.properties` in classpath
2. System property `judo.test.discovery.basedir` for ad-hoc directories

## Bash Script Design

`execute-performance-tests.sh` in project root:
- Wraps `mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm -Dtest=Jsl2PsmDiscoveryComparisonTest`
- Accepts flags: `--iterations N`, `--models-dir PATH`, `--no-warmup`, `--comparison-mode MODE`
- Displays `target/comparison-results.json` path on completion

## Key Decisions

- **ETL runs once** — deterministic, slow; one run sufficient for baseline
- **ZETA runs multiple times** — fast, JIT-sensitive; averaging improves accuracy
- **STRICT by default** — EAnnotation parity matters for correctness
- **Keep standalone** — don't extend `AbstractExternalModelTest` (JSL parsing differs from ESM loading)
- **Reuse existing `ModelConfig`** — same properties format, same companions support
