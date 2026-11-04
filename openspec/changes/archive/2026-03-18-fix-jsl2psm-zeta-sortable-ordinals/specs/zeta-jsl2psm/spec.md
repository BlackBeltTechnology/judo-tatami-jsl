## MODIFIED Requirements

### Requirement: EOL operations ported as static helper methods
All 158 EOL operations from jsl2psm SHALL be ported to static Java helper methods in extension/helper classes. These methods SHALL produce identical results to their EOL counterparts. This includes `isSortable()`, `isFilterable()`, and `hasSortableField()` from `transferFieldDeclaration.eol`, which SHALL be implemented in `Jsl2PsmHelper` using the same primitive-kind and binding checks as the ETL originals.

#### Scenario: JSL expression to JQL conversion works in Zeta
- **WHEN** a JSL expression is encountered during Zeta transformation
- **THEN** the `JslExpressionToJqlExpression` converter SHALL produce the same JQL output as in ETL mode

#### Scenario: isSortable correctly identifies sortable fields
- **WHEN** `Jsl2PsmHelper.isSortable()` is called on a TransferFieldDeclaration
- **THEN** it SHALL return the same result as the ETL `isSortable()` EOL operation

### Requirement: Zeta JSL2PSM transformation produces equivalent PSM output
The system SHALL provide a Java-based Zeta transformation engine (`Jsl2PsmZetaTransformation`) that transforms JSL models to PSM models, producing output structurally equivalent to the existing ETL transformation. The `postProcess()` method SHALL include enumeration ordinal assignment matching the ETL `@post` block.

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

#### Scenario: Post-processing assigns enumeration ordinals
- **WHEN** a Zeta transformation produces EnumerationMembers with ordinal -1
- **THEN** postProcess() SHALL assign sequential ordinals starting from 0, matching ETL @post behavior
