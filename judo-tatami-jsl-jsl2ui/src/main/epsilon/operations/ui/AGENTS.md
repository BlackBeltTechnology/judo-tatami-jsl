# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/epsilon/operations/ui`

EOL operations on UI metamodel elements.

| File | Purpose |
| --- | --- |
| `namedElement.eol` | Navigates container hierarchy to resolve parent `Application`. Exports cached `getRootApplication()` on `UI!ui::NamedElement`; recurses up `eContainer` chain until finding `UI!ui::Application`. |
