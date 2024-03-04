package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
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

    @Test
    void testMultipleActors() throws Exception {
        jslModel = JslParser.getModelFromStrings("MultipleActorsTestModel", List.of("""
            model MultipleActorsTestModel;
            
            import judo::types;
            
            entity User {
                identifier required String userName;
            }
            
            entity User2 {
                identifier required String userName;
            }
            
            view UserListView {
                table UserRow[] users <= User.all();
            }
            
            view User2ListView {
                table User2Row[] users <= User2.all();
            }
            
            row UserRow(User user) {
                column String userName <= user.userName;
            }
            
            row User2Row(User2 user) {
                column String userName <= user.userName;
            }
            
            entity Product {
                identifier required String name;
                field required Integer price;
            }
            
            view ProductListView {
                table ProductRow[] products <= Product.all() detail:ProductDetailView;
            }
            
            view ProductDetailView(Product product) {
                field String name <= product.name;
                field String price <= product.price.asString() + " HUF";
            }
            
            row ProductRow(Product product) {
                column String name <= product.name;
                column String price <= product.price.asString() + " HUF";
            }
            
            actor human Actor1(User user) {
                group first label:"Group1" {
                    menu ProductListView products2 label:"Products11";
                }
                menu ProductListView allProducts label:"All Products" icon:"tools";
            }
            
            actor human Actor2(User2 user2) {
                group first label:"Group2" {
                    menu ProductListView products22 label:"Products21";
                }
                menu ProductListView allProducts2 label:"All Products 2" icon:"tools";
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        // Apps

        assertEquals(2, apps.size());

        Application app1 = apps.get(0);
        Application app2 = apps.get(1);

        assertEquals("Actor1::Application", app1.getName());
        assertEquals("MultipleActorsTestModel", app1.getModelName());
        assertEquals("judo-color-logo.png", app1.getLogo());
        assertEquals("en-US", app1.getDefaultLanguage());

        assertNotNull(app1.getActor());

        ClassType actor = app1.getActor();

        assertEquals("MultipleActorsTestModel::Actor1::ClassType", actor.getName());
        assertEquals("Actor1", actor.getSimpleName());
        assertTrue(actor.isIsActor());

        assertEquals("Actor1::Application", app1.getName());
        assertEquals("MultipleActorsTestModel", app1.getModelName());
        assertEquals("judo-color-logo.png", app1.getLogo());
        assertEquals("en-US", app1.getDefaultLanguage());

        assertNotNull(app2.getActor());

        ClassType actor2 = app2.getActor();

        assertEquals("MultipleActorsTestModel::Actor2::ClassType", actor2.getName());
        assertEquals("Actor2", actor2.getSimpleName());
        assertTrue(actor2.isIsActor());

        assertEquals("Actor2::Application", app2.getName());
        assertEquals("MultipleActorsTestModel", app2.getModelName());
        assertEquals("judo-color-logo.png", app2.getLogo());
        assertEquals("en-US", app2.getDefaultLanguage());

        // Menus

        NavigationController navigationController = app1.getNavigationController();
        assertNotNull(navigationController);

        List<NavigationItem> firstLevelMenus = navigationController.getItems();

        NavigationItem first1 = firstLevelMenus.get(0);
        NavigationItem first2 = firstLevelMenus.get(1);

        assertEquals("MultipleActorsTestModel::Actor1::MenuItemGroup::first", first1.getName());
        assertEquals("Group1", first1.getLabel());
        assertEquals("MultipleActorsTestModel::Actor1::allProducts", first2.getName());
        assertEquals("All Products", first2.getLabel());

        NavigationController navigationController2 = app2.getNavigationController();
        assertNotNull(navigationController2);

        List<NavigationItem> firstLevelMenus2 = navigationController2.getItems();

        NavigationItem first21 = firstLevelMenus2.get(0);
        NavigationItem first22 = firstLevelMenus2.get(1);

        assertEquals("MultipleActorsTestModel::Actor2::MenuItemGroup::first", first21.getName());
        assertEquals("Group2", first21.getLabel());
        assertEquals("MultipleActorsTestModel::Actor2::allProducts2", first22.getName());
        assertEquals("All Products 2", first22.getLabel());

        // Data Elements

        List<ClassType> classTypes = app1.getClassTypes();

        assertEquals(1, classTypes.size());

        assertEquals("MultipleActorsTestModel::Actor1::ClassType", classTypes.get(0).getName());

        List<PageDefinition> pages = app1.getPages();

        assertEquals(2, pages.size());

        PageDefinition page = pages.get(0);
        PageDefinition page2 = pages.get(1);

        assertEquals("MultipleActorsTestModel::Actor1::MenuItemGroup::first::products2::PageDefinition", page.getName());
        assertEquals("MultipleActorsTestModel::Actor1::allProducts::PageDefinition", page2.getName());

        List<PageDefinition> pages2 = app2.getPages();

        assertEquals(2, pages2.size());

        PageDefinition page21 = pages2.get(0);
        PageDefinition page22 = pages2.get(1);

        assertEquals("MultipleActorsTestModel::Actor2::MenuItemGroup::first::products22::PageDefinition", page21.getName());
        assertEquals("MultipleActorsTestModel::Actor2::allProducts2::PageDefinition", page22.getName());
    }
}
