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

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.support.JslDslModelResourceSupport;
import hu.blackbelt.judo.meta.ui.PageContainer;
import hu.blackbelt.judo.meta.ui.PageDefinition;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.meta.ui.support.UiModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.ui.runtime.UiEpsilonValidator.calculateUiValidationScriptURI;
import static hu.blackbelt.judo.meta.ui.runtime.UiEpsilonValidator.validateUi;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.SaveArguments.uiSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.Jsl2UiParameter.jsl2UiParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.executeJsl2UiTransformation;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
abstract public class AbstractTest {
    protected static String TEST_SOURCE_MODEL_NAME = "urn:test.judo-meta-jsl";

    protected Logger slf4jlog;
    protected JslDslModel jslModel;

    protected String testName;
    protected Map<EObject, List<EObject>> resolvedTrace;
    protected UiModel uiModel;
    protected Jsl2UiTransformationTrace jsl2UiTransformationTrace;

    protected UiModelResourceSupport uiModelWrapper;
    protected JslDslModelResourceSupport jslModelWrapper;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        // Default logger
        testName = testInfo.getTestClass().get().getSimpleName() + "." + testInfo.getTestMethod().get().getName();

    	slf4jlog = createLog();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (slf4jlog instanceof Cloneable) {
            ((Closeable) slf4jlog).close();
        }
        final String traceFileName = testName + "-jsl2ui.model";

        // Saving trace map
        if (jsl2UiTransformationTrace != null) {
            jsl2UiTransformationTrace.save(new File(getTargetTestClasses(), traceFileName));

            // Loading trace map
            Jsl2UiTransformationTrace jsl2UiTransformationTraceLoaded = Jsl2UiTransformationTrace
                    .fromModelsAndTrace(
                            uiModel.getName(),
                            jslModel,
                            uiModel,
                            new File(getTargetTestClasses(), traceFileName)
                    );

            // Resolve serialized URI's as EObject map
            resolvedTrace = jsl2UiTransformationTraceLoaded.getTransformationTrace();

            // Printing trace
            for (EObject e : resolvedTrace.keySet()) {
                for (EObject t : resolvedTrace.get(e)) {
                    log.trace(e.toString() + " -> " + t.toString());
                }
            }
        }

        jslModel.saveJslDslModel(jslDslSaveArgumentsBuilder().validateModel(false).file(new File(getTargetTestClasses(), testName + "-jsl.model")));

        if (uiModel != null) {
            uiModel.saveUiModel(uiSaveArgumentsBuilder().validateModel(false).file(new File(getTargetTestClasses(), testName + "-ui.model")));
            if (!uiModel.isValid()) {
                log.error(uiModel.getDiagnosticsAsString());
            }

            assertTrue(uiModel.isValid());
        }
    }

    protected void transform() throws Exception {
        // Create empty UI model
        uiModel = buildUiModel().name(jslModel.getName()).build();
        uiModelWrapper = UiModelResourceSupport.uiModelResourceSupportBuilder().resourceSet(uiModel.getResourceSet()).build();
        jslModelWrapper = JslDslModelResourceSupport.jslDslModelResourceSupportBuilder().resourceSet(jslModel.getResourceSet()).build();

        assertTrue(jslModel.isValid());
//        validateJsl(log, jslModel, calculateEsmValidationScriptURI());


        // Make transformation which returns the trace with the serialized URI's
        executeJsl2UiTransformation(addTransformationParameters(testName, jsl2UiParameter()
                .log(slf4jlog)
                .jslModel(jslModel)
                .uiModel(uiModel)
                .createTrace(true)));

        if (!uiModel.isValid()) {
            log.error(uiModel.getDiagnosticsAsString());
        }
        assertTrue(uiModel.isValid());
        validateUi(slf4jlog, uiModel, calculateUiValidationScriptURI());
    }

    public Jsl2Ui.Jsl2UiParameter.Jsl2UiParameterBuilder addTransformationParameters(String testName, Jsl2Ui.Jsl2UiParameter.Jsl2UiParameterBuilder parameters) {
        return parameters;
    }

    abstract protected String getTargetTestClasses();

    abstract protected String getTest();

    abstract protected Logger createLog();


    public Set<PageContainer> getPageContainers() {
        return uiModelWrapper.getStreamOfUiPageContainer().collect(Collectors.toSet());
    }

    public Set<PageDefinition> getPageDefinitions() {
        return uiModelWrapper.getStreamOfUiPageDefinition().collect(Collectors.toSet());
    }

    public PageContainer assertPageContainer(String name) {
        final Optional<PageContainer> to = getPageContainers().stream().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(to.isPresent());
        return to.get();
    }

    public PageDefinition assertPageDefinition(String name) {
        final Optional<PageDefinition> to = getPageDefinitions().stream().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(to.isPresent());
        return to.get();
    }

    public static String getXMIID(EObject element) {
        if (element.eResource() instanceof XMIResource) {
            return ((XMIResource)element.eResource()).getID(element);
        }
        throw new IllegalArgumentException("Cannot get XMIID of element: " + element.toString());
    }
}
