package hu.blackbelt.judo.tatami.jsl.jsl2ui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.jsldsl.ActorAccessDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.HumanModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.support.JslDslModelResourceSupport;
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.meta.ui.runtime.UiUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.common.util.UriUtil;
import org.slf4j.Logger;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext.etlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;

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

        Logger log;

        @Builder.Default
        java.net.URI scriptUri = Jsl2Ui.calculateJsl2UiTransformationScriptURI();

        @Builder.Default
        Boolean createTrace = false;

        @Builder.Default
        Boolean parallel = true;

    }

    public static void executeJsl2UiTransformation(Jsl2UiParameter.Jsl2UiParameterBuilder builder) throws Exception {
        executeJsl2UiTransformation(builder.build());
    }

    public static void executeJsl2UiTransformation(Jsl2UiParameter parameter) throws Exception {
        final AtomicBoolean loggerToBeClosed = new AtomicBoolean(false);
        Logger log = Objects.requireNonNullElseGet(parameter.log,
                () -> {
                    loggerToBeClosed.set(true);
                    return new BufferedSlf4jLogger(Jsl2Ui.log);
                });

        EtlExecutionContext etlExecutionContext;
        try {
            // Execution context
            ExecutionContext.ExecutionContextBuilder executionContextBuilder = executionContextBuilder();

            JslDslModelResourceSupport jslDslModelResourceSupport = JslDslModelResourceSupport.jslDslModelResourceSupportBuilder()
                    .resourceSet(parameter.jslModel.getResourceSet())
                    .uri(parameter.jslModel.getUri())
                    .build();

            for (ActorDeclaration actorDeclaration : jslDslModelResourceSupport.getStreamOfJsldslActorDeclaration().filter(actorDeclaration -> actorDeclaration.getModifiers().stream().anyMatch(m -> m instanceof HumanModifier)).toList()) {
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
                                .put("actorDeclaration", actorDeclaration)
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
            }
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

        /*Map<EObject, List<EObject>> traceMap = new ConcurrentHashMap<>();
        if (parameter.createTrace) {
            List<EObject> traceModel = getTransformationTraceFromEtlExecutionContext(JSL_2_UI_URI_POSTFIX, etlExecutionContext);
            traceMap = resolveJsl2UiTrace(traceModel, parameter.jslModel, parameter.uiModel);
        }

        return Jsl2UiTransformationTrace.jsl2UiTransformationTraceBuilder()
                .jslModel(parameter.jslModel)
                .uiModel(parameter.uiModel)
                .trace(traceMap).build();
         */
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
