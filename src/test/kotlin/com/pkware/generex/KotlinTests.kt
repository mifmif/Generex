package com.pkware.generex

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream
import kotlin.collections.iterator
import kotlin.math.max
import kotlin.math.min

class KotlinTests {

    @Test
    fun `infinite regex isn't implicitly infinite`() {

        val generex = Generex("a*")
        repeat(100) {
            // 50 happens to be the default implicit cap for infinite regexes
            assertThat(generex.random().length).isAtMost(50)
        }
    }

    @ParameterizedTest
    @MethodSource("escapeArgs")
    fun `escapes multiple escape sequences correctly`(regex: String, expectedMatch: String) {

        val generex = Generex(regex)
        assertThat(generex.random()).isEqualTo(expectedMatch)
    }

    @ParameterizedTest
    @MethodSource("shortRegexArgs")
    fun `longest regex is produced when shorter than min length`(regex: String, longestLength: Int) {

        val generex = Generex(regex)

        repeat(100) {
            val result = generex.random(5)
            assertThat(result).matches(regex)
            assertThat(result).hasLength(longestLength)
        }
    }

    @ParameterizedTest
    @MethodSource("trimArgs")
    fun `regex is trimmed on overflow`(regex: String, prefixRegex: String) {

        val generex = Generex(regex)
        repeat(100) {
            assertThat(generex.random(0, 5)).matches(prefixRegex)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["a*b", "a{1,}b", "(ab)*c", "a*b*c"])
    fun `regex doesn't overflow`(regex: String?) {
        val generex = Generex(regex)
        for (i in 0..99) {
            val result = generex.random(0, 5)
            assertThat(result).matches(regex)
            assertThat(result.length).isAtMost(5)
        }
    }

    @ParameterizedTest
    @MethodSource("uniformDistributionArgs")
    fun `simple regexes generate with uniform distribution`(regex: String, minLength: Int, maxLength: Int) {

        val generex = Generex(regex)
        val instancesMap = HashMap<String, Int>()

        repeat(100_000) {

            val result = generex.random(minLength, maxLength)
            instancesMap[result] = instancesMap.getOrDefault(result, 0) + 1

            assertThat(result).matches(regex)
        }

        var maxInstances = 0
        var minInstances = Int.MAX_VALUE

        // Assumes all possible strings have actually been produced.
        for ((key, instances) in instancesMap) {
            println("$key: $instances")
            maxInstances = max(maxInstances, instances)
            minInstances = min(minInstances, instances)
        }

        val ratio = 1.0 * maxInstances / minInstances
        assertThat(ratio).isLessThan(1.1)
    }

    @Test
    fun `ranges of group generate with a semi-uniform distribution`() {
        val regex = "(abcde){1,5}"

        val generex = Generex(regex)
        val instancesMap = HashMap<String, Int>()

        repeat(100_000) {

            val result = generex.random(5, 25)
            instancesMap[result] = instancesMap.getOrDefault(result, 0) + 1

            assertThat(result).matches(regex)
        }

        val sortedKeys = instancesMap.keys.sortedBy { it.length }


        // Bounds are uniformly distributed

        var maxInstances = 0
        var minInstances = Int.MAX_VALUE

        println("Bounds:")
        println("\t${sortedKeys.first()}: ${instancesMap[sortedKeys.first()]}")
        maxInstances = max(maxInstances, instancesMap[sortedKeys.first()]!!)
        minInstances = min(minInstances, instancesMap[sortedKeys.first()]!!)
        println("\t${sortedKeys.last()}: ${instancesMap[sortedKeys.last()]}")
        maxInstances = max(maxInstances, instancesMap[sortedKeys.last()]!!)
        minInstances = min(minInstances, instancesMap[sortedKeys.last()]!!)

        var ratio = 1.0 * maxInstances / minInstances
        assertThat(ratio).isLessThan(1.1)


        // Middle range is uniformly distributed

        maxInstances = 0
        minInstances = Int.MAX_VALUE

        println("Middle Ranges:")
        for (key in sortedKeys.subList(1, 4)) {
            println("\t$key: ${instancesMap[key]}")
            maxInstances = max(maxInstances, instancesMap[key]!!)
            minInstances = min(minInstances, instancesMap[key]!!)
        }

        ratio = 1.0 * maxInstances / minInstances
        assertThat(ratio).isLessThan(1.1)



    }

    @ParameterizedTest
    @MethodSource("rangeUniformDistributionArgs")
    fun `range regexes generate with uniform distributions`(regex: String) {

        val generex = Generex(regex)
        val instancesMap = HashMap<Int, Int>()

        repeat(100_000) {

            val result = generex.random()
            instancesMap[result.length] = instancesMap.getOrDefault(result.length, 0) + 1

            assertThat(result).matches(regex)
        }

        var maxInstances = 0
        var minInstances = Int.MAX_VALUE

        // Assumes all possible strings have actually been produced.
        for ((key, instances) in instancesMap) {
            println("$key: $instances")
            maxInstances = max(maxInstances, instances)
            minInstances = min(minInstances, instances)
        }

        val ratio = 1.0 * maxInstances / minInstances
        assertThat(ratio).isLessThan(1.1)
    }

    @ParameterizedTest
    @MethodSource("regexExceedsColumnValue")
    fun `if regex must produce longer value, return value is trimmed`(
        regex: String,
        targetLength: Int,
    ) {
        val generated = Generex(regex).random(targetLength, targetLength)

        assertThat(generated.length).isEqualTo(targetLength)
    }

    @ParameterizedTest
    @MethodSource("infiniteRegexArgs")
    fun `infinite regex does not hang`(regex: String) {
        val generex = Generex(regex)
        repeat(10) {
            val result = generex.random()
            assertThat(result).matches(regex)
        }
    }

    @Test
    fun `anchors are stripped from regex`() {
        val regex = "^[A-Za-z]+$"
        val generex = Generex(regex)
        repeat(100) {
            val result = generex.random()
            assertThat(result).matches(regex)
            assertThat(result).doesNotContain("^")
            assertThat(result).doesNotContain("$")
        }
    }

    @Test
    fun `non-capturing groups are converted to plain groups`() {
        val regex = "(?:abc)+"
        val generex = Generex(regex)
        repeat(100) {
            val result = generex.random()
            assertThat(result).matches(regex)
        }
    }

    @Test
    fun `escaped dollar sign at end is not stripped`() {
        val regex = "abc\\$"
        val generex = Generex(regex)
        val result = generex.random()
        assertThat(result).isEqualTo("abc$")
    }

    companion object {

        @JvmStatic
        fun escapeArgs() = Stream.of(
            Arguments.of("""\Q$\E\Q$\E\Q$\E""", "$$$"),
            Arguments.of("""\\Q\\E""", """\Q\E"""),
            Arguments.of("""\\\\\Q\\\E""", """\\\\"""),
            Arguments.of("""\\\\\\Q\\\\E""", """\\\Q\\E"""),
            Arguments.of("""\\\\Q\\\Q$\\\E\\Q\\E""", """\\Q\$\\\Q\E"""),
        )

        @JvmStatic
        fun shortRegexArgs() = Stream.of(
            Arguments.of("a{0,2}", 2),
            Arguments.of("HI|BRO", 3),
            Arguments.of("a{0,2}[A-Z]", 3),
            Arguments.of("[A-Z]a{0,2}[A-Z]", 4),
            Arguments.of("AT|CAT|DOG|FLY", 3),
        )

        @JvmStatic
        fun trimArgs() = Stream.of(
            Arguments.of("\\d{10}", "\\d{5}"),
            Arguments.of("1234567890", "12345"),
            Arguments.of("HELLO_THERE|THERE_HELLO", "HELLO|THERE"),
            Arguments.of("""\d{3,5}\s{3,5}""", """(?s)\d{3}(?=..$)\d*\s*""")
        )

        @JvmStatic
        fun uniformDistributionArgs() = Stream.of(
            Arguments.of("a|\\d", 1, 1),
            Arguments.of("a|c", 1, 1),
            Arguments.of("a*", 0, 10),
            Arguments.of("a+", 5, 10),
            Arguments.of("[a-ce-gr-ux-z]", 1, 1),
            Arguments.of("123a*", 1, 10),
            Arguments.of("123a*", 5, 10),
            Arguments.of("a*123", 5, 20),
        )

        @JvmStatic
        fun rangeUniformDistributionArgs() = Stream.of(
            Arguments.of("\\d{1,5}"),
            Arguments.of("\\d{1,10}"),
        )

        @JvmStatic
        fun infiniteRegexArgs() = Stream.of(
            Arguments.of("^[A-Za-z]+(?:[ '-][A-Za-z]+)*$"),
            Arguments.of("[A-Za-z]+([ '-][A-Za-z]+)*"),
            Arguments.of("(\\d{1,3}\\.){3}\\d{1,3}"),
            Arguments.of("[A-Z][a-z]*( [A-Z][a-z]*)*"),
            Arguments.of("(ab|cd)+(ef|gh)*"),
        )

        @JvmStatic
        fun regexExceedsColumnValue() = Stream.of(
            Arguments.of("(hi){3,5}", 7),
            Arguments.of("aaa", 2),
            Arguments.of("a{5,10}", 2),
        )
    }
}
