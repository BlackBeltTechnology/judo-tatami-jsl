## Context

The JSL2UI Zeta transformation was implemented to provide a Java-based alternative to the ETL transformation. While basic functionality (applications, actors, simple views/rows) works correctly—all 9 simple dual tests pass—complex scenarios involving nested forms, selectors, and various page types have significant gaps.

Current state:
- **Dual tests (simple models)**: 9/9 pass—basic transformation is solid
- **Discovery tests (real models)**: 28/39 fail—missing ~141 elements per complex model
- **Root cause**: 10 ETL page type modules have no Zeta port

The JSL2UI transformation pipeline:
```
JSL (UIFrontendDeclaration, UIViewDeclaration, UIRowDeclaration, UITagDeclaration)
  ├→ ETL: 28 ETL modules in `src/main/epsilon/transformations/ui/modules/view/`
  └→ Zeta: 17 rule classes in `src/main/java/.../zeta/rules/view/`
```

Missing Zeta rule classes (10):
- `MenuTableDeclarationFormPageRules` - `menuTableDeclarationFormPage.etl`
- `MenuTableDeclarationAddSelectorPageRules` - `menuTableDeclarationAddSelectorPage.etl`
- `MenuTableDeclarationCardsPageRules` - `menuTableDeclarationCardsPage.etl`
- `MenuTableDeclarationTagsPageRules` - `menuTableDeclarationTagsPage.etl`
- `MenuTableDeclarationTablePageRules` - `menuTableDeclarationTablePage.etl`
- `MenuLinkDeclarationFormPageRules` - `menuLinkDeclarationFormPage.etl`
- `MenuLinkDeclarationViewPageRules` - `menuLinkDeclarationViewPage.etl`
- `ViewLinkDeclarationFormPageRules` - `viewLinkDeclarationFormPage.etl`
- `ViewLinkDeclarationSetSelectorPageRules` - `viewLinkDeclarationSetSelectorPage.etl`
- `ViewTableDeclarationFormPageRules` - `viewTableDeclarationFormPage.etl`

## Goals / Non-Goals

**Goals:**
- Achieve 39/39 discovery test pass rate by implementing missing page type rules
- Use TDD: create synthetic dual tests for each complex scenario before implementation
- Maintain compatibility with recent Zeta framework changes (guard caching, execution order)
- Ensure all rules use `getFqName()` for consistent naming with ETL

**Non-Goals:**
- Modifying ETL behavior—ETL is the source of truth
- Changing Zeta framework—only ensure compatibility
- Refactoring existing Zeta rules—focus is additive implementation

## Decisions

### 1. TDD Approach: Synthetic Tests First

**Decision:** Create minimal synthetic JSL models that reproduce each missing page type scenario, write dual tests that fail, then implement rules to make them pass.

**Rationale:**
- Complex discovery models (like SummaryCRUD with 772 elements) are too hard to debug
- Synthetic models isolate specific features (e.g., "menu table with form only")
- Failing dual tests provide clear feedback when implementation is correct
- Matches the pattern used successfully in JSL2PSM dual tests

**Alternative considered:** Debug against discovery test output directly. Rejected because difference files are too large (50+ differences) and hard to pinpoint root cause.

### 2. ETL-First Porting Strategy

**Decision:** Port each ETL module by reading the ETL file, extracting all rules, and converting to Zeta rule methods following the established pattern in `ViewDeclarationRules.java`, `RowDeclarationRules.java`, etc.

**Rationale:**
- ETL modules are the source of truth for transformation logic
- Established Zeta patterns exist (e.g., `@TransformRule`, `@Lazy`, `@Greedy` usage)
- Direct porting minimizes divergence risk

**Alternative considered:** Re-implement from UI metamodel. Rejected because ETL has business logic encoded that's not obvious from metamodel alone.

### 3. Rule Naming Convention

**Decision:** All new rule classes use pattern `<SourceType><PageType>Rules` (e.g., `MenuTableDeclarationFormPageRules`) and rule names follow `<ELEMENT_TYPE><PAGE_TYPE>` pattern (e.g., `FORM_PAGE_CONTAINER`, `ACCESS_TABLE_CREATE_FORM_BACK_ACTION`).

**Rationale:**
- Consistent with existing Zeta rules (`RowDeclarationRules`, `ViewDeclarationRules`)
- Matches ETL rule naming where possible
- Clear and self-documenting

