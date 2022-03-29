package hu.blackbelt.judo.tatami.jsl.jsl2psm.entity;

import com.google.common.collect.ImmutableSet;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.calculatePsmValidationScriptURI;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.validatePsm;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.TestUtils.allPsm;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslEntityDeclaration2PsmEntityTypeTest {
    private static final String TEST_SOURCE_MODEL_NAME = "urn:test.judo-meta-jsl";
    private static final String TEST = "JslEntityDeclaration2PsmEntityTypeTest";
    private static final String TARGET_TEST_CLASSES = "target/test-classes/entity";

    Log slf4jlog;
    private JslParser parser;
    JslDslModel jslModel;

    String testName;
    Map<EObject, List<EObject>> resolvedTrace;
    PsmModel psmModel;
    Jsl2PsmTransformationTrace jsl2PsmTransformationTrace;

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @BeforeEach
    void setUp() {
        // Default logger
        slf4jlog = new Slf4jLog(log);
        parser = new JslParser();

        // Loading JSL to isolated ResourceSet, because in Tatami
        // there is no new namespace registration made.
        jslModel = buildJslDslModel().uri(URI.createURI(TEST_SOURCE_MODEL_NAME)).name(TEST).build();

        // Create empty PSM model
        psmModel = buildPsmModel().name(TEST).build();
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
        testName = "TestCreateEntityType";

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

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<EntityType> psmEntityTypes = allPsm(psmModel, EntityType.class).collect(Collectors.toSet());
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
}
