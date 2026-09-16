# AGENTS.md — `judo-tatami-jsl-workflow-maven-plugin/src/main/java/hu/blackbelt/judo/tatami/jsl/workflow/maven/plugin`

Maven plugin Mojos and artifact resolution utilities executing JSL-to-metamodel compilation workflows.

| File | Purpose |
| --- | --- |
| `AbstractJslDslWorkflowProjectMojo.java` | Base Mojo providing dependency artifact extraction, classpath assembly, and Xtext resource validation via `performExecution()`. |
| `ArtifactResolver.java` | Resolves Maven artifacts and unpacks zip/tar/gzip/bzip2 archives via Aether repository system; exports `getArtifact()`, `iterateArchive()`, and `getResolvedTemplateDirectory()`. |
| `DefaultWorkflowMojo.java` | Maven Mojo `default-model-workflow` binding to COMPILE phase; sets up `DefaultWorkflow` parameters (PSM, ASM, RDBMS, Liquibase) and runs transformation workflow. |
| `DialectParam.java` | JavaBean data container holding database dialect configuration (`rdbms`, `liquibase`, `asm2rdbmsTrace`) for Maven plugin configuration blocks. |
| `ResourceList.java` | Discovers and filters classpath resources across file system directories and jar archives matching a regex `Pattern`. |
