# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/entity`

Integration tests for JSL entity declarations, default field expressions, and entity relations mapped to PSM.

| File | Purpose |
| --- | --- |
| `JslEntityDeclaration2PsmEntityTypeTest.java` | Parameterized test across ETL and ZETA validating transformation of JSL entity declarations (concrete, abstract, inherited) into PSM `EntityType` instances. |
| `JslEntityDefaultValue2PsmPrimitiveAccessorTest.java` | Parameterized test across ETL and ZETA validating default values on entity fields mapped to PSM `StaticData` primitive accessors. Loads `TestDefaultExpressionModel.jsl`. |
| `JslEntityRelationDeclaration2PsmRelationTest.java` | Parameterized test across ETL and ZETA validating unidirectional and bidirectional relations, compositions, and cardinalities mapped to PSM `Relation` and `AssociationEnd`. |
