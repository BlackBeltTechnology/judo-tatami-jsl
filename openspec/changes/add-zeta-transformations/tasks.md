## 1. Infrastructure Setup (Phase 0)

- [x] 1.1 Compile judo-zeta locally: `mvn clean install -DskipTests` in `/Users/robson/Project/judo-ng/runtime/judo-zeta`
- [x] 1.2 Compile judo-tatami-core locally: `mvn clean install -DskipTests` in `/Users/robson/Project/judo-ng/runtime/judo-tatami-core`
- [x] 1.3 Compile judo-tatami-util locally: `mvn clean install -DskipTests` in `/Users/robson/Project/judo-ng/runtime/judo-tatami-util`
- [x] 1.4 Compile judo-tatami-base locally (for test-utils): `mvn clean install -DskipTests` in `/Users/robson/Project/judo-ng/runtime/judo-tatami-base`
- [x] 1.5 Add judo-zeta SNAPSHOT dependency to parent `pom.xml` (dependency management)
- [x] 1.6 Switch `judo-tatami-core-version` and `judo-tatami-util-version` to SNAPSHOT in parent `pom.xml`
- [x] 1.7 Add `judo-tatami-test-utils` as test-scoped dependency in jsl2psm and jsl2ui modules
- [x] 1.8 Add judo-zeta transformation-core dependency to jsl2psm and jsl2ui modules
- [x] 1.9 Verify build: `mvn clean install` for entire project

## 2. JSL2PSM Zeta Scaffold (Phase 1 - Namespace + Type)

- [x] 2.1 Create `Jsl2PsmRuleNames.java` with constants for all 367 rule names
- [x] 2.2 Create `Jsl2PsmHelper.java` with common utility methods (ID handling, thread-safe adds, annotation helpers)
- [x] 2.3 Port EOL operations from `operations/jsl/` to static helper classes
- [x] 2.4 Port EOL operations from `operations/utils.eol`, `operations/id.eol`, `operations/equivalentDiscriminated.eol`
- [x] 2.5 Create `Jsl2PsmZetaTransformation.java` scaffold with `execute()`, `createRegistry()`, `createContext()`, `postProcess()`
- [x] 2.6 TDD: Write Zeta unit test for namespace rules (minimal JSL model → verify Model + Package in PSM)
- [x] 2.7 Implement `zeta/rules/namespace/NamespaceRules.java` (2 rules from `namespace.etl`)
- [x] 2.8 TDD: Write Zeta unit test for type rules (all 8 primitive types)
- [x] 2.9 Implement `zeta/rules/type/TypeRules.java` (9 rules from `type.etl`)
- [x] 2.10 Add `TransformationMode` field to `Jsl2PsmWorkParameter` (default: `TransformationMode.fromSystemProperty()`)
- [x] 2.11 Update `Jsl2PsmWork.execute()` to dispatch between ETL and Zeta based on mode
- [x] 2.12 Create `Jsl2PsmDualTransformationTest.java` (6 tests — all passing, ETL/Zeta equivalence verified)
- [x] 2.13 Verify build: `mvn clean test -pl judo-tatami-jsl-jsl2psm` (80 tests, 0 failures)

## 3. JSL2PSM Data + Derived Rules (Phase 2)

- [x] 3.1 TDD: Write Zeta unit tests for entity type, association, containment rules
- [x] 3.2 Implement `zeta/rules/data/EntityTypeRules.java` (1 rule from `entityType.etl`)
- [x] 3.3 Implement `zeta/rules/data/AssociationRules.java` (3 rules from `association.etl`)
- [x] 3.4 Implement `zeta/rules/data/ContainmentRules.java` (10 rules from `containment.etl`)
- [x] 3.5 Implement `zeta/rules/data/CardinalityRules.java` (6 rules from `cardinality.etl`)
- [x] 3.6 Implement `zeta/rules/data/PrimitiveTypedElementRules.java` (12 rules from `primitiveTypedElement.etl`)
- [x] 3.7 Implement `zeta/rules/data/StaticQueryRules.java` (17 rules from `query.etl`)
- [x] 3.8 TDD: Write Zeta unit tests for derived properties
- [x] 3.9 Implement `zeta/rules/derived/DataPropertyRules.java` (1 rule from `dataProperty.etl`)
- [x] 3.10 Implement `zeta/rules/derived/NavigationPropertyRules.java` (1 rule from `navigationProperty.etl`)
- [x] 3.11 Implement `zeta/rules/derived/EntityQueryRules.java` (3 rules from `entityQuery.etl`)
- [x] 3.12 Implement `zeta/rules/derived/ExpressionTypeRules.java` (2 rules from `expressionType.etl`)
- [x] 3.13 Update `Jsl2PsmDualTransformationTest` — verify diff count decreased
- [x] 3.14 Verify build: `mvn clean test -pl judo-tatami-jsl-jsl2psm` (92 tests, 0 failures)

