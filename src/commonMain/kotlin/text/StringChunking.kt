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

/**
 * Splits this string into a [Sequence] of chunks with random lengths.
 *
 * Useful for testing streaming text processing, simulating chunked API responses,
 * or validating that code correctly handles arbitrarily split input.
 */
public fun CharSequence.chunkedRandomly(
    minLength: Int = 0,
    maxLength: Int = 10
): Sequence<String> {

    require(minLength <= maxLength) {
        "minLength ($minLength) must be <= maxLength ($maxLength)"
    }

    return sequence {
        val sizeRange = minLength..maxLength
        var index = 0
        while (index < length) {
            val chunkSize = maxOf(1, sizeRange.random())
            val end = minOf(index + chunkSize, length)
            yield(substring(index, end))
            index = end
        }
    }
}
