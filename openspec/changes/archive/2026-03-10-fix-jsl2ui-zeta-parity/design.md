## Context

JSL2PSM Zeta has full parity with ETL: all 27 test classes are parameterized with `@ParameterizedTest @EnumSource(TransformationMode.class)`, 91 external models are configured for discovery comparison, and there are no disabled dual tests.

JSL2UI Zeta has a naming bug (`TransferRelationDeclarationRules:67` uses `source.getName()` instead of `getFqName(source)`) that causes RelationType FQ names to differ from ETL. This blocks enabling the 9 disabled dual tests and prevents parameterizing the 12 existing test classes.

## Goals / Non-Goals

**Goals:**
- Fix all naming mismatches between ETL and Zeta JSL2UI output
- Achieve the same test infrastructure as JSL2PSM: parameterized tests, external model comparison, no duplicated test code
- Make the 9 disabled dual tests pass

**Non-Goals:**
- Fixing Zeta transformation logic beyond naming (all rules are already implemented)
- Changing ETL behavior
- Adding new Zeta rules

## Decisions

### 1. Fix-then-parameterize ordering
Fix the naming bug first, enable dual tests to verify parity, then parameterize. This avoids parameterizing tests that would immediately fail on Zeta.

### 2. Extract inline models to `.jsl` files
Place in `judo-tatami-jsl-jsl2ui/src/test/resources/model/`. Each file named after its model (e.g., `ApplicationTestModel.jsl`). Tests continue using `JslParser.getModelFromStrings()` — the extracted files are for external discovery comparison only.

**Alternative considered:** Converting tests to load from files. Rejected — too invasive, inline models in tests are clear and self-contained.

### 3. Delete separate zeta/ test classes
The 5 zeta/ test classes (`Jsl2UiZetaApplicationTest`, `Jsl2UiZetaViewTest`, `Jsl2UiZetaListItemTest`, `Jsl2UiZetaActionGroupsTest`, `Jsl2UiZetaRowOperationsTest`) will be deleted. Their coverage is subsumed by parameterized tests running with `TransformationMode.ZETA`.

### 4. Parameterization pattern
Same as JSL2PSM: change `@Test void testFoo()` to `@ParameterizedTest(name = "{0}") @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"}) void testFoo(TransformationMode mode)`, then call `transform(mode)` instead of `transform()`.

## Risks / Trade-offs

- **[Risk] Other naming bugs beyond line 67** → Mitigation: Enable dual tests first with ModelComparator; any remaining mismatches will surface as test failures before parameterizing.
- **[Risk] Zeta produces valid but structurally different models** → Mitigation: ModelComparator STRUCTURAL mode catches element-level differences. If true structural gaps exist, keep specific tests as `@EnumSource(names = {"ETL"})` with a comment explaining why.
- **[Risk] Test count doubles (~54 → ~108)** → Trade-off: Acceptable. JSL2PSM already runs this way. CI time increase is minimal since Zeta is faster than ETL.
