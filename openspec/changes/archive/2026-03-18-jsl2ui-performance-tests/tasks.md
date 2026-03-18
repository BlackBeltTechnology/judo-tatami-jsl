## 1. Create JSL2UI discovery performance test

- [x] 1.1 Create `Jsl2UiDiscoveryComparisonTest.java` in `jsl2ui/src/test/java/.../perf/` mirroring the JSL2PSM version
- [x] 1.2 Create `external-model-tests.properties` in `jsl2ui/src/test/resources/` with models that have actor/frontend declarations
- [x] 1.3 Run JSL2UI performance tests — verify they pass

## 2. Update bash script

- [x] 2.1 Add `--module jsl2psm|jsl2ui|all` flag to `execute-performance-tests.sh` (default: `jsl2psm`)
- [x] 2.2 Verify `--module jsl2ui` and `--module all` work
