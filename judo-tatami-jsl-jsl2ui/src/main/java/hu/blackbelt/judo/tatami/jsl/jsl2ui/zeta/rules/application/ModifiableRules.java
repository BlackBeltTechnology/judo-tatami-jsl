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

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ClaimModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.IconModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.IdentityModifier;
import hu.blackbelt.judo.meta.ui.Authentication;
import hu.blackbelt.judo.meta.ui.Claim;
import hu.blackbelt.judo.meta.ui.ClaimType;
import hu.blackbelt.judo.meta.ui.Icon;
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

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from actorGroupDeclaration.etl (the modifier rules):
 * - IconModifierIcon: @lazy, creates Icon from IconModifier
 * - ClaimModifier: creates Claim from ClaimModifier
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ModifiableRules {

    private static final Logger LOG = LoggerFactory.getLogger(ModifiableRules.class);

    /**
     * Creates an Icon from an IconModifier (lazy rule, called on demand).
     *
     * ETL equivalent:
     * <pre>
     * @lazy
     * rule IconModifierIcon
     *     transform s: JSL!IconModifier
     *     to t: UI!ui::Icon {
     *         t.iconName = s.value.value;
     *         t.name = s.getId() + "/Icon";
     * </pre>
     */
    @TransformRule(name = ICON_MODIFIER_ICON, description = "Create Icon from IconModifier")
    @Lazy
    @Transform(type = IconModifier.class)
    @To(type = Icon.class)
    public TransformFunction<IconModifier, Icon> iconModifierIcon() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/IconModifierIcon");

            target.setIconName(source.getValue().getValue());
            target.setName(getJslId(source) + "/Icon");

            ctx.addToResource(target);

            return target;
        };
    }

    /**
     * Creates a Claim from a ClaimModifier.
     *
     * ETL equivalent:
     * <pre>
     * rule ClaimModifier
     *     transform s: JSL!ClaimModifier
     *     to t: UI!ui::Claim {
     *         guard: s.eContainer == actorDeclaration and s.eContainer.getIdentity().isDefined()
     *         t.type = UI!ui::ClaimType#UNDEFINED;
     *         s.eContainer.equivalent("Authentication").claims.add(t);
     * </pre>
     */
    @TransformRule(name = CLAIM_MODIFIER, description = "Create Claim from ClaimModifier")
    @Greedy
    @Transform(type = ClaimModifier.class)
    @To(type = Claim.class)
    public TransformFunction<ClaimModifier, Claim> claimModifier() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            if (source.eContainer() != actorDeclaration) return null;
            if (getIdentity(actorDeclaration) == null) return null;

            Claim target = ctx.createTarget(Claim.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/ClaimModifier");

            target.setType(ClaimType.UNDEFINED);

            // ETL: t.attributeType = s.eContainer.getIdentity().field.getTransferFieldDeclarationEquivalent()
            IdentityModifier identity = getIdentity(actorDeclaration);
            if (identity != null && identity.getField() != null) {
                target.setAttributeType(getTransferFieldAttributeType(identity.getField(), ctx));
            }

            // Add to Authentication
            Authentication auth = ctx.equivalent(actorDeclaration,
                    Authentication.class, AUTHENTICATION);
            if (auth != null) {
                auth.getClaims().add(target);
            }

            ctx.addToResource(target);

            return target;
        };
    }
}
