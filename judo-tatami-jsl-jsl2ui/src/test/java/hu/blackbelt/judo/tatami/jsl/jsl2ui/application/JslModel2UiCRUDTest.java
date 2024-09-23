package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
public class JslModel2UiCRUDTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/crud";

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

            entity User {
                identifier String email required;
                field Integer numeric;
        
                relation Related related;
                relation Related[] relatedCollection;
            }
        
            entity Related {
                field String first;
                field Integer second;
                relation Jumper theJumper;
                relation Jumper[] theJumpersCollection;
            }
        
            entity Jumper {
                field String first;
                field String second;
            }
        
            transfer UserTransfer(User u) {
                field String email <= u.email;
                relation RelatedTransfer related <= u.related choices:Related.all() eager create update delete;
                relation RelatedTransfer[] relatedCollection <= u.relatedCollection create update;
        
                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }
        
            transfer RelatedTransfer(Related r) {
                field String first <= r.first;
                field Integer second <= r.second;
                relation JumperTransfer theJumper <= r.theJumper create update delete choices:Jumper.all();
                relation JumperTransfer[] theJumpersCollection <= r.theJumpersCollection create update delete choices:Jumper.all();
        
                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }
        
            transfer JumperTransfer(Jumper j) {
                field String first <=j.first;
                field String second <= j.second;
        
                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }
        
            view UserView(UserTransfer u) {
                widget String email <= u.email label:"Email";
                group level {
                    link RelatedView related <= u.related icon:"related" label:"Related" width:6 form:RelatedForm selector:RelatedTable;
                    table RelatedTable relatedCollection <= u.relatedCollection icon:"relatedCollection" label:"Related Collection" view:RelatedView;
                }
            }
        
            table RelatedTable(RelatedTransfer r) {
                column String first <= r.first label:"First";
                column Integer second <= r.second label:"Second";
            }
        
            view RelatedView(RelatedTransfer r) {
                widget String first <= r.first label:"First";
                widget Integer second <= r.second label:"Second";
                group g1 {
                    link JumperView readOnlyJumper <= r.theJumper icon:"jumping" label:"Read only Jumper" form:JumperForm view:JumperView selector:JumperTable;
                    link JumperView myJumper <= r.theJumper icon:"jumping" label:"My Jumper" width:6 form:JumperForm selector:JumperTable;
                    table JumperTable myJumpers <= r.theJumpersCollection icon:"jumping-all" label:"My Jumpers" width:6 view:JumperView form:JumperForm selector:JumperTable;
                }
            }
        
            form RelatedForm(RelatedTransfer r) {
                group one label:"Group 1" {
                    widget String first <= r.first label:"First";
                }
            }
        
            view JumperView(JumperTransfer j) {
                widget String first <= j.first label:"First";
            }
        
            table JumperTable(JumperTransfer j) {
                column String second <= j.second label:"Second";
            }
        
            form JumperForm(JumperTransfer j) {
                group one label:"Group 1" {
                    widget String firstOnForm <= j.first label:"First on form";
                }
            }
        
            actor NavigationActor {
                access UserTransfer user <= User.any() update delete;
            }
        
            menu NavigationApp(NavigationActor a) {
                link UserView user <= a.user label:"User" icon:"tools" view:UserView;
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
    void testSummaryCRUD() throws Exception {
        jslModel = JslParser.getModelFromStrings("SummaryCRUD", List.of(createModelString("SummaryCRUD")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        List<RelationType> relationTypes = application.getRelationTypes();
        List<ClassType> classTypes = application.getClassTypes();
        List<PageContainer> pageContainers = application.getPageContainers();
        List<PageDefinition> pages = application.getPages();
        List<Link> links = application.getLinks();
        List<Table> tables = application.getTables();
        List<Action> allActions = application.getPages().stream().flatMap(ps -> ps.getActions().stream()).toList();

        PageDefinition actorDashboardPage = pages.stream().filter(p -> p.getName().equals("SummaryCRUD::NavigationApp::DashboardPage")).findFirst().orElseThrow();

        assertEquals(Set.of(
                "NavigationActor::SummaryCRUD::NavigationActor::user",
                "NavigationActor::SummaryCRUD::RelatedTransfer::theJumpersCollection",
                "NavigationActor::SummaryCRUD::RelatedTransfer::theJumper",
                "NavigationActor::SummaryCRUD::UserTransfer::relatedCollection",
                "NavigationActor::SummaryCRUD::UserTransfer::related"
        ), relationTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SummaryCRUD::NavigationActor",
                "NavigationActor::SummaryCRUD::RelatedTransfer",
                "NavigationActor::SummaryCRUD::JumperTransfer",
                "NavigationActor::SummaryCRUD::UserTransfer"
        ), classTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SummaryCRUD::NavigationApp::Dashboard",
                "NavigationActor::SummaryCRUD::UserView::View::PageContainer",
                "NavigationActor::SummaryCRUD::UserView::level::related::SetSelector::PageContainer",
                "NavigationActor::SummaryCRUD::RelatedTable::Table::PageContainer",
                "NavigationActor::SummaryCRUD::RelatedForm::Create::PageContainer",
                "NavigationActor::SummaryCRUD::RelatedView::View::PageContainer",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::SetSelector::PageContainer",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::AddSelector::PageContainer",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelector::PageContainer",
                "NavigationActor::SummaryCRUD::JumperTable::Table::PageContainer",
                "NavigationActor::SummaryCRUD::JumperView::View::PageContainer",
                "NavigationActor::SummaryCRUD::JumperForm::Create::PageContainer"
        ), pageContainers.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SummaryCRUD::NavigationApp::DashboardPage",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage",
                "NavigationActor::SummaryCRUD::UserView::level::related::FormPage",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage",
                "NavigationActor::SummaryCRUD::UserView::level::related::SetSelectorPage",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::ViewPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::AddSelectorPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::FormPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::ViewPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::FormPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::SetSelectorPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::ViewPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::FormPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelectorPage"
        ), pages.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SummaryCRUD::UserView::View::PageContainer::UserView::level::related",
                "NavigationActor::SummaryCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper",
                "NavigationActor::SummaryCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper"
        ), links.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SummaryCRUD::UserView::View::PageContainer::UserView::level::relatedCollection",
                "NavigationActor::SummaryCRUD::UserView::level::related::SetSelector::PageContainer::related::related::Set::Selector",
                "NavigationActor::SummaryCRUD::JumperTable::Table::PageContainer::JumperTable::JumperTable::Table",
                "NavigationActor::SummaryCRUD::RelatedTable::Table::PageContainer::RelatedTable::RelatedTable::Table",
                "NavigationActor::SummaryCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::SetSelector::PageContainer::myJumper::myJumper::Set::Selector",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::AddSelector::PageContainer::myJumpers::myJumpers::Add::Selector",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelector::PageContainer::readOnlyJumper::readOnlyJumper::Set::Selector"
        ), tables.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SummaryCRUD::UserView::level::related::SetSelectorPage::SummaryCRUD::UserView::level::related::SetSelector::Set",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::FormPage::readOnlyJumper::Back",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::FormPage::myJumpers::Back",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SummaryCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Filter",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::ViewPage::readOnlyJumper::Delete",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SummaryCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Range",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::FormPage::myJumper::Back",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::ViewPage::myJumper::Update",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::readOnlyJumper::RowDelete",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::OpenCreate",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::OpenPage",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::related::Refresh",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::ViewPage::myJumpers::Refresh",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumper::RowDelete",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::ViewPage::myJumpers::Delete",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::user::Back",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::readOnlyJumper::Unset",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::ViewPage::myJumper::Delete",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::OpenAddSelector",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::relatedCollection::Back",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::related::OpenForm",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::ViewPage::myJumpers::Update",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::ViewPage::myJumper::Refresh",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SummaryCRUD::RelatedView::g1::myJumpers::AddSelector::Back",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::RowDelete",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::ViewPage::readOnlyJumper::Update",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::myJumper::SetSelector::Set",
                "NavigationActor::SummaryCRUD::UserView::level::related::FormPage::related::Create",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumper::OpenForm",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::myJumper::SetSelector::Back",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::related::OpenSetSelector",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::readOnlyJumper::Refresh",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::related::Update",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::Filter",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::FormPage::myJumpers::Create",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::relatedCollection::Refresh",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::FormPage::readOnlyJumper::Create",
                "NavigationActor::SummaryCRUD::UserView::level::related::SetSelectorPage::SummaryCRUD::UserView::level::related::SetSelector::Back",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumper::OpenForm",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::ViewPage::myJumpers::Back",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::FormPage::myJumper::Create",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::ViewPage::readOnlyJumper::Refresh",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::OpenCreate",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::Filter",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::Clear",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::user::Refresh",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::related::RowDelete",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::relatedCollection::Refresh",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::relatedCollection::Update",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelector::Table::Range",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::OpenAddSelector",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::OpenPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::myJumper::SetSelector::Table::Range",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::related::OpenPage",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::user::Update",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::readOnlyJumper::Refresh",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenForm",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::readOnlyJumper::OpenSetSelector",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumper::RowDelete",
                "NavigationActor::SummaryCRUD::UserView::level::related::FormPage::related::Back",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumper::OpenSetSelector",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::user::Delete",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::readOnlyJumper::Unset",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::related::Back",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelector::Table::Filter",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::readOnlyJumper::OpenPage",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumper::Refresh",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelector::Back",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::readOnlyJumper::RowDelete",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::related::Delete",
                "NavigationActor::SummaryCRUD::UserView::level::related::SetSelectorPage::SummaryCRUD::UserView::level::related::SetSelector::Table::Filter",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::readOnlyJumper::OpenForm",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::Refresh",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumper::OpenPage",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenPage",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::BulkRemove",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::RowDelete",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::ViewPage::readOnlyJumper::Back",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::Refresh",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::ViewPage::myJumper::Back",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::relatedCollection::OpenPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::myJumper::SetSelector::Table::Filter",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumpers::Clear",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::relatedCollection::Filter",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenSetSelector",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumper::Unset",
                "NavigationActor::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelectorPage::SummaryCRUD::RelatedView::g1::readOnlyJumper::SetSelector::Set",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumper::OpenSetSelector",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumper::Unset",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumper::OpenPage",
                "NavigationActor::SummaryCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SummaryCRUD::RelatedView::g1::myJumpers::AddSelector::Add",
                "NavigationActor::SummaryCRUD::UserView::level::related::SetSelectorPage::SummaryCRUD::UserView::level::related::SetSelector::Table::Range",
                "NavigationActor::SummaryCRUD::UserView::level::relatedCollection::ViewPage::myJumpers::BulkRemove",
                "NavigationActor::SummaryCRUD::NavigationApp::user::AccessViewPage::related::Unset",
                "NavigationActor::SummaryCRUD::UserView::level::related::ViewPage::myJumper::Refresh"
        ), allActions.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));
    }

    @Test
    void testAccessViewCRUD() throws Exception {
        jslModel = JslParser.getModelFromStrings("AccessViewCRUD", List.of(createModelString("AccessViewCRUD")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        Application application = apps.get(0);

        List<PageDefinition> pages = application.getPages();

        PageDefinition pageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::NavigationApp::user::AccessViewPage")).findFirst().orElseThrow();
        PageDefinition userViewRelatedViewPageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::UserView::level::related::ViewPage")).findFirst().orElseThrow();
        PageDefinition userViewRelatedCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::UserView::level::related::FormPage")).findFirst().orElseThrow();
        PageDefinition relatedRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::UserView::level::relatedCollection::ViewPage")).findFirst().orElseThrow();

        PageContainer pageContainer = pageDefinition.getContainer();

        assertEquals(Set.of(
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::user::Back",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::user::Refresh",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::user::Delete",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::user::Update",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::relatedCollection::Filter",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::relatedCollection::Refresh",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::relatedCollection::OpenPage",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::related::OpenForm",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::related::OpenPage",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::related::RowDelete",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::related::Unset",
                "NavigationActor::AccessViewCRUD::NavigationApp::user::AccessViewPage::related::OpenSetSelector"
        ), pageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::related"
        ), pageContainer.getLinks().stream().map(l -> ((Link) l).getFQName()).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::relatedCollection"
        ), pageContainer.getTables().stream().map(t -> ((Table) t).getFQName()).collect(Collectors.toSet()));

        Action backAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("user::Back")).findFirst().orElseThrow();
        assertTrue(backAction.getIsBackAction());

        Action updateAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("user::Update")).findFirst().orElseThrow();
        assertTrue(updateAction.getIsUpdateAction());

        Action deleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("user::Delete")).findFirst().orElseThrow();
        assertTrue(deleteAction.getIsDeleteAction());

        Action relatedOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::OpenPage")).findFirst().orElseThrow();
        assertTrue(relatedOpenPageAction.getIsOpenPageAction());
        assertEquals(userViewRelatedViewPageDefinition, relatedOpenPageAction.getTargetPageDefinition());

        Action relatedOpenFormAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::OpenForm")).findFirst().orElseThrow();
        assertTrue(relatedOpenFormAction.getIsOpenFormAction());
        assertEquals(userViewRelatedCreatePageDefinition, relatedOpenFormAction.getTargetPageDefinition());

        Action relatedRowDeleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::RowDelete")).findFirst().orElseThrow();
        assertTrue(relatedRowDeleteAction.getIsRowDeleteAction());

        Action relatedCollectionOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::OpenPage")).findFirst().orElseThrow();
        assertTrue(relatedCollectionOpenPageAction.getIsOpenPageAction());
        assertEquals(relatedRowDetailViewPageDefinition, relatedCollectionOpenPageAction.getTargetPageDefinition());

        Action relatedCollectionFilterAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::Filter")).findFirst().orElseThrow();
        assertTrue(relatedCollectionFilterAction.getIsFilterAction());

        Action relatedCollectionRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::Refresh")).findFirst().orElseThrow();
        assertTrue(relatedCollectionRefreshAction.getIsRefreshAction());

        // - Link - related

        Link related = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("related")).findFirst().orElseThrow();
        assertEquals("related", related.getDataElement().getName());
        assertEquals(Set.of(
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::related::related::Actions::related::OpenSetSelector",
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::related::related::Actions::related::Create::Open",
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::related::related::Actions::related::Delete",
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::related::related::Actions::related::View",
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::related::related::Actions::related::Unset"
        ), related.getActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button relatedCreateOpen = related.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("related::Create::Open")).findFirst().orElseThrow();
        assertTrue(relatedCreateOpen.getActionDefinition().getIsOpenCreateFormAction());
        assertButtonVisuals(relatedCreateOpen, "Create", "note-add", "contained");
        assertEquals(relatedOpenFormAction.getActionDefinition(), relatedCreateOpen.getActionDefinition());

        Button relatedView = related.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("related::View")).findFirst().orElseThrow();
        assertTrue(relatedView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(relatedView, "View", "eye", "contained");
        assertEquals(relatedOpenPageAction.getActionDefinition(), relatedView.getActionDefinition());
        assertEquals(userViewRelatedViewPageDefinition, relatedOpenPageAction.getTargetPageDefinition());

        Button relatedDelete = related.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("related::Delete")).findFirst().orElseThrow();
        assertTrue(relatedDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(relatedDelete, "Delete", "delete_forever", "contained");
        assertEquals(relatedRowDeleteAction.getActionDefinition(), relatedDelete.getActionDefinition());

        // - Table - relatedCollection

        Table relatedCollection = (Table) pageContainer.getTables().stream().filter(t -> ((Table) t).getName().equals("relatedCollection")).findFirst().orElseThrow();
        assertEquals("relatedCollection", relatedCollection.getDataElement().getName());

        assertEquals(Set.of(
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::Filter",
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::relatedCollection::relatedCollection::InlineViewTableButtonGroup::relatedCollection::Refresh"
        ), relatedCollection.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button relatedCollectionFilter = relatedCollection.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::Filter")).findFirst().orElseThrow();
        assertTrue(relatedCollectionFilter.getActionDefinition().getIsFilterAction());
        assertButtonVisuals(relatedCollectionFilter, "Filter", "filter", "text");
        assertEquals(relatedCollectionFilterAction.getActionDefinition(), relatedCollectionFilter.getActionDefinition());

        Button relatedCollectionRefresh = relatedCollection.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::Refresh")).findFirst().orElseThrow();
        assertTrue(relatedCollectionRefresh.getActionDefinition().getIsRefreshAction());
        assertButtonVisuals(relatedCollectionRefresh, "Refresh", "refresh", "text");
        assertEquals(relatedCollectionRefreshAction.getActionDefinition(), relatedCollectionRefresh.getActionDefinition());

        assertEquals(Set.of(
                "NavigationActor::AccessViewCRUD::UserView::View::PageContainer::UserView::level::relatedCollection::relatedCollectionInlineViewTableRowButtonGroup::relatedCollection::View"
        ), relatedCollection.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button relatedCollectionView = relatedCollection.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::View")).findFirst().orElseThrow();
        assertTrue(relatedCollectionView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(relatedCollectionView, "View", "visibility", "contained");
        assertEquals(relatedCollectionOpenPageAction.getActionDefinition(), relatedCollectionView.getActionDefinition());
        assertEquals(userViewRelatedViewPageDefinition, relatedOpenPageAction.getTargetPageDefinition());

    }

    @Test
    void testSingleRelationViewCRUD() throws Exception {
        jslModel = JslParser.getModelFromStrings("SingleRelationViewCRUD", List.of(createModelString("SingleRelationViewCRUD")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        Application application = apps.get(0);
        List<PageDefinition> pages = application.getPages();

        PageDefinition pageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::UserView::level::related::ViewPage")).findFirst().orElseThrow();
        PageContainer pageContainer = pageDefinition.getContainer();

        PageDefinition readOnlyJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::g1::readOnlyJumper::ViewPage")).findFirst().orElseThrow();
        PageDefinition relatedSetSelectorPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::UserView::level::related::SetSelectorPage")).findFirst().orElseThrow();
        PageDefinition myJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::ViewPage")).findFirst().orElseThrow();
        PageDefinition myJumperCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::FormPage")).findFirst().orElseThrow();
        PageDefinition myJumperSetSelectorPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelectorPage")).findFirst().orElseThrow();
        PageDefinition jumperRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::ViewPage")).findFirst().orElseThrow();
        PageDefinition myJumpersCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::FormPage")).findFirst().orElseThrow();
        PageDefinition myJumpersAddSelectorPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelectorPage")).findFirst().orElseThrow();
        PageContainer myJumpersAddSelectorPageContainer = myJumpersAddSelectorPageDefinition.getContainer();
        PageContainer myJumperSetSelectorPageContainer = myJumperSetSelectorPageDefinition.getContainer();

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumper::OpenForm",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumper::OpenPage",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumper::OpenSetSelector",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumper::Refresh",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumper::RowDelete",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumper::Unset",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::BulkRemove",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::Clear",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::Filter",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::OpenAddSelector",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::OpenCreate",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::OpenPage",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::Refresh",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::myJumpers::RowDelete",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenForm",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenPage",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenSetSelector",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::Refresh",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::RowDelete",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::Unset",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::related::Back",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::related::Delete",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::related::Refresh",
                "NavigationActor::SingleRelationViewCRUD::UserView::level::related::ViewPage::related::Update"
        ), pageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper"
        ), pageContainer.getLinks().stream().map(l -> ((Link) l).getFQName()).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers"
        ), pageContainer.getTables().stream().map(t -> ((Table) t).getFQName()).collect(Collectors.toSet()));

        Action BackAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::Back")).findFirst().orElseThrow();
        assertTrue(BackAction.getIsBackAction());

        Action refreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::Refresh")).findFirst().orElseThrow();
        assertTrue(refreshAction.getIsRefreshAction());

        Action updateAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::Update")).findFirst().orElseThrow();
        assertTrue(updateAction.getIsUpdateAction());

        Action deleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::Delete")).findFirst().orElseThrow();
        assertTrue(deleteAction.getIsDeleteAction());

        Action readOnlyJumperOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("readOnlyJumper::OpenPage")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperOpenPageAction.getIsOpenPageAction());
        assertEquals(readOnlyJumperViewPageDefinition, readOnlyJumperOpenPageAction.getTargetPageDefinition());

        Action readOnlyJumperRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("readOnlyJumper::Refresh")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperRefreshAction.getIsRefreshAction());

        Action myJumperOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::OpenPage")).findFirst().orElseThrow();
        assertTrue(myJumperOpenPageAction.getIsOpenPageAction());
        assertEquals(myJumperViewPageDefinition, myJumperOpenPageAction.getTargetPageDefinition());

        Action myJumperOpenFormAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::OpenForm")).findFirst().orElseThrow();
        assertTrue(myJumperOpenFormAction.getIsOpenFormAction());
        assertEquals(myJumperCreatePageDefinition, myJumperOpenFormAction.getTargetPageDefinition());

        Action myJumperRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumperRefreshAction.getIsRefreshAction());

        Action myJumperRowDeleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumperRowDeleteAction.getIsRowDeleteAction());

        Action myJumperOpenSetSelectorAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::OpenSetSelector")).findFirst().orElseThrow();
        assertTrue(myJumperOpenSetSelectorAction.isOpenSetSelectorAction());
        assertEquals(myJumperSetSelectorPageDefinition, myJumperOpenSetSelectorAction.getTargetPageDefinition());

        Action myJumperUnsetAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::Unset")).findFirst().orElseThrow();
        assertTrue(myJumperUnsetAction.getIsUnsetAction());

        Action myJumpersOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::OpenPage")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenPageAction.getIsOpenPageAction());
        assertEquals(jumperRowDetailViewPageDefinition, myJumpersOpenPageAction.getTargetPageDefinition());

        Action myJumpersFilterAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::Filter")).findFirst().orElseThrow();
        assertTrue(myJumpersFilterAction.getIsFilterAction());

        Action myJumpersRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumpersRefreshAction.getIsRefreshAction());

        Action myJumpersOpenCreateAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::OpenCreate")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenCreateAction.isOpenCreateFormAction());
        assertEquals(myJumpersCreatePageDefinition, myJumpersOpenCreateAction.getTargetPageDefinition());

        Action myJumpersRowDeleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumpersRowDeleteAction.getIsRowDeleteAction());

        Action myJumpersClearAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::Clear")).findFirst().orElseThrow();
        assertTrue(myJumpersClearAction.getIsClearAction());

        Action myJumpersBulkRemoveAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::BulkRemove")).findFirst().orElseThrow();
        assertTrue(myJumpersBulkRemoveAction.getIsBulkRemoveAction());

        Action myJumpersOpenAddSelectorAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::OpenAddSelector")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenAddSelectorAction.isOpenAddSelectorAction());
        assertEquals(myJumpersAddSelectorPageDefinition, myJumpersOpenAddSelectorAction.getTargetPageDefinition());

        // - link readOnlyJumper

        Link readOnlyJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("readOnlyJumper")).findFirst().orElseThrow();
        assertEquals("theJumper", readOnlyJumper.getDataElement().getName());
        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::OpenSetSelector",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::View",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::Delete",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::Unset",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::Create::Open"
        ), readOnlyJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button readOnlyJumperView = readOnlyJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("readOnlyJumper::View")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(readOnlyJumperView, "View", "eye", "contained");
        assertEquals(readOnlyJumperOpenPageAction.getActionDefinition(), readOnlyJumperView.getActionDefinition());
        assertEquals(readOnlyJumperViewPageDefinition, readOnlyJumperOpenPageAction.getTargetPageDefinition());

        // - Link - myJumper

        Link myJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("myJumper")).findFirst().orElseThrow();
        assertEquals("theJumper", myJumper.getDataElement().getName());
        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::Create::Open",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::View",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::OpenSetSelector",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::Unset",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::Delete"
        ), myJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumperCreateOpen = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::Create::Open")).findFirst().orElseThrow();
        assertTrue(myJumperCreateOpen.getActionDefinition().getIsOpenCreateFormAction());
        assertButtonVisuals(myJumperCreateOpen, "Create", "note-add", "contained");
        assertEquals(myJumperOpenFormAction.getActionDefinition(), myJumperCreateOpen.getActionDefinition());

        Button myJumperView = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::View")).findFirst().orElseThrow();
        assertTrue(myJumperView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(myJumperView, "View", "eye", "contained");
        assertEquals(myJumperOpenPageAction.getActionDefinition(), myJumperView.getActionDefinition());
        assertEquals(myJumperViewPageDefinition, myJumperOpenPageAction.getTargetPageDefinition());

        Button myJumperDelete = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::Delete")).findFirst().orElseThrow();
        assertTrue(myJumperDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(myJumperDelete, "Delete", "delete_forever", "contained");
        assertEquals(myJumperRowDeleteAction.getActionDefinition(), myJumperDelete.getActionDefinition());

        Button myJumperOpenSetSelector = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::OpenSetSelector")).findFirst().orElseThrow();
        assertTrue(myJumperOpenSetSelector.getActionDefinition().getIsOpenSelectorAction());
        assertButtonVisuals(myJumperOpenSetSelector, "Set", "link", "contained");
        assertEquals(myJumperOpenSetSelectorAction.getActionDefinition(), myJumperOpenSetSelector.getActionDefinition());

        Button myJumperUnset = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::Unset")).findFirst().orElseThrow();
        assertTrue(myJumperUnset.getActionDefinition().getIsUnsetAction());
        assertButtonVisuals(myJumperUnset, "Unset", "link-off", "contained");
        assertEquals(myJumperUnsetAction.getActionDefinition(), myJumperUnset.getActionDefinition());

        // - Table - myJumpers

        Table myJumpers = (Table) pageContainer.getTables().stream().filter(t -> ((Table) t).getName().equals("myJumpers")).findFirst().orElseThrow();
        assertEquals("theJumpersCollection", myJumpers.getDataElement().getName());

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::Clear",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::BulkRemove",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::Filter",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::Refresh",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::OpenAddSelector",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::OpenCreate"
        ), myJumpers.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumpersFilter = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::Filter")).findFirst().orElseThrow();
        assertTrue(myJumpersFilter.getActionDefinition().getIsFilterAction());
        assertButtonVisuals(myJumpersFilter, "Filter", "filter", "text");
        assertEquals(myJumpersFilterAction.getActionDefinition(), myJumpersFilter.getActionDefinition());

        Button myJumpersRefresh = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumpersRefresh.getActionDefinition().getIsRefreshAction());
        assertButtonVisuals(myJumpersRefresh, "Refresh", "refresh", "text");
        assertEquals(myJumpersRefreshAction.getActionDefinition(), myJumpersRefresh.getActionDefinition());

        Button myJumpersOpenCreate = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::OpenCreate")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenCreate.getActionDefinition().getIsOpenCreateFormAction());
        assertButtonVisuals(myJumpersOpenCreate, "Create", "file-document-plus", "text");
        assertEquals(myJumpersOpenCreateAction.getActionDefinition(), myJumpersOpenCreate.getActionDefinition());

        Button myJumpersClear = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::Clear")).findFirst().orElseThrow();
        assertTrue(myJumpersClear.getActionDefinition().getIsClearAction());
        assertButtonVisuals(myJumpersClear, "Clear", "link-off", "text");
        assertEquals(myJumpersClearAction.getActionDefinition(), myJumpersClear.getActionDefinition());

        Button myJumpersBulkRemove = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::BulkRemove")).findFirst().orElseThrow();
        assertTrue(myJumpersBulkRemove.getActionDefinition().getIsBulkRemoveAction());
        assertButtonVisuals(myJumpersBulkRemove, "Remove", "link-off", "text");
        assertEquals(myJumpersBulkRemoveAction.getActionDefinition(), myJumpersBulkRemove.getActionDefinition());

        Button myJumpersOpenAddSelector = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::OpenAddSelector")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenAddSelector.getActionDefinition().getIsOpenAddSelectorAction());
        assertButtonVisuals(myJumpersOpenAddSelector, "Add", "attachment-plus", "text");
        assertEquals(myJumpersOpenAddSelectorAction.getActionDefinition(), myJumpersOpenAddSelector.getActionDefinition());

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpersInlineViewTableRowButtonGroup::myJumpers::View",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpersInlineViewTableRowButtonGroup::myJumpers::RowDelete"
        ), myJumpers.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumpersView = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::View")).findFirst().orElseThrow();
        assertTrue(myJumpersView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(myJumpersView, "View", "visibility", "contained");
        assertEquals(myJumpersOpenPageAction.getActionDefinition(), myJumpersView.getActionDefinition());
        assertEquals(jumperRowDetailViewPageDefinition, myJumpersOpenPageAction.getTargetPageDefinition());

        Button myJumpersRowDelete = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumpersRowDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(myJumpersRowDelete, "Delete", "delete_forever", "contained");
        assertEquals(myJumpersRowDeleteAction.getActionDefinition(), myJumpersRowDelete.getActionDefinition());

        // add selector - myJumpers

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Add",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Range",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Back",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Filter"
        ), myJumpersAddSelectorPageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Action myJumpersAddSelectorAddAction = myJumpersAddSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Add")).findFirst().orElseThrow();
        assertTrue(myJumpersAddSelectorAddAction.getActionDefinition().getIsAddAction());

        Action myJumpersAddSelectorFilterAction = myJumpersAddSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Filter")).findFirst().orElseThrow();
        assertTrue(myJumpersAddSelectorFilterAction.getActionDefinition().getIsFilterAction());

        Action myJumpersAddSelectorrangeAction = myJumpersAddSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Range")).findFirst().orElseThrow();
        assertTrue(myJumpersAddSelectorrangeAction.getActionDefinition().getIsSelectorRangeAction());

        Action myJumpersAddSelectorBackAction = myJumpersAddSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Back")).findFirst().orElseThrow();
        assertTrue(myJumpersAddSelectorBackAction.getActionDefinition().getIsBackAction());

        Table myJumpersAddSelector = (Table) myJumpersAddSelectorPageContainer.getTables().stream().filter(t -> ((Table) t).getName().equals("myJumpers::Add::Selector")).findFirst().orElseThrow();
        assertEquals("SingleRelationViewCRUD::JumperTransfer", myJumpersAddSelector.getDataElement().getName());
        assertTrue(myJumpersAddSelector.isAllowSelectMultiple());
        assertTrue(myJumpersAddSelector.isIsRelationSelectorTable());
        assertTrue(myJumpersAddSelector.isIsSelectorTable());

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::PageContainer::myJumpers::myJumpers::Add::Selector::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelectorTableActions::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Refresh",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::PageContainer::myJumpers::myJumpers::Add::Selector::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelectorTableActions::SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Filter"
        ), myJumpersAddSelector.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumpersAddSelectorFilter = myJumpersAddSelector.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumpers::AddSelector::Table::Filter")).findFirst().orElseThrow();
        assertTrue(myJumpersAddSelectorFilter.getActionDefinition().getIsFilterAction());
        assertButtonVisuals(myJumpersAddSelectorFilter, "Set Filters", "filter", "text");
        assertEquals(myJumpersAddSelectorFilterAction.getActionDefinition(), myJumpersAddSelectorFilter.getActionDefinition());

        // set selector - myJumper

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Filter",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Back",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Set",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelectorPage::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Range"
        ), myJumperSetSelectorPageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Action myJumperSetSelectorFilterAction = myJumperSetSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Filter")).findFirst().orElseThrow();
        assertTrue(myJumperSetSelectorFilterAction.getActionDefinition().getIsFilterAction());

        Action myJumperSetSelectorrangeAction = myJumperSetSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Range")).findFirst().orElseThrow();
        assertTrue(myJumperSetSelectorrangeAction.getActionDefinition().getIsSelectorRangeAction());

        Action myJumperSetSelectorSetAction = myJumperSetSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Set")).findFirst().orElseThrow();
        assertTrue(myJumperSetSelectorSetAction.getActionDefinition().getIsSetAction());

        Action myJumperSetSelectorBackAction = myJumperSetSelectorPageDefinition.getActions().stream().filter(a -> a.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Back")).findFirst().orElseThrow();
        assertTrue(myJumperSetSelectorBackAction.getActionDefinition().getIsBackAction());

        Table myJumperSetSelector = (Table) myJumperSetSelectorPageContainer.getTables().stream().filter(t -> ((Table) t).getName().equals("myJumper::Set::Selector")).findFirst().orElseThrow();
        assertEquals("SingleRelationViewCRUD::JumperTransfer", myJumpersAddSelector.getDataElement().getName());
        assertTrue(myJumpersAddSelector.isAllowSelectMultiple());
        assertTrue(myJumpersAddSelector.isIsRelationSelectorTable());
        assertTrue(myJumpersAddSelector.isIsSelectorTable());

        assertEquals(Set.of(
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::PageContainer::myJumper::myJumper::Set::Selector::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelectorTableActions::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Refresh",
                "NavigationActor::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::PageContainer::myJumper::myJumper::Set::Selector::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelectorTableActions::SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Filter"
        ), myJumperSetSelector.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumperSetSelectorFilter = myJumperSetSelector.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Filter")).findFirst().orElseThrow();
        assertTrue(myJumperSetSelectorFilter.getActionDefinition().getIsFilterAction());
        assertButtonVisuals(myJumperSetSelectorFilter, "Set Filters", "filter", "text");
        assertEquals(myJumperSetSelectorFilterAction.getActionDefinition(), myJumperSetSelectorFilter.getActionDefinition());

        Button myJumperSetSelectorRefresh = myJumperSetSelector.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("SingleRelationViewCRUD::RelatedView::g1::myJumper::SetSelector::Table::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumperSetSelectorRefresh.getActionDefinition().getIsSelectorRangeAction());
        assertButtonVisuals(myJumperSetSelectorRefresh, "Refresh", "refresh", "text");
        assertEquals(myJumperSetSelectorrangeAction.getActionDefinition(), myJumperSetSelectorRefresh.getActionDefinition());
    }

    @Test
    void testRelatedRowDetailViewCRUD() throws Exception {
        jslModel = JslParser.getModelFromStrings("RelatedRowDetailViewCRUD", List.of(createModelString("RelatedRowDetailViewCRUD")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        Application application = apps.get(0);
        List<PageDefinition> pages = application.getPages();

        PageDefinition pageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::UserView::level::related::ViewPage")).findFirst().orElseThrow();
        PageContainer pageContainer = pageDefinition.getContainer();

        PageDefinition readOnlyJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::g1::readOnlyJumper::ViewPage")).findFirst().orElseThrow();

        PageDefinition myJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::g1::myJumper::ViewPage")).findFirst().orElseThrow();
        PageDefinition myJumperCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::g1::myJumper::FormPage")).findFirst().orElseThrow();
        PageDefinition myJumpersCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::g1::myJumpers::FormPage")).findFirst().orElseThrow();
        PageDefinition myJumpersAddSelectorPageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::g1::myJumpers::AddSelectorPage")).findFirst().orElseThrow();

        PageDefinition jumperRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::g1::myJumpers::ViewPage")).findFirst().orElseThrow();

        assertEquals(24, pageDefinition.getActions().size());
        assertEquals(2, pageContainer.getLinks().size());
        assertEquals(1, pageContainer.getTables().size());

        assertEquals(Set.of(
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumper::OpenForm",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumper::OpenPage",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumper::OpenSetSelector",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumper::Refresh",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumper::RowDelete",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumper::Unset",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::BulkRemove",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::Clear",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::Filter",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::OpenAddSelector",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::OpenCreate",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::OpenPage",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::Refresh",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::myJumpers::RowDelete",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenForm",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenPage",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::OpenSetSelector",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::Refresh",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::RowDelete",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::readOnlyJumper::Unset",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::related::Back",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::related::Delete",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::related::Refresh",
                "NavigationActor::RelatedRowDetailViewCRUD::UserView::level::related::ViewPage::related::Update"
        ), pageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Action BackAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::Back")).findFirst().orElseThrow();
        assertTrue(BackAction.getIsBackAction());

        Action refreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("related::Refresh")).findFirst().orElseThrow();
        assertTrue(refreshAction.getIsRefreshAction());

        Action readOnlyJumperOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("readOnlyJumper::OpenPage")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperOpenPageAction.getIsOpenPageAction());
        assertEquals(readOnlyJumperViewPageDefinition, readOnlyJumperOpenPageAction.getTargetPageDefinition());

        Action readOnlyJumperRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("readOnlyJumper::Refresh")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperRefreshAction.getIsRefreshAction());

        Action myJumperOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::OpenPage")).findFirst().orElseThrow();
        assertTrue(myJumperOpenPageAction.getIsOpenPageAction());
        assertEquals(myJumperViewPageDefinition, myJumperOpenPageAction.getTargetPageDefinition());

        Action myJumperOpenFormAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::OpenForm")).findFirst().orElseThrow();
        assertTrue(myJumperOpenFormAction.getIsOpenFormAction());
        assertEquals(myJumperCreatePageDefinition, myJumperOpenFormAction.getTargetPageDefinition());

        Action myJumperRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumperRefreshAction.getIsRefreshAction());

        Action myJumperRowDeleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumperRowDeleteAction.getIsRowDeleteAction());

        Action myJumpersOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::OpenPage")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenPageAction.getIsOpenPageAction());
        assertEquals(jumperRowDetailViewPageDefinition, myJumpersOpenPageAction.getTargetPageDefinition());

        Action myJumpersFilterAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::Filter")).findFirst().orElseThrow();
        assertTrue(myJumpersFilterAction.getIsFilterAction());

        Action myJumpersRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumpersRefreshAction.getIsRefreshAction());

        Action myJumpersOpenCreateAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::OpenCreate")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenCreateAction.getIsOpenFormAction());
        assertEquals(myJumpersCreatePageDefinition, myJumpersOpenCreateAction.getTargetPageDefinition());

        Action myJumpersRowDeleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumpersRowDeleteAction.getIsRowDeleteAction());

        Action myJumpersOpenAddSelectorAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::OpenAddSelector")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenAddSelectorAction.isOpenAddSelectorAction());
        assertEquals(myJumpersAddSelectorPageDefinition, myJumpersOpenAddSelectorAction.getTargetPageDefinition());

        // - link readOnlyJumper

        Link readOnlyJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("readOnlyJumper")).findFirst().orElseThrow();
        assertEquals("theJumper", readOnlyJumper.getDataElement().getName());
        assertEquals(Set.of(
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::Delete",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::Create::Open",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::OpenSetSelector",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::Unset",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::readOnlyJumper::readOnlyJumper::Actions::readOnlyJumper::View"
        ), readOnlyJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button readOnlyJumperView = readOnlyJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("readOnlyJumper::View")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(readOnlyJumperView, "View", "eye", "contained");
        assertEquals(readOnlyJumperOpenPageAction.getActionDefinition(), readOnlyJumperView.getActionDefinition());
        assertEquals(readOnlyJumperViewPageDefinition, readOnlyJumperOpenPageAction.getTargetPageDefinition());

        // - Link - myJumper

        Link myJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("myJumper")).findFirst().orElseThrow();
        assertEquals("theJumper", myJumper.getDataElement().getName());
        assertEquals(Set.of(
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::Delete",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::Create::Open",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::OpenSetSelector",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::Unset",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumper::myJumper::Actions::myJumper::View"
        ), myJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumperCreateOpen = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::Create::Open")).findFirst().orElseThrow();
        assertTrue(myJumperCreateOpen.getActionDefinition().getIsOpenCreateFormAction());
        assertButtonVisuals(myJumperCreateOpen, "Create", "note-add", "contained");
        assertEquals(myJumperOpenFormAction.getActionDefinition(), myJumperCreateOpen.getActionDefinition());

        Button myJumperView = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::View")).findFirst().orElseThrow();
        assertTrue(myJumperView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(myJumperView, "View", "eye", "contained");
        assertEquals(myJumperOpenPageAction.getActionDefinition(), myJumperView.getActionDefinition());
        assertEquals(myJumperViewPageDefinition, myJumperOpenPageAction.getTargetPageDefinition());

        Button myJumperDelete = myJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumper::Delete")).findFirst().orElseThrow();
        assertTrue(myJumperDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(myJumperDelete, "Delete", "delete_forever", "contained");
        assertEquals(myJumperRowDeleteAction.getActionDefinition(), myJumperDelete.getActionDefinition());

        // - Table - myJumpers

        Table myJumpers = (Table) pageContainer.getTables().stream().filter(t -> ((Table) t).getName().equals("myJumpers")).findFirst().orElseThrow();
        assertEquals("theJumpersCollection", myJumpers.getDataElement().getName());

        assertEquals(Set.of(
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::Filter",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::Refresh",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::Clear",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::BulkRemove",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::OpenAddSelector",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpers::InlineViewTableButtonGroup::myJumpers::OpenCreate"
        ), myJumpers.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumpersFilter = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::Filter")).findFirst().orElseThrow();
        assertTrue(myJumpersFilter.getActionDefinition().getIsFilterAction());
        assertButtonVisuals(myJumpersFilter, "Filter", "filter", "text");
        assertEquals(myJumpersFilterAction.getActionDefinition(), myJumpersFilter.getActionDefinition());

        Button myJumpersRefresh = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumpersRefresh.getActionDefinition().getIsRefreshAction());
        assertButtonVisuals(myJumpersRefresh, "Refresh", "refresh", "text");
        assertEquals(myJumpersRefreshAction.getActionDefinition(), myJumpersRefresh.getActionDefinition());

        Button myJumpersOpenCreate = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::OpenCreate")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenCreate.getActionDefinition().getIsOpenCreateFormAction());
        assertButtonVisuals(myJumpersOpenCreate, "Create", "file-document-plus", "text");
        assertEquals(myJumpersOpenCreateAction.getActionDefinition(), myJumpersOpenCreate.getActionDefinition());

        Button myJumpersOpenAddSelector = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::OpenAddSelector")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenAddSelector.getActionDefinition().getIsOpenAddSelectorAction());
        assertButtonVisuals(myJumpersOpenAddSelector, "Add", "attachment-plus", "text");
        assertEquals(myJumpersOpenAddSelectorAction.getActionDefinition(), myJumpersOpenAddSelector.getActionDefinition());

        Button myJumpersClear = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::Clear")).findFirst().orElseThrow();
        assertTrue(myJumpersClear.getActionDefinition().getIsClearAction());
        assertButtonVisuals(myJumpersClear, "Clear", "link-off", "text");
        assertEquals(myJumpersClear.getActionDefinition(), myJumpersClear.getActionDefinition());

        Button myJumpersBulkRemove = myJumpers.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::BulkRemove")).findFirst().orElseThrow();
        assertTrue(myJumpersBulkRemove.getActionDefinition().getIsBulkRemoveAction());
        assertButtonVisuals(myJumpersBulkRemove, "Remove", "link-off", "text");
        assertEquals(myJumpersBulkRemove.getActionDefinition(), myJumpersBulkRemove.getActionDefinition());

        assertEquals(Set.of(
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpersInlineViewTableRowButtonGroup::myJumpers::RowDelete",
                "NavigationActor::RelatedRowDetailViewCRUD::RelatedView::View::PageContainer::RelatedView::g1::myJumpers::myJumpersInlineViewTableRowButtonGroup::myJumpers::View"
        ), myJumpers.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button myJumpersView = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::View")).findFirst().orElseThrow();
        assertTrue(myJumpersView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(myJumpersView, "View", "visibility", "contained");
        assertEquals(myJumpersOpenPageAction.getActionDefinition(), myJumpersView.getActionDefinition());
        assertEquals(jumperRowDetailViewPageDefinition, myJumpersOpenPageAction.getTargetPageDefinition());

        Button myJumpersRowDelete = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumpersRowDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(myJumpersRowDelete, "Delete", "delete_forever", "contained");
        assertEquals(myJumpersRowDeleteAction.getActionDefinition(), myJumpersRowDelete.getActionDefinition());
    }

    @Test
    void testRelatedFormCRUD() throws Exception {
        jslModel = JslParser.getModelFromStrings("RelatedFormCRUD", List.of(createModelString("RelatedFormCRUD")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        Application application = apps.get(0);
        List<PageDefinition> pages = application.getPages();

        PageDefinition pageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedFormCRUD::UserView::level::related::FormPage")).findFirst().orElseThrow();
        PageContainer pageContainer = pageDefinition.getContainer();
        List<Button> buttons = pageContainer.getActionButtonGroup().getButtons();
        List<Action> actions = pageDefinition.getActions();

        ClassType classType = (ClassType) application.getClassTypes().stream().filter(c -> ((ClassType) c).getName().equals("RelatedFormCRUD::RelatedTransfer")).findFirst().orElseThrow();

        assertEquals(Set.of(
                "NavigationActor::RelatedFormCRUD::UserView::level::related::FormPage::related::Create",
                "NavigationActor::RelatedFormCRUD::UserView::level::related::FormPage::related::Back"
        ), actions.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Action relatedBackActions = actions.stream().filter(a -> a.getName().equals("related::Back")).findFirst().orElseThrow();
        assertTrue(relatedBackActions.getIsBackAction());

        Action relatedCreateActions = actions.stream().filter(a -> a.getName().equals("related::Create")).findFirst().orElseThrow();
        assertTrue(relatedCreateActions.getIsCreateAction());

        assertEquals(PageContainerType.FORM, pageContainer.getType());
        assertEquals(classType, pageDefinition.getRelationType().getTarget());
        assertEquals(Set.of(
                "NavigationActor::RelatedFormCRUD::RelatedForm::Create::PageContainer::RelatedFormCRUD::RelatedForm::PageActions::RelatedFormCRUD::RelatedForm::Create",
                "NavigationActor::RelatedFormCRUD::RelatedForm::Create::PageContainer::RelatedFormCRUD::RelatedForm::PageActions::RelatedFormCRUD::RelatedForm::Back"
        ), buttons.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Button back = buttons.stream().filter(b -> b.getName().equals("RelatedFormCRUD::RelatedForm::Back")).findFirst().orElseThrow();
        assertTrue(back.getActionDefinition().getIsBackAction());
        assertButtonVisuals(back, "Back", "arrow-left", "text");
        assertEquals(relatedBackActions.getActionDefinition(), back.getActionDefinition());

        Button myJumpersView = buttons.stream().filter(b -> b.getName().equals("RelatedFormCRUD::RelatedForm::Create")).findFirst().orElseThrow();
        assertTrue(myJumpersView.getActionDefinition().getIsCreateAction());
        assertButtonVisuals(myJumpersView, "Create", "content-save", "contained");
        assertEquals(relatedCreateActions.getActionDefinition(), myJumpersView.getActionDefinition());
    }

    public static void assertButtonVisuals(Button button, String label, String icon, String style) {
        assertEquals(label, button.getLabel());
        if (button.getIcon() != null && button.getIcon().getIconName() != null && icon != null) {
            assertEquals(icon, button.getIcon().getIconName());
        } else if (button.getIcon() == null && icon == null) {
            // we are good
        } else {
            throw new IllegalArgumentException("Icon value of " + icon + " does not match with Button " + button.getName() + "'s icon!");
        }
        assertEquals(style, button.getButtonStyle());
    }
}
