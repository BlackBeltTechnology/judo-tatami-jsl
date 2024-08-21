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
public class JslModel2UiTest extends AbstractTest  {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/car";

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
    void testCar() throws Exception {
        jslModel = JslParser.getModelFromStrings("Car", List.of("""
            model Car;

            import judo::types;

            entity Car {
                field String make;
                field String type;
            }

            transfer CarTransfer(Car c) {
                field String make <=> c.make;
                field String type <=> c.type;
            }

            actor User {
                access CarTransfer[] cars <= Car.all() create delete update;
            }

            form CarForm(CarTransfer ct) {
                widget String make2 <= ct.make;
            }

            view CarView(CarTransfer ct) {
                widget String make2 <= ct.make;
                widget String type <= ct.type;
            }

            table CarTable(CarTransfer ct) {
                column String make <= ct.make;
                column String type <= ct.type;
            }

            menu CarApp(User usr) {
                table CarTable cars <= usr.cars form:CarForm view:CarView;
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app = apps.get(0);
    }
}
