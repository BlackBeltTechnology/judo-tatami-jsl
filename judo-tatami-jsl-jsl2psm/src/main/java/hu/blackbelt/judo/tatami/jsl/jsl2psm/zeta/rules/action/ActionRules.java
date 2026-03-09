package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.ErrorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.psm.data.BoundOperation;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.ReferenceExpressionType;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.service.BoundTransferOperation;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.data.OperationBody;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Action rules for JSL to PSM transformation.
 *
 * Ported from action/action.etl (30 rules):
 * - 4 abstract rules (shared populate methods)
 * - CreateEmptyOperationBody (@Greedy)
 * - CreateFaultParameter (@Lazy @Greedy, transforms ErrorDeclaration)
 * - 3 unmapped TO unbound operations + input/output params
 * - 3 mapped TO unbound operations + input/output params
 * - 4 entity bound operations + input/output params
 * - 3 mapped TO bound transfer operations + input/output params
 * - 7 cardinality rules (@Lazy @Greedy)
 * - 6 action input parameter range rules (mapped/unmapped)
 */
@TransformationContext(
        source = TransferActionDeclaration.class,
        target = UnboundOperation.class
)
public class ActionRules {

    private static final Logger LOG = LoggerFactory.getLogger(ActionRules.class);

    // ===================================================================
    // Abstract rule helpers (inlined from @abstract ETL rules)
    // ===================================================================

    /**
     * Populate shared fields for AbstractTransferUnboundActionDeclaration.
     * ETL @abstract rule sets: name, updateOnResult, deleteOnResult, implementation, inputRange, faults.
     */
    private void populateUnboundAction(TransferActionDeclaration source, UnboundOperation target,
                                        hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());
        target.setUpdateOnResult(isActionUpdateAllowed(source));
        target.setDeleteOnResult(isActionDeleteAllowed(source));
        target.setImplementation(ctx.equivalent(source, OperationBody.class, CREATE_EMPTY_OPERATION_BODY));

        // if (s.parameterType?.map.isDefined()) { t.inputRange = choice.equivalent("CreateActionInputParameterRangeTransferObjectRelation") }
        if (source.getParameterType() != null && source.getParameterType().getMap() != null) {
            ChoiceModifier choice = getChoiceModifier(source);
            if (choice != null) {
                TransferObjectRelation inputRange = ctx.equivalent(choice, TransferObjectRelation.class,
                        CREATE_ACTION_INPUT_PARAMETER_RANGE_TRANSFER_OBJECT_RELATION);
                target.setInputRange(inputRange);
            }
        }

