# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/operations/jsl/namespace`

EOL operations defining model namespace resolutions, modifier accessors, and identifier generation for JSL models.

| File | Purpose |
| --- | --- |
| `_importNamespace.eol` | Aggregation script importing namespace-related EOL operation modules. Imports `modelDeclaration.eol`, `modifiable.eol`, and `modifier.eol`. |
| `modelDeclaration.eol` | Computes slash-separated path identifiers and unique model name sanitization for JSL model declarations. Exports `getId() : String`, `getFqName() : String`, `getUniqueModelName() : String`. |
| `modifiable.eol` | Provides cached lookup operations for modifiers attached to modifiable JSL elements. Exports `getLabelModifier()`, `getLabelWithNameFallback() : String`, `getActionGroupModifier()`, `getIconModifier()`, `isPredictive() : Boolean`, `getPrecision()`, `getScale()`. |
| `modifier.eol` | Computes cached unique identifiers and fully-qualified names for modifier instances and concrete subtypes. Exports `getId() : String`, `getFqName() : String` handling type fallbacks to Ecore class names. |
