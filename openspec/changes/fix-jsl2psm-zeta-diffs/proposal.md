## Why

The JSL2PSM Zeta transformation produces DIFFs (non-equivalent output vs ETL) for 2 out of 51 itest models: `itest-AbstractModel` (7 extra elements) and `itest-RelationWithDefaults` (8 missing elements). Fixing these brings the Zeta engine to full parity with ETL for all discovered models.

## What Changes

- Fix guard logic in `ContainmentRules.java` and `TransferRelationRules.java` for default-value rules: check whether the **relation** is mapped (`isMaps(rel)`) instead of whether the **transfer** is mapped (`transferDecl.getMap() != null`), matching ETL's `s.maps()` semantics
- Fix abstract entity handling in `DefaultTransferObjectTypeRules.java` to avoid producing extra elements for abstract entities that ETL skips

## Capabilities

### New Capabilities
- `default-relation-guard-fix`: Correct guard logic for default-value transfer relation and containment rules so unmapped transfers with mapped relations produce the expected NavigationProperty/StaticNavigation elements
- `abstract-entity-extra-elements`: Eliminate extra elements produced by Zeta for abstract entity default transfer object types

### Modified Capabilities

## Impact

- `ContainmentRules.java` — guard methods `isDefaultForMappedTransferRelation`, `isDefaultForUnmappedTransferRelation`
- `TransferRelationRules.java` — same guard methods
- `DefaultTransferObjectTypeRules.java` — abstract entity handling
- Performance test results: 49/51 → 51/51 STRICT EQUIVALENT
