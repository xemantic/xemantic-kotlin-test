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

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.fail

class TextComparisonTest {

    @Test
    fun `should pass if strings are the same`() {
        "foo" shouldEqual "foo"
    }

    @Test
    fun `should report if strings are different`() = assertErrorMessage(
        actual = "foo",
        expected = "bar",
        expectedMessage = """
            Text comparison failed:
            ┌─ actual
            │ foo
            └─ differs from expected
            │ bar
            └─ differences
              • line 1: strings differ
                - actual:   "foo"
                - expected: "bar"
                - changes:  "[-f-o-o-]{+b+a+r+}"
        """.trimIndent()
    )

    @Test
    fun `should fail if multiline strings are different`() = assertErrorMessage(
        actual = """
            # Heading

            This is a paragraph
            with two lines.

            * List item 1
            * List item 2
        """.trimIndent(),
        expected = """
            # Heading

            This is paragraph
            with 2 lines.

            * List item 1
            * List item three
        """.trimIndent(),
        expectedMessage = """
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
                changes: "This is {+a +}paragraph"
              • line 4: "with two lines." vs "with 2 lines."
                changes: "with [-two-]{+2+} lines."
              • line 7: "* List item 2" vs "* List item three"
                changes: "* List item [-2-]{+three+}"
        """.trimIndent()
    )

    @Test
    fun `should fail even if only whitespaces are different`() = assertErrorMessage(
        actual = """
            <div>
                <p>Hello</p>
            </div>
        """.trimIndent(),
        expected = """
            <div>
               <p>Hello</p>
            </div>
        """.trimIndent(),
        expectedMessage = """
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
                - changes:  "{+ +}<p>Hello</p>"
        """.trimIndent()
    )

    @Test
    fun `should show differences in trailing whitespace`() = assertErrorMessage(
        actual = """
            Line with no space
            Line with one space 
            Line with two spaces  
            No newline at the end""".trimIndent(),
        expected = """
            Line with no space
            Line with one space
            Line with two spaces
            No newline at the end
        """.trimIndent(),
        expectedMessage = """
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
              • line 3: trailing whitespace difference
              • structural: missing newline at end of file
            Note: ⠀ represents a space character
        """.trimIndent()
    )

    @Test
    fun `should handle complex differences`() = assertErrorMessage(
        actual = """
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
        expected = """
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
        expectedMessage = """
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
                changes: '<div class="[-container-]{+main-container+}">'
              • line 8: 'Hello World' vs 'Hello, World!'
                changes: 'Hello[- -]{+,+} World{+!+}'
        """.trimIndent()
    )

    @Test
    fun `should handle differences in markdown content`() = assertErrorMessage(
        actual = """
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
        expected = """
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
        expectedMessage = """
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
                changes: "[-**-]{+__+}bold[-**-]{+__+} text. It continues on the"
              • line 12: missing period at end of line
                changes: "> with multiple lines{+.+}"
              • line 16: indentation and missing semicolon
                changes: "[-    -]{+  +}println("Hello"){+;+}"
        """.trimIndent()
    )

    private fun assertErrorMessage(actual: String, expected: String, expectedMessage: String) {
        val error = assertFailsWith<AssertionError> {
            actual shouldEqual expected
        }
        assertNotNull(error.message)
        fail("The actual error message was:\n${error.message}")
    }

}