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
                    }

                    transfer TransferX {
                        field Integer number;
                    }

                    form Form1(Transfer1 t1) {
                        widget Integer number <=> t1.number;
                    }

                    form Form2(Transfer2 t2) {
                        widget Integer number <=> t2.number;
                    }

                    form FormX(TransferX tx) {
                        widget Integer number <=> tx.number;
                    }

                    view View1(Transfer1 t1) {
                        widget Integer number <=> t1.number;

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
                        widget Integer number <=> t2.number;
                    }

                    row Table1(Transfer1 t1) {
                        column Integer number <= t1.number;
                    }

                    row Table2(Transfer2 t2) {
                        column Integer number <= t2.number;
                    }

                    actor A {
                        access Transfer1[] t1s <= Entity1.all() create update;
                    }

                    menu M(A a) {
                        table Table1[] v1s <= a.t1s view:View1 form:Form1;
                    }
                """.formatted(name);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testTableOperations() throws Exception {
        jslModel = JslParser.getModelFromStrings("TableOperations", List.of(createModelString("TableOperations")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        assertEquals(List.of(
                "A::TableOperations::M::DashboardPage",
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
                "A::TableOperations::View1::myActions::myAction5::OperationOutput"
        ), application.getPages().stream().map(NamedElement::getFQName).sorted().toList());

        PageDefinition view1Page = application.getPages().stream().filter(p -> p.getFQName().equals("A::TableOperations::M::v1s::AccessTableViewPage")).findFirst().orElseThrow();

        assertEquals(List.of(
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myAction1::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myAction2::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myAction3::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myActions::myAction4::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::TableOperations::View1::myActions::myAction5::Action",
                "A::TableOperations::M::v1s::AccessTableViewPage::table2::Filter",
                "A::TableOperations::M::v1s::AccessTableViewPage::table2::Refresh",
                "A::TableOperations::M::v1s::AccessTableViewPage::v1s::Back",
                "A::TableOperations::M::v1s::AccessTableViewPage::v1s::Cancel",
                "A::TableOperations::M::v1s::AccessTableViewPage::v1s::Refresh",
                "A::TableOperations::M::v1s::AccessTableViewPage::v1s::Update"
        ), view1Page.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::TableOperations::Form1::Create::PageContainer",
                "A::TableOperations::FormX::Create::PageContainer",
                "A::TableOperations::M::Dashboard",
                "A::TableOperations::Table1::Table::PageContainer",
                "A::TableOperations::Table2::Table::PageContainer",
                "A::TableOperations::View1::View::PageContainer",
                "A::TableOperations::View2::View::PageContainer"
        ), application.getPageContainers().stream().map(NamedElement::getFQName).sorted().toList());

        PageContainer view1Container = view1Page.getContainer();

        assertEquals(List.of(
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Back::TableOperations::View1::Back",
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Cancel::TableOperations::View1::Cancel",
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Delete::TableOperations::View1::Delete",
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Refresh::TableOperations::View1::Refresh",
                "A::TableOperations::View1::View::PageContainer::TableOperations::View1::PageActions::TableOperations::View1::Update::TableOperations::View1::Update",
                "A::TableOperations::View1::View::PageContainer::View1::myAction1::TableOperations::View1::myAction1::Call",
                "A::TableOperations::View1::View::PageContainer::View1::myAction2::TableOperations::View1::myAction2::Open::Selector",
                "A::TableOperations::View1::View::PageContainer::View1::myAction3::TableOperations::View1::myAction3::Open::Operation::Form",
                "A::TableOperations::View1::View::PageContainer::View1::myActions::myAction4::TableOperations::View1::myActions::myAction4::Open::Selector",
                "A::TableOperations::View1::View::PageContainer::View1::myActions::myAction5::TableOperations::View1::myActions::myAction5::Open::Operation::Form",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Filter::table2::Filter",
                "A::TableOperations::View1::View::PageContainer::View1::table2::table2::InlineViewTableButtonGroup::table2::Refresh::table2::Refresh"
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

    }
}
