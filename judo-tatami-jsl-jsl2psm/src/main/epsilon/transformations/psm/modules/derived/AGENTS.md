# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/modules/derived`

Epsilon Transformation Language (ETL) modules converting calculated and derived JSL fields, queries, and relations into PSM data and navigation properties.

| File | Purpose |
| --- | --- |
| `dataProperty.etl` | Transforms calculated, eager primitive entity fields into `JUDOPSM!DataProperty` with getter expression bindings. |
| `entityQuery.etl` | Transforms calculated, non-eager primitive entity fields into PSM data properties with query-without-parameter annotations and JQL query getter expressions. |
| `expressionType.etl` | Generates JQL-based data and reference getter expressions (`DataExpressionType`, `ReferenceExpressionType`) for derived entity fields and relations. |
| `navigationProperty.etl` | Transforms calculated entity relations into `JUDOPSM!NavigationProperty` with target entity type, getter expression, and derived cardinality. |
