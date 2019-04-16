package simplescala_experimentation.syntax;

public class FunctionType implements Type {
    public final Type paramType;
    public final Type returnType;

    public FunctionType(final Type paramType, final Type returnType) {
        this.paramType = paramType;
        this.returnType = returnType;
    }
    
    public <D, U, E extends Exception> U visitType(final TypeVisitor<D, U, E> visitor, D down) throws E {
        return visitor.visit(this, down);
    }

    public boolean equals(final Object other) {
        if (other instanceof FunctionType) {
            final FunctionType ft = (FunctionType)other;
            return (ft.paramType.equals(paramType) &&
                    ft.returnType.equals(returnType));
        } else {
            return false;
        }
    }

    public String toString() {
        return ("(" + paramType.toString() +
                " => " + returnType.toString() +
                ")");
    }
                
}
