package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.DataType;
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

            actor AppActor;

            menu AppMenu(AppActor a) {
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);

        assertEquals("AppActor", app1.getName());
        assertEquals("ApplicationTestModel", app1.getModelName());
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

    @Test
    void testMenu() throws Exception {
        jslModel = JslParser.getModelFromStrings("MenuTestModel", List.of("""
            model MenuTestModel;

            import judo::types;

            entity User {
                identifier String userName required;
            }

            entity Product {
                identifier String name required;
                field Integer price required;
            }

            transfer UserTransfer(User u) {
                field String userName <= u.userName required;
            }

            transfer ProductTransfer(Product p) {
                field String name <= p.name required;
                field String price <= p.price.asString() + " HUF";
            }

            table UsersTable(UserTransfer u) {
                column String userName <= u.userName label:"Username";
            }

            table ProductsTable(ProductTransfer p) {
                column String name <= p.name label:"Name";
                column String price <= p.price label:"Price";
            }

            actor Actor {
                access ProductTransfer[] products <= Product.all();
                access ProductTransfer[] products2 <= Product.all();
                access UserTransfer[] users <= User.all();
            }

            menu MenuActor(Actor usr) {
                group first label:"Group1" {
                    group second label:"Group2" {
                        table ProductsTable products <= usr.products label:"Products" icon:"close";
                    }
                    table ProductsTable products2 <= usr.products2 label:"Products2";
                }
                table UsersTable users <= usr.users label:"Users" icon:"account-multiple";
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

        assertEquals("MenuTestModel::MenuActor::first", first1.getName());
        assertEquals("Group1", first1.getLabel());
        assertEquals("MenuTestModel::MenuActor::users", first2.getName());
        assertEquals("Users", first2.getLabel());

        List<NavigationItem> secondLevelMenus = first1.getItems();

        assertEquals(2, secondLevelMenus.size());

        NavigationItem second1 = secondLevelMenus.get(0);
        NavigationItem second2 = secondLevelMenus.get(1);

        assertEquals("MenuTestModel::MenuActor::first::second", second1.getName());
        assertEquals("Group2", second1.getLabel());
        assertEquals("MenuTestModel::MenuActor::first::products2", second2.getName());
        assertEquals("Products2", second2.getLabel());

        List<NavigationItem> thirdLevelMenus = second1.getItems();

        assertEquals(1, thirdLevelMenus.size());

        NavigationItem third1 = thirdLevelMenus.get(0);

        assertEquals("MenuTestModel::MenuActor::first::second::products", third1.getName());
        assertEquals("Products", third1.getLabel());
        assertEquals("close", third1.getIcon().getIconName());
    }

    @Test
    void testMultipleActors() throws Exception {
        jslModel = JslParser.getModelFromStrings("MultipleActorsTestModel", List.of("""
            model MultipleActorsTestModel;

            import judo::types;

            entity User {
                identifier String userName required;
            }

            entity User2 {
                identifier String userName2 required;
            }

            entity Product {
                identifier String name required;
                field Integer price required;
            }

            entity Product2 {
                identifier String name2 required;
                field Integer price2 required;
            }

            transfer UserTransfer(User u) {
                field String userName <= u.userName;
            }

            transfer User2Transfer(User2 u) {
                field String userName2 <= u.userName2;
            }

            transfer ProductTransfer(Product p) {
                field String name required;
                field Integer priceOriginal <= p.price;
                field String price <= p.price.asString() + " HUF";

                event update onUpdate;
            }

            transfer Product2Transfer(Product2 p) {
                field String name2 required;
                field Integer price2Original <= p.price2;
                field String price2 <= p.price2.asString() + " HUF";

                event update onUpdate;
            }

            view ProductView(ProductTransfer product) {
                widget String name <= product.name;
                widget String price <= product.price;
            }

            view Product2View(Product2Transfer product2) {
                widget String name2 <= product2.name2;
                widget String price2 <= product2.price2;
            }

            table ProductsTable(ProductTransfer product) {
                column String name <= product.name label:"Name";
                column String price <= product.price label:"Price";
            }

            table ProductsTable2(Product2Transfer product2) {
                column String name2 <= product2.name2 label:"Name 2";
                column String price2 <= product2.price2 label:"Price 2";
            }

            actor Actor1 {
                access ProductTransfer[] products <= Product.all() update;
            }

            actor Actor2 {
                access Product2Transfer[] products2 <= Product2.all() update;
            }

            menu App1(Actor1 a) {
                group first label:"Group1" {
                    table ProductsTable products1 <= a.products label:"Products1" view:ProductView;
                }
                table ProductsTable allProducts <= a.products label:"All Products" icon:"tools" view:ProductView;
            }

            menu App2(Actor2 a) {
                group first label:"Group2" {
                    table ProductsTable2 products2 <= a.products2 label:"Products2" view:Product2View;
                }
                table ProductsTable2 allProducts2 <= a.products2 label:"All Products 2" icon:"tools" view:Product2View;
            }
        """));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        // Apps

        assertEquals(2, apps.size());

        Application app1 = apps.get(0);
        Application app2 = apps.get(1);

        assertEquals("Actor1", app1.getName());
        assertEquals("MultipleActorsTestModel", app1.getModelName());
        assertEquals("judo-color-logo.png", app1.getLogo());
        assertEquals("en-US", app1.getDefaultLanguage());

        assertNotNull(app1.getActor());

        ClassType actor = app1.getActor();

        assertEquals("MultipleActorsTestModel::Actor1", actor.getName());
        assertEquals("Actor1", actor.getSimpleName());
        assertTrue(actor.isIsActor());

        assertEquals("Actor1", app1.getName());
        assertEquals("MultipleActorsTestModel", app1.getModelName());
        assertEquals("judo-color-logo.png", app1.getLogo());
        assertEquals("en-US", app1.getDefaultLanguage());

        assertNotNull(app2.getActor());

        ClassType actor2 = app2.getActor();

        assertEquals("MultipleActorsTestModel::Actor2", actor2.getName());
        assertEquals("Actor2", actor2.getSimpleName());
        assertTrue(actor2.isIsActor());

        assertEquals("Actor2", app2.getName());
        assertEquals("MultipleActorsTestModel", app2.getModelName());
        assertEquals("judo-color-logo.png", app2.getLogo());
        assertEquals("en-US", app2.getDefaultLanguage());

        // Menus

        NavigationController navigationController = app1.getNavigationController();
        assertNotNull(navigationController);

        List<NavigationItem> firstLevelMenus = navigationController.getItems();

        NavigationItem first1 = firstLevelMenus.get(0);
        NavigationItem first2 = firstLevelMenus.get(1);

        assertEquals("MultipleActorsTestModel::App1::first", first1.getName());
        assertEquals("Group1", first1.getLabel());
        assertEquals("MultipleActorsTestModel::App1::allProducts", first2.getName());
        assertEquals("All Products", first2.getLabel());

        NavigationController navigationController2 = app2.getNavigationController();
        assertNotNull(navigationController2);

        List<NavigationItem> firstLevelMenus2 = navigationController2.getItems();

        NavigationItem first21 = firstLevelMenus2.get(0);
        NavigationItem first22 = firstLevelMenus2.get(1);

        assertEquals("MultipleActorsTestModel::App2::first", first21.getName());
        assertEquals("Group2", first21.getLabel());
        assertEquals("MultipleActorsTestModel::App2::allProducts2", first22.getName());
        assertEquals("All Products 2", first22.getLabel());

        // Data Elements

        List<ClassType> classTypes = app1.getClassTypes();
        List<RelationType> relationTypes = app1.getRelationTypes();

        assertEquals(2, classTypes.size());
        assertEquals(1, relationTypes.size());

        Set<String> class1Names = classTypes.stream().map(c -> c.getName()).collect(Collectors.toSet());
        Set<String> relations1Names = relationTypes.stream().map(c -> c.getFQName()).collect(Collectors.toSet());

        assertEquals(Set.of(
                "MultipleActorsTestModel::ProductTransfer",
                "MultipleActorsTestModel::Actor1"
        ), class1Names);

        assertEquals(Set.of(
                "Actor1::MultipleActorsTestModel::Actor1::products"
        ), relations1Names);

        List<DataType> dataTypes1 = app1.getDataTypes();

        Set<String> dataTypes1Names = dataTypes1.stream().map(c -> c.getName()).collect(Collectors.toSet());

        assertEquals(Set.of(
                "StringOperation",
                "Integer",
                "EnumerationOperation",
                "BooleanOperation",
                "String",
                "NumericOperation"
        ), dataTypes1Names);

        List<ClassType> classTypes2 = app2.getClassTypes();
        List<RelationType> relationsTypes2 = app2.getRelationTypes();

        assertEquals(2, classTypes2.size());
        assertEquals(1, relationsTypes2.size());

        Set<String> class2Names = classTypes2.stream().map(c -> c.getName()).collect(Collectors.toSet());
        Set<String> relations2Names = relationsTypes2.stream().map(c -> c.getFQName()).collect(Collectors.toSet());

        assertEquals(Set.of(
                "MultipleActorsTestModel::Product2Transfer",
                "MultipleActorsTestModel::Actor2"
        ), class2Names);

        assertEquals(Set.of(
                "Actor2::MultipleActorsTestModel::Actor2::products2"
        ), relations2Names);

        // Pages

        List<PageDefinition> pages = app1.getPages();

        assertEquals(Set.of(
                "MultipleActorsTestModel::App1::allProducts::AccessTablePage",
                "MultipleActorsTestModel::App1::DashboardPage",
                "MultipleActorsTestModel::App1::first::products1::AccessTablePage"
        ), pages.stream().map(NamedElement::getName).collect(Collectors.toSet()));

        List<PageDefinition> pages2 = app2.getPages();

        assertEquals(Set.of(
                "MultipleActorsTestModel::App2::allProducts2::AccessTablePage",
                "MultipleActorsTestModel::App2::DashboardPage",
                "MultipleActorsTestModel::App2::first::products2::AccessTablePage"
        ), pages2.stream().map(NamedElement::getName).collect(Collectors.toSet()));
    }

    @Test
    void testSecurity() throws Exception {
        jslModel = JslParser.getModelFromStrings("SecurityTestModel", List.of("""
            model SecurityTestModel;

            import judo::types;

            entity User {
                identifier String email required;
            }

            transfer UserTransfer(User u) {
                field String email <=> u.email;
            }

            actor Actor realm:"COMPANY" claim:"email" identity:UserTransfer::email;

            menu ActorApp(Actor usr) {
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

        // Actor

        ClassType actor = apps.get(0).getActor();

        assertNotNull(actor);
        assertEquals("SecurityTestModel::Actor", actor.getName());
        assertEquals("Actor", actor.getSimpleName());

        // Principal

        ClassType principal = apps.get(0).getPrincipal();

        assertNotNull(principal);
        assertEquals("SecurityTestModel::UserTransfer", principal.getName());
        assertEquals("UserTransfer", principal.getSimpleName());
        assertTrue(principal.isIsPrincipal());

        // Claim

        assertEquals(1, authentication.getClaims().size());
        assertEquals("UNDEFINED", authentication.getClaims().get(0).getType().getName());
        assertEquals(principal.getAttributes().stream().filter(a -> a.getName().equals("email")).findFirst().orElse(null), authentication.getClaims().get(0).getAttributeType());

    }
}
