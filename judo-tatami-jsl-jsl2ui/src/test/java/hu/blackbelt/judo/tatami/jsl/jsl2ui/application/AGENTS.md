# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/application`

Application-level transformation test suites asserting conversion of JSL declarations into UI models.

| File | Purpose |
| --- | --- |
| `JSLModel2UiListItemTest.java` | Tests transformation of JSL entities and transfers into UI list item definitions, relation collections, and embedded views under ETL and ZETA modes. |
| `JslModel2UiActionGroupsTest.java` | Tests generation of UI action group operations, bulk actions, and operation bindings from JSL transfer actions. |
| `JslModel2UiApplicationTest.java` | Tests transformation of full JSL models into UI `Application` trees, menus, page containers, and component hierarchies. |
| `JslModel2UiCRUDTest.java` | CRUD-page tests over `TransformationMode` asserting generated UI `Action` flags `getIsBackAction()`/`isOpenSetSelectorAction()` and `getTargetPageDefinition()` targets in `testSummaryCRUD`/`testSingleRelationViewCRUD`/`testRelatedFormCRUD`. Editing the `createModelString` JSL fixture (`relatedCollection`, `theJumpersCollection`) without updating expected navigation asserts fails the suite. |
| `JslModel2UiCarTest.java` | Tests UI model transformation on a Car domain JSL model featuring actors, entities, and data bindings. |
| `JslModel2UiDataTest.java` | Tests mapping of JSL primitives, collections, and relations to UI data elements (`ClassType`, `DataType`, `RelationType`). |
| `JslModel2UiNavigationTest.java` | Tests generation of UI navigation links, master-detail relation bindings, and target page containers. |
| `JslModel2UiOperationsTest.java` | Tests transformation of static and instance JSL actions, input parameters, and error types into UI operations and dialogs. |
| `JslModel2UiRowOperationsTest.java` | Tests table row action generation, row-level event triggers, and action bar bindings in UI table widgets. |
| `JslModel2UiWidgetsTest.java` | Widget-tree tests `testBasicWidgets`/`testRelationWidgets` (models `BasicWidgetsTestModel`/`RelationWidgetsTestModel`) over `TransformationMode` assert `TextInput` instance-of, `TabOrientation.HORIZONTAL`, `CrossAxisAlignment.START`, `isIsTypeAheadField()`, `getLines()`. Changing widget declarations or `getCol()`/`getDirection()` defaults without updating asserts fails. |
