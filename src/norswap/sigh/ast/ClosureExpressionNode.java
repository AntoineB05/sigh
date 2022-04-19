package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.sigh.interpreter.ScopeStorage;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.List;

public class ClosureExpressionNode extends ExpressionNode{

    public final BlockNode block;
    public final List<ParameterClosureNode> arguments;


    public ClosureExpressionNode (Span span, Object arguments, Object block) {
        super(span);
        this.arguments = arguments==null ? new ArrayList() :  Util.cast(arguments, List.class);
        this.block = Util.cast(block, BlockNode.class);
    }

    @Override
    public String contents () {
        return "closure";
    }
}
