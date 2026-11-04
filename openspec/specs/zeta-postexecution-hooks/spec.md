## ADDED Requirements

### Requirement: Association partner assignment via @PostExecution
`AssociationRules` SHALL have a `@PostExecution` method that sets `AssociationEnd.partner` references bidirectionally for all entity relations with opposites. This replaces the `setAssociationPartners()` method in `Jsl2PsmZetaTransformation.postProcess()`.

#### Scenario: Bidirectional relation partners are set
- **WHEN** an entity relation has an `opposite:` declaration
- **THEN** both `AssociationEnd` instances SHALL have their `partner` references pointing to each other after transformation

#### Scenario: Named injected opposite partners are set
- **WHEN** an entity relation has a named injected opposite (`EntityRelationOppositeInjected`)
- **THEN** the injected `AssociationEnd` and the declared `AssociationEnd` SHALL have their `partner` references pointing to each other

### Requirement: Enumeration ordinal assignment via @PostExecution
`QueryCustomizerRules` SHALL have a `@PostExecution` method that assigns sequential ordinals (starting from 0) to all `EnumerationMember` instances with `ordinal == -1`, grouped by parent `EnumerationType`. This replaces the `setEnumerationOrdinals()` method in `Jsl2PsmZetaTransformation.postProcess()`.

#### Scenario: Ordering enumeration members get ordinals
- **WHEN** QueryCustomizer ordering enumeration members are created with `ordinal == -1`
- **THEN** post-execution assigns ordinals 0, 1, 2, ... in member order

### Requirement: Cardinality ID normalization via @PostExecution
A rule class SHALL have a `@PostExecution` method that normalizes all `Cardinality` XMI IDs to the pattern `<container-id>/cardinality`, matching ETL's `@post` block behavior.

#### Scenario: All cardinality IDs are normalized
- **WHEN** transformation produces Cardinality instances with rule-specific IDs
- **THEN** post-execution overwrites all Cardinality IDs to `<eContainer().getId()>/cardinality`

#### Scenario: STRICT dual test shows matching cardinality IDs
- **WHEN** a JSL model with entity relations, field cardinalities, and transfer relation cardinalities is transformed by both ETL and Zeta
- **THEN** all Cardinality XMI IDs SHALL match in STRICT comparison

### Requirement: Primitive type materialization via @PostExecution
`TypeRules` SHALL have a `@PostExecution` method that forces lazy primitive type creation for `PrimitiveDeclaration` instances only referenced through `NavigationBaseDeclarationReference`, matching ETL's `@post` block behavior.

#### Scenario: Primitive type used only in expression is materialized
- **WHEN** a `PrimitiveDeclaration` is referenced only through a `NavigationBaseDeclarationReference` (not as a field type)
- **THEN** the corresponding PSM `Primitive` type SHALL be created

### Requirement: Empty postProcess() in Jsl2PsmZetaTransformation
After all post-processing logic is moved to `@PostExecution` hooks, `Jsl2PsmZetaTransformation.postProcess()` SHALL be empty or removed.

#### Scenario: No centralized post-processing remains
- **WHEN** all `@PostExecution` hooks are in place
- **THEN** `Jsl2PsmZetaTransformation.postProcess()` SHALL contain no logic
