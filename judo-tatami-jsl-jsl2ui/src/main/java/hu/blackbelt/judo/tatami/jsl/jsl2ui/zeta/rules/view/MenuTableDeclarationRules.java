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
import hu.blackbelt.judo.meta.ui.data.MemberType;
import hu.blackbelt.judo.meta.ui.data.OperationType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from menuTableDeclaration.etl and menuTableDeclarationTablePage.etl:
 * Creates NavigationItem and PageDefinition for menu table declarations.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class MenuTableDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(MenuTableDeclarationRules.class);

    // =========================================================================
    // MenuTableNavigationItem (@greedy)
    // =========================================================================

    @TransformRule(name = MENU_TABLE_NAVIGATION_ITEM, description = "Create NavigationItem for menu table")
    @Greedy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = NavigationItem.class)
    public TransformFunction<UIMenuTableDeclaration, NavigationItem> menuTableNavigationItem() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            NavigationItem target = ctx.createTarget(NavigationItem.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/MenuTableNavigationItem");
            target.setName(getFqName(source));
            target.setLabel(getLabelWithNameFallback(source));

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(iconMod, Icon.class, ICON_MODIFIER_ICON));
            }

            target.setTarget(ctx.equivalent(source, PageDefinition.class,
                    ACCESS_TABLE_PAGE_DEFINITION));

            // Add to parent: menu group or navigation controller
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            if (source.eContainer() instanceof UIMenuGroupDeclaration) {
                NavigationItem parentGroup = ctx.equivalent(source.eContainer(),
                        NavigationItem.class, MENU_ITEM_GROUP);
                parentGroup.getItems().add(target);
                posMap.put(target, indexInParentMembers(source));
            } else {
                Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
                app.getNavigationController().getItems().add(target);
                posMap.put(target, indexInParentMembers(source));
            }

            LOG.debug("MenuTableNavigationItem: {}", source.getName());
            return target;
        };
    }

    // =========================================================================
    // AccessTablePageDefinition (@greedy)
    // =========================================================================

    @TransformRule(name = ACCESS_TABLE_PAGE_DEFINITION, description = "Create PageDefinition for table access page")
    @Greedy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIMenuTableDeclaration, PageDefinition> accessTablePageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (!(source.getReferenceType() instanceof UIRowDeclaration)) return null;

            TransferRelationDeclaration relation = source.getActorAccess().getTarget();

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTablePageDefinition");
            target.setName(getFqName(source) + "::AccessTablePage");
            target.setContainer(ctx.equivalent(source.getReferenceType(),
                    PageContainer.class, TABLE_PAGE_CONTAINER));

            RelationType relType = ctx.equivalent(relation, RelationType.class, RELATION_TYPE);
            target.setDataElement(relType);
            relType.setMemberType(MemberType.ACCESS);

            target.setDashboard(isDashBoard(source));

            // Conditional actions
            if (getUpdateViewModifier(source) != null) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_OPEN_PAGE_ACTION));
            }
            if (isRefreshAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_TABLE_REFRESH_ACTION));
            }
            if (isDeleteAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_ROW_DELETE_ACTION));
            }
            if (isFilterSupported(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_TABLE_FILTER_ACTION));
            }
            if (getCreateFormModifier(source) != null) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_TABLE_OPEN_CREATE_ACTION));
            }

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("AccessTablePageDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Access table lazy action rules
    // =========================================================================

    @TransformRule(name = ACCESS_TABLE_BACK_ACTION, description = "Create back action for table page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, TABLE_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_TABLE_OPEN_CREATE_ACTION, description = "Create open create action for table page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableTableOpenCreateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableTableOpenCreateAction");
            target.setName(source.getName() + "::OpenCreate");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, TABLE_TABLE_OPEN_CREATE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source, PageDefinition.class,
                    ACCESS_TABLE_CREATE_FORM_PAGE_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_TABLE_REFRESH_ACTION, description = "Create refresh action for table page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableTableRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableTableRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, TABLE_TABLE_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_TABLE_FILTER_ACTION, description = "Create filter action for table page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableTableFilterAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableTableFilterAction");
            target.setName(source.getName() + "::Filter");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, TABLE_TABLE_FILTER_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_OPEN_PAGE_ACTION, description = "Create open page action for table page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableOpenPageAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableOpenPageAction");
            target.setName(source.getName() + "::OpenPage");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, TABLE_OPEN_PAGE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source, PageDefinition.class,
                    ACCESS_TABLE_VIEW_PAGE_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_ROW_DELETE_ACTION, description = "Create row delete action for table page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableRowDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableRowDeleteAction");
            target.setName(source.getName() + "::RowDelete");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, TABLE_ROW_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // AccessTableViewPageDefinition (@greedy) — ported from menuTableDeclarationViewPage.etl
    // =========================================================================

    @TransformRule(name = ACCESS_TABLE_VIEW_PAGE_DEFINITION, description = "Create PageDefinition for table view page")
    @Greedy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIMenuTableDeclaration, PageDefinition> accessTableViewPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (getUpdateViewModifier(source) == null) return null;

            TransferRelationDeclaration relation = source.getActorAccess().getTarget();

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableViewPageDefinition");
            target.setName(getFqName(source) + "::AccessTableViewPage");

            target.setContainer(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    PageContainer.class, VIEW_PAGE_CONTAINER));

            RelationType relType = ctx.equivalent(relation, RelationType.class, RELATION_TYPE);
            target.setDataElement(relType);
            relType.setMemberType(MemberType.ACCESS);

            if (isOpenInDialog(getUpdateViewModifier(source).getView())) {
                target.setOpenInDialog(true);
            }

            UIViewDeclaration view = getUpdateViewModifier(source).getView();
            List<UIActionDeclaration> actionDeclarationsToProcess = new ArrayList<>(getAllActionDeclarations(view));

            // Link processing
            for (UIViewLinkDeclaration link : getOwnLinks(view)) {
                TransferRelationDeclaration lRelation = link.getTransferRelation().getTarget();

                target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                        VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION, getJslId(source)));
                if (isRefreshAllowed(lRelation) && !isEager(lRelation)) {
                    if (link.isButton()) {
                        target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                                VIEW_LINK_DECLARATION_PRE_FETCH_ACTION, getJslId(source)));
                    } else {
                        target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                                VIEW_LINK_DECLARATION_REFRESH_ACTION, getJslId(source)));
                    }
                }
                if (getCreateFormModifier(link) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_OPEN_FORM_ACTION, getJslId(source)));
                }
                if (isDeleteAllowed(lRelation) && !link.isButton()) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_ROW_DELETE_ACTION, getJslId(source)));
                }
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
            for (UIViewTableDeclaration table : getOwnTables(view)) {
                TransferRelationDeclaration tRelation = table.getTransferRelation().getTarget();

                if (getUpdateViewModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION, getJslId(source)));
                }
                if (isFilterSupported(tRelation)) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_FILTER_ACTION, getJslId(source)));
                }
                if (isRefreshAllowed(tRelation)) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_REFRESH_ACTION, getJslId(source)));
                }
                if (getCreateFormModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_OPEN_CREATE_ACTION, getJslId(source)));
                }
                if (isDeleteAllowed(tRelation)) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_ROW_DELETE_ACTION, getJslId(source)));
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

                for (UIActionDeclaration actionDeclaration : getAllActionDeclarations(table)) {
                    Action viewAction = ctx.equivalentDiscriminated(actionDeclaration, Action.class,
                            VIEW_ACTION, getJslId(source));
                    viewAction.setOwnerDataElement(ctx.equivalent(tRelation, RelationType.class, RELATION_TYPE));
                    viewAction.setTargetDataElement(ctx.equivalent(actionDeclaration.getTransferAction().getTarget(),
                            OperationType.class, OPERATION_TYPE));
                    target.getActions().add(viewAction);
                    actionDeclarationsToProcess.remove(actionDeclaration);

                    if (viewAction.getActionDefinition() instanceof ParameterlessCallOperationActionDefinition pcoad
                            && pcoad.getTargetType() == null) {
                        pcoad.setTargetType(ctx.equivalent(tRelation.getReferenceType(), ClassType.class, CLASS_TYPE));
                    }
                }
            }

            // Remaining action declarations (direct view-level actions)
            for (UIActionDeclaration actionDeclaration : actionDeclarationsToProcess) {
                Action viewAction = ctx.equivalentDiscriminated(actionDeclaration, Action.class,
                        VIEW_ACTION, getJslId(source));
                viewAction.setOwnerDataElement(relType);
                viewAction.setTargetDataElement(ctx.equivalent(actionDeclaration.getTransferAction().getTarget(),
                        OperationType.class, OPERATION_TYPE));
                target.getActions().add(viewAction);
            }

            // Standard actions
            target.getActions().add(ctx.equivalent(source, Action.class,
                    ACCESS_TABLE_VIEW_BACK_ACTION));
            if (isRefreshAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_VIEW_REFRESH_ACTION));
            }
            if (isUpdateAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_VIEW_UPDATE_ACTION));
            }
            if (isDeleteAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_VIEW_DELETE_ACTION));
            }

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("AccessTableViewPageDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Access table view lazy action rules — ported from menuTableDeclarationViewPage.etl
    // =========================================================================

    @TransformRule(name = ACCESS_TABLE_VIEW_BACK_ACTION, description = "Create back action for view page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableViewBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableViewBackAction");
            target.setName(source.getName() + "::Back");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_VIEW_REFRESH_ACTION, description = "Create refresh action for view page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableViewRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableViewRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_VIEW_UPDATE_ACTION, description = "Create update action for view page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableViewUpdateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableViewUpdateAction");
            target.setName(source.getName() + "::Update");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_VIEW_DELETE_ACTION, description = "Create delete action for view page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableViewDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableViewDeleteAction");
            target.setName(getFqName(source) + "::Delete");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Access table create form page rules — ported from menuTableDeclarationFormPage.etl
    // =========================================================================

    @TransformRule(name = ACCESS_TABLE_CREATE_FORM_PAGE_DEFINITION, description = "Create PageDefinition for create form page")
    @Greedy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIMenuTableDeclaration, PageDefinition> accessTableCreateFormPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (getCreateFormModifier(source) == null) return null;

            TransferRelationDeclaration relation = source.getActorAccess().getTarget();
            UIViewDeclaration form = getCreateFormModifier(source).getForm();

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableCreateFormPageDefinition");
            target.setName(getFqName(source) + "::AccessFormPage");
            target.setOpenInDialog(true);
            target.setContainer(ctx.equivalent(form, PageContainer.class, FORM_PAGE_CONTAINER));

            RelationType relType = ctx.equivalent(relation, RelationType.class, RELATION_TYPE);
            target.setDataElement(relType);
            relType.setMemberType(MemberType.ACCESS);

            // Standard actions
            target.getActions().add(ctx.equivalent(source, Action.class,
                    ACCESS_TABLE_CREATE_FORM_BACK_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    ACCESS_TABLE_CREATE_FORM_CREATE_ACTION));
            if (isTemplateAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_TABLE_CREATE_FORM_GET_TEMPLATE_ACTION));
            }

            // Link processing
            for (UIViewLinkDeclaration link : getOwnLinks(form)) {
                TransferRelationDeclaration lRelation = link.getTransferRelation().getTarget();

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

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("AccessTableCreateFormPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_CREATE_FORM_BACK_ACTION, description = "Create back action for create form page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableCreateFormBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableCreateFormBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_CREATE_FORM_GET_TEMPLATE_ACTION, description = "Create get template action for create form page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableCreateFormGetTemplateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableCreateFormGetTemplateAction");
            target.setName(source.getName() + "::GetTemplate");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_GET_TEMPLATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_TABLE_CREATE_FORM_CREATE_ACTION, description = "Create create action for create form page")
    @Lazy
    @Transform(type = UIMenuTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuTableDeclaration, Action> accessTableCreateFormCreateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessTableCreateFormCreateAction");
            target.setName(source.getName() + "::Create");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_CREATE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source, PageDefinition.class,
                    ACCESS_TABLE_VIEW_PAGE_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private int indexInParentMembers(UIMenuTableDeclaration source) {
        EObject container = source.eContainer();
        if (container instanceof UIMenuGroupDeclaration) {
            return ((UIMenuGroupDeclaration) container).getMembers().indexOf(source);
        }
        if (container instanceof MenuModifier) {
            return ((MenuModifier) container).getMembers().indexOf(source);
        }
        return 0;
    }
}
