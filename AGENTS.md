# JUDO Tatami JSL - Project Documentation

## Project Overview

**Repository:** BlackBeltTechnology/judo-tatami-jsl
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21
**Build System:** Maven 3.9.4 with Maven Wrapper (`./mvnw`)

1. **Model transformation engine** — converts JSL (JUDO Specification Language) source files into a chain of runtime models (PSM, ASM, UI, RDBMS, Liquibase, Expression, Keycloak)
2. **Built on Eclipse Epsilon** — uses ETL (Epsilon Transformation Language) and EOL (Epsilon Object Language) scripts for declarative model-to-model transformations
3. **Maven plugin integration** — packages the transformation pipeline as a Maven plugin (`default-model-workflow` goal) for build-time code generation
4. **Multi-dialect database support** — generates RDBMS schemas for PostgreSQL and HSQLDB
5. **Part of the JUDO ecosystem** — depends on judo-meta-* metamodels and judo-tatami-core workflow infrastructure

## Directory Structure

```
judo-tatami-jsl/
├── judo-tatami-jsl-jsl2psm/           # JSL → PSM transformation module
│   ├── src/main/java/                 #   Java orchestration (Jsl2Psm, expression translation)
│   └── src/main/epsilon/              #   Epsilon ETL/EOL transformation scripts
├── judo-tatami-jsl-jsl2ui/            # JSL → UI transformation module
│   ├── src/main/java/                 #   Java orchestration (Jsl2Ui)
│   └── src/main/epsilon/              #   Epsilon ETL/EOL transformation scripts
├── judo-tatami-jsl-workflow/          # Pipeline orchestration module
│   └── src/main/java/                 #   Workflow engine, configuration, metrics
├── judo-tatami-jsl-workflow-maven-plugin/      # Maven Mojo plugin
│   └── src/main/java/                 #   DefaultWorkflowMojo, artifact resolution
├── judo-tatami-jsl-workflow-maven-plugin-test/ # Plugin integration tests
├── docs/                              # Documentation (Markdown)
├── .github/workflows/                 # CI/CD pipelines (GitHub Actions)
├── pom.xml                            # Parent POM (all modules, dependency management)
└── logback-test.xml                   # Shared test logging configuration
```

## Core Modules

### Transformation Modules

| Module | Type | Purpose |
|--------|------|---------|
| `judo-tatami-jsl-jsl2psm/` | Library | Transforms JSL models to PSM using Epsilon ETL. Contains `Jsl2Psm.java` (orchestrator), `JslExpressionToJqlExpression.java` (expression translation), and ~60 EOL operation scripts in `src/main/epsilon/transformations/psm/`. |
| `judo-tatami-jsl-jsl2ui/` | Library | Transforms JSL models to UI models using Epsilon ETL. Contains `Jsl2Ui.java` (orchestrator) and ~40 EOL operation scripts in `src/main/epsilon/transformations/ui/`. Processes each `UIFrontendDeclaration` separately. |

### Orchestration Modules

| Module | Type | Purpose |
|--------|------|---------|
| `judo-tatami-jsl-workflow/` | Library | Core workflow orchestration. `AbstractTatamiPipelineWorkflow` configures and runs the full transformation pipeline (JSL→PSM→ASM→RDBMS→Liquibase, JSL→UI, etc.). `WorkflowHelper` is the factory. `DefaultWorkflowSetupParameters` holds all configuration via builder pattern. |
| `judo-tatami-jsl-workflow-maven-plugin/` | Maven Plugin | `DefaultWorkflowMojo` — Maven Mojo (`default-model-workflow` goal, default phase: COMPILE). Bridges Maven parameters to `DefaultWorkflowSetupParameters` and executes the workflow. |

### Test & Documentation Modules

| Module | Type | Purpose |
|--------|------|---------|
| `judo-tatami-jsl-workflow-maven-plugin-test/` | Integration Test | End-to-end tests for the Maven plugin |
| `docs/` | Documentation | Markdown documentation and Antora configuration |

## Technology Stack

### Core Technologies
- **Eclipse Epsilon 2.8.0** — ETL/EOL model transformation engine
- **Eclipse EMF** — Ecore metamodeling framework, XMI model serialization
- **ANTLR 3.2** — Parser generator (DSL parsing)
- **JUDO Metamodels** — judo-meta-jsl (1.0.3), judo-meta-psm (1.3.0), judo-meta-ui (1.1.0), judo-meta-asm (1.1.4), judo-meta-rdbms (1.0.2), judo-meta-jql (1.0.4), judo-meta-expression (1.0.5), judo-meta-liquibase (1.0.2)
- **judo-tatami-core** — Workflow and Work abstractions (sequential/parallel flow engine)
- **judo-tatami-base** — Base transformation utilities

