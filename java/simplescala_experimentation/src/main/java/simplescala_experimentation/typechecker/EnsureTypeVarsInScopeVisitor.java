package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.*;
import simplescala_experimentation.syntax.*;

public class EnsureTypeVarsInScopeVisitor implements TypeVisitor<JavaUnit, JavaUnit, IllTyped> {
    public final ImmutableSet<TypeVariable> inScope;

    public EnsureTypeVarsInScopeVisitor(final ImmutableSet<TypeVariable> inScope) {
        this.inScope = inScope;
    }

    private JavaUnit visitTypes(final ImmutableList<Type> types, final JavaUnit down) throws IllTyped {
        for (final Type type : types) {
            type.visitType(this, down);
        }
        return down;
    }

    public JavaUnit visit(final StringType t, final JavaUnit down) throws IllTyped { return down; }
    public JavaUnit visit(final BooleanType t, final JavaUnit down) throws IllTyped { return down; }
    public JavaUnit visit(final IntegerType t, final JavaUnit down) throws IllTyped { return down; }
    public JavaUnit visit(final UnitType t, final JavaUnit down) throws IllTyped { return down; }
    public JavaUnit visit(final FunctionType t, final JavaUnit down) throws IllTyped {
        t.paramType.visitType(this, down);
        t.returnType.visitType(this, down);
        return down;
    }
    public JavaUnit visit(final TupleType t, final JavaUnit down) throws IllTyped {
        visitTypes(t.types, down);
        return down;
    }
    public JavaUnit visit(final UserType t, final JavaUnit down) throws IllTyped {
        visitTypes(t.types, down);
        return down;
    }
    public JavaUnit visit(final TypeVariableType t, final JavaUnit down) throws IllTyped {
        if (inScope.contains(t.tv)) {
            return down;
        } else {
            throw new IllTyped();
        }
    }
    public JavaUnit visit(final TypePlaceholder t, final JavaUnit down) throws IllTyped {
        return down;
    }
}
