## Context

The Zeta JSL2PSM transformation has two bugs causing STRICT dual-test failures against ETL output:

1. **QueryCustomizer guard logic**: The `isSortable()`, `isFilterable()`, and `hasSortableField()` methods in `QueryCustomizerRules` use Java reflection (`field.getClass().getMethod("isSortable")`) to call methods that only exist as ETL EOL operations, not as Java methods on EMF model classes. The fallback checks for a `"sortable"` modifier which also doesn't exist.

2. **Enumeration ordinal assignment**: ETL's `jslToPsm.etl` has a `@post` block that finds all `EnumerationMember` instances with `ordinal == -1` and assigns sequential ordinals. Zeta's `postProcess()` only calls `setAssociationPartners()` — the ordinal step was never ported.

## Goals / Non-Goals

**Goals:**
- Port ETL's `isSortable()`/`isFilterable()` logic from `transferFieldDeclaration.eol` to Java in `Jsl2PsmHelper`
- Add enumeration ordinal post-processing to `Jsl2PsmZetaTransformation`
- Achieve zero differences in all 37 STRICT dual transformation tests

**Non-Goals:**
- Changing ETL transformation logic
- Adding new dual test cases (existing coverage is sufficient)
- Fixing XMI ID collisions (cosmetic, matches ETL behavior)

## Decisions

1. **Place sortable/filterable logic in `Jsl2PsmHelper`** (not in `QueryCustomizerRules`): These are general-purpose predicates that may be needed by other rule classes. Follows the existing pattern where helper methods live in `Jsl2PsmHelper`.

2. **Port ETL logic directly**: The `isSortable()` check is: field has a `DataTypeDeclaration` reference type whose primitive is one of `string`, `numeric`, `date`, `timestamp`, `time`, `boolean`, AND the field is mapped (`<=>`) or derived/reads (`<=`). Same logic for `isFilterable()`.

3. **Ordinal assignment in `postProcess()`**: Scan all `EnumerationMember` instances in the PSM resource set, group by parent `EnumerationType`, assign ordinals 0, 1, 2, ... in member order. Same approach as the ETL `@post` block.

## Risks / Trade-offs

- **Tight coupling to ETL semantics**: The sortable primitive list (`string`, `numeric`, `date`, `timestamp`, `time`, `boolean`) is hardcoded to match ETL. If ETL changes, Zeta must be updated manually. This is acceptable since both engines are maintained in the same repository.
- **Post-processing order**: `setEnumerationOrdinals()` runs after `setAssociationPartners()`. Order doesn't matter since they operate on disjoint element sets.
