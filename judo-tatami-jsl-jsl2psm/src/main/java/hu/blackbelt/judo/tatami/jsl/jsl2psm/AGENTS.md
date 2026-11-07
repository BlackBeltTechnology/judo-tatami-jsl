# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm`

Epsilon-free Java orchestration, ETL engine SPI and expression translators for the JSL-to-PSM transformation (Zeta runs here; ETL lives in `judo-tatami-jsl-jsl2psm-etl`) and JSL→JQL expression conversion.

| File | Purpose |
| --- | --- |
| `Jsl2JqlFunction.java` | Translates built-in JSL functions and lambdas (such as timestamp arithmetic) into corresponding JQL function expressions. Exports `getEffectiveFunctionName`, `getEffectiveLambdaName`, `getFunctionAsJql`, `getTimestampPlusFunctionAsJql`. |
| `Jsl2PsmEtlEngine.java` | SPI interface for optional ETL add-on `judo-tatami-jsl-jsl2psm-etl`. Exports `ARTIFACT_COORDINATES` (`hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2psm-etl`), `Jsl2PsmTransformationTrace execute(JslDslModel, PsmModel, Logger, createTrace, generateBehaviours, parallel)`; `log` may be `null`. Resolved by `Jsl2PsmWork` via `EtlEngineRegistry`. See change: split-etl-artifacts. |
| `Jsl2PsmTransformationTrace.java` | Implements `TransformationTrace` for recording and serializing EMF EObject correspondence traces between source JSL and target PSM elements. Exports `createJsl2PsmTraceResource`, `resolveJsl2PsmTrace`, `save`. |
| `Jsl2PsmWork.java` | Work step for JSL→PSM; Zeta in-module, ETL via `Jsl2PsmEtlEngine` add-on. → see `Jsl2PsmWork.java.AGENTS.md`. See change: split-etl-artifacts. |
| `JslExpressionToJqlExpression.java` | Recursively traverses JSL AST expression nodes to render equivalent JQL query strings with prefix/postfix entity names. Exports `getJqlForDerived`, `getJqlForEntityQuery`, `getJqlForStaticQuery`, `getJqlForExpression`. Epsilon-free: unused `Ecl_EolParserRules`/`HashedMap` imports removed. See change: split-etl-artifacts. |
