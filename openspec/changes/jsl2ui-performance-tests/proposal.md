## Why

The JSL2PSM module now has a proper discovery-based performance test with multi-iteration ZETA runs, STRICT comparison, and a bash script. The JSL2UI module has no equivalent — it lacks discovery tests entirely. Adding the same test type for JSL2UI ensures consistent performance tracking across both transformation pipelines.

## What Changes

- Create `Jsl2UiDiscoveryComparisonTest.java` in `jsl2ui/src/test/java/.../perf/` following the same pattern as the JSL2PSM version (ETL 1x, ZETA Nx, STRICT comparison, JSON export)
- Create `external-model-tests.properties` in `jsl2ui/src/test/resources/` wiring in JSL test models that have frontend/actor declarations
- Update `execute-performance-tests.sh` to support `--module jsl2psm|jsl2ui|all` flag

## Capabilities

### New Capabilities
- `jsl2ui-perf-tests`: Discovery-based performance comparison test for JSL2UI transformation with STRICT comparison and JSON export

### Modified Capabilities

## Impact

- `judo-tatami-jsl-jsl2ui/src/test/java/.../perf/Jsl2UiDiscoveryComparisonTest.java` — new file
- `judo-tatami-jsl-jsl2ui/src/test/resources/external-model-tests.properties` — new file
- `execute-performance-tests.sh` — updated with `--module` flag
