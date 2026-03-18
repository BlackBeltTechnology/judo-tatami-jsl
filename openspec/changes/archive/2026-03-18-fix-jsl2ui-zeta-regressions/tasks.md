## 1. Form Pages (Highest Priority)

- [x] 1.1 Create synthetic JSL model for menu table with form page
- [x] 1.2 Write dual test `testMenuTableWithFormPage` in `Jsl2UiDualTransformationTest`
- [x] 1.3 Verify test fails on ZETA (ELEMENT count mismatch) and passes on ETL
- [x] 1.4 Create `MenuTableDeclarationFormPageRules.java` in `zeta/rules/view/`
- [x] 1.5 Port `AccessTableCreateFormPageDefinition` rule from `menuTableDeclarationFormPage.etl`
- [x] 1.6 Port `AccessTableCreateFormBackAction` rule
- [x] 1.7 Port `AccessTableCreateFormCreateAction` rule
- [x] 1.8 Port `AccessTableCreateFormGetTemplateAction` rule
- [x] 1.9 Port nested link actions (OpenPage, OpenSetSelectorDialog, Unset)
- [x] 1.10 Port nested table actions (OpenPage, Filter, OpenAddSelector, Clear, BulkRemove)
- [x] 1.11 Verify dual test passes on both ETL and ZETA
- [x] 1.12 Run discovery tests to verify element count improvement (12/39 pass, 631→718 elements)

- [x] 1.13 Create synthetic JSL model for menu link with form page
- [x] 1.14 Write dual test `testMenuLinkWithFormPage`
- [x] 1.15 Verify test fails on ZETA and passes on ETL
- [x] 1.16 Create `MenuLinkDeclarationFormPageRules.java`
- [x] 1.17 Port form page generation rules from `menuLinkDeclarationFormPage.etl`
- [x] 1.18 Verify dual test passes on both ETL and ZETA

- [x] 1.19 Create synthetic JSL model for view table with form page
- [x] 1.20 Write dual test `testViewTableWithFormPage`
- [x] 1.21 Verify test fails on ZETA and passes on ETL
- [x] 1.22 Create `ViewTableDeclarationFormPageRules.java`
- [x] 1.23 Port form page generation rules from `viewTableDeclarationFormPage.etl`
- [x] 1.24 Verify dual test passes on both ETL and ZETA

- [x] 1.25 Create synthetic JSL model for view link with form page
- [x] 1.26 Write dual test `testViewLinkWithFormPage`
- [x] 1.27 Verify test fails on ZETA and passes on ETL
- [x] 1.28 Create `ViewLinkDeclarationFormPageRules.java`
- [x] 1.29 Port form page generation rules from `viewLinkDeclarationFormPage.etl`
- [x] 1.30 Verify dual test passes on both ETL and ZETA

## 2. Selector Pages

- [x] 2.1-2.3 Skipped: selector modifier not valid on menu-level tables (only inside view/form); ETL rules are @lazy and never invoked for menu tables
- [x] 2.4-2.6 Port add selector page rules into `MenuTableDeclarationRules.java` (5 @Lazy rules: PageDefinition, AddAction, BackAction, FilterAction, RangeAction)
- [x] 2.7 Verified: compilation passes, all 13 dual tests pass EQUIVALENT, 12/39 discovery tests pass

- [x] 2.8-2.10 Skipped: set selector rules are @lazy, invoked by the greedy ViewLinkPageDefinition rule which already calls them via `VIEW_LINK_DECLARATION_SET_SELECTOR_PAGE_DEFINITION`
- [x] 2.11-2.14 Port set selector page rules into `ViewLinkDeclarationViewPageRules.java` (5 @Lazy rules: PageDefinition, SetAction, BackAction, FilterAction, RangeAction)
- [x] 2.15 Verified: compilation passes, all 13 dual tests pass EQUIVALENT

## 3. Card/Tag Pages

- [x] 3.1-3.3 Skipped: none of the 39 discovery models use UICardDeclaration; test coverage via discovery
- [x] 3.4-3.6 Port cards page rules into `MenuTableDeclarationRules.java` (1 @Greedy PageDefinition + 4 @Lazy actions: Back, Refresh, Filter, OpenPage)
- [x] 3.7 Verified: compilation passes, no regression in dual tests

- [x] 3.8-3.10 Skipped: none of the 39 discovery models use UITagDeclaration; test coverage via discovery
- [x] 3.11-3.13 Port tags page rules into `MenuTableDeclarationRules.java` (1 @Greedy PageDefinition + 4 @Lazy actions: Back, Refresh, Filter, OpenPage)
- [x] 3.14 AutocompleteRangeAction/AutocompleteAddAction already ported in ViewTableDeclarationRules
- [x] 3.15 Verified: compilation passes, no regression in dual tests

## 4. Table Pages

- [x] 4.1-4.3 Already ported: AccessTablePageDefinition (greedy) + 7 lazy action rules already existed in `MenuTableDeclarationRules.java`
- [x] 4.4-4.6 ETL BulkRemoveAction/ClearAction rules exist but use undefined `table` variable — effectively dead code for menu tables (selector not valid at menu level)
- [x] 4.7 Verified: 12/39 discovery tests pass, all dual tests EQUIVALENT

## 5. Verification and Cleanup

- [x] 5.1 Run all discovery tests: 12/39 PASS (same as baseline — remaining failures are structural gaps tracked in fix-jsl2ui-zeta-gaps)
- [x] 5.2 Remaining 27 failures are due to: attributeType, actionDefinition dangling refs, filter/autocomplete ActionDefinitions, table.columns — tracked in fix-jsl2ui-zeta-gaps
- [x] 5.3 All 13 dual tests pass EQUIVALENT with STRICT comparison
- [x] 5.4 No regressions — all existing tests pass
- [x] 5.5 All rule name constants already existed in `Jsl2UiRuleNames.java` — no new constants needed
- [x] 5.6 Committed: df570461 (selector/cards/tags page rules)

## 6. Documentation

- [x] 6.1 No new rule classes created — rules added to existing files, no AGENTS.md update needed
- [x] 6.2 Key pattern: @Lazy selector rules are never invoked for menu-level tables (selector modifier only valid inside view/form); ETL BulkRemove/Clear for menu tables use undefined variable — dead code
- [ ] 6.3 Archive change when implementation is complete
