## ADDED Requirements

### Requirement: Zeta generates card pages for menu table declarations
The system SHALL provide a Zeta transformation rule class `MenuTableDeclarationCardsPageRules` that transforms `UIMenuTableDeclaration` with cards modifiers into `PageDefinition` and `PageContainer` elements for card pages, producing output equivalent to the ETL `menuTableDeclarationCardsPage.etl` module.

#### Scenario: Menu table with cards modifier generates cards page container
- **WHEN** a `UIMenuTableDeclaration` has a `getCardsModifier()` defined
- **THEN** the system SHALL create a `PageContainer` of type `CARDS`
- **AND** the container SHALL include a visual Flex element
- **AND** the container SHALL include a `ButtonGroup` with Back button
- **AND** the container SHALL reference the target transfer's data element

#### Scenario: Menu table with cards modifier generates cards page definition
- **WHEN** a `UIMenuTableDeclaration` has a `getCardsModifier()` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page name SHALL use `getFqName()` producing `<TransferFqName>::CardsPage`
- **AND** the page SHALL include a Back action
- **AND** the page SHALL generate card elements for each item in the collection

### Requirement: Zeta generates tag pages for menu table declarations
The system SHALL provide a Zeta transformation rule class `MenuTableDeclarationTagsPageRules` that transforms `UIMenuTableDeclaration` with tags modifiers into `PageDefinition` and `PageContainer` elements for tag pages, producing output equivalent to the ETL `menuTableDeclarationTagsPage.etl` module.

#### Scenario: Menu table with tags modifier generates tags page container
- **WHEN** a `UIMenuTableDeclaration` has a `getTagsModifier()` defined
- **THEN** the system SHALL create a `PageContainer` of type `TAGS`
- **AND** the container SHALL include a visual Flex element
- **AND** the container SHALL include a `ButtonGroup` with Back button
- **AND** the container SHALL reference the target transfer's data element

#### Scenario: Menu table with tags modifier generates tags page definition
- **WHEN** a `UIMenuTableDeclaration` has a `getTagsModifier()` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page name SHALL use `getFqName()` producing `<TransferFqName>::TagsPage`
- **AND** the page SHALL include a Back action
- **AND** the page SHALL include a Chips element for tag display
- **AND** the page SHALL include autocomplete actions for tag management

### Requirement: Tag pages include autocomplete actions
The system SHALL generate autocomplete range and add actions for tag pages, allowing users to search and add tags to entities.

#### Scenario: Tag page includes autocomplete actions
- **WHEN** a tags `PageDefinition` is created for a `UITagDeclaration`
- **THEN** the page SHALL include `AutocompleteRangeAction` for searching available tags
- **AND** the page SHALL include `AutocompleteAddAction` for adding selected tags
- **AND** each action SHALL have an associated button or trigger

### Requirement: Card and tag page containers include proper navigation
The system SHALL add generated card and tag page containers to the Application's pageContainers collection, ensuring they are accessible via navigation.

#### Scenario: Card page container added to application
- **WHEN** a cards `PageContainer` is created
- **THEN** the container SHALL be added to `Application.pageContainers`

#### Scenario: Tag page container added to application
- **WHEN** a tags `PageContainer` is created
- **THEN** the container SHALL be added to `Application.pageContainers`
