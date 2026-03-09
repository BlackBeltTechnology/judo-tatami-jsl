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
 * Ported from cardDeclaration.etl:
 * Creates PageContainer, Table (CARD representation), columns, filters, and button groups for UICardDeclaration.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class CardDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(CardDeclarationRules.class);

    // =========================================================================
    // CardPageContainer (@greedy)
    // =========================================================================

    @TransformRule(name = CARD_PAGE_CONTAINER, description = "Create PageContainer for card declaration")
    @Greedy
    @Transform(type = UICardDeclaration.class)
    @To(type = PageContainer.class)
    public TransformFunction<UICardDeclaration, PageContainer> cardPageContainer() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            PageContainer target = ctx.createTarget(PageContainer.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardPageContainer");
            target.setName(getFqName(source) + "::Card::PageContainer");
            target.setLabel(getLabelWithNameFallback(source));
            target.setTitleFrom(TitleFrom.LABEL);
            target.setType(PageContainerType.TABLE);
            target.getChildren().add(ctx.equivalent(source, Flex.class,
                    CARD_PAGE_CONTAINER_VISUAL_ELEMENT));
            target.getActionButtonGroups().add(ctx.equivalent(source, ButtonGroup.class,
                    CARD_PAGE_CONTAINER_BUTTON_GROUP));
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            // onInit uses the same RefreshActionDefinition pattern
            target.setOnInit(ctx.equivalent(source, RefreshActionDefinition.class,
                    CARD_TABLE_REFRESH_ACTION_DEFINITION));

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPageContainers().add(target);

            LOG.debug("CardPageContainer: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Page-level button group and back button
    // =========================================================================

    @TransformRule(name = CARD_PAGE_CONTAINER_BUTTON_GROUP, description = "Create page-level ButtonGroup for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UICardDeclaration, ButtonGroup> cardPageContainerButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardPageContainerButtonGroup");
            target.setName(getFqName(source) + "::PageActions");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    CARD_PAGE_CONTAINER_BACK_BUTTON));
            LOG.debug("CardPageContainerButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CARD_PAGE_CONTAINER_BACK_BUTTON_ICON, description = "Create Icon for card back button")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UICardDeclaration, Icon> cardPageContainerBackButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardPageContainerBackButtonIcon");
            target.setIconName("arrow-left");
            target.setName(source.getName() + "BackIcon");
            return target;
        };
    }

    @TransformRule(name = CARD_PAGE_CONTAINER_BACK_ACTION_DEFINITION, description = "Create BackActionDefinition for card page")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = BackActionDefinition.class)
    public TransformFunction<UICardDeclaration, BackActionDefinition> cardPageContainerBackActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BackActionDefinition target = ctx.createTarget(BackActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardPageContainerBackActionDefinition");
            target.setName(getFqName(source) + "::Back");
            LOG.debug("CardPageContainerBackActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CARD_PAGE_CONTAINER_BACK_BUTTON, description = "Create back Button for card page")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UICardDeclaration, Button> cardPageContainerBackButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardPageContainerBackButton");
            target.setName(getFqName(source) + "::Back");
            target.setLabel("Back");
            target.setButtonStyle("text");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    CARD_PAGE_CONTAINER_BACK_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, BackActionDefinition.class,
                    CARD_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            LOG.debug("CardPageContainerBackButton: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Visual element
    // =========================================================================

    @TransformRule(name = CARD_PAGE_CONTAINER_VISUAL_ELEMENT, description = "Create Flex for card page")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Flex.class)
    public TransformFunction<UICardDeclaration, Flex> cardPageContainerVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Flex target = ctx.createTarget(Flex.class);
            posMap.put(target, 0);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardPageContainerVisualElement");
            target.setName(source.getName());
            target.setDirection(Axis.VERTICAL);
            target.setMainAxisAlignment(MainAxisAlignment.START);
            target.setCrossAxisAlignment(CrossAxisAlignment.STRETCH);
            target.setCol(12.0);
            target.getChildren().add(ctx.equivalent(source, Table.class, CARD_TABLE));

            LOG.debug("CardPageContainerVisualElement: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // CardTable (CARD representation)
    // =========================================================================

    @TransformRule(name = CARD_TABLE, description = "Create Table with CARD representation")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Table.class)
    public TransformFunction<UICardDeclaration, Table> cardTable() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Table target = ctx.createTarget(Table.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTable");
            target.setCol(12.0);
            target.setLabel(getLabelWithNameFallback(source));
            target.setName(source.getName() + "::Cards");
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            target.setRelationName("");
            target.setIsInlineEditable(true);
            target.setRepresentationComponent(TableRepresentation.CARD);
            if (!posMap.containsKey(target)) {
                posMap.put(target, 0);
            }

            // Add columns and filters from widget members
            for (EObject member : source.getMembers()) {
                if (member instanceof UIViewWidgetDeclaration) {
                    UIViewWidgetDeclaration widget = (UIViewWidgetDeclaration) member;
                    if (widget.getTransferField() != null
                            && widget.getTransferField().getTarget() != null
                            && widget.getTransferField().getTarget().getReferenceType() != null
                            && widget.getTransferField().getTarget().getReferenceType() instanceof DataTypeDeclaration
                            && ((DataTypeDeclaration) widget.getTransferField().getTarget().getReferenceType()).getPrimitive() != null) {
                        Column col = ctx.equivalent(widget, Column.class,
                                CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN);
                        target.getColumns().add(col);
                        if (col.getAttributeType() != null && col.getAttributeType().isIsFilterable()) {
                            target.getFilters().add(ctx.equivalent(widget, Filter.class,
                                    CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER));
                        }
                    }
                }
            }

            target.setTableActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    CARD_TABLE_BUTTON_GROUP));
            target.setRowActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    CARD_TABLE_ROW_BUTTON_GROUP));
            target.setSelectorRowsPerPage(10);

            LOG.debug("CardTable: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Card widget column/filter rules (from card members)
    // =========================================================================

    @TransformRule(name = CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN, description = "Create Column for card widget")
    @Lazy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = Column.class)
    public TransformFunction<UIViewWidgetDeclaration, Column> cardWidgetDeclarationPrimitiveColumn() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Column target = ctx.createTarget(Column.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardWidgetDeclarationPrimitiveColumn");
            target.setName(source.getName());
            target.setFormat("%s");
            target.setLabel(getLabelWithNameFallback(source));
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(source, Icon.class, CARD_COLUMN_DECLARATION_COLUMN_ICON));
            }
            Modifier widthMod = getWidth(source);
            target.setWidth(widthMod != null ? String.valueOf(getModifierIntValue(widthMod)) : "120");
            target.setAttributeType(getTransferFieldDeclarationEquivalent(source, ctx));
            LOG.debug("CardWidgetDeclarationPrimitiveColumn: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CARD_COLUMN_DECLARATION_COLUMN_ICON, description = "Create Icon for card column")
    @Lazy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewWidgetDeclaration, Icon> cardColumnDeclarationColumnIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardColumnDeclarationColumnIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "FieldIcon");
            return target;
        };
    }

    @TransformRule(name = CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER, description = "Create Filter for card widget")
    @Lazy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = Filter.class)
    public TransformFunction<UIViewWidgetDeclaration, Filter> cardWidgetDeclarationPrimitiveColumnFilter() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Filter target = ctx.createTarget(Filter.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardWidgetDeclarationPrimitiveColumnFilter");
            target.setName(source.getName() + "Filter");
            target.setAttributeType(getTransferFieldDeclarationEquivalent(source, ctx));
            target.setLabel(getLabelWithNameFallback(source));
            LOG.debug("CardWidgetDeclarationPrimitiveColumnFilter: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Table-level button groups
    // =========================================================================

    @TransformRule(name = CARD_TABLE_BUTTON_GROUP, description = "Create table-level ButtonGroup for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UICardDeclaration, ButtonGroup> cardTableButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTableButtonGroup");
            target.setName(source.getName() + "::CardTableButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    CARD_TABLE_FILTER_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    CARD_TABLE_REFRESH_BUTTON));
            LOG.debug("CardTableButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CARD_TABLE_ROW_BUTTON_GROUP, description = "Create row-level ButtonGroup for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UICardDeclaration, ButtonGroup> cardTableRowButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            // Note: ETL uses "TableRowButtonGroup" as the ID suffix here
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableRowButtonGroup");
            target.setName(source.getName() + "TableRowButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    CARD_OPEN_PAGE_BUTTON));
            LOG.debug("CardTableRowButtonGroup: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Filter button triplet
    // =========================================================================

    @TransformRule(name = CARD_TABLE_FILTER_BUTTON, description = "Create filter Button for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UICardDeclaration, Button> cardTableFilterButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTableFilterButton");
            target.setName(source.getName() + "::Filter");
            target.setIcon(ctx.equivalent(source, Icon.class, CARD_TABLE_FILTER_BUTTON_ICON));
            target.setLabel("Filter");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, FilterActionDefinition.class,
                    CARD_TABLE_FILTER_ACTION_DEFINITION));
            LOG.debug("CardTableFilterButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CARD_TABLE_FILTER_BUTTON_ICON, description = "Create Icon for card filter button")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UICardDeclaration, Icon> cardTableFilterButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTableFilterButtonIcon");
            target.setName(source.getName() + "FilterIcon");
            target.setIconName("filter");
            return target;
        };
    }

    @TransformRule(name = CARD_TABLE_FILTER_ACTION_DEFINITION, description = "Create FilterActionDefinition for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = FilterActionDefinition.class)
    public TransformFunction<UICardDeclaration, FilterActionDefinition> cardTableFilterActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            FilterActionDefinition target = ctx.createTarget(FilterActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTableFilterActionDefinition");
            target.setName(source.getName() + "::Filter");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("CardTableFilterActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Refresh button triplet
    // =========================================================================

    @TransformRule(name = CARD_TABLE_REFRESH_BUTTON, description = "Create refresh Button for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UICardDeclaration, Button> cardTableRefreshButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTableRefreshButton");
            target.setName(source.getName() + "::Refresh");
            target.setIcon(ctx.equivalent(source, Icon.class, CARD_TABLE_REFRESH_BUTTON_ICON));
            target.setLabel("Refresh");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, RefreshActionDefinition.class,
                    CARD_TABLE_REFRESH_ACTION_DEFINITION));
            LOG.debug("CardTableRefreshButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CARD_TABLE_REFRESH_BUTTON_ICON, description = "Create Icon for card refresh button")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UICardDeclaration, Icon> cardTableRefreshButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTableRefreshButtonIcon");
            target.setName(source.getName() + "RefreshIcon");
            target.setIconName("refresh");
            return target;
        };
    }

    @TransformRule(name = CARD_TABLE_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UICardDeclaration, RefreshActionDefinition> cardTableRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardTableRefreshActionDefinition");
            target.setName(source.getName() + "::Refresh");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("CardTableRefreshActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // OpenPage button triplet (row-level)
    // =========================================================================

    @TransformRule(name = CARD_OPEN_PAGE_BUTTON, description = "Create view Button for card row")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UICardDeclaration, Button> cardOpenPageButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardOpenPageButton");
            target.setName(source.getName() + "::View");
            target.setIcon(ctx.equivalent(source, Icon.class, CARD_OPEN_PAGE_BUTTON_ICON));
            target.setLabel("View");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, OpenPageActionDefinition.class,
                    CARD_OPEN_PAGE_ACTION_DEFINITION));
            LOG.debug("CardOpenPageButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CARD_OPEN_PAGE_BUTTON_ICON, description = "Create Icon for card view button")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UICardDeclaration, Icon> cardOpenPageButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardOpenPageButtonIcon");
            target.setName(source.getName() + "OpenPageIcon");
            target.setIconName("visibility");
            return target;
        };
    }

    @TransformRule(name = CARD_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition for card")
    @Lazy
    @Transform(type = UICardDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UICardDeclaration, OpenPageActionDefinition> cardOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CardOpenPageActionDefinition");
            target.setName(source.getName() + "::View");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("CardOpenPageActionDefinition: {}", target.getName());
            return target;
        };
    }
}
