## MODIFIED Requirements

### Requirement: Zeta JSL2UI transformation produces equivalent UI output
The system SHALL provide a Java-based Zeta transformation engine (`Jsl2UiZetaTransformation`) that transforms JSL models to UI models, producing output structurally equivalent to the existing ETL transformation. All element names (including RelationType FQ names, NavigationItem names, and PageDefinition names) SHALL use fully qualified naming via `getFqName()` matching ETL conventions.

#### Scenario: Application rules produce correct actor and authentication elements
- **WHEN** a JSL model with actor declarations and actor groups is transformed using Zeta
- **THEN** the UI output SHALL contain Application, ClassType, and authentication elements matching ETL output

#### Scenario: Structure rules produce correct transfer object types
- **WHEN** a JSL model with transfer declarations is transformed using Zeta
- **THEN** the UI output SHALL contain ClassType, RelationType, and DataType elements matching ETL output

#### Scenario: RelationType uses fully qualified names
- **WHEN** a JSL model with transfer relation declarations is transformed using Zeta
- **THEN** each RelationType name SHALL use `getFqName(source)` producing FQ names like `Actor::Model::Actor::relationName`

#### Scenario: Type rules produce correct data types and operations
- **WHEN** a JSL model with primitive types and type operations is transformed using Zeta
- **THEN** the UI output SHALL contain DataType elements with correct operations matching ETL output

#### Scenario: View rules produce correct pages and containers
- **WHEN** a JSL model with view declarations, tables, links, and widgets is transformed using Zeta
- **THEN** the UI output SHALL contain PageContainer, PageDefinition, and widget elements matching ETL output

#### Scenario: Menu rules produce correct navigation items
- **WHEN** a JSL model with menu declarations is transformed using Zeta
- **THEN** the UI output SHALL contain NavigationItem elements with correct routing matching ETL output
