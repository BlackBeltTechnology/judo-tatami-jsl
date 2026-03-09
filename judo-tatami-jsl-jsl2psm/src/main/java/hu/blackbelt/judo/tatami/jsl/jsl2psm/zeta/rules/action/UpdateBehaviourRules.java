package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferUpdateDeclaration;
import hu.blackbelt.judo.meta.psm.data.BoundOperation;
import hu.blackbelt.judo.meta.psm.service.BoundTransferOperation;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Update behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/updateBehaviour.etl (11 rules):
 * - CreateUpdateOperationForEntityType (BoundOperation)
 * - CreateUpdateBehaviourForTransferType (TransferOperationBehaviour)
 * - CreateUpdateOperationForTransferType (BoundTransferOperation)
 * - CreateUpdateOperationForTransferTypeInputParameter (Parameter)
 * - CreateUpdateOperationForTransferTypeOutputParameter (Parameter)
 * - CreateUpdateOperationForEntityTypeInputParameter (Parameter)
 * - CreateUpdateOperationForEntityTypeOutputParameter (Parameter)
 * - CreateCardinalityForUpdateTransferTypeInput (Cardinality, @Lazy)
 * - CreateCardinalityForUpdateTransferTypeOutput (Cardinality, @Lazy)
 * - CreateCardinalityForUpdateEntityTypeInput (Cardinality, @Lazy)
 * - CreateCardinalityForUpdateEntityTypeOutput (Cardinality, @Lazy)
 *
 * Guard: generateBehaviours
 */
@TransformationContext(
        source = TransferUpdateDeclaration.class,
        target = BoundOperation.class
)
public class UpdateBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isUpdateBehaviourSupported(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferUpdateDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        return generateBehaviours != null && generateBehaviours;
    }

    // ===================================================================
    // Entity-level BoundOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for update on entity type"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, BoundOperation> createUpdateOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUpdateOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setInstanceRepresentation((MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));
            target.setName(source.getName() + fqNameToCamelCase(getFqName(container)));

            // s.eContainer.map.entity.getEntityDeclarationEquivalent().operations.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateUpdateOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // TransferOperationBehaviour
    // ===================================================================

    @TransformRule(
            name = CREATE_UPDATE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for update"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, TransferOperationBehaviour> createUpdateBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUpdateBehaviourForTransferType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setBehaviourType(TransferOperationBehaviourType.UPDATE_INSTANCE);
            target.setOwner(getTransferDeclarationEquivalent(container, ctx));

            LOG.debug("Created CreateUpdateBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // Transfer-level BoundTransferOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for update on transfer type"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, BoundTransferOperation> createUpdateOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUpdateOperationForTransferType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setName(source.getName());
            target.setBinding(ctx.equivalent(source, BoundOperation.class,
                    CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_UPDATE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            TransferObjectType transferObj = getTransferDeclarationEquivalent(container, ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateUpdateOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Transfer-level Parameters
    // ===================================================================

    @TransformRule(
            name = CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for update transfer operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createUpdateOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUpdateOperationForTransferTypeInputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForUpdateTransferTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateUpdateOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for update transfer operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createUpdateOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUpdateOperationForTransferTypeOutputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForUpdateTransferTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateUpdateOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Entity-level Parameters
    // ===================================================================

    @TransformRule(
            name = CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for update entity operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createUpdateOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUpdateOperationForEntityTypeInputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForUpdateEntityTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateUpdateOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for update entity operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createUpdateOperationForEntityTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUpdateOperationForEntityTypeOutputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForUpdateEntityTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateUpdateOperationForEntityTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

}
