# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/transferobject`

Integration tests for default, mapped, unmapped, choices-based, and constructor transfer objects mapped to PSM.

| File | Purpose |
| --- | --- |
| `JslEntityDeclaration2PsmDefaultTransferObjectTypeTest.java` | Parameterized test across ETL and ZETA validating default transfer object generation for entities, inheritance hierarchies, and attribute sets. Loads `TestCreateDefaultTransferObjectTypeModel.jsl`. |
| `JslMappedTranferObject2PsmTransferObjectTypeTest.java` | Parameterized test across ETL and ZETA validating explicit mapped transfer object definitions, projected attributes, and relations. Loads `TestCreateMappedTransferObjectTypeModel.jsl`. |
| `JslTransferObjectChoices2PsmTransferObjectTypeTest.java` | Parameterized test across ETL and ZETA verifying choices and select-list specifications on transfer relations mapped to PSM operations and navigation properties. Loads `TestTransferObjectChoicesModel.jsl`. |
| `JslTransferObjectConstructor2PsmTransferObjectTypeTest.java` | Parameterized test across ETL and ZETA validating custom transfer object constructor declarations mapped to PSM creation operations and parameter bindings. Loads `TestTransferObjectConstructorModel.jsl`. |
| `JslUnmappedTranferObject2PsmTransferObjectTypeTest.java` | Parameterized test across ETL and ZETA validating unmapped transfer objects, standalone data fields, and transfer relations. Loads `TestCreateUnmappedTransferObjectTypeModel.jsl`. |
