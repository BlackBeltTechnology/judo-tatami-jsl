## ADDED Requirements

### Requirement: Zeta generates add selector pages for menu table declarations
The system SHALL provide a Zeta transformation rule class `MenuTableDeclarationAddSelectorPageRules` that transforms `UIMenuTableDeclaration` with add selector modifiers into `PageDefinition` and `PageContainer` elements for add selectors, producing output equivalent to the ETL `menuTableDeclarationAddSelectorPage.etl` module.

#### Scenario: Menu table with add selector generates add selector page container
- **WHEN** a `UIMenuTableDeclaration` has a `getAddSelectorModifier()` defined
- **THEN** the system SHALL create a `PageContainer` of type `TABLE`
- **AND** the container SHALL include a visual Flex element
- **AND** the container SHALL include a `ButtonGroup` with Back, Set, and Add buttons
- **AND** the container SHALL reference the table's data element as `ClassType`

#### Scenario: Menu table with add selector generates add selector page definition
- **WHEN** a `UIMenuTableDeclaration` has a `getAddSelectorModifier()` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page name SHALL use `getFqName()` producing `<TransferFqName>::AddSelectorPage`
- **AND** the page SHALL include Back and Set actions
- **AND** the page data element SHALL use `RelationType` with `MemberType#ACCESS`

### Requirement: Zeta generates set selector pages for view link declarations
The system SHALL provide a Zeta transformation rule class `ViewLinkDeclarationSetSelectorPageRules` that transforms `UIViewLinkDeclaration` with selector modifiers into `PageDefinition` elements for set selectors, producing output equivalent to the ETL `viewLinkDeclarationSetSelectorPage.etl` module.

#### Scenario: View link with set selector generates set selector page definition
- **WHEN** a `UIViewLinkDeclaration` has a `getSelectorTableModifier()` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page SHALL include Back, Set, and Unset actions
- **AND** the page SHALL include a table with columns from the target transfer
- **AND** the page data element SHALL use the target transfer's `ClassType`

#### Scenario: Set selector page includes filter action when supported
- **WHEN** a `UIViewLinkDeclaration` has a `getSelectorTableModifier()` defined
- **AND** the target relation supports filtering (`isFilterSupported()`)
- **THEN** the `PageDefinition` SHALL include a Filter action

### Requirement: Add selector page includes navigation actions
The system SHALL generate appropriate navigation actions for add/set selector pages, including Back button, Set button, and Add button (for add selectors) or Unset button (for set selectors).

#### Scenario: Add selector page includes Back, Set, and Add actions
- **WHEN** an add selector `PageDefinition` is created
- **THEN** the page SHALL include Back, Set, and Add action definitions
- **AND** each action SHALL have an associated button with appropriate icon and label

#### Scenario: Set selector page includes Back, Set, and Unset actions
- **WHEN** a set selector `PageDefinition` is created
- **THEN** the page SHALL include Back, Set, and Unset action definitions
- **AND** each action SHALL have an associated button with appropriate icon and label
