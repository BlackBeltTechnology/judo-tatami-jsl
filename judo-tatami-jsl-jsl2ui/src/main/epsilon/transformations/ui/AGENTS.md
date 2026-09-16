# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui`

Master ETL transformation entry point for converting JSL metamodel models into UI metamodel models.

| File | Purpose |
| --- | --- |
| `jslToUi.etl` | Orchestrates JSL-to-UI ETL transformation pipeline. In `pre` block indexes element ordering into `__pos` map; in `post` block sorts navigation items and containers by position; throws when child under `Container` lacks position. |
