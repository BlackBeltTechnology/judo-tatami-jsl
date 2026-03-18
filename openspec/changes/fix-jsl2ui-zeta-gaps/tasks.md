## 1. Fix Claim.attributeType (Gap #2 — 3 tests)

- [ ] 1.1 Enable ZETA in `@EnumSource` for `JslModel2UiCarTest.testCar`, `JslModel2UiApplicationTest.testMenu`, `JslModel2UiDataTest.testBasicData` — verify they fail
- [ ] 1.2 In `ModifiableRules.java:claimModifier()`, after creating the Claim, resolve the identity's transfer field via `getIdentity(actorDeclaration).getField()` and call `Jsl2UiHelper.getTransferFieldAttributeType(field, ctx)` to set `target.setAttributeType(...)`
- [ ] 1.3 Verify the 3 tests pass in both ETL and ZETA modes

## 2. Fix Action.actionDefinition dangling references (Gap #3a — ~4 tests)

- [ ] 2.1 Enable ZETA in `@EnumSource` for `JslModel2UiActionGroupsTest.testLinkOperations`, `testTableOperations`, `JslModel2UiRowOperationsTest.testAccessTableRowOperations`, `JslModel2UiOperationsTest.testOperationsOnViewsWithInputForms` — verify they fail
- [ ] 2.2 Find Zeta rules that create `ParameterlessCallOperationActionDefinition` objects and add `ctx.addToResource()` calls for each
- [ ] 2.3 Verify the affected tests pass in both ETL and ZETA modes

## 3. Fix Action.actionDefinition missing for filter/autocomplete/tags (Gap #3b — 2 tests)

- [ ] 3.1 Enable ZETA in `@EnumSource` for `JSLModel2UiListItemTest.testCards`, `testTags` — verify they fail
- [ ] 3.2 Trace ETL rules for `ViewTableDeclarationFilterAction`, `ViewTableTagsDeclarationAutocompleteRangeAction`, `ViewTableTagsDeclarationAutocompleteAddAction` to identify what ActionDefinitions they create
- [ ] 3.3 Add corresponding Zeta rules that create and register ActionDefinition objects for filter and autocomplete/tags actions
- [ ] 3.4 Verify the 2 tests pass in both ETL and ZETA modes

## 4. Fix Table.columns empty (Gap #1 — ~14 tests)

- [ ] 4.1 Enable ZETA in `@EnumSource` for all remaining ETL-only tests in `JslModel2UiCRUDTest` (6 tests), `JslModel2UiWidgetsTest.testRelationWidgets`, `JslModel2UiDataTest.testRelations`, `JslModel2UiNavigationTest.testNavigation` — verify they fail
- [ ] 4.2 Trace ETL row/column rules (`rowDeclaration.eol`, `columnDeclaration.eol`, related table EOL files) to understand how columns are created inside Tables
- [ ] 4.3 Implement Zeta column creation logic in the appropriate row/table rule classes, creating Column objects from RowDeclaration column definitions and adding them to the Table's columns containment
- [ ] 4.4 Verify all affected tests pass in both ETL and ZETA modes

## 5. Final verification

- [ ] 5.1 Run full `judo-tatami-jsl-jsl2ui` test suite (`mvn clean test -pl judo-tatami-jsl-jsl2ui`) — all tests must pass
- [ ] 5.2 Confirm no remaining `names = {"ETL"} // TODO: Enable ZETA` annotations in test files
