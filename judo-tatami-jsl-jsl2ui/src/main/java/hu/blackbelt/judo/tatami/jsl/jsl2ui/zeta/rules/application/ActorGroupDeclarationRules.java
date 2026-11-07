package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.application;

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
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.Icon;
import hu.blackbelt.judo.meta.ui.NavigationItem;
import hu.blackbelt.judo.zeta.annotation.Greedy;
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
 * Ported from actorGroupDeclaration.etl (actually modifiable.etl):
 * - MenuItemGroup: transforms UIMenuGroupDeclaration to NavigationItem
 *
 * The ETL file also contained IconModifierIcon and ClaimModifier rules,
 * which are in the separate ModifiableRules class since they transform
 * different source types.
 */
@TransformationContext(
        source = UIMenuGroupDeclaration.class,
        target = NavigationItem.class
)
public class ActorGroupDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ActorGroupDeclarationRules.class);

    /**
     * Creates a NavigationItem for a menu group.
     *
     * ETL equivalent:
     * <pre>
     * rule MenuItemGroup
     *     transform s: JSL!UIMenuGroupDeclaration
     *     to t : UI!ui::NavigationItem {
     *         guard: s.getFrontend() == frontend
     *         t.name = s.getFqName();
     *         // label, icon, parent assignment
     * </pre>
     */
    @TransformRule(name = MENU_ITEM_GROUP, description = "Create NavigationItem for menu group")
    @Greedy
    @Transform(type = UIMenuGroupDeclaration.class)
    @To(type = NavigationItem.class)
    public TransformFunction<UIMenuGroupDeclaration, NavigationItem> menuItemGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (getFrontend(source) != frontend) return null;

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            NavigationItem target = ctx.createTarget(NavigationItem.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/MenuItemGroup");

            target.setName(getFqName(source));

            LabelModifier label = getLabelModifier(source);
            if (label != null) {
                target.setLabel(label.getValue().getValue());
            }

            IconModifier icon = getIconModifier(source);
            if (icon != null) {
                Icon uiIcon = ctx.equivalent(icon, Icon.class, ICON_MODIFIER_ICON);
                target.setIcon(uiIcon);
            }

            // Add to parent group or navigation controller
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            if (source.eContainer() instanceof UIMenuGroupDeclaration) {
                NavigationItem parentGroup = ctx.equivalent(
                        (UIMenuGroupDeclaration) source.eContainer(),
                        NavigationItem.class, MENU_ITEM_GROUP);
                parentGroup.getItems().add(target);
                posMap.put(target, ((UIMenuGroupDeclaration) source.eContainer())
                        .getMembers().indexOf(source));
            } else {
                Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
                app.getNavigationController().getItems().add(target);
                // Position relative to parent menu/profile modifier members
                if (source.eContainer() instanceof MenuModifier) {
                    posMap.put(target, ((MenuModifier) source.eContainer())
                            .getMembers().indexOf(source));
                }
            }

            LOG.debug("Create Navigation Group: {}", source.getName());
            return target;
        };
    }
}
