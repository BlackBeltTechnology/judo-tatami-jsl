# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2ui/osgi`

OSGi service components tracking JSL models and registering generated UI models dynamically.

| File | Purpose |
| --- | --- |
| `Jsl2UiTransformationJslModelTracker.java` | OSGi Declarative Services component extending `AbstractModelTracker<JslDslModel>`; tracks registered JSL models and publishes resulting `UiModel` OSGi services. |
| `Jsl2UiTransformationService.java` | OSGi Declarative Services component providing `install(JslDslModel)` and `uninstall(JslDslModel)`; resolves bundle ETL script URI and invokes `executeJsl2UiTransformation()`. |
