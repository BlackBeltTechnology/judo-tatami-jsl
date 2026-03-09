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
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferFieldDeclaration;
import hu.blackbelt.judo.meta.ui.data.AttributeType;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.MemberType;
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
 * Ported from transferFieldDeclaration.etl:
 * - CreateTransientTransferAttribute
 * - CreateDerivedTransferAttribute
 * - CreateMappedTransferAttribute
 *
 * Note: targets are added to their parent ClassType's attributes containment,
 * so ctx.addToResource() must NOT be called (EMF containment semantics).
 */
@TransformationContext(
        source = TransferFieldDeclaration.class,
        target = EObject.class
)
public class TransferFieldDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferFieldDeclarationRules.class);

    /**
     * Common logic from AbstractCreateTransferAttribute.
     * Returns null if the guard fails.
     */
    private AttributeType createBaseAttribute(TransferFieldDeclaration source,
                                               hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(source.getReferenceType() instanceof PrimitiveDeclaration)) return null;
        ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
        if (!(source.eContainer() instanceof TransferDeclaration)) return null;
        if (!getExposedTransferObjects(actorDeclaration).contains((TransferDeclaration) source.eContainer())) return null;

        AttributeType target = ctx.createTarget(AttributeType.class);
        target.setName(source.getName());
        target.setIsRequired(isRequired(source));
        target.setIsFilterable(isFilterable(source));

        return target;
    }

    @TransformRule(name = CREATE_TRANSIENT_TRANSFER_ATTRIBUTE, description = "Create transient AttributeType")
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = AttributeType.class)
    public TransformFunction<TransferFieldDeclaration, AttributeType> createTransientTransferAttribute() {
        return (source, ctx) -> {
            if (isMaps(source) || isReads(source)) return null;

            AttributeType target = createBaseAttribute(source, ctx);
            if (target == null) return null;

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateTransientTransferAttribute");

            target.setMemberType(MemberType.TRANSIENT);
            target.setIsReadOnly(false);

            ClassType parentCt = ctx.equivalent((TransferDeclaration) source.eContainer(),
                    ClassType.class, CLASS_TYPE);
            if (parentCt != null) {
                parentCt.getAttributes().add(target);
            }

            LOG.debug("Created AttributeType (Transient): {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_DERIVED_TRANSFER_ATTRIBUTE, description = "Create derived AttributeType")
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = AttributeType.class)
    public TransformFunction<TransferFieldDeclaration, AttributeType> createDerivedTransferAttribute() {
        return (source, ctx) -> {
            if (!isReads(source)) return null;

            AttributeType target = createBaseAttribute(source, ctx);
            if (target == null) return null;

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateDerivedTransferAttribute");

            target.setMemberType(MemberType.DERIVED);
            target.setIsReadOnly(true);

            ClassType parentCt = ctx.equivalent((TransferDeclaration) source.eContainer(),
                    ClassType.class, CLASS_TYPE);
            if (parentCt != null) {
                parentCt.getAttributes().add(target);
            }

            LOG.debug("Created AttributeType (Derived): {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_MAPPED_TRANSFER_ATTRIBUTE, description = "Create mapped AttributeType")
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = AttributeType.class)
    public TransformFunction<TransferFieldDeclaration, AttributeType> createMappedTransferAttribute() {
        return (source, ctx) -> {
            if (!isMaps(source)) return null;

            AttributeType target = createBaseAttribute(source, ctx);
            if (target == null) return null;

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateMappedTransferAttribute");

            target.setMemberType(MemberType.MAPPED);
            target.setIsReadOnly(false);

            ClassType parentCt = ctx.equivalent((TransferDeclaration) source.eContainer(),
                    ClassType.class, CLASS_TYPE);
            if (parentCt != null) {
                parentCt.getAttributes().add(target);
            }

            LOG.debug("Created AttributeType (Mapped): {}", target.getName());
            return target;
        };
    }
}
