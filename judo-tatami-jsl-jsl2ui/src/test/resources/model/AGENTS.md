# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/test/resources/model`

JSL DSL model fixtures used by JSL-to-UI transformation unit tests to verify UI generation across tables, forms, views, cards, tags, operations, and navigation hierarchies.

| File | Purpose |
| --- | --- |
| `AccessFormsRelationActions.jsl` | Defines `model AccessFormsRelationActions` with `User`, `Related`, and `Jumper` entities and nested relation actions for testing access form navigation actions in `JslModel2UiCRUDTest.testAccessFormsRelationActions`. |
| `AccessTableRowOperations.jsl` | Defines `model AccessTableRowOperations` with `Entity1`, `Entity2`, mapped transfers `Transfer1`, `Transfer3`, and `TransferX`, testing table-row action invocation and parameter forms. |
| `AccessViewCRUD.jsl` | Defines `model AccessViewCRUD` with `User`, `Related`, and `Jumper` entities, used by `JslModel2UiCRUDTest.testAccessViewCRUD` to verify view CRUD actions, links, and table buttons. |
| `ActionGroupModel.jsl` | Defines `model ActionGroupModel` with `Item` entity, `ItemTransfer`, `ItemView`, and operations action group on `TestActor`'s menu table. |
| `ActorWithMenuLinkModel.jsl` | Defines `model ActorWithMenuLinkModel` with human actor `TestActor`, `TestActorTransfer`, and profile view link in actor menu. |
| `ActorWithMenuModel.jsl` | Defines `model ActorWithMenuModel` with `Person` entity, `PersonTransfer` with update event, and table menu bound to `TestActor.persons`. |
| `ApplicationTestModel.jsl` | Defines minimal `model ApplicationTestModel` with empty `AppActor` and `AppMenu` frontend definition. |
| `BasicDataCrossTransfersTestModel.jsl` | Defines `model BasicDataCrossTransfersTestModel` with `Entity1`, `Entity2`, and cross-transfers `Transfer1` and `Transfer2` bound to `TestApp` menu tables. |
| `BasicDataTestModel.jsl` | Defines `model BasicDataTestModel` with `User` entity containing all core data types (binary, boolean, date, numeric, string, time, timestamp, enum) and mapped/transient/derived fields in `UserTransfer`. |
| `BasicWidgetsTestModel.jsl` | Defines `model BasicWidgetsTestModel` mapping primitive types and enums to UI widgets (`StringWidget`, `NumericWidget`, `BinaryWidget`, `DateWidget`, `TimeWidget`, `TimestampWidget`, `ComboWidget`, `RadioWidget`) in multi-level groups and tabs. |
| `CRUDModel.jsl` | Defines `model CRUDModel` with `Item` entity, `ItemTransfer` supporting create, update, and delete events, and `TestMenu` table. |
| `Car.jsl` | Defines `model Car` with `User` and `Car` entities, mapped transfers, `CarForm`, `CarView`, and `CarApp` menu table with form and view bindings. |
| `Cards.jsl` | Defines `model Cards` with `Entity1`, `Entity2`, and nested `Card1`/`Card2` and `Tag1`/`Tag2` widget definitions for card and tag layout tests. |
| `DialogTestModel.jsl` | Defines `model DialogTestModel` with `dialog:true` views (`UserView`, `RelatedView`) testing modal dialog transformation for nested relations. |
| `EnumModel.jsl` | Defines `model EnumModel` with `Status` enum (`ACTIVE`, `INACTIVE`, `PENDING`), `Task` entity, and `ComboWidget` bound to enum fields. |
| `LinkOperations.jsl` | Defines `model LinkOperations` with entity operations, grouped actions, and table actions across `Transfer1`, `Transfer2`, and `TransferX`. |
| `MenuTableWithFormPage.jsl` | Defines `model MenuTableWithFormPage` with `Product` entity, calculated price string, and menu table referencing `ProductForm`. |
| `MenuTestModel.jsl` | Defines `model MenuTestModel` with `User` and `Product` entities, nested menu groups (`Group1`, `Group2`), and multiple menu tables. |
| `MultipleActorsTestModel.jsl` | Defines `model MultipleActorsTestModel` with two actors (`Actor1`, `Actor2`), separate frontends (`App1`, `App2`), and shared product entities. |
| `NavigationTestModel.jsl` | Defines `model NavigationTestModel` with nested three-tier entity structure (`User` -> `Related` -> `Jumper`) testing multi-level page and relation navigation. |
| `OperationsOnViews.jsl` | Defines `model OperationsOnViews` with custom error declarations (`Error1`, `ErrorWithDefaults`), operations throwing errors, and operation input selector/form actions on `ViewX`. |
| `OperationsOnViewsWithInputForms.jsl` | Defines `model OperationsOnViewsWithInputForms` with error declarations and operation input form actions on `ViewX` for verifying operation dialog form generation. |
| `OperationsOnViewsWithInputSelectors.jsl` | Defines `model OperationsOnViewsWithInputSelectors` with operation input selector actions on `ViewX` for verifying operation lookup selector table generation. |
| `ParameterlessVoidOperationsOnViews.jsl` | Defines `model ParameterlessVoidOperationsOnViews` with parameterless void operations throwing errors on `ViewX`. |
| `ProfileModel.jsl` | Defines `model ProfileModel` with `User` and `Product` entities, testing frontend `profile` section linking to `UserView`. |
| `RelatedFormCRUD.jsl` | Defines `model RelatedFormCRUD` with `User`, `Related`, and `Jumper` entities, used by `JslModel2UiCRUDTest.testRelatedFormCRUD` to verify related form generation and CRUD action buttons. |
| `RelatedRowDetailViewCRUD.jsl` | Defines `model RelatedRowDetailViewCRUD` with `User`, `Related`, and `Jumper` entities, testing row detail view pages and action buttons for collection relations. |
| `RelationWidgetsTestModel.jsl` | Defines `model RelationWidgetsTestModel` testing single and collection relation widgets with `RelatedView`, `RelatedForm`, and `RelatedRow` selectors. |
| `RelationsModel.jsl` | Defines `model RelationsModel` with `Category` and `Product` entities and relation links inside `ProductView`. |
| `RelationsTestModel.jsl` | Defines `model RelationsTestModel` testing eager, lazy, static, derived, transient, opposite, and unmapped relation variants. |
| `RowModel.jsl` | Defines `model RowModel` with `Person` entity and `PersonTransfer` supporting update and delete events on menu table rows. |
| `SecurityTestModel.jsl` | Defines `model SecurityTestModel` with `COMPANY` security realm and `email` claim identity mapping for `Actor`. |
| `SimpleActorModel.jsl` | Defines minimal `model SimpleActorModel` with empty `TestActor` and `TestMenu`. |
| `SingleRelationViewCRUD.jsl` | Defines `model SingleRelationViewCRUD` with `User`, `Related`, and `Jumper` entities, used by `JslModel2UiCRUDTest.testSingleRelationViewCRUD` to verify single-relation view page definitions, set selectors, and unset actions. |
| `StackOverFlowTestModel.jsl` | Defines `model StackOverFlowTestModel` with four mutually referencing entities (`A`, `B`, `C`, `D`) creating cyclical relation graphs to test recursion handling. |
| `SummaryCRUD.jsl` | Defines `model SummaryCRUD` with `User`, `Related`, and `Jumper` entities, used by `JslModel2UiCRUDTest.testSummaryCRUD` to test summary view and access page generation. |
| `TableOperations.jsl` | Defines `model TableOperations` with mapped/unmapped transfers and operation action definitions on tables and forms. |
| `TabsModel.jsl` | Defines `model TabsModel` with `Order` entity, `OrderView`, and tabbed layout container with `basic` and `advanced` tabs. |
| `Tags.jsl` | Defines `model Tags` with `Entity1`, `Entity2`, and `Tag1`/`Tag2` element definitions bound to transfer fields. |
| `WidgetsModel.jsl` | Defines `model WidgetsModel` with `Record` entity and detail view exercising `StringWidget`, `NumericWidget`, `BooleanWidget`, `DateWidget`, and `TimestampWidget`. |
| `testRelationFormsRelationActions.jsl` | Defines `model testRelationFormsRelationActions` with `User`, `Related`, and `Jumper` entities, testing relation form navigation actions in `JslModel2UiCRUDTest.testRelationFormsRelationActions`. |
