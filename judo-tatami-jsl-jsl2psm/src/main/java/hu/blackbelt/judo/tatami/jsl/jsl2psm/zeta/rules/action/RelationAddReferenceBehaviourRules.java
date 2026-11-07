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
 * Relation add reference behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/relationAddReferenceBehaviour.etl (6 rules):
 * - CreateAddReferenceOperationForEntityType (@Greedy, BoundOperation)
 * - CreateAddReferenceBehaviourForTransferType (@Greedy, TransferOperationBehaviour)
 * - CreateAddReferenceOperationForTransferType (@Greedy, BoundTransferOperation)
 * - CreateAddReferenceOperationForTransferTypeInputParameter (@Greedy, Parameter)
 * - CreateAddReferenceOperationForEntityTypeInputParameter (@Greedy, Parameter)
 * - CreateCardinalityForAddReferenceTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForAddReferenceEntityTypeInput (@Lazy @Greedy)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and s.isAddReferenceAllowed()
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = BoundOperation.class
)
public class RelationAddReferenceBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RelationAddReferenceBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isAddReferenceRelationBehaviour(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        TransferRelationDeclaration source = (TransferRelationDeclaration) eObject;
        if (source instanceof ActorAccessDeclaration) return false;
        return isAddReferenceAllowed(source);
    }

    // ===================================================================
    // CreateAddReferenceOperationForEntityType
    // ===================================================================

    @TransformRule(
            name = CREATE_ADD_REFERENCE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for add reference relation on entity type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isAddReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundOperation> createAddReferenceOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateAddReferenceOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setInstanceRepresentation((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));
            target.setName("addReferencesToRelation" + fqNameToCamelCase(getFqName(source)));

            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateAddReferenceOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateAddReferenceBehaviourForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_ADD_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for add reference relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isAddReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, TransferOperationBehaviour> createAddReferenceBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateAddReferenceBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.ADD_REFERENCE);
            target.setOwner(getMappedTransferRelationEquivalent(source, ctx));

            LOG.debug("Created CreateAddReferenceBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateAddReferenceOperationForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_ADD_REFERENCE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for add reference relation on transfer type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isAddReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundTransferOperation> createAddReferenceOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateAddReferenceOperationForTransferType");

            String name = "addReferencesTo" + firstToUpperCase(source.getName());
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> name.equals(getNameOf(m)))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class, CREATE_ADD_REFERENCE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class, CREATE_ADD_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateAddReferenceOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateAddReferenceOperationForTransferTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_ADD_REFERENCE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for add reference transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAddReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createAddReferenceOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateAddReferenceOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForAddReferenceTransferTypeInput", 1, isMany(source) ? -1 : 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_ADD_REFERENCE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateAddReferenceOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateAddReferenceOperationForEntityTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_ADD_REFERENCE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for add reference entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAddReferenceRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createAddReferenceOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateAddReferenceOperationForEntityTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForAddReferenceEntityTypeInput", 1, isMany(source) ? -1 : 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_ADD_REFERENCE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateAddReferenceOperationForEntityTypeInputParameter: {}", target.getName());
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
