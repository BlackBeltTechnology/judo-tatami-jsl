# judo-tatami-jsl — module agent doctrine

## Module purpose

`judo-tatami-jsl` is the **JSL front end** of the Tatami transformation layer.
It is the only place in the estate that takes `.jsl` textual DSL sources — the
declarative language a JUDO application author actually writes — parsed into a
`JslDslModel` by `judo-meta-jsl`, and lowers them into the platform-specific
model the rest of the toolchain assumes exists. Two lowerings are owned here and
nowhere else:

- **JSL → PSM** (`judo-tatami-jsl-jsl2psm`) — entities, relations, queries,
  derived members, actors, transfer objects, and the whole generated *behaviour*
  surface (create / update / delete / list / range / reference-set / template /
  upload-token operations) materialised as a `PsmModel`.
- **JSL → UI** (`judo-tatami-jsl-jsl2ui`) — each `UIFrontendDeclaration` in the
  JSL source turned into its own UI model, processed declaration by declaration.

Everything downstream of PSM is *not* implemented here: `judo-tatami-jsl-workflow`
orchestrates it by calling into the sibling `judo-tatami` repository for
PSM→ASM, PSM→Measure, ASM→Expression, ASM→RDBMS (once per SQL dialect) and
RDBMS→Liquibase, plus ASM→Keycloak. So the chain this module drives end to end is
`JSL → PSM → {ASM → {Expression, RDBMS → Liquibase, Keycloak}, Measure}` and, in
parallel, `JSL → UI`.

**How this differs from `judo-tatami`.** `judo-tatami` is the *ESM*-fronted path:
its entry transformation is `esm2psm`, consuming the ESM (Extended Structure
Model) that the graphical modelling tooling produces. `judo-tatami-jsl` consumes
textual JSL instead. The two are alternative front doors that converge on the
same PSM waist and therefore reuse the same downstream transformations — this
repository re-declares neither of them. `judo-tatami-core` is a third thing
again: the flow engine (`Work`, `WorkFlow`, `WorkFlowEngine`,
`TransformationContext`) that both front ends schedule their steps on. Neither
of those two repositories can read a `.jsl` file; only this one can.

**Dual transformation engines.** Each of `Jsl2PsmWork` and `Jsl2UiWork` can run
its step through the Epsilon path (`EtlExecutionContext` over the `.etl`/`.eol`
scripts under `src/main/epsilon/`) or the Zeta path
(`Jsl2PsmZetaTransformation` / `Jsl2UiZetaTransformation`, annotated Java rule
classes under `…/zeta/rules/`, executed by `judo-zeta`'s
`TransformationExecutor` + `TransformationRegistry`), or both — the mode enum
lives in `judo-tatami-core` (`ETL` / `ZETA` / `BOTH`) and is queried as
`mode.isZeta()`. A change to transformation semantics generally has to land on
both sides until the Zeta migration completes.

**Repository:** BlackBeltTechnology/judo-tatami-jsl ·
**Reactor artifact:** `judo-tatami-jsl-parent` (packaging `pom`, version
`1.1.4-SNAPSHOT`) · **License:** EPL-2.0 · **Java:** 21 ·
**Build:** Maven 3.9.4 with wrapper (`./mvnw`).

## Reactor map

The root `pom.xml` declares its `<modules>` inside the `modules` profile, which
activates whenever `skipModules` is not `true`; `-DskipModules=true` builds the
parent POM alone. Declared order is build order.

