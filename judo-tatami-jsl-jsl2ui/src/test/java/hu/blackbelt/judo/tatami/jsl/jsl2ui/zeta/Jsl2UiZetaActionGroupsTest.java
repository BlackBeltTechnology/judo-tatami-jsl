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
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.meta.ui.support.UiModelResourceSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Zeta tests for action groups.
 * Verifies action group ButtonGroup creation, custom labels/icons, and
 * inline table row/table action buttons.
 */
public class Jsl2UiZetaActionGroupsTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiZetaActionGroupsTest.class);

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

    private static String createModelString(String name) {
        return """
                    model %s;

                    import judo::types;

                    widget numeric NumericWidget;

                    entity Entity1 {
                        field Integer number;
                        relation Entity2[] entities;
                    }

                    entity Entity2 {
                        field Integer number;
                    }

                    transfer Transfer1(Entity1 e1) {
                        field Integer number <=> e1.number;

                        action void myAction1();
                        action Transfer1 myAction2(Transfer1 input choices:Entity1.all());
                        action Transfer2 myAction3(TransferX input);

                        event create createT1;
                        event update updateT1;

                        relation Transfer2[] list2 <= e1.entities;
                    }

                    transfer Transfer2(Entity2 e2) {
                        field Integer number <=> e2.number;

                        action void someAction();
                    }

                    transfer TransferX {
                        field Integer number;
                    }

                    form Form1(Transfer1 t1) {
                        widget NumericWidget number <=> t1.number;
                    }

                    form FormX(TransferX tx) {
                        widget NumericWidget number <=> tx.number;
                    }

                    view View1(Transfer1 t1) {
                        widget NumericWidget number <=> t1.number;

                        action void myAction1() <= t1.myAction1 label:"my action 1";
                        action View1 myAction2(View1 input selector:Table1) <= t1.myAction2  label:"my action 2";
                        action View2 myAction3(FormX input) <= t1.myAction3 label:"my action 3";

                        actions myActions label:"hello" icon:"bello" {
                            action View1 myAction4(View1 input selector:Table1) <= t1.myAction2  label:"my action 4";
                            action View2 myAction5(FormX input) <= t1.myAction3 label:"my action 5" icon:"horse";
                        }

                        table Table2[] table2 <= t1.list2
                            actions: {
                                action View1 myAction6(View1 input selector:Table1) <= t1.myAction2  label:"my action 6";
                            };
                    }

                    view View2(Transfer2 t2) {
                        widget NumericWidget number <=> t2.number;
                    }

                    row Table1(Transfer1 t1) {
                        column Integer number <= t1.number;
                    }

                    row Table2(Transfer2 t2) {
                        column Integer number <= t2.number;

                        action void someAction() <= t2.someAction label:"some action";
                    }

                    actor A {
                        access Transfer1[] t1s <= Entity1.all() create update;
                    }

                    frontend M(A a)
                        menu: {
                            table Table1[] v1s <= a.t1s view:View1 form:Form1;
                        };
                """.formatted(name);
    }

    @Test
    void testActionGroupStructure() throws Exception {
        UiModelResourceSupport ui = executeTransformation("ActionGroups",
                createModelString("ActionGroups"));

        List<Application> apps = ui.getStreamOfUiApplication().toList();
        assertEquals(1, apps.size());
        Application app = apps.get(0);

        List<PageContainer> containers = app.getPageContainers();
        log.info("PageContainers: {}", containers.size());
        for (PageContainer pc : containers) {
            log.info("  Container: fqName={}", pc.getFQName());
        }

        // Verify View1 container exists
        PageContainer view1Container = containers.stream()
                .filter(c -> c.getFQName() != null && c.getFQName().contains("View1"))
                .findFirst().orElseThrow();

        // Verify top-level Flex has children
        Flex topFlex = (Flex) view1Container.getChildren().get(0);
        log.info("View1 top flex children: {}", topFlex.getChildren().size());
        for (VisualElement child : topFlex.getChildren()) {
            log.info("  Child: name={}, type={}", child.getName(), child.getClass().getSimpleName());
        }

        // Verify action group "myActions" exists as ButtonGroup
        ButtonGroup myActions = null;
        for (VisualElement child : topFlex.getChildren()) {
            if (child instanceof ButtonGroup && "myActions".equals(child.getName())) {
                myActions = (ButtonGroup) child;
                break;
            }
        }
        assertNotNull(myActions, "Action group 'myActions' should exist as ButtonGroup");
        assertEquals("hello", myActions.getLabel(), "Action group should have label 'hello'");
        assertNotNull(myActions.getIcon(), "Action group should have an icon");
        assertEquals("bello", myActions.getIcon().getIconName(), "Action group icon should be 'bello'");

        // Verify buttons inside action group
        log.info("myActions buttons: {}", myActions.getButtons().size());
        for (Button b : myActions.getButtons()) {
            log.info("  Button: name={}, label={}, icon={}", b.getName(), b.getLabel(),
                    b.getIcon() != null ? b.getIcon().getIconName() : null);
        }

        Button myAction4 = myActions.getButtons().stream()
                .filter(b -> "myAction4".equals(b.getName()))
                .findFirst().orElseThrow();
        assertEquals("my action 4", myAction4.getLabel());
        assertNull(myAction4.getIcon(), "myAction4 should have no icon");

        Button myAction5 = myActions.getButtons().stream()
                .filter(b -> "myAction5".equals(b.getName()))
                .findFirst().orElseThrow();
        assertEquals("my action 5", myAction5.getLabel());
        assertNotNull(myAction5.getIcon(), "myAction5 should have an icon");
        assertEquals("horse", myAction5.getIcon().getIconName());

        // Verify inline table "table2" exists
        Table table2 = (Table) view1Container.getTables().stream()
                .filter(t -> ((Table) t).getName().equals("table2"))
                .findFirst().orElseThrow();

        // Row button group should have someAction
        assertNotNull(table2.getRowActionButtonGroup(), "table2 should have row button group");
        assertTrue(table2.getRowActionButtonGroup().getButtons().stream()
                        .anyMatch(b -> "someAction".equals(b.getName())),
                "table2 should have 'someAction' row button");

        Button someActionButton = table2.getRowActionButtonGroup().getButtons().stream()
                .filter(b -> "someAction".equals(b.getName()))
                .findFirst().orElseThrow();
        assertTrue(someActionButton.getActionDefinition().getIsCallOperationAction(),
                "someAction should be a call operation action");
        assertEquals("text", someActionButton.getButtonStyle(),
                "Row action button style should be 'text'");

        // Table button group should have myAction6, Filter, Refresh
        assertNotNull(table2.getTableActionButtonGroup(), "table2 should have table button group");
        assertTrue(table2.getTableActionButtonGroup().getButtons().stream()
                        .anyMatch(b -> "myAction6".equals(b.getName())),
                "table2 should have 'myAction6' table button");

        Button myAction6 = table2.getTableActionButtonGroup().getButtons().stream()
                .filter(b -> "myAction6".equals(b.getName()))
                .findFirst().orElseThrow();
        assertTrue(myAction6.getActionDefinition().getIsOpenOperationInputSelectorAction(),
                "myAction6 should be an open selector action");
        assertEquals("text", myAction6.getButtonStyle(),
                "Table action button style should be 'text'");
    }
}
