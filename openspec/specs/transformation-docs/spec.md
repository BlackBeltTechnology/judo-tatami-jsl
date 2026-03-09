## ADDED Requirements

### Requirement: All AsciiDoc files converted to Markdown
All `.adoc` files in the project SHALL be converted to equivalent `.md` files. The original `.adoc` files SHALL be removed after conversion.

#### Scenario: README.adoc converted to README.md
- **WHEN** the documentation conversion is complete
- **THEN** `README.adoc` SHALL be replaced by `README.md` with equivalent content

#### Scenario: CONTRIBUTING.adoc converted to CONTRIBUTING.md
- **WHEN** the documentation conversion is complete
- **THEN** `CONTRIBUTING.adoc` SHALL be replaced by `CONTRIBUTING.md`

#### Scenario: Maven plugin documentation converted
- **WHEN** the documentation conversion is complete
- **THEN** `docs/pages/judo-tatami-jsl-workflow-maven-plugin.adoc` SHALL be replaced by a `.md` equivalent

### Requirement: PlantUML diagrams converted to Mermaid
All 14 PlantUML diagrams in `judo-tatami-jsl-jsl2psm/diagram/` SHALL be converted to Mermaid syntax in Markdown files. Inline PlantUML in documentation SHALL also be converted to Mermaid.

#### Scenario: PlantUML class diagrams become Mermaid class diagrams
- **WHEN** a `.plantuml` file containing class diagrams is converted
- **THEN** the output `.md` file SHALL contain equivalent Mermaid class diagram syntax

#### Scenario: Inline PlantUML in docs converted
- **WHEN** the Maven plugin documentation contains inline PlantUML
- **THEN** it SHALL be replaced with equivalent inline Mermaid diagram

### Requirement: Transformation rules documented in docs/transformations/
The project SHALL contain a `docs/transformations/` directory with documentation for all transformation rules in both jsl2psm and jsl2ui.

#### Scenario: jsl2psm rules documented
- **WHEN** the documentation is complete
- **THEN** `docs/transformations/jsl2psm.md` SHALL list all ETL rules with their source type, target type, guard condition, and brief description

#### Scenario: jsl2ui rules documented
- **WHEN** the documentation is complete
- **THEN** `docs/transformations/jsl2ui.md` SHALL list all ETL rules with their source type, target type, guard condition, and brief description

### Requirement: Updated README with Zeta information
The project README SHALL document the dual-engine architecture, including how to select transformation mode and how to run performance tests.

#### Scenario: README mentions Zeta support
- **WHEN** a developer reads the README
- **THEN** they SHALL find instructions for configuring TransformationMode and running dual-engine tests
