package hu.blackbelt.judo.tatami.jsl.jsl2psm;

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
import hu.blackbelt.judo.meta.jsl.runtime.TypeInfo;
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.ecl.parse.Ecl_EolParserRules.throwStatement_return;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SuppressWarnings("all")
public class JslExpressionToJqlExpression {

    static JslDslModelExtension modelExtension = new JslDslModelExtension();

    private Deque<Map<String, String>> queryStackParameterValues = new ArrayDeque();
    private Deque<EObject> queryCallStack = new ArrayDeque();
    private String entityNamePrefix = "";
    private String entityNamePostfix = "";
    private boolean selfEnabled = true;

    public static  <T> T getContainer(EObject from, Class<T> type) {
        T found = null;
        Object current = from;
        while (found == null && current != null) {
            if (type.isAssignableFrom(current.getClass())) {
                found = (T) current;
            }
            if (from.eContainer() != null) {
                current = ((EObject) current).eContainer();
            } else {
                current = null;
            }
        }
        return found;
    }

    public static String getJqlForDerived(EntityMemberDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();

        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;

        return transformer.getJql(declaration.getGetterExpr());
    }

    public static String getJqlForEntityQuery(EntityMemberDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();

        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;

        HashMap<String, String> passedArgs = new HashMap<String, String>();

        return transformer.getJql(declaration.getGetterExpr(), passedArgs);
    }


    public static String getJqlForStaticQuery(QueryDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();

        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;

        HashMap<String, String> passedArgs = new HashMap<String, String>();

        for (QueryParameterDeclaration queryParameter : declaration.getParameters()) {
            String value = "input." + queryParameter.getName();

            if (queryParameter.getDefault() != null) {
                value = String.format("(%s!isDefined() ? %s : %s)", value, value, transformer.getJql(queryParameter.getDefault()));
            }

            passedArgs.put(queryParameter.getName(), value);
        }

        return transformer.getJql(declaration.getGetterExpr(), passedArgs);
    }

    public static String getJqlForExpression(Expression expression, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();

        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;

        String result = transformer.getJql(expression);
        if (result == null || result.isBlank()) {
            throw new IllegalArgumentException("Empty expresion for: '" + expression + "'");
        }

        return result;
    }

    public String getModelDeclarationPSMFullyQualifiedName(ModelDeclaration modelDeclaration, ModelDeclaration owner) {
        String fqName = owner.getName().replaceAll("::", "_");
        return fqName + "::" + modelDeclaration.getName();
    }

    public String getEntityPSMFullyQualifiedName(EntityDeclaration entityDeclaration, EObject owner) {
        ModelDeclaration modelDeclaration = (ModelDeclaration) getContainer(entityDeclaration, ModelDeclaration.class);
        ModelDeclaration ownerModelDeclaration = (ModelDeclaration) getContainer(owner, ModelDeclaration.class);
        return getModelDeclarationPSMFullyQualifiedName(modelDeclaration, ownerModelDeclaration) + "::" + entityNamePrefix + entityDeclaration.getName() + entityNamePostfix;
    }

    public String getDataTypePSMFullyQualifiedName(DataTypeDeclaration dataTypeDeclaration, EObject owner) {
        ModelDeclaration modelDeclaration = (ModelDeclaration) getContainer(dataTypeDeclaration, ModelDeclaration.class);
        ModelDeclaration ownerModelDeclaration = (ModelDeclaration) getContainer(owner, ModelDeclaration.class);
        return getModelDeclarationPSMFullyQualifiedName(modelDeclaration, ownerModelDeclaration) + "::" + dataTypeDeclaration.getName();
    }

    public String getEnumTypePSMFullyQualifiedName(EnumDeclaration enumDeclaration, EObject owner) {
        ModelDeclaration modelDeclaration = (ModelDeclaration) getContainer(enumDeclaration, ModelDeclaration.class);
        ModelDeclaration ownerModelDeclaration = (ModelDeclaration) getContainer(owner, ModelDeclaration.class);
        return getModelDeclarationPSMFullyQualifiedName(modelDeclaration, ownerModelDeclaration) + "::" + enumDeclaration.getName();
    }


