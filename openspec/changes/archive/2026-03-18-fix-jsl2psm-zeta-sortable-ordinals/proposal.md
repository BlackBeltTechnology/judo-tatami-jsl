## Why

The Zeta JSL2PSM transformation produces incorrect output for two categories of rules: (1) QueryCustomizer rules that depend on `isSortable()`/`isFilterable()`/`hasSortableField()` guards produce wrong results because the Zeta implementation used broken Java reflection to call ETL-only EOL operations that don't exist on EMF model classes, and (2) EnumerationMember ordinals are all left at `-1` because the ETL `@post` block that assigns sequential ordinals was never ported to Zeta's `postProcess()`. Together these cause 24+ differences in STRICT dual-transformation comparison tests.

## What Changes

- **Fix isSortable/isFilterable/hasSortableField**: Port the ETL EOL logic from `transferFieldDeclaration.eol` into proper Java methods in `Jsl2PsmHelper`. Remove the broken reflective fallback methods from `QueryCustomizerRules`. This fixes guards on `CreateQueryOrderingType`, `CreateQueryCustomizerOrderingEnumeration`, `CreateQueryCustomizerOrderingEnumerationMember`, `CreateQueryCustomizerSeekLastItemAttribute`, and related rules.
- **Fix EnumerationMember ordinal assignment**: Add `setEnumerationOrdinals()` post-processing to `Jsl2PsmZetaTransformation.postProcess()` that mirrors the ETL `@post` block — finds all EnumerationMembers with `ordinal == -1` and assigns sequential ordinals within each EnumerationType.

## Capabilities

### New Capabilities

- `zeta-sortable-filterable`: Proper isSortable/isFilterable/hasSortableField logic for Zeta JSL2PSM transformation guards
- `zeta-enumeration-ordinals`: Post-processing step to assign sequential ordinals to EnumerationMembers in Zeta transformation

### Modified Capabilities

- `zeta-jsl2psm`: QueryCustomizer rules now use correct sortable/filterable guards; postProcess includes ordinal assignment

## Impact

- `Jsl2PsmHelper.java` — new static methods: `isSortable()`, `isFilterable()`, `hasSortableField()`, `isSortablePrimitive()`, `getPrimitiveKind()`
- `QueryCustomizerRules.java` — guard methods updated to use `Jsl2PsmHelper` instead of broken reflection; private helper methods removed
- `Jsl2PsmZetaTransformation.java` — `postProcess()` extended with `setEnumerationOrdinals()`
- All 37 dual transformation tests pass after these fixes
