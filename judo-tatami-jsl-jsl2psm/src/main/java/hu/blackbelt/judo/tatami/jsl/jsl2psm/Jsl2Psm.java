package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.ExecutionContext.ExecutionContextBuilder;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.api.ModelContext;
import hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;
import hu.blackbelt.judo.meta.psm.PsmUtils;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.common.util.UriUtil;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static hu.blackbelt.epsilon.runtime.execution.ExecutionContext.executionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.contexts.EtlExecutionContext.etlExecutionContextBuilder;
import static hu.blackbelt.epsilon.runtime.execution.model.emf.WrappedEmfModelContext.wrappedEmfModelContextBuilder;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.getTransformationTraceFromEtlExecutionContext;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace.JSL_2_PSM_URI_POSTFIX;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace.resolveJsl2PsmTrace;

public class Jsl2Psm {

    public static final String SCRIPT_ROOT_TATAMI_JSL_2_PSM = "tatami/jsl2psm/transformations/psm/";

    @Builder(builderMethodName = "jsl2PsmParameter")
    public static class Jsl2PsmParameter {

        @NonNull
        JslDslModel jslModel;

        @NonNull
        PsmModel psmModel;

        @Builder.Default
        Log log = new Slf4jLog(LoggerFactory.getLogger(Jsl2Psm.class));

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
}


    public static Jsl2PsmTransformationTrace executeJsl2PsmTransformation(Jsl2PsmParameter.Jsl2PsmParameterBuilder builder) throws Exception {
        return executeJsl2PsmTransformation(builder.build());
    }

    public static Jsl2PsmTransformationTrace executeJsl2PsmTransformation(Jsl2PsmParameter parameter) throws Exception {

        // Execution context
        ExecutionContextBuilder executionContextBuilder = executionContextBuilder();
        
    	ExecutionContext executionContext = executionContextBuilder
                .log(parameter.log)
                .modelContexts(ImmutableList.<ModelContext>builder()
                		.add(wrappedEmfModelContextBuilder()
                                .log(parameter.log)
                                .name("JSL")
                                .resource(parameter.jslModel.getResource())
                                .build()
                                )
                		.add(wrappedEmfModelContextBuilder()
                                .log(parameter.log)
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
						.put("defaultModelName", parameter.jslModel.getName())
						.put("expressionUtils", new JslExpressionToJqlExpression())
						.put("ecoreUtil", new EcoreUtil())
						.put("jslUtils", new JslDslModelExtension())
						.put("psmUtils", new PsmUtils(parameter.psmModel.getResourceSet())).build())
                .build();

        // run the model / metadata loading
        executionContext.load();

        EtlExecutionContext etlExecutionContext = etlExecutionContextBuilder()
                .source(UriUtil.resolve("jslToPsm.etl", parameter.scriptUri))
                .parallel(parameter.parallel)
                .build();

        // Transformation script
        executionContext.executeProgram(etlExecutionContext);
        executionContext.commit();
        executionContext.close();

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
