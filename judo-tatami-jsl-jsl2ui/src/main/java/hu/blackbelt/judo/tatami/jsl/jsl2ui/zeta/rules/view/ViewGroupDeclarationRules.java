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
 * Ported from viewGroupDeclaration.etl:
 * - GroupVisualElement: creates Flex for UIViewGroupDeclaration (extends abstract)
 * - GroupFrame (@lazy @greedy): Frame for group
 * - GroupIcon (@lazy @greedy): Icon for group
 * - TabGroup (@greedy): creates Tab when group is inside tabs
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ViewGroupDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewGroupDeclarationRules.class);

    @TransformRule(name = GROUP_VISUAL_ELEMENT, description = "Create Flex for UIViewGroupDeclaration")
    @Greedy
    @Transform(type = UIViewGroupDeclaration.class)
    @To(type = Flex.class)
    public TransformFunction<UIViewGroupDeclaration, Flex> groupVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            Flex target = ctx.createTarget(Flex.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/GroupVisualElement");

            // Apply abstract rule logic
            applyAbstractGroupDeclaration(source, target, ctx);

            // Add to parent container (unless inside tabs - TabGroup handles that)
            if (!(source.eContainer() instanceof UIViewTabsDeclaration)) {
                VisualElement container = resolveUiContainer(source.eContainer(), ctx);
                if (container instanceof Flex) {
                    ((Flex) container).getChildren().add(target);
                }
            }

            LOG.debug("Flex: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = GROUP_FRAME, description = "Create Frame for group")
    @Lazy
    @Greedy
    @Transform(type = UIViewGroupDeclaration.class)
    @To(type = Frame.class)
    public TransformFunction<UIViewGroupDeclaration, Frame> groupFrame() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Frame target = ctx.createTarget(Frame.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/GroupFrame");
            return target;
        };
    }

    @TransformRule(name = GROUP_ICON, description = "Create Icon for group")
    @Lazy
    @Greedy
    @Transform(type = UIViewGroupDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewGroupDeclaration, Icon> groupIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/GroupIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "GroupIcon");
            return target;
        };
    }

    @TransformRule(name = TAB_GROUP, description = "Create Tab for group inside tabs")
    @Greedy
    @Transform(type = UIViewGroupDeclaration.class)
    @To(type = Tab.class)
    public TransformFunction<UIViewGroupDeclaration, Tab> tabGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!(source.eContainer() instanceof UIViewTabsDeclaration)) return null;
            if (!containsVisualElement(frontend, source)) return null;

            Tab target = ctx.createTarget(Tab.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TabGroup");

            TabController tabController = ctx.equivalent(source.eContainer(),
                    TabController.class, TAB_BAR_VISUAL_ELEMENT);
            tabController.getTabs().add(target);

            Flex groupFlex = ctx.equivalent(source, Flex.class, GROUP_VISUAL_ELEMENT);
            target.setElement(groupFlex);
            target.setName(groupFlex.getName());

            LOG.debug("TabGroup: {}", target.getName());
            return target;
        };
    }

    /**
     * Applies the abstract AbstractViewGroupDeclaration rule logic to a Flex target.
     */
    private void applyAbstractGroupDeclaration(UIViewGroupDeclaration source, Flex target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        LabelModifier labelMod = getLabelModifier(source);
        if (labelMod != null) {
            target.setLabel(labelMod.getValue().getValue());
        }

        IconModifier iconMod = getIconModifier(source);
        if (iconMod != null) {
            target.setIcon(ctx.equivalent(source, Icon.class, GROUP_ICON));
        }

        if (isFrame(source)) {
            target.setFrame(ctx.equivalent(source, Frame.class, GROUP_FRAME));
        }

        StretchModifier stretchMod = getStretchModifier(source);
        if (stretchMod != null && !stretchMod.isFalse()) {
            target.setStretch(Stretch.BOTH);
        }

        Modifier widthMod = getWidth(source);
        target.setCol(widthMod != null ? getModifierDoubleValue(widthMod) : 12.0);

        ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
        posMap.put(target, getPos(source));

        OrientationModifier orientMod = getOrientationModifier(source);
        if (orientMod != null) {
            target.setDirection(orientMod.isVertical() ? Axis.VERTICAL : Axis.HORIZONTAL);
        } else {
            target.setDirection(Axis.VERTICAL);
        }

        // Alignment logic
        if (target.getDirection() == Axis.VERTICAL) {
            applyVerticalAlignment(source, target);
        } else {
            applyHorizontalAlignment(source, target);
        }
    }

    private void applyVerticalAlignment(UIViewGroupDeclaration source, Flex target) {
        Modifier vAlign = getVAlign(source);
        if (vAlign != null && isCenter(vAlign)) {
            target.setMainAxisAlignment(MainAxisAlignment.CENTER);
        } else if (vAlign != null && isBottom(vAlign)) {
            target.setMainAxisAlignment(MainAxisAlignment.END);
        } else {
            target.setMainAxisAlignment(MainAxisAlignment.START);
        }

        Modifier hAlign = getHAlign(source);
        if (hAlign != null && isCenter(hAlign)) {
            target.setCrossAxisAlignment(CrossAxisAlignment.CENTER);
        } else if (hAlign != null && isRight(hAlign)) {
            target.setCrossAxisAlignment(CrossAxisAlignment.END);
        } else {
            target.setCrossAxisAlignment(CrossAxisAlignment.START);
        }
    }

    private void applyHorizontalAlignment(UIViewGroupDeclaration source, Flex target) {
        Modifier hAlign = getHAlign(source);
        if (hAlign != null && isCenter(hAlign)) {
            target.setMainAxisAlignment(MainAxisAlignment.CENTER);
        } else if (hAlign != null && isRight(hAlign)) {
            target.setMainAxisAlignment(MainAxisAlignment.END);
        } else {
            target.setMainAxisAlignment(MainAxisAlignment.START);
        }

        Modifier vAlign = getVAlign(source);
        if (vAlign != null && isCenter(vAlign)) {
            target.setCrossAxisAlignment(CrossAxisAlignment.CENTER);
        } else if (vAlign != null && isBottom(vAlign)) {
            target.setCrossAxisAlignment(CrossAxisAlignment.END);
        } else {
            target.setCrossAxisAlignment(CrossAxisAlignment.START);
        }
    }
}
