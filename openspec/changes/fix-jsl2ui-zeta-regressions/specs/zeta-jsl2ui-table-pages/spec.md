## ADDED Requirements

### Requirement: Zeta generates table pages for menu table declarations
The system SHALL provide a Zeta transformation rule class `MenuTableDeclarationTablePageRules` that transforms `UIMenuTableDeclaration` with table modifiers into `PageDefinition` and `PageContainer` elements for table pages, producing output equivalent to the ETL `menuTableDeclarationTablePage.etl` module.

#### Scenario: Menu table with table modifier generates table page container
- **WHEN** a `UIMenuTableDeclaration` has a `getTableModifier()` defined
- **THEN** the system SHALL create a `PageContainer` of type `TABLE`
- **AND** the container SHALL include a visual Flex element
- **AND** the container SHALL include a `ButtonGroup` with Back button
- **AND** the container SHALL include a Table element with columns
- **AND** the container SHALL reference the target transfer's data element

#### Scenario: Menu table with table modifier generates table page definition
- **WHEN** a `UIMenuTableDeclaration` has a `getTableModifier()` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page name SHALL use `getFqName()` producing `<TransferFqName>::TablePage`
- **AND** the page SHALL include a Back action
- **AND** the page SHALL include a Table element with columns for each field

### Requirement: Table pages include filter actions when supported
The system SHALL generate filter actions for table pages when the target relation supports filtering.

#### Scenario: Table page includes filter action when filtering is supported
- **WHEN** a table `PageDefinition` is created
- **AND** the target relation supports filtering (`isFilterSupported()`)
- **THEN** the page SHALL include a `FilterActionDefinition`
- **AND** the filter action SHALL reference the table's data element

### Requirement: Table pages include clear and bulk remove actions
The system SHALL generate clear and bulk remove actions for table pages to allow users to clear selections and remove multiple items.

#### Scenario: Table page includes clear action
- **WHEN** a table `PageDefinition` is created for a relation
- **THEN** the page SHALL include a `ClearActionDefinition`
- **AND** the clear action SHALL be marked as contained relation action

#### Scenario: Table page includes bulk remove action
- **WHEN** a table `PageDefinition` is created for a relation
- **THEN** the page SHALL include a `BulkRemoveActionDefinition`
- **AND** the bulk remove action SHALL be marked as contained relation action
- **AND** the bulk remove action SHALL be marked as bulk operation

### Requirement: Table page includes open page action for update view modifier
The system SHALL generate open page actions for tables with update view modifiers, allowing navigation to detail views.

#### Scenario: Table page with update view includes open page action
- **WHEN** a `UIViewTableDeclaration` has an `updateViewModifier()` defined
- **THEN** the table `PageDefinition` SHALL include an `OpenPageActionDefinition`
- **AND** the open page action SHALL reference the target view page

### Requirement: All table page actions include proper buttons and icons
The system SHALL generate appropriate buttons and icons for all table page actions, maintaining consistency with ETL output.

#### Scenario: Table page actions have associated buttons
- **WHEN** a table page action definition is created (Filter, Clear, BulkRemove, OpenPage)
- **THEN** the action SHALL have an associated button
- **AND** the button SHALL have an appropriate icon
- **AND** the button SHALL have a label matching the action type
