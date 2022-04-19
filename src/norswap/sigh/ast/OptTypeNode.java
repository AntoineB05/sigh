package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class OptTypeNode extends TypeNode{
    public final TypeNode contentType;

    public OptTypeNode (Span span, Object contentType) {
        super(span);
        this.contentType = new SimpleTypeNode(span,Util.cast(contentType, String.class));
    }

    @Override public String contents () {
        return "Optionnal with content " + contentType.contents();
    }
}
