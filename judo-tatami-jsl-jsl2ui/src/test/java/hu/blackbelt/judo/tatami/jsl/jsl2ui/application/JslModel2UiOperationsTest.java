package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JslModel2UiOperationsTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/operations";

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

            error Error1 {
            }

            error ErrorWithDefaults {
                field String withDefault default:"Hello!";
            }

            entity Entity1 {
                field Integer number;
            }

            transfer Transfer1(Entity1 e1) {
                field Integer number <=> e1.number;
            }

            view View1(Transfer1 t1) {
                widget Integer number <=> t1.number;
            }

            table Table1(Transfer1 t1) {
                column Integer number <= t1.number;
            }

            // unmapped transfers

            transfer Transfer2 {
                field Integer number;
            }

            view View2(Transfer2 t2) {
                widget Integer number <=> t2.number;
            }

            form Form2(Transfer2 t2) {
                widget Integer number <=> t2.number;
            }

            // test

            transfer TransferX(Entity1 e1) {
                field Integer number <=> e1.number;

                action void myAction1() throws Error1,ErrorWithDefaults;
                action Transfer1 myAction2(Transfer1 input choices:Entity1.all());
                action Transfer2 myAction3(Transfer2 input);

                event create createTX;
                event update updateTX;
            }

            view ViewX(TransferX tx) {
                widget Integer number <=> tx.number;

                group level1 label:"Yo" icon:"text" {
                    action void myAction1() <= tx.myAction1 label:"my action 1" icon:"flower";
                }
                action View1 myAction2(View1 input selector:Table1) <= tx.myAction2  label:"my action 2";
                action View2 myAction3(Form2 input) <= tx.myAction3 label:"my action 3";
            }

            form FormX(TransferX tx) {
                widget Integer number <=> tx.number;
            }

            table TableX(TransferX tx) {
                column Integer number <= tx.number;
            }

            actor A {
                access TransferX[] txs <= Entity1.all() create:true update:true;
            }

            menu M(A a) {
                table TableX vxs <= a.txs view:ViewX form:FormX;
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
    void testOperationsOnViews() throws Exception {
        jslModel = JslParser.getModelFromStrings("OperationsOnViews", List.of(createModelString("OperationsOnViews")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        // DataElements

        assertEquals(List.of(
                "A::OperationsOnViews::A",
                "A::OperationsOnViews::Error1",
                "A::OperationsOnViews::ErrorWithDefaults",
                "A::OperationsOnViews::Transfer1",
                "A::OperationsOnViews::Transfer2",
                "A::OperationsOnViews::TransferX"
        ), application.getDataElements().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::OperationsOnViews::M::DashboardPage",
                "A::OperationsOnViews::M::vxs::AccessFormPage",
                "A::OperationsOnViews::M::vxs::AccessTablePage",
                "A::OperationsOnViews::M::vxs::AccessTableViewPage",
                "A::OperationsOnViews::ViewX::myAction2::OperationInputSelector",
                "A::OperationsOnViews::ViewX::myAction3::OperationInputForm",
                "A::myAction2::OperationOutput",
                "A::myAction3::OperationOutput"
        ), application.getPages().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::OperationsOnViews::A::txs"
        ), application.getRelationTypes().stream().map(r -> ((RelationType) r).getFQName()).sorted().toList());

        // Error(s)

        ClassType errorWithDefaults = (ClassType) application.getDataElements().stream()
                .filter(e -> Objects.equals(e.getFQName(), "A::OperationsOnViews::ErrorWithDefaults"))
                .findFirst().orElseThrow();

        assertEquals(List.of(
            "A::OperationsOnViews::ErrorWithDefaults::withDefault"
        ), errorWithDefaults.getAttributes().stream().map(NamedElement::getFQName).sorted().toList());

        AttributeType withDefault = errorWithDefaults.getAttributes().stream()
                .filter(e -> Objects.equals(e.getFQName(), "A::OperationsOnViews::ErrorWithDefaults::withDefault"))
                .findFirst().orElseThrow();

        assertEquals("String", withDefault.getDataType().getName());

        // ClassTypes

        List<ClassType> classTypes = application.getClassTypes();

        assertEquals(List.of(
                "A::OperationsOnViews::A",
                "A::OperationsOnViews::Error1",
                "A::OperationsOnViews::ErrorWithDefaults",
                "A::OperationsOnViews::Transfer1",
                "A::OperationsOnViews::Transfer2",
                "A::OperationsOnViews::TransferX"
        ), classTypes.stream().map(NamedElement::getFQName).sorted().toList());

        ClassType transferX = classTypes.stream()
                .filter(c -> c.getFQName().equals("A::OperationsOnViews::TransferX"))
                .findFirst().orElseThrow();

        List<OperationType> transferXOperationTypes = transferX.getOperations();

        assertEquals(List.of(
                "A::OperationsOnViews::TransferX::myAction1",
                "A::OperationsOnViews::TransferX::myAction2",
                "A::OperationsOnViews::TransferX::myAction3"
        ), transferXOperationTypes.stream().map(NamedElement::getFQName).sorted().toList());

        // operations

        List<OperationType> allOperations = classTypes.stream()
                .flatMap(c -> c.getOperations().stream())
                .toList();

        assertEquals(List.of(
                "A::OperationsOnViews::TransferX::myAction1",
                "A::OperationsOnViews::TransferX::myAction2",
                "A::OperationsOnViews::TransferX::myAction3"
        ), allOperations.stream().map(NamedElement::getFQName).sorted().toList());
    }

    @Test
    void testParameterlessVoidOperationsOnViews() throws Exception {
        jslModel = JslParser.getModelFromStrings("ParameterlessVoidOperationsOnViews", List.of(createModelString("ParameterlessVoidOperationsOnViews")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        List<ClassType> classTypes = application.getClassTypes();

        ClassType transferX = classTypes.stream()
                .filter(c -> c.getFQName().equals("A::ParameterlessVoidOperationsOnViews::TransferX"))
                .findFirst().orElseThrow();

        OperationType myAction1 = transferX.getOperations().stream()
                .filter(o -> o.getFQName().equals("A::ParameterlessVoidOperationsOnViews::TransferX::myAction1"))
                .findFirst().orElseThrow();

        assertNull(myAction1.getInput());
        assertNull(myAction1.getOutput());

        List<OperationParameterType> myAction1Faults = myAction1.getFaults();

        assertEquals(List.of(
                "A::ParameterlessVoidOperationsOnViews::TransferX::myAction1::Error1",
                "A::ParameterlessVoidOperationsOnViews::TransferX::myAction1::ErrorWithDefaults"
        ), myAction1Faults.stream().map(NamedElement::getFQName).sorted().toList());

    }

    @Test
    void testOperationsOnViewsWithInputSelectors() throws Exception {
        jslModel = JslParser.getModelFromStrings("OperationsOnViewsWithInputSelectors", List.of(createModelString("OperationsOnViewsWithInputSelectors")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        List<ClassType> classTypes = application.getClassTypes();

        ClassType transferX = classTypes.stream()
                .filter(c -> c.getFQName().equals("A::OperationsOnViewsWithInputSelectors::TransferX"))
                .findFirst().orElseThrow();

        OperationType myAction2Operation = transferX.getOperations().stream()
                .filter(o -> o.getFQName().equals("A::OperationsOnViewsWithInputSelectors::TransferX::myAction2"))
                .findFirst().orElseThrow();

        OperationParameterType myAction2Input = myAction2Operation.getInput();
        OperationParameterType myAction2Output = myAction2Operation.getOutput();

        assertEquals("A::OperationsOnViewsWithInputSelectors::TransferX::myAction2::input", myAction2Input.getFQName());
        assertEquals("A::OperationsOnViewsWithInputSelectors::TransferX::myAction2::output", myAction2Output.getFQName());

        assertTrue(myAction2Input.isIsOrderable());
        assertTrue(myAction2Input.isIsFilterable());

        assertEquals(List.of(
                "REFRESH"
        ), myAction2Output.getBehaviours().stream().map(OperationTargetBehaviourType::getName).sorted().toList());

        List<OperationParameterType> myAction2Faults = myAction2Operation.getFaults();

        assertEquals(List.of(), myAction2Faults.stream().map(NamedElement::getFQName).sorted().toList());

        PageDefinition accessTableViewPage = application.getPages().stream().filter(p -> p.getFQName().equals("A::OperationsOnViewsWithInputSelectors::M::vxs::AccessTableViewPage")).findFirst().orElseThrow();

        List<Action> accessTableViewPageActions = accessTableViewPage.getActions();

        PageDefinition myAction2InputSelectorPage = application.getPages().stream().filter(p -> p.getFQName().equals("A::OperationsOnViewsWithInputSelectors::ViewX::myAction2::OperationInputSelector")).findFirst().orElseThrow();
        PageDefinition myAction2OutputPage = application.getPages().stream().filter(p -> p.getFQName().equals("A::myAction2::OperationOutput")).findFirst().orElseThrow();

        Action myAction2 = accessTableViewPageActions.stream().filter( a -> a.getFQName().equals("A::OperationsOnViewsWithInputSelectors::M::vxs::AccessTableViewPage::OperationsOnViewsWithInputSelectors::ViewX::myAction2::Action")).findFirst().orElseThrow();

        RelationType txs = (RelationType) application.getRelationTypes().stream().filter(r -> ((RelationType) r).getFQName().equals("A::OperationsOnViewsWithInputSelectors::A::txs")).findFirst().orElseThrow();

        assertEquals("OperationsOnViewsWithInputSelectors::ViewX::myAction2::Action", myAction2.getName());
        assertEquals(myAction2Operation, myAction2.getTargetDataElement());
        assertEquals(txs, myAction2.getOwnerDataElement());
        assertEquals(myAction2InputSelectorPage, myAction2.getTargetPageDefinition());
        assertTrue(myAction2InputSelectorPage.isOpenInDialog());
        assertTrue(myAction2InputSelectorPage.getContainer().isTable());

        Table selectorTable = (Table) myAction2InputSelectorPage.getContainer().getTables().get(0);

        assertEquals(List.of(
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::number"
        ), selectorTable.getColumns().stream().map(NamedElement::getFQName).sorted().toList());

        Column selectorTableNumberColumn = selectorTable.getColumns().stream().filter(c -> c.getFQName().equals("A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::number")).findFirst().orElseThrow();

        assertEquals("Integer", selectorTableNumberColumn.getAttributeType().getDataType().getName());
        assertEquals("number", selectorTableNumberColumn.getAttributeType().getName());
        assertEquals(Sort.NONE, selectorTableNumberColumn.getSort());

        List<Button> selectorTableButtons = selectorTable.getTableActionButtonGroup().getButtons();
        List<Button> selectorTableRowButtons = selectorTable.getRowActionButtonGroup().getButtons();

        assertEquals(List.of(
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1::TableTableButtonGroup::Table1::BulkRemove",
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1::TableTableButtonGroup::Table1::Clear",
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1::TableTableButtonGroup::Table1::Filter",
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1::TableTableButtonGroup::Table1::OpenAddSelector",
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1::TableTableButtonGroup::Table1::OpenCreate",
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1::TableTableButtonGroup::Table1::Refresh"
        ), selectorTableButtons.stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1TableRowButtonGroup::Table1::RowDelete",
                "A::OperationsOnViewsWithInputSelectors::Table1::Table::PageContainer::Table1::Table1::Table::Table1TableRowButtonGroup::Table1::View"
        ), selectorTableRowButtons.stream().map(NamedElement::getFQName).sorted().toList());

        List<Action> myAction2InputActions = myAction2InputSelectorPage.getActions();

        assertEquals(List.of(
                "A::OperationsOnViewsWithInputSelectors::ViewX::myAction2::OperationInputSelector::myAction2::Back",
                "A::OperationsOnViewsWithInputSelectors::ViewX::myAction2::OperationInputSelector::myAction2::CallOperation"
        ), myAction2InputActions.stream().map(NamedElement::getFQName).sorted().toList());

        Action myAction2CallOperationAction = myAction2InputActions.stream().filter(a -> a.getFQName().equals("A::OperationsOnViewsWithInputSelectors::ViewX::myAction2::OperationInputSelector::myAction2::CallOperation")).findFirst().orElseThrow();

        assertEquals(myAction2OutputPage, myAction2CallOperationAction.getTargetPageDefinition());
        assertEquals(myAction2Operation, myAction2CallOperationAction.getTargetDataElement());
        assertTrue(myAction2OutputPage.isOpenInDialog());

        assertEquals(List.of(
                "A::myAction2::OperationOutput::myAction2::Back",
                "A::myAction2::OperationOutput::myAction2::Refresh"
        ), myAction2OutputPage.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        Action myAction2OutputRefreshAction = myAction2OutputPage.getActions().stream().filter(a -> a.getFQName().equals("A::myAction2::OperationOutput::myAction2::Refresh")).findFirst().orElseThrow();

        assertEquals(myAction2Operation, myAction2OutputRefreshAction.getOwnerDataElement());

    }

    @Test
    void testOperationsOnViewsWithInputForms() throws Exception {
        jslModel = JslParser.getModelFromStrings("OperationsOnViewsWithInputForms", List.of(createModelString("OperationsOnViewsWithInputForms")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        List<ClassType> classTypes = application.getClassTypes();

        ClassType transferX = classTypes.stream()
                .filter(c -> c.getFQName().equals("A::OperationsOnViewsWithInputForms::TransferX"))
                .findFirst().orElseThrow();

        OperationType myAction3Operation = transferX.getOperations().stream()
                .filter(o -> o.getFQName().equals("A::OperationsOnViewsWithInputForms::TransferX::myAction3"))
                .findFirst().orElseThrow();

        OperationParameterType myAction3Input = myAction3Operation.getInput();
        OperationParameterType myAction3Output = myAction3Operation.getOutput();

        assertEquals("A::OperationsOnViewsWithInputForms::TransferX::myAction3::input", myAction3Input.getFQName());
        assertEquals("A::OperationsOnViewsWithInputForms::TransferX::myAction3::output", myAction3Output.getFQName());

        PageDefinition inputPage = application.getPages().stream().filter(p -> p.getFQName().equals("A::OperationsOnViewsWithInputForms::ViewX::myAction3::OperationInputForm")).findFirst().orElseThrow();
        PageDefinition outputPage = application.getPages().stream().filter(p -> p.getFQName().equals("A::myAction3::OperationOutput")).findFirst().orElseThrow();

        assertEquals(myAction3Input, inputPage.getDataElement());

        List<Action> inputPageActions = inputPage.getActions();

        assertEquals(List.of(
                "A::OperationsOnViewsWithInputForms::ViewX::myAction3::OperationInputForm::myAction3::Back",
                "A::OperationsOnViewsWithInputForms::ViewX::myAction3::OperationInputForm::myAction3::CallOperation",
                "A::OperationsOnViewsWithInputForms::ViewX::myAction3::OperationInputForm::myAction3::GetTemplate"
        ), inputPageActions.stream().map(NamedElement::getFQName).sorted().toList());

        Action callOperationAction = inputPageActions.stream().filter(a -> a.getFQName().equals("A::OperationsOnViewsWithInputForms::ViewX::myAction3::OperationInputForm::myAction3::CallOperation")).findFirst().orElseThrow();

        assertEquals(myAction3Operation, callOperationAction.getTargetDataElement());
        assertEquals(outputPage, callOperationAction.getTargetPageDefinition());
    }
}
