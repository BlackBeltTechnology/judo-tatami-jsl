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

import static org.junit.jupiter.api.Assertions.*;

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

                field Related related;
                relation Related[] relatedCollection;
            }

            entity Related {
                field String first;
                field Integer second;
                field Jumper theJumper;
                relation Jumper[] theJumpersCollection;
            }

            entity Jumper {
                field String first;
                field String second;
            }

            view UserView(User u) {
                field String email <= u.email label:"Email";
                group level {
                    link RelatedView related <= u.related eager:true icon:"related" label:"Related" width:6 create:true update:true delete:true;
                    table RelatedRow[] relatedCollection <= u.relatedCollection icon:"relatedCollection" label:"Related Collection";
                }

                event update onUpdate;
                event delete onDelete;
            }

            row RelatedRow(Related r) {
                field String first <= r.first label:"First";
                field Integer second <= r.second label:"Second";
                link RelatedView detail <= r detail:true;
            }

            view RelatedView(Related r) {
                field String first <= r.first label:"First";
                field Integer second <= r.second label:"Second";
                group g1 {
                    link JumperView readOnlyJumper <= r.theJumper eager:false icon:"jumping" label:"Read only Jumper";
                    link JumperView myJumper <= r.theJumper eager:false icon:"jumping" label:"My Jumper" width:6 create:true update:true delete:true;
                    table JumperRow[] myJumpers <= r.theJumpersCollection eager:false icon:"jumping-all" label:"My Jumpers" width:6 create:true update:true delete:true;
                }

                event create onCreate(RelatedForm form);
                event update onUpdate;
                event delete onDelete;
            }

            view RelatedForm(Related r) {
                group one label:"Group 1" {
                    field String first <= r.first label:"First";
                }
            }

            view JumperView(Jumper j) {
                field String first <= j.first label:"First";

                event create onCreate(JumperForm form);
                event update onUpdate;
                event delete onDelete;
            }

            row JumperRow(Jumper j) {
                field String second <= j.second label:"Second";
                link JumperView jumperRowDetail <= j detail:true;

                event create onCreate(JumperForm form);
                event update onUpdate;
                event delete onDelete;
            }

            view JumperForm(Jumper j) {
                group one label:"Group 1" {
                    field String firstOnForm <= j.first label:"First on form";
                }
            }

            actor NavigationActor human {
                link UserView user <= User.any() label:"User" icon:"tools" update:true delete:true;
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

        assertEquals(8, relationTypes.size());
        assertEquals(8, classTypes.size());
        assertEquals(8, pageContainers.size());
        assertEquals(10, pages.size());
        assertEquals(3, links.size());
        assertEquals(2, tables.size());
        assertEquals(52, allActions.size());

        PageDefinition actorDashboardPage = pages.stream().filter(p -> p.getName().equals("SummaryCRUD::NavigationActor::DashboardPage")).findFirst().orElseThrow();

        assertEquals(Set.of(
                "NavigationActor::Application::SummaryCRUD::JumperRow::ClassType::jumperRowDetail",
                "NavigationActor::Application::SummaryCRUD::NavigationActor::ClassType::user",
                "NavigationActor::Application::SummaryCRUD::RelatedView::ClassType::myJumpers",
                "NavigationActor::Application::SummaryCRUD::UserView::ClassType::related",
                "NavigationActor::Application::SummaryCRUD::RelatedView::ClassType::myJumper",
                "NavigationActor::Application::SummaryCRUD::RelatedView::ClassType::readOnlyJumper",
                "NavigationActor::Application::SummaryCRUD::RelatedRow::ClassType::detail",
                "NavigationActor::Application::SummaryCRUD::UserView::ClassType::relatedCollection"
        ), relationTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::SummaryCRUD::NavigationActor::ClassType",
                "NavigationActor::Application::SummaryCRUD::JumperRow::ClassType",
                "NavigationActor::Application::SummaryCRUD::RelatedForm::ClassType",
                "NavigationActor::Application::SummaryCRUD::RelatedRow::ClassType",
                "NavigationActor::Application::SummaryCRUD::UserView::ClassType",
                "NavigationActor::Application::SummaryCRUD::JumperView::ClassType",
                "NavigationActor::Application::SummaryCRUD::RelatedView::ClassType",
                "NavigationActor::Application::SummaryCRUD::JumperForm::ClassType"
        ), classTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::SummaryCRUD::NavigationActor::Dashboard",
                "NavigationActor::Application::SummaryCRUD::UserView::PageContainer",
                "NavigationActor::Application::SummaryCRUD::RelatedView::PageContainer",
                "NavigationActor::Application::SummaryCRUD::JumperView::PageContainer",
                "NavigationActor::Application::SummaryCRUD::RelatedForm::PageContainer",
                "NavigationActor::Application::SummaryCRUD::RelatedRow::PageContainer",
                "NavigationActor::Application::SummaryCRUD::JumperRow::PageContainer",
                "NavigationActor::Application::SummaryCRUD::JumperForm::PageContainer"
        ), pageContainers.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::SummaryCRUD::NavigationActor::DashboardPage",
                "NavigationActor::Application::SummaryCRUD::NavigationActor::user::View::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::UserView::related::Create::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::UserView::related::View::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::RelatedView::readOnlyJumper::View::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::RelatedView::myJumper::View::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::RelatedView::myJumper::Create::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::RelatedView::myJumpers::Create::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::JumperRow::jumperRowDetail::View::PageDefinition",
                "NavigationActor::Application::SummaryCRUD::RelatedRow::detail::View::PageDefinition"
        ), pages.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::SummaryCRUD::RelatedView::PageContainer::RelatedView::g1::myJumper",
                "NavigationActor::Application::SummaryCRUD::RelatedView::PageContainer::RelatedView::g1::readOnlyJumper",
                "NavigationActor::Application::SummaryCRUD::UserView::PageContainer::UserView::level::related"
        ), links.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::SummaryCRUD::RelatedView::PageContainer::RelatedView::g1::myJumpers",
                "NavigationActor::Application::SummaryCRUD::UserView::PageContainer::UserView::level::relatedCollection"
        ), tables.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));
    }

    @Test
    void testAccessViewCRUD() throws Exception {
        jslModel = JslParser.getModelFromStrings("AccessViewCRUD", List.of(createModelString("AccessViewCRUD")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        Application application = apps.get(0);

        List<PageDefinition> pages = application.getPages();

        PageDefinition pageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::NavigationActor::user::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition userViewRelatedViewPageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::UserView::related::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition userViewRelatedCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::UserView::related::Create::PageDefinition")).findFirst().orElseThrow();
        PageDefinition relatedRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("AccessViewCRUD::RelatedRow::detail::View::PageDefinition")).findFirst().orElseThrow();

        PageContainer pageContainer = pageDefinition.getContainer();

        assertEquals(10, pageDefinition.getActions().size());
        assertEquals(1, pageContainer.getLinks().size());
        assertEquals(1, pageContainer.getTables().size());

        assertEquals(Set.of(
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::related::OpenPage",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::related::OpenForm",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::related::RowDelete",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::relatedCollection::OpenPage",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::relatedCollection::Filter",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::relatedCollection::Refresh",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::user::Back",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::user::Refresh",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::user::Update",
                "NavigationActor::Application::AccessViewCRUD::NavigationActor::user::View::PageDefinition::user::Delete"
        ), pageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

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
        assertTrue(relatedCollectionOpenPageAction.getIsRowOpenPageAction());
        assertEquals(relatedRowDetailViewPageDefinition, relatedCollectionOpenPageAction.getTargetPageDefinition());

        Action relatedCollectionFilterAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::Filter")).findFirst().orElseThrow();
        assertTrue(relatedCollectionFilterAction.getIsFilterAction());

        Action relatedCollectionRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::Refresh")).findFirst().orElseThrow();
        assertTrue(relatedCollectionRefreshAction.getIsRefreshAction());

        // - Link - related

        Link related = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("related")).findFirst().orElseThrow();
        assertEquals("related", related.getDataElement().getName());
        assertEquals(Set.of(
                "related::Create::Open",
                "related::View",
                "related::Delete"
        ), related.getActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

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
                "relatedCollection::Filter",
                "relatedCollection::Refresh"
        ), relatedCollection.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button relatedCollectionFilter = relatedCollection.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::Filter")).findFirst().orElseThrow();
        assertTrue(relatedCollectionFilter.getActionDefinition().getIsFilterAction());
        assertButtonVisuals(relatedCollectionFilter, "Filter", "filter", "text");
        assertEquals(relatedCollectionFilterAction.getActionDefinition(), relatedCollectionFilter.getActionDefinition());

        Button relatedCollectionRefresh = relatedCollection.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::Refresh")).findFirst().orElseThrow();
        assertTrue(relatedCollectionRefresh.getActionDefinition().getIsRefreshAction());
        assertButtonVisuals(relatedCollectionRefresh, "Refresh", "refresh", "text");
        assertEquals(relatedCollectionRefreshAction.getActionDefinition(), relatedCollectionRefresh.getActionDefinition());

        assertEquals(Set.of(
                "relatedCollection::View"
        ), relatedCollection.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button relatedCollectionView = relatedCollection.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::View")).findFirst().orElseThrow();
        assertTrue(relatedCollectionView.getActionDefinition().getIsRowOpenPageAction());
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

        PageDefinition pageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::UserView::related::View::PageDefinition")).findFirst().orElseThrow();
        PageContainer pageContainer = pageDefinition.getContainer();

        PageDefinition readOnlyJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::readOnlyJumper::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition myJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::myJumper::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition jumperRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::JumperRow::jumperRowDetail::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition myJumpersCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("SingleRelationViewCRUD::RelatedView::myJumpers::Create::PageDefinition")).findFirst().orElseThrow();

        assertEquals(15, pageDefinition.getActions().size());
        assertEquals(2, pageContainer.getLinks().size());
        assertEquals(1, pageContainer.getTables().size());

        assertEquals(Set.of(
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::related::Back",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::related::Refresh",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::related::Update",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::related::Delete",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::readOnlyJumper::OpenPage",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::readOnlyJumper::Refresh",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumper::OpenPage",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumper::Refresh",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumper::OpenForm",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumper::RowDelete",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumpers::OpenPage",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumpers::OpenCreate",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumpers::Filter",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumpers::Refresh",
                "NavigationActor::Application::SingleRelationViewCRUD::UserView::related::View::PageDefinition::myJumpers::RowDelete"
        ), pageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

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

        Action myJumperRefreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::Refresh")).findFirst().orElseThrow();
        assertTrue(myJumperRefreshAction.getIsRefreshAction());

        Action myJumperRowDeleteAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumper::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumperRowDeleteAction.getIsRowDeleteAction());

        Action myJumpersOpenPageAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("myJumpers::OpenPage")).findFirst().orElseThrow();
        assertTrue(myJumpersOpenPageAction.getIsRowOpenPageAction());
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

        // - link readOnlyJumper

        Link readOnlyJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("readOnlyJumper")).findFirst().orElseThrow();
        assertEquals("readOnlyJumper", readOnlyJumper.getDataElement().getName());
        assertEquals(Set.of(
                "readOnlyJumper::View"
        ), readOnlyJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button readOnlyJumperView = readOnlyJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("readOnlyJumper::View")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(readOnlyJumperView, "View", "eye", "contained");
        assertEquals(readOnlyJumperOpenPageAction.getActionDefinition(), readOnlyJumperView.getActionDefinition());
        assertEquals(readOnlyJumperViewPageDefinition, readOnlyJumperOpenPageAction.getTargetPageDefinition());

        // - Link - myJumper

        Link myJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("myJumper")).findFirst().orElseThrow();
        assertEquals("myJumper", myJumper.getDataElement().getName());
        assertEquals(Set.of(
                "myJumper::Create::Open",
                "myJumper::View",
                "myJumper::Delete"
        ), myJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

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
        assertEquals("myJumpers", myJumpers.getDataElement().getName());

        assertEquals(Set.of(
                "myJumpers::Filter",
                "myJumpers::Refresh",
                "myJumpers::OpenCreate"
        ), myJumpers.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

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

        assertEquals(Set.of(
                "myJumpers::View",
                "myJumpers::RowDelete"
        ), myJumpers.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button myJumpersView = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::View")).findFirst().orElseThrow();
        assertTrue(myJumpersView.getActionDefinition().getIsRowOpenPageAction());
        assertButtonVisuals(myJumpersView, "View", "visibility", "contained");
        assertEquals(myJumpersOpenPageAction.getActionDefinition(), myJumpersView.getActionDefinition());
        assertEquals(jumperRowDetailViewPageDefinition, myJumpersOpenPageAction.getTargetPageDefinition());

        Button myJumpersRowDelete = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumpersRowDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(myJumpersRowDelete, "Delete", "delete_forever", "contained");
        assertEquals(myJumpersRowDeleteAction.getActionDefinition(), myJumpersRowDelete.getActionDefinition());

    }

    @Test
    void testRelatedRowDetailViewCRUD() throws Exception {
        // relatedCollection row's RelatedView page
        jslModel = JslParser.getModelFromStrings("RelatedRowDetailViewCRUD", List.of(createModelString("RelatedRowDetailViewCRUD")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        Application application = apps.get(0);
        List<PageDefinition> pages = application.getPages();

        PageDefinition pageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition")).findFirst().orElseThrow();
        PageContainer pageContainer = pageDefinition.getContainer();

        PageDefinition readOnlyJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::readOnlyJumper::View::PageDefinition")).findFirst().orElseThrow();

        PageDefinition myJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::myJumper::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition myJumperCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::myJumper::Create::PageDefinition")).findFirst().orElseThrow();
        PageDefinition myJumpersCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::RelatedView::myJumpers::Create::PageDefinition")).findFirst().orElseThrow();

        PageDefinition jumperRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("RelatedRowDetailViewCRUD::JumperRow::jumperRowDetail::View::PageDefinition")).findFirst().orElseThrow();

        assertEquals(13, pageDefinition.getActions().size());
        assertEquals(2, pageContainer.getLinks().size());
        assertEquals(1, pageContainer.getTables().size());

        assertEquals(Set.of(
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::detail::Back",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::detail::Refresh",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::readOnlyJumper::OpenPage",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::readOnlyJumper::Refresh",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumper::OpenPage",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumper::Refresh",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumper::OpenForm",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumper::RowDelete",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumpers::OpenPage",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumpers::OpenCreate",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumpers::Filter",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumpers::Refresh",
                "NavigationActor::Application::RelatedRowDetailViewCRUD::RelatedRow::detail::View::PageDefinition::myJumpers::RowDelete"
        ), pageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Action BackAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("detail::Back")).findFirst().orElseThrow();
        assertTrue(BackAction.getIsBackAction());

        Action refreshAction = pageDefinition.getActions().stream().filter(a -> a.getName().equals("detail::Refresh")).findFirst().orElseThrow();
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
        assertTrue(myJumpersOpenPageAction.getIsRowOpenPageAction());
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

        // - link readOnlyJumper

        Link readOnlyJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("readOnlyJumper")).findFirst().orElseThrow();
        assertEquals("readOnlyJumper", readOnlyJumper.getDataElement().getName());
        assertEquals(Set.of(
                "readOnlyJumper::View"
        ), readOnlyJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button readOnlyJumperView = readOnlyJumper.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("readOnlyJumper::View")).findFirst().orElseThrow();
        assertTrue(readOnlyJumperView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(readOnlyJumperView, "View", "eye", "contained");
        assertEquals(readOnlyJumperOpenPageAction.getActionDefinition(), readOnlyJumperView.getActionDefinition());
        assertEquals(readOnlyJumperViewPageDefinition, readOnlyJumperOpenPageAction.getTargetPageDefinition());

        // - Link - myJumper

        Link myJumper = (Link) pageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("myJumper")).findFirst().orElseThrow();
        assertEquals("myJumper", myJumper.getDataElement().getName());
        assertEquals(Set.of(
                "myJumper::Create::Open",
                "myJumper::View",
                "myJumper::Delete"
        ), myJumper.getActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

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
        assertEquals("myJumpers", myJumpers.getDataElement().getName());

        assertEquals(Set.of(
                "myJumpers::Filter",
                "myJumpers::Refresh",
                "myJumpers::OpenCreate"
        ), myJumpers.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

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

        assertEquals(Set.of(
                "myJumpers::View",
                "myJumpers::RowDelete"
        ), myJumpers.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button myJumpersView = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::View")).findFirst().orElseThrow();
        assertTrue(myJumpersView.getActionDefinition().getIsRowOpenPageAction());
        assertButtonVisuals(myJumpersView, "View", "visibility", "contained");
        assertEquals(myJumpersOpenPageAction.getActionDefinition(), myJumpersView.getActionDefinition());
        assertEquals(jumperRowDetailViewPageDefinition, myJumpersOpenPageAction.getTargetPageDefinition());

        Button myJumpersRowDelete = myJumpers.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("myJumpers::RowDelete")).findFirst().orElseThrow();
        assertTrue(myJumpersRowDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(myJumpersRowDelete, "Delete", "delete_forever", "contained");
        assertEquals(myJumpersRowDeleteAction.getActionDefinition(), myJumpersRowDelete.getActionDefinition());
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
