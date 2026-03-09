## Why

The JSL transformation pipeline (jsl2psm and jsl2ui) currently relies solely on Epsilon ETL for model transformations. The sister project judo-tatami-base has already migrated to support dual-engine transformations (ETL + Zeta), achieving 30-45x performance improvements with the Java-based Zeta engine. This project needs the same migration to maintain consistency across the platform, improve build times, and enable future deprecation of the ETL engine.

## What Changes

- Add Java-based Zeta transformation implementations for **jsl2psm** (367 ETL rules, 158 EOL operations) and **jsl2ui** (397 ETL rules, 243 EOL operations)
- Port all EOL extension operations to static Java helper classes
- Keep both ETL and Zeta engines running in parallel (dual transformation mode) with `TransformationMode` enum (default: ZETA, overridable)
- Add `TransformationMode` parameter to `Jsl2PsmWork`, `Jsl2UiWork`, `DefaultWorkflowSetupParameters`, and Maven plugin configuration
- Parameterize existing test suites to run with both ETL and Zeta engines
- Add `DualTransformationTest` classes using `ModelComparator` to assert ETL/Zeta output equivalence
- Add performance comparison tests using a synthetic model (based on RackInspect characteristics) with ~10,000 elements
- Add judo-zeta framework as SNAPSHOT dependency; switch tatami-core and tatami-util to SNAPSHOT versions
- Add judo-tatami-test-utils as test-scoped dependency for `ModelComparator` and model generators
- Use `@PreExecution` / `@PostExecution` Zeta annotations for jsl2ui position tracking and sorting (matching ETL pre/post blocks)
- Use constants for all rule names in Zeta transformations for reference safety
- Convert all `.adoc` documentation to Markdown; convert PlantUML diagrams to Mermaid
- Document all transformation rules in `docs/transformations/`
- Update README and existing documentation

## Capabilities

### New Capabilities
- `zeta-jsl2psm`: Java-based Zeta transformation engine for JSL to PSM conversion, replicating all 367 ETL rules and 158 EOL operations
- `zeta-jsl2ui`: Java-based Zeta transformation engine for JSL to UI conversion, replicating all 397 ETL rules and 243 EOL operations, including per-frontend iteration with @PreExecution/@PostExecution hooks
- `dual-transformation-testing`: Parameterized test infrastructure running existing test suites against both ETL and Zeta engines, plus ModelComparator-based equivalence tests
- `transformation-mode-config`: TransformationMode parameter support in Work classes, workflow, and Maven plugin (default ZETA, overridable to ETL)
- `performance-testing`: Synthetic model generator and performance comparison benchmarks for ETL vs Zeta
- `transformation-docs`: Markdown documentation of all transformation rules and updated project documentation

### Modified Capabilities

## Impact

- **Dependencies**: Add judo-zeta (SNAPSHOT), switch judo-tatami-core and judo-tatami-util to SNAPSHOT, add judo-tatami-test-utils (test scope). All must be compiled locally first.
- **judo-tatami-jsl-jsl2psm**: New `zeta/` package with transformation classes, rule classes, helper classes. Modified `Jsl2PsmWork` to support dual engine.
- **judo-tatami-jsl-jsl2ui**: New `zeta/` package. Modified `Jsl2UiWork` to support dual engine. Per-frontend Zeta execution with pre/post hooks.
- **judo-tatami-jsl-workflow**: Modified `DefaultWorkflowSetupParameters` and `WorkflowHelper` to propagate `TransformationMode`.
- **judo-tatami-jsl-workflow-maven-plugin**: New `transformationMode` configuration parameter.
- **Test suites**: All existing tests parameterized for dual-engine execution. New DualTransformationTest and performance test classes.
- **Documentation**: 10 `.adoc` files converted to Markdown, 14 PlantUML diagrams converted to Mermaid, new `docs/transformations/` directory.
- **Build**: Must compile judo-zeta, judo-tatami-core, judo-tatami-util locally before building this project.
