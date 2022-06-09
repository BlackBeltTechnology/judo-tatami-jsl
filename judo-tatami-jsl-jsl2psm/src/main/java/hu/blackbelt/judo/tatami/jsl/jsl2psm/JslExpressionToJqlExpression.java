package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.judo.meta.jsl.jsldsl.BinaryOperation;
import hu.blackbelt.judo.meta.jsl.jsldsl.BooleanLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.CreateExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.DateLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDerivedDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityMemberDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityQueryDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EnumLiteralReference;
import hu.blackbelt.judo.meta.jsl.jsldsl.EscapedStringLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.Expression;
import hu.blackbelt.judo.meta.jsl.jsldsl.Feature;
import hu.blackbelt.judo.meta.jsl.jsldsl.Function;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionCall;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionedExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.InstanceFunction;
import hu.blackbelt.judo.meta.jsl.jsldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.LambdaFunction;
import hu.blackbelt.judo.meta.jsl.jsldsl.LambdaFunctionParameters;
import hu.blackbelt.judo.meta.jsl.jsldsl.LambdaVariable;
import hu.blackbelt.judo.meta.jsl.jsldsl.LiteralFunction;
import hu.blackbelt.judo.meta.jsl.jsldsl.Named;
import hu.blackbelt.judo.meta.jsl.jsldsl.NamedFunction;
import hu.blackbelt.judo.meta.jsl.jsldsl.NavigationBaseReference;
import hu.blackbelt.judo.meta.jsl.jsldsl.NavigationExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.ParenthesizedExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryCall;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryDeclarationParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.RawStringLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.SelectorFunction;
import hu.blackbelt.judo.meta.jsl.jsldsl.SpawnOperation;
import hu.blackbelt.judo.meta.jsl.jsldsl.TernaryOperation;
import hu.blackbelt.judo.meta.jsl.jsldsl.TimeLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.TimeStampLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.UnaryOperation;
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;

