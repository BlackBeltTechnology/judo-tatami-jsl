package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.support.JslDslModelResourceSupport;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.List;
import java.util.Map;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.calculatePsmValidationScriptURI;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.validatePsm;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
abstract public class AbstractTest {
    protected static String TEST_SOURCE_MODEL_NAME = "urn:test.judo-meta-jsl";

    protected Log slf4jlog;
    protected JslParser parser;
    protected JslDslModel jslModel;

    protected String testName;
    protected Map<EObject, List<EObject>> resolvedTrace;
    protected PsmModel psmModel;
    protected Jsl2PsmTransformationTrace jsl2PsmTransformationTrace;

    protected PsmModelResourceSupport psmModelWrapper;
    protected JslDslModelResourceSupport jslModelWrapper;

    @BeforeEach
    void setUp() {
        // Default logger
        slf4jlog = createLog();
        parser = new JslParser();
    }

    @AfterEach
    void tearDown() throws Exception {
        final String traceFileName = testName + "-jsl2psm.model";

        // Saving trace map
        jsl2PsmTransformationTrace.save(new File(getTargetTestClasses(), traceFileName));

        // Loading trace map
        Jsl2PsmTransformationTrace jsl2PsmTransformationTraceLoaded = Jsl2PsmTransformationTrace
                .fromModelsAndTrace(
                        psmModel.getName(),
                        jslModel,
                        psmModel,
                        new File(getTargetTestClasses(), traceFileName)
                );

        // Resolve serialized URI's as EObject map
        resolvedTrace = jsl2PsmTransformationTraceLoaded.getTransformationTrace();

        // Printing trace
        for (EObject e : resolvedTrace.keySet()) {
            for (EObject t : resolvedTrace.get(e)) {
                log.trace(e.toString() + " -> " + t.toString());
            }
        }

        jslModel.saveJslDslModel(jslDslSaveArgumentsBuilder().file(new File(getTargetTestClasses(), testName + "-jsl.model")));
        psmModel.savePsmModel(psmSaveArgumentsBuilder().file(new File(getTargetTestClasses(), testName + "-psm.model")));
    }

    protected void transform() throws Exception {
        // Create empty PSM model
        psmModel = buildPsmModel().name(jslModel.getName()).build();
        psmModelWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder().resourceSet(psmModel.getResourceSet()).build();
        jslModelWrapper = JslDslModelResourceSupport.jslDslModelResourceSupportBuilder().resourceSet(jslModel.getResourceSet()).build();

    	
    	assertTrue(jslModel.isValid());
//        validateJsl(new Slf4jLog(log), jslModel, calculateEsmValidationScriptURI());

        
        // Make transformation which returns the trace with the serialized URI's
        jsl2PsmTransformationTrace = executeJsl2PsmTransformation(jsl2PsmParameter()
        		.log(slf4jlog)
                .jslModel(jslModel)
                .psmModel(psmModel)
                .createTrace(true));

        assertTrue(psmModel.isValid());
        validatePsm(createLog(), psmModel, calculatePsmValidationScriptURI());
    }

    abstract protected String getTargetTestClasses();

    abstract protected String getTest();

    abstract protected Log createLog();
}