### Build & Quality
- **Maven 3.9.4** with Flatten, Bundle (OSGi), and Build Helper plugins
- **JUnit 5** (5.5.1) via Maven Surefire 3.5.1
- **JaCoCo 0.8.12** for code coverage
- **SonarQube** integration via sonar-maven-plugin
- **Lombok 1.18.34** — `@Getter`, `@Builder`, `@Slf4j` used throughout
- **SLF4J 2.0.16 + Logback 1.5.12** for logging

## Build Commands

```bash
mvn clean install                                    # Full build (all modules)
mvn clean test                                       # Run all tests
mvn clean test -pl judo-tatami-jsl-jsl2psm           # Run tests for one module
mvn clean test -pl judo-tatami-jsl-jsl2psm -Dtest=ClassName  # Single test class
mvn clean test -pl judo-tatami-jsl-jsl2psm -Dtest=ClassName#methodName  # Single test method
mvn clean verify                                     # Full verification (tests + integration)
./mvnw clean install                                 # Using Maven wrapper
```

> **Note:** Surefire requires `--add-opens` JVM args for reflective access (configured in the parent POM). Tests use the shared `logback-test.xml` at the project root.

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates all submodules (active by default) |
| `generate-checksum` | Generates checksum artifacts for builds |
| `sign-artifacts` | GPG-signs artifacts for release |
| `release-dummy` | Dummy distribution management |
| `release-judong` | Deploy to judong Nexus repository |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generates documentation diagrams |
| `update-source-code-license` | Updates license headers in source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM: all module declarations, dependency management, plugin configuration, build profiles |
| `logback-test.xml` | Shared Logback configuration for all test modules |
| `.github/workflows/build.yml` | Main CI build pipeline (GitHub Actions) |
| `.github/workflows/release.yml` | Release automation workflow |
| `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/jslToPsm.etl` | Main JSL→PSM transformation entry point |
| `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui/jslToUi.etl` | Main JSL→UI transformation entry point |

## Development Environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+

**Recommended IDE settings:** See `.vscode/settings.json` and `.zed/settings.json` for pre-configured Java settings (auto-format disabled, auto-build disabled, Maven source download enabled).

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** `1.1.4-SNAPSHOT` (semantic versioning, GitFlow model)
- **Branch naming:** `feature/JNG-NUMBER_summary`, `bugfix/JNG-NUMBER_summary`, `release/X.Y.Z`
- **Commit policy:** Every commit must reference a JIRA ticket (e.g., `JNG-6337`)
- **CI/CD:** GitHub Actions — build on push to `develop`, deploy on `release/*` branches
- **Release:** Automated via `release.yml` workflow; creates PRs to both `master` and `develop`

## Important Notes

1. **Epsilon scripts are the core transformation logic.** Most feature changes involve editing `.etl` and `.eol` files under `src/main/epsilon/transformations/`, not Java code.
2. **The pipeline runs transformations in parallel by default** (`runInParallel=true`). Individual steps can be toggled on/off with `ignore*` flags.
3. **Transformation traces** map source elements to target elements. They are optional (toggleable) and stored in XMI format.
4. **All naming conventions for generated RDBMS artifacts** (table prefixes, column prefixes, etc.) are configurable via `DefaultWorkflowSetupParameters` or Maven plugin parameters.
5. **OSGi support** is included via Maven Bundle Plugin — each module produces an OSGi bundle. The `jsl2psm` and `jsl2ui` modules include OSGi service tracker classes.
6. **The `judo-tatami-jsl-workflow-maven-plugin-test` module** contains the integration tests that exercise the full pipeline end-to-end.

## Related Documentation

- [Workflow Maven Plugin Reference](docs/pages/judo-tatami-jsl-workflow-maven-plugin.md) — full parameter reference and usage examples
- [Contributing Guide](CONTRIBUTING.md) — development setup and submission guidelines
- [CI/CD Flow](/.github/CIFLOW.md) — branching strategy and GitHub Actions workflow diagrams
- [JUDO Community](https://github.com/BlackBeltTechnology/judo-community) — parent ecosystem documentation
