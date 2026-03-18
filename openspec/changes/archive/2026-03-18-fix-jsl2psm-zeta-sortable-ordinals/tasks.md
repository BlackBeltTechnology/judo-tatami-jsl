## 1. Fix isSortable/isFilterable/hasSortableField

- [x] 1.1 Add `isSortable(TransferFieldDeclaration)`, `isFilterable(TransferFieldDeclaration)`, `hasSortableField(TransferDeclaration)` static methods to `Jsl2PsmHelper.java`, porting logic from `transferFieldDeclaration.eol` (check primitive kind + maps/reads binding)
- [x] 1.2 Add private helper methods `isSortablePrimitive(String)` and `getPrimitiveKind(PrimitiveDeclaration)` to `Jsl2PsmHelper.java`
- [x] 1.3 Update `QueryCustomizerRules.java` guard methods (`isMappedWithBehavioursAndSortable`, `isSortableFieldWithBehaviours`, `isFilterableOrSortableFieldWithBehaviours`) to use `Jsl2PsmHelper.isSortable()` / `isFilterable()` / `hasSortableField()` via static import
- [x] 1.4 Remove broken reflective `isSortable()`, `isFilterable()`, `hasSortableField()` private methods from `QueryCustomizerRules.java`

## 2. Fix EnumerationMember ordinal assignment

- [x] 2.1 Add `setEnumerationOrdinals()` private method to `Jsl2PsmZetaTransformation.java` that scans PSM resource set for EnumerationMembers with `ordinal == -1`, groups by parent EnumerationType, assigns sequential ordinals 0, 1, 2, ...
- [x] 2.2 Call `setEnumerationOrdinals()` from `postProcess()` after `setAssociationPartners()`

## 3. Verify

- [x] 3.1 Run all 37 dual transformation tests — all must pass with zero STRICT differences
- [x] 3.2 Run full test suite (172 tests) — all must pass
