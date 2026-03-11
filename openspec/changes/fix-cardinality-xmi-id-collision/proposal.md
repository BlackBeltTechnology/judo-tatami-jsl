## Why

The JSL2PSM Zeta transformation produces XMI ID collisions for `Cardinality` objects on transfer relations. When multiple rules create Cardinality instances from the same `TransferRelationDeclaration` source (transient, derived, mapped variants), they all receive the same XMI ID because `createCardinalityFromModifiable()` uses only the source ID + a fixed suffix. ETL avoids this via `equivalentDiscriminated()` which includes the parent's ID as a discriminator.

## What Changes

- Fix `createCardinalityFromModifiable()` calls in `TransferRelationRules.java` and `ContainmentRules.java` to produce unique XMI IDs by including the parent target's ID as a discriminator
- Eliminate the ERROR-level `XMI ID COLLISION DETECTED` log messages for Cardinality objects

## Capabilities

### New Capabilities
- `cardinality-id-fix`: Fix XMI ID collisions for Cardinality objects created from TransferRelationDeclaration sources

### Modified Capabilities

## Impact

- `judo-tatami-jsl-jsl2psm/src/main/java/.../zeta/rules/structure/TransferRelationRules.java` — fix Cardinality ID generation
- `judo-tatami-jsl-jsl2psm/src/main/java/.../zeta/rules/data/ContainmentRules.java` — fix Cardinality ID generation
- `judo-tatami-jsl-jsl2psm/src/main/java/.../zeta/Jsl2PsmHelper.java` — possibly update helper signature
