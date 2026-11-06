## Approach

Mirror the JSL2PSM discovery performance test pattern for JSL2UI. The transformation API differs slightly (UI uses `executeJsl2UiTransformation` / `Jsl2UiZetaTransformation`) but the test structure is identical.

## Test Class Design

Same flow as `Jsl2PsmDiscoveryComparisonTest`:

1. Parse JSL files (not timed)
2. Optional warmup: ETL once + ZETA once
3. ETL: 1 run → timed, produces baseline UI model
4. ZETA: N runs → each timed, averaged, last produces comparison UI model
5. STRICT comparison of ETL vs ZETA UI models
6. Collect TestResult, print summary, export JSON

### ETL invocation
```java
UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
executeJsl2UiTransformation(jsl2UiParameter()
    .jslModel(jslModel).uiModel(uiModel).createTrace(false));
```

### Zeta invocation
```java
UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
Jsl2UiZetaTransformation.builder()
    .jslModel(jslModel).uiModel(uiModel)
    .defaultModelName(jslModel.getName()).build().execute();
```

## Model sources

`external-model-tests.properties` — only include models that have actor/frontend declarations (models without frontends produce empty UI models for both engines, which is valid but not meaningful for performance testing).

## Bash script update

Add `--module` flag to `execute-performance-tests.sh`:
- `jsl2psm` (default) — run JSL2PSM tests only
- `jsl2ui` — run JSL2UI tests only
- `all` — run both sequentially

## Key Decisions

- Reuse same `ModelConfig` pattern (same properties format)
- STRICT comparison by default
- Same system properties (`judo.test.zeta.iterations`, `judo.test.warmup`, `judo.test.comparison.mode`)
- JSON output to `judo-tatami-jsl-jsl2ui/target/comparison-results.json`
