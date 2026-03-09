## ADDED Requirements

### Requirement: Zeta JSL2UI transformation produces equivalent UI output
The system SHALL provide a Java-based Zeta transformation engine (`Jsl2UiZetaTransformation`) that transforms JSL models to UI models, producing output structurally equivalent to the existing ETL transformation.

#### Scenario: Application rules produce correct actor and authentication elements
- **WHEN** a JSL model with actor declarations and actor groups is transformed using Zeta
- **THEN** the UI output SHALL contain Application, ClassType, and authentication elements matching ETL output

#### Scenario: Structure rules produce correct transfer object types
- **WHEN** a JSL model with transfer declarations is transformed using Zeta
- **THEN** the UI output SHALL contain ClassType, RelationType, and DataType elements matching ETL output

#### Scenario: Type rules produce correct data types and operations
- **WHEN** a JSL model with primitive types and type operations is transformed using Zeta
- **THEN** the UI output SHALL contain DataType elements with correct operations matching ETL output

#### Scenario: View rules produce correct pages and containers
- **WHEN** a JSL model with view declarations, tables, links, and widgets is transformed using Zeta
- **THEN** the UI output SHALL contain PageContainer, PageDefinition, and widget elements matching ETL output

#### Scenario: Menu rules produce correct navigation items
- **WHEN** a JSL model with menu declarations is transformed using Zeta
- **THEN** the UI output SHALL contain NavigationItem elements with correct routing matching ETL output

### Requirement: Zeta JSL2UI executes per-frontend
The system SHALL execute the Zeta transformation once per `UIFrontendDeclaration` in the JSL model, matching the ETL execution model. Each frontend SHALL produce independent UI elements for its associated actor.

#### Scenario: Multiple frontends produce separate UI outputs
- **WHEN** a JSL model with multiple UIFrontendDeclaration elements is transformed using Zeta
- **THEN** each frontend SHALL produce its own set of Application, pages, and navigation elements

### Requirement: Zeta JSL2UI handles position tracking via PreExecution/PostExecution
The system SHALL use `@PreExecution` to initialize position counters and `@PostExecution` to sort navigation items and container children by position, matching the ETL pre/post block behavior.

#### Scenario: Elements are sorted by position after transformation
- **WHEN** a Zeta UI transformation completes
- **THEN** navigation items and container children SHALL be sorted by their position attribute, matching ETL output ordering

### Requirement: EOL operations ported as static helper methods for jsl2ui
All 243 EOL operations from jsl2ui SHALL be ported to static Java helper methods. These methods SHALL produce identical results to their EOL counterparts.

#### Scenario: Modifiable operations work correctly in Zeta
- **WHEN** a UI element's modifiability is checked during Zeta transformation
- **THEN** the result SHALL match the EOL `modifiable.eol` operation output
