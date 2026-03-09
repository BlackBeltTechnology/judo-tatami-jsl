package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorAccessDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
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
 * Access list behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/accessListBehaviour.etl (6 rules):
 * - CreateListBehaviourForAccess (@Greedy)
 * - CreateListOperationForAccess (@Greedy)
 * - CreateListOperationForAccessInputParameter (@Greedy)
 * - CreateListOperationForAccessOutputParameter (@Greedy)
 * - CreateCardinalityForListAccessInput (@Lazy @Greedy)
 * - CreateCardinalityForListAccessOutput (@Lazy @Greedy)
 */
@TransformationContext(
        source = ActorAccessDeclaration.class,
        target = TransferOperationBehaviour.class
)
public class AccessListBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(AccessListBehaviourRules.class);

    // ---- Guard method ----

    /**
     * Guard: generateBehaviours and s.isActorRelated()
     * Since source type is ActorAccessDeclaration, isActorRelated() is always true.
     */
    public boolean isAccessListGuard(EObject eObject,
                                      hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ActorAccessDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) {
            return false;
        }
        // ActorAccessDeclaration is always actor-related (container is ActorDeclaration)
        return true;
    }

    // ---- Rules ----

    /**
     * CreateListBehaviourForAccess
     * Creates a TransferOperationBehaviour with LIST type.
     */
    @TransformRule(
            name = CREATE_LIST_BEHAVIOUR_FOR_ACCESS,
            description = "Create LIST behaviour for actor access declaration"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isAccessListGuard")
    public TransformFunction<ActorAccessDeclaration, TransferOperationBehaviour> createListBehaviourForAccess() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListBehaviourForAccess");

            target.setBehaviourType(TransferOperationBehaviourType.LIST);
            // t.owner = s.equivalent("CreateTransientTransferObjectRelationForActorAccessDeclaration")
            target.setOwner(ctx.equivalent(source, TransferObjectRelation.class,
                    CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_ACTOR_ACCESS_DECLARATION));

            LOG.debug("Created CreateListBehaviourForAccess: {}", source.getName());
            return target;
        };
    }

    /**
     * CreateListOperationForAccess
     * Creates an UnboundOperation for listing access items.
     */
    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_ACCESS,
            description = "Create list unbound operation for actor access declaration"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isAccessListGuard")
    public TransformFunction<ActorAccessDeclaration, UnboundOperation> createListOperationForAccess() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForAccess");

            target.setName("listOf" + firstToUpperCase(source.getName()));

            // Check name collision with container members
            ActorDeclaration container = (ActorDeclaration) source.eContainer();
            if (container.getMembers().stream().anyMatch(m -> (m instanceof hu.blackbelt.judo.meta.jsl.jsldsl.Named) && ((hu.blackbelt.judo.meta.jsl.jsldsl.Named) m).getName().equals(target.getName()))) {
                target.setName("_" + target.getName());
            }

            // t.behaviour = s.equivalent("CreateListBehaviourForAccess")
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_LIST_BEHAVIOUR_FOR_ACCESS));

            target.setUpdateOnResult(isUpdateSupported(source.getReferenceType()));
            target.setDeleteOnResult(isDeleteSupported(source.getReferenceType()));

            // s.eContainer.getActorDeclarationEquivalent().operations.add(t)
            addOperation(getActorDeclarationEquivalent(container, ctx), target);

            LOG.debug("Created CreateListOperationForAccess: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateListOperationForAccessInputParameter
     * Creates the input parameter (QueryCustomizerType) for the list operation.
     */
    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_ACCESS_INPUT_PARAMETER,
            description = "Create input parameter for access list operation"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAccessListGuard")
    public TransformFunction<ActorAccessDeclaration, Parameter> createListOperationForAccessInputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForAccessInputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForListAccessInput", 0, 1));
            target.setName("input");
            // t.type = s.referenceType.equivalent("CreateQueryCustomizerType")
            target.setType(ctx.equivalent(source.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class,
                    CREATE_QUERY_CUSTOMIZER_TYPE));
            target.setWrapAsOptional(false);

            // s.equivalent("CreateListOperationForAccess").input = t
            ctx.equivalent(source, UnboundOperation.class, CREATE_LIST_OPERATION_FOR_ACCESS).setInput(target);

            LOG.debug("Created CreateListOperationForAccessInputParameter: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateListOperationForAccessOutputParameter
     * Creates the output parameter for the list operation.
     */
    @TransformRule(
            name = CREATE_LIST_OPERATION_FOR_ACCESS_OUTPUT_PARAMETER,
            description = "Create output parameter for access list operation"
    )
    @Greedy
    @Transform(type = ActorAccessDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isAccessListGuard")
    public TransformFunction<ActorAccessDeclaration, Parameter> createListOperationForAccessOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateListOperationForAccessOutputParameter");

            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateCardinalityForListAccessOutput", isRequired(source) && !isMany(source) ? 1 : 0, isMany(source) ? -1 : 1));
            target.setName("return");
            // t.type = s.referenceType.getTransferDeclarationEquivalent()
            target.setType(getTransferDeclarationEquivalent(source.getReferenceType(), ctx));
            target.setWrapAsOptional(false);

            // s.equivalent("CreateListOperationForAccess").output = t
            ctx.equivalent(source, UnboundOperation.class, CREATE_LIST_OPERATION_FOR_ACCESS).setOutput(target);

            LOG.debug("Created CreateListOperationForAccessOutputParameter: {}", target.getName());
            return target;
        };
    }

}
