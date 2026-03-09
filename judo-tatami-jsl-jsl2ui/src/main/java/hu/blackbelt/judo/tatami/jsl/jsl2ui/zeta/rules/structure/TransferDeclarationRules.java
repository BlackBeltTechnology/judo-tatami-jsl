package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.structure;

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
import hu.blackbelt.judo.meta.ui.data.ClassBehaviourType;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.zeta.annotation.Greedy;
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
 * Ported from transferDeclaration.etl:
 * - ClassType: creates ClassType for exposed TransferDeclarations
 */
@TransformationContext(
        source = TransferDeclaration.class,
        target = EObject.class
)
public class TransferDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferDeclarationRules.class);

    @TransformRule(name = CLASS_TYPE, description = "Create ClassType for TransferDeclaration")
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = ClassType.class)
    public TransformFunction<TransferDeclaration, ClassType> classType() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            if (!getExposedTransferObjects(actorDeclaration).contains(source)) return null;

            ClassType target = ctx.createTarget(ClassType.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/ClassType");

            target.setName(getFqName(source));
            target.setTransferObjectTypeName(source.getName());
            target.setSimpleName(source.getName());
            String defaultModelName = ctx.getAttribute("defaultModelName");
            target.getPackageNameTokens().add(0, defaultModelName);

            target.setIsMapped(source.getMap() != null);

            if (isRefreshSupported(source)) {
                target.getBehaviours().add(ClassBehaviourType.REFRESH);
            }
            if (isUpdateSupported(source)) {
                target.getBehaviours().add(ClassBehaviourType.UPDATE);
                target.getBehaviours().add(ClassBehaviourType.VALIDATE_UPDATE);
                target.setIsForCreateOrUpdateType(true);
            }
            if (isDeleteSupported(source)) {
                target.getBehaviours().add(ClassBehaviourType.DELETE);
            }
            if (isGetTemplateSupported(source)) {
                target.getBehaviours().add(ClassBehaviourType.TEMPLATE);
                target.setIsOptional(true);
            }

            // Add to Application's dataElements
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            if (app != null) {
                app.getDataElements().add(target);
            }

            // Check if this is the identity transfer declaration
            TransferDeclaration identity = getIdentityTransferDeclaration(actorDeclaration);
            if (identity != null && identity == source) {
                target.setIsPrincipal(true);
            }

            target.setIsActor(source instanceof ActorDeclaration);

            ctx.addToResource(target);

            LOG.debug("Create ClassType: {}", target.getName());
            return target;
        };
    }
}
