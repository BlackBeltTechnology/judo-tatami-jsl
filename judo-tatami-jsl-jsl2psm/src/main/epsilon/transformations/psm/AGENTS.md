# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm`

Root orchestration transformation script for translating JSL AST specifications into JUDO PSM models.

| File | Purpose |
| --- | --- |
| `jslToPsm.etl` | Orchestrates end-to-end JSL-to-PSM Epsilon Transformation Language (ETL) pipeline, importing all modular rules (namespace, structure, data, derived, action, type, actor); initializes target PSM root model in `pre` block, and backfills cardinalities, unreferenced primitive types, and missing enumeration ordinals in `post` block. |
