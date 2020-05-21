/*
 * Copyright (c) 2020 Todd Vierling <tv@duh.org> <tv@pobox.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY 
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
@file:Suppress("DEPRECATION", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "UNCHECKED_CAST", "UNUSED_PARAMETER")

package org.duh.koptional

import kotlin.jvm.JvmName

/**
 * A container that indicates whether a value is present or absent.
 */
inline class Optional<out T : Any>
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS") // KT-28056
@PublishedApi internal constructor(@PublishedApi internal val value: Any?) : Set<T> {
    /* Iterable/Collection/Set operations */

    inline fun all(predicate: (T) -> Boolean): Boolean =
        (value == null || predicate(value as T))

    inline fun any(): Boolean = (value != null)

    inline fun any(predicate: (T) -> Boolean): Boolean =
        (value != null && predicate(value as T))

    inline fun asIterable(): Optional<T> = this

    fun asSequence(): Sequence<T> = Sequence { iterator() }

    override inline operator fun contains(element: @UnsafeVariance T): Boolean =
        (value != null && value == element)

    override fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean {
        if (value == null)
            return elements.isEmpty()

        for (e in elements)
            if (e != value)
                return false

        return true
    }

    inline fun count(): Int =
        if (value != null) 1 else 0

    inline fun count(predicate: (T) -> Boolean): Int =
        if (value != null && predicate(value as T)) 1 else 0

    inline fun distinct(): Optional<T> = this

    inline fun <K> distinctBy(selector: (T) -> K): Optional<T> {
        if (value != null)
            selector(value as T) // simulate "selecting"

        return this
    }

    fun drop(n: Int): Optional<T> {
        require(n >= 0) { "Requested element count $n is less than zero." }

        if (value == null || n < 1)
            return this

        return Optional(null)
    }

    inline fun dropWhile(predicate: (T) -> Boolean): Optional<T> =
        if (value == null || !predicate(value as T)) this else Optional(null)

    fun elementAt(index: Int): T {
        if (index != 0 || value == null)
            throw IndexOutOfBoundsException()

        return value as T
    }

    inline fun elementAtOrElse(index: Int, defaultValue: (Int) -> @UnsafeVariance T): T =
        if (value != null && index == 0) (value as T) else defaultValue(index)

    inline fun elementAtOrNull(index: Int): T? =
        if (index == 0) (value as T?) else null

    /* also in java.util.Optional */
    inline fun filter(predicate: (T) -> Boolean): Optional<T> =
        if (value == null || predicate(value as T)) this else Optional(null)

    inline fun filterIndexed(predicate: (index: Int, T) -> Boolean): Optional<T> =
        if (value == null || predicate(0, value as T)) this else Optional(null)

    inline fun <C : MutableCollection<in T>> filterIndexedTo(
        destination: C,
        predicate: (index: Int, T) -> Boolean
    ): C {
        if (value != null && predicate(0, value as T))
            destination.add(value)

        return destination
    }

    inline fun <reified R : Any> filterIsInstance(): Optional<R> =
        if (value is R) Optional(value) else Optional(
            null
        )

    inline fun <reified R : Any, C : MutableCollection<in R>> filterIsInstanceTo(destination: C): C {
        if (value != null && value is R)
            destination.add(value)

        return destination
    }

    inline fun filterNot(predicate: (T) -> Boolean): Optional<T> =
        if (value == null || !predicate(value as T)) this else Optional(null)

    inline fun filterNotNull(): Optional<T> = this

    inline fun <C : MutableCollection<in T>> filterNotNullTo(destination: C): C {
        if (value != null)
            destination.add(value as T)

        return destination
    }

    inline fun <C : MutableCollection<in T>> filterNotTo(
        destination: C,
        predicate: (T) -> Boolean
    ): C {
        if (value != null && !predicate(value as T))
            destination.add(value)

        return destination
    }

    inline fun <C : MutableCollection<in T>> filterTo(
        destination: C,
        predicate: (T) -> Boolean
    ): C {
        if (value != null && predicate(value as T))
            destination.add(value)

        return destination
    }

    inline fun find(predicate: (T) -> Boolean): T? =
        if (value != null && predicate(value as T)) value else null

    inline fun findLast(predicate: (T) -> Boolean): T? =
        if (value != null && predicate(value as T)) value else null

    fun first(): T = (value as T?) ?: throw NoSuchElementException()

    inline fun first(predicate: (T) -> Boolean): T =
        if (value != null && predicate(value as T)) value else throw NoSuchElementException()

    inline fun firstOrNull(): T? = (value as T?)

    inline fun firstOrNull(predicate: (T) -> Boolean): T? =
        if (value != null && predicate(value as T)) value else null

    /* specialized case also in java.util.Optional */
    inline fun <R : Any> flatMap(transform: (T) -> Optional<R>): Optional<R> =
        if (value != null) transform(value as T) else Optional(null)

    inline fun <R> fold(initial: R, operation: (acc: R, T) -> R): R {
        var res = initial
        if (value != null)
            res = operation(res, value as T)
        return res
    }

    inline fun <R> foldIndexed(initial: R, operation: (index: Int, acc: R, T) -> R): R {
        var res = initial
        if (value != null)
            res = operation(0, res, value as T)
        return res
    }

    //@kotlin.internal.HidesMembers
    inline fun forEach(action: (T) -> Unit) {
        if (value != null)
            action(value as T)
    }

    inline fun forEachIndexed(action: (index: Int, T) -> Unit) {
        if (value != null)
            action(0, value as T)
    }

    inline fun indexOf(element: @UnsafeVariance T): Int =
        if (value != null && value == element) 0 else -1

    inline fun indexOfFirst(predicate: (T) -> Boolean): Int =
        if (value != null && predicate(value as T)) 0 else -1

    inline fun indexOfLast(predicate: (T) -> Boolean): Int =
        if (value != null && predicate(value as T)) 0 else -1

    /* also in java.util.Optional */
    override inline fun isEmpty(): Boolean = (value == null)

    override fun iterator(): Iterator<T> = OptionalIterator<T>(value as T?)

    fun last(): T = (value as T?) ?: throw NoSuchElementException()

    inline fun last(predicate: (T) -> Boolean): T =
        if (value != null && predicate(value as T)) value else throw NoSuchElementException()

    inline fun lastIndexOf(element: @UnsafeVariance T): Int =
        if (value != null && value == element) 0 else -1

    inline fun lastOrNull(): T? = (value as T?)

    inline fun lastOrNull(predicate: (T) -> Boolean): T? =
        if (value != null && predicate(value as T)) value else null

    /* also in java.util.Optional */
    inline fun <R : Any> map(transform: (T) -> R): Optional<R> =
        if (value != null) Optional(transform(value as T)) else Optional(
            null
        )

    inline fun <R : Any> mapIndexed(transform: (index: Int, T) -> R): Optional<R> =
        if (value != null) Optional(
            transform(
                0,
                value as T
            )
        ) else Optional(null)

    inline fun <R : Any> mapIndexedNotNull(transform: (index: Int, T) -> R?): Optional<R> {
        if (value != null) {
            val r = transform(0, value as T)
            if (r != null)
                return Optional(r)
        }

        return Optional(null)
    }

    inline fun <R : Any, C : MutableCollection<in R>> mapIndexedNotNullTo(
        destination: C,
        transform: (index: Int, T) -> R?
    ): C {
        if (value != null) {
            val r = transform(0, value as T)
            if (r != null)
                destination.add(r)
        }

        return destination
    }

    inline fun <R, C : MutableCollection<in R>> mapIndexedTo(
        destination: C,
        transform: (index: Int, T) -> R
    ): C {
        if (value != null)
            destination.add(transform(0, value as T))

        return destination
    }

    inline fun <R : Any> mapNotNull(transform: (T) -> R?): Optional<R> {
        if (value != null) {
            val r = transform(value as T)
            if (r != null)
                return Optional(r)
        }

        return Optional(null)
    }

    inline fun <R : Any, C : MutableCollection<in R>> mapNotNullTo(
        destination: C,
        transform: (T) -> R?
    ): C {
        if (value != null) {
            val r = transform(value as T)
            if (r != null)
                destination.add(r)
        }

        return destination
    }

    inline fun <R, C : MutableCollection<in R>> mapTo(
        destination: C,
        transform: (T) -> R
    ): C {
        if (value != null)
            destination.add(transform(value as T))

        return destination
    }

    inline fun <R : Comparable<R>> maxBy(selector: (T) -> R): T? = (value as T?)

    inline fun maxWith(comparator: Comparator<in T>): T? = (value as T?)

    inline fun <R : Comparable<R>> minBy(selector: (T) -> R): T? = (value as T?)

    inline fun minWith(comparator: Comparator<in T>): T? = (value as T?)

    inline fun none(): Boolean = (value == null)

    inline fun none(predicate: (T) -> Boolean): Boolean =
        (value == null || !predicate(value as T))

    inline fun onEach(action: (T) -> Unit): Optional<T> {
        if (value != null)
            action(value as T)

        return this
    }

    inline fun requireNoNulls(): Optional<T> = this

    inline fun reversed(): Optional<T> = this

    inline fun shuffled(): Optional<T> = this

    inline fun shuffled(random: kotlin.random.Random): Optional<T> = this

    fun single(): T = (value as T?) ?: throw NoSuchElementException()

    fun single(predicate: (T) -> Boolean): T =
        if (value != null && predicate(value as T)) value else throw NoSuchElementException()

    inline fun singleOrNull(): T? = (value as T?)

    inline fun singleOrNull(predicate: (T) -> Boolean): T? =
        if (value != null && predicate(value as T)) value else null

    override val size: Int
        inline get() = if (value != null) 1 else 0

    inline fun <R : Comparable<R>> sortedBy(selector: (T) -> R?): Optional<T> = this

    inline fun <R : Comparable<R>> sortedByDescending(selector: (T) -> R?): Optional<T> = this

    inline fun sortedWith(comparator: Comparator<in T>): Optional<T> = this

    inline fun take(n: Int): Optional<T> {
        require(n >= 0) { "Requested element count $n is less than zero." }

        if (value == null || n >= 1)
            return this

        return Optional(null)
    }

    inline fun takeWhile(predicate: (T) -> Boolean): Optional<T> =
        if (value == null || predicate(value as T)) this else Optional(null)

    inline fun <C : MutableCollection<in T>> toCollection(destination: C): C {
        if (value != null)
            destination.add(value as T)

        return destination
    }

    fun toList(): List<T> =
        if (value != null) listOf(value as T) else emptyList()

    inline fun toSet(): Optional<T> = this

    override fun toString(): String =
        if (value != null) "Some($value)" else "None"

    /* Some/None style methods */

    val isNone: Boolean
        inline get() = (value == null)

    val isSome: Boolean
        inline get() = (value != null)

    /* java.util.Optional style methods */

    fun get(): T = (value as T?) ?: throw NoSuchElementException()

    inline fun ifPresent(action: (T) -> Unit): OptionalIfPresent {
        if (value != null)
            action(value as T)

        return OptionalIfPresent(value != null)
    }

    inline fun ifPresentOrElse(action: (T) -> Unit, emptyAction: () -> Unit) {
        if (value != null)
            action(value as T)
        else
            emptyAction()
    }

    inline fun isPresent(): Boolean = (value != null)

    inline fun or(supplier: () -> Optional<@UnsafeVariance T>): Optional<T> =
        if (value != null) this else supplier()

    inline fun orElse(other: @UnsafeVariance T?): T? = (value as T?) ?: other

    inline fun orElseNotNull(other: @UnsafeVariance T): T = (value as T?) ?: other

    fun orElseGet(alternative: () -> @UnsafeVariance T?): T? =
        if (value != null) (value as T) else alternative()

    fun orElseGetNotNull(alternative: () -> @UnsafeVariance T): T =
        if (value != null) (value as T) else alternative()

    fun orElseThrow(): T = (value as T?) ?: throw NoSuchElementException()

    fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): T =
        (value as T?) ?: throw exceptionSupplier()

    /* other functions */

    inline fun ifEmpty(action: () -> Unit) {
        if (value == null)
            action()
    }

    inline fun also(block: (T) -> Unit): Optional<T> {
        if (value != null)
            block(value as T)

        return this
    }

    inline fun apply(block: T.() -> Unit): Optional<T> {
        if (value != null)
            (value as T).block()

        return this
    }

    inline fun <R : Any> let(action: (T) -> R?): Optional<R> =
        if (value != null) Optional(action(value as T)) else Optional(null)

    inline fun <R : Any> run(action: T.() -> R?): Optional<R> =
        if (value != null) Optional((value as T).action()) else Optional(null)

    inline fun takeIf(predicate: (T) -> Boolean): Optional<T> =
        if (value == null || predicate(value as T)) this else Optional(null)

    inline fun takeUnless(predicate: (T) -> Boolean): Optional<T> =
        if (value == null || !predicate(value as T)) this else Optional(null)

    companion object {
        /* java.util.Optional style construction */

        fun empty(): Optional<Nothing> =
            Optional(null)

        inline fun <T : Any> of(value: T): Optional<T> =
            Optional(value)

        fun <T : Any> ofNullable(value: T?): Optional<T> =
            Optional(value)
    }

    /* avoid Optional<Optional<*>> types */

    val asOptional: Optional<T>
        inline get() = this
}

