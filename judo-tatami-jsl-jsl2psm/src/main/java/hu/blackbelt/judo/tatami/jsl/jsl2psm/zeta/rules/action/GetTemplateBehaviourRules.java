package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * GetTemplate behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/getTemplateBehaviour.etl (4 rules):
 * - CreateGetTemplateBehaviourForTransferType (TransferOperationBehaviour)
 * - CreateGetTemplateOperationForTransferType (UnboundOperation)
 * - CreateGetTemplateOperationForTransferTypeOutputParameter (Parameter)
 * - CreateCardinalityForGetTemplateTransferTypeOutput (Cardinality, @Lazy)
 *
 * Guard: generateBehaviours and s.isGetTemplateSupported()
 */
@TransformationContext(
        source = TransferDeclaration.class,
        target = UnboundOperation.class
)
public class GetTemplateBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(GetTemplateBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isGetTemplateSupportedGuard(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) {
            return false;
        }
        TransferDeclaration source = (TransferDeclaration) eObject;
        return isGetTemplateSupported(source);
    }

    // ===================================================================
    // TransferOperationBehaviour
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_TEMPLATE_BEHAVIOUR_FOR_TRANSFER_TYPE,
            description = "Create TransferOperationBehaviour for getTemplate"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isGetTemplateSupportedGuard")
    public TransformFunction<TransferDeclaration, TransferOperationBehaviour> createGetTemplateBehaviourForTransferType() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetTemplateBehaviourForTransferType");

            target.setBehaviourType(TransferOperationBehaviourType.GET_TEMPLATE);
            target.setOwner(getTransferDeclarationEquivalent(source, ctx));

            LOG.debug("Created CreateGetTemplateBehaviourForTransferType: {}", source.getName());
            return target;
        };
    }

    // ===================================================================
    // UnboundOperation
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_TEMPLATE_OPERATION_FOR_TRANSFER_TYPE,
            description = "Create UnboundOperation for getTemplate on transfer type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isGetTemplateSupportedGuard")
    public TransformFunction<TransferDeclaration, UnboundOperation> createGetTemplateOperationForTransferType() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetTemplateOperationForTransferType");

            target.setName("default");
            // If a member with the same name exists, prefix with underscore
            if (hasMemberWithName(source, "default")) {
                target.setName("_default");
            }

            target.setImmutable(true);
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_GET_TEMPLATE_BEHAVIOUR_FOR_TRANSFER_TYPE));

            TransferObjectType transferObj = getTransferDeclarationEquivalent(source, ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateGetTemplateOperationForTransferType: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Output Parameter
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_TEMPLATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER,
            description = "Create output Parameter for getTemplate operation"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isGetTemplateSupportedGuard")
    public TransformFunction<TransferDeclaration, Parameter> createGetTemplateOperationForTransferTypeOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetTemplateOperationForTransferTypeOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForGetTemplateTransferTypeOutput", 0, 1));
            target.setName("return");
            target.setType(getTransferDeclarationEquivalent(source, ctx));
            target.setWrapAsOptional(false);

            UnboundOperation op = ctx.equivalent(source, UnboundOperation.class,
                    CREATE_GET_TEMPLATE_OPERATION_FOR_TRANSFER_TYPE);
            if (op != null) {
                op.setOutput(target);
            }

            LOG.debug("Created CreateGetTemplateOperationForTransferTypeOutputParameter: {}", target.getName());
            return target;
        };
    }

}
