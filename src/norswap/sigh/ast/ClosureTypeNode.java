package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class ClosureTypeNode extends TypeNode {

    public final TypeNode returnType;
    public final List<TypeNode> paramTypes;
    @SuppressWarnings("unchecked")
    public ClosureTypeNode (Span span, Object paramTypes, Object returnType) {
        super(span);
        this.returnType = Util.cast(returnType, TypeNode.class);
        this.paramTypes = Util.cast(paramTypes, List.class);
    }

    @Override
    public String contents () {
        return returnType.contents();
    }
}
