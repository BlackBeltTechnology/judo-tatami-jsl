# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/modules/namespace`

Epsilon Transformation Language (ETL) rules creating root PSM models and hierarchical namespace packages from qualified names.

| File | Purpose |
| --- | --- |
| `namespace.etl` | Transforms qualified namespace strings into `JUDOPSM!Model` root hierarchies and traverses qualified segments (`::`) to build nested `JUDOPSM!Package` trees. |
