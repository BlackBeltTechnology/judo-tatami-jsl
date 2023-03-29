package hu.blackbelt.judo.tatami.jsl.jsl2ui;

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
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.meta.ui.runtime.UiUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.common.util.UriUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext.etlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.getTransformationTraceFromEtlExecutionContext;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2UiTransformationTrace.JSL_2_UI_URI_POSTFIX;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2UiTransformationTrace.resolveJsl2UiTrace;

@Slf4j
public class Jsl2Ui {

    public static final String SCRIPT_ROOT_TATAMI_JSL_2_UI = "tatami/jsl2ui/transformations/ui/";

    @Builder(builderMethodName = "jsl2UiParameter")
    @Getter
    public static class Jsl2UiParameter {

        @NonNull
        JslDslModel jslModel;

        @NonNull
        UiModel uiModel;

        Log log;

        @Builder.Default
        URI scriptUri = Jsl2Ui.calculateJsl2UiTransformationScriptURI();

        @Builder.Default
        Boolean createTrace = false;

        @Builder.Default
        Boolean parallel = true;

        @Builder.Default
        String applicationType = "default";

        @Builder.Default
        Integer applicationColumns = 12;

        @Builder.Default
        Boolean allRowActions = false;

        @Builder.Default
        Boolean organizeLayout = true;

        @Builder.Default
        LayoutTypeResolver layoutTypeResolver = LayoutDefaults.defaultLayouts();
    }

    public static Jsl2UiTransformationTrace executeJsl2UiTransformation(Jsl2UiParameter.Jsl2UiParameterBuilder builder) throws Exception {
        return executeJsl2UiTransformation(builder.build());
    }

    public static Jsl2UiTransformationTrace executeJsl2UiTransformation(Jsl2UiParameter parameter) throws Exception {
        final AtomicBoolean loggerToBeClosed = new AtomicBoolean(false);
        Log log = Objects.requireNonNullElseGet(parameter.log,
                                                () -> {
                                                    loggerToBeClosed.set(true);
                                                    return new BufferedSlf4jLogger(Jsl2Ui.log);
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
                                    .name("UI")
                                    .resource(parameter.uiModel.getResource())
                                    .build()
                            )
                            .build()
                    )
                    .injectContexts(ImmutableMap.<String, Object>builder()
                            .put("applicationType", parameter.applicationType)
                            .put("applicationColumns", parameter.applicationColumns)
                            .put("allRowActions", parameter.allRowActions)
                            .put("layoutParameterResolver", parameter.layoutTypeResolver)
                            .put("organizeLayout", parameter.organizeLayout)
                            .put("defaultModelName", parameter.jslModel.getName())
                            .put("ecoreUtil", new EcoreUtil())
                            .put("jslUtils", new JslDslModelExtension())
                            .put("uiUtils", new UiUtils()).build())
                    .build();

            // run the model / metadata loading
            executionContext.load();

            etlExecutionContext = etlExecutionContextBuilder()
                    .source(UriUtil.resolve("jslToUi.etl", parameter.scriptUri))
                    .parallel(parameter.parallel)
                    .build();

            // Transformation script
            executionContext.executeProgram(etlExecutionContext);
            executionContext.commit();
            executionContext.close();
        } finally {
            if (loggerToBeClosed.get()) {
                log.close();
            }
        }

        Map<EObject, List<EObject>> traceMap = new ConcurrentHashMap<>();
        if (parameter.createTrace) {
            List<EObject> traceModel = getTransformationTraceFromEtlExecutionContext(JSL_2_UI_URI_POSTFIX, etlExecutionContext);
            traceMap = resolveJsl2UiTrace(traceModel, parameter.jslModel, parameter.uiModel);
        }

        return Jsl2UiTransformationTrace.jsl2UiTransformationTraceBuilder()
                .jslModel(parameter.jslModel)
                .uiModel(parameter.uiModel)
                .trace(traceMap).build();
    }

    @SneakyThrows(URISyntaxException.class)
    public static URI calculateJsl2UiTransformationScriptURI() {
        return calculateURI(SCRIPT_ROOT_TATAMI_JSL_2_UI);
    }

    public static URI calculateURI(String path) throws URISyntaxException {
        URI uiRoot = Jsl2Ui.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        if (uiRoot.toString().endsWith(".jar")) {
            uiRoot = new URI("jar:" + uiRoot.toString() + "!/" + path);
        } else if (uiRoot.toString().startsWith("jar:bundle:")) {
            uiRoot = new URI(uiRoot.toString().substring(4, uiRoot.toString().indexOf("!")) + path);
        } else {
            uiRoot = new URI(uiRoot.toString() + "/" + path);
        }
        return uiRoot;
    }

}
