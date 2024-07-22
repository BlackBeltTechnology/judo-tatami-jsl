package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
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
public class JslModel2UiWidgetsTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/widgets";

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
    void testBasicWidgets() throws Exception {
        jslModel = JslParser.getModelFromStrings("BasicWidgetsTestModel", List.of("""
            model BasicWidgetsTestModel;

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

            view UserView(User u) {
                group level1 label:"Yo" icon:"text" {
                    group level2 width:12 frame:true icon:"unicorn" label:"Level 2" stretch:true {
                        field String email <= u.email bind required;
                        field Binary binaryDerived <= u.binary;
                        field String stringDerived <= u.string;
                    }

                    group level22 width:6 frame:true icon:"dog" label:"Level 2 - 2" orientation:horizontal {
                        field Boolean booleanDerived <= u.boolean;
                        field Date dateDerived <= u.date;
                        field Numeric numericDerived <= u.numeric;
                    }

                    tabs tabs0 orientation:horizontal width:6 {
                        group tab1 label:"Tab1" icon:"numbers" h-align:left {
                            field Time timeDerived <= u.time;
                        }

                        group tab2 label:"Tab2" icon:"numbers" h-align:right {
                            field Timestamp timestampDerived <= u.timestamp;
                            field MyEnum mappedEnum <= u.`enum` bind default:MyEnum#Crazy;
                        }
                    }
                }
            }

            actor WidgetsActor human {
                link UserView user label:"User" icon:"tools";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app = apps.get(0);

        List<PageContainer> pageContainers = app.getPageContainers();

        assertEquals(2, pageContainers.size());

        PageContainer dashboard = pageContainers.stream().filter(c -> c.getName().equals("BasicWidgetsTestModel::WidgetsActor::Dashboard")).findFirst().orElseThrow();
        PageContainer user = pageContainers.stream().filter(c -> c.getName().equals("BasicWidgetsTestModel::UserView::PageContainer")).findFirst().orElseThrow();

        // Dashboard
        assertEquals(0, dashboard.getChildren().size());

        // User

        assertEquals(1, user.getChildren().size());

        // Root Flex
        Flex rootFlex = (Flex) user.getChildren().get(0);

        assertEquals("UserView", rootFlex.getName());
        assertNull(rootFlex.getLabel());
        assertEquals(12, rootFlex.getCol());
        assertEquals(Axis.VERTICAL, rootFlex.getDirection());
        assertEquals(1, rootFlex.getChildren().size());

        // level1

        Flex level1 = (Flex) rootFlex.getChildren().get(0);

        assertEquals("level1", level1.getName());
        assertEquals("Yo", level1.getLabel());
        assertEquals("text", level1.getIcon().getIconName());
        assertEquals(12, level1.getCol());
        assertEquals(Axis.VERTICAL, level1.getDirection());
        assertEquals(3, level1.getChildren().size());

        // level2

        Flex level2 = (Flex) level1.getChildren().stream().filter(c -> c.getName().equals("level2")).findFirst().orElseThrow();

        assertEquals("level2", level2.getName());
        assertEquals("Level 2", level2.getLabel());
        assertEquals("unicorn", level2.getIcon().getIconName());
        assertEquals(12, level2.getCol());
        assertEquals(Axis.VERTICAL, level2.getDirection());
        assertNotNull(level2.getFrame());
        assertNotNull(level2.getStretch());
        assertEquals(3, level2.getChildren().size());

        // level2 -> children

        TextInput email = (TextInput) level2.getChildren().stream().filter(c -> c.getName().equals("email")).findFirst().orElseThrow();

        assertEquals("email", email.getName());
        assertEquals("String", email.getAttributeType().getDataType().getName());
        assertTrue(email.getAttributeType().isIsRequired());
        assertFalse(email.getAttributeType().isIsReadOnly());

        BinaryTypeInput binaryDerived = (BinaryTypeInput) level2.getChildren().stream().filter(c -> c.getName().equals("binaryDerived")).findFirst().orElseThrow();

        assertEquals("binaryDerived", binaryDerived.getName());
        assertEquals("Binary", binaryDerived.getAttributeType().getDataType().getName());
        assertFalse(binaryDerived.getAttributeType().isIsRequired());
        assertTrue(binaryDerived.getAttributeType().isIsReadOnly());

        TextInput stringDerived = (TextInput) level2.getChildren().stream().filter(c -> c.getName().equals("stringDerived")).findFirst().orElseThrow();

        assertEquals("stringDerived", stringDerived.getName());
        assertEquals("String", stringDerived.getAttributeType().getDataType().getName());
        assertFalse(stringDerived.getAttributeType().isIsRequired());
        assertTrue(stringDerived.getAttributeType().isIsReadOnly());

        // level2 - 2

        Flex level22 = (Flex) level1.getChildren().stream().filter(c -> c.getName().equals("level22")).findFirst().orElseThrow();

        assertEquals("level22", level22.getName());
        assertEquals("Level 2 - 2", level22.getLabel());
        assertEquals("dog", level22.getIcon().getIconName());
        assertEquals(6, level22.getCol());
        assertEquals(Axis.HORIZONTAL, level22.getDirection());
        assertEquals(3, level22.getChildren().size());

        // level2 - 2 -> children

        TrinaryLogicCombo booleanDerived = (TrinaryLogicCombo) level22.getChildren().stream().filter(c -> c.getName().equals("booleanDerived")).findFirst().orElseThrow();

        assertEquals("booleanDerived", booleanDerived.getName());
        assertEquals("Boolean", booleanDerived.getAttributeType().getDataType().getName());
        assertFalse(booleanDerived.getAttributeType().isIsRequired());
        assertTrue(booleanDerived.getAttributeType().isIsReadOnly());

        DateInput dateDerived = (DateInput) level22.getChildren().stream().filter(c -> c.getName().equals("dateDerived")).findFirst().orElseThrow();

        assertEquals("dateDerived", dateDerived.getName());
        assertEquals("Date", dateDerived.getAttributeType().getDataType().getName());
        assertFalse(dateDerived.getAttributeType().isIsRequired());
        assertTrue(dateDerived.getAttributeType().isIsReadOnly());

        NumericInput numericDerived = (NumericInput) level22.getChildren().stream().filter(c -> c.getName().equals("numericDerived")).findFirst().orElseThrow();

        assertEquals("numericDerived", numericDerived.getName());
        assertEquals("Numeric", numericDerived.getAttributeType().getDataType().getName());
        assertFalse(numericDerived.getAttributeType().isIsRequired());
        assertTrue(numericDerived.getAttributeType().isIsReadOnly());

        // tabs 0

        TabController tabs0 = (TabController) level1.getChildren().stream().filter(c -> c.getName().equals("tabs0")).findFirst().orElseThrow();

        assertEquals("tabs0", tabs0.getName());
        assertEquals(6, tabs0.getCol());
        assertEquals(TabOrientation.HORIZONTAL, tabs0.getOrientation());
        assertEquals(2, tabs0.getTabs().size());

        // tab1

        Tab tab1 = tabs0.getTabs().stream().filter(t -> t.getName().equals("tab1")).findFirst().orElseThrow();
        Flex tab1Element = (Flex) tab1.getElement();

        assertEquals("tab1", tab1Element.getName());
        assertEquals(12, tab1Element.getCol());
        assertEquals("Tab1", tab1Element.getLabel());
        assertEquals("numbers", tab1Element.getIcon().getIconName());
        assertEquals(Axis.VERTICAL, tab1Element.getDirection());
        assertEquals(CrossAxisAlignment.START, tab1Element.getCrossAxisAlignment());
        assertEquals(1, tab1Element.getChildren().size());

        // tab1 -> children

        TimeInput timeDerived = (TimeInput) tab1Element.getChildren().stream().filter(c -> c.getName().equals("timeDerived")).findFirst().orElseThrow();

        assertEquals("timeDerived", timeDerived.getName());
        assertEquals("Time", timeDerived.getAttributeType().getDataType().getName());
        assertFalse(timeDerived.getAttributeType().isIsRequired());
        assertTrue(timeDerived.getAttributeType().isIsReadOnly());

        // tab2

        Tab tab2 = tabs0.getTabs().stream().filter(t -> t.getName().equals("tab2")).findFirst().orElseThrow();
        Flex tab2Element = (Flex) tab2.getElement();

        assertEquals("tab2", tab2Element.getName());
        assertEquals(12, tab2Element.getCol());
        assertEquals("Tab2", tab2Element.getLabel());
        assertEquals("numbers", tab2Element.getIcon().getIconName());
        assertEquals(Axis.VERTICAL, tab2Element.getDirection());
        assertEquals(CrossAxisAlignment.END, tab2Element.getCrossAxisAlignment());
        assertEquals(2, tab2Element.getChildren().size());

        // tab2 -> children

        DateTimeInput timestampDerived = (DateTimeInput) tab2Element.getChildren().stream().filter(c -> c.getName().equals("timestampDerived")).findFirst().orElseThrow();

        assertEquals("timestampDerived", timestampDerived.getName());
        assertEquals("Timestamp", timestampDerived.getAttributeType().getDataType().getName());
        assertFalse(timestampDerived.getAttributeType().isIsRequired());
        assertTrue(timestampDerived.getAttributeType().isIsReadOnly());

        EnumerationCombo mappedEnum = (EnumerationCombo) tab2Element.getChildren().stream().filter(c -> c.getName().equals("mappedEnum")).findFirst().orElseThrow();

        assertEquals("mappedEnum", mappedEnum.getName());
        assertEquals("MyEnum", mappedEnum.getAttributeType().getDataType().getName());
        assertFalse(mappedEnum.getAttributeType().isIsRequired());
        assertFalse(mappedEnum.getAttributeType().isIsReadOnly());
    }
}
