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
 * Relation set reference behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/relationSetReferenceBehaviour.etl (6 rules):
 * - CreateSetReferenceOperationForEntityType (@Greedy, BoundOperation)
 * - CreateSetReferenceBehaviourForTransferType (@Greedy, TransferOperationBehaviour)
 * - CreateSetReferenceOperationForTransferType (@Greedy, BoundTransferOperation)
 * - CreateSetReferenceOperationForTransferTypeInputParameter (@Greedy, Parameter)
 * - CreateSetReferenceOperationForEntityTypeInputParameter (@Greedy, Parameter)
 * - CreateCardinalityForSetReferenceTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForSetReferenceEntityTypeInput (@Lazy @Greedy)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and s.isSetReferenceAllowed()
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = BoundOperation.class
)
public class RelationSetReferenceBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RelationSetReferenceBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isSetReferenceRelationBehaviour(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        TransferRelationDeclaration source = (TransferRelationDeclaration) eObject;
        if (source instanceof ActorAccessDeclaration) return false;
        return isSetReferenceAllowed(source);
    }

    // ===================================================================
    // CreateSetReferenceOperationForEntityType
    // ===================================================================

    @TransformRule(
            name = CREATE_SET_REFERENCE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for set reference relation on entity type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isSetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundOperation> createSetReferenceOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateSetReferenceOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setInstanceRepresentation((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));
            target.setName("setReferencesOfRelation" + fqNameToCamelCase(getFqName(source)));

            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateSetReferenceOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateSetReferenceBehaviourForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_SET_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for set reference relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isSetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, TransferOperationBehaviour> createSetReferenceBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateSetReferenceBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.SET_REFERENCE);
            target.setOwner(getMappedTransferRelationEquivalent(source, ctx));

            LOG.debug("Created CreateSetReferenceBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateSetReferenceOperationForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_SET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for set reference relation on transfer type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isSetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundTransferOperation> createSetReferenceOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateSetReferenceOperationForTransferType");

            String name = "setReferencesOf" + firstToUpperCase(source.getName());
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> name.equals(getNameOf(m)))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class, CREATE_SET_REFERENCE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class, CREATE_SET_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateSetReferenceOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateSetReferenceOperationForTransferTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_SET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for set reference transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isSetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createSetReferenceOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateSetReferenceOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForSetReferenceTransferTypeInput", isRequired(source) && !isMany(source) ? 1 : 0, isMany(source) ? -1 : 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_SET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateSetReferenceOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateSetReferenceOperationForEntityTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_SET_REFERENCE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for set reference entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isSetReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createSetReferenceOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateSetReferenceOperationForEntityTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForSetReferenceEntityTypeInput", isRequired(source) && !isMany(source) ? 1 : 0, isMany(source) ? -1 : 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_SET_REFERENCE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateSetReferenceOperationForEntityTypeInputParameter: {}", target.getName());
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
