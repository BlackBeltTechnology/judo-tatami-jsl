package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorAccessDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Relation get range reference behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/relationGetRangeReferenceBehaviour.etl (11 rules):
 * - CreateGetRangeRelationBehaviourForTransferType (@Greedy, TransferOperationBehaviour)
 * - CreateGetRangeRelationOperationForTransferType (@Greedy, UnboundOperation)
 * - CreateGetRangeRelationOperationForTransferTypeInputParameter (@Greedy, Parameter)
 * - CreateGetRangeRelationOperationForTransferTypeOutputParameter (@Greedy, Parameter)
 * - CreateCardinalityForGetRangeRelationTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForGetRangeRelationTransferTypeOutput (@Lazy @Greedy)
 * - CreateCardinalityForGetRangeRelationTransferTypeInputOwner (@Lazy @Greedy)
 * - CreateCardinalityForGetRangeRelationTransferTypeInputQueryCustomizer (@Lazy @Greedy)
 * - CreateTransferObjectRelationForGetRangeRelationTransferTypeInputOwner (@Lazy @Greedy)
 * - CreateTransferObjectRelationForGetRangeRelationTransferTypeInputQueryCustomizer (@Lazy @Greedy)
 * - CreateGetRangeInputType (@Lazy @Greedy, UnmappedTransferObjectType)
 *
 * Guard: generateBehaviours and not s.isActorRelated() and s.isGetRangeSupported()
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = UnboundOperation.class
)
public class RelationGetRangeReferenceBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(RelationGetRangeReferenceBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isGetRangeRelationBehaviour(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        TransferRelationDeclaration source = (TransferRelationDeclaration) eObject;
        if (source instanceof ActorAccessDeclaration) return false;
        return isGetRangeSupported(source);
    }

    // ===================================================================
    // CreateGetRangeRelationBehaviourForTransferType
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_RANGE_RELATION_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for get range relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isGetRangeRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, TransferOperationBehaviour> createGetRangeRelationBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeRelationBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.GET_RANGE);
            target.setOwner(getMappedTransferRelationEquivalent(source, ctx));

            LOG.debug("Created CreateGetRangeRelationBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateGetRangeRelationOperationForTransferType (UnboundOperation)
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create UnboundOperation for get range relation on transfer type"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isGetRangeRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, UnboundOperation> createGetRangeRelationOperationForTransferType() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeRelationOperationForTransferType");

            String name = "getRangeFor" + firstToUpperCase(source.getName());
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> name.equals(getNameOf(m)))) {
                target.setName("_" + name);
            } else {
                target.setName(name);
            }

            target.setImmutable(true);

            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class, CREATE_GET_RANGE_RELATION_BEHAVIOUR_FOR_TRANSFER_TYPE));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateGetRangeRelationOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateGetRangeRelationOperationForTransferTypeInputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for get range relation transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isGetRangeRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createGetRangeRelationOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeRelationOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeRelationTransferTypeInput", 0, 1));
            target.setName("input");
            target.setType(ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_GET_RANGE_INPUT_TYPE));
            target.setWrapAsOptional(false);

            UnboundOperation op = ctx.equivalent(source, UnboundOperation.class, CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateGetRangeRelationOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateGetRangeRelationOperationForTransferTypeOutputParameter
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for get range relation transfer operation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isGetRangeRelationBehaviour")
    public TransformFunction<TransferRelationDeclaration, Parameter> createGetRangeRelationOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeRelationOperationForTransferTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeRelationTransferTypeOutput", 0, -1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            UnboundOperation op = ctx.equivalent(source, UnboundOperation.class, CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateGetRangeRelationOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_OWNER,
            description = "Create owner TransferObjectRelation for get range input type"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    public TransformFunction<TransferRelationDeclaration, TransferObjectRelation> createTransferObjectRelationForGetRangeRelationTransferTypeInputOwner() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectRelationForGetRangeRelationTransferTypeInputOwner");

            target.setName("owner");
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setTarget(getTransferDeclarationEquivalent(container, ctx));
            target.setEmbedded(true);
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeRelationTransferTypeInputOwner", 0, 1));

            return target;
        };
    }

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_QUERY_CUSTOMIZER,
            description = "Create queryCustomizer TransferObjectRelation for get range input type"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    public TransformFunction<TransferRelationDeclaration, TransferObjectRelation> createTransferObjectRelationForGetRangeRelationTransferTypeInputQueryCustomizer() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectRelationForGetRangeRelationTransferTypeInputQueryCustomizer");

            target.setName("queryCustomizer");
            target.setTarget(ctx.equivalent(source.getReferenceType(),
                    UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE));
            target.setEmbedded(true);
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeRelationTransferTypeInputQueryCustomizer", 0, 1));

            return target;
        };
    }

    // ===================================================================
    // CreateGetRangeInputType (@Lazy @Greedy, UnmappedTransferObjectType)
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_RANGE_INPUT_TYPE,
            description = "Create UnmappedTransferObjectType as input type for get range operation"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    public TransformFunction<TransferRelationDeclaration, UnmappedTransferObjectType> createGetRangeInputType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeInputType");

            // t.name = "_GetRangeInput" + s.eContainer.eContainer.name.firstToUpperCase() + s.eContainer.name.firstToUpperCase() + s.name.firstToUpperCase()
            TransferDeclaration transferContainer = (TransferDeclaration) source.eContainer();
            EObject modelContainer = transferContainer.eContainer();
            String modelName = getNameOf(modelContainer);
            String containerName = getNameOf(transferContainer);
            target.setName("_GetRangeInput" + firstToUpperCase(modelName) + firstToUpperCase(containerName) + firstToUpperCase(source.getName()));

            // Add owner relation
            TransferObjectRelation ownerRelation = ctx.equivalent(source, TransferObjectRelation.class,
                    CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_OWNER);
            if (ownerRelation != null) {
                target.getRelations().add(ownerRelation);
            }

            // Add queryCustomizer relation only if referenceType has a map (s.referenceType.map.isDefined())
            if (source.getReferenceType().getMap() != null) {
                TransferObjectRelation qcRelation = ctx.equivalent(source, TransferObjectRelation.class,
                        CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_QUERY_CUSTOMIZER);
                if (qcRelation != null) {
                    target.getRelations().add(qcRelation);
                }
            }

            // s.eContainer.eContainer.getModelRoot().elements.add(t)
            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Get range input type created: {}", target.getName());
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
