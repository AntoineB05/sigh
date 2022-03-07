package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;


public final class CaseNode extends StatementNode
{
    public final ExpressionNode expression;
    public final StatementNode body;

    public CaseNode (Span span, Object expression, Object body) {
        super(span);
        this.expression = Util.cast(expression, ExpressionNode.class);
        this.body = Util.cast(body, StatementNode.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("case %s ...", expression.contents());

        return candidate.length() <= contentsBudget()
                ? candidate
                : "case (?) ...";
    }
}

