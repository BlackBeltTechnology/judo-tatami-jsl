# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/zeta/rules/application`

Zeta transformation rule classes implementing application, actor, menu navigation, and modifier mappings to UI metamodel targets.

| File | Purpose |
| --- | --- |
| `ActorDeclarationRules.java` | Zeta rules `actor()` and `authentication()`; transforms JSL `ActorDeclaration` into UI `ClassType` and creates UI `Authentication` with principal/realm attributes. |
| `ActorGroupDeclarationRules.java` | Zeta rule `menuItemGroup()`; transforms JSL `UIMenuGroupDeclaration` into UI `NavigationItem` with labels and icons, registering positions in `__pos`. |
| `FrontendDeclarationRules.java` | Zeta rules `application()`, `theme()`, `navigationController()`, and empty dashboard page definitions; builds root UI application structure from `UIFrontendDeclaration`. |
| `ModifiableRules.java` | Zeta rules `iconModifierIcon()` and `claimModifier()`; converts JSL `IconModifier` into UI `Icon` and `ClaimModifier` into UI `Claim` linked to authentication. |
