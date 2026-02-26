# jsl2ui Specification

## Purpose

Transforms JSL (JUDO Specification Language) models into UI (User Interface) models using Eclipse Epsilon ETL scripts. This module produces the frontend metadata describing views, widgets, navigation, data bindings, and action definitions used by UI code generators.

## Architecture

The module contains two layers:

- **Java orchestration layer:** `Jsl2Ui` iterates over `UIFrontendDeclaration` elements in the JSL model and executes the `jslToUi.etl` script once per frontend declaration. `Jsl2UiWork` wraps it as a workflow work unit.
- **Epsilon script layer:** `jslToUi.etl` is the main entry point, supported by ~40 EOL operation files organized into `operations/jsl/actor/`, `operations/jsl/data/`, `operations/jsl/namespace/`, `operations/jsl/ui/` (views, widgets, menus, navigation, action groups, rows, cards, tags, etc.).

Key relationships:
- `Jsl2Ui.Jsl2UiParameter` (builder) → configures JSL model, UI model, trace/parallel options
- `Jsl2UiTransformationTrace` → maps JSL source to UI target elements
- `Jsl2UiWork` → integrates with `judo-tatami-core` WorkFlow engine
- OSGi layer: `Jsl2UiTransformationService` + `Jsl2UiTransformationJslModelTracker`

## Requirements

### Requirement: Per-frontend transformation

The module SHALL process each `UIFrontendDeclaration` in the JSL model independently, executing the ETL transformation once per frontend.

#### Scenario: Multiple frontends
- **GIVEN** a JSL model containing two `UIFrontendDeclaration` elements
- **WHEN** `Jsl2Ui.executeJsl2UiTransformation()` is called
- **THEN** the `jslToUi.etl` script SHALL be executed twice, once for each frontend declaration, and the resulting UI elements SHALL all be committed to the target UI model

### Requirement: Actor and access declaration mapping

The module SHALL extract the `ActorDeclaration` associated with each frontend and inject it into the Epsilon context as a transformation input.

#### Scenario: Actor-based UI generation
- **GIVEN** a JSL frontend declaration associated with an actor that has access declarations
- **WHEN** the transformation processes this frontend
- **THEN** the UI model SHALL contain application-level elements (menus, navigation) corresponding to the actor's access declarations

### Requirement: View and widget generation

The module SHALL generate UI views, panels, groups, tabs, links, widgets, and action definitions from JSL view declarations.

#### Scenario: View with fields and relations
- **GIVEN** a JSL view declaration containing field widgets and relation links
- **WHEN** the transformation is executed
- **THEN** the UI model SHALL contain corresponding `View` elements with `Widget` components and `Link` navigation elements

### Requirement: Menu structure generation

The module SHALL generate menu structures (menu tables, menu groups, menu links) from JSL menu declarations.

#### Scenario: Actor with menu items
- **GIVEN** a JSL actor declaration with menu table and menu link declarations
- **WHEN** the transformation is executed
- **THEN** the UI model SHALL contain a navigable menu tree with table views and link targets

### Requirement: Action group generation

The module SHALL generate action groups from JSL `actionGroup` declarations.

#### Scenario: Action group on a view
- **GIVEN** a JSL view declaration containing an action group with multiple actions
- **WHEN** the transformation is executed
- **THEN** the UI model SHALL contain an action group element with the correct child action definitions

### Requirement: Row and card generation

The module SHALL generate row layouts (with columns) and card components from JSL row and card declarations.

#### Scenario: Row with columns
- **GIVEN** a JSL view containing a row declaration with column children
- **WHEN** the transformation is executed
- **THEN** the UI model SHALL contain a row layout element with properly ordered column components

### Requirement: Parallel execution support

The module SHALL support parallel Epsilon execution when `parallel` is `true` (default).

#### Scenario: Parallel execution
- **GIVEN** `parallel=true` in the parameter configuration
- **WHEN** the transformation is executed
- **THEN** the Epsilon execution context SHALL be configured for parallel rule evaluation
