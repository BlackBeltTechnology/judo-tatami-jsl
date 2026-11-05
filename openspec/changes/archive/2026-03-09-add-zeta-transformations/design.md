## Context

The judo-tatami-jsl project transforms JSL (Judo Specification Language) models into PSM and UI models using Epsilon ETL. The sister project judo-tatami-base has already completed a migration to support dual-engine transformation (ETL + Zeta), providing a proven reference implementation with patterns for Work classes, parameterized tests, model comparison, and performance benchmarking.

Current state:
- **jsl2psm**: 367 ETL rules across 41 `.etl` files, 158 EOL operations across 37 `.eol` files
- **jsl2ui**: 397 ETL rules across 38 `.etl` files, 243 EOL operations across 42 `.eol` files
- **Total**: 1,165 transformation elements to port
- **No Zeta support** exists in this project today
- Shared libraries (tatami-core, tatami-util, tatami-test-utils) already provide `TransformationMode`, `ModelComparator`, and model generators

## Goals / Non-Goals

**Goals:**
- Implement Java-based Zeta transformations for jsl2psm and jsl2ui that produce output models equivalent to ETL
- Support dual-engine mode: both engines coexist, default to Zeta, overridable to ETL
- Parameterize existing test suites for both engines
- Add ModelComparator-based equivalence tests
- Add performance benchmarks comparing ETL vs Zeta
- Convert documentation from AsciiDoc to Markdown, PlantUML to Mermaid
- Document all transformation rules

**Non-Goals:**
- Removing ETL support (both engines remain operational)
- Modifying the JSL, PSM, or UI metamodels
- Changing transformation semantics (Zeta must produce identical output to ETL)
- Adding new transformation rules beyond what ETL currently implements
- Modifying upstream dependencies (tatami-core, tatami-util, judo-zeta)

## Decisions

### 1. EOL Operations → Static Helper Classes (not @ExtensionMethod)

**Decision**: Port EOL operations to plain static Java helper methods in `*Extensions.java` and `*Helper.java` classes.

**Alternatives considered**:
- `@ExtensionMethod` with `@Cached`: Matches Zeta idiom but has known issues — `@Cached` with `ctx.call()` causes `IllegalStateException: Recursive update` in `ConcurrentHashMap.computeIfAbsent`, and Zeta looks up extension methods by exact runtime type (not inheritance hierarchy). These issues are documented in judo-tatami-base.
- Hybrid (selective `@ExtensionMethod`): More complex, harder to maintain consistency.

**Rationale**: Static helpers are simpler, avoid framework bugs, and match what judo-tatami-base converged on after encountering the recursion issues.

### 2. Phased Implementation with Layered Testing

**Decision**: Implement in 8 phases, using a 4-layer test strategy:

- **Layer 1**: Existing ETL tests — always run with ETL, never break, safety net
- **Layer 2**: Phase-scoped Zeta unit tests (TDD) — test only what's implemented in current phase
- **Layer 3**: DualTransformationTest with ModelComparator — informational during development, must-pass at phase completion
- **Layer 4**: Parameterize existing tests with `@ParameterizedTest` + `TransformationMode` — only after all Zeta rules for a domain are complete

**Rationale**: Existing tests depend on rules from multiple phases. Parameterizing them too early would fail for missing Zeta rules. The layered approach ensures no regressions while enabling TDD.

### 3. jsl2psm Before jsl2ui

**Decision**: Implement jsl2psm Zeta transformation first (Phases 1-4), then jsl2ui (Phases 5-6).

**Rationale**: jsl2psm follows the same source→PSM pattern as esm2psm in the base project, so reference patterns apply directly. jsl2ui has unique complexity: per-frontend iteration, position-based sorting via pre/post blocks, and massive view rules (67 rules in `viewTableDeclaration.etl` alone).

### 4. Per-Frontend Iteration in jsl2ui Zeta

