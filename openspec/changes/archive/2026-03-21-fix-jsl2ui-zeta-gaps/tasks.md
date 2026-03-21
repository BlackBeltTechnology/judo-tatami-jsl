## 1. Fix Claim.attributeType (Gap #2 — 3 tests)

- [x] 1.1 Enable ZETA in `@EnumSource` for `JslModel2UiCarTest.testCar`, `JslModel2UiApplicationTest.testMenu`, `JslModel2UiDataTest.testBasicData` — verify they fail
- [x] 1.2 In `ModifiableRules.java:claimModifier()`, after creating the Claim, resolve the identity's transfer field via `getIdentity(actorDeclaration).getField()` and call `Jsl2UiHelper.getTransferFieldAttributeType(field, ctx)` to set `target.setAttributeType(...)`
- [x] 1.3 Verify the 3 tests pass in both ETL and ZETA modes

## 2. Fix Action.actionDefinition dangling references (Gap #3a — ~4 tests)

- [x] 2.1 Enable ZETA in `@EnumSource` for `JslModel2UiActionGroupsTest.testLinkOperations`, `testTableOperations`, `JslModel2UiRowOperationsTest.testAccessTableRowOperations`, `JslModel2UiOperationsTest.testOperationsOnViewsWithInputForms` — verify they fail
- [x] 2.2 Find Zeta rules that create `ParameterlessCallOperationActionDefinition` objects and add `ctx.addToResource()` calls for each
- [x] 2.3 Verify the affected tests pass in both ETL and ZETA modes

## 3. Fix Action.actionDefinition missing for filter/autocomplete/tags (Gap #3b — 2 tests)

- [x] 3.1 Enable ZETA in `@EnumSource` for `JSLModel2UiListItemTest.testCards`, `testTags` — verify they fail
- [x] 3.2 Trace ETL rules for `ViewTableDeclarationFilterAction`, `ViewTableTagsDeclarationAutocompleteRangeAction`, `ViewTableTagsDeclarationAutocompleteAddAction` to identify what ActionDefinitions they create
- [x] 3.3 Add corresponding Zeta rules that create and register ActionDefinition objects for filter and autocomplete/tags actions
- [x] 3.4 Verify the 2 tests pass in both ETL and ZETA modes

## 4. Fix Table.columns empty (Gap #1 — ~14 tests)

- [x] 4.1 Enable ZETA in `@EnumSource` for all remaining ETL-only tests in `JslModel2UiCRUDTest` (6 tests), `JslModel2UiWidgetsTest.testRelationWidgets`, `JslModel2UiDataTest.testRelations`, `JslModel2UiNavigationTest.testNavigation` — verify they fail
- [x] 4.2 Trace ETL row/column rules (`rowDeclaration.eol`, `columnDeclaration.eol`, related table EOL files) to understand how columns are created inside Tables
- [x] 4.3 Implement Zeta column creation logic in the appropriate row/table rule classes, creating Column objects from RowDeclaration column definitions and adding them to the Table's columns containment
- [x] 4.4 Verify all affected tests pass in both ETL and ZETA modes

## 5. Final verification

- [x] 5.1 Run full `judo-tatami-jsl-jsl2ui` test suite (`mvn clean test -pl judo-tatami-jsl-jsl2ui`) — all 139 tests pass (1 skipped: `@Disabled` parser syntax test)
- [x] 5.2 Confirm no remaining `names = {"ETL"}` annotations in test files — all tests run with `{"ETL", "ZETA"}`

## 6. Additional fixes from STRICT external model comparison

- [x] 6.1 Fix `getAllPrimitiveFields()` in `Jsl2UiHelper.java` — added `instanceof DataTypeDeclaration && getPrimitive() != null` check to match ETL's filter
- [x] 6.2 Fix panel traversal ordering in `getAllPrimitiveFieldsFromPanel()` — process groups before tabs to match ETL's EOL ordering
- [x] 6.3 Fix `setTargetType()` missing in 4 ViewTableDeclarationRules action definitions
- [x] 6.4 Fix `setIsContainedRelationAction(true)` missing in 6 Cards/Tags action definitions
- [x] 6.5 Fix Card/Tag `onInit` over-setting — removed `setOnInit()` that resolves to null in ETL
- [x] 6.6 Fix ViewLinkDeclaration autocomplete `ownerDataElement`/`targetDataElement` over-setting
- [x] 6.7 Fix ViewActionDeclaration `operationInputFormBackAction` `ownerDataElement` over-setting
- [x] 6.8 External STRICT comparison: 39/39 models pass (21 with known non-semantic icon.name/parts diffs filtered)
