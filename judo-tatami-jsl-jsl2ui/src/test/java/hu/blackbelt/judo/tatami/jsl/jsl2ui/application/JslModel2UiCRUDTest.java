package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.Application;
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
                link JumperView myJumper <= r.theJumper eager:false icon:"jumping" label:"My Jumper" width:6 create:true update: true delete:true;
                table JumperRow[] myJumpers <= r.theJumpersCollection eager:false icon:"jumping-all" label:"My Jumpers" width:6;

                event create onCreate(RelatedForm form);
                event update onUpdate;
                event delete onDelete;
            }

            view RelatedForm(Related r) {
                group one label:"Group 1" {
                    field String first <= r.first label: "First";
                }

                action void doSubmit();
            }

            view JumperView(Jumper j) {
                field String first <= j.first label: "First";

                event create onCreate;
                event update onUpdate;
                event delete onDelete;
            }

            row JumperRow(Jumper j) {
                field String first <= j.first label: "First";
                link JumperView jumperRowDetail <= j detail:true;
            }

            actor NavigationActor human {
                link UserView user <= User.any() label:"User" icon:"tools" update: true delete:true;
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);
    }
}
