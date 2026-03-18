## ADDED Requirements

### Requirement: Zeta generates form pages for menu table declarations
The system SHALL provide a Zeta transformation rule class `MenuTableDeclarationFormPageRules` that transforms `UIMenuTableDeclaration` with form modifiers into `PageDefinition` elements for create forms, producing output equivalent to the ETL `menuTableDeclarationFormPage.etl` module.

#### Scenario: Menu table with create form modifier generates form page definition
- **WHEN** a `UIMenuTableDeclaration` has a `createFormModifier` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page name SHALL use `getFqName()` producing `<TransferFqName>::AccessFormPage`
- **AND** the page SHALL reference the form's `FormPageContainer`
- **AND** the page SHALL include Back, Create, and GetTemplate actions
- **AND** for each nested link in the form, the page SHALL include OpenPage, OpenSetSelectorDialog, and Unset actions
- **AND** for each nested table in the form, the page SHALL include OpenPage, Filter, OpenAddSelector, Clear, and BulkRemove actions

#### Scenario: Menu table without create form modifier does not generate form page
- **WHEN** a `UIMenuTableDeclaration` has no `createFormModifier` defined
- **THEN** the system SHALL NOT create a form `PageDefinition`

### Requirement: Zeta generates form pages for menu link declarations
The system SHALL provide a Zeta transformation rule class `MenuLinkDeclarationFormPageRules` that transforms `UIMenuLinkDeclaration` with form modifiers into `PageDefinition` elements, producing output equivalent to the ETL `menuLinkDeclarationFormPage.etl` module.

#### Scenario: Menu link with create form modifier generates form page definition
- **WHEN** a `UIMenuLinkDeclaration` has a `createFormModifier` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page SHALL reference the form's `FormPageContainer`
- **AND** the page SHALL include Back, Create, and GetTemplate actions
- **AND** the page data element SHALL use `RelationType` with `MemberType#ACCESS`

### Requirement: Zeta generates form pages for view table declarations
The system SHALL provide a Zeta transformation rule class `ViewTableDeclarationFormPageRules` that transforms `UIViewTableDeclaration` with form modifiers into `PageDefinition` elements, producing output equivalent to the ETL `viewTableDeclarationFormPage.etl` module.

#### Scenario: View table with create form modifier generates form page definition
- **WHEN** a `UIViewTableDeclaration` has a `createFormModifier` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page SHALL include OpenPage, Filter, Clear, and BulkRemove actions for nested tables
- **AND** the page SHALL include OpenPage, OpenSetSelectorDialog, and Unset actions for nested links

### Requirement: Zeta generates form pages for view link declarations
The system SHALL provide a Zeta transformation rule class `ViewLinkDeclarationFormPageRules` that transforms `UIViewLinkDeclaration` with form modifiers into `PageDefinition` elements, producing output equivalent to the ETL `viewLinkDeclarationFormPage.etl` module.

#### Scenario: View link with create form modifier generates form page definition
- **WHEN** a `UIViewLinkDeclaration` has a `createFormModifier` defined
- **THEN** the system SHALL create a `PageDefinition` with `openInDialog=true`
- **AND** the page SHALL include Back, Create, and GetTemplate actions
- **AND** the page SHALL include SetRelation actions for each nested relation

### Requirement: All form page rules use fully qualified names
All form page rule classes SHALL use `getFqName(source)` for element names to match ETL naming conventions, ensuring RelationType FQ names and PageDefinition names are consistent with ETL output.

#### Scenario: Form page names use FQ naming
- **WHEN** a form page `PageDefinition` is created
- **THEN** the name SHALL use `getFqName(source)` producing fully qualified names like `Model::Entity::relation::AccessFormPage`
