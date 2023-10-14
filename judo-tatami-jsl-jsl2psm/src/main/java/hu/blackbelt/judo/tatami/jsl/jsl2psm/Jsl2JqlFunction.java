package hu.blackbelt.judo.tatami.jsl.jsl2psm;

/*-
 * #%L
 * Judo :: Tatami :: JSL :: Jsl2Psm
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import hu.blackbelt.judo.meta.jsl.jsldsl.Argument;
import hu.blackbelt.judo.meta.jsl.jsldsl.Expression;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionOrQueryCall;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionParameterDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.LambdaCall;
import hu.blackbelt.judo.meta.jsl.jsldsl.LambdaDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.Navigation;

import hu.blackbelt.judo.meta.jsl.runtime.TypeInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Jsl2JqlFunction {



    private static class ParameterValue {
        private String name;
        private String defaultValue;
        private Boolean mandatory = true;

        ParameterValue(String name, String defaultValue, Boolean mandatory) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.mandatory = mandatory;
        }

        private static Boolean $default$mandatory() {
            return true;
        }

        public static ParameterValueBuilder builder() {
            return new ParameterValueBuilder();
        }

        public String getName() {
            return this.name;
        }

        public String getDefaultValue() {
            return this.defaultValue;
        }

        public Boolean getMandatory() {
            return this.mandatory;
        }

        public static class ParameterValueBuilder {
            private String name;
            private String defaultValue;
            private Boolean mandatory$value;
            private boolean mandatory$set;

            ParameterValueBuilder() {
            }

            public ParameterValueBuilder name(String name) {
                this.name = name;
                return this;
            }

            public ParameterValueBuilder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }

            public ParameterValueBuilder mandatory(Boolean mandatory) {
                this.mandatory$value = mandatory;
                this.mandatory$set = true;
                return this;
            }

            public ParameterValue build() {
                Boolean mandatory$value = this.mandatory$value;
                if (!this.mandatory$set) {
                    mandatory$value = ParameterValue.$default$mandatory();
                }
                return new ParameterValue(name, defaultValue, mandatory$value);
            }

            public String toString() {
                return "Jsl2JqlFunction.ParameterValue.ParameterValueBuilder(name=" + this.name + ", defaultValue=" + this.defaultValue + ", mandatory$value=" + this.mandatory$value + ")";
            }
        }
    }

    /**
     * Keys are function names. Each "function" can have multiple possible parameter lists.
     */
    private static Map<String, Collection<Collection<ParameterValue>>> literalFunctionParameters =
            ImmutableMap.<String, Collection<Collection<ParameterValue>>>builder()
                    .put("getVariable", ImmutableList.of( ImmutableList.of(
                            ParameterValue.builder().name("category").build(),
                            ParameterValue.builder().name("key").build())))
                    .put("round", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("scale").mandatory(false).build())))
                    .put("left", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("count").build())))
                    .put("right", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("count").build())))
                    .put("substring", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("count").build(),
                            ParameterValue.builder().name("offset").build())))
                    .put("matches", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("pattern").build())))
                    .put("position", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("substring").build())))
                    .put("like", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("pattern").build())))
                    .put("ilike", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("pattern").build())))
                    .put("lpad", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("size").build(),
                            ParameterValue.builder().name("padstring").mandatory(false).build())))
                    .put("rpad", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("size").build(),
                            ParameterValue.builder().name("padstring").mandatory(false).build())))
                    .put("replace", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("oldstring").build(),
                            ParameterValue.builder().name("newstring").build())))
                    .put("fromMilliseconds", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("milliseconds").build())))
                    .put("of", ImmutableList.of(
							// date
                            ImmutableList.of(
                                    ParameterValue.builder().name("year").build(),
                                    ParameterValue.builder().name("month").build(),
                                    ParameterValue.builder().name("day").build()),
							// time
                            ImmutableList.of(
                                    ParameterValue.builder().name("hour").build(),
                                    ParameterValue.builder().name("minute").build(),
                                    ParameterValue.builder().name("second").defaultValue("0").mandatory(false).build(),
                                    ParameterValue.builder().name("millisecond").defaultValue("0").mandatory(false).build()),
							// timestamp
                            ImmutableList.of(
                                    ParameterValue.builder().name("date").build(),
                                    ParameterValue.builder().name("time").mandatory(false).build())))
                    .put("plus", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("years").mandatory(false).build(),
                            ParameterValue.builder().name("months").mandatory(false).build(),
                            ParameterValue.builder().name("days").mandatory(false).build(),
                            ParameterValue.builder().name("hours").mandatory(false).build(),
                            ParameterValue.builder().name("minutes").mandatory(false).build(),
                            ParameterValue.builder().name("seconds").mandatory(false).build(),
                            ParameterValue.builder().name("milliseconds").mandatory(false).build())))
                    .put("typeOf", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("entityType").build())))
                    .put("kindOf", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("entityType").build())))
                    .put("asCollection", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("entityType").build())))
                    .put("container", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("entityType").build())))
                    .put("asType", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("entityType").build())))
                    .put("memberOf", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("instances").build())))
                    .put("contains", ImmutableList.of(ImmutableList.of(
                            ParameterValue.builder().name("instance").build())))
                    .build();

    public static String getEffectiveFunctionName(FunctionDeclaration function) {
        if (function.getName().equalsIgnoreCase("right"))  {
            return "last";
        } else if (function.getName().equalsIgnoreCase("left"))  {
            return "first";
        } else if (function.getName().equalsIgnoreCase("lower"))  {
            return "lowerCase";
        } else  if (function.getName().equalsIgnoreCase("upper"))  {
            return "upperCase";
        } else  if (function.getName().equalsIgnoreCase("size"))  {
            TypeInfo baseType = TypeInfo.getTargetType(function.getBaseType());
            if (baseType.isPrimitive()) {
                if (baseType.getPrimitive() == TypeInfo.PrimitiveType.STRING) {
                    return "length";
                }
            } else if (baseType.isCollection()) {
                return "count";
            }
            return "count";
        } else {
            return function.getName();
        }
    }

    public static String getEffectiveLambdaName(LambdaDeclaration lambda) {
        return lambda.getName();
    }

    /**
     * Keys are function names. Each "function" can have multiple possible parameter lists.
     */

    public static String getFunctionAsJql(FunctionOrQueryCall it, Function<Expression, String> expressionExtractor, String functionName) {
//                System.out.println(getStack(it));
//                System.out.println(TypeInfo.getFunctionCallReferences());
        FunctionDeclaration functionDeclaration = (FunctionDeclaration)it.getDeclaration();

        if (literalFunctionParameters.containsKey(functionName)) {
            List<String> givenParameterNames = it.getArguments().stream().map(p -> ((FunctionParameterDeclaration)p.getDeclaration()).getName()).collect(Collectors.toList());
            List<Collection<ParameterValue>> alignedParameterLists =
                    literalFunctionParameters.get(functionName).stream()
                            .filter(parameterValues -> parameterValues.stream().map(ParameterValue::getName).collect(Collectors.toList()).containsAll(givenParameterNames))
                            .collect(Collectors.toList());
            if (alignedParameterLists.size() > 1) {
                throw new IllegalStateException(String.format("Cannot determine which definition of function '%s' to use with [%s] given parameters",
                        functionDeclaration.getName(), String.join(", ", givenParameterNames)));
            } else if (alignedParameterLists.size() == 1) {
                Map<String, Argument> givenParameters = it.getArguments().stream().collect(Collectors.toMap(p -> ((FunctionParameterDeclaration)p.getDeclaration()).getName(), p -> p));
                Collection<ParameterValue> definedParameters = alignedParameterLists.get(0);
                List<String> jqlParameters = new ArrayList<>();
                for (ParameterValue definedParameter : definedParameters) {
                    if (definedParameter.getMandatory() && !givenParameterNames.contains(definedParameter.getName())) {
                        throw new IllegalArgumentException(String.format("Parameter '%s' is required for '%s' function", definedParameter.getName(), functionName));
                    } else if (!definedParameter.getMandatory() && !givenParameterNames.contains(definedParameter.getName())) {
                        String defaultValue = definedParameter.getDefaultValue();
                        if (defaultValue != null) {
                            jqlParameters.add(defaultValue);
                        }
                    } else if (givenParameterNames.contains(definedParameter.getName())) {
                        jqlParameters.add(expressionExtractor.apply(givenParameters.get(definedParameter.getName()).getExpression()));
                    }
                }
                return getEffectiveFunctionName((FunctionDeclaration)it.getDeclaration()) + "(" + String.join(", ", jqlParameters) + ")";
            }
        }

        return getEffectiveFunctionName((FunctionDeclaration)it.getDeclaration()) + "()";
    }

    public static String getTimestampPlusFunctionAsJql(FunctionOrQueryCall it, Function<Expression, String> expressionExtractor, String functionName) {
        Collection<Collection<ParameterValue>> timestampPlusParameterLists = literalFunctionParameters.get(functionName);
        if (timestampPlusParameterLists.size() != 1) {
            throw new IllegalStateException("Unsupported number of timestamp plus definitions: " + timestampPlusParameterLists.size());
        }
        Set<String> definedParameters = timestampPlusParameterLists.stream().findFirst().orElseThrow().stream().map(ParameterValue::getName).collect(Collectors.toSet());
        List<String> jqlFunctionCall = new ArrayList<>();
        for (Argument argument : it.getArguments()) {
            String argumentName = ((FunctionParameterDeclaration)argument.getDeclaration()).getName();
            if (!definedParameters.contains(argumentName)){
                throw new IllegalArgumentException("Invalid parameter name: " + argumentName);
            }
            jqlFunctionCall.add(String.format("%s(%s)", getJqlTimestampArithmeticFunctionNameOf(argumentName), expressionExtractor.apply(argument.getExpression())));
        }
        return String.join("!", jqlFunctionCall);
    }

    private static String getJqlTimestampArithmeticFunctionNameOf(String parameterName) {
        switch (parameterName){
            case "years": return "plusYears";
            case "months": return "plusMonths";
            case "days": return "plusDays";
            case "hours": return "plusHours";
            case "minutes": return "plusMinutes";
            case "seconds": return "plusSeconds";
            case "milliseconds": return "plusMilliseconds";
            default: throw new IllegalArgumentException("Unsupported timestamp arithmetic function (from parameter name): " + parameterName);
        }
    }
}
