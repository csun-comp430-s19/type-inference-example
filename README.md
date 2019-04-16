# Type Inference Example #

For this example, a new language called SimpleScala is introduced.
SimpleScala is a hybrid between Scala and Haskell, with the following features of note:

- Top-level function definitions with `def`, which require explicit parameter types (like Scala).
  Unlike Scala, an explicit return type is also required.
- Algebraic data types, like Haskell.
  New algebraic data types are introduced with the `algebraic` keyword.
- A restricted version of pattern matching, which does not support catch-all patterns or nested patterns.
- A special placeholder type, which, despite being in the syntax, cannot be used directly by programmers.
- Function arguments, local variables, and generic type specializations are completely inferred
- Tuples, but the only way to get values out of tuples is via pattern-matching.
  This makes inference easier, since it forces us to always know exactly how many fields a tuple has when we access it.
- String concatenation is a separate operator from integer addition.
  This makes inference easier, as otherwise we would have nondeterminism during typechecking (i.e., `e + e` could either return a string OR an integer, not both)
- Higher-order functions always take exactly one parameter, for simplicity.
  If more parameters are desired, a tuple can be taken.
  If fewer parameters are desired, unit can be passed as a dummy value.
- Constructors always take exactly one parameter, for simplicity.
  The same workarounds as for higher-order functions apply to constructors, as well.

## Syntax ##

```
x is a variable
str is a string
b is a boolean
i is an integer
n is a natural number
fn is a function name
cn is a constructor name
un is a user-defined type name
T is a type variable

p ::= placeholder n  // used internally during type inference; these denote
                     // types which may or may not be known at a given moment
type ::= string | boolean | integer  // usuals
         | unitType // common to functional programming; only one value of
                    // unitType exists, namely unit
         | type => type // higher-order functions
         | (type*) // tuples
         | un[type*] // user-defined types
         | T
         | p
op ::= + | - | * | `/` // typical arithmetic operators
       | && | `||` // typical boolean operators
       | < | <= // typical integer comparison operators
       | ++ // string concatenation
val ::= `val` x = exp // local variable definition
exp ::= x | str | b | i // the usuals
        | unit // only value of type unitType
        | exp op exp
        | x => exp // higher-order function creation
        | exp(exp) // calling a higher-order function
        | fn(exp) // calling a top-level function
        | if (exp) exp else exp
        | {val* e} // blocks
        | (exp*) // tuple creation
        | cn(exp) // algebraic data type constructor
        | exp match {case*} // pattern matching
case ::= `case` cn(x) => exp // constructor pattern
         | `case` (x*) => exp // tuple pattern
tdef ::= algebraic un[T*] = cdef* // algebraic data type declaration
cdef ::= cn(type) // constructor declaration
def ::= `def` fn[T*](x: type): type = exp // top-level function definition
program ::= tdef* def* exp
```

## Formalism ##

A mathematical formalism of how this typechecker works has been provided in `formalism.pdf`.
It is not expected that you will be able to understand this in one sitting, but it's incredibly concise relative to the code.

## Code ##

The typechecker for SimpleScala has been implemented in both Scala and Java.
The Java-based implementation is a very direct translation from the Scala-based implementation, and may be difficult to understand as a result.
A s-expression-based parser has also been provided for both implementations.
For the typechecker code, see:

- Scala: `scala/src/main/scala/polytyped/typechecker.scala`
- Java: `java/simplescala_experimentation/src/main/java/simplescala_experimentation/typechecker/Typechecker.java`

### Testing ###

The code was originally tested via an experimental automated approach.
While it mostly lacks a traditional unit test suite, I am highly confident that it is correct.

## Basic Idea ##

With type inference, the basic idea is that we can _infer_ the type of values used, based on context clues from the program.
For example, consider the following code snippet in Scala, which features type inference:

```scala
val x = 42
```

This snippet defines a local variable `x`, and assigns it the value `42`.
The Scala compiler knows that `x` is of type `Int` (i.e., `x` is an integer), because `42` is an integer.
There is no need to explicitly say that `x` is an integer; the Scala compiler infers it.

