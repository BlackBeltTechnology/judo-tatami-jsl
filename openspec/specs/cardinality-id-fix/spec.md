## ADDED Requirements

### Requirement: Unique Cardinality XMI IDs for transfer relations
Each `Cardinality` object created for a `TransferRelationDeclaration` SHALL have a unique XMI ID that includes the parent target element's ID as a discriminator, preventing collisions when multiple rules create Cardinality instances from the same source.

#### Scenario: Multiple rules create Cardinality from same source
- **WHEN** `TransferRelationRules` creates transient, derived, and mapped `TransferObjectRelation` objects from the same `TransferRelationDeclaration`
- **THEN** each `TransferObjectRelation`'s `Cardinality` SHALL have a distinct XMI ID

#### Scenario: No XMI ID collision errors in logs
- **WHEN** the JSL2PSM Zeta transformation runs on a model with transfer relations (e.g., `ContainerTest`)
- **THEN** no `XMI ID COLLISION DETECTED` ERROR messages SHALL appear in the log output

#### Scenario: Performance tests pass without regression
- **WHEN** the JSL2PSM discovery performance tests run with STRICT comparison
- **THEN** the number of EQUIVALENT models SHALL not decrease compared to the current baseline
