# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/zeta/rules/namespace`

Zeta transformation rule sets mapping JSL model declarations and namespace hierarchies to PSM root models and packages.

| File | Purpose |
| --- | --- |
| `NamespaceRules.java` | Lazy transformation rules creating root PSM `Model` instances and hierarchical `Package` structures from `ModelDeclaration`. Exports `createRootModel()`, `createModelPackages()`. Parses package segments delimited by `::` and creates parent-child namespace containment. |