| Module | Artifact / packaging | What it contributes |
|---|---|---|
| `docs` | `judo-tatami-jsl-docs`, `jar` | The Antora/Markdown documentation set under `docs/pages/` packaged as an artifact so downstream doc sites can pull it. Notably carries the Workflow Maven Plugin parameter reference — the authoritative description of every Mojo parameter. No transformation logic. |
| `judo-tatami-jsl-jsl2ui` | `judo-tatami-jsl-jsl2ui`, `bundle` (OSGi) | JSL → UI. `Jsl2Ui` orchestrates the Epsilon route over `src/main/epsilon/transformations/ui/jslToUi.etl` plus ~40 EOL operation scripts; `zeta/Jsl2UiZetaTransformation` plus `zeta/rules/{application,structure,type,view}` carry the Java route. `Jsl2UiWork` adapts it to the flow engine, `Jsl2UiTransformationTrace` records source→target links, and `osgi/` registers the transformation as an OSGi service. Each `UIFrontendDeclaration` is transformed separately. |
| `judo-tatami-jsl-jsl2psm` | `judo-tatami-jsl-jsl2psm`, `bundle` (OSGi) | JSL → PSM, the heaviest module. `Jsl2Psm` drives `src/main/epsilon/transformations/psm/jslToPsm.etl` and ~60 EOL operation scripts; `zeta/Jsl2PsmZetaTransformation` drives the Java rule classes under `zeta/rules/{action,actor,data,derived,namespace,structure,type}`. `JslExpressionToJqlExpression` and `Jsl2JqlFunction` translate JSL expressions into JQL. `Jsl2PsmWork` wires it into the flow engine; `osgi/` publishes the OSGi service. |
| `judo-tatami-jsl-workflow` | `judo-tatami-jsl-workflow`, `bundle` (OSGi) | Pipeline orchestration. `AbstractTatamiPipelineWorkflow` assembles the whole chain — JSL→PSM and JSL→UI from this repository, PSM→ASM / PSM→Measure / ASM→Expression / ASM→RDBMS / RDBMS→Liquibase / ASM→Keycloak from `judo-tatami` — into a `WorkFlow` on `judo-tatami-core`'s engine, honouring the `ignore*` toggles and per-dialect fan-out. `WorkflowHelper` is the work factory, `DefaultWorkflowSetupParameters` the builder-shaped configuration record, `DefaultWorkflow` / `JslDefaultWorkflow` / `PsmDefaultWorkflow` the ready-made entry points, `DefaultWorkflowSave` the model persistence step, and `DefaultWorkflowMetricsCollector` / `WorkflowMetrics` the timing instrumentation. |
| `judo-tatami-jsl-workflow-maven-plugin` | `judo-tatami-jsl-workflow-maven-plugin`, `maven-plugin` | The build-time face of the pipeline. `DefaultWorkflowMojo` binds goal `default-model-workflow` to the `COMPILE` phase and maps Maven parameters onto `DefaultWorkflowSetupParameters`; `AbstractJslDslWorkflowProjectMojo` holds the shared project plumbing, `ArtifactResolver` locates model artifacts on the dependency graph, `DialectParam` carries the requested SQL dialects, `ResourceList` enumerates the JSL sources to feed in. |
| `judo-tatami-jsl-workflow-maven-plugin-test` | `judo-tatami-jsl-workflow-maven-plugin-test`, `jar` | The end-to-end proof: runs the plugin against a real JSL model and asserts the full pipeline produces the expected artifacts. This is where a regression spanning several transformations actually surfaces. |

## Repository layout

