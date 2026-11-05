## Context

`createCardinalityFromModifiable(ctx, source, suffix)` in `Jsl2PsmHelper` generates XMI IDs as `(jsl/{sourceId})/{suffix}`. When multiple rules call this with the **same source** and **same suffix**, the resulting Cardinality objects collide.

The collision occurs in:
- **TransferRelationRules.java** — 5 calls all using suffix `"CreateCardinalityForTransferRelationDeclaration"` with the same `TransferRelationDeclaration` source
- **ContainmentRules.java** — 4 calls using the same suffix for transfer relation reads/default StaticNavigation/NavigationProperty

The clone helpers in `Jsl2PsmHelper` (for `DefaultTransferObjectTypeRules`) already avoid this by using discriminated suffixes like `discriminator + "/CloneCardinalityFromEntityFieldForDefaultTransferObjectType/" + getJslId(field)`.

## Goals / Non-Goals

**Goals:**
- Eliminate XMI ID collisions for Cardinality objects by making each call site produce a unique suffix
- Match ETL's `equivalentDiscriminated()` semantics where each parent gets its own Cardinality with a unique ID

**Non-Goals:**
- Refactoring the `createCardinalityFromModifiable` helper signature (suffix parameter already supports this)
- Fixing any other XMI ID collision issues beyond Cardinality on transfer relations

## Decisions

**Use the parent target's rule name as the suffix discriminator.** Each call site in TransferRelationRules and ContainmentRules already has a unique parent rule name (e.g., `CreateTransientTransferObjectRelationForTransferRelationDeclaration`). Use that as the suffix instead of the shared `"CreateCardinalityForTransferRelationDeclaration"`.

Pattern:
```
BEFORE: createCardinalityFromModifiable(ctx, source, "CreateCardinalityForTransferRelationDeclaration")
AFTER:  createCardinalityFromModifiable(ctx, source, "CreateCardinalityFor/CreateTransientTransferObjectRelationForTransferRelationDeclaration")
```

This is the simplest fix — no signature changes, no new abstractions. Each call site just passes a unique suffix.

**Alternative considered:** Adding a `parentId` parameter to the helper — rejected because the suffix parameter already serves this purpose and the clone helpers already use this pattern.

## Risks / Trade-offs

- [XMI ID format change] → Existing serialized models that reference these IDs will see different IDs. This is acceptable since Cardinality objects are not externally referenced.
- [normalizeCardinalityIds @PostExecution] → The existing `CardinalityRules.normalizeCardinalityIds()` @PostExecution hook normalizes all Cardinality IDs to `{containerId}/cardinality`. After the fix, this hook will still run and produce the final IDs. The fix prevents the collision ERROR during transformation; the hook produces clean final IDs.
