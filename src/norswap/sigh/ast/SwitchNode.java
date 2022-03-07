package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import java.util.List;

public final class SwitchNode extends StatementNode
{
    public final ExpressionNode expression;
    public final List<StatementNode> caseList;

    @SuppressWarnings("unchecked")
    public SwitchNode (Span span, Object expression, Object caseList) {
        super(span);
        this.expression = Util.cast(expression, ExpressionNode.class);
        this.caseList = Util.cast(caseList, List.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("switch(%s) ...", expression.contents());

        return candidate.length() <= contentsBudget()
                ? candidate
                : "switch(?) ...";
    }
}

