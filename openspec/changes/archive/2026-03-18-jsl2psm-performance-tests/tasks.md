## 1. Rewrite Jsl2PsmDiscoveryComparisonTest

- [x] 1.1 Rewrite `Jsl2PsmDiscoveryComparisonTest.java` with: warmup support, ETL 1x + ZETA Nx per model, STRICT comparison default, `TestResult` record with speedup, console summary table, JSON export to `target/comparison-results.json`
- [x] 1.2 Run performance tests against built-in models to verify — all must pass with STRICT comparison

## 2. Create bash script

- [x] 2.1 Create `execute-performance-tests.sh` in project root with flags: `--iterations`, `--models-dir`, `--no-warmup`, `--comparison-mode`
- [x] 2.2 Verify script runs successfully
