# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/zeta/rules/structure`

Zeta transformation rule classes implementing structural transfer action, class, field, and relation mappings to UI data elements.

| File | Purpose |
| --- | --- |
| `TransferActionDeclarationRules.java` | Zeta rules `operationType()`, `operationInputParameterType()`, `operationOutputParameterType()`, and `operationFaultParameterType()`; maps JSL transfer actions to UI operations. |
| `TransferDeclarationRules.java` | Zeta rule `classType()`; transforms exposed JSL `TransferDeclaration` instances into UI `ClassType` with CRUD/refresh behavioural flags. |
| `TransferFieldDeclarationRules.java` | Zeta rules `createTransientTransferAttribute()`, `createDerivedTransferAttribute()`, and `createMappedTransferAttribute()`; populates UI `ClassType` attributes from transfer fields. |
| `TransferRelationDeclarationRules.java` | Zeta rules `relationType()` and lazy `cloneRelationType()`; transforms JSL `TransferRelationDeclaration` into UI `RelationType` associations and aggregations. |
