## 1. Add STRICT dual test models (TDD — write tests first, verify they fail)

- [x] 1.1 Create `CardinalityIdTestModel.jsl` test resource with entity relations, field cardinalities, and transfer relation cardinalities
- [x] 1.2 Add `strictCardinalityIds` test to `Jsl2PsmDualTransformationTest` using STRICT comparison — expect failures on cardinality XMI IDs
- [x] 1.3 Create `AssociationPartnerTestModel.jsl` test resource with bidirectional entity relations (`opposite:`) — reused existing `AssociationRelationTestModel.jsl`
- [x] 1.4 Add `strictAssociationPartners` test to `Jsl2PsmDualTransformationTest` — verify partners are set correctly
- [x] 1.5 Investigate whether a JSL model can be constructed where a `PrimitiveDeclaration` is only referenced via `NavigationBaseDeclarationReference` (not as a field type). Deferred — edge case, all practical models already covered.

## 2. Add @PostExecution hooks to rule classes

- [x] 2.1 Add `@PostExecution` method `setPartners()` to `AssociationRules.java` — move logic from `Jsl2PsmZetaTransformation.setAssociationPartners()`
- [x] 2.2 Add `@PostExecution` method `setEnumerationOrdinals()` to `QueryCustomizerRules.java` — move logic from `Jsl2PsmZetaTransformation.setEnumerationOrdinals()`
- [x] 2.3 Add `@PostExecution` method `normalizeCardinalityIds()` to `CardinalityRules.java` — iterate all Cardinality instances, set ID to `eContainer().getId() + "/cardinality"`
- [x] 2.4 Add `@PostExecution` method `materializePrimitiveTypes()` to `TypeRules.java` — iterate `NavigationBaseDeclarationReference` instances referencing `PrimitiveDeclaration`, call `ctx.equivalent()` to force lazy type creation

## 3. Clean up and verify

- [x] 3.1 Empty `postProcess()` in `Jsl2PsmZetaTransformation.java` (remove `setAssociationPartners()` and `setEnumerationOrdinals()` calls and private methods)
- [x] 3.2 Run all STRICT dual tests — verify zero differences (39/39 pass)
- [x] 3.3 Run full test suite (174 tests) — all pass
