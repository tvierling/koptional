# Optional for Kotlin
> High-performance implementation of null-safe containers for Kotlin

One of the major goals of Kotlin is `null` safety. While Kotlin itself 
provides many primitive operations for safely handling `null` values and 
provides non-nullable data types, it is missing the present/not-present 
idiom available in FP and other languages.

`Optional` or `Option` are container types available in Java 8 and 
Scala, respectively. Both represent the concept of a "present" or 
"empty" container, with functional-programming operations to allow safe 
use of the contained data without `null` dereferences. However, these 
both suffer from performance issues, as they are full objects and 
participate in the object lifecycle.

Kotlin offers **inline classes**, a compile-time metaprogramming concept 
that treats a value as a different type than it has at the JVM/bytecode 
level. With judicious use of inline classes, we can use the `Optional` 
idiom on values that are really just references-or-`null`. 
Programmatically it works the same, but at the JVM level, (almost) no 
container objects get created.

## Why a new design?

Sure, `java.util.Optional` has existed since Java 8, and there's an 
`Option` type in Arrow. However, both of these implementations rely on 
being actual wrapper objects with type inheritance; they add nontrivial 
overhead.

Additionally, the Arrow `Option` is greatly overimplemented; it tries to 
fit into a larger functional-programming ecosystem. `Optional` here is 
intended to be standalone and have as few dependencies as possible.

`Optional` provides the same programming idiom with little to no added 
bytecode and runtime overhead, by making the Kotlin compiler (rather 
than the runtime) do almost all of the work.

## Design

`Optional`, as far as the runtime knows, doesn't exist. The value (or 
lack of value) contained inside is simply the original object reference, 
or `null`. When compiled, `Optional` operations basically reduce down to 
the same operations you'd use on nullable values, inserting null checks 
or safe-casts where appropriate. However, the code can be much easier to 
understand and less prone to error.

In addition to the basics of null safety operations, `Optional` also 
implements the `Set` interface (after all, it's a set of exactly zero or 
one items), so `Optional`s can be used in various places where an 
immutable collection is needed.

Because `Optional` can only include zero or one items, many collection 
operations become trivially inlined. For instance, `sorted` is obvious; 
either the set is empty or it's already sorted. So pre-existing 
collection code can be used as-is.
 
The functions on `Optional` are modeled after those available for 
`kotlin.Iterable` and `java.util.Optional` for familiarity. 
Additionally, a few operations using the `Option`, `Some`, and `None` 
idiom are available, modeled after Scala.

## Important notes

* As of this writing (Kotlin 1.3.70), Kotlin inline classes are still 
marked experimental. So while this implementation does work, it relies 
on a feature that isn't yet complete or blessed by the Kotlin 
maintainers.

* Usually in Kotlin, developers are discouraged from making a function 
`inline` if it doesn't handle lambdas, because the JIT will take care of 
inlining bytecode for common operations. However, `Optional` uses 
`inline` throughout for many operations because they are quite trivial. 
The result is bytecode that does `null` checks and safe-cast operations 
as if they were written that way in the original code.

* Interoperability with Java is tricky. Because of how inline classes 
work, the final type of an `Optional` is always erased to 
`java.lang.Object`. This is what shows up in function signatures. (Think 
of it as generic type erasure, but here it's just erasing the type `T` 
of the contained value.) Interoperability conversions with 
`java.util.Optional` are provided to reduce the chance of error.

* When passing an `Optional` to a function accepting a `Set`, 
`Collection`, or `Iterable`, the Kotlin compiler automatically generates 
a wrapper object to implement the interface. Inlining happens when 
working with the `Optional` type directly.

* An `Optional<primitivetype>` such as `Optional<Int>` results in one 
wrapper object on the JVM, as it will be converted to the boxed type 
`Integer` (Kotlin: `Int?`) in order to be nullable. However, this use 
case is highly optimized on the JVM, and differs from 
`java.util.Optional<Integer>` which creates **two** wrapper objects: 
`Integer` and `Optional`.

## Usage examples

### Construction and variable assignment

```kotlin
import org.duh.koptional.*

// Java style construction

val empty = Optional.empty()              // Optional<Nothing>
val alsoEmpty = Optional.ofNullable(null)
var oint = Optional.ofNullable(12345)     // Optional<Int>
var ostr = Optional.of("hello")           // Optional<String>

// Scala style construction

val empty = None             // Optional<Nothing>
val alsoEmpty = Option(null)
var oint = Option(12345)     // Optional<Int>
var ostr = Some("hello")     // Optional<String>

// If working with arbitrary maybe-nullable types, .asOptional is clearest

val anotherEmpty = null.asOptional // Optional<Nothing>
var ofloat: Optional<Number> =
    1234.56f.asOptional            // Optional<Float> declared as Optional<Number>

// Java interop

val javaOptional: java.util.Optional<Foo> = ...
val fromJava = javaOptional.asOptional    // Optional<Foo>
val toJava = Option("hello").asJOptional  // java.util.Optional<String>

// Reassignment and type compatibility

var oobj: Optional<Any> = empty
oobj = oint                  // succeeds, Optional is covariant
oobj = ostr                  // also succeeds

ostr = empty                 // succeeds, Optional<Nothing> is subtype of everything
ofloat = oint                // succeeds, we declared as Optional<Number>
ostr = oint                  // ERROR: String not compatible with Int
```

