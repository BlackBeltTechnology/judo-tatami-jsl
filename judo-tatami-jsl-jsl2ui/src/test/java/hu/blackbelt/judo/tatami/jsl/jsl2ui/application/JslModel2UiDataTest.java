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
        StringType stringType = (StringType) dataTypes.stream().filter(t -> t instanceof StringType).findFirst().get();
        BooleanType booleanType = (BooleanType) dataTypes.stream().filter(t -> t instanceof BooleanType).findFirst().get();
        BinaryType binaryType = (BinaryType) dataTypes.stream().filter(t -> t instanceof BinaryType).findFirst().get();
        NumericType numericType = (NumericType) dataTypes.stream().filter(t -> t instanceof NumericType).findFirst().get();
        EnumerationType enumType = (EnumerationType) dataTypes.stream().filter(t -> t instanceof EnumerationType).findFirst().get();
        DateType dateType = (DateType) dataTypes.stream().filter(t -> t instanceof DateType).findFirst().get();
        TimeType timeType = (TimeType) dataTypes.stream().filter(t -> t instanceof TimeType).findFirst().get();
        TimestampType timestampType = (TimestampType) dataTypes.stream().filter(t -> t instanceof TimestampType).findFirst().get();

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

        ClassType userTransfer = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataTestModel::UserTransfer::ClassType")).findFirst().get();
        assertNotNull(userTransfer);
        assertEquals(23, userTransfer.getAttributes().size());

        AttributeType email = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("email")).findFirst().get();
        asserter.assertAttributeType(email, "email", "String", MemberType.MAPPED, true, true, false);

        AttributeType binaryDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("binaryDerived")).findFirst().get();
        asserter.assertAttributeType(binaryDerived, "binaryDerived", "Binary", MemberType.DERIVED, false, false, true);

        AttributeType stringDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("stringDerived")).findFirst().get();
        asserter.assertAttributeType(stringDerived, "stringDerived", "String", MemberType.DERIVED, false, true, true);

        AttributeType booleanDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("booleanDerived")).findFirst().get();
        asserter.assertAttributeType(booleanDerived, "booleanDerived", "Boolean", MemberType.DERIVED, false, true, true);

        AttributeType dateDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("dateDerived")).findFirst().get();
        asserter.assertAttributeType(dateDerived, "dateDerived", "Date", MemberType.DERIVED, false, true, true);

        AttributeType numericDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("numericDerived")).findFirst().get();
        asserter.assertAttributeType(numericDerived, "numericDerived", "Numeric", MemberType.DERIVED, false, true, true);

        AttributeType timeDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timeDerived")).findFirst().get();
        asserter.assertAttributeType(timeDerived, "timeDerived", "Time", MemberType.DERIVED, false, true, true);

        AttributeType timestampDerived = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timestampDerived")).findFirst().get();
        asserter.assertAttributeType(timestampDerived, "timestampDerived", "Timestamp", MemberType.DERIVED, false, true, true);

        AttributeType mappedEnum = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("mappedEnum")).findFirst().get();
        asserter.assertAttributeType(mappedEnum, "mappedEnum", "MyEnum", MemberType.MAPPED, false, false, false);

        AttributeType binaryTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("binaryTransient")).findFirst().get();
        asserter.assertAttributeType(binaryTransient, "binaryTransient", "Binary", MemberType.TRANSIENT, false, false, false);

        AttributeType stringTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("stringTransient")).findFirst().get();
        asserter.assertAttributeType(stringTransient, "stringTransient", "String", MemberType.TRANSIENT, false, false, false);

        AttributeType booleanTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("booleanTransient")).findFirst().get();
        asserter.assertAttributeType(booleanTransient, "booleanTransient", "Boolean", MemberType.TRANSIENT, false, false, false);

        AttributeType dateTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("dateTransient")).findFirst().get();
        asserter.assertAttributeType(dateTransient, "dateTransient", "Date", MemberType.TRANSIENT, false, false, false);

        AttributeType numericTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("numericTransient")).findFirst().get();
        asserter.assertAttributeType(numericTransient, "numericTransient", "Numeric", MemberType.TRANSIENT, false, false, false);

        AttributeType timeTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timeTransient")).findFirst().get();
        asserter.assertAttributeType(timeTransient, "timeTransient", "Time", MemberType.TRANSIENT, false, false, false);

        AttributeType timestampTransient = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timestampTransient")).findFirst().get();
        asserter.assertAttributeType(timestampTransient, "timestampTransient", "Timestamp", MemberType.TRANSIENT, false, false, false);

        AttributeType binaryMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("binaryMapped")).findFirst().get();
        asserter.assertAttributeType(binaryMapped, "binaryMapped", "Binary", MemberType.MAPPED, false, false, false);

        AttributeType stringMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("stringMapped")).findFirst().get();
        asserter.assertAttributeType(stringMapped, "stringMapped", "String", MemberType.MAPPED, false, true, false);

        AttributeType booleanMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("booleanMapped")).findFirst().get();
        asserter.assertAttributeType(booleanMapped, "booleanMapped", "Boolean", MemberType.MAPPED, false, true, false);

        AttributeType dateMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("dateMapped")).findFirst().get();
        asserter.assertAttributeType(dateMapped, "dateMapped", "Date", MemberType.MAPPED, false, true, false);

        AttributeType numericMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("numericMapped")).findFirst().get();
        asserter.assertAttributeType(numericMapped, "numericMapped", "Numeric", MemberType.MAPPED, false, true, false);

        AttributeType timeMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timeMapped")).findFirst().get();
        asserter.assertAttributeType(timeMapped, "timeMapped", "Time", MemberType.MAPPED, false, true, false);

        AttributeType timestampMapped = userTransfer.getAttributes().stream().filter(a -> a.getName().equals("timestampMapped")).findFirst().get();
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

        ClassType transfer1Row = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataCrossTransfersTestModel::Transfer1Row::ClassType")).findFirst().get();
        assertNotNull(transfer1Row);
        assertEquals(2, transfer1Row.getAttributes().size());

        AttributeAssertion tr1Asserter = new AttributeAssertion("Actor1::Application::BasicDataCrossTransfersTestModel::Transfer1Row::ClassType::");

        AttributeType string = transfer1Row.getAttributes().stream().filter(a -> a.getName().equals("string")).findFirst().get();
        tr1Asserter.assertAttributeType(string, "string", "String", MemberType.DERIVED, false, true, true);

        AttributeType booleanAttr = transfer1Row.getAttributes().stream().filter(a -> a.getName().equals("boolean")).findFirst().get();
        tr1Asserter.assertAttributeType(booleanAttr, "boolean", "Boolean", MemberType.DERIVED, false, true, true);

        ClassType transfer2Row = (ClassType) app.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("BasicDataCrossTransfersTestModel::Transfer2Row::ClassType")).findFirst().get();
        assertNotNull(transfer2Row);
        assertEquals(1, transfer2Row.getAttributes().size());

        AttributeAssertion tr2Asserter = new AttributeAssertion("Actor1::Application::BasicDataCrossTransfersTestModel::Transfer2Row::ClassType::");

        AttributeType integer = transfer2Row.getAttributes().stream().filter(a -> a.getName().equals("integer")).findFirst().get();
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
                relation EntityRelated containmentDerived <= self.containment eager:true;
                relation EntityRelated[] containmentCollectionDerived <= self.containmentCollection eager:true;
            }

            entity EntityRelated {
                relation User `user` opposite-add:userRelatedOpposite;
                field String hello;
            }

            transfer UserTransfer maps User as u {
                field String email <= u.email bind;
                relation UnmappedRelated unmappedContainment;
                relation UnmappedRelated unmappedContainmentRequired required;
                relation UnmappedRelated[] unmappedContainmentCollection;

                relation MappedRelated mappedAssociation <= u.association create:true;
                relation MappedRelated mappedAssociationOpposite <= u.userRelatedOpposite create:true;
                relation MappedRelated derivedAssociation <= u.association;
                relation MappedRelated derivedAssociationOpposite <= u.userRelatedOpposite create:true;
                relation MappedRelated derivedContainmentStatic <= EntityRelated.any();
                relation MappedRelated[] derivedContainmentCollectionStatic <= EntityRelated.all();
                relation MappedRelated mappedContainmentDerived <= u.containmentDerived;
                relation MappedRelated[] mappedContainmentCollectionDerived <= u.containmentCollectionDerived;
            }

            transfer UnmappedRelated {
                field String someField;
            }

            transfer MappedRelated maps EntityRelated as e {
                field String mappedAttribute <= e.hello bind;
                event create onCreate;
            }
        
            actor Actor human realm:"COMPANY" claim:"email" identity:UserTransfer::email;
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
