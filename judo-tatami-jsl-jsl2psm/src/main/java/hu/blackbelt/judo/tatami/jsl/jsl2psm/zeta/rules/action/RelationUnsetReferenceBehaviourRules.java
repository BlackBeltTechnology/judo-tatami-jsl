package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorAccessDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.psm.data.BoundOperation;
import hu.blackbelt.judo.meta.psm.service.BoundTransferOperation;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Relation unset reference behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/relationUnsetReferenceBehaviour.etl (6 rules):
 * - CreateUnsetReferenceOperationForEntityType (@Greedy, BoundOperation)
 * - CreateUnsetReferenceBehaviourForTransferType (@Greedy, TransferOperationBehaviour)
 * - CreateUnsetReferenceOperationForTransferType (@Greedy, BoundTransferOperation)
 * - CreateUnsetReferenceOperationForTransferTypeInputParameter (@Greedy, Parameter)
 * - CreateUnsetReferenceOperationForEntityTypeInputParameter (@Greedy, Parameter)
 * - CreateCardinalityForUnsetReferenceTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForUnsetReferenceEntityTypeInput (@Lazy @Greedy)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and s.isUnsetReferenceAllowed()
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = BoundOperation.class
)
public class RelationUnsetReferenceBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RelationUnsetReferenceBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isUnsetReferenceRelationBehaviour(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        TransferRelationDeclaration source = (TransferRelationDeclaration) eObject;
        if (source instanceof ActorAccessDeclaration) return false;
        return isUnsetReferenceAllowed(source);
    }

    // ===================================================================
    // CreateUnsetReferenceOperationForEntityType
    // ===================================================================

    @TransformRule(
            name = CREATE_UNSET_REFERENCE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for unset reference relation on entity type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isUnsetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundOperation> createUnsetReferenceOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnsetReferenceOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setInstanceRepresentation((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));
            target.setName("unsetReferencesOfRelation" + fqNameToCamelCase(getFqName(source)));

            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateUnsetReferenceOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateUnsetReferenceBehaviourForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_UNSET_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for unset reference relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isUnsetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, TransferOperationBehaviour> createUnsetReferenceBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnsetReferenceBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.UNSET_REFERENCE);
            target.setOwner(getMappedTransferRelationEquivalent(source, ctx));

            LOG.debug("Created CreateUnsetReferenceBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateUnsetReferenceOperationForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_UNSET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for unset reference relation on transfer type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isUnsetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundTransferOperation> createUnsetReferenceOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnsetReferenceOperationForTransferType");

            String name = "unsetReferencesOf" + firstToUpperCase(source.getName());
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> name.equals(getNameOf(m)))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class, CREATE_UNSET_REFERENCE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class, CREATE_UNSET_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateUnsetReferenceOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateUnsetReferenceOperationForTransferTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_UNSET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for unset reference transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUnsetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createUnsetReferenceOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnsetReferenceOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForUnsetReferenceTransferTypeInput", isRequired(source) && !isMany(source) ? 1 : 0, isMany(source) ? -1 : 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_UNSET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateUnsetReferenceOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateUnsetReferenceOperationForEntityTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_UNSET_REFERENCE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for unset reference entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUnsetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createUnsetReferenceOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnsetReferenceOperationForEntityTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForUnsetReferenceEntityTypeInput", isRequired(source) && !isMany(source) ? 1 : 0, isMany(source) ? -1 : 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_UNSET_REFERENCE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateUnsetReferenceOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Helper: get name of a member (reflective)
    // ===================================================================

    private static String getNameOf(EObject member) {
        if (member == null) return null;
        try {
            java.lang.reflect.Method nameMethod = member.getClass().getMethod("getName");
            return (String) nameMethod.invoke(member);
        } catch (Exception e) {
            return null;
        }
    }
}