```
judo-tatami-jsl/
├── judo-tatami-jsl-jsl2psm/           # JSL → PSM transformation module
│   ├── src/main/java/                 #   Java orchestration + zeta/ rule classes
│   └── src/main/epsilon/              #   Epsilon ETL/EOL transformation scripts
├── judo-tatami-jsl-jsl2ui/            # JSL → UI transformation module
│   ├── src/main/java/                 #   Java orchestration + zeta/ rule classes
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

The two files at the reactor root carry more weight than their size suggests.
`pom.xml` is the parent POM: it declares every module, the dependency
management that pins the whole JUDO metamodel set, plugin configuration, and
every build profile listed below. `logback-test.xml` is the single Logback
configuration every test module shares — change it and every module's test
output changes with it.

## Build commands

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

### Maven profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates all submodules (active by default; disable with `-DskipModules=true`) |
| `performance` | Inverts the Surefire group filter to run *only* the `performance`-tagged tests, which the default build excludes |
| `generate-checksum` | Generates checksum artifacts for builds (active unless `-Dignore_checksum=true`) |
| `sign-artifacts` | GPG-signs artifacts for release |
| `release-dummy` | Dummy distribution management |
| `release-judong` | Deploy to judong Nexus repository |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generates documentation diagrams |
| `update-source-code-license` | Updates license headers in source files |

## Technology stack

### Core technologies
- **Eclipse Epsilon 2.8.0** — ETL/EOL model transformation engine
- **Eclipse EMF** — Ecore metamodeling framework, XMI model serialization
- **ANTLR 3.2** — Parser generator (DSL parsing)
- **JUDO Metamodels** — judo-meta-jsl (1.0.3), judo-meta-psm (1.3.0), judo-meta-ui (1.1.0), judo-meta-asm (1.1.4), judo-meta-rdbms (1.0.2), judo-meta-jql (1.0.4), judo-meta-expression (1.0.5), judo-meta-liquibase (1.0.2)
- **judo-tatami-core** — Workflow and Work abstractions (sequential/parallel flow engine), plus the `ETL` / `ZETA` / `BOTH` transformation-mode selector
- **judo-tatami-base** — Base transformation utilities
- **judo-zeta** — `TransformationExecutor`, `TransformationRegistry`, `ModelProvider`, `ExtensionMethodRegistry` behind the annotated-Java rule route

### Build & quality
- **Maven 3.9.4** with Flatten, Bundle (OSGi), and Build Helper plugins
- **JUnit 5** (5.5.1) via Maven Surefire 3.5.1
- **JaCoCo 0.8.12** for code coverage
- **SonarQube** integration via sonar-maven-plugin
- **Lombok 1.18.34** — `@Getter`, `@Builder`, `@Slf4j` used throughout
- **SLF4J 2.0.16 + Logback 1.5.12** for logging

## Architecture pointers

- `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/jslToPsm.etl` — main JSL→PSM transformation entry point.
- `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui/jslToUi.etl` — main JSL→UI transformation entry point.
- `judo-tatami-jsl-jsl2psm/src/main/java/…/zeta/Jsl2PsmZetaTransformation.java` and the sibling `Jsl2UiZetaTransformation` — the Zeta-route counterparts of those two entry points.
- `judo-tatami-jsl-workflow/src/main/java/…/AbstractTatamiPipelineWorkflow.java` — the assembled pipeline; read it to see which steps exist and in what order.
- `.github/workflows/build.yml` — main CI build pipeline; `.github/workflows/release.yml` — release automation.

## Development environment

**Required:**
- Java 21 JDK
- Maven 3.9.4+

**Recommended IDE settings:** See `.vscode/settings.json` and `.zed/settings.json` for pre-configured Java settings (auto-format disabled, auto-build disabled, Maven source download enabled).

## Git workflow

- **Main Branch:** `develop`
- **Versioning:** `1.1.4-SNAPSHOT` (semantic versioning, GitFlow model)
- **Branch naming:** `feature/JNG-NUMBER_summary`, `bugfix/JNG-NUMBER_summary`, `release/X.Y.Z`
- **Commit policy:** Every commit must reference a JIRA ticket (e.g., `JNG-6337`)
- **CI/CD:** GitHub Actions — build on push to `develop`, deploy on `release/*` branches
- **Release:** Automated via `release.yml` workflow; creates PRs to both `master` and `develop`

## Important notes

1. **Epsilon scripts are the core transformation logic.** Most feature changes involve editing `.etl` and `.eol` files under `src/main/epsilon/transformations/`, not Java code.
2. **The pipeline runs transformations in parallel by default** (`runInParallel=true`). Individual steps can be toggled on/off with `ignore*` flags.
3. **Transformation traces** map source elements to target elements. They are optional (toggleable) and stored in XMI format.
4. **All naming conventions for generated RDBMS artifacts** (table prefixes, column prefixes, etc.) are configurable via `DefaultWorkflowSetupParameters` or Maven plugin parameters.
5. **OSGi support** is included via Maven Bundle Plugin — each module produces an OSGi bundle. The `jsl2psm` and `jsl2ui` modules include OSGi service tracker classes.
6. **The `judo-tatami-jsl-workflow-maven-plugin-test` module** contains the integration tests that exercise the full pipeline end-to-end.

## Code instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Related documentation

- [Workflow Maven Plugin Reference](docs/pages/judo-tatami-jsl-workflow-maven-plugin.md) — full parameter reference and usage examples
- [Contributing Guide](CONTRIBUTING.md) — development setup and submission guidelines
- [CI/CD Flow](/.github/CIFLOW.md) — branching strategy and GitHub Actions workflow diagrams
- [JUDO Community](https://github.com/BlackBeltTechnology/judo-community) — parent ecosystem documentation

<!-- dox-doctrine -->
## Documentation Update Protocol (WRITE discipline)

Per-directory `AGENTS.md` files form a tree. Each directory `AGENTS.md` is the
per-file record for the files in that directory. This module-root `AGENTS.md`
holds doctrine + architecture pointers only — never a per-file index.

**Keep the root lean.** This file loads into every agent turn — every byte costs
tokens on every turn. A verbose root file buries the rules the model must follow
(signal dilution) and measurably degrades adherence; a lean file keeps doctrine
salient. Default assumption: your update does NOT belong in the root — route it
by the table below.

**Route every doc update by kind:**

| Kind of update | Goes in |
|---|---|
| New file in a directory, or its per-file detail / change history | Nearest directory `AGENTS.md`. Add a `` | `<basename>` | <purpose> | `` row, path-alphabetical. |
| Data flow, protocol, architecture rationale | `docs/architecture.md` or a `docs/<topic>.md` |
| End-user / developer setup | `README.md` |
| Cross-cutting rule every agent needs every turn (rare) | this module-root `AGENTS.md` |

**Read before editing (chain walk).** Before editing a file, read the nearest
`AGENTS.md` chain root→leaf so you know the file's recorded purpose, contracts,
and change history. Do not edit blind.

**Update after editing (closeout pass).** After changing a file, update its row
in the nearest directory `AGENTS.md`: find the file's row, update its purpose in
place; if absent, add it in path-alphabetical order. New directory → scaffold
its `AGENTS.md`. One row per file. The purpose carries a one-line summary, key
exported symbols, contracts/invariants, and `See change: <id>` history.

**Row style (caveman).** Short declarative fragments. Drop articles. Subject →
verb → object, present tense. One fact per row. Prefer concrete tokens (paths,
symbols, env vars) over prose. Keep identifiers verbatim.

**Size rule — split an over-large directory `AGENTS.md` file-based.** pi
auto-injects a directory `AGENTS.md` on every turn when cwd sits at/below it, so
an over-large directory `AGENTS.md` is not supported. Split it file-based: a row
exceeding the length threshold promotes to a per-file `<File>.AGENTS.md`
sidecar carrying that file's full detail (including every `See change:`). The
sidecar is pull-only — its name is not `AGENTS.md`, so pi never auto-injects it
— yet it stays search-indexed (`agents` doc_type). The directory `AGENTS.md`
keeps a one-line summary plus a `→ see `<File>.AGENTS.md`` pointer. Rows within
the threshold stay verbatim (lossless).

## Finding docs (READ discipline)

`kb_*` tools are faster and cheaper than raw search — they return a one-line
purpose + key exports per file, not raw bytes. **This fires on the ACTION, not
the intent** — before you `grep`/`rg` for a symbol, `cat`/read a file to learn
what it does, or chase an import, the kb call goes first. It fires **even
mid-task when you already know the file**; knowing the file does not exempt you.
When your reflex is the left column, run the right column instead:

| You're about to… | Do this FIRST instead |
|---|---|
| `grep -rn "SymbolName" src/` — find where a fn / type / const lives | `kb_search --doc-type agents "SymbolName"` — tree indexes key exports per file |
| `grep -rn "feature\|topic" src/` — how does X work / where's X handled | `kb_search "feature topic"` |
| `cat` / read a file just to learn its purpose before editing | `kb agents <path>` — one-line purpose + exports + change history |
| chase imports / callers across files | `kb_neighbors <path\|heading>` |
| read one doc section in full | `kb_get <path> <section>` |

**Fall-through (explicit):** if the kb call returns nothing relevant, `rg` /
source read is allowed — then add the missing directory `AGENTS.md` row per the
WRITE discipline. kb does NOT replace grep; it goes first.
