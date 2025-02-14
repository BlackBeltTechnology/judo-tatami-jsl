package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JSLModel2UiListItemTest extends AbstractTest  {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/list-item";

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

    private static String createModelString(String name) {
        return """
                    model %s;

                    import judo::types;

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
                        widget Integer number <=> t1.number;
                        widget String blaa <=> t1.blaa;

                        group level {
                            table Card2[] relatedCollection <= t1.relatedCollection view:View2;
                            table Tag2[] relatedTagCollection <= t1.relatedCollection view:View2;
                        }
                    }

                    view View2(Transfer2 t2) {
                        widget String name <=> t2.name;
                        widget String hello <=> t2.hello;
                    }

                    card Card1(Transfer1 t1) {
                        widget Integer number <=> t1.number;
                        widget String blaa <=> t1.blaa;
                    }

                    card Card2(Transfer2 t2) {
                        widget String name <=> t2.name;
                        widget String hello <=> t2.hello;
                    }

                    tag Tag1(Transfer1 t1) text:t1.blaa;

                    tag Tag2(Transfer2 t2) text:t2.name;

                    actor A {
                        access Transfer1[] t1s <= Entity1.all() update:true;
                    }

                    menu M(A a) {
                        table Card1[] v1s <= a.t1s view:View1;
                        table Tag1[] t1s <= a.t1s view:View1;
                    }

                """.formatted(name);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testCards() throws Exception {
        jslModel = JslParser.getModelFromStrings("Cards", List.of(createModelString("Cards")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        List<PageDefinition> pages = apps.get(0).getPages();
        List<PageContainer> containers = apps.get(0).getPageContainers();

        assertEquals(List.of(
                "A::Cards::M::DashboardPage",
                "A::Cards::M::t1s::AccessTableViewPage",
                "A::Cards::M::t1s::AccessTagsPage",
                "A::Cards::M::v1s::AccessCardsPage",
                "A::Cards::M::v1s::AccessTableViewPage",
                "A::Cards::View1::level::relatedCollection::ViewPage",
                "A::Cards::View1::level::relatedTagCollection::ViewPage"
        ), pages.stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Cards::Card1::Card::PageContainer",
                "A::Cards::Card2::Card::PageContainer",
                "A::Cards::M::Dashboard",
                "A::Cards::Tag1::Tag::PageContainer",
                "A::Cards::Tag2::Tag::PageContainer",
                "A::Cards::View1::View::PageContainer",
                "A::Cards::View2::View::PageContainer"
        ), containers.stream().map(NamedElement::getFQName).sorted().toList());

        PageDefinition accessCardsPage = pages.stream().filter(p -> p.getFQName().equals("A::Cards::M::v1s::AccessCardsPage")).findFirst().orElseThrow();
        PageDefinition accessViewPage = pages.stream().filter(p -> p.getFQName().equals("A::Cards::M::v1s::AccessTableViewPage")).findFirst().orElseThrow();
        PageDefinition relatedViewPage = pages.stream().filter(p -> p.getFQName().equals("A::Cards::View1::level::relatedCollection::ViewPage")).findFirst().orElseThrow();

        // Access Cards Page

        assertEquals(List.of(
                "A::Cards::M::v1s::AccessCardsPage::v1s::Back",
                "A::Cards::M::v1s::AccessCardsPage::v1s::Filter",
                "A::Cards::M::v1s::AccessCardsPage::v1s::OpenPage",
                "A::Cards::M::v1s::AccessCardsPage::v1s::Refresh"
        ), accessCardsPage.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        PageContainer accessCardsPageContainer = containers.stream().filter(c -> c.getFQName().equals("A::Cards::Card1::Card::PageContainer")).findFirst().orElseThrow();
        Table accessCardsTable = (Table) accessCardsPageContainer.getTables().get(0);

        assertTrue(accessCardsPageContainer.isTable());
        assertNotNull(accessCardsTable);
        assertEquals(TableRepresentation.CARD, accessCardsTable.getRepresentationComponent());

        assertEquals(List.of(
                "A::Cards::Card1::Card::PageContainer::Card1::Card1::Cards::blaa",
                "A::Cards::Card1::Card::PageContainer::Card1::Card1::Cards::number"
        ), accessCardsTable.getColumns().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Cards::Card1::Card::PageContainer::Card1::Card1::Cards::Card1::CardTableButtonGroup::Card1::Filter",
                "A::Cards::Card1::Card::PageContainer::Card1::Card1::Cards::Card1::CardTableButtonGroup::Card1::Refresh"
        ), accessCardsTable.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Cards::Card1::Card::PageContainer::Card1::Card1::Cards::Card1TableRowButtonGroup::Card1::View"
        ), accessCardsTable.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        Button accessCardsTableViewButton = accessCardsTable.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getFQName().equals("A::Cards::Card1::Card::PageContainer::Card1::Card1::Cards::Card1TableRowButtonGroup::Card1::View")).findFirst().orElseThrow();
        Action viewPageAction = accessCardsPage.getActions().stream().filter(a -> a.getFQName().equals("A::Cards::M::v1s::AccessCardsPage::v1s::OpenPage")).findFirst().orElseThrow();

        assertEquals(viewPageAction.getActionDefinition(), accessCardsTableViewButton.getActionDefinition());
        assertEquals(accessViewPage, viewPageAction.getTargetPageDefinition());

        // Inline View Cards Component

        assertEquals(List.of(
                "A::Cards::M::v1s::AccessTableViewPage::relatedCollection::Filter",
                "A::Cards::M::v1s::AccessTableViewPage::relatedCollection::OpenPage",
                "A::Cards::M::v1s::AccessTableViewPage::relatedCollection::Refresh",
                "A::Cards::M::v1s::AccessTableViewPage::relatedTagCollection::AutocompleteAdd",
                "A::Cards::M::v1s::AccessTableViewPage::relatedTagCollection::AutocompleteRange",
                "A::Cards::M::v1s::AccessTableViewPage::relatedTagCollection::Filter",
                "A::Cards::M::v1s::AccessTableViewPage::relatedTagCollection::OpenPage",
                "A::Cards::M::v1s::AccessTableViewPage::relatedTagCollection::Refresh",
                "A::Cards::M::v1s::AccessTableViewPage::v1s::Back",
                "A::Cards::M::v1s::AccessTableViewPage::v1s::Cancel",
                "A::Cards::M::v1s::AccessTableViewPage::v1s::Refresh",
                "A::Cards::M::v1s::AccessTableViewPage::v1s::Update"
        ), accessViewPage.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        PageContainer accessViewPageContainer = accessViewPage.getContainer();

        assertEquals(List.of(
                "A::Cards::View1::View::PageContainer::View1::level::relatedCollection",
                "A::Cards::View1::View::PageContainer::View1::level::relatedTagCollection"
        ), accessViewPageContainer.getTables().stream().map(t -> ((Table) t).getFQName()).sorted().toList());

        Table inlineCardsTable = (Table) accessViewPageContainer.getTables().stream().filter(t -> ((Table) t).getFQName().equals("A::Cards::View1::View::PageContainer::View1::level::relatedCollection")).findFirst().orElseThrow();

        assertEquals(TableRepresentation.CARD, inlineCardsTable.getRepresentationComponent());

        assertEquals(List.of(
                "A::Cards::View1::View::PageContainer::View1::level::relatedCollection::hello",
                "A::Cards::View1::View::PageContainer::View1::level::relatedCollection::name"
        ), inlineCardsTable.getColumns().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Cards::View1::View::PageContainer::View1::level::relatedCollection::relatedCollection::InlineViewCardsButtonGroup::relatedCollection::Filter",
                "A::Cards::View1::View::PageContainer::View1::level::relatedCollection::relatedCollection::InlineViewCardsButtonGroup::relatedCollection::Refresh"
        ), inlineCardsTable.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Cards::View1::View::PageContainer::View1::level::relatedCollection::relatedCollectionTableRowButtonGroup::relatedCollection::View"
        ), inlineCardsTable.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        Button inlineCardsOpenPageButton = inlineCardsTable.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getFQName().equals("A::Cards::View1::View::PageContainer::View1::level::relatedCollection::relatedCollectionTableRowButtonGroup::relatedCollection::View")).findFirst().orElseThrow();
        Action inlineCardsOpenPageAction = accessViewPage.getActions().stream().filter(a -> a.getFQName().equals("A::Cards::M::v1s::AccessTableViewPage::relatedCollection::OpenPage")).findFirst().orElseThrow();

        assertEquals(inlineCardsOpenPageAction.getActionDefinition(), inlineCardsOpenPageButton.getActionDefinition());
        assertEquals(relatedViewPage, inlineCardsOpenPageAction.getTargetPageDefinition());
    }

    @Test
    void testTags() throws Exception {
        jslModel = JslParser.getModelFromStrings("Tags", List.of(createModelString("Tags")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        List<PageDefinition> pages = apps.get(0).getPages();
        List<PageContainer> containers = apps.get(0).getPageContainers();

        assertEquals(List.of(
                "A::Tags::M::DashboardPage",
                "A::Tags::M::t1s::AccessTableViewPage",
                "A::Tags::M::t1s::AccessTagsPage",
                "A::Tags::M::v1s::AccessCardsPage",
                "A::Tags::M::v1s::AccessTableViewPage",
                "A::Tags::View1::level::relatedCollection::ViewPage",
                "A::Tags::View1::level::relatedTagCollection::ViewPage"
        ), pages.stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Tags::Card1::Card::PageContainer",
                "A::Tags::Card2::Card::PageContainer",
                "A::Tags::M::Dashboard",
                "A::Tags::Tag1::Tag::PageContainer",
                "A::Tags::Tag2::Tag::PageContainer",
                "A::Tags::View1::View::PageContainer",
                "A::Tags::View2::View::PageContainer"
        ), containers.stream().map(NamedElement::getFQName).sorted().toList());

        PageDefinition accessTagsPage = pages.stream().filter(p -> p.getFQName().equals("A::Tags::M::t1s::AccessTagsPage")).findFirst().orElseThrow();
        PageDefinition accessViewPage = pages.stream().filter(p -> p.getFQName().equals("A::Tags::M::t1s::AccessTableViewPage")).findFirst().orElseThrow();
        PageDefinition relatedViewPage = pages.stream().filter(p -> p.getFQName().equals("A::Tags::View1::level::relatedTagCollection::ViewPage")).findFirst().orElseThrow();

        // Access Tags Page

        assertEquals(List.of(
                "A::Tags::M::t1s::AccessTagsPage::t1s::Back",
                "A::Tags::M::t1s::AccessTagsPage::t1s::Filter",
                "A::Tags::M::t1s::AccessTagsPage::t1s::OpenPage",
                "A::Tags::M::t1s::AccessTagsPage::t1s::Refresh"
        ), accessTagsPage.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        PageContainer accessTagsPageContainer = containers.stream().filter(c -> c.getFQName().equals("A::Tags::Tag1::Tag::PageContainer")).findFirst().orElseThrow();
        Table accessTagsTable = (Table) accessTagsPageContainer.getTables().get(0);

        assertTrue(accessTagsPageContainer.isTable());
        assertNotNull(accessTagsTable);
        assertEquals(TableRepresentation.TAG, accessTagsTable.getRepresentationComponent());

        assertEquals(List.of(
                "A::Tags::Tag1::Tag::PageContainer::Tag1::Tag1::Tags::Tag1"
        ), accessTagsTable.getColumns().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Tags::Tag1::Tag::PageContainer::Tag1::Tag1::Tags::Tag1::TagTableButtonGroup::Tag1::Filter",
                "A::Tags::Tag1::Tag::PageContainer::Tag1::Tag1::Tags::Tag1::TagTableButtonGroup::Tag1::Refresh"
        ), accessTagsTable.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Tags::Tag1::Tag::PageContainer::Tag1::Tag1::Tags::Tag1TableRowButtonGroup::Tag1::View"
        ), accessTagsTable.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        Button accessTagsTableViewButton = accessTagsTable.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getFQName().equals("A::Tags::Tag1::Tag::PageContainer::Tag1::Tag1::Tags::Tag1TableRowButtonGroup::Tag1::View")).findFirst().orElseThrow();
        Action viewPageAction = accessTagsPage.getActions().stream().filter(a -> a.getFQName().equals("A::Tags::M::t1s::AccessTagsPage::t1s::OpenPage")).findFirst().orElseThrow();

        assertEquals(viewPageAction.getActionDefinition(), accessTagsTableViewButton.getActionDefinition());
        assertEquals(accessViewPage, viewPageAction.getTargetPageDefinition());

        // Inline View Tags Component

        assertEquals(List.of(
                "A::Tags::M::t1s::AccessTableViewPage::relatedCollection::Filter",
                "A::Tags::M::t1s::AccessTableViewPage::relatedCollection::OpenPage",
                "A::Tags::M::t1s::AccessTableViewPage::relatedCollection::Refresh",
                "A::Tags::M::t1s::AccessTableViewPage::relatedTagCollection::AutocompleteAdd",
                "A::Tags::M::t1s::AccessTableViewPage::relatedTagCollection::AutocompleteRange",
                "A::Tags::M::t1s::AccessTableViewPage::relatedTagCollection::Filter",
                "A::Tags::M::t1s::AccessTableViewPage::relatedTagCollection::OpenPage",
                "A::Tags::M::t1s::AccessTableViewPage::relatedTagCollection::Refresh",
                "A::Tags::M::t1s::AccessTableViewPage::t1s::Back",
                "A::Tags::M::t1s::AccessTableViewPage::t1s::Cancel",
                "A::Tags::M::t1s::AccessTableViewPage::t1s::Refresh",
                "A::Tags::M::t1s::AccessTableViewPage::t1s::Update"
        ), accessViewPage.getActions().stream().map(NamedElement::getFQName).sorted().toList());

        PageContainer accessViewPageContainer = accessViewPage.getContainer();

        assertEquals(List.of(
                "A::Tags::View1::View::PageContainer::View1::level::relatedCollection",
                "A::Tags::View1::View::PageContainer::View1::level::relatedTagCollection"
        ), accessViewPageContainer.getTables().stream().map(t -> ((Table) t).getFQName()).sorted().toList());

        Table inlineTagsTable = (Table) accessViewPageContainer.getTables().stream().filter(t -> ((Table) t).getFQName().equals("A::Tags::View1::View::PageContainer::View1::level::relatedTagCollection")).findFirst().orElseThrow();

        assertEquals(TableRepresentation.CARD, inlineTagsTable.getRepresentationComponent());

        assertEquals(List.of(
                "A::Tags::View1::View::PageContainer::View1::level::relatedTagCollection::Tag2"
        ), inlineTagsTable.getColumns().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Tags::View1::View::PageContainer::View1::level::relatedTagCollection::relatedTagCollection::InlineViewTagsButtonGroup::relatedTagCollection::Filter",
                "A::Tags::View1::View::PageContainer::View1::level::relatedTagCollection::relatedTagCollection::InlineViewTagsButtonGroup::relatedTagCollection::Refresh"
        ), inlineTagsTable.getTableActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        assertEquals(List.of(
                "A::Tags::View1::View::PageContainer::View1::level::relatedTagCollection::relatedTagCollectionTableRowButtonGroup::relatedTagCollection::View"
        ), inlineTagsTable.getRowActionButtonGroup().getButtons().stream().map(NamedElement::getFQName).sorted().toList());

        Button inlineTagsOpenPageButton = inlineTagsTable.getRowActionButtonGroup().getButtons().stream().filter(b -> b.getFQName().equals("A::Tags::View1::View::PageContainer::View1::level::relatedTagCollection::relatedTagCollectionTableRowButtonGroup::relatedTagCollection::View")).findFirst().orElseThrow();
        Action inlineTagsOpenPageAction = accessViewPage.getActions().stream().filter(a -> a.getFQName().equals("A::Tags::M::t1s::AccessTableViewPage::relatedTagCollection::OpenPage")).findFirst().orElseThrow();

        assertEquals(inlineTagsOpenPageAction.getActionDefinition(), inlineTagsOpenPageButton.getActionDefinition());
        assertEquals(relatedViewPage, inlineTagsOpenPageAction.getTargetPageDefinition());
    }
}
