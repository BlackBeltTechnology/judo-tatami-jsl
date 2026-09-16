# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/operations`

Shared EOL operation library imported by the JSL→PSM ETL transformation. Pulls in
the `jsl/` metamodel-navigation layer plus resource-ID and discriminator plumbing.

| File | Purpose |
|---|---|
| `_importAll.eol` | Single import aggregator for the operation layer. Imports `id.eol`, `equivalentDiscriminated.eol`, `utils.eol` and `jsl/_importAll.eol`. Transformation modules import this one file instead of individual operations; adding a new operation file here is required for it to be visible to ETL rules. |
| `equivalentDiscriminated.eol` | Fan-out of ETL `equivalent()` yielding one target per discriminator via `Any.equivalentDiscriminated(transformation, id, discriminator)` (+ 2-arg overload defaulting id); target id looked up in `__cacheMap`, else `equivalent()` / `ecoreUtil.copy` on id clash. → see `equivalentDiscriminated.eol.AGENTS.md` |
| `id.eol` | Resource-backed identity accessors for EObjects. Exports `Any.setId(id)` and `Any.getId()`, both delegating to the containing `eResource`. `setId` keeps `__originalMap` and `__cacheMap` in sync by re-keying the cache entry from old id to new. Both throw when `self` is undefined or not yet added to a resource — objects must be attached before any id operation. |
| `utils.eol` | String helper for JSL fully-qualified names. Exports `String.fqNameToCamelCase()`, splitting on `::` and `#` and upper-casing each segment's first character before concatenation. Input must be an FQ name; separators other than `::`/`#` are not split. |
