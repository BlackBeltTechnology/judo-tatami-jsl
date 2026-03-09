# JUDO Tatami JSL - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-tatami-jsl
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4+ with OSGi bundles
**Version:** 1.1.4-SNAPSHOT

This project contains the **JSL (Judo Specification Language) transformation pipeline** for the JUDO platform. It transforms JSL source models into PSM (Platform Specific Model) and UI (User Interface) models using two transformation engines: **Epsilon ETL** (Epsilon Transformation Language) and **Zeta** (Java-based annotation-driven framework). The workflow module orchestrates the full chain from JSL through PSM, ASM, RDBMS, Liquibase, Expression, Measure, Keycloak, and UI models by delegating to the downstream `judo-tatami` (judo-tatami-base) transformation modules.

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
judo-tatami-jsl/
├── judo-tatami-jsl-jsl2psm/                    # JSL to PSM transformation (ETL + Zeta)
├── judo-tatami-jsl-jsl2ui/                      # JSL to UI transformation (ETL + Zeta)
├── judo-tatami-jsl-workflow/                    # Workflow orchestration (full pipeline)
├── judo-tatami-jsl-workflow-maven-plugin/       # Maven plugin for running workflows
├── judo-tatami-jsl-workflow-maven-plugin-test/  # Maven plugin integration test
├── docs/                                        # Documentation
└── openspec/                                    # OpenSpec change management
```

## Transformation Pipeline

```
JSL (Judo Specification Language - .jsl files)
  ├→ (judo-tatami-jsl-jsl2psm) → PSM (Platform Specific Model)
  │   ├→ (judo-tatami-psm2asm*) → ASM (Abstract Semantic Model)
  │   │   ├→ (judo-tatami-asm2rdbms*) → RDBMS Schema (per dialect: hsqldb, postgresql)
  │   │   │   └→ (judo-tatami-rdbms2liquibase*) → Liquibase Changelog
  │   │   ├→ (judo-tatami-asm2expression*) → Expression Model
  │   │   └→ (judo-tatami-asm2keycloak*) → Keycloak Config
  │   └→ (judo-tatami-psm2measure*) → Measure Model
  └→ (judo-tatami-jsl-jsl2ui) → UI Model

* = downstream transformations from judo-tatami-base, orchestrated by judo-tatami-jsl-workflow
```

## Module Details

### judo-tatami-jsl-jsl2psm

**Purpose:** Transforms JSL models into PSM models using Epsilon ETL.

**Key Classes:**
| Class | Purpose |
|-------|---------|
| `Jsl2Psm.java` | Main transformation entry point. Executes ETL with configurable parameters |
| `Jsl2PsmWork.java` | Workflow integration class (extends AbstractTransformationWork) |
| `Jsl2PsmTransformationTrace.java` | Tracks mapping between JSL and PSM elements |
| `JslExpressionToJqlExpression.java` | Converts JSL expressions to JQL (Judo Query Language) |
| `Jsl2JqlFunction.java` | JSL function to JQL function translation |
| `Jsl2PsmTransformationService.java` | OSGi Declarative Service |

**ETL Structure:**
```
src/main/epsilon/
├── transformations/psm/
│   ├── jslToPsm.etl                    # Main transformation entry point
│   └── modules/
│       ├── namespace/namespace.etl      # Model and package creation
│       ├── data/                        # Entity types, attributes, relations
│       │   ├── entityType.etl
│       │   ├── association.etl
│       │   ├── containment.etl
│       │   ├── cardinality.etl
│       │   ├── primitiveTypedElement.etl
│       │   └── query.etl
│       ├── type/type.etl               # Primitive types (string, numeric, date, etc.)
│       ├── derived/                     # Derived properties and queries
│       │   ├── dataProperty.etl
│       │   ├── navigationProperty.etl
│       │   ├── entityQuery.etl
│       │   └── expressionType.etl
│       ├── structure/                   # Transfer objects (mapped and unmapped)
│       │   ├── entityDeclarationDefaultTransferObjectType.etl
│       │   ├── transferDeclarationTransferObjectType.etl
│       │   ├── transferDeclarationQueryCustomizer.etl
│       │   └── ... (attributes, relations)
│       ├── action/                      # Operations and behaviours
│       │   ├── action.etl
│       │   ├── actorBehaviour.etl
│       │   ├── deleteBehaviour.etl
│       │   ├── updateBehaviour.etl
│       │   └── ... (CRUD behaviours)
│       └── actor/                       # Actor types and access control
│           ├── actorType.etl
│           └── access.etl
└── operations/                          # EOL helper operations
    ├── _importAll.eol
    ├── utils.eol
    └── jsl/                             # JSL-specific operations
