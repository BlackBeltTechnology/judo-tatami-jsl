# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm/zeta/rules/type`

Zeta transformation rule sets mapping JSL primitive data types and enumerations to PSM type definitions.

| File | Purpose |
| --- | --- |
| `TypeRules.java` | Transformation rules generating PSM primitive types (`NumericType`, `DateType`, `TimeType`, `TimestampType`, `BooleanType`, `StringType`, `BinaryType`) and `EnumerationType` with members. Exports `createNumericType()`, `createStringType()`, `createEnumerationType()`. Applies precision, scale, regex, and mime type modifiers. |
