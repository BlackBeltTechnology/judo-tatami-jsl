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
 * Ported from menuLinkDeclaration.etl and menuLinkDeclarationViewPage.etl:
 * Creates NavigationItem and PageDefinition for menu link declarations.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class MenuLinkDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(MenuLinkDeclarationRules.class);

    // =========================================================================
    // MenuLinkNavigationItem (@greedy)
    // =========================================================================

    @TransformRule(name = MENU_LINK_NAVIGATION_ITEM, description = "Create NavigationItem for menu link")
    @Greedy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = NavigationItem.class)
    public TransformFunction<UIMenuLinkDeclaration, NavigationItem> menuLinkNavigationItem() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            NavigationItem target = ctx.createTarget(NavigationItem.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/MenuLinkNavigationItem");
            target.setName(getFqName(source));

            LabelModifier labelMod = getLabelModifier(source);
            if (labelMod != null) {
                target.setLabel(labelMod.getValue().getValue());
            }

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(iconMod, Icon.class, ICON_MODIFIER_ICON));
            }

            target.setTarget(ctx.equivalent(source, PageDefinition.class,
                    ACCESS_VIEW_PAGE_DEFINITION));

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

            LOG.debug("MenuLinkNavigationItem: {}", source.getName());
            return target;
        };
    }

    // =========================================================================
    // AccessViewPageDefinition (@greedy)
    // =========================================================================

    @TransformRule(name = ACCESS_VIEW_PAGE_DEFINITION, description = "Create PageDefinition for link access view page")
    @Greedy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIMenuLinkDeclaration, PageDefinition> accessViewPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            TransferRelationDeclaration relation = source.getActorAccess().getTarget();

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessViewPageDefinition");
            target.setName(getFqName(source) + "::AccessViewPage");
            target.setContainer(ctx.equivalent(source.getReferenceType(),
                    PageContainer.class, VIEW_PAGE_CONTAINER));
            target.setDashboard(isDashBoard(source));

            RelationType relType = ctx.equivalent(relation, RelationType.class, RELATION_TYPE);
            target.setDataElement(relType);

            // Profile page assignment (ported from menuLinkDeclarationViewPage.etl)
            if (isNestedInProfile(source)) {
                Application app2 = ctx.equivalent(frontend, Application.class, APPLICATION);
                app2.setProfilePage(target);
            }

            if (isOpenInDialog(source.getReferenceType())) {
                target.setOpenInDialog(true);
            }

            UIViewDeclaration view = source.getReferenceType();
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

            // Back action
            target.getActions().add(ctx.equivalent(source, Action.class,
                    ACCESS_VIEW_BACK_ACTION));

            // Refresh action
            if (isRefreshAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_VIEW_REFRESH_ACTION));
            }

            // Update action
            if (isUpdateAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_VIEW_UPDATE_ACTION));
            }

            // Delete action
            if (isDeleteAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_VIEW_DELETE_ACTION));
            }

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("AccessViewPageDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Access view lazy action rules
    // =========================================================================

    @TransformRule(name = ACCESS_VIEW_BACK_ACTION, description = "Create back action for view page")
    @Lazy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuLinkDeclaration, Action> accessViewBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessViewBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_VIEW_REFRESH_ACTION, description = "Create refresh action for view page")
    @Lazy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuLinkDeclaration, Action> accessViewRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessViewRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_VIEW_UPDATE_ACTION, description = "Create update action for view page")
    @Lazy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuLinkDeclaration, Action> accessViewUpdateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessViewUpdateAction");
            target.setName(source.getName() + "::Update");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_VIEW_DELETE_ACTION, description = "Create delete action for view page")
    @Lazy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuLinkDeclaration, Action> accessViewDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessViewDeleteAction");
            target.setName(source.getName() + "::Delete");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Access link create form page rules — ported from menuLinkDeclarationFormPage.etl
    // =========================================================================

    @TransformRule(name = ACCESS_LINK_CREATE_FORM_PAGE_DEFINITION, description = "Create PageDefinition for create form page")
    @Greedy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIMenuLinkDeclaration, PageDefinition> accessLinkCreateFormPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (getCreateFormModifier(source) == null) return null;

            TransferRelationDeclaration relation = source.getActorAccess().getTarget();
            UIViewDeclaration form = getCreateFormModifier(source).getForm();

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessViewCreateFormPageDefinition");
            target.setName(getFqName(source) + "::AccessFormPage");
            target.setOpenInDialog(true);
            target.setContainer(ctx.equivalent(form, PageContainer.class, FORM_PAGE_CONTAINER));

            RelationType relType = ctx.equivalent(relation, RelationType.class, RELATION_TYPE);
            target.setDataElement(relType);
            relType.setMemberType(MemberType.ACCESS);

            // Standard actions
            target.getActions().add(ctx.equivalent(source, Action.class,
                    ACCESS_LINK_CREATE_FORM_BACK_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    ACCESS_LINK_CREATE_FORM_CREATE_ACTION));
            if (isTemplateAllowed(relation)) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        ACCESS_LINK_CREATE_FORM_GET_TEMPLATE_ACTION));
            }

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

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("AccessViewCreateFormPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ACCESS_LINK_CREATE_FORM_BACK_ACTION, description = "Create back action for create form page")
    @Lazy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuLinkDeclaration, Action> accessLinkCreateFormBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessLinkCreateFormBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_LINK_CREATE_FORM_GET_TEMPLATE_ACTION, description = "Create get template action for create form page")
    @Lazy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuLinkDeclaration, Action> accessLinkCreateFormGetTemplateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessLinkCreateFormGetTemplateAction");
            target.setName(source.getName() + "::GetTemplate");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_GET_TEMPLATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = ACCESS_LINK_CREATE_FORM_CREATE_ACTION, description = "Create create action for create form page")
    @Lazy
    @Transform(type = UIMenuLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIMenuLinkDeclaration, Action> accessLinkCreateFormCreateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/AccessLinkCreateFormCreateAction");
            target.setName(source.getName() + "::Create");
            target.setOwnerDataElement(ctx.equivalent(source.getActorAccess().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_CREATE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source, PageDefinition.class,
                    ACCESS_VIEW_PAGE_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private int indexInParentMembers(UIMenuLinkDeclaration source) {
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
