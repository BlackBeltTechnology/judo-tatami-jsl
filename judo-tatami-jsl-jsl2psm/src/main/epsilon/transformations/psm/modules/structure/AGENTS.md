# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/modules/structure`

Epsilon Transformation Language (ETL) modules generating PSM default and custom transfer object types, attributes, relations, and query customizers.

| File | Purpose |
| --- | --- |
| `entityDeclarationDefaultTransferAttribute.etl` | Generates default and cloned `JUDOPSM!TransferAttribute` elements from entity fields for default transfer object representations. |
| `entityDeclarationDefaultTransferObjectType.etl` | Transforms `JSL!EntityDeclaration` into `JUDOPSM!MappedTransferObjectType` default representations, copying flattened inherited attributes and relations. |
| `entityDeclarationDefaultTransferRelation.etl` | Transforms entity reference fields and associations into embedded default transfer object relations (`JUDOPSM!TransferObjectRelation`). |
| `transferDeclarationQueryCustomizer.etl` | Generates query customizer types (`_mask`, `_identifier`, order-by relations) for mapped transfer types to enable runtime filtering and ordering. |
| `transferDeclarationTransferAttribute.etl` | Transforms `JSL!TransferFieldDeclaration` into transient, derived, or mapped `JUDOPSM!TransferAttribute` elements with defaults and bindings. |
| `transferDeclarationTransferObjectType.etl` | Transforms `JSL!TransferDeclaration` into unmapped (`JUDOPSM!UnmappedTransferObjectType`) or mapped (`JUDOPSM!MappedTransferObjectType`) transfer types. |
| `transferDeclarationTransferRelation.etl` | Transforms `JSL!TransferRelationDeclaration` into transient, derived, or mapped PSM transfer relations with cardinality and range accessors. |
