/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.kotlin.test.compare

import com.xemantic.kotlin.test.assert
import kotlin.test.Test
import kotlin.test.fail

/**
 * These tests are verifying that the [diff] function output is correct.
 * The [diff] function compares the original and the revised strings passed as parameters
 * and returns a description of differences.
 *
 * The output format is intended to be as easy as possible to process
 * by the Large Language Model (LLM) like Claude from Anthropic.
 *
 * Test cases are using various formats, like Markdown or HTML, however the difference
 * logic and produced description should be format agnostic by principle.
 */
class TextDiffTest {

    @Test
    fun `should have empty result if strings are the same`() {
        assert(diff(original = "foo", revised = "foo").isEmpty())
    }

    /**
     * This test case shows the LLM analyzing the code how to interpret trailing whitespaces
     * in multiline strings in Kotlin. Basically the top and the bottom triple quotes are discarded.
     */
    @Test
    fun `should have empty result if multiline string is the same as simple string`() {
        assert(
            diff(
                """
                foo
                """.trimIndent(),
                "foo"
            ).isEmpty()
        )
    }

    @Test
    fun `should report if strings are different`() = assertDifference(
        original = "foo",
        revised = "bar",
        difference = """
            Text comparison failed:
            Format description:
            • [-c-] indicates character 'c' present in original text but not in revised
            • {+c+} indicates character 'c' present in revised text but not in original
            • ⠀ (braille pattern blank, U+2800) represents a space character in differences to make them visible
            • Each character change is marked separately for precise difference tracking
            • Line numbers are 1-based
            • Structural changes show exact position of insertion or deletion

            ┌─ original
            │ foo
            └─ differs from revised
            │ bar
            └─ differences
              • line 1: strings differ
                - original: "foo"
                - revised:  "bar"
                - changes:  "[-f-]{+b+}[-o-]{+a+}[-o-]{+r+}"

        """.trimIndent()
    )

    @Test
    fun `should show differences in trailing whitespace`() = assertDifference(
        original = """
            Line with no space
            Line with one space 
            Line with two spaces  
            No newline at the end
        """.trimIndent(),
        revised = """
            Line with no space
            Line with one space
            Line with two spaces
            No newline at the end
            
        """.trimIndent(),
        difference = """
            Text comparison failed:
            Format description:
            • [-c-] indicates character 'c' present in original text but not in revised
            • {+c+} indicates character 'c' present in revised text but not in original
            • ⠀ (braille pattern blank, U+2800) represents a space character in differences to make them visible
            • Each character change is marked separately for precise difference tracking
            • Line numbers are 1-based
            • Structural changes show exact position of insertion or deletion

            ┌─ original
            │ Line with no space
            │ Line with one space⠀
            │ Line with two spaces⠀⠀
            │ No newline at the end
            └─ differs from revised
            │ Line with no space
            │ Line with one space
            │ Line with two spaces
            │ No newline at the end
            │ 
            └─ differences
              • line 2: trailing whitespace difference
                - original: "Line with one space⠀"
                - revised:  "Line with one space"
                - changes:  "Line with one space[-⠀-]"
              • line 3: trailing whitespace difference
                - original: "Line with two spaces⠀⠀"
                - revised:  "Line with two spaces"
                - changes:  "Line with two spaces[-⠀-][-⠀-]"
              • structural: missing line after line 4
                + 

        """.trimIndent()
    )

    @Test
    fun `should fail even if only whitespaces are different`() = assertDifference(
        original = """
            <div>
                <p>Hello</p>
            </div>
        """.trimIndent(),
        revised = """
            <div>
               <p>Hello</p>
            </div>
        """.trimIndent(),
        difference = """
            Text comparison failed:
            Format description:
            • [-c-] indicates character 'c' present in original text but not in revised
            • {+c+} indicates character 'c' present in revised text but not in original
            • ⠀ (braille pattern blank, U+2800) represents a space character in differences to make them visible
            • Each character change is marked separately for precise difference tracking
            • Line numbers are 1-based
            • Structural changes show exact position of insertion or deletion

            ┌─ original
            │ <div>
            │     <p>Hello</p>
            │ </div>
            └─ differs from revised
            │ <div>
            │    <p>Hello</p>
            │ </div>
            └─ differences
              • line 2: indentation difference
                - original: "    <p>Hello</p>"(4 spaces)
                - revised:  "   <p>Hello</p>"(3 spaces)
                - changes:  "[-⠀-]<p>Hello</p>"

        """.trimIndent()
    )