    /**
     *
     * getJql methods
     *
     */


    private String getJql(final TernaryOperation it,  Map<String, String> args) {
        return it != null
                ? getJql(it.getCondition(), args) + " ? " + getJql(it.getThenExpression(), args) + " : " + getJql(it.getElseExpression(), args)
                : null;
    }

    private String getJql(final BinaryOperation it, Map<String, String> args) {
        if (it == null) {
            return null;
        }
        if (it.getOperator().equals("^")) {
            Integer power = Integer.parseInt(getJql(it.getRightOperand()));
            return String.join(" * ", Collections.nCopies(power, getJql(it.getLeftOperand())));
        } else {
            return getJql(it.getLeftOperand(), args) + " " + it.getOperator() + " " + getJql(it.getRightOperand(), args);
        }
    }

    private String getJql(final UnaryOperation it, Map<String, String> args) {
        if (it != null) {
            if ("+".equals(it.getOperator())) {
                return getJql(it.getOperand());
            }

            return it.getOperator() + ' ' + getJql(it.getOperand());
        }

        return null;
    }


    private String getJql(final Navigation it, int featurePos, String base, Map<String, String> args) {
        if (it.getFeatures().size() > featurePos) {
            Feature feature = it.getFeatures().get(featurePos);

            if (feature instanceof MemberReference && !modelExtension.isQueryCall(feature)) {
                return getJql(it, featurePos + 1, base + "." + getJql((MemberReference)feature), args);
            }

            if (feature instanceof FunctionOrQueryCall) {
            	FunctionOrQueryCall functionOrQueryCall = (FunctionOrQueryCall)feature;

            	if (functionOrQueryCall.getDeclaration() instanceof FunctionDeclaration) {
            		FunctionDeclaration functionDeclaration = (FunctionDeclaration)functionOrQueryCall.getDeclaration();
            		
	                if (functionDeclaration.getName().equals("orElse")) {
	                    String elseExpression = getJql(functionOrQueryCall.getArguments().get(0).getExpression(), args);
	
	                    if (!(functionOrQueryCall.getArguments().get(0).getExpression() instanceof Navigation)) {
	                        elseExpression = "(" + elseExpression + ")";
	                    }
	
	                    return String.format("(%s!isDefined() ? %s : %s)", base, getJql(it, featurePos + 1, base, args), getJql(it, featurePos + 1, elseExpression, args));
	                } else {
	                    return getJql(it, featurePos + 1, base + getJql(functionOrQueryCall, args), args);
	                }
            	}

            	if (functionOrQueryCall.getDeclaration() instanceof QueryDeclaration) {
                    HashMap<String, String> passedArgs = new HashMap<String, String>();
                    passedArgs.put("self", base);

                    for (Argument queryArgument : functionOrQueryCall.getArguments()) {
                        String argument = getJql(queryArgument.getExpression(), args);
                        if (!(queryArgument.getExpression() instanceof Navigation)) {
                            argument = "(" + argument + ")";
                        }
                        passedArgs.put(((QueryParameterDeclaration)queryArgument.getDeclaration()).getName(), argument);
                    }

                    return getJql(it, featurePos + 1, getJql(((QueryDeclaration)functionOrQueryCall.getDeclaration()).getGetterExpr(), passedArgs), args);
            	}
            }

            if (feature instanceof LambdaCall) {
                return getJql(it, featurePos + 1, base + getJql((LambdaCall)feature, args), args);
            }

            if (modelExtension.isQueryCall(feature)) {
                return getJql(it, featurePos + 1, getJqlQuery((MemberReference)feature, base, args), args);
            }
        }

        return base;
    }

    private String getJql(final Navigation it, Map<String, String> args) {
        return getJql(it, 0, getJql(it.getBase(), args), args);
    }

    public String getJql(final NavigationBase it, Map<String, String> args) {
        if (it instanceof Self) {
            return getJql( (Self) it, args );
        } else if (it instanceof Parentheses) {
            return getJql( (Parentheses) it, args);
        } else if (it instanceof NavigationBaseDeclarationReference) {
            return getJql( (NavigationBaseDeclarationReference) it, args);
        } else if (it instanceof FunctionOrQueryCall) {
            return getJql( (FunctionOrQueryCall) it, args);
        } else if (it instanceof Literal) {
            return getJql( (Literal) it);
        }

        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
    }

