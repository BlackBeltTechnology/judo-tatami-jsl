# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui/modules/structure`

ETL transformation rules mapping JSL transfer structures (actions, transfer objects, fields, and relations) to UI data metamodel elements.

| File | Purpose |
| --- | --- |
| `transferActionDeclaration.etl` | Transforms JSL `TransferActionDeclaration` into UI `OperationType` (STATIC or MAPPED) with input, output, and fault parameter types attached to owner `ClassType`. |
| `transferDeclaration.etl` | Transforms JSL `TransferDeclaration` into UI `ClassType` with behaviour flags (REFRESH, UPDATE, DELETE, TEMPLATE) and registers it in `Application.dataElements`. |
| `transferFieldDeclaration.etl` | Transforms JSL `TransferFieldDeclaration` into UI `AttributeType` classified by persistence mode (transient, derived/read-only, or mapped) with primitive data types. |
| `transferRelationDeclaration.etl` | Transforms JSL `TransferRelationDeclaration` into UI `RelationType` (association or aggregation) with target `ClassType` and CRUD/reference behaviour flags. |
