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

            entity Related {
                field String first;
                field Integer second;
                field Jumper theJumper;
                relation Jumper[] theJumpersCollection;
            }

            entity Jumper {
                field String first;
            }

            view UserView(User u) {
                group level1 {
                    link RelatedView related <= u.related eager:true icon:"related" label:"Related" width:6;
                    table RelatedRow[] relatedCollection <= u.relatedCollection icon:"relatedCollection" label:"Related Collection";
                }
            }

            row RelatedRow(Related r) {
                field String first <= r.first label:"First";
                field Integer second <= r.second label:"Second";
                link RelatedView detail <= r detail:true;
            }

            view RelatedView(Related r) {
                field String first <= r.first label: "First";
                field Integer second <= r.second label: "Second";
                link JumperView myJumper <= r.theJumper eager:false icon:"jumping" label:"My Jumper" width:6;
                table JumperRow[] myJumpers <= r.theJumpersCollection eager:false icon:"jumping-all" label:"My Jumpers" width:6;
            }

            view JumperView(Jumper j) {
                field String first <= j.first label: "First";
            }

            row JumperRow(Jumper j) {
                field String first <= j.first label: "First";
                link JumperView jumperRowDetail <= j detail:true;
            }

            actor NavigationActor human {
                link UserView user <= User.any() label:"User" icon:"tools";
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

        assertEquals(6, classTypes.size());
        assertEquals(2, links.size());
        assertEquals(2, tables.size());
        assertEquals(6, pages.size());
        assertEquals(7, relationTypes.size());

        ClassType relatedRowClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationTestModel::RelatedRow::ClassType")).findFirst().orElseThrow();
        ClassType relatedViewClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationTestModel::RelatedView::ClassType")).findFirst().orElseThrow();
        ClassType jumperRowClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationTestModel::JumperRow::ClassType")).findFirst().orElseThrow();
        ClassType jumperViewClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationTestModel::JumperView::ClassType")).findFirst().orElseThrow();

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

        assertEquals(relatedRowClassType, rowOpenPageActionDefinition.getTargetType());
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

        assertEquals(jumperRowClassType, myJumpersRowOpenPageActionDefinition.getTargetType());
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

        assertEquals(relatedViewClassType, openPageActionDefinition.getTargetType());

        // - myJumper

        Link myJumperLink = links.stream().filter(l -> l.getName().equals("myJumper")).findFirst().orElseThrow();

        List<Button> myJumperLinkButtons =  myJumperLink.getActionButtonGroup().getButtons();

        assertEquals(1, myJumperLinkButtons.size());

        Button myJumperOpenButton = myJumperLinkButtons.stream().filter(b -> b.getActionDefinition().getIsOpenPageAction()).findFirst().orElseThrow();
        OpenPageActionDefinition myJumperOpenPageActionDefinition = (OpenPageActionDefinition) myJumperOpenButton.getActionDefinition();

        assertEquals("contained", myJumperOpenButton.getButtonStyle());
        assertEquals("View", myJumperOpenButton.getLabel());
        assertEquals("eye", myJumperOpenButton.getIcon().getIconName());

        assertEquals(jumperViewClassType, myJumperOpenPageActionDefinition.getTargetType());

        // Actions

        // - User access

        PageDefinition userAccessPage = pages.stream().filter(p -> p.getName().equals("NavigationTestModel::NavigationActor::user::PageDefinition")).findFirst().orElseThrow();
        List<Action> userAccessPageActions = userAccessPage.getActions();

        assertEquals(2, userAccessPageActions.size());

        Action relatedOpenPageAction = userAccessPageActions.stream().filter(a -> a.getName().equals("related::ViewLinkDeclarationOpenPageAction")).findFirst().orElseThrow();
        assertTrue(relatedOpenPageAction.getIsOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::UserView::related::PageDefinition")).findFirst().orElse(null), relatedOpenPageAction.getTargetPageDefinition());

        Action relatedCollectionOpenPageAction = userAccessPageActions.stream().filter(a -> a.getName().equals("relatedCollection::ViewTableDeclarationOpenPageAction")).findFirst().orElseThrow();
        assertTrue(relatedCollectionOpenPageAction.getIsRowOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::RelatedRow::detail::PageDefinition")).findFirst().orElse(null), relatedCollectionOpenPageAction.getTargetPageDefinition());

        // - RelatedView relation view

        PageDefinition relatedViewPage = pages.stream().filter(p -> p.getName().equals("NavigationTestModel::RelatedRow::detail::PageDefinition")).findFirst().orElseThrow();
        List<Action> relatedViewPageActions = relatedViewPage.getActions();

        assertEquals(2, relatedViewPageActions.size());

        Action myJumperOpenPageAction = relatedViewPageActions.stream().filter(a -> a.getName().equals("myJumper::ViewLinkDeclarationOpenPageAction")).findFirst().orElseThrow();
        assertTrue(myJumperOpenPageAction.getIsOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::RelatedView::myJumper::PageDefinition")).findFirst().orElse(null), myJumperOpenPageAction.getTargetPageDefinition());

        Action myJumpersCollectionOpenPageAction = relatedViewPageActions.stream().filter(a -> a.getName().equals("myJumpers::ViewTableDeclarationOpenPageAction")).findFirst().orElseThrow();
        assertTrue(myJumpersCollectionOpenPageAction.getIsRowOpenPageAction());
        assertEquals(pages.stream().filter(p -> p.getName().equals("NavigationTestModel::JumperRow::jumperRowDetail::PageDefinition")).findFirst().orElse(null), myJumpersCollectionOpenPageAction.getTargetPageDefinition());
    }
}
