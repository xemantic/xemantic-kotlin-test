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
import kotlin.test.assertEquals
import kotlin.test.fail

class TextComparisonTest {

    @Test
    fun `should pass if strings are the same`() {
        assert(("foo" diff "foo").isEmpty())
    }

    /**
     * This test case shows the LLM how to interpret trailing whitespaces in multiline strings in Kotlin.
     * Basically the top and bottom triple quotes are discarded.
     */
    @Test
    fun `should pass if multiline string is the same as simple string`() {
        assert(("""
            foo
        """.trimIndent() diff "foo").isEmpty())
    }

    @Test
    fun `should report if strings are different`() = assertDifference(
        text1 = "foo",
        text2 = "bar",
        difference = """
            Text comparison failed:
            ┌─ actual
            │ foo
            └─ differs from expected
            │ bar
            └─ differences
              • line 1: strings differ
                - actual:   "foo"
                - expected: "bar"
                - changes:  "[-f-]{+b+}[-o-]{+a+}[-o-]{+r+}"
        """.trimIndent()
    )

    @Test
    fun `should fail if multiline strings are different`() = assertDifference(
        text1 = """
            # Heading

            This is a paragraph
            with two lines.

            * List item 1
            * List item 2
        """.trimIndent(),
        text2 = """
            # Heading

            This is paragraph
            with 2 lines.

            * List item 1
            * List item three
        """.trimIndent(),
        difference = """
            Text comparison failed:
            ┌─ actual
            │ # Heading
            │ 
            │ This is a paragraph
            │ with two lines.
            │ 
            │ * List item 1
            │ * List item 2
            └─ differs from expected
            │ # Heading
            │ 
            │ This is paragraph
            │ with 2 lines.
            │ 
            │ * List item 1
            │ * List item three
            └─ differences
              • line 3: "This is a paragraph" vs "This is paragraph"
                changes: "This is [-a-][ -]paragraph"
              • line 4: "with two lines." vs "with 2 lines."
                changes: "with [-t-][-w-][-o-]{+2+} lines."
              • line 7: "* List item 2" vs "* List item three"
                changes: "* List item [-2-]{+t+}{+h+}{+r+}{+e+}{+e+}"
        """.trimIndent()
    )

    @Test
    fun `should fail even if only whitespaces are different`() = assertDifference(
        text1 = """
            <div>
                <p>Hello</p>
            </div>
        """.trimIndent(),
        text2 = """
            <div>
               <p>Hello</p>
            </div>
        """.trimIndent(),
        difference = """
            Text comparison failed:
            ┌─ actual
            │ <div>
            │     <p>Hello</p>
            │ </div>
            └─ differs from expected
            │ <div>
            │    <p>Hello</p>
            │ </div>
            └─ differences
              • line 2: indentation difference
                - actual:   "    <p>Hello</p>" (4 spaces)
                - expected: "   <p>Hello</p>"  (3 spaces)
                - changes:  "    <p>Hello</p>[-⠀-]"
        """.trimIndent()
    )

    @Test
    fun `should show differences in trailing whitespace`() = assertDifference(
        text1 = """
            Line with no space
            Line with one space 
            Line with two spaces  
            No newline at the end
        """.trimIndent(),
        text2 = """
            Line with no space
            Line with one space
            Line with two spaces
            No newline at the end
            
        """.trimIndent(),
        difference = """
            Text comparison failed:
            ┌─ actual
            │ Line with no space
            │ Line with one space⠀
            │ Line with two spaces⠀⠀
            │ No newline at the end
            └─ differs from expected
            │ Line with no space
            │ Line with one space
            │ Line with two spaces
            │ No newline at the end
            │ 
            └─ differences
              • line 2: trailing whitespace difference
                changes: "Line with one space{+⠀+}"
              • line 3: trailing whitespace difference
                changes: "Line with two spaces{+⠀+}{+⠀+}"
              • structural: missing newline at end of file
            Note: ⠀ represents a space character
        """.trimIndent()
    )

    @Test
    fun `should handle complex differences`() = assertDifference(
        text1 = """
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
        text2 = """
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
            ┌─ actual
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
            └─ differs from expected
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
                + <meta charset="utf-8">
              • line 7: 'container' vs 'main-container'
                changes: '<div class="[-c-][-o-][-n-][-t-][-a-][-i-][-n-][-e-][-r-]{+m+}{+a+}{+i+}{+n+}{+-+}{+c+}{+o+}{+n+}{+t+}{+a+}{+i+}{+n+}{+e+}{+r+}">'
              • line 8: 'Hello World' vs 'Hello, World!'
                changes: "Hello[-⠀-]{+,+}{+⠀+}World{+!+}"
        """.trimIndent()
    )

    @Test
    fun `should handle differences in markdown content`() = assertDifference(
        text1 = """
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
        text2 = """
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
            ┌─ actual
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
            └─ differs from expected
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
              • line 5: characters differ
                changes: "This is a paragraph with [-*-]{+_+}italic[-*-]{+_+} and"
              • line 6: characters differ
                changes: "[-*-][-*-]{+_+}{+_+}bold[-*-][-*-]{+_+}{+_+} text. It continues on the"
              • line 12: missing period at end of line
                changes: "> with multiple lines{+.+}"
              • line 16: indentation and missing semicolon
                changes: "[-⠀-][-⠀-][-⠀-][-⠀-]println("Hello"){+;+}"
        """.trimIndent()
    )

    private fun assertDifference(text1: String, text2: String, difference: String) {
        val diff = text1 diff text2
        assertEquals(expected = difference, actual = diff)
        if (diff.isNotEmpty()) {
            fail("The actual difference message was:\n${diff}")
        }
    }

}