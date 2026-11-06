# `equivalentDiscriminated.eol`

Fan-out variant of ETL `equivalent()` yielding one target per discriminator.
Exports `Any.equivalentDiscriminated(transformation, id, discriminator)` plus a
2-arg overload defaulting id to `(jsl/<self id>)/<transformation>`.

Target id is `<id>/(discriminator/<discriminator>)`, looked up in `__cacheMap`,
then the resource, else `equivalent(transformation)` and `ecoreUtil.copy` when
the hit already owns another id. Throws on undefined `self` and on cache id
mismatch.