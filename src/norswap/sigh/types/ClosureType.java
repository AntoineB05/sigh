package norswap.sigh.types;

public final class ClosureType extends Type {

    public final Type returnType;
    public final int paramNum;

    public ClosureType (Type returnType,int paramNum) {
        this.returnType = returnType;
        this.paramNum = paramNum;
    }

    @Override
    public String name () {
        return String.format("(%s parameters) -> %s", paramNum, returnType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClosureType)) return false;
        ClosureType other = (ClosureType) o;

        return returnType.equals(other.returnType)
            && paramNum==other.paramNum;
    }
}