import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.emf.ecore.EObject;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class JslExpressionToJqlExpression {
    
    static JslDslModelExtension modelExtension = new JslDslModelExtension();
    
    private Deque<Map<QueryParameter, EObject>> queryStackParameterValues = new ArrayDeque();

    public static  <T extends Class> T getContainer(EObject from, Class<T> type) {
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

    /*
    public Deque<EntityDerivedDeclaration> getBaseQuery(final EntityDerivedDeclaration d) {
        Deque<EntityDerivedDeclaration> stack = new ArrayDeque();
        EntityDerivedDeclaration entityDerivedDeclaration = d;
        stack.push(entityDerivedDeclaration);
        
        while (containsParametrizedQueryCall(entityDerivedDeclaration.getExpression())) {
            
            if (!(d.getExpression() instanceof NavigationExpression)) {
                throw new IllegalArgumentException("Parametrized query expression has to be Navigation Expression - " + entityDerivedDeclaration.getName());
            }
        
            NavigationExpression nav = (NavigationExpression) entityDerivedDeclaration.getExpression();
            if (!(nav.isIsSelf())) {
                throw new IllegalArgumentException("A Parametrized query's navigation expression has to be Self - " + entityDerivedDeclaration.getName());
            }

            
            if (nav.getFeatures().size() == 0 || 
                    nav.getFeatures().get(0).getMember() == null || 
                    nav.getFeatures().get(0).getMember().getNavigationDeclarationType() == null ||
                    !(nav.getFeatures().get(0).getMember().getNavigationDeclarationType() instanceof EntityDerivedDeclaration)) {
                throw new IllegalArgumentException("The Self referenced feature has to be an entity derived declaration - " + entityDerivedDeclaration.getName());
            }

            entityDerivedDeclaration = (EntityDerivedDeclaration) nav.getFeatures().get(0).getMember().getNavigationDeclarationType();
            stack.push(entityDerivedDeclaration);

        }
        return stack;
    }
    */

    
    /*
    public Deque<EntityDerivedDeclaration> getBaseDerived(final EntityDerivedDeclaration d) {
        Deque<EntityDerivedDeclaration> stack = new ArrayDeque();
        EntityDerivedDeclaration entityDerivedDeclaration = d;
        stack.push(entityDerivedDeclaration);
        
        while (containsParametrizedQueryCall(entityDerivedDeclaration.getExpression())) {
            
            if (!(d.getExpression() instanceof NavigationExpression)) {
                throw new IllegalArgumentException("Parametrized query expression has to be Navigation Expression - " + entityDerivedDeclaration.getName());
            }
        
            NavigationExpression nav = (NavigationExpression) entityDerivedDeclaration.getExpression();
            if (!(nav.isIsSelf())) {
                throw new IllegalArgumentException("A Parametrized query's navigation expression has to be Self - " + entityDerivedDeclaration.getName());
            }

            
            if (nav.getFeatures().size() == 0 || 
                    nav.getFeatures().get(0).getMember() == null || 
                    nav.getFeatures().get(0).getMember().getNavigationDeclarationType() == null ||
                    !(nav.getFeatures().get(0).getMember().getNavigationDeclarationType() instanceof EntityDerivedDeclaration)) {
                throw new IllegalArgumentException("The Self referenced feature has to be an entity derived declaration - " + entityDerivedDeclaration.getName());
            }

            entityDerivedDeclaration = (EntityDerivedDeclaration) nav.getFeatures().get(0).getMember().getNavigationDeclarationType();
            stack.push(entityDerivedDeclaration);

        }
        return stack;
    }
    */
    
    public static String getJql(EntityDerivedDeclaration declaration) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
//        transformer.originalDerivedDeclaration = declaration;
//        transformer.entityDerivedDeclarationCallStack = transformer.getBaseDerived(declaration);
//        transformer.baseDerivedDeclaration = transformer.entityDerivedDeclarationCallStack.getFirst();
//        transformer.collectedParameterValues = transformer.collectParameterValues();
        return transformer.getJql(declaration.getExpression());
    }
    
    public static String getJqlForExpression(Expression expression) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
//        transformer.originalDerivedDeclaration = null;
//        transformer.entityDerivedDeclarationCallStack = new ArrayDeque<>();        
//        transformer.baseDerivedDeclaration = null;
//        transformer.collectedParameterValues = new HashMap<>();
        return transformer.getJql(expression);
    }

//    private Map<QueryParameter, EObject> collectParameterValues() {
//        Map<QueryParameter, EObject> derivedValueOrParameter = new HashMap();
        
        /*
        entityDerivedDeclarationCallStack.descendingIterator().forEachRemaining((c) -> {
            EntityDerivedDeclaration current = c;
            
            // Collecting parameters
            if (current != baseDerivedDeclaration) {
                //addParameterValues(c, previousDeclaration.get(), parameterValues);            

                NavigationExpression currentNav = (NavigationExpression) current.getExpression();
                Feature currentMember = (Feature) currentNav.getFeatures().get(0).getMember();
                EntityDerivedDeclaration calledQuery = (EntityDerivedDeclaration) currentMember.getNavigationDeclarationType();

                // Check all parameters of the called method
                for (QueryParameter p : calledQuery.getParameters()) {
                    
                    Optional<QueryParameter> queryParameter = currentMember.getParameters().stream().filter(q -> q.getQueryParameterType() == p).findAny();

                    if (!queryParameter.isPresent()) {
                        // When the parameter is not given, using default value
                        derivedValueOrParameter.put(p, p.getDefault());
                    } else if (queryParameter.get().getParameter() != null) {
                        // When parameter is given and mapped from the caller,
                        derivedValueOrParameter.put(p, derivedValueOrParameter.get(queryParameter.get().getParameter()));
                        if (!originalDerivedDeclaration.getParameters().contains(queryParameter.get().getParameter())) {
                            derivedValueOrParameter.put(p, derivedValueOrParameter.get(queryParameter.get().getParameter()));                            
                        } else {
                            derivedValueOrParameter.put(p, queryParameter.get().getParameter());                                                        
                        }
                    } else {
                        // When parameter is given and mapped caller literal
                        derivedValueOrParameter.put(p, queryParameter.get().getLiteral());        
                    }

                }
            } else {
                for (QueryParameter p : originalDerivedDeclaration.getParameters()) {
                    if (!derivedValueOrParameter.containsKey(p)) {
                        derivedValueOrParameter.put(p, p);                                    
                    }
                }                
            }
            
        });
        */

