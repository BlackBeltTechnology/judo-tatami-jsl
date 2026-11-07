package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.type;

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

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.UIFrontendDeclaration;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.data.EnumerationMember;
import hu.blackbelt.judo.meta.ui.data.EnumerationType;
import hu.blackbelt.judo.zeta.annotation.Greedy;
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
 * Ported from dataTypeOperation.etl:
 * Creates operator EnumerationTypes (BooleanOperation, NumericOperation,
 * EnumerationOperation, StringOperation) and their member values.
 * All transform ActorDeclaration with guard: s == actorDeclaration.
 */
@TransformationContext(
        source = ActorDeclaration.class,
        target = EObject.class
)
public class DataTypeOperationRules {

    private static final Logger LOG = LoggerFactory.getLogger(DataTypeOperationRules.class);

    private TransformFunction<ActorDeclaration, EnumerationType> createOperationType(
            String ruleName, String operationName) {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            if (source != actorDeclaration) return null;

            EnumerationType target = ctx.createTarget(EnumerationType.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/" + ruleName);

            target.setName(operationName);

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created operation type: {}", operationName);
            return target;
        };
    }

    private TransformFunction<ActorDeclaration, EnumerationMember> createOperationMember(
            String ruleName, String parentRuleName, String memberName, int ordinal) {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            if (source != actorDeclaration) return null;

            EnumerationMember target = ctx.createTarget(EnumerationMember.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/" + ruleName);

            target.setOrdinal(ordinal);
            target.setName(memberName);

            EnumerationType parent = ctx.equivalent(source, EnumerationType.class, parentRuleName);
            parent.getMembers().add(target);

            return target;
        };
    }

    // =========================================================================
    // Boolean operations
    // =========================================================================