### Inspecting the object

Many constructs are available to inspect the present/empty status of `Optional` as well as perform actions on different conditions.

```kotlin
fun inspectExample(opt: Optional<Foo>, something: Foo) {
    // Java style presence
    if (opt.isPresent()) println("present")
    if (opt.isEmpty()) println("empty")

    // Scala style presence
    if (opt.isSome) println("present")
    if (opt.isNone) println("empty")

    // Collection style presence
    if (opt.any()) println("present")
    if (opt.none()) println("empty")

    // Collection "size": returns 1 if present, 0 if absent
    println(opt.count())
    println(opt.size)

    // if non-empty and contains this value
    if (opt.contains(something)) println("something")

    // same as above but 0 if present, -1 if not
    if (opt.indexOf(something) >= 0) println("something")

    // "any" returns condition result, or false if empty
    if (opt.any { it == something }) println("something")

    // "all" returns condition result, or true if empty
    if (opt.all { it == something }) println("something")

    // "none" returns false if condition matches,
    // and true if empty or condition does not match
    if (opt.none { it == something }) println("something")

    // "find" returns the actual contained value if condition matches,
    // and null if empty or condition does not match
    if (opt.find { it == something } != null) println("something")
}
```

### Using the contained data

Remember that `Optional` can be thought of as a set of zero-or-one 
items. So most extension functions you might expect of a `Set` (or 
`Collection` or `Iterable`) are available. Common collection functions 
that can be optimized for the `Optional` use case are inlined.

