## MODIFIED Requirements

### Requirement: Zeta JSL2PSM transformation produces equivalent PSM output
The system SHALL provide a Java-based Zeta transformation engine (`Jsl2PsmZetaTransformation`) that transforms JSL models to PSM models, producing output structurally equivalent to the existing ETL transformation. All post-processing logic SHALL be implemented via `@PostExecution` hooks in the rule classes that own the related elements, not in a centralized `postProcess()` method.

#### Scenario: Namespace rules produce correct model and packages
- **WHEN** a JSL model with packages is transformed using Zeta
- **THEN** the PSM output SHALL contain a Model element and Package elements matching the ETL output

#### Scenario: Type rules produce correct primitive types
- **WHEN** a JSL model with primitive type declarations (string, numeric, boolean, date, time, timestamp, binary, enum) is transformed using Zeta
- **THEN** the PSM output SHALL contain corresponding PSM type elements with correct properties (maxLength, regExp, etc.)

#### Scenario: Data rules produce correct entities and relations
- **WHEN** a JSL model with entity declarations, associations, containments, and cardinality definitions is transformed using Zeta
- **THEN** the PSM output SHALL contain EntityType, AssociationEnd, and related elements matching ETL output

#### Scenario: Derived rules produce correct derived properties
- **WHEN** a JSL model with derived attributes, navigation properties, and entity queries is transformed using Zeta
- **THEN** the PSM output SHALL contain derived DataProperty, NavigationProperty, and BoundOperation elements matching ETL output

#### Scenario: Structure rules produce correct transfer objects
- **WHEN** a JSL model with transfer object declarations (mapped, unmapped, default) is transformed using Zeta
- **THEN** the PSM output SHALL contain TransferObjectType elements with correct attributes, relations, and query customizers matching ETL output

#### Scenario: Action rules produce correct operations and behaviours
- **WHEN** a JSL model with action declarations and CRUD behaviours is transformed using Zeta
- **THEN** the PSM output SHALL contain BoundTransferOperation and UnboundTransferOperation elements with correct parameters and behaviours matching ETL output

#### Scenario: Actor rules produce correct actor types and access
- **WHEN** a JSL model with actor declarations and access controls is transformed using Zeta
- **THEN** the PSM output SHALL contain ActorType elements with correct realm, claims, and access point references matching ETL output

#### Scenario: Post-processing uses @PostExecution hooks
- **WHEN** the Zeta transformation completes
- **THEN** all post-processing (partner assignment, ordinal assignment, cardinality ID normalization, primitive materialization) SHALL be handled by `@PostExecution` methods on rule classes, not by `Jsl2PsmZetaTransformation.postProcess()`
