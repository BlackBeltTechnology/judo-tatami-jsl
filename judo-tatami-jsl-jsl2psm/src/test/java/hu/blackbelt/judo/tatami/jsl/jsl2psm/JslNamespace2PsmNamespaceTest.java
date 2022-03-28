package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.calculatePsmValidationScriptURI;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.validatePsm;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslNamespace2PsmNamespaceTest {
    private final String TEST_SOURCE_MODEL_NAME = "urn:test.judo-meta-jsl";
    private final String TEST = "test";
    private final String TARGET_TEST_CLASSES = "target/test-classes";

    Log slf4jlog;
    private JslParser parser;
    JslDslModel jslModel;

    String testName;
    Map<EObject, List<EObject>> resolvedTrace;
    PsmModel psmModel;
    Jsl2PsmTransformationTrace jsl2PsmTransformationTrace;

    @BeforeEach
    void setUp() {
        // Default logger
        slf4jlog = new Slf4jLog(log);
        parser = new JslParser();

        // Loading JSL to isolated ResourceSet, because in Tatami
        // there is no new namespace registration made.
        jslModel = buildJslDslModel()
                .uri(URI.createURI(TEST_SOURCE_MODEL_NAME))
                .name(TEST)
                .build();

        // Create empty PSM model
        psmModel = buildPsmModel()
                .name(TEST)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        final String traceFileName = testName + "-jsl2psm.model";

        // Saving trace map
        jsl2PsmTransformationTrace.save(new File(TARGET_TEST_CLASSES, traceFileName));

        // Loading trace map
        Jsl2PsmTransformationTrace jsl2PsmTransformationTraceLoaded = Jsl2PsmTransformationTrace.fromModelsAndTrace(
                TEST, jslModel, psmModel, new File(TARGET_TEST_CLASSES, traceFileName));

        // Resolve serialized URI's as EObject map
        resolvedTrace = jsl2PsmTransformationTraceLoaded.getTransformationTrace();

        // Printing trace
        for (EObject e : resolvedTrace.keySet()) {
            for (EObject t : resolvedTrace.get(e)) {
                log.trace(e.toString() + " -> " + t.toString());
            }
        }

        jslModel.saveJslDslModel(jslDslSaveArgumentsBuilder().file(new File(TARGET_TEST_CLASSES, testName + "-jsl.model")));
        psmModel.savePsmModel(psmSaveArgumentsBuilder().file(new File(TARGET_TEST_CLASSES, testName + "-psm.model")));
    }

    private void transform() throws Exception {
        assertTrue(jslModel.isValid());
//        validateJsl(new Slf4jLog(log), jslModel, calculateEsmValidationScriptURI());

        // Make transformation which returns the trace with the serialized URI's
        jsl2PsmTransformationTrace = executeJsl2PsmTransformation(jsl2PsmParameter()
                .jslModel(jslModel)
                .psmModel(psmModel)
                .createTrace(true));

        assertTrue(psmModel.isValid());
        validatePsm(new Slf4jLog(log), psmModel, calculatePsmValidationScriptURI());
    }

    @Test
    void testCreateModel() throws Exception {
        testName = "CreateModel";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "TestModel",
                List.of("model TestModel"));

        jslModel.addContent(model.get());
        transform();

        final Optional<hu.blackbelt.judo.meta.psm.namespace.Model> lookupPSMModel = allPsm(hu.blackbelt.judo.meta.psm.namespace.Model.class)
                .findAny();

        assertTrue(lookupPSMModel.isPresent());
        assertThat(lookupPSMModel.get().getName(), IsEqual.equalTo(model.get().getName()));
    }

//    @Test
//    void testCreatePackage() {
////        testName = "CreatePackage";
//
//        Optional<ModelDeclaration> model = parser.getModelFromStrings(
//                "TestModel",
//                List.of("model TestModel"));
//
//        jslModel.addContent(model.get());
//        transform();
//
//        final Optional<hu.blackbelt.judo.meta.psm.namespace.Package> psmPackage = allPsm(hu.blackbelt.judo.meta.psm.namespace.Package.class)
//                .findAny();
//
//        assertTrue(psmPackage.isPresent());
//        assertThat(psmPackage.get().getName(), IsEqual.equalTo("package"));
//        assertThat(psmPackage.get().getNamespace().getName(), IsEqual.equalTo(model.get().getName()));
        // TODO implement later
//    }

    static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    <T> Stream<T> allPsm() {
        return asStream((Iterator<T>) psmModel.getResourceSet().getAllContents(), false);
    }

    private <T> Stream<T> allPsm(final Class<T> clazz) {
        return allPsm().filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
    }
}
