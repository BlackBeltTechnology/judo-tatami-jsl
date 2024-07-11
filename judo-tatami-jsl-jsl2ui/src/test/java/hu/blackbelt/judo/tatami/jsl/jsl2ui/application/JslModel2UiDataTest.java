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
                field String email <= u.email bind;
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
}
