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
 * Zeta tests for row operations.
 * Verifies table page containers, row/table button groups, inline table row actions,
 * and view action declarations.
 */
public class Jsl2UiZetaRowOperationsTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiZetaRowOperationsTest.class);

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
                         relation Entity2[] relatedCollection;
                     }

                     entity Entity2 {
                         field String name;
                     }

                     transfer Transfer1(Entity1 e1) {
                     	 field Integer number <=> e1.number;

                     	 event create onCreate;
                         event update onUpdate;
                         event delete onDelete;
                     }

                     transfer Transfer3(Entity2 e2) {
                     	 field String name <=> e2.name;
                     	 action void myAction4();

                         event create onCreate;
                         event update onUpdate;
                         event delete onDelete;
                     }

                     view View1(Transfer1 t1) {
                     	widget NumericWidget number <=> t1.number;
                     }

                     row Row1(Transfer1 t1) {
                     	column Integer number <= t1.number;
                     }

                     row Row3(Transfer3 t3) {
                     	column String name <= t3.name;
                     	action void myAction4() <= t3.myAction4 label:"my action 4";
                     }

                     transfer Transfer2 {
                         field Integer number;
                     }

                     view View2(Transfer2 t2) {
                         widget NumericWidget number <=> t2.number;
                     }

                     form Form2(Transfer2 t2) {
                         widget NumericWidget number <=> t2.number;
                     }

                     transfer TransferX(Entity1 e1) {
                     	field Integer number <=> e1.number;
                     	relation Transfer3[] relatedCollection <= e1.relatedCollection create update;

                     	action void myAction1();
                     	action Transfer1 myAction2(Transfer1 input choices:Entity1.all());
                     	action Transfer2 myAction3(Transfer2 input);

                     	event create createTX;
                     	event update updateTX;
                     }

                     view ViewX(TransferX tx) {
                     	widget NumericWidget number <=> tx.number;

                     	group level {
                     	    table Row3[] relatedCollection <= tx.relatedCollection;
                        }

                     	action void myAction1() <= tx.myAction1 label:"my action 1";
                     	action View1 myAction2(View1 input selector:Row1) <= tx.myAction2  label:"my action 2";
                     	action View2 myAction3(Form2 input) <= tx.myAction3 label:"my action 3";
                     }

                     form FormX(TransferX tx) {
                     	widget NumericWidget number <=> tx.number;
                     }

                     row RowX(TransferX tx) {
                     	column Integer number <= tx.number;
                     	action View2 myAction3(Form2 input) <= tx.myAction3 label:"my action 3";
                     }

                     actor A {
                     	access TransferX[] txs <= Entity1.all() create:true update:true;
                     }

                     frontend M(A a)
                        menu: {
                            table RowX[] vxs <= a.txs view:ViewX form:FormX;
                         };
                """.formatted(name);
    }

    @Test
    void testTablePageContainersAndButtons() throws Exception {
        UiModelResourceSupport ui = executeTransformation("RowOps",
                createModelString("RowOps"));

        List<Application> apps = ui.getStreamOfUiApplication().toList();
        assertEquals(1, apps.size());
        Application app = apps.get(0);

        List<PageContainer> containers = app.getPageContainers();
        log.info("PageContainers: {}", containers.size());
        for (PageContainer pc : containers) {
            log.info("  Container: fqName={}, type={}", pc.getFQName(), pc.getType());
        }

        // RowX table page container should exist
        PageContainer rowXPc = containers.stream()
                .filter(c -> c.getFQName() != null && c.getFQName().contains("RowX"))
                .findFirst().orElseThrow();
        assertTrue(rowXPc.isTable(), "RowX should be a table container");

        Table rowXTable = (Table) rowXPc.getTables().get(0);
        assertNotNull(rowXTable, "RowX should have a table");

        // Table should have columns from RowX declaration
        assertTrue(rowXTable.getColumns().size() >= 1,
                "RowX table should have at least 1 column (number)");

        // Table button group should have standard actions (Filter, Refresh, OpenCreate, OpenPage)
        ButtonGroup tableButtons = rowXTable.getTableActionButtonGroup();
        assertNotNull(tableButtons, "Table should have table action button group");
        log.info("Table buttons: {}", tableButtons.getButtons().size());
        for (Button b : tableButtons.getButtons()) {
            log.info("  Button: name={}, fqName={}", b.getName(), b.getFQName());
        }
        assertTrue(tableButtons.getButtons().size() >= 2,
                "Table button group should have multiple buttons");

        // Row button group should have OpenPage and row action for myAction3
        ButtonGroup rowButtons = rowXTable.getRowActionButtonGroup();
        assertNotNull(rowButtons, "Table should have row action button group");
        log.info("Row buttons: {}", rowButtons.getButtons().size());
        for (Button b : rowButtons.getButtons()) {
            log.info("  Button: name={}, label={}", b.getName(), b.getLabel());
        }
        assertTrue(rowButtons.getButtons().stream()
                        .anyMatch(b -> "myAction3".equals(b.getName())),
                "Row buttons should include myAction3");

        // Verify ViewX view container exists
        PageContainer viewXPc = containers.stream()
                .filter(c -> c.getFQName() != null && c.getFQName().contains("ViewX"))
                .findFirst().orElseThrow();
        assertEquals(PageContainerType.VIEW, viewXPc.getType());

        // ViewX should have inline table for relatedCollection
        assertTrue(viewXPc.getTables().size() >= 1,
                "ViewX should have at least 1 inline table (relatedCollection)");
        Table inlineTable = (Table) viewXPc.getTables().stream()
                .filter(t -> ((Table) t).getName().equals("relatedCollection"))
                .findFirst().orElseThrow();

        // Inline table should have row action for myAction4
        assertNotNull(inlineTable.getRowActionButtonGroup(),
                "Inline table should have row button group");
        log.info("Inline table row buttons: {}", inlineTable.getRowActionButtonGroup().getButtons().size());
        for (Button b : inlineTable.getRowActionButtonGroup().getButtons()) {
            log.info("  Inline row button: name={}, label={}", b.getName(), b.getLabel());
        }
        assertTrue(inlineTable.getRowActionButtonGroup().getButtons().stream()
                        .anyMatch(b -> "myAction4".equals(b.getName())),
                "Inline table should have myAction4 row button");

        Button myAction4Button = inlineTable.getRowActionButtonGroup().getButtons().stream()
                .filter(b -> "myAction4".equals(b.getName()))
                .findFirst().orElseThrow();
        assertEquals("my action 4", myAction4Button.getLabel());
        assertTrue(myAction4Button.getActionDefinition().getIsCallOperationAction());

        // Verify view action buttons exist in ViewX container
        log.info("ViewX action definitions: {}", viewXPc.getAllActionDefinitions().size());
        for (var ad : viewXPc.getAllActionDefinitions()) {
            log.info("  ActionDef: fqName={}", ((ActionDefinition) ad).getFQName());
        }
        assertTrue(viewXPc.getAllActionDefinitions().size() >= 3,
                "ViewX should have action definitions for view-level actions");
    }
}
