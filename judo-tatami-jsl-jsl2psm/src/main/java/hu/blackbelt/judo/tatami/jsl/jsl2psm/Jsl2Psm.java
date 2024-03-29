package hu.blackbelt.judo.tatami.jsl.jsl2psm;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext.ExecutionContextBuilder;
import org.slf4j.Logger;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.common.util.UriUtil;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext.etlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.getTransformationTraceFromEtlExecutionContext;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace.JSL_2_PSM_URI_POSTFIX;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace.resolveJsl2PsmTrace;

@Slf4j
public class Jsl2Psm {

    public static final String SCRIPT_ROOT_TATAMI_JSL_2_PSM = "tatami/jsl2psm/transformations/psm/";

    @Builder(builderMethodName = "jsl2PsmParameter")
    @Getter
    public static class Jsl2PsmParameter {

        @NonNull
        JslDslModel jslModel;

        @NonNull
        PsmModel psmModel;

        Logger log;

        @Builder.Default
        java.net.URI scriptUri = Jsl2Psm.calculateJsl2PsmTransformationScriptURI();

        @Builder.Default
        Boolean createTrace = false;

        @Builder.Default
        Boolean parallel = true;

        @Builder.Default
        @NonNull
        String entityNamePrefix = "_";

        @Builder.Default
        @NonNull
        String entityNamePostfix = "";

        @Builder.Default
        @NonNull
        Boolean generateDefaultTransferObject = true;

        @Builder.Default
        @NonNull
        String defaultTransferObjectNamePrefix = "";

        @Builder.Default
        @NonNull
        String defaultTransferObjectNamePostfix = "";

        @Builder.Default
        @NonNull
        String defaultParameterNamePrefix = "_";

        @Builder.Default
        @NonNull
        String defaultParameterNamePostfix = "_Parameters";

        @Builder.Default
        @NonNull
        String defaultParameterNameMidfix = "_";

        @Builder.Default
        @NonNull
        String defaultDefaultNamePrefix = "_";

        @Builder.Default
        @NonNull
        String defaultDefaultNamePostfix = "";

        @Builder.Default
        @NonNull
        String defaultDefaultNameMidfix = "_Default_";

        @Builder.Default
        @NonNull
        String defaultReadsNamePrefix = "_";

        @Builder.Default
        @NonNull
        String defaultReadsNamePostfix = "";

        @Builder.Default
        @NonNull
        String defaultReadsNameMidfix = "_Reads_";

        @Builder.Default
        @NonNull
        String defaultRelationRangeNamePrefix = "_";

        @Builder.Default
        @NonNull
        String defaultRelationRangeNamePostfix = "";

        @Builder.Default
        @NonNull
        String defaultRelationRangeNameMidfix = "_RelationRange_";

        @Builder.Default
        @NonNull
        String defaultActionInputParameterRangeNamePrefix = "_";

        @Builder.Default
        @NonNull
        String defaultActionInputParameterRangeNamePostfix = "";

        @Builder.Default
        @NonNull
        String defaultActionInputParameterRangeNameMidfix = "_ActionInputParameterRange_";
        
        @Builder.Default
        @NonNull
        Boolean generateBehaviours = false;

    }



    public static Jsl2PsmTransformationTrace executeJsl2PsmTransformation(Jsl2PsmParameter.Jsl2PsmParameterBuilder builder) throws Exception {
        return executeJsl2PsmTransformation(builder.build());
    }