**Decision**: Run the full Zeta transformation once per `UIFrontendDeclaration`, matching ETL behavior. Use `@PreExecution` for position counter initialization and `@PostExecution` for sorting.

**Alternatives considered**:
- Single-pass with discriminators: More efficient but harder to guarantee identical output and riskier for equivalence testing.

**Rationale**: Matching ETL's execution model (one pass per frontend) ensures output equivalence and simplifies comparison.

### 5. SNAPSHOT Dependencies

**Decision**: Use SNAPSHOT versions for judo-zeta, judo-tatami-core, and judo-tatami-util. Compile them locally with `mvn clean install` before building this project.

**Rationale**: These are actively developed and the Zeta transformation feature requires latest APIs (TransformationMode, TraceFormat, etc.). The test-utils module (ModelComparator, model generators) is added as test-scoped.

### 6. Rule Name Constants

**Decision**: Define all rule names as `public static final String` constants in `Jsl2PsmRuleNames.java` and `Jsl2UiRuleNames.java`. Use these constants in `@TransformRule(name = ...)`, `@Extends(...)`, and `ctx.executeParentRule(...)`.

**Rationale**: Type-safe references prevent typos in rule names, which are stringly-typed in the framework. Matches the pattern used in judo-tatami-base.

### 7. Zeta Rule Class Organization — 1:1 with ETL Files

**Decision**: Create one Zeta rule class per ETL file, organized in the same package structure as the ETL modules. Each `.etl` file maps to exactly one Java class in the corresponding `zeta/rules/<domain>/` package.

**Alternatives considered**:
- Consolidating multiple ETL files into one Java class per domain: Fewer files, but loses the clear traceability between ETL and Zeta implementations, makes it harder to review and compare conversions, and results in very large files.

**Rationale**: 1:1 mapping preserves direct traceability (`action.etl` → `ActionRules.java`), makes code review straightforward (diff ETL file against its Java counterpart), and keeps each file focused and manageable.

