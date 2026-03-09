## ADDED Requirements

### Requirement: Jsl2PsmWork supports TransformationMode
`Jsl2PsmWork` and its `Jsl2PsmWorkParameter` SHALL accept a `TransformationMode` parameter. When set to `ZETA`, it SHALL execute the Zeta transformation. When set to `ETL`, it SHALL execute the ETL transformation. The default SHALL be `TransformationMode.ZETA`.

#### Scenario: Jsl2PsmWork defaults to Zeta
- **WHEN** `Jsl2PsmWork` is created without specifying a transformation mode
- **THEN** it SHALL use `TransformationMode.ZETA` (resolved via `TransformationMode.fromSystemProperty()`)

#### Scenario: Jsl2PsmWork uses ETL when configured
- **WHEN** `Jsl2PsmWork` is created with `transformationMode = TransformationMode.ETL`
- **THEN** it SHALL execute the Epsilon ETL transformation

#### Scenario: Jsl2PsmWork uses Zeta when configured
- **WHEN** `Jsl2PsmWork` is created with `transformationMode = TransformationMode.ZETA`
- **THEN** it SHALL execute the Zeta Java transformation

### Requirement: Jsl2UiWork supports TransformationMode
`Jsl2UiWork` and its `Jsl2UiWorkParameter` SHALL accept a `TransformationMode` parameter with the same behavior as `Jsl2PsmWork`. Default SHALL be `TransformationMode.ZETA`.

#### Scenario: Jsl2UiWork defaults to Zeta
- **WHEN** `Jsl2UiWork` is created without specifying a transformation mode
- **THEN** it SHALL use `TransformationMode.ZETA`

#### Scenario: Jsl2UiWork uses ETL when configured
- **WHEN** `Jsl2UiWork` is created with `transformationMode = TransformationMode.ETL`
- **THEN** it SHALL execute the Epsilon ETL transformation

### Requirement: DefaultWorkflowSetupParameters supports TransformationMode
`DefaultWorkflowSetupParameters` SHALL accept a `transformationMode` parameter that propagates to all Work classes in the workflow pipeline.

#### Scenario: Workflow propagates mode to jsl2psm
- **WHEN** the workflow is configured with `transformationMode = TransformationMode.ETL`
- **THEN** `Jsl2PsmWork` SHALL execute using ETL

#### Scenario: Workflow propagates mode to jsl2ui
- **WHEN** the workflow is configured with a specific TransformationMode
- **THEN** `Jsl2UiWork` SHALL execute using that mode

### Requirement: Maven plugin exposes transformationMode parameter
The `DefaultWorkflowMojo` SHALL accept a `transformationMode` configuration parameter (string: "ZETA" or "ETL"). Default SHALL be "ZETA".

#### Scenario: Maven plugin defaults to Zeta
- **WHEN** the Maven plugin is configured without specifying transformationMode
- **THEN** transformations SHALL run using the Zeta engine

#### Scenario: Maven plugin accepts ETL override
- **WHEN** the Maven plugin is configured with `<transformationMode>ETL</transformationMode>`
- **THEN** transformations SHALL run using the ETL engine

### Requirement: TransformationMode is resolvable from system property
The system SHALL support `-Djudo.transformation.mode=ETL` system property to override the transformation mode globally. This is provided by `TransformationMode.fromSystemProperty()` in tatami-core.

#### Scenario: System property overrides default
- **WHEN** the JVM is started with `-Djudo.transformation.mode=ETL`
- **THEN** all Work classes using `TransformationMode.fromSystemProperty()` SHALL default to ETL
