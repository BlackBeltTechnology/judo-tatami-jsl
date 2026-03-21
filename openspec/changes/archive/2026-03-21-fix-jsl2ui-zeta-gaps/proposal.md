## Why

The JSL2UI Zeta transformation has 3 categories of gaps that cause 22 out of 22 ETL-only tests to fail when run in ZETA mode. These gaps block the `zeta-only-runtime` initiative (verification task V1: "Full pipeline comparison — all stages EQUIVALENT"). Until these are fixed, the runtime cannot fully switch from Epsilon ETL to Zeta for UI model generation.

## What Changes

- **Fix Claim.attributeType not being set** in `ModifiableRules.claimModifier()` — the Zeta rule creates Claim objects but never wires the required `attributeType` reference that the ETL version sets via `getTransferFieldDeclarationEquivalent()`
- **Fix Table.columns being empty** — Zeta creates Table objects from row declarations but does not populate their `columns` containment reference (requires at least 1 column per UI metamodel constraint)
- **Fix Action.actionDefinition dangling or missing** — two sub-issues:
  - ActionDefinition objects created but not added to the model resource (dangling cross-reference)
  - Filter/autocomplete/tags action types missing ActionDefinition creation entirely
- **Enable ZETA mode** in all 22 ETL-only test parameterizations across 10 test files

## Capabilities

### New Capabilities
- `jsl2ui-zeta-claim-wiring`: Fix Claim.attributeType assignment in Zeta ClaimModifier rule
- `jsl2ui-zeta-table-columns`: Fix Table column population from Row declarations in Zeta rules
- `jsl2ui-zeta-action-definitions`: Fix ActionDefinition creation and resource registration in Zeta action rules

### Modified Capabilities
<!-- No existing spec-level requirement changes — these are implementation fixes to achieve existing ETL/ZETA parity -->

## Impact

- **Module**: `judo-tatami-jsl-jsl2ui` (only module affected)
- **Source files**: Zeta rule classes under `src/main/java/.../zeta/rules/` (application/, view/)
- **Test files**: 10 test files under `src/test/java/.../application/` — change `@EnumSource` from `{"ETL"}` to `{"ETL", "ZETA"}`
- **Downstream**: Unblocks `zeta-only-runtime` change verification task V1
- **Risk**: Low — additive changes to Zeta rules only, ETL path unchanged
