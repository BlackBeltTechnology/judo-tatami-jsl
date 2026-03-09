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
 * Ported from actionGroupDeclaration.etl:
 * Creates ButtonGroup for UIActionGroupDeclaration.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ActionGroupDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ActionGroupDeclarationRules.class);

    @TransformRule(name = ACTION_GROUP_VISUAL_ELEMENT, description = "Create ButtonGroup for action group")
    @Greedy
    @Transform(type = UIActionGroupDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIActionGroupDeclaration, ButtonGroup> actionGroupVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;

            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ActionGroupVisualElement");
            target.setName(source.getName());
            target.setLabel(getLabelWithNameFallback(source));

            Modifier widthMod = getWidth(source);
            target.setCol(widthMod != null ? getModifierDoubleValue(widthMod) : 4.0);

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(iconMod, Icon.class, ICON_MODIFIER_ICON));
            }

            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            posMap.put(target, getPos(source));

            // Add to parent container
            VisualElement container = resolveUiContainer(source.eContainer(), ctx);
            if (container instanceof Flex) {
                ((Flex) container).getChildren().add(target);
            }

            LOG.debug("ActionGroupVisualElement: {}", target.getName());
            return target;
        };
    }
}
