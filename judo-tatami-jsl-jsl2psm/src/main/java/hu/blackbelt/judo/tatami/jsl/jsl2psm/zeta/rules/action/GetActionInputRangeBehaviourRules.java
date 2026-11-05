package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Get action input range behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/getActionInputRangeBehaviour.etl (12 active rules):
 * - CreateGetRangeActionInputBehaviourForTransferType (@Greedy)
 * - CreateGetRangeActionInputOperationForMappedTransferType (@Greedy)
 * - CreateGetRangeActionInputOperationForUnmappedTransferType (@Greedy)
 * - CreateGetRangeActionInputOperationForTransferTypeInputParameter (@Greedy)
 * - CreateGetRangeActionInputOperationForTransferTypeOutputParameter (@Greedy)
 * - CreateCardinalityForGetRangeActionInputTransferTypeInput (@Lazy @Greedy)
 * - CreateCardinalityForGetRangeActionInputTransferTypeOutput (@Lazy @Greedy)
 * - CreateCardinalityForGetRangeActionInputTransferTypeInputParameterOwner (@Lazy @Greedy)
 * - CreateCardinalityForGetRangeActionInputTransferTypeInputParameterQueryCustomizer (@Lazy @Greedy)
 * - CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterOwner (@Lazy @Greedy)
 * - CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterQueryCustomizer (@Lazy @Greedy)
 * - CreateGetRangeInputTypeForInputParameter (@Lazy @Greedy)
 */
