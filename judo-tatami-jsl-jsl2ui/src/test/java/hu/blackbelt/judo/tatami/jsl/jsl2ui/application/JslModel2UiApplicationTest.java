package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
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
            
            entity Product {
                identifier required String name;
                field required Integer price;
            }
            
            entity Product2 {
                identifier required String name2;
                field required Integer price2;
            }
            
            view ProductListView {
                table ProductRow[] products <= Product.all() detail:ProductDetailView;
            }
            
            view ProductListView2 {
                table ProductRow2[] products2 <= Product2.all() detail:ProductDetailView2;
            }
            
            view ProductDetailView(Product product) {
                field String name <= product.name;
                field String price <= product.price.asString() + " HUF";
            }
            
            view ProductDetailView2(Product2 product2) {
                field String name2 <= product2.name2;
                field String price2 <= product2.price2.asString() + " HUF";
            }
            
            row ProductRow(Product product) {
                column String name <= product.name;
                column String price <= product.price.asString() + " HUF";
            }
            
            row ProductRow2(Product2 product2) {
                column String name <= product2.name2;
                column String price <= product2.price2.asString() + " HUF";
            }
            
            actor human Actor1(User user) {
                group first label:"Group1" {
                    menu ProductListView products2 label:"Products11";
                }
                menu ProductListView allProducts label:"All Products" icon:"tools";
            }
            
            actor human Actor2(User2 user2) {
                group first label:"Group2" {
                    menu ProductListView2 products22 label:"Products21";
                }
                menu ProductListView2 allProducts2 label:"All Products 2" icon:"tools";
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

        assertEquals(4, classTypes.size());

        Set<String> class1Names = classTypes.stream().map(c -> c.getName()).collect(Collectors.toSet());

        assertTrue(Set.of(
                "MultipleActorsTestModel::Actor1::ClassType",
                "MultipleActorsTestModel::ProductDetailView::ClassType",
                "MultipleActorsTestModel::ProductListView::ClassType",
                "MultipleActorsTestModel::ProductRow::ClassType"
        ).containsAll(class1Names));


        List<ClassType> classTypes2 = app2.getClassTypes();

        assertEquals(4, classTypes2.size());

        Set<String> class2Names = classTypes2.stream().map(c -> c.getName()).collect(Collectors.toSet());

        assertTrue(Set.of(
                "MultipleActorsTestModel::Actor2::ClassType",
                "MultipleActorsTestModel::ProductDetailView2::ClassType",
                "MultipleActorsTestModel::ProductListView2::ClassType",
                "MultipleActorsTestModel::ProductRow2::ClassType"
        ).containsAll(class2Names));

        // Pages

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

    @Test
    void testSecurity() throws Exception {
        jslModel = JslParser.getModelFromStrings("SecurityTestModel", List.of("""
            model SecurityTestModel;
            
            import judo::types;
            
            entity User {
                identifier required String email;
            }
            
            transfer UserTransfer maps User as u {
                field String email <= u.email update: true;
            }
            
            actor human Actor(User user) realm:"COMPANY" claim:"email" identity:UserTransfer::email {
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        // Apps

        assertEquals(1, apps.size());

        // Authentication

        Authentication authentication = apps.get(0).getAuthentication();

        assertNotNull(authentication);

        assertEquals("COMPANY", authentication.getRealm());

    }
}
