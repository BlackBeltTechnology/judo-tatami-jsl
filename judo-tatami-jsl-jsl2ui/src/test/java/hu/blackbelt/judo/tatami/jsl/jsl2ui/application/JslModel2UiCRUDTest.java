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

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testCRUD() throws Exception {
        jslModel = JslParser.getModelFromStrings("CRUDTestModel", List.of("""
            model CRUDTestModel;

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
                    link RelatedView related <= u.related eager:true icon:"related" label:"Related" width:6 create:true update: true delete:true;
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
                field String first <= r.first label: "First";
                field Integer second <= r.second label: "Second";
                link JumperView readOnlyJumper <= r.theJumper eager:false icon:"jumping" label:"Read only Jumper";
                link JumperView myJumper <= r.theJumper eager:false icon:"jumping" label:"My Jumper" width:6 create:true update: true delete:true;
                table JumperRow[] myJumpers <= r.theJumpersCollection eager:false icon:"jumping-all" label:"My Jumpers" width:6 create:true update: true delete:true;

                event create onCreate(RelatedForm form);
                event update onUpdate;
                event delete onDelete;
            }

            view RelatedForm(Related r) {
                group one label:"Group 1" {
                    field String first <= r.first label: "First";
                }
            }

            view JumperView(Jumper j) {
                field String first <= j.first label: "First";

                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }

            row JumperRow(Jumper j) {
                field String second <= j.second label: "Second";
                link JumperView jumperRowDetail <= j detail:true;

                event create onCreate(JumperForm form);
                event update onUpdate;
                event delete onDelete;
            }

            view JumperForm(Jumper j) {
                group one label:"Group 1" {
                    field String firstOnForm <= j.first label: "First on form";
                }
            }

            actor NavigationActor human {
                link UserView user <= User.any() label:"User" icon:"tools" update: true delete:true;
            }
        """));

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
        assertEquals(8, pages.size());
        assertEquals(3, links.size());
        assertEquals(2, tables.size());
        assertEquals(26, allActions.size());


        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::JumperRow::ClassType::jumperRowDetail",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::ClassType::user",
                "NavigationActor::Application::CRUDTestModel::RelatedView::ClassType::myJumpers",
                "NavigationActor::Application::CRUDTestModel::UserView::ClassType::related",
                "NavigationActor::Application::CRUDTestModel::RelatedView::ClassType::myJumper",
                "NavigationActor::Application::CRUDTestModel::RelatedView::ClassType::readOnlyJumper",
                "NavigationActor::Application::CRUDTestModel::RelatedRow::ClassType::detail",
                "NavigationActor::Application::CRUDTestModel::UserView::ClassType::relatedCollection"
        ), relationTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::NavigationActor::ClassType",
                "NavigationActor::Application::CRUDTestModel::JumperRow::ClassType",
                "NavigationActor::Application::CRUDTestModel::RelatedForm::ClassType",
                "NavigationActor::Application::CRUDTestModel::RelatedRow::ClassType",
                "NavigationActor::Application::CRUDTestModel::UserView::ClassType",
                "NavigationActor::Application::CRUDTestModel::JumperView::ClassType",
                "NavigationActor::Application::CRUDTestModel::RelatedView::ClassType",
                "NavigationActor::Application::CRUDTestModel::JumperForm::ClassType"
        ), classTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::NavigationActor::Dashboard",
                "NavigationActor::Application::CRUDTestModel::UserView::PageContainer",
                "NavigationActor::Application::CRUDTestModel::RelatedView::PageContainer",
                "NavigationActor::Application::CRUDTestModel::JumperView::PageContainer",
                "NavigationActor::Application::CRUDTestModel::RelatedForm::PageContainer",
                "NavigationActor::Application::CRUDTestModel::RelatedRow::PageContainer",
                "NavigationActor::Application::CRUDTestModel::JumperRow::PageContainer",
                "NavigationActor::Application::CRUDTestModel::JumperForm::PageContainer"
        ), pageContainers.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::NavigationActor::DashboardPage",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition",
                "NavigationActor::Application::CRUDTestModel::UserView::related::View::PageDefinition",
                "NavigationActor::Application::CRUDTestModel::RelatedView::myJumper::View::PageDefinition",
                "NavigationActor::Application::CRUDTestModel::RelatedView::readOnlyJumper::View::PageDefinition",
                "NavigationActor::Application::CRUDTestModel::JumperRow::jumperRowDetail::View::PageDefinition",
                "NavigationActor::Application::CRUDTestModel::UserView::related::Create::PageDefinition",
                "NavigationActor::Application::CRUDTestModel::RelatedRow::detail::View::PageDefinition"
        ), pages.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        PageDefinition actorDashboardPage = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::NavigationActor::DashboardPage")).findFirst().orElseThrow();
        PageDefinition actorUserViewPageDefinition = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::NavigationActor::user::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition userViewRelatedViewPageDefinition = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::UserView::related::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition relatedViewReadOnlyJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::RelatedView::readOnlyJumper::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition relatedViewMyJumperViewPageDefinition = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::RelatedView::myJumper::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition jumperRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::JumperRow::jumperRowDetail::View::PageDefinition")).findFirst().orElseThrow();
        PageDefinition userViewRelatedCreatePageDefinition = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::UserView::related::Create::PageDefinition")).findFirst().orElseThrow();
        PageDefinition relatedRowDetailViewPageDefinition = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::RelatedRow::detail::View::PageDefinition")).findFirst().orElseThrow();

        PageContainer actorUserViewPageContainer = actorUserViewPageDefinition.getContainer();
        PageContainer userViewRelatedViewPageContainer = userViewRelatedViewPageDefinition.getContainer();
        PageContainer relatedViewReadOnlyJumperViewPageContainer = relatedViewReadOnlyJumperViewPageDefinition.getContainer();
        PageContainer relatedViewMyJumperViewPageContainer = relatedViewMyJumperViewPageDefinition.getContainer();
        PageContainer jumperRowDetailViewPageContainer = jumperRowDetailViewPageDefinition.getContainer();
        PageContainer userViewRelatedCreatePageContainer = userViewRelatedCreatePageDefinition.getContainer();
        PageContainer relatedRowDetailViewPageContainer = relatedRowDetailViewPageDefinition.getContainer();

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::RelatedView::PageContainer::RelatedView::myJumper",
                "NavigationActor::Application::CRUDTestModel::RelatedView::PageContainer::RelatedView::readOnlyJumper",
                "NavigationActor::Application::CRUDTestModel::UserView::PageContainer::UserView::level::related"
        ), links.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::RelatedView::PageContainer::RelatedView::myJumpers",
                "NavigationActor::Application::CRUDTestModel::UserView::PageContainer::UserView::level::relatedCollection"
        ), tables.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(9, actorUserViewPageDefinition.getActions().size());
        assertEquals(1, actorUserViewPageContainer.getLinks().size());
        assertEquals(1, actorUserViewPageContainer.getTables().size());

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::related::OpenPage",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::related::OpenForm",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::related::RowDelete",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::relatedCollection::OpenPage",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::relatedCollection::Filter",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::relatedCollection::Refresh",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::user::Back",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::user::Update",
                "NavigationActor::Application::CRUDTestModel::NavigationActor::user::View::PageDefinition::user::Delete"
        ), actorUserViewPageDefinition.getActions().stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        // User View page

        Action userViewLinkPageDefinitionBackAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("user::Back")).findFirst().orElseThrow();
        assertTrue(userViewLinkPageDefinitionBackAction.getIsBackAction());

        Action userViewLinkPageDefinitionUpdateAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("user::Update")).findFirst().orElseThrow();
        assertTrue(userViewLinkPageDefinitionUpdateAction.getIsUpdateAction());

        Action userViewLinkPageDefinitionDeleteAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("user::Delete")).findFirst().orElseThrow();
        assertTrue(userViewLinkPageDefinitionDeleteAction.getIsDeleteAction());

        Action relatedViewLinkDeclarationOpenPageAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("related::OpenPage")).findFirst().orElseThrow();
        assertTrue(relatedViewLinkDeclarationOpenPageAction.getIsOpenPageAction());
        assertEquals(userViewRelatedViewPageDefinition, relatedViewLinkDeclarationOpenPageAction.getTargetPageDefinition());

        Action relatedViewLinkDeclarationOpenFormAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("related::OpenForm")).findFirst().orElseThrow();
        assertTrue(relatedViewLinkDeclarationOpenFormAction.getIsOpenFormAction());
        assertEquals(userViewRelatedCreatePageDefinition, relatedViewLinkDeclarationOpenFormAction.getTargetPageDefinition());

        Action relatedViewLinkDeclarationRowDeleteAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("related::RowDelete")).findFirst().orElseThrow();
        assertTrue(relatedViewLinkDeclarationRowDeleteAction.getIsRowDeleteAction());

        Action relatedCollectionViewTableDeclarationOpenPageAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::OpenPage")).findFirst().orElseThrow();
        assertTrue(relatedCollectionViewTableDeclarationOpenPageAction.getIsRowOpenPageAction());
        assertEquals(relatedRowDetailViewPageDefinition, relatedCollectionViewTableDeclarationOpenPageAction.getTargetPageDefinition());

        Action relatedCollectionViewTableDeclarationFilterAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::Filter")).findFirst().orElseThrow();
        assertTrue(relatedCollectionViewTableDeclarationFilterAction.getIsFilterAction());

        Action relatedCollectionViewTableDeclarationRefreshAction = actorUserViewPageDefinition.getActions().stream().filter(a -> a.getName().equals("relatedCollection::Refresh")).findFirst().orElseThrow();
        assertTrue(relatedCollectionViewTableDeclarationRefreshAction.getIsRefreshAction());

        // - Link - related

        Link related = (Link) actorUserViewPageContainer.getLinks().stream().filter(l -> ((Link) l).getName().equals("related")).findFirst().orElseThrow();
        assertEquals("related", related.getDataElement().getName());
        assertEquals(Set.of(
                "related::Create::Open",
                "related::View",
                "related::Delete"
        ), related.getActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button relatedCreateOpen = related.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("related::Create::Open")).findFirst().orElseThrow();
        assertTrue(relatedCreateOpen.getActionDefinition().getIsOpenCreateFormAction());
        assertButtonVisuals(relatedCreateOpen, "Create", "note-add", "contained");
        assertEquals(relatedViewLinkDeclarationOpenFormAction.getActionDefinition(), relatedCreateOpen.getActionDefinition());

        Button relatedView = related.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("related::View")).findFirst().orElseThrow();
        assertTrue(relatedView.getActionDefinition().getIsOpenPageAction());
        assertButtonVisuals(relatedView, "View", "eye", "contained");
        assertEquals(relatedViewLinkDeclarationOpenPageAction.getActionDefinition(), relatedView.getActionDefinition());

        Button relatedDelete = related.getActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("related::Delete")).findFirst().orElseThrow();
        assertTrue(relatedDelete.getActionDefinition().getIsRowDeleteAction());
        assertButtonVisuals(relatedDelete, "Delete", "delete_forever", "contained");
        assertEquals(relatedViewLinkDeclarationRowDeleteAction.getActionDefinition(), relatedDelete.getActionDefinition());

        // - Table - relatedCollection

        Table relatedCollection = (Table) actorUserViewPageContainer.getTables().stream().filter(t -> ((Table) t).getName().equals("relatedCollection")).findFirst().orElseThrow();
        assertEquals("relatedCollection", relatedCollection.getDataElement().getName());

        assertEquals(Set.of(
                "relatedCollection::Filter",
                "relatedCollection::Refresh"
        ), relatedCollection.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getName).collect(Collectors.toSet()));

        Button relatedCollectionFilter = relatedCollection.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::Filter")).findFirst().orElseThrow();
        assertTrue(relatedCollectionFilter.getActionDefinition().getIsFilterAction());
        assertButtonVisuals(relatedCollectionFilter, "Filter", "filter", "text");
        assertEquals(relatedCollectionViewTableDeclarationFilterAction.getActionDefinition(), relatedCollectionFilter.getActionDefinition());

        Button relatedCollectionRefresh = relatedCollection.getTableActionButtonGroup().getButtons().stream().filter(b -> b.getName().equals("relatedCollection::Refresh")).findFirst().orElseThrow();
        assertTrue(relatedCollectionRefresh.getActionDefinition().getIsRefreshAction());
        assertButtonVisuals(relatedCollectionRefresh, "Refresh", "refresh", "text");
        assertEquals(relatedCollectionViewTableDeclarationRefreshAction.getActionDefinition(), relatedCollectionRefresh.getActionDefinition());
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