```

**Transformation Parameters (Jsl2PsmParameter):**
- `createTrace` - Enable/disable trace generation
- `parallel` - Enable parallel execution
- `generateBehaviours` - Generate CRUD behaviours
- `useCache` - Enable model caching
- Naming prefixes/suffixes for entities, transfer objects, parameters

**Tests:** 33 test files organized by domain (entity, type, transferobject, derived, operation, actor, namespace, error, functions).

**Zeta Structure:**
```
src/main/java/.../jsl2psm/zeta/
├── Jsl2PsmZetaTransformation.java    # Main entry point (builder pattern)
├── Jsl2PsmHelper.java                # Utility methods (ID handling, thread-safe adds)
├── Jsl2PsmRuleNames.java             # Constants for all rule names
└── rules/
    ├── namespace/NamespaceRules.java  # Model and package creation
    ├── type/TypeRules.java            # Primitive types
    ├── data/                          # Entity types, attributes, relations
    ├── structure/                     # Transfer objects
    ├── derived/                       # Derived properties
    ├── action/                        # Operations and behaviours
    └── actor/                         # Actor types and access control
```

**Zeta Tests:**
- `src/test/java/.../dual/` - Dual transformation tests comparing ETL and Zeta
- `src/test/java/.../perf/` - Performance comparison tests (discovery + realistic)

### judo-tatami-jsl-jsl2ui

**Purpose:** Transforms JSL models into UI models using Epsilon ETL or Zeta. Generates complete user interface specifications including applications, pages, navigation, widgets, and actions.

**Key Classes:**
| Class | Purpose |
|-------|---------|
| `Jsl2Ui.java` | Core transformation orchestrator. Executes ETL per `UIFrontendDeclaration` |
| `Jsl2UiWork.java` | Workflow integration class (supports TransformationMode) |
| `Jsl2UiZetaTransformation.java` | Zeta transformation entry point (builder pattern) |
| `Jsl2UiTransformationTrace.java` | Tracks JSL-to-UI element mappings |
| `Jsl2UiTransformationService.java` | OSGi Declarative Service |

**ETL Structure:**
```
src/main/epsilon/transformations/ui/
├── jslToUi.etl                          # Main transformation (imports 46 modules)
└── modules/
    ├── application/                     # Actor declarations, actor groups
    ├── structure/                       # Transfer objects, fields, relations, actions
    ├── type/                            # Data types, type operations
    └── view/                            # Views, menus, pages, widgets (22 files)
        ├── viewDeclaration.etl
        ├── viewTableDeclaration.etl
        ├── menuTableDeclaration.etl
        ├── menuLinkDeclaration.etl
        ├── cardDeclaration.etl
        ├── tagDeclaration.etl
        └── ...
```

**Context Variables Injected into ETL:**
- `frontend` - Current `UIFrontendDeclaration` being transformed
- `actorDeclaration` - Associated `ActorDeclaration`
- `defaultModelName` - Model name for package tokens
- `ecoreUtil`, `jslUtils`, `uiUtils` - Utility instances

**Tests:** 11 ETL test classes covering applications, CRUD, navigation, operations, widgets, data types, row operations, action groups.

**Zeta Structure:**
```
src/main/java/.../jsl2ui/zeta/
├── Jsl2UiZetaTransformation.java      # Main entry point (builder, per-frontend execution)
├── Jsl2UiHelper.java                  # Utility methods
├── Jsl2UiRuleNames.java               # Constants for all rule names
└── rules/
    ├── application/                    # FrontendDeclaration, ActorDeclaration, ActorGroup, Modifiables
    ├── structure/                      # TransferDeclaration, fields, relations, actions
    ├── type/                           # TypeRules, DataTypeOperationRules
    └── view/                           # ViewDeclaration, RowDeclaration, CardDeclaration, TagDeclaration, ActionGroups, MenuTable, MenuLink
