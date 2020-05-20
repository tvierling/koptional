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

@file:JvmName("OptionalJVM")
@file:Suppress("DEPRECATION", "NOTHING_TO_INLINE", "UNCHECKED_CAST", "UNUSED_PARAMETER")

package org.duh.koptional

/* Accessors that only work on the JVM */

fun <R : Any> Optional<*>.filterIsInstance(klass: Class<R>): Optional<R> {
    if (value != null && klass.isInstance(value))
        return Optional(value)

    return Optional(null)
}

fun <C : MutableCollection<in R>, R : Any> Optional<*>.filterIsInstanceTo(
    destination: C,
    klass: Class<R>
): C {
    if (value != null && klass.isInstance(value))
        destination.add(value as R)

    return destination
}

inline fun <T : Any> Optional<T>.shuffled(random: java.util.Random): Optional<T> = this

/* Java 8 interop */

typealias JOptional<T> = java.util.Optional<T>

val <T : Any> T?.asJOptional
    inline get() = JOptional.ofNullable(this)

inline fun <T : Any> JOptional<T>?.orEmpty(): JOptional<T> =
    this ?: JOptional.empty()

val <T : Any> Optional<T>.box: JOptional<T>
    @JvmName("box")
    inline get() = JOptional.ofNullable(value as T?)

val <T : Any> JOptional<T>.unbox: Optional<T>
    @JvmName("unbox")
    inline get() = Optional(orElse(null))
