package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.core.TransformationTrace;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.createTraceModelResource;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.createTraceModelResourceFromEObjectMap;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.getTransformationTraceFromEtlExecutionContext;
import static hu.blackbelt.judo.tatami.core.TransformationTraceUtil.resolveTransformationTraceAsEObjectMap;

@Builder(builderMethodName = "jsl2PsmTransformationTraceBuilder")
public class Jsl2PsmTransformationTrace implements TransformationTrace {

    public static final String JSL_2_PSM_URI_POSTFIX = "jsl2psm";
    public static final String JSL_2_PSM_TRACE_URI_PREFIX = "jsl2psmTrace:";

    @NonNull
    @Getter
    JslDslModel jslModel;

    @NonNull
    @Getter
    PsmModel psmModel;

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
        return PsmModel.class;
    }

    @Override
    public Object getTargetModel() {
        return psmModel;
    }

    @Override
    public ResourceSet getTargetResourceSet() {
        return psmModel.getResourceSet();
    }

    @Override
    public URI getTargetURI() {
        return psmModel.getUri();
    }

    @Override
    public Class<? extends TransformationTrace> getType() {
        return Jsl2PsmTransformationTrace.class;
    }

    @Override
    public String getTransformationTraceName() {
        return "jsl2psm";
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


    /**
     * Create PSM 2 JSL Trace model {@link Resource} wth isolated {@link ResourceSet}
     *
     * @param uri
     * @param uriHandler
     *
     * @return the trace {@link Resource} with the registered namespace.
     */
    public static Resource createJsl2PsmTraceResource(org.eclipse.emf.common.util.URI uri,
                                                        URIHandler uriHandler) {
        return createTraceModelResource(JSL_2_PSM_URI_POSTFIX, uri, uriHandler);
    }

    /**
     * Resolves PSM 2 JSL Trace model {@link Resource} and resturns the trace {@link EObject } map
     *
     * @param traceResource
     * @param jslModel
     * @param psmModel
     *
     * @return the trace {@link EObject} map between PSM source and JSL target.
     */
    public static Map<EObject, List<EObject>> resolveJsl2PsmTrace(Resource traceResource,
                                                                    JslDslModel jslModel,
                                                                    PsmModel psmModel) {
        return resolveJsl2PsmTrace(traceResource.getContents(), jslModel, psmModel);
    }

    /**
     * Resolves PSM 2 JSL trace:Trace model entries returns the trace {@link EObject } map
     *
     * @param trace
     * @param jslModel
     * @param psmModel
     *
     * @return the trace {@link EObject} map between PSM source and JSL target.
     */
    public static Map<EObject, List<EObject>> resolveJsl2PsmTrace(List<EObject> trace,
                                                                    JslDslModel jslModel,
                                                                    PsmModel psmModel) {
        return resolveTransformationTraceAsEObjectMap(trace,
                ImmutableList.of(jslModel.getResourceSet(), psmModel.getResourceSet()));
    }

    /**
     * Convert race {@link EObject } map to trace:Trace model entrie.
     *
     * @param trace
     *
     * @return the trace trace:Trace entries
     */
    public static List<EObject> getJsl2PsmTrace(Map<EObject, List<EObject>> trace) {
        return getTransformationTraceFromEtlExecutionContext(JSL_2_PSM_URI_POSTFIX, trace);
    }

    /**
     * Convert race {@link EObject } map to trace:Trace model {@link Resource} with isolated {@link ResourceSet}.
     *
     * @param trace
     * @param modelUri
     * @param uriHandler
     * @return the trace trace:Trace entries
     */
    public static Resource getJsl2PsmTraceResource(Map<EObject, List<EObject>> trace,
                                                     org.eclipse.emf.common.util.URI modelUri,
                                                     URIHandler uriHandler) {
        return createTraceModelResourceFromEObjectMap(trace, JSL_2_PSM_URI_POSTFIX, modelUri, uriHandler);
    }

    /**
     * Convert race {@link EObject } map to trace:Trace model {@link Resource} with isolated {@link ResourceSet}.
     *
     * @param trace
     * @param modelUri
     *
     * @return the trace trace:Trace entries
     */
    public static Resource getJsl2PsmTraceResource(Map<EObject, List<EObject>> trace,
                                                     org.eclipse.emf.common.util.URI modelUri) {
        return createTraceModelResourceFromEObjectMap(trace, JSL_2_PSM_URI_POSTFIX, modelUri, null);
    }

    /**
     * Create transformation trace from models and trace inputstream.
     * @param modelName
     * @param jslModel
     * @param psmModel
     * @param traceModelFile
     * @return
     * @throws IOException
     */
    public static Jsl2PsmTransformationTrace fromModelsAndTrace(String modelName,
                                                                  JslDslModel jslModel,
                                                                  PsmModel psmModel,
                                                                  File traceModelFile) throws IOException {
        return fromModelsAndTrace(modelName, jslModel, psmModel, new FileInputStream(traceModelFile));
    }

    /**
     * Create transformation trace from models and trace inputstream.
     * @param modelName
     * @param jslModel
     * @param psmModel
     * @param traceModelInputStream
     * @return
     * @throws IOException
     */
    public static Jsl2PsmTransformationTrace fromModelsAndTrace(String modelName,
                                                                  JslDslModel jslModel,
                                                                  PsmModel psmModel,
                                                                  InputStream traceModelInputStream) throws IOException {

        checkArgument(jslModel.getName().equals(psmModel.getName()), "Model name does not match");

        Resource traceResoureLoaded = createJsl2PsmTraceResource(
                URI.createURI(JSL_2_PSM_TRACE_URI_PREFIX + modelName),
                null);

        traceResoureLoaded.load(traceModelInputStream, ImmutableMap.of());

        return Jsl2PsmTransformationTrace.jsl2PsmTransformationTraceBuilder()
                .psmModel(psmModel)
                .jslModel(jslModel)
                .trace(resolveJsl2PsmTrace(traceResoureLoaded, jslModel, psmModel)).build();

    }

    /**
     * Save trace to the given stream.
     *
     * @param outputStream
     * @return
     * @throws IOException
     */
    public Resource save(OutputStream outputStream) throws IOException {
        Resource  traceResoureSaved = getJsl2PsmTraceResource(
                trace,
                URI.createURI(JSL_2_PSM_TRACE_URI_PREFIX + getModelName()));

        traceResoureSaved.save(outputStream, ImmutableMap.of());
        return traceResoureSaved;
    }

    /**
     * Save trace to the given file.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public Resource save(File file) throws IOException {
        return save(new FileOutputStream(file));
    }
}