As another example, consider the following:

```
// Java-like class definition
class Foo<A> {
  public final A a;
  public Foo(final A a) {
    this.a = a;
  }
}
...
// Scala-like variable definition
val f = new Foo(7)
```

In this case, the Scala compiler knows that `f` is of type `Foo<Int>`; there is no need to provide any annotations.

## How Does Inference Work? ##

As humans, we're pretty good at figuring out the context clues.
However, the compiler has to take a more incremental, stepwise approach.
To do this, the compiler introduces the notion of a type which isn't known yet.
In SimpleScala, this is what _placeholders_ are for: they maintain that _something_ is a type, but we might not know exactly what type yet.
As values with placeholder types are used, we effectively "fill-in" the placeholder with information as we find it.

To show an example, let's consider again the following code snippet:

```scala
val f = new Foo(7)
```

Going from left to right, the compiler does the following:

1. At `val f`, it knows that `f` is of _some_ yet-unknown type.
   It gives `f` a placeholder type, which will be filled in later.
   For discussion, we will call this placeholder `p0`.
2. At `=`, it knows that the type of `f` must be the same as the type of the expression to the right of `=`.
   As such, the compiler states that both `f` and the expression are of the _same_ placeholder type, i.e., both are of type `p0`.
3. The compiler sees `new Foo`.
   This expression creates something of type `Foo<p1>`, where `p1` is a _different_ placeholder.
   The fact that `p1` is used comes from the definition of `Foo`, which took a type variable.
   We don't know exactly what `Foo` is going to take at this point, so we use a placeholder here.
   Putting it all together, from the prior reasoning with `=`, the compiler learns that `p0 = Foo<p1>`.
4. The compiler sees `7`.
   From the definition of `Foo`, the parameter to the constructor has the same type as the generic type held in `Foo` (i.e., they both take something of type `A`).
   As such, `p1` must be of the same type as whatever was passed in the constructor, so `p1 = Int`.
5. Putting everything together, the type of `f` is `p0`, `p0 = Foo<p1>`, and `p1 = Int`.
   Substituting variables for their values, we ultimately get that `f` is of type `Foo<Int>`.

This setup makes use of _unification_, which is more commonly seen in logic programming languages like Prolog.
Each placeholder acts as a logical variable, and these logical variables/placeholders are filled in later with type information.
As with unification, once a variable gets a value, it can never be replaced by a different value.

Type errors can still occur when type inference is in play.
For example, consider the following:

```
// Java-like class definition
class Bar<A> {
  public Bar(A first, A second) { ... }
}
...
// Scala-like variable definition
val b = new Bar(7, "foo")
```

As humans, we can see a problem: `Bar` is supposed to take two values of the same type, but here we take two values of _different_ types.
This causes a type error.
From the compiler's standpoint, it will figure out there is a type error via the following stepwise process:

1. From `val b`, the compiler doesn't know what type `b` is yet.
   It assigns it a placeholder type, so `b` is of type `p0`.
2. From `=`, it knows that the expression must be of the same type as `b`.
3. From `new Bar`, it knows that there is something of type `Bar<p1>`, where `p1` is a new placeholder.
   Putting this all together, `p0 = Bar<p1>`.
4. From `7`, it knows that `p1 = Int`.
5. From `"foo"`, the compiler _wants_ to assign `p1 = String`.
   However, `p1` already has a type, and that type isn't `String`.
   As such, the compiler fails to typecheck at this point.

This situation plays out differently if no type error were present, like so:

```scala
val b = new Bar(7, 8)
```

Steps 1-4 are the same as before.
However, at step 5, the compiler sees `8` instead of `"foo"`.
The compiler sees that `p1 = Int`, and it's trying to assign `Int` again to `p1`.
While there is already a type, that type is the same as the type we attempt to put into it, which is ok as far as unification is concerned.
As such, this program snippet is well-typed, and it ultimately determines that `b` is of type `Bar<Int>` (`b` is of type `p0`, `p0 = Bar<p1>`, and `p1 = Int`).
