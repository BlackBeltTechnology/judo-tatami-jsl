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
 * Relation list behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/relationListBehaviour.etl (10 rules):
 * - CreateListOperationForEntityType (@Greedy, BoundOperation)
 * - CreateListBehaviourForTransferType (@Greedy, TransferOperationBehaviour)
 * - CreateListOperationForTransferType (@Greedy, BoundTransferOperation)
 * - CreateListOperationForTransferTypeInputParameter (@Greedy, Parameter)
 * - CreateListOperationForTransferTypeOutputParameter (@Greedy, Parameter)
 * - CreateListOperationForEntityTypeInputParameter (@Greedy, Parameter)
 * - CreateListOperationForEntityTypeOutputParameter (@Greedy, Parameter)
 * - CreateCardinalityForListTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForListTransferTypeOutput (@Lazy @Greedy)
 * - CreateCardinalityForListEntityTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForListEntityTypeOutput (@Lazy @Greedy)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and (s.maps() or s.reads())
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = BoundOperation.class
)
public class RelationListBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RelationListBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isListRelationBehaviour(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        TransferRelationDeclaration source = (TransferRelationDeclaration) eObject;
        if (source instanceof ActorAccessDeclaration) return false;
        return isMaps(source) || isReads(source);
    }

    // ===================================================================
    // CreateListOperationForEntityType
    // ===================================================================

    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for list relation on entity type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isListRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundOperation> createListOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForEntityType");

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setInstanceRepresentation((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(container, ctx));
            target.setName("listRelation" + fqNameToCamelCase(getFqName(source)));

            // s.eContainer.map.entity.getEntityDeclarationEquivalent().operations.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    container.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateListOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateListBehaviourForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_LIST_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for list relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isListRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, TransferOperationBehaviour> createListBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.LIST);
            target.setOwner(getMappedTransferRelationEquivalent(source, ctx));

            LOG.debug("Created CreateListBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateListOperationForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for list relation on transfer type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isListRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, BoundTransferOperation> createListOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForTransferType");

            String name = "listOf" + firstToUpperCase(source.getName());
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> name.equals(getNameOf(m)))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class, CREATE_LIST_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class, CREATE_LIST_BEHAVIOUR_FOR_TRANSFER_TYPE));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateListOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateListOperationForTransferTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for list transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isListRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createListOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForListTransferTypeInput", 0, 1));
            target.setName("input");
            target.setType(ctx.equivalent(source.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateListOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateListOperationForTransferTypeOutputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for list transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isListRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createListOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForTransferTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForListTransferTypeOutput", isRequired(source) && !isMany(source) ? 1 : 0, isMany(source) ? -1 : 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class, CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateListOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateListOperationForEntityTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for list entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isListRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createListOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForEntityTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForListEntityTypeInput", 0, 1));
            target.setName("input");
            target.setType(ctx.equivalent(source.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_LIST_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateListOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateListOperationForEntityTypeOutputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for list entity operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isListRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createListOperationForEntityTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForEntityTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForListEntityTypeOutput", isRequired(source) && !isMany(source) ? 1 : 0, isMany(source) ? -1 : 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class, CREATE_LIST_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateListOperationForEntityTypeOutputParameter: {}", target.getName());
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
