# Proposal: Generalized Dual Comparison Test Infrastructure

**Target module:** `judo-tatami-base` → `judo-tatami-test-utils`
**Package:** `hu.blackbelt.judo.tatami.test.util`
**Consumers:** `judo-tatami-jsl` (jsl2psm, jsl2ui), and any future transformation module

## Problem

Every transformation module in `judo-tatami-jsl` duplicates ~300-450 LOC of dual comparison test infrastructure (ETL vs ZETA). The following are copy-pasted across `jsl2psm` and `jsl2ui`:

- `ModelConfig` inner class (properties parsing)
- `TestResult` record (timing + comparison result)
- `printSummary()` (~40 LOC summary table)
- `writeJsonResults()` (~50 LOC JSON export)
- `countElements()` helper
- `resolveJslFiles()` file resolution
- `loadModelConfigs()` properties loading
- Timing measurement boilerplate
- Comparison invocation + result handling

Current test classes with this duplication:
- `Jsl2PsmDualTransformationTest` (1299 LOC)
- `Jsl2PsmDiscoveryComparisonTest` (~460 LOC)
- `Jsl2UiDualTransformationTest` (~300 LOC)
- `Jsl2UiExternalDualComparisonTest` (~290 LOC)
- `Jsl2UiDiscoveryComparisonTest` (~458 LOC)

## Goal

Extract a generic, maximally configurable dual comparison test base class into `judo-tatami-test-utils` that:

1. Is **generic** with `<S, T>` — no JSL/PSM/UI knowledge
2. Is **cross-module reusable** — any transformation module can extend it
3. Keeps **performance measurement as a separate utility** (mixin/wrapper), not baked in
4. Supports both **inline model tests** (unit tests) and **external model discovery** (integration tests)
5. Reduces per-module external dual test code from ~300-450 LOC to ~30 LOC

## Existing Infrastructure to Build On

`judo-tatami-test-utils` already contains:

| Class | What it does |
|-------|-------------|
| `ModelComparator` | STRICT/SKELETON EMF model comparison (2326 LOC) |
| `StructuralModelComparator` | Checksum-based comparison with subtree skipping (746 LOC) |
| `AbstractExternalModelTest` | Parametrized testing of transformations against external model files (1044 LOC) |
| `ExternalModelConfig` | Model config POJO (name, path, companions, behaviours) |
| `ModelChecksumCalculator` | Checksum computation for structural comparison |

The new classes should **compose with** these, not duplicate them.

## Architecture

### New Classes to Create

```
hu.blackbelt.judo.tatami.test.util/
├── AbstractDualComparisonTest<S, T>           # core base class
├── AbstractExternalDualComparisonTest<S, T>   # external model discovery + @TestFactory
├── DualComparisonConfig                        # configuration POJO
├── DualComparisonResult                        # per-model result record
├── DualComparisonReporter                      # summary table + JSON output
└── PerformanceMeasurement                      # timing wrapper (standalone utility)
```

### 1. `AbstractDualComparisonTest<S, T>`

Core base class. `S` = source model type, `T` = target model type.

```java
public abstract class AbstractDualComparisonTest<S, T> {

    // --- Template methods (subclass MUST implement) ---

    /** Run ETL transformation on source model, return target model */
    protected abstract T executeEtl(S source) throws Exception;

    /** Run ZETA transformation on source model, return target model */
    protected abstract T executeZeta(S source) throws Exception;

    /** Extract root EObject from target model for comparison */
    protected abstract EObject getRoot(T model);

    /** Count elements in target model */
    protected abstract int countElements(T model);

    // --- Optional overrides ---

    /** Comparison mode, default from system property or STRICT */
    protected ComparisonMode getComparisonMode() {
        return ModelComparator.getConfiguredMode();
    }

    /** Module name for reporting (e.g., "jsl2ui") */
    protected String getModuleName() {
        return getClass().getSimpleName();
    }

    // --- Provided methods (final) ---

    /** Compare ETL vs ZETA output, return result without failing */
    protected final DualComparisonResult compareModels(S source, String modelName) throws Exception {
        T etlResult = executeEtl(source);
        T zetaResult = executeZeta(source);

        int etlElements = countElements(etlResult);
        int zetaElements = countElements(zetaResult);

        if (etlElements == 0 && zetaElements == 0) {
            return DualComparisonResult.equivalent(modelName, etlElements, zetaElements);
        }

        EObject etlRoot = getRoot(etlResult);
        EObject zetaRoot = getRoot(zetaResult);
        ComparisonResult result = ModelComparator.compare(etlRoot, zetaRoot, getComparisonMode());

        return DualComparisonResult.from(modelName, result, etlElements, zetaElements);
    }

    /** Compare and fail on diff */
    protected final void assertEquivalent(S source, String modelName) throws Exception {
        DualComparisonResult result = compareModels(source, modelName);
        if (!result.isEquivalent()) {
            fail(getModuleName() + " comparison failed for " + modelName + ":\n"
                + result.getDifferenceReport());
        }
    }
}
```

### 2. `AbstractExternalDualComparisonTest<S, T>`