```

**Zeta Tests:**
- `src/test/java/.../zeta/` - Zeta-specific unit tests (Application, View, ListItem, RowOps, ActionGroups)
- `src/test/java/.../dual/` - Dual transformation tests comparing ETL and Zeta
- `src/test/java/.../perf/` - Performance comparison tests

### judo-tatami-jsl-workflow

**Purpose:** Orchestrates the full transformation pipeline from JSL through all intermediate models.

**Key Classes:**
| Class | Purpose |
|-------|---------|
| `AbstractTatamiPipelineWorkflow.java` | Core pipeline orchestration with parallel/sequential modes |
| `JslDefaultWorkflow.java` | JSL-based workflow entry point |
| `PsmDefaultWorkflow.java` | PSM-based workflow (skips JSL→PSM) |
| `DefaultWorkflow.java` | Alias for JslDefaultWorkflow |
| `WorkflowHelper.java` | Factory for all transformation Work items |
| `DefaultWorkflowSetupParameters.java` | Workflow configuration (builder pattern, includes `transformationMode`) |
| `DefaultWorkflowMetricsCollector.java` | Thread-safe execution metrics |
| `DefaultWorkflowSave.java` | Model saving utilities |

**Execution Modes:**

Parallel (default):
```
1. Parallel Validations (PSM validation)
2. Parallel JSL Transformations (JSL→PSM, JSL→UI)
3. Parallel PSM Transformations (PSM→ASM, PSM→Measure)
4. Parallel ASM Transformations (ASM→Expression, ASM→Keycloak, ASM→RDBMS per dialect)
5. Parallel RDBMS Transformations (RDBMS→Liquibase per dialect)
```

**Configuration Flags:**
- `ignoreJsl2Psm`, `ignoreJsl2Ui` - Skip JSL transformations
- `ignorePsm2Asm`, `ignorePsm2Measure` - Skip PSM transformations
- `ignoreAsm2Rdbms`, `ignoreAsm2Expression`, `ignoreAsm2Keycloak` (default: true) - Skip ASM transformations
- `ignoreRdbms2Liquibase` - Skip RDBMS transformation
- `dialectList` - Database dialects (e.g., "hsqldb", "postgresql")
- `rdbmsTablePrefix`, `rdbmsColumnPrefix`, etc. - RDBMS naming conventions

### judo-tatami-jsl-workflow-maven-plugin

**Purpose:** Maven plugin that executes the JSL transformation pipeline as part of a Maven build.

**Goal:** `default-model-workflow` (default phase: `COMPILE`)

**Key Configuration Parameters:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `sources` | `${project.basedir}/src/main/model` | Source directories for `.jsl` files |
| `destination` | `${project.basedir}/target/model` | Output directory for generated models |
| `dialects` | `postgresql,hsqldb` | Database dialects to generate |
| `saveModels` | `true` | Save transformed models to disk |
| `runInParallel` | `true` | Enable parallel execution |
| `useCache` | `true` | Enable transformation cache |
| `generateBehaviours` | `true` | Generate CRUD behaviours |
| `ignoreAsm2Keycloak` | `true` | Skip Keycloak generation |
| All `ignore*` flags | varies | Skip individual transformations |

**Generated Output Files:**
```
<modelName>-jsl.model                    # JSL model (XMI)
<modelName>-psm.model                    # PSM model
<modelName>-asm.model                    # ASM model
<modelName>-ui.model                     # UI model
<modelName>-measure.model                # Measure model
<modelName>-expression.model             # Expression model
<modelName>-rdbms_<dialect>.model        # RDBMS model per dialect
<modelName>-liquibase_<dialect>.changelog.xml  # Liquibase changelog per dialect
<modelName>-keycloak.model               # Keycloak config (if enabled)
<modelName>-jsl2psm.model               # Transformation trace
<modelName>-psm2asm.model               # Transformation trace
<modelName>-asm2rdbms_<dialect>.model    # Transformation trace
```

### judo-tatami-jsl-workflow-maven-plugin-test

**Purpose:** Integration test module that validates the Maven plugin using a simple JSL model with `Person`, `User`, and `UserActor` declarations.

## Technology Stack

### Core Technologies
- **Epsilon Runtime** 2.8.0 - ETL/EOL model transformation engine
- **Eclipse Modeling Framework (EMF)** - Metamodel foundation (ecore 2.38.0, common 2.40.0, xmi 2.38.0)
- **Apache Felix** 5.1.8 - OSGi bundle plugin
- **Guava** 30.0-jre - Utility library

### Testing
- **JUnit Jupiter** 5.5.1 - Test framework
- **Hamcrest** 2.2 - Assertion library
- **Mockito** 4.8.0 - Mocking framework

### Build & Quality
- **Maven 3.9.4+** with wrapper (mvnw)
- **JaCoCo** 0.8.12 - Code coverage
- **Lombok** 1.18.34 - Annotation processing
- **SonarQube** integration (sonar-maven-plugin 3.9.1.2184)
- **Surefire** 3.5.1 - Test execution

## Key Dependencies

```xml
<!-- Epsilon Runtime -->
<epsilon-runtime-version>2.8.0.20251022_112123_14c440b1_develop</epsilon-runtime-version>

