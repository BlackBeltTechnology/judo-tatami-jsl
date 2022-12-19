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

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.ecl.parse.Ecl_EolParserRules.throwStatement_return;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.*;
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
    
    public static String getJqlForDerived(EntityDerivedDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
        
        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;
        
        return transformer.getJql(declaration.getExpression());
    }

    public static String getJqlForEntityQuery(EntityQueryDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
        //transformer.forQuery = true;
        
        Map<String, String> parameterValues = new HashMap();
        parameterValues.putAll(declaration.getParameters()
                .stream()
                .collect(
                        Collectors.toMap(
                                e -> e.getName(), 
                                e -> e.getDefault() != null ? "(input." + e.getName() + "!isDefined() ? " 
                                        + "input." + e.getName() 
                                        + " : " + transformer.getJql(e.getDefault()) + ")" : "input." + e.getName(), 
                                (key1, key2)-> key2)));

        transformer.queryStackParameterValues.add(parameterValues);
        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;
        
        return transformer.getJql(declaration.getExpression());
    }


    public static String getJqlForStaticQuery(QueryDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();

        Map<String, String> parameterValues = new HashMap();
        parameterValues.putAll(declaration.getParameters()
                .stream()
                .collect(
                        Collectors.toMap(
                                e -> e.getName(), 
                                e -> e.getDefault() != null ? "(input." + e.getName() + "!isDefined() ? " 
                                        + "input." + e.getName() 
                                        + " : " + transformer.getJql(e.getDefault()) + ")" : "input." + e.getName(), 
                                (key1, key2)-> key2)));

        transformer.queryStackParameterValues.add(parameterValues);
        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;
        transformer.selfEnabled = false;

        return transformer.getJql(declaration.getExpression());
    }

    public static String getJqlForExpression(Expression expression, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;
        
        String result = transformer.getJql(expression);
        if (result == null || result.isBlank()) {
        	throw new IllegalArgumentException("Empty expresion for: '" + expression + "'");
        }
        
        return transformer.getJql(expression);
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
     * Expression returns Expression hidden(WS, CONT_NL, SL_COMMENT, ML_COMMENT)
     * : SwitchExpression
     * ;
     */
    private String getJql(final Expression it) {
        return getJqlDispacher(it);
    }

    private String getJql(final Literal it) {
        return getJqlDispacher(it);
    }

    
    /**
     * // right associative rule
     * SwitchExpression returns Expression
     * : ImpliesExpression (=> ({TernaryOperation.condition=current} '?')
     * thenExpression=SwitchExpression ':'
     * elseExpression=SwitchExpression)?
     * ;
     */
    private String getJqlDispacher(final TernaryOperation it) {
        return it != null
                ? getJql(it.getCondition()) + " ? " + getJql(it.getThenExpression()) + " : " + getJql(it.getElseExpression())
                : null;
    }

    /**
     * ImpliesExpression returns Expression
     * : OrExpression (=> ({BinaryOperation.leftOperand=current} operator='implies') rightOperand=OrExpression)
     * ;
     *
     * OrExpression returns Expression
     * : XorExpression (=> ({BinaryOperation.leftOperand=current} operator='or') rightOperand=XorExpression)
     * ;
     *
     * XorExpression returns Expression
     * : AndExpression (=> ({BinaryOperation.leftOperand=current} operator='xor') rightOperand=AndExpression)
     * ;
     *
     * AndExpression returns Expression
     * : EqualityExpression (=> ({BinaryOperation.leftOperand=current} operator='and') rightOperand=EqualityExpression)
     * ;
     *
     * EqualityExpression returns Expression
     * : RelationalExpression (=> ({BinaryOperation.leftOperand=current} operator=('!='|'==')) rightOperand=RelationalExpression)
     * ;
     *
     * RelationalExpression returns Expression
     * : AdditiveExpression (=> ({BinaryOperation.leftOperand=current} operator=('>=' | '<=' | '>' | '<')) rightOperand=AdditiveExpression)
     * ;
     *
     * AdditiveExpression returns Expression
     * : MultiplicativeExpression (=> ({BinaryOperation.leftOperand=current} operator=('+'|'-')) rightOperand=MultiplicativeExpression)
     * ;
     *
     * MultiplicativeExpression returns Expression
     * : ExponentExpression (=> ({BinaryOperation.leftOperand=current} operator=('*' | '/' | 'div' | 'mod')) rightOperand=ExponentExpression)
     * ;
     *
     * ExponentExpression returns Expression
     * : SpawnOperation (=> ({BinaryOperation.leftOperand=current} operator='^') rightOperand=SpawnOperation)
     * ;
     */
    private String getJqlDispacher(final BinaryOperation it) {
        if (it == null) {
            return null;
        }
        if (it.getOperator().equals("^")) {
            Integer power = Integer.parseInt(getJql(it.getRightOperand()));
            return String.join(" * ", Collections.nCopies(power, getJql(it.getLeftOperand())));
        } else {
            return getJql(it.getLeftOperand()) + " " + it.getOperator() + " " + getJql(it.getRightOperand());
        }
    }

    /**
     * UnaryOperation returns Expression
     * : {UnaryOperation} operator=('not' | '-') operand=FunctionedExpression
     * | FunctionedExpression
     * ;
     */
    private String getJqlDispacher(final UnaryOperation it) {
    	if (it != null) {
    		if ("+".equals(it.getOperator())) {
    			return getJql(it.getOperand());
    		}
    		
    		return it.getOperator() + ' ' + getJql(it.getOperand());
    	}

    	return null;
    }


	private String getJqlDispacher(final Navigation it) {
		int size = it.getFeatures().size();
		if (size > 0) {
			return getJqlDispacher(it.getFeatures().get(size - 1));
		} else {
			return getJqlDispacher( it.getBase() );
		}
	}

	public String getJqlDispacher(final NavigationBase it) {
		if (it instanceof Self) {
			return getJqlDispacher( (Self) it );
		} else if (it instanceof Parentheses) {
			return getJqlDispacher( (Parentheses) it);
		} else if (it instanceof NavigationBaseDeclarationReference) {
			return getJqlDispacher( (NavigationBaseDeclarationReference) it);
		} else if (it instanceof QueryCall) {
			return getJqlDispacher( (QueryCall) it);
		} else if (it instanceof Literal) {
			return getJqlDispacher( (Literal) it);
		}

        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
	}

	public String getJqlDispacher(final Self it) {
		return "self";
	}
	
	private String getJqlDispacher(final Parentheses it) {
		return "(" + it.getExpression() + ")";
	}
	
	private String getTargetType(final NavigationBaseDeclarationReference it) {
		NavigationBaseDeclaration navigationBaseReference = it.getReference();
		
		if (navigationBaseReference instanceof EntityDeclaration) {
			return getEntityPSMFullyQualifiedName((EntityDeclaration) navigationBaseReference, it);
		} else if (navigationBaseReference instanceof LambdaVariable) {
			return ((LambdaVariable) it.getReference()).getName();
		} else if (navigationBaseReference instanceof QueryParameterDeclaration) {
			return ((QueryParameterDeclaration) it.getReference()).getName();
		} else if (navigationBaseReference instanceof DataTypeDeclaration) {
			return getDataTypePSMFullyQualifiedName((DataTypeDeclaration) navigationBaseReference, it);
		} else if (navigationBaseReference instanceof EnumDeclaration) {
			return getEnumTypePSMFullyQualifiedName((EnumDeclaration) navigationBaseReference, it);
		}
		
        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
	}

//    private String getJqlDispacher(final Navigation it) {
//    	if (it != null) {
//    		return getJqlDispacher(it.getBase()) + getJqlDispacher(it.getFeature());
//    	}
//
//    	return null;
//    }
  
//    Call
//	: FunctionOrQueryCall
//	| LambdaCall
//	;

    private String getJqlDispacher(final Feature it) {
    	if (it instanceof MemberReference) {
    		return "." + getJqlDispacher((MemberReference)it);
    	}
    	
    	if (it instanceof FunctionCall) {
    		return "!" + getJql((FunctionCall)it);
    	}

    	if (it instanceof LambdaCall) {
    		return "!" + getJql((LambdaCall)it);
    	}

    	return null;
    }

 
    private String getJqlDispacher(final MemberReference it) {
    	NavigationTarget navigationTarget = it.getMember();
    	
    	if (navigationTarget instanceof EntityFieldDeclaration) {
    		return "." + ((EntityFieldDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityIdentifierDeclaration) {
    		return "." + ((EntityIdentifierDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityRelationDeclaration) {
    		return "." + ((EntityRelationDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityDerivedDeclaration) {
    		return "." + ((EntityDerivedDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityRelationOppositeInjected) {
    		return "." + ((EntityRelationOppositeInjected)navigationTarget).getName();
    	}
    	
    	return null;
    }
    
    /**
     * FunctionedExpression returns Expression
     * : NavigationExpression ({FunctionedExpression.operand=current} functionCall=FunctionCall)?
     * ;
     */
//    private String getJqlDispacher(final FunctionedExpression it) {
//        return it != null
//                ? getJql(it.getFunctionCall(), getJql(it.getOperand()))
//                : null;
//    }

    /**
     * QueryDeclaration
     * : 'query' (referenceType = [SingleType | LocalName] Cardinality?) Named
     *   ('(' parameters += QueryDeclarationParameter (',' parameters += QueryDeclarationParameter)* ')')
     *   "=>" expression = Expression
     * ;
     */
    private String getJql(final QueryDeclaration it) {
        if (it == null) {
            return null;
        }
        return getJql(it.getExpression());
    }
    
    
//    private String resolveQueryDeclarationParameterValue(QueryDeclarationParameter parameter) {
//        String keyName = parameter.getName();
//
//        Map<String, String> stackValues = queryStackParameterValues.peekLast();
//        if (stackValues != null && stackValues.containsKey(keyName)) {
//            return stackValues.get(keyName);
//        }
//        return getJql(parameter.getDefault());
//    }

    
    /**    
    * NavigationBaseExpression returns NavigationExpression
    *     : {NavigationBaseExpression} navigationBaseType = [NavigationBaseReference | LocalName]  (features+=Feature*)
    *     ;
    * NavigationBaseReference
    *     : EntityDeclaration
    *     | QueryDeclaration
    *     | LambdaVariable
    *     | QueryDeclarationParameter
    *     | PrimitiveDeclaration
    ;
    *
    */
//    private String getJqlDispacher(final NavigationBaseExpression it) {
//
//    	if (it == null) {
//            return "";
//        }
//        
//        String navExpression = "";
//        if (it.getNavigationBaseType() instanceof EntityDeclaration) {
//            navExpression = getEntityPSMFullyQualifiedName((EntityDeclaration) it.getNavigationBaseType(), it);
//        } else if (it.getNavigationBaseType() instanceof QueryDeclaration) {
//            navExpression = ((QueryDeclaration) it.getNavigationBaseType()).getName();            
//        } else if (it.getNavigationBaseType() instanceof LambdaVariable) {
//            navExpression = ((LambdaVariable) it.getNavigationBaseType()).getName();
//        } else if (it.getNavigationBaseType() instanceof QueryDeclarationParameter) {               
//            navExpression = resolveQueryDeclarationParameterValue((QueryDeclarationParameter) it.getNavigationBaseType());
//        } else if (it.getNavigationBaseType() instanceof DataTypeDeclaration) {
//            navExpression = getDataTypePSMFullyQualifiedName((DataTypeDeclaration) it.getNavigationBaseType(), it);
//        } else if (it.getNavigationBaseType() instanceof EnumDeclaration) {
//            navExpression = getEnumTypePSMFullyQualifiedName((EnumDeclaration) it.getNavigationBaseType(), it);
//        }
//        return navExpression + it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining());
//    }
    
//    /**
//     * SelfExpression returns NavigationExpression
//     *     : {SelfExpression} isSelf ?= 'self' (features+=Feature*)        
//     *     ;
//     */
//    private String getJqlDispacher(final SelfExpression it) {
//    	if (!selfEnabled) {
//    		throw new IllegalArgumentException("Self expression is not enabled in this expression");
//    	}
//    	
//        if (it == null) {
//            return "";
//        }
//
//        return "self";
//    }

    
    /**
     *  QueryCallExpression returns NavigationExpression
     *      : {QueryCallExpression} queryDeclarationType = [ QueryDeclaration | LocalName ] '(' (parameters+=QueryParameter (',' parameters+=QueryParameter)*)? ')' (features+=Feature*)
     *  ;
     */
//    private String getJqlDispacher(final QueryCallExpression it) {
//
//        if (it == null) {
//            return "";
//        }
//        
//         QueryDeclaration queryDeclaration = it.getQueryDeclarationType();
//        
//        // Get parameters which passed from call
//        Map<String, String> parameterValues = new HashMap();
//        parameterValues.putAll(it.getParameters()
//                .stream()
//                .filter(e -> e.getQueryParameterType() != null)
//                .collect(
//                        Collectors.toMap(
//                                e -> e.getQueryParameterType().getName(), 
//                                e -> resolveQueryParameterValue(e), 
//                                (key1, key2)-> key2)));
//
//        // Search for parameters which is not defined to set default values
//        Map<String, String> missingValues = queryDeclaration.getParameters().stream()
//            .filter(e -> !parameterValues.containsKey(e.getName()))
//            .collect(
//                    Collectors.toMap(
//                            e -> e.getName(), 
//                            e -> getJql(e.getDefault()), 
//                            (key1, key2)-> key2)                        
//                    );
//        
//        parameterValues.putAll(missingValues);
//
//        queryCallStack.add(queryDeclaration);
//        queryStackParameterValues.add(parameterValues);
//        String repr = getJql(queryDeclaration);
//        queryStackParameterValues.poll();
//        queryCallStack.poll();            
//
//        return repr;
//    }



    /**
     * FunctionCall
     * : {FunctionCall} '!' function=Function features+=Feature* call=FunctionCall?
     * ;
     */
//    private String getJql(final FunctionCall it, String base) {
//		if (it != null) {
//			String jqlFunction = "!" + getJql(it.getFunction());
//			String jqlCall = "";
//
//            String jqlFeatures = it.getFeatures() != null
//              			? it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining())
//            			: "";
//			
//			if (it.getFunction() instanceof LiteralFunction) {
//				LiteralFunction literalFunction = (LiteralFunction)it.getFunction();
//				
//    			if (literalFunction.getFunctionDeclarationReference().getName().equals("orElse")) {
//    				jqlCall = getJql(it.getCall(), base + jqlFeatures);
//    				String jqlElse = getJql(it.getCall(), "(" + getJql(literalFunction.getParameters().get(0).getExpression()) + ")");
//
//    				return String.format("(%s!isDefined() ? %s : %s)", base + jqlFeatures, jqlCall, jqlElse);
//    			}
//			}
//			
//			if (it.getCall() != null) {
//				jqlCall = getJql(it.getCall(), base + "!" + getJql(it.getFunction()) + jqlFeatures);
//				return jqlCall;
//			}
//			
//			return base + jqlFunction + jqlFeatures;
//		} else {
//			return base;
//		}
//    }


    private String getJql(final EntityQueryDeclaration it) {
        if (it == null) {
            return null;
        }
        return getJql(it.getExpression());            
    }

    
//    private String resolveQueryParameterValue(QueryParameter parameter) {
//        if (parameter.getLiteral() != null) {
//            return getJql(parameter.getLiteral());
//        } else if (parameter.getParameter() != null) {
//            Map<String, String> stackValues = queryStackParameterValues.peekLast();
//            if (stackValues != null && stackValues.containsKey(parameter.getParameter().getName())) {
//                return stackValues.get(parameter.getParameter().getName());
//            }
//        } 
//        return getJql(parameter.getQueryParameterType().getDefault());            
//    }
    
    /**
     * Feature
     *     : {Feature} {Feature.base = current} '.' navigationTargetType = [NavigationTarget | LocalName]('(' (parameters+=QueryParameter (',' parameters+=QueryParameter)*)? ')')?
     *     ;
     * NavigationTarget
     *     : EntityMemberDeclaration
     *     | EntityDeclaration
     *     ;
     */
//    private String getJql(final Feature it) {
//        if (it == null) {
//            return null;
//        }
//        String repr = null;
//        
//        if (it.getNavigationTargetType() instanceof EntityQueryDeclaration) {
//        	boolean oldSelfEnabled = selfEnabled;
//            EntityQueryDeclaration entityQueryDeclaration = (EntityQueryDeclaration) it.getNavigationTargetType();
//            
//            // Get parameters which passed from call
//            Map<String, String> parameterValues = new HashMap();
//            parameterValues.putAll(it.getParameters()
//                    .stream()
//                    .filter(e -> e.getQueryParameterType() != null)
//                    .collect(
//                            Collectors.toMap(
//                                    e -> e.getQueryParameterType().getName(), 
//                                    e -> resolveQueryParameterValue(e), 
//                                    (key1, key2)-> key2)));
//
//            // Search for parameters which is not defined to set default values
//            Map<String, String> missingValues = entityQueryDeclaration.getParameters().stream()
//                .filter(e -> !parameterValues.containsKey(e.getName()))
//                .collect(
//                        Collectors.toMap(
//                                e -> e.getName(), 
//                                e -> getJql(e.getDefault()), 
//                                (key1, key2)-> key2)                        
//                        );
//            
//            parameterValues.putAll(missingValues);
//            
//            queryCallStack.add(entityQueryDeclaration);
//            queryStackParameterValues.add(parameterValues);
//            selfEnabled = true;
//            repr = getJql((EntityQueryDeclaration) it.getNavigationTargetType());
//            selfEnabled = oldSelfEnabled;
//            queryStackParameterValues.poll();
//            queryCallStack.poll();            
//            
//        } else if (it.getNavigationTargetType() instanceof EntityDeclaration) {
//            repr = getEntityPSMFullyQualifiedName((EntityDeclaration) it.getNavigationTargetType(), it);
//        } else if (it.getNavigationTargetType() instanceof Named) {
//            repr = getNameForNamed(it.getNavigationTargetType());
//        } else {
//            throw new IllegalArgumentException("Feature navigation target have to be Named");
//        }
//        
//        if (queryCallStack.size() > 0 && it.eContainer() instanceof SelfExpression) {
//        	if (!selfEnabled) {
//        		throw new IllegalArgumentException("Self expression is not enabled in this expression");
//        	}
//            return repr;            
//        } else {
//            return "." + repr;
//        }
//    }


    /**
     * QueryParameter
     *     :  queryParameterType=[QueryDeclarationParameter | QueryParameterName] '=' (literal = Literal | parameter = [QueryDeclarationParameter | QueryParameterName])     // expression=MultilineExpression
     *     ;
     * 
     */
//    private String getJql(final QueryParameter it) {
//        return it != null
//                ? it.getQueryParameterType().getName() + "=" + 
//                    (it.getLiteral() != null 
//                    ? getJql(it.getLiteral())
//                    : it.getQueryParameterType().getName())
//                : null;
//    }

//    /**
//     * ParenthesizedExpression returns Expression
//     * : '(' Expression ')'
//     * ;
//     */
//    private String getJqlDispacher(final ParenthesizedExpression it) {
//        if (it == null) {
//            return null;
//        }
//        if ((it.eContainer() instanceof QueryDeclaration) || (it.eContainer() instanceof EntityQueryDeclaration)) {
//            return getJql(it.getExpression());
//        }
//        return it != null
//                ?  "(" + getJql(it.getExpression()) + ")"
//                : null;
//    }

    /**
     * // Warning: create statement is not allowed in getter!
     * CreateExpression returns Expression
     * : {CreateExpression} 'new'? type=[ClassDeclaration]
     * (   '(' (assignments+=CreateParameter (',' assignments+=CreateParameter)*)? ')'
     * | '[' (creates += Expression (',' creates += Expression)*)? ']'
     * )
     * ;
     *
     * CreateParameter:
     * name=ID '=' right=Expression;
     */
//    private String getJqlDispacher(final CreateExpression it) {
//        return it != null
//                ?  ""
//                : null;
//    }

    
    /**
     * Function
     *     : LiteralFunction
     *     | LambdaFunction
     *     | InstanceFunction
     *     | SelectorFunction
     *     ;

     * LiteralFunction returns Function: {LiteralFunction} functionDeclarationReference = [LiteralFunctionDeclaration | LocalName ] '(' (parameters += LiteralFunctionParameter (',' parameters += LiteralFunctionParameter)*)? ')' ;
     * LambdaFunction returns Function : {LambdaFunction} functionDeclarationReference = [LambdaFunctionDeclaration | LocalName ] '(' lambdaArgument=LambdaVariable '|' expression = Expression ')';
     * InstanceFunction returns Function : {InstanceFunction} functionDeclarationReference = [InstanceFunctionDeclaration | LocalName ] '(' entityDeclaration = [EntityDeclaration | LocalName] ')';
     * //SelectorFunction returns Function : {SelectorFunction} => functionDeclarationReference = [SelectorFunctionDeclaration | LocalName ] '('  selectorArgument = SelectorVariable '|' selectors += SelectorDeclaration (',' selectors += SelectorDeclaration)* ')';

     * LiteralFunctionParameters
     *     : {LiteralFunctionParameters} parameters += LiteralFunctionParameter (',' parameters += LiteralFunctionParameter)*
     *     ;

     * LiteralFunctionParameter 
     *     : declaration = [FunctionParameterDeclaration | LocalName] '=' expression=Expression 
     *     ;

     * LambdaFunctionParameters
     *     : lambdaArgument=LambdaVariable '|' expression = Expression
     *     ;
    
     * LambdaVariable
     *     : {LambdaVariable} Named        
     *     ;

     * SelectorFunctionParameters
     *     : {SelectorFunctionParameters} selectorArgument=SelectorVariable '|' selectors += SelectorDeclaration (',' selectors += SelectorDeclaration)*
     *     ;

     * SelectorDeclaration
     *     : {SelectorDeclaration} selector = [SelectorVariable | LocalName] '.' member = [EntityMemberDeclaration | LocalName] ('DESC' | isTrue?='ASC')?
     *     ;

     * SelectorVariable
     *     : {SelectorVariable} Named
     *     ;

     */

//    private String getJql(final Function it) {
//        if (it instanceof LiteralFunction) {
//            return getJql((LiteralFunction)it);
////        } else if (it instanceof InstanceFunction) {
////            return getJql((InstanceFunction)it);
////        } else if (it instanceof SelectorFunction) {
////            return getJql((SelectorFunction)it);
//        } else if (it instanceof LambdaFunction) {
//            return getJql((LambdaFunction)it);
//        }
//        else return ""; // getJql((NamedFunction) it);
//    }

    

    
    private String getJql(final FunctionCall it) {
        if (it == null) {
            return null;            
        }
        
        if (it.getDeclaration() instanceof FunctionDeclaration) {
        	return Jsl2JqlFunction.getFunctionAsJql(it, f -> getJql(f));
        }
        
        return null;
   }

    private String getJql(final LambdaCall it) {
        if (it == null) {
            return null;            
        }
        
        if (it.getDeclaration() instanceof FunctionDeclaration) {
        	return Jsl2JqlFunction.getLambdaAsJql(it, f -> getJql(f));
        }
        
        return null;
   }

    /*
    private String getJql(final LiteralFunctionParameter it) {
        if (it == null) {
            return null;            
        }
        // Determinate functions
        
        return it.getDeclaration().getName() + " = " + getJql(it.getExpression());
    } */
    

    /*
    private String getJql(final InstanceFunction it) {
        if (it == null) {
            return null;            
        }
        return it.getFunctionDeclarationReference().getName() + "(" + 
        	(it.getEntityDeclaration() != null 
        		? getEntityPSMFullyQualifiedName(it.getEntityDeclaration(), it) 
				: "") 
        	+ ")";
   }*/

//    private String getJql(final LambdaFunction it) {
//        if (it == null) {
//            return null;            
//        }
//        
//        String functionName = it.getFunctionDeclarationReference().getName();
//
//        if (functionName.equals("first")) {
//            return "heads" + "(" + it.getLambdaArgument().getName() + " | " + getJql(it.getExpression()) + ")";
//        }
//
//        if (functionName.equals("last")) {
//            return "heads" + "(" + it.getLambdaArgument().getName() + " | " + getJql(it.getExpression()) + " DESC)";
//        }
//
//        if (functionName.equals("front")) {
//            return "tails" + "(" + it.getLambdaArgument().getName() + " | " + getJql(it.getExpression()) + " DESC)";
//        }
//
//        if (functionName.equals("back")) {
//            return "tails" + "(" + it.getLambdaArgument().getName() + " | " + getJql(it.getExpression()) + ")";
//        }
//
//        return it.getFunctionDeclarationReference().getName() + (it.getLambdaArgument() != null 
//                ? ("(" + it.getLambdaArgument().getName() + " | " + getJql(it.getExpression()) + ")") : "()") ;
//   }
    

    /**
     * Literal returns Expression
     * : BooleanLiteral
     * | NumberLiteral
     * | StringLiteral
     * | TemporalLiteral
     * ;
     *
     * BooleanLiteral returns Expression
     * : {BooleanLiteral} ('false' | isTrue?='true')
     * ;
     *
     * NumberLiteral returns Expression
     * : {IntegerLiteral} value=INTEGER
     * | {DecimalLiteral} value=DECIMAL
     * ;
     *
     * StringLiteral returns Expression
     * : {EscapedStringLiteral} value=STRING
     * | {RawStringLiteral} value=RAW_STRING
     * ;
     *
     * TemporalLiteral returns Expression
     * : {DateLiteral} value=DATE
     * | {TimeStampLiteral} value=TIMESTAMP
     * | {TimeLiteral} value=TIME
     * ;
     */
    private String getJqlDispacher(final DateLiteral it) {
        return it != null
              ? "`" + it.getValue() + "`"

                : null;
    }

    private String getJqlDispacher(final TimeStampLiteral it) {
        return it != null
              ? "`" + it.getValue() + "`"

                : null;
    }

    private String getJqlDispacher(final TimeLiteral it) {
        return it != null
                ? "`" + it.getValue() + "`"
                : null;
    }

    private String getJqlDispacher(final RawStringLiteral it) {
        return it != null
                ? "\"" + it.getValue() + "\""
                : null;
    }

    private String getJqlDispacher(final EscapedStringLiteral it) {
        return it != null
                ? "\"" + it.getValue() + "\""
                : null;
    }

    private String getJqlDispacher(final DecimalLiteral it) {
        return it != null
                ? (it.isMinus() ? "(-" + it.getValue().toString() + ")" : it.getValue().toString())
                : null;
    }

    private String getJqlDispacher(final IntegerLiteral it) {
        return it != null
                ? (it.isMinus() ? "(-" + it.getValue().toString() + ")" : it.getValue().toString())
                : null;
    }

    private String getJqlDispacher(final BooleanLiteral it) {
        return it != null
                ? Boolean.toString(it.isIsTrue())
                : null;
    }

    private String getJqlDispacher(final EnumLiteralReference it) {
        return it != null
                ? it.getEnumDeclaration().getName() + "#" + it.getEnumLiteral().getName()
                : null;
    }

	public String getJqlDispacher(final Literal it) {
	  	if (it instanceof IntegerLiteral) {
	  		return getJqlDispacher((IntegerLiteral) it);
		} else if (it instanceof DecimalLiteral) {
	  		return getJqlDispacher((DecimalLiteral) it);
		} else if (it instanceof BooleanLiteral) {
	  		return getJqlDispacher((BooleanLiteral) it);
		} else if (it instanceof EscapedStringLiteral) {
	  		return getJqlDispacher((EscapedStringLiteral) it);
		} else if (it instanceof RawStringLiteral) {
	  		return getJqlDispacher((RawStringLiteral) it);
		} else if (it instanceof DateLiteral) {
	  		return getJqlDispacher((DateLiteral) it);
		} else if (it instanceof TimeLiteral) {
	  		return getJqlDispacher((TimeLiteral) it);
		} else if (it instanceof TimeStampLiteral) {
	  		return getJqlDispacher((TimeStampLiteral) it);
		} else if (it instanceof EnumLiteralReference) {
	  		return getJqlDispacher((EnumLiteralReference) it);
		}

        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
	}

	public String getJqlDispacher(final Expression it) {
		if (it instanceof TernaryOperation) {
			return getJqlDispacher( (TernaryOperation) it);
		} else if (it instanceof BinaryOperation) {
			return getJqlDispacher( (BinaryOperation) it);
		} else if (it instanceof UnaryOperation) {
            return getJqlDispacher((UnaryOperation) it);
		} else if (it instanceof Navigation) {
			return getJqlDispacher( (Navigation) it);
		}		

        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
	}

//    private String getJqlDispacher(final Expression it) {
//        if (it instanceof BinaryOperation) {
//            return getJqlDispacher((BinaryOperation) it);
//        } else if (it instanceof BooleanLiteral) {
//            return getJqlDispacher((BooleanLiteral) it);
////        } else if (it instanceof CreateExpression) {
////            return getJqlDispacher((CreateExpression) it);
//        } else if (it instanceof DateLiteral) {
//            return getJqlDispacher((DateLiteral) it);
//        } else if (it instanceof DecimalLiteral) {
//            return getJqlDispacher((DecimalLiteral) it);
//        } else if (it instanceof EscapedStringLiteral) {
//            return getJqlDispacher((EscapedStringLiteral) it);
//            
//        } else if (it instanceof Navigation) {
//            return getJqlDispacher((Navigation) it);
//            
////        } else if (it instanceof FunctionedExpression) {
////            return getJqlDispacher((FunctionedExpression) it);
//        } else if (it instanceof IntegerLiteral) {
//            return getJqlDispacher((IntegerLiteral) it);
//        } else if (it instanceof RawStringLiteral) {
//            return getJqlDispacher((RawStringLiteral) it);
//        } else if (it instanceof TernaryOperation) {
//            return getJqlDispacher((TernaryOperation) it);
//        } else if (it instanceof TimeLiteral) {
//            return getJqlDispacher((TimeLiteral) it);
//        } else if (it instanceof TimeStampLiteral) {
//            return getJqlDispacher((TimeStampLiteral) it);
//        } else if (it instanceof UnaryOperation) {
//            return getJqlDispacher((UnaryOperation) it);
//        } else if (it instanceof ParenthesizedExpression) {
//            return getJqlDispacher((ParenthesizedExpression) it);
////        } else if (it instanceof QueryCallExpression) {
////            return getJqlDispacher((QueryCallExpression) it);
//        } else if (it instanceof SelfExpression) {
//        	if (!selfEnabled) {
//        		throw new IllegalArgumentException("Self expression is not enabled in this expression");
//        	}
//            return getJqlDispacher((SelfExpression) it);
////        } else if (it instanceof NavigationBaseExpression) {
////            return getJqlDispacher((NavigationBaseExpression) it);
//        } else if (it instanceof EnumLiteralReference) {
//            return getJqlDispacher((EnumLiteralReference) it);
//        } else {
//            throw new IllegalArgumentException("Unhandled parameter types: " +
//                    Arrays.<Object>asList(it).toString());
//        }
//    }
    
    private String getNameForNamed(EObject baseRef) {
        if (baseRef == null) {
            return null;
        }
        if (baseRef instanceof Named) {
            return ((Named) baseRef).getName();
        } else {
            throw new IllegalArgumentException("Not a named object: " + baseRef);
        }
    }

}