Extends the base with external model discovery, `@TestFactory`, performance timing, and summary reporting.

```java
public abstract class AbstractExternalDualComparisonTest<S, T>
    extends AbstractDualComparisonTest<S, T> {

    private final DualComparisonReporter reporter = new DualComparisonReporter();

    // --- Additional template methods ---

    /** Parse source model from files */
    protected abstract S parseSource(List<File> files) throws Exception;

    /** Properties file name on classpath (e.g., "external-model-tests.properties") */
    protected abstract String getPropertiesFile();

    // --- Optional overrides ---

    /** System property for base dir discovery (default: "judo.test.discovery.basedir") */
    protected String getBaseDirProperty() {
        return "judo.test.discovery.basedir";
    }

    /** Whether to measure performance (default: from system property) */
    protected DualComparisonConfig getConfig() {
        return DualComparisonConfig.fromSystemProperties();
    }

    // --- Provided ---

    @TestFactory
    Collection<DynamicTest> compareExternalModels() {
        List<ExternalModelConfig> configs = loadAndDiscoverModels();
        Assumptions.assumeTrue(!configs.isEmpty(),
            "No models found (configure " + getPropertiesFile() + ")");

        reporter.clear();
        DualComparisonConfig config = getConfig();

        List<DynamicTest> tests = configs.stream()
            .map(mc -> DynamicTest.dynamicTest(mc.getName(), () -> testModel(mc, config)))
            .collect(Collectors.toList());

        tests.add(DynamicTest.dynamicTest("== Summary ==", () -> {
            reporter.printSummary(getModuleName());
            reporter.writeJson(Path.of("target"), getModuleName());
        }));
        return tests;
    }

    private void testModel(ExternalModelConfig modelConfig, DualComparisonConfig config)
        throws Exception {
        List<File> files = modelConfig.resolveFiles();
        Assumptions.assumeTrue(files.get(0).exists(),
            "File not found: " + files.get(0));

        // Parse (not timed)
        S source = parseSource(files);

        // ETL (timed)
        S etlSource = parseSource(files);
        var etl = PerformanceMeasurement.measure(() -> executeEtl(etlSource));

        // ZETA (timed, optionally averaged)
        S zetaSource = parseSource(files);
        var zeta = config.getZetaIterations() > 1
            ? PerformanceMeasurement.measureAvg(
                () -> { try { return executeZeta(parseSource(files)); }
                        catch (Exception e) { throw new RuntimeException(e); } },
                config.getZetaIterations())
            : PerformanceMeasurement.measure(() -> executeZeta(zetaSource));

        int etlElements = countElements(etl.result());
        int zetaElements = countElements(zeta.result());
        double speedup = zeta.durationMs() > 0
            ? (double) etl.durationMs() / zeta.durationMs() : 0;

        // Compare
        DualComparisonResult result;
        if (etlElements == 0 && zetaElements == 0) {
            result = DualComparisonResult.equivalent(modelConfig.getName(), 0, 0);
        } else {
            EObject etlRoot = getRoot(etl.result());
            EObject zetaRoot = getRoot(zeta.result());
            var cmp = ModelComparator.compare(etlRoot, zetaRoot, getComparisonMode());
            result = DualComparisonResult.from(modelConfig.getName(), cmp,
                etlElements, zetaElements);
        }
        result = result.withTiming(etl.durationMs(), zeta.durationMs(), speedup);
        reporter.addResult(result);

        if (config.isFailOnDiff() && !result.isEquivalent()) {
            fail("Comparison failed for " + modelConfig.getName() + ":\n"
                + result.getDifferenceReport());
        }
    }
}
```

### 3. `DualComparisonConfig`

```java
public class DualComparisonConfig {
    private ComparisonMode comparisonMode = ComparisonMode.STRICT;
    private int zetaIterations = 1;
    private boolean warmupEnabled = false;
    private boolean failOnDiff = true;
    private Path jsonOutputDir = Path.of("target");

    public static DualComparisonConfig fromSystemProperties() {
        // Reads: judo.test.comparison.mode, judo.test.zeta.iterations,
        //        judo.test.warmup, judo.test.fail.on.diff
    }

    // Builder pattern or simple setters
}
```

### 4. `DualComparisonResult`

```java
public class DualComparisonResult {
    private final String modelName;
    private final boolean equivalent;
    private final int differenceCount;
    private final String differenceReport;
    private final int etlElementCount;
    private final int zetaElementCount;

    // Optional timing (null if not measured)
    private final Long etlTimeMs;
    private final Long zetaTimeMs;
    private final Double speedup;

    public static DualComparisonResult equivalent(String name, int etlCount, int zetaCount);
    public static DualComparisonResult from(String name, ComparisonResult cmp, int etlCount, int zetaCount);
    public DualComparisonResult withTiming(long etlMs, long zetaMs, double speedup);
}
```

### 5. `DualComparisonReporter`

