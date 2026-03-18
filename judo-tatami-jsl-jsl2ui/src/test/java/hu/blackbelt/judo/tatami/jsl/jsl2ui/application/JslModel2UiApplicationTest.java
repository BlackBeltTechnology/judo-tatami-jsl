package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.DataType;
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

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"})
    void testActors(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("ApplicationTestModel", List.of("""
            model ApplicationTestModel;

            actor AppActor;

            frontend AppMenu(AppActor a);
        """));

        transform(mode);

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

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class)
    void testMenu(TransformationMode mode) throws Exception {
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

            row UsersRow(UserTransfer u) {
                column String userName <= u.userName label:"Username";
            }

            row ProductsRow(ProductTransfer p) {
                column String name <= p.name label:"Name";
                column String price <= p.price label:"Price";
            }

            actor Actor {
                access ProductTransfer[] products <= Product.all();
                access ProductTransfer[] products2 <= Product.all();
                access UserTransfer[] users <= User.all();
            }

            frontend MenuActor(Actor usr)
                menu: {
                    group first label:"Group1" {
                        group second label:"Group2" {
                            table ProductsRow[] products <= usr.products label:"Products" icon:"close";
                        }
                        table ProductsRow[] products2 <= usr.products2 label:"Products2";
                    }
                    table UsersRow[] users <= usr.users label:"Users" icon:"account-multiple";
                }
                title: "Yayy, JSL!";
        """));

        transform(mode);

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);

        NavigationController navigationController = app1.getNavigationController();

        PageDefinition dashboardPage = app1.getPages().stream().filter(page -> page.getFQName().equals("Actor::MenuTestModel::MenuActor::DashboardPage")).findFirst().orElseThrow();

        assertTrue(dashboardPage.isDashboard());
        assertEquals("MenuActor/(jsl/MenuTestModel/MenuActor)/EmptyDashboardPageDefinition", getXMIID(dashboardPage));

        assertEquals("Yayy, JSL!", app1.getTitle());
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

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class)
    void testMultipleActors(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("MultipleActorsTestModel", List.of("""
            model MultipleActorsTestModel;

            import judo::types;

            widget string StringWidget;

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
                widget StringWidget name <= product.name;
                widget StringWidget price <= product.price;
            }

            view Product2View(Product2Transfer product2) {
                widget StringWidget name2 <= product2.name2;
                widget StringWidget price2 <= product2.price2;
            }

            row ProductsRow(ProductTransfer product) {
                column String name <= product.name label:"Name";
                column String price <= product.price label:"Price";
            }

            row ProductsRow2(Product2Transfer product2) {
                column String name2 <= product2.name2 label:"Name 2";
                column String price2 <= product2.price2 label:"Price 2";
            }

            actor Actor1 {
                access ProductTransfer[] products <= Product.all() update;
            }

            actor Actor2 {
                access Product2Transfer[] products2 <= Product2.all() update;
            }

            frontend App1(Actor1 a)
                menu: {
                    group first label:"Group1" {
                        table ProductsRow[] products1 <= a.products label:"Products1" view:ProductView;
                    }
                    table ProductsRow[] allProducts <= a.products label:"All Products" icon:"tools" view:ProductView;
                };

            frontend App2(Actor2 a)
                menu: {
                    group first label:"Group2" {
                        table ProductsRow2[] products2 <= a.products2 label:"Products2" view:Product2View;
                    }
                    table ProductsRow2[] allProducts2 <= a.products2 label:"All Products 2" icon:"tools" view:Product2View dashboard;
                };
        """));

        transform(mode);

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

        List<String> class1Names = classTypes.stream().map(c -> c.getName()).sorted().toList();
        List<String> relations1Names = relationTypes.stream().map(c -> c.getFQName()).sorted().toList();

        assertEquals(List.of(
                "MultipleActorsTestModel::Actor1",
                "MultipleActorsTestModel::ProductTransfer"
        ), class1Names);

        assertEquals(List.of(
                "Actor1::MultipleActorsTestModel::Actor1::products"
        ), relations1Names);

        List<DataType> dataTypes1 = app1.getDataTypes();

        List<String> dataTypes1Names = dataTypes1.stream().map(c -> c.getName()).sorted().toList();

        assertEquals(List.of(
                "BooleanOperation",
                "EnumerationOperation",
                "Integer",
                "NumericOperation",
                "String",
                "StringOperation"
        ), dataTypes1Names);

        List<ClassType> classTypes2 = app2.getClassTypes();
        List<RelationType> relationsTypes2 = app2.getRelationTypes();

        assertEquals(2, classTypes2.size());
        assertEquals(1, relationsTypes2.size());

        List<String> class2Names = classTypes2.stream().map(c -> c.getName()).sorted().toList();
        List<String> relations2Names = relationsTypes2.stream().map(c -> c.getFQName()).sorted().toList();

        assertEquals(List.of(
                "MultipleActorsTestModel::Actor2",
                "MultipleActorsTestModel::Product2Transfer"
        ), class2Names);

        assertEquals(List.of(
                "Actor2::MultipleActorsTestModel::Actor2::products2"
        ), relations2Names);

        // Pages

        List<PageDefinition> pages = app1.getPages();

        assertEquals(List.of(
                "MultipleActorsTestModel::App1::DashboardPage",
                "MultipleActorsTestModel::App1::allProducts::AccessTablePage",
                "MultipleActorsTestModel::App1::allProducts::AccessTableViewPage",
                "MultipleActorsTestModel::App1::first::products1::AccessTablePage",
                "MultipleActorsTestModel::App1::first::products1::AccessTableViewPage"
        ), pages.stream().map(NamedElement::getName).sorted().toList());

        List<PageDefinition> pages2 = app2.getPages();

        assertEquals(List.of(
                "MultipleActorsTestModel::App2::allProducts2::AccessTablePage",
                "MultipleActorsTestModel::App2::allProducts2::AccessTableViewPage",
                "MultipleActorsTestModel::App2::first::products2::AccessTablePage",
                "MultipleActorsTestModel::App2::first::products2::AccessTableViewPage"
        ), pages2.stream().map(NamedElement::getName).sorted().toList());

        PageDefinition dashboard2Page = app2.getPages().stream().filter(page -> page.getFQName().equals("Actor2::MultipleActorsTestModel::App2::allProducts2::AccessTablePage")).findFirst().orElseThrow();

        assertTrue(dashboard2Page.isDashboard());
        assertEquals("App2/(jsl/MultipleActorsTestModel/App2/allProducts2)/AccessTablePageDefinition", getXMIID(dashboard2Page));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class)
    void testSecurity(TransformationMode mode) throws Exception {
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

            frontend ActorApp(Actor usr);
        """));

        transform(mode);

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

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"})
    void testMenuStackOverFlow(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("StackOverFlowTestModel", List.of("""
        model StackOverFlowTestModel;
        
        import judo::types;
        
        widget string StringWidget;
        
        entity A {
            field String name;
            field B[] bs;
        }
        
        entity B {
            field C[] cs;
        }
        
        entity C {
            relation D[] ds opposite: c;
        }
        
        entity D {
            relation C c opposite: ds;
        }
        
        transfer ATransfer(A a) {
            field String name <=> a.name;
            relation BTransfer[] bs <= a.bs eager:true create:true delete:true update:true;
        
            event create createOn;
            event update updateOn;
            event delete deleteOn;
        }
        
        transfer BTransfer(B b) {
            relation CTransfer[] cs <= b.cs eager:true create:true delete:true update:true;
        
            event create createOn;
            event update updateOn;
            event delete deleteOn;
        }
        
        transfer CTransfer(C c) {
            relation DTransfer[] ds <= c.ds eager:true create:true delete:true update:true;
        
            event create createOn;
            event update updateOn;
            event delete deleteOn;
        }
        
        transfer DTransfer(D d) {
            relation CTransfer c <= d.c create:true delete:true update:true;
        
            event create createOn;
            event update updateOn;
            event delete deleteOn;
        }
        
        form AForm(ATransfer a) {
            widget StringWidget name <= a.name icon: "atom-variant";
        }
        
        view AView(ATransfer a) {
            widget StringWidget name <= a.name;
        }
        
        row ARow(ATransfer a) {
            column String name <= a.name;
        }
        
        // Actor
        
        actor Actor {
            access ATransfer[] `as` <= A.all() create delete update;
        }
        
        frontend actorApp(Actor act)
            menu: {
                table ARow[] asTable <= act.`as` form:AForm view:AView; // this cause the stackoverflow
            };
        """));

        transform(mode);

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);

    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = TransformationMode.class, names = {"ETL", "ZETA"})
    void testProfile(TransformationMode mode) throws Exception {
        jslModel = JslParser.getModelFromStrings("ProfileModel", List.of("""
            model ProfileModel;

            import judo::types;

            widget string StringWidget;

            entity User {
                identifier String userName required;
            }

            entity Product {
                identifier String name required;
                field Integer price required;
            }

            transfer UserTransfer(User u) {
                field String userName <= u.userName;

                event update onUpdate;
            }

            transfer ProductTransfer(Product p) {
                field String name required;

                event update onUpdate;
            }

            view UserView(UserTransfer u) {
                widget StringWidget userName <= u.userName;
            }

            view ProductView(ProductTransfer product) {
                widget StringWidget name <= product.name;
            }

            row ProductsRow(ProductTransfer product) {
                column String name <= product.name label:"Name";
            }

            actor Actor1 {
                access ProductTransfer[] products <= Product.all() update;
                access UserTransfer user <= User.any() update;
            }

            frontend App1(Actor1 a)
                menu: {
                    table ProductsRow[] allProducts <= a.products label:"All Products" icon:"tools" view:ProductView;
                }
                profile: {
                    link UserView myProfile <= a.user label:"My Profile";
                };
        """));

        transform(mode);

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application app1 = apps.get(0);

        assertEquals(List.of(
                "Actor1::ProfileModel::App1::DashboardPage",
                "Actor1::ProfileModel::App1::allProducts::AccessTablePage",
                "Actor1::ProfileModel::App1::allProducts::AccessTableViewPage",
                "Actor1::ProfileModel::App1::myProfile::AccessViewPage"
        ), app1.getPages().stream().map(NamedElement::getFQName).sorted().toList());

        PageDefinition profilePage = app1.getPages().stream().filter(p -> p.getFQName().equals("Actor1::ProfileModel::App1::myProfile::AccessViewPage")).findFirst().orElseThrow();
        PageDefinition markedProfilePage = app1.getProfilePage();

        assertEquals(markedProfilePage, profilePage);
    }

}
