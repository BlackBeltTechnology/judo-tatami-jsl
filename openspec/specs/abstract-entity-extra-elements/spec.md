## ADDED Requirements

### Requirement: No extra elements for abstract entity models
The Zeta transformation SHALL NOT produce extra PSM elements compared to ETL when transforming models containing abstract entities with self-referencing or cross-referencing derived relations.

#### Scenario: itest-AbstractModel produces no DIFF
- **WHEN** the JSL2PSM Zeta transformation runs on the `itest-AbstractModel` model with STRICT comparison against ETL
- **THEN** the models SHALL be EQUIVALENT with zero missing and zero extra elements

#### Scenario: No XMI ID collisions for abstract entity default transfer objects
- **WHEN** the JSL2PSM Zeta transformation runs on a model with abstract entities that have derived relations referencing concrete subtypes
- **THEN** no `XMI ID COLLISION DETECTED` errors SHALL appear in the log output for `CreateEntityDefaultTransferObjectType` elements