```java
public class DualComparisonReporter {
    private final List<DualComparisonResult> results = new CopyOnWriteArrayList<>();

    public void addResult(DualComparisonResult r);
    public void clear();
    public List<DualComparisonResult> getResults();

    /** Print formatted summary table to SLF4J logger */
    public void printSummary(String moduleName);

    /** Write JSON results to file */
    public void writeJson(Path outputDir, String moduleName);
}
```

### 6. `PerformanceMeasurement`

Standalone utility, not coupled to the test hierarchy.

```java
public class PerformanceMeasurement {

    public record TimedResult<T>(T result, long durationMs) {}

    /** Measure a single execution */
    public static <T> TimedResult<T> measure(Callable<T> action) throws Exception {
        long start = System.currentTimeMillis();
        T result = action.call();
        return new TimedResult<>(result, System.currentTimeMillis() - start);
    }

    /** Measure N executions, return last result with average time */
    public static <T> TimedResult<T> measureAvg(Callable<T> action, int iterations)
        throws Exception {
        long total = 0;
        T result = null;
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            result = action.call();
            total += System.currentTimeMillis() - start;
        }
        return new TimedResult<>(result, total / iterations);
    }
}
```

## Consumer Example: jsl2ui External Dual Test

After this change, the entire `Jsl2UiExternalDualComparisonTest` becomes:

```java
public class Jsl2UiExternalDualComparisonTest
    extends AbstractExternalDualComparisonTest<JslDslModel, UiModel> {

    @Override protected String getPropertiesFile() {
        return "external-model-tests.properties";
    }

    @Override protected String getModuleName() { return "jsl2ui"; }

    @Override protected JslDslModel parseSource(List<File> files) throws Exception {
        return JslParser.getModelFromFiles(files);
    }

    @Override protected UiModel executeEtl(JslDslModel m) throws Exception {
        UiModel ui = buildUiModel().name(m.getName()).build();
        executeJsl2UiTransformation(jsl2UiParameter()
            .jslModel(m).uiModel(ui).createTrace(false));
        return ui;
    }

    @Override protected UiModel executeZeta(JslDslModel m) {
        UiModel ui = buildUiModel().name(m.getName()).build();
        Jsl2UiZetaTransformation.builder()
            .jslModel(m).uiModel(ui).defaultModelName(m.getName())
            .build().execute();
        return ui;
    }

    @Override protected EObject getRoot(UiModel m) {
        return m.getResourceSet().getResources().get(0).getContents().get(0);
    }

    @Override protected int countElements(UiModel m) {
        int count = 0;
        for (Resource r : m.getResourceSet().getResources()) {
            var it = r.getAllContents();
            while (it.hasNext()) { it.next(); count++; }
        }
        return count;
    }
}
```

**~30 LOC** vs the current **~290 LOC**.

## Consumer Example: jsl2ui Inline Dual Test

The inline test stays as a unit test, just extends the base:

```java
class Jsl2UiDualTransformationTest
    extends AbstractDualComparisonTest<JslDslModel, UiModel> {

    // Same executeEtl, executeZeta, getRoot, countElements as above

    @Test void testSimpleActor() throws Exception {
        JslDslModel model = JslParser.getModelFromStrings("Test", List.of("""
            model Test;
            entity E { field String name; }
            view EView(E e) { group g { field String name <= e.name; } }
            actor TestActor { menu EMenu(E[] e) { table ETable(EView ev); } }
        """));
        assertEquivalent(model, "testSimpleActor");
    }
}
```

## System Properties

| Property | Default | Description |
|----------|---------|-------------|
| `judo.test.comparison.mode` | `STRICT` | STRICT, SKELETON, or STRUCTURAL |
| `judo.test.zeta.iterations` | `1` | Number of ZETA runs for averaging |
| `judo.test.warmup` | `false` | Enable warmup run before measurement |
| `judo.test.fail.on.diff` | `true` | Fail test on diff (false = log only) |
| `judo.test.discovery.basedir` | (none) | Directory for auto-discovery of model files |

## Relationship to Existing `AbstractExternalModelTest`

`AbstractExternalModelTest` runs a **single** transformation (ETL or ZETA) per model and uses `StructuralModelComparator` for structural comparison. It is **not** a dual comparison tool.

The new `AbstractExternalDualComparisonTest` is a **different concern**: it runs **both** ETL and ZETA on the same source and compares their outputs. It reuses `ExternalModelConfig` for model loading but has its own test lifecycle.

These two class hierarchies are **parallel, not nested**:

```
AbstractExternalModelTest         ← single transformation + structural check
AbstractDualComparisonTest        ← dual transformation + model comparison
  └─ AbstractExternalDualComparisonTest  ← + external model discovery
```

## Implementation Order

1. `PerformanceMeasurement` — standalone, no dependencies
2. `DualComparisonConfig` — standalone POJO
3. `DualComparisonResult` — standalone record
4. `DualComparisonReporter` — depends on DualComparisonResult
5. `AbstractDualComparisonTest<S, T>` — depends on ModelComparator, DualComparisonResult
6. `AbstractExternalDualComparisonTest<S, T>` — depends on all above + ExternalModelConfig

## Testing

Unit tests in `judo-tatami-test-utils` using mock implementations of the abstract classes with simple in-memory EMF models.
