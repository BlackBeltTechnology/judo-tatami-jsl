# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/zeta`

Zeta-based engine and helper utilities transforming JSL DSL models to PSM models.

| File | Purpose |
| --- | --- |
| `Jsl2PsmHelper.java` | Helper for JSL-to-PSM transformations resolving model packages, data types, relation opposite navigation, cardinality flags, and PSM model element additions. Exports `getJslId(EObject)`, `setId(EObject, String)`, `addElement(Namespace, NamedElement)`, `addRelation(...)`, `addAttribute(...)`, `addNavigationProperty(...)`. |
| `Jsl2PsmRuleNames.java` | Constant registry defining transformation rule names across namespace, type, data, actor, derived, action, and structure domains. Exports static String constants such as `CREATE_ROOT_MODEL`, `CREATE_ENTITY_TYPE`, `CREATE_NUMERIC_TYPE`. |
| `Jsl2PsmZetaTransformation.java` | Zeta transformation runner executing registered rule sets to convert a `JslDslModel` into a `PsmModel`. Exports `execute(TransformationTrace)` using `Jsl2PsmZetaTransformation.builder()`. Requires non-null `jslModel` and `psmModel` in builder configuration. |