        // faults from errors
        for (ErrorDeclaration error : source.getErrors()) {
            Parameter faultParam = ctx.equivalentDiscriminated(error, Parameter.class,
                    CREATE_FAULT_PARAMETER, getJslId(source));
            if (faultParam != null) {
                target.getFaults().add(faultParam);
            }
        }
    }

    /**
     * Populate shared fields for AbstractTransferBoundActionDeclaration.
     * ETL @abstract rule sets: name, updateOnResult, deleteOnResult, inputRange, faults.
     */
    private void populateBoundTransferAction(TransferActionDeclaration source, BoundTransferOperation target,
                                              hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());
        target.setUpdateOnResult(isActionUpdateAllowed(source));
        target.setDeleteOnResult(isActionDeleteAllowed(source));

        // if (s.parameterType?.map.isDefined()) { t.inputRange = choice.equivalent(...) }
        if (source.getParameterType() != null && source.getParameterType().getMap() != null) {
            ChoiceModifier choice = getChoiceModifier(source);
            if (choice != null) {
                TransferObjectRelation inputRange = ctx.equivalent(choice, TransferObjectRelation.class,
                        CREATE_ACTION_INPUT_PARAMETER_RANGE_TRANSFER_OBJECT_RELATION);
                target.setInputRange(inputRange);
            }
        }

        // faults from errors (with "/Transfer" discriminator suffix)
        for (ErrorDeclaration error : source.getErrors()) {
            Parameter faultParam = ctx.equivalentDiscriminated(error, Parameter.class,
                    CREATE_FAULT_PARAMETER, getJslId(source) + "/Transfer");
            if (faultParam != null) {
                target.getFaults().add(faultParam);
            }
        }
    }

    /**
     * Populate shared fields for AbstractTransferActionDeclarationReturnParameter.
     */
    private void populateReturnParameter(TransferActionDeclaration source, Parameter target,
                                          hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForTransferActionDeclarationOutput", 1, 1));
        target.setName("return");
        target.setType(getTransferDeclarationEquivalent((TransferDeclaration) source.getReturn(), ctx));
        target.setWrapAsOptional(false);
    }

    /**
     * Populate shared fields for AbstractTransferActionDeclarationInputParameter.
     */
    private void populateInputParameter(TransferActionDeclaration source, Parameter target,
                                         hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForTransferActionDeclarationInput", 1, 1));
        target.setName(source.getParamaterName());
        target.setWrapAsOptional(false);
        target.setType(getTransferDeclarationEquivalent(source.getParameterType(), ctx));
    }

    // ===================================================================
    // CreateEmptyOperationBody (@Greedy)
    // ===================================================================

    @TransformRule(
            name = CREATE_EMPTY_OPERATION_BODY,
            description = "Create empty OperationBody for TransferActionDeclaration"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = OperationBody.class)
    public TransformFunction<TransferActionDeclaration, OperationBody> createEmptyOperationBody() {
        return (source, ctx) -> {
            OperationBody target = ctx.createTarget(OperationBody.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateEmptyOperationBody");

            target.setBody("// (jsl/" + getJslId(source) + ")/CreateEmptyOperationBody");
            target.setStateful(true);
            target.setCustomImplementation(true);

            LOG.debug("Created CreateEmptyOperationBody: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // CreateFaultParameter (@Lazy @Greedy, transforms ErrorDeclaration)
    // ===================================================================

    @TransformRule(
            name = CREATE_FAULT_PARAMETER,
            description = "Create fault Parameter for ErrorDeclaration"
    )
    @Lazy
    @Greedy
    @Transform(type = ErrorDeclaration.class)
    @To(type = Parameter.class)
    public TransformFunction<ErrorDeclaration, Parameter> createFaultParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateFaultParameter");

            target.setName(source.getName());
            target.setType(ctx.equivalent(source,
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class,
                    CREATE_UNMAPPED_TRANSFER_OBJECT_TYPE));
            target.setWrapAsOptional(false);
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForFaultParameter", 1, 1));

            LOG.debug("Created CreateFaultParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Unmapped transfer object type operations
    // ===================================================================

    @TransformRule(
            name = CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE,
            description = "Create UnboundOperation for unmapped transfer object type"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isUnmappedContainerAction")
    public TransformFunction<TransferActionDeclaration, UnboundOperation> createUnboundOperationForUnmappedTransferObjectType() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnboundOperationForUnmappedTransferObjectType");

            populateUnboundAction(source, target, ctx);

            // s.eContainer.equivalent("CreateUnmappedTransferObjectType").operations.add(t)
            TransferObjectType transferObj = ctx.equivalent(source.eContainer(),
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class,
                    CREATE_UNMAPPED_TRANSFER_OBJECT_TYPE);
            addOperation(transferObj, target);

            LOG.debug("Created CreateUnboundOperationForUnmappedTransferObjectType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for unmapped transfer object type UnboundOperation"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUnmappedContainerActionWithParameter")
    public TransformFunction<TransferActionDeclaration, Parameter> createUnboundOperationForUnmappedTransferObjectTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnboundOperationForUnmappedTransferObjectTypeInputParameter");

            populateInputParameter(source, target, ctx);

            // s.equivalent("CreateUnboundOperationForUnmappedTransferObjectType").input = t
            UnboundOperation op = ctx.equivalent(source, UnboundOperation.class,
                    CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateUnboundOperationForUnmappedTransferObjectTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for unmapped transfer object type UnboundOperation"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isUnmappedContainerActionWithReturn")
    public TransformFunction<TransferActionDeclaration, Parameter> createUnboundOperationForUnmappedTransferObjectTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnboundOperationForUnmappedTransferObjectTypeOutputParameter");

            populateReturnParameter(source, target, ctx);

            // s.equivalent("CreateUnboundOperationForUnmappedTransferObjectType").output = t
            UnboundOperation op = ctx.equivalent(source, UnboundOperation.class,
                    CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateUnboundOperationForUnmappedTransferObjectTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Mapped transfer object type Unbound Operations
    // ===================================================================

    @TransformRule(
            name = CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE,
            description = "Create UnboundOperation for mapped transfer object type (static action)"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isMappedContainerStaticAction")
    public TransformFunction<TransferActionDeclaration, UnboundOperation> createUnboundOperationForMappedTransferObjectType() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnboundOperationForMappedTransferObjectType");

            populateUnboundAction(source, target, ctx);

            // s.eContainer.getTransferDeclarationEquivalent().operations.add(t)
            TransferObjectType transferObj = getTransferDeclarationEquivalent(
                    (TransferDeclaration) source.eContainer(), ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateUnboundOperationForMappedTransferObjectType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for mapped transfer object type UnboundOperation"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isMappedContainerStaticActionWithParameter")
    public TransformFunction<TransferActionDeclaration, Parameter> createUnboundOperationForMappedTransferObjectTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnboundOperationForMappedTransferObjectTypeInputParameter");

            populateInputParameter(source, target, ctx);

            // s.equivalent("CreateUnboundOperationForMappedTransferObjectType").input = t
            UnboundOperation op = ctx.equivalent(source, UnboundOperation.class,
                    CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateUnboundOperationForMappedTransferObjectTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for mapped transfer object type UnboundOperation"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isMappedContainerStaticActionWithReturn")
    public TransformFunction<TransferActionDeclaration, Parameter> createUnboundOperationForMappedTransferObjectTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnboundOperationForMappedTransferObjectTypeOutputParameter");

            populateReturnParameter(source, target, ctx);

            // s.equivalent("CreateUnboundOperationForMappedTransferObjectType").output = t
            UnboundOperation op = ctx.equivalent(source, UnboundOperation.class,
                    CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateUnboundOperationForMappedTransferObjectTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Mapped transfer object type Bound Operations (Entity-level)
    // ===================================================================

    @TransformRule(
            name = CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE,
            description = "Create BoundOperation on EntityType for mapped non-static action"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = BoundOperation.class)
    @Guard(method = "isMappedContainerNonStaticAction")
    public TransformFunction<TransferActionDeclaration, BoundOperation> createBoundOperationForEntityType() {
        return (source, ctx) -> {
            BoundOperation target = ctx.createTarget(BoundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBoundOperationForEntity");

            target.setAbstract(false);
            target.setImplementation(ctx.equivalent(source, OperationBody.class, CREATE_EMPTY_OPERATION_BODY));
            target.setInstanceRepresentation((MappedTransferObjectType) getTransferDeclarationEquivalent(
                    (TransferDeclaration) source.eContainer(), ctx));

            // t.name = s.name + s.eContainer.getFqName().fqNameToCamelCase()
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            String containerFqName = getFqName(container);
            target.setName(source.getName() + fqNameToCamelCase(containerFqName));

            // faults (with "/Entity" discriminator suffix)
            for (ErrorDeclaration error : source.getErrors()) {
                Parameter faultParam = ctx.equivalentDiscriminated(error, Parameter.class,
                        CREATE_FAULT_PARAMETER, getJslId(source) + "/Entity");
                if (faultParam != null) {
                    target.getFaults().add(faultParam);
                }
            }

            // s.eContainer.map.entity.getEntityDeclarationEquivalent().operations.add(t)
            hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration entityDecl = container.getMap().getEntity();
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(entityDecl,
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addBoundOperation(entityType, target);

            LOG.debug("Created CreateBoundOperationForEntity: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for BoundOperation on EntityType"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isMappedContainerNonStaticActionWithParameter")
    public TransformFunction<TransferActionDeclaration, Parameter> createBoundOperationForEntityTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBoundOperationForEntityTypeInputParameter");

            // Uses entity-level cardinality (not transfer-level)
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForTransferActionDeclarationEntityInput", 1, 1));
            target.setName(source.getParamaterName());
            target.setType(getTransferDeclarationEquivalent(source.getParameterType(), ctx));

            // s.equivalent("CreateBoundOperationForEntityType").input = t
            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateBoundOperationForEntityTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for BoundOperation on EntityType"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isMappedContainerNonStaticActionWithReturn")
    public TransformFunction<TransferActionDeclaration, Parameter> createBoundOperationForEntityTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBoundOperationForEntityTypeOutputParameter");

            // Uses entity-level cardinality
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForTransferActionDeclarationEntityOutput", 1, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent((TransferDeclaration) source.getReturn(), ctx));

            // s.equivalent("CreateBoundOperationForEntityType").output = t
            BoundOperation op = ctx.equivalent(source, BoundOperation.class,
                    CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateBoundOperationForEntityTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Mapped transfer object type Bound Transfer Operations
    // ===================================================================

    @TransformRule(
            name = CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE,
            description = "Create BoundTransferOperation for mapped non-static action"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = BoundTransferOperation.class)
    @Guard(method = "isMappedContainerNonStaticAction")
    public TransformFunction<TransferActionDeclaration, BoundTransferOperation> createBoundOperationForMappedTransferObjectType() {
        return (source, ctx) -> {
            BoundTransferOperation target = ctx.createTarget(BoundTransferOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBoundOperationForMappedTransferObjectType");

            populateBoundTransferAction(source, target, ctx);

            // t.binding = s.equivalent("CreateBoundOperationForEntityType")
            BoundOperation binding = ctx.equivalent(source, BoundOperation.class,
                    CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE);
            target.setBinding(binding);

            // s.eContainer.getTransferDeclarationEquivalent().operations.add(t)
            TransferObjectType transferObj = getTransferDeclarationEquivalent(
                    (TransferDeclaration) source.eContainer(), ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateBoundOperationForMappedTransferObjectType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_INPUT_PARAMETER,
            description = "Create input Parameter for BoundTransferOperation on mapped TO"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isMappedContainerNonStaticActionWithParameter")
    public TransformFunction<TransferActionDeclaration, Parameter> createBoundOperationForMappedTransferObjectTypeInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBoundOperationForMappedTransferObjectTypeInputParameter");

            populateInputParameter(source, target, ctx);

            // s.equivalent("CreateBoundOperationForMappedTransferObjectType").input = t
            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE);
            if (op != null) {
                op.setInput(target);
            }

            LOG.debug("Created CreateBoundOperationForMappedTransferObjectTypeInputParameter: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for BoundTransferOperation on mapped TO"
    )
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isMappedContainerNonStaticActionWithReturn")
    public TransformFunction<TransferActionDeclaration, Parameter> createBoundOperationForMappedTransferObjectTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBoundOperationForMappedTransferObjectTypeOutputParameter");

            populateReturnParameter(source, target, ctx);

            // s.equivalent("CreateBoundOperationForMappedTransferObjectType").output = t
            BoundTransferOperation op = ctx.equivalent(source, BoundTransferOperation.class,
                    CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateBoundOperationForMappedTransferObjectTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Action input parameter range rules (Mapped)
    // ===================================================================

    @TransformRule(
            name = CREATE_ACTION_INPUT_PARAMETER_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_ACTION_DECLARATION,
            description = "Create ReferenceExpressionType for mapped action input parameter range"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isMappedTransferActionChoice")
    public TransformFunction<ChoiceModifier, ReferenceExpressionType> createActionInputParameterRangeReferenceExpressionTypeForMappedTransferActionDeclaration() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);

            TransferActionDeclaration action = (TransferActionDeclaration) source.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(action) + ")/CreateActionInputParameterRangeReferenceExpressionTypeForMappedTransferActionDeclaration");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix") != null
                    ? ctx.getAttribute("entityNamePrefix") : "";
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix") != null
                    ? ctx.getAttribute("entityNamePostfix") : "";
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(
                    source.getExpression(), entityNamePrefix, entityNamePostfix));

            // s.equivalent("CreateActionInputParameterRangeNavigationPropertyForMappedTransferActionDeclaration").getterExpression = t
            NavigationProperty navProp = ctx.equivalent(source, NavigationProperty.class,
                    CREATE_ACTION_INPUT_PARAMETER_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_ACTION_DECLARATION);
            if (navProp != null) {
                navProp.setGetterExpression(target);
            }

            LOG.debug("Created ReferenceExpressionType for action input parameter range: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_ACTION_INPUT_PARAMETER_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_ACTION_DECLARATION,
            description = "Create NavigationProperty for mapped action input parameter range"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = NavigationProperty.class)
    @Guard(method = "isMappedTransferActionChoice")
    public TransformFunction<ChoiceModifier, NavigationProperty> createActionInputParameterRangeNavigationPropertyForMappedTransferActionDeclaration() {
        return (source, ctx) -> {
            NavigationProperty target = ctx.createTarget(NavigationProperty.class);

            TransferActionDeclaration action = (TransferActionDeclaration) source.eContainer();
            TransferDeclaration container = (TransferDeclaration) action.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(action) + ")/CreateActionInputParameterRangeNavigationPropertyForMappedTransferActionDeclaration");

            String prefix = ctx.getAttribute("defaultActionInputParameterRangeNamePrefix") != null
                    ? ctx.getAttribute("defaultActionInputParameterRangeNamePrefix") : "";
            String midfix = ctx.getAttribute("defaultActionInputParameterRangeNameMidfix") != null
                    ? ctx.getAttribute("defaultActionInputParameterRangeNameMidfix") : "";
            String postfix = ctx.getAttribute("defaultActionInputParameterRangeNamePostfix") != null
                    ? ctx.getAttribute("defaultActionInputParameterRangeNamePostfix") : "";
            target.setName(prefix + action.getName() + midfix + container.getName() + postfix);

            // t.target = s.eContainer.getParameterType().getTransferDeclarationEquivalent().entityType
            TransferObjectType paramTransferObj = getTransferDeclarationEquivalent(action.getParameterType(), ctx);
            if (paramTransferObj instanceof MappedTransferObjectType) {
                target.setTarget(((MappedTransferObjectType) paramTransferObj).getEntityType());
            }

            // t.cardinality - create inline
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(action) + ")/CreateCardinalityForGetActionInputParameterRangeEntityRelation", 0, -1));

            // s.eContainer.eContainer.map.entity.getEntityDeclarationEquivalent().navigationProperties.add(t)
            hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration entityDecl = container.getMap().getEntity();
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(entityDecl,
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addNavigationProperty(entityType, target);

            LOG.debug("Created NavigationProperty for action input parameter range: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Action input parameter range rules (Unmapped)
    // ===================================================================

    @TransformRule(
            name = CREATE_ACTION_INPUT_PARAMETER_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_ACTION_DECLARATION,
            description = "Create ReferenceExpressionType for unmapped action input parameter range"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isUnmappedTransferActionChoice")
    public TransformFunction<ChoiceModifier, ReferenceExpressionType> createActionInputParameterRangeReferenceExpressionTypeForUnmappedTransferActionDeclaration() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);

            TransferActionDeclaration action = (TransferActionDeclaration) source.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(action) + ")/CreateActionInputParameterRangeReferenceExpressionTypeForUnmappedTransferActionDeclaration");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix") != null
                    ? ctx.getAttribute("entityNamePrefix") : "";
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix") != null
                    ? ctx.getAttribute("entityNamePostfix") : "";
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(
                    source.getExpression(), entityNamePrefix, entityNamePostfix));

            // s.equivalent("CreateActionInputParameterRangeStaticNavigationForUnmappedTransferActionDeclaration").getterExpression = t
            StaticNavigation staticNav = ctx.equivalent(source, StaticNavigation.class,
                    CREATE_ACTION_INPUT_PARAMETER_RANGE_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_ACTION_DECLARATION);
            if (staticNav != null) {
                staticNav.setGetterExpression(target);
            }

            LOG.debug("Created ReferenceExpressionType for unmapped action input parameter range: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_ACTION_INPUT_PARAMETER_RANGE_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_ACTION_DECLARATION,
            description = "Create StaticNavigation for unmapped action input parameter range"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = StaticNavigation.class)
    @Guard(method = "isUnmappedTransferActionChoice")
    public TransformFunction<ChoiceModifier, StaticNavigation> createActionInputParameterRangeStaticNavigationForUnmappedTransferActionDeclaration() {
        return (source, ctx) -> {
            StaticNavigation target = ctx.createTarget(StaticNavigation.class);

            TransferActionDeclaration action = (TransferActionDeclaration) source.eContainer();
            TransferDeclaration container = (TransferDeclaration) action.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(action) + ")/CreateActionInputParameterRangeStaticNavigationForUnmappedTransferActionDeclaration");

            String prefix = ctx.getAttribute("defaultActionInputParameterRangeNamePrefix") != null
                    ? ctx.getAttribute("defaultActionInputParameterRangeNamePrefix") : "";
            String midfix = ctx.getAttribute("defaultActionInputParameterRangeNameMidfix") != null
                    ? ctx.getAttribute("defaultActionInputParameterRangeNameMidfix") : "";
            String postfix = ctx.getAttribute("defaultActionInputParameterRangeNamePostfix") != null
                    ? ctx.getAttribute("defaultActionInputParameterRangeNamePostfix") : "";
            target.setName(prefix + action.getName() + midfix + container.getName() + postfix);

            // t.target = s.eContainer.getParameterType().getTransferDeclarationEquivalent().entityType
            TransferObjectType paramTransferObj = getTransferDeclarationEquivalent(action.getParameterType(), ctx);
            if (paramTransferObj instanceof MappedTransferObjectType) {
                target.setTarget(((MappedTransferObjectType) paramTransferObj).getEntityType());
            }

            // t.cardinality - create inline
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(action) + ")/CreateCardinalityForGetActionInputParameterRangeEntityRelation", 0, -1));

            // s.eContainer.eContainer.eContainer.getModelRoot().elements.add(t)
            hu.blackbelt.judo.meta.psm.namespace.Package modelRoot = getModelRoot(container, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticNavigation for unmapped action input parameter range: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Action input parameter range TransferObjectRelation
    // ===================================================================

    @TransformRule(
            name = CREATE_ACTION_INPUT_PARAMETER_RANGE_TRANSFER_OBJECT_RELATION,
            description = "Create TransferObjectRelation for action input parameter range"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isTransferActionChoice")
    public TransformFunction<ChoiceModifier, TransferObjectRelation> createActionInputParameterRangeTransferObjectRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateActionInputParameterRangeTransferObjectRelation");

            TransferActionDeclaration action = (TransferActionDeclaration) source.eContainer();
            TransferDeclaration container = (TransferDeclaration) action.eContainer();

            // t.name = s.eContainer.getTransferActionRangeEquivalent().name
            EObject rangeEquivalent = getTransferActionRangeEquivalent(action, ctx);
            if (rangeEquivalent != null) {
                try {
                    java.lang.reflect.Method nameMethod = rangeEquivalent.getClass().getMethod("getName");
                    String rangeName = (String) nameMethod.invoke(rangeEquivalent);
                    target.setName(rangeName);
                } catch (Exception e) {
                    LOG.warn("Could not get name from range equivalent", e);
                }
            }

            // t.cardinality - create inline
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(action) + ")/CreateCardinalityForGetActionInputParameterRangeTransferObjectRelation", 0, -1));

            // t.target = s.eContainer.getParameterType().getTransferDeclarationEquivalent()
            target.setTarget(getTransferDeclarationEquivalent(action.getParameterType(), ctx));

            // t.range = s.eContainer.getTransferActionRangeEquivalent()
            // t.binding = s.eContainer.getTransferActionRangeEquivalent()
            if (rangeEquivalent != null) {
                // Use reflective EMF to set binding (may be NavigationProperty or StaticNavigation)
                target.eSet(target.eClass().getEStructuralFeature("binding"), rangeEquivalent);
                // range is also the same element
                target.eSet(target.eClass().getEStructuralFeature("range"), rangeEquivalent);
            }

            // s.eContainer.eContainer.getTransferDeclarationEquivalent().relations.add(t)
            TransferObjectType transferObj = getTransferDeclarationEquivalent(container, ctx);
            addTransferRelation(transferObj, target);

            return target;
        };
    }

    // ===================================================================
    // Lazy cardinality for action input parameter range
    // ===================================================================

    @TransformRule(
            name = CREATE_CARDINALITY_FOR_GET_ACTION_INPUT_PARAMETER_RANGE_ENTITY_RELATION,
            description = "Create Cardinality for action input parameter range entity relation"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Cardinality.class)
    public TransformFunction<TransferActionDeclaration, Cardinality> createCardinalityForGetActionInputParameterRangeEntityRelation() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetActionInputParameterRangeEntityRelation");
            target.setLower(0);
            target.setUpper(-1);
            return target;
        };
    }

    @TransformRule(
            name = CREATE_CARDINALITY_FOR_GET_ACTION_INPUT_PARAMETER_RANGE_TRANSFER_OBJECT_RELATION,
            description = "Create Cardinality for action input parameter range transfer object relation"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = Cardinality.class)
    public TransformFunction<TransferActionDeclaration, Cardinality> createCardinalityForGetActionInputParameterRangeTransferObjectRelation() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetActionInputParameterRangeTransferObjectRelation");
            target.setLower(0);
            target.setUpper(-1);
            return target;
        };
    }

    // ===================================================================
    // Guard methods
    // ===================================================================

    // --- Unmapped container guards ---

    public boolean isUnmappedContainerAction(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() == null;
    }

    public boolean isUnmappedContainerActionWithParameter(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() == null && action.getParameterType() != null;
    }

    public boolean isUnmappedContainerActionWithReturn(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() == null && action.getReturn() != null;
    }

    // --- Mapped container static action guards ---

    public boolean isMappedContainerStaticAction(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() != null && isStatic(action);
    }

    public boolean isMappedContainerStaticActionWithParameter(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() != null
                && action.getParameterType() != null && isStatic(action);
    }

    public boolean isMappedContainerStaticActionWithReturn(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() != null
                && action.getReturn() != null && isStatic(action);
    }

    // --- Mapped container non-static action guards ---

    public boolean isMappedContainerNonStaticAction(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() != null && !isStatic(action);
    }

    public boolean isMappedContainerNonStaticActionWithParameter(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() != null
                && action.getParameterType() != null && !isStatic(action);
    }

    public boolean isMappedContainerNonStaticActionWithReturn(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject;
        EObject container = action.eContainer();
        if (!(container instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) container).getMap() != null
                && action.getReturn() != null && !isStatic(action);
    }

    // --- ChoiceModifier guards ---

    public boolean isTransferActionChoice(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ChoiceModifier)) return false;
        return eObject.eContainer() instanceof TransferActionDeclaration;
    }

    public boolean isMappedTransferActionChoice(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ChoiceModifier)) return false;
        if (!(eObject.eContainer() instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject.eContainer();
        EObject actionContainer = action.eContainer();
        if (!(actionContainer instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) actionContainer).getMap() != null;
    }

    public boolean isUnmappedTransferActionChoice(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ChoiceModifier)) return false;
        if (!(eObject.eContainer() instanceof TransferActionDeclaration)) return false;
        TransferActionDeclaration action = (TransferActionDeclaration) eObject.eContainer();
        EObject actionContainer = action.eContainer();
        if (!(actionContainer instanceof TransferDeclaration)) return false;
        return ((TransferDeclaration) actionContainer).getMap() == null;
    }
}
