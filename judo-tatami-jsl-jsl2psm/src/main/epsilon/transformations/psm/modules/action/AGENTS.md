# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/epsilon/transformations/psm/modules/action`

Epsilon Transformation Language (ETL) modules generating PSM operations, parameters, and execution behaviours for entity and transfer object actions, relations, and actor interactions.

| File | Purpose |
| --- | --- |
| `accessCreateBehaviour.etl` | Transforms `JSL!ActorAccessDeclaration` into PSM create operation, input/output parameters, cardinality, and transfer operation behaviour when create modifier is enabled. |
| `accessListBehaviour.etl` | Transforms `JSL!ActorAccessDeclaration` into PSM list operation, query-customizer input parameter, collection output parameter, and list behaviour. |
| `accessValidateCreateBehaviour.etl` | Transforms `JSL!ActorAccessDeclaration` into PSM validate-create operation, validation input/output parameters, and validation behaviour. |
| `action.etl` | Transforms bound and unbound `JSL!TransferActionDeclaration` into PSM operations, return parameters, input parameters, and stub operation bodies. |
| `actorBehaviour.etl` | Generates built-in PSM operations (`_metadata`, `_principal`) and return parameters for actor types. |
| `deleteBehaviour.etl` | Transforms `JSL!TransferDeleteDeclaration` into entity and transfer type delete operations and PSM delete behaviours. |
| `getActionInputRangeBehaviour.etl` | Generates range-retrieval operations, parameters, and behaviours for action input parameters constrained by choices or filters. |
| `getTemplateBehaviour.etl` | Generates template retrieval operations, output parameters, and behaviour for transfer types supporting template instantiation. |
| `getUploadTokenBehaviour.etl` | Synthesizes upload token types and generates `getUploadToken` operations and behaviours for binary transfer attributes. |
| `refreshBehaviour.etl` | Transforms mapped transfer types into entity and transfer refresh operations with input/output parameters and refresh behaviours. |
| `relationAddReferenceBehaviour.etl` | Generates add-reference operations, parameters, and behaviours on entities and transfer types for editable multi-valued relations. |
| `relationCreateBehaviour.etl` | Transforms `JSL!TransferRelationDeclaration` into relation-create operations, input/output parameters, and creation behaviours on parent entities and transfers. |
| `relationGetRangeReferenceBehaviour.etl` | Generates range navigation operations, query parameters, and behaviours for selectable relation target values. |
| `relationListBehaviour.etl` | Generates relation listing operations, pagination/filtering parameters, and collection list behaviours for relation navigation. |
| `relationRemoveReferenceBehaviour.etl` | Generates remove-reference operations and parameters on entities and transfer types for clearing relation associations. |
| `relationSetReferenceBehaviour.etl` | Generates set-reference operations and parameters on entities and transfer types for single-valued relation assignment. |
| `relationUnsetReferenceBehaviour.etl` | Generates unset-reference operations and parameters on entities and transfer types for nullable single-valued relation clearing. |
| `relationValidateCreateBehaviour.etl` | Generates validate-create operations, parameters, and behaviours for validating relation target creation prior to commit. |
| `updateBehaviour.etl` | Transforms `JSL!TransferUpdateDeclaration` into update operations, payload input parameters, and update behaviours on entities and transfer types. |
| `validateUpdateBehaviour.etl` | Transforms `JSL!TransferUpdateDeclaration` into validate-update operations and parameters for pre-update payload validation. |
