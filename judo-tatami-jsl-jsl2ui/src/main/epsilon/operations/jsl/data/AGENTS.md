# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/operations/jsl/data`

EOL operations defining query functions, type mapping, and capability checks on JSL data declarations.

| File | Purpose |
| --- | --- |
| `_importData.eol` | Aggregation script importing data domain EOL helper operations. Imports `dataTypeDeclaration.eol`, `entityMemberDeclaration.eol`, `enumDeclaration.eol`, `enumLiteral.eol`, `primitiveDeclaration.eol`, `simpleTransferDeclaration.eol`, `transferActionDeclaration.eol`, `transferDeclaration.eol`, `transferFieldDeclaration.eol`, and `transferRelationDeclaration.eol`. |
| `dataTypeDeclaration.eol` | Computes cached identifiers and resolves parent actor context for data type declarations. Exports `getActorDeclaration() : JSL!ActorDeclaration`, `getId() : String`. |
| `entityMemberDeclaration.eol` | Queries delegation helpers for eager, calculated, and required modifiers on entity members. Exports `isEager() : Boolean`, `isCalculated() : Boolean`, `isRequired() : Boolean`. |
| `enumDeclaration.eol` | Resolves cached element identifiers and enclosing actor declarations for JSL enums. Exports `getId() : String`, `getActorDeclaration() : JSL!ActorDeclaration`. |
| `enumLiteral.eol` | Resolves cached identifiers and owning actor declarations for individual enum literals. Exports `getId() : String`, `getActorDeclaration() : JSL!ActorDeclaration`. |
| `primitiveDeclaration.eol` | Maps primitive declarations and enum declarations to UI data types via ETL transformation rules. Exports `getPrimitiveDeclarationEquivalent() : UI!ui::data::DataType`. Throws exception on unsupported primitive type names. |
| `simpleTransferDeclaration.eol` | Computes cached relative identifiers and fully-qualified names for simple transfer declarations. Exports `getId() : String`, `getFqName() : String`. |
| `transferActionDeclaration.eol` | Evaluates action attributes such as static, update-allowed, delete-allowed, output existence, and owning class type equivalent. Exports `isStatic() : Boolean`, `isUpdateAllowed() : Boolean`, `isDeleteAllowed() : Boolean`, `getContainerEquivalentClassType() : UI!ui::data::ClassType`. |
| `transferDeclaration.eol` | Computes exposed relations, referenced transfer objects, actions, and direct relation links for transfer declarations. Exports `getExposedTransferObjects(Set) : Set`, `getExposedRelations() : Set`, `getAllActions() : Set`, `getDirectRelations() : Set`. |
| `transferFieldDeclaration.eol` | Inspects field characteristics including mapping, reading, transience, sortability, and filterability. Exports `isCalculated() : Boolean`, `maps() : Boolean`, `reads() : Boolean`, `isTransient() : Boolean`, `isSortable() : Boolean`, `isFilterable() : Boolean`. |
| `transferRelationDeclaration.eol` | Evaluates relation capability flags including eager, aggregation, create/update/delete/unset permissions, and range query support. Exports `isAggregation() : Boolean`, `isCreateAllowed() : Boolean`, `isSetReferenceAllowed() : Boolean`, `isRangeAllowed() : Boolean`. |
