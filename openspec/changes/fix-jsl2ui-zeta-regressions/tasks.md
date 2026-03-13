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

- [ ] 2.1 Create synthetic JSL model for menu table with add selector
- [ ] 2.2 Write dual test `testMenuTableWithAddSelector`
- [ ] 2.3 Verify test fails on ZETA and passes on ETL
- [ ] 2.4 Create `MenuTableDeclarationAddSelectorPageRules.java`
- [ ] 2.5 Port add selector page container rule from `menuTableDeclarationAddSelectorPage.etl`
- [ ] 2.6 Port add selector page definition rule with Back, Set actions
- [ ] 2.7 Verify dual test passes on both ETL and ZETA

- [ ] 2.8 Create synthetic JSL model for view link with set selector
- [ ] 2.9 Write dual test `testViewLinkWithSetSelector`
- [ ] 2.10 Verify test fails on ZETA and passes on ETL
- [ ] 2.11 Create `ViewLinkDeclarationSetSelectorPageRules.java`
- [ ] 2.12 Port set selector page rules from `viewLinkDeclarationSetSelectorPage.etl`
- [ ] 2.13 Include Back, Set, Unset actions and table with columns
- [ ] 2.14 Add filter action when relation supports filtering
- [ ] 2.15 Verify dual test passes on both ETL and ZETA

## 3. Card/Tag Pages

- [ ] 3.1 Create synthetic JSL model for menu table with cards
- [ ] 3.2 Write dual test `testMenuTableWithCards`
- [ ] 3.3 Verify test fails on ZETA and passes on ETL
- [ ] 3.4 Create `MenuTableDeclarationCardsPageRules.java`
- [ ] 3.5 Port cards page container rule from `menuTableDeclarationCardsPage.etl`
- [ ] 3.6 Port cards page definition rule with Back action
- [ ] 3.7 Verify dual test passes on both ETL and ZETA

- [ ] 3.8 Create synthetic JSL model for menu table with tags
- [ ] 3.9 Write dual test `testMenuTableWithTags`
- [ ] 3.10 Verify test fails on ZETA and passes on ETL
- [ ] 3.11 Create `MenuTableDeclarationTagsPageRules.java`
- [ ] 3.12 Port tags page container rule from `menuTableDeclarationTagsPage.etl`
- [ ] 3.13 Port tags page definition rule with Back action
- [ ] 3.14 Port AutocompleteRangeAction and AutocompleteAddAction
- [ ] 3.15 Verify dual test passes on both ETL and ZETA

## 4. Table Pages

- [ ] 4.1 Create synthetic JSL model for menu table with table page
- [ ] 4.2 Write dual test `testMenuTableWithTablePage`
- [ ] 4.3 Verify test fails on ZETA and passes on ETL
- [ ] 4.4 Create `MenuTableDeclarationTablePageRules.java`
- [ ] 4.5 Port table page container rule from `menuTableDeclarationTablePage.etl`
- [ ] 4.6 Port table page definition rule with Back action
- [ ] 4.7 Verify dual test passes on both ETL and ZETA

## 5. Verification and Cleanup

- [ ] 5.1 Run all discovery tests: `mvn test -Pperformance -pl judo-tatami-jsl-jsl2ui`
- [ ] 5.2 Verify 39/39 tests pass (or document any remaining gaps)
- [ ] 5.3 Run full test suite: `mvn clean test`
- [ ] 5.4 Ensure no regressions in existing dual tests
- [ ] 5.5 Update `Jsl2UiRuleNames.java` with all new rule constants if needed
- [ ] 5.6 Commit changes with descriptive message

## 6. Documentation

- [ ] 6.1 Update AGENTS.md or CLAUDE.md with new rule classes if applicable
- [ ] 6.2 Document any new patterns discovered during implementation
- [ ] 6.3 Archive change when implementation is complete
