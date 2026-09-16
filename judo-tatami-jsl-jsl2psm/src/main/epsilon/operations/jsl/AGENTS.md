# AGENTS.md — jsl2psm JSL-side operation layer

Aggregation point for the JSL metamodel navigation operations consumed by the
JSL→PSM ETL rules. Each subpackage (`action`, `data`, `namespace`, `type`) owns its
own `_import*.eol` entry point.

| File | Purpose |
|---|---|
| `_importAll.eol` | Entry point pulling the four JSL operation subpackages together: `data/_importData.eol`, `type/_importType.eol`, `action/_importAction.eol`, `namespace/_importNamespace.eol`. Imported by `operations/_importAll.eol`, so it is the only path by which JSL navigation operations reach the ETL modules; a new subpackage stays invisible until listed here. |
