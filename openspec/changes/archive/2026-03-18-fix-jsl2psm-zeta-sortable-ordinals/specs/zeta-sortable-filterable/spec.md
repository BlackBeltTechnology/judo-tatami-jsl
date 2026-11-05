## ADDED Requirements

### Requirement: isSortable predicate for TransferFieldDeclaration
`Jsl2PsmHelper.isSortable(TransferFieldDeclaration)` SHALL return `true` when all of the following conditions are met:
- The field's `referenceType` is a `DataTypeDeclaration`
- The primitive kind is one of: `string`, `numeric`, `date`, `timestamp`, `time`, `boolean`
- The field is mapped (`<=>`) or reads/derived (`<=`)

It SHALL return `false` otherwise, including when `referenceType` is `null`.

#### Scenario: Mapped string field is sortable
- **WHEN** a TransferFieldDeclaration has `referenceType` of DataTypeDeclaration with primitive `string` and binding `<=>`
- **THEN** `isSortable()` returns `true`

#### Scenario: Transient field is not sortable
- **WHEN** a TransferFieldDeclaration has no mapping (transient field)
- **THEN** `isSortable()` returns `false`

#### Scenario: Binary field is not sortable
- **WHEN** a TransferFieldDeclaration has `referenceType` of DataTypeDeclaration with primitive `binary`
- **THEN** `isSortable()` returns `false`

### Requirement: isFilterable predicate for TransferFieldDeclaration
`Jsl2PsmHelper.isFilterable(TransferFieldDeclaration)` SHALL use the same logic as `isSortable()`.

#### Scenario: Derived numeric field is filterable
- **WHEN** a TransferFieldDeclaration has `referenceType` of DataTypeDeclaration with primitive `numeric` and binding `<=`
- **THEN** `isFilterable()` returns `true`

### Requirement: hasSortableField predicate for TransferDeclaration
`Jsl2PsmHelper.hasSortableField(TransferDeclaration)` SHALL return `true` when at least one member of the transfer declaration is a `TransferFieldDeclaration` for which `isSortable()` returns `true`.

#### Scenario: Transfer with mapped string field has sortable field
- **WHEN** a TransferDeclaration contains a mapped string field
- **THEN** `hasSortableField()` returns `true`

#### Scenario: Transfer with only transient fields has no sortable field
- **WHEN** a TransferDeclaration contains only transient fields
- **THEN** `hasSortableField()` returns `false`
