# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui/modules/view`

ETL transformation rules mapping JSL UI view declarations, pages, navigation links, tables, cards, tags, buttons, and visual widgets into UI metamodel elements.

| File | Purpose |
| --- | --- |
| `actionGroupDeclaration.etl` | Transforms JSL `UIActionGroupDeclaration` into UI `ButtonGroup` containing grouped action buttons. |
| `cardDeclaration.etl` | Transforms JSL `UICardDeclaration` into UI `PageContainer`, `Flex` layout, `Table` with card representation, pagination, filter, refresh, and open-page actions. |
| `enumLiteral.etl` | Transforms JSL `EnumLiteral` into UI `Option` representation for enumeration inputs. |
| `frontendDeclaration.etl` | Transforms JSL `UIFrontendDeclaration` into root UI `Application`, default `Theme`, `NavigationController`, and empty dashboard page elements. |
| `menuLinkDeclaration.etl` | Transforms top-level JSL `UIMenuLinkDeclaration` into UI `NavigationItem` mounted onto navigation controller or parent menu group. |
| `menuLinkDeclarationFormPage.etl` | Generates UI create-form `PageDefinition` and associated actions (`back`, `getTemplate`, `create`) for menu links. |
| `menuLinkDeclarationViewPage.etl` | Generates UI view-page `PageDefinition` and operations (`back`, `refresh`, `update`, `delete`) for menu links. |
| `menuTableDeclaration.etl` | Transforms top-level JSL `UIMenuTableDeclaration` into UI `NavigationItem` mounted onto navigation controller or parent group. |
| `menuTableDeclarationAddSelectorPage.etl` | Generates UI selector `PageDefinition` and actions (`add`, `back`, `filter`, `range`) for menu table relations. |
| `menuTableDeclarationCardsPage.etl` | Generates UI cards `PageDefinition` with back, refresh, filter, and open-page actions for menu tables. |
| `menuTableDeclarationFormPage.etl` | Generates UI create-form `PageDefinition` with back, get-template, and create actions for menu table targets. |
| `menuTableDeclarationTablePage.etl` | Generates UI table `PageDefinition` with CRUD, bulk-remove, clear, refresh, filter, and row navigation actions for menu tables. |
| `menuTableDeclarationTagsPage.etl` | Generates UI tags view `PageDefinition` with back, refresh, filter, and open-page actions for menu tables. |
| `menuTableDeclarationViewPage.etl` | Generates UI detail view `PageDefinition` with back, refresh, update, and delete actions for menu table rows. |
| `rowActionDeclaration.etl` | Transforms JSL `UIActionDeclaration` in table rows to UI `Button`, `Action`, and parameter/selector action definitions. |
| `rowColumnDeclaration.etl` | Transforms JSL `UIRowColumnDeclaration` into UI `Column`, `Icon`, and column `Filter` elements. |
| `rowDeclaration.etl` | Transforms JSL `UIRowDeclaration` into UI table `PageContainer`, `Frame`, `Table`, toolbar buttons, row buttons, and CRUD action definitions. |
| `tagDeclaration.etl` | Transforms JSL `UITagDeclaration` into UI tags `PageContainer`, `Table`, filter, refresh, and open-page action definitions. |
| `viewActionDeclaration.etl` | Transforms JSL `UIActionDeclaration` into UI `Button`, `Action`, and input form/selector/output `PageDefinition` structures for operation calls. |
| `viewDeclaration.etl` | Transforms JSL `UIViewDeclaration` into UI detail view `PageContainer`, `Flex` layout, button group, and CRUD action definitions. |
| `viewDeclarationForm.etl` | Transforms JSL `UIViewDeclaration` into UI create/edit form `PageContainer`, `Flex` layout, get-template, and create action definitions. |
| `viewGroupDeclaration.etl` | Transforms JSL `UIViewGroupDeclaration` into UI layout containers (`Flex`, `Frame`, `Tab`, and optional `Icon`). |
| `viewLinkDeclaration.etl` | Transforms JSL `UIViewLinkDeclaration` into UI inline `Link`, button group, representation column, autocomplete, and CRUD action definitions. |
| `viewLinkDeclarationFormPage.etl` | Generates UI create-form `PageDefinition` with create and back actions for view relation links. |
| `viewLinkDeclarationSetSelectorPage.etl` | Generates UI set-selector `PageDefinition` with filter, range, and set actions for view relation links. |
| `viewLinkDeclarationViewPage.etl` | Generates UI view `PageDefinition` and detail/modal actions for navigating view links. |
| `viewTableDeclaration.etl` | Transforms JSL `UIViewTableDeclaration` into UI `Table`, cards, tags, columns, filters, toolbar/row button groups, and bulk action definitions. |
| `viewTableDeclarationAddSelectorPage.etl` | Generates UI add-selector `PageDefinition` with filter, range, and add actions for view tables. |
| `viewTableDeclarationFormPage.etl` | Generates UI create-form `PageDefinition` with create and back actions for view tables. |
| `viewTableDeclarationViewPage.etl` | Generates UI detail view `PageDefinition` and table operations for view tables. |
| `viewTabsDeclaration.etl` | Transforms JSL `UIViewTabsDeclaration` into UI `TabController`, child `Tab` elements, and associated icons. |
| `viewWidgetDeclaration.etl` | Transforms JSL `UIViewWidgetDeclaration` into UI visual input components (`TextInput`, `TextArea`, `Checkbox`, `TrinaryLogicCombo`, `NumericInput`, `DateInput`, `DateTimeInput`, `TimeInput`, `BinaryTypeInput`, `EnumerationRadio`, `EnumerationCombo`). |
