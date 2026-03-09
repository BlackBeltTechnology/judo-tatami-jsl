package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
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
 * Refresh behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/refreshBehaviour.etl (10 rules):
 * - CreateRefreshOperationForEntityType (BoundOperation)
 * - CreateRefreshBehaviourForTransferType (TransferOperationBehaviour)
 * - CreateRefreshOperationForTransferType (BoundTransferOperation)
 * - CreateRefreshOperationForTransferTypeInputParameter (Parameter)
 * - CreateRefreshOperationForTransferTypeOutputParameter (Parameter)
 * - CreateRefreshOperationForEntityTypeInputParameter (Parameter)
 * - CreateRefreshOperationForEntityTypeOutputParameter (Parameter)
 * - CreateCardinalityForRefreshTransferTypeInput (Cardinality, @Lazy)
 * - CreateCardinalityForRefreshTransferTypeOutput (Cardinality, @Lazy)
 * - CreateCardinalityForRefreshEntityTypeInput (Cardinality, @Lazy)
 * - CreateCardinalityForRefreshEntityTypeOutput (Cardinality, @Lazy)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and s.map.isDefined()
 */
@TransformationContext(
        source = TransferDeclaration.class,
        target = BoundOperation.class
)
public class RefreshBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isRefreshSupported(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) {
            return false;
        }
        TransferDeclaration source = (TransferDeclaration) eObject;
        return !isActorRelated(source) && source.getMap() != null;
    }

    // ===================================================================
    // Entity-level BoundOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation for refresh on entity type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isRefreshSupported")
    public TransformFunction<TransferDeclaration, BoundOperation> createRefreshOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateRefreshOperationForEntityType");

            target.setInstanceRepresentation((MappedTransferObjectType) getTransferDeclarationEquivalent(source, ctx));
            target.setName("Refresh" + fqNameToCamelCase(getFqName(source)));

            // s.map.entity.getEntityDeclarationEquivalent().operations.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    source.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateRefreshOperationForEntityType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // TransferOperationBehaviour
    // ===================================================================

    @TransformRule(
            name = CREATE_REFRESH_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for refresh"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isRefreshSupported")
    public TransformFunction<TransferDeclaration, TransferOperationBehaviour> createRefreshBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateRefreshBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.REFRESH);
            target.setOwner(getTransferDeclarationEquivalent(source, ctx));

            LOG.debug("Created CreateRefreshBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // Transfer-level BoundTransferOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create BoundTransferOperation for refresh on transfer type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isRefreshSupported")
    public TransformFunction<TransferDeclaration, BoundTransferOperation> createRefreshOperationForTransferType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateRefreshOperationForTransferType");

            target.setName("refreshInstance");
            // If a member with the same name exists, prefix with underscore
            if (hasMemberWithName(source, "refreshInstance")) {
                target.setName("_refreshInstance");
            }

            target.setBinding(ctx.equivalent(source, BoundOperation.class,
                    CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE));
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_REFRESH_BEHAVIOUR_FOR_TRANSFER_TYPE));

            TransferObjectType transferObj = getTransferDeclarationEquivalent(source, ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateRefreshOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Transfer-level Parameters
    // ===================================================================

    @TransformRule(
            name = CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for refresh transfer operation"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isRefreshSupported")
    public TransformFunction<TransferDeclaration, Parameter> createRefreshOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateRefreshOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForRefreshTransferTypeInput", 1, 1));
            target.setName("input");
            target.setType(ctx.equivalent(source,
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class,
                    CREATE_QUERY_CUSTOMIZER_TYPE));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateRefreshOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for refresh transfer operation"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isRefreshSupported")
    public TransformFunction<TransferDeclaration, Parameter> createRefreshOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateRefreshOperationForTransferTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForRefreshTransferTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source, ctx));
            target.setWrapAsOptional(false);

            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateRefreshOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Entity-level Parameters
    // ===================================================================

    @TransformRule(
            name = CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for refresh entity operation"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isRefreshSupported")
    public TransformFunction<TransferDeclaration, Parameter> createRefreshOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateRefreshOperationForEntityTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForRefreshEntityTypeInput", 1, 1));
            target.setName("input");
            target.setType(ctx.equivalent(source,
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class,
                    CREATE_QUERY_CUSTOMIZER_TYPE));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateRefreshOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for refresh entity operation"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isRefreshSupported")
    public TransformFunction<TransferDeclaration, Parameter> createRefreshOperationForEntityTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateRefreshOperationForEntityTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForRefreshEntityTypeOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source, ctx));
            target.setWrapAsOptional(false);

            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateRefreshOperationForEntityTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

}