### 4. Guard Logic for Conditional Page Generation

**Decision:** Page rules only generate output when the corresponding modifier is defined. Use guard checks like `source.getCreateFormModifier().isDefined()` to return `null` when not applicable.

**Rationale:**
- ETL uses guard clauses for conditional rule execution
- Matches Zeta's lazy evaluation model
- Prevents creating empty/invalid page elements

**Alternative considered:** Always create rules and filter downstream. Rejected because it creates unnecessary elements that fail comparison.

### 5. Action Generation Pattern

**Decision:** For complex pages with multiple action types (Back, Create, Filter, etc.), create separate lazy rules for each action and action definition, then compose them in the main page rule.

**Rationale:**
- Keeps rules focused and testable
- Matches existing Zeta pattern in `RowDeclarationRules.java`
- Allows reuse of action definitions across multiple pages

## Risks / Trade-offs

### [Risk] Recent Zeta framework changes may cause rule execution issues

**Context:** Zeta had significant changes in March 2025:
- Guard rejection caching (commit 0a9fe21)
- Rule-by-rule execution strategy (commit 1170d7a)
- XMI ID assignment fixes (commits 8bff98e, 94a2410)

**Mitigation:**
- Test each new rule class in isolation with synthetic models
- Verify guard logic returns `null` appropriately for cached rejections
- Use `ctx.equivalent()` with explicit rule names to avoid lookup issues
- Monitor Zeta framework for breaking changes during implementation

### [Risk] Complex nested action generation may have ordering dependencies

**Context:** Form pages include actions for nested links and tables, which may not exist yet when the parent rule executes.

**Mitigation:**
- Use `@Lazy` annotation for all action rules
- Reference actions via `ctx.equivalent()` with rule names, not direct references
- Ensure action rules have no dependencies on each other (only on parent source)

### [Trade-off] Test execution time increases with synthetic tests

**Impact:** Adding ~10-15 new dual tests will increase test time by ~5-10 seconds.

**Acceptance:** Tests run in parallel where possible, and Zeta is much faster than ETL (30x speedup observed). The debugging time saved is worth the small execution cost.

### [Risk] Discovery test differences may be due to non-page issues

**Context:** Some discovery failures may be caused by guard rejection caching or XMI ID issues, not missing rules.

**Mitigation:**
- Run discovery tests after each rule class implementation to track progress
- If element count matches but values differ, investigate EAnnotation/XMI ID issues
- Keep STRICT comparison mode but be prepared to use STRUCTURAL if EAnnotations cause noise

## Implementation Order

1. **Form pages** (highest impact, ~141 missing elements in CRUD models)
   - `MenuTableDeclarationFormPageRules`
   - `MenuLinkDeclarationFormPageRules`
   - `ViewTableDeclarationFormPageRules`
   - `ViewLinkDeclarationFormPageRules`

2. **Selector pages** (medium impact)
   - `MenuTableDeclarationAddSelectorPageRules`
   - `ViewLinkDeclarationSetSelectorPageRules`

3. **Card/Tag pages** (medium impact)
   - `MenuTableDeclarationCardsPageRules`
   - `MenuTableDeclarationTagsPageRules`

4. **Table pages** (lower impact, view-only scenarios)
   - `MenuTableDeclarationTablePageRules`

Each step:
1. Create synthetic JSL model in test resources
2. Write dual test that reproduces the scenario
3. Verify test fails on ZETA but passes on ETL
4. Implement Zeta rule class
5. Verify test passes on both ETL and ZETA
6. Run affected discovery tests to confirm improvement

## Open Questions

1. **Should we update `Jsl2UiRuleNames.java` with all new rule constants?**
   - Yes, follow existing pattern of declaring rule name constants

2. **Should we add new rule classes to `Jsl2UiZetaTransformation` builder?**
   - Yes, they will be auto-discovered via `@TransformRule` annotation

3. **How do we handle `ViewLinkDeclarationViewPageRules` which already exists?**
   - Investigate if existing implementation is complete or partial
   - Extend or replace as needed

4. **Should we parameterize existing dual tests to run with ETL+ZETA?**
   - Only for simple tests that currently pass
   - Keep complex model-specific tests separate until gaps are filled