    private String getJql(final NavigationBaseDeclarationReference it, Map<String, String> args) {

        NavigationBaseDeclaration navigationBaseReference = it.getReference();

        if (navigationBaseReference instanceof EntityDeclaration) {
            return getEntityPSMFullyQualifiedName((EntityDeclaration) navigationBaseReference, it);
        } else if (navigationBaseReference instanceof LambdaVariable) {
            return ((LambdaVariable) it.getReference()).getName();
        } else if (navigationBaseReference instanceof QueryParameterDeclaration) {
            return getJql( (QueryParameterDeclaration)navigationBaseReference, args );
        } else if (navigationBaseReference instanceof DataTypeDeclaration) {
            return getDataTypePSMFullyQualifiedName((DataTypeDeclaration) navigationBaseReference, it);
        } else if (navigationBaseReference instanceof EnumDeclaration) {
            return getEnumTypePSMFullyQualifiedName((EnumDeclaration) navigationBaseReference, it);
        } else if (navigationBaseReference instanceof EntityMapDeclaration) {
            return "self";
        }

        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it.getReference()).toString());
    }

    private String getJql(final QueryParameterDeclaration it, Map<String, String> args) {
        if (args.containsKey(it.getName()))
            return args.get(it.getName());

        return getJql(it.getDefault());
    }

    private String getJql(final Self it, Map<String, String> args) {
        if (args.containsKey("self"))
            return args.get("self");

        return "self";
    }

    private String getJql(final Parentheses it, Map<String, String> args) {
        return "(" + getJql(it.getExpression(), args) + ")";
    }

    private String getJql(final FunctionOrQueryCall it, Map<String, String> args) {
        HashMap<String, String> passedArgs = new HashMap<String, String>();

        if (it.getDeclaration() instanceof FunctionDeclaration) {
	        FunctionDeclaration functionDeclaration = (FunctionDeclaration)it.getDeclaration();
	        String functionName = functionDeclaration.getName();

	        if ("all".equals(functionName)) {
	            return "";
	        } else if ("plus".equals(functionName)) {
	            return "!" + Jsl2JqlFunction.getTimestampPlusFunctionAsJql(it, f -> getJql(f, args), functionName);
	        } else {
	            return "!" + Jsl2JqlFunction.getFunctionAsJql(it, f -> getJql(f, args), functionName);
	        }
        }

        for (Argument argument : it.getArguments()) {
            String argumentJql = getJql(argument.getExpression(), args);
            if (!(argument.getExpression() instanceof Navigation)) {
                argumentJql = "(" + argument + ")";
            }
            passedArgs.put(((QueryParameterDeclaration)argument.getDeclaration()).getName(), argumentJql);
        }

        return getJql(((QueryDeclaration)it.getDeclaration()).getGetterExpr(), passedArgs);
    }

    private String getJql(final MemberReference it) {
        NavigationTarget navigationTarget = it.getMember();

        if (navigationTarget instanceof EntityFieldDeclaration) {
            return ((EntityFieldDeclaration)navigationTarget).getName();
        } else if (navigationTarget instanceof EntityRelationDeclaration) {
            return ((EntityRelationDeclaration)navigationTarget).getName();
        } else if (navigationTarget instanceof EntityRelationOppositeInjected) {
            return ((EntityRelationOppositeInjected)navigationTarget).getName();
        }

        return null;
    }


    private String getJqlQuery(final MemberReference it, String self, Map<String, String> args) {
        HashMap<String, String> passedArgs = new HashMap<String, String>();
        passedArgs.put("self", self);

        if (it.getMember() instanceof EntityMemberDeclaration && modelExtension.isCalculated((EntityMemberDeclaration)it.getMember())) {
           	return getJql(((EntityMemberDeclaration)it.getMember()).getGetterExpr(), passedArgs);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    it.toString());

        }
    }

    private String getJql(final LambdaCall it, Map<String, String> args) {
        LambdaDeclaration lambdaDeclaration = (LambdaDeclaration)it.getDeclaration();
        String lambdaName = lambdaDeclaration.getName();

        if (lambdaName.equals("first")) {
            return "!heads" + "(" + it.getVariable().getName() + " | " + getJql(it.getLambdaExpression(), args) + ")";
        }

        if (lambdaName.equals("last")) {
            return "!heads" + "(" + it.getVariable().getName() + " | " + getJql(it.getLambdaExpression(), args) + " DESC)";
        }

        if (lambdaName.equals("front")) {
            return "!tails" + "(" + it.getVariable().getName() + " | " + getJql(it.getLambdaExpression(), args) + " DESC)";
        }

        if (lambdaName.equals("back")) {
            return "!tails" + "(" + it.getVariable().getName() + " | " + getJql(it.getLambdaExpression(), args) + ")";
        }

        return "!" + Jsl2JqlFunction.getEffectiveLambdaName(it.getDeclaration()) + "(" + it.getVariable().getName() + " | " +  getJql(it.getLambdaExpression(), args) + ")";
   }


    private String getJql(final DateLiteral it) {
        return it != null
              ? "`" + it.getValue() + "`"

                : null;
    }

    private String getJql(final TimestampLiteral it) {
        return it != null
              ? "`" + it.getValue() + "`"

                : null;
    }

    private String getJql(final TimeLiteral it) {
        return it != null
                ? "`" + it.getValue() + "`"
                : null;
    }

    private String getJql(final RawStringLiteral it) {
        return it != null
                ? "\"" + it.getValue() + "\""
                : null;
    }

    private String getJql(final EscapedStringLiteral it) {
        return it != null
                ? "\"" + it.getValue() + "\""
                : null;
    }

    private String getJql(final DecimalLiteral it) {
        return it != null
                ? (it.isMinus() ? "(-" + it.getValue().toString() + ")" : it.getValue().toString())
                : null;
    }

    private String getJql(final IntegerLiteral it) {
        return it != null
                ? (it.isMinus() ? "(-" + it.getValue().toString() + ")" : it.getValue().toString())
                : null;
    }

    private String getJql(final BooleanLiteral it) {
        return it != null
                ? Boolean.toString(it.isTrue())
                : null;
    }

    private String getJql(final EnumLiteralReference it) {
        return it != null
                ? getEnumTypePSMFullyQualifiedName(it.getEnumDeclaration(), it) + "#" + it.getEnumLiteral().getName()
                : null;
    }

	private String getJql(final Literal it) {
	  	if (it instanceof IntegerLiteral) {
	  		return getJql((IntegerLiteral) it);
		} else if (it instanceof DecimalLiteral) {
	  		return getJql((DecimalLiteral) it);
		} else if (it instanceof BooleanLiteral) {
	  		return getJql((BooleanLiteral) it);
		} else if (it instanceof EscapedStringLiteral) {
	  		return getJql((EscapedStringLiteral) it);
		} else if (it instanceof RawStringLiteral) {
	  		return getJql((RawStringLiteral) it);
		} else if (it instanceof DateLiteral) {
	  		return getJql((DateLiteral) it);
		} else if (it instanceof TimeLiteral) {
	  		return getJql((TimeLiteral) it);
		} else if (it instanceof TimestampLiteral) {
	  		return getJql((TimestampLiteral) it);
		} else if (it instanceof EnumLiteralReference) {
	  		return getJql((EnumLiteralReference) it);
		}

        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
    }

    public String getJql(final Expression it, Map<String, String> args) {
        if (it instanceof TernaryOperation) {
            return getJql( (TernaryOperation) it, args);
        } else if (it instanceof BinaryOperation) {
            return getJql( (BinaryOperation) it, args);
        } else if (it instanceof UnaryOperation) {
            return getJql((UnaryOperation) it, args);
        } else if (it instanceof Navigation) {
            return getJql( (Navigation) it, args);
        }

        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
    }

    public String getJql(final Expression it) {
        return getJql(it, new HashMap<String, String>());
    }
}
