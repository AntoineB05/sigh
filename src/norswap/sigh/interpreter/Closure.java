package norswap.sigh.interpreter;

import norswap.sigh.ast.ClosureExpressionNode;
import norswap.sigh.scopes.Scope;

public final class Closure {
    public final ClosureExpressionNode expression;
    public ScopeStorage storage;


    public Closure (ClosureExpressionNode expression, ScopeStorage storage) {
        this.expression = expression;
        this.storage = storage;
    }

    @Override public int hashCode () {
        return 31 * expression.hashCode() + 1;
    }

    @Override public boolean equals (Object other) {
        return other instanceof Closure && ((Closure) other).expression== expression;
    }
}
