package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.*;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.meta.ui.support.UiModelResourceSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for JSL2UI Zeta view rules (Phase 7).
 * Tests view declarations, forms, groups, tabs, and widget rules.
 */
public class Jsl2UiZetaViewTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiZetaViewTest.class);

    private UiModelResourceSupport executeTransformation(String modelName, String jslContent) throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(modelName, List.of(jslContent));
        assertTrue(jslModel.isValid(), "JSL model should be valid");

        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();

        Jsl2UiZetaTransformation.builder()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .defaultModelName(jslModel.getName())
                .build()
                .execute();

        return UiModelResourceSupport
                .uiModelResourceSupportBuilder()
                .resourceSet(uiModel.getResourceSet())
                .build();
    }

    @Test
    void testViewPageContainer() throws Exception {
        UiModelResourceSupport ui = executeTransformation("ViewTest", """
                model ViewTest;

                import judo::types;

                type numeric Numeric scale:0 precision:9;
                type string String min-size:0 max-size:255;

                widget string StringWidget;

                entity Person {
                    identifier String email required;
                    field String firstName;
                }

                transfer PersonTransfer(Person p) {
                    field String email <= p.email;
                    field String firstName <= p.firstName;

                    event create onCreate;
                    event update onUpdate;
                    event delete onDelete;
                }

                row PersonRow(PersonTransfer p) {
                    column String email <= p.email;
                }

                view PersonView(PersonTransfer u) {
                    group main label:"Main" {
                        widget StringWidget email <= u.email label:"Email";
                        widget StringWidget firstName <= u.firstName label:"First Name";
                    }
                }

                actor TestActor {
                    access PersonTransfer[] persons <= Person.all() create delete update;
                }

                frontend TestApp(TestActor a)
                    menu: {
                        table PersonRow[] persons <= a.persons label:"Persons" view:PersonView;
                    };
                """);

        // Verify Application exists
        List<Application> apps = ui.getStreamOfUiApplication().toList();
        assertEquals(1, apps.size(), "Should have exactly one Application");

        Application app = apps.get(0);

        // Verify PageContainers were created
        List<PageContainer> pageContainers = app.getPageContainers();
        log.info("PageContainers found: {}", pageContainers.size());
        for (PageContainer pc : pageContainers) {
            log.info("  PageContainer: name={}, type={}, label={}", pc.getName(), pc.getType(), pc.getLabel());
        }

        // Should have VIEW PageContainer for PersonView
        assertTrue(pageContainers.stream().anyMatch(
                pc -> pc.getType() == PageContainerType.VIEW && pc.getName() != null
                        && pc.getName().contains("PersonView")),
                "Should have a VIEW PageContainer for PersonView");

        // Verify the VIEW PageContainer has children (visual elements)
        PageContainer viewPc = pageContainers.stream()
                .filter(pc -> pc.getType() == PageContainerType.VIEW && pc.getName() != null
                        && pc.getName().contains("PersonView"))
                .findFirst()
                .orElseThrow();

        assertFalse(viewPc.getChildren().isEmpty(), "VIEW PageContainer should have children");

        // Verify it has action button groups
        assertFalse(viewPc.getActionButtonGroups().isEmpty(),
                "VIEW PageContainer should have action button groups");

        // Verify dataElement is set
        assertNotNull(viewPc.getDataElement(), "PageContainer should have dataElement set");
    }

    @Test
    void testViewWidgets() throws Exception {
        UiModelResourceSupport ui = executeTransformation("WidgetTest", """
                model WidgetTest;

                import judo::types;

                type numeric Numeric scale:0 precision:9;
                type string String min-size:0 max-size:255;
                type boolean Bool;
                type date MyDate;
                type time MyTime;
                type timestamp MyTimestamp;

                widget string StringWidget;
                widget numeric NumericWidget;
                widget boolean BooleanWidget;
                widget date DateWidget;
                widget time TimeWidget;
                widget timestamp TimestampWidget;

                entity Item {
                    identifier String name required;
                    field Numeric price;
                    field Bool active;
                    field MyDate createdDate;
                    field MyTime createdTime;
                    field MyTimestamp createdAt;
                }

                transfer ItemTransfer(Item i) {
                    field String name <= i.name;
                    field Numeric price <= i.price;
                    field Bool active <= i.active;
                    field MyDate createdDate <= i.createdDate;
                    field MyTime createdTime <= i.createdTime;
                    field MyTimestamp createdAt <= i.createdAt;

                    event update onUpdate;
                }

                row ItemRow(ItemTransfer i) {
                    column String name <= i.name;
                }

                view ItemView(ItemTransfer u) {
                    group details label:"Details" {
                        widget StringWidget name <= u.name label:"Name";
                        widget NumericWidget price <= u.price label:"Price";
                        widget BooleanWidget active <= u.active label:"Active";
                        widget DateWidget createdDate <= u.createdDate label:"Date";
                        widget TimeWidget createdTime <= u.createdTime label:"Time";
                        widget TimestampWidget createdAt <= u.createdAt label:"Timestamp";
                    }
                }

                actor TestActor {
                    access ItemTransfer[] items <= Item.all() update;
                }

                frontend TestApp(TestActor a)
                    menu: {
                        table ItemRow[] items <= a.items label:"Items" view:ItemView;
                    };
                """);

        // Verify various widget types were created
        List<TextInput> textInputs = ui.getStreamOfUiTextInput().toList();
        log.info("TextInputs found: {}", textInputs.size());
        for (TextInput ti : textInputs) {
            log.info("  TextInput: name={}", ti.getName());
        }
        assertTrue(textInputs.stream().anyMatch(ti -> "name".equals(ti.getName())),
                "Should have TextInput for 'name'");

        List<NumericInput> numericInputs = ui.getStreamOfUiNumericInput().toList();
        log.info("NumericInputs found: {}", numericInputs.size());
        assertTrue(numericInputs.stream().anyMatch(ni -> "price".equals(ni.getName())),
                "Should have NumericInput for 'price'");

        List<TrinaryLogicCombo> booleanCombos = ui.getStreamOfUiTrinaryLogicCombo().toList();
        log.info("TrinaryLogicCombos found: {}", booleanCombos.size());
        assertTrue(booleanCombos.stream().anyMatch(tc -> "active".equals(tc.getName())),
                "Should have TrinaryLogicCombo for 'active'");

        List<DateInput> dateInputs = ui.getStreamOfUiDateInput().toList();
        log.info("DateInputs found: {}", dateInputs.size());
        assertTrue(dateInputs.stream().anyMatch(di -> "createdDate".equals(di.getName())),
                "Should have DateInput for 'createdDate'");

        List<TimeInput> timeInputs = ui.getStreamOfUiTimeInput().toList();
        log.info("TimeInputs found: {}", timeInputs.size());
        assertTrue(timeInputs.stream().anyMatch(ti -> "createdTime".equals(ti.getName())),
                "Should have TimeInput for 'createdTime'");

        List<DateTimeInput> dateTimeInputs = ui.getStreamOfUiDateTimeInput().toList();
        log.info("DateTimeInputs found: {}", dateTimeInputs.size());
        assertTrue(dateTimeInputs.stream().anyMatch(dti -> "createdAt".equals(dti.getName())),
                "Should have DateTimeInput for 'createdAt'");
    }

    @Test
    void testViewGroupAndTabs() throws Exception {
        UiModelResourceSupport ui = executeTransformation("GroupTabTest", """
                model GroupTabTest;

                import judo::types;

                type string String min-size:0 max-size:255;

                widget string StringWidget;

                entity Employee {
                    identifier String name required;
                    field String email;
                    field String phone;
                    field String address;
                }

                transfer EmployeeTransfer(Employee e) {
                    field String name <= e.name;
                    field String email <= e.email;
                    field String phone <= e.phone;
                    field String address <= e.address;

                    event update onUpdate;
                }

                row EmployeeRow(EmployeeTransfer e) {
                    column String name <= e.name;
                }

                view EmployeeView(EmployeeTransfer u) {
                    group personal label:"Personal Info" {
                        widget StringWidget name <= u.name label:"Name";
                        widget StringWidget email <= u.email label:"Email";
                    }
                    tabs contactTabs label:"Contact" {
                        group phoneGroup label:"Phone" {
                            widget StringWidget phone <= u.phone label:"Phone";
                        }
                        group addressGroup label:"Address" {
                            widget StringWidget address <= u.address label:"Address";
                        }
                    }
                }

                actor TestActor {
                    access EmployeeTransfer[] employees <= Employee.all() update;
                }

                frontend TestApp(TestActor a)
                    menu: {
                        table EmployeeRow[] employees <= a.employees label:"Employees" view:EmployeeView;
                    };
                """);

        // Verify Flex elements (groups) were created
        List<Flex> flexes = ui.getStreamOfUiFlex().toList();
        log.info("Flex elements found: {}", flexes.size());
        for (Flex f : flexes) {
            log.info("  Flex: name={}, label={}, direction={}", f.getName(), f.getLabel(), f.getDirection());
        }

        // Should have Flex for "personal" group
        assertTrue(flexes.stream().anyMatch(f -> "personal".equals(f.getName())),
                "Should have Flex for 'personal' group");

        // Verify TabController was created
        List<TabController> tabControllers = ui.getStreamOfUiTabController().toList();
        log.info("TabControllers found: {}", tabControllers.size());
        for (TabController tc : tabControllers) {
            log.info("  TabController: name={}, tabs={}", tc.getName(), tc.getTabs().size());
        }

        assertTrue(tabControllers.stream().anyMatch(tc -> "contactTabs".equals(tc.getName())),
                "Should have TabController for 'contactTabs'");

        // Verify Tab elements for tab groups
        List<Tab> tabs = ui.getStreamOfUiTab().toList();
        log.info("Tabs found: {}", tabs.size());
        for (Tab tab : tabs) {
            log.info("  Tab: name={}", tab.getName());
        }

        assertTrue(tabs.size() >= 2, "Should have at least 2 Tab elements (phoneGroup, addressGroup)");
    }

    @Test
    void testFormPageContainer() throws Exception {
        UiModelResourceSupport ui = executeTransformation("FormTest", """
                model FormTest;

                import judo::types;

                type string String min-size:0 max-size:255;

                widget string StringWidget;

                entity Task {
                    identifier String taskName required;
                    field String description;
                }

                transfer TaskTransfer(Task t) {
                    field String taskName <= t.taskName;
                    field String description <= t.description;

                    event create onCreate;
                    event update onUpdate;
                    event delete onDelete;
                }

                row TaskRow(TaskTransfer t) {
                    column String taskName <= t.taskName;
                }

                view TaskView(TaskTransfer u) {
                    group main label:"Main" {
                        widget StringWidget taskName <= u.taskName label:"Task Name";
                        widget StringWidget description <= u.description label:"Description";
                    }
                }

                form TaskForm(TaskTransfer u) {
                    group main label:"Create Task" {
                        widget StringWidget taskName <= u.taskName label:"Task Name";
                        widget StringWidget description <= u.description label:"Description";
                    }
                }

                actor TestActor {
                    access TaskTransfer[] tasks <= Task.all() create delete update;
                }

                frontend TestApp(TestActor a)
                    menu: {
                        table TaskRow[] tasks <= a.tasks label:"Tasks" view:TaskView form:TaskForm;
                    };
                """);

        List<Application> apps = ui.getStreamOfUiApplication().toList();
        assertEquals(1, apps.size());
        Application app = apps.get(0);

        List<PageContainer> pageContainers = app.getPageContainers();
        log.info("PageContainers: {}", pageContainers.size());
        for (PageContainer pc : pageContainers) {
            log.info("  PageContainer: name={}, type={}", pc.getName(), pc.getType());
        }

        // Should have FORM PageContainer for TaskForm
        assertTrue(pageContainers.stream().anyMatch(
                pc -> pc.getType() == PageContainerType.FORM && pc.getName() != null
                        && pc.getName().contains("TaskForm")),
                "Should have a FORM PageContainer for TaskForm");

        // Should have VIEW PageContainer for TaskView
        assertTrue(pageContainers.stream().anyMatch(
                pc -> pc.getType() == PageContainerType.VIEW && pc.getName() != null
                        && pc.getName().contains("TaskView")),
                "Should have a VIEW PageContainer for TaskView");

        // Verify FORM PageContainer has children and action groups
        PageContainer formPc = pageContainers.stream()
                .filter(pc -> pc.getType() == PageContainerType.FORM)
                .findFirst()
                .orElseThrow();
        assertFalse(formPc.getChildren().isEmpty(), "FORM PageContainer should have children");
        assertFalse(formPc.getActionButtonGroups().isEmpty(),
                "FORM PageContainer should have action button groups");
    }

    @Test
    void testInlineViewTable() throws Exception {
        UiModelResourceSupport ui = executeTransformation("InlineTableTest", """
                model InlineTableTest;

                import judo::types;

                type string String min-size:0 max-size:255;

                widget string StringWidget;

                entity Order {
                    identifier String orderNumber required;
                    relation OrderItem[] items;
                }

                entity OrderItem {
                    identifier String itemName required;
                    field String description;
                }

                transfer OrderItemTransfer(OrderItem oi) {
                    field String itemName <= oi.itemName;
                    field String description <= oi.description;

                    event update onUpdate;
                    event delete onDelete;
                }

                transfer OrderTransfer(Order o) {
                    field String orderNumber <= o.orderNumber;
                    relation OrderItemTransfer[] items <= o.items update delete;

                    event update onUpdate;
                }

                row OrderItemRow(OrderItemTransfer oi) {
                    column String itemName <= oi.itemName;
                    column String description <= oi.description;
                }

                view OrderItemView(OrderItemTransfer u) {
                    group main label:"Details" {
                        widget StringWidget itemName <= u.itemName label:"Item Name";
                    }
                }

                view OrderView(OrderTransfer u) {
                    group info label:"Order Info" {
                        widget StringWidget orderNumber <= u.orderNumber label:"Order Number";
                    }
                    table OrderItemRow[] items <= u.items label:"Items" view:OrderItemView;
                }

                row OrderRow(OrderTransfer o) {
                    column String orderNumber <= o.orderNumber;
                }

                actor TestActor {
                    access OrderTransfer[] orders <= Order.all() update;
                }

                frontend TestApp(TestActor a)
                    menu: {
                        table OrderRow[] orders <= a.orders label:"Orders" view:OrderView;
                    };
                """);

        // Verify Table was created
        List<Table> tables = ui.getStreamOfUiTable().toList();
        log.info("Tables found: {}", tables.size());
        for (Table t : tables) {
            log.info("  Table: name={}, columns={}, relationName={}", t.getName(),
                    t.getColumns().size(), t.getRelationName());
        }

        // Should have an inline table for items
        assertTrue(tables.stream().anyMatch(t -> "items".equals(t.getName())),
                "Should have Table for 'items'");

        Table itemsTable = tables.stream()
                .filter(t -> "items".equals(t.getName()))
                .findFirst()
                .orElseThrow();

        // Should have columns from the row declaration
        assertTrue(itemsTable.getColumns().size() >= 2,
                "Items table should have at least 2 columns (itemName, description)");

        // Should have button groups
        assertNotNull(itemsTable.getTableActionButtonGroup(),
                "Table should have table action button group");
        assertNotNull(itemsTable.getRowActionButtonGroup(),
                "Table should have row action button group");
    }

    @Test
    void testInlineViewLink() throws Exception {
        UiModelResourceSupport ui = executeTransformation("InlineLinkTest", """
                model InlineLinkTest;

                import judo::types;

                type string String min-size:0 max-size:255;

                widget string StringWidget;

                entity Customer {
                    identifier String name required;
                }

                entity Project {
                    identifier String projectName required;
                    relation Customer customer;
                }

                transfer CustomerTransfer(Customer c) {
                    field String name <= c.name;

                    event update onUpdate;
                }

                transfer ProjectTransfer(Project p) {
                    field String projectName <= p.projectName;
                    relation CustomerTransfer customer <= p.customer;

                    event update onUpdate;
                }

                view CustomerView(CustomerTransfer u) {
                    group main label:"Customer" {
                        widget StringWidget name <= u.name label:"Name";
                    }
                }

                view ProjectView(ProjectTransfer u) {
                    group info label:"Project Info" {
                        widget StringWidget projectName <= u.projectName label:"Project Name";
                    }
                    link CustomerView customer <= u.customer label:"Customer";
                }

                row ProjectRow(ProjectTransfer p) {
                    column String projectName <= p.projectName;
                }

                actor TestActor {
                    access ProjectTransfer[] projects <= Project.all() update;
                }

                frontend TestApp(TestActor a)
                    menu: {
                        table ProjectRow[] projects <= a.projects label:"Projects" view:ProjectView;
                    };
                """);

        // Verify Link was created
        List<Link> links = ui.getStreamOfUiLink().toList();
        log.info("Links found: {}", links.size());
        for (Link l : links) {
            log.info("  Link: name={}, relationName={}, label={}", l.getName(), l.getRelationName(), l.getLabel());
        }

        assertTrue(links.stream().anyMatch(l -> "customer".equals(l.getName())),
                "Should have Link for 'customer'");

        Link customerLink = links.stream()
                .filter(l -> "customer".equals(l.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals("customer", customerLink.getRelationName(), "Link relationName should be 'customer'");
        assertNotNull(customerLink.getActionButtonGroup(), "Link should have action button group");
        assertFalse(customerLink.getParts().isEmpty(), "Link should have representation column parts");

        // Verify autocomplete action definitions
        assertNotNull(customerLink.getAutocompleteRangeActionDefinition(),
                "Link should have autocomplete range action definition");
        assertNotNull(customerLink.getAutocompleteSetActionDefinition(),
                "Link should have autocomplete set action definition");
    }
}
