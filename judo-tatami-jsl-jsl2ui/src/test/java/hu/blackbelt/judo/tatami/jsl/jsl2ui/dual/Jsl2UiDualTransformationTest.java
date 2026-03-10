package hu.blackbelt.judo.tatami.jsl.jsl2ui.dual;

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
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiZetaTransformation;
import hu.blackbelt.judo.tatami.test.util.ModelComparator;
import hu.blackbelt.judo.tatami.test.util.ModelComparator.ComparisonMode;
import hu.blackbelt.judo.tatami.test.util.ModelComparator.ComparisonResult;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.Jsl2UiParameter.jsl2UiParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.executeJsl2UiTransformation;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Dual transformation test comparing ETL and Zeta outputs for JSL2UI.
 *
 * Runs the same JSL model through both ETL and Zeta transformations,
 * then compares the resulting UI models using ModelComparator in STRICT mode.
 */
public class Jsl2UiDualTransformationTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiDualTransformationTest.class);

    private static final String TARGET_TEST_CLASSES = "target/test-classes/dual";

    private static final String TYPES =
            "type string String min-size:0 max-size:255;\n" +
            "type numeric Integer precision:9 scale:0;\n" +
            "type boolean Boolean;\n" +
            "type date Date;\n" +
            "type timestamp Timestamp;\n" +
            "widget string StringWidget;\n" +
            "widget numeric NumericWidget;\n" +
            "widget boolean BooleanWidget;\n" +
            "widget date DateWidget;\n" +
            "widget timestamp TimestampWidget;\n" +
            "widget enum ComboWidget;\n";

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    private UiModel executeEtl(JslDslModel jslModel) throws Exception {
        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
        executeJsl2UiTransformation(jsl2UiParameter()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .createTrace(false));
        return uiModel;
    }

    private UiModel executeZeta(JslDslModel jslModel) {
        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();
        Jsl2UiZetaTransformation.builder()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .defaultModelName(jslModel.getName())
                .build()
                .execute();
        return uiModel;
    }

    private void assertModelsEquivalent(String testName, String modelName, String modelSource) throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings(modelName, List.of(modelSource));
        UiModel etlResult = executeEtl(jslModel);
        UiModel zetaResult = executeZeta(jslModel);

        ComparisonMode mode = ModelComparator.getConfiguredMode();
        log.info("DUAL TEST [{}]: comparison mode = {}", testName, mode);

        int etlElements = countElements(etlResult);
        int zetaElements = countElements(zetaResult);
        log.info("DUAL TEST [{}]: ETL={} elements, ZETA={} elements", testName, etlElements, zetaElements);

        EObject etlRoot = etlResult.getResourceSet().getResources().get(0).getContents().get(0);
        EObject zetaRoot = zetaResult.getResourceSet().getResources().get(0).getContents().get(0);

        ComparisonResult result = ModelComparator.compare(etlRoot, zetaRoot, mode);

        if (result.isEquivalent()) {
            log.info("DUAL TEST [{}]: EQUIVALENT", testName);
        } else {
            log.error("DUAL TEST [{}]: {} differences\n{}", testName,
                    result.getDifferenceCount(), result.getSummary());
            fail("ETL and ZETA UI models differ for " + testName + ":\n" + result.getDetailedReport());
        }
    }

    private int countElements(UiModel model) {
        int count = 0;
        for (var resource : model.getResourceSet().getResources()) {
            var iter = resource.getAllContents();
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
        }
        return count;
    }

    // ========== Test Cases ==========

    @Test
    void testSimpleActor() throws Exception {
        assertModelsEquivalent("simpleActor", "SimpleActorModel", """
                model SimpleActorModel;

                actor TestActor;

                frontend TestMenu(TestActor a);
        """);
    }

    @Test
    void testActorWithMenu() throws Exception {
        assertModelsEquivalent("actorWithMenu", "ActorWithMenuModel",
                "model ActorWithMenuModel;\n" +
                TYPES +
                "entity Person {\n" +
                "    field String name;\n" +
                "}\n" +
                "transfer PersonTransfer(Person p) {\n" +
                "    field String name <= p.name;\n" +
                "    event update onUpdate;\n" +
                "}\n" +
                "row PersonRow(PersonTransfer p) {\n" +
                "    column String name <= p.name;\n" +
                "}\n" +
                "view PersonView(PersonTransfer p) {\n" +
                "    group main {\n" +
                "        widget StringWidget name <= p.name;\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access PersonTransfer[] persons <= Person.all() update;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table PersonRow[] persons <= a.persons label:\"Persons\" view:PersonView;\n" +
                "    };\n");
    }

    @Disabled("JSL parser cannot parse TestActor#asActor() syntax - JslParseException")
    @Test
    void testActorWithMenuLink() throws Exception {
        assertModelsEquivalent("actorWithMenuLink", "ActorWithMenuLinkModel",
                "model ActorWithMenuLinkModel;\n" +
                TYPES +
                "actor TestActor human {\n" +
                "    field String name;\n" +
                "}\n" +
                "transfer TestActorTransfer(TestActor ta) {\n" +
                "    field String name <= ta.name;\n" +
                "}\n" +
                "view ProfileView(TestActorTransfer t) {\n" +
                "    group main {\n" +
                "        widget StringWidget name <= t.name;\n" +
                "    }\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        link ProfileView profile <= TestActor#asActor() label:\"Profile\";\n" +
                "    };\n");
    }

    @Test
    void testEntityWithCRUD() throws Exception {
        assertModelsEquivalent("entityWithCRUD", "CRUDModel",
                "model CRUDModel;\n" +
                TYPES +
                "entity Item {\n" +
                "    field String name;\n" +
                "    field Integer quantity;\n" +
                "}\n" +
                "transfer ItemTransfer(Item i) {\n" +
                "    field String name <= i.name;\n" +
                "    field Integer quantity <= i.quantity;\n" +
                "    event create onCreate;\n" +
                "    event update onUpdate;\n" +
                "    event delete onDelete;\n" +
                "}\n" +
                "row ItemRow(ItemTransfer i) {\n" +
                "    column String name <= i.name;\n" +
                "}\n" +
                "view ItemView(ItemTransfer i) {\n" +
                "    group main {\n" +
                "        widget StringWidget name <= i.name;\n" +
                "        widget NumericWidget quantity <= i.quantity;\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access ItemTransfer[] items <= Item.all() create update delete;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table ItemRow[] items <= a.items label:\"Items\" view:ItemView;\n" +
                "    };\n");
    }

    @Test
    void testRelationsAndNavigation() throws Exception {
        assertModelsEquivalent("relationsAndNavigation", "RelationsModel",
                "model RelationsModel;\n" +
                TYPES +
                "entity Category {\n" +
                "    field String name;\n" +
                "}\n" +
                "entity Product {\n" +
                "    field String name;\n" +
                "    relation Category category;\n" +
                "}\n" +
                "transfer CategoryTransfer(Category c) {\n" +
                "    field String name <= c.name;\n" +
                "    event update onUpdate;\n" +
                "}\n" +
                "transfer ProductTransfer(Product p) {\n" +
                "    field String name <= p.name;\n" +
                "    relation CategoryTransfer category <= p.category;\n" +
                "    event update onUpdate;\n" +
                "}\n" +
                "row ProductRow(ProductTransfer p) {\n" +
                "    column String name <= p.name;\n" +
                "}\n" +
                "view CategoryView(CategoryTransfer c) {\n" +
                "    group main {\n" +
                "        widget StringWidget name <= c.name;\n" +
                "    }\n" +
                "}\n" +
                "view ProductView(ProductTransfer p) {\n" +
                "    group main {\n" +
                "        widget StringWidget name <= p.name;\n" +
                "        link CategoryView category <= p.category icon:\"category\" label:\"Category\";\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access ProductTransfer[] products <= Product.all() update;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table ProductRow[] products <= a.products label:\"Products\" view:ProductView;\n" +
                "    };\n");
    }

    @Test
    void testWidgets() throws Exception {
        assertModelsEquivalent("widgets", "WidgetsModel",
                "model WidgetsModel;\n" +
                TYPES +
                "entity Record {\n" +
                "    field String name;\n" +
                "    field Integer count;\n" +
                "    field Boolean active;\n" +
                "    field Date created;\n" +
                "    field Timestamp modified;\n" +
                "}\n" +
                "transfer RecordTransfer(Record r) {\n" +
                "    field String name <= r.name;\n" +
                "    field Integer count <= r.count;\n" +
                "    field Boolean active <= r.active;\n" +
                "    field Date created <= r.created;\n" +
                "    field Timestamp modified <= r.modified;\n" +
                "    event update onUpdate;\n" +
                "}\n" +
                "row RecordRow(RecordTransfer r) {\n" +
                "    column String name <= r.name;\n" +
                "}\n" +
                "view RecordView(RecordTransfer r) {\n" +
                "    group details {\n" +
                "        widget StringWidget name <= r.name;\n" +
                "        widget NumericWidget count <= r.count;\n" +
                "        widget BooleanWidget active <= r.active;\n" +
                "        widget DateWidget created <= r.created;\n" +
                "        widget TimestampWidget modified <= r.modified;\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access RecordTransfer[] records <= Record.all() update;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table RecordRow[] records <= a.records label:\"Records\" view:RecordView;\n" +
                "    };\n");
    }

    @Test
    void testEnumTypes() throws Exception {
        assertModelsEquivalent("enumTypes", "EnumModel",
                "model EnumModel;\n" +
                TYPES +
                "enum Status {\n" +
                "    ACTIVE = 0;\n" +
                "    INACTIVE = 1;\n" +
                "    PENDING = 2;\n" +
                "}\n" +
                "entity Task {\n" +
                "    field String taskName;\n" +
                "    field Status status default:Status#ACTIVE;\n" +
                "}\n" +
                "transfer TaskTransfer(Task t) {\n" +
                "    field String taskName <= t.taskName;\n" +
                "    field Status status <= t.status;\n" +
                "    event update onUpdate;\n" +
                "}\n" +
                "row TaskRow(TaskTransfer t) {\n" +
                "    column String taskName <= t.taskName;\n" +
                "}\n" +
                "view TaskView(TaskTransfer t) {\n" +
                "    group main {\n" +
                "        widget StringWidget taskName <= t.taskName;\n" +
                "        widget ComboWidget status <= t.status;\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access TaskTransfer[] tasks <= Task.all() update;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table TaskRow[] tasks <= a.tasks label:\"Tasks\" view:TaskView;\n" +
                "    };\n");
    }

    @Test
    void testRowDeclarations() throws Exception {
        assertModelsEquivalent("rowDeclarations", "RowModel",
                "model RowModel;\n" +
                TYPES +
                "entity Person {\n" +
                "    field String name;\n" +
                "}\n" +
                "transfer PersonTransfer(Person p) {\n" +
                "    field String name <= p.name;\n" +
                "    event update onUpdate;\n" +
                "    event delete onDelete;\n" +
                "}\n" +
                "row PersonRow(PersonTransfer p) {\n" +
                "    column String name <= p.name;\n" +
                "}\n" +
                "view PersonView(PersonTransfer p) {\n" +
                "    group main {\n" +
                "        widget StringWidget name <= p.name;\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access PersonTransfer[] persons <= Person.all() update delete;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table PersonRow[] persons <= a.persons label:\"Persons\" view:PersonView;\n" +
                "    };\n");
    }

    @Test
    void testActionGroups() throws Exception {
        assertModelsEquivalent("actionGroups", "ActionGroupModel",
                "model ActionGroupModel;\n" +
                TYPES +
                "entity Item {\n" +
                "    field String name;\n" +
                "}\n" +
                "transfer ItemTransfer(Item i) {\n" +
                "    field String name <= i.name;\n" +
                "    action void myAction();\n" +
                "    event create onCreate;\n" +
                "    event update onUpdate;\n" +
                "}\n" +
                "row ItemRow(ItemTransfer i) {\n" +
                "    column String name <= i.name;\n" +
                "}\n" +
                "view ItemView(ItemTransfer i) {\n" +
                "    group main {\n" +
                "        widget StringWidget name <= i.name;\n" +
                "    }\n" +
                "    actions operations label:\"Operations\" {\n" +
                "        action void myAction() <= i.myAction label:\"My Action\";\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access ItemTransfer[] items <= Item.all() create update;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table ItemRow[] items <= a.items label:\"Items\" view:ItemView;\n" +
                "    };\n");
    }

    @Test
    void testTabs() throws Exception {
        assertModelsEquivalent("tabs", "TabsModel",
                "model TabsModel;\n" +
                TYPES +
                "entity Order {\n" +
                "    field String name;\n" +
                "    field String note;\n" +
                "}\n" +
                "transfer OrderTransfer(Order o) {\n" +
                "    field String name <= o.name;\n" +
                "    field String note <= o.note;\n" +
                "    event update onUpdate;\n" +
                "}\n" +
                "row OrderRow(OrderTransfer o) {\n" +
                "    column String name <= o.name;\n" +
                "}\n" +
                "view OrderView(OrderTransfer o) {\n" +
                "    tabs orderTabs {\n" +
                "        group basic label:\"Basic\" {\n" +
                "            widget StringWidget name <= o.name;\n" +
                "        }\n" +
                "        group advanced label:\"Advanced\" {\n" +
                "            widget StringWidget note <= o.note;\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "actor TestActor {\n" +
                "    access OrderTransfer[] orders <= Order.all() update;\n" +
                "}\n" +
                "frontend TestMenu(TestActor a)\n" +
                "    menu: {\n" +
                "        table OrderRow[] orders <= a.orders label:\"Orders\" view:OrderView;\n" +
                "    };\n");
    }
}