    public static Jsl2PsmTransformationTrace executeJsl2PsmTransformation(Jsl2PsmParameter parameter) throws Exception {
        final AtomicBoolean loggerToBeClosed = new AtomicBoolean(false);
        Logger log = Objects.requireNonNullElseGet(parameter.log,
                                                () -> {
                                                    loggerToBeClosed.set(true);
                                                    return new BufferedSlf4jLogger(Jsl2Psm.log);
                                                });

        EtlExecutionContext etlExecutionContext;
        try {
            // Execution context
            ExecutionContextBuilder executionContextBuilder = executionContextBuilder();

            ExecutionContext executionContext = executionContextBuilder
                    .log(log)
                    .modelContexts(ImmutableList.<ModelContext>builder()
                            .add(wrappedEmfModelContextBuilder()
                                    .log(log)
                                    .name("JSL")
                                    .resource(parameter.jslModel.getResource())
                                    .build()
                                    )
                            .add(wrappedEmfModelContextBuilder()
                                    .log(log)
                                    .name("JUDOPSM")
                                    .resource(parameter.psmModel.getResource())
                                    .build()
                            )
                            .build()
                    )
                    .injectContexts(ImmutableMap.<String, Object>builder()
                            .put("entityNamePrefix", parameter.entityNamePrefix)
                            .put("entityNamePostfix", parameter.entityNamePostfix)
                            .put("defaultTransferObjectNamePrefix", parameter.defaultTransferObjectNamePrefix)
                            .put("defaultTransferObjectNamePostfix", parameter.defaultTransferObjectNamePostfix)
                            .put("generateDefaultTransferObject", parameter.generateDefaultTransferObject)
                            .put("defaultParameterNamePrefix", parameter.defaultParameterNamePrefix)
                            .put("defaultParameterNamePostfix", parameter.defaultParameterNamePostfix)
                            .put("defaultParameterNameMidfix", parameter.defaultParameterNameMidfix)
                            .put("defaultDefaultNamePrefix", parameter.defaultDefaultNamePrefix)
                            .put("defaultDefaultNamePostfix", parameter.defaultDefaultNamePostfix)
                            .put("defaultDefaultNameMidfix", parameter.defaultDefaultNameMidfix)
                            .put("defaultReadsNamePrefix", parameter.defaultReadsNamePrefix)
                            .put("defaultReadsNamePostfix", parameter.defaultReadsNamePostfix)
                            .put("defaultReadsNameMidfix", parameter.defaultReadsNameMidfix)
                            .put("defaultRelationRangeNamePrefix", parameter.defaultRelationRangeNamePrefix)
                            .put("defaultRelationRangeNamePostfix", parameter.defaultRelationRangeNamePostfix)
                            .put("defaultRelationRangeNameMidfix", parameter.defaultRelationRangeNameMidfix)
                            .put("defaultActionInputParameterRangeNamePrefix", parameter.defaultActionInputParameterRangeNamePrefix)
                            .put("defaultActionInputParameterRangeNamePostfix", parameter.defaultActionInputParameterRangeNamePostfix)
                            .put("defaultActionInputParameterRangeNameMidfix", parameter.defaultActionInputParameterRangeNameMidfix)                            
                            .put("generateBehaviours", parameter.generateBehaviours)
                            .put("defaultModelName", parameter.jslModel.getName())
                            .put("expressionUtils", new JslExpressionToJqlExpression())
                            .put("ecoreUtil", new EcoreUtil())
                            .put("jslUtils", new JslDslModelExtension())
                            .put("psmUtils", new PsmUtils(parameter.psmModel.getResourceSet())).build())
                    .build();

            // run the model / metadata loading
            executionContext.load();

            etlExecutionContext = etlExecutionContextBuilder()
                    .source(UriUtil.resolve("jslToPsm.etl", parameter.scriptUri))
                    .parallel(parameter.parallel)
                    .build();

            // Transformation script
            executionContext.executeProgram(etlExecutionContext);
            executionContext.commit();
            executionContext.close();
        } finally {
            if (loggerToBeClosed.get()) {
                try {
                    if (log instanceof Closeable) {
                        ((Closeable) log).close();
                    }
                } catch (Exception e) {
                    //noinspection ThrowFromFinallyBlock
                    throw new RuntimeException(e);
                }
            }
        }

        Map<EObject, List<EObject>> traceMap = new ConcurrentHashMap<>();
        if (parameter.createTrace) {
            List<EObject> traceModel = getTransformationTraceFromEtlExecutionContext(JSL_2_PSM_URI_POSTFIX, etlExecutionContext);
            traceMap = resolveJsl2PsmTrace(traceModel, parameter.jslModel, parameter.psmModel);
        }

        return Jsl2PsmTransformationTrace.jsl2PsmTransformationTraceBuilder()
                .jslModel(parameter.jslModel)
                .psmModel(parameter.psmModel)
                .trace(traceMap).build();
    }

    @SneakyThrows(URISyntaxException.class)
    public static URI calculateJsl2PsmTransformationScriptURI() {
        return calculateURI(SCRIPT_ROOT_TATAMI_JSL_2_PSM);
    }

    public static URI calculateURI(String path) throws URISyntaxException {
        URI psmRoot = Jsl2Psm.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        if (psmRoot.toString().endsWith(".jar")) {
            psmRoot = new URI("jar:" + psmRoot.toString() + "!/" + path);
        } else if (psmRoot.toString().startsWith("jar:bundle:")) {
            psmRoot = new URI(psmRoot.toString().substring(4, psmRoot.toString().indexOf("!")) + path);
        } else {
            psmRoot = new URI(psmRoot.toString() + "/" + path);
        }
        return psmRoot;
    }

}
