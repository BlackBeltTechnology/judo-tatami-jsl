package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.actor;

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorAccessDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Access rules for JSL to PSM transformation.
 *
 * Ported from actor/access.etl (2 rules):
 * - CreateTransientTransferObjectRelationForActorAccessDeclaration
 * - CreateCardinalityForAccessDeclaration (@lazy @greedy)
 */
@TransformationContext(
        source = ActorAccessDeclaration.class,
        target = TransferObjectRelation.class
)
public class AccessRules {

    private static final Logger LOG = LoggerFactory.getLogger(AccessRules.class);

    /**
     * CreateTransientTransferObjectRelationForActorAccessDeclaration
     * Transforms ActorAccessDeclaration to a TransferObjectRelation on the actor type.
     */
    @TransformRule(
            name = CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_ACTOR_ACCESS_DECLARATION,
            description = "Transform ActorAccessDeclaration to TransferObjectRelation on actor type"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = TransferObjectRelation.class)
    public TransformFunction<ActorAccessDeclaration, TransferObjectRelation> createTransientTransferObjectRelationForActorAccessDeclaration() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransientTransferObjectRelationForActorAccessDeclaration");

            target.setName(source.getName());

            // t.target = s.getReferenceType().getTransferDeclarationEquivalent()
            target.setTarget(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));

            target.setAccess(true);

            if (isCreateAllowed(source)) {
                target.setEmbeddedCreate(true);
            }
            if (isUpdateAllowed(source)) {
                target.setEmbeddedUpdate(true);
            }
            if (isDeleteAllowed(source)) {
                target.setEmbeddedDelete(true);
            }
            target.setEmbedded(isAggregation(source));

            // t.cardinality = s.equivalentDiscriminated("CreateCardinalityForAccessDeclaration", t.getId())
            {
                int lower = isRequired(source) && !isMany(source) ? 1 : 0;
                int upper = isMany(source) ? -1 : 1;
                target.setCardinality(createCardinality(ctx,
                        "(jsl/" + getJslId(source) + ")/CreateCardinalityForAccessDeclaration",
                        lower, upper));
            }

            // Default value
            if (getDefault(source) != null) {
                var defaultNav = ctx.equivalent(getDefault(source),
                        hu.blackbelt.judo.meta.psm.derived.StaticNavigation.class,
                        "CreateDefaultStaticNavigationForUnmappedTransferObjectConstructor");
                target.setDefaultValue(defaultNav);
            }

            // Binding
            EObject getterExpr = source.getGetterExpr();
            if (getterExpr != null) {
                EObject container = source.eContainer();
                if (container instanceof ActorDeclaration) {
                    ActorDeclaration actor = (ActorDeclaration) container;
                    if (actor.getMap() != null) {
                        // mapped: s.getterExpr.equivalent("CreateReadsNavigationPropertyForMappedTransferObjectTransferRelationDeclaration")
                        var binding = ctx.equivalent(getterExpr,
                                hu.blackbelt.judo.meta.psm.derived.NavigationProperty.class,
                                "CreateReadsNavigationPropertyForMappedTransferObjectTransferRelationDeclaration");
                        target.setBinding(binding);
                    } else {
                        // unmapped: s.getterExpr.equivalent("CreateReadsReferenceExpressionForUnmappedTransferObjectTransferRelationDeclaration")
                        // ReferenceExpressionType is not a ReferenceTypedElement, use reflective EMF
                        EObject binding = ctx.equivalent(getterExpr, EObject.class,
                                "CreateReadsReferenceExpressionForUnmappedTransferObjectTransferRelationDeclaration");
                        if (binding != null) {
                            target.eSet(target.eClass().getEStructuralFeature("binding"), binding);
                        }
                    }
                }
            }

            // Add to actor type: s.eContainer.getActorDeclarationEquivalent().relations.add(t)
            EObject container = source.eContainer();
            if (container instanceof ActorDeclaration) {
                addTransferRelation(
                        getActorDeclarationEquivalent((ActorDeclaration) container, ctx),
                        target);
            }

            LOG.debug("Created TransferObjectRelation for ActorAccessDeclaration: [{}] into [{}]",
                    target.getName(),
                    target.eContainer() != null ? ((hu.blackbelt.judo.meta.psm.namespace.NamedElement) target.eContainer()).getName() : "?");
            return target;
        };
    }

}
