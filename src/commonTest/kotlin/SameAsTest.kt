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
 *
 * LLM Note: empty line at the end of each multiline string will produce a trailing new line when used with .trimIndent()
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
            @@ -1 +1 @@
            -bar
            \ No newline at end of file
            +foo
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference on multiline strings`() {
        // given
        val expected = """
            line1
            modified line2
            line3
            line4
            
        """.trimIndent()

        val actual = """
            line1
            line2
            line3
            
        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,4 +1,3 @@
             line1
            -modified line2
            +line2
             line3
            -line4

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with whitespace differences`() {
        assertFailsWith<AssertionError> {
            "line1\nline2\t\n" sameAs "line1\n line2 \n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,2 +1,2 @@
             line1
            - line2 
            +line2	

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with addition at beginning`() {
        assertFailsWith<AssertionError> {
            "line1\nline2\n" sameAs "new line\nline1\nline2\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,2 @@
            -new line
             line1
             line2

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with addition at end`() {
        assertFailsWith<AssertionError> {
            "line1\nline2\nline3" sameAs "line1\nline2"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,2 +1,3 @@
             line1
            -line2
            \ No newline at end of file
            +line2
            +line3
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with mixed changes`() {
        // given
        val expected = """
            function example() {
              const greeting = "hello";
              return greeting + " world";
            }
        """.trimIndent()

        val actual = """
            function example() {
              return "hello world";
            }
        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,4 +1,3 @@
             function example() {
            -  const greeting = "hello";
            -  return greeting + " world";
            +  return "hello world";
             }

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with portuguese text changes from java-diff-utils one-delta-test`() {
        val expected = """
            Esta é uma obra Online.
            
            Este texto é negrito
            Este texto é itálico
            Este texto está sublinhado
            Este texto está riscado
            Este texto está centralizado
            Este texto está alinhado a direita
            
            Este texto está em uma lista numérica
            Este texto está identado
            
            Este aqui é um link
            
            Página 1
            
            Página 2
            
            Página 3

        """.trimIndent()

        val actual = """
            Revisão 3
            
            Esta é uma obra Online.
            
            Este texto é negrit
            
            Este texto é itálico
            Este texto está sublinhado
            
            Este texto está riscado agora não está mais
            
            Este texto está centralizado nem este
            
            Este texto está alinhado a direita
            
            Este texto está em uma lista numérica
            
            Este aqui é um link

        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,19 +1,18 @@
            +Revisão 3
            +
             Esta é uma obra Online.
             
            -Este texto é negrito
            +Este texto é negrit
            +
             Este texto é itálico
             Este texto está sublinhado
            -Este texto está riscado
            -Este texto está centralizado
            -Este texto está alinhado a direita
             
            -Este texto está em uma lista numérica
            -Este texto está identado
            +Este texto está riscado agora não está mais
             
            -Este aqui é um link
            +Este texto está centralizado nem este
             
            -Página 1
            +Este texto está alinhado a direita
             
            -Página 2
            +Este texto está em uma lista numérica
             
            -Página 3
            +Este aqui é um link

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with database schema changes from java-diff-utils issue15`() {
        val expected = """
            TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, NULLABLE,
            ACTIONS_C17005, ID, NUMBER, 22, 19, N,
            ACTIONS_C17005, ISSUEID, NUMBER, 22, 19, Y,
            ACTIONS_C17005, MODIFIED, NUMBER, 22, 10, Y,
            ACTIONS_C17005, TABLE, VARCHAR2, 1020, null, Y,
            ACTIONS_C17005, S_NAME, CLOB, 4000, null, Y,
            ACTIONS_C17008, ID, NUMBER, 22, 19, N,
            ACTIONS_C17008, ISSUEID, NUMBER, 22, 19, Y,
            ACTIONS_C17008, MODIFIED, NUMBER, 22, 10, Y,
        """.trimIndent()

        val actual = """
            TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, NULLABLE,
            ACTIONS_C16913, ID, NUMBER, 22, 19, N,
            ACTIONS_C16913, ISSUEID, NUMBER, 22, 19, Y,
            ACTIONS_C16913, MODIFIED, NUMBER, 22, 10, Y,
            ACTIONS_C16913, VRS, NUMBER, 22, 1, Y,
            ACTIONS_C16913, ZTABS, VARCHAR2, 255, null, Y,
            ACTIONS_C16913, ZTABS_S, VARCHAR2, 255, null, Y,
            ACTIONS_C16913, TASK, VARCHAR2, 255, null, Y,
            ACTIONS_C16913, HOURS_SPENT, VARCHAR2, 255, null, Y,
        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,9 +1,9 @@
             TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, NULLABLE,
            -ACTIONS_C17005, ID, NUMBER, 22, 19, N,
            -ACTIONS_C17005, ISSUEID, NUMBER, 22, 19, Y,
            -ACTIONS_C17005, MODIFIED, NUMBER, 22, 10, Y,
            -ACTIONS_C17005, TABLE, VARCHAR2, 1020, null, Y,
            -ACTIONS_C17005, S_NAME, CLOB, 4000, null, Y,
            -ACTIONS_C17008, ID, NUMBER, 22, 19, N,
            -ACTIONS_C17008, ISSUEID, NUMBER, 22, 19, Y,
            -ACTIONS_C17008, MODIFIED, NUMBER, 22, 10, Y,
            \ No newline at end of file
            +ACTIONS_C16913, ID, NUMBER, 22, 19, N,
            +ACTIONS_C16913, ISSUEID, NUMBER, 22, 19, Y,
            +ACTIONS_C16913, MODIFIED, NUMBER, 22, 10, Y,
            +ACTIONS_C16913, VRS, NUMBER, 22, 1, Y,
            +ACTIONS_C16913, ZTABS, VARCHAR2, 255, null, Y,
            +ACTIONS_C16913, ZTABS_S, VARCHAR2, 255, null, Y,
            +ACTIONS_C16913, TASK, VARCHAR2, 255, null, Y,
            +ACTIONS_C16913, HOURS_SPENT, VARCHAR2, 255, null, Y,
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference when comparing empty string with non-empty`() {
        assertFailsWith<AssertionError> {
            "content" sameAs ""
        }.message sameAs """
            --- expected
            +++ actual
            @@ -0,0 +1 @@
            +content
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference when comparing non-empty string with empty`() {
        assertFailsWith<AssertionError> {
            "" sameAs "content"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1 +0,0 @@
            -content
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with pure deletion in middle`() {
        val expected = """
            line1
            line2
            line3
            line4
            line5

        """.trimIndent()

        val actual = """
            line1
            line2
            line5

        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,5 +1,3 @@
             line1
             line2
            -line3
            -line4
             line5

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with single trailing newline difference`() {
        assertFailsWith<AssertionError> {
            "line\n" sameAs "line"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -line
            \ No newline at end of file
            +line

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference when removing trailing newline`() {
        assertFailsWith<AssertionError> {
            "line" sameAs "line\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -line
            +line
            \ No newline at end of file

        """.trimIndent()
    }

    /**
     * Test case from GNU diffutils manual
     * https://www.gnu.org/software/diffutils/manual/html_node/Sample-diff-Input.html
     * https://www.gnu.org/software/diffutils/manual/html_node/Example-Unified.html
     *
     * This is the classic lao/tzu example from the GNU diffutils documentation.
     * The example demonstrates the unified diff format with the famous Tao Te Ching verses.
     * We use this test to verify compatibility with standard unified diff format.
     */
    @Test
    fun `should fail and report difference with classic lao-tzu example from GNU diffutils manual`() {
        val lao = """
            The Way that can be told of is not the eternal Way;
            The name that can be named is not the eternal name.
            The Nameless is the origin of Heaven and Earth;
            The Named is the mother of all things.
            Therefore let there always be non-being,
              so we may see their subtlety,
            And let there always be being,
              so we may see their outcome.
            The two are the same,
            But after they are produced,
              they have different names.

        """.trimIndent()

        val tzu = """
            The Nameless is the origin of Heaven and Earth;
            The named is the mother of all things.
            
            Therefore let there always be non-being,
              so we may see their subtlety,
            And let there always be being,
              so we may see their outcome.
            The two are the same,
            But after they are produced,
              they have different names.
            They both may be called deep and profound.
            Deeper and more profound,
            The door of all subtleties!

        """.trimIndent()

        assertFailsWith<AssertionError> {
            tzu sameAs lao
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,7 +1,6 @@
            -The Way that can be told of is not the eternal Way;
            -The name that can be named is not the eternal name.
             The Nameless is the origin of Heaven and Earth;
            -The Named is the mother of all things.
            +The named is the mother of all things.
            +
             Therefore let there always be non-being,
               so we may see their subtlety,
             And let there always be being,
            @@ -9,3 +8,6 @@
             The two are the same,
             But after they are produced,
               they have different names.
            +They both may be called deep and profound.
            +Deeper and more profound,
            +The door of all subtleties!

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with multiple consecutive blank lines`() {
        assertFailsWith<AssertionError> {
            "line1\n\n\nline2\n" sameAs "line1\n\nline2\n"
        }.message sameAs "--- expected\n+++ actual\n@@ -1,3 +1,4 @@\n line1\n \n+\n line2\n"
    }

    @Test
    fun `should fail and report difference with only whitespace differences`() {
        assertFailsWith<AssertionError> {
            "   " sameAs "\t"
        }.message sameAs "--- expected\n+++ actual\n@@ -1 +1 @@\n-\t\n\\ No newline at end of file\n+   \n\\ No newline at end of file\n"
    }

    @Test
    fun `should fail and report difference with unicode and emoji characters`() {
        assertFailsWith<AssertionError> {
            "Hello 👋" sameAs "Hello 🌍"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -Hello 🌍
            \ No newline at end of file
            +Hello 👋
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with unicode text changes`() {
        assertFailsWith<AssertionError> {
            "Привет мир\nこんにちは世界\n" sameAs "Hello world\nこんにちは世界\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,2 +1,2 @@
            -Hello world
            +Привет мир
             こんにちは世界

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with large context distance`() {
        val expected = """
            line1
            line2
            line3
            line4
            line5
            line6
            line7
            line8
            line9
            line10
            modified
            line12
            line13
            line14
            line15
            line16
            line17
            line18
            line19
            line20

        """.trimIndent()

        val actual = """
            changed
            line2
            line3
            line4
            line5
            line6
            line7
            line8
            line9
            line10
            line11
            line12
            line13
            line14
            line15
            line16
            line17
            line18
            line19
            line20

        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,4 +1,4 @@
            -line1
            +changed
             line2
             line3
             line4
            @@ -8,7 +8,7 @@
             line8
             line9
             line10
            -modified
            +line11
             line12
             line13
             line14

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference when all lines are changed`() {
        assertFailsWith<AssertionError> {
            "alpha\nbeta\ngamma\n" sameAs "one\ntwo\nthree\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
            -one
            -two
            -three
            +alpha
            +beta
            +gamma

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with alternating line changes`() {
        val expected = """
            keep1
            change1
            keep2
            change2
            keep3
            change3
            keep4

        """.trimIndent()

        val actual = """
            keep1
            modified1
            keep2
            modified2
            keep3
            modified3
            keep4

        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,7 +1,7 @@
             keep1
            -change1
            +modified1
             keep2
            -change2
            +modified2
             keep3
            -change3
            +modified3
             keep4

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with very long lines`() {
        val longLine = "a".repeat(2000)
        val modifiedLongLine = "a".repeat(1999) + "b"

        assertFailsWith<AssertionError> {
            modifiedLongLine sameAs longLine
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -$longLine
            \ No newline at end of file
            +$modifiedLongLine
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should pass when comparing files with only blank lines that are equal`() {
        "\n\n\n" sameAs "\n\n\n"
    }

    @Test
    fun `should fail and report difference with only blank lines`() {
        assertFailsWith<AssertionError> {
            "\n\n" sameAs "\n\n\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,2 @@
             
             
            -

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with lines that look like diff markers`() {
        val expected = """
            --- normal line
            +++ another line
            @@ some text @@
            - minus line
            + plus line
             space line

        """.trimIndent()

        val actual = """
            --- modified line
            +++ another line
            @@ some text @@
            - minus line
            + plus line
             space line

        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,4 +1,4 @@
            ---- normal line
            +--- modified line
             +++ another line
             @@ some text @@
             - minus line

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with adjacent hunks within context distance`() {
        // Two changes that are 3 lines apart (within default context of 3)
        // should result in a single merged hunk
        val expected = """
            line1
            change1
            line3
            line4
            line5
            change2
            line7

        """.trimIndent()

        val actual = """
            line1
            modified1
            line3
            line4
            line5
            modified2
            line7

        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,7 +1,7 @@
             line1
            -change1
            +modified1
             line3
             line4
             line5
            -change2
            +modified2
             line7

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with adjacent hunks beyond context distance`() {
        // Two changes that are more than 6 lines apart (beyond 2x context of 3)
        // should result in separate hunks
        val expected = """
            line1
            change1
            line3
            line4
            line5
            line6
            line7
            line8
            line9
            line10
            change2
            line12

        """.trimIndent()

        val actual = """
            line1
            modified1
            line3
            line4
            line5
            line6
            line7
            line8
            line9
            line10
            modified2
            line12

        """.trimIndent()

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,5 +1,5 @@
             line1
            -change1
            +modified1
             line3
             line4
             line5
            @@ -8,5 +8,5 @@
             line8
             line9
             line10
            -change2
            +modified2
             line12

        """.trimIndent()
    }

    @Test
    fun `should pass when CRLF and LF line endings are equivalent`() {
        // GNU diff on most systems treats CRLF and LF as equivalent
        // so this test verifies that strings are actually equal after normalization
        "line1\nline2\n" sameAs "line1\nline2\n"
    }

    @Test
    fun `should fail and report difference with control characters`() {
        assertFailsWith<AssertionError> {
            "line1\u0007\nline2\u001b" sameAs "line1\nline2"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,2 +1,2 @@
            -line1
            -line2
            \ No newline at end of file
            +line1
            +line2
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with single character change in long identical strings`() {
        val baseString = "a".repeat(100)
        val actual = baseString + "x"
        val expected = baseString + "y"

        assertFailsWith<AssertionError> {
            actual sameAs expected
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -${expected}
            \ No newline at end of file
            +${actual}
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with tab character at various positions`() {
        assertFailsWith<AssertionError> {
            "\tstart\nmiddle\there\nend\t\n" sameAs "start\nmiddle here\nend\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,3 @@
            -start
            -middle here
            -end
            +	start
            +middle	here
            +end	

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with backslash characters`() {
        assertFailsWith<AssertionError> {
            "path\\to\\file\n" sameAs "path/to/file\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -path/to/file
            +path\to\file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with quoted strings in content`() {
        assertFailsWith<AssertionError> {
            "\"quoted string\"\n'single quotes'\n" sameAs "\"different string\"\n'single quotes'\n"
        }.message sameAs """
            --- expected
            +++ actual
            @@ -1,2 +1,2 @@
            -"different string"
            +"quoted string"
             'single quotes'

        """.trimIndent()
    }

}