    @Test
    fun `should fail if multiline strings are different`() = assertDifference(
        original = """
            # Heading

            This is a paragraph
            with two lines.

            * List item 1
            * List item 2
        """.trimIndent(),
        revised = """
            # Heading

            This is paragraph
            with 2 lines.

            * List item 1
            * List item three
        """.trimIndent(),
        difference = """
            Text comparison failed:
            Format description:
            • [-c-] indicates character 'c' present in original text but not in revised
            • {+c+} indicates character 'c' present in revised text but not in original
            • ⠀ (braille pattern blank, U+2800) represents a space character in differences to make them visible
            • Each character change is marked separately for precise difference tracking
            • Line numbers are 1-based
            • Structural changes show exact position of insertion or deletion

            ┌─ original
            │ # Heading
            │ 
            │ This is a paragraph
            │ with two lines.
            │ 
            │ * List item 1
            │ * List item 2
            └─ differs from revised
            │ # Heading
            │ 
            │ This is paragraph
            │ with 2 lines.
            │ 
            │ * List item 1
            │ * List item three
            └─ differences
              • line 3: strings differ
                - original: "This is a paragraph"
                - revised:  "This is paragraph"
                - changes:  "This is [-a-][-⠀-]paragraph"
              • line 4: strings differ
                - original: "with two lines."
                - revised:  "with 2 lines."
                - changes:  "with [-t-][-w-][-o-]{+2+}[-⠀-]{+⠀+}lines."
              • line 7: strings differ
                - original: "* List item 2"
                - revised:  "* List item three"
                - changes:  "* List item [-2-]{+t+}{+h+}{+r+}{+e+}{+e+}"

        """.trimIndent()
    )

    @Test
    fun `should handle complex differences`() = assertDifference(
        original = """
            <!DOCTYPE html>
            <html>
              <head>
                <title>Test Page</title>
              </head>
              <body>
                <div class="container">
                  <h1>Hello World</h1>
                  <p>This is a test.</p>
                </div>
              </body>
            </html>
        """.trimIndent(),
        revised = """
            <!DOCTYPE html>
            <html>
              <head>
                <title>Test Page</title>
                <meta charset="utf-8">
              </head>
              <body>
                <div class="main-container">
                  <h1>Hello, World!</h1>
                  <p>This is a test.</p>
                </div>
              </body>
            </html>
        """.trimIndent(),
        difference = """
            Text comparison failed:
            Format description:
            • [-c-] indicates character 'c' present in original text but not in revised
            • {+c+} indicates character 'c' present in revised text but not in original
            • ⠀ (braille pattern blank, U+2800) represents a space character in differences to make them visible
            • Each character change is marked separately for precise difference tracking
            • Line numbers are 1-based
            • Structural changes show exact position of insertion or deletion

            ┌─ original
            │ <!DOCTYPE html>
            │ <html>
            │   <head>
            │     <title>Test Page</title>
            │   </head>
            │   <body>
            │     <div class="container">
            │       <h1>Hello World</h1>
            │       <p>This is a test.</p>
            │     </div>
            │   </body>
            │ </html>
            └─ differs from revised
            │ <!DOCTYPE html>
            │ <html>
            │   <head>
            │     <title>Test Page</title>
            │     <meta charset="utf-8">
            │   </head>
            │   <body>
            │     <div class="main-container">
            │       <h1>Hello, World!</h1>
            │       <p>This is a test.</p>
            │     </div>
            │   </body>
            │ </html>
            └─ differences
              • structural: missing line after line 4
                +     <meta charset="utf-8">
              • line 7: strings differ
                - original: "    <div class=\"container\">"
                - revised:  "    <div class=\"main-container\">"
                - changes:  "    <div class=\"[-c-][-o-][-n-][-t-][-a-][-i-][-n-][-e-][-r-]{+m+}{+a+}{+i+}{+n+}{+-+}{+c+}{+o+}{+n+}{+t+}{+a+}{+i+}{+n+}{+e+}{+r+}\">"
              • line 8: strings differ
                - original: "      <h1>Hello World</h1>"
                - revised:  "      <h1>Hello, World!</h1>"
                - changes:  "      <h1>Hello[-⠀-]{+,+}{+⠀+}World{+!+}</h1>"

        """.trimIndent()
    )

