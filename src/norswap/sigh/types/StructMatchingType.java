package norswap.sigh.types;

public class StructMatchingType extends Type {
    public static final StructMatchingType INSTANCE = new StructMatchingType();
    private StructMatchingType () {}

    @Override public String name() {
        return "StructMatching";
    }
}
