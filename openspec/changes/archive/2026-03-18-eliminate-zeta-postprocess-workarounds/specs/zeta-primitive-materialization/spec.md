## ADDED Requirements

### Requirement: Primitive types from NavigationBaseDeclarationReference are materialized
When a `PrimitiveDeclaration` is referenced through a `NavigationBaseDeclarationReference` but not used as a direct field type, the Zeta transformation SHALL still create the corresponding PSM `Primitive` type by forcing the lazy type rule to execute.

#### Scenario: Type used only in JQL expression is created
- **WHEN** a `PrimitiveDeclaration` (e.g., Boolean) is only referenced in a derived expression via `NavigationBaseDeclarationReference`
- **THEN** the PSM model SHALL contain the corresponding `Primitive` type

#### Scenario: Type already used as field type is not duplicated
- **WHEN** a `PrimitiveDeclaration` is used both as a field type and in a `NavigationBaseDeclarationReference`
- **THEN** only one PSM `Primitive` type instance SHALL exist (lazy rule idempotency)
