# AGENTS.md — `src/org/eclipse/epsilon/eol/types`

Patched or custom EOL collection type adapter for Epsilon Object Language runtime.

| File | Purpose |
| --- | --- |
| `EolSet.java` | Extends `EolCollection<T>` and implements `java.util.Set<T>` wrapping `java.util.HashSet`. Exports parameterised generic `EolSet<T>` for EOL script execution. |
