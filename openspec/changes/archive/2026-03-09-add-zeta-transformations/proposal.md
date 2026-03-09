## Why

The JSL transformation pipeline (jsl2psm and jsl2ui) currently relies solely on Epsilon ETL for model transformations. The sister project judo-tatami-base has already migrated to support dual-engine transformations (ETL + Zeta), achieving 30-45x performance improvements with the Java-based Zeta engine. This project needs the same migration to maintain consistency across the platform, improve build times, and enable future deprecation of the ETL engine.

## What Changes

- Add Java-based Zeta transformation implementations for **jsl2psm** (367 ETL rules, 158 EOL operations) and **jsl2ui** (397 ETL rules, 243 EOL operations)
- Port all EOL extension operations to static Java helper classes
- Keep both ETL and Zeta engines running in parallel (dual transformation mode) with `TransformationMode` enum (default: ETL, overridable)
- Add `TransformationMode` parameter to `Jsl2PsmWork`, `Jsl2UiWork`, `DefaultWorkflowSetupParameters`, and Maven plugin configuration
- Parameterize existing jsl2psm test suites to run with both ETL and Zeta engines; jsl2ui tests run ETL-only with separate Zeta dual tests
- Add `DualTransformationTest` classes using `ModelComparator` to assert ETL/Zeta output equivalence
- Add performance comparison tests using a synthetic model (based on RackInspect characteristics) with ~10,000 elements
- Add judo-zeta framework as SNAPSHOT dependency; switch tatami-core and tatami-util to SNAPSHOT versions
- Add judo-tatami-test-utils as test-scoped dependency for `ModelComparator` and model generators
- Use `@PreExecution` / `@PostExecution` Zeta annotations for jsl2ui position tracking and sorting (matching ETL pre/post blocks)
- Use constants for all rule names in Zeta transformations for reference safety
- Convert all `.adoc` documentation to Markdown
- Update README and existing documentation

## Capabilities

### New Capabilities
- `zeta-jsl2psm`: Java-based Zeta transformation engine for JSL to PSM conversion, replicating all 367 ETL rules and 158 EOL operations
- `zeta-jsl2ui`: Java-based Zeta transformation engine for JSL to UI conversion, replicating all 397 ETL rules and 243 EOL operations, including per-frontend iteration with @PreExecution/@PostExecution hooks
- `dual-transformation-testing`: jsl2psm tests parameterized with `@EnumSource(TransformationMode.class)`, jsl2ui `AbstractTest.transform(mode)` supports both engines (individual test parameterization pending Zeta gap fixes), plus ModelComparator-based equivalence and discovery comparison tests
- `transformation-mode-config`: TransformationMode parameter support in Work classes, workflow, and Maven plugin (default ETL, overridable to ZETA)
- `performance-testing`: Synthetic model generator and performance comparison benchmarks for ETL vs Zeta
- `transformation-docs`: Converted `.adoc` documentation to Markdown and updated project documentation (README, AGENTS.md, plugin docs)

### Modified Capabilities

## Impact

- **Dependencies**: Add judo-zeta (SNAPSHOT), switch judo-tatami-core and judo-tatami-util to SNAPSHOT, add judo-tatami-test-utils (test scope). All must be compiled locally first.
- **judo-tatami-jsl-jsl2psm**: New `zeta/` package with transformation classes, rule classes, helper classes. Modified `Jsl2PsmWork` to support dual engine.
- **judo-tatami-jsl-jsl2ui**: New `zeta/` package. Modified `Jsl2UiWork` to support dual engine. Per-frontend Zeta execution with pre/post hooks.
- **judo-tatami-jsl-workflow**: Modified `DefaultWorkflowSetupParameters` and `WorkflowHelper` to propagate `TransformationMode`.
- **judo-tatami-jsl-workflow-maven-plugin**: New `transformationMode` configuration parameter.
- **Test suites**: jsl2psm tests parameterized for dual-engine execution. jsl2ui uses separate Zeta dual tests (9 @Disabled documenting known view-rule gaps). New DualTransformationTest, DiscoveryComparisonTest, and RealisticPerformanceTest classes.
- **Documentation**: `.adoc` files converted to Markdown, updated README and AGENTS.md with Zeta architecture.
- **Build**: Must compile judo-zeta, judo-tatami-core, judo-tatami-util locally before building this project.
