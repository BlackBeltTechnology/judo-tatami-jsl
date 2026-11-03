# `action.eol`

`@cached` operations on `JSL!TransferActionDeclaration`. Identity: `getId()` =
container id + `/` + name, `getFqName()` = container FQ name + `#` + name.

Permission probes `isUpdateAllowed()` / `isDeleteAllowed()` require a declared
`return` and a non-`false` `UpdateModifier`/`DeleteModifier`.

`getOperationDeclarationEquivalent()` picks unbound/bound operation rules from
container `map` and `isStatic()`; `getTransferActionRangeEquivalent()` needs a
`ChoiceModifier` and throws `"No choice defined"` without one.