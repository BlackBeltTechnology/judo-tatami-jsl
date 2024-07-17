package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.data.*;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
                field String email <= u.email bind required;
                field Binary binaryDerived <= u.binary;
                field String stringDerived <= u.string;
                field Boolean booleanDerived <= u.boolean;
                field Date dateDerived <= u.date;
                field Numeric numericDerived <= u.numeric;
                field Time timeDerived <= u.time;
                field Timestamp timestampDerived <= u.timestamp;
                field MyEnum mappedEnum <= u.`enum` bind default:MyEnum#Crazy;

                field Binary binaryTransient;
                field String stringTransient;
                field Boolean booleanTransient;
                field Date dateTransient;
                field Numeric numericTransient;
                field Time timeTransient;
                field Timestamp timestampTransient;

                field Binary binaryMapped <= u.binary bind: true;
                field String stringMapped <= u.string bind: true;
                field Boolean booleanMapped <= u.boolean bind: true;
                field Date dateMapped <= u.date bind: true;
                field Numeric numericMapped <= u.numeric bind: true;
                field Time timeMapped <= u.time bind: true;
                field Timestamp timestampMapped <= u.timestamp bind: true;
            }
        
            actor Actor human realm:"COMPANY" claim:"email" identity:UserTransfer::email;
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
        EnumerationType enumType = (EnumerationType) dataTypes.stream().filter(t -> t instanceof EnumerationType).findFirst().orElseThrow();
        DateType dateType = (DateType) dataTypes.stream().filter(t -> t instanceof DateType).findFirst().orElseThrow();
        TimeType timeType = (TimeType) dataTypes.stream().filter(t -> t instanceof TimeType).findFirst().orElseThrow();
        TimestampType timestampType = (TimestampType) dataTypes.stream().filter(t -> t instanceof TimestampType).findFirst().orElseThrow();

        assertEquals(8, dataTypes.size());

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

        AttributeAssertion asserter = new AttributeAssertion("Actor::Application::BasicDataTestModel::UserTransfer::ClassType::");

        ClassType userTransfer = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataTestModel::UserTransfer::ClassType")).findFirst().orElseThrow();
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

            row Transfer1Row(Entity1 e1) {
                field String string <= e1.string;
                field Boolean boolean <= e1.boolean;
            }

            row Transfer2Row(Entity2 e2) {
                field Integer integer <= e2.integer;
            }

            view Transfer1ListView {
                table Transfer1Row[] tr1s <= Entity1.all();
            }

            view Transfer2ListView {
                table Transfer2Row[] tr2s <= Entity2.all();
            }

            actor Actor1 human {
                link Transfer1ListView tr1s label:"TR1S";
                link Transfer2ListView tr2s label:"TR2S";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app = apps.get(0);

        ClassType transfer1Row = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataCrossTransfersTestModel::Transfer1Row::ClassType")).findFirst().orElseThrow();
        assertNotNull(transfer1Row);
        assertEquals(2, transfer1Row.getAttributes().size());

        AttributeAssertion tr1Asserter = new AttributeAssertion("Actor1::Application::BasicDataCrossTransfersTestModel::Transfer1Row::ClassType::");

        AttributeType string = transfer1Row.getAttributes().stream().filter(a -> a.getName().equals("string")).findFirst().orElseThrow();
        tr1Asserter.assertAttributeType(string, "string", "String", MemberType.DERIVED, false, true, true);

        AttributeType booleanAttr = transfer1Row.getAttributes().stream().filter(a -> a.getName().equals("boolean")).findFirst().orElseThrow();
        tr1Asserter.assertAttributeType(booleanAttr, "boolean", "Boolean", MemberType.DERIVED, false, true, true);

        ClassType transfer2Row = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataCrossTransfersTestModel::Transfer2Row::ClassType")).findFirst().orElseThrow();
        assertNotNull(transfer2Row);
        assertEquals(1, transfer2Row.getAttributes().size());

        AttributeAssertion tr2Asserter = new AttributeAssertion("Actor1::Application::BasicDataCrossTransfersTestModel::Transfer2Row::ClassType::");

        AttributeType integer = transfer2Row.getAttributes().stream().filter(a -> a.getName().equals("integer")).findFirst().orElseThrow();
        tr2Asserter.assertAttributeType(integer, "integer", "Integer", MemberType.DERIVED, false, true, true);
    }

    @Test
    void testNestedFieldsAndRelations() throws Exception {
        jslModel = JslParser.getModelFromStrings("NestedFieldsAndRelationsTestModel", List.of("""
            model NestedFieldsAndRelationsTestModel;

            import judo::types;

            entity Entity1 {
                field String string;
                field Boolean boolean;
            }

            row Transfer1Row(Entity1 e1) {
                field String stringOnRow <= e1.string;
                field Boolean booleanOnRow <= e1.boolean;
            }

            view Transfer1View(Entity1 e1) {
                field String stringOnView <= e1.string;
                field Boolean booleanOnView <= e1.boolean;
            }

            view TransferRootView {
                group level1 frame {
                    group level2 {
                        table Transfer1Row[] tr1s <= Entity1.all();
                        link Transfer1View tr1 <= Entity1.any();
                        field String transientNested;
                    }
                }
            }

            actor ActorForNestedMembers human {
                link TransferRootView root label:"Root";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app = apps.get(0);

        List<ClassType> classTypes = app.getClassTypes();

        assertEquals(4, classTypes.size());

        ClassType actorClass = classTypes.stream().filter(c -> c.isIsActor()).findFirst().orElseThrow();
        ClassType transferRootViewClass = classTypes.stream().filter(c -> c.getName().equals("NestedFieldsAndRelationsTestModel::TransferRootView::ClassType")).findFirst().orElseThrow();
        ClassType transfer1ViewClass = classTypes.stream().filter(c -> c.getName().equals("NestedFieldsAndRelationsTestModel::Transfer1View::ClassType")).findFirst().orElseThrow();
        ClassType transfer1RowClass = classTypes.stream().filter(c -> c.getName().equals("NestedFieldsAndRelationsTestModel::Transfer1Row::ClassType")).findFirst().orElseThrow();

        assertEquals("NestedFieldsAndRelationsTestModel::ActorForNestedMembers::ClassType", actorClass.getName());
        assertEquals(1, actorClass.getRelations().size());

        RelationType relation = actorClass.getRelations().get(0);
        assertEquals("root", relation.getName());
        assertEquals(transferRootViewClass, relation.getTarget());

        assertEquals(1, transferRootViewClass.getAttributes().size());
        assertEquals("transientNested", transferRootViewClass.getAttributes().get(0).getName());

        assertEquals(2, transferRootViewClass.getRelations().size());

        RelationType tr1 = transferRootViewClass.getRelations().stream().filter(r -> r.getTarget().equals(transfer1ViewClass)).findFirst().orElseThrow();
        RelationType tr1s = transferRootViewClass.getRelations().stream().filter(r -> r.getTarget().equals(transfer1RowClass)).findFirst().orElseThrow();

        assertEquals("tr1", tr1.getName());
        assertFalse(tr1.isIsCollection());

        assertEquals("tr1s", tr1s.getName());
        assertTrue(tr1s.isIsCollection());

        assertEquals(2, transfer1RowClass.getAttributes().size());

        AttributeType stringOnRow = transfer1RowClass.getAttributes().stream().filter(a -> a.getName().equals("stringOnRow")).findFirst().orElseThrow();
        AttributeType booleanOnRow = transfer1RowClass.getAttributes().stream().filter(a -> a.getName().equals("booleanOnRow")).findFirst().orElseThrow();

        assertEquals("String", stringOnRow.getDataType().getName());
        assertTrue(stringOnRow.getIsMemberTypeDerived());

        assertEquals("Boolean", booleanOnRow.getDataType().getName());
        assertTrue(booleanOnRow.getIsMemberTypeDerived());

        assertEquals(2, transfer1ViewClass.getAttributes().size());

        AttributeType stringOnView = transfer1ViewClass.getAttributes().stream().filter(a -> a.getName().equals("stringOnView")).findFirst().orElseThrow();
        AttributeType booleanOnView = transfer1ViewClass.getAttributes().stream().filter(a -> a.getName().equals("booleanOnView")).findFirst().orElseThrow();

        assertEquals("String", stringOnView.getDataType().getName());
        assertTrue(stringOnView.getIsMemberTypeDerived());

        assertEquals("Boolean", booleanOnView.getDataType().getName());
        assertTrue(booleanOnView.getIsMemberTypeDerived());
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
                relation EntityRelated derivedContainment <= self.containment eager:true;
                relation EntityRelated[] derivedContainmentCollection <= self.containmentCollection eager:true;
            }

            entity EntityRelated {
                field String hello;

                relation User user opposite-add:userRelatedOpposite;
                relation User userCollection opposite-add:userRelatedOppositeCollection[];
            }

            transfer UserTransfer maps User as u {
                field String email <= u.email bind;
            }

            view UserView(User u) {
                link UnmappedRelated unmappedLazy;
                link UnmappedRelated unmappedLazyRequired required;
                table UnmappedRelatedRow[] unmappedLazyCollection;

                link MappedRelated lazyAssociation <= u.association create:true;
                link MappedRelated lazyAssociationOpposite <= u.userRelatedOpposite create:true;

                link MappedRelated derivedLazyContainment <= u.containment;
                table MappedRelatedRow[] derivedLazyContainmentCollection <= u.containmentCollection;

                link MappedRelated derivedEagerContainment <= u.containment eager:true;
                table MappedRelatedRow[] derivedEagerContainmentCollection <= u.containmentCollection eager:true;

                link MappedRelated derivedLazyAssociation <= u.association;
                link MappedRelated derivedLazyAssociationOpposite <= u.userRelatedOpposite create:true;

                link MappedRelated derivedEagerAssociation <= u.association eager:true;
                link MappedRelated derivedEagerAssociationOpposite <= u.userRelatedOpposite create:true eager:true;

                link MappedRelated derivedLazyStatic <= EntityRelated.any();
                table MappedRelatedRow[] derivedLazyCollectionStatic <= EntityRelated.all();

                link MappedRelated mappedLazyAssociationDerived <= u.derivedContainment;
                table MappedRelatedRow[] mappedLazyAssociationCollectionDerived <= u.derivedContainmentCollection;

                link MappedRelated lazyAssociationWithChoicesAndDefault <= u.association choices:EntityRelated.all() default:EntityRelated.any();
                table MappedRelatedRow[] lazyAssociationCollectionWithChoicesAndDefault <= u.associationCollection choices:EntityRelated.all() default:EntityRelated.all();
                link MappedRelated lazyAssociationOppositeWithChoicesAndDefault <= u.userRelatedOpposite choices:EntityRelated.all() default:EntityRelated.any();
                table MappedRelatedRow[] lazyAssociationOppositeCollectionWithChoicesAndDefault <= u.userRelatedOppositeCollection choices:EntityRelated.all() default:EntityRelated.all();

                link MappedRelated lazyTransientWithDefault default:EntityRelated.any();
                table MappedRelatedRow[] lazyTransientCollectionWithDefault default:EntityRelated.all();
            }

            view UnmappedRelated {
                field String transient;
            }

            row UnmappedRelatedRow {
                field String transient;
            }

            view MappedRelated(EntityRelated e) {
                field String mappedAttribute <= e.hello;
                event create onCreate;
            }

            row MappedRelatedRow(EntityRelated e) {
                field String mappedAttribute <= e.hello;
                event create onCreate;
            }
        
            actor Actor human realm:"COMPANY" claim:"email" identity:UserTransfer::email {
                link UserView users label:"Users";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);
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
}
