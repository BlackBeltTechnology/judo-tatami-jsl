# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/modules/data`

Epsilon Transformation Language (ETL) modules converting core JSL data constructs into PSM entity types, attributes, associations, containments, cardinalities, and queries.

| File | Purpose |
| --- | --- |
| `association.etl` | Transforms `JSL!EntityRelationDeclaration` and opposite declarations into `JUDOPSM!AssociationEnd` partner pairs and default value annotations. |
| `cardinality.etl` | Computes PSM `Cardinality` (lower and upper bounds) for entity relations, entity fields, opposite relations, derived declarations, queries, and transfer relations. |
| `containment.etl` | Transforms non-calculated entity reference fields into `JUDOPSM!Containment` relations and binds reads-reference expressions for transfer relations. |
| `entityType.etl` | Transforms `JSL!EntityDeclaration` into `JUDOPSM!EntityType`, attaching abstract flag and super entity type inheritance hierarchy. |
| `primitiveTypedElement.etl` | Transforms primitive entity fields into `JUDOPSM!Attribute`, configuring data types, identifier flags, default values, and data expressions. |
| `query.etl` | Transforms `JSL!QueryDeclaration` into transfer attributes, transfer object relations, and default query access bindings on default transfer objects. |