/* Conversions */

val <T : Any> T?.asOptional
    inline get() = Optional<T>(this)

val <T : Any> Optional<T>.asReference: T?
    inline get() = (this.value as T?)

/* Some/None style construction */

inline fun <T : Any> Option(t: T?): Optional<T> = Optional(t)

inline fun <T : Any> Some(t: T): Optional<T> = Optional(t)

val None = Optional<Nothing>(null)

/* Accessors allowing contravariant projections */

inline fun <S : Any, T : S> Optional<T>.reduce(
    operation: (acc: S, T) -> S
): S = (value as T?) ?: throw UnsupportedOperationException("can't reduce None")

@ExperimentalStdlibApi
inline fun <S : Any, T : S> Optional<T>.reduceOrNull(
    operation: (acc: S, T) -> S
): S? = (value as T?)

/* Comparable specialized cases */

inline fun <T : Comparable<T>> Optional<T>.max(): T? = (value as T?)

inline fun <T : Comparable<T>> Optional<T>.min(): T? = (value as T?)

inline fun <T : Comparable<T>> Optional<T>.sorted(): Optional<T> = this

inline fun <T : Comparable<T>> Optional<T>.sortedDescending(): Optional<T> = this

/* Collection iterator */

internal class OptionalIterator<T : Any>(private var cachedValue: T?) : Iterator<T> {
    override fun hasNext(): Boolean = (cachedValue != null)

    override fun next(): T {
        val t = cachedValue
        cachedValue = null
        return t ?: throw NoSuchElementException()
    }
}

/* Syntactic sugar for ifPresent {...} orElse {...} */

inline class OptionalIfPresent(val completed: Boolean) {
    inline infix fun orElse(block: () -> Unit) {
        if (!completed)
            block()
    }
}
