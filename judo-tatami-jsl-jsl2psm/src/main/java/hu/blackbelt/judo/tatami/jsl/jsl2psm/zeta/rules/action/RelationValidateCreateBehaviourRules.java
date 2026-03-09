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
 * Relation validate create behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/relationValidateCreateBehaviour.etl (10 rules):
 * - CreateValidateCreateOperationForEntityType (@Greedy, BoundOperation)
 * - CreateValidateCreateBehaviourForTransferType (@Greedy, TransferOperationBehaviour)
 * - CreateValidateCreateOperationForTransferType (@Greedy, BoundTransferOperation)
 * - CreateValidateCreateOperationForTransferTypeInputParameter (@Greedy, Parameter)
 * - CreateValidateCreateOperationForTransferTypeOutputParameter (@Greedy, Parameter)
 * - CreateValidateCreateOperationForEntityTypeInputParameter (@Greedy, Parameter)
 * - CreateValidateCreateOperationForEntityTypeOutputParameter (@Greedy, Parameter)
 * - CreateCardinalityForValidateCreateTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForValidateCreateTransferTypeOutput (@Lazy @Greedy)
 * - CreateCardinalityForValidateCreateEntityTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForValidateCreateEntityTypeOutput (@Lazy @Greedy)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and (s.maps() or s.reads())
 *        and s.referenceType.isCreateSupported() and s.isCreateAllowed()
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = BoundOperation.class
)
public class RelationValidateCreateBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RelationValidateCreateBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isValidateCreateRelationBehaviour(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        TransferRelationDeclaration source = (TransferRelationDeclaration) eObject;
        if (source instanceof ActorAccessDeclaration) return false;
        if (!(isMaps(source) || isReads(source))) return false;
        if (!isCreateSupported(source.getReferenceType())) return false;
        return isCreateAllowed(source);
    }

    // ===================================================================
    // CreateValidateCreateOperationForEntityType
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for validate create relation on entity type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isValidateCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundOperation> createValidateCreateOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setInstanceRepresentation((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));

            EObject createEvent = getCreateEvent(source.getReferenceType());
            String createEventName = getNameOf(createEvent);
            target.setName("validate" + firstToUpperCase(createEventName) + "ForRelation" + fqNameToCamelCase(getFqName(source)));

            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateValidateCreateOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateValidateCreateBehaviourForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_CREATE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for validate create relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isValidateCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, TransferOperationBehaviour> createValidateCreateBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.VALIDATE_CREATE);
            target.setOwner(getMappedTransferRelationEquivalent(source, ctx));

            LOG.debug("Created CreateValidateCreateBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateValidateCreateOperationForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for validate create relation on transfer type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isValidateCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundTransferOperation> createValidateCreateOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForTransferType");

            EObject createEvent = getCreateEvent(source.getReferenceType());
            String createEventName = getNameOf(createEvent);
            String name = "validate" + firstToUpperCase(createEventName) + "Of" + firstToUpperCase(source.getName());
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> name.equals(getNameOf(m)))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class, CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class, CREATE_VALIDATE_CREATE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateValidateCreateOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateValidateCreateOperationForTransferTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for validate create transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createValidateCreateOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateCreateTransferTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateValidateCreateOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateValidateCreateOperationForTransferTypeOutputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for validate create transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createValidateCreateOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForTransferTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateCreateTransferTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateValidateCreateOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateValidateCreateOperationForEntityTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for validate create entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createValidateCreateOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForEntityTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateCreateEntityTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateValidateCreateOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateValidateCreateOperationForEntityTypeOutputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for validate create entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isValidateCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createValidateCreateOperationForEntityTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForEntityTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateCreateEntityTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateValidateCreateOperationForEntityTypeOutputParameter: {}", target.getName());
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
