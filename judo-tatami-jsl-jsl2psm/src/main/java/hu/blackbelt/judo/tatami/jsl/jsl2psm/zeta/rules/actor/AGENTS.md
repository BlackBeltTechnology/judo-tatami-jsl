# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/zeta/rules/actor`

Zeta transformation rule sets mapping JSL actor declarations and access points to PSM actor types and relations.

| File | Purpose |
| --- | --- |
| `AccessRules.java` | Transformation rules converting `ActorAccessDeclaration` into transient `TransferObjectRelation` instances on PSM actor types. Exports `createTransientTransferObjectRelationForActorAccessDeclaration()`. Maps target transfer declaration equivalents and sets up relation cardinalities. |
| `ActorTypeRules.java` | Transformation rules generating PSM `ActorType`, `MappedActorType`, security metadata types, and principal-retrieval operations from JSL actor declarations. Exports `createActorType()`, `createMappedActorType()`, `createMetadataSecurityType()`, `createGetPrincipalOperationForActorType()`. Supports OpenID configuration and claim mappings. |
