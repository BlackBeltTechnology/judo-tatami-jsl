package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.Theme;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
public class JslModel2UiApplicationTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/application";

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
        return new BufferedSlf4jLogger(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testActors() throws Exception {
        jslModel = JslParser.getModelFromStrings("ApplicationTestModel", List.of("""
            model ApplicationTestModel;
            
            import judo::types;
            
            entity User {
                identifier required String userName;
            }
            
            actor human AppActor(User user) {
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);

        assertEquals("AppActor", app1.getName());
        assertEquals("judo-color-logo.png", app1.getLogo());
        assertEquals("en-US", app1.getDefaultLanguage());

        assertNotNull(app1.getActor());

        ClassType actor = app1.getActor();

        assertEquals("ApplicationTestModel::AppActor", actor.getName());
        assertEquals("AppActor", actor.getSimpleName());
        assertTrue(actor.isIsActor());

        assertNotNull(app1.getTheme());

        Theme theme = app1.getTheme();

        assertEquals("#3C4166FF", theme.getPrimaryColor());
        assertEquals("#E7501DFF", theme.getSecondaryColor());
        assertEquals("#17191DFF", theme.getTextPrimaryColor());
        assertEquals("#434448FF", theme.getTextSecondaryColor());
        assertEquals("#FAFAFAFF", theme.getBackgroundColor());
        assertEquals("#8C8C8C", theme.getSubtitleColor());
    }
}
