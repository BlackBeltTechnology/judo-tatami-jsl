## ADDED Requirements

### Requirement: Post-process enumeration member ordinals
After Zeta transformation completes, `Jsl2PsmZetaTransformation.postProcess()` SHALL assign sequential ordinals (starting from 0) to all `EnumerationMember` instances that have `ordinal == -1`, grouped by their parent `EnumerationType`.

#### Scenario: QueryCustomizer ordering enumeration gets ordinals assigned
- **WHEN** Zeta transformation produces an EnumerationType with members having `ordinal == -1`
- **THEN** post-processing assigns ordinals 0, 1, 2, ... in member order

#### Scenario: Regular enumeration ordinals are preserved
- **WHEN** an EnumerationType has members with ordinals already set (not -1)
- **THEN** post-processing does not modify those ordinals
