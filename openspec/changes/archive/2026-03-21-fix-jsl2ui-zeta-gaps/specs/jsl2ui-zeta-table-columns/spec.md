## ADDED Requirements

### Requirement: Zeta Table rules populate columns from Row declarations
The Zeta transformation SHALL create `Column` objects inside `Table` elements based on the row declaration's column definitions, matching the ETL behavior from `rowDeclaration.eol` and `columnDeclaration.eol`.

#### Scenario: Menu table with row declaration produces columns
- **WHEN** a JSL model defines a `row` declaration with `column` fields (e.g., `row CarRow(CarTransfer ct) { column String make <= ct.make; }`)
- **AND** a frontend menu references that row in a table (e.g., `table CarRow[] cars <= usr.cars`)
- **THEN** the Zeta transformation SHALL create a `Table` with at least one `Column` for each column declaration in the row

#### Scenario: View relation table with row produces columns
- **WHEN** a view contains a table relation referencing a row declaration
- **THEN** the resulting `Table` element SHALL contain `Column` objects matching the row's column declarations

#### Scenario: Table columns validation passes
- **WHEN** the Zeta transformation completes for any model containing tables with row declarations
- **THEN** `uiModel.isValid()` SHALL return `true` with no "feature 'columns' must have at least 1 values" diagnostics

#### Scenario: Multiple columns in a single row
- **WHEN** a row declaration defines multiple columns (e.g., `column String make` and `column String type`)
- **THEN** the resulting `Table` SHALL contain one `Column` per declared column, in the same order
