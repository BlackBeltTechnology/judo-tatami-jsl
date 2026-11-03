package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.RelationType;
import hu.blackbelt.judo.tatami.core.TransformationMode;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JslModel2UiCarTest extends AbstractTest  {
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


    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class)
    void testCar(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("Car", List.of("""
            model Car;

            type binary Binary max-file-size: 1MB  mime-type: ["image/*"];
            type boolean Boolean;
            type date Date;
            type numeric Numeric scale: 0 precision: 9;
            type string String min-size: 0 max-size: 255;
            type time Time;
            type timestamp Timestamp;

            widget string StringWidget;

            entity User {
                identifier String email;
                field Boolean isActive;
            }

            transfer UserTransfer maps User as u {
                field String email <=> u.email;
  
                event create createInstance;
                event delete deleteInstance;
                event update updateInstance;
            }

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

            actor UserActor realm: "COMPANY" claim: "email" identity: UserTransfer::email {
                access CarTransfer[] cars <= Car.all() create delete update;
            }

            form CarForm(CarTransfer ct) {
                widget StringWidget make2 <= ct.make;
            }

            view CarView(CarTransfer ct) {
                widget StringWidget make2 <= ct.make;
                widget StringWidget type <= ct.type;
            }

            row CarRow(CarTransfer ct) {
                column String make <= ct.make;
                column String type <= ct.type;
            }

            frontend CarApp(UserActor usr)
                menu: {
                    table CarRow[] cars <= usr.cars form:CarForm view:CarView;
                };
        """));

        transform(mode);

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);

        assertEquals("UserActor", application.getName());

        List<RelationType> relationTypes = application.getRelationTypes();
        List<ClassType> classTypes = application.getClassTypes();
        List<PageContainer> pageContainers = application.getPageContainers();
        List<PageDefinition> pages = application.getPages();
        List<Link> links = application.getLinks();
        List<Table> tables = application.getTables();
        List<Action> allActions = application.getPages().stream().flatMap(ps -> ps.getActions().stream()).toList();

        assertEquals(List.of(
                "cars"
        ), relationTypes.stream().map(NamedElement::getName).sorted().toList());

        assertEquals(List.of(
                "Car::CarTransfer",
                "Car::UserActor",
                "Car::UserTransfer"
        ), classTypes.stream().map(NamedElement::getName).sorted().toList());

        assertEquals(List.of(
                "Car::CarApp::Dashboard",
                "Car::CarForm::Create::PageContainer",
                "Car::CarRow::Table::PageContainer",
                "Car::CarView::View::PageContainer"
        ), pageContainers.stream().map(NamedElement::getName).sorted().toList());

        assertEquals(List.of(
                "Car::CarApp::DashboardPage",
                "Car::CarApp::cars::AccessFormPage",
                "Car::CarApp::cars::AccessTablePage",
                "Car::CarApp::cars::AccessTableViewPage"
        ), pages.stream().map(NamedElement::getName).sorted().toList());

        assertEquals(List.of(), links.stream().map(NamedElement::getName).sorted().toList());

        assertEquals(List.of(
                "CarRow::Table"
        ), tables.stream().map(NamedElement::getName).sorted().toList());
    }
}
