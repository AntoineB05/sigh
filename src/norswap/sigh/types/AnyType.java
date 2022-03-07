package norswap.sigh.types;

public final class AnyType extends Type
{
    public static final AnyType INSTANCE = new AnyType();
    private AnyType () {}

    @Override public String name() {
        return "Any";
    }
}
