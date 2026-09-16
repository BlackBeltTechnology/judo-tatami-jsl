# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/operations/jsl/actor`

EOL operations defining query functions and modifier accessors for JSL actor models.

| File | Purpose |
| --- | --- |
| `_importActor.eol` | Aggregation script importing all actor-related EOL helper operations. Imports `actorAccessDeclaration.eol` and `actorDeclaration.eol`. |
| `actorAccessDeclaration.eol` | Computes cached hierarchical identifier path for actor access points. Exports `JSL!ActorAccessDeclaration getId() : String` concatenating container ID and element name. |
| `actorDeclaration.eol` | Provides cached lookup operations for actor declarations extracting identity, realm, claim, guard modifiers, principal transfer declarations, and recursive menu declarations. Exports `getIdentity()`, `getPrincipal()`, `getAllMenuDeclarations() : Set`, `getIdentityTransferDeclaration()`. |
