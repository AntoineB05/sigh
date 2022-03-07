package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class BinaryExpressionTypeCheckNode extends ExpressionNode
{
    public final ExpressionNode left;
    public final TypeNode right;
    public final BinaryOperator operator;

    public BinaryExpressionTypeCheckNode (Span span, Object left, Object operator, Object right) {
        super(span);
        this.left = Util.cast(left, ExpressionNode.class);
        this.right = Util.cast(right, TypeNode.class);
        this.operator = Util.cast(operator, BinaryOperator.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s %s %s",
                left.contents(), operator.string, right.contents());

        return candidate.length() <= contentsBudget()
                ? candidate
                : String.format("(?) %s (?)", operator.string);
    }
}
