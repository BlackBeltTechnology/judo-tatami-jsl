package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JslModel2UiRowOperationsTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/row-operations";

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

                    widget numeric NumericWidget;
                
                     // mapped transfer
                
                     entity Entity1 {
                         field Integer number;
                         relation Entity2[] relatedCollection;
                     }

                     entity Entity2 {
                         field String name;
                     }
                
                     transfer Transfer1(Entity1 e1) {
                     	 field Integer number <=> e1.number;

                     	 event create onCreate;
                         event update onUpdate;
                         event delete onDelete;
                     }

                     transfer Transfer3(Entity2 e2) {
                     	 field String name <=> e2.name;
                     	 action void myAction4();

                         event create onCreate;
                         event update onUpdate;
                         event delete onDelete;
                     }
                
                     view View1(Transfer1 t1) {
                     	widget NumericWidget number <=> t1.number;
                     }
                
                     row Row1(Transfer1 t1) {
                     	column Integer number <= t1.number;
                     }

                     row Row3(Transfer3 t3) {
                     	column String name <= t3.name;
                     	action void myAction4() <= t3.myAction4 label:"my action 4";
                     }
                
                     // unmapped transfers
                
                     transfer Transfer2 {
                         field Integer number;
                     }
                
                     view View2(Transfer2 t2) {
                         widget NumericWidget number <=> t2.number;
                     }
                
                     form Form2(Transfer2 t2) {
                         widget NumericWidget number <=> t2.number;
                     }
                
                     // test
                
                     transfer TransferX(Entity1 e1) {
                     	field Integer number <=> e1.number;
                     	relation Transfer3[] relatedCollection <= e1.relatedCollection create update;
                
                     	action void myAction1();
                     	action Transfer1 myAction2(Transfer1 input choices:Entity1.all());
                     	action Transfer2 myAction3(Transfer2 input);
                
                     	event create createTX;
                     	event update updateTX;
                     }
                
                     view ViewX(TransferX tx) {
                     	widget NumericWidget number <=> tx.number;

                     	group level {
                     	    table Row3[] relatedCollection <= tx.relatedCollection;
                        }
                
                     	action void myAction1() <= tx.myAction1 label:"my action 1";
                     	action View1 myAction2(View1 input selector:Row1) <= tx.myAction2  label:"my action 2";
                     	action View2 myAction3(Form2 input) <= tx.myAction3 label:"my action 3";
                     }
                
                     form FormX(TransferX tx) {
                     	widget NumericWidget number <=> tx.number;
                     }
                
                     row RowX(TransferX tx) {
                     	column Integer number <= tx.number;
                     	action View2 myAction3(Form2 input) <= tx.myAction3 label:"my action 3";
                     }
                
                     actor A {
                     	access TransferX[] txs <= Entity1.all() create:true update:true;
                     }
                
                     frontend M(A a)
                        menu: {
                            table RowX[] vxs <= a.txs view:ViewX form:FormX;
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
    @EnumSource(value = TransformationMode.class)
    void testAccessTableRowOperations(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("AccessTableRowOperations", List.of(createModelString("AccessTableRowOperations")));

        transform(mode);

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        List<PageDefinition> pages = application.getPages();

        assertEquals(List.of(
                "A::AccessTableRowOperations::M::DashboardPage",
                "A::AccessTableRowOperations::M::vxs::AccessFormPage",
                "A::AccessTableRowOperations::M::vxs::AccessTablePage",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage",
                "A::AccessTableRowOperations::RowX::myAction3::OperationInputForm",
                "A::AccessTableRowOperations::RowX::myAction3::OperationOutput",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationInputSelector",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationOutput",
                "A::AccessTableRowOperations::ViewX::myAction3::OperationInputForm",
                "A::AccessTableRowOperations::ViewX::myAction3::OperationOutput"
        ), pages.stream().map(NamedElement::getFQName).sorted().toList());

        List<Action> actions = pages.stream().flatMap(p -> p.getActions().stream()).toList();

        assertEquals(List.of(
                "A::AccessTableRowOperations::M::vxs::AccessFormPage::vxs::Back",
                "A::AccessTableRowOperations::M::vxs::AccessFormPage::vxs::Create",
                "A::AccessTableRowOperations::M::vxs::AccessFormPage::vxs::GetTemplate",
                "A::AccessTableRowOperations::M::vxs::AccessTablePage::AccessTableRowOperations::RowX::myAction3::Action",
                "A::AccessTableRowOperations::M::vxs::AccessTablePage::vxs::Filter",
                "A::AccessTableRowOperations::M::vxs::AccessTablePage::vxs::OpenCreate",
                "A::AccessTableRowOperations::M::vxs::AccessTablePage::vxs::OpenPage",
                "A::AccessTableRowOperations::M::vxs::AccessTablePage::vxs::Refresh",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::AccessTableRowOperations::Row3::myAction4::Action",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::AccessTableRowOperations::ViewX::myAction1::Action",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::AccessTableRowOperations::ViewX::myAction2::Action",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::AccessTableRowOperations::ViewX::myAction3::Action",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::relatedCollection::Filter",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::relatedCollection::Refresh",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::vxs::Back",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::vxs::Refresh",
                "A::AccessTableRowOperations::M::vxs::AccessTableViewPage::vxs::Update",
                "A::AccessTableRowOperations::RowX::myAction3::OperationInputForm::myAction3::Back",
                "A::AccessTableRowOperations::RowX::myAction3::OperationInputForm::myAction3::CallOperation",
                "A::AccessTableRowOperations::RowX::myAction3::OperationInputForm::myAction3::GetTemplate",
                "A::AccessTableRowOperations::RowX::myAction3::OperationOutput::myAction3::Back",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationInputSelector::myAction2::Back",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationInputSelector::myAction2::CallOperation",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationInputSelector::myAction2::Filter",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationInputSelector::myAction2::Refresh",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationOutput::AccessTableRowOperations::ViewX::myAction2::Delete",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationOutput::myAction2::Back",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationOutput::myAction2::Refresh",
                "A::AccessTableRowOperations::ViewX::myAction2::OperationOutput::myAction2::Update",
                "A::AccessTableRowOperations::ViewX::myAction3::OperationInputForm::myAction3::Back",
                "A::AccessTableRowOperations::ViewX::myAction3::OperationInputForm::myAction3::CallOperation",
                "A::AccessTableRowOperations::ViewX::myAction3::OperationInputForm::myAction3::GetTemplate",
                "A::AccessTableRowOperations::ViewX::myAction3::OperationOutput::myAction3::Back"
        ), actions.stream().map(NamedElement::getFQName).sorted().toList());

        PageDefinition myAction3Input = pages.stream().filter(a -> a.getFQName().equals("A::AccessTableRowOperations::RowX::myAction3::OperationInputForm")).findFirst().orElseThrow();
        PageDefinition myAction3Output = pages.stream().filter(a -> a.getFQName().equals("A::AccessTableRowOperations::RowX::myAction3::OperationOutput")).findFirst().orElseThrow();
        Action myAction3Call = actions.stream().filter(a -> a.getFQName().equals("A::AccessTableRowOperations::RowX::myAction3::OperationInputForm::myAction3::CallOperation")).findFirst().orElseThrow();
        Action accessTableMyAction3 = actions.stream().filter(a -> a.getFQName().equals("A::AccessTableRowOperations::M::vxs::AccessTablePage::AccessTableRowOperations::RowX::myAction3::Action")).findFirst().orElseThrow();

        assertTrue(accessTableMyAction3.isOpenOperationInputFormAction());
        assertTrue(accessTableMyAction3.isOpenOperationInputFormAction());
        assertEquals(myAction3Input, accessTableMyAction3.getTargetPageDefinition());
        assertTrue(myAction3Input.getActions().contains(myAction3Call));
        assertEquals(myAction3Output, myAction3Call.getTargetPageDefinition());


        PageDefinition accessView = pages.stream().filter(p -> p.getFQName().equals("A::AccessTableRowOperations::M::vxs::AccessTableViewPage")).findFirst().orElseThrow();
        PageDefinition myAction2InputSelector = pages.stream().filter(p -> p.getFQName().equals("A::AccessTableRowOperations::ViewX::myAction2::OperationInputSelector")).findFirst().orElseThrow();
        Table relatedCollectionTable = (Table) accessView.getContainer().getTables().stream().filter(t -> ((Table) t).getName().equals("relatedCollection")).findFirst().orElseThrow();
        Action myAction2 = accessView.getActions().stream().filter(a -> a.getFQName().equals("A::AccessTableRowOperations::M::vxs::AccessTableViewPage::AccessTableRowOperations::ViewX::myAction2::Action")).findFirst().orElseThrow();
        Action myAction2Call = myAction2InputSelector.getActions().stream().filter(a -> a.getFQName().equals("A::AccessTableRowOperations::ViewX::myAction2::OperationInputSelector::myAction2::CallOperation")).findFirst().orElseThrow();
        Action myAction4 = accessView.getActions().stream().filter(a -> a.getFQName().equals("A::AccessTableRowOperations::M::vxs::AccessTableViewPage::AccessTableRowOperations::Row3::myAction4::Action")).findFirst().orElseThrow();

        assertEquals(myAction2Call.getActionDefinition(), ((OpenOperationInputSelectorActionDefinition) myAction2.getActionDefinition()).getSelectorFor());

        assertTrue(relatedCollectionTable.getRowActionButtonGroup().getButtons().stream().anyMatch(b -> myAction4.getActionDefinition().equals(b.getActionDefinition())));

        assertEquals(List.of(
                "A::AccessTableRowOperations::ViewX::View::PageContainer::ViewX::level::relatedCollection::relatedCollectionInlineViewTableRowButtonGroup::myAction4"
        ), relatedCollectionTable.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        Button myAction4Button = relatedCollectionTable.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getFQName().equals("A::AccessTableRowOperations::ViewX::View::PageContainer::ViewX::level::relatedCollection::relatedCollectionInlineViewTableRowButtonGroup::myAction4")).findFirst().get();

        assertEquals("my action 4", myAction4Button.getLabel());
        assertTrue(myAction4Button.getActionDefinition().getIsCallOperationAction());
    }
}
