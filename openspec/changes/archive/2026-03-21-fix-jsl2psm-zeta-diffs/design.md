## Context

The JSL2PSM Zeta transformation produces DIFFs for 2 of 51 itest models compared to ETL. The root causes are:

1. **`itest-RelationWithDefaults`** (8 MISSING elements): Guard methods `isDefaultForMappedTransferRelation` / `isDefaultForUnmappedTransferRelation` check `transferDecl.getMap() != null` (whether the transfer is mapped) instead of `isMaps(rel)` (whether the relation is mapped). ETL uses `s.maps()` which checks the relation.

2. **`itest-AbstractModel`** (7 EXTRA elements): Likely caused by circular dependency during `CreateEntityDefaultTransferObjectType` — when entity Q extends abstract P, and P has derived relations referencing Q, cloning inherited relations triggers recursive creation of Q's default transfer object, producing duplicate elements.

## Goals / Non-Goals

**Goals:**
- Fix guard logic for default-value rules to match ETL's `s.maps()` semantics
- Eliminate extra elements for abstract entity models
- Achieve 51/51 STRICT EQUIVALENT in performance tests

**Non-Goals:**
- Changing the `createCardinalityFromModifiable` helper (already fixed in prior change)
- Fixing other non-PSM transformation issues

## Decisions

**Fix 1 — Default relation guards:** Change `isDefaultForMappedTransferRelation` and `isDefaultForUnmappedTransferRelation` in both `ContainmentRules.java` and `TransferRelationRules.java` to use `isMaps(rel)` instead of `transferDecl.getMap() != null`. This is a direct semantic correction to match ETL.

**Fix 2 — Abstract entity extra elements:** Investigate the actual 7 extra elements during implementation. Likely requires one of:
- (a) Preventing recursive `CreateEntityDefaultTransferObjectType` invocation via `@Lazy` instead of `@Greedy`
- (b) Adding a processing guard to prevent duplicate creation
- (c) Fixing the clone logic to not trigger the parent rule recursively

The exact fix will be determined after examining the actual extra elements with debug logging.

## Risks / Trade-offs

- [Guard change for Fix 1] → Could affect other models. Mitigated by running full 51-model performance test suite.
- [Fix 2 investigation needed] → Root cause for abstract entity issue needs runtime debugging. May require more code changes than anticipated.