<!-- Model Versions -->
<judo-meta-jsl-version>1.0.3.20260220_043802_c482d4d4_develop</judo-meta-jsl-version>
<judo-meta-psm-version>1.3.0.20260220_043801_3057adf9_develop</judo-meta-psm-version>
<judo-meta-asm-version>1.1.4.20260202_143734_65499fd0_develop</judo-meta-asm-version>
<judo-meta-ui-version>1.1.0.20260303_104354_d8775168_develop</judo-meta-ui-version>
<judo-meta-rdbms-version>1.0.2.20260202_144825_fd632c1e_develop</judo-meta-rdbms-version>
<judo-meta-liquibase-version>1.0.2.20260202_143730_43d18c39_develop</judo-meta-liquibase-version>
<judo-meta-expression-version>1.0.5.20260202_145435_0ec02f4a_develop</judo-meta-expression-version>
<judo-meta-jql-version>1.0.4.20260202_145048_84ff3d22_develop</judo-meta-jql-version>

<!-- Tatami Framework -->
<judo-tatami-base-version>1.1.6.20260220_044537_c328291c_develop</judo-tatami-base-version>
<judo-tatami-core-version>1.1.4.20260202_143500_4fe70cce_develop</judo-tatami-core-version>
<judo-tatami-util-version>1.0.0.20260202_143554_ef77c3c7_develop</judo-tatami-util-version>

<!-- Runtime -->
<judo-runtime-core-version>1.0.6.20260220_044935_c04234c8_develop</judo-runtime-core-version>
<judo-sdk-common-version>1.0.4.20260202_144500_9ef79c87_develop</judo-sdk-common-version>
```

## Build Commands

```bash
# Standard build (all modules)
mvn clean install
# or with wrapper
./mvnw clean install

# Build with modules profile (default, active unless -DskipModules)
mvn clean install -Pmodules

# Run tests only
mvn clean test

# Skip tests
mvn clean install -DskipTests

# Run specific module tests
mvn clean test -pl judo-tatami-jsl-jsl2psm
mvn clean test -pl judo-tatami-jsl-jsl2ui
```

## Test Structure

### JSL2PSM Tests (33 files)
Organized by domain in `src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/`:
- `entity/` - Entity type transformations
- `type/` - Primitive types (string, numeric, boolean, date, time, timestamp, binary, enum)
- `transferobject/` - Mapped, unmapped, and default transfer objects
- `derived/` - Derived properties, expressions, relations, parameters
- `operation/` - Actions, CRUD behaviours
- `actor/` - Actor types, anonymous actors
- `namespace/` - Model and package creation
- `error/` - Error declarations
- `functions/` - Instance function translation

Base test class: `AbstractTest.java` provides JSL/PSM model loading, transformation execution, trace handling, and helper assertions.

### JSL2UI Tests (11 files)
In `src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/`:
- `JslModel2UiApplicationTest.java` - Actor, menu, security, profile pages
- `JslModel2UiCRUDTest.java` - CRUD operations
- `JslModel2UiNavigationTest.java` - Navigation and dialogs
- `JslModel2UiOperationsTest.java` - Operations and actions
- `JslModel2UiWidgetsTest.java` - Widget generation
- `JslModel2UiDataTest.java` - Data types and relations
- `JslModel2UiRowOperationsTest.java` - Row-level operations
- `JslModel2UiActionGroupsTest.java` - Action groups
- `JSLModel2UiListItemTest.java` - List items, tags, cards
- `JslModel2UiCarTest.java` - Car domain test
- `Jsl2UiWorkTest.java` - Workflow integration

Tests use `JslParser.getModelFromStrings()` to create JSL models from inline DSL strings.

### Workflow Tests
- `JslDefaultWorkflowTest.java` - End-to-end pipeline test

## Code Patterns

### Transformation Execution

```java
// Execute JSL to PSM transformation
Jsl2PsmTransformationTrace trace = executeJsl2PsmTransformation(
    Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter()
        .jslModel(jslModel)
        .psmModel(psmModel)
        .createTrace(true)
        .parallel(true)
        .generateBehaviours(true)
);
```

### Work Class Pattern

```java
// Jsl2PsmWork extends AbstractTransformationWork
Jsl2PsmWork work = new Jsl2PsmWork(TransformationContext.transformationContext()
    .put(jslModel)
    .put(psmModel));
