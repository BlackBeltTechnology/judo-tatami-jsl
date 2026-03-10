## Why

The JSL2UI Zeta transformation has a naming bug in `TransferRelationDeclarationRules` that causes all non-trivial UI models to produce different output than ETL. This blocks parameterized dual-engine testing and leaves JSL2UI without the same test parity that JSL2PSM already has. Additionally, the 27 inline test models are not available as external comparison models, and the 5 separate Zeta test classes duplicate coverage that parameterized tests would provide.

## What Changes

- Fix `TransferRelationDeclarationRules.java:67` — use `getFqName(source)` instead of `source.getName()` to match ETL naming
- Investigate and fix any other `getName()` vs `getFqName()` mismatches surfaced by enabling dual tests
- Enable the 9 currently `@Disabled` dual tests in `Jsl2UiDualTransformationTest`
- Parameterize all 12 existing jsl2ui test classes with `@ParameterizedTest @EnumSource(TransformationMode.class)` (matching jsl2psm pattern)
- Extract 27 inline JSL models from test classes into `.jsl` files under `src/test/resources/model/`
- Populate `external-model-tests.properties` with the 27 extracted models
- Delete the 5 separate `zeta/` test classes (coverage now provided by parameterized tests)

## Capabilities

### New Capabilities
- `jsl2ui-external-test-models`: Extracted `.jsl` test models with frontend/actor declarations for external discovery comparison testing

### Modified Capabilities
- `zeta-jsl2ui`: Fix naming bug in TransferRelationDeclarationRules to achieve ETL/Zeta parity
- `dual-transformation-testing`: Enable 9 disabled jsl2ui dual tests, parameterize all 12 jsl2ui test classes, delete redundant zeta/ test classes

## Impact

- **judo-tatami-jsl-jsl2ui main**: Fix in `zeta/rules/structure/TransferRelationDeclarationRules.java` (one-line change, potentially more if other naming mismatches found)
- **judo-tatami-jsl-jsl2ui tests**: 12 test classes converted from `@Test` to `@ParameterizedTest`, 5 zeta/ test classes deleted, 27 `.jsl` model files created, `external-model-tests.properties` populated
- **Test count**: Expected ~54 ETL tests + ~54 Zeta tests (doubled via parameterization), plus discovery comparison tests