## 4. JSL2PSM Structure Rules (Phase 3)

- [x] 4.1 TDD: Write Zeta unit tests for transfer object types (mapped, unmapped, default)
- [x] 4.2 Implement `zeta/rules/structure/DefaultTransferObjectTypeRules.java` (1 rule)
- [x] 4.3 Implement `zeta/rules/structure/DefaultTransferAttributeRules.java` (12 rules)
- [x] 4.4 Implement `zeta/rules/structure/DefaultTransferRelationRules.java` (15 rules)
- [x] 4.5 Implement `zeta/rules/structure/TransferObjectTypeRules.java` (2 rules)
- [x] 4.6 Implement `zeta/rules/structure/TransferAttributeRules.java` (9 rules)
- [x] 4.7 Implement `zeta/rules/structure/TransferRelationRules.java` (13 rules)
- [x] 4.8 Implement `zeta/rules/structure/QueryCustomizerRules.java` (23 rules)
- [x] 4.9 Update `Jsl2PsmDualTransformationTest` — verify significant convergence (18 dual tests passing)
- [x] 4.10 Verify build: `mvn clean test -pl judo-tatami-jsl-jsl2psm` (92 tests, 0 failures)

## 5. JSL2PSM Action + Actor Rules (Phase 4)

- [x] 5.1 TDD: Write Zeta unit tests for action declarations and core behaviours
- [x] 5.2 Implement `zeta/rules/action/ActionRules.java` (30 rules from `action.etl`)
- [x] 5.3 Implement `zeta/rules/action/ActorBehaviourRules.java` (4 rules from `actorBehaviour.etl`) — skipped, entirely commented out in ETL (ESM not JSL), rules already in ActorTypeRules.java
- [x] 5.4 Implement `zeta/rules/action/DeleteBehaviourRules.java` (3 rules from `deleteBehaviour.etl`)
- [x] 5.5 Implement `zeta/rules/action/UpdateBehaviourRules.java` (11 rules from `updateBehaviour.etl`)
- [x] 5.6 Implement `zeta/rules/action/ValidateUpdateBehaviourRules.java` (11 rules from `validateUpdateBehaviour.etl`)
- [x] 5.7 Implement `zeta/rules/action/RefreshBehaviourRules.java` (11 rules from `refreshBehaviour.etl`)
- [x] 5.8 Implement `zeta/rules/action/GetTemplateBehaviourRules.java` (4 rules from `getTemplateBehaviour.etl`)
- [x] 5.9 Implement `zeta/rules/action/GetUploadTokenBehaviourRules.java` (7 rules from `getUploadTokenBehaviour.etl`)
- [x] 5.10 Implement `zeta/rules/action/RelationListBehaviourRules.java` (11 rules from `relationListBehaviour.etl`)
- [x] 5.11 Implement `zeta/rules/action/RelationCreateBehaviourRules.java` (12 rules from `relationCreateBehaviour.etl`)
- [x] 5.12 Implement `zeta/rules/action/RelationValidateCreateBehaviourRules.java` (12 rules from `relationValidateCreateBehaviour.etl`)
- [x] 5.13 Implement `zeta/rules/action/RelationAddReferenceBehaviourRules.java` (7 rules from `relationAddReferenceBehaviour.etl`)
- [x] 5.14 Implement `zeta/rules/action/RelationRemoveReferenceBehaviourRules.java` (7 rules from `relationRemoveReferenceBehaviour.etl`)
- [x] 5.15 Implement `zeta/rules/action/RelationSetReferenceBehaviourRules.java` (7 rules from `relationSetReferenceBehaviour.etl`)
- [x] 5.16 Implement `zeta/rules/action/RelationUnsetReferenceBehaviourRules.java` (7 rules from `relationUnsetReferenceBehaviour.etl`)
- [x] 5.17 Implement `zeta/rules/action/RelationGetRangeReferenceBehaviourRules.java` (16 rules from `relationGetRangeReferenceBehaviour.etl`)
- [x] 5.18 Implement `zeta/rules/action/GetActionInputRangeBehaviourRules.java` (17 rules from `getActionInputRangeBehaviour.etl`)
- [x] 5.19 Implement `zeta/rules/action/AccessListBehaviourRules.java` (6 rules from `accessListBehaviour.etl`)
- [x] 5.20 Implement `zeta/rules/action/AccessCreateBehaviourRules.java` (6 rules from `accessCreateBehaviour.etl`)
- [x] 5.21 Implement `zeta/rules/action/AccessValidateCreateBehaviourRules.java` (6 rules from `accessValidateCreateBehaviour.etl`)
- [x] 5.22 TDD: Write Zeta unit tests for actor types and access
- [x] 5.23 Implement `zeta/rules/actor/ActorTypeRules.java` (28 rules from `actorType.etl`)
- [x] 5.24 Implement `zeta/rules/actor/AccessRules.java` (2 rules from `access.etl`)
- [x] 5.25 `Jsl2PsmDualTransformationTest` — MUST PASS (zero differences)
- [x] 5.26 Parameterize ALL existing jsl2psm tests with `@ParameterizedTest @EnumSource(TransformationMode.class)`
- [x] 5.27 Verify full build: `mvn clean test -pl judo-tatami-jsl-jsl2psm`

