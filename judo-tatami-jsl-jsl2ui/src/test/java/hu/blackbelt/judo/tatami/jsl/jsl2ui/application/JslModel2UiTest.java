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

                event create createCar;
                event update updateCar;
                event delete deleteCar;
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

        Application application = apps.get(0);

        List<RelationType> relationTypes = application.getRelationTypes();
        List<ClassType> classTypes = application.getClassTypes();
        List<PageContainer> pageContainers = application.getPageContainers();
        List<PageDefinition> pages = application.getPages();
        List<Link> links = application.getLinks();
        List<Table> tables = application.getTables();
        List<Action> allActions = application.getPages().stream().flatMap(ps -> ps.getActions().stream()).toList();

        assertEquals(Set.of(
                "cars"
        ), relationTypes.stream().map(NamedElement::getName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "Car::CarTransfer::ClassType",
                "Car::User::ClassType"
        ), classTypes.stream().map(NamedElement::getName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "Car::CarView::View::PageContainer",
                "Car::CarApp::cars::Table::PageContainer",
                "Car::CarApp::Dashboard",
                "Car::CarForm::Create::PageContainer"
        ), pageContainers.stream().map(NamedElement::getName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "Car::CarApp::cars::View::PageDefinition",
                "Car::CarApp::cars::Create::PageDefinition",
                "Car::CarApp::cars::Table::PageDefinition",
                "Car::CarApp::DashboardPage"
        ), pages.stream().map(NamedElement::getName).collect(Collectors.toSet()));

        assertEquals(Set.of(), links.stream().map(NamedElement::getName).collect(Collectors.toSet()));

        assertEquals(Set.of(
                "cars::Table"
        ), tables.stream().map(NamedElement::getName).collect(Collectors.toSet()));
    }
}
