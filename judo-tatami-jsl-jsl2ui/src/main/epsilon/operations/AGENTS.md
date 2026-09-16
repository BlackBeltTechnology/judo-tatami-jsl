# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/operations`

Core Epsilon Object Language (EOL) operations providing identifier management, discriminated transformation caching, and operation bundle aggregation for JSL-to-UI transformations.

| File | Purpose |
| --- | --- |
| `_importAll.eol` | Aggregation script importing core id utilities, discriminated equivalent operations, UI element helpers, and all JSL domain operations. Includes `id.eol`, `equivalentDiscriminated.eol`, `ui/namedElement.eol`, `jsl/_importAll.eol`. |
| `equivalentDiscriminated.eol` | Dispatches discriminated model transformation equivalents with cache retrieval and EMF copying for duplicate discriminator keys. Exports `Any equivalentDiscriminated(String, String, String)` and `Any equivalentDiscriminated(String, String)`. Requires non-undefined target resource and valid transformation rule name. |
| `id.eol` | Manages EMF resource identifier setting, retrieval, cache synchronization, and XMI identifier purification. Exports `setId(String)`, `getId() : String`, `purify() : String`. Throws exception if target object is undefined or not attached to an EMF resource. |
