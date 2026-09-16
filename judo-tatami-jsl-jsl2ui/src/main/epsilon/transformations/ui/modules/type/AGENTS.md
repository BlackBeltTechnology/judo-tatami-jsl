# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/transformations/ui/modules/type`

ETL transformation rules mapping JSL data types, primitives, and enum declarations to UI data types and comparison operation enumerations.

| File | Purpose |
| --- | --- |
| `dataTypeOperation.etl` | Generates UI comparison `EnumerationType` definitions (`BooleanOperation`, `NumericOperation`, `StringOperation`, `EnumerationOperation`) and member operators (`equals`, `lessThan`, `greaterThan`, `like`, `regex`). |
| `type.etl` | Transforms JSL `DataTypeDeclaration` and `EnumDeclaration` into UI `NumericType`, `DateType`, `TimeType`, `TimestampType`, `StringType`, and `EnumerationType` linked to their respective operators. |
