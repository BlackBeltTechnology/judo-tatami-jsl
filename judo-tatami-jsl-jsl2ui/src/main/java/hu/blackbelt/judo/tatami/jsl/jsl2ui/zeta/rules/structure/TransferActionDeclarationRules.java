package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.structure;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.jsl.jsldsl.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.OperationParameterType;
import hu.blackbelt.judo.meta.ui.data.OperationTargetBehaviourType;
import hu.blackbelt.judo.meta.ui.data.OperationType;
import hu.blackbelt.judo.meta.ui.data.OperationTypeEnum;
import hu.blackbelt.judo.zeta.annotation.Greedy;
import hu.blackbelt.judo.zeta.annotation.Lazy;
import hu.blackbelt.judo.zeta.annotation.To;
import hu.blackbelt.judo.zeta.annotation.Transform;
import hu.blackbelt.judo.zeta.annotation.TransformRule;
import hu.blackbelt.judo.zeta.annotation.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from transferActionDeclaration.etl:
 * - OperationType: creates OperationType (cached/greedy)
 * - OperationOutputParameterType: @lazy
 * - OperationInputParameterType: @lazy
 * - OperationFaultParameterType: @lazy (for ErrorDeclaration)
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class TransferActionDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferActionDeclarationRules.class);

    /**
     * Common logic from AbstractOperationType.
     */
    private OperationType createBaseOperation(TransferActionDeclaration source,
                                               hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
        if (!(source.eContainer() instanceof TransferDeclaration)) return null;
        if (!getExposedTransferObjects(actorDeclaration).contains((TransferDeclaration) source.eContainer())) return null;

        OperationType target = ctx.createTarget(OperationType.class);
        target.setName(source.getName());

        TransferDeclaration container = (TransferDeclaration) source.eContainer();
        if (container.getMap() != null && isStatic(source)) {
            target.setOperationType(OperationTypeEnum.STATIC);
        } else if (container.getMap() != null && !isStatic(source)) {
            target.setOperationType(OperationTypeEnum.MAPPED);
        }

        if (source.getParameterType() != null) {
            OperationParameterType input = ctx.equivalent(source,
                    OperationParameterType.class, OPERATION_INPUT_PARAMETER_TYPE);
            target.setInput(input);
        }

        if (hasOutput(source)) {
            OperationParameterType output = ctx.equivalent(source,
                    OperationParameterType.class, OPERATION_OUTPUT_PARAMETER_TYPE);
            target.setOutput(output);
        }

        for (ErrorDeclaration error : source.getErrors()) {
            boolean alreadyAdded = target.getFaults().stream()
                    .anyMatch(f -> f.getName().equals(error.getName()));
            if (!alreadyAdded) {
                OperationParameterType fault = ctx.equivalent(error,
                        OperationParameterType.class, OPERATION_FAULT_PARAMETER_TYPE);
                if (fault != null) {
                    target.getFaults().add(fault);
                }
            }
        }

        return target;
    }

    @TransformRule(name = OPERATION_TYPE, description = "Create OperationType for TransferActionDeclaration")
    @Greedy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = OperationType.class)
    public TransformFunction<TransferActionDeclaration, OperationType> operationType() {
        return (source, ctx) -> {
            OperationType target = createBaseOperation(source, ctx);
            if (target == null) return null;

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationType");

            // Add to container ClassType
            ClassType containerCt = ctx.equivalent((TransferDeclaration) source.eContainer(),
                    ClassType.class, CLASS_TYPE);
            if (containerCt != null) {
                containerCt.getOperations().add(target);
            }

            LOG.debug("Created OperationType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_OUTPUT_PARAMETER_TYPE, description = "Create output OperationParameterType")
    @Lazy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = OperationParameterType.class)
    public TransformFunction<TransferActionDeclaration, OperationParameterType> operationOutputParameterType() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            OperationParameterType target = ctx.createTarget(OperationParameterType.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationOutputParameterType");

            target.setName("output");
            target.setIsCollection(false);
            target.setIsOptional(true);

            // Get the return type's ClassType
            TransferDeclaration returnType = getReturnType(source);
            if (returnType != null) {
                ClassType targetCt = ctx.equivalent(returnType, ClassType.class, CLASS_TYPE);
                target.setTarget(targetCt);

                if (isRefreshSupported(returnType)) {
                    target.getBehaviours().add(OperationTargetBehaviourType.REFRESH);
                }
            }
            if (isActionUpdateAllowed(source)) {
                target.getBehaviours().add(OperationTargetBehaviourType.UPDATE);
            }
            if (isActionDeleteAllowed(source)) {
                target.getBehaviours().add(OperationTargetBehaviourType.DELETE);
            }

            ctx.addToResource(target);

            LOG.debug("Created OperationOutputParameterType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_PARAMETER_TYPE, description = "Create input OperationParameterType")
    @Lazy
    @Transform(type = TransferActionDeclaration.class)
    @To(type = OperationParameterType.class)
    public TransformFunction<TransferActionDeclaration, OperationParameterType> operationInputParameterType() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            OperationParameterType target = ctx.createTarget(OperationParameterType.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputParameterType");

            target.setName(source.getParamaterName());
            target.setIsCollection(false);
            target.setIsOptional(false);

            TransferDeclaration paramType = source.getParameterType();
            if (paramType != null) {
                ClassType targetCt = ctx.equivalent(paramType, ClassType.class, CLASS_TYPE);
                target.setTarget(targetCt);

                // Only set orderable/filterable if no form modifier
                if (paramType instanceof Modifiable) {
                    Modifier formMod = getModifierByType((Modifiable) paramType, "form");
                    if (formMod == null) {
                        target.setIsOrderable(hasSortableField(paramType));
                        target.setIsFilterable(hasFilterableField(paramType));
                    }
                } else {
                    target.setIsOrderable(hasSortableField(paramType));
                    target.setIsFilterable(hasFilterableField(paramType));
                }
            }

            ctx.addToResource(target);

            LOG.debug("Created OperationInputParameterType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_FAULT_PARAMETER_TYPE, description = "Create fault OperationParameterType")
    @Lazy
    @Transform(type = ErrorDeclaration.class)
    @To(type = OperationParameterType.class)
    public TransformFunction<ErrorDeclaration, OperationParameterType> operationFaultParameterType() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            OperationParameterType target = ctx.createTarget(OperationParameterType.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationFaultParameterType");

            target.setName(source.getName());

            ClassType targetCt = ctx.equivalent(source, ClassType.class, CLASS_TYPE);
            target.setTarget(targetCt);

            target.setIsCollection(false);
            target.setIsOptional(false);

            ctx.addToResource(target);

            LOG.debug("Created OperationFaultParameterType: {}", target.getName());
            return target;
        };
    }

    /**
     * Gets the return type as TransferDeclaration (handling the UnionOrTransferDeclaration indirection).
     */
    private static TransferDeclaration getReturnType(TransferActionDeclaration action) {
        if (action.getReturn() == null) return null;
        if (action.getReturn() instanceof TransferDeclaration) {
            return (TransferDeclaration) action.getReturn();
        }
        return null;
    }
}
