# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/type`

Transformation tests asserting conversion of JSL data type declarations into PSM types across ETL and ZETA modes.

| File | Purpose |
| --- | --- |
| `JslBinaryTypeDeclaration2PsmBinaryTypeTest.java` | Tests JSL binary type declaration transformation to PSM binary type with MIME type filtering and max file size constraints under ETL and ZETA modes. |
| `JslBooleanTypeDeclaration2PsmBooleanTypeTest.java` | Tests JSL boolean type declaration and entity attribute transformation to PSM boolean types under ETL and ZETA modes. |
| `JslDateDeclaration2PsmDateTypeTest.java` | Tests JSL date type declaration and entity member mapping to PSM date types under ETL and ZETA modes. |
| `JslEnumDeclaration2PsmEnumerationTypeTest.java` | Tests JSL enum declaration transformation to PSM enumeration types, verifying literal values and ordinals under ETL and ZETA modes. |
| `JslNumericTypeDeclaration2PsmNumericTypeTest.java` | Tests JSL numeric type declaration with precision and scale configurations transformed to PSM numeric types under ETL and ZETA modes. |
| `JslStringTypeDeclaration2PsmStringTypeTest.java` | Tests JSL string type declarations with min/max length and regex constraints mapped to PSM string types under ETL and ZETA modes. |
| `JslTimeTypeDeclaration2PsmTimeTypeTest.java` | Tests JSL time type declaration and entity member transformation to PSM time types under ETL and ZETA modes. |
| `JslTimestampTypeDeclaration2PsmTimestampTypeTest.java` | Tests JSL timestamp type declaration and entity member transformation to PSM timestamp types under ETL and ZETA modes. |
