# Spec Delta

## ADDED Requirements

### Requirement: Query model stage parameter

The `default-model-workflow` goal SHALL accept an `ignoreAsm2Query` parameter (property `ignoreAsm2Query`, default
`false`) and map it onto `DefaultWorkflowSetupParameters.ignoreAsm2Query`.

#### Scenario: Default build writes the query model
- **GIVEN** no `<ignoreAsm2Query>` configuration
- **WHEN** the plugin completes execution
- **THEN** the destination directory SHALL contain `<name>-query.model` next to `<name>-asm.model`

#### Scenario: Opt out
- **GIVEN** `<ignoreAsm2Query>true</ignoreAsm2Query>` in plugin configuration
- **WHEN** the plugin completes execution
- **THEN** no `<name>-query.model` SHALL exist in the destination directory

#### Scenario: Expression stage disabled
- **GIVEN** `<ignoreAsm2Expression>true</ignoreAsm2Expression>` and the default `ignoreAsm2Query`
- **WHEN** the plugin executes
- **THEN** the build SHALL succeed, no query model SHALL be written, and a warning SHALL report the missing expression model
