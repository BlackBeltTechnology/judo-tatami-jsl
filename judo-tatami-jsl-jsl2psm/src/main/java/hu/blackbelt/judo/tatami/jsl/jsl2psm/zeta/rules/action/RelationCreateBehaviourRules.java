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
 * Relation create behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/relationCreateBehaviour.etl (10 rules):
 * - CreateCreateOperationForEntityType (@Greedy, BoundOperation)
 * - CreateCreateBehaviourForTransferType (@Greedy, TransferOperationBehaviour)
 * - CreateCreateOperationForTransferType (@Greedy, BoundTransferOperation)
 * - CreateCreateOperationForTransferTypeInputParameter (@Greedy, Parameter)
 * - CreateCreateOperationForTransferTypeOutputParameter (@Greedy, Parameter)
 * - CreateCreateOperationForEntityTypeInputParameter (@Greedy, Parameter)
 * - CreateCreateOperationForEntityTypeOutputParameter (@Greedy, Parameter)
 * - CreateCardinalityForCreateTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForCreateTransferTypeOutput (@Lazy @Greedy)
 * - CreateCardinalityForCreateEntityTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForCreateEntityTypeOutput (@Lazy @Greedy)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and (s.maps() or s.reads())
 *        and s.referenceType.isCreateSupported() and s.isCreateAllowed()
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = BoundOperation.class
)
public class RelationCreateBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RelationCreateBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isCreateRelationBehaviour(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
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
    // CreateCreateOperationForEntityType
    // ===================================================================

    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for create relation on entity type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundOperation> createCreateOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setInstanceRepresentation((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));

            EObject createEvent = getCreateEvent(source.getReferenceType());
            String createEventName = getNameOf(createEvent);
            target.setName(createEventName + "ForRelation" + fqNameToCamelCase(getFqName(source)));

            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateCreateOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateCreateBehaviourForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_CREATE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for create relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, TransferOperationBehaviour> createCreateBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.CREATE_INSTANCE);
            target.setOwner(getMappedTransferRelationEquivalent(source, ctx));

            LOG.debug("Created CreateCreateBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateCreateOperationForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for create relation on transfer type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundTransferOperation> createCreateOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForTransferType");

            EObject createEvent = getCreateEvent(source.getReferenceType());
            String createEventName = getNameOf(createEvent);
            String name = createEventName + "Of" + firstToUpperCase(source.getName());
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> name.equals(getNameOf(m)))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class, CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class, CREATE_CREATE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateCreateOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateCreateOperationForTransferTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for create transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createCreateOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForCreateTransferTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateCreateOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateCreateOperationForTransferTypeOutputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for create transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createCreateOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForTransferTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForCreateTransferTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateCreateOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateCreateOperationForEntityTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for create entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createCreateOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForEntityTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForCreateEntityTypeInput", 1, 1));
            target.setName("input");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateCreateOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateCreateOperationForEntityTypeOutputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for create entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isCreateRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createCreateOperationForEntityTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForEntityTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForCreateEntityTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateCreateOperationForEntityTypeOutputParameter: {}", target.getName());
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
