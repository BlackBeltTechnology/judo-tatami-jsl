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
 * Ported from rowDeclaration.etl and rowColumnDeclaration.etl:
 * Creates PageContainer, Table, columns, filters, and button groups for UIRowDeclaration.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class RowDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(RowDeclarationRules.class);

    // =========================================================================
    // TablePageContainer (@greedy)
    // =========================================================================

    @TransformRule(name = TABLE_PAGE_CONTAINER, description = "Create PageContainer for row declaration table")
    @Greedy
    @Transform(type = UIRowDeclaration.class)
    @To(type = PageContainer.class)
    public TransformFunction<UIRowDeclaration, PageContainer> tablePageContainer() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            PageContainer target = ctx.createTarget(PageContainer.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainer");
            target.setName(getFqName(source) + "::Table::PageContainer");
            target.setLabel(getLabelWithNameFallback(source));
            target.setTitleFrom(TitleFrom.LABEL);
            target.setType(PageContainerType.TABLE);
            target.getChildren().add(ctx.equivalent(source, Flex.class,
                    TABLE_PAGE_CONTAINER_VISUAL_ELEMENT));
            target.getActionButtonGroups().add(ctx.equivalent(source, ButtonGroup.class,
                    TABLE_PAGE_CONTAINER_BUTTON_GROUP));
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            target.setOnInit(ctx.equivalent(source, RefreshActionDefinition.class,
                    TABLE_TABLE_REFRESH_ACTION_DEFINITION));

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPageContainers().add(target);

            LOG.debug("TablePageContainer: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Page-level button group and buttons
    // =========================================================================

    @TransformRule(name = TABLE_PAGE_CONTAINER_BUTTON_GROUP, description = "Create page-level ButtonGroup for table")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIRowDeclaration, ButtonGroup> tablePageContainerButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerButtonGroup");
            target.setName(getFqName(source) + "::PageActions");
            target.setLabel("Actions");

            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_PAGE_CONTAINER_BACK_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_BUTTON));

            LOG.debug("TablePageContainerButtonGroup: {}", target.getName());
            return target;
        };
    }

    // --- Set selector triplet ---

    @TransformRule(name = TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_ACTION_DEFINITION, description = "Create SetActionDefinition for page container")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = SetActionDefinition.class)
    public TransformFunction<UIRowDeclaration, SetActionDefinition> tablePageContainerSetSelectorSetActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            SetActionDefinition target = ctx.createTarget(SetActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerSetSelectorSetActionDefinition");
            target.setName(getFqName(source) + "::Set");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TablePageContainerSetSelectorSetActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_BUTTON_ICON, description = "Create Icon for set selector button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tablePageContainerSetSelectorSetButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerSetSelectorSetButtonIcon");
            target.setIconName("attachment-plus");
            target.setName(source.getName() + "Icon");
            return target;
        };
    }

    @TransformRule(name = TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_BUTTON, description = "Create set selector Button for page container")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tablePageContainerSetSelectorSetButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerSetSelectorSetButton");
            target.setName(getFqName(source) + "::Set");
            target.setLabel("Set");
            target.setButtonStyle("contained");
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            target.setIcon(ctx.equivalent(source, Icon.class,
                    TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, SetActionDefinition.class,
                    TABLE_PAGE_CONTAINER_SET_SELECTOR_SET_ACTION_DEFINITION));
            LOG.debug("TablePageContainerSetSelectorSetButton: {}", target.getName());
            return target;
        };
    }

    // --- Add selector triplet ---

    @TransformRule(name = TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_ACTION_DEFINITION, description = "Create AddActionDefinition for page container")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = AddActionDefinition.class)
    public TransformFunction<UIRowDeclaration, AddActionDefinition> tablePageContainerAddSelectorAddActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            AddActionDefinition target = ctx.createTarget(AddActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerAddSelectorAddActionDefinition");
            target.setName(getFqName(source) + "::Add");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TablePageContainerAddSelectorAddActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_BUTTON_ICON, description = "Create Icon for add selector button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tablePageContainerAddSelectorAddButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerAddSelectorAddButtonIcon");
            target.setIconName("attachment-plus");
            target.setName(source.getName() + "Icon");
            return target;
        };
    }

    @TransformRule(name = TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_BUTTON, description = "Create add selector Button for page container")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tablePageContainerAddSelectorAddButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerAddSelectorAddButton");
            target.setName(getFqName(source) + "::Add");
            target.setLabel("Add");
            target.setButtonStyle("contained");
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            target.setIcon(ctx.equivalent(source, Icon.class,
                    TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, AddActionDefinition.class,
                    TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_ACTION_DEFINITION));
            LOG.debug("TablePageContainerAddSelectorAddButton: {}", target.getName());
            return target;
        };
    }

    // --- Back button triplet ---

    @TransformRule(name = TABLE_PAGE_CONTAINER_BACK_BUTTON_ICON, description = "Create Icon for back button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tablePageContainerBackButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerBackButtonIcon");
            target.setIconName("arrow-left");
            target.setName(source.getName() + "BackIcon");
            return target;
        };
    }

    @TransformRule(name = TABLE_PAGE_CONTAINER_BACK_ACTION_DEFINITION, description = "Create BackActionDefinition for page container")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = BackActionDefinition.class)
    public TransformFunction<UIRowDeclaration, BackActionDefinition> tablePageContainerBackActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BackActionDefinition target = ctx.createTarget(BackActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerBackActionDefinition");
            target.setName(getFqName(source) + "::Back");
            LOG.debug("TablePageContainerBackActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_PAGE_CONTAINER_BACK_BUTTON, description = "Create back Button for page container")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tablePageContainerBackButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerBackButton");
            target.setName(getFqName(source) + "::Back");
            target.setLabel("Back");
            target.setButtonStyle("text");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    TABLE_PAGE_CONTAINER_BACK_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, BackActionDefinition.class,
                    TABLE_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            LOG.debug("TablePageContainerBackButton: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Visual element and frame
    // =========================================================================

    @TransformRule(name = TABLE_PAGE_CONTAINER_VISUAL_ELEMENT, description = "Create Flex container for table page")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Flex.class)
    public TransformFunction<UIRowDeclaration, Flex> tablePageContainerVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Flex target = ctx.createTarget(Flex.class);
            posMap.put(target, 0);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePageContainerVisualElement");
            target.setName(source.getName());
            target.setDirection(Axis.VERTICAL);
            target.setMainAxisAlignment(MainAxisAlignment.START);
            target.setCrossAxisAlignment(CrossAxisAlignment.STRETCH);
            target.setCol(12.0);
            target.getChildren().add(ctx.equivalent(source, Table.class, TABLE_TABLE));
            target.setFrame(ctx.equivalent(source, Frame.class, TABLE_FRAME));

            LOG.debug("TablePageContainerVisualElement: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_FRAME, description = "Create Frame for table")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Frame.class)
    public TransformFunction<UIRowDeclaration, Frame> tableFrame() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Frame target = ctx.createTarget(Frame.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableFrame");
            return target;
        };
    }

    // =========================================================================
    // TableTable (main table)
    // =========================================================================

    @TransformRule(name = TABLE_TABLE, description = "Create Table for row declaration")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Table.class)
    public TransformFunction<UIRowDeclaration, Table> tableTable() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Table target = ctx.createTarget(Table.class);
            String id = frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTable";
            ctx.setElementId(target, id);
            target.setCol(12.0);
            target.setLabel(getLabelWithNameFallback(source));
            target.setName(source.getName() + "::Table");
            target.setDataElement(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            target.setRelationName("");
            target.setIsInlineEditable(true);
            if (!posMap.containsKey(target)) {
                posMap.put(target, 0);
            }

            // Add columns and filters from primitive members
            // ETL uses equivalentDiscriminated with table id to create separate Column per table context
            // ETL: s.members.select(m | m.transferField.isDefined() and m.transferField.target.referenceType.`primitive`.isDefined())
            for (EObject member : source.getMembers()) {
                if (member instanceof UIRowColumnDeclaration) {
                    UIRowColumnDeclaration colDecl = (UIRowColumnDeclaration) member;
                    if (colDecl.getTransferField() != null
                            && colDecl.getTransferField().getTarget() != null
                            && colDecl.getTransferField().getTarget().getReferenceType() instanceof DataTypeDeclaration
                            && ((DataTypeDeclaration) colDecl.getTransferField().getTarget().getReferenceType()).getPrimitive() != null) {
                        Column col = ctx.equivalentDiscriminated(colDecl, Column.class,
                                ROW_COLUMN_DECLARATION_PRIMITIVE_COLUMN, id);
                        target.getColumns().add(col);
                        if (col.getAttributeType() != null && col.getAttributeType().isIsFilterable()) {
                            target.getFilters().add(ctx.equivalentDiscriminated(colDecl, Filter.class,
                                    ROW_COLUMN_DECLARATION_PRIMITIVE_COLUMN_FILTER, id));
                        }
                    }
                }
            }

            target.setTableActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    TABLE_TABLE_BUTTON_GROUP));
            target.setRowActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    TABLE_ROW_BUTTON_GROUP));
            target.setSelectorRowsPerPage(10);

            LOG.debug("TableTable: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Row column rules (from rowColumnDeclaration.etl)
    // =========================================================================

    @TransformRule(name = ROW_COLUMN_DECLARATION_PRIMITIVE_COLUMN, description = "Create Column for row column declaration")
    @Lazy
    @Transform(type = UIRowColumnDeclaration.class)
    @To(type = Column.class)
    public TransformFunction<UIRowColumnDeclaration, Column> rowColumnDeclarationPrimitiveColumn() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Column target = ctx.createTarget(Column.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowColumnDeclarationPrimitiveColumn");
            target.setName(source.getName());
            target.setFormat("%s");
            target.setLabel(getLabelWithNameFallback(source));
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(source, Icon.class, ROW_COLUMN_DECLARATION_COLUMN_ICON));
            }
            Modifier widthMod = getWidth(source);
            target.setWidth(widthMod != null ? String.valueOf(getModifierIntValue(widthMod)) : "120");
            target.setAttributeType(getColumnTransferFieldEquivalent(source, ctx));
            LOG.debug("RowColumnDeclarationPrimitiveColumn: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ROW_COLUMN_DECLARATION_COLUMN_ICON, description = "Create Icon for row column")
    @Lazy
    @Transform(type = UIRowColumnDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowColumnDeclaration, Icon> rowColumnDeclarationColumnIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowColumnDeclarationColumnIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "FieldIcon");
            return target;
        };
    }

    @TransformRule(name = ROW_COLUMN_DECLARATION_PRIMITIVE_COLUMN_FILTER, description = "Create Filter for row column")
    @Lazy
    @Transform(type = UIRowColumnDeclaration.class)
    @To(type = Filter.class)
    public TransformFunction<UIRowColumnDeclaration, Filter> rowColumnDeclarationPrimitiveColumnFilter() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Filter target = ctx.createTarget(Filter.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowColumnDeclarationPrimitiveColumnFilter");
            target.setName(source.getName() + "Filter");
            target.setAttributeType(getColumnTransferFieldEquivalent(source, ctx));
            target.setLabel(getLabelWithNameFallback(source));
            LOG.debug("RowColumnDeclarationPrimitiveColumnFilter: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Table-level button group and buttons
    // =========================================================================

    @TransformRule(name = TABLE_TABLE_BUTTON_GROUP, description = "Create table-level ButtonGroup")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIRowDeclaration, ButtonGroup> tableTableButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableButtonGroup");
            target.setName(source.getName() + "::TableTableButtonGroup");
            target.setLabel("Actions");

            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_TABLE_FILTER_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_TABLE_REFRESH_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_TABLE_OPEN_CREATE_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_TABLE_OPEN_ADD_SELECTOR_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_TABLE_CLEAR_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_TABLE_BULK_REMOVE_BUTTON));

            LOG.debug("TableTableButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_ROW_BUTTON_GROUP, description = "Create row-level ButtonGroup")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIRowDeclaration, ButtonGroup> tableRowButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableRowButtonGroup");
            target.setName(source.getName() + "TableRowButtonGroup");
            target.setLabel("Actions");

            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_OPEN_PAGE_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class,
                    TABLE_ROW_DELETE_BUTTON));

            // Row action declarations
            for (EObject member : source.getMembers()) {
                if (member instanceof UIActionDeclaration) {
                    UIActionDeclaration action = (UIActionDeclaration) member;
                    Button button = ctx.equivalent(action, Button.class,
                            ROW_ACTION_DECLARATION_BUTTON);
                    target.getButtons().add(button);

                    if (button.getActionDefinition() instanceof ParameterlessCallOperationActionDefinition) {
                        ParameterlessCallOperationActionDefinition callDef =
                                (ParameterlessCallOperationActionDefinition) button.getActionDefinition();
                        if (callDef.getTargetType() == null) {
                            callDef.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                                    ClassType.class, CLASS_TYPE));
                        }
                    }
                }
            }

            LOG.debug("TableRowButtonGroup: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Filter button triplet
    // =========================================================================

    @TransformRule(name = TABLE_TABLE_FILTER_BUTTON, description = "Create filter Button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableTableFilterButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableFilterButton");
            target.setName(source.getName() + "::Filter");
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_TABLE_FILTER_BUTTON_ICON));
            target.setLabel("Filter");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, FilterActionDefinition.class,
                    TABLE_TABLE_FILTER_ACTION_DEFINITION));
            LOG.debug("TableTableFilterButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_FILTER_BUTTON_ICON, description = "Create Icon for filter button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableTableFilterButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableFilterButtonIcon");
            target.setName(source.getName() + "FilterIcon");
            target.setIconName("filter");
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_FILTER_ACTION_DEFINITION, description = "Create FilterActionDefinition")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = FilterActionDefinition.class)
    public TransformFunction<UIRowDeclaration, FilterActionDefinition> tableTableFilterActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            FilterActionDefinition target = ctx.createTarget(FilterActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableFilterActionDefinition");
            target.setName(source.getName() + "::Filter");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TableTableFilterActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Refresh button triplet
    // =========================================================================

    @TransformRule(name = TABLE_TABLE_REFRESH_BUTTON, description = "Create refresh Button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableTableRefreshButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableRefreshButton");
            target.setName(source.getName() + "::Refresh");
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_TABLE_REFRESH_BUTTON_ICON));
            target.setLabel("Refresh");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, RefreshActionDefinition.class,
                    TABLE_TABLE_REFRESH_ACTION_DEFINITION));
            LOG.debug("TableTableRefreshButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_REFRESH_BUTTON_ICON, description = "Create Icon for refresh button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableTableRefreshButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableRefreshButtonIcon");
            target.setName(source.getName() + "RefreshIcon");
            target.setIconName("refresh");
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UIRowDeclaration, RefreshActionDefinition> tableTableRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableRefreshActionDefinition");
            target.setName(source.getName() + "::Refresh");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TableTableRefreshActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // OpenCreate button triplet
    // =========================================================================

    @TransformRule(name = TABLE_TABLE_OPEN_CREATE_BUTTON, description = "Create open-create Button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableTableOpenCreateButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableOpenCreateButton");
            target.setName(source.getName() + "::OpenCreate");
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_TABLE_OPEN_CREATE_BUTTON_ICON));
            target.setLabel("Create");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, OpenCreateFormActionDefinition.class,
                    TABLE_TABLE_OPEN_CREATE_ACTION_DEFINITION));
            LOG.debug("TableTableOpenCreateButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_OPEN_CREATE_BUTTON_ICON, description = "Create Icon for open-create button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableTableOpenCreateButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableOpenCreateButtonIcon");
            target.setName(source.getName() + "OpenCreateIcon");
            target.setIconName("file-document-plus");
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_OPEN_CREATE_ACTION_DEFINITION, description = "Create OpenCreateFormActionDefinition")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = OpenCreateFormActionDefinition.class)
    public TransformFunction<UIRowDeclaration, OpenCreateFormActionDefinition> tableTableOpenCreateActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenCreateFormActionDefinition target = ctx.createTarget(OpenCreateFormActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableOpenCreateActionDefinition");
            target.setName(source.getName() + "::OpenCreate");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TableTableOpenCreateActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // OpenPage button triplet (row-level)
    // =========================================================================

    @TransformRule(name = TABLE_OPEN_PAGE_BUTTON, description = "Create view Button for row")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableOpenPageButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableOpenPageButton");
            target.setName(source.getName() + "::View");
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_OPEN_PAGE_BUTTON_ICON));
            target.setLabel("View");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, OpenPageActionDefinition.class,
                    TABLE_OPEN_PAGE_ACTION_DEFINITION));
            LOG.debug("TableOpenPageButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_OPEN_PAGE_BUTTON_ICON, description = "Create Icon for view button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableOpenPageButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableOpenPageButtonIcon");
            target.setName(source.getName() + "OpenPageIcon");
            target.setIconName("visibility");
            return target;
        };
    }

    @TransformRule(name = TABLE_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition for row")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UIRowDeclaration, OpenPageActionDefinition> tableOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableOpenPageActionDefinition");
            target.setName(source.getName() + "::View");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TableOpenPageActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // RowDelete button triplet (row-level)
    // =========================================================================

    @TransformRule(name = TABLE_ROW_DELETE_BUTTON, description = "Create delete Button for row")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableRowDeleteButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableRowDeleteButton");
            target.setName(source.getName() + "::RowDelete");
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_ROW_DELETE_BUTTON_ICON));
            target.setLabel("Delete");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, RowDeleteActionDefinition.class,
                    TABLE_ROW_DELETE_ACTION_DEFINITION));
            LOG.debug("TableRowDeleteButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_ROW_DELETE_BUTTON_ICON, description = "Create Icon for row delete button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableRowDeleteButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableRowDeleteButtonIcon");
            target.setName(source.getName() + "RowDeleteIcon");
            target.setIconName("delete_forever");
            return target;
        };
    }

    @TransformRule(name = TABLE_ROW_DELETE_ACTION_DEFINITION, description = "Create RowDeleteActionDefinition")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = RowDeleteActionDefinition.class)
    public TransformFunction<UIRowDeclaration, RowDeleteActionDefinition> tableRowDeleteActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RowDeleteActionDefinition target = ctx.createTarget(RowDeleteActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableRowDeleteActionDefinition");
            target.setName(source.getName() + "::RowDelete");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("TableRowDeleteActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // OpenAddSelector button triplet
    // =========================================================================

    @TransformRule(name = TABLE_TABLE_OPEN_ADD_SELECTOR_BUTTON, description = "Create open add-selector Button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableTableOpenAddSelectorButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableOpenAddSelectorButton");
            target.setName(source.getName() + "::OpenAddSelector");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    TABLE_TABLE_OPEN_ADD_SELECTOR_BUTTON_ICON));
            target.setLabel("Add");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, OpenAddSelectorActionDefinition.class,
                    TABLE_TABLE_OPEN_ADD_SELECTOR_ACTION_DEFINITION));
            LOG.debug("TableTableOpenAddSelectorButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_OPEN_ADD_SELECTOR_BUTTON_ICON, description = "Create Icon for add-selector button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableTableOpenAddSelectorButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableOpenAddSelectorButtonIcon");
            target.setName(source.getName() + "OpenAddSelectorIcon");
            target.setIconName("attachment-plus");
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_OPEN_ADD_SELECTOR_ACTION_DEFINITION, description = "Create OpenAddSelectorActionDefinition")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = OpenAddSelectorActionDefinition.class)
    public TransformFunction<UIRowDeclaration, OpenAddSelectorActionDefinition> tableTableOpenAddSelectorActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenAddSelectorActionDefinition target = ctx.createTarget(OpenAddSelectorActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableOpenAddSelectorActionDefinition");
            target.setName(source.getName() + "::OpenAddSelector");
            target.setIsContainedRelationAction(true);
            // Note: ETL references "TableTableAddSelectorActionDefinition" which is the
            // ViewTableDeclarationAddSelectorActionDefinition in the inline table context.
            // This is set via the parent view table declaration, not on the row itself.
            LOG.debug("TableTableOpenAddSelectorActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // BulkRemove button triplet
    // =========================================================================

    @TransformRule(name = TABLE_TABLE_BULK_REMOVE_BUTTON, description = "Create bulk-remove Button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableTableBulkRemoveButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableBulkRemoveButton");
            target.setName(source.getName() + "::BulkRemove");
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_TABLE_BULK_REMOVE_BUTTON_ICON));
            target.setLabel("Remove");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, BulkRemoveActionDefinition.class,
                    TABLE_TABLE_BULK_REMOVE_ACTION_DEFINITION));
            LOG.debug("TableTableBulkRemoveButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_BULK_REMOVE_BUTTON_ICON, description = "Create Icon for bulk-remove button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableTableBulkRemoveButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableBulkRemoveButtonIcon");
            target.setName(source.getName() + "RemoveIcon");
            target.setIconName("link-off");
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_BULK_REMOVE_ACTION_DEFINITION, description = "Create BulkRemoveActionDefinition")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = BulkRemoveActionDefinition.class)
    public TransformFunction<UIRowDeclaration, BulkRemoveActionDefinition> tableTableBulkRemoveActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BulkRemoveActionDefinition target = ctx.createTarget(BulkRemoveActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableBulkRemoveActionDefinition");
            target.setName(source.getName() + "::BulkRemove");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(),
                    ClassType.class, CLASS_TYPE));
            target.setIsContainedRelationAction(true);
            target.setIsBulk(true);
            LOG.debug("TableTableBulkRemoveActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Clear button triplet
    // =========================================================================

    @TransformRule(name = TABLE_TABLE_CLEAR_BUTTON, description = "Create clear Button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIRowDeclaration, Button> tableTableClearButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableClearButton");
            target.setName(source.getName() + "::Clear");
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_TABLE_CLEAR_BUTTON_ICON));
            target.setLabel("Clear");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, ClearActionDefinition.class,
                    TABLE_TABLE_CLEAR_ACTION_DEFINITION));
            LOG.debug("TableTableClearButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_CLEAR_BUTTON_ICON, description = "Create Icon for clear button")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowDeclaration, Icon> tableTableClearButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableClearButtonIcon");
            target.setName(source.getName() + "ClearIcon");
            target.setIconName("link-off");
            return target;
        };
    }

    @TransformRule(name = TABLE_TABLE_CLEAR_ACTION_DEFINITION, description = "Create ClearActionDefinition")
    @Lazy
    @Transform(type = UIRowDeclaration.class)
    @To(type = ClearActionDefinition.class)
    public TransformFunction<UIRowDeclaration, ClearActionDefinition> tableTableClearActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ClearActionDefinition target = ctx.createTarget(ClearActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableTableClearActionDefinition");
            target.setName(source.getName() + "::Clear");
            target.setIsContainedRelationAction(true);
            LOG.debug("TableTableClearActionDefinition: {}", target.getName());
            return target;
        };
    }
}
