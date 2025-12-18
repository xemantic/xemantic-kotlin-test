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

package com.xemantic.kotlin.test.text

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.sameAs
import com.xemantic.kotlin.test.should
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ChunkedRandomlyTest {

    @Test
    fun `should chunk string randomly and reconstruct original`() {
        // given
        val text = """
            The quick brown fox jumps over the lazy dog. This pangram contains every letter
            of the English alphabet at least once. It has been used since the late 19th
            century to test typewriters and computer keyboards, as well as to display font
            samples. The phrase is notable for its brevity while still containing all 26
            letters, making it an ideal test case for text processing algorithms.

            In addition to its practical applications, the sentence has become a cultural
            reference, appearing in various forms of media and literature. Software developers
            often use it when testing text rendering, chunking algorithms, and streaming
            implementations. The fox, in this context, represents the flow of data being
            processed in discrete segments, while the lazy dog symbolizes the patient
            accumulation of those segments into a coherent whole.
        """.trimIndent()

        // when
        val chunks = text.chunkedRandomly().toList()

        // then
        assert(chunks.size > 1)
        chunks.joinToString("") sameAs text
    }

    @Test
    fun `should return empty sequence for empty string`() {
        // when
        val chunks = "".chunkedRandomly().toList()

        // then
        assert(chunks.isEmpty())
    }

    @Test
    fun `should handle single character string`() {
        // when
        val chunks = "X".chunkedRandomly().toList()

        // then
        chunks should {
            have(size == 1)
            have(first() == "X")
        }
    }

    @Test
    fun `should produce fixed size chunks when minLength equals maxLength`() {
        // given
        val text = "abcdefghij" // 10 characters

        // when
        val chunks = text.chunkedRandomly(minLength = 2, maxLength = 2).toList()

        // then
        chunks should {
            have(size == 5)
            have(all { it.length == 2 })
        }
        chunks.joinToString("") sameAs text
    }

    @Test
    fun `should respect chunk size bounds`() {
        // given
        val text = "The quick brown fox jumps over the lazy dog"
        val minLength = 3
        val maxLength = 7

        // when
        val chunks = text.chunkedRandomly(minLength = minLength, maxLength = maxLength).toList()

        // then
        val allButLast = chunks.dropLast(1)
        allButLast should {
            have(all { it.length in minLength..maxLength })
        }
        // last chunk may be smaller if remaining text is shorter than minLength
        chunks.last() should {
            have(length in 1..maxLength)
        }
        chunks.joinToString("") sameAs text
    }

    @Test
    fun `should handle string shorter than minLength`() {
        // given
        val text = "hi" // 2 characters

        // when
        val chunks = text.chunkedRandomly(minLength = 5, maxLength = 10).toList()

        // then
        chunks should {
            have(size == 1)
            have(first() == "hi")
        }
    }

    @Test
    fun `should throw when minLength is greater than maxLength`() {
        // when
        val error = assertFailsWith<IllegalArgumentException> {
            "test".chunkedRandomly(minLength = 10, maxLength = 5)
        }

        // then
        assert(error.message == "minLength (10) must be <= maxLength (5)")
    }

}
