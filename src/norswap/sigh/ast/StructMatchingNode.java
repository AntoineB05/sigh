package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import java.util.List;

public class StructMatchingNode extends ExpressionNode{

    public final List<ExpressionNode> fields;

    @SuppressWarnings("unchecked")
    public StructMatchingNode (Span span, Object fields) {
        super(span);
        this.fields = Util.cast(fields, List.class);
    }


    @Override public String contents () {
        return "(...,...) ";
    }

}
