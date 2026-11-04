package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view;

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

import hu.blackbelt.judo.meta.jsl.jsldsl.*;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.AttributeType;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.zeta.annotation.Greedy;
import hu.blackbelt.judo.zeta.annotation.Lazy;
import hu.blackbelt.judo.zeta.annotation.To;
import hu.blackbelt.judo.zeta.annotation.Transform;
import hu.blackbelt.judo.zeta.annotation.TransformRule;
import hu.blackbelt.judo.zeta.annotation.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from tagDeclaration.etl:
 * Creates PageContainer, Table (TAG representation), column, filter, and button groups for UITagDeclaration.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class TagDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(TagDeclarationRules.class);

    // =========================================================================
    // TagPageContainer (@greedy)
    // =========================================================================

    @TransformRule(name = TAG_PAGE_CONTAINER, description = "Create PageContainer for tag declaration")
    @Greedy
    @Transform(type = UITagDeclaration.class)
    @To(type = PageContainer.class)
    public TransformFunction<UITagDeclaration, PageContainer> tagPageContainer() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            PageContainer target = ctx.createTarget(PageContainer.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagPageContainer");
            target.setName(getFqName(source) + "::Tag::PageContainer");
            target.setLabel(source.getName());
            target.setTitleFrom(TitleFrom.LABEL);
            target.setType(PageContainerType.TABLE);
            target.getChildren().add(ctx.equivalent(source, Flex.class,
                    TAG_PAGE_CONTAINER_VISUAL_ELEMENT));
            target.getActionButtonGroups().add(ctx.equivalent(source, ButtonGroup.class,
                    TAG_PAGE_CONTAINER_BUTTON_GROUP));
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPageContainers().add(target);

            LOG.debug("TagPageContainer: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Page-level button group and back button
    // =========================================================================

    @TransformRule(name = TAG_PAGE_CONTAINER_BUTTON_GROUP, description = "Create page-level ButtonGroup for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UITagDeclaration, ButtonGroup> tagPageContainerButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagPageContainerButtonGroup");
            target.setName(getFqName(source) + "::PageActions");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TAG_PAGE_CONTAINER_BACK_BUTTON));
            LOG.debug("TagPageContainerButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TAG_PAGE_CONTAINER_BACK_BUTTON_ICON, description = "Create Icon for tag back button")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UITagDeclaration, Icon> tagPageContainerBackButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagPageContainerBackButtonIcon");
            target.setIconName("arrow-left");
            target.setName(source.getName() + "BackIcon");
            return target;
        };
    }

    @TransformRule(name = TAG_PAGE_CONTAINER_BACK_ACTION_DEFINITION, description = "Create BackActionDefinition for tag page")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = BackActionDefinition.class)
    public TransformFunction<UITagDeclaration, BackActionDefinition> tagPageContainerBackActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BackActionDefinition target = ctx.createTarget(BackActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagPageContainerBackActionDefinition");
            target.setName(getFqName(source) + "::Back");
            LOG.debug("TagPageContainerBackActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TAG_PAGE_CONTAINER_BACK_BUTTON, description = "Create back Button for tag page")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UITagDeclaration, Button> tagPageContainerBackButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagPageContainerBackButton");
            target.setName(getFqName(source) + "::Back");
            target.setLabel("Back");
            target.setButtonStyle("text");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    TAG_PAGE_CONTAINER_BACK_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, BackActionDefinition.class,
                    TAG_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            LOG.debug("TagPageContainerBackButton: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Visual element
    // =========================================================================

    @TransformRule(name = TAG_PAGE_CONTAINER_VISUAL_ELEMENT, description = "Create Flex for tag page")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Flex.class)
    public TransformFunction<UITagDeclaration, Flex> tagPageContainerVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Flex target = ctx.createTarget(Flex.class);
            posMap.put(target, 0);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagPageContainerVisualElement");
            target.setName(source.getName());
            target.setDirection(Axis.VERTICAL);
            target.setMainAxisAlignment(MainAxisAlignment.START);
            target.setCrossAxisAlignment(CrossAxisAlignment.STRETCH);
            target.setCol(12.0);
            target.getChildren().add(ctx.equivalent(source, Table.class, TAG_TABLE));

            LOG.debug("TagPageContainerVisualElement: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // TagTable (TAG representation)
    // =========================================================================

    @TransformRule(name = TAG_TABLE, description = "Create Table with TAG representation")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Table.class)
    public TransformFunction<UITagDeclaration, Table> tagTable() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Table target = ctx.createTarget(Table.class);
            String id = frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagTable";
            ctx.setElementId(target, id);
            target.setCol(12.0);
            target.setLabel(getLabelWithNameFallback(source));
            target.setName(source.getName() + "::Tags");
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            target.setRelationName("");
            target.setIsInlineEditable(true);
            target.setRepresentationComponent(TableRepresentation.TAG);
            if (!posMap.containsKey(target)) {
                posMap.put(target, 0);
            }

            // Tag uses a single column from getTransferFieldDeclaration()
            // ETL uses equivalentDiscriminated with table id
            Column col = ctx.equivalentDiscriminated(source, Column.class,
                    TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN, id);
            target.getColumns().add(col);
            if (col.getAttributeType() != null && col.getAttributeType().isIsFilterable()) {
                target.getFilters().add(ctx.equivalentDiscriminated(source, Filter.class,
                        TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER, id));
            }

            target.setTableActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    TAG_TABLE_BUTTON_GROUP));
            target.setRowActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    TAG_TABLE_ROW_BUTTON_GROUP));
            target.setSelectorRowsPerPage(10);

            LOG.debug("TagTable: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Tag column/filter rules
    // =========================================================================

    @TransformRule(name = TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN, description = "Create Column for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Column.class)
    public TransformFunction<UITagDeclaration, Column> tagWidgetDeclarationPrimitiveColumn() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Column target = ctx.createTarget(Column.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagWidgetDeclarationPrimitiveColumn");
            target.setName(source.getName());
            target.setFormat("%s");
            target.setLabel(source.getName());
            target.setWidth("120");
            target.setAttributeType(getTagTransferFieldEquivalent(source, ctx));
            LOG.debug("TagWidgetDeclarationPrimitiveColumn: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER, description = "Create Filter for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Filter.class)
    public TransformFunction<UITagDeclaration, Filter> tagWidgetDeclarationPrimitiveColumnFilter() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Filter target = ctx.createTarget(Filter.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagWidgetDeclarationPrimitiveColumnFilter");
            target.setName(source.getName() + "Filter");
            target.setAttributeType(getTagTransferFieldEquivalent(source, ctx));
            target.setLabel(source.getName());
            LOG.debug("TagWidgetDeclarationPrimitiveColumnFilter: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Table-level button groups
    // =========================================================================

    @TransformRule(name = TAG_TABLE_BUTTON_GROUP, description = "Create table-level ButtonGroup for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UITagDeclaration, ButtonGroup> tagTableButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagTableButtonGroup");
            target.setName(source.getName() + "::TagTableButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TAG_TABLE_FILTER_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TAG_TABLE_REFRESH_BUTTON));
            LOG.debug("TagTableButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TAG_TABLE_ROW_BUTTON_GROUP, description = "Create row-level ButtonGroup for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UITagDeclaration, ButtonGroup> tagTableRowButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            // Note: ETL uses "TableRowButtonGroup" as the ID suffix
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableRowButtonGroup");
            target.setName(source.getName() + "TableRowButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TAG_OPEN_PAGE_BUTTON));
            LOG.debug("TagTableRowButtonGroup: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Filter button triplet
    // =========================================================================

    @TransformRule(name = TAG_TABLE_FILTER_BUTTON, description = "Create filter Button for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UITagDeclaration, Button> tagTableFilterButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagTableFilterButton");
            target.setName(source.getName() + "::Filter");
            target.setIcon(ctx.equivalent(source, Icon.class, TAG_TABLE_FILTER_BUTTON_ICON));
            target.setLabel("Filter");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, FilterActionDefinition.class,
                    TAG_TABLE_FILTER_ACTION_DEFINITION));
            LOG.debug("TagTableFilterButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TAG_TABLE_FILTER_BUTTON_ICON, description = "Create Icon for tag filter button")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UITagDeclaration, Icon> tagTableFilterButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagTableFilterButtonIcon");
            target.setName(source.getName() + "FilterIcon");
            target.setIconName("filter");
            return target;
        };
    }

    @TransformRule(name = TAG_TABLE_FILTER_ACTION_DEFINITION, description = "Create FilterActionDefinition for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = FilterActionDefinition.class)
    public TransformFunction<UITagDeclaration, FilterActionDefinition> tagTableFilterActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            FilterActionDefinition target = ctx.createTarget(FilterActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagTableFilterActionDefinition");
            target.setName(source.getName() + "::Filter");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TagTableFilterActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Refresh button (no icon in ETL for tag refresh)
    // =========================================================================

    @TransformRule(name = TAG_TABLE_REFRESH_BUTTON, description = "Create refresh Button for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UITagDeclaration, Button> tagTableRefreshButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagTableRefreshButton");
            target.setName(source.getName() + "::Refresh");
            target.setLabel("Refresh");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, RefreshActionDefinition.class,
                    TAG_TABLE_REFRESH_ACTION_DEFINITION));
            LOG.debug("TagTableRefreshButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TAG_TABLE_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UITagDeclaration, RefreshActionDefinition> tagTableRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagTableRefreshActionDefinition");
            target.setName(source.getName() + "::Refresh");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TagTableRefreshActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // OpenPage button (row-level, no icon in ETL)
    // =========================================================================

    @TransformRule(name = TAG_OPEN_PAGE_BUTTON, description = "Create view Button for tag row")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UITagDeclaration, Button> tagOpenPageButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagOpenPageButton");
            target.setName(source.getName() + "::View");
            target.setLabel("View");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, OpenPageActionDefinition.class,
                    TAG_OPEN_PAGE_ACTION_DEFINITION));
            LOG.debug("TagOpenPageButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TAG_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition for tag")
    @Lazy
    @Transform(type = UITagDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UITagDeclaration, OpenPageActionDefinition> tagOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TagOpenPageActionDefinition");
            target.setName(source.getName() + "::View");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TagOpenPageActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Helper: get AttributeType for tag's transfer field
    // =========================================================================

    private AttributeType getTagTransferFieldEquivalent(UITagDeclaration tag,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        TransferFieldDeclaration field = getTransferFieldDeclaration(tag);
        if (field != null) {
            return getTransferFieldAttributeType(field, ctx);
        }
        return null;
    }
}
