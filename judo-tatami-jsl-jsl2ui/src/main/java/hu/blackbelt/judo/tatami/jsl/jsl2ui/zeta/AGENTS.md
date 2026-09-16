# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/zeta`

Zeta-based engine implementation and helpers mirroring the JSL-to-UI Epsilon ETL transformation pipeline.

| File | Purpose |
| --- | --- |
| `Jsl2UiHelper.java` | Static helper methods for JSL AST navigation, modifier extraction, and ID calculation across Zeta transformation rules. Exports `getJslId()`, `purify()`, `getFqName()`, `getLabelModifier()`, and UI modifier getters. |
| `Jsl2UiRuleNames.java` | String constant definitions for all JSL-to-UI transformation rule names across application, structure, type, and view modules. |
| `Jsl2UiZetaTransformation.java` | Zeta transformation orchestrator mapping JSL models to UI models; registers rules across application, structure, type, and view packages; handles pre-order indexing and post-order sorting. |
