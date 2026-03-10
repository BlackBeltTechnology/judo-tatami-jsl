package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.DataElement;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import hu.blackbelt.judo.tatami.core.TransformationMode;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslModel2UiActionGroupsTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/action-group-operations";

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

    private static String createModelString(String name) {
        return """
                    model %s;
                
                    import judo::types;

                    widget binary BinaryWidget;
                    widget boolean BooleanWidget;
                    widget date DateWidget;
                    widget enum ComboWidget;
                    widget enum RadioWidget;
                    widget numeric NumericWidget;
                    widget string StringWidget;
                    widget time TimeWidget;
                    widget timestamp TimestampWidget;

                    // mapped transfer

                    entity Entity1 {
                        field Integer number;
                        relation Entity2[] entities;
                    }

                    entity Entity2 {
                        field Integer number;
                    }

                    transfer Transfer1(Entity1 e1) {
                        field Integer number <=> e1.number;

                        action void myAction1();
                        action Transfer1 myAction2(Transfer1 input choices:Entity1.all());
                        action Transfer2 myAction3(TransferX input);

                        event create createT1;
                        event update updateT1;

                        relation Transfer2[] list2 <= e1.entities;
                    }

                    transfer Transfer2(Entity2 e2) {
                        field Integer number <=> e2.number;

                        action void someAction();
                    }

                    transfer TransferX {
                        field Integer number;
                    }

                    form Form1(Transfer1 t1) {
                        widget NumericWidget number <=> t1.number;
                    }

                    form Form2(Transfer2 t2) {
                        widget NumericWidget number <=> t2.number;
                    }

                    form FormX(TransferX tx) {
                        widget NumericWidget number <=> tx.number;
                    }

                    view View1(Transfer1 t1) {
                        widget NumericWidget number <=> t1.number;

                        action void myAction1() <= t1.myAction1 label:"my action 1";
                        action View1 myAction2(View1 input selector:Table1) <= t1.myAction2  label:"my action 2";
                        action View2 myAction3(FormX input) <= t1.myAction3 label:"my action 3";

                        actions myActions label:"hello" icon:"bello" {
                            action View1 myAction4(View1 input selector:Table1) <= t1.myAction2  label:"my action 4";
                            action View2 myAction5(FormX input) <= t1.myAction3 label:"my action 5" icon:"horse";
                        }

                        table Table2[] table2 <= t1.list2
                            actions: {
                                action View1 myAction6(View1 input selector:Table1) <= t1.myAction2  label:"my action 6";
                            };
                    }

                    view View2(Transfer2 t2) {
                        widget NumericWidget number <=> t2.number;
                    }

                    row Table1(Transfer1 t1) {
                        column Integer number <= t1.number;
                    }

                    row Table2(Transfer2 t2) {
                        column Integer number <= t2.number;

                        action void someAction() <= t2.someAction label:"some action";
                    }

                    actor A {
                        access Transfer1[] t1s <= Entity1.all() create update;
                        access Transfer1 t1 <= Entity1.any() create update;
                    }

                    frontend M(A a)
                        menu: {
                            table Table1[] v1s <= a.t1s view:View1 form:Form1;
                            link View1 v1 <= a.t1 label:"V1";
                        };
                """.formatted(name);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL"}) // TODO: Enable ZETA when gaps are fixed
    void testTableOperations(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("TableOperations", List.of(createModelString("TableOperations")));

        transform(mode);

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        assertEquals(List.of(
                "A::TableOperations::A::t1",
                "A::TableOperations::A::t1s",
                "A::TableOperations::Transfer1::list2"
        ), application.getRelationTypes().stream().map(r -> ((DataElement) r).getFQName()).sorted().toList());

        RelationType list2 = (RelationType) application.getRelationTypes().stream().filter(r -> ((DataElement) r).getFQName().equals("A::TableOperations::Transfer1::list2")).findFirst().orElseThrow();

        assertEquals(List.of(
                "A::TableOperations::M::DashboardPage",
                "A::TableOperations::M::v1::AccessViewPage",
                "A::TableOperations::M::v1s::AccessFormPage",
                "A::TableOperations::M::v1s::AccessTablePage",
                "A::TableOperations::M::v1s::AccessTableViewPage",
                "A::TableOperations::View1::myAction2::OperationInputSelector",
                "A::TableOperations::View1::myAction2::OperationOutput",
                "A::TableOperations::View1::myAction3::OperationInputForm",
                "A::TableOperations::View1::myAction3::OperationOutput",
                "A::TableOperations::View1::myActions::myAction4::OperationInputSelector",
                "A::TableOperations::View1::myActions::myAction4::OperationOutput",
                "A::TableOperations::View1::myActions::myAction5::OperationInputForm",
                "A::TableOperations::View1::myActions::myAction5::OperationOutput",
                "A::TableOperations::View1::table2::myAction6::OperationInputSelector",
                "A::TableOperations::View1::table2::myAction6::OperationOutput"
        ), application.getPages().stream().map(NamedElement::getFQName).sorted().toList());

        PageDefinition accessTableView1Page = application.getPages().stream().filter(p -> p.getFQName().equals("A::TableOperations::M::v1s::AccessTableViewPage")).findFirst().orElseThrow();

        assertEquals(List.of(
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::Table2::someAction::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myAction1::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myAction2::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myAction3::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myActions::myAction4::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myActions::myAction5::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::table2::myAction6::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::table2::Filter",
                "A::TableOperations::M::v1s::AccessTableViewPage::table2::Refresh",
                "A::TableOperations::M::v1s::AccessTableViewPage::v1s::Back",
                "A::TableOperations::M::v1s::AccessTableViewPage::v1s::Refresh",
                "A::TableOperations::M::v1s::AccessTableViewPage::v1s::Update"
        ), accessTableView1Page.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::TableOperations::Form1::Create::PageContainer",
                "A::TableOperations::FormX::Create::PageContainer",
                "A::TableOperations::M::Dashboard",
                "A::TableOperations::Table1::Table::PageContainer",
                "A::TableOperations::Table2::Table::PageContainer",
                "A::TableOperations::View1::View::PageContainer",
                "A::TableOperations::View2::View::PageContainer"
        ), application.getPageContainers().stream().map(NamedElement::getFQName).sorted().toList());

        PageContainer view1Container = accessTableView1Page.getContainer();

        assertEquals(List.of(
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Back::TableOperations::View1::Back",
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Delete::TableOperations::View1::Delete",
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Refresh::TableOperations::View1::Refresh",
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Update::TableOperations::View1::Update",
                "A::TableOperations::View1::View::PageContainer::View1::myAction1::TableOperations::View1::myAction1::Call",
                "A::TableOperations::View1::View::PageContainer::View1::myAction2::TableOperations::View1::myAction2::Open::Selector",
                "A::TableOperations::View1::View::PageContainer::View1::myAction3::TableOperations::View1::myAction3::Open::Operation::Form",
                "A::TableOperations::View1::View::PageContainer::View1::myActions::myAction4::TableOperations::View1::myActions::myAction4::Open::Selector",
                "A::TableOperations::View1::View::PageContainer::View1::myActions::myAction5::TableOperations::View1::myActions::myAction5::Open::Operation::Form",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::myAction6::TableOperations::View1::table2::myAction6::Open::Selector",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Filter::table2::Filter",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Refresh::table2::Refresh",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2InlineViewTableRowButtonGroup::someAction::TableOperations::Table2::someAction::Call"
        ), view1Container.getAllActionDefinitions().stream().map(a -> ((ActionDefinition) a).getFQName()).toList());

        ButtonGroup myActions = (ButtonGroup) ((Flex) view1Container.getChildren().get(0)).getChildren().stream().filter(c -> c.getName().equals("myActions")).findFirst().orElseThrow();

        assertEquals("myActions", myActions.getName());
        assertEquals("hello", myActions.getLabel());
        assertEquals("bello", myActions.getIcon().getIconName());

        Button myAction4 = myActions.getButtons().stream().filter(c -> c.getName().equals("myAction4")).findFirst().orElseThrow();
        ActionDefinition myAction4ActionDefinition = (ActionDefinition) view1Container.getAllActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getFQName().equals("A::TableOperations::View1::View::PageContainer::View1::myActions::myAction4::TableOperations::View1::myActions::myAction4::Open::Selector")).findFirst().orElseThrow();

        assertEquals(myAction4ActionDefinition, myAction4.getActionDefinition());
        assertEquals("my action 4", myAction4.getLabel());
        assertNull(myAction4.getIcon());

        Button myAction5 = myActions.getButtons().stream().filter(c -> c.getName().equals("myAction5")).findFirst().orElseThrow();
        ActionDefinition myAction5ActionDefinition = (ActionDefinition) view1Container.getAllActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getFQName().equals("A::TableOperations::View1::View::PageContainer::View1::myActions::myAction5::TableOperations::View1::myActions::myAction5::Open::Operation::Form")).findFirst().orElseThrow();

        assertEquals(myAction5ActionDefinition, myAction5.getActionDefinition());
        assertEquals("my action 5", myAction5.getLabel());
        assertEquals("horse", myAction5.getIcon().getIconName());

        Table table2 = (Table) view1Container.getTables().stream().filter(t -> ((Table) t).getName().equals("table2")).findFirst().orElseThrow();
        ButtonGroup table2RowButtons = table2.getRowActionButtonGroup();
        ButtonGroup table2TableButtons = table2.getTableActionButtonGroup();

        assertEquals(List.of(
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2InlineViewTableRowButtonGroup::someAction"
        ), table2RowButtons.getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        Button someActionButton =  table2RowButtons.getButtons().stream().filter(c -> c.getName().equals("someAction")).findFirst().orElseThrow();

        assertTrue(someActionButton.getActionDefinition().getIsCallOperationAction());
        assertEquals(list2.getTarget(), someActionButton.getActionDefinition().getTargetType());
        assertEquals("text", someActionButton.getButtonStyle());

        Action table2SomeAction = accessTableView1Page.getActions().stream().filter(a -> a.getFQName().equals("A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::Table2::someAction::Action")).findFirst().orElseThrow();

        assertEquals(list2, table2SomeAction.getOwnerDataElement());

        assertEquals(List.of(
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::myAction6",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Filter",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Refresh"
        ), table2TableButtons.getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        Action table2MyAction6 = accessTableView1Page.getActions().stream().filter(a -> a.getFQName().equals("A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::table2::myAction6::Action")).findFirst().orElseThrow();

        Button myAction6 = table2TableButtons.getButtons().stream().filter(c -> c.getName().equals("myAction6")).findFirst().orElseThrow();
        ClassType transfer1 = (ClassType) application.getDataElements().stream().filter(d -> d.getFQName().equals("A::TableOperations::Transfer1")).findFirst().orElseThrow();

        assertTrue(myAction6.getActionDefinition().getIsOpenOperationInputSelectorAction());
        assertEquals(table2MyAction6.getActionDefinition(), myAction6.getActionDefinition());
        assertEquals(transfer1, myAction6.getActionDefinition().getTargetType());
        assertEquals("text", myAction6.getButtonStyle());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL"}) // TODO: Enable ZETA when gaps are fixed
    void testLinkOperations(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("LinkOperations", List.of(createModelString("LinkOperations")));

        transform(mode);

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        PageDefinition accessLinkView1Page = application.getPages().stream().filter(p -> p.getFQName().equals("A::LinkOperations::M::v1::AccessViewPage")).findFirst().orElseThrow();

        assertEquals(List.of(
                "A::LinkOperations::M::v1::AccessViewPage::LinkOperations::Table2::someAction::Action",
                "A::LinkOperations::M::v1::AccessViewPage::LinkOperations::View1::myAction1::Action",
                "A::LinkOperations::M::v1::AccessViewPage::LinkOperations::View1::myAction2::Action",
                "A::LinkOperations::M::v1::AccessViewPage::LinkOperations::View1::myAction3::Action",
                "A::LinkOperations::M::v1::AccessViewPage::LinkOperations::View1::myActions::myAction4::Action",
                "A::LinkOperations::M::v1::AccessViewPage::LinkOperations::View1::myActions::myAction5::Action",
                "A::LinkOperations::M::v1::AccessViewPage::LinkOperations::View1::table2::myAction6::Action",
                "A::LinkOperations::M::v1::AccessViewPage::table2::Filter",
                "A::LinkOperations::M::v1::AccessViewPage::table2::Refresh",
                "A::LinkOperations::M::v1::AccessViewPage::v1::Back",
                "A::LinkOperations::M::v1::AccessViewPage::v1::Refresh",
                "A::LinkOperations::M::v1::AccessViewPage::v1::Update"
        ), accessLinkView1Page.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        PageContainer view1Container = accessLinkView1Page.getContainer();

        assertEquals(List.of(
                "A::LinkOperations::View1::View::PageContainer::LinkOperations::View1::PageActions::LinkOperations::View1::Back::LinkOperations::View1::Back",
                "A::LinkOperations::View1::View::PageContainer::LinkOperations::View1::PageActions::LinkOperations::View1::Delete::LinkOperations::View1::Delete",
                "A::LinkOperations::View1::View::PageContainer::LinkOperations::View1::PageActions::LinkOperations::View1::Refresh::LinkOperations::View1::Refresh",
                "A::LinkOperations::View1::View::PageContainer::LinkOperations::View1::PageActions::LinkOperations::View1::Update::LinkOperations::View1::Update",
                "A::LinkOperations::View1::View::PageContainer::View1::myAction1::LinkOperations::View1::myAction1::Call",
                "A::LinkOperations::View1::View::PageContainer::View1::myAction2::LinkOperations::View1::myAction2::Open::Selector",
                "A::LinkOperations::View1::View::PageContainer::View1::myAction3::LinkOperations::View1::myAction3::Open::Operation::Form",
                "A::LinkOperations::View1::View::PageContainer::View1::myActions::myAction4::LinkOperations::View1::myActions::myAction4::Open::Selector",
                "A::LinkOperations::View1::View::PageContainer::View1::myActions::myAction5::LinkOperations::View1::myActions::myAction5::Open::Operation::Form",
                "A::LinkOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::myAction6::LinkOperations::View1::table2::myAction6::Open::Selector",
                "A::LinkOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Filter::table2::Filter",
                "A::LinkOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Refresh::table2::Refresh",
                "A::LinkOperations::View1::View::PageContainer::View1::table2::table2InlineViewTableRowButtonGroup::someAction::LinkOperations::Table2::someAction::Call"
        ), view1Container.getAllActionDefinitions().stream().map(a -> ((ActionDefinition) a).getFQName()).toList());

        ButtonGroup myActions = (ButtonGroup) ((Flex) view1Container.getChildren().get(0)).getChildren().stream().filter(c -> c.getName().equals("myActions")).findFirst().orElseThrow();

        assertEquals("myActions", myActions.getName());
        assertEquals("hello", myActions.getLabel());
        assertEquals("bello", myActions.getIcon().getIconName());

        Button myAction4 = myActions.getButtons().stream().filter(c -> c.getName().equals("myAction4")).findFirst().orElseThrow();
        ActionDefinition myAction4ActionDefinition = (ActionDefinition) view1Container.getAllActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getFQName().equals("A::LinkOperations::View1::View::PageContainer::View1::myActions::myAction4::LinkOperations::View1::myActions::myAction4::Open::Selector")).findFirst().orElseThrow();

        assertEquals(myAction4ActionDefinition, myAction4.getActionDefinition());
        assertEquals("my action 4", myAction4.getLabel());
        assertNull(myAction4.getIcon());

        Button myAction5 = myActions.getButtons().stream().filter(c -> c.getName().equals("myAction5")).findFirst().orElseThrow();
        ActionDefinition myAction5ActionDefinition = (ActionDefinition) view1Container.getAllActionDefinitions().stream().filter(a -> ((ActionDefinition) a).getFQName().equals("A::LinkOperations::View1::View::PageContainer::View1::myActions::myAction5::LinkOperations::View1::myActions::myAction5::Open::Operation::Form")).findFirst().orElseThrow();

        assertEquals(myAction5ActionDefinition, myAction5.getActionDefinition());
        assertEquals("my action 5", myAction5.getLabel());
        assertEquals("horse", myAction5.getIcon().getIconName());
    }
}
