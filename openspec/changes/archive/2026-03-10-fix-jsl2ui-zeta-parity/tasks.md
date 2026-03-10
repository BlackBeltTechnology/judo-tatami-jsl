## 1. Fix Zeta Structural Bugs

- [x] 1.1 Fix `getFqName()` in `Jsl2UiHelper.java` — handle `MenuModifier`/`ProfileModifier` (no getName, delegate to container)
- [x] 1.2 Port missing `AccessTableViewPageDefinition` rule from ETL `menuTableDeclarationViewPage.etl` to Zeta `MenuTableDeclarationRules.java` (+ 4 lazy action rules)
- [x] 1.3 Fix `TransferDeclarationRules.java` — remove `ctx.addToResource(target)` after containment add to `Application.dataElements`
- [x] 1.4 Fix `TransferFieldDeclarationRules.java` — add missing `dataType` assignment via `getPrimitiveDeclarationEquivalent()`
- [x] 1.5 Fix `Jsl2UiDualTransformationTest.java` — replace `@Slf4j` with explicit Logger (Lombok not processed for tests)
- [x] 1.6 Remove `@Disabled` from 5 dual tests that now pass (simpleActor, actorWithMenu, entityWithCRUD, widgets, rowDeclarations, tabs)
- [x] 1.7 Fix remaining dual tests: view page link/action processing (ViewLinkDeclarationViewPageRules, ViewTableDeclarationViewPageRules), enum options (EnumerationMemberOption), menu link (JSL parse error — known limitation)
- [x] 1.8 Run `Jsl2UiDualTransformationTest` — 9/10 pass (1 @Disabled due to JSL parser limitation with #asActor() syntax)

## 2. Parameterize Existing Test Classes

- [x] 2.1 Parameterize `JslModel2UiApplicationTest` — 3 ETL+ZETA, 3 ETL-only (Zeta gaps)
- [x] 2.2 Parameterize `JslModel2UiCRUDTest` — 7 ETL-only (Zeta gaps: form pages, relation actions)
- [x] 2.3 Parameterize `JslModel2UiNavigationTest` — 1 ETL+ZETA, 1 ETL-only
- [x] 2.4 Parameterize `JslModel2UiOperationsTest` — 2 ETL+ZETA, 2 ETL-only
- [x] 2.5 Parameterize `JslModel2UiWidgetsTest` — 0 ETL+ZETA, 2 ETL-only
- [x] 2.6 Parameterize `JslModel2UiDataTest` — 1 ETL+ZETA, 2 ETL-only
- [x] 2.7 Parameterize `JslModel2UiRowOperationsTest` — 0 ETL+ZETA, 1 ETL-only
- [x] 2.8 Parameterize `JslModel2UiActionGroupsTest` — 0 ETL+ZETA, 2 ETL-only
- [x] 2.9 Parameterize `JSLModel2UiListItemTest` — 0 ETL+ZETA, 2 ETL-only
- [x] 2.10 Parameterize `JslModel2UiCarTest` — 0 ETL+ZETA, 1 ETL-only
- [x] 2.11 Parameterize `Jsl2UiWorkTest` — 1 ETL+ZETA
- [x] 2.12 Verify full test suite: 63 tests, 0 failures, 0 errors, 1 skipped

## 3. Delete Redundant Zeta Test Classes

- [x] 3.1-3.5 Verified: parameterized tests + dual tests cover all 5 zeta test class scenarios
- [x] 3.6 Deleted all 5 zeta/ test classes
- [x] 3.7 Build passes: 49 tests, 0 failures, 0 errors, 1 skipped

## 4. Extract Inline Models to Files

- [x] 4.1-4.11 Extracted 40 inline models to `src/test/resources/model/*.jsl`
- [x] 4.12 All 40 `.jsl` files parse successfully (verified via discovery test)

## 5. Configure External Comparison Tests

- [x] 5.1 Populated `external-model-tests.properties` with 39 entries (40 models minus ActorWithMenuLinkModel which has JSL parse issue)
- [x] 5.2 Discovery test: 40 tests run, 0 failures — 14 EQUIVALENT, 26 DIFF (documenting remaining Zeta gaps)
- [x] 5.3 Final build: `mvn clean test -pl judo-tatami-jsl-jsl2ui` — 49 tests, 0 failures, 0 errors, 1 skipped