## 6. JSL2UI Zeta Scaffold + Core Rules (Phase 5)

- [ ] 6.1 Port 243 EOL operations from jsl2ui `operations/` to static helper classes
- [ ] 6.2 Create `Jsl2UiRuleNames.java` with constants for all 397 rule names
- [ ] 6.3 Create `Jsl2UiHelper.java` with UI-specific utility methods
- [ ] 6.4 Create `Jsl2UiZetaTransformation.java` with per-frontend iteration, `@PreExecution` position init, `@PostExecution` sorting
- [ ] 6.5 TDD: Write Zeta unit tests for application rules
- [ ] 6.6 Implement `zeta/rules/application/ActorDeclarationRules.java` (2 rules from `actorDeclaration.etl`)
- [ ] 6.7 Implement `zeta/rules/application/ActorGroupDeclarationRules.java` (1 rule from `actorGroupDeclaration.etl`)
- [ ] 6.8 Implement `zeta/rules/application/ModifiableRules.java` (2 rules from `modifiable.etl`)
- [ ] 6.9 TDD: Write Zeta unit tests for structure rules
- [ ] 6.10 Implement `zeta/rules/structure/TransferDeclarationRules.java` (1 rule from `transferDeclaration.etl`)
- [ ] 6.11 Implement `zeta/rules/structure/TransferActionDeclarationRules.java` (5 rules from `transferActionDeclaration.etl`)
- [ ] 6.12 Implement `zeta/rules/structure/TransferFieldDeclarationRules.java` (4 rules from `transferFieldDeclaration.etl`)
- [ ] 6.13 Implement `zeta/rules/structure/TransferRelationDeclarationRules.java` (3 rules from `transferRelationDeclaration.etl`)
- [ ] 6.14 TDD: Write Zeta unit tests for type rules
- [ ] 6.15 Implement `zeta/rules/type/TypeRules.java` (10 rules from `type.etl`)
- [ ] 6.16 Implement `zeta/rules/type/DataTypeOperationRules.java` (21 rules from `dataTypeOperation.etl`)
- [ ] 6.17 Add `TransformationMode` field to `Jsl2UiWorkParameter`
- [ ] 6.18 Update `Jsl2UiWork.execute()` to dispatch between ETL and Zeta
- [ ] 6.19 Create `Jsl2UiDualTransformationTest.java` (informational)
- [ ] 6.20 Verify build: `mvn clean test -pl judo-tatami-jsl-jsl2ui`

