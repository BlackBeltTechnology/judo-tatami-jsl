# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/modules/actor`

Epsilon Transformation Language (ETL) rules for actor types, principal bindings, access declarations, and actor guard filters.

| File | Purpose |
| --- | --- |
| `access.etl` | Transforms `JSL!ActorAccessDeclaration` into `JUDOPSM!TransferObjectRelation` with access flags, embedded CRUD permissions, cardinality, default values, and getter bindings. |
| `actorType.etl` | Transforms `JSL!ActorDeclaration` into PSM actor types (`ActorType`, `MappedActorType`, actor without principal), mapping principal transfer objects, security realms, claims, and guard filter expressions (`LogicalExpressionType`). |
