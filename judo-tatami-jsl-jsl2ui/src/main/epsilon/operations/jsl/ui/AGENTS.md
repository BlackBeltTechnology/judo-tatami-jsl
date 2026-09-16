# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/operations/jsl/ui`

EOL operations defining query functions, container visual element mappings, and hierarchy traversals for JSL UI constructs.

| File | Purpose |
| --- | --- |
| `_importUI.eol` | Aggregation import barrel for UI EOL helpers; entry point importing every `*.eol` helper module (`actionGroupDeclaration.eol` … `viewActionDeclaration.eol`). → see `_importUI.eol.AGENTS.md` |
| `actionGroupDeclaration.eol` | Computes cached identifiers, fully-qualified names, exposed visual elements, and UI container visual element mappings for UI action groups. Exports `getId() : String`, `getFqName() : String`, `getExposedVisualElements(Set) : Set`, `uiContainer() : UI!ui::VisualElement`. |
| `cardDeclaration.eol` | Provides cached lookup for UI card declarations, computing identifiers, fully-qualified names, exposed visual widgets, and constituent action declarations. Exports `getId() : String`, `getFqName() : String`, `getExposedVisualElements(Set) : Set`, `getAllActionDeclarations() : Set`. |
| `frontendDeclaration.eol` | Inspects UI frontend declarations, resolving target actor declarations, menu structures, dashboard menus, profile menus, and entry point views. Exports `getId() : String`, `getFrontend() : JSL!UIFrontendDeclaration`, `getActorDeclaration() : JSL!ActorDeclaration`, `getDashboardMenus() : Set`. |
| `menuGroupDeclaration.eol` | Resolves hierarchical identifiers, fully-qualified names, and parent UI frontend references for menu group declarations across nested menu items. Exports `getId() : String`, `getFqName() : String`, `getFrontend() : JSL!UIFrontendDeclaration`. |
| `menuLinkDeclaration.eol` | Computes cached identifiers, enclosing frontend, target actor, and target view references for navigation menu links. Exports `getId() : String`, `getFrontend() : JSL!UIFrontendDeclaration`, `getActorDeclaration() : JSL!ActorDeclaration`. |
| `menuModifier.eol` | Resolves enclosing frontend container references, identifiers, and fully-qualified names for menu modifiers. Exports `getId() : String`, `getFqName() : String`, `getFrontend() : JSL!UIFrontendDeclaration`. |
| `menuTableDeclaration.eol` | Traverses visual elements and create-form modifiers exposed by menu table declarations. Exports `getId() : String`, `getFqName() : String`, `getExposedVisualElements(Set) : Set`. |
| `profileModifier.eol` | Resolves container frontend reference, identifiers, and fully-qualified names for profile modifiers. Exports `getId() : String`, `getFqName() : String`, `getFrontend() : JSL!UIFrontendDeclaration`. |
| `rowColumnDeclaration.eol` | Computes cached hierarchical identifiers and fully-qualified names for UI grid row columns. Exports `getId() : String`, `getFqName() : String`. |
| `rowDeclaration.eol` | Computes cached identifiers and collects exposed visual elements contained within UI layout rows. Exports `getId() : String`, `getFqName() : String`, `getExposedVisualElements(Set) : Set`. |
| `tagDeclaration.eol` | Computes cached identifiers and collects child visual elements exposed inside UI tag declarations. Exports `getId() : String`, `getFqName() : String`, `getExposedVisualElements(Set) : Set`. |
| `viewActionDeclaration.eol` | Resolves cached identifiers and fully-qualified names for UI view actions nested under action groups or view containers. Exports `getId() : String`, `getFqName() : String`. |
| `viewDeclaration.eol` | Resolves container visual element equivalents for form pages or standard views. Exports `getId() : String`, `getFqName() : String`, `uiContainer() : UI!ui::VisualElement`. |
| `viewGroupDeclaration.eol` | Computes cached identifiers, frame layout flags, and contained child visual elements for view groups. Exports `getId() : String`, `getFqName() : String`, `isFrame() : Boolean`. |
| `viewLinkDeclaration.eol` | Resolves cached identifiers and fully-qualified names for view link declarations. Exports `getId() : String`, `getFqName() : String`. |
| `viewPanelDeclaration.eol` | Collects direct relations and nested group/tab relation associations across UI view panels. Exports `getDirectRelations() : Set`. Traverses child groups and tab panels recursively. |
| `viewTableDeclaration.eol` | Resolves cached identifiers, fully-qualified names, and table visual properties for UI view tables. Exports `getId() : String`, `getFqName() : String`. |
| `viewTabsDeclaration.eol` | Computes cached identifiers, fully-qualified names, and frame status for tabbed view panel containers. Exports `getId() : String`, `getFqName() : String`, `isFrame() : Boolean`. |
| `viewWidgetDeclaration.eol` | Resolves UI attribute type equivalents for view widgets bound to transfer fields. Exports `getId() : String`, `getFqName() : String`, `getTransferFieldDeclarationEquivalent() : UI!ui::data::AttributeType`. |
| `visibleDeclaration.eol` | Calculates 0-based layout position index of elements within parent views, groups, tabs, or action groups. Exports `getPos() : Integer`. Falls back to recorded element position cache. |
| `widgetDeclaration.eol` | Computes cached hierarchical identifiers and fully-qualified names for widget declarations. Exports `getId() : String`, `getFqName() : String`. |
