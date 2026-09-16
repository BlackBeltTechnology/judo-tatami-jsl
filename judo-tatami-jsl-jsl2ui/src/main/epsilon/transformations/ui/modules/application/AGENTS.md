# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui/modules/application`

ETL transformation rules mapping JSL actor, menu group, and modifier declarations to UI application and authentication metamodel elements.

| File | Purpose |
| --- | --- |
| `actorDeclaration.etl` | Transforms JSL `ActorDeclaration` into UI `ClassType` (`isActor = true`) and UI `Authentication` with realm config; links `Authentication` to root `Application`. |
| `actorGroupDeclaration.etl` | Transforms JSL `UIMenuGroupDeclaration` into UI `NavigationItem` hierarchy with label and icon; registers element position in `__pos` under container or navigation controller. |
| `modifiable.etl` | Defines lazy rule `IconModifierIcon` transforming JSL `IconModifier` to UI `Icon`, and rule `ClaimModifier` transforming JSL `ClaimModifier` to UI `Claim` attached to `Authentication`. |
