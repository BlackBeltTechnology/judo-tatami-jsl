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


    /**
     * Create UI 2 JSL Trace model {@link Resource} wth isolated {@link ResourceSet}
     *
     * @param uri uri
     * @param uriHandler uriHandler
     *
     * @return the trace {@link Resource} with the registered namespace.
     */
    public static Resource createJsl2UiTraceResource(URI uri, URIHandler uriHandler) {
        return createTraceModelResource(JSL_2_UI_URI_POSTFIX, uri, uriHandler);
    }

    /**
     * Resolves UI 2 JSL Trace model {@link Resource} and returns the trace {@link EObject } map
     *
     * @param traceResource traceResource
     * @param jslModel jslModel
     * @param uiModel uiModel
     *
     * @return the trace {@link EObject} map between UI source and JSL target.
     */
    public static Map<EObject, List<EObject>> resolveJsl2UiTrace(Resource traceResource, JslDslModel jslModel, UiModel uiModel) {
        return resolveJsl2UiTrace(traceResource.getContents(), jslModel, uiModel);
    }

    /**
     * Resolves UI 2 JSL trace:Trace model entries returns the trace {@link EObject } map
     *
     * @param trace trace
     * @param jslModel jslModel
     * @param uiModel uiModel
     *
     * @return the trace {@link EObject} map between UI source and JSL target.
     */
    public static Map<EObject, List<EObject>> resolveJsl2UiTrace(List<EObject> trace, JslDslModel jslModel, UiModel uiModel) {
        return resolveTransformationTraceAsEObjectMap(trace,
                ImmutableList.of(jslModel.getResourceSet(), uiModel.getResourceSet()));
    }

    /**
     * Convert race {@link EObject } map to trace:Trace model entries.
     *
     * @param trace trace
     *
     * @return the trace trace:Trace entries
     */
    public static List<EObject> getJsl2UiTrace(Map<EObject, List<EObject>> trace) {
        return getTransformationTraceFromEtlExecutionContext(JSL_2_UI_URI_POSTFIX, trace);
    }

    /**
     * Convert race {@link EObject } map to trace:Trace model {@link Resource} with isolated {@link ResourceSet}.
     *
     * @param trace trace
     * @param modelUri modelUri
     * @param uriHandler uriHandler
     * @return the trace trace:Trace entries
     */
    public static Resource getJsl2UiTraceResource(Map<EObject, List<EObject>> trace, URI modelUri, URIHandler uriHandler) {
        return createTraceModelResourceFromEObjectMap(trace, JSL_2_UI_URI_POSTFIX, modelUri, uriHandler);
    }

    /**
     * Convert race {@link EObject } map to trace:Trace model {@link Resource} with isolated {@link ResourceSet}.
     *
     * @param trace trace
     * @param modelUri modelUri
     *
     * @return the trace trace:Trace entries
     */
    public static Resource getJsl2UiTraceResource(Map<EObject, List<EObject>> trace, URI modelUri) {
        return createTraceModelResourceFromEObjectMap(trace, JSL_2_UI_URI_POSTFIX, modelUri, null);
    }

    /**
     * Create transformation trace from models and trace inputstream.
     * @param modelName modelName
     * @param jslModel jslModel
     * @param uiModel uiModel
     * @param traceModelFile traceModelFile
     * @return Jsl2UiTransformationTrace
     * @throws IOException exception
     */
    public static Jsl2UiTransformationTrace fromModelsAndTrace(String modelName, JslDslModel jslModel, UiModel uiModel, File traceModelFile) throws IOException {
        return fromModelsAndTrace(modelName, jslModel, uiModel, new FileInputStream(traceModelFile));
    }

    /**
     * Create transformation trace from models and trace inputstream.
     * @param modelName modelName
     * @param jslModel jslModel
     * @param uiModel uiModel
     * @param traceModelInputStream traceModelInputStream
     * @return Jsl2UiTransformationTrace
     * @throws IOException exception
     */
    public static Jsl2UiTransformationTrace fromModelsAndTrace(String modelName,
                                                               JslDslModel jslModel,
                                                               UiModel uiModel,
                                                               InputStream traceModelInputStream) throws IOException {

        checkArgument(jslModel.getName().equals(uiModel.getName()), "Model name does not match");

        Resource traceResourceLoaded = createJsl2UiTraceResource(
                URI.createURI(JSL_2_UI_TRACE_URI_PREFIX + modelName),
                null);

        traceResourceLoaded.load(traceModelInputStream, ImmutableMap.of());

        return Jsl2UiTransformationTrace.jsl2UiTransformationTraceBuilder()
                .uiModel(uiModel)
                .jslModel(jslModel)
                .trace(resolveJsl2UiTrace(traceResourceLoaded, jslModel, uiModel)).build();

    }

    /**
     * Save trace to the given stream.
     *
     * @param outputStream outputStream
     * @return Resource
     * @throws IOException exception
     */
    public Resource save(OutputStream outputStream) throws IOException {
        Resource  traceResourceSaved = getJsl2UiTraceResource(trace, URI.createURI(JSL_2_UI_TRACE_URI_PREFIX + getModelName()));

        traceResourceSaved.save(outputStream, ImmutableMap.of());
        return traceResourceSaved;
    }

    /**
     * Save trace to the given file.
     *
     * @param file file
     * @return Resource
     * @throws IOException exception
     */
    public Resource save(File file) throws IOException {
        return save(new FileOutputStream(file));
    }
}
