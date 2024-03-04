package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.NavigationController;
import hu.blackbelt.judo.meta.ui.NavigationItem;
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
        return log;
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

        assertEquals("AppActor::Application", app1.getName());
        assertEquals("ApplicationTestModel", app1.getModelName());
        assertEquals("judo-color-logo.png", app1.getLogo());
        assertEquals("en-US", app1.getDefaultLanguage());

        assertNotNull(app1.getActor());

        ClassType actor = app1.getActor();

        assertEquals("ApplicationTestModel::AppActor::ClassType", actor.getName());
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

    @Test
    void testMenu() throws Exception {
        jslModel = JslParser.getModelFromStrings("MenuTestModel", List.of("""
            model MenuTestModel;
            
            import judo::types;
            
            entity User {
                identifier required String userName;
            }
            
            view UserListView {
                table UserRow[] users <= User.all();
            }
            
            row UserRow(User user) {
                column String userName <= user.userName;
            }
            
            entity Product {
                identifier required String name;
                field required Integer price;
            }
            
            view ProductListView {
                table ProductRow[] products <= Product.all();
            }
            
            row ProductRow(Product product) {
                column String name <= product.name;
                column String price <= product.price.asString() + " HUF";
            }
            
            actor human MenuActor(User user) {
                group first label:"Group1" {
                    group second label:"Group2" {
                        menu ProductListView products label:"Products" icon:"close";
                    }
                    menu ProductListView products2 label:"Products2";
                }
                menu UserListView users label:"Users" icon:"account-multiple";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);

        NavigationController navigationController = app1.getNavigationController();

        assertNotNull(navigationController);

        List<NavigationItem> firstLevelMenus = navigationController.getItems();

        assertEquals(2, firstLevelMenus.size());

        NavigationItem first1 = firstLevelMenus.get(0);
        NavigationItem first2 = firstLevelMenus.get(1);

        assertEquals("MenuTestModel::MenuActor::MenuItemGroup::first", first1.getName());
        assertEquals("Group1", first1.getLabel());
        assertEquals("MenuTestModel::MenuActor::users", first2.getName());
        assertEquals("Users", first2.getLabel());

        List<NavigationItem> secondLevelMenus = first1.getItems();

        assertEquals(2, secondLevelMenus.size());

        NavigationItem second1 = secondLevelMenus.get(0);
        NavigationItem second2 = secondLevelMenus.get(1);

        assertEquals("MenuTestModel::MenuActor::MenuItemGroup::first::MenuItemGroup::second", second1.getName());
        assertEquals("Group2", second1.getLabel());
        assertEquals("MenuTestModel::MenuActor::MenuItemGroup::first::products2", second2.getName());
        assertEquals("Products2", second2.getLabel());

        List<NavigationItem> thirdLevelMenus = second1.getItems();

        assertEquals(1, thirdLevelMenus.size());

        NavigationItem third1 = thirdLevelMenus.get(0);

        assertEquals("MenuTestModel::MenuActor::MenuItemGroup::first::MenuItemGroup::second::products", third1.getName());
        assertEquals("Products", third1.getLabel());
        assertEquals("close", third1.getIcon().getIconName());
    }
}
