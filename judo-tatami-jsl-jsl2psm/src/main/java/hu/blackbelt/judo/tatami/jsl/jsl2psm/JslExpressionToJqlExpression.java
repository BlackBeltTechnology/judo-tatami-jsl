package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.judo.meta.jsl.jsldsl.BinaryOperation;
import hu.blackbelt.judo.meta.jsl.jsldsl.BooleanLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.CreateExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.DateLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.DecimalLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.EscapedStringLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.Expression;
import hu.blackbelt.judo.meta.jsl.jsldsl.Feature;
import hu.blackbelt.judo.meta.jsl.jsldsl.Function;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionCall;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.FunctionedExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.IntegerLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.NavigationExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.ParenthesizedExpression;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryParameter;
import hu.blackbelt.judo.meta.jsl.jsldsl.RawStringLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.Self;
import hu.blackbelt.judo.meta.jsl.jsldsl.SpawnOperation;
import hu.blackbelt.judo.meta.jsl.jsldsl.TernaryOperation;
import hu.blackbelt.judo.meta.jsl.jsldsl.TimeLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.TimeStampLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.UnaryOperation;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class JslExpressionToJqlExpression {
    /**
     * Expression returns Expression hidden(WS, CONT_NL, SL_COMMENT, ML_COMMENT)
     * : SwitchExpression
     * ;
     */
    public static String getJql(final Expression it) {
        return JslExpressionToJqlExpression.getJqlDispacher(it);
    }

    /**
     * // right associative rule
     * SwitchExpression returns Expression
     * : ImpliesExpression (=> ({TernaryOperation.condition=current} '?')
     * thenExpression=SwitchExpression ':'
     * elseExpression=SwitchExpression)?
     * ;
     */
    protected static String _getJqlDispacher(final TernaryOperation it) {
        return it != null
                ? getJql(it.getCondition()) + '?' + getJql(it.getThenExpression()) + ':' + getJql(it.getElseExpression())
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
    protected static String _getJqlDispacher(final BinaryOperation it) {
        return it != null
                ? getJql(it.getLeftOperand()) + it.getOperator() + getJql(it.getRightOperand())
                : null;
    }

    /**
     * SpawnOperation returns Expression
     * : UnaryOperation (=> ({SpawnOperation.operand=current} 'as' type=LocalName))?
     * ;
     */
    protected static String _getJqlDispacher(final SpawnOperation it) {
        return it != null
                ? getJql(it.getOperand()) + " as " + it.getType()
                : null;
    }


    /**
     * UnaryOperation returns Expression
     * : {UnaryOperation} operator=('not' | '-') operand=FunctionedExpression
     * | FunctionedExpression
     * ;
     */
    protected static String _getJqlDispacher(final UnaryOperation it) {
        return it != null
                ? it.getOperator() + getJql(it.getOperand())
                : null;
    }

    /**
     * FunctionedExpression returns Expression
     * : NavigationExpression ({FunctionedExpression.operand=current} functionCall=FunctionCall)?
     * ;
     */
    protected static String _getJqlDispacher(final FunctionedExpression it) {
        return it != null
                ? getJql(it.getOperand()) + getJql(it.getFunctionCall())
                : null;
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
    protected static String _getJqlDispacher(final NavigationExpression it) {
        return it != null
                ? it.getBase() != null
                ? getJql(it.getBase()) + it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining())
                : it.getQName() + (it.getEnumValue() != null
                ? "#" + it.getEnumValue()
                : it.getFeatures().stream().map(p -> getJql(p)).collect(Collectors.joining()))
                : "";
    }

    /**
     * FunctionCall
     * : {FunctionCall} '!' function=Function features+=Feature* call=FunctionCall?
     * ;
     */
    public static String getJql(final FunctionCall it) {
        return it != null
                ? '!' + getJql(it.getFunction()) +
                (
                        it.getCall() != null
                                ? getJql(it.getCall())
                                : ""
                )
                : null;
    }

    /**
     * Feature
     * : {Feature} '.' name=ID ('(' parameters+=QueryParameter (',' parameters+=QueryParameter)* ')')?
     * ;
     */
    public static String getJql(final Feature it) {
        return it != null
                ? '.' + it.getName() +
                (
                        it.getParameters().size() > 0
                                ? "(" +  it.getParameters().stream().map(p -> getJql(p)).collect(Collectors.joining(",")) + ")"
                                : ""
                )
                : null;
    }

    /**
     * QueryParameter
     * : name = ID '=' expression=Expression
     * ;
     */
    public static String getJql(final QueryParameter it) {
        return it != null
                ? it.getName() + "=" + getJql(it.getExpression())
                : null;
    }

    /**
     * ParenthesizedExpression returns Expression
     * : '(' Expression ')'
     * ;
     */
    protected static String _getJqlDispacher(final ParenthesizedExpression it) {
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
    protected static String _getJqlDispacher(final CreateExpression it) {
        return it != null
                ?  ""
                : null;
    }

    /**
     * Function returns Function
     * : name=ID '(' (lambdaArgument=ID '|')? (parameters+=FunctionParameter (',' parameters+=FunctionParameter)*)? ')'
     * ;
     */
    public static String getJql(final Function it) {
        return it != null
                ? it.getName() + '(' +
                (
                        it.getLambdaArgument() != null
                                ? it.getLambdaArgument() + "|"
                                : ""
                ) + it.getParameters().stream().map(p -> getJql(p)).collect(Collectors.joining(",")) + ")"
                : null;
    }

    /**
     * FunctionParameter
     * : {FunctionParameter} expression=Expression
     * ;
     */
    public static String getJql(final FunctionParameter it) {
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
    protected static String _getJqlDispacher(final DateLiteral it) {
        return it != null
                ? "`" + it.getValue() + "`"
                : null;
    }

    protected static String _getJqlDispacher(final TimeStampLiteral it) {
        return it != null
                ? "`" + it.getValue() + "`"
                : null;
    }

    protected static String _getJqlDispacher(final TimeLiteral it) {
        return it != null
                ? "`" + it.getValue() + "`"
                : null;
    }

    protected static String _getJqlDispacher(final RawStringLiteral it) {
        return it != null
                ? "\"" + StringEscapeUtils.escapeJava(it.getValue()) + "\""
                : null;
    }

    protected static String _getJqlDispacher(final EscapedStringLiteral it) {
        return it != null
                ? "\"" + it.getValue() + "\""
                : null;
    }

    protected static String _getJqlDispacher(final DecimalLiteral it) {
        return it != null
                ? it.getValue().toString()
                : null;
    }

    protected static String _getJqlDispacher(final IntegerLiteral it) {
        return it != null
                ? it.getValue().toString()
                : null;
    }

    protected static String _getJqlDispacher(final BooleanLiteral it) {
        return it != null
                ? Boolean.toString(it.isIsTrue())
                : null;
    }

    /**
     * Self returns Expression
     * : {Self} 'self'
     * ;
     */
    protected static String _getJqlDispacher(final Self it) {
        return it != null
                ?  "self"
                : null;
    }

    public static String getJqlDispacher(final Expression it) {
        if (it instanceof BinaryOperation) {
            return _getJqlDispacher((BinaryOperation)it);
        } else if (it instanceof BooleanLiteral) {
            return _getJqlDispacher((BooleanLiteral)it);
        } else if (it instanceof CreateExpression) {
            return _getJqlDispacher((CreateExpression)it);
        } else if (it instanceof DateLiteral) {
            return _getJqlDispacher((DateLiteral)it);
        } else if (it instanceof DecimalLiteral) {
            return _getJqlDispacher((DecimalLiteral)it);
        } else if (it instanceof EscapedStringLiteral) {
            return _getJqlDispacher((EscapedStringLiteral)it);
        } else if (it instanceof FunctionedExpression) {
            return _getJqlDispacher((FunctionedExpression)it);
        } else if (it instanceof IntegerLiteral) {
            return _getJqlDispacher((IntegerLiteral)it);
        } else if (it instanceof NavigationExpression) {
            return _getJqlDispacher((NavigationExpression)it);
        } else if (it instanceof ParenthesizedExpression) {
            return _getJqlDispacher((ParenthesizedExpression)it);
        } else if (it instanceof RawStringLiteral) {
            return _getJqlDispacher((RawStringLiteral)it);
        } else if (it instanceof Self) {
            return _getJqlDispacher((Self)it);
        } else if (it instanceof SpawnOperation) {
            return _getJqlDispacher((SpawnOperation)it);
        } else if (it instanceof TernaryOperation) {
            return _getJqlDispacher((TernaryOperation)it);
        } else if (it instanceof TimeLiteral) {
            return _getJqlDispacher((TimeLiteral)it);
        } else if (it instanceof TimeStampLiteral) {
            return _getJqlDispacher((TimeStampLiteral)it);
        } else if (it instanceof UnaryOperation) {
            return _getJqlDispacher((UnaryOperation)it);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: " +
                    Arrays.<Object>asList(it).toString());
        }
    }
}
