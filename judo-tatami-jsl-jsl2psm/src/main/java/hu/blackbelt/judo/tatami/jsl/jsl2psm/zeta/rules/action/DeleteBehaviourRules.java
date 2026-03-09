package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeleteDeclaration;
import hu.blackbelt.judo.meta.psm.data.BoundOperation;
import hu.blackbelt.judo.meta.psm.service.BoundTransferOperation;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
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
 * Delete behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/deleteBehaviour.etl (3 rules):
 * - CreateDeleteOperationForEntityType (BoundOperation)
 * - CreateDeleteBehaviourForTransferType (TransferOperationBehaviour)
 * - CreateDeleteOperationForTransferType (BoundTransferOperation)
 *
 * Guard: generateBehaviours
 */
@TransformationContext(
        source = TransferDeleteDeclaration.class,
        target = BoundOperation.class
)
public class DeleteBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isDeleteBehaviourSupported(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferDeleteDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        return generateBehaviours != null && generateBehaviours;
    }

    // ===================================================================
    // Entity-level BoundOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_DELETE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for delete on entity type"
    )
    @Greedy
    @Transform(type = TransferDeleteDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isDeleteBehaviourSupported")
    public TransformFunction<TransferDeleteDeclaration, BoundOperation> createDeleteOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDeleteOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setInstanceRepresentation((MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));
            target.setName(source.getName() + fqNameToCamelCase(getFqName(container)));

            // s.eContainer.map.entity.getEntityDeclarationEquivalent().operations.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateDeleteOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // TransferOperationBehaviour
    // ===================================================================

    @TransformRule(
            name = CREATE_DELETE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for delete"
    )
    @Greedy
    @Transform(type = TransferDeleteDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isDeleteBehaviourSupported")
    public TransformFunction<TransferDeleteDeclaration, TransferOperationBehaviour> createDeleteBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDeleteBehaviourForTransferType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setBehaviourType(TransferOperationBehaviourType.DELETE_INSTANCE);
            target.setOwner(getTransferDeclarationEquivalent(container, ctx));

            LOG.debug("Created CreateDeleteBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // Transfer-level BoundTransferOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_DELETE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for delete on transfer type"
    )
    @Greedy
    @Transform(type = TransferDeleteDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isDeleteBehaviourSupported")
    public TransformFunction<TransferDeleteDeclaration, BoundTransferOperation> createDeleteOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDeleteOperationForTransferType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setName(source.getName());
            target.setBinding(ctx.equivalent(source, BoundOperation.class,
                    CREATE_DELETE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_DELETE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            TransferObjectType transferObj = getTransferDeclarationEquivalent(container, ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateDeleteOperationForTransferType: {}", target.getName());
            return target;
        };
    }
}
