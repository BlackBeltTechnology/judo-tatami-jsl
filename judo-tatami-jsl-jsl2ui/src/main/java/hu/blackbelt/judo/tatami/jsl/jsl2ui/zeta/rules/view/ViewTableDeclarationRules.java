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
import hu.blackbelt.judo.meta.ui.data.RelationType;
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
 * Ported from viewTableDeclaration.etl:
 * InlineViewTable rule for row-based tables with columns, filters, button groups.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ViewTableDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewTableDeclarationRules.class);

    // =========================================================================
    // Shared lazy rules
    // =========================================================================

    @TransformRule(name = TABLE_ICON, description = "Create Icon for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> tableIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TableIcon");
            target.setName(source.getName() + "TableIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            return target;
        };
    }

    // =========================================================================
    // InlineViewTable (row-based tables)
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_TABLE, description = "Create Table for row-based UIViewTableDeclaration")
    @Greedy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Table.class)
    public TransformFunction<UIViewTableDeclaration, Table> inlineViewTable() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (!(source.getReferenceType() instanceof UIRowDeclaration)) return null;

            Table target = ctx.createTarget(Table.class);
            String id = frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTable";
            ctx.setElementId(target, id);

            // Apply abstract table logic
            applyAbstractTableDeclaration(source, target, ctx);

            target.setDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));

            // Add to parent container
            VisualElement container = resolveUiContainer(source.eContainer(), ctx);
            if (container instanceof Flex) {
                ((Flex) container).getChildren().add(target);
            }

            // Add columns from row declaration
            // ETL uses equivalentDiscriminated with table id to create separate Column per table context
            UIRowDeclaration row = (UIRowDeclaration) source.getReferenceType();
            for (EObject member : row.getMembers()) {
                if (member instanceof UIRowColumnDeclaration) {
                    UIRowColumnDeclaration colDecl = (UIRowColumnDeclaration) member;
                    if (colDecl.getReferenceType() instanceof DataTypeDeclaration
                            && ((DataTypeDeclaration) colDecl.getReferenceType()).getPrimitive() != null) {
                        Column col = ctx.equivalentDiscriminated(colDecl, Column.class,
                                TABLE_PRIMITIVE_COLUMN, id);
                        target.getColumns().add(col);
                        if (col.getAttributeType() != null && col.getAttributeType().isIsFilterable()) {
                            target.getFilters().add(ctx.equivalentDiscriminated(colDecl, Filter.class,
                                    TABLE_PRIMITIVE_COLUMN_FILTER, id));
                        }
                    }
                }
            }

            // Button groups
            target.setTableActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    INLINE_VIEW_TABLE_BUTTON_GROUP));
            target.setRowActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    INLINE_VIEW_TABLE_ROW_BUTTON_GROUP));

            target.setSelectorRowsPerPage(10);

            LOG.debug("InlineViewTable: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Column rules
    // =========================================================================

    @TransformRule(name = TABLE_PRIMITIVE_COLUMN, description = "Create Column for row column declaration")
    @Lazy
    @Transform(type = UIRowColumnDeclaration.class)
    @To(type = Column.class)
    public TransformFunction<UIRowColumnDeclaration, Column> tablePrimitiveColumn() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Column target = ctx.createTarget(Column.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePrimitiveColumn");
            target.setName(source.getName());
            target.setFormat("%s");
            target.setLabel(getLabelWithNameFallback(source));
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(source, Icon.class, COLUMN_ICON));
            }
            Modifier widthMod = getWidth(source);
            target.setWidth(widthMod != null ? String.valueOf(getModifierIntValue(widthMod)) : "120");
            target.setAttributeType(getColumnTransferFieldEquivalent(source, ctx));
            LOG.debug("TablePrimitiveColumn: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = COLUMN_ICON, description = "Create Icon for column")
    @Lazy
    @Transform(type = UIRowColumnDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIRowColumnDeclaration, Icon> columnIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ColumnIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "FieldIcon");
            return target;
        };
    }

    @TransformRule(name = TABLE_PRIMITIVE_COLUMN_FILTER, description = "Create Filter for column")
    @Lazy
    @Transform(type = UIRowColumnDeclaration.class)
    @To(type = Filter.class)
    public TransformFunction<UIRowColumnDeclaration, Filter> tablePrimitiveColumnFilter() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Filter target = ctx.createTarget(Filter.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TablePrimitiveColumnFilter");
            target.setName(source.getName() + "Filter");
            target.setAttributeType(getColumnTransferFieldEquivalent(source, ctx));
            target.setLabel(getLabelWithNameFallback(source));
            LOG.debug("TablePrimitiveColumnFilter: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Table button groups
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_TABLE_BUTTON_GROUP, description = "Create table-level ButtonGroup")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewTableDeclaration, ButtonGroup> inlineViewTableButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTableButtonGroup");
            target.setName(source.getName() + "::InlineViewTableButtonGroup");
            target.setLabel("Actions");

            TransferRelationDeclaration relation = source.getTransferRelation().getTarget();

            if (isFilterSupported(relation)) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_FILTER_BUTTON));
            }
            if (isRefreshAllowed(relation)) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_REFRESH_BUTTON));
            }
            if (getCreateFormModifier(source) != null) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_OPEN_CREATE_BUTTON));
            }
            if (getSelectorTableModifier(source) != null) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_BUTTON));
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_CLEAR_BUTTON));
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_BULK_REMOVE_BUTTON));
            }

            // Table-level action buttons from ActionGroupModifier
            ActionGroupModifier actionGroupMod = getActionGroupModifier(source);
            if (actionGroupMod != null) {
                for (UIActionDeclaration action : actionGroupMod.getActions()) {
                    target.getButtons().add(ctx.equivalent(action, Button.class,
                            VIEW_TABLE_ACTION_DECLARATION_BUTTON));
                }
            }

            LOG.debug("InlineViewTableButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TABLE_ROW_BUTTON_GROUP, description = "Create row-level ButtonGroup")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewTableDeclaration, ButtonGroup> inlineViewTableRowButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTableRowButtonGroup");
            target.setName(source.getName() + "InlineViewTableRowButtonGroup");
            target.setLabel("Actions");

            if (getUpdateViewModifier(source) != null) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_OPEN_PAGE_BUTTON));
            }
            if (isDeleteAllowed(source.getTransferRelation().getTarget())) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_TABLE_DECLARATION_ROW_DELETE_BUTTON));
            }

            // Row action declaration buttons from the row type
            if (source.getReferenceType() instanceof UIRowDeclaration) {
                UIRowDeclaration row = (UIRowDeclaration) source.getReferenceType();
                for (EObject member : row.getMembers()) {
                    if (member instanceof UIActionDeclaration) {
                        target.getButtons().add(ctx.equivalentDiscriminated(member,
                                Button.class, VIEW_TABLE_ACTION_DECLARATION_BUTTON,
                                getJslId(source)));
                    }
                }
            }

            LOG.debug("InlineViewTableRowButtonGroup: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Filter button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_FILTER_BUTTON, description = "Create filter button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationFilterButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationFilterButton");
            target.setName(source.getName() + "::Filter");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_FILTER_BUTTON_ICON));
            target.setLabel("Filter");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, FilterActionDefinition.class,
                    VIEW_TABLE_DECLARATION_FILTER_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_FILTER_BUTTON_ICON, description = "Create filter button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationFilterButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationFilterButtonIcon");
            target.setName(source.getName() + "FilterIcon");
            target.setIconName("filter");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_FILTER_ACTION_DEFINITION, description = "Create FilterActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = FilterActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, FilterActionDefinition> viewTableDeclarationFilterActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            FilterActionDefinition target = ctx.createTarget(FilterActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationFilterActionDefinition");
            target.setName(source.getName() + "::Filter");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // Refresh button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_REFRESH_BUTTON, description = "Create refresh button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationRefreshButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRefreshButton");
            target.setName(source.getName() + "::Refresh");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_REFRESH_BUTTON_ICON));
            target.setLabel("Refresh");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, RefreshActionDefinition.class,
                    VIEW_TABLE_DECLARATION_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_REFRESH_BUTTON_ICON, description = "Create refresh button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationRefreshButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRefreshButtonIcon");
            target.setName(source.getName() + "RefreshIcon");
            target.setIconName("refresh");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, RefreshActionDefinition> viewTableDeclarationRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRefreshActionDefinition");
            target.setName(source.getName() + "::Refresh");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // OpenPage (View) button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_PAGE_BUTTON, description = "Create open page button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationOpenPageButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenPageButton");
            target.setName(source.getName() + "::View");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_OPEN_PAGE_BUTTON_ICON));
            target.setLabel("View");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, OpenPageActionDefinition.class,
                    VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_PAGE_BUTTON_ICON, description = "Create open page button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationOpenPageButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenPageButtonIcon");
            target.setName(source.getName() + "OpenPageIcon");
            target.setIconName("visibility");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, OpenPageActionDefinition> viewTableDeclarationOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenPageActionDefinition");
            target.setName(source.getName() + "::View");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // Row Delete button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_ROW_DELETE_BUTTON, description = "Create row delete button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationRowDeleteButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRowDeleteButton");
            target.setName(source.getName() + "::RowDelete");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_ROW_DELETE_BUTTON_ICON));
            target.setLabel("Delete");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, RowDeleteActionDefinition.class,
                    VIEW_TABLE_DECLARATION_ROW_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_ROW_DELETE_BUTTON_ICON, description = "Create row delete button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationRowDeleteButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRowDeleteButtonIcon");
            target.setName(source.getName() + "RowDeleteIcon");
            target.setIconName("delete_forever");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_ROW_DELETE_ACTION_DEFINITION, description = "Create RowDeleteActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = RowDeleteActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, RowDeleteActionDefinition> viewTableDeclarationRowDeleteActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RowDeleteActionDefinition target = ctx.createTarget(RowDeleteActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRowDeleteActionDefinition");
            target.setName(source.getName() + "::RowDelete");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // OpenCreate button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_CREATE_BUTTON, description = "Create open create button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationOpenCreateButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenCreateButton");
            target.setName(source.getName() + "::OpenCreate");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_OPEN_CREATE_BUTTON_ICON));
            target.setLabel("Create");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, OpenCreateFormActionDefinition.class,
                    VIEW_TABLE_DECLARATION_OPEN_CREATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_CREATE_BUTTON_ICON, description = "Create open create button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationOpenCreateButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenCreateButtonIcon");
            target.setName(source.getName() + "OpenCreateIcon");
            target.setIconName("file-document-plus");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_CREATE_ACTION_DEFINITION, description = "Create OpenCreateFormActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = OpenCreateFormActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, OpenCreateFormActionDefinition> viewTableDeclarationOpenCreateActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenCreateFormActionDefinition target = ctx.createTarget(OpenCreateFormActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenCreateActionDefinition");
            target.setName(source.getName() + "::OpenCreate");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // OpenAddSelector button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_BUTTON, description = "Create add selector button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationOpenAddSelectorButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenAddSelectorButton");
            target.setName(source.getName() + "::OpenAddSelector");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_BUTTON_ICON));
            target.setLabel("Add");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, OpenAddSelectorActionDefinition.class,
                    VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_BUTTON_ICON, description = "Create add selector button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationOpenAddSelectorButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenAddSelectorButtonIcon");
            target.setName(source.getName() + "OpenAddSelectorIcon");
            target.setIconName("attachment-plus");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION_DEFINITION, description = "Create OpenAddSelectorActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = OpenAddSelectorActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, OpenAddSelectorActionDefinition> viewTableDeclarationOpenAddSelectorActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenAddSelectorActionDefinition target = ctx.createTarget(OpenAddSelectorActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenAddSelectorActionDefinition");
            target.setName(source.getName() + "::OpenAddSelector");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // Clear button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_CLEAR_BUTTON, description = "Create clear button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationClearButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationClearButton");
            target.setName(source.getName() + "::Clear");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_CLEAR_BUTTON_ICON));
            target.setLabel("Clear");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, ClearActionDefinition.class,
                    VIEW_TABLE_DECLARATION_CLEAR_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_CLEAR_BUTTON_ICON, description = "Create clear button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationClearButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationClearButtonIcon");
            target.setName(source.getName() + "ClearIcon");
            target.setIconName("link-off");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_CLEAR_ACTION_DEFINITION, description = "Create ClearActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = ClearActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, ClearActionDefinition> viewTableDeclarationClearActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ClearActionDefinition target = ctx.createTarget(ClearActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationClearActionDefinition");
            target.setName(source.getName() + "::Clear");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // BulkRemove button/action
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_BULK_REMOVE_BUTTON, description = "Create bulk remove button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> viewTableDeclarationBulkRemoveButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationBulkRemoveButton");
            target.setName(source.getName() + "::BulkRemove");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_TABLE_DECLARATION_BULK_REMOVE_BUTTON_ICON));
            target.setLabel("Remove");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, BulkRemoveActionDefinition.class,
                    VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_BULK_REMOVE_BUTTON_ICON, description = "Create bulk remove button icon")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> viewTableDeclarationBulkRemoveButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationBulkRemoveButtonIcon");
            target.setName(source.getName() + "RemoveIcon");
            target.setIconName("link-off");
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION_DEFINITION, description = "Create BulkRemoveActionDefinition")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = BulkRemoveActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, BulkRemoveActionDefinition> viewTableDeclarationBulkRemoveActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BulkRemoveActionDefinition target = ctx.createTarget(BulkRemoveActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationBulkRemoveActionDefinition");
            target.setName(source.getName() + "::BulkRemove");
            target.setIsContainedRelationAction(true);
            target.setIsBulk(true);
            return target;
        };
    }

    // =========================================================================
    // View table create form page rules — ported from viewTableDeclarationFormPage.etl
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_CREATE_FORM_PAGE_DEFINITION, description = "Create PageDefinition for create form page")
    @Greedy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIViewTableDeclaration, PageDefinition> viewTableCreateFormPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (getCreateFormModifier(source) == null) return null;
            if (!(source.getReferenceType() instanceof UIRowDeclaration)) return null;

            TransferRelationDeclaration relation = source.getTransferRelation().getTarget();
            UIViewDeclaration form = getCreateFormModifier(source).getForm();

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableCreateFormPageDefinition");
            target.setName(getFqName(source) + "::FormPage");
            target.setOpenInDialog(true);
            target.setContainer(ctx.equivalent(form, PageContainer.class, FORM_PAGE_CONTAINER));

            RelationType relType = ctx.equivalent(relation, RelationType.class, RELATION_TYPE);
            target.setDataElement(relType);

            // Link processing
            for (UIViewLinkDeclaration link : getOwnLinks(form)) {
                target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                        VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION, getJslId(source)));

                if (getSelectorTableModifier(link) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_DIALOG_ACTION, getJslId(source)));
                }
                if (getSelectorTableModifier(link) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_UNSET_ACTION, getJslId(source)));
                }
            }

            // Table processing
            for (UIViewTableDeclaration table : getOwnTables(form)) {
                TransferRelationDeclaration tRelation = table.getTransferRelation().getTarget();

                if (getUpdateViewModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION, getJslId(source)));
                }
                if (isFilterSupported(tRelation)) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_FILTER_ACTION, getJslId(source)));
                }
                if (getSelectorTableModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION, getJslId(source)));
                }
                if (getSelectorTableModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_CLEAR_ACTION, getJslId(source)));
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION, getJslId(source)));
                }
                if (table.getReferenceType() instanceof UITagDeclaration) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION, getJslId(source)));
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION, getJslId(source)));
                }
            }

            // Standard actions
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_TABLE_CREATE_FORM_BACK_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_TABLE_CREATE_FORM_CREATE_ACTION));

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("ViewTableCreateFormPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_CREATE_FORM_CREATE_ACTION, description = "Create create action for form page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableCreateFormCreateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableCreateFormCreateAction");
            target.setName(source.getName() + "::Create");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_CREATE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source, PageDefinition.class,
                    VIEW_TABLE_VIEW_PAGE_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_CREATE_FORM_BACK_ACTION, description = "Create back action for form page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableCreateFormBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableCreateFormBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Shared helper
    // =========================================================================

    private void applyAbstractTableDeclaration(UIViewTableDeclaration source, Table target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());
        if (source.getTransferRelation() != null && source.getTransferRelation().getTarget() != null) {
            target.setRelationName(source.getTransferRelation().getTarget().getName());
        }
        LabelModifier labelMod = getLabelModifier(source);
        if (labelMod != null) {
            target.setLabel(labelMod.getValue().getValue());
        }
        IconModifier iconMod = getIconModifier(source);
        if (iconMod != null) {
            target.setIcon(ctx.equivalent(source, Icon.class, TABLE_ICON));
        }
        target.setRow(1.0);
        Modifier widthMod = getWidth(source);
        target.setCol(widthMod != null ? getModifierDoubleValue(widthMod) : 12.0);

        if (source.getTransferRelation() != null && source.getTransferRelation().getTarget() != null) {
            target.setIsEager(isEager(source.getTransferRelation().getTarget()));
            target.setIsInlineEditable(target.isIsEager());
        }

        ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
        if (posMap.containsKey(source)) {
            posMap.put(target, posMap.get(source));
        } else {
            posMap.put(target, 0);
        }
    }

    // =========================================================================
    // InlineViewCards (card-based tables) — greedy rule
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_CARDS, description = "Create Table for card-based UIViewTableDeclaration")
    @Greedy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Table.class)
    public TransformFunction<UIViewTableDeclaration, Table> inlineViewCards() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (!(source.getReferenceType() instanceof UICardDeclaration)) return null;

            Table target = ctx.createTarget(Table.class);
            String id = frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCards";
            ctx.setElementId(target, id);

            applyAbstractTableDeclaration(source, target, ctx);

            target.setDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));

            VisualElement container = resolveUiContainer(source.eContainer(), ctx);
            if (container instanceof Flex) {
                ((Flex) container).getChildren().add(target);
            }

            target.setRepresentationComponent(TableRepresentation.CARD);

            UICardDeclaration card = (UICardDeclaration) source.getReferenceType();
            for (EObject member : card.getMembers()) {
                if (member instanceof UIViewWidgetDeclaration) {
                    UIViewWidgetDeclaration widget = (UIViewWidgetDeclaration) member;
                    if (widget.getTransferField() != null
                            && widget.getTransferField().getTarget() != null
                            && widget.getTransferField().getTarget().getReferenceType() != null
                            && ((DataTypeDeclaration) widget.getTransferField().getTarget().getReferenceType()).getPrimitive() != null) {
                        Column col = ctx.equivalentDiscriminated(widget, Column.class,
                                CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN, id);
                        target.getColumns().add(col);
                        if (col.getAttributeType() != null && col.getAttributeType().isIsFilterable()) {
                            target.getFilters().add(ctx.equivalentDiscriminated(widget, Filter.class,
                                    CARD_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER, id));
                        }
                    }
                }
            }

            target.setTableActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    INLINE_VIEW_CARDS_BUTTON_GROUP));
            target.setRowActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    INLINE_VIEW_CARDS_ROW_BUTTON_GROUP));

            target.setSelectorRowsPerPage(10);

            LOG.debug("InlineViewCards: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // InlineViewTags (tag-based tables) — greedy rule
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_TAGS, description = "Create Table for tag-based UIViewTableDeclaration")
    @Greedy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Table.class)
    public TransformFunction<UIViewTableDeclaration, Table> inlineViewTags() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (!(source.getReferenceType() instanceof UITagDeclaration)) return null;

            Table target = ctx.createTarget(Table.class);
            String id = frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTags";
            ctx.setElementId(target, id);

            applyAbstractTableDeclaration(source, target, ctx);

            target.setDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));

            VisualElement container = resolveUiContainer(source.eContainer(), ctx);
            if (container instanceof Flex) {
                ((Flex) container).getChildren().add(target);
            }

            target.setRepresentationComponent(TableRepresentation.CARD);
            target.setLabel(getLabelWithNameFallback(source));

            UITagDeclaration tag = (UITagDeclaration) source.getReferenceType();
            Column col = ctx.equivalentDiscriminated(tag, Column.class,
                    TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN, id);
            target.getColumns().add(col);
            if (col.getAttributeType() != null && col.getAttributeType().isIsFilterable()) {
                target.getFilters().add(ctx.equivalentDiscriminated(tag, Filter.class,
                        TAG_WIDGET_DECLARATION_PRIMITIVE_COLUMN_FILTER, id));
            }

            target.setTableActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    INLINE_VIEW_TAGS_BUTTON_GROUP));
            target.setRowActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    INLINE_VIEW_TAGS_ROW_BUTTON_GROUP));

            target.setAutocompleteRangeActionDefinition(ctx.equivalent(source,
                    AutocompleteRangeActionDefinition.class, VIEW_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION_DEFINITION));
            target.setAutocompleteAddActionDefinition(ctx.equivalent(source,
                    AutocompleteAddActionDefinition.class, VIEW_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION_DEFINITION));

            target.setSelectorRowsPerPage(10);

            LOG.debug("InlineViewTags: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Card ButtonGroups
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_CARDS_BUTTON_GROUP, description = "Create table-level ButtonGroup for inline cards")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewTableDeclaration, ButtonGroup> inlineViewCardsButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsButtonGroup");
            target.setName(source.getName() + "::InlineViewCardsButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class, INLINE_VIEW_CARDS_FILTER_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class, INLINE_VIEW_CARDS_REFRESH_BUTTON));
            LOG.debug("InlineViewCardsButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_ROW_BUTTON_GROUP, description = "Create row-level ButtonGroup for inline cards")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewTableDeclaration, ButtonGroup> inlineViewCardsRowButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsRowButtonGroup");
            target.setName(source.getName() + "TableRowButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class, INLINE_VIEW_CARDS_OPEN_PAGE_BUTTON));
            LOG.debug("InlineViewCardsRowButtonGroup: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Card Buttons + Icons + ActionDefinitions
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_CARDS_FILTER_BUTTON, description = "Create filter Button for inline cards")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> inlineViewCardsFilterButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsFilterButton");
            target.setName(source.getName() + "::Filter");
            target.setIcon(ctx.equivalent(source, Icon.class, INLINE_VIEW_CARDS_FILTER_BUTTON_ICON));
            target.setLabel("Filter");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, FilterActionDefinition.class,
                    INLINE_VIEW_CARDS_FILTER_BUTTON_ACTION_DEFINITION));
            LOG.debug("InlineViewCardsFilterButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_FILTER_BUTTON_ICON, description = "Create Icon for inline cards filter button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> inlineViewCardsFilterButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsFilterButtonIcon");
            target.setName(source.getName() + "FilterIcon");
            target.setIconName("filter");
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_FILTER_BUTTON_ACTION_DEFINITION, description = "Create FilterActionDefinition for inline cards")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = FilterActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, FilterActionDefinition> inlineViewCardsFilterButtonActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            FilterActionDefinition target = ctx.createTarget(FilterActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsFilterButtonActionDefinition");
            target.setName(source.getName() + "::Filter");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_REFRESH_BUTTON, description = "Create refresh Button for inline cards")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> inlineViewCardsRefreshButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsRefreshButton");
            target.setName(source.getName() + "::Refresh");
            target.setIcon(ctx.equivalent(source, Icon.class, INLINE_VIEW_CARDS_REFRESH_BUTTON_ICON));
            target.setLabel("Refresh");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, RefreshActionDefinition.class,
                    INLINE_VIEW_CARDS_REFRESH_ACTION_DEFINITION));
            LOG.debug("InlineViewCardsRefreshButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_REFRESH_BUTTON_ICON, description = "Create Icon for inline cards refresh button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> inlineViewCardsRefreshButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsRefreshButtonIcon");
            target.setName(source.getName() + "RefreshIcon");
            target.setIconName("refresh");
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition for inline cards")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, RefreshActionDefinition> inlineViewCardsRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsRefreshActionDefinition");
            target.setName(source.getName() + "::Refresh");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_OPEN_PAGE_BUTTON, description = "Create view Button for inline cards row")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> inlineViewCardsOpenPageButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsOpenPageButton");
            target.setName(source.getName() + "::View");
            target.setIcon(ctx.equivalent(source, Icon.class, INLINE_VIEW_CARDS_OPEN_PAGE_BUTTON_ICON));
            target.setLabel("View");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, OpenPageActionDefinition.class,
                    INLINE_VIEW_CARDS_OPEN_PAGE_ACTION_DEFINITION));
            LOG.debug("InlineViewCardsOpenPageButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_OPEN_PAGE_BUTTON_ICON, description = "Create Icon for inline cards view button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> inlineViewCardsOpenPageButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsOpenPageButtonIcon");
            target.setName(source.getName() + "OpenPageIcon");
            target.setIconName("visibility");
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_CARDS_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition for inline cards")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, OpenPageActionDefinition> inlineViewCardsOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewCardsOpenPageActionDefinition");
            target.setName(source.getName() + "::View");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    // =========================================================================
    // Tag ButtonGroups
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_TAGS_BUTTON_GROUP, description = "Create table-level ButtonGroup for inline tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewTableDeclaration, ButtonGroup> inlineViewTagsButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsButtonGroup");
            target.setName(source.getName() + "::InlineViewTagsButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class, INLINE_VIEW_TAGS_FILTER_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class, INLINE_VIEW_TAGS_REFRESH_BUTTON));
            LOG.debug("InlineViewTagsButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_ROW_BUTTON_GROUP, description = "Create row-level ButtonGroup for inline tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewTableDeclaration, ButtonGroup> inlineViewTagsRowButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsRowButtonGroup");
            target.setName(source.getName() + "TableRowButtonGroup");
            target.setLabel("Actions");
            target.getButtons().add(ctx.equivalent(source, Button.class, INLINE_VIEW_TAGS_OPEN_PAGE_BUTTON));
            LOG.debug("InlineViewTagsRowButtonGroup: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Tag Buttons + Icons + ActionDefinitions
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_TAGS_FILTER_BUTTON, description = "Create filter Button for inline tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> inlineViewTagsFilterButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsFilterButton");
            target.setName(source.getName() + "::Filter");
            target.setIcon(ctx.equivalent(source, Icon.class, INLINE_VIEW_TAGS_FILTER_BUTTON_ICON));
            target.setLabel("Filter");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, FilterActionDefinition.class,
                    INLINE_VIEW_TAGS_FILTER_ACTION_DEFINITION));
            LOG.debug("InlineViewTagsFilterButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_FILTER_BUTTON_ICON, description = "Create Icon for inline tags filter button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> inlineViewTagsFilterButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsFilterButtonIcon");
            target.setName(source.getName() + "FilterIcon");
            target.setIconName("filter");
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_FILTER_ACTION_DEFINITION, description = "Create FilterActionDefinition for inline tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = FilterActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, FilterActionDefinition> inlineViewTagsFilterActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            FilterActionDefinition target = ctx.createTarget(FilterActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsFilterActionDefinition");
            target.setName(source.getName() + "::Filter");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_REFRESH_BUTTON, description = "Create refresh Button for inline tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> inlineViewTagsRefreshButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsRefreshButton");
            target.setName(source.getName() + "::Refresh");
            target.setIcon(ctx.equivalent(source, Icon.class, INLINE_VIEW_TAGS_REFRESH_BUTTON_ICON));
            target.setLabel("Refresh");
            target.setButtonStyle("text");
            target.setActionDefinition(ctx.equivalent(source, RefreshActionDefinition.class,
                    INLINE_VIEW_TAGS_REFRESH_ACTION_DEFINITION));
            LOG.debug("InlineViewTagsRefreshButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_REFRESH_BUTTON_ICON, description = "Create Icon for inline tags refresh button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> inlineViewTagsRefreshButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsRefreshButtonIcon");
            target.setName(source.getName() + "RefreshIcon");
            target.setIconName("refresh");
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition for inline tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, RefreshActionDefinition> inlineViewTagsRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsRefreshActionDefinition");
            target.setName(source.getName() + "::Refresh");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_OPEN_PAGE_BUTTON, description = "Create view Button for inline tags row")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewTableDeclaration, Button> inlineViewTagsOpenPageButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsOpenPageButton");
            target.setName(source.getName() + "::View");
            target.setIcon(ctx.equivalent(source, Icon.class, INLINE_VIEW_TAGS_OPEN_PAGE_BUTTON_ICON));
            target.setLabel("View");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source, OpenPageActionDefinition.class,
                    INLINE_VIEW_TAGS_OPEN_PAGE_ACTION_DEFINITION));
            LOG.debug("InlineViewTagsOpenPageButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_OPEN_PAGE_BUTTON_ICON, description = "Create Icon for inline tags view button")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTableDeclaration, Icon> inlineViewTagsOpenPageButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsOpenPageButtonIcon");
            target.setName(source.getName() + "OpenPageIcon");
            target.setIconName("visibility");
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_TAGS_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition for inline tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, OpenPageActionDefinition> inlineViewTagsOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewTagsOpenPageActionDefinition");
            target.setName(source.getName() + "::View");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    // =========================================================================
    // Tag Autocomplete ActionDefinitions
    // =========================================================================

    @TransformRule(name = VIEW_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION_DEFINITION, description = "Create AutocompleteRangeActionDefinition for tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = AutocompleteRangeActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, AutocompleteRangeActionDefinition> viewTagsDeclarationAutocompleteRangeActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            AutocompleteRangeActionDefinition target = ctx.createTarget(AutocompleteRangeActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTagsDeclarationAutocompleteRangeActionDefinition");
            target.setName(source.getName() + "::Autocomplete");
            target.setIsContainedRelationAction(true);
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION_DEFINITION, description = "Create AutocompleteAddActionDefinition for tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = AutocompleteAddActionDefinition.class)
    public TransformFunction<UIViewTableDeclaration, AutocompleteAddActionDefinition> viewTagsDeclarationAutocompleteAddActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            AutocompleteAddActionDefinition target = ctx.createTarget(AutocompleteAddActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTagsDeclarationAutocompleteAddActionDefinition");
            target.setName(source.getName() + "::AutocompleteAdd");
            target.setIsContainedRelationAction(true);
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    ClassType.class, CLASS_TYPE));
            return target;
        };
    }
}
