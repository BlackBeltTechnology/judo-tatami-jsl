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
    
    public static String getJqlForDerived(EntityDerivedDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
        
        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;
        
        return transformer.getJql(declaration.getExpression());
    }

    public static String getJqlForEntityQuery(EntityQueryDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
 
        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;

    	HashMap<String, String> passedArgs = new HashMap<String, String>();
    	
    	for (QueryParameterDeclaration queryParameter : declaration.getParameters()) {
    		String value = "input." + queryParameter.getName();
    		
    		if (queryParameter.getDefault() != null) {
    			value = String.format("%s!isDefined() ? %s : %s", value, value, transformer.getJql(queryParameter.getDefault()));
    		}
    		
    		passedArgs.put(queryParameter.getName(), value);
    	}
    	
        return transformer.getJql(declaration.getExpression(), passedArgs);
    }


    public static String getJqlForStaticQuery(QueryDeclaration declaration, String entityNamePrefix, String entityNamePostfix) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();

        transformer.entityNamePrefix = entityNamePrefix;
        transformer.entityNamePostfix = entityNamePostfix;

    	HashMap<String, String> passedArgs = new HashMap<String, String>();
    	
    	for (QueryParameterDeclaration queryParameter : declaration.getParameters()) {
    		String value = "input." + queryParameter.getName();
    		
    		if (queryParameter.getDefault() != null) {
    			value = String.format("%s!isDefined() ? %s : %s", value, value, transformer.getJql(queryParameter.getDefault()));
    		}
    		
    		passedArgs.put(queryParameter.getName(), value);
    	}
    	
        return transformer.getJql(declaration.getExpression(), passedArgs);
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

	    	if (feature instanceof MemberReference) {
	    		return getJql(it, featurePos + 1, base + "." + getJql((MemberReference)feature), args);
	    	}
	    	
	    	if (feature instanceof FunctionCall) {
	    		FunctionCall functionCall = (FunctionCall)feature;
	    		
	    		if (functionCall.getDeclaration().getName().equals("orElse")) {
	    	    	String elseExpression = getJql(functionCall.getArguments().get(0).getExpression(), args);	    	    	
	    	    	return String.format("%s!isDefined() ? (%s) : (%s)", base, getJql(it, featurePos + 1, base, args), getJql(it, featurePos + 1, "(" + elseExpression + ")", args));
	    		} else {
	        		return getJql(it, featurePos + 1, base + getJql(functionCall, args), args);
	    		}
	    	}

	    	if (feature instanceof LambdaCall) {
        		return getJql(it, featurePos + 1, base + getJql((LambdaCall)feature, args), args);
	    	}

	    	if (feature instanceof EntityQueryCall) {
        		return getJql(it, featurePos + 1, getJql((EntityQueryCall)feature, base, args), args);
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
		} else if (it instanceof QueryCall) {
			return getJql( (QueryCall) it, args);
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
		}
		
        throw new IllegalArgumentException("Unhandled parameter types: " +
                Arrays.<Object>asList(it).toString());
	}

	private String getJql(final QueryParameterDeclaration it, Map<String, String> args) {
		if (args.containsKey(it.getName()))
			return "(" + args.get(it.getName()) + ")";
		
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
	
    private String getJql(final QueryCall it, Map<String, String> args) {
    	HashMap<String, String> passedArgs = new HashMap<String, String>();
    	    	
    	for (QueryArgument queryArgument : it.getArguments()) {
    		passedArgs.put(queryArgument.getDeclaration().getName(), getJql(queryArgument.getExpression(), args));
    	}
    	
        return getJql(it.getDeclaration().getExpression(), passedArgs);
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

 
    private String getJql(final MemberReference it) {
    	NavigationTarget navigationTarget = it.getMember();
    	
    	if (navigationTarget instanceof EntityFieldDeclaration) {
    		return ((EntityFieldDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityIdentifierDeclaration) {
    		return ((EntityIdentifierDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityRelationDeclaration) {
    		return ((EntityRelationDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityDerivedDeclaration) {
    		return ((EntityDerivedDeclaration)navigationTarget).getName();
    	} else if (navigationTarget instanceof EntityRelationOppositeInjected) {
    		return ((EntityRelationOppositeInjected)navigationTarget).getName();
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
    private String getJql(final EntityQueryCall it, String self, Map<String, String> args) {
    	HashMap<String, String> passedArgs = new HashMap<String, String>();
    	passedArgs.put("self", self);
    	
    	for (QueryArgument queryArgument : it.getArguments()) {
    		passedArgs.put(queryArgument.getDeclaration().getName(), getJql(queryArgument.getExpression(), args));
    	}
    	
        return getJql(it.getDeclaration().getExpression(), passedArgs);
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
//    private String getJqlDispacher(final EntityQueryCall it) {
//
//        if (it == null) {
//            return "";
//        }
//        
//        EntityQueryDeclaration queryDeclaration = it.getDeclaration();
//        
//        // Get parameters which passed from call
//        Map<String, String> parameterValues = new HashMap();
//        parameterValues.putAll(it.getArguments()
//                .stream()
//                .filter(e -> e.getDeclaration() != null)
//                .collect(
//                        Collectors.toMap(
//                                e -> e.getDeclaration().getName(), 
//                                e -> resolveQueryArgumentValue(e), 
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


//    private String getJql(final EntityQueryCall it) {
//        if (it == null) {
//            return null;
//        }
//        return getJql(it.getExpression());            
//    }
//
//    
//    private String resolveQueryArgumentValue(QueryArgument argument) {
//        if (TypeInfo.getTargetType(argument.getExpression()).isLiteral()) {
//            return getJql(argument.getExpression());
//        } else if (argument.getExpression() != null) {
//            Map<String, String> stackValues = queryStackParameterValues.peekLast();
//            if (stackValues != null && stackValues.containsKey(argument.getDeclaration().getName())) {
//                return stackValues.get(argument.getDeclaration().getName());
//            }
//        } 
//        return getJql(argument.getDeclaration().getDefault());            
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

    

    
	private String getJql(final FunctionCall it, Map<String, String> args) {
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

    private String getJql(final LambdaCall it, Map<String, String> args) {
		LambdaDeclaration lambdaDeclaration = (LambdaDeclaration)it.getDeclaration();
		String lambdaName = lambdaDeclaration.getName();
		
		return "!" + Jsl2JqlFunction.getEffectiveLambdaName(it.getDeclaration()) + "(" + it.getVariable().getName() + " | " +  getJql(it.getLambdaExpression(), args) + ")";
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
    private String getJql(final DateLiteral it) {
        return it != null
              ? "`" + it.getValue() + "`"

                : null;
    }

    private String getJql(final TimeStampLiteral it) {
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
                ? Boolean.toString(it.isIsTrue())
                : null;
    }

    private String getJql(final EnumLiteralReference it) {
        return it != null
                ? it.getEnumDeclaration().getName() + "#" + it.getEnumLiteral().getName()
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
		} else if (it instanceof TimeStampLiteral) {
	  		return getJql((TimeStampLiteral) it);
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
