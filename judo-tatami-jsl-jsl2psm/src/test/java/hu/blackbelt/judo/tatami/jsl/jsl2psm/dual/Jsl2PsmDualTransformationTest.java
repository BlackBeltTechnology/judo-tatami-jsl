package hu.blackbelt.judo.tatami.jsl.jsl2psm.dual;

import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.accesspoint.AbstractActorType;
import hu.blackbelt.judo.meta.psm.accesspoint.ActorType;
import hu.blackbelt.judo.meta.psm.accesspoint.MappedActorType;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.type.EnumerationMember;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmZetaTransformation;
import hu.blackbelt.judo.tatami.test.util.ModelComparator;
import hu.blackbelt.judo.tatami.test.util.ModelComparator.ComparisonMode;
import hu.blackbelt.judo.tatami.test.util.ModelComparator.ComparisonResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Dual transformation test comparing ETL and Zeta outputs.
 *
 * Runs the same JSL model through both ETL and Zeta transformations,
 * then compares the resulting PSM models to ensure they produce equivalent output.
 *
 * This test focuses on the rules implemented so far (namespace + type).
 */
@Slf4j
public class Jsl2PsmDualTransformationTest {

    private static final String TARGET_TEST_CLASSES = "target/test-classes/dual";

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    private Logger createLog() {
        return new BufferedSlf4jLogger(log);
    }

    private PsmModel executeEtl(JslDslModel jslModel) throws Exception {
        PsmModel psmModel = buildPsmModel().build();
        executeJsl2PsmTransformation(jsl2PsmParameter()
                .log(createLog())
                .jslModel(jslModel)
                .psmModel(psmModel)
                .parallel(true)
                .useCache(true)
                .generateBehaviours(false)
                .createTrace(false));
        return psmModel;
    }

    private PsmModel executeZeta(JslDslModel jslModel) {
        PsmModel psmModel = buildPsmModel().build();
        Jsl2PsmZetaTransformation.builder()
                .jslModel(jslModel)
                .psmModel(psmModel)
                .defaultModelName(jslModel.getName())
                .build()
                .execute();
        return psmModel;
    }

    @Test
    void testNamespace_simpleModel() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Verify both have a model
        Optional<Model> etlModel = etlWrapper.getStreamOfPsmNamespaceModel().findAny();
        Optional<Model> zetaModel = zetaWrapper.getStreamOfPsmNamespaceModel().findAny();
        assertTrue(etlModel.isPresent(), "ETL should create a Model");
        assertTrue(zetaModel.isPresent(), "Zeta should create a Model");
        assertEquals(etlModel.get().getName(), zetaModel.get().getName(),
                "Model names should match");

        // Verify both have the same packages
        long etlPackageCount = etlWrapper.getStreamOfPsmNamespacePackage().count();
        long zetaPackageCount = zetaWrapper.getStreamOfPsmNamespacePackage().count();
        assertEquals(etlPackageCount, zetaPackageCount,
                "Package count should match");

