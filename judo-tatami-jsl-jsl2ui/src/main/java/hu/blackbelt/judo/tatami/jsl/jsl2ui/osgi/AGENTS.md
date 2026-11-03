# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/osgi`

OSGi service components tracking JSL models and registering generated UI models dynamically. Epsilon-free; ZETA by default.

| File | Purpose |
| --- | --- |
| `Jsl2UiTransformationJslModelTracker.java` | OSGi Declarative Services component extending `AbstractModelTracker<JslDslModel>`; tracks registered JSL models and publishes resulting `UiModel` OSGi services. |
| `Jsl2UiTransformationService.java` | OSGi DS service building `UiModel` via `Jsl2UiWork` (ZETA default; ETL needs the `-etl` bundle) → see `Jsl2UiTransformationService.java.AGENTS.md`. See change: split-etl-artifacts. |
