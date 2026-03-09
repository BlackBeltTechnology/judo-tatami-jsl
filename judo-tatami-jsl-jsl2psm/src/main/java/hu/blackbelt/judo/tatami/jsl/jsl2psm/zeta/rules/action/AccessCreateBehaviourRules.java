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
 * Access create behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/accessCreateBehaviour.etl (6 rules):
 * - CreateCreateBehaviourForAccess (@Greedy)
 * - CreateCreateOperationForAccess (@Greedy)
 * - CreateCreateOperationForAccessInputParameter (@Greedy)
 * - CreateCreateOperationForAccessOutputParameter (@Greedy)
 * - CreateCardinalityForCreateAccessInput (@Lazy @Greedy)
 * - CreateCardinalityForCreateAccessOutput (@Lazy @Greedy)
 */
@TransformationContext(
        source = ActorAccessDeclaration.class,
        target = TransferOperationBehaviour.class
)
public class AccessCreateBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(AccessCreateBehaviourRules.class);

    // ---- Guard method ----

    /**
     * Guard: generateBehaviours and s.isActorRelated() and (s.maps() or s.reads())
     *        and s.referenceType.isCreateSupported() and s.isCreateAllowed()
     */
    public boolean isAccessCreateGuard(EObject eObject,
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
     * CreateCreateBehaviourForAccess
     * Creates a TransferOperationBehaviour with CREATE_INSTANCE type.
     */
    @TransformRule(
            name = CREATE_CREATE_BEHAVIOUR_FOR_ACCESS,
            description = "Create CREATE_INSTANCE behaviour for actor access declaration"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isAccessCreateGuard")
    public TransformFunction<ActorAccessDeclaration, TransferOperationBehaviour> createCreateBehaviourForAccess() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateBehaviourForAccess");

            target.setBehaviourType(TransferOperationBehaviourType.CREATE_INSTANCE);
            // t.owner = s.equivalent("CreateTransientTransferObjectRelationForActorAccessDeclaration")
            target.setOwner(ctx.equivalent(source, TransferObjectRelation.class,
                    CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_ACTOR_ACCESS_DECLARATION));

            LOG.debug("Created CreateCreateBehaviourForAccess: {}", source.getName());
            return target;
        };
    }

    /**
     * CreateCreateOperationForAccess
     * Creates an UnboundOperation for creating access items.
     */
    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_ACCESS,
            description = "Create create unbound operation for actor access declaration"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isAccessCreateGuard")
    public TransformFunction<ActorAccessDeclaration, UnboundOperation> createCreateOperationForAccess() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForAccess");

            // t.name = s.referenceType.getCreateEvent().name + "Of" + s.name.firstToUpperCase()
            EObject createEvent = getCreateEvent(source.getReferenceType());
            String createEventName = "";
            if (createEvent instanceof hu.blackbelt.judo.meta.jsl.jsldsl.Named) {
                createEventName = ((hu.blackbelt.judo.meta.jsl.jsldsl.Named) createEvent).getName();
            }
            target.setName(createEventName + "Of" + firstToUpperCase(source.getName()));

            // Check name collision with container members
            ActorDeclaration container = (ActorDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> (m instanceof hu.blackbelt.judo.meta.jsl.jsldsl.Named) && ((hu.blackbelt.judo.meta.jsl.jsldsl.Named) m).getName().equals(target.getName()))) {
                target.setName("_" + target.getName());
            }

            // t.behaviour = s.equivalent("CreateCreateBehaviourForAccess")
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_CREATE_BEHAVIOUR_FOR_ACCESS));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            // s.eContainer.getActorDeclarationEquivalent().operations.add(t)
            addOperation(getActorDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateCreateOperationForAccess: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateCreateOperationForAccessInputParameter
     * Creates the input parameter for the create operation.
     */
    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_ACCESS_INPUT_PARAMETER,
            description = "Create input parameter for access create operation"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAccessCreateGuard")
    public TransformFunction<ActorAccessDeclaration, Parameter> createCreateOperationForAccessInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForAccessInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForCreateAccessInput", 1, 1));
            target.setName("input");
            // t.type = s.referenceType.getTransferDeclarationEquivalent()
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            // s.equivalent("CreateCreateOperationForAccess").input = t
            ctx.equivalent(source, UnboundOperation.class, CREATE_CREATE_OPERATION_FOR_ACCESS).setInput(target);

            LOG.debug("Created CreateCreateOperationForAccessInputParameter: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateCreateOperationForAccessOutputParameter
     * Creates the output parameter for the create operation.
     */
    @TransformRule(
            name = CREATE_CREATE_OPERATION_FOR_ACCESS_OUTPUT_PARAMETER,
            description = "Create output parameter for access create operation"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAccessCreateGuard")
    public TransformFunction<ActorAccessDeclaration, Parameter> createCreateOperationForAccessOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCreateOperationForAccessOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForCreateAccessOutput", 1, 1));
            target.setName("return");
            // t.type = s.referenceType.getTransferDeclarationEquivalent()
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            // s.equivalent("CreateCreateOperationForAccess").output = t
            ctx.equivalent(source, UnboundOperation.class, CREATE_CREATE_OPERATION_FOR_ACCESS).setOutput(target);

            LOG.debug("Created CreateCreateOperationForAccessOutputParameter: {}", target.getName());
            return target;
        };
    }

}
