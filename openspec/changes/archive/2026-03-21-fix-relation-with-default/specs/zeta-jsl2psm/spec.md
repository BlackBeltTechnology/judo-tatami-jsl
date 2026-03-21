## MODIFIED Requirements

### Requirement: Zeta JSL2PSM transformation produces equivalent PSM output

#### Scenario: Inherited relation default values are resolved on cloned TransferObjectRelation
- **WHEN** a JSL entity extends another entity that declares a relation with a `default:` modifier
- **AND** the ZETA transformation clones that relation for the extended entity's default transfer object
- **THEN** the cloned `TransferObjectRelation.defaultValue` SHALL reference the same `NavigationProperty` as the ETL output
- **AND** the `itest-RelationWithDefaults` external model SHALL produce STRICT-equivalent output between ETL and ZETA