## 7. JSL2UI View Rules (Phase 6)

- [ ] 7.1 TDD: Write Zeta unit tests for view declaration rules
- [ ] 7.2 Implement `zeta/rules/view/ViewDeclarationRules.java` (15 rules from `viewDeclaration.etl`)
- [ ] 7.3 Implement `zeta/rules/view/ViewDeclarationFormRules.java` (10 rules from `viewDeclarationForm.etl`)
- [ ] 7.4 Implement `zeta/rules/view/ViewGroupDeclarationRules.java` (5 rules from `viewGroupDeclaration.etl`)
- [ ] 7.5 Implement `zeta/rules/view/ViewTabsDeclarationRules.java` (3 rules from `viewTabsDeclaration.etl`)
- [ ] 7.6 TDD: Write Zeta unit tests for view table rules
- [ ] 7.7 Implement `zeta/rules/view/ViewTableDeclarationRules.java` (67 rules from `viewTableDeclaration.etl`)
- [ ] 7.8 Implement `zeta/rules/view/ViewTableDeclarationAddSelectorPageRules.java` (5 rules)
- [ ] 7.9 Implement `zeta/rules/view/ViewTableDeclarationFormPageRules.java` (3 rules)
- [ ] 7.10 Implement `zeta/rules/view/ViewTableDeclarationViewPageRules.java` (15 rules)
- [ ] 7.11 TDD: Write Zeta unit tests for view link rules
- [ ] 7.12 Implement `zeta/rules/view/ViewLinkDeclarationRules.java` (29 rules from `viewLinkDeclaration.etl`)
- [ ] 7.13 Implement `zeta/rules/view/ViewLinkDeclarationFormPageRules.java` (3 rules)
- [ ] 7.14 Implement `zeta/rules/view/ViewLinkDeclarationSetSelectorPageRules.java` (5 rules)
- [ ] 7.15 Implement `zeta/rules/view/ViewLinkDeclarationViewPageRules.java` (12 rules)
- [ ] 7.16 TDD: Write Zeta unit tests for menu rules
- [ ] 7.17 Implement `zeta/rules/view/MenuTableDeclarationRules.java` (1 rule from `menuTableDeclaration.etl`)
- [ ] 7.18 Implement `zeta/rules/view/MenuTableDeclarationTablePageRules.java` (9 rules)
- [ ] 7.19 Implement `zeta/rules/view/MenuTableDeclarationCardsPageRules.java` (5 rules)
- [ ] 7.20 Implement `zeta/rules/view/MenuTableDeclarationTagsPageRules.java` (5 rules)
- [ ] 7.21 Implement `zeta/rules/view/MenuTableDeclarationFormPageRules.java` (4 rules)
- [ ] 7.22 Implement `zeta/rules/view/MenuTableDeclarationAddSelectorPageRules.java` (5 rules)
- [ ] 7.23 Implement `zeta/rules/view/MenuTableDeclarationViewPageRules.java` (5 rules)
- [ ] 7.24 Implement `zeta/rules/view/MenuLinkDeclarationRules.java` (1 rule from `menuLinkDeclaration.etl`)
- [ ] 7.25 Implement `zeta/rules/view/MenuLinkDeclarationFormPageRules.java` (4 rules)
- [ ] 7.26 Implement `zeta/rules/view/MenuLinkDeclarationViewPageRules.java` (5 rules)
- [ ] 7.27 TDD: Write Zeta unit tests for row, card, and tag rules
- [ ] 7.28 Implement `zeta/rules/view/RowDeclarationRules.java` (40 rules from `rowDeclaration.etl`)
- [ ] 7.29 Implement `zeta/rules/view/RowActionDeclarationRules.java` (5 rules from `rowActionDeclaration.etl`)
- [ ] 7.30 Implement `zeta/rules/view/RowColumnDeclarationRules.java` (3 rules from `rowColumnDeclaration.etl`)
- [ ] 7.31 Implement `zeta/rules/view/CardDeclarationRules.java` (21 rules from `cardDeclaration.etl`)
- [ ] 7.32 Implement `zeta/rules/view/TagDeclarationRules.java` (18 rules from `tagDeclaration.etl`)
- [ ] 7.33 TDD: Write Zeta unit tests for action and widget rules
- [ ] 7.34 Implement `zeta/rules/view/ViewActionDeclarationRules.java` (24 rules from `viewActionDeclaration.etl`)
- [ ] 7.35 Implement `zeta/rules/view/ActionGroupDeclarationRules.java` (1 rule from `actionGroupDeclaration.etl`)
- [ ] 7.36 Implement `zeta/rules/view/ViewWidgetDeclarationRules.java` (14 rules from `viewWidgetDeclaration.etl`)
- [ ] 7.37 Implement `zeta/rules/view/FrontendDeclarationRules.java` (5 rules from `frontendDeclaration.etl`)
- [ ] 7.38 Implement `zeta/rules/view/EnumLiteralRules.java` (1 rule from `enumLiteral.etl`)
- [ ] 7.39 `Jsl2UiDualTransformationTest` — MUST PASS (zero differences)
- [ ] 7.40 Parameterize ALL existing jsl2ui tests with `@ParameterizedTest @EnumSource(TransformationMode.class)`
- [ ] 7.41 Verify full build: `mvn clean test -pl judo-tatami-jsl-jsl2ui`

