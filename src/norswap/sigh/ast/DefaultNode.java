package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;


public final class DefaultNode extends StatementNode
{
    public final StatementNode body;

    public DefaultNode (Span span, Object body) {
        super(span);
        this.body = Util.cast(body, StatementNode.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("default ...");

        return candidate.length() <= contentsBudget()
                ? candidate
                : "default ...";
    }
}

