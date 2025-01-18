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

/**
 * Compares two strings and returns a formatted string describing their differences.
 *
 * This function compares the [original] and the [revised] strings passed as parameters
 * and returns a description of differences.
 * The output format is intended to be as easy as possible to process
 * by the Large Language Model (LLM) like Claude from Anthropic.
 *
 * @param original the original string to compare.
 * @param revised the revised string to compare.
 * @return the formatted description of differences or an empty string if both parameters are equal.
 */
public fun diff(
    original: String,
    revised: String
): String {
    if (original == revised) return ""

    val originalLines = original.lines()
    val revisedLines = revised.lines()

    return buildString {
        append("""
            Text comparison failed:
            Format description:
            • [-c-] shows deleted character
            • {+c+} shows added character
            • Spaces are marked explicitly in changes
            • Changes are shown character by character
            
        """.trimIndent())

        // Original text section
        append("\n┌─ original\n")
        originalLines.forEach { line ->
            append("│ $line\n")
        }

        // Revised text section
        append("└─ differs from revised\n")
        revisedLines.forEach { line ->
            append("│ $line\n")
        }

        append("└─ differences\n")

        // TODO finish this implementation
    }
}
