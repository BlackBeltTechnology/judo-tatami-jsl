## ADDED Requirements

### Requirement: Default relation guards check relation mapping, not transfer mapping
The guard methods `isDefaultForMappedTransferRelation` and `isDefaultForUnmappedTransferRelation` in `ContainmentRules.java` and `TransferRelationRules.java` SHALL check whether the **relation** is mapped (`isMaps(rel)`) rather than whether the **transfer** is mapped (`transferDecl.getMap() != null`), matching ETL's `s.maps()` semantics.

#### Scenario: Unmapped transfer with mapped relation has default value
- **WHEN** a `TransferRelationDeclaration` with a `DefaultModifier` is inside an unmapped transfer (`map == null`) but the relation itself uses `maps` (i.e., `isMaps(rel) == true`)
- **THEN** the `isDefaultForMappedTransferRelation` guard SHALL return `true` and a `NavigationProperty` binding SHALL be created for the default value

#### Scenario: itest-RelationWithDefaults produces no DIFF
- **WHEN** the JSL2PSM Zeta transformation runs on the `itest-RelationWithDefaults` model with STRICT comparison against ETL
- **THEN** the models SHALL be EQUIVALENT with zero missing and zero extra elements
