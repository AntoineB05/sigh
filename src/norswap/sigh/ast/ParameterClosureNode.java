package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class ParameterClosureNode extends DeclarationNode {

    public final String name;

    public ParameterClosureNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override
    public String name () {
        return name;
    }

    @Override
    public String declaredThing () {
        return "parameterClosure";
    }

    @Override
    public String contents () {
        return name;
    }
}
