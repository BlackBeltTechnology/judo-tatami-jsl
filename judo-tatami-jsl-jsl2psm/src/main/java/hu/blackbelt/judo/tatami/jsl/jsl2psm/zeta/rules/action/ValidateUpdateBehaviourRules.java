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
 * Validate update behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/validateUpdateBehaviour.etl (11 rules):
 * - CreateValidateUpdateOperationForEntityType (BoundOperation)
 * - CreateValidateUpdateBehaviourForTransferType (TransferOperationBehaviour)
 * - CreateValidateUpdateOperationForTransferType (BoundTransferOperation)
 * - CreateValidateUpdateOperationForTransferTypeInputParameter (Parameter)
 * - CreateValidateUpdateOperationForTransferTypeOutputParameter (Parameter)
 * - CreateValidateUpdateOperationForEntityTypeInputParameter (Parameter)
 * - CreateValidateUpdateOperationForEntityTypeOutputParameter (Parameter)
 * - CreateCardinalityForValidateUpdateTransferTypeInput (Cardinality, @Lazy)
 * - CreateCardinalityForValidateUpdateTransferTypeOutput (Cardinality, @Lazy)
 * - CreateCardinalityForValidateUpdateEntityTypeInput (Cardinality, @Lazy)
 * - CreateCardinalityForValidateUpdateEntityTypeOutput (Cardinality, @Lazy)
 *
 * Guard: generateBehaviours
 */
@TransformationContext(
        source = TransferUpdateDeclaration.class,
        target = BoundOperation.class
)
public class ValidateUpdateBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateUpdateBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isValidateUpdateBehaviourSupported(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
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
            name = CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for validate update on entity type"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isValidateUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, BoundOperation> createValidateUpdateOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateUpdateOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setInstanceRepresentation((MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));
            target.setName("validate" + firstToUpperCase(source.getName()) + fqNameToCamelCase(getFqName(container)));

            // s.eContainer.map.entity.getEntityDeclarationEquivalent().operations.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateValidateUpdateOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // TransferOperationBehaviour
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_UPDATE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for validate update"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isValidateUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, TransferOperationBehaviour> createValidateUpdateBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateUpdateBehaviourForTransferType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setBehaviourType(TransferOperationBehaviourType.VALIDATE_UPDATE);
            target.setOwner(getTransferDeclarationEquivalent(container, ctx));

            LOG.debug("Created CreateValidateUpdateBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // Transfer-level BoundTransferOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for validate update on transfer type"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isValidateUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, BoundTransferOperation> createValidateUpdateOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateUpdateOperationForTransferType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            String name = "validate" + firstToUpperCase(source.getName());
            // If a member with the same name exists, prefix with underscore
            if (container.getMembers().stream().anyMatch(m -> (m instanceof hu.blackbelt.judo.meta.jsl.jsldsl.Named) && name.equals(((hu.blackbelt.judo.meta.jsl.jsldsl.Named) m).getName()))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class,
                    CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_VALIDATE_UPDATE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            TransferObjectType transferObj = getTransferDeclarationEquivalent(container, ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateValidateUpdateOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Transfer-level Parameters
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for validate update transfer operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createValidateUpdateOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateUpdateOperationForTransferTypeInputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateUpdateTransferTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateValidateUpdateOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for validate update transfer operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createValidateUpdateOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateUpdateOperationForTransferTypeOutputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateUpdateTransferTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateValidateUpdateOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Entity-level Parameters
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for validate update entity operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createValidateUpdateOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateUpdateOperationForEntityTypeInputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateUpdateEntityTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateValidateUpdateOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for validate update entity operation"
    )
    @Greedy
    @Transform(type = TransferUpdateDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateUpdateBehaviourSupported")
    public TransformFunction<TransferUpdateDeclaration, Parameter> createValidateUpdateOperationForEntityTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateUpdateOperationForEntityTypeOutputParameter");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateUpdateEntityTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(container, ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateValidateUpdateOperationForEntityTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

}
