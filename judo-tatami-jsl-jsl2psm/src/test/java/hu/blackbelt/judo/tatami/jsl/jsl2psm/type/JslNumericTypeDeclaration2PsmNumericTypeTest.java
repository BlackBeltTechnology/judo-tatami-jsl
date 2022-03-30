package hu.blackbelt.judo.tatami.jsl.jsl2psm.type;

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.Slf4jLog;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
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
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslNumericTypeDeclaration2PsmNumericTypeTest {
    private static final String TEST_SOURCE_MODEL_NAME = "urn:test.judo-meta-jsl";
    private static final String TEST = "JslNumericTypeDeclaration2PsmNumericTypeTest";
    private static final String TARGET_TEST_CLASSES = "target/test-classes/type";

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
    void testDeclaration() throws Exception {
        testName = "TestDeclaration";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "DeclarationModel",
                List.of("model DeclarationModel\n" +
                        "\n" +
                        "type numeric MyNumber precision 12 scale 5\n"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Set<NumericType> psmNumerics = allPsm(psmModel, NumericType.class).collect(Collectors.toSet());
        assertEquals(1, psmNumerics.size());

        final Optional<NumericType> myNumber = psmNumerics.stream().filter(n -> n.getName().equals("MyNumber")).findFirst();
        assertTrue(myNumber.isPresent());
        assertEquals(myNumber.get().getName(), "MyNumber");
        assertEquals(myNumber.get().getPrecision(), 12);
        assertEquals(myNumber.get().getScale(), 5);
    }

    @Test
    void testEntityMember() throws Exception {
        testName = "TestEntityMember";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityMemberModel",
                List.of("model EntityMemberModel\n" +
                        "\n" +
                        "type numeric Height precision 3 scale 0\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Height height\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<NumericType> psmTypeHeight = allPsm(psmModel, NumericType.class).filter(n -> n.getName().equals("Height")).findFirst();
        assertTrue(psmTypeHeight.isPresent());
        final Optional<EntityType> psmEntityPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonHeightAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("height")).findFirst();
        assertTrue(psmPersonHeightAttribute.isPresent());
    }

    @Test
    void testEntityMemberRequired() throws Exception {
        testName = "TestEntityMemberRequired";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityMemberRequiredModel",
                List.of("model EntityMemberRequiredModel\n" +
                        "\n" +
                        "type numeric Height precision 3 scale 0\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield required Height height\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmEntityPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("Person")).findFirst();
        assertTrue(psmEntityPerson.isPresent());
        final Optional<Attribute> psmPersonHeightAttribute = psmEntityPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("height")).findFirst();
        assertTrue(psmPersonHeightAttribute.isPresent());
        assertTrue(psmPersonHeightAttribute.get().isRequired());
    }

    @Test
    void testEntityMemberInheritance() throws Exception {
        testName = "TestEntityMemberInheritance";

        Optional<ModelDeclaration> model = parser.getModelFromStrings(
                "EntityMemberInheritanceModel",
                List.of("model EntityMemberInheritanceModel\n" +
                        "\n" +
                        "type numeric Height precision 3 scale 0\n" +
                        "\n" +
                        "entity Person {\n" +
                        "\tfield Height height\n" +
                        "}\n" +
                        "\n" +
                        "entity StudentPerson extends Person {\n" +
                        "}"
                )
        );

        assertTrue(model.isPresent());

        jslModel.addContent(model.get());
        transform();

        final Optional<EntityType> psmStudentPerson = allPsm(psmModel, EntityType.class).filter(e -> e.getName().equals("StudentPerson")).findFirst();
        assertTrue(psmStudentPerson.isPresent());
        final Optional<Attribute> psmStudentHeightAttribute = psmStudentPerson.get().getAllAttributes().stream().filter(a -> a.getName().equals("height")).findFirst();
        assertTrue(psmStudentHeightAttribute.isPresent());
    }
}
