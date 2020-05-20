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

@file:Suppress("SENSELESS_COMPARISON", "UNREACHABLE_CODE")

package org.duh.koptional.set

import org.duh.koptional.OptError
import org.duh.koptional.assertFailsWithMsg
import kotlin.random.Random
import kotlin.test.*

// fragments of OptionalTests for validation of correct behavior wrt collections
typealias Optional<T> = List<T>

private fun <T> Option(t: T?) =
    if (t != null) listOf(t) else emptyList()

private fun <T> Some(t: T) = listOf(t)

private val None = emptyList<Int>()

class ListTests {
    private val o = Option(123)
    private val no = None

    private val l = mutableListOf<Int>()
    private val el = emptyList<Int>()
    private val sl = mutableListOf<String>()
    private val al = mutableListOf<Any>()
    private val eal = emptyList<Any>()

    private inline fun assertIdentityFunc(f: Optional<Int>.() -> Optional<Int>) {
        assertEquals(o, o.f())
        assertEquals(None, no.f())
    }

    private inline fun assertGetFunc(f: Optional<Int>.() -> Int) {
        assertEquals(123, o.f())
        assertFailsWith<NoSuchElementException> { no.f() }
    }

    private inline fun assertGetPredFunc(f: Optional<Int>.((Int) -> Boolean) -> Int) {
        assertEquals(123, o.f { it == 123 })
        assertFailsWith<NoSuchElementException> { o.f { it == 456 } }
        assertFailsWithMsg<OptError>("123") { o.f { throw OptError("$it") } }
        assertFailsWith<NoSuchElementException> { no.f { throw OptError() } }
    }

    private inline fun assertGetNullFunc(f: Optional<Int>.() -> Int?) {
        assertEquals(123, o.f())
        assertNull(no.f())
    }

    private inline fun assertGetPredNullFunc(f: Optional<Int>.((Int) -> Boolean) -> Int?) {
        assertEquals(123, o.f { it == 123 })
        assertNull(o.f { it == 456 })
        assertFailsWithMsg<OptError>("123") { o.f { throw OptError("$it") } }
        assertNull(no.f { throw OptError() })
    }

    private inline fun <T> assertForEachFunc(f: Optional<Int>.((Int) -> Unit) -> T): T {
        assertFailsWithMsg<OptError>("123") { o.f { throw OptError("$it") } }
        no.f { throw OptError() }
        return o.f {}
    }

    private inline fun assertIterator(iterator: (Optional<Int>) -> Iterator<Int>) {
        assertTrue(iterator(o).hasNext())
        assertEquals(123, iterator(o).next())
        assertFalse(iterator(no).hasNext())
        assertFailsWith<NoSuchElementException> {
            iterator(no).next()
        }
    }

    // === common trivial functions ===

    @Test
    fun testIdentities() {
        assertIdentityFunc { requireNoNulls() }
        assertIdentityFunc { reversed() }
        assertIdentityFunc { shuffled() }
        assertIdentityFunc { shuffled(Random.Default) }
    }

    private inline fun assertNonEmpty(f: Optional<Int>.() -> Boolean) {
        assertTrue(o.f())
        assertFalse(no.f())
    }

    @Test
    fun testBooleans() {
        assertNonEmpty { !none() }
    }

    // === specific functions ===

