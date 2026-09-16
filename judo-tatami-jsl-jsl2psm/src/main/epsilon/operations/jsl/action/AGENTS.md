# AGENTS.md — jsl2psm JSL action operations

Navigation operations over `JSL!TransferActionDeclaration`, resolving each action to
the PSM operation rule that must have produced it.

| File | Purpose |
|---|---|
| `_importAction.eol` | Import barrel for the action operation package; imports `action.eol` only. `jsl/_importAll.eol` imports this file, so new action operation files must be listed here to become visible to ETL rules. |
| `action.eol` | `@cached` ops on `JSL!TransferActionDeclaration`: `getId()`/`getFqName()` identity, `isUpdateAllowed()`/`isDeleteAllowed()` permission probes, `getOperationDeclarationEquivalent()` rule picker, `getTransferActionRangeEquivalent()` (throws `"No choice defined"`). → see `action.eol.AGENTS.md` |
