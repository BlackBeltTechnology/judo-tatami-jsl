## 1. Maven Configuration

- [x] 1.1 Add `<excludedGroups>performance</excludedGroups>` to the surefire plugin configuration in parent pom.xml
- [x] 1.2 Add `performance` Maven profile to parent pom.xml with `<excludedGroups combine.self="override" />` and `<groups>performance</groups>`
- [x] 1.3 Add comparison system property passthroughs (`judo.test.comparison.enabled`, `judo.test.comparison.mode`, `judo.test.comparison.maxDifferences`) to surefire systemPropertyVariables
- [x] 1.4 Verify existing 163 jsl2psm tests still pass with `mvn test -pl judo-tatami-jsl-jsl2psm`

## 2. JSL2PSM Discovery Comparison Test

- [x] 2.1 Create `external-model-tests.properties` in `judo-tatami-jsl-jsl2psm/src/test/resources/` listing all 18 built-in JSL test models
- [x] 2.2 Create `Jsl2PsmDiscoveryComparisonTest.java` in `judo-tatami-jsl-jsl2psm/src/test/java/.../perf/` with `@Tag("performance")` and `@TestFactory` that discovers `.jsl` files, runs both ETL and ZETA transformations, measures transformation time only (not parsing), compares output PSM models with ModelComparator STRICT mode, and prints per-model results plus summary
- [x] 2.3 Verify discovery test passes with `mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm -Dtest=Jsl2PsmDiscoveryComparisonTest`

## 3. JSL2UI Discovery Comparison Test

- [x] 3.1 Create `external-model-tests.properties` in `judo-tatami-jsl-jsl2ui/src/test/resources/` listing the TestShop.jsl model
- [x] 3.2 Create `Jsl2UiDiscoveryComparisonTest.java` in `judo-tatami-jsl-jsl2ui/src/test/java/.../perf/` with `@Tag("performance")` and `@TestFactory` that discovers `.jsl` files, runs ETL-only transformation, measures timing, and reports results (no ZETA comparison)
- [x] 3.3 Verify discovery test passes with `mvn test -Pperformance -pl judo-tatami-jsl-jsl2ui -Dtest=Jsl2UiDiscoveryComparisonTest`

## 4. Realistic JSL Model Generator

- [x] 4.1 Create `RealisticJslModelGenerator.java` in `judo-tatami-jsl-jsl2psm/src/test/java/.../perf/` that generates valid JSL text strings with configurable entity count, including entities (4 attrs, 2 relations), enums, mapped transfers (2 per entity), unmapped transfers (9 per entity), derived fields, queries, and actions matching RackInspect characteristics
- [x] 4.2 Verify generated JSL string parses successfully with `JslParser.getModelFromStrings()` for sizes 20, 70, and 100 entities

## 5. Realistic Performance Test

- [x] 5.1 Create `Jsl2PsmRealisticPerformanceTest.java` in `judo-tatami-jsl-jsl2psm/src/test/java/.../perf/` with `@Tag("performance")` that generates a 70-entity model, runs warmup (1 ETL + 1 ZETA on fresh models), then single measured run (1 ETL + 1 ZETA on fresh models), compares outputs with ModelComparator STRICT mode, and reports timing/throughput/speedup
- [x] 5.2 Verify realistic performance test passes with `mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm -Dtest=Jsl2PsmRealisticPerformanceTest`

## 6. Final Verification

- [x] 6.1 Verify all existing tests still pass: `mvn test` (performance tests excluded by default)
- [x] 6.2 Verify all performance tests pass: `mvn test -Pperformance`
