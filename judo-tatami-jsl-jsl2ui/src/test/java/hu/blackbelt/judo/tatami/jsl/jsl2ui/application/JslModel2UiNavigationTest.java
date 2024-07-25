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

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JslModel2UiNavigationTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/widgets";

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
        List<Link> links = application.getLinks();
        List<Table> tables = application.getTables();

        ClassType relatedRowClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationTestModel::RelatedRow::ClassType")).findFirst().orElseThrow();
        ClassType relatedViewClassType = classTypes.stream().filter(c -> c.getName().equals("NavigationTestModel::RelatedView::ClassType")).findFirst().orElseThrow();

        RelationType detailRelation = (RelationType) application.getRelationTypes().stream().filter(r -> ((RelationType) r).getName().equals("detail")).findFirst().orElseThrow();

        // Tables

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


        // Links

        Link relatedLink = links.stream().filter(l -> l.getName().equals("related")).findFirst().orElseThrow();

        List<Button> linkButtons =  relatedLink.getActionButtonGroup().getButtons();

        assertEquals(1, linkButtons.size());

        Button openButton = linkButtons.stream().filter(b -> b.getActionDefinition().getIsOpenPageAction()).findFirst().orElseThrow();
        OpenPageActionDefinition openPageActionDefinition = (OpenPageActionDefinition) openButton.getActionDefinition();

        assertEquals("contained", openButton.getButtonStyle());
        assertEquals("View", openButton.getLabel());
        assertEquals("eye", openButton.getIcon().getIconName());

        assertEquals(relatedViewClassType, openPageActionDefinition.getTargetType());
    }
}
