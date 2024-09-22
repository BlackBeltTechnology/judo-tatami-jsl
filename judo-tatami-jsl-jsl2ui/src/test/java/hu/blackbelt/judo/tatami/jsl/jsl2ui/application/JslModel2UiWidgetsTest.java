package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.AttributeType;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

            transfer UserTransfer(User u) {
                field String email <=> u.email required;
                field Binary binary <= u.binary;
                field String string <= u.string;
                field Boolean boolean <= u.boolean;
                field Date date <= u.date;
                field Numeric numeric <= u.numeric;
                field Time time <= u.time;
                field Timestamp timestamp <= u.timestamp;
                field MyEnum `enum` <=> u.`enum` default:MyEnum#Crazy;

                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }

            view UserView(UserTransfer u) {
                group level1 label:"Yo" icon:"text" {
                    group level2 width:12 frame:true icon:"unicorn" label:"Level 2" stretch:true {
                        widget String email <= u.email icon:"text" label: "My Email";
                        widget Binary binaryDerived <= u.binary icon:"binary" label:"Binary Derived";
                        widget String stringDerived <= u.string icon:"string" label:"String Derived";
                    }

                    group level22 width:6 frame:true icon:"dog" label:"Level 2 - 2" orientation:horizontal {
                        widget Boolean booleanDerived <= u.boolean icon:"boolean" label:"Boolean Derived";
                        widget Date dateDerived <= u.date icon:"date" label:"Date Derived";
                        widget Numeric numericDerived <= u.numeric icon:"numeric" label:"Numeric Derived";
                    }

                    tabs tabs0 orientation:horizontal width:6 {
                        group tab1 label:"Tab1" icon:"numbers" h-align:left {
                            widget Time timeDerived <= u.time icon:"time" label:"Time Derived";
                        }

                        group tab2 label:"Tab2" icon:"numbers" h-align:right {
                            widget Timestamp timestampDerived <= u.timestamp icon:"timestamp" label:"Timestamp Derived";
                            widget MyEnum mappedEnum <= u.`enum` icon:"enum" label:"Mapped Enum";
                        }
                    }
                }
            }

            form UserForm(UserTransfer u) {
                widget String email <= u.email icon:"text" label: "My Email";
                group level1 label:"Yo" icon:"text" {
                    widget Timestamp timestampDerived <= u.timestamp icon:"timestamp" label:"Timestamp Derived";
                }
            }

            actor WidgetsActor {
                access UserTransfer user <= User.any() create delete update;
            }

            menu WidgetsApp(WidgetsActor a) {
                link UserView user <= a.user label:"User" icon:"tools" form:UserForm;
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app = apps.get(0);

        List<PageContainer> pageContainers = app.getPageContainers();

        assertEquals(Set.of(
                "WidgetsActor::BasicWidgetsTestModel::UserView::View::PageContainer",
                "WidgetsActor::BasicWidgetsTestModel::UserForm::Create::PageContainer",
                "WidgetsActor::BasicWidgetsTestModel::WidgetsApp::Dashboard"
        ), pageContainers.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        PageContainer dashboard = pageContainers.stream().filter(c -> c.getName().equals("BasicWidgetsTestModel::WidgetsApp::Dashboard")).findFirst().orElseThrow();
        PageContainer userView = pageContainers.stream().filter(c -> c.getName().equals("BasicWidgetsTestModel::UserView::View::PageContainer")).findFirst().orElseThrow();
        PageContainer userForm = pageContainers.stream().filter(c -> c.getName().equals("BasicWidgetsTestModel::UserForm::Create::PageContainer")).findFirst().orElseThrow();

        // Dashboard
        assertEquals(0, dashboard.getChildren().size());

        // User

        assertEquals(1, userView.getChildren().size());

        // Root Flex
        Flex rootFlex = (Flex) userView.getChildren().get(0);

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
        assertEquals("My Email", email.getLabel());
        assertEquals("text", email.getIcon().getIconName());
        assertEquals("String", email.getAttributeType().getDataType().getName());
        assertTrue(email.getAttributeType().isIsRequired());
        assertFalse(email.getAttributeType().isIsReadOnly());

        BinaryTypeInput binaryDerived = (BinaryTypeInput) level2.getChildren().stream().filter(c -> c.getName().equals("binaryDerived")).findFirst().orElseThrow();

        assertEquals("binaryDerived", binaryDerived.getName());
        assertEquals("Binary Derived", binaryDerived.getLabel());
        assertEquals("binary", binaryDerived.getIcon().getIconName());
        assertEquals("Binary", binaryDerived.getAttributeType().getDataType().getName());
        assertFalse(binaryDerived.getAttributeType().isIsRequired());
        assertTrue(binaryDerived.getAttributeType().isIsReadOnly());

        TextInput stringDerived = (TextInput) level2.getChildren().stream().filter(c -> c.getName().equals("stringDerived")).findFirst().orElseThrow();

        assertEquals("stringDerived", stringDerived.getName());
        assertEquals("String Derived", stringDerived.getLabel());
        assertEquals("string", stringDerived.getIcon().getIconName());
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
        assertEquals("Boolean Derived", booleanDerived.getLabel());
        assertEquals("boolean", booleanDerived.getIcon().getIconName());
        assertEquals("Boolean", booleanDerived.getAttributeType().getDataType().getName());
        assertFalse(booleanDerived.getAttributeType().isIsRequired());
        assertTrue(booleanDerived.getAttributeType().isIsReadOnly());

        DateInput dateDerived = (DateInput) level22.getChildren().stream().filter(c -> c.getName().equals("dateDerived")).findFirst().orElseThrow();

        assertEquals("dateDerived", dateDerived.getName());
        assertEquals("Date Derived", dateDerived.getLabel());
        assertEquals("date", dateDerived.getIcon().getIconName());
        assertEquals("Date", dateDerived.getAttributeType().getDataType().getName());
        assertFalse(dateDerived.getAttributeType().isIsRequired());
        assertTrue(dateDerived.getAttributeType().isIsReadOnly());

        NumericInput numericDerived = (NumericInput) level22.getChildren().stream().filter(c -> c.getName().equals("numericDerived")).findFirst().orElseThrow();

        assertEquals("numericDerived", numericDerived.getName());
        assertEquals("Numeric Derived", numericDerived.getLabel());
        assertEquals("numeric", numericDerived.getIcon().getIconName());
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
        assertEquals("Time Derived", timeDerived.getLabel());
        assertEquals("time", timeDerived.getIcon().getIconName());
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
        assertEquals("Timestamp Derived", timestampDerived.getLabel());
        assertEquals("timestamp", timestampDerived.getIcon().getIconName());
        assertEquals("Timestamp", timestampDerived.getAttributeType().getDataType().getName());
        assertFalse(timestampDerived.getAttributeType().isIsRequired());
        assertTrue(timestampDerived.getAttributeType().isIsReadOnly());

        EnumerationCombo mappedEnum = (EnumerationCombo) tab2Element.getChildren().stream().filter(c -> c.getName().equals("mappedEnum")).findFirst().orElseThrow();

        assertEquals("mappedEnum", mappedEnum.getName());
        assertEquals("Mapped Enum", mappedEnum.getLabel());
        assertEquals("enum", mappedEnum.getIcon().getIconName());
        assertEquals("MyEnum", mappedEnum.getAttributeType().getDataType().getName());
        assertFalse(mappedEnum.getAttributeType().isIsRequired());
        assertFalse(mappedEnum.getAttributeType().isIsReadOnly());

        // User Form

        Flex formFlex = (Flex) userForm.getChildren().get(0);

        assertNotNull(formFlex);

        assertEquals(Set.of(
                "WidgetsActor::BasicWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1",
                "WidgetsActor::BasicWidgetsTestModel::UserForm::Create::PageContainer::UserForm::email"
        ), formFlex.getChildren().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        VisualElement formEmail = formFlex.getChildren().get(0);
        assertEquals("email", formEmail.getName());
        assertTrue(formEmail instanceof TextInput);

        VisualElement formLevel1Group = formFlex.getChildren().get(1);
        assertEquals("level1", formLevel1Group.getName());
        assertTrue(formLevel1Group instanceof Flex);

        assertEquals(Set.of(
                "WidgetsActor::BasicWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1::timestampDerived"
        ), ((Flex) formLevel1Group).getChildren().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        VisualElement formLevel1Timestamp = ((Flex) formLevel1Group).getChildren().get(0);
        assertEquals("timestampDerived", formLevel1Timestamp.getName());
        assertTrue(formLevel1Timestamp instanceof DateTimeInput);
    }

    @Test
    void testRelationWidgets() throws Exception {
        jslModel = JslParser.getModelFromStrings("RelationWidgetsTestModel", List.of("""
            model RelationWidgetsTestModel;

            type numeric Numeric scale: 0 precision: 9;
            type string String min-size: 0 max-size: 255;

            entity User {
                identifier String email required;
                field Numeric numeric;
                field Related related;
                relation Related relatedAssociation;
                relation Related[] relatedCollection;
            }

            entity Related {
                field String first;
                field Numeric second;
            }

            transfer UserTransfer(User u) {
                field String email <= u.email required;
                field Numeric numeric <= u.numeric;
    
                relation RelatedTransfer related <= u.related eager create update delete;
                relation RelatedTransfer relatedAssociation <= u.relatedAssociation choices:Related.all() create update delete;
                relation RelatedTransfer[] relatedCollection <= u.relatedCollection choices:Related.all() create update delete;
    
                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }

            transfer RelatedTransfer(Related r) {
                field String first <= r.first;
                field Numeric second <= r.second;
    
                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }

            table UserTable(UserTransfer u) {
                column String email <= u.email;
            }

            view UserView(UserTransfer u) {
                group level1 label:"Yo" icon:"text" {
                    group level2 width:12 frame:true icon:"unicorn" label:"Level 2" stretch:true {
                        link RelatedView related <= u.related icon:"related" label:"Related" width:6 form:RelatedForm;
                        link RelatedView relatedAssociation <= u.relatedAssociation icon:"related-association" label:"Related Association" width:6 selector:RelatedTable form:RelatedForm;
                    }
    
                    tabs tabs0 orientation:horizontal width:6 {
                        group tab1 label:"Tab1" icon:"numbers" h-align:left {
                            widget String email <= u.email icon:"text" label:"My Email";
                        }
        
                        group tab2 label:"Tab2" icon:"numbers" h-align:right {
                            table RelatedTable relatedCollection <= u.relatedCollection icon:"relatedCollection" label:"Related Collection" width:6 selector:RelatedTable view:RelatedView form:RelatedForm;
                        }
                    }
                }
            }

            form UserForm(UserTransfer u) {
                widget String emailReadOnly <= u.email icon:"text" label:"Readonly Email";
                widget String emailWritable <=> u.email icon:"text" label:"Writable Email";
                group level1 label:"Yo" icon:"text" {
                    link RelatedView related <= u.related icon:"related" label:"Related" width:6 form:RelatedForm;
                }
                table RelatedTable relatedCollection <= u.relatedCollection icon:"relatedCollection" label:"Related Collection" width:6 selector:RelatedTable view:RelatedView form:RelatedForm;
            }

            table RelatedTable(RelatedTransfer r) {
                column String first <= r.first label:"First";
                column Numeric second <= r.second label:"Second";
            }

            view RelatedView(RelatedTransfer r) {
                widget String first <= r.first label: "First";
                widget Numeric second <= r.second label: "Second";
            }

            form RelatedForm(RelatedTransfer r) {
                widget String first <= r.first label: "First";
                widget Numeric second <= r.second label: "Second";
            }

            actor RelationWidgetsActor {
                access UserTransfer user <= User.any() create;
                access UserTransfer[] users <= User.all() create;
            }

            menu RelationWidgets(RelationWidgetsActor a) {
                link UserView user <= a.user label:"User" icon:"tools";
                table UserTable users <= a.users label:"Users" icon:"tools" form:UserForm;
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        List<ClassType> classTypes = application.getClassTypes();
        List<Link> links = application.getLinks();
        List<Table> tables = application.getTables();
        List<PageDefinition> pages = application.getPages();

        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserTransfer",
                "RelationWidgetsActor::RelationWidgetsTestModel::RelatedTransfer",
                "RelationWidgetsActor::RelationWidgetsTestModel::RelationWidgetsActor"
        ), classTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "RelationWidgetsTestModel::RelationWidgets::user::AccessViewPage",
                "RelationWidgetsTestModel::UserForm::relatedCollection::ViewPage",
                "RelationWidgetsTestModel::UserForm::relatedCollection::FormPage",
                "RelationWidgetsTestModel::UserForm::level1::related::FormPage",
                "RelationWidgetsTestModel::UserForm::level1::related::ViewPage",
                "RelationWidgetsTestModel::UserView::level1::level2::related::ViewPage",
                "RelationWidgetsTestModel::UserView::level1::level2::related::FormPage",
                "RelationWidgetsTestModel::UserView::level1::level2::relatedAssociation::FormPage",
                "RelationWidgetsTestModel::UserView::level1::level2::relatedAssociation::ViewPage",
                "RelationWidgetsTestModel::UserView::level1::level2::relatedAssociation::SetSelectorPage",
                "RelationWidgetsTestModel::UserView::level1::tabs0::tab2::relatedCollection::FormPage",
                "RelationWidgetsTestModel::UserView::level1::tabs0::tab2::relatedCollection::AddSelectorPage",
                "RelationWidgetsTestModel::UserView::level1::tabs0::tab2::relatedCollection::ViewPage",
                "RelationWidgetsTestModel::RelationWidgets::users::AccessTablePage",
                "RelationWidgetsTestModel::RelationWidgets::users::AccessFormPage",
                "RelationWidgetsTestModel::RelationWidgets::DashboardPage"
                ), pages.stream().map(PageDefinition::getName).collect(Collectors.toSet()));

        // Links

        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1::related",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::View::PageContainer::UserView::level1::level2::related",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::View::PageContainer::UserView::level1::level2::relatedAssociation"
        ), links.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        ClassType relatedViewClassType = classTypes.stream().filter(c -> c.getName().equals("RelationWidgetsTestModel::RelatedTransfer")).findFirst().orElseThrow();

        Link related = links.stream().filter(l -> l.getName().equals("related")).findFirst().orElseThrow();
        RelationType relatedRelation = (RelationType) related.getDataElement();

        assertEquals("Related", related.getLabel());
        assertEquals("related", related.getIcon().getIconName());
        assertEquals("related", related.getRelationName());
        assertTrue(related.isIsEager());
        assertEquals("related", relatedRelation.getName());
        assertEquals(relatedViewClassType, relatedRelation.getTarget());

        Link relatedAssociation = links.stream().filter(l -> l.getName().equals("relatedAssociation")).findFirst().orElseThrow();
        RelationType relatedAssociationAttribute = (RelationType) relatedAssociation.getDataElement();

        assertEquals("Related Association", relatedAssociation.getLabel());
        assertEquals("related-association", relatedAssociation.getIcon().getIconName());
        assertEquals("relatedAssociation", relatedAssociation.getRelationName());
        assertFalse(relatedAssociation.isIsEager());
        assertEquals("relatedAssociation", relatedAssociationAttribute.getName());
        assertEquals(relatedViewClassType, relatedAssociationAttribute.getTarget());

        // Tables

        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserTable::Table::PageContainer::UserTable::UserTable::Table",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::level1::level2::relatedAssociation::SetSelector::PageContainer::relatedAssociation::relatedAssociation::Set::Selector",
                "RelationWidgetsActor::RelationWidgetsTestModel::RelatedTable::Table::PageContainer::RelatedTable::RelatedTable::Table",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::level1::tabs0::tab2::relatedCollection::AddSelector::PageContainer::relatedCollection::relatedCollection::Add::Selector",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::View::PageContainer::UserView::level1::tabs0::tab2::tab2::relatedCollection"
        ), tables.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        PageDefinition userView = application.getPages().stream().filter(p -> p.getName().equals("RelationWidgetsTestModel::RelationWidgets::user::AccessViewPage")).findFirst().orElseThrow();

        Table userViewTable = ((Collection<Table>) userView.getContainer().getTables()).stream().filter(t -> t.getName().equals("relatedCollection")).findFirst().orElseThrow();
        RelationType tableRelation = (RelationType) userViewTable.getDataElement();
        ClassType relatedRowClassType = classTypes.stream().filter(c -> c.getName().equals("RelationWidgetsTestModel::RelatedTransfer")).findFirst().orElseThrow();

        assertEquals("Related Collection", userViewTable.getLabel());
        assertEquals(12, userViewTable.getCol());
        assertEquals("relatedCollection", userViewTable.getRelationName());
        assertEquals("relatedCollection", tableRelation.getName());
        assertEquals(relatedRowClassType, tableRelation.getTarget());

        Table relatedCollectionAddSelector = tables.stream().filter(t -> t.getName().equals("relatedCollection::Add::Selector")).findFirst().orElseThrow();
        assertTrue(relatedCollectionAddSelector.getDataElement() instanceof ClassType);

        assertEquals("Related Collection", relatedCollectionAddSelector.getLabel());
        assertEquals(12, relatedCollectionAddSelector.getCol());
        assertEquals("relatedCollection", relatedCollectionAddSelector.getRelationName());

        Table relatedAssociationSetSelector = tables.stream().filter(t -> t.getName().equals("relatedAssociation::Set::Selector")).findFirst().orElseThrow();
        assertTrue(relatedAssociationSetSelector.getDataElement() instanceof ClassType);

        assertEquals("Related Association", relatedAssociationSetSelector.getLabel());
        assertEquals(12, relatedAssociationSetSelector.getCol());
        assertEquals("relatedAssociation", relatedAssociationSetSelector.getRelationName());

        // Columns

        List<Column> columns =  userViewTable.getColumns();

        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::View::PageContainer::UserView::level1::tabs0::tab2::tab2::relatedCollection::second",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::View::PageContainer::UserView::level1::tabs0::tab2::tab2::relatedCollection::first"
        ), columns.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));


        Column firstColumn = columns.stream().filter(c -> c.getName().equals("first")).findFirst().orElseThrow();
        AttributeType firstAttribute = relatedRowClassType.getAttributes().stream().filter(a -> a.getName().equals("first")).findFirst().orElseThrow();
        Column secondColumn = columns.stream().filter(c -> c.getName().equals("second")).findFirst().orElseThrow();
        AttributeType secondAttribute = relatedRowClassType.getAttributes().stream().filter(a -> a.getName().equals("second")).findFirst().orElseThrow();

        assertEquals("First", firstColumn.getLabel());
        assertEquals("%s", firstColumn.getFormat());
        assertEquals("120", firstColumn.getWidth());
        assertEquals(firstAttribute, firstColumn.getAttributeType());
        assertTrue(firstAttribute.getIsMemberTypeDerived());
        assertTrue(firstAttribute.isIsFilterable());
        assertEquals("String", firstAttribute.getDataType().getName());

        assertEquals("Second", secondColumn.getLabel());
        assertEquals("%s", secondColumn.getFormat());
        assertEquals("120", secondColumn.getWidth());
        assertEquals(secondAttribute, secondColumn.getAttributeType());
        assertTrue(secondAttribute.getIsMemberTypeDerived());
        assertTrue(secondAttribute.isIsFilterable());
        assertEquals("Numeric", secondAttribute.getDataType().getName());

        List<Column> relatedAddSelectorColumns = relatedCollectionAddSelector.getColumns();
        assertEquals(List.of("First", "Second"), relatedAddSelectorColumns.stream().map(LabeledElement::getLabel).toList());
        assertEquals(List.of("first", "second"), relatedAddSelectorColumns.stream().map(c -> c.getAttributeType().getName()).toList());
        assertEquals(List.of("String", "Numeric"), relatedAddSelectorColumns.stream().map(c -> c.getAttributeType().getDataType().getName()).toList());

        // Filters

        List<Filter> filters =  userViewTable.getFilters();

        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::View::PageContainer::UserView::level1::tabs0::tab2::tab2::relatedCollection::firstFilter",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserView::View::PageContainer::UserView::level1::tabs0::tab2::tab2::relatedCollection::secondFilter"
        ), filters.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Filter firstFilter = filters.stream().filter(c -> c.getName().equals("firstFilter")).findFirst().orElseThrow();
        Filter secondFilter = filters.stream().filter(c -> c.getName().equals("secondFilter")).findFirst().orElseThrow();

        assertEquals("First", firstFilter.getLabel());
        assertEquals(firstAttribute, firstFilter.getAttributeType());

        assertEquals("Second", secondFilter.getLabel());
        assertEquals(secondAttribute, secondFilter.getAttributeType());

        List<Filter> relatedAddSelectorFilters = relatedCollectionAddSelector.getFilters();

        assertEquals(List.of("First", "Second"), relatedAddSelectorFilters.stream().map(LabeledElement::getLabel).toList());
        assertEquals(List.of("first", "second"), relatedAddSelectorFilters.stream().map(c -> c.getAttributeType().getName()).toList());
        assertEquals(List.of("String", "Numeric"), relatedAddSelectorFilters.stream().map(c -> c.getAttributeType().getDataType().getName()).toList());
        assertEquals(List.of(true, true), relatedAddSelectorFilters.stream().map(c -> c.getAttributeType().isIsFilterable()).toList());

        List<Filter> relatedAssociationSetSelectorFilters = relatedAssociationSetSelector.getFilters();

        assertEquals(List.of("First", "Second"), relatedAssociationSetSelectorFilters.stream().map(LabeledElement::getLabel).toList());
        assertEquals(List.of("first", "second"), relatedAssociationSetSelectorFilters.stream().map(c -> c.getAttributeType().getName()).toList());
        assertEquals(List.of("String", "Numeric"), relatedAssociationSetSelectorFilters.stream().map(c -> c.getAttributeType().getDataType().getName()).toList());
        assertEquals(List.of(true, true), relatedAssociationSetSelectorFilters.stream().map(c -> c.getAttributeType().isIsFilterable()).toList());

        // User Form

        PageDefinition usersForm = application.getPages().stream().filter(p -> p.getName().equals("RelationWidgetsTestModel::RelationWidgets::users::AccessFormPage")).findFirst().orElseThrow();

        List<VisualElement> formChildren = ((Flex) usersForm.getContainer().getChildren().get(0)).getChildren();

        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::emailReadOnly",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::emailWritable",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection"
        ), formChildren.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        // primitives

        TextInput formEmailReadonly = (TextInput) formChildren.get(0);
        assertEquals("emailReadOnly", formEmailReadonly.getName());
        assertEquals("Readonly Email", formEmailReadonly.getLabel());
        assertEquals("text", formEmailReadonly.getIcon().getIconName());
        assertTrue(formEmailReadonly.isIsReadOnly());

        TextInput formEmailWritable = (TextInput) formChildren.get(1);
        assertEquals("emailWritable", formEmailWritable.getName());
        assertEquals("Writable Email", formEmailWritable.getLabel());
        assertEquals("text", formEmailWritable.getIcon().getIconName());
        assertFalse(formEmailWritable.isIsReadOnly());

        // group level1

        Flex formLevel1 = (Flex) formChildren.get(2);
        assertEquals("level1", formLevel1.getName());
        assertEquals("Yo", formLevel1.getLabel());
        assertEquals("text", formLevel1.getIcon().getIconName());

        assertEquals(Set.of(
            "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1::related"
        ), formLevel1.getChildren().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Link formLevel1Related = (Link) formLevel1.getChildren().get(0);
        assertEquals("related", formLevel1Related.getName());
        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1::related::related::Actions::related::Delete",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1::related::related::Actions::related::View",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::level1::related::related::Actions::related::Create::Open"
        ), formLevel1Related.getActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button userFormRelatedView = formLevel1Related.getActionButtonGroup().getButtons().get(0);
        assertEquals("related::View", userFormRelatedView.getName());
        assertEquals("View", userFormRelatedView.getLabel());
        assertEquals("contained", userFormRelatedView.getButtonStyle());
        assertTrue(userFormRelatedView.getActionDefinition().getIsOpenPageAction());

        Button userFormRelatedCreateOpen = formLevel1Related.getActionButtonGroup().getButtons().get(1);
        assertEquals("related::Create::Open", userFormRelatedCreateOpen.getName());
        assertEquals("Create", userFormRelatedCreateOpen.getLabel());
        assertEquals("contained", userFormRelatedCreateOpen.getButtonStyle());
        assertTrue(userFormRelatedCreateOpen.getActionDefinition().getIsOpenCreateFormAction());

        Button userFormRelatedDelete = formLevel1Related.getActionButtonGroup().getButtons().get(2);
        assertEquals("related::Delete", userFormRelatedDelete.getName());
        assertEquals("Delete", userFormRelatedDelete.getLabel());
        assertEquals("contained", userFormRelatedDelete.getButtonStyle());
        assertTrue(userFormRelatedDelete.getActionDefinition().getIsRowDeleteAction());

        // table

        Table formRelatedCollection = (Table) formChildren.get(3);
        assertEquals("relatedCollection", formRelatedCollection.getName());
        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::first",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::second"
        ), formRelatedCollection.getColumns().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));
        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::firstFilter",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::secondFilter"
        ), formRelatedCollection.getFilters().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));
        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::Clear",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::OpenAddSelector",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::Refresh",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::BulkRemove",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::Filter",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::OpenCreate"
        ), formRelatedCollection.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button formRelatedCollectionFilter = formRelatedCollection.getTableActionButtonGroup().getButtons().get(0);
        assertEquals("relatedCollection::Filter", formRelatedCollectionFilter.getName());
        assertEquals("Filter", formRelatedCollectionFilter.getLabel());
        assertEquals("text", formRelatedCollectionFilter.getButtonStyle());
        assertTrue(formRelatedCollectionFilter.getActionDefinition().getIsFilterAction());

        Button formRelatedCollectionRefresh = formRelatedCollection.getTableActionButtonGroup().getButtons().get(1);
        assertEquals("relatedCollection::Refresh", formRelatedCollectionRefresh.getName());
        assertEquals("Refresh", formRelatedCollectionRefresh.getLabel());
        assertEquals("text", formRelatedCollectionRefresh.getButtonStyle());
        assertTrue(formRelatedCollectionRefresh.getActionDefinition().getIsRefreshAction());

        Button formRelatedCollectionOpenCreate = formRelatedCollection.getTableActionButtonGroup().getButtons().get(2);
        assertEquals("relatedCollection::OpenCreate", formRelatedCollectionOpenCreate.getName());
        assertEquals("Create", formRelatedCollectionOpenCreate.getLabel());
        assertEquals("text", formRelatedCollectionOpenCreate.getButtonStyle());
        assertTrue(formRelatedCollectionOpenCreate.getActionDefinition().getIsOpenCreateFormAction());

        Button formRelatedCollectionOpenAddSelector = formRelatedCollection.getTableActionButtonGroup().getButtons().get(3);
        assertEquals("relatedCollection::OpenAddSelector", formRelatedCollectionOpenAddSelector.getName());
        assertEquals("Add", formRelatedCollectionOpenAddSelector.getLabel());
        assertEquals("text", formRelatedCollectionOpenAddSelector.getButtonStyle());
        assertTrue(formRelatedCollectionOpenAddSelector.getActionDefinition().getIsOpenAddSelectorAction());

        Button formRelatedCollectionClear = formRelatedCollection.getTableActionButtonGroup().getButtons().get(4);
        assertEquals("relatedCollection::Clear", formRelatedCollectionClear.getName());
        assertEquals("Clear", formRelatedCollectionClear.getLabel());
        assertEquals("text", formRelatedCollectionClear.getButtonStyle());
        assertTrue(formRelatedCollectionClear.getActionDefinition().getIsClearAction());

        Button formRelatedCollectionBulkRemove = formRelatedCollection.getTableActionButtonGroup().getButtons().get(5);
        assertEquals("relatedCollection::BulkRemove", formRelatedCollectionBulkRemove.getName());
        assertEquals("Remove", formRelatedCollectionBulkRemove.getLabel());
        assertEquals("text", formRelatedCollectionBulkRemove.getButtonStyle());
        assertTrue(formRelatedCollectionBulkRemove.getActionDefinition().getIsBulkRemoveAction());

        assertEquals(Set.of(
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollectionInlineViewTableRowButtonGroup::relatedCollection::RowDelete",
                "RelationWidgetsActor::RelationWidgetsTestModel::UserForm::Create::PageContainer::UserForm::relatedCollection::relatedCollectionInlineViewTableRowButtonGroup::relatedCollection::View"
        ), formRelatedCollection.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button formRelatedCollectionRowView = formRelatedCollection.getRowActionButtonGroup().getButtons().get(0);
        assertEquals("relatedCollection::View", formRelatedCollectionRowView.getName());
        assertEquals("View", formRelatedCollectionRowView.getLabel());
        assertEquals("contained", formRelatedCollectionRowView.getButtonStyle());
        assertTrue(formRelatedCollectionRowView.getActionDefinition().getIsOpenPageAction());

        Button formRelatedCollectionRowDelete = formRelatedCollection.getRowActionButtonGroup().getButtons().get(1);
        assertEquals("relatedCollection::RowDelete", formRelatedCollectionRowDelete.getName());
        assertEquals("Delete", formRelatedCollectionRowDelete.getLabel());
        assertEquals("contained", formRelatedCollectionRowDelete.getButtonStyle());
        assertTrue(formRelatedCollectionRowDelete.getActionDefinition().getIsRowDeleteAction());
    }
}
