## 1. Fix default relation guard logic (itest-RelationWithDefaults)

- [x] 1.1 Fix `isDefaultForMappedTransferRelation` and `isDefaultForUnmappedTransferRelation` in `ContainmentRules.java` to check `transferDecl.getMap() != null` (the containing transfer's map), matching ETL semantics
- [x] 1.2 Fix `isDefaultForMappedTransferRelation` and `isDefaultForUnmappedTransferRelation` in `TransferRelationRules.java` to check `transferDecl.getMap() != null` (the containing transfer's map), matching ETL semantics
- [x] 1.3 Run dual tests to verify no regression

## 2. Fix abstract entity extra elements (itest-AbstractModel)

- [x] 2.1 Investigate and identify the 7 extra elements produced by Zeta for `itest-AbstractModel` — caused by XMI ID collisions from recursive `ctx.equivalent()` calls in `cloneDerivedRelation()` when derived relation target type equals the entity whose default TO is being created
- [x] 2.2 Fix by adding `currentEntity`/`currentTO` parameters to `cloneDerivedRelation()` in `Jsl2PsmHelper.java` to avoid recursive creation, and pass them from `DefaultTransferObjectTypeRules`
- [x] 2.3 Run dual tests and performance tests — verified 50/50 STRICT EQUIVALENT (all models pass)

## 3. Add missing `CreateDefaultValueAnnotationForEntityRelationDeclaration` rule

- [x] 3.1 Add the missing `@Greedy` rule to `AssociationRules.java` that creates "DefaultValue" `Annotation` for entity relation defaults (was explicitly omitted as "for now" comment)
- [x] 3.2 Add guard method `isDefaultForEntityRelation` to check DefaultModifier on non-calculated EntityRelationDeclaration with entity reference type
