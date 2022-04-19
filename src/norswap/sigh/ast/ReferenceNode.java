package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ReferenceNode extends ExpressionNode
{
    public final String name;
    public Boolean unwrap;

    public ReferenceNode (Span span, Object name, Boolean unwrap) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.unwrap = unwrap;
    }

    @Override public String contents() {
        return name;
    }
}
