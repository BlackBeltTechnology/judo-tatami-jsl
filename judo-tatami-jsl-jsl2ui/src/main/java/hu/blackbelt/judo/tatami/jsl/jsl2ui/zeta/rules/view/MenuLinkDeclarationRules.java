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
