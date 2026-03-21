## ADDED Requirements

### Requirement: Zeta action rules register ActionDefinition objects in model resource
The Zeta transformation SHALL call `ctx.addToResource()` on all `ActionDefinition` objects it creates, ensuring they are part of the UI model resource and not dangling cross-references.

#### Scenario: ParameterlessCallOperationActionDefinition is resolvable
- **WHEN** a Zeta action rule creates a `ParameterlessCallOperationActionDefinition` and assigns it to an `Action.actionDefinition`
- **THEN** the ActionDefinition SHALL be added to the model resource via `ctx.addToResource()`
- **AND** `uiModel.isValid()` SHALL report no "contains a dangling reference" diagnostics for `actionDefinition`

#### Scenario: View operations with action groups produce valid actions
- **WHEN** a JSL model defines view operations (e.g., `event void doSomething()` on a transfer)
- **AND** the operations are surfaced via action groups in views
- **THEN** all generated `Action` objects SHALL have a valid, non-dangling `actionDefinition` reference

### Requirement: Zeta creates ActionDefinitions for filter and autocomplete actions
The Zeta transformation SHALL create `ActionDefinition` objects for filter, autocomplete-range, and autocomplete-add action types on table/tags declarations, matching the ETL behavior.

#### Scenario: Table declaration filter action has actionDefinition
- **WHEN** a JSL model includes a table declaration in a view (e.g., `table PersonRow[] persons <= p.persons`)
- **THEN** the generated `ViewTableDeclarationFilterAction` SHALL have its `actionDefinition` reference set to a valid `ActionDefinition` object

#### Scenario: Tags declaration autocomplete actions have actionDefinitions
- **WHEN** a JSL model includes a tags declaration in a view
- **THEN** the generated `ViewTableTagsDeclarationAutocompleteRangeAction` and `ViewTableTagsDeclarationAutocompleteAddAction` SHALL each have their `actionDefinition` reference set

#### Scenario: Input form operations produce valid action definitions
- **WHEN** a view operation declares an input form parameter
- **THEN** the generated action and its `actionDefinition` SHALL both be valid and resolvable within the model resource