    @Test
    fun `should handle differences in markdown content`() = assertDifference(
        original = """
            # Main Heading

            ## Sub-heading

            This is a paragraph with *italic* and
            **bold** text. It continues on the
            next line.

            * List item 1
            * List item 2

            > This is a blockquote
            > with multiple lines

            ```kotlin
            fun main() {
                println("Hello")
            }
            ```
        """.trimIndent(),
        revised = """
            # Main Heading

            ## Sub-heading

            This is a paragraph with _italic_ and
            __bold__ text. It continues on the
            next line.

            * List item 1
            * List item 2

            > This is a blockquote
            > with multiple lines.

            ```kotlin
            fun main() {
              println("Hello");
            }
            ```
        """.trimIndent(),
        difference = """
            Text comparison failed:
            Format description:
            • [-c-] indicates character 'c' present in original text but not in revised
            • {+c+} indicates character 'c' present in revised text but not in original
            • ⠀ (braille pattern blank, U+2800) represents a space character in differences to make them visible
            • Each character change is marked separately for precise difference tracking
            • Line numbers are 1-based
            • Structural changes show exact position of insertion or deletion

            ┌─ original
            │ # Main Heading
            │ 
            │ ## Sub-heading
            │ 
            │ This is a paragraph with *italic* and
            │ **bold** text. It continues on the
            │ next line.
            │ 
            │ * List item 1
            │ * List item 2
            │ 
            │ > This is a blockquote
            │ > with multiple lines
            │ 
            │ ```kotlin
            │ fun main() {
            │     println("Hello")
            │ }
            │ ```
            └─ differs from revised
            │ # Main Heading
            │ 
            │ ## Sub-heading
            │ 
            │ This is a paragraph with _italic_ and
            │ __bold__ text. It continues on the
            │ next line.
            │ 
            │ * List item 1
            │ * List item 2
            │ 
            │ > This is a blockquote
            │ > with multiple lines.
            │ 
            │ ```kotlin
            │ fun main() {
            │   println("Hello");
            │ }
            │ ```
            └─ differences
              • line 5: strings differ
                - original: "This is a paragraph with *italic* and"
                - revised:  "This is a paragraph with _italic_ and"
                - changes:  "This is a paragraph with [-*-]{+_+}italic[-*-]{+_+} and"
              • line 6: strings differ
                - original: "**bold** text. It continues on the"
                - revised:  "__bold__ text. It continues on the"
                - changes:  "[-*-]{+_+}[-*-]{+_+}bold[-*-]{+_+}[-*-]{+_+} text. It continues on the"
              • line 13: strings differ
                - original: "> with multiple lines"
                - revised:  "> with multiple lines."
                - changes:  "> with multiple lines{+.+}"
              • line 17: strings differ
                - original: "    println(\"Hello\")"
                - revised:  "  println(\"Hello\");"
                - changes:  "  [-⠀-][-⠀-]println(\"Hello\"){+;+}"

        """.trimIndent()
    )

    private fun assertDifference(
        original: String,
        revised: String,
        difference: String
    ) {
        val diff = diff(original, revised)
        if ((diff != difference) && diff.isNotEmpty()) {
            fail("The actual difference message was:\n${diff}")
        }
    }

}