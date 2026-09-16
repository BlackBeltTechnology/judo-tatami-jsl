# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/operations/jsl/data`

Epsilon Operations Language (EOL) helper queries and metamodel navigation extensions for JSL data declarations and modifiers during JSL-to-PSM transformations.

| File | Purpose |
| --- | --- |
| `_importData.eol` | Aggregates and imports all sibling EOL data operation modules in dependency sequence for downstream transformation scripts. |
| `actorAccessDeclaration.eol` | Computes element identifier `getId()` on `JSL!ActorAccessDeclaration` relative to its parent container. |
| `actorDeclaration.eol` | Resolves identifier, fully qualified name, security modifiers (`getIdentity`, `getRealm`, `getClaim`, `getGuard`), principal transfer, and maps `JSL!ActorDeclaration` to corresponding PSM transfer object types. |
| `dataTypeDeclaration.eol` | Computes identifier and qualified name for `JSL!DataTypeDeclaration`, and converts `JSL!MaxFileSizeModifier` values across decimal and binary units (kB, MB, GB, KiB, MiB, GiB) to byte counts. |
| `defaultModifier.eol` | Computes hierarchical element identifier (`/default`) and fully qualified name for `JSL!DefaultModifier`. |
| `entityDeclaration.eol` | Resolves PSM entity type equivalent, container-relative identifier, root model package, inheritance tree members (`getInheritedMembers`), abstract flag, and qualified name for `JSL!EntityDeclaration`. |
| `entityDerivedDeclaration.eol` | Computes hierarchical identifier and qualified name separated by `#` for `JSL!EntityMemberDeclaration`. |
| `entityFieldDeclaration.eol` | Computes container-relative identifier and `#`-delimited qualified name for `JSL!EntityFieldDeclaration`. |
| `entityMemberDeclaration.eol` | Queries eager loading, calculated status, and required validation modifiers on `JSL!EntityMemberDeclaration` via `jslUtils`. |
| `entityRelationDeclaration.eol` | Resolves container-relative identifier and `#`-delimited fully qualified name for `JSL!EntityRelationDeclaration`. |
| `entityRelationOpposite.eol` | Resolves container-relative `/opposite-add/` identifier and `#`-delimited qualified name for injected relation opposites `JSL!EntityRelationOppositeInjected`. |
| `enumDeclaration.eol` | Computes container-relative identifier and `::`-delimited qualified name for `JSL!EnumDeclaration`. |
| `enumLiteral.eol` | Computes container-relative identifier and `#`-delimited qualified name for `JSL!EnumLiteral`. |
| `errorDeclaration.eol` | Computes container-relative identifier and `::`-delimited qualified name for `JSL!ErrorDeclaration`. |
| `errorField.eol` | Computes container-relative identifier and `#`-delimited qualified name for `JSL!ErrorField`. |
| `expression.eol` | Resolves container-relative `/expression` identifier and checks whether an expression serves as getter for readable transfer fields or relations. |
| `modelDeclaration.eol` | Maps `JSL!ModelDeclaration` to PSM root model, resolves nested namespace packages, computes unique package names, and formats model identifiers. |
| `modifiable.eol` | Queries modifier definitions on `JSL!Modifiable` elements, resolving layout, validation, UI presentation, security modifiers, and event execution phase flags (`isEventInstead`, `isEventBefore`, `isEventAfter`). |
| `navigation.eol` | Computes container-relative `/navigation` identifier string for `JSL!Navigation`. |
| `primitiveDeclaration.eol` | Resolves PSM primitive types (`numeric`, `date`, `time`, `timestamp`, `boolean`, `string`, `binary`) or enumeration equivalents for `JSL!PrimitiveDeclaration`. |
| `queryDeclaration.eol` | Computes container-relative identifier and `::`-delimited qualified name for `JSL!QueryDeclaration`. |
| `queryParameterDeclaration.eol` | Computes container-relative identifier for `JSL!QueryParameterDeclaration`. |
| `simpleTransferDeclaration.eol` | Computes container-relative identifier and `::`-delimited qualified name for `JSL!SimpleTransferDeclaration`. |
| `transferActionDeclaration.eol` | Evaluates whether `JSL!TransferActionDeclaration` represents a static action via `jslUtils.isStatic`. |
| `transferCreateDeclaration.eol` | Computes container-relative identifier string for `JSL!TransferCreateDeclaration`. |
| `transferDeclaration.eol` | Resolves mapped/unmapped PSM transfer object equivalents, checks CRUD and lifecycle operation support (`isCreateSupported`, `isUpdateSupported`, `isDeleteSupported`, `isGetTemplateSupported`), and queries sortable fields. |
| `transferDeleteDeclaration.eol` | Computes container-relative identifier string for `JSL!TransferDeleteDeclaration`. |
| `transferFieldDeclaration.eol` | Resolves PSM transfer attribute equivalents (transient, derived, mapped), checks field characteristics (mapped, readable, required, sortable, filterable), and queries upload token support for binary fields. |
| `transferRelationDeclaration.eol` | Resolves mapped/derived/transient PSM embedded transfer relations, checks reference manipulation permissions (add, remove, set, unset), and resolves choice range accessors. |
| `transferUpdateDeclaration.eol` | Computes container-relative identifier string for `JSL!TransferUpdateDeclaration`. |
