# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/resources/derived`

JSL test model definitions for derived attributes, relations, and parameterized queries.

| File | Purpose |
| --- | --- |
| `TestDerivedExpressionModel.jsl` | Defines queries and derived expressions with default arguments, ternary expressions, cross-model imports, and member navigations over `Lead` and `SalesPerson`. |
| `TestDerivedExpressionModelInherited.jsl` | Test model declaring `LeadInherited` extending `TestDerivedExpressionModel::Lead` across model imports. |
| `TestDerivedRelationModel.jsl` | Test model defining filtered collection and scalar derived relations (`keyCustomers`, `keyCustomer`) on `Lead` referencing `Customer`. |
| `TestDerivedWithParametersModel.jsl` | Test model declaring static and member-bound parameterized queries with default argument expressions and chained query invocations. |
