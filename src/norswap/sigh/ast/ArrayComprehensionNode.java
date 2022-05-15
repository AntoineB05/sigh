package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class ArrayComprehensionNode extends ExpressionNode{
    public final VarDeclarationNode localVar;
    public final ExpressionNode expression;
    public final ExpressionNode list;
    public final ExpressionNode condition;

    @SuppressWarnings("unchecked")
    public ArrayComprehensionNode (Span span, Object localVar, Object expression, Object list, Object condition) {
        super(span);
        this.localVar = Util.cast(localVar, VarDeclarationNode.class);
        this.expression = Util.cast(expression, ExpressionNode.class);
        this.list = Util.cast(list, ExpressionNode.class);
        if(condition != null){
            this.condition = Util.cast(condition, ExpressionNode.class);
        }else {
            this.condition = null;
        }
    }

    @Override public String contents () {
        return "List comprenhension Node";
    }
}
