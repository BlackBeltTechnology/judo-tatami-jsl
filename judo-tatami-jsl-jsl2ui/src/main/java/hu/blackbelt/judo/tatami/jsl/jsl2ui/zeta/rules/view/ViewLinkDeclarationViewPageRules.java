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

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from viewLinkDeclarationViewPage.etl:
 * - ViewLinkPageDefinition (greedy)
 * - ViewLinkPageDefinitionRefreshAction, BackAction, UpdateAction, DeleteAction (lazy)
 * - ViewLinkDeclarationRefreshAction, PreFetchAction, OpenPageAction, OpenFormAction,
 *   RowDeleteAction, UnsetAction, OpenSetSelectorDialogAction (lazy)
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ViewLinkDeclarationViewPageRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewLinkDeclarationViewPageRules.class);

    // =========================================================================
    // ViewLinkPageDefinition (@greedy) — main page for view links
    // =========================================================================

    @TransformRule(name = VIEW_LINK_PAGE_DEFINITION, description = "Create PageDefinition for link view page")
    @Greedy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, PageDefinition> viewLinkPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkPageDefinition");
            target.setName(getFqName(source) + "::ViewPage");
            target.setContainer(ctx.equivalent(source.getReferenceType(),
                    PageContainer.class, VIEW_PAGE_CONTAINER));

            target.setDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));

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

            // Remaining action declarations
            for (UIActionDeclaration actionDeclaration : actionDeclarationsToProcess) {
                Action viewAction = ctx.equivalentDiscriminated(actionDeclaration, Action.class,
                        VIEW_ACTION, getJslId(source));
                viewAction.setOwnerDataElement(target.getDataElement());
                viewAction.setTargetDataElement(ctx.equivalent(actionDeclaration.getTransferAction().getTarget(),
                        OperationType.class, OPERATION_TYPE));
                target.getActions().add(viewAction);
            }

            // Standard actions
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_LINK_PAGE_DEFINITION_BACK_ACTION));
            if (isRefreshAllowed(source.getTransferRelation().getTarget())) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        VIEW_LINK_PAGE_DEFINITION_REFRESH_ACTION));
            }
            if (isUpdateAllowed(source.getTransferRelation().getTarget())) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        VIEW_LINK_PAGE_DEFINITION_UPDATE_ACTION));
            }
            if (isDeleteAllowed(source.getTransferRelation().getTarget())) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        VIEW_LINK_PAGE_DEFINITION_DELETE_ACTION));
            }

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("ViewLinkPageDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // ViewLinkPageDefinition page-level lazy actions
    // =========================================================================

    @TransformRule(name = VIEW_LINK_PAGE_DEFINITION_REFRESH_ACTION, description = "Create refresh action for link view page")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkPageDefinitionRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkPageDefinitionRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_PAGE_DEFINITION_BACK_ACTION, description = "Create back action for link view page")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkPageDefinitionBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkPageDefinitionBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_PAGE_DEFINITION_UPDATE_ACTION, description = "Create update action for link view page")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkPageDefinitionUpdateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkPageDefinitionUpdateAction");
            target.setName(source.getName() + "::Update");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_PAGE_DEFINITION_DELETE_ACTION, description = "Create delete action for link view page")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkPageDefinitionDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkPageDefinitionDeleteAction");
            target.setName(source.getName() + "::Delete");
            target.setActionDefinition(ctx.equivalent(source.getReferenceType(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // ViewLinkDeclaration discriminated lazy action rules
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION, description = "Create open page action for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkDeclarationOpenPageAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenPageAction");
            target.setName(source.getName() + "::OpenPage");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source,
                    PageDefinition.class, VIEW_LINK_PAGE_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_REFRESH_ACTION, description = "Create refresh action for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkDeclarationRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_LINK_DECLARATION_REFRESH_ACTION_DEFINITION));
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_PRE_FETCH_ACTION, description = "Create pre-fetch action for button link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkDeclarationPreFetchAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationPreFetchAction");
            target.setName(source.getName() + "::PreFetch");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, INLINE_VIEW_BUTTON_PRE_FETCH_ACTION_DEFINITION));
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_FORM_ACTION, description = "Create open form action for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkDeclarationOpenFormAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenFormAction");
            target.setName(source.getName() + "::OpenForm");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_LINK_DECLARATION_OPEN_CREATE_FORM_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source,
                    PageDefinition.class, VIEW_LINK_CREATE_FORM_PAGE_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_ROW_DELETE_ACTION, description = "Create row delete action for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkDeclarationRowDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationRowDeleteAction");
            target.setName(source.getName() + "::RowDelete");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_LINK_DECLARATION_ROW_DELETE_ACTION_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_UNSET_ACTION, description = "Create unset action for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkDeclarationUnsetAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationUnsetAction");
            target.setName(source.getName() + "::Unset");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_LINK_DECLARATION_UNSET_ACTION_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_DIALOG_ACTION, description = "Create open set selector dialog action for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkDeclarationOpenSetSelectorDialogAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenSetSelectorDialogAction");
            target.setName(source.getName() + "::OpenSetSelector");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source,
                    PageDefinition.class, VIEW_LINK_DECLARATION_SET_SELECTOR_PAGE_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }
}