## 8. Workflow Integration (Phase 7)

- [ ] 8.1 Add `transformationMode` field to `DefaultWorkflowSetupParameters` (default: ZETA)
- [ ] 8.2 Update `WorkflowHelper` to pass `TransformationMode` to `Jsl2PsmWorkParameter` and `Jsl2UiWorkParameter`
- [ ] 8.3 Add `transformationMode` Maven parameter to `DefaultWorkflowMojo` (string, default "ZETA")
- [ ] 8.4 Update `AbstractJslDslWorkflowProjectMojo` to propagate mode to workflow setup
- [ ] 8.5 Update `JslDefaultWorkflowTest` to test with both transformation modes
- [ ] 8.6 Verify end-to-end: `mvn clean install`

## 9. Performance Testing (Phase 8a)

- [ ] 9.1 Analyze RackInspect model at `/Users/robson/Project/rackinspect/application/model/target/generated-resources/model/` to extract characteristics (entity count, attribute density, relation patterns)
- [ ] 9.2 Create `RealisticJslModelGenerator.java` that builds a JSL model programmatically with ~10,000 elements matching RackInspect characteristics
- [ ] 9.3 Create `Jsl2PsmPerformanceTest.java` with `@Tag("performance")` — runs ETL and Zeta, logs timing comparison, asserts equivalence
- [ ] 9.4 Create `Jsl2UiPerformanceTest.java` with `@Tag("performance")` — same structure
- [ ] 9.5 Configure Surefire to exclude `@Tag("performance")` by default
- [ ] 9.6 Verify: `mvn clean test -Dgroups=performance -pl judo-tatami-jsl-jsl2psm`

## 10. Documentation (Phase 8b)

- [ ] 10.1 Convert `README.adoc` → `README.md`
- [ ] 10.2 Convert `CONTRIBUTING.adoc` → `CONTRIBUTING.md`
- [ ] 10.3 Convert `docs/pages/judo-tatami-jsl-workflow-maven-plugin.adoc` → `.md` (convert inline PlantUML → Mermaid)
- [ ] 10.4 Convert `docs/_attributes.adoc` and `docs/pages/_attributes.adoc` if needed, or remove if only relevant to AsciiDoc rendering
- [ ] 10.5 Convert 14 PlantUML diagrams in `judo-tatami-jsl-jsl2psm/diagram/` → Mermaid `.md` files
- [ ] 10.6 Convert module README files (`judo-tatami-jsl-workflow-maven-plugin/README.adoc`)
- [ ] 10.7 Create `docs/transformations/jsl2psm.md` — document all 367 ETL rules (name, source type, target type, guard, description)
- [ ] 10.8 Create `docs/transformations/jsl2ui.md` — document all 397 ETL rules
- [ ] 10.9 Update `README.md` with Zeta support, dual-engine config, performance test instructions
- [ ] 10.10 Update `AGENTS.md` with Zeta transformation architecture section
- [ ] 10.11 Remove original `.adoc` files after conversion