//        return derivedValueOrParameter;
//    }

    
    /**
     * Expression returns Expression hidden(WS, CONT_NL, SL_COMMENT, ML_COMMENT)
     * : SwitchExpression
     * ;
     */
    private String getJql(final Expression it) {
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
        return it != null
                ? getJql(it.getLeftOperand()) + " " + it.getOperator() + " " + getJql(it.getRightOperand())
                : null;
    }

    /**
     * SpawnOperation returns Expression
     * : UnaryOperation (=> ({SpawnOperation.operand=current} 'as' spawnTargetType=LocalName))?
     * ;
     */
    private String getJqlDispacher(final SpawnOperation it) {
        return it != null
                ? getJql(it.getOperand()) + " as " + it.getSpawnTargetType()
                : null;
    }

    /**
     * UnaryOperation returns Expression
     * : {UnaryOperation} operator=('not' | '-') operand=FunctionedExpression
     * | FunctionedExpression
     * ;
     */
    private String getJqlDispacher(final UnaryOperation it) {
        return it != null
                ? it.getOperator() + ' ' + getJql(it.getOperand())
                : null;
    }

    /**
     * FunctionedExpression returns Expression
     * : NavigationExpression ({FunctionedExpression.operand=current} functionCall=FunctionCall)?
     * ;
     */
    private String getJqlDispacher(final FunctionedExpression it) {
        return it != null
                ? getJql(it.getOperand()) + getJql(it.getFunctionCall())
                : null;
    }

    /**
     * 
     */
    private String getJql(final QueryDeclaration it) {
    	if (it == null) {
    		return null;
    	}
    	return getJql(it.getExpression());
    }

    private String getJqlWithoutBase(final QueryDeclaration it) {
    	if (it == null) {
    		return null;
    	}
    	return getJql(it.getExpression());
    }
    
    /**
     * NavigationExpression returns Expression
     * 	: PrimaryExpression features+=Feature*
     *  | NavigationBase
     *  | EnumLiteralReference
     *  | QueryCall features+=Feature*
     *  | PrimitiveDeclaration
     *  ;
     * NavigationBase returns NavigationExpression
     * 	: {NavigationExpression} (isSelf ?= 'self' | navigationBaseType = [NavigationBaseReference | LocalName]) (features+=Feature*) 
     * 	;
     * NavigationBaseReference
     * 	: EntityDeclaration
     * 	| LambdaVariable
     * 	| QueryDeclarationParameter
     * 	;
     * 
     * PrimaryExpression returns Expression
     * 	: ParenthesizedExpression
     * 	| CreateExpression
     * 	| Literal
     * 	;
     */
    private String getJqlDispacher(final NavigationExpression it, boolean cutPrefix) {

    	if (it == null) {
            return "";
        }

    	//if (it.getBase() != null) {
        //    return getJql(it.getBase()) + it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining());
        //}
    	
    	/*
    	 * 	: ParenthesizedExpression
	| CreateExpression
	| Literal

    	 */

    	String navExpression = "";
    	if (it.isIsSelf()) {
    		navExpression = "self";
    	} else if (it.getNavigationBaseType() != null) {
   	
	    	if (it.getNavigationBaseType() instanceof QueryDeclaration) {
	    		//navExpression = getNameForNamed(it.getNavigationBaseType());
	    		navExpression = getJql((QueryDeclaration)it.getNavigationBaseType());	    		
	    		// TODO: Put parameters to stack, call getJql for QueryDeclaration and remove parameters
	    		// queryStackParameterValues.add(null)		    		
	    	} else if (it.getNavigationBaseType() instanceof QueryDeclarationParameter) {
	    		// TODO: Get parameter value from stack or default and put inside
	    		navExpression += ((QueryDeclarationParameter)it.getNavigationBaseType()).getName();
	
	    	} else if (it.getNavigationBaseType() instanceof Named) {
	    		navExpression = getNameForNamed(it.getNavigationBaseType());
	    	} else {
	    		throw new IllegalArgumentException("Expression base type have to be Named");
	    	}
    	} else if (it instanceof EnumLiteralReference) {
    		// TODO: Resolve PSM fully qualified name
    		navExpression = ((EnumLiteralReference) it).getEnumDeclaration().getName() + "#" + ((EnumLiteralReference) it).getEnumLiteral().getName();
    	} else {
    		navExpression = getJql(it);
    	}
        return navExpression + it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining());

    	
    	/*
    	if (it.getBase() != null) {
            return getJql(it.getBase()) + it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining());
        }
        if (it.getEnumValue() != null) {
            return it.getQName() + "#" + it.getEnumValue();
        }
        if (it.getFeatures().size() > 0) {
            return it.getQName() + it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining());
        }
        
        // Check the navigation name matches with declared parameter
        Optional<DerivedParameter> parameter = baseDerivedDeclaration.getParameters().stream()
                .filter(p -> p.getName().equals(it.getQName())).findFirst();
        
        if (parameter.isPresent()) {
            if (collectedParameterValues.get(parameter.get()) != null) {
                if (collectedParameterValues.get(parameter.get()) instanceof DerivedParameter) {
                	DerivedParameter derivedParameter = (DerivedParameter) collectedParameterValues.get(parameter.get());
                	String parName = "input." + derivedParameter.getName();
                    return parName + "!isDefined() ? " + parName + " : " + getJql(derivedParameter.getDefault());                 
                } else {
                    return getJql((Expression) collectedParameterValues.get(parameter.get()));
                }
            } else {
                throw new IllegalStateException("Could not resolve parameter value");
            }
        }
        return it.getQName();
        */

    	
        /*
        return it != null
                ? it.getBase() != null
                ? getJql(it.getBase()) + it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining())
                : it.getQName() + (it.getEnumValue() != null
                ? "#" + it.getEnumValue()
                : it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining()))
                : "";
        */
    }

    /**
     * QueryCall returns Expression
	 *  : {QueryCall} queryDeclarationReference = [ QueryDeclaration | LocalName ] '(' (parameters+=QueryParameter (',' parameters+=QueryParameter)*)? ')'
	 *  ;
     */
    // TODO: Cleanup mess
    private String getJqlDispacher(final QueryCall it) {

    	if (it == null) {
            return "";
        }
    	
    	// TODO: Put parameter values to stack. In QueryParameter the values have to be resolved, because it's value
    	// can be reference for the base query's parameter, which have to be presented on stack. 

    	return "";
    }



    /**
     * FunctionCall
     * : {FunctionCall} '!' function=Function features+=Feature* call=FunctionCall?
     * ;
     */
    private String getJql(final FunctionCall it) {
        return it != null
                ? '!' + getJql(it.getFunction()) +
                (
                        it.getCall() != null
                                ? getJql(it.getCall())
                                : ""
                )
                : null;
    }


    private String getJql(final EntityQueryDeclaration it) {
    	if (it == null) {
    		return null;
    	}
    	return getJql(it.getExpression());
    }
    
    private String getJqlWithoutBase(final EntityQueryDeclaration it) {
    	if (it == null) {
    		return null;
    	}
    	if (it.getExpression() instanceof NavigationExpression) {
    		NavigationExpression navigation = (NavigationExpression) it.getExpression();
    		if (navigation.isIsSelf()) {
    			
    		}
    	} else {
    		
    	}
    	return getJql(it.getExpression());
    }

    
    /**
     * Feature
     *     : {Feature} '.' navigationTargetType = [NavigationDeclaration | LocalName]('(' (parameters+=QueryParameter (',' parameters+=QueryParameter)*)? ')')?
     *     ;
     * NavigationDeclaration
     *     : LambdaVariable
     *     | EntityMemberDeclaration
     *     | EntityDeclaration
     *     ;
     */
    private String getJql(final Feature it) {
    	if (it == null) {
    		return null;
    	}
    	String repr = null; //"<." + getNameForNamed(it.getNavigationTargetType());
    	
    	if (it.getNavigationTargetType() instanceof EntityQueryDeclaration) {
    		repr = getJql((EntityQueryDeclaration) it.getNavigationTargetType());    		
    	} else if (it.getNavigationTargetType() instanceof EntityDeclaration) {
    		// TODO: Resolve entity new name
    		repr = ((EntityDeclaration) it.getNavigationTargetType()).getName();
    	} else if (it.getNavigationTargetType() instanceof Named) {
    		repr = getNameForNamed(it.getNavigationTargetType());
    	} else {
    		throw new IllegalArgumentException("Feature navigation target have to be Named");
    	}
    	
    	// TODO: Push parameters to stack.
    	
//    	if (it.getParameters().size() > 0) {
//    		repr += "(" +  it.getParameters().stream().map(p -> getJql(p)).collect(Collectors.joining(",")) + ")";    		
//    	}
    	return "." + repr;
    }


    /**
     * QueryParameter
     * :  derivedParameterType=[DerivedParameter | LocalName] '=' (literal = Literal | parameter = [DerivedParameter | LocalName])     // expression=MultilineExpression
     * ;
     */
    private String getJql(final QueryParameter it) {
        return it != null
                ? it.getQueryParameterType().getName() + "=" + 
                    (it.getLiteral() != null 
                    ? getJql(it.getLiteral())
                    : it.getQueryParameterType().getName())
                : null;
    }

    /**
     * ParenthesizedExpression returns Expression
     * : '(' Expression ')'
     * ;
     */
    private String getJqlDispacher(final ParenthesizedExpression it) {
        return it != null
                ?  "(" + getJql(it.getExpression()) + ")"
                : null;
    }

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
    private String getJqlDispacher(final CreateExpression it) {
        return it != null
                ?  ""
                : null;
    }

    
    /**
     * Function
     * 	: SelectableFunction
     *  ;
    
     * FunctionParameterDeclaration
     * 	: LambdaFunctionParameters
     * 	| ParametrizedFunctionParameters
     * 	;
     * 
     * LambdaFunctionParameters
     * : lambdaArgument=LambdaVariable '|' expression = Expression
     * ;
     * 
     * ParametrizedFunctionParameters
     * : parameters+=FunctionParameter (',' parameters+=FunctionParameter)*
     * ;
     *     
     * LambdaVariable
     * : {LambdaVariable} name=ID    	
     * ;
     * 
     * NamedFunction returns Function : {NamedFunction} name = JslID
     * ;
     * LambdaFunction returns NamedFunction : {LambdaFunction} lambdaArgument = LambdaFunctionParameters;
     * LiteralFunction returns NamedFunction : {LiteralFunction} parameters += FunctionParameter;
     * InstanceFunction returns NamedFunction : {InstanceFunction} entityDeclaration = [EntityDeclaration | LocalName];
     * SelectorFunction returns NamedFunction : {SelectorFunction} selectorArgument = SelectorFunctionParameters;
     * 
     */

    private String getJql(final Function it) {
        if (it instanceof LiteralFunction) {
            return getJql((LiteralFunction)it);
        } else if (it instanceof InstanceFunction) {
            return getJql((InstanceFunction)it);
        } else if (it instanceof SelectorFunction) {
            return getJql((SelectorFunction)it);
        } else if (it instanceof LambdaFunction) {
            return getJql((LambdaFunction)it);
        }
        else return  getJql((NamedFunction) it);
    }

    private String getJql(final NamedFunction it) {
    	return it.getName() + "()";
    }

    private String getJql(final LiteralFunction it) {
    	if (it == null) {
        	return null;    		
    	}
    	return it.getName() + "(" + it.getParameters().stream().map(p -> getJql(p)).collect(Collectors.joining(",")) + ")";
   }

    private String getJql(final InstanceFunction it) {
    	if (it == null) {
        	return null;    		
    	}
    	return it.getName() + "()";
   }

    private String getJql(final SelectorFunction it) {
    	if (it == null) {
        	return null;    		
    	}
    	return it.getName() + "()";
   }

    private String getJql(final LambdaFunction it) {
    	if (it == null) {
        	return null;    		
    	}
    	return it.getName() + (it.getLambdaArgument() != null ? ("(" + it.getLambdaArgument().getLambdaArgument().getName() + " | " + getJql(it.getLambdaArgument().getExpression()) + ")") : "()") ;
   }
    

    /**
     * FunctionParameter
     * : {FunctionParameter} expression=Expression
     * ;
     */
    private String getJql(final FunctionParameter it) {
        return it != null
                ? getJql(it.getExpression())
                : null;
    }

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
//                ? "`" + it.getValue() + "`"
              ? it.getValue()

        		: null;
    }

    private String getJqlDispacher(final TimeStampLiteral it) {
        return it != null
//                ? "`" + it.getValue() + "`"
              ? it.getValue()

        		: null;
    }

    private String getJqlDispacher(final TimeLiteral it) {
        return it != null
//                ? "`" + it.getValue() + "`"
        		? it.getValue()
                : null;
    }

    private String getJqlDispacher(final RawStringLiteral it) {
        return it != null
                ? "\"" + StringEscapeUtils.escapeJava(it.getValue()
                        .replaceAll("^r\"|\"$", "")) + "\""
                : null;
    }

    private String getJqlDispacher(final EscapedStringLiteral it) {
        return it != null
                ? "\"" + it.getValue().replaceAll("^\"|\"$", "") + "\""
                : null;
    }

    private String getJqlDispacher(final DecimalLiteral it) {
        return it != null
                ? it.getValue().toString()
                : null;
    }

    private String getJqlDispacher(final IntegerLiteral it) {
        return it != null
                ? it.getValue().toString()
                : null;
    }

    private String getJqlDispacher(final BooleanLiteral it) {
        return it != null
                ? Boolean.toString(it.isIsTrue())
                : null;
    }


    private String getJqlDispacher(final Expression it) {
        if (it instanceof BinaryOperation) {
            return getJqlDispacher((BinaryOperation)it);
        } else if (it instanceof BooleanLiteral) {
            return getJqlDispacher((BooleanLiteral)it);
        } else if (it instanceof CreateExpression) {
            return getJqlDispacher((CreateExpression)it);
        } else if (it instanceof DateLiteral) {
            return getJqlDispacher((DateLiteral)it);
        } else if (it instanceof DecimalLiteral) {
            return getJqlDispacher((DecimalLiteral)it);
        } else if (it instanceof EscapedStringLiteral) {
            return getJqlDispacher((EscapedStringLiteral)it);
        } else if (it instanceof FunctionedExpression) {
            return getJqlDispacher((FunctionedExpression)it);
        } else if (it instanceof IntegerLiteral) {
            return getJqlDispacher((IntegerLiteral)it);
        } else if (it instanceof RawStringLiteral) {
            return getJqlDispacher((RawStringLiteral)it);
        } else if (it instanceof SpawnOperation) {
            return getJqlDispacher((SpawnOperation)it);
        } else if (it instanceof TernaryOperation) {
            return getJqlDispacher((TernaryOperation)it);
        } else if (it instanceof TimeLiteral) {
            return getJqlDispacher((TimeLiteral)it);
        } else if (it instanceof TimeStampLiteral) {
            return getJqlDispacher((TimeStampLiteral)it);
        } else if (it instanceof UnaryOperation) {
            return getJqlDispacher((UnaryOperation)it);
        } else if (it instanceof QueryCall) {
            return getJqlDispacher((QueryCall)it);
        } else if (it instanceof ParenthesizedExpression) {
            return getJqlDispacher((ParenthesizedExpression)it);
        } else if (it instanceof NavigationExpression) {
            return getJqlDispacher((NavigationExpression)it, false);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    Arrays.<Object>asList(it).toString());
        }
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
