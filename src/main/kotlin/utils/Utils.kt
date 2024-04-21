package utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/main/resources/$name.txt").readLines()


/**
 * Returns the ints inside a String.
 */
private val intRx = Regex("""(?<!\d)-?\d+""")
fun String.getInts() = intRx.findAll(this).mapNotNull { it.value.toIntOrNull() }


/**
 * Splits ints based on the passed delimiter.
 */
fun String.splitIntsNotNull(vararg delimiters: String = arrayOf(" ")) = split(*delimiters).mapNotNull(String::toIntOrNull)


/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)


/**
 * Testing with check.
 */
fun checkTest(partId: Int, expected: Int, checkInput: String, partFunc: (input: List<String>) -> Int) {
    val actual = partFunc(listOf(checkInput))
    check(expected == actual) {
        """
            FAILED   :  $partId
            input    -> $checkInput
            expected   [$expected]
            but got    [$actual]
        """.trimIndent()
    }
}