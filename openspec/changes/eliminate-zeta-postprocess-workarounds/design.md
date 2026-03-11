## Context

The Zeta JSL2PSM `postProcess()` method currently has two operations:
1. `setAssociationPartners()` — Zeta-specific workaround for circular bidirectional references (ETL does this inline via target pre-allocation)
2. `setEnumerationOrdinals()` — Port of ETL `@post` block

Two ETL `@post` operations are missing entirely:
1. Cardinality ID normalization — ETL overwrites all Cardinality IDs to `<container-id>/cardinality`
2. Primitive type materialization — ETL forces lazy primitive type creation for `NavigationBaseDeclarationReference` referencing `PrimitiveDeclaration`

The Zeta framework provides `@PostExecution` lifecycle hooks that run after all transformation rules complete. These are the correct Zeta mechanism for post-processing, keeping logic co-located with the rules that created the elements.

## Goals / Non-Goals

**Goals:**
- Move all post-processing from centralized `postProcess()` into `@PostExecution` hooks on rule classes
- Add missing Cardinality ID normalization to match ETL `@post`
- Add missing primitive type materialization to match ETL `@post`
- Add STRICT dual tests that reproduce each gap before fixing
- Empty `postProcess()` in `Jsl2PsmZetaTransformation.java`

**Non-Goals:**
- Implementing Zeta framework-level target pre-allocation (Approach 1) — filed as future enhancement
- Eliminating `setAssociationPartners()` logic entirely — still needed due to Zeta's caching model, just relocated
- Changing ETL transformation logic

## Decisions

1. **Use `@PostExecution` hooks (Option B)**: Each post-processing operation moves into the rule class that owns the related transformation logic. This keeps responsibility co-located and makes `postProcess()` empty.

2. **Rule class assignment**:
   - `AssociationRules.java` → `@PostExecution setPartners()` — owns association creation
   - `QueryCustomizerRules.java` → `@PostExecution setEnumerationOrdinals()` — owns ordering enumeration creation
   - `CardinalityRules.java` → `@PostExecution normalizeCardinalityIds()` — owns cardinality creation (or new class if no CardinalityRules exists)
   - `TypeRules.java` → `@PostExecution materializePrimitiveTypes()` — owns primitive type creation

3. **Cardinality ID normalization**: Iterate all `Cardinality` instances in PSM model, set each ID to `eContainer().getId() + "/cardinality"`. Must run AFTER all rules complete since container assignment happens during rule execution.

4. **Primitive materialization**: Iterate all `NavigationBaseDeclarationReference` in JSL model, for those referencing `PrimitiveDeclaration`, call `ctx.equivalent()` to force the lazy `TypeRules` to execute. `QueryParameterDeclaration` already handled by `StaticQueryRules` `@Greedy` rule.

5. **TDD approach**: Write STRICT dual test first for each gap, verify it fails, then implement the fix.

## Risks / Trade-offs

- **@PostExecution execution order**: Multiple `@PostExecution` methods across different rule classes execute in registration order. Cardinality ID normalization must run AFTER all rules that create cardinalities. This is guaranteed since `@PostExecution` runs after ALL transformation rules.
- **Association partners still post-processed**: Cannot be eliminated without Zeta framework changes (target pre-allocation). Just relocated to the owning rule class.
- **Primitive materialization edge case**: Need to verify that a test model can be constructed where a `PrimitiveDeclaration` is only referenced through `NavigationBaseDeclarationReference` and not as a field type. If no such model exists in practice, this fix is defensive.
