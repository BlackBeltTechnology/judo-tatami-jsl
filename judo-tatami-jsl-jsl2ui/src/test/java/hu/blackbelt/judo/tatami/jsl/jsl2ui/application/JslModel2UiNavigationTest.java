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
public class JslModel2UiNavigationTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/navigation";

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
    void testNavigation() throws Exception {
        jslModel = JslParser.getModelFromStrings("NavigationTestModel", List.of("""
            model NavigationTestModel;

            import judo::types;

            entity User {
                identifier String email required;
                field Integer numeric;

                field Related related;
                relation Related[] relatedCollection;
            }

            transfer UserTransfer(User u) {
                relation RelatedTransfer related <= u.related eager;
                relation RelatedTransfer[] relatedCollection <= u.relatedCollection update create;
            }

            entity Related {
                field String first;
                field Integer second;
                field Jumper theJumper;
                relation Jumper[] theJumpersCollection;
            }

            transfer RelatedTransfer(Related r) {
                field String first <= r.first;
                field Integer second <= r.second;
                relation JumperTransfer theJumper <= r.theJumper;
                relation JumperTransfer[] theJumpersCollection <= r.theJumpersCollection update create;

                event create onCreate;
                event update onUpdate;
            }

            entity Jumper {
                field String first;
            }

            transfer JumperTransfer(Jumper j) {
                field String first <= j.first;

                event create onCreate;
                event update onUpdate;
            }

            view UserView(UserTransfer u) {
                group level1 {
                    link RelatedView related <= u.related icon:"related" label:"Related" width:6;
                    table RelatedTable relatedCollection <= u.relatedCollection icon:"relatedCollection" label:"Related Collection" view:RelatedView;
                }
            }

            table RelatedTable(RelatedTransfer r) {
                column String first <= r.first label:"First";
                column Integer second <= r.second label:"Second";
            }

            view RelatedView(RelatedTransfer r) {
                widget String first <= r.first label: "First";
                widget Integer second <= r.second label: "Second";
                link JumperView myJumper <= r.theJumper icon:"jumping" label:"My Jumper" width:6;
                table JumperTable myJumpers <= r.theJumpersCollection icon:"jumping-all" label:"My Jumpers" width:6 view:JumperView;
            }

            view JumperView(JumperTransfer j) {
                widget String first <= j.first label: "First";
            }

            table JumperTable(JumperTransfer j) {
                column String first <= j.first label: "First";
            }

            actor NavigationActor {
                access UserTransfer user <= User.any();
            }

            menu NavigationApp(NavigationActor a){
                link UserView user <= a.user label:"User" icon:"tools";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        List<ClassType> classTypes = application.getClassTypes();
        List<RelationType> relationTypes = application.getRelationTypes();
        List<Link> links = application.getLinks();
        List<Table> tables = application.getTables();
        List<PageDefinition> pages = application.getPages();

        assertEquals(Set.of(
                "NavigationActor::NavigationTestModel::RelatedTransfer",
                "NavigationActor::NavigationTestModel::JumperTransfer",
                "NavigationActor::NavigationTestModel::UserTransfer",
                "NavigationActor::NavigationTestModel::NavigationActor"
        ), classTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(2, links.size());
        assertEquals(2, tables.size());
        assertEquals(6, pages.size());
        assertEquals(7, relationTypes.size());

        ClassType relatedTransferClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationActor::NavigationTestModel::RelatedTransfer")).findFirst().orElseThrow();
        ClassType jumperTransferClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationActor::NavigationTestModel::JumperTransfer")).findFirst().orElseThrow();

        RelationType detailRelation = (RelationType) application.getRelationTypes().stream().filter(r -> ((RelationType) r).getName().equals("detail")).findFirst().orElseThrow();
        RelationType jumperRowDetailRelation = (RelationType) application.getRelationTypes().stream().filter(r -> ((RelationType) r).getName().equals("jumperRowDetail")).findFirst().orElseThrow();

        // Relations

        assertEquals(Set.of(
                "NavigationActor::Application::NavigationTestModel::JumperRow::ClassType::jumperRowDetail",
                "NavigationActor::Application::NavigationTestModel::NavigationActor::ClassType::user",
                "NavigationActor::Application::NavigationTestModel::RelatedRow::ClassType::detail",
                "NavigationActor::Application::NavigationTestModel::RelatedView::ClassType::myJumper",
                "NavigationActor::Application::NavigationTestModel::RelatedView::ClassType::myJumpers",
                "NavigationActor::Application::NavigationTestModel::UserView::ClassType::related",
                "NavigationActor::Application::NavigationTestModel::UserView::ClassType::relatedCollection"
        ), relationTypes.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        // Tables

        // - relatedCollection

        Table relatedCollectionTable =  tables.stream().filter(t -> t.getName().equals("relatedCollection")).findFirst().orElseThrow();

        List<Button> rowButtons =  relatedCollectionTable.getRowActionButtonGroup().getButtons();

        assertEquals(1, rowButtons.size());

        Button rowOpenButton = rowButtons.stream().filter(b -> b.getActionDefinition().getIsRowOpenPageAction()).findFirst().orElseThrow();
        RowOpenPageActionDefinition rowOpenPageActionDefinition = (RowOpenPageActionDefinition) rowOpenButton.getActionDefinition();

        assertEquals("contained", rowOpenButton.getButtonStyle());
        assertEquals("View", rowOpenButton.getLabel());
        assertEquals("visibility", rowOpenButton.getIcon().getIconName());

        assertEquals(relatedTransferClassType, rowOpenPageActionDefinition.getTargetType());
        assertEquals(detailRelation, rowOpenPageActionDefinition.getLinkRelation());

        // - myJumpers

        Table myJumpersTable =  tables.stream().filter(t -> t.getName().equals("myJumpers")).findFirst().orElseThrow();

        List<Button> myJumpersRowButtons =  myJumpersTable.getRowActionButtonGroup().getButtons();

        assertEquals(1, myJumpersRowButtons.size());

        Button myJumpersRowOpenButton = myJumpersRowButtons.stream().filter(b -> b.getActionDefinition().getIsRowOpenPageAction()).findFirst().orElseThrow();
        RowOpenPageActionDefinition myJumpersRowOpenPageActionDefinition = (RowOpenPageActionDefinition) myJumpersRowOpenButton.getActionDefinition();

        assertEquals("contained", myJumpersRowOpenButton.getButtonStyle());
        assertEquals("View", myJumpersRowOpenButton.getLabel());
        assertEquals("visibility", myJumpersRowOpenButton.getIcon().getIconName());

        assertEquals(jumperTransferClassType, myJumpersRowOpenPageActionDefinition.getTargetType());
        assertEquals(jumperRowDetailRelation, myJumpersRowOpenPageActionDefinition.getLinkRelation());

        // Links

        // - related

        Link relatedLink = links.stream().filter(l -> l.getName().equals("related")).findFirst().orElseThrow();

        List<Button> linkButtons =  relatedLink.getActionButtonGroup().getButtons();

        assertEquals(1, linkButtons.size());

        Button openButton = linkButtons.stream().filter(b -> b.getActionDefinition().getIsOpenPageAction()).findFirst().orElseThrow();
        OpenPageActionDefinition openPageActionDefinition = (OpenPageActionDefinition) openButton.getActionDefinition();

        assertEquals("contained", openButton.getButtonStyle());
        assertEquals("View", openButton.getLabel());
        assertEquals("eye", openButton.getIcon().getIconName());

        assertEquals(relatedTransferClassType, openPageActionDefinition.getTargetType());

        // - myJumper

        Link myJumperLink = links.stream().filter(l -> l.getName().equals("myJumper")).findFirst().orElseThrow();

        List<Button> myJumperLinkButtons =  myJumperLink.getActionButtonGroup().getButtons();

        assertEquals(1, myJumperLinkButtons.size());

        Button myJumperOpenButton = myJumperLinkButtons.stream().filter(b -> b.getActionDefinition().getIsOpenPageAction()).findFirst().orElseThrow();
        OpenPageActionDefinition myJumperOpenPageActionDefinition = (OpenPageActionDefinition) myJumperOpenButton.getActionDefinition();

        assertEquals("contained", myJumperOpenButton.getButtonStyle());
        assertEquals("View", myJumperOpenButton.getLabel());
        assertEquals("eye", myJumperOpenButton.getIcon().getIconName());

        assertEquals(jumperTransferClassType, myJumperOpenPageActionDefinition.getTargetType());

        // Actions

        // - User access

        PageDefinition userAccessPage = pages.stream().filter(p -> p.getName().equals("NavigationTestModel::NavigationActor::user::View::PageDefinition")).findFirst().orElseThrow();
        List<Action> userAccessPageActions = userAccessPage.getActions();

        assertEquals(6, userAccessPageActions.size());

        assertEquals(Set.of(
                "NavigationActor::Application::NavigationTestModel::NavigationActor::user::View::PageDefinition::user::Back",
                "NavigationActor::Application::NavigationTestModel::NavigationActor::user::View::PageDefinition::user::Refresh",
                "NavigationActor::Application::NavigationTestModel::NavigationActor::user::View::PageDefinition::related::OpenPage",
                "NavigationActor::Application::NavigationTestModel::NavigationActor::user::View::PageDefinition::relatedCollection::OpenPage",
                "NavigationActor::Application::NavigationTestModel::NavigationActor::user::View::PageDefinition::relatedCollection::Filter",
                "NavigationActor::Application::NavigationTestModel::NavigationActor::user::View::PageDefinition::relatedCollection::Refresh"
        ), userAccessPageActions.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Action backAction = userAccessPageActions.stream().filter(a -> a.getName().equals("user::Back")).findFirst().orElseThrow();
        assertTrue(backAction.getIsBackAction());

        Action relatedOpenPageAction = userAccessPageActions.stream().filter(a -> a.getName().equals("related::OpenPage")).findFirst().orElseThrow();
        assertTrue(relatedOpenPageAction.getIsOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::UserView::related::View::PageDefinition")).findFirst().orElse(null), relatedOpenPageAction.getTargetPageDefinition());

        Action relatedCollectionOpenPageAction = userAccessPageActions.stream().filter(a -> a.getName().equals("relatedCollection::OpenPage")).findFirst().orElseThrow();
        assertTrue(relatedCollectionOpenPageAction.getIsRowOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::RelatedRow::detail::View::PageDefinition")).findFirst().orElse(null), relatedCollectionOpenPageAction.getTargetPageDefinition());

        // - RelatedView relation view

        PageDefinition relatedViewPage = pages.stream().filter(p -> p.getName().equals("NavigationTestModel::RelatedRow::detail::View::PageDefinition")).findFirst().orElseThrow();
        List<Action> relatedViewPageActions = relatedViewPage.getActions();

        assertEquals(7, relatedViewPageActions.size());

        assertEquals(Set.of(
                "NavigationActor::Application::NavigationTestModel::RelatedRow::detail::View::PageDefinition::detail::Back",
                "NavigationActor::Application::NavigationTestModel::RelatedRow::detail::View::PageDefinition::detail::Refresh",
                "NavigationActor::Application::NavigationTestModel::RelatedRow::detail::View::PageDefinition::myJumper::OpenPage",
                "NavigationActor::Application::NavigationTestModel::RelatedRow::detail::View::PageDefinition::myJumper::Refresh",
                "NavigationActor::Application::NavigationTestModel::RelatedRow::detail::View::PageDefinition::myJumpers::OpenPage",
                "NavigationActor::Application::NavigationTestModel::RelatedRow::detail::View::PageDefinition::myJumpers::Filter",
                "NavigationActor::Application::NavigationTestModel::RelatedRow::detail::View::PageDefinition::myJumpers::Refresh"
        ), relatedViewPageActions.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        Action detailBackAction = relatedViewPageActions.stream().filter(a -> a.getName().equals("detail::Back")).findFirst().orElseThrow();
        assertTrue(detailBackAction.getIsBackAction());

        Action myJumperOpenPageAction = relatedViewPageActions.stream().filter(a -> a.getName().equals("myJumper::OpenPage")).findFirst().orElseThrow();
        assertTrue(myJumperOpenPageAction.getIsOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::RelatedView::myJumper::View::PageDefinition")).findFirst().orElse(null), myJumperOpenPageAction.getTargetPageDefinition());

        Action myJumpersCollectionOpenPageAction = relatedViewPageActions.stream().filter(a -> a.getName().equals("myJumpers::OpenPage")).findFirst().orElseThrow();
        assertTrue(myJumpersCollectionOpenPageAction.getIsRowOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::JumperRow::jumperRowDetail::View::PageDefinition")).findFirst().orElse(null), myJumpersCollectionOpenPageAction.getTargetPageDefinition());
    }
}
