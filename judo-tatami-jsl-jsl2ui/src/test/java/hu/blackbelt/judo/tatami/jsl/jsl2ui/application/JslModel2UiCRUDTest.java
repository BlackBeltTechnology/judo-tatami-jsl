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

        assertEquals(8, relationTypes.size());
        assertEquals(8, classTypes.size());
        assertEquals(8, pageContainers.size());
        assertEquals(8, pages.size());
        assertEquals(3, links.size());
        assertEquals(2, tables.size());


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

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::RelatedView::PageContainer::RelatedView::myJumper",
                "NavigationActor::Application::CRUDTestModel::RelatedView::PageContainer::RelatedView::readOnlyJumper",
                "NavigationActor::Application::CRUDTestModel::UserView::PageContainer::UserView::level::related"
        ), links.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "NavigationActor::Application::CRUDTestModel::RelatedView::PageContainer::RelatedView::myJumpers",
                "NavigationActor::Application::CRUDTestModel::UserView::PageContainer::UserView::level::relatedCollection"
        ), tables.stream().map(NamedElement::getFQName).collect(Collectors.toSet()));

        PageDefinition userPage = pages.stream().filter(p -> p.getName().equals("CRUDTestModel::NavigationActor::user::View::PageDefinition")).findFirst().orElseThrow();
        PageContainer userPageContainer = userPage.getContainer();

        assertEquals(1, userPageContainer.getLinks().size());
        assertEquals(1, userPageContainer.getTables().size());
        assertTrue(userPage.getActions().stream().anyMatch(a -> a.getActionDefinition().getIsDeleteAction()));
        assertTrue(userPage.getActions().stream().anyMatch(a -> a.getActionDefinition().getIsUpdateAction()));
    }
}
