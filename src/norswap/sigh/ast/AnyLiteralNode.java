package norswap.sigh.ast;

import norswap.autumn.positions.Span;

public class AnyLiteralNode extends ExpressionNode{
    public AnyLiteralNode (Span span) {
        super(span);
    }

    @Override public String contents() {
        return "_";
    }
}
