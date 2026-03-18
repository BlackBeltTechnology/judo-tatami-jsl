## Context

The JSL2UI transformation converts JSL (JUDO Specification Language) models into UI models. It supports two engines: ETL (Epsilon Transformation Language) and Zeta (Java-based rules). The Zeta engine was implemented to replace ETL for faster execution and simpler deployment, but 22 test parameterizations across 10 test files are still restricted to ETL-only mode due to 3 categories of gaps in the Zeta rules.

Key files:
- Zeta rules: `judo-tatami-jsl-jsl2ui/src/main/java/.../zeta/rules/`
- ETL rules: `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui/modules/`
- Helper: `judo-tatami-jsl-jsl2ui/src/main/java/.../zeta/Jsl2UiHelper.java`
- Base test: `judo-tatami-jsl-jsl2ui/src/test/java/.../AbstractTest.java`

## Goals / Non-Goals

**Goals:**
- Achieve ZETA parity with ETL for all 22 currently ETL-only test parameterizations
- Fix 3 distinct validation error categories in the Zeta JSL2UI transformation
- Enable `@EnumSource(names = {"ETL", "ZETA"})` on all affected tests

**Non-Goals:**
- Modifying the ETL transformation path
- Adding new transformation capabilities beyond what ETL already produces
- Addressing the `@Disabled` test (`testActorWithMenuLink`) — that's a JSL parser issue, not a Zeta gap
- Full pipeline comparison testing (that's `zeta-only-runtime` V1 scope)

## Decisions

### 1. Fix Claim.attributeType by calling existing helper

The `Jsl2UiHelper.getTransferFieldAttributeType()` method already exists and resolves the correct `AttributeType` from a `TransferFieldDeclaration`. The ETL rule does: `t.attributeType = s.eContainer.getIdentity().field.getTransferFieldDeclarationEquivalent()`. The Zeta fix mirrors this by calling the helper after the identity's field has been resolved.

**Alternative considered**: Creating the AttributeType inline — rejected because the helper already exists and follows DRY.

### 2. Fix Table.columns by tracing the ETL RowDeclaration → Column mapping

The ETL path creates Column objects inside Tables via row declaration EOL operations. The Zeta rules create Tables but skip column population. The fix adds column creation logic to the existing Zeta row/table rules, following the same pattern used in ETL's `rowDeclaration.eol` and `columnDeclaration.eol`.

**Alternative considered**: Post-processing pass to populate columns — rejected because it would diverge from the rule-by-rule ETL parity approach.

### 3. Fix Action.actionDefinition via resource registration and missing rules

Two sub-fixes:
- **Dangling references**: `ParameterlessCallOperationActionDefinition` objects are created but not added to the model resource via `ctx.addToResource()`. Adding this call fixes the dangling cross-reference.
- **Missing ActionDefinitions**: Filter, autocomplete, and tags action types need ActionDefinition creation rules that don't exist yet in Zeta. These will be added following the ETL patterns in `viewTableDeclarationAction.eol` and related files.

### 4. TDD approach — enable ZETA in test annotations first

Per project conventions (AGENTS.md: "Use TDD"), for each gap:
1. Change `@EnumSource(names = {"ETL"})` to `{"ETL", "ZETA"}` on affected tests
2. Verify tests fail in ZETA mode
3. Implement the fix
4. Verify tests pass in both modes

## Risks / Trade-offs

- **[Risk] Zeta output may differ subtly from ETL beyond validation** → Mitigation: The dual test approach runs the same assertions against both engines, catching structural differences
- **[Risk] Fixing one gap may expose additional assertion failures** → Mitigation: Fix validation errors first (model.isValid()), then address any assertion mismatches incrementally
- **[Risk] Column ordering in Zeta may differ from ETL** → Mitigation: The Zeta transformation already has position post-processing logic; verify column positions match ETL output
