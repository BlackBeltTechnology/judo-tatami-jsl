# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/resources/transferobject`

JSL test model definitions for transfer object mappings, unmapped transfers, choices, and constructor defaults.

| File | Purpose |
| --- | --- |
| `TestCreateDefaultTransferObjectTypeModel.jsl` | Test model declaring multi-inheritance entities (`SalesPerson`, `PersonWithAge`, `Named`) and opposite relations for default transfer object generation. |
| `TestCreateMappedTransferObjectTypeModel.jsl` | Test model covering mapped transfer objects with mapped, derived, static, opposite, and eager containment relations. |
| `TestCreateUnmappedTransferObjectTypeModel.jsl` | Test model declaring unmapped transfer objects with transient attributes, required relations, and derived queries over entities. |
| `TestTransferObjectChoicesModel.jsl` | Test model covering relation choice expressions and action choice parameters on mapped and unmapped transfer objects. |
| `TestTransferObjectConstructorModel.jsl` | Test model defining transfer object field and relation default values inherited from ancestor entities and explicitly overridden in transfer constructors. |