Most operations on the data within an `Optional` should be done using 
lambda processing blocks, rather than extracting the value directly, 
similarly to the use of Kotlin [scope 
functions](https://kotlinlang.org/docs/reference/scope-functions.html) 
on regular references.

In particular, `Optional` overloads `also`, `apply`, `let`, `run`, 
`takeIf`, and `takeUnless` to apply by default to the contained value, 
not the `Optional` itself; and rather than returning nullable values, 
they return instances of `Optional`.

```kotlin
fun useExample(nullstr: String?, optstr: Optional<String>) {
    // present-only

    nullstr?.let { println(it) }
    optstr.let { println(it) }

    if (nullstr != null) println(nullstr)
    optstr.ifPresent { println(it) }

    // forEach works, but is not preferred; see note below
    optstr.forEach { println(it) }

    // absent-only

    if (nullstr == null) println("is null")
    optstr.ifEmpty { println("is empty") }

    // lambda as expression
    // this is one case where null handling is more concise
    
    val maybeRegex = nullstr?.toRegex()        // type Regex?
    val optRegex = optstr.let { it.toRegex() } // type Optional<Regex>

    // receiver object as expression

    val lower = nullstr?.run { toRegex() } // type Regex?
    val olower = optstr.run { toRegex() }  // type Optional<Regex>

    // statements, return original object

    val printedStr = nullstr?.also { println(it) } // type String?
    val printedOpt = optstr.also { println(it) }   // type Optional<String>

    // receiver object statements, but return original object

    var rx: Optional<Regex> = None
    val regexedStr = nullstr?.apply { rx = Some(toRegex()) } // type String?
    val regexedOpt = optstr.apply { rx = Some(toRegex()) }   // type Optional<String>

    // takeIf (and takeUnless)

    val upper = nullstr?.takeIf { it.isNotEmpty() } // null if string empty
    val oupper = optstr.takeIf { it.isNotEmpty() }  // None if string empty
}
```
**A note about `forEach`:**

As of Kotlin 1.3, the extension function `Iterable.forEach` overrides 
the implementation in `Optional` due to a `@HidesMembers` annotation. 
Because of this, `Optional.forEach` is going to be slower than all the 
other possibilities here (it will create an `Iterator` and call 
functions on it rather than inlining the code).

I recommend useing `ifPresent` or `let` to avoid this issue. `ifPresent` 
is unambiguous, and `let` is more intuitive to experienced Kotlin 
programmers.

### ifPresent-orElse syntactic sugar

`java.util.Optional.ifPresentOrElse()` accepts two functions, one to run 
if the value is present, and one to run if it is empty. This function 
exists in `Optional` but can be cumbersome to use:

```kotlin
fun tellMeIfPresent(opt: Optional<String>): String {
    opt.ifPresentOrElse({
        return "present: $it"
    }, {
        return "empty"
    })
}
```

That's because Kotlin only supports bare lambdas with a function if 
there is exactly one such block as the final parameter. `Optional` 
offers an alternative that breaks up the function into two parts:

```kotlin
fun tellMeIfPresent(opt: Optional<String>): String {
    opt.ifPresent {
        return "present: $it"
    } orElse {
        return "empty"
    }
}
```

This provides a more natural `if-else` feel to the code, and is fully 
inlined: all of the above results in no calls into `Optional` support 
functions. (The `orElse` and following block can be omitted if all you 
want is the nonempty case.)

Braces are required (the blocks are actually lambdas) and the `orElse` 
keyword **must** be on the same line as the first closing brace. These 
will not work:

```kotlin
    opt.ifPresent
        return "present: $it"
        // ERROR: no braces for the block

    opt.ifPresent {
        return "present: $it"
    } // ERROR: orElse not on same line
    orElse {
        return "empty"
    }
```

### Providing alternatives if empty

In addition to the `ifEmpty` and `ifPresent-orElse` syntax, it's also 
possible to provide an alternative object if the `Optional` is empty: 
`or`. This function takes a lambda that provides an alternative 
`Optional` value, which may itself be empty. This operation works just 
like its `java.util.Optional` counterpart.

```kotlin
val optIsEmpty: Optional<String> = None

// optIsEmpty is empty, so the lambda replaces it
val optWithValue = optIsEmpty.or { Some("hello") }

// the lambda returns None so the result is still None
val optAlsoEmpty = optIsEmpty.or { None }

// optWithValue has a value, so the lambda is not executed
val optStillHasValue = optWithValue.or { throw MyException() }
```

### Functional data manipulation

Kotlin offers several idioms for altering data in a chain while keeping 
container type safety. These operations are also available on `Optional` 
in highly optimized forms.

**All** these functional-programming style operations return an empty 
`Optional` if it was already empty to begin with. The behaviors 
described below only happen if the `Optional` was non-empty to start.

```kotlin
fun filterExample(opt: Optional<Foo>, something: Foo) {
    // returns "opt" itself if condition is true, None if false
    val filtered = opt.filter { it == something }

    // returns "opt" itself if condition is false, None if true
    val filteredNot = opt.filterNot { it == something }

    // returns Optional<Bar> if value is of type Bar, None if not
    val barTyped = opt.filterIsInstance<Bar>()

    // Kotlin's equivalent for bare reference, returning null if not
    val somethingTyped = something as? Bar

    // this is a no-op; if it's non-empty it's also nonnull
    val sameAsOpt = opt.filterNotNull()
}

// mapping operations, all the below return Optional<String>
fun transformExample(opt: Optional<Foo>, something: Foo) {
    // flatMap lets you change the contained type, or replace with None
    val asString = opt.flatMap { Some(it.toString()) }
    val mightBeNone = opt.flatMap { 
        if (it == something) Some(it.toString()) else None
    }

    // map does the same with an unwrapped result, but requires non-null
    val asString2 = opt.map { it.toString() }

    // mapNotNull operates like flatMap, but takes an unwrapped result;
    // null results in returning None
    val mightBeNone2 = opt.mapNotNull {
        if (it == something) it.toString() else null
    }
}
```

Several forms of the above exist in `*To()` forms, e.g. 
`.mapNotNullTo()`, which take a `Collection` as the first parameter, and 
return the `Collection` itself. These are frequently useful for filling 
collections with data based on conditions.

### Extracting the contained data

Sometimes we need the data inside the `Optional` in a bare (possibly 
nullable) reference form. Multiple options are available.

```kotlin
fun extractExample(opt: Optional<Foo>, nullableFoo: Foo?, realFoo: Foo) {
    val foo = opt.let { it }      // type Foo?
    val foo2 = opt.run { this }   // type Foo?
    val fooref = opt.asReference  // type Foo?

    // Collection style extraction
    val foocoll = opt.singleOrNull() // type Foo?
    val foocollthrow = opt.single()  // type Foo, throws NoSuchElementException if empty

    // Java style extraction
    val fooelse = opt.orElse(nullableFoo) // type Foo?
    val fooelse2 = opt.orElse(null)       // type Foo?
    val fooelse3 = opt.orElse(realFoo)    // type Foo? (NOTE: nullable!)
    val fooelse4 = opt.orElseThrow()      // type Foo, throws NoSuchElementException if empty
    val fooget = opt.get()                // type Foo, throws NoSuchElementException if empty

    // Java style extraction with supplier lambda
    // this is the safest form to avoid nullability and exceptions
    val foosup = opt.orElseGet { realFoo } // type Foo
    val foosup2 = opt.orElseGet { Foo() }  // type Foo

    // Java style with a custom exception supplier
    val fooexc = opt.orElseThrow { MyException("oops I'm empty!") } // type Foo
}
```

## Development setup

This project is currently just an IntelliJ IDEA generated Gradle project 
for multiplatform Kotlin; it should get cleaned up in the future.

However, it already works on all Kotlin target platforms, and includes 
Java interoperability for the JVM target.

## Release History

* 0.1
    * Initial rough draft version

## Meta

[Todd Vierling](https://www.duh.org/) - also [@tvierling](https://twitter.com/tvierling) - tv@duh.org

Distributed under the BSD 2-clause license. See ``LICENSE`` for more 
information.

https://github.com/tvierling

## Contributing

1. Fork it (<https://github.com/yourname/yourproject/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes (`git commit -am 'Add some fooBar'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request