```
jsl2psm/zeta/rules/
├── namespace/
│   └── NamespaceRules.java                          (← namespace.etl, 2 rules)
├── type/
│   └── TypeRules.java                               (← type.etl, 9 rules)
├── data/
│   ├── EntityTypeRules.java                         (← entityType.etl, 1 rule)
│   ├── AssociationRules.java                        (← association.etl, 3 rules)
│   ├── ContainmentRules.java                        (← containment.etl, 10 rules)
│   ├── CardinalityRules.java                        (← cardinality.etl, 6 rules)
│   ├── PrimitiveTypedElementRules.java              (← primitiveTypedElement.etl, 12 rules)
│   └── QueryRules.java                              (← query.etl, 17 rules)
├── derived/
│   ├── DataPropertyRules.java                       (← dataProperty.etl, 1 rule)
│   ├── NavigationPropertyRules.java                 (← navigationProperty.etl, 1 rule)
│   ├── EntityQueryRules.java                        (← entityQuery.etl, 3 rules)
│   └── ExpressionTypeRules.java                     (← expressionType.etl, 2 rules)
├── structure/
│   ├── EntityDeclarationDefaultTransferObjectTypeRules.java  (← 1 rule)
│   ├── EntityDeclarationDefaultTransferAttributeRules.java   (← 12 rules)
│   ├── EntityDeclarationDefaultTransferRelationRules.java    (← 15 rules)
│   ├── TransferDeclarationTransferObjectTypeRules.java       (← 2 rules)
│   ├── TransferDeclarationTransferAttributeRules.java        (← 9 rules)
│   ├── TransferDeclarationTransferRelationRules.java         (← 13 rules)
│   └── TransferDeclarationQueryCustomizerRules.java          (← 23 rules)
├── action/
│   ├── ActionRules.java                             (← action.etl, 30 rules)
│   ├── ActorBehaviourRules.java                     (← actorBehaviour.etl, 4 rules)
│   ├── DeleteBehaviourRules.java                    (← deleteBehaviour.etl, 3 rules)
│   ├── UpdateBehaviourRules.java                    (← updateBehaviour.etl, 11 rules)
│   ├── ValidateUpdateBehaviourRules.java            (← validateUpdateBehaviour.etl, 11 rules)
│   ├── RefreshBehaviourRules.java                   (← refreshBehaviour.etl, 11 rules)
│   ├── GetTemplateBehaviourRules.java               (← getTemplateBehaviour.etl, 4 rules)
│   ├── GetUploadTokenBehaviourRules.java            (← getUploadTokenBehaviour.etl, 7 rules)
│   ├── RelationListBehaviourRules.java              (← relationListBehaviour.etl, 11 rules)
│   ├── RelationCreateBehaviourRules.java            (← relationCreateBehaviour.etl, 12 rules)
│   ├── RelationValidateCreateBehaviourRules.java    (← relationValidateCreateBehaviour.etl, 12 rules)
│   ├── RelationAddReferenceBehaviourRules.java      (← relationAddReferenceBehaviour.etl, 7 rules)
│   ├── RelationRemoveReferenceBehaviourRules.java   (← relationRemoveReferenceBehaviour.etl, 7 rules)
│   ├── RelationSetReferenceBehaviourRules.java      (← relationSetReferenceBehaviour.etl, 7 rules)
│   ├── RelationUnsetReferenceBehaviourRules.java    (← relationUnsetReferenceBehaviour.etl, 7 rules)
│   ├── RelationGetRangeReferenceBehaviourRules.java (← relationGetRangeReferenceBehaviour.etl, 16 rules)
│   ├── GetActionInputRangeBehaviourRules.java       (← getActionInputRangeBehaviour.etl, 17 rules)
│   ├── AccessListBehaviourRules.java                (← accessListBehaviour.etl, 6 rules)
│   ├── AccessCreateBehaviourRules.java              (← accessCreateBehaviour.etl, 6 rules)
│   └── AccessValidateCreateBehaviourRules.java      (← accessValidateCreateBehaviour.etl, 6 rules)
└── actor/
    ├── ActorTypeRules.java                          (← actorType.etl, 28 rules)
    └── AccessRules.java                             (← access.etl, 2 rules)

jsl2ui/zeta/rules/
├── application/
│   ├── ActorDeclarationRules.java                   (← actorDeclaration.etl, 2 rules)
│   ├── ActorGroupDeclarationRules.java              (← actorGroupDeclaration.etl, 1 rule)
│   └── ModifiableRules.java                         (← modifiable.etl, 2 rules)
├── structure/
│   ├── TransferDeclarationRules.java                (← transferDeclaration.etl, 1 rule)
│   ├── TransferActionDeclarationRules.java          (← transferActionDeclaration.etl, 5 rules)
│   ├── TransferFieldDeclarationRules.java           (← transferFieldDeclaration.etl, 4 rules)
│   └── TransferRelationDeclarationRules.java        (← transferRelationDeclaration.etl, 3 rules)
├── type/
│   ├── TypeRules.java                               (← type.etl, 10 rules)
│   └── DataTypeOperationRules.java                  (← dataTypeOperation.etl, 21 rules)
└── view/
    ├── ViewDeclarationRules.java                    (← viewDeclaration.etl, 15 rules)
    ├── ViewDeclarationFormRules.java                (← viewDeclarationForm.etl, 10 rules)
    ├── ViewGroupDeclarationRules.java               (← viewGroupDeclaration.etl, 5 rules)
    ├── ViewTabsDeclarationRules.java                (← viewTabsDeclaration.etl, 3 rules)
    ├── ViewTableDeclarationRules.java               (← viewTableDeclaration.etl, 67 rules)
    ├── ViewTableDeclarationAddSelectorPageRules.java (← 5 rules)
    ├── ViewTableDeclarationFormPageRules.java       (← 3 rules)
    ├── ViewTableDeclarationViewPageRules.java       (← 15 rules)
    ├── ViewLinkDeclarationRules.java                (← viewLinkDeclaration.etl, 29 rules)
    ├── ViewLinkDeclarationFormPageRules.java        (← 3 rules)
    ├── ViewLinkDeclarationSetSelectorPageRules.java (← 5 rules)
    ├── ViewLinkDeclarationViewPageRules.java        (← 12 rules)
    ├── MenuTableDeclarationRules.java               (← menuTableDeclaration.etl, 1 rule)
    ├── MenuTableDeclarationTablePageRules.java      (← 9 rules)
    ├── MenuTableDeclarationCardsPageRules.java      (← 5 rules)
    ├── MenuTableDeclarationTagsPageRules.java       (← 5 rules)
    ├── MenuTableDeclarationFormPageRules.java       (← 4 rules)
    ├── MenuTableDeclarationAddSelectorPageRules.java (← 5 rules)
    ├── MenuTableDeclarationViewPageRules.java       (← 5 rules)
    ├── MenuLinkDeclarationRules.java                (← menuLinkDeclaration.etl, 1 rule)
    ├── MenuLinkDeclarationFormPageRules.java        (← 4 rules)
    ├── MenuLinkDeclarationViewPageRules.java        (← 5 rules)
    ├── RowDeclarationRules.java                     (← rowDeclaration.etl, 40 rules)
    ├── RowActionDeclarationRules.java               (← rowActionDeclaration.etl, 5 rules)
    ├── RowColumnDeclarationRules.java               (← rowColumnDeclaration.etl, 3 rules)
    ├── CardDeclarationRules.java                    (← cardDeclaration.etl, 21 rules)
    ├── TagDeclarationRules.java                     (← tagDeclaration.etl, 18 rules)
    ├── ViewActionDeclarationRules.java              (← viewActionDeclaration.etl, 24 rules)
    ├── ActionGroupDeclarationRules.java             (← actionGroupDeclaration.etl, 1 rule)
    ├── ViewWidgetDeclarationRules.java              (← viewWidgetDeclaration.etl, 14 rules)
    ├── FrontendDeclarationRules.java                (← frontendDeclaration.etl, 5 rules)
    └── EnumLiteralRules.java                        (← enumLiteral.etl, 1 rule)
```

