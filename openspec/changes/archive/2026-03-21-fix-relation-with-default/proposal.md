## Why

The ZETA JSL2PSM transformation does not set `defaultValue` on `TransferObjectRelation` when cloning inherited entity relations for extended transfer objects. This causes 2 STRICT comparison failures in the `itest-RelationWithDefaults` model (50/51 pass, 1 fail).

## What Changes

- Add a failing dual test for inherited default relation resolution (TDD: test first)
- Fix `cloneEntityRelation()` in `Jsl2PsmHelper.java` to resolve and set the `defaultValue` NavigationProperty on cloned TransferObjectRelation, matching ETL behavior
- Verify all external model comparisons pass (51/51)

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `zeta-jsl2psm`: Fix inherited relation default value resolution in cloneEntityRelation

## Impact

- `judo-tatami-jsl-jsl2psm/src/main/java/.../zeta/Jsl2PsmHelper.java` — add `defaultValue` assignment in `cloneEntityRelation()`
- `judo-tatami-jsl-jsl2psm/src/test/` — new or extended test for inherited relation defaults
- No API or breaking changes
