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
 * Ported from viewTabsDeclaration.etl:
 * - TabBarVisualElement: creates TabController for UIViewTabsDeclaration
 * - SubTab (@greedy): creates Tab for nested tabs
 * - TabsIcon (@lazy): icon for tabs
 */
@TransformationContext(
        source = UIViewTabsDeclaration.class,
        target = EObject.class
)
public class ViewTabsDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewTabsDeclarationRules.class);

    @TransformRule(name = TAB_BAR_VISUAL_ELEMENT, description = "Create TabController for UIViewTabsDeclaration")
    @Transform(type = UIViewTabsDeclaration.class)
    @To(type = TabController.class)
    public TransformFunction<UIViewTabsDeclaration, TabController> tabBarVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            TabController target = ctx.createTarget(TabController.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TabBarVisualElement");

            target.setName(source.getName());

            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            posMap.put(target, getPos(source));

            Modifier widthMod = getWidth(source);
            target.setCol(widthMod != null ? getModifierDoubleValue(widthMod) : 12.0);

            target.setLabel(getLabelWithNameFallback(source));

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(source, Icon.class, TABS_ICON));
            }

            OrientationModifier orientMod = getOrientationModifier(source);
            if (orientMod != null) {
                if (orientMod.isVertical()) {
                    target.setOrientation(TabOrientation.VERTICAL);
                } else {
                    target.setOrientation(TabOrientation.HORIZONTAL);
                }
            }

            // Add to parent container
            VisualElement container = resolveUiContainer(source.eContainer(), ctx);
            if (container instanceof Flex) {
                ((Flex) container).getChildren().add(target);
            }

            LOG.debug("TabBarVisualElement: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = SUB_TAB, description = "Create Tab for nested UIViewTabsDeclaration")
    @Greedy
    @Transform(type = UIViewTabsDeclaration.class)
    @To(type = Tab.class)
    public TransformFunction<UIViewTabsDeclaration, Tab> subTab() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!(source.eContainer() instanceof UIViewTabsDeclaration)) return null;
            if (!containsVisualElement(frontend, source)) return null;

            Tab target = ctx.createTarget(Tab.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/SubTab");

            TabController parentTabController = ctx.equivalent(source.eContainer(),
                    TabController.class, TAB_BAR_VISUAL_ELEMENT);
            parentTabController.getTabs().add(target);

            TabController thisTabController = ctx.equivalent(source, TabController.class, TAB_BAR_VISUAL_ELEMENT);
            target.setElement(thisTabController);
            target.setName(thisTabController.getName());

            LOG.debug("SubTab: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TABS_ICON, description = "Create Icon for tabs")
    @Lazy
    @Transform(type = UIViewTabsDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewTabsDeclaration, Icon> tabsIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TabsIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "TabsIcon");
            return target;
        };
    }
}
