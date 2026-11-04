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

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from viewTableDeclarationViewPage.etl:
 * - ViewTableViewPageDefinition (greedy)
 * - Page-level lazy actions (refresh, back, update, delete)
 * - Table-specific lazy actions (filter, refresh, open page, etc.)
 * - Tag autocomplete actions
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ViewTableDeclarationViewPageRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewTableDeclarationViewPageRules.class);

    // =========================================================================
    // ViewTableViewPageDefinition (@greedy) — main page for view table view pages
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_VIEW_PAGE_DEFINITION, description = "Create PageDefinition for table view page")
    @Greedy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIViewTableDeclaration, PageDefinition> viewTableViewPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (getUpdateViewModifier(source) == null) return null;

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableViewPageDefinition");
            target.setName(getFqName(source) + "::ViewPage");
            target.setContainer(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    PageContainer.class, VIEW_PAGE_CONTAINER));

            target.setDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));

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
                    VIEW_TABLE_VIEW_PAGE_DEFINITION_BACK_ACTION));
            if (isRefreshAllowed(source.getTransferRelation().getTarget())) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        VIEW_TABLE_VIEW_PAGE_DEFINITION_REFRESH_ACTION));
            }
            if (isUpdateAllowed(source.getTransferRelation().getTarget())) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        VIEW_TABLE_VIEW_PAGE_DEFINITION_UPDATE_ACTION));
            }
            if (isDeleteAllowed(source.getTransferRelation().getTarget())) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        VIEW_TABLE_VIEW_PAGE_DEFINITION_DELETE_ACTION));
            }

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("ViewTableViewPageDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Tag autocomplete actions
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION, description = "Create autocomplete range action for tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableTagsDeclarationAutocompleteRangeAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableTagsDeclarationAutocompleteRangeAction");
            target.setName(source.getName() + "::AutocompleteRange");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION, description = "Create autocomplete add action for tags")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableTagsDeclarationAutocompleteAddAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableTagsDeclarationAutocompleteAddAction");
            target.setName(source.getName() + "::AutocompleteAdd");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Page-level lazy actions
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_VIEW_PAGE_DEFINITION_REFRESH_ACTION, description = "Create refresh action for table view page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableViewPageDefinitionRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableViewPageDefinitionRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_VIEW_PAGE_DEFINITION_BACK_ACTION, description = "Create back action for table view page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableViewPageDefinitionBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableViewPageDefinitionBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_VIEW_PAGE_DEFINITION_UPDATE_ACTION, description = "Create update action for table view page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableViewPageDefinitionUpdateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableViewPageDefinitionUpdateAction");
            target.setName(source.getName() + "::Update");
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_VIEW_PAGE_DEFINITION_DELETE_ACTION, description = "Create delete action for table view page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableViewPageDefinitionDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableViewPageDefinitionDeleteAction");
            target.setName(source.getName() + "::Delete");
            target.setActionDefinition(ctx.equivalent(getUpdateViewModifier(source).getView(),
                    ActionDefinition.class, VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Table-specific discriminated lazy action rules
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_FILTER_ACTION, description = "Create filter action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationFilterAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationFilterAction");
            target.setName(source.getName() + "::Filter");
            if (source.getReferenceType() instanceof UIRowDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, VIEW_TABLE_DECLARATION_FILTER_ACTION_DEFINITION));
            } else if (source.getReferenceType() instanceof UICardDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, INLINE_VIEW_CARDS_FILTER_BUTTON_ACTION_DEFINITION));
            } else if (source.getReferenceType() instanceof UITagDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, INLINE_VIEW_TAGS_FILTER_ACTION_DEFINITION));
            }
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_REFRESH_ACTION, description = "Create refresh action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            if (source.getReferenceType() instanceof UIRowDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, VIEW_TABLE_DECLARATION_REFRESH_ACTION_DEFINITION));
            } else if (source.getReferenceType() instanceof UICardDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, INLINE_VIEW_CARDS_REFRESH_ACTION_DEFINITION));
            } else if (source.getReferenceType() instanceof UITagDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, INLINE_VIEW_TAGS_REFRESH_ACTION_DEFINITION));
            }
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_CREATE_ACTION, description = "Create open create action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationOpenCreateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenCreateAction");
            target.setName(source.getName() + "::OpenCreate");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_TABLE_DECLARATION_OPEN_CREATE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source,
                    PageDefinition.class, VIEW_TABLE_CREATE_FORM_PAGE_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION, description = "Create open page action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationOpenPageAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenPageAction");
            target.setName(source.getName() + "::OpenPage");
            target.setTargetPageDefinition(ctx.equivalent(source,
                    PageDefinition.class, VIEW_TABLE_VIEW_PAGE_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            if (source.getReferenceType() instanceof UIRowDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION_DEFINITION));
            } else if (source.getReferenceType() instanceof UICardDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, INLINE_VIEW_CARDS_OPEN_PAGE_ACTION_DEFINITION));
            } else if (source.getReferenceType() instanceof UITagDeclaration) {
                target.setActionDefinition(ctx.equivalent(source,
                        ActionDefinition.class, INLINE_VIEW_TAGS_OPEN_PAGE_ACTION_DEFINITION));
            }
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_ROW_DELETE_ACTION, description = "Create row delete action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationRowDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationRowDeleteAction");
            target.setName(source.getName() + "::RowDelete");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_TABLE_DECLARATION_ROW_DELETE_ACTION_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION, description = "Create open add selector action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationOpenAddSelectorAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationOpenAddSelectorAction");
            target.setName(source.getName() + "::OpenAddSelector");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source,
                    PageDefinition.class, VIEW_TABLE_DECLARATION_ADD_SELECTOR_PAGE_DEFINITION));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION, description = "Create bulk remove action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationBulkRemoveAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationBulkRemoveAction");
            target.setName(source.getName() + "::BulkRemove");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION_DEFINITION));
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_CLEAR_ACTION, description = "Create clear action for table")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationClearAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationClearAction");
            target.setName(source.getName() + "::Clear");
            target.setActionDefinition(ctx.equivalent(source,
                    ActionDefinition.class, VIEW_TABLE_DECLARATION_CLEAR_ACTION_DEFINITION));
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            return target;
        };
    }

    // =========================================================================
    // Add selector page rules — ported from viewTableDeclarationAddSelectorPage.etl
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_DECLARATION_ADD_SELECTOR_PAGE_DEFINITION, description = "Create PageDefinition for add selector page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIViewTableDeclaration, PageDefinition> viewTableDeclarationAddSelectorPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (getSelectorTableModifier(source) == null) return null;

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationAddSelectorPageDefinition");
            target.setName(getFqName(source) + "::AddSelectorPage");

            target.setContainer(ctx.equivalent(getSelectorTableModifier(source).getRow(),
                    PageContainer.class, TABLE_PAGE_CONTAINER));
            target.setDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setOpenInDialog(true);
            target.setDialogSize(DialogSize.MD);
            target.setIsSelector(true);
            target.setIsRelationSelector(true);

            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_TABLE_DECLARATION_ADD_SELECTOR_ADD_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_TABLE_DECLARATION_ADD_SELECTOR_BACK_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_TABLE_DECLARATION_ADD_SELECTOR_TABLE_FILTER_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_TABLE_DECLARATION_ADD_SELECTOR_TABLE_RANGE_ACTION));

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("ViewTableDeclarationAddSelectorPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_ADD_SELECTOR_ADD_ACTION, description = "Create add action for add selector page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationAddSelectorAddAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationAddSelectorAddAction");
            target.setName(getFqName(source) + "::AddSelector::Add");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getSelectorTableModifier(source).getRow(),
                    ActionDefinition.class, TABLE_PAGE_CONTAINER_ADD_SELECTOR_ADD_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_ADD_SELECTOR_BACK_ACTION, description = "Create back action for add selector page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationAddSelectorBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationAddSelectorBackAction");
            target.setName(getFqName(source) + "::AddSelector::Back");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getSelectorTableModifier(source).getRow(),
                    ActionDefinition.class, TABLE_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_ADD_SELECTOR_TABLE_FILTER_ACTION, description = "Create filter action for add selector page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationAddSelectorTableFilterAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationAddSelectorTableFilterAction");
            target.setName(getFqName(source) + "::AddSelector::Table::Filter");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getSelectorTableModifier(source).getRow(),
                    ActionDefinition.class, TABLE_TABLE_FILTER_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_DECLARATION_ADD_SELECTOR_TABLE_RANGE_ACTION, description = "Create range action for add selector page")
    @Lazy
    @Transform(type = UIViewTableDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewTableDeclaration, Action> viewTableDeclarationAddSelectorTableRangeAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableDeclarationAddSelectorTableRangeAction");
            target.setName(getFqName(source) + "::AddSelector::Table::Range");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferRelation().getTarget(),
                    RelationType.class, RELATION_TYPE));
            target.setActionDefinition(ctx.equivalent(getSelectorTableModifier(source).getRow(),
                    ActionDefinition.class, TABLE_TABLE_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }
}
