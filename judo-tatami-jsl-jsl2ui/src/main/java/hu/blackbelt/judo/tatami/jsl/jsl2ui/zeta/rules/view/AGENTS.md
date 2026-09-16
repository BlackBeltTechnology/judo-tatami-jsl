# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/zeta/rules/view`

Zeta transformation rule classes implementing UI view pages, tables, cards, tags, navigation links, and input widgets.

| File | Purpose |
| --- | --- |
| `ActionGroupDeclarationRules.java` | Zeta rule `actionGroupVisualElement()`; transforms JSL `UIActionGroupDeclaration` into UI `ButtonGroup` visual layout element. |
| `CardDeclarationRules.java` | Zeta rules mapping `UICardDeclaration` to UI `PageContainer`, card `Table`, columns, filters, and card button group action definitions. |
| `MenuLinkDeclarationRules.java` | Zeta rules mapping `UIMenuLinkDeclaration` to UI `NavigationItem` and generating view/create-form `PageDefinition` elements. |
| `MenuTableDeclarationRules.java` | Zeta rules mapping `UIMenuTableDeclaration` to UI `NavigationItem` and associated table, cards, tags, selector, and view `PageDefinition` elements. |
| `RowActionDeclarationRules.java` | Zeta lazy rules `rowActionDeclarationButton()`, `rowAction()`, and operation call action definitions for row actions. |
| `RowDeclarationRules.java` | Zeta rules mapping `UIRowDeclaration` to UI table `PageContainer`, `Frame`, `Table`, toolbar buttons, row buttons, and CRUD action definitions. |
| `TagDeclarationRules.java` | Zeta rules mapping `UITagDeclaration` to UI `PageContainer`, tag representation `Table`, column filters, and navigation button groups. |
| `ViewActionDeclarationRules.java` | Zeta rules mapping `UIActionDeclaration` to UI `Button`, `Action`, and operation input form, selector, and output `PageDefinition` elements. |
| `ViewDeclarationFormRules.java` | Zeta rules mapping form `UIViewDeclaration` to UI create/edit form `PageContainer`, root `Flex`, and template/create action definitions. |
| `ViewDeclarationRules.java` | Zeta rules mapping non-form `UIViewDeclaration` to UI detail view `PageContainer`, root `Flex`, and CRUD action definitions. |
| `ViewGroupDeclarationRules.java` | Zeta rules `groupVisualElement()`, `groupFrame()`, `groupIcon()`, and `tabGroup()`; transforms `UIViewGroupDeclaration` into UI `Flex`, `Frame`, or `Tab`. |
| `ViewLinkDeclarationRules.java` | Zeta rules mapping `UIViewLinkDeclaration` to UI inline `Link`, button group, representation column, autocomplete, and create-form page definitions. |
| `ViewLinkDeclarationViewPageRules.java` | Zeta rules mapping `UIViewLinkDeclaration` to detail view `PageDefinition` and selector `PageDefinition` with navigation actions. |
| `ViewTableDeclarationRules.java` | Zeta rules mapping `UIViewTableDeclaration` to UI `Table`, cards, tags, columns, filters, toolbar/row button groups, and bulk action definitions. |
| `ViewTableDeclarationViewPageRules.java` | Zeta rules mapping `UIViewTableDeclaration` to detail view `PageDefinition` and selector `PageDefinition` with table operations. |
| `ViewTabsDeclarationRules.java` | Zeta rules `tabBarVisualElement()`, `subTab()`, and `tabsIcon()`; transforms `UIViewTabsDeclaration` into UI `TabController` and tabs. |
| `ViewWidgetDeclarationRules.java` | Zeta rules transforming `UIViewWidgetDeclaration` into UI input components (`TextInput`, `TextArea`, `Checkbox`, `TrinaryLogicCombo`, `NumericInput`, `DateInput`, `DateTimeInput`, `TimeInput`, `BinaryTypeInput`, `EnumerationRadio`, `EnumerationCombo`). |
