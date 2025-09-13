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

package com.xemantic.kotlin.test

import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The [sameAs] reports differences in unified diff format.
 */
class SameAsTest {

    @Test
    fun `should pass on equal strings`() {
        "" sameAs ""
        "foo" sameAs "foo"
    }

    @Test
    fun `should fail and report difference on different single line strings`() {
        assertFailsWith<AssertionError> {
            "foo" sameAs "bar"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,1 +1,1 @@
            -bar
            +foo
        """.trimIndent()
    }

    @Test
    fun `should fail and report difference on multiline strings`() {
        val actual = """
            line1
            line2
            line3
        """.trimIndent()
        val expected = """
            line1
            modified line2
            line3
            line4
        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -2,1 +2,1 @@
            -modified line2
            +line2
            @@ -4,1 +4,0 @@
            -line4
        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with empty strings`() {
        assertFailsWith<AssertionError> {
            "content" sameAs ""
        }.message sameAs """
            |--- expected
            |+++ actual
            |@@ -1,1 +1,1 @@
            |-(empty)
            |+content
        """.trimMargin()
    }

    @Test
    fun `should fail and report difference with whitespace differences`() {
        assertFailsWith<AssertionError> {
            "line1\nline2\t" sameAs "line1\n line2 "
        }.message sameAs "--- expected\n" +
            "+++ actual\n" +
            "@@ -2,1 +2,1 @@\n" +
            "- line2 \n" +  // Note: space at the end
            "+line2\t"      // Note: tab at the end
    }

    @Test
    fun `should fail and report difference with addition at beginning`() {
        assertFailsWith<AssertionError> {
            "line1\nline2" sameAs "new line\nline1\nline2"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,1 +1,0 @@
            -new line
        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with addition at end`() {
        assertFailsWith<AssertionError> {
            "line1\nline2\nline3" sameAs "line1\nline2"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -3,0 +3,1 @@
            +line3
        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with mixed changes`() {
        val actual = """
            function example() {
              return "hello world";
            }
        """.trimIndent()
        val expected = """
            function example() {
              const greeting = "hello";
              return greeting + " world";
            }
        """.trimIndent()

        val exception = assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -2,2 +2,1 @@
            -  const greeting = "hello";
            -  return greeting + " world";
            +  return "hello world";
        """.trimIndent()
    }

}
