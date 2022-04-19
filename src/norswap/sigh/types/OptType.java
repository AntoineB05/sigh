package norswap.sigh.types;

public final class OptType extends Type
{
    public static final OptType INSTANCE = new OptType();
    private OptType () {}

    @Override public boolean isPrimitive () {
        return true;
    }

    @Override public String name() {
        return "Optional";
    }
}
