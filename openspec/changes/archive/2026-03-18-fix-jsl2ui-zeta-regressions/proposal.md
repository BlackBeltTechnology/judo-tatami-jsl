## Why

The JSL2UI Zeta transformation has significant gaps in complex scenarios: 28/39 discovery test models fail with missing elements (up to 141 fewer per model), while all 9 simple dual tests pass. This indicates missing page type rules for form pages, selector pages, card/tag pages, and table pages that are only exercised by complex real-world models.

## What Changes

- Implement 10 missing Zeta page type rule classes that have no ETL→Zeta port:
  - `MenuTableDeclarationFormPageRules` - Form page generation for menu table declarations
  - `MenuTableDeclarationAddSelectorPageRules` - Add selector page generation
  - `MenuTableDeclarationCardsPageRules` - Card page generation
  - `MenuTableDeclarationTagsPageRules` - Tag page generation
  - `MenuTableDeclarationTablePageRules` - Table page generation
  - `MenuLinkDeclarationFormPageRules` - Form page generation for menu link declarations
  - `MenuLinkDeclarationViewPageRules` - View page generation for menu links
  - `ViewLinkDeclarationFormPageRules` - Form page generation for view links
  - `ViewLinkDeclarationSetSelectorPageRules` - Set selector page generation
  - `ViewTableDeclarationFormPageRules` - Form page generation for view tables
- Create synthetic dual tests following TDD approach to reproduce each complex scenario before implementing fixes
- Ensure all page type rules use `getFqName()` for naming consistency
- Verify guard logic compatibility with recent Zeta framework changes (guard rejection caching, rule execution order)

## Capabilities

### New Capabilities
- `zeta-jsl2ui-form-pages`: Form page generation for menu table, menu link, and view table/link declarations
- `zeta-jsl2ui-selector-pages`: Add selector and set selector page generation with dialog actions
- `zeta-jsl2ui-card-tag-pages`: Card page and tag page generation with autocomplete actions
- `zeta-jsl2ui-table-pages`: Table page generation with filter, clear, and bulk remove actions

### Modified Capabilities
- `zeta-jsl2ui`: Extend existing Zeta JSL2UI transformation requirement to cover complex page types (form, selector, card, tag, table pages) in addition to basic view/row/card/tag declarations

## Impact

- **judo-tatami-jsl-jsl2ui main**: Add 10 new rule classes in `zeta/rules/view/`
- **judo-tatami-jsl-jsl2ui tests**: Add synthetic dual tests in `src/test/java/.../dual/` for TDD verification
- **judo-zeta framework**: No changes, but must verify compatibility with recent guard caching and execution order changes
- **Test execution**: Discovery test pass rate expected to increase from 11/39 to 39/39 after implementation
