import com.xemantic.kotlin.test.sameAs

fun main() {
    try {
        "foo" sameAs "bar"
    } catch (e: AssertionError) {
        println("Error message:")
        println(e.message)
        println("\nExpected:")
        val expected = """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -bar
            \ No newline at end of file
            +foo
            \ No newline at end of file
        """.trimIndent()
        println(expected)
    }
}
