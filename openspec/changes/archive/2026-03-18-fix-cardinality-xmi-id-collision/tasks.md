## 1. Fix Cardinality XMI ID collisions

- [x] 1.1 Update 5 `createCardinalityFromModifiable` calls in `TransferRelationRules.java` to use unique suffixes (parent rule name)
- [x] 1.2 Update 4 `createCardinalityFromModifiable` calls in `ContainmentRules.java` to use unique suffixes (parent rule name or context)
- [x] 1.3 Run existing dual tests and performance tests — verify no regression and no `XMI ID COLLISION DETECTED` errors
