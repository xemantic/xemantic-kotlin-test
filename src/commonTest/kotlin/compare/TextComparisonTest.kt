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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

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
              • at position 0: expected 'b' but was 'f'
              • at position 1: expected 'a' but was 'o'
              • at position 2: expected 'r' but was 'o'
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
              • line 3: extra 'a' at position 8 in "This is a paragraph"
              • line 4: text differs: "with two lines." vs "with 2 lines."
              • line 7: text differs: "* List item 2" vs "* List item three"
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
              • line 2: indentation differs
                actual:   "    <p>Hello</p>" (4 spaces)
                expected: "   <p>Hello</p>"  (3 spaces)
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
            └─ differences
              • line 2: trailing whitespace: actual has 1 space
              • line 3: trailing whitespace: actual has 2 spaces
              • at the end: expected has a newline character
            Note: ⠀ represents a space character
        """.trimIndent()
    )

    @Test
    fun `should handle complex HTML structure differences`() = assertErrorMessage(
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
              • line 4: missing next line with <meta> tag
              • line 7: attribute value differs:
                actual:   class="container"
                expected: class="main-container"
              • line 8: text content differs:
                actual:   "Hello World"
                expected: "Hello, World!"
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
              • line 5: markdown syntax differs: "*italic*" vs "_italic_"
              • line 6: markdown syntax differs: "**bold**" vs "__bold__"
              • line 13: missing period at end of line
              • line 16-17: code block differences:
                - indentation: 4 spaces vs 2 spaces
                - missing semicolon after println("Hello")
        """.trimIndent()
    )

    @Test
    fun `should handle empty or blank line differences`() = assertErrorMessage(
        actual = """
            First line

            
            Last line
        """.trimIndent(),
        expected = """
            First line

            Last line
        """.trimIndent(),
        expectedMessage = """
            Text comparison failed:
            ┌─ actual
            │ First line
            │ 
            │ 
            │ Last line
            └─ differs from expected
            │ First line
            │ 
            │ Last line
            └─ differences
              • extra empty line at line 3
              • line count differs: actual has 4 lines, expected has 3 lines
        """.trimIndent()
    )

    @Test
    fun `should handle Unicode and special character differences`() = assertErrorMessage(
        actual = """
            Hello → World
            Copyright © 2025
            Em — dash
            Temperature: 20°C
        """.trimIndent(),
        expected = """
            Hello -> World
            Copyright (c) 2025
            Em -- dash
            Temperature: 20℃
        """.trimIndent(),
        expectedMessage = """
            Text comparison failed:
            ┌─ actual
            │ Hello → World
            │ Copyright © 2025
            │ Em — dash
            │ Temperature: 20°C
            └─ differs from expected
            │ Hello -> World
            │ Copyright (c) 2025
            │ Em -- dash
            │ Temperature: 20℃
            └─ differences
              • line 1: Unicode arrow (→) vs ASCII arrow (->)
              • line 2: Unicode symbol (©) vs ASCII representation "(c)"
              • line 3: Unicode em dash (—) vs ASCII dashes (--)
              • line 4: degree symbol (°C) vs Unicode celsius symbol (℃)
        """.trimIndent()
    )

    private fun assertErrorMessage(actual: String, expected: String, expectedMessage: String) {
        val error = assertFailsWith<AssertionError> {
            actual shouldEqual expected
        }
        assertNotNull(error.message)
        assertEquals(expected, actual)
    }

}
