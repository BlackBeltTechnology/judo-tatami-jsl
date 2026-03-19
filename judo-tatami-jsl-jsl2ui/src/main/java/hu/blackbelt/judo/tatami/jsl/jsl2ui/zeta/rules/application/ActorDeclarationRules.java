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
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.UIFrontendDeclaration;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.Authentication;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.zeta.annotation.Greedy;
import hu.blackbelt.judo.zeta.annotation.To;
import hu.blackbelt.judo.zeta.annotation.Transform;
import hu.blackbelt.judo.zeta.annotation.TransformRule;
import hu.blackbelt.judo.zeta.annotation.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from actorDeclaration.etl:
 * - Actor: creates ClassType for the actor declaration
 * - Authentication: creates Authentication when principal/realm/identity are defined
 */
@TransformationContext(
        source = ActorDeclaration.class,
        target = org.eclipse.emf.ecore.EObject.class
)
public class ActorDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ActorDeclarationRules.class);

    /**
     * Creates a ClassType representing the actor.
     *
     * ETL equivalent:
     * <pre>
     * rule Actor
     *     transform s: JSL!ActorDeclaration
     *     to t: UI!ui::data::ClassType {
     *         guard: s == actorDeclaration
     *         t.name = s.getFqName();
     *         t.simpleName = s.name;
     *         t.packageNameTokens.add(0, defaultModelName);
     *         t.isActor = true;
     * </pre>
     */
    @TransformRule(name = ACTOR, description = "Create ClassType for actor declaration")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = ClassType.class)
    public TransformFunction<ActorDeclaration, ClassType> actor() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            if (source != actorDeclaration) return null;

            ClassType target = ctx.createTarget(ClassType.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/Actor");

            target.setName(getFqName(source));
            target.setSimpleName(source.getName());
            String defaultModelName = ctx.getAttribute("defaultModelName");
            target.getPackageNameTokens().add(0, defaultModelName);
            target.setIsActor(true);

            ctx.addToResource(target);

            LOG.debug("Create class type (Actor): {}", target.getName());
            return target;
        };
    }

    /**
     * Creates Authentication when principal, realm and identity are defined.
     *
     * ETL equivalent:
     * <pre>
     * rule Authentication
     *     transform s: JSL!ActorDeclaration
     *     to t: UI!ui::Authentication {
     *         guard: s == actorDeclaration
     *             and s.getPrincipal().isDefined()
     *             and s.getRealm().isDefined()
     *             and s.getIdentity().isDefined()
     *         t.realm = s.getRealm().value.value;
     *         frontend.equivalent("Application").authentication = t;
     * </pre>
     */
    @TransformRule(name = AUTHENTICATION, description = "Create Authentication for actor")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = Authentication.class)
    public TransformFunction<ActorDeclaration, Authentication> authentication() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            if (source != actorDeclaration) return null;
            if (getPrincipal(source) == null) return null;
            if (getRealm(source) == null) return null;
            if (getIdentity(source) == null) return null;

            Authentication target = ctx.createTarget(Authentication.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/Authentication");

            target.setRealm(getRealm(source).getValue().getValue());

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            if (app != null) {
                app.setAuthentication(target);
            }

            LOG.debug("Create Authentication: {}", target.getRealm());
            return target;
        };
    }
}
