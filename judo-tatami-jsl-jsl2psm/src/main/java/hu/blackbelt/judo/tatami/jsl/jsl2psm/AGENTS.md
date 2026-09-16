# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/main/java/hu/blackbelt/judo/tatami/jsl/jsl2psm`

Java orchestrators and expression translators for driving the JSL-to-PSM transformation pipeline and converting JSL expressions to JQL queries.

| File | Purpose |
| --- | --- |
| `Jsl2JqlFunction.java` | Translates built-in JSL functions and lambdas (such as timestamp arithmetic) into corresponding JQL function expressions. Exports `getEffectiveFunctionName`, `getEffectiveLambdaName`, `getFunctionAsJql`, `getTimestampPlusFunctionAsJql`. |
| `Jsl2Psm.java` | Executes JSL-to-PSM ETL transformation against provided JSL and PSM models. Exports `Jsl2Psm`, `Jsl2PsmParameter`, `executeJsl2PsmTransformation`, `calculateJsl2PsmTransformationScriptURI`. Requires valid non-null `jslModel` and `psmModel`. |
| `Jsl2PsmTransformationTrace.java` | Implements `TransformationTrace` for recording and serializing EMF EObject correspondence traces between source JSL and target PSM elements. Exports `createJsl2PsmTraceResource`, `resolveJsl2PsmTrace`, `save`. |
| `Jsl2PsmWork.java` | Extends `AbstractTransformationWork` to wrap JSL-to-PSM transformation into runnable workflow steps or standalone CLI execution. Exports `Jsl2PsmWork`, `Jsl2PsmWorkParameter`, `execute`, `main`. |
| `JslExpressionToJqlExpression.java` | Recursively traverses JSL AST expression nodes to render equivalent JQL query strings with prefix/postfix entity names. Exports `getJqlForDerived`, `getJqlForEntityQuery`, `getJqlForStaticQuery`, `getJqlForExpression`. |
