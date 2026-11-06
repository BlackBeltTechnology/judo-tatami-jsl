package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorAccessDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferMemberDeclaration;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
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
 * Access validate create behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/accessValidateCreateBehaviour.etl (6 rules):
 * - CreateValidateCreateBehaviourForAccess (@Greedy)
 * - CreateValidateCreateOperationForAccess (@Greedy)
 * - CreateValidateCreateOperationForAccessInputParameter (@Greedy)
 * - CreateValidateCreateOperationForAccessOutputParameter (@Greedy)
 * - CreateCardinalityForValidateCreateAccessInput (@Lazy @Greedy)
 * - CreateCardinalityForValidateCreateAccessOutput (@Lazy @Greedy)
 */
@TransformationContext(
        source = ActorAccessDeclaration.class,
        target = TransferOperationBehaviour.class
)
public class AccessValidateCreateBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(AccessValidateCreateBehaviourRules.class);

    // ---- Guard method ----

    /**
     * Guard: generateBehaviours and s.isActorRelated() and (s.maps() or s.reads())
     *        and s.referenceType.isCreateSupported() and s.isCreateAllowed()
     */
    public boolean isAccessValidateCreateGuard(EObject eObject,
                                                hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ActorAccessDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) {
            return false;
        }
        ActorAccessDeclaration source = (ActorAccessDeclaration) eObject;
        // (s.maps() or s.reads())
        if (!isMaps(source) && !isReads(source)) {
            return false;
        }
        // s.referenceType.isCreateSupported()
        if (!isCreateSupported(source.getReferenceType())) {
            return false;
        }
        // s.isCreateAllowed()
        if (!isCreateAllowed(source)) {
            return false;
        }
        return true;
    }

    // ---- Rules ----

    /**
     * CreateValidateCreateBehaviourForAccess
     * Creates a TransferOperationBehaviour with VALIDATE_CREATE type.
     */
    @TransformRule(
            name = CREATE_VALIDATE_CREATE_BEHAVIOUR_FOR_ACCESS,
            description = "Create VALIDATE_CREATE behaviour for actor access declaration"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isAccessValidateCreateGuard")
    public TransformFunction<ActorAccessDeclaration, TransferOperationBehaviour> createValidateCreateBehaviourForAccess() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateBehaviourForAccess");

            target.setBehaviourType(TransferOperationBehaviourType.VALIDATE_CREATE);
            // t.owner = s.equivalent("CreateTransientTransferObjectRelationForActorAccessDeclaration")
            target.setOwner(ctx.equivalent(source, TransferObjectRelation.class,
                    CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_ACTOR_ACCESS_DECLARATION));

            LOG.debug("Created CreateValidateCreateBehaviourForAccess: {}", source.getName());
            return target;
        };
    }

    /**
     * CreateValidateCreateOperationForAccess
     * Creates an UnboundOperation for validating create on access items.
     */
    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS,
            description = "Create validate create unbound operation for actor access declaration"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isAccessValidateCreateGuard")
    public TransformFunction<ActorAccessDeclaration, UnboundOperation> createValidateCreateOperationForAccess() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForAccess");

            // t.name = "validate" + s.referenceType.getCreateEvent().name.firstToUpperCase() + "Of" + s.name.firstToUpperCase()
            EObject createEvent = getCreateEvent(source.getReferenceType());
            String createEventName = "";
            if (createEvent instanceof hu.blackbelt.judo.meta.jsl.jsldsl.Named) {
                createEventName = ((hu.blackbelt.judo.meta.jsl.jsldsl.Named) createEvent).getName();
            }
            target.setName("validate" + firstToUpperCase(createEventName) + "Of" + firstToUpperCase(source.getName()));

            // Check name collision with container members
            ActorDeclaration container = (ActorDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> (m instanceof hu.blackbelt.judo.meta.jsl.jsldsl.Named) && ((hu.blackbelt.judo.meta.jsl.jsldsl.Named) m).getName().equals(target.getName()))) {
                target.setName("_" + target.getName());
            }

            // t.behaviour = s.equivalent("CreateValidateCreateBehaviourForAccess")
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_VALIDATE_CREATE_BEHAVIOUR_FOR_ACCESS));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            // s.eContainer.getActorDeclarationEquivalent().operations.add(t)
            addOperation(getActorDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateValidateCreateOperationForAccess: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateValidateCreateOperationForAccessInputParameter
     * Creates the input parameter for the validate create operation.
     */
    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS_INPUT_PARAMETER,
            description = "Create input parameter for access validate create operation"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAccessValidateCreateGuard")
    public TransformFunction<ActorAccessDeclaration, Parameter> createValidateCreateOperationForAccessInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForAccessInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateCreateAccessInput", 1, 1));
            target.setName("input");
            // t.type = s.referenceType.getTransferDeclarationEquivalent()
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            // s.equivalent("CreateValidateCreateOperationForAccess").input = t
            ctx.equivalent(source, UnboundOperation.class, CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS).setInput(target);

            LOG.debug("Created CreateValidateCreateOperationForAccessInputParameter: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateValidateCreateOperationForAccessOutputParameter
     * Creates the output parameter for the validate create operation.
     */
    @TransformRule(
            name = CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS_OUTPUT_PARAMETER,
            description = "Create output parameter for access validate create operation"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAccessValidateCreateGuard")
    public TransformFunction<ActorAccessDeclaration, Parameter> createValidateCreateOperationForAccessOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateValidateCreateOperationForAccessOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForValidateCreateAccessOutput", 1, 1));
            target.setName("return");
            // t.type = s.referenceType.getTransferDeclarationEquivalent()
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            // s.equivalent("CreateValidateCreateOperationForAccess").output = t
            ctx.equivalent(source, UnboundOperation.class, CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS).setOutput(target);

            LOG.debug("Created CreateValidateCreateOperationForAccessOutputParameter: {}", target.getName());
            return target;
        };
    }

}