    @Test
    fun testAllAnyNone() {
        assertTrue(o.all { it == 123 })
        assertFalse(o.all { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.all { throw OptError("$it") }
        }
        assertTrue(no.all { throw OptError() })

        assertTrue(o.any())
        assertFalse(no.any())

        assertTrue(o.any { it == 123 })
        assertFalse(o.any { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.any { throw OptError("$it") }
        }
        assertFalse(no.any { throw OptError() })

        assertFalse(o.none { it == 123 })
        assertTrue(o.none { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.none { throw OptError("$it") }
        }
        assertTrue(no.none { throw OptError() })
    }

    @Test
    fun testAs() {
        assertIterator { it.asSequence().iterator() }
    }

    @Test
    fun testContains() {
        assertTrue(o.contains(123))
        assertFalse(o.contains(456))
        assertFalse(no.contains(123))

        assertTrue(o.containsAll(listOf()))
        assertTrue(no.containsAll(listOf()))
        assertTrue(o.containsAll(listOf(123, 123, 123)))
        assertFalse(o.containsAll(listOf(123, 456)))
        assertFalse(no.containsAll(listOf(123)))
    }

    @Test
    fun testCount() {
        assertEquals(1, o.count())
        assertEquals(0, no.count())

        assertEquals(1, o.count { it == 123 })
        assertEquals(0, o.count { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.count { throw OptError("$it") }
        }
        assertEquals(0, no.count { it == 123 })
    }

    @Test
    fun testDistinct() {
        assertIdentityFunc { distinct() }

        assertEquals(o, o.distinctBy { it == 123 })
        assertEquals(o, o.distinctBy { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.distinctBy { throw OptError("$it") }
        }
        assertEquals(None, no.distinctBy { throw OptError() })
    }

    @Test
    fun testDrop() {
        assertFailsWithMsg<IllegalArgumentException>(
            "Requested element count -123 is less than zero."
        ) { o.drop(-123) }
        assertEquals(o, o.drop(0))
        assertEquals(None, o.drop(1))
        assertEquals(None, o.drop(123))
        assertEquals(None, no.drop(0))
        assertEquals(None, no.drop(123))

        assertEquals(None, o.dropWhile { it == 123 })
        assertEquals(o, o.dropWhile { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.dropWhile { throw OptError("$it") }
        }
        assertEquals(None, no.dropWhile { it == 123 })
    }

    @Test
    fun testElementAt() {
        assertEquals(123, o.elementAt(0))
        assertFailsWith<IndexOutOfBoundsException> { o.elementAt(-1) }
        assertFailsWith<IndexOutOfBoundsException> { o.elementAt(1) }
        assertFailsWith<IndexOutOfBoundsException> { no.elementAt(0) }

        assertEquals(123, o.elementAtOrElse(0) { 456 })
        assertEquals(123, o.elementAtOrElse(0) { throw OptError() })
        assertEquals(456, o.elementAtOrElse(1) { 456 })
        assertEquals(456, o.elementAtOrElse(-1) { 456 })
        assertFailsWithMsg<OptError>("foo") {
            o.elementAtOrElse(1) { throw OptError("foo") }
        }
        assertEquals(456, no.elementAtOrElse(0) { 456 })

        assertEquals(123, o.elementAtOrNull(0))
        assertNull(o.elementAtOrNull(1))
        assertNull(o.elementAtOrNull(-1))
        assertNull(no.elementAtOrNull(0))
    }

    @Test
    fun testFilter() {
        assertEquals(o, o.filter { it == 123 })
        assertEquals(None, o.filter { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.filter { throw OptError("$it") }
        }
        assertEquals(None, no.filter { it == 123 })
        assertEquals(None, no.filter { throw OptError("$it") })
    }

    @Test
    fun testFilterIndexed() {
        assertEquals(o, o.filterIndexed { index, i -> index == 0 && i == 123 })
        assertFailsWithMsg<OptError>("0:123") {
            o.filterIndexed { index, i -> throw OptError("$index:$i") }
        }
        assertEquals(None, o.filterIndexed { index, i -> index != 0 || i != 123 })
        assertEquals(None, no.filterIndexed { _, _ -> throw OptError() })
    }

    @Test
    fun testFilterIndexedTo() {
        assertEquals(el, o.filterIndexedTo(l) { index, i -> index != 0 || i != 123 })
        assertEquals(el, no.filterIndexedTo(l) { _, _ -> throw OptError() })
        assertEquals(listOf(123), o.filterIndexedTo(l) { index, i -> index == 0 && i == 123 })
        l.clear()
        assertFailsWithMsg<OptError>("0:123") {
            o.filterIndexedTo(l) { index, i -> throw OptError("$index:$i") }
        }
        assertEquals(el, l)
    }

    @Test
    fun testFilterIsInstance() {
        assertEquals(o, o.filterIsInstance<Int>())
        assertEquals(o, o.filterIsInstance<Any>())
        assertEquals(emptyList(), o.filterIsInstance<String>())
        assertEquals(o, o.filterIsInstance<Number>())
        assertEquals(None, no.filterIsInstance<Any>())

        assertEquals(listOf(123), o.filterIsInstanceTo(l))
        l.clear()
        assertEquals(emptyList<String>(), o.filterIsInstanceTo(sl))
        assertEquals(eal, no.filterIsInstanceTo(al))
    }

    @Test
    fun testFilterNot() {
        assertEquals(None, o.filterNot { it == 123 })
        assertEquals(o, o.filterNot { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.filterNot { throw OptError("$it") }
        }
        assertEquals(None, no.filterNot { throw OptError() })
    }

    @Test
    fun testFilterNotNull() {
        assertEquals(o, o.filterNotNull())
        assertEquals(None, no.filterNotNull())

        assertEquals(listOf(123), o.filterNotNullTo(l))
        l.clear()
        assertEquals(eal, no.filterNotNullTo(al))
    }

    @Test
    fun testFilterNotTo() {
        assertEquals(el, o.filterNotTo(l) { it == 123 })
        assertEquals(el, no.filterNotTo(l) { throw OptError() })
        assertEquals(listOf(123), o.filterNotTo(l) { it != 123 })
        l.clear()
        assertFailsWithMsg<OptError>("123") {
            o.filterNotTo(l) { throw OptError("$it") }
        }
        assertEquals(el, l)
    }

    @Test
    fun testFilterTo() {
        assertEquals(el, o.filterTo(l) { it != 123 })
        assertEquals(el, no.filterTo(l) { throw OptError() })
        assertEquals(listOf(123), o.filterTo(l) { it == 123 })
        l.clear()
        assertFailsWithMsg<OptError>("123") {
            o.filterTo(l) { throw OptError("$it") }
        }
        assertEquals(el, l)
    }

    private inline fun assertFindFunc(f: Optional<Int>.((Int) -> Boolean) -> Int?) {
        assertEquals(123, o.f { it == 123 })
        assertNull(o.f { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.f { throw OptError("$it") }
        }
        assertNull(no.f { throw OptError() })
    }

    @Test
    fun testFind() {
        assertFindFunc { find(it) }
        assertFindFunc { findLast(it) }
    }

    @Test
    fun testFirst() {
        assertGetFunc { first() }
        assertGetPredFunc { first(it) }
        assertGetNullFunc { firstOrNull() }
        assertGetPredNullFunc { firstOrNull(it) }
    }

    @Test
    fun testFlatMap() {
        assertEquals(Some(1230), o.flatMap { Option(it * 10) })
        assertEquals(None, o.flatMap { Option(null) })
        assertEquals(None, no.flatMap { throw OptError() })
    }

    @Test
    fun testFold() {
        assertFailsWithMsg<OptError>("E123") {
            o.fold("E") { r, it -> throw OptError("$r$it") }
        }
        assertEquals("A123", o.fold("A") { r, it -> r + it })
        assertEquals("A", no.fold("A") { _, _ -> throw OptError() })
        assertEquals("I0123", o.foldIndexed("I") { index, r, it -> r + index + it })
        assertEquals("I", no.foldIndexed("I") { _, _, _ -> throw OptError() })
    }

    @Test
    fun testForEach() {
        assertForEachFunc { forEach(it) }

        o.forEachIndexed { _, _ -> }
        assertFailsWithMsg<OptError>("0:123") {
            o.forEachIndexed { index, i -> throw OptError("$index:$i") }
        }
        no.forEachIndexed { _, _ -> throw OptError() }
    }

    private inline fun assertIndexOfFunc(f: Optional<Int>.(Int) -> Int) {
        assertEquals(0, o.f(123))
        assertEquals(-1, o.f(456))
        assertEquals(-1, no.f(123))
    }

    private inline fun assertIndexOfPred(f: Optional<Int>.((Int) -> Boolean) -> Int) {
        assertEquals(0, o.f { it == 123 })
        assertEquals(-1, o.f { it == 456 })
        assertFailsWithMsg<OptError>("123") { o.f { throw OptError("$it") } }
        assertEquals(-1, no.f { throw OptError() })
    }

    @Test
    fun testIndexOf() {
        assertIndexOfFunc { indexOf(it) }
        assertIndexOfPred { indexOfFirst(it) }
        assertIndexOfPred { indexOfLast(it) }
        assertIndexOfFunc { lastIndexOf(it) }
    }

    @Test
    fun testIterator() = assertIterator { it.iterator() }

    @Test
    fun testLast() {
        assertGetFunc { last() }
        assertGetPredFunc { last(it) }
        assertGetNullFunc { lastOrNull() }
        assertGetPredNullFunc { lastOrNull(it) }
    }


    @Test
    fun testMap() {
        assertEquals(Some(1230), o.map { it * 10 })
        assertFailsWithMsg<OptError>("123") {
            o.map { throw OptError("$it") }
        }
        assertEquals(None, no.map { throw OptError() })
    }

    @Test
    fun testMapIndexed() {
        assertEquals(Some(1230), o.mapIndexed { index, i ->
            assertEquals(0, index)
            i * 10
        })
        assertFailsWithMsg<OptError>("0:123") {
            o.mapIndexed { index, i -> throw OptError("$index:$i") }
        }
        assertEquals(None, no.mapIndexed { _, _ -> throw OptError() })
    }

    @Test
    fun testMapIndexedNotNull() {
        assertEquals(Some(1230), o.mapIndexedNotNull { index, i ->
            assertEquals(0, index)
            i * 10
        })
        assertEquals(None, o.mapIndexedNotNull { _, _ -> null })
        assertFailsWithMsg<OptError>("0:123") {
            o.mapIndexedNotNull { index, i -> throw OptError("$index:$i") }
        }
        assertEquals(None, no.mapIndexedNotNull { _, _ -> throw OptError() })
    }

    @Test
    fun testMapIndexedNotNullTo() {
        assertEquals(listOf(1230), o.mapIndexedNotNullTo(l) { index, i ->
            assertEquals(0, index)
            i * 10
        })
        l.clear()
        assertEquals(el, o.mapIndexedNotNullTo(l) { _, _ -> null })
        assertFailsWithMsg<OptError>("0:123") {
            o.mapIndexedNotNullTo(l) { index, i -> throw OptError("$index:$i") }
        }
        assertEquals(el, l)
        assertEquals(el, no.mapIndexedNotNullTo(l) { _, _ -> throw OptError() })
    }

    @Test
    fun testMapIndexedTo() {
        assertEquals(listOf(1230), o.mapIndexedTo(l) { index, i ->
            assertEquals(0, index)
            i * 10
        })
        l.clear()
        assertFailsWithMsg<OptError>("0:123") {
            o.mapIndexedTo(l) { index, i -> throw OptError("$index:$i") }
        }
        assertEquals(el, l)
        assertEquals(el, no.mapIndexedTo(l) { _, _ -> throw OptError() })
    }

    @Test
    fun testMapNotNull() {
        assertEquals(Some("123"), o.mapNotNull { "$it" })
        assertEquals(None, o.mapNotNull { null })
        assertFailsWithMsg<OptError>("123") {
            o.mapNotNull { throw OptError("$it") }
        }
        assertEquals(None, no.mapNotNull { throw OptError() })
    }

    @Test
    fun testMapNotNullTo() {
        assertEquals(listOf(1230), o.mapNotNullTo(l) { it * 10 })
        l.clear()
        assertEquals(el, o.mapNotNullTo(l) { null })
        assertFailsWithMsg<OptError>("123") {
            o.mapNotNullTo(l) { throw OptError("$it") }
        }
        assertEquals(el, l)
        assertEquals(el, no.mapNotNullTo(l) { throw OptError() })
    }

    @Test
    fun testMapTo() {
        assertEquals(listOf(1230), o.mapTo(l) { it * 10 })
        l.clear()
        assertFailsWithMsg<OptError>("123") {
            o.mapTo(l) { throw OptError("$it") }
        }
        assertEquals(el, l)
        assertEquals(el, no.mapTo(l) { throw OptError() })
    }

    @Test
    fun testOnEach() {
        assertEquals(o, assertForEachFunc { onEach(it) })
    }

    @Test
    fun testMinMax() {
        val c = Comparator<Int> { _, _ -> throw OptError() }

        // List does not execute the block if only one element exists
        assertGetNullFunc { maxBy { throw OptError() } }
        assertGetNullFunc { maxWith(c) }
        assertGetNullFunc { minBy { throw OptError() } }
        assertGetNullFunc { minWith(c) }
    }

    @Test
    fun testSingle() {
        assertGetFunc { single() }
        assertGetPredFunc { single(it) }
        assertGetNullFunc { singleOrNull() }
        assertGetPredNullFunc { singleOrNull(it) }
    }

    @Test
    fun testSize() {
        assertEquals(1, o.size)
        assertEquals(0, no.size)
    }

    @Test
    fun testSortedBy() {
        // List does not execute the block if only one element exists
        assertIdentityFunc { sortedBy { throw OptError() } }
        assertIdentityFunc { sortedByDescending { throw OptError() } }
    }

    @Test
    fun testSortedWith() = assertIdentityFunc {
        sortedWith(Comparator { _, _ -> throw OptError() })
    }

    @Test
    fun testTake() {
        assertFailsWithMsg<IllegalArgumentException>(
            "Requested element count -123 is less than zero."
        ) { o.take(-123) }
        assertEquals(None, o.take(0))
        assertEquals(o, o.take(1))
        assertEquals(o, o.take(123))
        assertEquals(None, no.take(0))
        assertEquals(None, no.take(123))

        assertEquals(o, o.takeWhile { it == 123 })
        assertEquals(None, o.takeWhile { it == 456 })
        assertFailsWithMsg<OptError>("123") {
            o.distinctBy { throw OptError("$it") }
        }
        assertEquals(None, no.takeWhile { it == 123 })
    }

    @Test
    fun testToCollection() {
        assertEquals(listOf(123), o.toCollection(l))
        l.clear()
        assertEquals(el, no.toCollection(l))
    }

    @Test
    fun testToList() {
        assertEquals(listOf(123), o.toList())
        assertEquals(el, no.toList())
    }

    @Test
    fun testReduce() {
        assertEquals(123, o.reduce { _, _ -> throw OptError() })
        assertFailsWithMsg<UnsupportedOperationException>("Empty collection can't be reduced.") {
            no.reduce { _, _ -> throw OptError() }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testReduceOrNull() {
        assertEquals(123, o.reduceOrNull { _, _ -> throw OptError() })
        assertNull(no.reduceOrNull { _, _ -> throw OptError() })
    }

    @Test
    fun testComparable() {
        assertGetNullFunc { max() }
        assertGetNullFunc { min() }
        assertEquals(o, o.sorted())
        assertEquals(o, o.sortedDescending())
    }
}
