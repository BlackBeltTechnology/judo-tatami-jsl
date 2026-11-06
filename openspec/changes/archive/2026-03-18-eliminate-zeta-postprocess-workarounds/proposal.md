## Why

The Zeta JSL2PSM transformation has post-processing workarounds in `Jsl2PsmZetaTransformation.postProcess()` that don't correspond to ETL's `@post` block, plus two ETL `@post` operations that are missing entirely. This creates maintenance burden, unclear ownership of logic, and STRICT dual-test ID mismatches. The goal is to use Zeta's `@PostExecution` hooks to move post-processing into the rule classes where the logic belongs, add the missing ETL `@post` operations, and ultimately empty out the centralized `postProcess()` method.

## What Changes

- **Move `setAssociationPartners()` to `@PostExecution` in `AssociationRules.java`**: This is a Zeta-specific workaround (ETL handles partner assignment inline). Move it from the central `postProcess()` into the rule class that owns association logic.
- **Move `setEnumerationOrdinals()` to `@PostExecution` in `QueryCustomizerRules.java`**: This mirrors ETL's `@post` block. Move it from `postProcess()` into the rule class that creates ordering enumerations.
- **Add Cardinality ID normalization via `@PostExecution`**: ETL's `@post` block overwrites ALL Cardinality IDs to `<container-id>/cardinality`. This is completely missing in Zeta, causing ~94 ID mismatches in STRICT comparison.
- **Add primitive type materialization via `@PostExecution`**: ETL's `@post` forces lazy primitive type creation for types only referenced through `NavigationBaseDeclarationReference`. Partially missing in Zeta.
- **Empty `postProcess()` in `Jsl2PsmZetaTransformation.java`**: After moving all logic to `@PostExecution` hooks.
- **Add targeted STRICT dual tests** for each gap to reproduce and verify fixes.

## Capabilities

### New Capabilities

- `zeta-postexecution-hooks`: @PostExecution lifecycle hooks in Zeta JSL2PSM rule classes for cardinality ID normalization, primitive type materialization, association partner resolution, and enumeration ordinal assignment
- `zeta-cardinality-ids`: Cardinality XMI ID normalization matching ETL @post behavior
- `zeta-primitive-materialization`: Force lazy primitive type creation for types only referenced via NavigationBaseDeclarationReference

### Modified Capabilities

- `zeta-jsl2psm`: postProcess() emptied; all post-processing moved to @PostExecution hooks in rule classes

## Impact

- `Jsl2PsmZetaTransformation.java` — `postProcess()` emptied, methods moved out
- `AssociationRules.java` — gains `@PostExecution` method for partner assignment
- `QueryCustomizerRules.java` — gains `@PostExecution` method for enumeration ordinals
- `TypeRules.java` or new class — gains `@PostExecution` method for primitive materialization
- New class or existing rule — gains `@PostExecution` method for cardinality ID normalization
- `Jsl2PsmDualTransformationTest.java` — new STRICT test cases for cardinality IDs, nav-ref primitives, association partners
