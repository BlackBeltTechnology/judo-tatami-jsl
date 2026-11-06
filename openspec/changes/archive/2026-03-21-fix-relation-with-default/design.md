## Approach

Single-line fix in `Jsl2PsmHelper.cloneEntityRelation()` to resolve and set the `defaultValue` NavigationProperty on cloned TransferObjectRelation, matching the ETL abstract rule behavior.

## Root Cause

ETL uses an abstract rule `AbstractCreateTransferObjectRelationFromEntityRelationForDefaultTransferObjectType` that sets `t.defaultValue` for **both** the greedy (direct) and lazy (clone/inherited) paths. The ZETA implementation has two separate code paths:

1. **Greedy path** (`DefaultTransferRelationRules.populateAssociationRelation()`) — correctly sets `target.setDefaultValue(...)` at line 352-357
2. **Clone path** (`Jsl2PsmHelper.cloneEntityRelation()`) — has a comment noting defaults exist but does NOT set `defaultValue` (lines 1197-1200)

## Changes

### `Jsl2PsmHelper.java` (line ~1197-1200)

Replace the empty comment block:

```java
// Clone default value if present
if (getDefault(rel) != null) {
    // Note: default value is handled separately via cloneDefaultRelation
}
```

With the actual defaultValue assignment:

```java
// Set default value reference (matches ETL abstract rule)
if (getDefault(rel) != null) {
    NavigationProperty defaultValue = ctx.equivalent(getDefault(rel),
            NavigationProperty.class, Jsl2PsmRuleNames.CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT);
    target.setDefaultValue(defaultValue);
}
```

This mirrors `populateAssociationRelation()` which already does the same for the greedy path.

## TDD Approach

1. Write a new test that transforms the `RelationWithDefaultsModel.jsl` (or a minimal subset) using both ETL and ZETA, then asserts STRICT equivalence — verify it fails
2. Apply the one-line fix
3. Verify the test passes, plus all 51 external models pass (51/51)

## Risk

Minimal — the NavigationProperty is already created by the greedy `CreateDefaultNavigationPropertyForDefaultTransferObject` rule. We're just resolving the existing equivalent, not creating anything new.
