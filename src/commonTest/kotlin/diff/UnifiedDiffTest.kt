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

package com.xemantic.kotlin.test.diff

import kotlin.test.Test
import kotlin.test.assertEquals

class UnifiedDiffTest {

    private fun toLines(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        // Normalize CRLF/CR to LF and split
        val normalized = text.replace("\r\n", "\n").replace('\r', '\n')
        val parts = normalized.split('\n')
        // Drop a trailing empty element if text ended with a newline to simulate typical line lists
        return if (parts.isNotEmpty() && parts.last().isEmpty()) parts.dropLast(1) else parts
    }

    private fun diff(originalName: String, revisedName: String, original: String, revised: String): List<String> =
        UnifiedDiff.generate(originalName, revisedName, toLines(original), toLines(revised), contextSize = 0)

    @Test
    fun `should produce only headers when there are no differences`() {
        val out = diff("a.txt", "b.txt", "one\ntwo\nthree", "one\ntwo\nthree")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
        ), out)
    }

    @Test
    fun `should handle insertion at end`() {
        val out = diff("a.txt", "b.txt", "one\ntwo", "one\ntwo\nthree")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -3,0 +3,1 @@",
            "+three",
        ), out)
    }

    @Test
    fun `should handle deletion at end`() {
        val out = diff("a.txt", "b.txt", "one\ntwo\nthree", "one\ntwo")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -3,1 +3,0 @@",
            "-three",
        ), out)
    }

    @Test
    fun `should handle insertion at start`() {
        val out = diff("a.txt", "b.txt", "one\ntwo", "zero\none\ntwo")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -1,0 +1,1 @@",
            "+zero",
        ), out)
    }

    @Test
    fun `should handle deletion at start`() {
        val out = diff("a.txt", "b.txt", "zero\none\ntwo", "one\ntwo")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -1,1 +1,0 @@",
            "-zero",
        ), out)
    }

    @Test
    fun `should handle single line replacement`() {
        val out = diff("a.txt", "b.txt", "one", "two")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -1,1 +1,1 @@",
            "-one",
            "+two",
        ), out)
    }

    @Test
    fun `should handle middle line replacement in one hunk`() {
        val out = diff("a.txt", "b.txt", "a\nx\nc", "a\ny\nc")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -2,1 +2,1 @@",
            "-x",
            "+y",
        ), out)
    }

    @Test
    fun `should create multiple hunks for separate changes`() {
        val out = diff(
            "a.txt", "b.txt",
            "a\nx\nc\nz\ne",
            "a\ny\nc\nw\ne",
        )
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -2,1 +2,1 @@",
            "-x",
            "+y",
            "@@ -4,1 +4,1 @@",
            "-z",
            "+w",
        ), out)
    }

    @Test
    fun `should handle empty original - pure insertion`() {
        val out = diff("a.txt", "b.txt", "", "one\ntwo")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -1,0 +1,2 @@",
            "+one",
            "+two",
        ), out)
    }

    @Test
    fun `should handle empty revised - pure deletion`() {
        val out = diff("a.txt", "b.txt", "one\ntwo", "")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -1,2 +1,0 @@",
            "-one",
            "-two",
        ), out)
    }

    @Test
    fun `should treat whitespace changes as differences`() {
        val out = diff("a.txt", "b.txt", "a", "a ")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -1,1 +1,1 @@",
            "-a",
            "+a ",
        ), out)
    }

    @Test
    fun `should handle unicode characters`() {
        val out = diff("a.txt", "b.txt", "café\n😀", "cafe\n😃")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -1,2 +1,2 @@",
            "-café",
            "-😀",
            "+cafe",
            "+😃",
        ), out)
    }

    @Test
    fun `should be robust to CRLF inputs`() {
        val out = diff("a.txt", "b.txt", "one\r\ntwo\r\nthree", "one\r\ntwo\r\nTHREE")
        assertEquals(listOf(
            "--- a.txt",
            "+++ b.txt",
            "@@ -3,1 +3,1 @@",
            "-three",
            "+THREE",
        ), out)
    }
}