### 8. Documentation Approach

**Decision**: Convert `.adoc` → `.md` and PlantUML → Mermaid. Create `docs/transformations/` with per-module rule documentation.

**Rationale**: Markdown is universally supported by AI tools, GitHub rendering, and modern tooling. Mermaid renders natively in GitHub Markdown.

## Risks / Trade-offs

- **[Scale]** 1,165 elements to port is large → Mitigated by phased approach and proven patterns from judo-tatami-base. Each phase is self-contained and testable.

- **[Equivalence]** Zeta output may not exactly match ETL output → Mitigated by ModelComparator with STRUCTURAL mode (tolerates annotation ordering differences). DualTransformationTest catches deviations early.

- **[SNAPSHOT instability]** SNAPSHOT dependencies may change → Mitigated by compiling all dependencies locally and testing before integration. Pin to specific commits if needed.

- **[jsl2ui complexity]** Per-frontend iteration and position sorting are unique patterns not in the base project → Mitigated by using `@PreExecution`/`@PostExecution` annotations and implementing jsl2psm first to build familiarity.

- **[EOL operation complexity]** Some EOL operations have complex navigation logic (e.g., `modifiable.eol` with 30 operations) → Mitigated by static helper approach which allows straightforward debugging and testing.

- **[Thread safety]** EMF ELists are not thread-safe when running Zeta in parallel mode → Mitigated by synchronized helper methods (pattern from judo-tatami-base's `Psm2AsmHelper`). Start with `parallel(false)` and enable after correctness is verified.
