## ADDED Requirements

### Requirement: Cardinality XMI IDs match ETL normalization
All `Cardinality` elements in the PSM output SHALL have XMI IDs following the pattern `<container-id>/cardinality`, where `<container-id>` is the XMI ID of the Cardinality's `eContainer()`.

#### Scenario: Entity relation cardinality ID
- **WHEN** an `AssociationEnd` has a cardinality
- **THEN** the cardinality's XMI ID SHALL be `<association-end-id>/cardinality`

#### Scenario: Transfer relation cardinality ID
- **WHEN** a `TransferObjectRelation` has a cardinality
- **THEN** the cardinality's XMI ID SHALL be `<transfer-relation-id>/cardinality`

#### Scenario: Parameter cardinality ID
- **WHEN** a `Parameter` has a cardinality
- **THEN** the cardinality's XMI ID SHALL be `<parameter-id>/cardinality`

#### Scenario: STRICT dual comparison shows zero cardinality ID differences
- **WHEN** the same JSL model is transformed by ETL and Zeta
- **THEN** all Cardinality XMI IDs SHALL be identical in STRICT comparison
