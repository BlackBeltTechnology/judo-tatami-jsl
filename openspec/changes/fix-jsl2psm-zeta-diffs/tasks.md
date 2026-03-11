## 1. Fix default relation guard logic (itest-RelationWithDefaults)

- [ ] 1.1 Update `isDefaultForMappedTransferRelation` and `isDefaultForUnmappedTransferRelation` in `ContainmentRules.java` to use `isMaps(rel)` instead of `transferDecl.getMap() != null`
- [ ] 1.2 Update `isDefaultForMappedTransferRelation` and `isDefaultForUnmappedTransferRelation` in `TransferRelationRules.java` to use `isMaps(rel)` instead of `transferDecl.getMap() != null`
- [ ] 1.3 Run dual tests to verify no regression

## 2. Fix abstract entity extra elements (itest-AbstractModel)

- [ ] 2.1 Investigate and identify the 7 extra elements produced by Zeta for `itest-AbstractModel` using debug logging or a targeted dual test
- [ ] 2.2 Implement the fix to eliminate extra elements
- [ ] 2.3 Run dual tests and performance tests — verify 51/51 STRICT EQUIVALENT