work.execute();
```

### Expression Translation (JSL to JQL)

```java
// Convert JSL expression to JQL
JslExpressionToJqlExpression converter = new JslExpressionToJqlExpression();
String jqlExpression = converter.convert(jslExpression, context);
```

### Full Workflow Execution

```java
DefaultWorkflow workflow = new DefaultWorkflow(
    DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters()
        .jslModel(jslModel)
        .modelName("MyModel")
        .dialectList(List.of("postgresql", "hsqldb"))
        .runInParallel(true)
        .enableMetrics(true)
);
workflow.startDefaultWorkflow();

// Access generated models
PsmModel psm = workflow.getTransformationContext().getByClass(PsmModel.class);
AsmModel asm = workflow.getTransformationContext().getByClass(AsmModel.class);
UiModel ui = workflow.getTransformationContext().getByClass(UiModel.class);
```

## Related Projects

- **judo-tatami-base (judo-tatami)** - Downstream transformation modules (PSM→ASM, ASM→RDBMS, etc.)
- **judo-meta-jsl** - JSL metamodel and parser
- **judo-meta-psm** - Platform Specific Model metamodel
- **judo-meta-asm** - Abstract Semantic Model metamodel
- **judo-meta-ui** - User Interface metamodel
- **judo-meta-rdbms** - RDBMS metamodel
- **judo-meta-liquibase** - Liquibase metamodel
- **judo-meta-expression** - Expression metamodel
- **judo-meta-measure** - Measure metamodel
- **judo-meta-keycloak** - Keycloak metamodel
- **judo-meta-jql** - JQL (Judo Query Language) metamodel
- **judo-runtime-core** - Runtime core (used in plugin test)
- **judo-community** - Parent aggregator project

## Maven Profiles

| Profile | Description | Activation |
|---------|-------------|------------|
| `modules` | Build all modules (default) | Active unless `-DskipModules` |
| `generate-checksum` | Generate artifact checksums | Active unless `-Dignore_checksum` |
| `sign-artifacts` | GPG sign artifacts | Manual |
| `release-dummy` | Release to local filesystem | Manual |
| `release-judong` | Release to JUDO Nexus | Manual |
| `release-central` | Release to Maven Central | Manual |

## OpenSpec Usage

This project uses OpenSpec for change management. See `openspec/AGENTS.md` for:
- Creating proposals
- Change workflow
- Spec format conventions

## Important Notes

1. **ETL files are the source of truth** for transformation logic in this project
2. **Dual transformation engines** - Both ETL and Zeta are supported. Select via `TransformationMode` (ETL, ZETA). Zeta rules are in `src/main/java/.../zeta/rules/` directories
3. **JSL is the source language** - Not ESM. JSL is a higher-level DSL that compiles to PSM
4. **OSGi compatibility** is maintained through Felix bundle plugin
5. **Transformation traces** allow mapping between source and target elements
6. **Multi-dialect RDBMS** support - generates per-dialect models (hsqldb, postgresql)
7. **Parallel execution** is the default mode for the workflow
8. **Expression translation** - JSL expressions are converted to JQL (Judo Query Language)
9. **Per-frontend UI generation** - The JSL2UI transformation runs separately for each `UIFrontendDeclaration`
