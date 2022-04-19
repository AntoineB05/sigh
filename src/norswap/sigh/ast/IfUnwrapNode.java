package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class IfUnwrapNode extends StatementNode
{
    public final VarDeclarationNode varDecl;
    public final StatementNode trueStatement;
    public final StatementNode falseStatement;

    public IfUnwrapNode (Span span, Object varDecl, Object trueStatement, Object falseStatement) {
        super(span);
        this.varDecl = Util.cast(varDecl, VarDeclarationNode.class);
        this.trueStatement = Util.cast(trueStatement, StatementNode.class);
        this.falseStatement = falseStatement == null
            ? null
            : Util.cast(falseStatement, StatementNode.class);
    }

    @Override public String contents ()
    {
        String candidate = falseStatement == null
            ? String.format("if var %s = %s ...", varDecl.name,varDecl.initializer.contents())
            : String.format("if var %s = %s ... else ...", varDecl.name,varDecl.initializer.contents());

        return candidate.length() <= contentsBudget()
            ? candidate
            : falseStatement == null
            ? "if (?) ..."
            : "if (?) ... else ...";
    }
}
