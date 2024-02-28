package hu.blackbelt.judo.tatami.jsl.jsl2ui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.core.TransformationTrace;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;

import java.io.*;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.*;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.createTraceModelResourceFromEObjectMap;

@Builder(builderMethodName = "jsl2UiTransformationTraceBuilder")
public class Jsl2UiTransformationTrace implements TransformationTrace {
    public static final String JSL_2_UI_URI_POSTFIX = "jsl2ui";
    public static final String JSL_2_UI_TRACE_URI_PREFIX = "jsl2uiTrace:";

    @NonNull
    @Getter
    JslDslModel jslModel;

    @NonNull
    @Getter
    UiModel uiModel;

    @NonNull
    Map<EObject, List<EObject>> trace;

    @SuppressWarnings("rawtypes")
    @Override
    public List<Class> getSourceModelTypes() {
        return ImmutableList.of(JslDslModel.class);
    }

    @Override
    public List<Object> getSourceModels() {
        return ImmutableList.of(jslModel);
    }

    @Override
    public <T> T getSourceModel(Class<T> sourceModelType) {
        return null;
    }

    @Override
    public <T> ResourceSet getSourceResourceSet(Class<T> sourceModelType) {
        if (sourceModelType == JslDslModel.class) {
            return jslModel.getResourceSet();
        }
        throw new IllegalArgumentException("Unknown source model type: " + sourceModelType.getName());
    }

    @Override
    public <T> URI getSourceURI(Class<T> sourceModelType) {
        if (sourceModelType == JslDslModel.class) {
            return jslModel.getUri();
        }
        throw new IllegalArgumentException("Unknown source model type: " + sourceModelType.getName());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getTargetModelType() {
        return UiModel.class;
    }

    @Override
    public Object getTargetModel() {
        return uiModel;
    }

    @Override
    public ResourceSet getTargetResourceSet() {
        return uiModel.getResourceSet();
    }

    @Override
    public URI getTargetURI() {
        return uiModel.getUri();
    }

    @Override
    public Class<? extends TransformationTrace> getType() {
        return Jsl2UiTransformationTrace.class;
    }

    @Override
    public String getTransformationTraceName() {
        return "jsl2ui";
    }

    @Override
    public String getModelVersion() {
        return jslModel.getVersion();
    }

    @Override
    public Map<EObject, List<EObject>> getTransformationTrace() {
        return trace;
    }

    @Override
    public String getModelName() {
        return jslModel.getName();
    }

    public static Resource createJsl2UiTraceResource(org.eclipse.emf.common.util.URI uri,
                                                      URIHandler uriHandler) {
        return createTraceModelResource(JSL_2_UI_URI_POSTFIX, uri, uriHandler);
    }

    public static Map<EObject, List<EObject>> resolveJsl2UiTrace(Resource traceResource,
                                                                  JslDslModel jslModel,
                                                                  UiModel uiModel) {
        return resolveJsl2UiTrace(traceResource.getContents(), jslModel, uiModel);
    }

    public static Map<EObject, List<EObject>> resolveJsl2UiTrace(List<EObject> trace,
                                                                  JslDslModel jslModel,
                                                                  UiModel uiModel) {
        return resolveTransformationTraceAsEObjectMap(trace,
                ImmutableList.of(jslModel.getResourceSet(), uiModel.getResourceSet()));
    }

    public static List<EObject> getJsl2UiTrace(Map<EObject, List<EObject>> trace) {
        return getTransformationTraceFromEtlExecutionContext(JSL_2_UI_URI_POSTFIX, trace);
    }

    public static Resource getJsl2UiTraceResource(Map<EObject, List<EObject>> trace,
                                                   org.eclipse.emf.common.util.URI modelUri,
                                                   URIHandler uriHandler) {
        return createTraceModelResourceFromEObjectMap(trace, JSL_2_UI_URI_POSTFIX, modelUri, uriHandler);
    }

    public static Resource getJsl2UiTraceResource(Map<EObject, List<EObject>> trace,
                                                   org.eclipse.emf.common.util.URI modelUri) {
        return createTraceModelResourceFromEObjectMap(trace, JSL_2_UI_URI_POSTFIX, modelUri, null);
    }

    public static Jsl2UiTransformationTrace fromModelsAndTrace(String modelName,
                                                                JslDslModel jslModel,
                                                                UiModel uiModel,
                                                                File traceModelFile) throws IOException {
        return fromModelsAndTrace(modelName, jslModel, uiModel, new FileInputStream(traceModelFile));
    }

    public static Jsl2UiTransformationTrace fromModelsAndTrace(String modelName,
                                                                JslDslModel jslModel,
                                                                UiModel uiModel,
                                                                InputStream traceModelInputStream) throws IOException {

        checkArgument(jslModel.getName().equals(uiModel.getName()), "Model name does not match");

        Resource traceResoureLoaded = createJsl2UiTraceResource(
                URI.createURI(JSL_2_UI_TRACE_URI_PREFIX + modelName),
                null);

        traceResoureLoaded.load(traceModelInputStream, ImmutableMap.of());

        return Jsl2UiTransformationTrace.jsl2UiTransformationTraceBuilder()
                .uiModel(uiModel)
                .jslModel(jslModel)
                .trace(resolveJsl2UiTrace(traceResoureLoaded, jslModel, uiModel)).build();

    }

    public Resource save(OutputStream outputStream) throws IOException {
        Resource  traceResoureSaved = getJsl2UiTraceResource(
                trace,
                URI.createURI(JSL_2_UI_TRACE_URI_PREFIX + getModelName()));

        traceResoureSaved.save(outputStream, ImmutableMap.of());
        return traceResoureSaved;
    }

    public Resource save(File file) throws IOException {
        return save(new FileOutputStream(file));
    }
}
