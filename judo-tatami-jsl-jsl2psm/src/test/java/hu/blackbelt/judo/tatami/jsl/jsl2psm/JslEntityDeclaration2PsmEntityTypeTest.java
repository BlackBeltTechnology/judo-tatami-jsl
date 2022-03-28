package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.type.DataType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.calculatePsmValidationScriptURI;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.validatePsm;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEntityDeclaration2PsmEntityTypeTest {
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
    void testCreateEntityType() throws Exception {
        testName = "EntityTypeCreateModel";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityTypeCreateModel",
                List.of("model EntityTypeCreateModel\n" +
                        "\n" +
                        "entity Test {\n" +
                        "}\n" +
                        "entity abstract Person {\n" +
                        "}\n" +
                        "entity SalesPerson extends Person {\n" +
                        "}\n"
                )
        );

        jslModel.addContent(model.get());
        transform();

        final Set<EntityType> psmEntityTypes = allPsm(hu.blackbelt.judo.meta.psm.data.EntityType.class).collect(Collectors.toSet());
        assertEquals(3, psmEntityTypes.size());

        final Set<String> psmEntityTypeNames = psmEntityTypes.stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("Test", "Person", "SalesPerson");
        assertThat(psmEntityTypeNames, IsEqual.equalTo(jslEntityTypeDeclarationNames));

        final Optional<EntityType> psmEntityPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("Person")).findAny();
        assertTrue(psmEntityPerson.isPresent());
        assertTrue(psmEntityPerson.get().isAbstract());

        final Optional<EntityType> psmEntitySalesPerson = psmEntityTypes.stream().filter(e -> e.getName().equals("SalesPerson")).findAny();
        assertTrue(psmEntitySalesPerson.isPresent());

        final Set<String> psmEntityType3SuperTypeNames = psmEntitySalesPerson.get().getSuperEntityTypes().stream().map(NamedElement::getName).collect(Collectors.toSet());
        final Set<String> esmEntityType3SuperTypeNames = ImmutableSet.of("Person");
        assertThat(psmEntityType3SuperTypeNames, IsEqual.equalTo(esmEntityType3SuperTypeNames));
    }

    @Test
    void testNumericType() throws Exception {
        testName = "NumericTypeCreateModel";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "NumericTypeCreateModel",
                List.of("model NumericTypeCreateModel\n" +
                        "\n" +
                        "type numeric MyNumber precision 12 scale 5\n"
                )
        );

        jslModel.addContent(model.get());
        transform();

        final Set<NumericType> psmNumerics = allPsm(NumericType.class).collect(Collectors.toSet());
        assertEquals(1, psmNumerics.size());
//
//        final Set<String> psmPrimitiveNames = psmNumerics.stream().map(NamedElement::getName).collect(Collectors.toSet());
//        final Set<String> jslEntityTypeDeclarationNames = ImmutableSet.of("MyNumber");
//        assertThat(psmPrimitiveNames, IsEqual.equalTo(jslEntityTypeDeclarationNames));



//        final Optional<EntityType> psmEntityWithCustomTypes = psmEntityTypes.stream().filter(e -> e.getName().equals("NumericTypeCreateModel")).findAny();
//        assertTrue(psmEntityWithCustomTypes.isPresent());
//        assertTrue(psmEntityWithCustomTypes.get().getAttributes().stream().filter(a -> a.getName().equals("requiredInteger")).findFirst().get().isRequired());
    }

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
