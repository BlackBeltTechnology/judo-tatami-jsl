package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.judo.meta.jsl.jsldsl.BinaryOperation;
import hu.blackbelt.judo.meta.jsl.jsldsl.BooleanLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.CreateExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.DateLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.DerivedParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDerivedDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityMemberDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EscapedStringLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.Expression;
import hu.blackbelt.judo.meta.jsl.jsldsl.Feature;
import hu.blackbelt.judo.meta.jsl.jsldsl.Function;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionCall;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionedExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.LambdaFunctionParameters;
import hu.blackbelt.judo.meta.jsl.jsldsl.NavigationExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.ParametrizedFunctionParameters;
import hu.blackbelt.judo.meta.jsl.jsldsl.ParenthesizedExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.RawStringLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.Self;
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
    
    private EntityDerivedDeclaration originalDerivedDeclaration;
    private EntityDerivedDeclaration baseDerivedDeclaration;
    private Deque<EntityDerivedDeclaration> entityDerivedDeclarationCallStack;
    private Map<DerivedParameter, EObject> collectedParameterValues;
    
    public EntityDerivedDeclaration getDerivedDeclaration(EObject from) {
        EntityDerivedDeclaration found = null;
        EObject current = from;
        while (found == null && current != null) {
            if (current instanceof EntityDerivedDeclaration) {
                found = (EntityDerivedDeclaration) current;
            }
            if (from.eContainer() != null) {
                current = current.eContainer();
            } else {
                current = null;
            }
        }
        return found;
    }

    public Deque<EntityDerivedDeclaration> getBaseDerived(final EntityDerivedDeclaration d) {
        Deque<EntityDerivedDeclaration> stack = new ArrayDeque();
        EntityDerivedDeclaration entityDerivedDeclaration = d;
        stack.push(entityDerivedDeclaration);
        
        while (containsParametrizedQueryCall(entityDerivedDeclaration.getExpression())) {
            
            if (!(d.getExpression() instanceof NavigationExpression)) {
                throw new IllegalArgumentException("Parametrized query expression has to be Navigation Expression - " + entityDerivedDeclaration.getName());
            }
        
            NavigationExpression nav = (NavigationExpression) entityDerivedDeclaration.getExpression();
            if (!(nav.getBase() instanceof Self)) {
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
    
    public static String getJql(EntityDerivedDeclaration declaration) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
        transformer.originalDerivedDeclaration = declaration;
        transformer.entityDerivedDeclarationCallStack = transformer.getBaseDerived(declaration);        
        transformer.baseDerivedDeclaration = transformer.entityDerivedDeclarationCallStack.getFirst();
        transformer.collectedParameterValues = transformer.collectParameterValues();
        return transformer.getJql(transformer.baseDerivedDeclaration.getExpression());
    }
    
    public static String getJqlForExpression(Expression expression) {
        JslExpressionToJqlExpression transformer = new JslExpressionToJqlExpression();
        transformer.originalDerivedDeclaration = null;
        transformer.entityDerivedDeclarationCallStack = new ArrayDeque<>();        
        transformer.baseDerivedDeclaration = null;
        transformer.collectedParameterValues = new HashMap<>();
        return transformer.getJql(expression);
    }

    private Map<DerivedParameter, EObject> collectParameterValues() {
        Map<DerivedParameter, EObject> derivedValueOrParameter = new HashMap();
        
        entityDerivedDeclarationCallStack.descendingIterator().forEachRemaining((c) -> {
            EntityDerivedDeclaration current = c;
            
            // Collecting parameters
            if (current != baseDerivedDeclaration) {
                //addParameterValues(c, previousDeclaration.get(), parameterValues);            

                NavigationExpression currentNav = (NavigationExpression) current.getExpression();
                Feature currentMember = (Feature) currentNav.getFeatures().get(0).getMember();
                EntityDerivedDeclaration calledQuery = (EntityDerivedDeclaration) currentMember.getNavigationDeclarationType();

                // Check all parameters of the called method
                for (DerivedParameter p : calledQuery.getParameters()) {
                    
                    Optional<QueryParameter> queryParameter = currentMember.getParameters().stream().filter(q -> q.getDerivedParameterType() == p).findAny();

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
                for (DerivedParameter p : originalDerivedDeclaration.getParameters()) {
                    if (!derivedValueOrParameter.containsKey(p)) {
                        derivedValueOrParameter.put(p, p);                                    
                    }
                }                
            }
            
        });

        return derivedValueOrParameter;
    }

    
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

    private Boolean containsParametrizedQueryCallDispacher(final TernaryOperation it) {
        if (it != null && containsParametrizedQueryCall(it.getCondition()) | containsParametrizedQueryCall(it.getThenExpression()) | containsParametrizedQueryCall(it.getElseExpression())) {
            throw new IllegalArgumentException("Ternary expression cannot contain parametrized query call");
        }
        return false;
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

    private Boolean containsParametrizedQueryCallDispacher(final BinaryOperation it) {
        if (it != null &&  containsParametrizedQueryCall(it.getLeftOperand()) | containsParametrizedQueryCall(it.getRightOperand())) {
            throw new IllegalArgumentException("Binary operation cannot contain parametrized query call");
        }
        return false;
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

    private Boolean containsParametrizedQueryCallDispacher(final SpawnOperation it) {
        if (it != null && containsParametrizedQueryCall(it.getOperand())) {
            throw new IllegalArgumentException("Spawn operation cannot contain parametrized query call");
        }
        return false;
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

    private Boolean containsParametrizedQueryCallDispacher(final UnaryOperation it) {
        if (it != null && containsParametrizedQueryCall(it.getOperand())) {
            throw new IllegalArgumentException("Unary operation cannot contain parametrized query call");
        }
        return false;
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

    private Boolean containsParametrizedQueryCallDispacher(final FunctionedExpression it) {
        if (it != null && containsParametrizedQueryCall(it.getFunctionCall())) {
            throw new IllegalArgumentException("Function expression cannot contain parametrized query call");
        }
        return false;
    }

    /**
     * NavigationExpression returns Expression
     * : PrimaryExpression ({NavigationExpression.base=current} features+=Feature+)?
     * | NavigationBase
     * ;
     * // enums as separate literals cause problems with completion and script
     * NavigationBase returns NavigationExpression
     * : {NavigationExpression} qName=LocalName (features+=Feature* | '#' enumValue = ID)
     * ;
     */
    private String getJqlDispacher(final NavigationExpression it) {
        if (it == null) {
            return "";
        }
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

    private Boolean containsParametrizedQueryCallDispacher(final NavigationExpression it) {
        if (it == null) {
            return false;
        }
        if (it.getBase() != null) {
            if (containsParametrizedQueryCall(it.getBase()) || !(it.getBase() instanceof Self)) {                
                throw new IllegalArgumentException("Base expression cannot contain parametrized query call");
            }
        }
        
        // If any feature contain parametrized call
        if (it.getFeatures().size() > 0) {
            return it.getFeatures().stream().map(p -> containsParametrizedQueryCall(p)).filter(p -> p).count() > 0;
        }
        
        return false;        
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

    private Boolean containsParametrizedQueryCall(final FunctionCall it) {
        return it != null
                ? containsParametrizedQueryCall(it.getFunction()) |
                (
                        it.getCall() != null
                                ? containsParametrizedQueryCall(it.getCall())
                                : false
                )
                : false;
    }

    /**
     * Feature
     * : {Feature} '.' member = EntityMemberDeclarationFeature
     * ;
     *
     * EntityMemberDeclarationFeature returns Feature
     * : entityMemberDeclarationType = [EntityMemberDeclaration | LocalName ] ('(' (parameters+=QueryParameter (',' parameters+=QueryParameter)*)? ')')?
     * ;
     */
    private String getJql(final Feature it) {
        return it != null
                ? '.' + modelExtension.getNameForEntityMemberDeclaration((EntityMemberDeclaration) it.getMember().getNavigationDeclarationType()) +
                (
                        it.getParameters().size() > 0
                                ? "(" +  it.getParameters().stream().map(p -> getJql(p)).collect(Collectors.joining(",")) + ")"
                                : ""
                )
                : null;
    }

    private Boolean containsParametrizedQueryCall(final Feature it) {
        if (it == null) {
            return false;
        }
        Feature member = it.getMember();
        if (member != null) {
            if (member.getNavigationDeclarationType() != null && member.getNavigationDeclarationType() instanceof EntityDerivedDeclaration) {
                return true;
            }                    
        }
        return false;
    }

    /**
     * QueryParameter
     * :  derivedParameterType=[DerivedParameter | LocalName] '=' (literal = Literal | parameter = [DerivedParameter | LocalName])     // expression=MultilineExpression
     * ;
     */
    private String getJql(final QueryParameter it) {
        return it != null
                ? it.getDerivedParameterType().getName() + "=" + 
                    (it.getLiteral() != null 
                    ? getJql(it.getLiteral())
                    : it.getDerivedParameterType().getName())
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

    private Boolean containsParametrizedQueryCallDispacher(final ParenthesizedExpression it) {
        if (it != null && containsParametrizedQueryCall(it.getExpression())) {                
            throw new IllegalArgumentException("Parenthesized expression cannot contain parametrized query call");
        }
        return false;
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
     * Function returns Function
     * : name=ID '(' parameterDeclaration = FunctionParameterDeclaration? ')'
     * ;
     * 
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
     */
    
    private String getJql(final Function it) {
        return it != null
                ? it.getName() + '(' +
                (
                        it.getParameterDeclaration() instanceof LambdaFunctionParameters
                                ? ((LambdaFunctionParameters) it.getParameterDeclaration()).getLambdaArgument().getName() + " | "
                                		+ getJql(((LambdaFunctionParameters) it.getParameterDeclaration()).getExpression())
                                : (
                                	it.getParameterDeclaration() instanceof ParametrizedFunctionParameters 
                                	? ((ParametrizedFunctionParameters) it.getParameterDeclaration()).getParameters().stream().map(p -> getJql(p)).collect(Collectors.joining(","))
                                	: "")
                ) + ")"
                : null;
   }

    private Boolean containsParametrizedQueryCall(final Function it) {
        if (it != null && it.getParameterDeclaration() instanceof ParametrizedFunctionParameters &&
        		((ParametrizedFunctionParameters) it.getParameterDeclaration()).getParameters().stream().map(p -> containsParametrizedQueryCall(p)).filter(p -> p).count() > 0) {
            throw new IllegalArgumentException("Function parameters cannot contain parametrized query call");            
        }
        return false;
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

    private Boolean containsParametrizedQueryCall(final FunctionParameter it) {
        return it != null
                ? containsParametrizedQueryCall(it.getExpression())
                : false;
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

    /**
     * Self returns Expression
     * : {Self} 'self'
     * ;
     */
    private String getJqlDispacher(final Self it) {
        return it != null
                ?  "self"
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
        } else if (it instanceof NavigationExpression) {
            return getJqlDispacher((NavigationExpression)it);
        } else if (it instanceof ParenthesizedExpression) {
            return getJqlDispacher((ParenthesizedExpression)it);
        } else if (it instanceof RawStringLiteral) {
            return getJqlDispacher((RawStringLiteral)it);
        } else if (it instanceof Self) {
            return getJqlDispacher((Self)it);
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
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    Arrays.<Object>asList(it).toString());
        }
    }
    
    private Boolean containsParametrizedQueryCall(final Expression it) {
        if (it instanceof BinaryOperation) {
            return containsParametrizedQueryCallDispacher((BinaryOperation)it);
        } else if (it instanceof BooleanLiteral) {
            return false;
        } else if (it instanceof DateLiteral) {
            return false;
        } else if (it instanceof DecimalLiteral) {
            return false;
        } else if (it instanceof EscapedStringLiteral) {
            return false;
        } else if (it instanceof FunctionedExpression) {
            return containsParametrizedQueryCallDispacher((FunctionedExpression)it);
        } else if (it instanceof IntegerLiteral) {
            return false;
        } else if (it instanceof NavigationExpression) {
            return  containsParametrizedQueryCallDispacher((NavigationExpression)it);
        } else if (it instanceof ParenthesizedExpression) {
            return containsParametrizedQueryCallDispacher((ParenthesizedExpression)it);
        } else if (it instanceof RawStringLiteral) {
            return false;
        } else if (it instanceof Self) {
            return false;
        } else if (it instanceof SpawnOperation) {
            return containsParametrizedQueryCallDispacher((SpawnOperation)it);
        } else if (it instanceof TernaryOperation) {
            return containsParametrizedQueryCallDispacher((TernaryOperation)it);
        } else if (it instanceof TimeLiteral) {
            return false;
        } else if (it instanceof TimeStampLiteral) {
            return false;
        } else if (it instanceof UnaryOperation) {
            return containsParametrizedQueryCallDispacher((UnaryOperation)it);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    Arrays.<Object>asList(it).toString());
        }
    }    
}
