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
 * Zeta tests for card and tag declarations.
 * Uses the same JSL model as ETL JSLModel2UiListItemTest.
 */
public class Jsl2UiZetaListItemTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiZetaListItemTest.class);

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
                    widget string StringWidget;

                    entity Entity1 {
                        field Integer number;
                        field String blaa;
                        relation Entity2[] relatedCollection;
                    }

                    entity Entity2 {
                        field String name;
                        field String hello;
                    }

                    transfer Transfer1(Entity1 e1) {
                        field Integer number <=> e1.number;
                        field String blaa <=> e1.blaa;
                        relation Transfer2[] relatedCollection <= e1.relatedCollection update;

                        event update onUpdate;
                    }

                    transfer Transfer2(Entity2 e2) {
                        field String name <=> e2.name;
                        field String hello <=> e2.hello;

                        event update onUpdate;
                    }

                    view View1(Transfer1 t1) {
                        widget NumericWidget number <=> t1.number;
                        widget StringWidget blaa <=> t1.blaa;

                        group level {
                            table Card2[] relatedCollection <= t1.relatedCollection view:View2;
                            table Tag2[] relatedTagCollection <= t1.relatedCollection view:View2;
                        }
                    }

                    view View2(Transfer2 t2) {
                        widget StringWidget name <=> t2.name;
                        widget StringWidget hello <=> t2.hello;
                    }

                    card Card1(Transfer1 t1) {
                        widget NumericWidget number <=> t1.number;
                        widget StringWidget blaa <=> t1.blaa;
                    }

                    card Card2(Transfer2 t2) {
                        widget StringWidget name <=> t2.name;
                        widget StringWidget hello <=> t2.hello;
                    }

                    tag Tag1(Transfer1 t1) text:t1.blaa;

                    tag Tag2(Transfer2 t2) text:t2.name;

                    actor A {
                        access Transfer1[] t1s <= Entity1.all() update:true;
                    }

                    frontend M(A a)
                        menu: {
                            table Card1[] v1s <= a.t1s view:View1;
                            table Tag1[] t1s <= a.t1s view:View1;
                        };
                """.formatted(name);
    }

    @Test
    void testCardPageContainers() throws Exception {
        UiModelResourceSupport ui = executeTransformation("Cards", createModelString("Cards"));

        List<Application> apps = ui.getStreamOfUiApplication().toList();
        assertEquals(1, apps.size());
        Application app = apps.get(0);

        // Verify card page containers exist
        List<PageContainer> containers = app.getPageContainers();
        log.info("PageContainers: {}", containers.size());
        for (PageContainer pc : containers) {
            log.info("  PageContainer: fqName={}, type={}, isTable={}", pc.getFQName(), pc.getType(), pc.isTable());
        }

        // Card1 page container should exist with CARD representation
        PageContainer card1Pc = containers.stream()
                .filter(c -> c.getFQName() != null && c.getFQName().contains("Card1"))
                .findFirst()
                .orElseThrow();
        assertTrue(card1Pc.isTable(), "Card1 PageContainer should be a table");
        assertFalse(card1Pc.getTables().isEmpty(), "Card1 should have tables");
        Table card1Table = (Table) card1Pc.getTables().get(0);
        assertEquals(TableRepresentation.CARD, card1Table.getRepresentationComponent(),
                "Card1 table should have CARD representation");

        // Card1 should have columns from widget declarations
        assertTrue(card1Table.getColumns().size() >= 2,
                "Card1 table should have at least 2 columns (number, blaa)");

        // Card1 should have table and row action button groups
        assertNotNull(card1Table.getTableActionButtonGroup(), "Card table should have table button group");
        assertNotNull(card1Table.getRowActionButtonGroup(), "Card table should have row button group");

        // Card2 page container should also exist
        assertTrue(containers.stream().anyMatch(c -> c.getFQName() != null && c.getFQName().contains("Card2")),
                "Card2 PageContainer should exist");

        // Tag1 page container should exist
        PageContainer tag1Pc = containers.stream()
                .filter(c -> c.getFQName() != null && c.getFQName().contains("Tag1"))
                .findFirst()
                .orElseThrow();
        assertTrue(tag1Pc.isTable(), "Tag1 PageContainer should be a table");
        Table tag1Table = (Table) tag1Pc.getTables().get(0);
        assertEquals(TableRepresentation.TAG, tag1Table.getRepresentationComponent(),
                "Tag1 table should have TAG representation");

        // Tag should have exactly 1 column
        assertEquals(1, tag1Table.getColumns().size(),
                "Tag1 table should have exactly 1 column");

        // Tag2 page container should exist
        assertTrue(containers.stream().anyMatch(c -> c.getFQName() != null && c.getFQName().contains("Tag2")),
                "Tag2 PageContainer should exist");
    }

    @Test
    void testCardAndTagTableStructure() throws Exception {
        UiModelResourceSupport ui = executeTransformation("CardTagStructure", createModelString("CardTagStructure"));

        List<Application> apps = ui.getStreamOfUiApplication().toList();
        assertEquals(1, apps.size());
        Application app = apps.get(0);

        // Verify Card1 table has correct button groups
        List<PageContainer> containers = app.getPageContainers();
        PageContainer card1Pc = containers.stream()
                .filter(c -> c.getFQName() != null && c.getFQName().contains("Card1"))
                .findFirst().orElseThrow();
        Table card1Table = (Table) card1Pc.getTables().get(0);

        // Card table button group should have Filter and Refresh
        ButtonGroup cardTableButtons = card1Table.getTableActionButtonGroup();
        assertNotNull(cardTableButtons, "Card table should have table button group");
        assertTrue(cardTableButtons.getButtons().stream().anyMatch(b -> b.getName().contains("Filter")),
                "Card table buttons should include Filter");
        assertTrue(cardTableButtons.getButtons().stream().anyMatch(b -> b.getName().contains("Refresh")),
                "Card table buttons should include Refresh");

        // Card row button group should have View (OpenPage)
        ButtonGroup cardRowButtons = card1Table.getRowActionButtonGroup();
        assertNotNull(cardRowButtons, "Card table should have row button group");
        assertTrue(cardRowButtons.getButtons().stream().anyMatch(b -> b.getName().contains("View")),
                "Card row buttons should include View");

        // Verify Tag1 table has correct structure
        PageContainer tag1Pc = containers.stream()
                .filter(c -> c.getFQName() != null && c.getFQName().contains("Tag1"))
                .findFirst().orElseThrow();
        Table tag1Table = (Table) tag1Pc.getTables().get(0);

        // Tag table button group
        ButtonGroup tagTableButtons = tag1Table.getTableActionButtonGroup();
        assertNotNull(tagTableButtons, "Tag table should have table button group");
        assertTrue(tagTableButtons.getButtons().stream().anyMatch(b -> b.getName().contains("Filter")),
                "Tag table buttons should include Filter");
        assertTrue(tagTableButtons.getButtons().stream().anyMatch(b -> b.getName().contains("Refresh")),
                "Tag table buttons should include Refresh");

        // Tag row button group
        ButtonGroup tagRowButtons = tag1Table.getRowActionButtonGroup();
        assertNotNull(tagRowButtons, "Tag table should have row button group");
        assertTrue(tagRowButtons.getButtons().stream().anyMatch(b -> b.getName().contains("View")),
                "Tag row buttons should include View");
    }
}
