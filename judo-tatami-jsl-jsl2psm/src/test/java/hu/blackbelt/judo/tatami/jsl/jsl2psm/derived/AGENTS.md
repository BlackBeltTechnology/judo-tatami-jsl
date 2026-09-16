# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/derived`

Integration tests for JSL derived fields, derived relations, parameterized derived features, and JSL-to-JQL expression translation.

| File | Purpose |
| --- | --- |
| `JslEntityDerivedDeclaration2PrimitiveAccessorTest.java` | Parameterized test across ETL and ZETA verifying transformation of primitive derived fields into PSM data properties and accessors. |
| `JslEntityDerivedRelation2PsmRelationTest.java` | Parameterized test across ETL and ZETA validating transformation of derived relations into PSM `NavigationProperty` instances. Loads `TestDerivedRelationModel.jsl`. |
| `JslEntityDerivedWithParametersTest.java` | Parameterized test across ETL and ZETA verifying transformation of parameterized derived entity attributes and relations into PSM navigation properties and data properties. Loads `TestDerivedWithParametersModel.jsl`. |
| `JslExpressionToJqlExpressionTest.java` | Unit tests for `JslExpressionToJqlExpression` verifying serialization and translation of JSL expressions (collection filtering, string functions, arithmetic) to JQL expressions. Loads `TestDerivedExpressionModel.jsl`. |
