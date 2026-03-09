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

import hu.blackbelt.judo.meta.jsl.jsldsl.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.MemberType;
import hu.blackbelt.judo.meta.ui.data.RelationBehaviourType;
import hu.blackbelt.judo.meta.ui.data.RelationKind;
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

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from transferRelationDeclaration.etl:
 * - RelationType: creates RelationType for exposed TransferRelationDeclarations
 * - CloneRelationType: @lazy version for cloning
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = EObject.class
)
public class TransferRelationDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferRelationDeclarationRules.class);

    /**
     * Common logic from AbstractRelationType.
     * Returns null if the guard fails.
     */
    private RelationType createBaseRelation(TransferRelationDeclaration source,
                                            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
        if (!getExposedRelations(actorDeclaration).contains(source)) return null;

        RelationType target = ctx.createTarget(RelationType.class);

        target.setName(source.getName());

        // Target ClassType
        ClassType targetCt = ctx.equivalent(source.getReferenceType(), ClassType.class, CLASS_TYPE);
        target.setTarget(targetCt);

        target.setIsOptional(!isRequired(source) || isMany(source));
        target.setIsCollection(isMany(source));
        target.setIsReadOnly(isReads(source));

        target.setMemberType(MemberType.TRANSIENT);

        if (isAggregation(source)) {
            target.setRelationKind(RelationKind.AGGREGATION);
        } else {
            target.setRelationKind(RelationKind.ASSOCIATION);
        }

        target.setIsAccess(source instanceof ActorAccessDeclaration);

        // Determine memberType
        if (isReads(source)) {
            target.setMemberType(MemberType.DERIVED);
        } else if (isMaps(source)) {
            if (source.getGetterExpr() instanceof Navigation nav
                    && !nav.getFeatures().isEmpty()
                    && nav.getFeatures().get(0) instanceof MemberReference memberRef) {
                EObject entityMember = memberRef.getMember();
                if (!(entityMember instanceof EntityRelationOppositeInjected)) {
                    if (entityMember instanceof EntityMemberDeclaration emd
                            && isCalculated(emd)) {
                        target.setMemberType(MemberType.DERIVED);
                    } else {
                        target.setMemberType(MemberType.STORED);
                    }
                } else {
                    target.setMemberType(MemberType.STORED);
                }
            }
        } else {
            target.setMemberType(MemberType.TRANSIENT);
        }

        // Behaviours
        if (isListAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.LIST);
        }
        if (isTemplateAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.TEMPLATE);
        }
        if (isCreateAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.CREATE);
            if (targetCt != null) targetCt.setIsForCreateOrUpdateType(true);
        }
        if (isValidateCreateAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.VALIDATE_CREATE);
            if (targetCt != null) targetCt.setIsForCreateOrUpdateType(true);
        }
        if (isSetReferenceAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.SET);
        }
        if (isUnsetReferenceAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.UNSET);
        }
        if (isAddReferenceAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.ADD);
        }
        if (isRemoveReferenceAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.REMOVE);
        }
        if (isGetRangeAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.RANGE);
            target.setIsOptional(true);
        }
        if (isRefreshAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.REFRESH);
        }
        if (isUpdateAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.UPDATE);
            if (targetCt != null) targetCt.setIsForCreateOrUpdateType(true);
        }
        if (isValidateUpdateAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.VALIDATE_UPDATE);
            if (targetCt != null) targetCt.setIsForCreateOrUpdateType(true);
        }
        if (isDeleteAllowed(source)) {
            target.getBehaviours().add(RelationBehaviourType.DELETE);
        }

        target.setIsOrderable(isOrderSupported(source));
        target.setIsFilterable(isFilterSupported(source));

        return target;
    }

    @TransformRule(name = RELATION_TYPE, description = "Create RelationType for TransferRelationDeclaration")
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = RelationType.class)
    public TransformFunction<TransferRelationDeclaration, RelationType> relationType() {
        return (source, ctx) -> {
            RelationType target = createBaseRelation(source, ctx);
            if (target == null) return null;

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/RelationType");

            // Add to container ClassType (Actor for access declarations, ClassType for regular)
            ClassType containerCt;
            if (source.eContainer() instanceof ActorDeclaration) {
                containerCt = ctx.equivalent((ActorDeclaration) source.eContainer(),
                        ClassType.class, ACTOR);
            } else if (source.eContainer() instanceof TransferDeclaration) {
                containerCt = ctx.equivalent((TransferDeclaration) source.eContainer(),
                        ClassType.class, CLASS_TYPE);
            } else {
                containerCt = null;
            }
            if (containerCt != null) {
                containerCt.getRelations().add(target);
            }

            LOG.debug("Created RelationType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CLONE_RELATION_TYPE, description = "Clone RelationType (lazy)")
    @Lazy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = RelationType.class)
    public TransformFunction<TransferRelationDeclaration, RelationType> cloneRelationType() {
        return (source, ctx) -> {
            RelationType target = createBaseRelation(source, ctx);
            if (target == null) return null;

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/CloneRelationType");

            ctx.addToResource(target);

            LOG.debug("Cloned RelationType: {}", target.getName());
            return target;
        };
    }
}