    @TransformRule(name = BOOLEAN_OPERATION, description = "Create BooleanOperation EnumerationType")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationType.class)
    public TransformFunction<ActorDeclaration, EnumerationType> booleanOperation() {
        return createOperationType(BOOLEAN_OPERATION, "BooleanOperation");
    }

    @TransformRule(name = BOOLEAN_OPERATION_EQUALS, description = "Create BooleanOperation equals member")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> booleanOperationEquals() {
        return createOperationMember(BOOLEAN_OPERATION_EQUALS, BOOLEAN_OPERATION, "equals", 0);
    }

    // =========================================================================
    // Numeric operations
    // =========================================================================

    @TransformRule(name = NUMERIC_OPERATION, description = "Create NumericOperation EnumerationType")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationType.class)
    public TransformFunction<ActorDeclaration, EnumerationType> numericOperation() {
        return createOperationType(NUMERIC_OPERATION, "NumericOperation");
    }

    @TransformRule(name = NUMERIC_OPERATION_LESS_THAN, description = "Create NumericOperation lessThan")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> numericOperationLessThan() {
        return createOperationMember(NUMERIC_OPERATION_LESS_THAN, NUMERIC_OPERATION, "lessThan", 0);
    }

    @TransformRule(name = NUMERIC_OPERATION_GREATER_THAN, description = "Create NumericOperation greaterThan")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> numericOperationGreaterThan() {
        return createOperationMember(NUMERIC_OPERATION_GREATER_THAN, NUMERIC_OPERATION, "greaterThan", 1);
    }

    @TransformRule(name = NUMERIC_OPERATION_LESS_OR_EQUAL, description = "Create NumericOperation lessOrEqual")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> numericOperationLessOrEqual() {
        return createOperationMember(NUMERIC_OPERATION_LESS_OR_EQUAL, NUMERIC_OPERATION, "lessOrEqual", 2);
    }

    @TransformRule(name = NUMERIC_OPERATION_GREATER_OR_EQUAL, description = "Create NumericOperation greaterOrEqual")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> numericOperationGreaterOrEqual() {
        return createOperationMember(NUMERIC_OPERATION_GREATER_OR_EQUAL, NUMERIC_OPERATION, "greaterOrEqual", 3);
    }

    @TransformRule(name = NUMERIC_OPERATION_EQUAL, description = "Create NumericOperation equal")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> numericOperationEqual() {
        return createOperationMember(NUMERIC_OPERATION_EQUAL, NUMERIC_OPERATION, "equal", 4);
    }

    @TransformRule(name = NUMERIC_OPERATION_NOT_EQUAL, description = "Create NumericOperation notEqual")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> numericOperationNotEqual() {
        return createOperationMember(NUMERIC_OPERATION_NOT_EQUAL, NUMERIC_OPERATION, "notEqual", 5);
    }

    // =========================================================================
    // Enumeration operations
    // =========================================================================

    @TransformRule(name = ENUMERATION_OPERATION, description = "Create EnumerationOperation EnumerationType")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationType.class)
    public TransformFunction<ActorDeclaration, EnumerationType> enumerationOperation() {
        return createOperationType(ENUMERATION_OPERATION, "EnumerationOperation");
    }

    @TransformRule(name = ENUMERATION_OPERATION_EQUALS, description = "Create EnumerationOperation equals")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> enumerationOperationEquals() {
        return createOperationMember(ENUMERATION_OPERATION_EQUALS, ENUMERATION_OPERATION, "equals", 0);
    }

    @TransformRule(name = ENUMERATION_OPERATION_NOT_EQUALS, description = "Create EnumerationOperation notEquals")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> enumerationOperationNotEquals() {
        return createOperationMember(ENUMERATION_OPERATION_NOT_EQUALS, ENUMERATION_OPERATION, "notEquals", 1);
    }

    // =========================================================================
    // String operations
    // =========================================================================

    @TransformRule(name = STRING_OPERATION, description = "Create StringOperation EnumerationType")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationType.class)
    public TransformFunction<ActorDeclaration, EnumerationType> stringOperation() {
        return createOperationType(STRING_OPERATION, "StringOperation");
    }

    @TransformRule(name = STRING_OPERATION_LESS_THAN, description = "Create StringOperation lessThan")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationLessThan() {
        return createOperationMember(STRING_OPERATION_LESS_THAN, STRING_OPERATION, "lessThan", 0);
    }

    @TransformRule(name = STRING_OPERATION_GREATER_THAN, description = "Create StringOperation greaterThan")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationGreaterThan() {
        return createOperationMember(STRING_OPERATION_GREATER_THAN, STRING_OPERATION, "greaterThan", 1);
    }

    @TransformRule(name = STRING_OPERATION_LESS_OR_EQUAL, description = "Create StringOperation lessOrEqual")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationLessOrEqual() {
        return createOperationMember(STRING_OPERATION_LESS_OR_EQUAL, STRING_OPERATION, "lessOrEqual", 2);
    }

    @TransformRule(name = STRING_OPERATION_GREATER_OR_EQUAL, description = "Create StringOperation greaterOrEqual")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationGreaterOrEqual() {
        return createOperationMember(STRING_OPERATION_GREATER_OR_EQUAL, STRING_OPERATION, "greaterOrEqual", 3);
    }

    @TransformRule(name = STRING_OPERATION_EQUAL, description = "Create StringOperation equal")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationEqual() {
        return createOperationMember(STRING_OPERATION_EQUAL, STRING_OPERATION, "equal", 4);
    }

    @TransformRule(name = STRING_OPERATION_NOT_EQUAL, description = "Create StringOperation notEqual")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationNotEqual() {
        return createOperationMember(STRING_OPERATION_NOT_EQUAL, STRING_OPERATION, "notEqual", 5);
    }

    @TransformRule(name = STRING_OPERATION_MATCHES, description = "Create StringOperation matches")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationMatches() {
        return createOperationMember(STRING_OPERATION_MATCHES, STRING_OPERATION, "matches", 6);
    }

    @TransformRule(name = STRING_OPERATION_LIKE, description = "Create StringOperation like")
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<ActorDeclaration, EnumerationMember> stringOperationLike() {
        return createOperationMember(STRING_OPERATION_LIKE, STRING_OPERATION, "like", 7);
    }
}
