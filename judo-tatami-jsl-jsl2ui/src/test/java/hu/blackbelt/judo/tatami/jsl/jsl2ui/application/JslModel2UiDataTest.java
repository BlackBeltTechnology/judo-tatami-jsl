package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.NamedElement;
import hu.blackbelt.judo.meta.ui.data.*;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslModel2UiDataTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/data";

    @Override
    protected String getTargetTestClasses() {
        return TARGET_TEST_CLASSES;
    }

    @Override
    protected String getTest() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected Logger createLog() {
        return log;
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testBasicData() throws Exception {
        jslModel = JslParser.getModelFromStrings("BasicDataTestModel", List.of("""
            model BasicDataTestModel;

            type binary Binary max-file-size: 1MB  mime-type: ["image/*"];
            type boolean Boolean;
            type date Date;
            type numeric Numeric scale: 0 precision: 9;
            type string String min-size: 0 max-size: 255;
            type time Time;
            type timestamp Timestamp;

            enum MyEnum {
                Atomic = 0;
                Bombastic = 1;
                Crazy = 2;
            }

            entity User {
                identifier String email required;
                field Binary binary;
                field String string;
                field Boolean boolean;
                field Date date;
                field Numeric numeric;
                field Time time;
                field Timestamp timestamp;
                field MyEnum `enum` default:MyEnum#Bombastic;
            }

            transfer UserTransfer maps User as u {
                field String email <=> u.email required;
                field Binary binaryDerived <= u.binary;
                field String stringDerived <= u.string;
                field Boolean booleanDerived <= u.boolean;
                field Date dateDerived <= u.date;
                field Numeric numericDerived <= u.numeric;
                field Time timeDerived <= u.time;
                field Timestamp timestampDerived <= u.timestamp;
                field MyEnum mappedEnum <=> u.`enum` default:MyEnum#Crazy;

                field Binary binaryTransient;
                field String stringTransient;
                field Boolean booleanTransient;
                field Date dateTransient;
                field Numeric numericTransient;
                field Time timeTransient;
                field Timestamp timestampTransient;

                field Binary binaryMapped <=> u.binary;
                field String stringMapped <=> u.string;
                field Boolean booleanMapped <=> u.boolean;
                field Date dateMapped <=> u.date;
                field Numeric numericMapped <=> u.numeric;
                field Time timeMapped <=> u.time;
                field Timestamp timestampMapped <=> u.timestamp;
            }

            actor Actor realm:"COMPANY" claim:"email" identity:UserTransfer::email;

            menu ActorApp(Actor a) {}
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app = apps.get(0);

        // DataTypes

        var dataTypes = app.getDataTypes();
        StringType stringType = (StringType) dataTypes.stream().filter(t -> t instanceof StringType).findFirst().orElseThrow();
        BooleanType booleanType = (BooleanType) dataTypes.stream().filter(t -> t instanceof BooleanType).findFirst().orElseThrow();
        BinaryType binaryType = (BinaryType) dataTypes.stream().filter(t -> t instanceof BinaryType).findFirst().orElseThrow();
        NumericType numericType = (NumericType) dataTypes.stream().filter(t -> t instanceof NumericType).findFirst().orElseThrow();
        EnumerationType enumType = (EnumerationType) dataTypes.stream().filter(t -> t instanceof EnumerationType && t.getName().equals("MyEnum")).findFirst().orElseThrow();
        DateType dateType = (DateType) dataTypes.stream().filter(t -> t instanceof DateType).findFirst().orElseThrow();
        TimeType timeType = (TimeType) dataTypes.stream().filter(t -> t instanceof TimeType).findFirst().orElseThrow();
        TimestampType timestampType = (TimestampType) dataTypes.stream().filter(t -> t instanceof TimestampType).findFirst().orElseThrow();

        assertEquals(Set.of(
                "Actor::Boolean",
                "Actor::Numeric",
                "Actor::Binary",
                "Actor::MyEnum",
                "Actor::String",
                "Actor::Time",
                "Actor::Timestamp",
                "Actor::Date",
                "Actor::BooleanOperation",
                "Actor::NumericOperation",
                "Actor::StringOperation",
                "Actor::EnumerationOperation"
        ), dataTypes.stream().map(d -> d.getFQName()).collect(Collectors.toSet()));

        assertNotNull(stringType);
        assertEquals("String", stringType.getName());
        assertEquals(255, stringType.getMaxLength());

        assertNotNull(booleanType);
        assertEquals("Boolean", booleanType.getName());

        assertNotNull(stringType);
        assertEquals("Numeric", numericType.getName());
        assertEquals(9, numericType.getPrecision());
        assertEquals(0, numericType.getScale());

        assertNotNull(dateType);
        assertEquals("Date", dateType.getName());

        assertNotNull(timeType);
        assertEquals("Time", timeType.getName());

        assertNotNull(timestampType);
        assertEquals("Timestamp", timestampType.getName());

        assertNotNull(binaryType);
        assertEquals("Binary", binaryType.getName());
        assertEquals(1000000, binaryType.getMaxFileSize());
        assertEquals(1, binaryType.getMimeTypes().size());
        assertEquals("image", binaryType.getMimeTypes().get(0).getType());
        assertEquals("*", binaryType.getMimeTypes().get(0).getSubType());

        assertNotNull(enumType);
        assertEquals("MyEnum", enumType.getName());
        assertEquals(3, enumType.getMembers().size());
        assertEquals(0, enumType.getMembers().get(0).getOrdinal());
        assertEquals("Atomic", enumType.getMembers().get(0).getName());
        assertEquals(1, enumType.getMembers().get(1).getOrdinal());
        assertEquals("Bombastic", enumType.getMembers().get(1).getName());
        assertEquals(2, enumType.getMembers().get(2).getOrdinal());
        assertEquals("Crazy", enumType.getMembers().get(2).getName());

        // Attributes

        AttributeAssertion asserter = new AttributeAssertion("Actor::BasicDataTestModel::UserTransfer::");

        ClassType userTransfer = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataTestModel::UserTransfer")).findFirst().orElseThrow();
        assertNotNull(userTransfer);
        assertEquals(23, userTransfer.getAttributes().size());

        AttributeType email = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("email")).findFirst().orElseThrow();
        asserter.assertAttributeType(email, "email", "String", MemberType.MAPPED, true, true, false);

        AttributeType binaryDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("binaryDerived")).findFirst().orElseThrow();
        asserter.assertAttributeType(binaryDerived, "binaryDerived", "Binary", MemberType.DERIVED, false, false, true);

        AttributeType stringDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("stringDerived")).findFirst().orElseThrow();
        asserter.assertAttributeType(stringDerived, "stringDerived", "String", MemberType.DERIVED, false, true, true);

        AttributeType booleanDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("booleanDerived")).findFirst().orElseThrow();
        asserter.assertAttributeType(booleanDerived, "booleanDerived", "Boolean", MemberType.DERIVED, false, true, true);

        AttributeType dateDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("dateDerived")).findFirst().orElseThrow();
        asserter.assertAttributeType(dateDerived, "dateDerived", "Date", MemberType.DERIVED, false, true, true);

        AttributeType numericDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("numericDerived")).findFirst().orElseThrow();
        asserter.assertAttributeType(numericDerived, "numericDerived", "Numeric", MemberType.DERIVED, false, true, true);

        AttributeType timeDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timeDerived")).findFirst().orElseThrow();
        asserter.assertAttributeType(timeDerived, "timeDerived", "Time", MemberType.DERIVED, false, true, true);

        AttributeType timestampDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timestampDerived")).findFirst().orElseThrow();
        asserter.assertAttributeType(timestampDerived, "timestampDerived", "Timestamp", MemberType.DERIVED, false, true, true);

        AttributeType mappedEnum = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("mappedEnum")).findFirst().orElseThrow();
        asserter.assertAttributeType(mappedEnum, "mappedEnum", "MyEnum", MemberType.MAPPED, false, false, false);

        AttributeType binaryTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("binaryTransient")).findFirst().orElseThrow();
        asserter.assertAttributeType(binaryTransient, "binaryTransient", "Binary", MemberType.TRANSIENT, false, false, false);

        AttributeType stringTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("stringTransient")).findFirst().orElseThrow();
        asserter.assertAttributeType(stringTransient, "stringTransient", "String", MemberType.TRANSIENT, false, false, false);

        AttributeType booleanTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("booleanTransient")).findFirst().orElseThrow();
        asserter.assertAttributeType(booleanTransient, "booleanTransient", "Boolean", MemberType.TRANSIENT, false, false, false);

        AttributeType dateTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("dateTransient")).findFirst().orElseThrow();
        asserter.assertAttributeType(dateTransient, "dateTransient", "Date", MemberType.TRANSIENT, false, false, false);

        AttributeType numericTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("numericTransient")).findFirst().orElseThrow();
        asserter.assertAttributeType(numericTransient, "numericTransient", "Numeric", MemberType.TRANSIENT, false, false, false);

        AttributeType timeTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timeTransient")).findFirst().orElseThrow();
        asserter.assertAttributeType(timeTransient, "timeTransient", "Time", MemberType.TRANSIENT, false, false, false);

        AttributeType timestampTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timestampTransient")).findFirst().orElseThrow();
        asserter.assertAttributeType(timestampTransient, "timestampTransient", "Timestamp", MemberType.TRANSIENT, false, false, false);

        AttributeType binaryMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("binaryMapped")).findFirst().orElseThrow();
        asserter.assertAttributeType(binaryMapped, "binaryMapped", "Binary", MemberType.MAPPED, false, false, false);

        AttributeType stringMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("stringMapped")).findFirst().orElseThrow();
        asserter.assertAttributeType(stringMapped, "stringMapped", "String", MemberType.MAPPED, false, true, false);

        AttributeType booleanMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("booleanMapped")).findFirst().orElseThrow();
        asserter.assertAttributeType(booleanMapped, "booleanMapped", "Boolean", MemberType.MAPPED, false, true, false);

        AttributeType dateMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("dateMapped")).findFirst().orElseThrow();
        asserter.assertAttributeType(dateMapped, "dateMapped", "Date", MemberType.MAPPED, false, true, false);

        AttributeType numericMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("numericMapped")).findFirst().orElseThrow();
        asserter.assertAttributeType(numericMapped, "numericMapped", "Numeric", MemberType.MAPPED, false, true, false);

        AttributeType timeMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timeMapped")).findFirst().orElseThrow();
        asserter.assertAttributeType(timeMapped, "timeMapped", "Time", MemberType.MAPPED, false, true, false);

        AttributeType timestampMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timestampMapped")).findFirst().orElseThrow();
        asserter.assertAttributeType(timestampMapped, "timestampMapped", "Timestamp", MemberType.MAPPED, false, true, false);
    }

    @Test
    void testBasicDataCrossTransfers() throws Exception {
        jslModel = JslParser.getModelFromStrings("BasicDataCrossTransfersTestModel", List.of("""
            model BasicDataCrossTransfersTestModel;

            import judo::types;

            entity Entity1 {
                field String string;
                field Boolean boolean;
            }

            entity Entity2 {
                field Integer integer;
            }

            transfer Transfer1(Entity1 e1) {
                field String string <= e1.string;
                field Boolean boolean <= e1.boolean;
            }

            transfer Transfer2(Entity2 e2) {
                field Integer integer <= e2.integer;
            }

            table Transfer1Table(Transfer1 t1) {
                column String string <= t1.string label:"String";
                column Boolean boolean <= t1.boolean label:"Boolean";
            }

            table Transfer2Table(Transfer2 t2) {
                column Integer integer <= t2.integer label:"Integer";
            }

            actor Actor1 {
                access Transfer1[] tr1s <= Entity1.all();
                access Transfer2[] tr2s <= Entity2.all();
            }

            menu TestApp(Actor1 a) {
                table Transfer1Table tr1s <= a.tr1s label:"TR1S";
                table Transfer2Table tr2s <= a.tr2s label:"TR2S";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app = apps.get(0);

        assertEquals(Set.of(
            "Actor1::BasicDataCrossTransfersTestModel::Actor1",
            "Actor1::BasicDataCrossTransfersTestModel::Transfer1",
            "Actor1::BasicDataCrossTransfersTestModel::Transfer2"
        ), app.getClassTypes().stream().map(c -> ((ClassType) c).getFQName()).collect(Collectors.toSet()));

        ClassType transfer1Row = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataCrossTransfersTestModel::Transfer1")).findFirst().orElseThrow();
        assertNotNull(transfer1Row);
        assertEquals(2, transfer1Row.getAttributes().size());

        AttributeAssertion tr1Asserter = new AttributeAssertion("Actor1::BasicDataCrossTransfersTestModel::Transfer1::");

        AttributeType string = transfer1Row.getAttributes().stream().filter(a -> a.getName().equals("string")).findFirst().orElseThrow();
        tr1Asserter.assertAttributeType(string, "string", "String", MemberType.DERIVED, false, true, true);

        AttributeType booleanAttr = transfer1Row.getAttributes().stream().filter(a -> a.getName().equals("boolean")).findFirst().orElseThrow();
        tr1Asserter.assertAttributeType(booleanAttr, "boolean", "Boolean", MemberType.DERIVED, false, true, true);

        ClassType transfer2Row = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataCrossTransfersTestModel::Transfer2")).findFirst().orElseThrow();
        assertNotNull(transfer2Row);
        assertEquals(1, transfer2Row.getAttributes().size());

        AttributeAssertion tr2Asserter = new AttributeAssertion("Actor1::BasicDataCrossTransfersTestModel::Transfer2::");

        AttributeType integer = transfer2Row.getAttributes().stream().filter(a -> a.getName().equals("integer")).findFirst().orElseThrow();
        tr2Asserter.assertAttributeType(integer, "integer", "Integer", MemberType.DERIVED, false, true, true);
    }

    @Test
    void testRelations() throws Exception {
        jslModel = JslParser.getModelFromStrings("RelationsTestModel", List.of("""
            model RelationsTestModel;

            import judo::types;

            entity User {
                identifier String email required;

                field EntityRelated containment;
                field EntityRelated[] containmentCollection;

                relation EntityRelated association;
                relation EntityRelated[] associationCollection;
                relation EntityRelated derivedContainment <= self.containment;
                relation EntityRelated[] derivedContainmentCollection <= self.containmentCollection;
            }

            entity EntityRelated {
                field String hello;

                relation User user opposite-add:userRelatedOpposite;
                relation User userCollection opposite-add:userRelatedOppositeCollection[];
            }

            transfer UserTransfer(User u) {
                field String email <=> u.email;

                relation EntityRelatedTransfer association <= u.association create:true update:true delete:true;
                relation EntityRelatedTransfer[] associationCollection <= u.associationCollection create:true update:true delete:true;
                relation EntityRelatedTransfer eagerAssociation <= u.association eager:true;
                relation EntityRelatedTransfer[] eagerAssociationCollection <= u.associationCollection eager:true;
                relation EntityRelatedTransfer userRelatedOpposite <= u.userRelatedOpposite create:true update:true delete:true;

                relation EntityRelatedTransfer containment <= u.containment;
                relation EntityRelatedTransfer[] containmentCollection <= u.containmentCollection;

                relation EntityRelatedTransfer derivedLazyStatic <= EntityRelated.any();
                relation EntityRelatedTransfer derivedLazyStaticEager <= EntityRelated.any() eager:true;
                relation EntityRelatedTransfer[] derivedLazyCollectionStatic <= EntityRelated.all();
                relation EntityRelatedTransfer[] derivedLazyCollectionStaticEager <= EntityRelated.all() eager:true;

                relation EntityRelatedTransfer derivedContainment <= u.containment;
                relation EntityRelatedTransfer derivedEagerContainment <= u.containment eager:true;
                relation EntityRelatedTransfer[] derivedContainmentCollection <= u.containmentCollection;
                relation EntityRelatedTransfer[] derivedEagerContainmentCollection <= u.containmentCollection eager:true;

                relation EntityRelatedTransfer lazyTransientWithDefault default:EntityRelated.any();
                relation EntityRelatedTransfer[] lazyTransientCollectionWithDefault default:EntityRelated.all();

                relation EntityRelatedTransfer unmapped;
                relation EntityRelatedTransfer unmappedRequired required:true;
                relation EntityRelatedTransfer[] unmappedCollection;

                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }

            transfer EntityRelatedTransfer(EntityRelated e) {
                field String hello <= e.hello;
                field String transient;

                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }

            view UserView(UserTransfer u) {
                link UnmappedRelated unmappedLazy <= u.unmapped;
                link UnmappedRelated unmappedLazyRequired <= u.unmappedRequired;
                table UnmappedRelatedTable unmappedLazyCollection <= u.unmappedCollection;

                link MappedRelated lazyAssociation <= u.association;
                table MappedRelatedTable lazyAssociationCollection <= u.associationCollection;
                link MappedRelated lazyAssociationOpposite <= u.userRelatedOpposite;

                link MappedRelated derivedLazyContainment <= u.containment;
                table MappedRelatedTable derivedLazyContainmentCollection <= u.containmentCollection;

                link MappedRelated derivedEagerContainment <= u.derivedEagerContainment;
                table MappedRelatedTable derivedEagerContainmentCollection <= u.derivedEagerContainmentCollection;

                link MappedRelated derivedEagerAssociation <= u.eagerAssociation;
                table MappedRelatedTable derivedEagerAssociationCollection <= u.eagerAssociationCollection;

                link MappedRelated derivedLazyStatic <= u.derivedLazyStatic;
                table MappedRelatedTable derivedLazyCollectionStatic <= u.derivedLazyCollectionStatic;

                link MappedRelated derivedEagerStatic <= u.derivedLazyStaticEager;
                table MappedRelatedTable derivedEagerCollectionStatic <= u.derivedLazyCollectionStaticEager;

                link MappedRelated lazyTransientWithDefault <= u.lazyTransientWithDefault;
                table MappedRelatedTable lazyTransientCollectionWithDefault <= u.lazyTransientCollectionWithDefault;
            }

            view UnmappedRelated(EntityRelatedTransfer t) {
                widget String transient <= t.transient label:"Transient Field";
            }

            table UnmappedRelatedTable(EntityRelatedTransfer t) {
                column String transient <= t.transient label:"Transient";
            }

            view MappedRelated(EntityRelatedTransfer e) {
                widget String mappedAttribute <= e.hello;
            }

            table MappedRelatedTable(EntityRelatedTransfer e) {
                column String mappedAttribute <= e.hello label:"Mapped Attribute";
            }

            actor Actor realm:"COMPANY" claim:"email" identity:UserTransfer::email {
                access UserTransfer user <= User.any() create delete update;
            }

            menu TestApp(Actor a) {
                link UserView user <= a.user label:"User";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);

        assertEquals(Set.of(
                "Actor::RelationsTestModel::UserTransfer",
                "Actor::RelationsTestModel::EntityRelatedTransfer",
                "Actor::RelationsTestModel::Actor"
        ), app1.getClassTypes().stream().map(c -> ((ClassType) c).getFQName()).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "Actor::RelationsTestModel::Actor::user",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyCollectionStaticEager",
                "Actor::RelationsTestModel::UserTransfer::lazyTransientWithDefault",
                "Actor::RelationsTestModel::UserTransfer::derivedEagerContainment",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyCollectionStatic",
                "Actor::RelationsTestModel::UserTransfer::associationCollection",
                "Actor::RelationsTestModel::UserTransfer::containmentCollection",
                "Actor::RelationsTestModel::UserTransfer::lazyTransientCollectionWithDefault",
                "Actor::RelationsTestModel::UserTransfer::userRelatedOpposite",
                "Actor::RelationsTestModel::UserTransfer::containment",
                "Actor::RelationsTestModel::UserTransfer::eagerAssociation",
                "Actor::RelationsTestModel::UserTransfer::association",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyStaticEager",
                "Actor::RelationsTestModel::UserTransfer::unmappedRequired",
                "Actor::RelationsTestModel::UserTransfer::derivedContainment",
                "Actor::RelationsTestModel::UserTransfer::derivedContainmentCollection",
                "Actor::RelationsTestModel::UserTransfer::eagerAssociationCollection",
                "Actor::RelationsTestModel::UserTransfer::unmappedCollection",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyStatic",
                "Actor::RelationsTestModel::UserTransfer::unmapped",
                "Actor::RelationsTestModel::UserTransfer::derivedEagerContainmentCollection"
        ), app1.getRelationTypes().stream().map(c -> ((RelationType) c).getFQName()).collect(Collectors.toSet()));

        RelationType user = (RelationType) app1.getRelationTypes().stream().filter(r -> ((RelationType) r).getName().equals("user")).findFirst().orElseThrow();

        ClassType actorTransfer = (ClassType) app1.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("RelationsTestModel::Actor")).findFirst().orElseThrow();
        ClassType userTransfer = (ClassType) app1.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("RelationsTestModel::UserTransfer")).findFirst().orElseThrow();
        ClassType entityRelatedTransfer = (ClassType) app1.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("RelationsTestModel::EntityRelatedTransfer")).findFirst().orElseThrow();

        assertEquals(userTransfer, user.getTarget());

        List<RelationType> userViewRelations = userTransfer.getRelations();

        assertEquals(Set.of(
                "Actor::RelationsTestModel::UserTransfer::derivedContainmentCollection",
                "Actor::RelationsTestModel::UserTransfer::containment",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyStatic",
                "Actor::RelationsTestModel::UserTransfer::derivedEagerContainment",
                "Actor::RelationsTestModel::UserTransfer::association",
                "Actor::RelationsTestModel::UserTransfer::eagerAssociation",
                "Actor::RelationsTestModel::UserTransfer::unmappedRequired",
                "Actor::RelationsTestModel::UserTransfer::unmapped",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyCollectionStaticEager",
                "Actor::RelationsTestModel::UserTransfer::derivedEagerContainmentCollection",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyStaticEager",
                "Actor::RelationsTestModel::UserTransfer::lazyTransientWithDefault",
                "Actor::RelationsTestModel::UserTransfer::unmappedCollection",
                "Actor::RelationsTestModel::UserTransfer::userRelatedOpposite",
                "Actor::RelationsTestModel::UserTransfer::associationCollection",
                "Actor::RelationsTestModel::UserTransfer::containmentCollection",
                "Actor::RelationsTestModel::UserTransfer::derivedLazyCollectionStatic",
                "Actor::RelationsTestModel::UserTransfer::lazyTransientCollectionWithDefault",
                "Actor::RelationsTestModel::UserTransfer::eagerAssociationCollection",
                "Actor::RelationsTestModel::UserTransfer::derivedContainment"
        ), userTransfer.getRelations().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        // According to JSLUtils, transients are always aggregation regardless of what we model.
        RelationType unmapped = userViewRelations.stream().filter(r -> r.getName().equals("unmapped")).findFirst().orElseThrow();
        assertRelationType(unmapped, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.TRANSIENT, false, true, false, false, Set.of(RelationBehaviourType.TEMPLATE));

        RelationType unmappedRequired = userViewRelations.stream().filter(r -> r.getName().equals("unmappedRequired")).findFirst().orElseThrow();
        assertRelationType(unmappedRequired, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.TRANSIENT, false, false, false, false, Set.of(RelationBehaviourType.TEMPLATE));

        RelationType unmappedCollection = userViewRelations.stream().filter(r -> r.getName().equals("unmappedCollection")).findFirst().orElseThrow();
        assertRelationType(unmappedCollection, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.TRANSIENT, true, true, false, false, Set.of(RelationBehaviourType.TEMPLATE));

        RelationType association = userViewRelations.stream().filter(r -> r.getName().equals("association")).findFirst().orElseThrow();
        assertRelationType(association, entityRelatedTransfer, RelationKind.ASSOCIATION, MemberType.STORED, false, true, true, true, Set.of(
                RelationBehaviourType.TEMPLATE,
                RelationBehaviourType.VALIDATE_CREATE,
                RelationBehaviourType.CREATE,
                RelationBehaviourType.VALIDATE_UPDATE,
                RelationBehaviourType.UPDATE,
                RelationBehaviourType.DELETE,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.LIST,
                RelationBehaviourType.SET,
                RelationBehaviourType.UNSET
        ));

        RelationType userRelatedOpposite = userViewRelations.stream().filter(r -> r.getName().equals("userRelatedOpposite")).findFirst().orElseThrow();
        assertRelationType(userRelatedOpposite, entityRelatedTransfer, RelationKind.ASSOCIATION, MemberType.STORED, false, true, true, true, Set.of(
                RelationBehaviourType.TEMPLATE,
                RelationBehaviourType.VALIDATE_CREATE,
                RelationBehaviourType.CREATE,
                RelationBehaviourType.VALIDATE_UPDATE,
                RelationBehaviourType.UPDATE,
                RelationBehaviourType.DELETE,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.LIST,
                RelationBehaviourType.SET,
                RelationBehaviourType.UNSET
        ));

        RelationType containment = userViewRelations.stream().filter(r -> r.getName().equals("containment")).findFirst().orElseThrow();
        assertRelationType(containment, entityRelatedTransfer, RelationKind.ASSOCIATION, MemberType.DERIVED, false, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType containmentCollection = userViewRelations.stream().filter(r -> r.getName().equals("containmentCollection")).findFirst().orElseThrow();
        assertRelationType(containmentCollection, entityRelatedTransfer, RelationKind.ASSOCIATION, MemberType.DERIVED, true, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType derivedEagerContainment = userViewRelations.stream().filter(r -> r.getName().equals("derivedEagerContainment")).findFirst().orElseThrow();
        assertRelationType(derivedEagerContainment, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.DERIVED, false, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType derivedEagerContainmentCollection = userViewRelations.stream().filter(r -> r.getName().equals("derivedEagerContainmentCollection")).findFirst().orElseThrow();
        assertRelationType(derivedEagerContainmentCollection, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.DERIVED, true, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType eagerAssociation = userViewRelations.stream().filter(r -> r.getName().equals("eagerAssociation")).findFirst().orElseThrow();
        assertRelationType(eagerAssociation, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.DERIVED, false, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType eagerAssociationCollection = userViewRelations.stream().filter(r -> r.getName().equals("eagerAssociationCollection")).findFirst().orElseThrow();
        assertRelationType(eagerAssociationCollection, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.DERIVED, true, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType derivedLazyStatic = userViewRelations.stream().filter(r -> r.getName().equals("derivedLazyStatic")).findFirst().orElseThrow();
        assertRelationType(derivedLazyStatic, entityRelatedTransfer, RelationKind.ASSOCIATION, MemberType.DERIVED, false, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType derivedLazyCollectionStatic = userViewRelations.stream().filter(r -> r.getName().equals("derivedLazyCollectionStatic")).findFirst().orElseThrow();
        assertRelationType(derivedLazyCollectionStatic, entityRelatedTransfer, RelationKind.ASSOCIATION, MemberType.DERIVED, true, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType derivedLazyStaticEager = userViewRelations.stream().filter(r -> r.getName().equals("derivedLazyStaticEager")).findFirst().orElseThrow();
        assertRelationType(derivedLazyStaticEager, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.DERIVED, false, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        RelationType derivedLazyCollectionStaticEager = userViewRelations.stream().filter(r -> r.getName().equals("derivedLazyCollectionStaticEager")).findFirst().orElseThrow();
        assertRelationType(derivedLazyCollectionStaticEager, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.DERIVED, true, true, true, true, Set.of(
                RelationBehaviourType.LIST,
                RelationBehaviourType.REFRESH,
                RelationBehaviourType.TEMPLATE
        ));

        // According to JSLUtils, transients are always aggregation regardless of what we model.
        RelationType lazyTransientWithDefault = userViewRelations.stream().filter(r -> r.getName().equals("lazyTransientWithDefault")).findFirst().orElseThrow();
        assertRelationType(lazyTransientWithDefault, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.TRANSIENT, false, true, false, false, Set.of(
                RelationBehaviourType.TEMPLATE
        ));

        RelationType lazyTransientCollectionWithDefault = userViewRelations.stream().filter(r -> r.getName().equals("lazyTransientCollectionWithDefault")).findFirst().orElseThrow();
        assertRelationType(lazyTransientCollectionWithDefault, entityRelatedTransfer, RelationKind.AGGREGATION, MemberType.TRANSIENT, true, true, false, false, Set.of(
                RelationBehaviourType.TEMPLATE
        ));
    }

    static class AttributeAssertion {
        private final String fqPrefix;

        AttributeAssertion(String fqPrefix) {
            this.fqPrefix = fqPrefix;
        }

        public void assertAttributeType(AttributeType attributeType, String name, String typeName, MemberType memberType, boolean isRequired, boolean isFilterable, boolean isReadonly) {
            assertEquals(name, attributeType.getName());
            assertEquals(fqPrefix + name, attributeType.getFQName());
            assertEquals(typeName, attributeType.getDataType().getName());
            assertEquals(memberType, attributeType.getMemberType());
            assertEquals(isRequired, attributeType.isIsRequired());
            assertEquals(isFilterable, attributeType.isIsFilterable());
            assertEquals(isReadonly,attributeType.isIsReadOnly());
        }
    }

    static void assertRelationType(RelationType relationType, ClassType target, RelationKind relationKind, MemberType memberType, boolean isCollection, boolean isOptional, boolean isOrderable, boolean isFilterable, Set<RelationBehaviourType> behaviourTypes) {
        assertEquals(isCollection, relationType.isIsCollection());
        assertEquals(isOptional, relationType.isIsOptional());
        assertEquals(target, relationType.getTarget());
        assertEquals(memberType, relationType.getMemberType());
        assertEquals(relationKind, relationType.getRelationKind());
        assertEquals(behaviourTypes, new HashSet<>(relationType.getBehaviours()));
        assertEquals(isOrderable, relationType.isIsOrderable());
        assertEquals(isFilterable, relationType.isIsFilterable());
    }
}