        log.info("DUAL TEST namespace_simpleModel: ETL model={}, Zeta model={}, packages ETL={}, Zeta={}",
                etlModel.get().getName(), zetaModel.get().getName(), etlPackageCount, zetaPackageCount);
    }

    @Test
    void testNamespace_qualifiedModel() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model com::example::TestModel;"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare model names
        Optional<Model> etlModel = etlWrapper.getStreamOfPsmNamespaceModel().findAny();
        Optional<Model> zetaModel = zetaWrapper.getStreamOfPsmNamespaceModel().findAny();
        assertTrue(etlModel.isPresent());
        assertTrue(zetaModel.isPresent());
        assertEquals(etlModel.get().getName(), zetaModel.get().getName());

        // Compare package hierarchy
        List<String> etlPackages = etlWrapper.getStreamOfPsmNamespacePackage()
                .map(Package::getName).sorted().collect(Collectors.toList());
        List<String> zetaPackages = zetaWrapper.getStreamOfPsmNamespacePackage()
                .map(Package::getName).sorted().collect(Collectors.toList());
        assertEquals(etlPackages, zetaPackages,
                "Package names should match (ETL=" + etlPackages + ", Zeta=" + zetaPackages + ")");

        log.info("DUAL TEST namespace_qualifiedModel: packages ETL={}, Zeta={}", etlPackages, zetaPackages);
    }

    @Test
    void testTypes_stringType() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string MyString min-size:0 max-size:255 regex:\"[a-z]+\";\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare string types
        Optional<StringType> etlString = etlWrapper.getStreamOfPsmTypeStringType()
                .filter(s -> "MyString".equals(s.getName())).findAny();
        Optional<StringType> zetaString = zetaWrapper.getStreamOfPsmTypeStringType()
                .filter(s -> "MyString".equals(s.getName())).findAny();

        assertTrue(etlString.isPresent(), "ETL should create MyString StringType");
        assertTrue(zetaString.isPresent(), "Zeta should create MyString StringType");
        assertEquals(etlString.get().getMaxLength(), zetaString.get().getMaxLength(),
                "MaxLength should match");
        assertEquals(etlString.get().getRegExp(), zetaString.get().getRegExp(),
                "RegExp should match");

        log.info("DUAL TEST types_stringType: ETL maxLength={}, regex={}, Zeta maxLength={}, regex={}",
                etlString.get().getMaxLength(), etlString.get().getRegExp(),
                zetaString.get().getMaxLength(), zetaString.get().getRegExp());
    }

    @Test
    void testTypes_numericType() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type numeric MyDecimal precision:10 scale:2;\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        Optional<NumericType> etlNumeric = etlWrapper.getStreamOfPsmTypeNumericType()
                .filter(n -> "MyDecimal".equals(n.getName())).findAny();
        Optional<NumericType> zetaNumeric = zetaWrapper.getStreamOfPsmTypeNumericType()
                .filter(n -> "MyDecimal".equals(n.getName())).findAny();

        assertTrue(etlNumeric.isPresent(), "ETL should create MyDecimal NumericType");
        assertTrue(zetaNumeric.isPresent(), "Zeta should create MyDecimal NumericType");
        assertEquals(etlNumeric.get().getPrecision(), zetaNumeric.get().getPrecision(),
                "Precision should match");
        assertEquals(etlNumeric.get().getScale(), zetaNumeric.get().getScale(),
                "Scale should match");

        log.info("DUAL TEST types_numericType: ETL precision={} scale={}, Zeta precision={} scale={}",
                etlNumeric.get().getPrecision(), etlNumeric.get().getScale(),
                zetaNumeric.get().getPrecision(), zetaNumeric.get().getScale());
    }

    @Test
    void testTypes_enumeration() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "enum Color {\n" +
                        "  RED = 0;\n" +
                        "  GREEN = 1;\n" +
                        "  BLUE = 2;\n" +
                        "}"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        Optional<EnumerationType> etlEnum = etlWrapper.getStreamOfPsmTypeEnumerationType()
                .filter(e -> "Color".equals(e.getName())).findAny();
        Optional<EnumerationType> zetaEnum = zetaWrapper.getStreamOfPsmTypeEnumerationType()
                .filter(e -> "Color".equals(e.getName())).findAny();

        assertTrue(etlEnum.isPresent(), "ETL should create Color EnumerationType");
        assertTrue(zetaEnum.isPresent(), "Zeta should create Color EnumerationType");
        assertEquals(etlEnum.get().getMembers().size(), zetaEnum.get().getMembers().size(),
                "Member count should match");

        // Compare member names and ordinals
        for (int i = 0; i < etlEnum.get().getMembers().size(); i++) {
            EnumerationMember etlMember = etlEnum.get().getMembers().get(i);
            EnumerationMember zetaMember = zetaEnum.get().getMembers().get(i);
            assertEquals(etlMember.getName(), zetaMember.getName(),
                    "Member " + i + " name should match");
            assertEquals(etlMember.getOrdinal(), zetaMember.getOrdinal(),
                    "Member " + i + " ordinal should match");
        }

        log.info("DUAL TEST types_enumeration: ETL members={}, Zeta members={}",
                etlEnum.get().getMembers().size(), zetaEnum.get().getMembers().size());
    }

    @Test
    void testTypes_allPrimitives() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string MyString min-size:0 max-size:100;\n" +
                        "type numeric MyDecimal precision:10 scale:2;\n" +
                        "type boolean MyBoolean;\n" +
                        "type date MyDate;\n" +
                        "type time MyTime;\n" +
                        "type timestamp MyTimestamp;\n" +
                        "type binary MyBinary mime-type:[\"application/octet-stream\"] max-file-size:1 MB;\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Count types by category
        long etlStringTypes = etlWrapper.getStreamOfPsmTypeStringType().count();
        long zetaStringTypes = zetaWrapper.getStreamOfPsmTypeStringType().count();
        long etlNumericTypes = etlWrapper.getStreamOfPsmTypeNumericType().count();
        long zetaNumericTypes = zetaWrapper.getStreamOfPsmTypeNumericType().count();
        long etlBooleanTypes = etlWrapper.getStreamOfPsmTypeBooleanType().count();
        long zetaBooleanTypes = zetaWrapper.getStreamOfPsmTypeBooleanType().count();
        long etlDateTypes = etlWrapper.getStreamOfPsmTypeDateType().count();
        long zetaDateTypes = zetaWrapper.getStreamOfPsmTypeDateType().count();
        long etlTimeTypes = etlWrapper.getStreamOfPsmTypeTimeType().count();
        long zetaTimeTypes = zetaWrapper.getStreamOfPsmTypeTimeType().count();
        long etlTimestampTypes = etlWrapper.getStreamOfPsmTypeTimestampType().count();
        long zetaTimestampTypes = zetaWrapper.getStreamOfPsmTypeTimestampType().count();
        long etlBinaryTypes = etlWrapper.getStreamOfPsmTypeBinaryType().count();
        long zetaBinaryTypes = zetaWrapper.getStreamOfPsmTypeBinaryType().count();

        assertEquals(etlStringTypes, zetaStringTypes, "StringType count should match");
        assertEquals(etlNumericTypes, zetaNumericTypes, "NumericType count should match");
        assertEquals(etlBooleanTypes, zetaBooleanTypes, "BooleanType count should match");
        assertEquals(etlDateTypes, zetaDateTypes, "DateType count should match");
        assertEquals(etlTimeTypes, zetaTimeTypes, "TimeType count should match");
        assertEquals(etlTimestampTypes, zetaTimestampTypes, "TimestampType count should match");
        assertEquals(etlBinaryTypes, zetaBinaryTypes, "BinaryType count should match");

        log.info("DUAL TEST types_allPrimitives: All primitive type counts match between ETL and Zeta");
    }

    // === Phase 2: Entity/Data/Derived tests ===

    @Test
    void testEntity_simpleEntity() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String firstName;\n" +
                        "  field String lastName;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare entity types
        long etlEntityCount = etlWrapper.getStreamOfPsmDataEntityType().count();
        long zetaEntityCount = zetaWrapper.getStreamOfPsmDataEntityType().count();
        assertEquals(etlEntityCount, zetaEntityCount, "EntityType count should match");

        // Check entity name (prefixed with _)
        Optional<EntityType> etlEntity = etlWrapper.getStreamOfPsmDataEntityType()
                .filter(e -> e.getName().contains("Person")).findAny();
        Optional<EntityType> zetaEntity = zetaWrapper.getStreamOfPsmDataEntityType()
                .filter(e -> e.getName().contains("Person")).findAny();
        assertTrue(etlEntity.isPresent(), "ETL should create Person entity");
        assertTrue(zetaEntity.isPresent(), "Zeta should create Person entity");
        assertEquals(etlEntity.get().getName(), zetaEntity.get().getName(),
                "Entity names should match");

        // Compare attributes
        long etlAttrCount = etlWrapper.getStreamOfPsmDataAttribute().count();
        long zetaAttrCount = zetaWrapper.getStreamOfPsmDataAttribute().count();
        assertEquals(etlAttrCount, zetaAttrCount, "Attribute count should match");

        log.info("DUAL TEST entity_simpleEntity: entities ETL={}, Zeta={}, attributes ETL={}, Zeta={}",
                etlEntityCount, zetaEntityCount, etlAttrCount, zetaAttrCount);
    }

    @Test
    void testEntity_inheritance() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Animal abstract {\n" +
                        "  field String name;\n" +
                        "}\n" +
                        "\n" +
                        "entity Dog extends Animal {\n" +
                        "  field String breed;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare entity count
        long etlEntityCount = etlWrapper.getStreamOfPsmDataEntityType().count();
        long zetaEntityCount = zetaWrapper.getStreamOfPsmDataEntityType().count();
        assertEquals(etlEntityCount, zetaEntityCount, "EntityType count should match");

        // Check abstract entity
        Optional<EntityType> etlAnimal = etlWrapper.getStreamOfPsmDataEntityType()
                .filter(e -> e.getName().contains("Animal")).findAny();
        Optional<EntityType> zetaAnimal = zetaWrapper.getStreamOfPsmDataEntityType()
                .filter(e -> e.getName().contains("Animal")).findAny();
        assertTrue(etlAnimal.isPresent());
        assertTrue(zetaAnimal.isPresent());
        assertEquals(etlAnimal.get().isAbstract(), zetaAnimal.get().isAbstract(),
                "Abstract flag should match");

        // Check Dog inherits from Animal
        Optional<EntityType> etlDog = etlWrapper.getStreamOfPsmDataEntityType()
                .filter(e -> e.getName().contains("Dog")).findAny();
        Optional<EntityType> zetaDog = zetaWrapper.getStreamOfPsmDataEntityType()
                .filter(e -> e.getName().contains("Dog")).findAny();
        assertTrue(etlDog.isPresent());
        assertTrue(zetaDog.isPresent());
        assertEquals(etlDog.get().getSuperEntityTypes().size(), zetaDog.get().getSuperEntityTypes().size(),
                "Super entity types count should match");

        log.info("DUAL TEST entity_inheritance: entities ETL={}, Zeta={}, Animal abstract={}, Dog supers={}",
                etlEntityCount, zetaEntityCount,
                zetaAnimal.get().isAbstract(), zetaDog.get().getSuperEntityTypes().size());
    }

    @Test
    void testEntity_containment() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Address {\n" +
                        "  field String city;\n" +
                        "}\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String name;\n" +
                        "  field Address address;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare containments
        long etlContainments = etlWrapper.getStreamOfPsmDataContainment().count();
        long zetaContainments = zetaWrapper.getStreamOfPsmDataContainment().count();
        assertEquals(etlContainments, zetaContainments,
                "Containment count should match (ETL=" + etlContainments + ", Zeta=" + zetaContainments + ")");

        // Check containment details
        Optional<Containment> etlC = etlWrapper.getStreamOfPsmDataContainment()
                .filter(c -> "address".equals(c.getName())).findAny();
        Optional<Containment> zetaC = zetaWrapper.getStreamOfPsmDataContainment()
                .filter(c -> "address".equals(c.getName())).findAny();
        assertTrue(etlC.isPresent(), "ETL should create address containment");
        assertTrue(zetaC.isPresent(), "Zeta should create address containment");

        log.info("DUAL TEST entity_containment: containments ETL={}, Zeta={}",
                etlContainments, zetaContainments);
    }

    @Test
    void testEntity_association() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Department {\n" +
                        "  field String name;\n" +
                        "}\n" +
                        "\n" +
                        "entity Employee {\n" +
                        "  field String name;\n" +
                        "  relation Department department;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare association ends
        long etlAssociations = etlWrapper.getStreamOfPsmDataAssociationEnd().count();
        long zetaAssociations = zetaWrapper.getStreamOfPsmDataAssociationEnd().count();
        assertEquals(etlAssociations, zetaAssociations,
                "AssociationEnd count should match (ETL=" + etlAssociations + ", Zeta=" + zetaAssociations + ")");

        log.info("DUAL TEST entity_association: associations ETL={}, Zeta={}",
                etlAssociations, zetaAssociations);
    }

    @Test
    void testDerived_calculatedField() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String firstName;\n" +
                        "  field String lastName;\n" +
                        "  field String fullName <= self.firstName + \" \" + self.lastName;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare data properties (derived/calculated fields)
        long etlDataProps = etlWrapper.getStreamOfPsmDerivedDataProperty().count();
        long zetaDataProps = zetaWrapper.getStreamOfPsmDerivedDataProperty().count();
        assertEquals(etlDataProps, zetaDataProps,
                "DataProperty count should match (ETL=" + etlDataProps + ", Zeta=" + zetaDataProps + ")");

        log.info("DUAL TEST derived_calculatedField: dataProperties ETL={}, Zeta={}",
                etlDataProps, zetaDataProps);
    }

    @Test
    void testDerived_calculatedRelation() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Company {\n" +
                        "  field String name;\n" +
                        "}\n" +
                        "\n" +
                        "entity Employee {\n" +
                        "  field String name;\n" +
                        "  relation Company company;\n" +
                        "  relation Company myCompany <= self.company;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare navigation properties (derived entity relations)
        long etlNavProps = etlWrapper.getStreamOfPsmDerivedNavigationProperty().count();
        long zetaNavProps = zetaWrapper.getStreamOfPsmDerivedNavigationProperty().count();
        assertEquals(etlNavProps, zetaNavProps,
                "NavigationProperty count should match (ETL=" + etlNavProps + ", Zeta=" + zetaNavProps + ")");

        log.info("DUAL TEST derived_calculatedRelation: navProperties ETL={}, Zeta={}",
                etlNavProps, zetaNavProps);
    }

    // === Phase 3: Structure/Transfer Object tests ===

    @Test
    void testDefaultTransferObject_simpleEntity() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String firstName;\n" +
                        "  field String lastName;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare mapped transfer object types (default TOs for entities)
        long etlMappedTOs = etlWrapper.getStreamOfPsmServiceMappedTransferObjectType().count();
        long zetaMappedTOs = zetaWrapper.getStreamOfPsmServiceMappedTransferObjectType().count();
        assertEquals(etlMappedTOs, zetaMappedTOs,
                "MappedTransferObjectType count should match (ETL=" + etlMappedTOs + ", Zeta=" + zetaMappedTOs + ")");

        // Check that the Person entity has a default representation
        Optional<MappedTransferObjectType> etlPersonTO = etlWrapper.getStreamOfPsmServiceMappedTransferObjectType()
                .filter(to -> to.getName().contains("Person")).findAny();
        Optional<MappedTransferObjectType> zetaPersonTO = zetaWrapper.getStreamOfPsmServiceMappedTransferObjectType()
                .filter(to -> to.getName().contains("Person")).findAny();

        assertTrue(etlPersonTO.isPresent(), "ETL should create Person MappedTransferObjectType");
        assertTrue(zetaPersonTO.isPresent(), "Zeta should create Person MappedTransferObjectType");
        assertEquals(etlPersonTO.get().getName(), zetaPersonTO.get().getName(),
                "TO names should match");

        // Compare transfer attributes
        assertEquals(etlPersonTO.get().getAttributes().size(), zetaPersonTO.get().getAttributes().size(),
                "Transfer attribute count should match (ETL=" + etlPersonTO.get().getAttributes().size()
                + ", Zeta=" + zetaPersonTO.get().getAttributes().size() + ")");

        log.info("DUAL TEST defaultTransferObject: mapped TOs ETL={}, Zeta={}, Person attrs ETL={}, Zeta={}",
                etlMappedTOs, zetaMappedTOs,
                etlPersonTO.get().getAttributes().size(), zetaPersonTO.get().getAttributes().size());
    }

    @Test
    void testDefaultTransferObject_withRelations() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Address {\n" +
                        "  field String city;\n" +
                        "}\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String name;\n" +
                        "  field Address homeAddress;\n" +
                        "  relation Address workAddress;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare transfer object relations
        long etlRelations = etlWrapper.getStreamOfPsmServiceTransferObjectRelation().count();
        long zetaRelations = zetaWrapper.getStreamOfPsmServiceTransferObjectRelation().count();
        assertEquals(etlRelations, zetaRelations,
                "TransferObjectRelation count should match (ETL=" + etlRelations + ", Zeta=" + zetaRelations + ")");

        log.info("DUAL TEST defaultTransferObject_withRelations: relations ETL={}, Zeta={}",
                etlRelations, zetaRelations);
    }

    @Test
    void testTransferDeclaration_mappedTransfer() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String firstName;\n" +
                        "  field String lastName;\n" +
                        "}\n" +
                        "\n" +
                        "transfer PersonView(Person p) {\n" +
                        "  field String firstName <= p.firstName;\n" +
                        "  field String lastName <= p.lastName;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare mapped transfer object types
        long etlMappedTOs = etlWrapper.getStreamOfPsmServiceMappedTransferObjectType().count();
        long zetaMappedTOs = zetaWrapper.getStreamOfPsmServiceMappedTransferObjectType().count();
        assertEquals(etlMappedTOs, zetaMappedTOs,
                "MappedTransferObjectType count should match (ETL=" + etlMappedTOs + ", Zeta=" + zetaMappedTOs + ")");

        // Check PersonView exists
        Optional<MappedTransferObjectType> etlPersonView = etlWrapper.getStreamOfPsmServiceMappedTransferObjectType()
                .filter(to -> "PersonView".equals(to.getName())).findAny();
        Optional<MappedTransferObjectType> zetaPersonView = zetaWrapper.getStreamOfPsmServiceMappedTransferObjectType()
                .filter(to -> "PersonView".equals(to.getName())).findAny();

        assertTrue(etlPersonView.isPresent(), "ETL should create PersonView");
        assertTrue(zetaPersonView.isPresent(), "Zeta should create PersonView");
        assertEquals(etlPersonView.get().getAttributes().size(), zetaPersonView.get().getAttributes().size(),
                "PersonView attribute count should match");

        // Compare total transfer attributes
        long etlAttrs = etlWrapper.getStreamOfPsmServiceTransferAttribute().count();
        long zetaAttrs = zetaWrapper.getStreamOfPsmServiceTransferAttribute().count();
        assertEquals(etlAttrs, zetaAttrs,
                "Total TransferAttribute count should match (ETL=" + etlAttrs + ", Zeta=" + zetaAttrs + ")");

        log.info("DUAL TEST transferDeclaration_mapped: mapped TOs ETL={}, Zeta={}, total attrs ETL={}, Zeta={}",
                etlMappedTOs, zetaMappedTOs, etlAttrs, zetaAttrs);
    }

    @Test
    void testTransferDeclaration_unmappedTransfer() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "transfer SearchParams {\n" +
                        "  field String searchTerm;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare unmapped transfer object types
        long etlUnmappedTOs = etlWrapper.getStreamOfPsmServiceUnmappedTransferObjectType().count();
        long zetaUnmappedTOs = zetaWrapper.getStreamOfPsmServiceUnmappedTransferObjectType().count();
        assertEquals(etlUnmappedTOs, zetaUnmappedTOs,
                "UnmappedTransferObjectType count should match (ETL=" + etlUnmappedTOs + ", Zeta=" + zetaUnmappedTOs + ")");

        // Check SearchParams exists
        Optional<UnmappedTransferObjectType> etlSearch = etlWrapper.getStreamOfPsmServiceUnmappedTransferObjectType()
                .filter(to -> "SearchParams".equals(to.getName())).findAny();
        Optional<UnmappedTransferObjectType> zetaSearch = zetaWrapper.getStreamOfPsmServiceUnmappedTransferObjectType()
                .filter(to -> "SearchParams".equals(to.getName())).findAny();

        assertTrue(etlSearch.isPresent(), "ETL should create SearchParams");
        assertTrue(zetaSearch.isPresent(), "Zeta should create SearchParams");
        assertEquals(etlSearch.get().getAttributes().size(), zetaSearch.get().getAttributes().size(),
                "SearchParams attribute count should match");

        log.info("DUAL TEST transferDeclaration_unmapped: unmapped TOs ETL={}, Zeta={}",
                etlUnmappedTOs, zetaUnmappedTOs);
    }

    @Test
    void testTransferDeclaration_transferWithRelation() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Address {\n" +
                        "  field String city;\n" +
                        "}\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String name;\n" +
                        "  relation Address address;\n" +
                        "}\n" +
                        "\n" +
                        "transfer AddressView(Address a) {\n" +
                        "  field String city <= a.city;\n" +
                        "}\n" +
                        "\n" +
                        "transfer PersonView(Person p) {\n" +
                        "  field String name <= p.name;\n" +
                        "  relation AddressView address <= p.address;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare all transfer object types
        long etlTOs = etlWrapper.getStreamOfPsmServiceTransferObjectType().count();
        long zetaTOs = zetaWrapper.getStreamOfPsmServiceTransferObjectType().count();
        assertEquals(etlTOs, zetaTOs,
                "TransferObjectType count should match (ETL=" + etlTOs + ", Zeta=" + zetaTOs + ")");

        // Compare transfer relations
        long etlRelations = etlWrapper.getStreamOfPsmServiceTransferObjectRelation().count();
        long zetaRelations = zetaWrapper.getStreamOfPsmServiceTransferObjectRelation().count();
        assertEquals(etlRelations, zetaRelations,
                "TransferObjectRelation count should match (ETL=" + etlRelations + ", Zeta=" + zetaRelations + ")");

        log.info("DUAL TEST transferWithRelation: TOs ETL={}, Zeta={}, relations ETL={}, Zeta={}",
                etlTOs, zetaTOs, etlRelations, zetaRelations);
    }

    @Test
    void testDefaultTransferObject_inheritance() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Animal abstract {\n" +
                        "  field String name;\n" +
                        "}\n" +
                        "\n" +
                        "entity Dog extends Animal {\n" +
                        "  field String breed;\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare mapped transfer objects
        long etlMappedTOs = etlWrapper.getStreamOfPsmServiceMappedTransferObjectType().count();
        long zetaMappedTOs = zetaWrapper.getStreamOfPsmServiceMappedTransferObjectType().count();
        assertEquals(etlMappedTOs, zetaMappedTOs,
                "MappedTransferObjectType count should match (ETL=" + etlMappedTOs + ", Zeta=" + zetaMappedTOs + ")");

        // Check Dog's default TO has inherited attribute (name from Animal)
        Optional<MappedTransferObjectType> etlDogTO = etlWrapper.getStreamOfPsmServiceMappedTransferObjectType()
                .filter(to -> to.getName().contains("Dog")).findAny();
        Optional<MappedTransferObjectType> zetaDogTO = zetaWrapper.getStreamOfPsmServiceMappedTransferObjectType()
                .filter(to -> to.getName().contains("Dog")).findAny();

        assertTrue(etlDogTO.isPresent());
        assertTrue(zetaDogTO.isPresent());
        assertEquals(etlDogTO.get().getAttributes().size(), zetaDogTO.get().getAttributes().size(),
                "Dog TO should have same number of attributes (including inherited) (ETL="
                + etlDogTO.get().getAttributes().size() + ", Zeta=" + zetaDogTO.get().getAttributes().size() + ")");

        log.info("DUAL TEST defaultTransferObject_inheritance: mapped TOs ETL={}, Zeta={}, Dog attrs ETL={}, Zeta={}",
                etlMappedTOs, zetaMappedTOs,
                etlDogTO.get().getAttributes().size(), zetaDogTO.get().getAttributes().size());
    }

    // ========================
    // Actor type tests
    // ========================

    @Test
    void testActorType_anonymousActor() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "actor AnonymousActor {\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare ActorType counts
        long etlActorTypes = etlWrapper.getStreamOfPsmAccesspointActorType().count();
        long zetaActorTypes = zetaWrapper.getStreamOfPsmAccesspointActorType().count();
        assertEquals(etlActorTypes, zetaActorTypes,
                "ActorType count should match (ETL=" + etlActorTypes + ", Zeta=" + zetaActorTypes + ")");

        // Compare unmapped transfer objects (metadata types)
        long etlUnmapped = etlWrapper.getStreamOfPsmServiceUnmappedTransferObjectType().count();
        long zetaUnmapped = zetaWrapper.getStreamOfPsmServiceUnmappedTransferObjectType().count();
        assertEquals(etlUnmapped, zetaUnmapped,
                "UnmappedTransferObjectType count should match (includes metadata types) (ETL=" + etlUnmapped + ", Zeta=" + zetaUnmapped + ")");

        // Compare UnboundOperation counts (_metadata operation)
        long etlOps = etlWrapper.getStreamOfPsmServiceUnboundOperation().count();
        long zetaOps = zetaWrapper.getStreamOfPsmServiceUnboundOperation().count();
        assertEquals(etlOps, zetaOps,
                "UnboundOperation count should match (ETL=" + etlOps + ", Zeta=" + zetaOps + ")");

        log.info("DUAL TEST actorType_anonymous: ActorTypes ETL={}, Zeta={}, UnmappedTOs ETL={}, Zeta={}, Operations ETL={}, Zeta={}",
                etlActorTypes, zetaActorTypes, etlUnmapped, zetaUnmapped, etlOps, zetaOps);
    }

    @Test
    void testActorType_mappedActorWithPrincipal() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "type boolean Boolean;\n" +
                        "\n" +
                        "entity User {\n" +
                        "  identifier String email;\n" +
                        "}\n" +
                        "\n" +
                        "transfer UserTransfer maps User as u {\n" +
                        "  field String email <=> u.email;\n" +
                        "}\n" +
                        "\n" +
                        "actor MyActor\n" +
                        "  realm: \"COMPANY\"\n" +
                        "  claim: \"email\"\n" +
                        "  identity: UserTransfer::email\n" +
                        "{\n" +
                        "}\n"));

        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        PsmModelResourceSupport etlWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(etlResult.getResourceSet()).build();
        PsmModelResourceSupport zetaWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder()
                .resourceSet(zetaResult.getResourceSet()).build();

        // Compare MappedActorType counts
        long etlMapped = etlWrapper.getStreamOfPsmAccesspointMappedActorType().count();
        long zetaMapped = zetaWrapper.getStreamOfPsmAccesspointMappedActorType().count();
        assertEquals(etlMapped, zetaMapped,
                "MappedActorType count should match (ETL=" + etlMapped + ", Zeta=" + zetaMapped + ")");

        // Check realm on the MappedActorType
        Optional<MappedActorType> etlActor = etlWrapper.getStreamOfPsmAccesspointMappedActorType()
                .filter(a -> "MyActor".equals(a.getName())).findAny();
        Optional<MappedActorType> zetaActor = zetaWrapper.getStreamOfPsmAccesspointMappedActorType()
                .filter(a -> "MyActor".equals(a.getName())).findAny();
        assertTrue(etlActor.isPresent(), "ETL should create MappedActorType 'MyActor'");
        assertTrue(zetaActor.isPresent(), "Zeta should create MappedActorType 'MyActor'");
        assertEquals(etlActor.get().getRealm(), zetaActor.get().getRealm(),
                "Realm should match");

        // Compare UnboundOperation counts (_metadata + _principal)
        long etlOps = etlWrapper.getStreamOfPsmServiceUnboundOperation().count();
        long zetaOps = zetaWrapper.getStreamOfPsmServiceUnboundOperation().count();
        assertEquals(etlOps, zetaOps,
                "UnboundOperation count should match (ETL=" + etlOps + ", Zeta=" + zetaOps + ")");

        // Compare total element count
        long etlElements = etlWrapper.getStreamOfPsmNamespaceNamespaceElement().count();
        long zetaElements = zetaWrapper.getStreamOfPsmNamespaceNamespaceElement().count();

        log.info("DUAL TEST actorType_mapped: MappedActorTypes ETL={}, Zeta={}, Operations ETL={}, Zeta={}, Elements ETL={}, Zeta={}",
                etlMapped, zetaMapped, etlOps, zetaOps, etlElements, zetaElements);
    }

    // ========================
    // Deep ModelComparator tests
    // ========================

    private void assertDeepEquivalence(String testName, JslDslModel jslModel) throws Exception {
        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        org.eclipse.emf.ecore.resource.Resource etlResource = etlResult.getResourceSet().getResources().get(0);
        org.eclipse.emf.ecore.resource.Resource zetaResource = zetaResult.getResourceSet().getResources().get(0);

        ComparisonResult result = ModelComparator.compare(etlResource, zetaResource, ComparisonMode.STRICT);

        if (!result.isEquivalent()) {
            String msg = "DEEP COMPARISON FAILED for " + testName + ": " + result.getDifferenceCount() + " differences\n"
                    + result.getSummary() + "\n" + result.getDetailedReport();
            log.error(msg);
            System.err.println(msg);
        } else {
            log.info("DEEP COMPARISON PASSED for {}: zero differences", testName);
        }

        assertTrue(result.isEquivalent(),
                "Deep comparison failed for " + testName + ":\n" + result.getSummary() + "\n" + result.getDetailedReport());
    }

    @Test
    void testDeepComparison_simpleEntity() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "type numeric Integer precision:9 scale:0;\n" +
                        "type boolean Boolean;\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String firstName;\n" +
                        "  field String lastName;\n" +
                        "  field Integer age;\n" +
                        "}\n"));

        assertDeepEquivalence("simpleEntity", jslModel);
    }

    @Test
    void testDeepComparison_entityWithRelations() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Address {\n" +
                        "  field String city;\n" +
                        "  field String street;\n" +
                        "}\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String name;\n" +
                        "  field Address homeAddress;\n" +
                        "  relation Address workAddress;\n" +
                        "}\n"));

        assertDeepEquivalence("entityWithRelations", jslModel);
    }

    @Test
    void testDeepComparison_transferObjects() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "type boolean Boolean;\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String firstName;\n" +
                        "  field String lastName;\n" +
                        "}\n" +
                        "\n" +
                        "transfer PersonView maps Person as p {\n" +
                        "  field String firstName <=> p.firstName;\n" +
                        "  field String lastName <=> p.lastName;\n" +
                        "}\n" +
                        "\n" +
                        "transfer SearchParams {\n" +
                        "  field String searchTerm;\n" +
                        "}\n"));

        assertDeepEquivalence("transferObjects", jslModel);
    }

    @Test
    void testDeepComparison_actorWithAccess() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "type boolean Boolean;\n" +
                        "\n" +
                        "entity User {\n" +
                        "  identifier String email;\n" +
                        "}\n" +
                        "\n" +
                        "entity Product {\n" +
                        "  field String name;\n" +
                        "}\n" +
                        "\n" +
                        "transfer UserTransfer maps User as u {\n" +
                        "  field String email <=> u.email;\n" +
                        "}\n" +
                        "\n" +
                        "transfer ProductView maps Product as p {\n" +
                        "  field String name <=> p.name;\n" +
                        "}\n" +
                        "\n" +
                        "actor MyActor\n" +
                        "  realm: \"COMPANY\"\n" +
                        "  claim: \"email\"\n" +
                        "  identity: UserTransfer::email\n" +
                        "{\n" +
                        "  access ProductView[] products;\n" +
                        "}\n"));

        assertDeepEquivalence("actorWithAccess", jslModel);
    }

    @Test
    void testDeepComparison_actions() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Item {\n" +
                        "  field String name;\n" +
                        "}\n" +
                        "\n" +
                        "transfer ItemView maps Item as i {\n" +
                        "  field String name <=> i.name;\n" +
                        "  action void doSomething();\n" +
                        "  action ItemView createItem(ItemView input choices:Item.all()) static;\n" +
                        "}\n"));

        assertDeepEquivalence("actions", jslModel);
    }

    @Test
    void testDeepComparison_inheritance() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "type numeric Integer precision:9 scale:0;\n" +
                        "\n" +
                        "entity Animal abstract {\n" +
                        "  field String name;\n" +
                        "}\n" +
                        "\n" +
                        "entity Dog extends Animal {\n" +
                        "  field String breed;\n" +
                        "}\n" +
                        "\n" +
                        "entity Cat extends Animal {\n" +
                        "  field Integer lives;\n" +
                        "}\n"));

        assertDeepEquivalence("inheritance", jslModel);
    }

    @Test
    void testDeepComparison_derivedProperties() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "entity Address {\n" +
                        "  field String city;\n" +
                        "}\n" +
                        "\n" +
                        "entity Person {\n" +
                        "  field String firstName;\n" +
                        "  field String lastName;\n" +
                        "  field Address homeAddress;\n" +
                        "  field String fullName <= self.firstName + \" \" + self.lastName;\n" +
                        "  relation Address[] addresses <= Address.all();\n" +
                        "}\n"));

        assertDeepEquivalence("derivedProperties", jslModel);
    }

    @Test
    void testDeepComparison_enumerations() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(
                List.of("model TestModel;\n" +
                        "\n" +
                        "type string String min-size:0 max-size:255;\n" +
                        "\n" +
                        "enum Color {\n" +
                        "  RED = 0;\n" +
                        "  GREEN = 1;\n" +
                        "  BLUE = 2;\n" +
                        "}\n" +
                        "\n" +
                        "entity Painting {\n" +
                        "  field String paintingName;\n" +
                        "  field Color color;\n" +
                        "}\n"));

        assertDeepEquivalence("enumerations", jslModel);
    }

    // ========================
    // STRICT comparison tests (using test resource .jsl files)
    // These reproduce diffs found in discovery comparison tests
    // ========================

    private void assertStrictEquivalence(String testName, JslDslModel jslModel) throws Exception {
        PsmModel etlResult = executeEtl(jslModel);
        PsmModel zetaResult = executeZeta(jslModel);

        org.eclipse.emf.ecore.resource.Resource etlResource = etlResult.getResourceSet().getResources().get(0);
        org.eclipse.emf.ecore.resource.Resource zetaResource = zetaResult.getResourceSet().getResources().get(0);

        ComparisonResult result = ModelComparator.compare(etlResource, zetaResource, ComparisonMode.STRICT);

        if (!result.isEquivalent()) {
            String msg = "STRICT COMPARISON FAILED for " + testName + ": " + result.getDifferenceCount() + " differences\n"
                    + result.getDetailedReport();
            log.error(msg);
        } else {
            log.info("STRICT COMPARISON PASSED for {}: zero differences", testName);
        }

        assertTrue(result.isEquivalent(),
                "Strict comparison failed for " + testName + ":\n" + result.getDetailedReport());
    }

    @Test
    void testStrictComparison_unmappedTransfer() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/transferobject/TestCreateUnmappedTransferObjectTypeModel.jsl")));
        assertStrictEquivalence("unmappedTransfer", jslModel);
    }

    @Test
    void testStrictComparison_choices() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/transferobject/TestTransferObjectChoicesModel.jsl")));
        assertStrictEquivalence("choices", jslModel);
    }

    @Test
    void testStrictComparison_defaultTransfer() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/transferobject/TestCreateDefaultTransferObjectTypeModel.jsl")));
        assertStrictEquivalence("defaultTransfer", jslModel);
    }

    @Test
    void testStrictComparison_derivedRelation() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/derived/TestDerivedRelationModel.jsl")));
        assertStrictEquivalence("derivedRelation", jslModel);
    }

    @Test
    void testStrictComparison_actor() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/actor/ActorTestModel.jsl")));
        assertStrictEquivalence("actor", jslModel);
    }

    @Test
    void testStrictComparison_anonymousActor() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/actor/AnonymousActorTestModel.jsl")));
        assertStrictEquivalence("anonymousActor", jslModel);
    }

    @Test
    void testStrictComparison_association() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/entity/AssociationRelationTestModel.jsl")));
        assertStrictEquivalence("association", jslModel);
    }

    @Test
    void testStrictComparison_actions() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/operation/ActionsTestModel.jsl")));
        assertStrictEquivalence("actions", jslModel);
    }

    @Test
    void testStrictComparison_crudBehaviour() throws Exception {
        // This model requires generateBehaviours=true
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/operation/CrudBehaviourTestModel.jsl")));

        // ETL with behaviours
        PsmModel etlPsm = buildPsmModel().build();
        executeJsl2PsmTransformation(jsl2PsmParameter()
                .log(createLog())
                .jslModel(jslModel)
                .psmModel(etlPsm)
                .parallel(true)
                .useCache(true)
                .generateBehaviours(true)
                .createTrace(false));

        // Re-parse for Zeta
        JslDslModel jslModelZeta = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/operation/CrudBehaviourTestModel.jsl")));

        // Zeta with behaviours
        PsmModel zetaPsm = buildPsmModel().build();
        Jsl2PsmZetaTransformation.builder()
                .jslModel(jslModelZeta)
                .psmModel(zetaPsm)
                .defaultModelName(jslModelZeta.getName())
                .generateBehaviours(true)
                .build()
                .execute();

        org.eclipse.emf.ecore.resource.Resource etlResource = etlPsm.getResourceSet().getResources().get(0);
        org.eclipse.emf.ecore.resource.Resource zetaResource = zetaPsm.getResourceSet().getResources().get(0);

        ComparisonResult result = ModelComparator.compare(etlResource, zetaResource, ComparisonMode.STRICT);

        if (!result.isEquivalent()) {
            String msg = "STRICT COMPARISON FAILED for crudBehaviour: " + result.getDifferenceCount() + " differences\n"
                    + result.getDetailedReport();
            log.error(msg);
        }

        assertTrue(result.isEquivalent(),
                "Strict comparison failed for crudBehaviour:\n" + result.getDetailedReport());
    }

    @Test
    void testStrictComparison_cardinalityIds() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/dual/CardinalityIdTestModel.jsl")));
        assertStrictEquivalence("cardinalityIds", jslModel);
    }

    @Test
    void testStrictComparison_associationPartners() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromFiles(
                List.of(new java.io.File("src/test/resources/entity/AssociationRelationTestModel.jsl")));
        assertStrictEquivalence("associationPartners", jslModel);
    }
}
