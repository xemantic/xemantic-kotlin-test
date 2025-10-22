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
        // when
        val error = assertFailsWith<AssertionError> {
            "foo" sameAs "bar"
        }

        // then
        error.message sameAs """
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "line1\n line2 \n"
        val actual = "line1\nline2\t\n"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "new line\nline1\nline2\n"
        val actual = "line1\nline2\n"

        // when
        val error = assertFailsWith<AssertionError> {
             actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "line1\nline2"
        val actual = "line1\nline2\nline3"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // when
        val error = assertFailsWith<AssertionError> {
            "content" sameAs ""
        }

        // then
        error.message sameAs """
            --- expected
            +++ actual
            @@ -0,0 +1 @@
            +content
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference when comparing non-empty string with empty`() {
        // when
        val error = assertFailsWith<AssertionError> {
            "" sameAs "content"
        }

        // then
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1 +0,0 @@
            -content
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with pure deletion in middle`() {
        // given
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // when
        val error = assertFailsWith<AssertionError> {
            "line\n" sameAs "line"
        }

        // then
        error.message sameAs """
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
        // when
        val error = assertFailsWith<AssertionError> {
            "line" sameAs "line\n"
        }

        // then
        error.message sameAs """
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
        // given
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

        // when
        val error = assertFailsWith<AssertionError> {
            tzu sameAs lao
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "line1\n\nline2\n"
        val actual = "line1\n\n\nline2\n"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs "--- expected\n+++ actual\n@@ -1,3 +1,4 @@\n line1\n \n+\n line2\n"
    }

    @Test
    fun `should fail and report difference with only whitespace differences`() {
        // given
        val expected = "\t"
        val actual = "   "

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs "--- expected\n+++ actual\n@@ -1 +1 @@\n-\t\n\\ No newline at end of file\n+   \n\\ No newline at end of file\n"
    }

    @Test
    fun `should fail and report difference with unicode and emoji characters`() {
        // given
        val expected = "Hello 🌍"
        val actual = "Hello 👋"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "Hello world\nこんにちは世界\n"
        val actual = "Привет мир\nこんにちは世界\n"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "one\ntwo\nthree\n"
        val actual = "alpha\nbeta\ngamma\n"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val longLine = "a".repeat(2000)
        val modifiedLongLine = "a".repeat(1999) + "b"

        // when
        val error = assertFailsWith<AssertionError> {
            modifiedLongLine sameAs longLine
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "\n\n\n"
        val actual = "\n\n"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1,3 +1,2 @@
             
             
            -

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with lines that look like diff markers`() {
        // given
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        // two changes that are 3 lines apart (within default context of 3)
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // should result in a single merged hunk
        error.message sameAs """
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
        // given
        // two changes that are more than 6 lines apart (beyond 2x context of 3)
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

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // should result in separate hunks
        error.message sameAs """
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
        // given
        val expected = "line1\nline2"
        val actual = "line1\u0007\nline2\u001b"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val baseString = "a".repeat(100)
        val actual = baseString + "x"
        val expected = baseString + "y"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "start\nmiddle here\nend\n"
        val actual = "\tstart\nmiddle\there\nend\t\n"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
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
        // given
        val expected = "path/to/file\n"
        val actual = "path\\to\\file\n"

        // when
        val error = assertFailsWith<AssertionError> {
             actual sameAs expected
        }

        // then
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1 @@
            -path/to/file
            +path\to\file

        """.trimIndent()
    }

    @Test
    fun `should fail and report difference with quoted strings in content`() {
        // given
        val expected = "\"different string\"\n'single quotes'\n"
        val actual = "\"quoted string\"\n'single quotes'\n"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1,2 +1,2 @@
            -"different string"
            +"quoted string"
             'single quotes'

        """.trimIndent()
    }

    @Test
    fun `should not truncate diff when exactly at 100 changed lines threshold`() {
        // given
        val expected = (1..50).joinToString("\n") { "old$it" }
        val actual = (1..50).joinToString("\n") { "new$it" }

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // exactly 100 changed lines (50 deletes + 50 inserts)
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1,50 +1,50 @@
            -old1
            -old2
            -old3
            -old4
            -old5
            -old6
            -old7
            -old8
            -old9
            -old10
            -old11
            -old12
            -old13
            -old14
            -old15
            -old16
            -old17
            -old18
            -old19
            -old20
            -old21
            -old22
            -old23
            -old24
            -old25
            -old26
            -old27
            -old28
            -old29
            -old30
            -old31
            -old32
            -old33
            -old34
            -old35
            -old36
            -old37
            -old38
            -old39
            -old40
            -old41
            -old42
            -old43
            -old44
            -old45
            -old46
            -old47
            -old48
            -old49
            -old50
            \ No newline at end of file
            +new1
            +new2
            +new3
            +new4
            +new5
            +new6
            +new7
            +new8
            +new9
            +new10
            +new11
            +new12
            +new13
            +new14
            +new15
            +new16
            +new17
            +new18
            +new19
            +new20
            +new21
            +new22
            +new23
            +new24
            +new25
            +new26
            +new27
            +new28
            +new29
            +new30
            +new31
            +new32
            +new33
            +new34
            +new35
            +new36
            +new37
            +new38
            +new39
            +new40
            +new41
            +new42
            +new43
            +new44
            +new45
            +new46
            +new47
            +new48
            +new49
            +new50
            \ No newline at end of file

        """.trimIndent()
    }

    @Test
    fun `should truncate diff when changes exceed reasonable size for LLM consumption`() {
        // given
        // a large string with many different lines that would generate a huge diff
        // expected has 250 lines, actual has 300 lines - both exceeding the 100-line change threshold
        val largeExpected = (1000..1249).joinToString("\n") { "$it" }
        val largeActual = (2000..2299).joinToString("\n") { "$it" }

        // when
        val error = assertFailsWith<AssertionError> {
            largeActual sameAs largeExpected
        }

        // then
        // we expect the diff to show only the first 100 changed lines
        // since all lines are different, the diff will show deletions followed by insertions
        // we truncate after 100 changed lines (counting both + and - lines)
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1,100 +0,0 @@
            -1000
            -1001
            -1002
            -1003
            -1004
            -1005
            -1006
            -1007
            -1008
            -1009
            -1010
            -1011
            -1012
            -1013
            -1014
            -1015
            -1016
            -1017
            -1018
            -1019
            -1020
            -1021
            -1022
            -1023
            -1024
            -1025
            -1026
            -1027
            -1028
            -1029
            -1030
            -1031
            -1032
            -1033
            -1034
            -1035
            -1036
            -1037
            -1038
            -1039
            -1040
            -1041
            -1042
            -1043
            -1044
            -1045
            -1046
            -1047
            -1048
            -1049
            -1050
            -1051
            -1052
            -1053
            -1054
            -1055
            -1056
            -1057
            -1058
            -1059
            -1060
            -1061
            -1062
            -1063
            -1064
            -1065
            -1066
            -1067
            -1068
            -1069
            -1070
            -1071
            -1072
            -1073
            -1074
            -1075
            -1076
            -1077
            -1078
            -1079
            -1080
            -1081
            -1082
            -1083
            -1084
            -1085
            -1086
            -1087
            -1088
            -1089
            -1090
            -1091
            -1092
            -1093
            -1094
            -1095
            -1096
            -1097
            -1098
            -1099

            Diff truncated: more than 100 lines changed

            Expected: 250 lines
            Actual: 300 lines

            The differences are too extensive to show in unified diff format.
            Consider comparing smaller sections or reviewing the strings directly.

        """.trimIndent()
    }

    @Test
    fun `should truncate diff in middle of single large hunk`() {
        // given
        // a single large hunk with more than 100 changes
        val expected = (1..150).joinToString("\n") { "line$it" }
        val actual = (1..150).joinToString("\n") { "modified$it" }

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // all lines are different: 150 deletions + 150 insertions = 300 total changes
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1,100 +0,0 @@
            -line1
            -line2
            -line3
            -line4
            -line5
            -line6
            -line7
            -line8
            -line9
            -line10
            -line11
            -line12
            -line13
            -line14
            -line15
            -line16
            -line17
            -line18
            -line19
            -line20
            -line21
            -line22
            -line23
            -line24
            -line25
            -line26
            -line27
            -line28
            -line29
            -line30
            -line31
            -line32
            -line33
            -line34
            -line35
            -line36
            -line37
            -line38
            -line39
            -line40
            -line41
            -line42
            -line43
            -line44
            -line45
            -line46
            -line47
            -line48
            -line49
            -line50
            -line51
            -line52
            -line53
            -line54
            -line55
            -line56
            -line57
            -line58
            -line59
            -line60
            -line61
            -line62
            -line63
            -line64
            -line65
            -line66
            -line67
            -line68
            -line69
            -line70
            -line71
            -line72
            -line73
            -line74
            -line75
            -line76
            -line77
            -line78
            -line79
            -line80
            -line81
            -line82
            -line83
            -line84
            -line85
            -line86
            -line87
            -line88
            -line89
            -line90
            -line91
            -line92
            -line93
            -line94
            -line95
            -line96
            -line97
            -line98
            -line99
            -line100

            Diff truncated: more than 100 lines changed

            Expected: 150 lines
            Actual: 150 lines

            The differences are too extensive to show in unified diff format.
            Consider comparing smaller sections or reviewing the strings directly.

        """.trimIndent()
    }

    @Test
    fun `should truncate diff across multiple hunks`() {
        // given
        // multiple separate changes that form different hunks
        // Each range creates lines, so we need to count: 30 + 40 + 30 + 40 = 140 lines
        // Plus 3 newlines between sections = 143 lines total
        val expected = buildString {
            append((1..30).joinToString("\n") { "unchanged$it" })
            append("\n")
            append((1..40).joinToString("\n") { "change1-$it" })
            append("\n")
            append((1..30).joinToString("\n") { "unchanged$it" })
            append("\n")
            append((1..40).joinToString("\n") { "change2-$it" })
        }

        val actual = buildString {
            append((1..30).joinToString("\n") { "unchanged$it" })
            append("\n")
            append((1..40).joinToString("\n") { "modified1-$it" })
            append("\n")
            append((1..30).joinToString("\n") { "unchanged$it" })
            append("\n")
            append((1..40).joinToString("\n") { "modified2-$it" })
        }

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        val lineCount = expected.count { it == '\n' } + 1
        error.message sameAs """
            --- expected
            +++ actual
            @@ -28,46 +28,46 @@
             unchanged28
             unchanged29
             unchanged30
            -change1-1
            -change1-2
            -change1-3
            -change1-4
            -change1-5
            -change1-6
            -change1-7
            -change1-8
            -change1-9
            -change1-10
            -change1-11
            -change1-12
            -change1-13
            -change1-14
            -change1-15
            -change1-16
            -change1-17
            -change1-18
            -change1-19
            -change1-20
            -change1-21
            -change1-22
            -change1-23
            -change1-24
            -change1-25
            -change1-26
            -change1-27
            -change1-28
            -change1-29
            -change1-30
            -change1-31
            -change1-32
            -change1-33
            -change1-34
            -change1-35
            -change1-36
            -change1-37
            -change1-38
            -change1-39
            -change1-40
            +modified1-1
            +modified1-2
            +modified1-3
            +modified1-4
            +modified1-5
            +modified1-6
            +modified1-7
            +modified1-8
            +modified1-9
            +modified1-10
            +modified1-11
            +modified1-12
            +modified1-13
            +modified1-14
            +modified1-15
            +modified1-16
            +modified1-17
            +modified1-18
            +modified1-19
            +modified1-20
            +modified1-21
            +modified1-22
            +modified1-23
            +modified1-24
            +modified1-25
            +modified1-26
            +modified1-27
            +modified1-28
            +modified1-29
            +modified1-30
            +modified1-31
            +modified1-32
            +modified1-33
            +modified1-34
            +modified1-35
            +modified1-36
            +modified1-37
            +modified1-38
            +modified1-39
            +modified1-40
             unchanged1
             unchanged2
             unchanged3
            @@ -98,23 +98,3 @@
             unchanged28
             unchanged29
             unchanged30
            -change2-1
            -change2-2
            -change2-3
            -change2-4
            -change2-5
            -change2-6
            -change2-7
            -change2-8
            -change2-9
            -change2-10
            -change2-11
            -change2-12
            -change2-13
            -change2-14
            -change2-15
            -change2-16
            -change2-17
            -change2-18
            -change2-19
            -change2-20

            Diff truncated: more than 100 lines changed

            Expected: $lineCount lines
            Actual: $lineCount lines

            The differences are too extensive to show in unified diff format.
            Consider comparing smaller sections or reviewing the strings directly.

        """.trimIndent()
    }

    @Test
    fun `should handle truncation with newline markers correctly`() {
        // given
        // Large diff where last line has no newline
        val expected = (1..120).joinToString("\n") { "line$it" }
        val actual = (1..120).joinToString("\n") { "other$it" } + "extra"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // Should truncate after 100 changed lines
        // The diff will show deletions of line1-line100, then truncate
        // "extra" should not appear since we stop at 100 changes
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1,100 +0,0 @@
            -line1
            -line2
            -line3
            -line4
            -line5
            -line6
            -line7
            -line8
            -line9
            -line10
            -line11
            -line12
            -line13
            -line14
            -line15
            -line16
            -line17
            -line18
            -line19
            -line20
            -line21
            -line22
            -line23
            -line24
            -line25
            -line26
            -line27
            -line28
            -line29
            -line30
            -line31
            -line32
            -line33
            -line34
            -line35
            -line36
            -line37
            -line38
            -line39
            -line40
            -line41
            -line42
            -line43
            -line44
            -line45
            -line46
            -line47
            -line48
            -line49
            -line50
            -line51
            -line52
            -line53
            -line54
            -line55
            -line56
            -line57
            -line58
            -line59
            -line60
            -line61
            -line62
            -line63
            -line64
            -line65
            -line66
            -line67
            -line68
            -line69
            -line70
            -line71
            -line72
            -line73
            -line74
            -line75
            -line76
            -line77
            -line78
            -line79
            -line80
            -line81
            -line82
            -line83
            -line84
            -line85
            -line86
            -line87
            -line88
            -line89
            -line90
            -line91
            -line92
            -line93
            -line94
            -line95
            -line96
            -line97
            -line98
            -line99
            -line100

            Diff truncated: more than 100 lines changed

            Expected: 120 lines
            Actual: 120 lines

            The differences are too extensive to show in unified diff format.
            Consider comparing smaller sections or reviewing the strings directly.

        """.trimIndent()
    }

    @Test
    fun `should truncate when first 100 changes are all deletions`() {
        // given
        val expected = (1..200).joinToString("\n") { "line$it" }
        val actual = "single line"

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // Should show first 100 deletions only, then truncate
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1,100 +0,0 @@
            -line1
            -line2
            -line3
            -line4
            -line5
            -line6
            -line7
            -line8
            -line9
            -line10
            -line11
            -line12
            -line13
            -line14
            -line15
            -line16
            -line17
            -line18
            -line19
            -line20
            -line21
            -line22
            -line23
            -line24
            -line25
            -line26
            -line27
            -line28
            -line29
            -line30
            -line31
            -line32
            -line33
            -line34
            -line35
            -line36
            -line37
            -line38
            -line39
            -line40
            -line41
            -line42
            -line43
            -line44
            -line45
            -line46
            -line47
            -line48
            -line49
            -line50
            -line51
            -line52
            -line53
            -line54
            -line55
            -line56
            -line57
            -line58
            -line59
            -line60
            -line61
            -line62
            -line63
            -line64
            -line65
            -line66
            -line67
            -line68
            -line69
            -line70
            -line71
            -line72
            -line73
            -line74
            -line75
            -line76
            -line77
            -line78
            -line79
            -line80
            -line81
            -line82
            -line83
            -line84
            -line85
            -line86
            -line87
            -line88
            -line89
            -line90
            -line91
            -line92
            -line93
            -line94
            -line95
            -line96
            -line97
            -line98
            -line99
            -line100

            Diff truncated: more than 100 lines changed

            Expected: 200 lines
            Actual: 1 lines

            The differences are too extensive to show in unified diff format.
            Consider comparing smaller sections or reviewing the strings directly.

        """.trimIndent()
    }

    @Test
    fun `should truncate when first 100 changes are all insertions`() {
        // given
        val expected = "single line"
        val actual = (1..200).joinToString("\n") { "line$it" }

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // Runs full Myers algorithm (edit distance 201 < 500), then truncates output at 100 changed lines
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1,99 @@
            -single line
            \ No newline at end of file
            +line1
            +line2
            +line3
            +line4
            +line5
            +line6
            +line7
            +line8
            +line9
            +line10
            +line11
            +line12
            +line13
            +line14
            +line15
            +line16
            +line17
            +line18
            +line19
            +line20
            +line21
            +line22
            +line23
            +line24
            +line25
            +line26
            +line27
            +line28
            +line29
            +line30
            +line31
            +line32
            +line33
            +line34
            +line35
            +line36
            +line37
            +line38
            +line39
            +line40
            +line41
            +line42
            +line43
            +line44
            +line45
            +line46
            +line47
            +line48
            +line49
            +line50
            +line51
            +line52
            +line53
            +line54
            +line55
            +line56
            +line57
            +line58
            +line59
            +line60
            +line61
            +line62
            +line63
            +line64
            +line65
            +line66
            +line67
            +line68
            +line69
            +line70
            +line71
            +line72
            +line73
            +line74
            +line75
            +line76
            +line77
            +line78
            +line79
            +line80
            +line81
            +line82
            +line83
            +line84
            +line85
            +line86
            +line87
            +line88
            +line89
            +line90
            +line91
            +line92
            +line93
            +line94
            +line95
            +line96
            +line97
            +line98
            +line99

            Diff truncated: more than 100 lines changed

            Expected: 1 lines
            Actual: 200 lines

            The differences are too extensive to show in unified diff format.
            Consider comparing smaller sections or reviewing the strings directly.

        """.trimIndent()
    }

    @Test
    fun `should handle extremely large string comparison without running out of memory`() {
        // given
        // This test verifies that comparing very large strings doesn't cause OOM
        // Early termination at d=500 prevents the Myers algorithm from exhausting memory
        val expected = "small expected string"
        // Create a very large actual string with 10,000 completely different lines
        val actual = (1..10_000).joinToString("\n") { "line$it" }

        // when
        val error = assertFailsWith<AssertionError> {
            actual sameAs expected
        }

        // then
        // With early termination at d=500, the diff computes limited operations
        // Then truncateIfNeeded limits output to first 100 changed lines
        error.message sameAs """
            --- expected
            +++ actual
            @@ -1 +1,99 @@
            -small expected string
            \ No newline at end of file
            +line1
            +line2
            +line3
            +line4
            +line5
            +line6
            +line7
            +line8
            +line9
            +line10
            +line11
            +line12
            +line13
            +line14
            +line15
            +line16
            +line17
            +line18
            +line19
            +line20
            +line21
            +line22
            +line23
            +line24
            +line25
            +line26
            +line27
            +line28
            +line29
            +line30
            +line31
            +line32
            +line33
            +line34
            +line35
            +line36
            +line37
            +line38
            +line39
            +line40
            +line41
            +line42
            +line43
            +line44
            +line45
            +line46
            +line47
            +line48
            +line49
            +line50
            +line51
            +line52
            +line53
            +line54
            +line55
            +line56
            +line57
            +line58
            +line59
            +line60
            +line61
            +line62
            +line63
            +line64
            +line65
            +line66
            +line67
            +line68
            +line69
            +line70
            +line71
            +line72
            +line73
            +line74
            +line75
            +line76
            +line77
            +line78
            +line79
            +line80
            +line81
            +line82
            +line83
            +line84
            +line85
            +line86
            +line87
            +line88
            +line89
            +line90
            +line91
            +line92
            +line93
            +line94
            +line95
            +line96
            +line97
            +line98
            +line99

            Diff truncated: more than 100 lines changed

            Expected: 1 lines
            Actual: 10000 lines

            The differences are too extensive to show in unified diff format.
            Consider comparing smaller sections or reviewing the strings directly.

        """.trimIndent()
    }

}