@TransformationContext(
        source = TransferActionDeclaration.class,
        target = TransferOperationBehaviour.class
)
public class GetActionInputRangeBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(GetActionInputRangeBehaviourRules.class);

    // ---- Guard methods ----

    /**
     * Guard: generateBehaviours and s.parameterType?.map.isDefined()
     */
    public boolean isGetRangeActionInputGuard(EObject eObject,
                                               hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) {
            return false;
        }
        TransferActionDeclaration source = (TransferActionDeclaration) eObject;
        // s.parameterType?.map.isDefined()
        return source.getParameterType() != null
                && source.getParameterType().getMap() != null;
    }

    /**
     * Guard: generateBehaviours and s.parameterType?.map.isDefined() and s.eContainer.map.isDefined()
     */
    public boolean isGetRangeActionInputMappedGuard(EObject eObject,
                                                     hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isGetRangeActionInputGuard(eObject, ctx)) {
            return false;
        }
        TransferActionDeclaration source = (TransferActionDeclaration) eObject;
        TransferDeclaration container = (TransferDeclaration) source.eContainer();
        return container.getMap() != null;
    }

    /**
     * Guard: generateBehaviours and s.parameterType?.map.isDefined() and s.eContainer.map.isUndefined()
     */
    public boolean isGetRangeActionInputUnmappedGuard(EObject eObject,
                                                       hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isGetRangeActionInputGuard(eObject, ctx)) {
            return false;
        }
        TransferActionDeclaration source = (TransferActionDeclaration) eObject;
        TransferDeclaration container = (TransferDeclaration) source.eContainer();
        return container.getMap() == null;
    }

    // ---- Helper: getOperationDeclarationEquivalent (from action.eol) ----

    /**
     * Resolves the operation declaration equivalent for a TransferActionDeclaration.
     * Ported from action.eol: getOperationDeclarationEquivalent()
     */
    private EObject getOperationDeclarationEquivalent(
            TransferActionDeclaration source,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        TransferDeclaration container = (TransferDeclaration) source.eContainer();
        if (container.getMap() == null) {
            return ctx.equivalent(source, UnboundOperation.class,
                    CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE);
        } else if (isStatic(source)) {
            return ctx.equivalent(source, UnboundOperation.class,
                    CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE);
        } else {
            return ctx.equivalent(source,
                    hu.blackbelt.judo.meta.psm.service.BoundTransferOperation.class,
                    CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE);
        }
    }

    /**
     * Resolves the behaviour operation equivalent (mapped or unmapped).
     * Ported from action.eol: getBehaviourOperationDeclarationEquivalent()
     */
    private UnboundOperation getBehaviourOperationDeclarationEquivalent(
            TransferActionDeclaration source,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        TransferDeclaration container = (TransferDeclaration) source.eContainer();
        if (container.getMap() != null) {
            return ctx.equivalent(source, UnboundOperation.class,
                    CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_MAPPED_TRANSFER_TYPE);
        } else {
            return ctx.equivalent(source, UnboundOperation.class,
                    CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_UNMAPPED_TRANSFER_TYPE);
        }
    }

    // ---- Rules ----

    /**
     * CreateGetRangeActionInputBehaviourForTransferType
     * Creates a TransferOperationBehaviour with GET_RANGE type.
     */
    @TransformRule(
            name = CREATE_GET_RANGE_ACTION_INPUT_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create GET_RANGE behaviour for action input on transfer type"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isGetRangeActionInputGuard")
    public TransformFunction<TransferActionDeclaration, TransferOperationBehaviour> createGetRangeActionInputBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeActionInputBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.GET_RANGE);
            // t.owner = s.getOperationDeclarationEquivalent()
            target.setOwner((hu.blackbelt.judo.meta.psm.namespace.NamedElement) getOperationDeclarationEquivalent(source, ctx));

            LOG.debug("Created CreateGetRangeActionInputBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    /**
     * CreateGetRangeActionInputOperationForMappedTransferType
     * Creates an UnboundOperation for mapped containers.
     */
    @TransformRule(
            name = CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_MAPPED_TRANSFER_TYPE,
            description = "Create get range unbound operation for mapped transfer type"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isGetRangeActionInputMappedGuard")
    public TransformFunction<TransferActionDeclaration, UnboundOperation> createGetRangeActionInputOperationForMappedTransferType() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeActionInputOperationForMappedTransferType");

            target.setName("getRangeFor" + firstToUpperCase(source.getName()));

            // Check name collision
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> (m instanceof hu.blackbelt.judo.meta.jsl.jsldsl.Named) && ((hu.blackbelt.judo.meta.jsl.jsldsl.Named) m).getName().equals(target.getName()))) {
                target.setName("_" + target.getName());
            }

            // t.behaviour = s.equivalent("CreateGetRangeActionInputBehaviourForTransferType")
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_GET_RANGE_ACTION_INPUT_BEHAVIOUR_FOR_TRANSFER_TYPE));

            // s.eContainer.getTransferDeclarationEquivalent().operations.add(t)
            addOperation(getTransferDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateGetRangeActionInputOperationForMappedTransferType: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateGetRangeActionInputOperationForUnmappedTransferType
     * Creates an UnboundOperation for unmapped containers.
     */
    @TransformRule(
            name = CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_UNMAPPED_TRANSFER_TYPE,
            description = "Create get range unbound operation for unmapped transfer type"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isGetRangeActionInputUnmappedGuard")
    public TransformFunction<TransferActionDeclaration, UnboundOperation> createGetRangeActionInputOperationForUnmappedTransferType() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeActionInputOperationForUnmappedTransferType");

            target.setName("getRangeFor" + firstToUpperCase(source.getName()));

            // t.behaviour = s.equivalent("CreateGetRangeActionInputBehaviourForTransferType")
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_GET_RANGE_ACTION_INPUT_BEHAVIOUR_FOR_TRANSFER_TYPE));

            // s.eContainer.equivalent("CreateUnmappedTransferObjectType").operations.add(t)
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            UnmappedTransferObjectType unmappedType = ctx.equivalent(container,
                    UnmappedTransferObjectType.class, CREATE_UNMAPPED_TRANSFER_OBJECT_TYPE);
            addOperation(unmappedType, target);

            LOG.debug("Created CreateGetRangeActionInputOperationForUnmappedTransferType: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateGetRangeActionInputOperationForTransferTypeInputParameter
     * Creates the input parameter pointing to the custom GetRangeInput type.
     */
    @TransformRule(
            name = CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER,
            description = "Create input parameter for get range action input operation"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isGetRangeActionInputGuard")
    public TransformFunction<TransferActionDeclaration, Parameter> createGetRangeActionInputOperationForTransferTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeActionInputOperationForTransferTypeInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeActionInputTransferTypeInput", 0, 1));
            target.setName("input");
            // t.type = s.equivalent("CreateGetRangeInputTypeForInputParameter")
            target.setType(ctx.equivalent(source, UnmappedTransferObjectType.class,
                    CREATE_GET_RANGE_INPUT_TYPE_FOR_INPUT_PARAMETER));
            target.setWrapAsOptional(false);

            // s.getBehaviourOperationDeclarationEquivalent().input = t
            getBehaviourOperationDeclarationEquivalent(source, ctx).setInput(target);

            LOG.debug("Created CreateGetRangeActionInputOperationForTransferTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateGetRangeActionInputOperationForTransferTypeOutputParameter
     * Creates the output parameter for the get range operation.
     */
    @TransformRule(
            name = CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output parameter for get range action input operation"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isGetRangeActionInputGuard")
    public TransformFunction<TransferActionDeclaration, Parameter> createGetRangeActionInputOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeActionInputOperationForTransferTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeActionInputTransferTypeOutput", 0, -1));
            target.setName("return");
            // t.type = s.parameterType.getTransferDeclarationEquivalent()
            target.setType(getTransferDeclarationEquivalent(source.getParameterType(), ctx));
            target.setWrapAsOptional(false);

            // s.getBehaviourOperationDeclarationEquivalent().output = t
            getBehaviourOperationDeclarationEquivalent(source, ctx).setOutput(target);

            LOG.debug("Created CreateGetRangeActionInputOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ---- Transfer object relation rules for the range input type ----

    /**
     * CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterOwner (@Lazy @Greedy)
     * Creates the "owner" relation on the custom input type.
     */
    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_OWNER,
            description = "Create owner relation for get range action input type"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = TransferObjectRelation.class)
    public TransformFunction<TransferActionDeclaration, TransferObjectRelation> createTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterOwner() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterOwner");

            target.setName("owner");
            // t.target = s.eContainer.getTransferDeclarationEquivalent()
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            target.setTarget(getTransferDeclarationEquivalent(container, ctx));
            target.setEmbedded(true);
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeActionInputTransferTypeInputParameterOwner", 0, 1));

            return target;
        };
    }

    /**
     * CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterQueryCustomizer (@Lazy @Greedy)
     * Creates the "queryCustomizer" relation on the custom input type.
     */
    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_QUERY_CUSTOMIZER,
            description = "Create queryCustomizer relation for get range action input type"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = TransferObjectRelation.class)
    public TransformFunction<TransferActionDeclaration, TransferObjectRelation> createTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterQueryCustomizer() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterQueryCustomizer");

            target.setName("queryCustomizer");
            // t.target = s.parameterType.equivalent("CreateQueryCustomizerType")
            target.setTarget(ctx.equivalent(source.getParameterType(),
                    UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE));
            target.setEmbedded(true);
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetRangeActionInputTransferTypeInputParameterQueryCustomizer", 0, 1));

            return target;
        };
    }

    // ---- Custom input type rule ----

    /**
     * CreateGetRangeInputTypeForInputParameter (@Lazy @Greedy)
     * Creates the custom _GetRangeInput{Container}{TransferDecl}{Action} type.
     */
    @TransformRule(
            name = CREATE_GET_RANGE_INPUT_TYPE_FOR_INPUT_PARAMETER,
            description = "Create custom UnmappedTransferObjectType for get range input parameter"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    public TransformFunction<TransferActionDeclaration, UnmappedTransferObjectType> createGetRangeInputTypeForInputParameter() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetRangeInputTypeForInputParameter");

            // t.name = "_GetRangeInput" + s.eContainer.eContainer.name.firstToUpperCase()
            //        + s.eContainer.name.firstToUpperCase() + s.name.firstToUpperCase()
            TransferDeclaration transferDecl = (TransferDeclaration) source.eContainer();
            EObject grandParent = transferDecl.eContainer();
            String grandParentName = "";
            if (grandParent instanceof ModelDeclaration) {
                grandParentName = ((ModelDeclaration) grandParent).getName();
            }
            target.setName("_GetRangeInput"
                    + firstToUpperCase(grandParentName)
                    + firstToUpperCase(transferDecl.getName())
                    + firstToUpperCase(source.getName()));

            // t.relations.add(owner relation)
            target.getRelations().add(ctx.equivalent(source, TransferObjectRelation.class,
                    CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_OWNER));

            // if (s.parameterType?.map.isDefined())
            if (source.getParameterType() != null && source.getParameterType().getMap() != null) {
                target.getRelations().add(ctx.equivalent(source, TransferObjectRelation.class,
                        CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_QUERY_CUSTOMIZER));
            }

            // s.eContainer.eContainer.getModelRoot().elements.add(t)
            Package modelRoot = getModelRoot(transferDecl, ctx);
            addElement(modelRoot, target);

            LOG.debug("Get range input type created: {}", target.getName());
            return target;
        };
    }
}
