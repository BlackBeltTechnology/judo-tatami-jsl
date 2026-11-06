# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/zeta/rules/type`

Zeta transformation rule classes implementing primitive data types and comparison operator enumerations.

| File | Purpose |
| --- | --- |
| `DataTypeOperationRules.java` | Zeta rules generating UI comparison operator enumerations (`BooleanOperation`, `NumericOperation`, `EnumerationOperation`, `StringOperation`) and their respective member literals. |
| `TypeRules.java` | Zeta rules converting JSL `DataTypeDeclaration` and `EnumDeclaration` into UI `NumericType`, `DateType`, `TimeType`, `TimestampType`, `BooleanType`, `StringType`, `BinaryType`, and `EnumerationType`. |
